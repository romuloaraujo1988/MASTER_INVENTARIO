package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Usuário
 */
@Dao
interface UsuarioDao {
    
    @Query("SELECT * FROM usuario ORDER BY nome ASC")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>
    
    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun getUsuarioById(id: Long): UsuarioEntity?
    
    @Query("SELECT * FROM usuario WHERE email = :email")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?
    
    @Query("SELECT * FROM usuario WHERE ativo = 1 ORDER BY nome ASC")
    suspend fun getUsuariosAtivos(): List<UsuarioEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>): List<Long>
    
    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)
    
    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)
    
    @Query("DELETE FROM usuario WHERE id = :id")
    suspend fun deleteUsuarioById(id: Long)
    
    @Query("UPDATE usuario SET ativo = 0 WHERE id = :id")
    suspend fun deactivateUsuario(id: Long)
    
    @Query("UPDATE usuario SET ativo = 1 WHERE id = :id")
    suspend fun activateUsuario(id: Long)
    
    @Query("SELECT COUNT(*) FROM usuario WHERE ativo = 1")
    suspend fun getActiveCount(): Int
    
    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM usuario")
    suspend fun clearAll()
}
