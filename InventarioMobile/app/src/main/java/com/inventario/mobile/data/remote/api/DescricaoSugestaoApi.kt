package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PagedResponseDto
import com.inventario.mobile.data.remote.dto.SugestaoDescricaoDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API Retrofit para o endpoint de sugestões de descrição de patrimônios
 * **não coletados** do inventário ativo, introduzido pela feature
 * "coleta com descrição livre e sugestão".
 *
 * Contrato: `GET /api/mobile/descricoes/sugestoes` (Req 5.1).
 *
 * Regra crítica (steering `endpoints-nao-alterar.md`): a URL Retrofit
 * **deve** manter o prefixo completo `api/mobile/`. Qualquer encurtamento
 * quebra as chamadas em produção — essa é exatamente a pegadinha que já
 * derrubou coletas em 18/11/2025 e levou à criação da steering rule.
 *
 * Todos os parâmetros são opcionais e enviados como `null` por padrão:
 *
 *   - `q`              : termo de busca; `null`/vazio ⇒ servidor retorna
 *                        todas as descrições não coletadas respeitando
 *                        paginação (Req 5.5). O comprimento é truncado
 *                        silenciosamente a 100 chars no servidor (Req 5.3).
 *   - `page`           : página 0-indexada; `null` ou inválida ⇒ servidor
 *                        aplica `0` silenciosamente (Req 5.6).
 *   - `size`           : tamanho da página em [1, 100]; `null` ou fora do
 *                        intervalo ⇒ servidor aplica `50` silenciosamente
 *                        sem erro (Req 5.7).
 *   - `idInventario`   : id explícito do inventário; `null` ⇒ servidor
 *                        resolve via `InventarioDAO.buscarInventarioAtivo()`
 *                        e, se não houver ativo, responde 200 OK com
 *                        `semInventarioAtivo=true` (Req 5.8).
 *
 * A autorização segue a matriz `@RequireColetor` herdada do controller:
 * JWT válido com perfil `ADMIN`, `SUPERVISOR` ou `COLETOR` retorna `200`;
 * `CONSULTA` recebe `403`; token ausente/inválido recebe `401`
 * (Req 10.1, 10.2, 10.3).
 *
 * Requirements: 5.1.
 */
interface DescricaoSugestaoApi {

    /**
     * Busca sugestões paginadas de descrições de patrimônios não coletados
     * do inventário ativo (ou do `idInventario` informado).
     *
     * Os parâmetros `null` são **omitidos** da URL final pelo Retrofit,
     * permitindo que o servidor aplique os defaults descritos em
     * `design.md` (Req 5.6, 5.7, 5.8) sem que o cliente precise replicar
     * essa lógica.
     *
     * @param termoBusca   termo de filtro (acento/caso-insensível no
     *                     servidor); `null` ou string vazia busca todas
     *                     as descrições não coletadas
     * @param page         página 0-indexada; `null` usa `0`
     * @param size         tamanho da página em [1, 100]; `null` usa `50`
     * @param idInventario inventário alvo; `null` usa o inventário ativo
     */
    @GET("api/mobile/descricoes/sugestoes")
    suspend fun buscarSugestoes(
        @Query("q") termoBusca: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("idInventario") idInventario: Int? = null
    ): ApiResponse<PagedResponseDto<SugestaoDescricaoDto>>
}
