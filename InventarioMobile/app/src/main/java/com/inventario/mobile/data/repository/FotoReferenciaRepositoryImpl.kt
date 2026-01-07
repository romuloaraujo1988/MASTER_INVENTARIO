package com.inventario.mobile.data.repository

import android.util.Log
import com.inventario.mobile.data.local.dao.FotoReferenciaDao
import com.inventario.mobile.data.local.entity.FotoReferenciaEntity
import com.inventario.mobile.data.remote.api.FotoReferenciaApi
import com.inventario.mobile.domain.repository.FotoReferenciaRepository
import com.inventario.mobile.domain.repository.FotoReferenciaStats
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de Fotos de Referência
 * 
 * Estratégia offline-first:
 * 1. Sincroniza fotos do servidor para cache local
 * 2. Busca sempre usa cache local
 * 3. Delta sync para otimizar transferência de dados
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Singleton
class FotoReferenciaRepositoryImpl @Inject constructor(
    private val fotoReferenciaDao: FotoReferenciaDao,
    private val fotoReferenciaApi: FotoReferenciaApi,
    private val preferencesManager: PreferencesManager
) : FotoReferenciaRepository {
    
    companion object {
        private const val TAG = "FotoReferenciaRepo"
        private const val PAGE_SIZE = 20
        private const val MAX_CACHE_SIZE_MB = 100
        private const val DIAS_RETENCAO_INATIVAS = 30
    }
    
    // ========================================
    // Sincronização
    // ========================================
    
    override suspend fun sincronizar(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronização de fotos de referência...")
            
            val ultimaSync = preferencesManager.getLastFotoReferenciaSyncTimestamp()
            Log.d(TAG, "Última sincronização: $ultimaSync")
            
            var totalSincronizadas = 0
            var pagina = 0
            var temMaisPaginas = true
            
            while (temMaisPaginas) {
                Log.d(TAG, "Buscando página $pagina...")
                
                val response = fotoReferenciaApi.buscarFotos(
                    ultimaAtualizacao = if (ultimaSync > 0) ultimaSync else null,
                    pagina = pagina,
                    tamanho = PAGE_SIZE,
                    incluirImagem = true
                )
                
                if (response.success && response.data != null) {
                    val pagedResponse = response.data
                    val fotos = pagedResponse.content
                    
                    if (fotos.isNotEmpty()) {
                        // Converter DTOs para Entities e salvar
                        val entities = fotos.map { it.toEntity() }
                        fotoReferenciaDao.inserirTodas(entities)
                        totalSincronizadas += entities.size
                        
                        Log.d(TAG, "Página $pagina: ${entities.size} fotos sincronizadas")
                        
                        // Processar fotos inativas (soft delete no servidor)
                        val inativas = entities.filter { !it.ativo }
                        if (inativas.isNotEmpty()) {
                            Log.d(TAG, "${inativas.size} fotos marcadas como inativas")
                        }
                    }
                    
                    // Verificar se há mais páginas
                    temMaisPaginas = !pagedResponse.last
                    pagina++
                } else {
                    Log.e(TAG, "Erro na resposta: ${response.message}")
                    temMaisPaginas = false
                }
            }
            
            // Atualizar timestamp de sincronização
            if (totalSincronizadas > 0) {
                preferencesManager.setLastFotoReferenciaSyncTimestamp(System.currentTimeMillis())
            }
            
            Log.d(TAG, "✓ Sincronização concluída: $totalSincronizadas fotos")
            Result.success(totalSincronizadas)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização de fotos", e)
            Result.failure(e)
        }
    }
    
    override suspend fun sincronizarCompleto(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronização COMPLETA de fotos...")
            
            // Limpar cache existente
            fotoReferenciaDao.limparTodas()
            
            // Resetar timestamp para forçar sync completo
            preferencesManager.setLastFotoReferenciaSyncTimestamp(0L)
            
            // Executar sincronização normal (sem delta)
            sincronizar()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização completa", e)
            Result.failure(e)
        }
    }
    
    override suspend fun verificarAtualizacoes(): Boolean = withContext(Dispatchers.IO) {
        try {
            val ultimaSync = preferencesManager.getLastFotoReferenciaSyncTimestamp()
            
            if (ultimaSync == 0L) {
                // Nunca sincronizou, há atualizações
                return@withContext true
            }
            
            val response = fotoReferenciaApi.verificarAtualizacoes(
                ultimaAtualizacao = ultimaSync,
                pagina = 0,
                tamanho = 1,
                incluirImagem = false
            )
            
            if (response.success && response.data != null) {
                val totalElements = response.data.totalElements
                Log.d(TAG, "Atualizações disponíveis: $totalElements")
                return@withContext totalElements > 0
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar atualizações", e)
            false
        }
    }
    
    // ========================================
    // Busca Local
    // ========================================
    
    override suspend fun buscarPorDescricao(descricao: String): FotoReferenciaEntity? {
        return withContext(Dispatchers.IO) {
            val normalizada = normalizarDescricao(descricao)
            fotoReferenciaDao.buscarPorDescricao(normalizada)
        }
    }
    
    override suspend fun buscarPorDescricaoParcial(query: String): List<FotoReferenciaEntity> {
        return withContext(Dispatchers.IO) {
            val queryLike = "%${query.trim()}%"
            fotoReferenciaDao.buscarPorDescricaoParcial(queryLike)
        }
    }
    
    override suspend fun buscarPorDescricoes(descricoes: List<String>): List<FotoReferenciaEntity> {
        return withContext(Dispatchers.IO) {
            val normalizadas = descricoes.map { normalizarDescricao(it) }
            fotoReferenciaDao.buscarPorDescricoes(normalizadas)
        }
    }
    
    override suspend fun buscarPorId(id: Int): FotoReferenciaEntity? {
        return withContext(Dispatchers.IO) {
            fotoReferenciaDao.buscarPorId(id)
        }
    }
    
    override suspend fun buscarTodas(): List<FotoReferenciaEntity> {
        return withContext(Dispatchers.IO) {
            fotoReferenciaDao.buscarTodas()
        }
    }
    
    override fun observarTodas(): Flow<List<FotoReferenciaEntity>> {
        return fotoReferenciaDao.observarTodas()
    }
    
    // ========================================
    // Gerenciamento de Cache
    // ========================================
    
    override suspend fun limparFotosAntigas(diasRetencao: Int): Int = withContext(Dispatchers.IO) {
        try {
            val timestampLimite = System.currentTimeMillis() - (diasRetencao * 24 * 60 * 60 * 1000L)
            
            // Contar antes de limpar
            val inativasAntes = fotoReferenciaDao.contarInativas()
            
            // Limpar fotos inativas antigas
            fotoReferenciaDao.limparInativasAntigas(timestampLimite)
            
            // Contar depois
            val inativasDepois = fotoReferenciaDao.contarInativas()
            val removidas = inativasAntes - inativasDepois
            
            Log.d(TAG, "Limpeza de fotos antigas: $removidas removidas")
            removidas
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar fotos antigas", e)
            0
        }
    }
    
    override suspend fun limparParaLiberarEspaco(limiteMB: Int): Int = withContext(Dispatchers.IO) {
        try {
            val limiteBytes = limiteMB * 1024L * 1024L
            var tamanhoAtual = fotoReferenciaDao.calcularTamanhoTotal()
            var removidas = 0
            
            Log.d(TAG, "Tamanho atual do cache: ${tamanhoAtual / (1024 * 1024)} MB, limite: $limiteMB MB")
            
            while (tamanhoAtual > limiteBytes) {
                // Buscar fotos mais antigas
                val fotosAntigas = fotoReferenciaDao.buscarFotosMaisAntigas(10)
                
                if (fotosAntigas.isEmpty()) break
                
                // Desativar fotos mais antigas
                val ids = fotosAntigas.map { it.id }
                fotoReferenciaDao.desativarPorIds(ids)
                removidas += ids.size
                
                // Recalcular tamanho
                tamanhoAtual = fotoReferenciaDao.calcularTamanhoTotal()
            }
            
            Log.d(TAG, "Liberação de espaço: $removidas fotos desativadas")
            removidas
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao liberar espaço", e)
            0
        }
    }
    
    override suspend fun limparCache() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Limpando todo o cache de fotos...")
            fotoReferenciaDao.limparTodas()
            preferencesManager.setLastFotoReferenciaSyncTimestamp(0L)
            Log.d(TAG, "✓ Cache limpo")
        }
    }
    
    // ========================================
    // Estatísticas
    // ========================================
    
    override suspend fun obterEstatisticas(): FotoReferenciaStats = withContext(Dispatchers.IO) {
        try {
            val stats = fotoReferenciaDao.buscarEstatisticas()
            FotoReferenciaStats(
                total = stats.total,
                ativas = stats.ativas,
                inativas = stats.inativas,
                tamanhoTotalBytes = stats.tamanhoTotalBytes,
                ultimaAtualizacao = stats.ultimaAtualizacao
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter estatísticas", e)
            FotoReferenciaStats(0, 0, 0, 0L, null)
        }
    }
    
    override suspend fun calcularTamanhoCache(): Long = withContext(Dispatchers.IO) {
        fotoReferenciaDao.calcularTamanhoTotal()
    }
    
    override suspend fun contarFotos(): Int = withContext(Dispatchers.IO) {
        fotoReferenciaDao.contar()
    }
    
    // ========================================
    // Utilitários
    // ========================================
    
    /**
     * Normaliza descrição para busca
     * Remove padrões conhecidos (patrimônio ANAC, números de série, etc.)
     */
    private fun normalizarDescricao(descricao: String): String {
        var normalizada = descricao.uppercase().trim()
        
        // Remover padrão "PATRIMÔNIO ANAC XXXXXXX" ou "- PATRIMÔNIO ANAC XXXXXXX"
        normalizada = normalizada.replace(Regex("-?\\s*PATRIMÔNIO\\s+ANAC\\s+\\d+"), "")
        
        // Remover números de série após "N/S:" ou "SÉRIE:"
        normalizada = normalizada.replace(Regex("N/S:\\s*\\S+"), "")
        normalizada = normalizada.replace(Regex("SÉRIE:\\s*\\S+"), "")
        
        // Normalizar espaços múltiplos
        normalizada = normalizada.replace(Regex("\\s+"), " ").trim()
        
        return normalizada
    }
}
