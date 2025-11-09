package com.inventario.mobile.data.cache

import com.inventario.mobile.domain.model.Sala
import android.util.Log

/**
 * Cache em memória para salas
 * Persiste durante toda a sessão do app
 */
object SalaCache {
    private const val TAG = "SalaCache"
    
    private var salas: List<Sala>? = null
    private var lastUpdateTime: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000L // 5 minutos
    
    /**
     * Verifica se o cache é válido
     */
    fun isValid(): Boolean {
        val isValid = salas != null && (System.currentTimeMillis() - lastUpdateTime) < cacheValidityMs
        Log.d(TAG, "isValid: $isValid (${salas?.size ?: 0} salas, ${(System.currentTimeMillis() - lastUpdateTime) / 1000}s atrás)")
        return isValid
    }
    
    /**
     * Obtém salas do cache
     */
    fun getSalas(): List<Sala>? {
        return if (isValid()) {
            Log.d(TAG, "getSalas: Retornando ${salas?.size ?: 0} salas do cache")
            salas
        } else {
            Log.d(TAG, "getSalas: Cache inválido ou vazio")
            null
        }
    }
    
    /**
     * Salva salas no cache
     */
    fun setSalas(newSalas: List<Sala>) {
        salas = newSalas
        lastUpdateTime = System.currentTimeMillis()
        Log.d(TAG, "setSalas: ${newSalas.size} salas salvas no cache")
    }
    
    /**
     * Adiciona mais salas ao cache (para paginação)
     */
    fun addSalas(newSalas: List<Sala>) {
        val currentSalas = salas ?: emptyList()
        salas = currentSalas + newSalas
        lastUpdateTime = System.currentTimeMillis()
        Log.d(TAG, "addSalas: ${newSalas.size} salas adicionadas (total: ${salas?.size ?: 0})")
    }
    
    /**
     * Limpa o cache
     */
    fun clear() {
        Log.d(TAG, "clear: Limpando cache de salas")
        salas = null
        lastUpdateTime = 0
    }
    
    /**
     * Obtém o tamanho do cache
     */
    fun size(): Int {
        return salas?.size ?: 0
    }
    
    /**
     * Verifica se tem mais salas para carregar
     * Baseado no último tamanho de página recebido
     */
    private var lastPageSize: Int = 0
    private var hasMorePages: Boolean = true
    
    fun setLastPageSize(size: Int, pageSize: Int) {
        lastPageSize = size
        hasMorePages = size >= pageSize
        Log.d(TAG, "setLastPageSize: $size (pageSize: $pageSize, hasMore: $hasMorePages)")
    }
    
    fun hasMorePages(): Boolean {
        return hasMorePages
    }
}
