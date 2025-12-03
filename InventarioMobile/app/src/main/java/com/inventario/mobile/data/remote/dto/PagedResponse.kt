package com.inventario.mobile.data.remote.dto

/**
 * DTO para resposta paginada do servidor
 * Corresponde ao PagedResponse.java do backend
 */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        /**
         * Cria uma resposta vazia
         */
        fun <T> empty(): PagedResponse<T> = PagedResponse(
            content = emptyList(),
            page = 0,
            size = 0,
            totalElements = 0,
            totalPages = 0,
            first = true,
            last = true,
            hasNext = false,
            hasPrevious = false
        )
    }
}
