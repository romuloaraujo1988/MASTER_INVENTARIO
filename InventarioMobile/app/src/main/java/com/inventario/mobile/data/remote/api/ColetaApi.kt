package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.MobileColetaRequest
import com.inventario.mobile.data.remote.dto.MobileColetaResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Retrofit para operações de Coleta
 */
interface ColetaApi {
    
    @POST("api/mobile/coletas")
    suspend fun registrarColeta(
        @Body request: MobileColetaRequest
    ): ApiResponse<MobileColetaResponseDto>
}
