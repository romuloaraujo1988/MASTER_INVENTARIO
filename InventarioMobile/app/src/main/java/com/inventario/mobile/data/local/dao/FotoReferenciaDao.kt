package com.inventario.mobile.data.local.dao

import androidx.room.*
import com.inventario.mobile.data.local.entity.FotoReferenciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de Foto de Referência no banco local
 * Otimizado para busca por descrição e delta sync
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Dao
interface FotoReferenciaDao {
    
    // ========================================
    // Queries de Busca
    // ========================================
    
    /**
     * Busca todas as fotos de referência ativas
     */
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1 ORDER BY descricaoNormalizada ASC")
    suspend fun buscarTodas(): List<FotoReferenciaEntity>
    
    /**
     * Observa todas as fotos de referência ativas (Flow para updates automáticos)
     */
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1 ORDER BY descricaoNormalizada ASC")
    fun observarTodas(): Flow<List<FotoReferenciaEntity>>
    
    /**
     * Busca foto por ID
     */
    @Query("SELECT * FROM foto_referencia WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): FotoReferenciaEntity?
    
    /**
     * Busca foto por descrição normalizada (busca exata)
     * Otimizado com índice único na coluna descricaoNormalizada
     * 
     * @param descricaoNormalizada Descrição normalizada (case-sensitive)
     */
    @Query("SELECT * FROM foto_referencia WHERE descricaoNormalizada = :descricaoNormalizada AND ativo = 1 LIMIT 1")
    suspend fun buscarPorDescricao(descricaoNormalizada: String): FotoReferenciaEntity?
    
    /**
     * Busca foto por descrição normalizada (busca parcial, case-insensitive)
     * Útil para busca por texto digitado pelo usuário
     * 
     * @param query Termo de busca (usa LIKE com %)
     */
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1 AND descricaoNormalizada LIKE :query ORDER BY descricaoNormalizada ASC")
    suspend fun buscarPorDescricaoParcial(query: String): List<FotoReferenciaEntity>
    
    /**
     * Busca fotos por lista de descrições (busca em lote)
     * Otimizado para buscar múltiplas fotos de uma vez
     * 
     * @param descricoes Lista de descrições normalizadas
     */
    @Query("SELECT * FROM foto_referencia WHERE descricaoNormalizada IN (:descricoes) AND ativo = 1")
    suspend fun buscarPorDescricoes(descricoes: List<String>): List<FotoReferenciaEntity>
    
    // ========================================
    // Queries para Delta Sync
    // ========================================
    
    /**
     * Busca fotos atualizadas após timestamp (delta sync)
     * Inclui fotos inativas para que o cliente possa removê-las
     * 
     * @param timestamp Timestamp em milissegundos
     */
    @Query("SELECT * FROM foto_referencia WHERE dataAtualizacao > :timestamp ORDER BY dataAtualizacao ASC")
    suspend fun buscarAtualizadasDesde(timestamp: Long): List<FotoReferenciaEntity>
    
    /**
     * Busca timestamp da última atualização
     * Usado para determinar o ponto de partida do delta sync
     */
    @Query("SELECT MAX(dataAtualizacao) FROM foto_referencia")
    suspend fun buscarUltimaAtualizacao(): Long?
    
    /**
     * Busca hash de todas as fotos ativas
     * Usado para verificar integridade do cache
     */
    @Query("SELECT id, hashImagem FROM foto_referencia WHERE ativo = 1")
    suspend fun buscarHashesFotos(): List<FotoHashInfo>
    
    // ========================================
    // Operações de Escrita
    // ========================================
    
    /**
     * Insere ou atualiza uma foto de referência
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(foto: FotoReferenciaEntity)
    
    /**
     * Insere ou atualiza múltiplas fotos de referência
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(fotos: List<FotoReferenciaEntity>)
    
    /**
     * Atualiza uma foto de referência
     */
    @Update
    suspend fun atualizar(foto: FotoReferenciaEntity)
    
    /**
     * Desativa uma foto de referência (soft delete)
     * Mantém o registro para delta sync
     */
    @Query("UPDATE foto_referencia SET ativo = 0, dataAtualizacao = :timestamp WHERE id = :id")
    suspend fun desativar(id: Int, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Desativa fotos por lista de IDs
     */
    @Query("UPDATE foto_referencia SET ativo = 0, dataAtualizacao = :timestamp WHERE id IN (:ids)")
    suspend fun desativarPorIds(ids: List<Int>, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Remove fotos inativas antigas (limpeza de cache)
     * Remove fotos inativas há mais de X dias
     * 
     * @param timestampLimite Timestamp limite (fotos inativas antes disso são removidas)
     */
    @Query("DELETE FROM foto_referencia WHERE ativo = 0 AND dataAtualizacao < :timestampLimite")
    suspend fun limparInativasAntigas(timestampLimite: Long)
    
    /**
     * Remove todas as fotos de referência
     * Usado para reset completo do cache
     */
    @Query("DELETE FROM foto_referencia")
    suspend fun limparTodas()
    
    // ========================================
    // Queries de Estatísticas
    // ========================================
    
    /**
     * Conta total de fotos ativas
     */
    @Query("SELECT COUNT(*) FROM foto_referencia WHERE ativo = 1")
    suspend fun contar(): Int
    
    /**
     * Conta fotos inativas (pendentes de remoção)
     */
    @Query("SELECT COUNT(*) FROM foto_referencia WHERE ativo = 0")
    suspend fun contarInativas(): Int
    
    /**
     * Calcula tamanho total do cache de fotos em bytes
     */
    @Query("SELECT COALESCE(SUM(tamanhoBytes), 0) FROM foto_referencia WHERE ativo = 1")
    suspend fun calcularTamanhoTotal(): Long
    
    /**
     * Busca estatísticas do cache
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN ativo = 1 THEN 1 ELSE 0 END) as ativas,
            SUM(CASE WHEN ativo = 0 THEN 1 ELSE 0 END) as inativas,
            COALESCE(SUM(tamanhoBytes), 0) as tamanhoTotalBytes,
            MAX(dataAtualizacao) as ultimaAtualizacao
        FROM foto_referencia
    """)
    suspend fun buscarEstatisticas(): FotoReferenciaStats
    
    // ========================================
    // Queries para Controle de Armazenamento
    // ========================================
    
    /**
     * Busca fotos ordenadas por tamanho (maiores primeiro)
     * Usado para limpeza de cache quando espaço está baixo
     */
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1 ORDER BY tamanhoBytes DESC LIMIT :limite")
    suspend fun buscarMaioresFotos(limite: Int): List<FotoReferenciaEntity>
    
    /**
     * Busca fotos mais antigas (para limpeza LRU)
     */
    @Query("SELECT * FROM foto_referencia WHERE ativo = 1 ORDER BY dataAtualizacao ASC LIMIT :limite")
    suspend fun buscarFotosMaisAntigas(limite: Int): List<FotoReferenciaEntity>
}

/**
 * Data class para resultado de busca de hashes
 */
data class FotoHashInfo(
    val id: Int,
    val hashImagem: String?
)

/**
 * Data class para estatísticas do cache de fotos
 */
data class FotoReferenciaStats(
    val total: Int,
    val ativas: Int,
    val inativas: Int,
    val tamanhoTotalBytes: Long,
    val ultimaAtualizacao: Long?
)
