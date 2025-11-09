package com.inventario.mobile.api

import com.inventario.mobile.model.ApiResponse
import com.inventario.mobile.model.Sala
import retrofit2.Response
import retrofit2.http.GET

/**
 * API para gerenciamento de salas
 */
interface SalaApi {
    
    @GET("salas")
    suspend fun listarSalas(): Response<ApiResponse<List<Sala>>>
}
