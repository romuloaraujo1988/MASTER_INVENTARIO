package com.inventario.mobile.utils

import android.content.Context

/**
 * Helper para verificar e gerenciar o modo offline forçado
 */
object OfflineModeHelper {
    
    /**
     * Verifica se o modo offline forçado está ativo
     */
    fun isForceOfflineMode(context: Context): Boolean {
        val prefs = PreferencesManager(context)
        return prefs.isForceOfflineMode()
    }
    
    /**
     * Verifica se deve fazer chamada de rede
     * Retorna false se modo offline estiver ativo
     */
    fun shouldMakeNetworkCall(context: Context): Boolean {
        return !isForceOfflineMode(context)
    }
    
    /**
     * Loga tentativa de chamada de rede em modo offline
     */
    fun logOfflineAttempt(tag: String, operation: String) {
        android.util.Log.d(tag, "⚠️ Modo offline ativo - Operação '$operation' será executada apenas localmente")
    }
}
