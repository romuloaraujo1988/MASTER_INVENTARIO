package com.inventario.mobile.data.cache

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de cache local para otimizar requisições ao servidor
 * Implementa estratégia de cache com expiração
 */
class CacheManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CacheManager"
        private const val CACHE_DIR = "api_cache"
        
        // Tempos de expiração padrão
        private val DEFAULT_EXPIRATION = TimeUnit.MINUTES.toMillis(5) // 5 minutos
        private val PATRIMONIO_EXPIRATION = TimeUnit.MINUTES.toMillis(10) // 10 minutos
        private val COLETAS_EXPIRATION = TimeUnit.MINUTES.toMillis(2) // 2 minutos
        private val DASHBOARD_EXPIRATION = TimeUnit.SECONDS.toMillis(30) // 30 segundos
        
        @Volatile
        private var instance: CacheManager? = null
        
        fun getInstance(context: Context): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val gson = Gson()
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Entrada de cache com timestamp
     */
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val expirationTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > timestamp + expirationTime
    }
    
    /**
     * Salva dados no cache
     */
    fun <T> put(key: String, data: T, expirationMs: Long = DEFAULT_EXPIRATION) {
        try {
            val entry = CacheEntry(
                data = data,
                timestamp = System.currentTimeMillis(),
                expirationTime = expirationMs
            )
            
            val json = gson.toJson(entry)
            val file = File(cacheDir, key.hashCode().toString())
            file.writeText(json)
            
            Log.d(TAG, "Cache salvo: $key (expira em ${expirationMs}ms)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar cache: $key", e)
        }
    }
    
    /**
     * Recupera dados do cache
     */
    fun <T> get(key: String, type: TypeToken<CacheEntry<T>>): T? {
        try {
            val file = File(cacheDir, key.hashCode().toString())
            if (!file.exists()) {
                Log.d(TAG, "Cache não encontrado: $key")
                return null
            }
            
            val json = file.readText()
            val entry: CacheEntry<T> = gson.fromJson(json, type.type)
            
            if (entry.isExpired()) {
                Log.d(TAG, "Cache expirado: $key")
                file.delete()
                return null
            }
            
            Log.d(TAG, "Cache recuperado: $key")
            return entry.data
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recuperar cache: $key", e)
            return null
        }
    }
    
    /**
     * Remove entrada específica do cache
     */
    fun remove(key: String) {
        try {
            val file = File(cacheDir, key.hashCode().toString())
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Cache removido: $key")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao remover cache: $key", e)
        }
    }
    
    /**
     * Limpa todo o cache
     */
    fun clearAll() {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "Todo cache limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar cache", e)
        }
    }
    
    /**
     * Remove entradas expiradas
     */
    fun cleanExpired() {
        try {
            var count = 0
            cacheDir.listFiles()?.forEach { file ->
                try {
                    val json = file.readText()
                    val type = object : TypeToken<CacheEntry<Any>>() {}.type
                    val entry: CacheEntry<Any> = gson.fromJson(json, type)
                    
                    if (entry.isExpired()) {
                        file.delete()
                        count++
                    }
                } catch (e: Exception) {
                    // Se não conseguir ler, deletar
                    file.delete()
                    count++
                }
            }
            Log.d(TAG, "Limpeza: $count entradas expiradas removidas")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar cache expirado", e)
        }
    }
    
    /**
     * Retorna tamanho do cache em bytes
     */
    fun getCacheSize(): Long {
        return cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    /**
     * Retorna tamanho do cache formatado
     */
    fun getCacheSizeFormatted(): String {
        val bytes = getCacheSize()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
    
    // ===== Métodos de conveniência para tipos específicos =====
    
    fun putPatrimonios(data: Any) = put("patrimonios", data, PATRIMONIO_EXPIRATION)
    inline fun <reified T> getPatrimonios(): T? {
        val type = object : TypeToken<CacheEntry<T>>() {}
        return get("patrimonios", type)
    }
    
    fun putColetas(page: Int, data: Any) = put("coletas_page_$page", data, COLETAS_EXPIRATION)
    inline fun <reified T> getColetas(page: Int): T? {
        val type = object : TypeToken<CacheEntry<T>>() {}
        return get("coletas_page_$page", type)
    }
    
    fun putDashboard(data: Any) = put("dashboard_stats", data, DASHBOARD_EXPIRATION)
    inline fun <reified T> getDashboard(): T? {
        val type = object : TypeToken<CacheEntry<T>>() {}
        return get("dashboard_stats", type)
    }
    
    fun invalidateColetas() {
        // Remove todas as páginas de coletas
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.contains("coletas_page")) {
                file.delete()
            }
        }
        Log.d(TAG, "Cache de coletas invalidado")
    }
}
