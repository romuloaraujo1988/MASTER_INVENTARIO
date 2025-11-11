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
     * Retorna o tipo de fonte de dados
     */
    fun getSourceType(): DataSourceType
}

/**
 * Tipos de fonte de dados
 */
enum class DataSourceType {
    REMOTE,  // Servidor (PostgreSQL via API)
    LOCAL,   // Banco local (SQLite via Room)
    CACHE    // Cache em memória
}
