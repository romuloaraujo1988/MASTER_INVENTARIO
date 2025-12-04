package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PagedResponse
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.data.remote.dto.MobilePatrimonioDto
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
    
    // ========================================
    // Endpoints com Paginação
    // ========================================
    
    /**
     * Busca patrimônios com paginação e metadados
     * 
     * @param page número da página (começa em 0)
     * @param size quantidade de itens por página
     * @return PagedResponse com lista e metadados de paginação
     */
    @GET("api/mobile/patrimonio/paged")
    suspend fun buscarPatrimoniosPaginado(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PagedResponse<MobilePatrimonioDto>>
    
    /**
     * Busca patrimônios por sala com paginação
     * 
     * @param salaId ID da sala
     * @param page número da página
     * @param size quantidade de itens por página
     * @param coletado filtro: true=coletados, false=não coletados, null=todos
     * @param inventarioId ID do inventário (opcional)
     */
    @GET("api/mobile/patrimonio/sala/{salaId}")
    suspend fun buscarPorSalaPaginado(
        @Path("salaId") salaId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("coletado") coletado: Boolean? = null,
        @Query("inventarioId") inventarioId: Int? = null
    ): ApiResponse<List<MobilePatrimonioDto>>
    
    /**
     * Busca patrimônios por responsável com paginação
     * 
     * @param idResponsavel ID do responsável
     * @param page número da página
     * @param size quantidade de itens por página
     * @param coletado filtro de status de coleta
     */
    @GET("api/mobile/patrimonio/responsavel/{idResponsavel}")
    suspend fun buscarPorResponsavelPaginado(
        @Path("idResponsavel") idResponsavel: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("coletado") coletado: Boolean? = null
    ): ApiResponse<List<MobilePatrimonioDto>>
    
    /**
     * Conta total de patrimônios por responsável
     */
    @GET("api/mobile/patrimonio/responsavel/{idResponsavel}/count")
    suspend fun contarPorResponsavel(
        @Path("idResponsavel") idResponsavel: Int
    ): ApiResponse<Int>
    
    /**
     * Busca patrimônios não coletados por descrição
     */
    @GET("api/mobile/patrimonio/descricao/{descricao}")
    suspend fun buscarPorDescricaoNaoColetados(
        @Path("descricao") descricao: String,
        @Query("inventarioId") inventarioId: Int? = null
    ): ApiResponse<List<MobilePatrimonioDto>>
    
    /**
     * Busca rápida de patrimônios por query de texto livre
     * Busca por número, descrição, nome da sala ou responsável
     * 
     * @param query termo de busca (mínimo 3 caracteres)
     * @param filtro filtro de status: ALL, COLETADOS, PENDENTES, DIVERGENCIAS
     * @param inventarioId ID do inventário (opcional)
     * @param limit limite de resultados (default: 100)
     */
    @GET("api/mobile/patrimonio/buscar")
    suspend fun buscarPorQuery(
        @Query("query") query: String,
        @Query("filtro") filtro: String = "ALL",
        @Query("inventarioId") inventarioId: Int? = null,
        @Query("limit") limit: Int = 100
    ): ApiResponse<List<MobilePatrimonioDto>>
}
