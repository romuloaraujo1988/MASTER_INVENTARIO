package com.inventario.mobile.api

import com.inventario.mobile.model.ApiResponse
import com.inventario.mobile.model.DispositivoMobile
import com.inventario.mobile.model.DispositivoRegistroRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API para gerenciamento de dispositivos
 */
interface DispositivoApi {
    
    @POST("dispositivos/registrar")
    suspend fun registrarDispositivo(
        @Body request: DispositivoRegistroRequest
    ): Response<ApiResponse<DispositivoMobile>>
    
    @GET("dispositivos/status/{deviceId}")
    suspend fun verificarStatus(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<DispositivoMobile>>
    
    @GET("dispositivos/autorizado/{deviceId}")
    suspend fun verificarAutorizacao(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<Boolean>>
    
    @POST("dispositivos/{id}/sincronizar")
    suspend fun registrarSincronizacao(
        @Path("id") id: Int
    ): Response<ApiResponse<Unit>>
}
