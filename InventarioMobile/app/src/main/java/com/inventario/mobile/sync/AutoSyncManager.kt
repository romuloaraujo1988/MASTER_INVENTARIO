package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import com.inventario.mobile.network.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gerenciador de sincronização automática
 * 
 * Monitora conectividade e sincroniza automaticamente quando:
 * 1. Servidor volta a ficar disponível
 * 2. Existem coletas pendentes
 * 3. App está em foreground
 */
class AutoSyncManager private constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AutoSyncManager"
        
        @Volatile
        private var instance: AutoSyncManager? = null
        
        fun getInstance(context: Context): AutoSyncManager {
            return instance ?: synchronized(this) {
                instance ?: AutoSyncManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val connectivityMonitor = ConnectivityMonitor.getInstance(context)
    
    // Estado da sincronização automática
    sealed class AutoSyncState {
        object Idle : AutoSyncState()
        object Syncing : AutoSyncState()
        data class Success(val count: Int) : AutoSyncState()
        data class Error(val message: String) : AutoSyncState()
    }
    
    private val _autoSyncState = MutableStateFlow<AutoSyncState>(AutoSyncState.Idle)
    val autoSyncState: StateFlow<AutoSyncState> = _autoSyncState.asStateFlow()
    
    private var isEnabled = true
    private var wasServerUnavailable = false
    
    /**
     * Inicia monitoramento e sincronização automática
     */
    fun start() {
        Log.d(TAG, "🚀 Iniciando AutoSyncManager")
        
        // Iniciar monitoramento de conectividade
        connectivityMonitor.startMonitoring()
        
        // Observar mudanças de conectividade
        scope.launch {
            connectivityMonitor.connectivityState.collect { state ->
                handleConnectivityChange(state)
            }
        }
    }
    
    /**
     * Para sincronização automática
     */
    fun stop() {
        Log.d(TAG, "🛑 Parando AutoSyncManager")
        connectivityMonitor.stopMonitoring()
    }
    
    /**
     * Habilita/desabilita sincronização automática
     */
    fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ AutoSync ${if (enabled) "HABILITADO" else "DESABILITADO"}")
        isEnabled = enabled
    }
    
    /**
     * Trata mudanças de conectividade
     */
    private fun handleConnectivityChange(state: ConnectivityMonitor.ConnectivityState) {
        Log.d(TAG, "🔄 Conectividade mudou: $state")
        
        when (state) {
            is ConnectivityMonitor.ConnectivityState.NoInternet -> {
                Log.d(TAG, "❌ Sem internet")
                wasServerUnavailable = true
            }
            
            is ConnectivityMonitor.ConnectivityState.InternetAvailable -> {
                Log.d(TAG, "✅ Internet disponível, verificando servidor...")
            }
            
            is ConnectivityMonitor.ConnectivityState.ServerAvailable -> {
                Log.d(TAG, "✅ Servidor disponível!")
                
                // Se servidor estava indisponível e agora voltou, sincronizar
                if (wasServerUnavailable && isEnabled) {
                    Log.d(TAG, "🔄 Servidor voltou! Iniciando sincronização automática...")
                    triggerAutoSync()
                }
                
                wasServerUnavailable = false
            }
            
            is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> {
                Log.d(TAG, "❌ Servidor indisponível")
                wasServerUnavailable = true
            }
        }
    }
    
    /**
     * Dispara sincronização automática
     * TODO: Implementar sincronização completa quando Use Cases estiverem prontos
     */
    private fun triggerAutoSync() {
        if (!isEnabled) {
            Log.d(TAG, "⚠️ AutoSync desabilitado, pulando sincronização")
            return
        }
        
        scope.launch {
            try {
                _autoSyncState.value = AutoSyncState.Syncing
                Log.d(TAG, "🔄 Sincronização automática detectada (implementação pendente)")
                
                // TODO: Usar SincronizarColetasPendentesUseCase quando estiver disponível
                Log.d(TAG, "⚠️ Sincronização automática ainda não implementada completamente")
                _autoSyncState.value = AutoSyncState.Success(0)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro na sincronização automática", e)
                _autoSyncState.value = AutoSyncState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Mostra notificação de sincronização
     */
    private fun showSyncNotification(count: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Criar canal de notificação (Android 8+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "auto_sync",
                    "Sincronização Automática",
                    android.app.NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notificações de sincronização automática"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            // Criar notificação
            val notification = androidx.core.app.NotificationCompat.Builder(context, "auto_sync")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Sincronização Concluída")
                .setContentText("$count coleta(s) sincronizada(s) automaticamente")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(1001, notification)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar notificação", e)
        }
    }
    
    /**
     * Força verificação de servidor e sincronização
     */
    fun forceCheck() {
        Log.d(TAG, "🔄 Forçando verificação de servidor...")
        connectivityMonitor.forceServerCheck()
    }
}
