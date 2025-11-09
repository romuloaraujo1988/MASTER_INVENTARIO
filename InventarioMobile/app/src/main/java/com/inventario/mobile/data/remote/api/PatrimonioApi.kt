package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.domain.model.Patrimonio
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Retrofit para operações de Patrimônio
 */
interface PatrimonioApi {
    
    @GET("api/mobile/patrimonio")
    suspend fun buscarTodos(): ApiResponse<List<Patrimonio>>
    
    @GET("api/mobile/patrimonio/numero/{numero}")
    suspend fun buscarPorNumero(
        @Path("numero") numero: String
    ): ApiResponse<Patrimonio>
    
    @GET("api/mobile/descricoes/nao-coletadas")
    suspend fun buscarDescricoesNaoColetadas(
        @Query("idInventario") idInventario: Int?
    ): ApiResponse<List<String>>
}
