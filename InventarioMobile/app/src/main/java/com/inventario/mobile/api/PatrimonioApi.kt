package com.inventario.mobile.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.model.Patrimonio
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API para gerenciamento de patrimônios
 */
interface PatrimonioApi {
    
    @GET("patrimonio")
    suspend fun listarPatrimonios(): Response<ApiResponse<List<Patrimonio>>>
    
    @GET("patrimonio/{id}")
    suspend fun buscarPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("patrimonio/numero/{numero}")
    suspend fun buscarPorNumero(
        @Path("numero") numero: String
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("descricoes/nao-coletadas")
    suspend fun buscarDescricoesNaoColetadas(
        @Query("idInventario") idInventario: Int? = null
    ): Response<ApiResponse<List<String>>>
    
    @GET("patrimonio/descricao/{descricao}/nao-coletados")
    suspend fun buscarPorDescricaoNaoColetados(
        @Path("descricao") descricao: String
    ): Response<ApiResponse<List<Patrimonio>>>
    
    /**
     * Verifica se um patrimônio já foi coletado no inventário
     */
    @GET("patrimonio/numero/{numero}/coletado")
    suspend fun verificarSePatrimonioFoiColetado(
        @Path("numero") numero: String,
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Valida um número de patrimônio antes de coletar
     */
    @GET("patrimonio/numero/{numero}/validar")
    suspend fun validarPatrimonio(
        @Path("numero") numero: String
    ): Response<ApiResponse<Map<String, Any>>>
}
