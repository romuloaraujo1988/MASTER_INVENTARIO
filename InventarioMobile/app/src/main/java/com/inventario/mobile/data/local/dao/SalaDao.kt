package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SalaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Sala
 */
@Dao
interface SalaDao {
    
    @Query("SELECT * FROM sala ORDER BY nome ASC")
    fun getAllSalas(): Flow<List<SalaEntity>>
    
    @Query("SELECT * FROM sala WHERE id = :id")
    suspend fun getSalaById(id: Long): SalaEntity?
    
    @Query("SELECT * FROM sala WHERE setorId = :setorId ORDER BY nome ASC")
    suspend fun getSalasBySetor(setorId: Long): List<SalaEntity>
    
    @Query("SELECT * FROM sala WHERE nome LIKE '%' || :nome || '%' ORDER BY nome ASC")
    suspend fun getSalasByNome(nome: String): List<SalaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSala(sala: SalaEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalas(salas: List<SalaEntity>): List<Long>
    
    @Update
    suspend fun updateSala(sala: SalaEntity)
    
    @Delete
    suspend fun deleteSala(sala: SalaEntity)
    
    @Query("DELETE FROM sala WHERE id = :id")
    suspend fun deleteSalaById(id: Long)
    
    @Query("SELECT COUNT(*) FROM sala WHERE setorId = :setorId")
    suspend fun getCountBySetor(setorId: Long): Int
    
    @Query("SELECT COUNT(*) FROM sala")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM sala")
    suspend fun clearAll()
}