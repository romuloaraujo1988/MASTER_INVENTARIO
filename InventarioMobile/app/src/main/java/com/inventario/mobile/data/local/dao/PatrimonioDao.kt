package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.PatrimonioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Patrimônio no banco local
 */
@Dao
interface PatrimonioDao {
    
    @Query("SELECT * FROM patrimonio WHERE numero = :numero LIMIT 1")
    suspend fun buscarPorNumero(numero: String): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonio WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonio WHERE coletado = 0")
    fun observarNaoColetados(): Flow<List<PatrimonioEntity>>
    
    @Query("SELECT * FROM patrimonio WHERE coletado = 0 LIMIT :limit OFFSET :offset")
    suspend fun buscarNaoColetadosPaginado(limit: Int, offset: Int): List<PatrimonioEntity>
    
    @Query("SELECT DISTINCT descricao FROM patrimonio WHERE coletado = 0 ORDER BY descricao")
    suspend fun buscarDescricoesNaoColetadas(): List<String>
    
    @Query("SELECT * FROM patrimonio WHERE descricao = :descricao AND coletado = 0")
    suspend fun buscarPorDescricaoNaoColetados(descricao: String): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonio WHERE idSala = :idSala AND coletado = 0")
    suspend fun buscarPorSalaNaoColetados(idSala: Int): List<PatrimonioEntity>
    
    @Query("UPDATE patrimonio SET coletado = 1 WHERE id = :id")
    suspend fun marcarComoColetado(id: Int)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(patrimonio: PatrimonioEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(patrimonios: List<PatrimonioEntity>)
    
    @Query("DELETE FROM patrimonio")
    suspend fun limparTodos()
    
    @Query("SELECT COUNT(*) FROM patrimonio")
    suspend fun contarTodos(): Int
    
    @Query("SELECT COUNT(*) FROM patrimonio WHERE coletado = 0")
    suspend fun contarNaoColetados(): Int
    
    @Query("SELECT COUNT(*) FROM patrimonio WHERE coletado = 1")
    suspend fun contarColetados(): Int
    
    @Query("SELECT * FROM patrimonio")
    suspend fun getAllPatrimoniosList(): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonio WHERE coletado = 0")
    suspend fun syncData(): List<PatrimonioEntity>
}
