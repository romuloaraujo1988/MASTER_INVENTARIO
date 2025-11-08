package com.inventario.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.inventario.mobile.data.local.entity.ColetaEntity

/**
 * Extensões do ColetaDao para suporte a sincronização offline
 */
@Dao
interface ColetaDaoExtensions {
    
    /**
     * Retorna todas as coletas que ainda não foram sincronizadas
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizada = 0 
        AND (tentativasSincronizacao < 3 OR tentativasSincronizacao IS NULL)
        ORDER BY dataColeta ASC
    """)
    suspend fun getPendingSync(): List<ColetaEntity>
    
    /**
     * Retorna coletas que falharam na sincronização
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizada = 0 
        AND tentativasSincronizacao >= 3
        ORDER BY ultimaTentativaSincronizacao DESC
    """)
    suspend fun getFailedSync(): List<ColetaEntity>
    
    /**
     * Conta quantas coletas estão pendentes de sincronização
     */
    @Query("""
        SELECT COUNT(*) FROM coleta 
        WHERE sincronizada = 0
    """)
    suspend fun countPendingSync(): Int
    
    /**
     * Conta quantas coletas foram sincronizadas com sucesso
     */
    @Query("""
        SELECT COUNT(*) FROM coleta 
        WHERE sincronizada = 1
    """)
    suspend fun countSynced(): Int
    
    /**
     * Conta quantas coletas falharam na sincronização
     */
    @Query("""
        SELECT COUNT(*) FROM coleta 
        WHERE sincronizada = 0 
        AND tentativasSincronizacao >= 3
    """)
    suspend fun countFailed(): Int
    
    /**
     * Retorna coletas sincronizadas em um período
     */
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizada = 1 
        AND ultimaTentativaSincronizacao BETWEEN :startDate AND :endDate
        ORDER BY ultimaTentativaSincronizacao DESC
    """)
    suspend fun getSyncedBetween(startDate: Long, endDate: Long): List<ColetaEntity>
    
    /**
     * Marca todas as coletas como não sincronizadas (para forçar re-sincronização)
     */
    @Query("""
        UPDATE coleta 
        SET sincronizada = 0, 
            tentativasSincronizacao = 0,
            erroSincronizacao = NULL
        WHERE sincronizada = 1
    """)
    suspend fun markAllAsNotSynced()
    
    /**
     * Limpa erros de sincronização
     */
    @Query("""
        UPDATE coleta 
        SET tentativasSincronizacao = 0,
            erroSincronizacao = NULL
        WHERE tentativasSincronizacao >= 3
    """)
    suspend fun clearSyncErrors()
    
    /**
     * Retorna estatísticas de sincronização
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN sincronizada = 1 THEN 1 ELSE 0 END) as synced,
            SUM(CASE WHEN sincronizada = 0 AND tentativasSincronizacao < 3 THEN 1 ELSE 0 END) as pending,
            SUM(CASE WHEN sincronizada = 0 AND tentativasSincronizacao >= 3 THEN 1 ELSE 0 END) as failed
        FROM coleta
    """)
    suspend fun getSyncStats(): SyncStats
}

/**
 * Estatísticas de sincronização
 */
data class SyncStats(
    val total: Int,
    val synced: Int,
    val pending: Int,
    val failed: Int
)
