package com.inventario.mobile.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API para gerenciamento de inventários
 */
interface InventarioApi {
    
    /**
     * Busca o inventário ativo (em andamento)
     */
    @GET("api/mobile/inventario/ativo")
    suspend fun buscarInventarioAtivo(): Response<ApiResponse<InventarioDTO>>
    
    /**
     * Busca inventário por ID
     */
    @GET("api/mobile/inventario/{id}")
    suspend fun buscarPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<InventarioDTO>>
    
    /**
     * Lista todos os inventários
     */
    @GET("api/mobile/inventario")
    suspend fun listarInventarios(): Response<ApiResponse<List<InventarioDTO>>>
    
    /**
     * Busca estatísticas do inventário
     */
    @GET("api/mobile/inventario/{id}/estatisticas")
    suspend fun buscarEstatisticas(
        @Path("id") id: Int
    ): Response<ApiResponse<Map<String, Any>>>
}

/**
 * DTO de Inventário
 */
data class InventarioDTO(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val status: String,
    val dataInicio: String?,
    val dataFim: String?,
    val dataCriacao: String?,
    val totalPatrimonios: Int?,
    val totalColetados: Int?,
    val percentualConclusao: Double?
)
