package com.inventario.mobile.data.cache

import android.util.Log
import com.inventario.mobile.domain.model.PatrimonioComColeta
import java.util.concurrent.TimeUnit

/**
 * Cache LRU para resultados de busca rápida
 * 
 * Estratégia: Manter os 50 últimos resultados em memória
 * Benefício: Reduz queries repetidas em 100-1000x
 * 
 * @see OTIMIZACAO_BUSCA_RAPIDA_ANDROID.md
 */
class SearchCache(private val maxSize: Int = 50) {
    
    companion object {
        private const val TAG = "SearchCache"
        private const val CACHE_EXPIRY_MINUTES = 30L
    }
    
    private data class CacheEntry(
        val data: List<PatrimonioComColeta>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean {
            val ageMinutes = TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - timestamp
            )
            return ageMinutes > CACHE_EXPIRY_MINUTES
        }
    }
    
    // LinkedHashMap LRU usando java.util.LinkedHashMap diretamente
    private val cache: java.util.LinkedHashMap<String, CacheEntry> = object : java.util.LinkedHashMap<String, CacheEntry>(
        maxSize, 0.75f, true // accessOrder = true para LRU
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
            return size > maxSize
        }
    }
    
    /**
     * Obtém resultado do cache
     * 
     * @param key Chave do cache (query:filtro)
     * @return Resultado em cache ou null se não encontrado/expirado
     */
    fun get(key: String): List<PatrimonioComColeta>? {
        val entry = cache[key]
        
        return when {
            entry == null -> {
                Log.d(TAG, "❌ Cache miss: $key")
                null
            }
            entry.isExpired() -> {
                Log.d(TAG, "⏰ Cache expirado: $key")
                cache.remove(key)
                null
            }
            else -> {
                Log.d(TAG, "✓ Cache hit: $key (${entry.data.size} resultados)")
                entry.data
            }
        }
    }
    
    /**
     * Armazena resultado no cache
     * 
     * @param key Chave do cache (query:filtro)
     * @param value Resultados a cachear
     */
    fun put(key: String, value: List<PatrimonioComColeta>) {
        cache[key] = CacheEntry(value)
        Log.d(TAG, "✓ Cacheado: $key (${value.size} resultados, tamanho cache: ${cache.size}/$maxSize)")
    }
    
    /**
     * Limpa todo o cache
     */
    fun clear() {
        val sizeAntes = cache.size
        cache.clear()
        Log.d(TAG, "🗑️ Cache limpo ($sizeAntes entradas removidas)")
    }
    
    /**
     * Remove entrada específica do cache
     */
    fun remove(key: String) {
        cache.remove(key)
        Log.d(TAG, "🗑️ Entrada removida: $key")
    }
    
    /**
     * Retorna tamanho atual do cache
     */
    fun size(): Int = cache.size
    
    /**
     * Retorna informações do cache para debug
     */
    fun getStats(): String {
        val entries = cache.entries.joinToString("\n") { (key, entry) ->
            val ageMinutes = TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - entry.timestamp
            )
            "  • $key: ${entry.data.size} resultados (${ageMinutes}min atrás)"
        }
        
        return """
            📊 Cache Stats:
            Tamanho: ${cache.size}/$maxSize
            Entradas:
            $entries
        """.trimIndent()
    }
    
    /**
     * Limpa entradas expiradas
     */
    fun cleanExpired() {
        val keysExpiradas = cache.filter { (_, entry) -> entry.isExpired() }.keys
        keysExpiradas.forEach { cache.remove(it) }
        
        if (keysExpiradas.isNotEmpty()) {
            Log.d(TAG, "🧹 Limpeza: ${keysExpiradas.size} entradas expiradas removidas")
        }
    }
}
