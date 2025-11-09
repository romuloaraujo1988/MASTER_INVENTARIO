package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.ResponsavelEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Responsável no banco local
 */
@Dao
interface ResponsavelDao {
    
    @Query("SELECT * FROM responsavel ORDER BY nome")
    suspend fun buscarTodos(): List<ResponsavelEntity>
    
    @Query("SELECT * FROM responsavel ORDER BY nome")
    fun observarTodos(): Flow<List<ResponsavelEntity>>
    
    @Query("SELECT * FROM responsavel WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): ResponsavelEntity?
    
    @Query("SELECT * FROM responsavel WHERE nome LIKE '%' || :termo || '%' ORDER BY nome")
    suspend fun buscarPorNome(termo: String): List<ResponsavelEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(responsavel: ResponsavelEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(responsaveis: List<ResponsavelEntity>)
    
    @Query("DELETE FROM responsavel")
    suspend fun limparTodos()
    
    @Query("SELECT COUNT(*) FROM responsavel")
    suspend fun contar(): Int
}
