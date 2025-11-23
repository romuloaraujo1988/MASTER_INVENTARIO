package com.inventario.mobile.ui.components

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.utils.NetworkMonitor
import kotlinx.coroutines.launch

/**
 * Componente para mostrar indicador de modo offline na toolbar
 */
object OfflineIndicator {
    
    /**
     * Configura o indicador de modo offline na Activity
     */
    fun setup(activity: Activity) {
        val offlineIndicator = activity.findViewById<LinearLayout>(R.id.offline_indicator)
        
        if (offlineIndicator == null) {
            android.util.Log.w("OfflineIndicator", "Indicador offline não encontrado no layout")
            return
        }
        
        val networkMonitor = NetworkMonitor.getInstance(activity)
        
        // Verificar estado inicial
        updateIndicator(offlineIndicator, networkMonitor.isConnected())
        
        // Observar mudanças de conectividade
        if (activity is androidx.lifecycle.LifecycleOwner) {
            activity.lifecycleScope.launch {
                networkMonitor.observeConnectivity().collect { isConnected ->
                    android.util.Log.d("OfflineIndicator", "Conexão mudou: ${if (isConnected) "ONLINE" else "OFFLINE"}")
                    updateIndicator(offlineIndicator, isConnected)
                }
            }
        }
    }
    
    /**
     * Atualiza a visibilidade do indicador
     */
    private fun updateIndicator(indicator: LinearLayout, isConnected: Boolean) {
        indicator.post {
            if (isConnected) {
                // Online - esconder indicador
                indicator.visibility = View.GONE
                android.util.Log.d("OfflineIndicator", "✅ Online - indicador oculto")
            } else {
                // Offline - mostrar indicador
                indicator.visibility = View.VISIBLE
                android.util.Log.d("OfflineIndicator", "📴 Offline - indicador visível")
            }
        }
    }
}
