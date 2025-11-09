package com.inventario.mobile.data.remote.dto

/**
 * Resposta padrão da API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String,
    val errorCode: String? = null
)
