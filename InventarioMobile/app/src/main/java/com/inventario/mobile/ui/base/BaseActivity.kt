package com.inventario.mobile.ui.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.ui.components.OfflineIndicatorView
import com.inventario.mobile.util.NetworkUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Activity base que fornece funcionalidades comuns:
 * - Indicador de modo offline
 * - Observação de conectividade
 * - Métodos utilitários
 * 
 * Uso:
 * ```kotlin
 * class MinhaActivity : BaseActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_minha)
 *         
 *         // Indicador offline já está funcionando!
 *     }
 * }
 * ```
 */
abstract class BaseActivity : AppCompatActivity() {

    private var offlineIndicator: OfflineIndicatorView? = null
    private var isObservingNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar observação de rede
        startNetworkObservation()
    }

    override fun onDestroy() {
        super.onDestroy()
        isObservingNetwork = false
    }

    /**
     * Adiciona indicador de modo offline ao layout
     * Deve ser chamado após setContentView()
     */
    protected fun setupOfflineIndicator() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        
        if (offlineIndicator == null) {
            offlineIndicator = OfflineIndicatorView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Adicionar no topo do layout
            rootView.addView(offlineIndicator, 0)
        }
        
        // Atualizar estado inicial
        updateOfflineIndicator(NetworkUtils.isNetworkAvailable(this))
    }

    /**
     * Inicia observação de mudanças de rede
     */
    private fun startNetworkObservation() {
        if (isObservingNetwork) return
        
        isObservingNetwork = true
        
        NetworkUtils.observeNetworkConnectivity(this)
            .onEach { isConnected ->
                updateOfflineIndicator(isConnected)
                onNetworkStatusChanged(isConnected)
            }
            .launchIn(lifecycleScope)
    }

    /**
     * Atualiza indicador visual de conectividade
     */
    private fun updateOfflineIndicator(isConnected: Boolean) {
        offlineIndicator?.let { indicator ->
            if (isConnected) {
                indicator.setStatus(OfflineIndicatorView.Status.ONLINE)
            } else {
                indicator.setStatus(OfflineIndicatorView.Status.OFFLINE)
            }
        }
    }

    /**
     * Callback chamado quando status de rede muda
     * Pode ser sobrescrito por subclasses
     */
    protected open fun onNetworkStatusChanged(isConnected: Boolean) {
        // Implementação padrão vazia
        // Subclasses podem sobrescrever para reagir a mudanças
    }

    /**
     * Mostra indicador de sincronização
     */
    protected fun showSyncingIndicator() {
        offlineIndicator?.setStatus(OfflineIndicatorView.Status.SYNCING)
    }

    /**
     * Mostra mensagem customizada no indicador
     */
    protected fun showIndicatorMessage(message: String, isError: Boolean = false) {
        offlineIndicator?.setCustomMessage(message, isError)
    }

    /**
     * Verifica se está online
     */
    protected fun isOnline(): Boolean {
        return NetworkUtils.isNetworkAvailable(this)
    }

    /**
     * Verifica se está offline
     */
    protected fun isOffline(): Boolean {
        return !isOnline()
    }
}
