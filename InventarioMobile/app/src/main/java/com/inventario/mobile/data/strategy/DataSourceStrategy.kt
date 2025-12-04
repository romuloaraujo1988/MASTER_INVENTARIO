package com.inventario.mobile.data.strategy

import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.Sala

/**
 * Strategy Pattern - Interface para diferentes fontes de dados
 */
interface DataSourceStrategy {
    
    /**
     * Indica se esta estratégia está disponível
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Busca patrimônios
     */
    suspend fun getPatrimonios(): Result<List<Patrimonio>>
    
    /**
     * Busca patrimônio por número
     */
    suspend fun getPatrimonioPorNumero(numero: String): Result<Patrimonio>
    
    /**
     * Busca salas
     */
    suspend fun getSalas(): Result<List<Sala>>
    
    /**
     * Busca sala por ID
     */
    suspend fun getSalaPorId(id: Int): Result<Sala>
    
    /**
     * Busca descrições de patrimônios não coletados
     */
    suspend fun buscarDescricoesNaoColetadas(): Result<List<String>>
    
    /**
     * Busca patrimônios por descrição (não coletados)
     */
    suspend fun buscarPorDescricaoNaoColetados(descricao: String): Result<List<Patrimonio>>
    
    /**
     * Retorna o tipo de fonte de dados
     */
    fun getSourceType(): DataSourceType
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    /**
     * Busca patrimônios por sala com filtro opcional de status de coleta e paginação.
     */
    suspend fun buscarPorSala(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>>
    
    /**
     * Conta total de patrimônios em uma sala.
     */
    suspend fun contarPorSala(salaId: Int): Result<Int>
    
    /**
     * Conta patrimônios coletados em uma sala.
     */
    suspend fun contarColetadosPorSala(salaId: Int): Result<Int>
    
    // ========================================
    // Métodos para Busca Rápida de Patrimônio
    // ========================================
    
    /**
     * Busca patrimônios por query (número, descrição ou nome da sala)
     * @see Requirements 1.1
     */
    suspend fun buscarPorQuery(query: String): Result<List<Patrimonio>>
    
    /**
     * Busca patrimônios coletados por query
     * @see Requirements 3.1
     */
    suspend fun buscarColetadosPorQuery(query: String): Result<List<Patrimonio>>
    
    /**
     * Busca patrimônios pendentes (não coletados) por query
     * @see Requirements 3.2
     */
    suspend fun buscarPendentesPorQuery(query: String): Result<List<Patrimonio>>
    
    /**
     * Busca patrimônios com divergência por query
     * @see Requirements 3.3
     */
    suspend fun buscarDivergenciasPorQuery(query: String, inventarioId: Int): Result<List<Patrimonio>>
}

/**
 * Tipos de fonte de dados
 */
enum class DataSourceType {
    REMOTE,  // Servidor (PostgreSQL via API)
    LOCAL,   // Banco local (SQLite via Room)
    CACHE    // Cache em memória
}
