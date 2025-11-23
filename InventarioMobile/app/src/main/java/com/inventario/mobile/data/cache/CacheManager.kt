package com.inventario.mobile.data.cache

import android.content.Context
import android.util.Log
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de Cache Inteligente
 * 
 * Funcionalidades:
 * - Armazena timestamp de última sincronização
 * - Controla validade do cache
 * - Sincronização incremental (apenas dados novos)
 * - Invalidação de cache quando necessário
 * 
 * Estratégia:
 * 1. Primeira vez: Carrega tudo
 * 2. Próximas vezes: Apenas dados modificados após último timestamp
 * 3. Cache expira após X horas (configurável)
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "CacheManager"
        
        // Chaves de preferências
        private const val KEY_LAST_SYNC_PATRIMONIOS = "last_sync_patrimonios"
        private const val KEY_LAST_SYNC_SALAS = "last_sync_salas"
        private const val KEY_LAST_SYNC_RESPONSAVEIS = "last_sync_responsaveis"
        private const val KEY_LAST_SYNC_COLETAS = "last_sync_coletas"
        
        // Tempo de validade do cache (em milissegundos)
        private const val CACHE_VALIDITY_HOURS = 24L // 24 horas
        private const val CACHE_VALIDITY_MS = CACHE_VALIDITY_HOURS * 60 * 60 * 1000
    }
    
    /**
     * Obtém timestamp da última sincronização de patrimônios
     */
    fun getLastSyncPatrimonios(): Long {
        return preferencesManager.getLong(KEY_LAST_SYNC_PATRIMONIOS, 0L)
    }
    
    /**
     * Salva timestamp da última sincronização de patrimônios
     */
    fun setLastSyncPatrimonios(timestamp: Long = System.currentTimeMillis()) {
        preferencesManager.putLong(KEY_LAST_SYNC_PATRIMONIOS, timestamp)
        Log.d(TAG, "✓ Timestamp de patrimônios atualizado: $timestamp")
    }
    
    /**
     * Obtém timestamp da última sincronização de salas
     */
    fun getLastSyncSalas(): Long {
        return preferencesManager.getLong(KEY_LAST_SYNC_SALAS, 0L)
    }
    
    /**
     * Salva timestamp da última sincronização de salas
     */
    fun setLastSyncSalas(timestamp: Long = System.currentTimeMillis()) {
        preferencesManager.putLong(KEY_LAST_SYNC_SALAS, timestamp)
        Log.d(TAG, "✓ Timestamp de salas atualizado: $timestamp")
    }
    
    /**
     * Obtém timestamp da última sincronização de responsáveis
     */
    fun getLastSyncResponsaveis(): Long {
        return preferencesManager.getLong(KEY_LAST_SYNC_RESPONSAVEIS, 0L)
    }
    
    /**
     * Salva timestamp da última sincronização de responsáveis
     */
    fun setLastSyncResponsaveis(timestamp: Long = System.currentTimeMillis()) {
        preferencesManager.putLong(KEY_LAST_SYNC_RESPONSAVEIS, timestamp)
        Log.d(TAG, "✓ Timestamp de responsáveis atualizado: $timestamp")
    }
    
    /**
     * Obtém timestamp da última sincronização de coletas
     */
    fun getLastSyncColetas(): Long {
        return preferencesManager.getLong(KEY_LAST_SYNC_COLETAS, 0L)
    }
    
    /**
     * Salva timestamp da última sincronização de coletas
     */
    fun setLastSyncColetas(timestamp: Long = System.currentTimeMillis()) {
        preferencesManager.putLong(KEY_LAST_SYNC_COLETAS, timestamp)
        Log.d(TAG, "✓ Timestamp de coletas atualizado: $timestamp")
    }
    
    /**
     * Verifica se o cache de patrimônios está válido
     */
    fun isCacheValidPatrimonios(): Boolean {
        val lastSync = getLastSyncPatrimonios()
        if (lastSync == 0L) return false
        
        val now = System.currentTimeMillis()
        val elapsed = now - lastSync
        val isValid = elapsed < CACHE_VALIDITY_MS
        
        Log.d(TAG, "Cache patrimônios: ${if (isValid) "VÁLIDO" else "EXPIRADO"} (${elapsed / 1000 / 60} min)")
        return isValid
    }
    
    /**
     * Verifica se o cache de salas está válido
     */
    fun isCacheValidSalas(): Boolean {
        val lastSync = getLastSyncSalas()
        if (lastSync == 0L) return false
        
        val now = System.currentTimeMillis()
        val elapsed = now - lastSync
        val isValid = elapsed < CACHE_VALIDITY_MS
        
        Log.d(TAG, "Cache salas: ${if (isValid) "VÁLIDO" else "EXPIRADO"} (${elapsed / 1000 / 60} min)")
        return isValid
    }
    
    /**
     * Verifica se o cache de responsáveis está válido
     */
    fun isCacheValidResponsaveis(): Boolean {
        val lastSync = getLastSyncResponsaveis()
        if (lastSync == 0L) return false
        
        val now = System.currentTimeMillis()
        val elapsed = now - lastSync
        val isValid = elapsed < CACHE_VALIDITY_MS
        
        Log.d(TAG, "Cache responsáveis: ${if (isValid) "VÁLIDO" else "EXPIRADO"} (${elapsed / 1000 / 60} min)")
        return isValid
    }
    
    /**
     * Verifica se o cache de coletas está válido
     */
    fun isCacheValidColetas(): Boolean {
        val lastSync = getLastSyncColetas()
        if (lastSync == 0L) return false
        
        val now = System.currentTimeMillis()
        val elapsed = now - lastSync
        val isValid = elapsed < CACHE_VALIDITY_MS
        
        Log.d(TAG, "Cache coletas: ${if (isValid) "VÁLIDO" else "EXPIRADO"} (${elapsed / 1000 / 60} min)")
        return isValid
    }
    
    /**
     * Invalida todo o cache (forçar sincronização completa)
     */
    fun invalidateAll() {
        preferencesManager.putLong(KEY_LAST_SYNC_PATRIMONIOS, 0L)
        preferencesManager.putLong(KEY_LAST_SYNC_SALAS, 0L)
        preferencesManager.putLong(KEY_LAST_SYNC_RESPONSAVEIS, 0L)
        preferencesManager.putLong(KEY_LAST_SYNC_COLETAS, 0L)
        Log.d(TAG, "⚠️ Todo o cache foi invalidado")
    }
    
    /**
     * Invalida cache de patrimônios
     */
    fun invalidatePatrimonios() {
        preferencesManager.putLong(KEY_LAST_SYNC_PATRIMONIOS, 0L)
        Log.d(TAG, "⚠️ Cache de patrimônios invalidado")
    }
    
    /**
     * Invalida cache de salas
     */
    fun invalidateSalas() {
        preferencesManager.putLong(KEY_LAST_SYNC_SALAS, 0L)
        Log.d(TAG, "⚠️ Cache de salas invalidado")
    }
    
    /**
     * Invalida cache de responsáveis
     */
    fun invalidateResponsaveis() {
        preferencesManager.putLong(KEY_LAST_SYNC_RESPONSAVEIS, 0L)
        Log.d(TAG, "⚠️ Cache de responsáveis invalidado")
    }
    
    /**
     * Invalida cache de coletas
     */
    fun invalidateColetas() {
        preferencesManager.putLong(KEY_LAST_SYNC_COLETAS, 0L)
        Log.d(TAG, "⚠️ Cache de coletas invalidado")
    }
    
    /**
     * Obtém estatísticas do cache
     */
    fun getCacheStats(): CacheStats {
        val now = System.currentTimeMillis()
        
        return CacheStats(
            patrimoniosLastSync = getLastSyncPatrimonios(),
            patrimoniosValid = isCacheValidPatrimonios(),
            patrimoniosAge = if (getLastSyncPatrimonios() > 0) now - getLastSyncPatrimonios() else 0,
            
            salasLastSync = getLastSyncSalas(),
            salasValid = isCacheValidSalas(),
            salasAge = if (getLastSyncSalas() > 0) now - getLastSyncSalas() else 0,
            
            responsaveisLastSync = getLastSyncResponsaveis(),
            responsaveisValid = isCacheValidResponsaveis(),
            responsaveisAge = if (getLastSyncResponsaveis() > 0) now - getLastSyncResponsaveis() else 0,
            
            coletasLastSync = getLastSyncColetas(),
            coletasValid = isCacheValidColetas(),
            coletasAge = if (getLastSyncColetas() > 0) now - getLastSyncColetas() else 0
        )
    }
}

/**
 * Estatísticas do cache
 */
data class CacheStats(
    val patrimoniosLastSync: Long,
    val patrimoniosValid: Boolean,
    val patrimoniosAge: Long,
    
    val salasLastSync: Long,
    val salasValid: Boolean,
    val salasAge: Long,
    
    val responsaveisLastSync: Long,
    val responsaveisValid: Boolean,
    val responsaveisAge: Long,
    
    val coletasLastSync: Long,
    val coletasValid: Boolean,
    val coletasAge: Long
) {
    fun getAgeInMinutes(ageMs: Long): Long = ageMs / 1000 / 60
    fun getAgeInHours(ageMs: Long): Long = ageMs / 1000 / 60 / 60
}

