package com.inventario.mobile.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.PagedResponse
import com.inventario.mobile.data.remote.dto.SalaComProgressoDTO
import com.inventario.mobile.data.model.Sala
import com.inventario.mobile.data.model.SalaSyncResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API para gerenciamento de salas
 */
interface SalaApi {
    
    /**
     * Lista todas as salas (endpoint antigo, mantido para compatibilidade)
     */
    @GET("api/mobile/salas")
    suspend fun listarSalas(): Response<ApiResponse<List<Sala>>>
    
    /**
     * Lista TODAS as salas de uma vez (otimizado para sincronização)
     * Usa endpoint /sync/salas que retorna todas as ~108 salas
     */
    @GET("api/mobile/sync/salas")
    suspend fun listarTodasSalas(): Response<ApiResponse<List<Sala>>>
    
    /**
     * Busca sala por ID
     * 
     * @param id ID da sala
     * @return Sala encontrada
     */
    @GET("api/mobile/salas/{id}")
    suspend fun buscarSalaPorId(
        @Path("id") id: Int
    ): Response<ApiResponse<Sala>>
    
    /**
     * Lista salas com paginação
     * 
     * @param page Número da página (começa em 0)
     * @param size Quantidade de itens por página
     * @return Resposta paginada com lista de salas
     */
    @GET("api/mobile/salas")
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
    @GET("api/mobile/salas/buscar")
    suspend fun buscarSalasPorNome(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PagedResponse<Sala>>
    
    /**
     * Lista salas com estatísticas de progresso de coleta.
     * Retorna total de patrimônios, coletados, pendentes e percentual.
     * 
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     * @return Lista de salas com progresso
     */
    @GET("api/mobile/salas/com-progresso")
    suspend fun listarSalasComProgresso(
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<List<SalaComProgressoDTO>>>
    
    /**
     * SINCRONIZAÇÃO INCREMENTAL de salas.
     * Retorna apenas salas modificadas após a data informada.
     * 
     * Estratégia:
     * - Se lastSync = 0 ou null: retorna TODAS as salas (sync inicial)
     * - Se lastSync > 0: retorna apenas salas modificadas/criadas após essa data
     * - Inclui lista de IDs de salas removidas/inativadas
     * 
     * @param lastSync Timestamp da última sincronização (milissegundos)
     * @return Salas novas/modificadas + IDs removidos
     */
    @GET("api/mobile/salas/sync")
    suspend fun sincronizarSalas(
        @Query("lastSync") lastSync: Long = 0
    ): Response<ApiResponse<SalaSyncResponse>>
}
