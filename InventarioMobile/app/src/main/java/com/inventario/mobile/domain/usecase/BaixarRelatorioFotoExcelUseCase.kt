package com.inventario.mobile.domain.usecase

import android.content.Context
import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.domain.model.PermissaoNegadaException
import com.inventario.mobile.domain.model.SemFotosException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Use Case: Baixar o relatório fotográfico de itens sem etiqueta em Excel (.xlsx).
 *
 * Espelho de [BaixarRelatorioFotoPdfUseCase], mas para o endpoint XLSX.
 * Chama `GET api/mobile/relatorios/fotos/{inventarioId}/excel?tipo=sem_etiqueta`,
 * grava os bytes em um arquivo temporário no cache do app e retorna o [File] resultante.
 *
 * Requer role SUPERVISOR ou superior no servidor.
 *
 * Regras de negócio (idênticas ao PDF):
 * - HTTP 204 → [Result.failure] com [SemFotosException].
 * - HTTP 403 → [Result.failure] com [PermissaoNegadaException].
 * - Corpo nulo → [Result.failure] com mensagem genérica.
 * - Arquivo gravado com buffer de 8 KB para evitar [OutOfMemoryError]
 *   em planilhas grandes.
 * - Nome do arquivo: `relatorio_sem_etiqueta_{inventarioId}_{timestamp}.xlsx`
 *   dentro de [Context.getCacheDir], sobrescrevendo qualquer arquivo
 *   com o mesmo nome.
 *
 * @param api     Interface Retrofit para os endpoints de relatório fotográfico.
 * @param context Contexto da aplicação, usado para obter [Context.getCacheDir].
 */
class BaixarRelatorioFotoExcelUseCase @Inject constructor(
    private val api: RelatorioFotoApi,
    @ApplicationContext private val context: Context
) {
    /**
     * Executa o download do XLSX para o inventário informado.
     *
     * @param inventarioId ID do inventário ativo.
     * @return [Result] contendo o [File] salvo em cache em caso de sucesso,
     *         ou uma exceção tipada ([SemFotosException], [PermissaoNegadaException])
     *         ou genérica em caso de falha.
     */
    suspend operator fun invoke(inventarioId: Int): Result<File> {
        return try {
            val response = api.baixarExcel(inventarioId)

            when (response.code()) {
                204 -> return Result.failure(SemFotosException())
                403 -> return Result.failure(PermissaoNegadaException())
            }

            val body = response.body()
                ?: return Result.failure(Exception("Resposta vazia do servidor"))

            val timestamp = System.currentTimeMillis()
            val arquivo = File(
                context.cacheDir,
                "relatorio_sem_etiqueta_${inventarioId}_${timestamp}.xlsx"
            )

            arquivo.outputStream().buffered(8 * 1024).use { out ->
                body.byteStream().use { input ->
                    input.copyTo(out)
                }
            }

            Result.success(arquivo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
