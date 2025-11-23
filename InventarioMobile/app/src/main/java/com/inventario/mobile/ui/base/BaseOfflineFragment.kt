package com.inventario.mobile.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.network.ConnectionStateManager
import com.inventario.mobile.ui.ConnectionStatusBar
import com.inventario.mobile.utils.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment base com suporte automático a modo offline
 * 
 * Funcionalidades:
 * - Detecta mudanças de conectividade automaticamente
 * - Acessa indicador visual da Activity pai
 * - Notifica subclasses sobre mudanças de conexão
 * - Gerencia estado de conexão centralizado
 * 
 * Como usar:
 * ```kotlin
 * @AndroidEntryPoint
 * class MeuFragment : BaseOfflineFragment() {
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
 * 
 * IMPORTANTE: Não adicione @AndroidEntryPoint nesta classe base.
 * Adicione apenas nas classes concretas que herdam dela.
 */
abstract class BaseOfflineFragment : Fragment() {
    
    @Inject
    lateinit var connectionStateManager: ConnectionStateManager
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    private var wasOffline = false
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeConnectivity()
    }
    
    /**
     * Observa mudanças de conectividade
     */
    private fun observeConnectivity() {
        // Observar estado de conexão do NetworkMonitor
        viewLifecycleOwner.lifecycleScope.launch {
            networkMonitor.observeConnectivity().collect { isOnline ->
                handleConnectivityChange(isOnline)
            }
        }
        
        // Observar estado do ConnectionStateManager
        viewLifecycleOwner.lifecycleScope.launch {
            connectionStateManager.connectionState.collect { state ->
                updateConnectionStatus(state)
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
     * Atualiza o status de conexão
     */
    private fun updateConnectionStatus(state: ConnectionStateManager.ConnectionState) {
        // A barra de status é gerenciada pela Activity pai
        // Fragment apenas observa o estado, não precisa atualizar a barra diretamente
        // A BaseOfflineActivity já está observando e atualizando a barra
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
