package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.ApiResponse
import com.inventario.mobile.data.remote.dto.MobileOfflineDataDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API para sincronização offline completa
 * 
 * ENDPOINT DEDICADO: /api/mobile/sync/offline-data
 * 
 * BENEFÍCIOS:
 * - 1 requisição ao invés de múltiplas (3+)
 * - Dados otimizados e compactados
 * - Fácil debug (um único ponto)
 * - Suporte a GZIP automático
 * 
 * v2.2: API dedicada para sincronização offline
 */
interface OfflineSyncApi {
    
    /**
     * Busca TODOS os dados necessários para modo offline
     * 
     * Retorna em uma única requisição:
     * - Patrimônios (10.000+)
     * - Salas (100+)
     * - Responsáveis
     * - Metadados (inventário ativo, timestamp, etc)
     * 
     * @param inventarioId ID do inventário ativo (opcional)
     * @return Todos os dados para offline
     */
    @GET("api/mobile/sync/offline-data")
    suspend fun buscarDadosOffline(
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<MobileOfflineDataDTO>>
    
    /**
     * Verifica status do servidor (endpoint leve)
     * 
     * Retorna apenas metadados sem dados completos
     * Útil para verificar se servidor está respondendo
     * 
     * @param inventarioId ID do inventário ativo (opcional)
     * @return Metadados do servidor
     */
    @GET("api/mobile/sync/status")
    suspend fun verificarStatus(
        @Query("inventarioId") inventarioId: Int? = null
    ): Response<ApiResponse<MobileOfflineDataDTO.MetadataDTO>>
}
