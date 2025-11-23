package com.inventario.mobile.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.util.NetworkUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Activity base que fornece funcionalidades comuns:
 * - Observação de conectividade
 * - Métodos utilitários
 * 
 * Uso:
 * ```kotlin
 * class MinhaActivity : BaseActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_minha)
 *     }
 * }
 * ```
 */
abstract class BaseActivity : AppCompatActivity() {

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
     * Inicia observação de mudanças de rede
     */
    private fun startNetworkObservation() {
        if (isObservingNetwork) return
        
        isObservingNetwork = true
        
        NetworkUtils.observeNetworkConnectivity(this)
            .onEach { isConnected ->
                onNetworkStatusChanged(isConnected)
            }
            .launchIn(lifecycleScope)
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
