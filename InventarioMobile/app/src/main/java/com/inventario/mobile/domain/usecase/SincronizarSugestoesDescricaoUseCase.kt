package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.repository.SugestaoDescricaoRepository
import javax.inject.Inject

/**
 * Caso de uso: recarregar o [Cache_Sugestoes_Local] de sugestões de descrição
 * a partir do endpoint `GET /api/mobile/descricoes/sugestoes`, garantindo o
 * isolamento por inventário ativo.
 *
 * Coordena duas operações do [SugestaoDescricaoRepository]:
 *  1. [SugestaoDescricaoRepository.limparCacheDeOutrosInventarios] — remove do
 *     cache entradas associadas a inventários diferentes do ativo, preservando
 *     o isolamento exigido pela Requirement 7.1 antes de popular o cache.
 *  2. [SugestaoDescricaoRepository.buscarSugestoes] — recarrega a primeira
 *     página (tamanho máximo 100) do servidor. O próprio repositório,
 *     quando recebe a resposta com sucesso, invoca `atualizarCache` para
 *     persistir as sugestões recebidas (Requirements 8.5, 8.6).
 *
 * Uso:
 *  - `SyncWorker` (background, WorkManager) — após sincronizar coletas com
 *    sucesso, dispara este use case para manter o cache de sugestões alinhado
 *    ao estado corrente do servidor (Requirement 8.6).
 *  - Refresh manual — acionado pela camada de apresentação quando o coletor
 *    solicita atualização explícita das sugestões (Requirement 8.5).
 *
 * Política de erro (Requirement 8.7): em caso de timeout de 10 segundos ou
 * erro de rede, o repositório aplica fallback para o cache e devolve
 * `Result.success` com `origem = CACHE` ou `VAZIO_SEM_CACHE`. Este use case
 * interpreta essas origens como falha de sincronização (o servidor não foi
 * efetivamente contatado) e retorna [Result.failure], preservando o cache
 * anterior intocado. A implementação de `atualizarCache` também não propaga
 * exceções de I/O, garantindo que falhas parciais nunca corrompam o cache.
 *
 * Retorno:
 *  - [Result.success] com a quantidade de sugestões efetivamente recarregadas
 *    do servidor quando `origem = SERVIDOR`.
 *  - [Result.failure] em qualquer outro cenário (origem de fallback ou
 *    exceção inesperada), sem alterar o cache anterior.
 *
 * Requirements: 8.5, 8.6, 8.7.
 */
class SincronizarSugestoesDescricaoUseCase @Inject constructor(
    private val repository: SugestaoDescricaoRepository
) {

    /**
     * Executa a sincronização do cache de sugestões para o inventário ativo.
     *
     * @param idInventarioAtivo Identificador do [Inventario_Ativo] cujo cache
     *                          de sugestões deve ser recarregado.
     * @return [Result.success] com o número de sugestões recarregadas quando
     *         o servidor responde com sucesso (Requirements 8.5, 8.6);
     *         [Result.failure] quando a sincronização falha por timeout, erro
     *         de rede ou qualquer outra exceção, preservando o cache anterior
     *         (Requirement 8.7).
     */
    suspend operator fun invoke(idInventarioAtivo: Int): Result<Int> {
        return try {
            // Req 7.1: isola o inventário ativo apagando caches de outros
            // inventários antes de povoar o cache corrente.
            repository.limparCacheDeOutrosInventarios(idInventarioAtivo)

            // Recarrega a primeira página (size=100) para popular o cache.
            // O repositório atualiza internamente o cache via atualizarCache
            // quando a origem é SERVIDOR (Req 8.5, 8.6).
            val resultado = repository.buscarSugestoes(
                idInventario = idInventarioAtivo,
                termoBusca = "",
                page = 0,
                size = 100
            ).getOrThrow()

            if (resultado.origem == OrigemSugestoes.SERVIDOR) {
                Result.success(resultado.sugestoes.size)
            } else {
                // Fallback para cache ou ausência de cache indica que o
                // servidor não foi efetivamente contatado — sinaliza falha
                // ao chamador para que ele registre pendência de sincronização
                // sem descartar o cache anterior (Req 8.7).
                Result.failure(
                    Exception("Sincronização de sugestões falhou: origem=${resultado.origem}")
                )
            }
        } catch (e: Exception) {
            // Req 8.7: falha preserva o cache anterior (atualizarCache no
            // repositório já engole exceções de I/O).
            Result.failure(e)
        }
    }
}
