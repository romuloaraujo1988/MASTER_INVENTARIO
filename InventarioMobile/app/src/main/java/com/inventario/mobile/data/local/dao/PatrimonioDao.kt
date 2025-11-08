package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.PatrimonioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de banco de dados relacionadas a Patrimônio
 */
@Dao
interface PatrimonioDao {
    
    @Query("SELECT * FROM patrimonio ORDER BY codigo ASC")
    fun getAllPatrimonios(): Flow<List<PatrimonioEntity>>
    
    @Query("SELECT * FROM patrimonio ORDER BY codigo ASC")
    suspend fun getAllPatrimoniosList(): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonio WHERE id = :id")
    suspend fun getPatrimonioById(id: Long): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonio WHERE codigo = :codigo")
    suspend fun getPatrimonioByCodigo(codigo: String): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonio WHERE qrCode = :qrCode")
    suspend fun getPatrimonioByQrCode(qrCode: String): PatrimonioEntity?
    
    @Query("SELECT * FROM patrimonio WHERE salaId = :salaId ORDER BY codigo ASC")
    suspend fun getPatrimoniosBySala(salaId: Long): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonio WHERE coletado = 0 ORDER BY codigo ASC")
    suspend fun getPatrimoniosNaoColetados(): List<PatrimonioEntity>
    
    @Query("SELECT * FROM patrimonio WHERE coletado = 1 ORDER BY codigo ASC")
    suspend fun getPatrimoniosColetados(): List<PatrimonioEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatrimonio(patrimonio: PatrimonioEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatrimonios(patrimonios: List<PatrimonioEntity>): List<Long>
    
    @Update
    suspend fun updatePatrimonio(patrimonio: PatrimonioEntity)
    
    @Delete
    suspend fun deletePatrimonio(patrimonio: PatrimonioEntity)
    
    @Query("DELETE FROM patrimonio WHERE id = :id")
    suspend fun deletePatrimonioById(id: Long)
    
    @Query("UPDATE patrimonio SET coletado = 1 WHERE id = :id")
    suspend fun markAsColetado(id: Long)
    
    @Query("UPDATE patrimonio SET coletado = 0 WHERE id = :id")
    suspend fun markAsNaoColetado(id: Long)
    
    @Query("SELECT COUNT(*) FROM patrimonio WHERE coletado = 0")
    suspend fun getCountNaoColetados(): Int
    
    @Query("SELECT COUNT(*) FROM patrimonio WHERE coletado = 1")
    suspend fun getCountColetados(): Int
    
    @Query("SELECT COUNT(*) FROM patrimonio")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM patrimonio")
    suspend fun clearAll()
}