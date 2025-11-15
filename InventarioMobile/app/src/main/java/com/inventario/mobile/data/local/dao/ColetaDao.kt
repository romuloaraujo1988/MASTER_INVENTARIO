package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.ColetaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Coleta no banco local
 */
@Dao
interface ColetaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(coleta: ColetaEntity): Long
    
    @Query("UPDATE coleta SET sincronizado = :sincronizado, servidorId = :servidorId WHERE id = :id")
    suspend fun atualizarSincronizado(id: Long, sincronizado: Boolean, servidorId: Int)
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0 ORDER BY dataColeta ASC")
    suspend fun buscarPendentes(): List<ColetaEntity>
    
    @Query("SELECT * FROM coleta WHERE sincronizado = 0")
    fun observarPendentes(): Flow<List<ColetaEntity>>
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC")
    suspend fun buscarTodas(): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    fun observarQuantidadePendentes(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM coleta WHERE sincronizado = 0")
    suspend fun contarPendentes(): Int
    
    @Query("UPDATE coleta SET sincronizado = 1, servidorId = :servidorId WHERE id = :id")
    suspend fun marcarSincronizada(id: Long, servidorId: Long? = null)
    
    @Query("UPDATE coleta SET tentativasSincronizacao = tentativasSincronizacao + 1, erroSincronizacao = :erro WHERE id = :id")
    suspend fun registrarErroSincronizacao(id: Long, erro: String?)
    
    @Query("DELETE FROM coleta WHERE id = :id")
    suspend fun deletar(id: Long)
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1")
    suspend fun limparSincronizadas()
    
    @Query("DELETE FROM coleta WHERE sincronizado = 1 AND dataColeta < :timestamp")
    suspend fun limparSincronizadasAntigas(timestamp: Long): Int
    
    @Query("SELECT * FROM coleta ORDER BY dataColeta DESC LIMIT :limit")
    suspend fun buscarRecentes(limit: Int): List<ColetaEntity>
    
    @Query("SELECT COUNT(*) FROM coleta")
    suspend fun contarTodas(): Int
    
    // ========================================
    // Queries para Gráficos
    // ========================================
    
    /**
     * Conta coletas por inventário
     */
    @Query("SELECT COUNT(DISTINCT idPatrimonio) FROM coleta WHERE idInventario = :idInventario")
    suspend fun countByInventario(idInventario: Int): Int
    
    /**
     * Busca evolução diária das coletas (últimos 30 dias)
     * Retorna data formatada e quantidade acumulada
     */
    @Query("""
        SELECT 
            strftime('%d/%m', dataColeta / 1000, 'unixepoch') as data,
            COUNT(*) as quantidade
        FROM coleta
        WHERE idInventario = :idInventario
        GROUP BY date(dataColeta / 1000, 'unixepoch')
        ORDER BY date(dataColeta / 1000, 'unixepoch') ASC
        LIMIT 30
    """)
    suspend fun getEvolutionData(idInventario: Int): List<EvolutionData>
    
    /**
     * Busca top 10 descrições mais coletadas
     */
    @Query("""
        SELECT 
            p.descricao as descricao,
            COUNT(c.id) as quantidade
        FROM coleta c
        INNER JOIN patrimonio p ON c.idPatrimonio = p.id
        WHERE c.idInventario = :idInventario
        GROUP BY p.descricao
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getTopItems(idInventario: Int): List<TopItemData>
    
}

/**
 * DTO para dados de evolução diária
 */
data class EvolutionData(
    val data: String,
    val quantidade: Int
)

/**
 * DTO para top itens coletados
 */
data class TopItemData(
    val descricao: String,
    val quantidade: Int
)
