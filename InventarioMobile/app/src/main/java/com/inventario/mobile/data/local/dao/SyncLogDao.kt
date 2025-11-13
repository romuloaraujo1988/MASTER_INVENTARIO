package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SyncLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de histórico de sincronização
 */
@Dao
interface SyncLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(log: SyncLogEntity): Long
    
    @Query("SELECT * FROM sync_log ORDER BY dataHora DESC")
    fun observarTodos(): Flow<List<SyncLogEntity>>
    
    @Query("SELECT * FROM sync_log ORDER BY dataHora DESC LIMIT :limit")
    suspend fun buscarUltimos(limit: Int = 50): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_log WHERE tipo = :tipo ORDER BY dataHora DESC")
    suspend fun buscarPorTipo(tipo: String): List<SyncLogEntity>
    
    @Query("SELECT * FROM sync_log WHERE status = :status ORDER BY dataHora DESC")
    suspend fun buscarPorStatus(status: String): List<SyncLogEntity>
    
    @Query("""
        DELETE FROM sync_log 
        WHERE id NOT IN (
            SELECT id FROM sync_log 
            ORDER BY dataHora DESC 
            LIMIT :limit
        )
    """)
    suspend fun limparAntigos(limit: Int = 50)
    
    @Query("DELETE FROM sync_log")
    suspend fun limparTodos()
    
    @Query("SELECT COUNT(*) FROM sync_log")
    suspend fun contar(): Int
    
    @Query("SELECT COUNT(*) FROM sync_log WHERE status = 'SUCCESS'")
    suspend fun contarSucessos(): Int
    
    @Query("SELECT COUNT(*) FROM sync_log WHERE status = 'ERROR'")
    suspend fun contarErros(): Int
    
    @Query("""
        SELECT AVG(duracao) FROM sync_log 
        WHERE status = 'SUCCESS' AND duracao > 0
    """)
    suspend fun duracaoMedia(): Long?
}
