package com.inventario.mobile.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.network.ConnectivityMonitor
import com.inventario.mobile.sync.AutoSyncManager
import kotlinx.coroutines.launch

/**
 * Activity principal que demonstra o uso do monitoramento automático de conectividade
 * 
 * EXEMPLO DE USO:
 * 
 * 1. Quando o app inicia, o monitoramento é ativado automaticamente
 * 2. Quando o servidor volta a ficar disponível, sincroniza automaticamente
 * 3. Mostra notificações para o usuário sobre o status
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var connectivityMonitor: ConnectivityMonitor
    private lateinit var autoSyncManager: AutoSyncManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar monitores
        connectivityMonitor = ConnectivityMonitor.getInstance(this)
        autoSyncManager = AutoSyncManager.getInstance(this)
        
        // Iniciar monitoramento automático
        setupAutoSync()
        
        // Observar mudanças de conectividade
        observeConnectivity()
        
        // Observar sincronização automática
        observeAutoSync()
    }
    
    /**
     * Configura sincronização automática
     */
    private fun setupAutoSync() {
        // Iniciar monitoramento
        autoSyncManager.start()
        
        // Habilitar sincronização automática
        autoSyncManager.setEnabled(true)
    }
    
    /**
     * Observa mudanças de conectividade
     */
    private fun observeConnectivity() {
        lifecycleScope.launch {
            connectivityMonitor.connectivityState.collect { state ->
                when (state) {
                    is ConnectivityMonitor.ConnectivityState.NoInternet -> {
                        showToast("❌ Sem internet - Modo offline ativado")
                    }
                    
                    is ConnectivityMonitor.ConnectivityState.InternetAvailable -> {
                        showToast("✅ Internet disponível - Verificando servidor...")
                    }
                    
                    is ConnectivityMonitor.ConnectivityState.ServerAvailable -> {
                        showToast("✅ Servidor disponível - Sincronizando...")
                    }
                    
                    is ConnectivityMonitor.ConnectivityState.ServerUnavailable -> {
                        showToast("⚠️ Servidor indisponível - Dados salvos localmente")
                    }
                }
            }
        }
    }
    
    /**
     * Observa sincronização automática
     */
    private fun observeAutoSync() {
        lifecycleScope.launch {
            autoSyncManager.autoSyncState.collect { state ->
                when (state) {
                    is AutoSyncManager.AutoSyncState.Idle -> {
                        // Nada a fazer
                    }
                    
                    is AutoSyncManager.AutoSyncState.Syncing -> {
                        showToast("🔄 Sincronizando dados...")
                    }
                    
                    is AutoSyncManager.AutoSyncState.Success -> {
                        if (state.count > 0) {
                            showToast("✅ ${state.count} coleta(s) sincronizada(s)")
                        }
                    }
                    
                    is AutoSyncManager.AutoSyncState.Error -> {
                        showToast("❌ Erro na sincronização: ${state.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Mostra toast para o usuário
     */
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Parar monitoramento quando app for destruído
        autoSyncManager.stop()
    }
}
