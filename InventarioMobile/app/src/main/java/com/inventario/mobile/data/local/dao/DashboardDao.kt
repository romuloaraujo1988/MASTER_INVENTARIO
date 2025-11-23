package com.inventario.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.inventario.mobile.data.local.dto.DashboardStatsDto
import kotlinx.coroutines.flow.Flow

/**
 * DAO para estatísticas do Dashboard
 * 
 * ✨ REATIVO: Usa Flow para observar mudanças automáticas no banco
 * 
 * v2.4: Corrigido para usar campos corretos da tabela coleta
 */
@Dao
interface DashboardDao {
    
    /**
     * 🔄 MÉTODO REATIVO - Observa mudanças nas tabelas automaticamente
     * 
     * Quando qualquer INSERT/UPDATE/DELETE acontecer nas tabelas:
     * - coleta (campo: idPatrimonio, idInventario)
     * - patrimonio (campo: id)
     * 
     * Este Flow emitirá um novo valor automaticamente!
     * 
     * Não precisa invalidar cache manualmente!
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT p.id) as totalPatrimonios,
            COUNT(DISTINCT CASE WHEN c.id IS NOT NULL THEN p.id END) as totalColetados,
            COUNT(DISTINCT CASE WHEN c.id IS NULL THEN p.id END) as totalPendentes,
            CAST(
                CASE 
                    WHEN COUNT(DISTINCT p.id) > 0 
                    THEN (COUNT(DISTINCT CASE WHEN c.id IS NOT NULL THEN p.id END) * 100.0 / COUNT(DISTINCT p.id))
                    ELSE 0 
                END AS REAL
            ) as percentualColetado
        FROM patrimonio p
        LEFT JOIN coleta c ON p.id = c.idPatrimonio
        WHERE (:inventarioId IS NULL OR c.idInventario = :inventarioId OR c.idInventario IS NULL)
    """)
    fun observarEstatisticas(inventarioId: Int? = null): Flow<DashboardStatsDto>
    
    /**
     * Método tradicional (não reativo) - para compatibilidade
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT p.id) as totalPatrimonios,
            COUNT(DISTINCT CASE WHEN c.id IS NOT NULL THEN p.id END) as totalColetados,
            COUNT(DISTINCT CASE WHEN c.id IS NULL THEN p.id END) as totalPendentes,
            CAST(
                CASE 
                    WHEN COUNT(DISTINCT p.id) > 0 
                    THEN (COUNT(DISTINCT CASE WHEN c.id IS NOT NULL THEN p.id END) * 100.0 / COUNT(DISTINCT p.id))
                    ELSE 0 
                END AS REAL
            ) as percentualColetado
        FROM patrimonio p
        LEFT JOIN coleta c ON p.id = c.idPatrimonio
        WHERE (:inventarioId IS NULL OR c.idInventario = :inventarioId OR c.idInventario IS NULL)
    """)
    suspend fun buscarEstatisticas(inventarioId: Int? = null): DashboardStatsDto?
    
    /**
     * 🔄 Observa total de patrimônios em tempo real
     * v2.5: Conta TODOS os patrimônios (independente do status)
     */
    @Query("SELECT COUNT(*) FROM patrimonio")
    fun observarTotalPatrimonios(): Flow<Int>
    
    /**
     * 🔄 Observa total de coletas LOCAIS NÃO SINCRONIZADAS em tempo real
     * v2.5: Conta apenas coletas NÃO sincronizadas (sincronizado = 0 ou NULL)
     * Isso evita duplicação com as coletas já contadas pelo servidor
     */
    @Query("SELECT COUNT(*) FROM coleta WHERE idInventario = :inventarioId AND (sincronizado = 0 OR sincronizado IS NULL)")
    fun observarTotalColetas(inventarioId: Int): Flow<Int>
    
    /**
     * 🔄 Observa percentual de coleta em tempo real
     */
    @Query("""
        SELECT 
            CAST(
                CASE 
                    WHEN COUNT(DISTINCT p.id) > 0 
                    THEN (COUNT(DISTINCT CASE WHEN c.id IS NOT NULL THEN p.id END) * 100.0 / COUNT(DISTINCT p.id))
                    ELSE 0 
                END AS REAL
            )
        FROM patrimonio p
        LEFT JOIN coleta c ON p.id = c.idPatrimonio
        WHERE c.idInventario = :inventarioId OR c.idInventario IS NULL
    """)
    fun observarPercentualColetado(inventarioId: Int): Flow<Float>
}
