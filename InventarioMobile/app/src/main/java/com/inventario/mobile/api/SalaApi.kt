package com.inventario.mobile.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PagedResponse
import com.inventario.mobile.data.model.Sala
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API para gerenciamento de salas
 */
interface SalaApi {
    
    /**
     * Lista todas as salas (endpoint antigo, mantido para compatibilidade)
     */
    @GET("salas")
    suspend fun listarSalas(): Response<ApiResponse<List<Sala>>>
    
    /**
     * Lista salas com paginação
     * 
     * @param page Número da página (começa em 0)
     * @param size Quantidade de itens por página
     * @return Resposta paginada com lista de salas
     */
    @GET("salas/paginado")
    suspend fun listarSalasPaginado(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<Sala>>
    
    /**
     * Busca salas por nome com paginação
     * 
     * @param query Termo de busca (busca no nome da sala)
     * @param page Número da página (começa em 0)
     * @param size Quantidade de itens por página
     * @return Resposta paginada com salas filtradas
     */
    @GET("salas/buscar")
    suspend fun buscarSalasPorNome(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<Sala>>
}
