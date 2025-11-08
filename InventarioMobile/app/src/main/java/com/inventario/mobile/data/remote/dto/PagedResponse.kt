package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO genérico para respostas paginadas da API
 */
data class PagedResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("totalElements")
    val totalElements: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("last")
    val last: Boolean
) {
    fun hasNext(): Boolean = !last
    fun hasPrevious(): Boolean = !first
}
