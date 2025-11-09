package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.ColetaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Coleta no banco local
 */
@Dao
interface ColetaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(coleta: ColetaEntity): Long
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta ASC")
    suspend fun buscarPendentes(): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0")
    fun observarPendentes(): Flow<List<ColetaEntity>>
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    fun observarQuantidadePendentes(): Flow<Int>
    
    @Query("UPDATE coleta SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: Long)
    
    @Query("UPDATE coleta SET tentativasSincronizacao = tentativasSincronizacao + 1, erroSincronizacao = :erro WHERE id = :id")
    suspend fun registrarErroSincronizacao(id: Long, erro: String)
    
    @Query("DELETE FROM coleta WHERE id = :id")
    suspend fun deletar(id: Long)
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1")
    suspend fun limparSincronizadas()
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC LIMIT :limit")
    suspend fun buscarRecentes(limit: Int): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coleta")
    suspend fun contarTodas(): Int
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0")
    suspend fun getColetasPendentes(): List<ColetaEntity>
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1 AND dataColeta < :timestamp")
    suspend fun deleteOldSyncedColetas(timestamp: Long): Int
}
