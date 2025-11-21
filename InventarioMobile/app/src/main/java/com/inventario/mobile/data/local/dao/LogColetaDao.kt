package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.LogColetaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para log de auditoria de coletas
 * 
 * v2.2: Rastreabilidade completa de operações
 */
@Dao
interface LogColetaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(log: LogColetaEntity): Long
    
    @Query("SELECT * FROM log_coleta WHERE coletaId = :coletaId ORDER BY timestamp DESC")
    suspend fun buscarPorColeta(coletaId: Long): List<LogColetaEntity>
    
    @Query("SELECT * FROM log_coleta WHERE acao = :acao ORDER BY timestamp DESC LIMIT :limit")
    suspend fun buscarPorAcao(acao: String, limit: Int = 100): List<LogColetaEntity>
    
    @Query("SELECT * FROM log_coleta ORDER BY timestamp DESC LIMIT :limit")
    suspend fun buscarRecentes(limit: Int = 100): List<LogColetaEntity>
    
    @Query("SELECT * FROM log_coleta WHERE sucesso = 0 ORDER BY timestamp DESC")
    suspend fun buscarErros(): List<LogColetaEntity>
    
    @Query("SELECT COUNT(*) FROM log_coleta WHERE acao = :acao AND timestamp > :timestampInicio")
    suspend fun contarAcoes(acao: String, timestampInicio: Long): Int
    
    @Query("DELETE FROM log_coleta WHERE timestamp < :timestamp")
    suspend fun limparAntigos(timestamp: Long): Int
    
    @Query("SELECT * FROM log_coleta ORDER BY timestamp DESC")
    fun observarTodos(): Flow<List<LogColetaEntity>>
    
    /**
     * Busca logs de sincronização com erro
     */
    @Query("""
        SELECT * FROM log_coleta 
        WHERE acao IN ('ERRO_SYNC', 'TENTATIVA_SYNC') 
        AND sucesso = 0
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun buscarErrosSincronizacao(limit: Int = 50): List<LogColetaEntity>
    
    /**
     * Estatísticas de sincronização
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN sucesso = 1 THEN 1 ELSE 0 END) as sucesso,
            SUM(CASE WHEN sucesso = 0 THEN 1 ELSE 0 END) as erro
        FROM log_coleta
        WHERE acao = 'SINCRONIZADA'
        AND timestamp > :timestampInicio
    """)
    suspend fun estatisticasSincronizacao(timestampInicio: Long): EstatisticasSync
}

/**
 * Estatísticas de sincronização
 */
data class EstatisticasSync(
    val total: Int,
    val sucesso: Int,
    val erro: Int
) {
    val taxaSucesso: Float get() = if (total > 0) sucesso.toFloat() / total else 0f
}
