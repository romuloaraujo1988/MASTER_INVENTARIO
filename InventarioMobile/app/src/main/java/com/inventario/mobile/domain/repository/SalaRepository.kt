package com.inventario.mobile.domain.repository

import androidx.paging.PagingData
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.model.SalaComProgresso
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Sala
 */
interface SalaRepository {
    
    /**
     * Obtém salas paginadas com busca opcional
     * 
     * @param query Termo de busca (vazio para listar todas)
     * @return Flow de PagingData com salas
     */
    fun getSalasPaginadas(query: String): Flow<PagingData<Sala>>
    
    /**
     * Busca salas no banco local
     * 
     * @param query Termo de busca
     * @return Lista de salas do banco local
     */
    suspend fun buscarSalasLocal(query: String): List<Sala>
    
    /**
     * Conta total de salas
     */
    suspend fun contarSalas(): Int
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    /**
     * Busca todas as salas com informações de progresso de coleta.
     * 
     * @return Result com lista de salas com progresso ou erro
     */
    suspend fun buscarComProgresso(): Result<List<SalaComProgresso>>
    
    /**
     * Busca salas por nome ou número (case-insensitive).
     * 
     * @param query Termo de busca
     * @return Result com lista de salas que correspondem à busca ou erro
     */
    suspend fun buscarPorNomeOuNumero(query: String): Result<List<Sala>>
}
