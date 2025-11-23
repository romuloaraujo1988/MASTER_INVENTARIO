package com.inventario.mobile.data.cache

import android.util.Log
import com.inventario.mobile.domain.model.DashboardStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache inteligente e reativo para estatísticas do Dashboard
 * 
 * Funcionalidades:
 * - Cache em memória com TTL (Time To Live)
 * - Reativo: notifica observers quando dados mudam
 * - Invalidação automática após mudanças
 * - Thread-safe
 * 
 * Uso:
 * ```kotlin
 * // Observar mudanças
 * cache.stats.collect { stats ->
 *     updateUI(stats)
 * }
 * 
 * // Atualizar cache
 * cache.update(newStats)
 * 
 * // Invalidar cache
 * cache.invalidate()
 * ```
 */
@Singleton
class DashboardStatsCache @Inject constructor() {
    
    companion object {
        private const val TAG = "DashboardStatsCache"
        private const val CACHE_TTL_MS = 30_000L // 30 segundos
    }
    
    // Estado reativo das estatísticas
    private val _stats = MutableStateFlow<CachedStats?>(null)
    val stats: StateFlow<CachedStats?> = _stats.asStateFlow()
    
    /**
     * Verifica se o cache é válido
     */
    fun isValid(): Boolean {
        val cached = _stats.value
        if (cached == null) {
            Log.d(TAG, "Cache vazio")
            return false
        }
        
        val age = System.currentTimeMillis() - cached.timestamp
        val isValid = age < CACHE_TTL_MS
        
        Log.d(TAG, "Cache idade: ${age}ms, válido: $isValid (TTL: ${CACHE_TTL_MS}ms)")
        return isValid
    }
    
    /**
     * Obtém estatísticas do cache (se válido)
     */
    fun get(): DashboardStats? {
        return if (isValid()) {
            Log.d(TAG, "✓ Retornando estatísticas do cache")
            _stats.value?.stats
        } else {
            Log.d(TAG, "✗ Cache inválido ou expirado")
            null
        }
    }
    
    /**
     * Atualiza o cache com novas estatísticas
     * Notifica todos os observers
     */
    fun update(stats: DashboardStats) {
        val cached = CachedStats(
            stats = stats,
            timestamp = System.currentTimeMillis()
        )
        
        _stats.value = cached
        Log.d(TAG, "✓ Cache atualizado - Coletados: ${stats.totalColetados}, Pendentes: ${stats.totalPendentes}")
    }
    
    /**
     * Invalida o cache
     * Força recarregamento na próxima consulta
     */
    fun invalidate() {
        _stats.value = null
        Log.d(TAG, "⚠️ Cache invalidado")
    }
    
    /**
     * Invalida o cache se uma coleta foi registrada
     * Chamado após salvar coleta localmente
     */
    fun invalidateOnColetaRegistrada() {
        Log.d(TAG, "🔄 Coleta registrada - invalidando cache")
        invalidate()
    }
    
    /**
     * Invalida o cache se dados foram sincronizados
     * Chamado após sincronização bem-sucedida
     */
    fun invalidateOnSyncCompleted() {
        Log.d(TAG, "🔄 Sincronização concluída - invalidando cache")
        invalidate()
    }
    
    /**
     * Atualiza estatísticas incrementalmente após coleta
     * Mais eficiente que recarregar tudo
     */
    fun incrementColetados() {
        val current = _stats.value?.stats
        if (current != null) {
            val updated = current.copy(
                totalColetados = current.totalColetados + 1,
                totalPendentes = maxOf(0, current.totalPendentes - 1),
                percentualConclusao = calcularPercentual(
                    current.totalColetados + 1,
                    current.totalPatrimonios
                )
            )
            update(updated)
            Log.d(TAG, "✓ Cache atualizado incrementalmente: +1 coletado")
        } else {
            Log.d(TAG, "⚠️ Cache vazio, não pode atualizar incrementalmente")
        }
    }
    
    /**
     * Calcula percentual de conclusão
     */
    private fun calcularPercentual(coletados: Int, total: Int): Double {
        return if (total > 0) {
            (coletados.toDouble() / total.toDouble()) * 100.0
        } else {
            0.0
        }
    }
    
    /**
     * Obtém idade do cache em milissegundos
     */
    fun getCacheAge(): Long {
        val cached = _stats.value
        return if (cached != null) {
            System.currentTimeMillis() - cached.timestamp
        } else {
            -1L
        }
    }
    
    /**
     * Verifica se o cache está próximo de expirar
     * Útil para refresh preventivo
     */
    fun isNearExpiration(): Boolean {
        val age = getCacheAge()
        if (age < 0) return false
        
        val remainingTime = CACHE_TTL_MS - age
        return remainingTime < 5000L // Menos de 5 segundos para expirar
    }
}

/**
 * Wrapper para estatísticas em cache com timestamp
 */
data class CachedStats(
    val stats: DashboardStats,
    val timestamp: Long
)
