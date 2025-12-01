package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Patrimonio
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Patrimônio
 */
interface PatrimonioRepository {

    // Operações de consulta
    fun getAllPatrimonios(): Flow<List<Patrimonio>>
    suspend fun getPatrimonioById(id: Long): Patrimonio?
    suspend fun getPatrimonioByNumero(numeroPatrimonio: String): Patrimonio?
    suspend fun getPatrimonioByQrCode(qrCode: String): Patrimonio?
    suspend fun getPatrimoniosBySetor(setorId: Long): List<Patrimonio>
    suspend fun getPatrimoniosBySala(salaId: Long): List<Patrimonio>
    suspend fun getPatrimoniosNaoSincronizados(): List<Patrimonio>
    suspend fun searchPatrimonios(query: String): List<Patrimonio>

    // Operações de inserção
    suspend fun insertPatrimonio(patrimonio: Patrimonio): Long
    suspend fun insertPatrimonios(patrimonios: List<Patrimonio>)

    // Operações de atualização
    suspend fun updatePatrimonio(patrimonio: Patrimonio)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deletePatrimonio(patrimonio: Patrimonio)
    suspend fun deletePatrimonioById(id: Long)

    // Operações de contagem
    suspend fun getPatrimonioCount(): Int
    suspend fun getPatrimoniosNaoSincronizadosCount(): Int

    // Sincronização
    suspend fun sincronizarPatrimonios(): Result<Unit>
    suspend fun enviarPatrimoniosParaServidor(): Result<Unit>
    
    // Novos métodos para Clean Architecture
    suspend fun buscarPorNumero(numero: String): Patrimonio?
    suspend fun buscarDescricoesNaoColetadas(): List<String>
    suspend fun buscarPorDescricaoNaoColetados(descricao: String): List<Patrimonio>
    
    // ========================================
    // Métodos para Inventário por Sala
    // ========================================
    
    /**
     * Busca patrimônios por sala com filtro opcional de status de coleta e paginação.
     * 
     * @param salaId ID da sala
     * @param coletado Filtro de status: null = todos, true = coletados, false = não coletados
     * @param page Número da página (0-indexed)
     * @param pageSize Quantidade de itens por página
     * @return Result com lista de patrimônios ou erro
     */
    suspend fun buscarPorSala(
        salaId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<List<Patrimonio>>
    
    /**
     * Conta total de patrimônios em uma sala.
     * 
     * @param salaId ID da sala
     * @return Result com total de patrimônios ou erro
     */
    suspend fun contarPorSala(salaId: Int): Result<Int>
    
    /**
     * Conta patrimônios coletados em uma sala.
     * 
     * @param salaId ID da sala
     * @return Result com total de patrimônios coletados ou erro
     */
    suspend fun contarColetadosPorSala(salaId: Int): Result<Int>
}