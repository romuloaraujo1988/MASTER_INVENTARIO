package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.HistoricoScanEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações do histórico de scans
 * 
 * @since v2.11.0
 */
@Dao
interface HistoricoScanDao {
    
    /**
     * Insere um novo registro no histórico
     * Se o patrimônio já existe, atualiza o timestamp
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(historico: HistoricoScanEntity): Long
    
    /**
     * Busca todos os registros ordenados por timestamp (mais recente primeiro)
     */
    @Query("SELECT * FROM historico_scan ORDER BY timestamp DESC")
    suspend fun buscarTodos(): List<HistoricoScanEntity>
    
    /**
     * Busca os últimos N registros
     */
    @Query("SELECT * FROM historico_scan ORDER BY timestamp DESC LIMIT :limite")
    suspend fun buscarUltimos(limite: Int = 20): List<HistoricoScanEntity>
    
    /**
     * Observa os últimos registros em tempo real
     */
    @Query("SELECT * FROM historico_scan ORDER BY timestamp DESC LIMIT :limite")
    fun observarUltimos(limite: Int = 20): Flow<List<HistoricoScanEntity>>
    
    /**
     * Busca por número do patrimônio
     */
    @Query("SELECT * FROM historico_scan WHERE numeroPatrimonio = :numero ORDER BY timestamp DESC LIMIT 1")
    suspend fun buscarPorNumero(numero: String): HistoricoScanEntity?
    
    /**
     * Busca registros de hoje
     */
    @Query("""
        SELECT * FROM historico_scan 
        WHERE timestamp >= :inicioHoje 
        ORDER BY timestamp DESC
    """)
    suspend fun buscarDeHoje(inicioHoje: Long): List<HistoricoScanEntity>
    
    /**
     * Conta total de scans de hoje
     */
    @Query("SELECT COUNT(*) FROM historico_scan WHERE timestamp >= :inicioHoje")
    suspend fun contarDeHoje(inicioHoje: Long): Int
    
    /**
     * Conta scans que resultaram em coleta hoje
     */
    @Query("SELECT COUNT(*) FROM historico_scan WHERE timestamp >= :inicioHoje AND foiColetado = 1")
    suspend fun contarColetasDeHoje(inicioHoje: Long): Int
    
    /**
     * Atualiza status de coleta de um registro
     */
    @Query("UPDATE historico_scan SET foiColetado = 1 WHERE numeroPatrimonio = :numero AND timestamp = (SELECT MAX(timestamp) FROM historico_scan WHERE numeroPatrimonio = :numero)")
    suspend fun marcarComoColetado(numero: String)
    
    /**
     * Remove registros antigos, mantendo apenas os últimos N
     */
    @Query("""
        DELETE FROM historico_scan 
        WHERE id NOT IN (
            SELECT id FROM historico_scan 
            ORDER BY timestamp DESC 
            LIMIT :manter
        )
    """)
    suspend fun limparAntigos(manter: Int = 50)
    
    /**
     * Limpa todo o histórico
     */
    @Query("DELETE FROM historico_scan")
    suspend fun limparTudo()
    
    /**
     * Conta total de registros
     */
    @Query("SELECT COUNT(*) FROM historico_scan")
    suspend fun contar(): Int
    
    /**
     * Busca registros por sala
     */
    @Query("SELECT * FROM historico_scan WHERE salaId = :salaId ORDER BY timestamp DESC")
    suspend fun buscarPorSala(salaId: Int): List<HistoricoScanEntity>
    
    /**
     * Busca registros que foram coletados
     */
    @Query("SELECT * FROM historico_scan WHERE foiColetado = 1 ORDER BY timestamp DESC LIMIT :limite")
    suspend fun buscarColetados(limite: Int = 20): List<HistoricoScanEntity>
    
    /**
     * Busca registros que NÃO foram coletados (apenas consultados)
     */
    @Query("SELECT * FROM historico_scan WHERE foiColetado = 0 ORDER BY timestamp DESC LIMIT :limite")
    suspend fun buscarApenasConsultados(limite: Int = 20): List<HistoricoScanEntity>
}
