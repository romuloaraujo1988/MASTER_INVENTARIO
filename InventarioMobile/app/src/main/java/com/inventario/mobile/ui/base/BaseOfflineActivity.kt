package com.inventario.mobile.ui.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.network.ConnectionStateManager
import com.inventario.mobile.ui.ConnectionStatusBar
import com.inventario.mobile.utils.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity base com suporte automático a modo offline
 * 
 * Funcionalidades:
 * - Detecta mudanças de conectividade automaticamente
 * - Mostra indicador visual de modo offline
 * - Notifica subclasses sobre mudanças de conexão
 * - Gerencia estado de conexão centralizado
 * 
 * Como usar:
 * ```kotlin
 * @AndroidEntryPoint
 * class MinhaActivity : BaseOfflineActivity() {
 *     
 *     override fun onConnectivityRestored() {
 *         // Conexão voltou, recarregar dados do servidor
 *         viewModel.recarregarDados()
 *     }
 *     
 *     override fun onConnectivityLost() {
 *         // Conexão perdida, usar apenas dados locais
 *         viewModel.usarDadosLocais()
 *     }
 * }
 * ```
 */
abstract class BaseOfflineActivity : AppCompatActivity() {
    
    // Injeção será feita pelas subclasses que têm @AndroidEntryPoint
    @Inject
    lateinit var connectionStateManager: ConnectionStateManager
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    protected lateinit var connectionStatusBar: ConnectionStatusBar
    
    private var wasOffline = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupConnectionStatusBar()
    }
    
    override fun onResume() {
        super.onResume()
        observeConnectivity()
    }
    
    /**
     * Configura a barra de status de conexão
     */
    private fun setupConnectionStatusBar() {
        // Criar barra de status
        connectionStatusBar = ConnectionStatusBar(this)
        
        // Adicionar no topo da hierarquia de views
        // Será adicionada após setContentView() ser chamado
        window.decorView.post {
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            if (rootView != null && connectionStatusBar.parent == null) {
                rootView.addView(connectionStatusBar, 0)
            }
        }
    }
    
    /**
     * Observa mudanças de conectividade
     */
    private fun observeConnectivity() {
        // Observar estado de conexão do NetworkMonitor
        lifecycleScope.launch {
            networkMonitor.observeConnectivity().collect { isOnline ->
                handleConnectivityChange(isOnline)
            }
        }
        
        // Observar estado do ConnectionStateManager
        lifecycleScope.launch {
            connectionStateManager.connectionState.collect { state ->
                updateConnectionStatusBar(state)
            }
        }
    }
    
    /**
     * Trata mudanças de conectividade
     */
    private fun handleConnectivityChange(isOnline: Boolean) {
        // Atualizar ConnectionStateManager
        connectionStateManager.updateConnectionState(isOnline = isOnline)
        
        // Notificar subclasses
        if (isOnline && wasOffline) {
            // Reconectou
            onConnectivityRestored()
        } else if (!isOnline && !wasOffline) {
            // Perdeu conexão
            onConnectivityLost()
        }
        
        wasOffline = !isOnline
    }
    
    /**
     * Atualiza a barra de status de conexão
     */
    private fun updateConnectionStatusBar(state: ConnectionStateManager.ConnectionState) {
        when {
            state.isOffline -> {
                connectionStatusBar.setStatus(
                    ConnectionStatusBar.Status.OFFLINE,
                    state.statusMessage
                )
            }
            state.pendingCollections > 0 -> {
                connectionStatusBar.setStatus(
                    ConnectionStatusBar.Status.SYNCING,
                    state.statusMessage
                )
            }
            else -> {
                connectionStatusBar.setStatus(ConnectionStatusBar.Status.ONLINE)
            }
        }
    }
    
    /**
     * Chamado quando a conexão é restaurada
     * Subclasses podem sobrescrever para reagir à reconexão
     */
    protected open fun onConnectivityRestored() {
        // Implementação padrão vazia
        // Subclasses podem sobrescrever
    }
    
    /**
     * Chamado quando a conexão é perdida
     * Subclasses podem sobrescrever para reagir à perda de conexão
     */
    protected open fun onConnectivityLost() {
        // Implementação padrão vazia
        // Subclasses podem sobrescrever
    }
    
    /**
     * Verifica se está online
     */
    protected fun isOnline(): Boolean {
        return !connectionStateManager.isOffline()
    }
    
    /**
     * Verifica se está offline
     */
    protected fun isOffline(): Boolean {
        return connectionStateManager.isOffline()
    }
    
    /**
     * Obtém o número de coletas pendentes
     */
    protected fun getPendingCollectionsCount(): Int {
        return connectionStateManager.getPendingCollectionsCount()
    }
    
    /**
     * Força modo offline
     */
    protected fun enableForceOfflineMode(reason: String = "Modo offline ativado manualmente") {
        connectionStateManager.enableForceOfflineMode(reason)
    }
    
    /**
     * Desativa modo offline forçado
     */
    protected fun disableForceOfflineMode() {
        connectionStateManager.disableForceOfflineMode()
    }
}
