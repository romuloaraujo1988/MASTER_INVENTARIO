package com.inventario.mobile.domain.repository

import com.inventario.mobile.data.local.entity.FotoReferenciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de Fotos de Referência
 * Define operações para sincronização e busca de fotos por descrição
 * 
 * Segue padrão offline-first:
 * - Fotos são baixadas do servidor e cacheadas localmente
 * - Busca sempre usa cache local primeiro
 * - Delta sync para atualizar apenas fotos modificadas
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
interface FotoReferenciaRepository {
    
    // ========================================
    // Sincronização
    // ========================================
    
    /**
     * Sincroniza fotos de referência do servidor
     * Usa delta sync para baixar apenas fotos novas/atualizadas
     * 
     * @return Result com número de fotos sincronizadas ou erro
     */
    suspend fun sincronizar(): Result<Int>
    
    /**
     * Força sincronização completa (ignora delta sync)
     * Útil para reset do cache ou primeira sincronização
     * 
     * @return Result com número de fotos sincronizadas ou erro
     */
    suspend fun sincronizarCompleto(): Result<Int>
    
    /**
     * Verifica se há atualizações disponíveis no servidor
     * 
     * @return true se há fotos novas/atualizadas
     */
    suspend fun verificarAtualizacoes(): Boolean
    
    // ========================================
    // Busca Local
    // ========================================
    
    /**
     * Busca foto de referência por descrição normalizada
     * Busca exata no cache local
     * 
     * @param descricao Descrição normalizada do patrimônio
     * @return FotoReferenciaEntity ou null se não encontrada
     */
    suspend fun buscarPorDescricao(descricao: String): FotoReferenciaEntity?
    
    /**
     * Busca foto de referência por descrição (busca parcial)
     * Útil para busca por texto digitado pelo usuário
     * 
     * @param query Termo de busca
     * @return Lista de fotos que contêm o termo
     */
    suspend fun buscarPorDescricaoParcial(query: String): List<FotoReferenciaEntity>
    
    /**
     * Busca fotos de referência por lista de descrições
     * Otimizado para buscar múltiplas fotos de uma vez
     * 
     * @param descricoes Lista de descrições normalizadas
     * @return Lista de fotos encontradas
     */
    suspend fun buscarPorDescricoes(descricoes: List<String>): List<FotoReferenciaEntity>
    
    /**
     * Busca foto de referência por ID
     * 
     * @param id ID da foto
     * @return FotoReferenciaEntity ou null se não encontrada
     */
    suspend fun buscarPorId(id: Int): FotoReferenciaEntity?
    
    /**
     * Busca todas as fotos de referência ativas
     * 
     * @return Lista de todas as fotos ativas
     */
    suspend fun buscarTodas(): List<FotoReferenciaEntity>
    
    /**
     * Observa todas as fotos de referência (Flow reativo)
     * Emite nova lista quando há mudanças no cache
     * 
     * @return Flow de lista de fotos
     */
    fun observarTodas(): Flow<List<FotoReferenciaEntity>>
    
    // ========================================
    // Gerenciamento de Cache
    // ========================================
    
    /**
     * Limpa fotos antigas não utilizadas
     * Remove fotos inativas há mais de X dias
     * 
     * @param diasRetencao Número de dias para manter fotos inativas
     * @return Número de fotos removidas
     */
    suspend fun limparFotosAntigas(diasRetencao: Int = 30): Int
    
    /**
     * Limpa fotos para liberar espaço
     * Remove fotos mais antigas até atingir o limite de armazenamento
     * 
     * @param limiteMB Limite de armazenamento em MB
     * @return Número de fotos removidas
     */
    suspend fun limparParaLiberarEspaco(limiteMB: Int = 100): Int
    
    /**
     * Limpa todo o cache de fotos
     * Usado para reset completo
     */
    suspend fun limparCache()
    
    // ========================================
    // Estatísticas
    // ========================================
    
    /**
     * Obtém estatísticas do cache de fotos
     * 
     * @return Estatísticas (total, ativas, tamanho, etc.)
     */
    suspend fun obterEstatisticas(): FotoReferenciaStats
    
    /**
     * Calcula tamanho total do cache em bytes
     * 
     * @return Tamanho em bytes
     */
    suspend fun calcularTamanhoCache(): Long
    
    /**
     * Conta total de fotos no cache
     * 
     * @return Número de fotos ativas
     */
    suspend fun contarFotos(): Int
}

/**
 * Data class para estatísticas do cache de fotos
 */
data class FotoReferenciaStats(
    val total: Int,
    val ativas: Int,
    val inativas: Int,
    val tamanhoTotalBytes: Long,
    val ultimaAtualizacao: Long?
) {
    /**
     * Tamanho em MB formatado
     */
    val tamanhoMB: String
        get() = String.format("%.2f MB", tamanhoTotalBytes / (1024.0 * 1024.0))
    
    /**
     * Verifica se o cache está vazio
     */
    val isEmpty: Boolean
        get() = ativas == 0
}
