package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para resposta paginada de coletas do servidor
 * 
 * O endpoint /api/mobile/coletas/all agora retorna dados paginados
 * com estrutura: { content: [...], page, size, totalElements, totalPages, ... }
 */
data class ColetasPagedResponse(
    @SerializedName("content")
    val content: List<MobileColetaResponseDto> = emptyList(),
    
    @SerializedName("page")
    val page: Int = 0,
    
    @SerializedName("size")
    val size: Int = 100,
    
    @SerializedName("totalElements")
    val totalElements: Int = 0,
    
    @SerializedName("totalPages")
    val totalPages: Int = 0,
    
    @SerializedName("first")
    val first: Boolean = true,
    
    @SerializedName("last")
    val last: Boolean = true,
    
    @SerializedName("aviso")
    val aviso: String? = null
)
