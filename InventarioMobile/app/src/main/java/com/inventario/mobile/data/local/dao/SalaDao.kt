package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.SalaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Sala no banco local
 * Otimizado para paginação e busca rápida
 */
@Dao
interface SalaDao {
    
    /**
     * Busca todas as salas ordenadas por nome
     */
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC")
    suspend fun buscarTodas(): List<SalaEntity>
    
    /**
     * Observa todas as salas (Flow para updates automáticos)
     */
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC")
    fun observarTodas(): Flow<List<SalaEntity>>
    
    /**
     * Busca sala por ID
     */
    @Query("SELECT * FROM sala WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): SalaEntity?
    
    /**
     * Busca salas por nome (case-insensitive)
     * Otimizado com índice na coluna nome
     * 
     * @param query Termo de busca (usa LIKE com %)
     */
    @Query("SELECT * FROM sala WHERE ativa = 1 AND nome LIKE :query ORDER BY nome ASC")
    suspend fun buscarPorNome(query: String): List<SalaEntity>
    
    /**
     * Busca salas paginadas
     * 
     * @param limit Quantidade de itens
     * @param offset Posição inicial
     */
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC LIMIT :limit OFFSET :offset")
    suspend fun buscarPaginado(limit: Int, offset: Int): List<SalaEntity>
    
    /**
     * Busca salas por nome paginadas
     * 
     * @param query Termo de busca
     * @param limit Quantidade de itens
     * @param offset Posição inicial
     */
    @Query("SELECT * FROM sala WHERE ativa = 1 AND nome LIKE :query ORDER BY nome ASC LIMIT :limit OFFSET :offset")
    suspend fun buscarPorNomePaginado(query: String, limit: Int, offset: Int): List<SalaEntity>
    
    /**
     * Insere uma sala
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(sala: SalaEntity)
    
    /**
     * Insere múltiplas salas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(salas: List<SalaEntity>)
    
    /**
     * Remove todas as salas
     */
    @Query("DELETE FROM sala")
    suspend fun limparTodas()
    
    /**
     * Conta total de salas ativas
     */
    @Query("SELECT COUNT(*) FROM sala WHERE ativa = 1")
    suspend fun contar(): Int
    
    /**
     * Conta salas que correspondem à busca
     */
    @Query("SELECT COUNT(*) FROM sala WHERE ativa = 1 AND nome LIKE :query")
    suspend fun contarPorNome(query: String): Int
    
    // ========================================
    // Queries para Inventário por Sala
    // ========================================
    
    /**
     * Busca todas as salas com estatísticas de coleta (total e coletados).
     * Usa LEFT JOIN com patrimonio para contar totais.
     * 
     * @return Lista de salas com estatísticas
     */
    @Query("""
        SELECT s.*, 
               COUNT(p.id) as total_patrimonios,
               SUM(CASE WHEN p.coletado = 1 THEN 1 ELSE 0 END) as coletados
        FROM sala s
        LEFT JOIN patrimonio p ON p.idSala = s.id
        WHERE s.ativa = 1
        GROUP BY s.id
        ORDER BY s.nome ASC
    """)
    suspend fun buscarComEstatisticas(): List<com.inventario.mobile.data.local.entity.SalaComEstatisticasEntity>
    
    /**
     * Busca salas por nome ou número (case-insensitive).
     * 
     * @param query Termo de busca (usa LIKE com %)
     * @return Lista de salas que correspondem à busca
     */
    @Query("""
        SELECT * FROM sala 
        WHERE ativa = 1 
        AND (nome LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%')
        ORDER BY nome ASC
    """)
    suspend fun buscarPorNomeOuNumero(query: String): List<SalaEntity>
}
