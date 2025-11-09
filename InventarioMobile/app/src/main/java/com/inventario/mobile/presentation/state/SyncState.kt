package com.inventario.mobile.presentation.state

/**
 * Estados possíveis da sincronização
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val progress: Int, val total: Int) : SyncState()
    data class Success(val totalSincronizado: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}
