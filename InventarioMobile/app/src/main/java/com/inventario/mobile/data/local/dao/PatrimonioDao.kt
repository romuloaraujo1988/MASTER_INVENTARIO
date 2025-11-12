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
    
    // ========================================
    // Queries para Gráficos
    // ========================================
    
    /**
     * Conta patrimônios por status
     */
    @Query("SELECT COUNT(*) FROM patrimonio WHERE UPPER(status) = UPPER(:status)")
    suspend fun countByStatus(status: String): Int
    
    /**
     * Busca distribuição de patrimônios por status
     */
    @Query("""
        SELECT 
            COALESCE(status, 'SEM STATUS') as status,
            COUNT(*) as quantidade
        FROM patrimonio
        GROUP BY status
        ORDER BY quantidade DESC
    """)
    suspend fun getStatusDistribution(): List<StatusData>
    
    /**
     * Busca patrimônios por setor (top 10)
     * Nota: Como não temos tabela setor no Room, vamos usar o campo nomeSetor da sala
     */
    @Query("""
        SELECT 
            COALESCE(sa.nomeSetor, 'Sem Setor') as setor,
            COUNT(p.id) as quantidade
        FROM patrimonio p
        LEFT JOIN sala sa ON p.idSala = sa.id
        GROUP BY sa.nomeSetor
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getPatrimoniosPorSetor(): List<SetorData>
    
    /**
     * Conta total de patrimônios
     */
    @Query("SELECT COUNT(*) FROM patrimonio")
    suspend fun countAll(): Int
    
    /**
     * Busca estatísticas gerais de patrimônios
     * Nota: Removida temporariamente - usar métodos individuais
     */
    // Usar countAll(), contarColetados(), contarNaoColetados(), countByStatus()
    
    /**
     * Busca descrições mais frequentes
     */
    @Query("""
        SELECT 
            descricao,
            COUNT(*) as quantidade
        FROM patrimonio
        GROUP BY descricao
        ORDER BY quantidade DESC
        LIMIT 10
    """)
    suspend fun getDescricoesFrequentes(): List<TopItemData>
    
}
