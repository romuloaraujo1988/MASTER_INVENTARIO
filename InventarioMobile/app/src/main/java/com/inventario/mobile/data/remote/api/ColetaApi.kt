package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.MobileColetaRequest
import com.inventario.mobile.data.remote.dto.MobileColetaResponseDto
import com.inventario.mobile.data.remote.dto.MobileColetaBatchRequest
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
    
    /**
     * Registra múltiplas coletas em lote
     * Endpoint: POST /api/mobile/coletas/batch
     */
    @POST("api/mobile/coletas/batch")
    suspend fun registrarColetasEmLote(
        @Body request: MobileColetaBatchRequest
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Verifica se uma coleta seria duplicada
     * Endpoint: POST /api/mobile/coletas/verificar-duplicata
     */
    @POST("api/mobile/coletas/verificar-duplicata")
    suspend fun verificarDuplicataColeta(
        @Body request: VerificarDuplicataRequest
    ): ApiResponse<Map<String, Any>>
}

/**
 * Request para verificação de duplicata de coleta
 */
data class VerificarDuplicataRequest(
    val numeroPatrimonio: String,
    val inventarioId: Int? = null
)
