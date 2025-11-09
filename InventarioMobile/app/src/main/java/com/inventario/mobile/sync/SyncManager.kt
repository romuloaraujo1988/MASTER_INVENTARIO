package com.inventario.mobile.sync

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SyncManager stub - Mantido para compatibilidade temporária
 * TODO: Migrar para Use Cases de sincronização
 */
class SyncManager private constructor(private val context: Context) {
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: SyncManager? = null
        
        fun getInstance(context: Context): SyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Métodos stub
    suspend fun syncAll(): SyncResult {
        return SyncResult.Success(0)
    }
    
    suspend fun syncColetas(): SyncResult {
        return SyncResult.Success(0)
    }
    
    suspend fun syncPatrimonios(): SyncResult {
        return SyncResult.Success(0)
    }
    
    suspend fun syncPendingData(): SyncResult {
        return SyncResult.Success(0)
    }
    
    suspend fun retryFailedSync(): SyncResult {
        return SyncResult.Success(0)
    }
    
    fun agendarSincronizacaoPeriodica() {
        // No-op
    }
    
    fun forceSyncNow() {
        // No-op
    }
    
    fun setAutoSyncEnabled(enabled: Boolean) {
        // No-op
    }
}

/**
 * Resultado de sincronização
 */
sealed class SyncResult {
    data class Success(val itemsSynced: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    object InProgress : SyncResult()
}

/**
 * Estado de sincronização
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val current: Int = 0, val total: Int = 0) : SyncState()
    data class Success(val itemsSynced: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}
