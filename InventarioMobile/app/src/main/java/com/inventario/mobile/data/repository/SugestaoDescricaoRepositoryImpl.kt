package com.inventario.mobile.data.repository

import android.util.Log
import com.inventario.mobile.data.local.dao.SugestaoDescricaoDao
import com.inventario.mobile.data.mapper.SugestaoDescricaoMapper
import com.inventario.mobile.data.remote.api.DescricaoSugestaoApi
import com.inventario.mobile.data.util.TextNormalizer
import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.ResultadoSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao
import com.inventario.mobile.domain.repository.SugestaoDescricaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação offline-first do [SugestaoDescricaoRepository] para a
 * feature "coleta com descrição livre e sugestão".
 *
 * Coordena três fontes de dados:
 *  - [DescricaoSugestaoApi]       : endpoint `GET /api/mobile/descricoes/sugestoes`.
 *  - [SugestaoDescricaoDao]       : tabela Room `sugestao_descricao`
 *                                   (cache offline particionado por inventário).
 *  - [SugestaoDescricaoMapper]    : conversão entre DTO ↔ domínio ↔ entity,
 *                                   pré-computando `descricaoNormalizada` via
 *                                   [TextNormalizer] no momento do upsert.
 *
 * Estratégia (Req 3.1, 3.2, 7.1, 7.2, 7.3):
 *  1. [buscarSugestoes] chama o servidor com `withTimeout(10_000)`.
 *  2. Em sucesso, atualiza o cache e devolve `origem = SERVIDOR`.
 *  3. Em falha (`IOException`, `HttpException`, `TimeoutCancellationException`),
 *     consulta o cache via [buscarOffline] com `limit = 10` e devolve
 *     `origem = CACHE` (se houver dados) ou `VAZIO_SEM_CACHE` (cache vazio).
 *
 * Política de erro no cache (Req 8.7): [atualizarCache] NÃO propaga exceções
 * de I/O — falhas de escrita são logadas e o conteúdo anterior do cache
 * permanece intocado.
 *
 * Threading: todas as operações de I/O rodam em `Dispatchers.IO`. Métodos
 * `suspend` simples que apenas delegam ao DAO (que já opera em IO via Room)
 * não envolvem dispatcher adicional.
 *
 * Requirements: 3.1, 3.2, 7.1, 7.2, 7.3, 7.7, 8.1, 8.2, 8.7.
 */
@Singleton
class SugestaoDescricaoRepositoryImpl @Inject constructor(
    private val dao: SugestaoDescricaoDao,
    private val api: DescricaoSugestaoApi,
    private val mapper: SugestaoDescricaoMapper,
    private val normalizer: TextNormalizer
) : SugestaoDescricaoRepository {

    companion object {
        private const val TAG = "SugestaoDescRepo"

        /** Timeout da chamada HTTP ao endpoint de sugestões (Req 3.2, 8.5, 8.6). */
        private const val TIMEOUT_MS = 10_000L

        /** Teto de itens no fallback offline (Req 7.2). */
        private const val LIMITE_OFFLINE = 10
    }

    override suspend fun buscarSugestoes(
        idInventario: Int,
        termoBusca: String,
        page: Int,
        size: Int
    ): Result<ResultadoSugestoes> = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(TIMEOUT_MS) {
                api.buscarSugestoes(
                    termoBusca = termoBusca.ifBlank { null },
                    page = page,
                    size = size,
                    idInventario = idInventario
                )
            }

            if (!response.success) {
                Log.w(
                    TAG,
                    "Servidor retornou success=false (message=${response.message}). " +
                        "Aplicando fallback offline."
                )
                return@withContext fallbackOffline(idInventario, termoBusca)
            }

            val paged = response.data
            val sugestoes = paged?.content.orEmpty().map(mapper::toDomain)

            // Atualiza o cache com o conjunto recebido. Erros de escrita são
            // swallowed pelo próprio atualizarCache (Req 8.7).
            atualizarCache(idInventario, sugestoes)

            Log.d(
                TAG,
                "Servidor OK: ${sugestoes.size} sugestões (page=$page, totalElements=" +
                    "${paged?.totalElements ?: 0L}, hasNext=${paged?.hasNext ?: false})"
            )

            Result.success(
                ResultadoSugestoes(
                    sugestoes = sugestoes,
                    origem = OrigemSugestoes.SERVIDOR,
                    totalElements = paged?.totalElements ?: sugestoes.size.toLong(),
                    hasNext = paged?.hasNext ?: false
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Timeout de ${TIMEOUT_MS}ms ao consultar sugestões. Fallback para cache.")
            fallbackOffline(idInventario, termoBusca)
        } catch (e: IOException) {
            Log.w(TAG, "Erro de rede ao consultar sugestões: ${e.message}. Fallback para cache.")
            fallbackOffline(idInventario, termoBusca)
        } catch (e: HttpException) {
            Log.w(TAG, "Erro HTTP ${e.code()} ao consultar sugestões. Fallback para cache.")
            fallbackOffline(idInventario, termoBusca)
        }
    }

    /**
     * Caminho comum de fallback para [buscarSugestoes]: consulta o cache
     * local via [buscarOffline] (limitado a [LIMITE_OFFLINE] itens) e mapeia
     * para [ResultadoSugestoes] com a origem apropriada (Req 7.2, 7.3).
     */
    private suspend fun fallbackOffline(
        idInventario: Int,
        termoBusca: String
    ): Result<ResultadoSugestoes> {
        val cache = buscarOffline(idInventario, termoBusca, limit = LIMITE_OFFLINE)
        val origem = if (cache.isEmpty()) OrigemSugestoes.VAZIO_SEM_CACHE else OrigemSugestoes.CACHE
        Log.d(TAG, "Fallback offline: ${cache.size} sugestões, origem=$origem")
        return Result.success(
            ResultadoSugestoes(
                sugestoes = cache,
                origem = origem,
                totalElements = cache.size.toLong(),
                hasNext = false
            )
        )
    }

    override suspend fun atualizarCache(
        idInventario: Int,
        sugestoes: List<SugestaoDescricao>
    ) {
        if (sugestoes.isEmpty()) {
            // Nada a persistir — evita custo de transação Room sem necessidade.
            return
        }
        try {
            val entities = sugestoes.map { mapper.toEntity(it, idInventario, normalizer) }
            dao.upsertAll(entities)
            Log.d(
                TAG,
                "Cache atualizado para inventário=$idInventario (${entities.size} entradas)"
            )
        } catch (e: Exception) {
            // Req 8.7: falha no upsert NÃO deve propagar — preservar cache anterior
            // e permitir que o fluxo de exibição continue.
            Log.e(
                TAG,
                "Falha ao atualizar cache (preservando conteúdo anterior): ${e.message}",
                e
            )
        }
    }

    override suspend fun buscarOffline(
        idInventario: Int,
        termoBusca: String,
        limit: Int
    ): List<SugestaoDescricao> {
        return try {
            val termoNormalizado = normalizer.normalize(termoBusca)
            val entities = dao.buscarFiltrado(
                idInventario = idInventario,
                termoNormalizado = termoNormalizado,
                limit = limit
            )
            entities.map(mapper::toDomain)
        } catch (e: Exception) {
            // Req 7.7: falha de leitura do cache não deve travar a UI — retornar
            // lista vazia e registrar o erro.
            Log.e(TAG, "Falha ao ler cache offline de sugestões: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun marcarColetadoLocalmente(idInventario: Int, idPatrimonio: Int) {
        dao.marcarColetado(idInventario = idInventario, idPatrimonio = idPatrimonio)
    }

    override suspend fun limparCacheDeOutrosInventarios(idInventarioAtivo: Int) {
        dao.limparOutrosInventarios(idInventarioAtivo = idInventarioAtivo)
    }
}
