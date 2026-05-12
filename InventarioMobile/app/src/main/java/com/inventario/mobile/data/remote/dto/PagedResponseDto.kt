package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO genérico de resposta paginada dedicado ao endpoint
 * `GET /api/mobile/descricoes/sugestoes` da feature
 * "coleta com descrição livre e sugestão".
 *
 * Espelha, campo a campo, o record `PagedResponseDTO<T>` definido no
 * servidor (`sihcp-server/.../mobile/server/dto/PagedResponseDTO.java`).
 * É um tipo **distinto** do existente [PagedResponse] (que continua sendo
 * usado pela implementação Paging 3 de patrimônios/coletas) porque:
 *
 *   1. contém o campo adicional `semInventarioAtivo` (Req 5.8),
 *   2. omite os campos `first`, `last` e `hasPrevious` do contrato legado,
 *   3. o contrato JSON do novo endpoint é diferente do antigo.
 *
 * Campos (Req 5.5, 5.6, 5.8):
 *   - `content`             itens da página atual
 *   - `page`                número da página (0-based)
 *   - `size`                tamanho efetivo da página (após sanitização)
 *   - `totalElements`       total de elementos em todas as páginas
 *   - `totalPages`          ceil(totalElements / size)
 *   - `hasNext`             (page + 1) * size < totalElements
 *   - `semInventarioAtivo`  true quando não há inventário ativo; nesse
 *                           caso `content` é vazio e os demais metadados
 *                           são zerados (Req 5.8)
 *
 * Tipos são plain Kotlin / boxed nulos onde faz sentido, para máxima
 * tolerância na desserialização via Gson/Retrofit.
 *
 * Requirements: 5.5, 5.6, 5.8.
 */
data class PagedResponseDto<T>(
    @SerializedName("content")
    val content: List<T> = emptyList(),

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("size")
    val size: Int = 0,

    @SerializedName("totalElements")
    val totalElements: Long = 0L,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("hasNext")
    val hasNext: Boolean = false,

    @SerializedName("semInventarioAtivo")
    val semInventarioAtivo: Boolean = false
) {
    companion object {
        /**
         * Fábrica para o caso "sem inventário ativo" (Req 5.8).
         *
         * Retorna uma resposta com `content` vazio, metadados zerados e
         * `semInventarioAtivo=true`. Espelha `PagedResponseDTO.empty()`
         * do servidor.
         */
        fun <T> empty(): PagedResponseDto<T> = PagedResponseDto(
            content = emptyList(),
            page = 0,
            size = 0,
            totalElements = 0L,
            totalPages = 0,
            hasNext = false,
            semInventarioAtivo = true
        )

        /**
         * Variante explícita da fábrica vazia que permite construir
         * respostas vazias com o flag `semInventarioAtivo` controlado.
         *
         * Útil para diferenciar o cenário "sem inventário ativo"
         * (`semInventarioAtivo=true`) do cenário "nenhum resultado com
         * inventário ativo presente" (`semInventarioAtivo=false`).
         */
        fun <T> empty(semInventarioAtivo: Boolean): PagedResponseDto<T> = PagedResponseDto(
            content = emptyList(),
            page = 0,
            size = 0,
            totalElements = 0L,
            totalPages = 0,
            hasNext = false,
            semInventarioAtivo = semInventarioAtivo
        )
    }
}
