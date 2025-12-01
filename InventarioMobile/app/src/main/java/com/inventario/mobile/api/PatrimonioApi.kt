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
    
    @GET("api/mobile/patrimonio")
    suspend fun listarPatrimonios(): Response<ApiResponse<List<Patrimonio>>>
    
    @GET("api/mobile/patrimonio")
    suspend fun listarPatrimoniosPaginado(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<List<Patrimonio>>>
    
    @GET("api/mobile/patrimonio/{id}")
    suspend fun buscarPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("api/mobile/patrimonio/numero/{numero}")
    suspend fun buscarPorNumero(
        @Path("numero") numero: String
    ): Response<ApiResponse<Patrimonio>>
    
    @GET("api/mobile/descricoes/nao-coletadas")
    suspend fun buscarDescricoesNaoColetadas(
        @Query("idInventario") idInventario: Int? = null
    ): Response<ApiResponse<List<String>>>
    
    @GET("api/mobile/patrimonio/descricao/{descricao}")
    suspend fun buscarPorDescricaoNaoColetados(
        @Path("descricao", encoded = true) descricao: String,
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<List<Patrimonio>>>
    
    /**
     * Verifica se um patrimônio já foi coletado no inventário
     */
    @GET("api/mobile/patrimonio/numero/{numero}/coletado")
    suspend fun verificarSePatrimonioFoiColetado(
        @Path("numero") numero: String,
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Valida um número de patrimônio antes de coletar
     */
    @GET("api/mobile/patrimonio/numero/{numero}/validar")
    suspend fun validarPatrimonio(
        @Path("numero") numero: String
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Busca patrimônios por sala com paginação e filtro de coleta
     * 
     * @param salaId ID da sala
     * @param page número da página (0-based)
     * @param size tamanho da página
     * @param coletado filtro de coleta (true=coletados, false=não coletados, null=todos)
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     */
    @GET("api/mobile/patrimonio/sala/{salaId}")
    suspend fun buscarPorSala(
        @Path("salaId") salaId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("coletado") coletado: Boolean? = null,
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<List<Patrimonio>>>
}
