package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SincronizacaoEntity

/**
 * DAO para operações de Sincronização
 */
@Dao
interface SincronizacaoDao {

    @Query("SELECT * FROM sincronizacao WHERE sincronizado = 0 ORDER BY dataHora ASC")
    suspend fun getSincronizacoesPendentes(): List<SincronizacaoEntity>

    @Query("SELECT * FROM sincronizacao WHERE erro IS NOT NULL ORDER BY dataHora DESC")
    suspend fun getSincronizacoesComErro(): List<SincronizacaoEntity>

    @Query("SELECT * FROM sincronizacao WHERE entidade = :entidade AND entidadeId = :entidadeId LIMIT 1")
    suspend fun getSincronizacaoByEntidade(entidade: String, entidadeId: Long): SincronizacaoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sincronizacao: SincronizacaoEntity): Long

    @Update
    suspend fun update(sincronizacao: SincronizacaoEntity)

    @Query("UPDATE sincronizacao SET sincronizado = 1, erro = NULL WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Long)

    @Query("UPDATE sincronizacao SET erro = :erro WHERE id = :id")
    suspend fun marcarComoErro(id: Long, erro: String)

    @Delete
    suspend fun delete(sincronizacao: SincronizacaoEntity)

    @Query("DELETE FROM sincronizacao WHERE entidade = :entidade AND entidadeId = :entidadeId")
    suspend fun deleteSincronizacaoByEntidade(entidade: String, entidadeId: Long)

    @Query("SELECT COUNT(*) FROM sincronizacao WHERE sincronizado = 0")
    suspend fun countSincronizacoesPendentes(): Int

    @Query("DELETE FROM sincronizacao WHERE sincronizado = 1")
    suspend fun limparSincronizadas()
}
