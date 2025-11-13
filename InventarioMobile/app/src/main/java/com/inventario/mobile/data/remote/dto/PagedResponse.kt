package com.inventario.mobile.data.remote.dto

/**
 * DTO genérico para respostas paginadas da API
 * 
 * @param T Tipo dos dados retornados
 */
data class PagedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
    val message: String? = null
)
