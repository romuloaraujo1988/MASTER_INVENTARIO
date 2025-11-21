package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API para sincronização de dados offline
 * Endpoints para download de patrimônios, salas e responsáveis
 */
interface SyncApi {
    
    /**
     * Sincronização completa de todos os dados
     * Retorna patrimônios, salas, responsáveis e inventário ativo
     * 
     * @param idInventario ID do inventário (opcional)
     * @return Pacote completo de dados
     */
    @GET("api/mobile/sync/full")
    suspend fun sincronizacaoCompleta(
        @Query("idInventario") idInventario: Int? = null
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Sincronizar apenas patrimônios
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Lista de patrimônios
     */
    @GET("api/mobile/sync/patrimonios")
    suspend fun sincronizarPatrimonios(
        @Query("ultimaAtualizacao") ultimaAtualizacao: Long? = null
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Sincronizar apenas salas
     * 
     * @return Lista de salas
     */
    @GET("api/mobile/sync/salas")
    suspend fun sincronizarSalas(): ApiResponse<Map<String, Any>>
    
    /**
     * Sincronizar apenas responsáveis
     * 
     * @return Lista de responsáveis
     */
    @GET("api/mobile/sync/responsaveis")
    suspend fun sincronizarResponsaveis(): ApiResponse<Map<String, Any>>
    
    /**
     * Verificar se há atualizações disponíveis
     * 
     * @param ultimaSincronizacao Timestamp da última sincronização
     * @return Informações sobre atualizações disponíveis
     */
    @GET("api/mobile/sync/check-updates")
    suspend fun verificarAtualizacoes(
        @Query("ultimaSincronizacao") ultimaSincronizacao: Long
    ): ApiResponse<Map<String, Any>>
    
    /**
     * Obter metadados da sincronização
     * 
     * @return Metadados (tamanhos, versões, etc)
     */
    @GET("api/mobile/sync/metadata")
    suspend fun obterMetadados(): ApiResponse<Map<String, Any>>
}
