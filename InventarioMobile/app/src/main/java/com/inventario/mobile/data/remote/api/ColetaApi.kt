package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.domain.model.Coleta
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Retrofit para operações de Coleta
 */
interface ColetaApi {
    
    @POST("api/mobile/coletas")
    suspend fun registrarColeta(
        @Body coleta: Coleta
    ): ApiResponse<Coleta>
}
