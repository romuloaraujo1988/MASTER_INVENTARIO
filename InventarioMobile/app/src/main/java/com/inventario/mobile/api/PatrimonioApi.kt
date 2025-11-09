package com.inventario.mobile.api

import com.inventario.mobile.model.ApiResponse
import com.inventario.mobile.model.Patrimonio
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API para gerenciamento de patrimônios
 */
interface PatrimonioApi {
    
    @GET("patrimonios")
    suspend fun listarPatrimonios(): Response<ApiResponse<List<Patrimonio>>>
    
    @GET("patrimonios/{id}")
    suspend fun buscarPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("patrimonios/numero/{numero}")
    suspend fun buscarPorNumero(
        @Path("numero") numero: String
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("descricoes/nao-coletadas")
    suspend fun buscarDescricoesNaoColetadas(
        @Query("idInventario") idInventario: Int?
    ): Response<ApiResponse<List<String>>>
}
