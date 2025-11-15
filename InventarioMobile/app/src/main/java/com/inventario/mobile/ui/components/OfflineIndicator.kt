package com.inventario.mobile.ui.components

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.inventario.mobile.R
import com.inventario.mobile.utils.PreferencesManager

/**
 * Componente para exibir indicador de modo offline na barra superior
 */
object OfflineIndicator {
    
    /**
     * Adiciona o indicador de modo offline ao layout da Activity
     * Deve ser chamado após setContentView()
     * 
     * @param activity Activity onde o indicador será adicionado
     * @return View do indicador (para controle manual se necessário)
     */
    fun setup(activity: Activity): View? {
        try {
            val prefsManager = PreferencesManager(activity)
            val isOfflineMode = prefsManager.isForceOfflineMode()
            
            // Buscar o root layout da activity
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val activityRoot = rootView.getChildAt(0) as? ViewGroup
            
            if (activityRoot != null) {
                // Inflar o indicador
                val indicator = activity.layoutInflater.inflate(
                    R.layout.view_offline_indicator,
                    activityRoot,
                    false
                )
                
                // Adicionar no topo do layout
                activityRoot.addView(indicator, 0)
                
                // Mostrar/ocultar baseado no modo offline
                indicator.visibility = if (isOfflineMode) View.VISIBLE else View.GONE
                
                android.util.Log.d("OfflineIndicator", "Indicador adicionado - Modo offline: $isOfflineMode")
                
                return indicator
            } else {
                android.util.Log.w("OfflineIndicator", "Não foi possível encontrar o root layout")
            }
        } catch (e: Exception) {
            android.util.Log.e("OfflineIndicator", "Erro ao adicionar indicador", e)
        }
        
        return null
    }
    
    /**
     * Atualiza a visibilidade do indicador
     * 
     * @param activity Activity onde o indicador está
     * @param show true para mostrar, false para ocultar
     */
    fun updateVisibility(activity: Activity, show: Boolean) {
        try {
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val activityRoot = rootView.getChildAt(0) as? ViewGroup
            
            activityRoot?.findViewById<View>(R.id.offlineIndicatorBar)?.let { indicator ->
                indicator.visibility = if (show) View.VISIBLE else View.GONE
                android.util.Log.d("OfflineIndicator", "Visibilidade atualizada: ${if (show) "VISIBLE" else "GONE"}")
            }
        } catch (e: Exception) {
            android.util.Log.e("OfflineIndicator", "Erro ao atualizar visibilidade", e)
        }
    }
    
    /**
     * Verifica e atualiza o indicador baseado nas preferências
     * 
     * @param activity Activity onde o indicador está
     */
    fun refresh(activity: Activity) {
        val prefsManager = PreferencesManager(activity)
        val isOfflineMode = prefsManager.isForceOfflineMode()
        updateVisibility(activity, isOfflineMode)
    }
}
