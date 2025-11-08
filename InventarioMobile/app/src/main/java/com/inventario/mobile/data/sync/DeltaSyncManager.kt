package com.inventario.mobile.data.sync

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gerenciador de sincronização delta
 * Busca apenas dados que mudaram desde a última sincronização
 * Reduz drasticamente o tráfego de rede
 */
class DeltaSyncManager(
    private val context: Context,
    private val apiService: ApiService
) {
    
    companion object {
        private const val TAG = "DeltaSyncManager"
        private const val PREF_LAST_SYNC_PATRIMONIOS = "last_sync_patrimonios"
        private const val PREF_LAST_SYNC_COLETAS = "last_sync_coletas"
        private const val PREF_LAST_SYNC_DASHBOARD = "last_sync_dashboard"
        
        @Volatile
        private var instance: DeltaSyncManager? = null
        
        fun getInstance(context: Context, apiService: ApiService): DeltaSyncManager {
            return instance ?: synchronized(this) {
                instance ?: DeltaSyncManager(context, apiService).also { instance = it }
            }
        }
    }
    
    private val preferencesManager = PreferencesManager(context)
    
    /**
     * Resultado de sincronização delta
     */
    data class DeltaSyncResult<T>(
        val newItems: List<T>,
        val updatedItems: List<T>,
        val deletedIds: List<Long>,
        val hasChanges: Boolean
    ) {
        val totalChanges: Int get() = newItems.size + updatedItems.size + deletedIds.size
    }
    
    /**
     * Sincroniza patrimônios (apenas mudanças)
     */
    suspend fun syncPatrimonios(): DeltaSyncResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val lastSync = getLastSyncTime(PREF_LAST_SYNC_PATRIMONIOS)
                
                Log.d(TAG, "Sincronizando patrimônios desde: $lastSync")
                
                // TODO: Implementar endpoint no servidor que aceita timestamp
                // GET /api/mobile/patrimonio/delta?since={timestamp}
                
                // Por enquanto, retornar resultado vazio
                val result = DeltaSyncResult<Any>(
                    newItems = emptyList(),
                    updatedItems = emptyList(),
                    deletedIds = emptyList(),
                    hasChanges = false
                )
                
                if (result.hasChanges) {
                    updateLastSyncTime(PREF_LAST_SYNC_PATRIMONIOS)
                    Log.d(TAG, "Patrimônios sincronizados: ${result.totalChanges} mudanças")
                } else {
                    Log.d(TAG, "Nenhuma mudança em patrimônios")
                }
                
                result
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar patrimônios", e)
                throw e
            }
        }
    }
    
    /**
     * Sincroniza coletas (apenas mudanças)
     */
    suspend fun syncColetas(): DeltaSyncResult<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val lastSync = getLastSyncTime(PREF_LAST_SYNC_COLETAS)
                
                Log.d(TAG, "Sincronizando coletas desde: $lastSync")
                
                // TODO: Implementar endpoint no servidor
                // GET /api/mobile/coletas/delta?since={timestamp}
                
                val result = DeltaSyncResult<Any>(
                    newItems = emptyList(),
                    updatedItems = emptyList(),
                    deletedIds = emptyList(),
                    hasChanges = false
                )
                
                if (result.hasChanges) {
                    updateLastSyncTime(PREF_LAST_SYNC_COLETAS)
                    Log.d(TAG, "Coletas sincronizadas: ${result.totalChanges} mudanças")
                } else {
                    Log.d(TAG, "Nenhuma mudança em coletas")
                }
                
                result
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar coletas", e)
                throw e
            }
        }
    }
    
    /**
     * Verifica se precisa sincronizar (baseado em tempo)
     */
    fun shouldSync(key: String, intervalMs: Long = 300000): Boolean { // 5 minutos padrão
        val lastSync = getLastSyncTime(key)
        val now = System.currentTimeMillis()
        return (now - lastSync) > intervalMs
    }
    
    /**
     * Força sincronização completa (ignora delta)
     */
    fun forceFullSync() {
        clearLastSyncTime(PREF_LAST_SYNC_PATRIMONIOS)
        clearLastSyncTime(PREF_LAST_SYNC_COLETAS)
        clearLastSyncTime(PREF_LAST_SYNC_DASHBOARD)
        Log.d(TAG, "Sincronização completa forçada")
    }
    
    // ===== Métodos auxiliares =====
    
    private fun getLastSyncTime(key: String): Long {
        return preferencesManager.getLong(key, 0L)
    }
    
    private fun updateLastSyncTime(key: String) {
        preferencesManager.putLong(key, System.currentTimeMillis())
    }
    
    private fun clearLastSyncTime(key: String) {
        preferencesManager.remove(key)
    }
}
