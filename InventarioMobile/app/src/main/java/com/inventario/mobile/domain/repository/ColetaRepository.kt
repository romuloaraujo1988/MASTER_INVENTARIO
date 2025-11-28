package com.inventario.mobile.domain.repository

import com.inventario.mobile.domain.model.Coleta
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Coleta
 */
interface ColetaRepository {

    // Operações de consulta
    fun getAllColetas(): Flow<List<Coleta>>
    suspend fun getColetaById(id: Long): Coleta?
    suspend fun getColetasByPatrimonio(patrimonioId: Long): List<Coleta>
    suspend fun getColetasByUsuario(usuarioId: Long): List<Coleta>
    suspend fun getColetasNaoSincronizadas(): List<Coleta>

    // Operações de inserção
    suspend fun insertColeta(coleta: Coleta): Long
    suspend fun insertColetas(coletas: List<Coleta>)

    // Operações de atualização
    suspend fun updateColeta(coleta: Coleta)
    suspend fun marcarComoSincronizado(id: Long, servidorId: Long)

    // Operações de exclusão
    suspend fun deleteColeta(coleta: Coleta)

    // Operações de contagem
    suspend fun getColetasNaoSincronizadasCount(): Int

    // Sincronização
    suspend fun sincronizarColetas(): Result<Unit>
    suspend fun enviarColetasParaServidor(): Result<Unit>
    
    // Novos métodos para Clean Architecture
    suspend fun registrarColeta(coleta: Coleta): Result<Coleta>
    suspend fun jaFoiColetado(idPatrimonio: Int): Boolean
    suspend fun sincronizarColetasPendentes(): Int
    suspend fun getColetasLocal(): List<Coleta>
    
    /**
     * v2.7: Registra coleta de item sem etiqueta
     */
    suspend fun registrarColetaSemEtiqueta(coleta: Coleta): Result<Coleta>
    
    /**
     * Sincroniza uma coleta específica (reenvio)
     */
    suspend fun sincronizarColetaEspecifica(coletaId: Long): Boolean
    
    // ========================================
    // Diagnóstico de Coletas Pendentes (v2.6)
    // ========================================
    
    /**
     * Busca coletas pendentes que têm erro de sincronização
     * Útil para diagnóstico de coletas "presas"
     */
    suspend fun getColetasPendentesComErro(): List<Coleta>
    
    /**
     * Busca coletas pendentes que nunca tentaram sincronizar
     */
    suspend fun getColetasPendentesSemErro(): List<Coleta>
    
    /**
     * Limpa erro de uma coleta específica para permitir nova tentativa
     */
    suspend fun limparErroColeta(coletaId: Long)
    
    /**
     * Limpa erros de todas as coletas pendentes (reset geral)
     * @return quantidade de coletas resetadas
     */
    suspend fun limparTodosErrosColetas(): Int
    
    /**
     * Remove uma coleta pendente que não pode ser sincronizada
     */
    suspend fun removerColetaPendente(coletaId: Long)
}