package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SalaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Sala no banco local
 */
@Dao
interface SalaDao {
    
    @Query("SELECT * FROM sala ORDER BY nome")
    suspend fun buscarTodas(): List<SalaEntity>
    
    @Query("SELECT * FROM sala ORDER BY nome")
    fun observarTodas(): Flow<List<SalaEntity>>
    
    @Query("SELECT * FROM sala WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): SalaEntity?
    
    @Query("SELECT * FROM sala WHERE nome LIKE '%' || :termo || '%' ORDER BY nome")
    suspend fun buscarPorNome(termo: String): List<SalaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(sala: SalaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(salas: List<SalaEntity>)
    
    @Query("DELETE FROM sala")
    suspend fun limparTodas()
    
    @Query("SELECT COUNT(*) FROM sala")
    suspend fun contar(): Int
}
