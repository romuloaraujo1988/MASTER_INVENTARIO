package com.inventario.mobile.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.inventario.mobile.data.local.entity.ColetaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Coleta
 */
@Dao
interface ColetaDao {
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC")
    fun getAllColetas(): Flow<List<ColetaEntity>>
    
    // ===== QUERIES PAGINADAS (Paging 3) =====
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC")
    fun getAllColetasPaged(): PagingSource<Int, ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE usuarioId = :usuarioId ORDER BY dataColeta DESC")
    fun getColetasByUsuarioPaged(usuarioId: Long): PagingSource<Int, ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta DESC")
    fun getColetasPendentesPaged(): PagingSource<Int, ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE patrimonioId = :patrimonioId ORDER BY dataColeta DESC")
    fun getColetasByPatrimonioPaged(patrimonioId: Long): PagingSource<Int, ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE id = :id")
    suspend fun getColetaById(id: Long): ColetaEntity?
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta ASC")
    suspend fun getColetasPendentes(): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE patrimonioId = :patrimonioId ORDER BY dataColeta DESC")
    suspend fun getColetasByPatrimonio(patrimonioId: Long): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE usuarioId = :usuarioId ORDER BY dataColeta DESC")
    suspend fun getColetasByUsuario(usuarioId: Long): List<ColetaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColeta(coleta: ColetaEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColetas(coletas: List<ColetaEntity>): List<Long>
    
    @Update
    suspend fun updateColeta(coleta: ColetaEntity)
    
    @Delete
    suspend fun deleteColeta(coleta: ColetaEntity)
    
    @Query("DELETE FROM coleta WHERE id = :id")
    suspend fun deleteColetaById(id: Long)
    
    @Query("UPDATE coleta SET sincronizado = 1, servidorId = :servidorId WHERE id = :id")
    suspend fun markAsSynchronized(id: Long, servidorId: Long)
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1")
    suspend fun clearSynchronizedColetas()
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM coleta")
    suspend fun getTotalCount(): Int
    
    // Métodos de sincronização offline
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizada = 0 
        AND (tentativasSincronizacao < 3 OR tentativasSincronizacao IS NULL)
        ORDER BY dataColeta ASC
    """)
    suspend fun getPendingSync(): List<ColetaEntity>
    
    @Query("""
        SELECT * FROM coleta 
        WHERE sincronizada = 0 
        AND tentativasSincronizacao >= 3
        ORDER BY ultimaTentativaSincronizacao DESC
    """)
    suspend fun getFailedSync(): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE id = :id")
    suspend fun getById(id: Long): ColetaEntity?
    
    @Update
    suspend fun update(coleta: ColetaEntity)
    
    // ===== LIMPEZA AUTOMÁTICA =====
    
    @Query("""
        DELETE FROM coleta 
        WHERE sincronizado = 1 
        AND dataColeta < :timestamp
    """)
    suspend fun deleteOldSyncedColetas(timestamp: Long): Int
}