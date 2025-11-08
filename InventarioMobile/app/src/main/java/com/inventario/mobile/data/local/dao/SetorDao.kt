package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SetorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Setor
 */
@Dao
interface SetorDao {
    
    @Query("SELECT * FROM setor ORDER BY nome ASC")
    fun getAllSetores(): Flow<List<SetorEntity>>
    
    @Query("SELECT * FROM setor WHERE id = :id")
    suspend fun getSetorById(id: Long): SetorEntity?
    
    @Query("SELECT * FROM setor WHERE nome LIKE '%' || :nome || '%' ORDER BY nome ASC")
    suspend fun getSetoresByNome(nome: String): List<SetorEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetor(setor: SetorEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetores(setores: List<SetorEntity>): List<Long>
    
    @Update
    suspend fun updateSetor(setor: SetorEntity)
    
    @Delete
    suspend fun deleteSetor(setor: SetorEntity)
    
    @Query("DELETE FROM setor WHERE id = :id")
    suspend fun deleteSetorById(id: Long)
    
    @Query("SELECT COUNT(*) FROM setor")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM setor")
    suspend fun clearAll()
}