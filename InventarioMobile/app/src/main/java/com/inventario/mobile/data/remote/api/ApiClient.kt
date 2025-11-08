package com.inventario.mobile.data.remote.api

import android.content.Context
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.utils.ServerConfigManager
import com.inventario.mobile.utils.PreferencesManager

/**
 * Cliente singleton para gerenciar a instância do ApiService
 * Garante que sempre usamos a configuração mais recente do servidor
 */
object ApiClient {
    
    @Volatile
    private var apiService: ApiService? = null
    
    @Volatile
    private var currentBaseUrl: String? = null
    
    /**
     * Obtém a instância do ApiService
     * Recria se a URL base mudou
     */
    fun getApiService(context: Context): ApiService {
        val serverConfigManager = ServerConfigManager.getInstance(context)
        val newBaseUrl = serverConfigManager.getBaseUrl()
        
        // Se a URL mudou ou não existe instância, criar nova
        if (apiService == null || currentBaseUrl != newBaseUrl) {
            synchronized(this) {
                if (apiService == null || currentBaseUrl != newBaseUrl) {
                    apiService = createApiService(context)
                    currentBaseUrl = newBaseUrl
                }
            }
        }
        
        return apiService!!
    }
    
    /**
     * Força a recriação do ApiService
     * Útil quando o usuário muda a configuração do servidor
     */
    fun recreateApiService(context: Context): ApiService {
        synchronized(this) {
            // Limpar cache do NetworkModule primeiro
            NetworkModule.clearApiService()
            
            // Criar nova instância
            apiService = createApiService(context)
            currentBaseUrl = ServerConfigManager.getInstance(context).getBaseUrl()
            
            android.util.Log.d("ApiClient", "ApiService recriado com nova URL: $currentBaseUrl")
        }
        return apiService!!
    }
    
    /**
     * Cria uma nova instância do ApiService
     */
    private fun createApiService(context: Context): ApiService {
        return NetworkModule.getApiService(context)
    }
    
    /**
     * Limpa a instância (útil para testes ou logout)
     */
    fun clearInstance() {
        synchronized(this) {
            // Limpar cache do NetworkModule também
            NetworkModule.clearApiService()
            
            apiService = null
            currentBaseUrl = null
            
            android.util.Log.d("ApiClient", "ApiClient limpo")
        }
    }
}
