package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO específico para resposta do endpoint /api/mobile/coletas/all
 * 
 * IMPORTANTE: Esta classe NÃO usa genéricos para evitar problemas
 * de Type Erasure do Java/Kotlin com Retrofit/Gson.
 * 
 * Estrutura esperada do servidor:
 * {
 *   "success": true,
 *   "data": {
 *     "content": [...],
 *     "page": 0,
 *     "size": 100,
 *     "totalElements": 50,
 *     "totalPages": 1,
 *     "first": true,
 *     "last": true
 *   },
 *   "message": "..."
 * }
 */
data class ColetasAllResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: ColetasPagedData? = null,
    
    @SerializedName("message")
    val message: String = "",
    
    @SerializedName("errorCode")
    val errorCode: String? = null
)

/**
 * Dados paginados de coletas
 */
data class ColetasPagedData(
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
