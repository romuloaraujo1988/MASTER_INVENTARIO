package com.inventario.mobile.network

import android.content.Context
import android.util.Log
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador centralizado do estado de conexão
 * 
 * Funcionalidades:
 * - Monitora estado de conexão (online/offline)
 * - Detecta modo offline forçado
 * - Notifica observers sobre mudanças
 * - Fornece informações sobre coletas pendentes
 */
@Singleton
class ConnectionStateManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "ConnectionStateManager"
    }
    
    /**
     * Estado de conexão
     */
    data class ConnectionState(
        val isOnline: Boolean = true,
        val isForceOffline: Boolean = false,
        val pendingCollections: Int = 0,
        val lastSyncTime: Long = 0L,
        val message: String = ""
    ) {
        val isOffline: Boolean
            get() = !isOnline || isForceOffline
            
        val statusMessage: String
            get() = when {
                isForceOffline -> "📴 Modo Offline Forçado"
                !isOnline -> "📴 Sem Conexão"
                pendingCollections > 0 -> "⏳ $pendingCollections coletas pendentes"
                else -> "✓ Online"
            }
    }
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    /**
     * Atualiza o estado de conexão
     */
    fun updateConnectionState(
        isOnline: Boolean? = null,
        pendingCollections: Int? = null
    ) {
        val current = _connectionState.value
        val isForceOffline = preferencesManager.isForceOfflineMode()
        
        _connectionState.value = current.copy(
            isOnline = isOnline ?: current.isOnline,
            isForceOffline = isForceOffline,
            pendingCollections = pendingCollections ?: current.pendingCollections,
            lastSyncTime = if (isOnline == true && !isForceOffline) {
                System.currentTimeMillis()
            } else {
                current.lastSyncTime
            }
        )
        
        Log.d(TAG, "Estado atualizado: ${_connectionState.value}")
    }
    
    /**
     * Ativa modo offline forçado
     */
    fun enableForceOfflineMode(reason: String = "Falhas consecutivas de conexão") {
        Log.w(TAG, "Ativando modo offline forçado: $reason")
        preferencesManager.setForceOfflineMode(true)
        
        _connectionState.value = _connectionState.value.copy(
            isForceOffline = true,
            message = reason
        )
    }
    
    /**
     * Desativa modo offline forçado
     */
    fun disableForceOfflineMode() {
        Log.d(TAG, "Desativando modo offline forçado")
        preferencesManager.setForceOfflineMode(false)
        
        _connectionState.value = _connectionState.value.copy(
            isForceOffline = false,
            message = "Conexão restaurada"
        )
    }
    
    /**
     * Verifica se está em modo offline
     */
    fun isOffline(): Boolean {
        return _connectionState.value.isOffline
    }
    
    /**
     * Obtém número de coletas pendentes
     */
    fun getPendingCollectionsCount(): Int {
        return _connectionState.value.pendingCollections
    }
    
    /**
     * Atualiza contador de coletas pendentes
     */
    fun updatePendingCollections(count: Int) {
        _connectionState.value = _connectionState.value.copy(
            pendingCollections = count
        )
        Log.d(TAG, "Coletas pendentes: $count")
    }
    
    /**
     * Incrementa contador de coletas pendentes
     */
    fun incrementPendingCollections() {
        val current = _connectionState.value.pendingCollections
        updatePendingCollections(current + 1)
    }
    
    /**
     * Decrementa contador de coletas pendentes
     */
    fun decrementPendingCollections(amount: Int = 1) {
        val current = _connectionState.value.pendingCollections
        updatePendingCollections(maxOf(0, current - amount))
    }
}
