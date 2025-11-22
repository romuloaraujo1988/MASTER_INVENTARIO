package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.inventario.mobile.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Observador de conectividade de rede que dispara sincronização automática
 * quando o dispositivo reconecta à internet
 * 
 * Funcionalidades:
 * - Monitora mudanças de conectividade
 * - Dispara sync automático ao reconectar
 * - Respeita lifecycle do app
 * - Evita múltiplas sincronizações simultâneas
 */
class NetworkConnectivityObserver(
    private val context: Context,
    private val syncManager: SyncManager
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wasOffline = false
    private var isSyncing = false

    companion object {
        private const val TAG = "NetworkConnectivityObserver"
        
        @Volatile
        private var instance: NetworkConnectivityObserver? = null
        
        fun getInstance(context: Context, syncManager: SyncManager): NetworkConnectivityObserver {
            return instance ?: synchronized(this) {
                instance ?: NetworkConnectivityObserver(
                    context.applicationContext,
                    syncManager
                ).also { instance = it }
            }
        }
    }

    /**
     * Inicia observação de conectividade
     */
    fun startObserving() {
        Log.d(TAG, "Iniciando observação de conectividade")
        
        // Registrar lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Observar mudanças de rede
        NetworkUtils.observeNetworkConnectivity(context)
            .onEach { isConnected ->
                handleConnectivityChange(isConnected)
            }
            .launchIn(scope)
    }

    /**
     * Para observação de conectividade
     */
    fun stopObserving() {
        Log.d(TAG, "Parando observação de conectividade")
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    /**
     * Trata mudanças de conectividade
     */
    private fun handleConnectivityChange(isConnected: Boolean) {
        Log.d(TAG, "Conectividade mudou: ${if (isConnected) "ONLINE" else "OFFLINE"}")
        
        if (isConnected && wasOffline && !isSyncing) {
            // Reconectou após estar offline
            Log.d(TAG, "✓ Reconectado! Disparando sincronização automática...")
            triggerAutoSync()
        }
        
        wasOffline = !isConnected
    }

    /**
     * Dispara sincronização automática
     */
    private fun triggerAutoSync() {
        if (isSyncing) {
            Log.d(TAG, "Sincronização já em andamento, ignorando...")
            return
        }
        
        isSyncing = true
        
        scope.launch {
            try {
                Log.d(TAG, "Iniciando sincronização automática...")
                
                // Disparar sync via SyncManager
                syncManager.forceSyncNow()
                
                Log.d(TAG, "✓ Sincronização automática concluída")
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro na sincronização automática", e)
            } finally {
                isSyncing = false
            }
        }
    }

    /**
     * Lifecycle: App em foreground
     */
    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "App em foreground")
        
        // Verificar se reconectou enquanto estava em background
        val isConnected = NetworkUtils.isNetworkAvailable(context)
        if (isConnected && wasOffline) {
            Log.d(TAG, "Reconectou enquanto em background, sincronizando...")
            triggerAutoSync()
        }
    }

    /**
     * Lifecycle: App em background
     */
    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "App em background")
    }
}
