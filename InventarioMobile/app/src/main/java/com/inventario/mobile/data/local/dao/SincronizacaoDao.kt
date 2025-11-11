package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SincronizacaoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Sincronização no banco local
 */
@Dao
interface SincronizacaoDao {
    
    @Query("SELECT * FROM sincronizacao WHERE sincronizado = 0 ORDER BY dataHora ASC")
    suspend fun buscarPendentes(): List<SincronizacaoEntity>
    
    @Query("SELECT * FROM sincronizacao WHERE sincronizado = 0 ORDER BY dataHora ASC")
    fun observarPendentes(): Flow<List<SincronizacaoEntity>>
    
    @Query("SELECT * FROM sincronizacao ORDER BY dataHora DESC LIMIT 1")
    suspend fun getUltimaSincronizacao(): SincronizacaoEntity?
    
    @Query("SELECT COUNT(*) FROM sincronizacao WHERE sincronizado = 0")
    suspend fun contarPendentes(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(sincronizacao: SincronizacaoEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(sincronizacoes: List<SincronizacaoEntity>)
    
    @Update
    suspend fun atualizar(sincronizacao: SincronizacaoEntity)
    
    @Query("UPDATE sincronizacao SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)
    
    @Query("UPDATE sincronizacao SET sincronizado = 1, erro = NULL WHERE id IN (:ids)")
    suspend fun marcarComoSincronizados(ids: List<Long>)
    
    @Query("UPDATE sincronizacao SET erro = :erro WHERE id = :id")
    suspend fun registrarErro(id: Long, erro: String)
    
    @Query("DELETE FROM sincronizacao WHERE sincronizado = 1 AND dataHora < :timestamp")
    suspend fun limparSincronizados(timestamp: Long)
    
    @Query("DELETE FROM sincronizacao")
    suspend fun limparTodas()
}
