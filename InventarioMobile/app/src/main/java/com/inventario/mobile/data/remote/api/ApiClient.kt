package com.inventario.mobile.data.remote.api

import android.content.Context
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.utils.ServerConfigManager

/**
 * Cliente singleton para gerenciar a instância do ApiService
 * 
 * NOTA: Se o IP do servidor mudar, o app precisa ser reiniciado
 * para que as novas configurações sejam aplicadas em todas as APIs.
 */
object ApiClient {
    
    @Volatile
    private var apiService: ApiService? = null
    
    @Volatile
    private var currentBaseUrl: String? = null
    
    fun getApiService(context: Context): ApiService {
        val serverConfigManager = ServerConfigManager.getInstance(context)
        val newBaseUrl = serverConfigManager.getBaseUrl()
        
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
     */
    fun recreateApiService(context: Context): ApiService {
        synchronized(this) {
            NetworkModule.clearApiService()
            apiService = createApiService(context)
            currentBaseUrl = ServerConfigManager.getInstance(context).getBaseUrl()
            android.util.Log.d("ApiClient", "ApiService recriado com URL: $currentBaseUrl")
        }
        return apiService!!
    }
    
    private fun createApiService(context: Context): ApiService {
        return NetworkModule.getApiService(context)
    }
    
    fun clearInstance() {
        synchronized(this) {
            NetworkModule.clearApiService()
            apiService = null
            currentBaseUrl = null
        }
    }
    
    /**
     * Obtém instância do AuthApi
     */
    fun getAuthApi(context: Context): AuthApi {
        val serverConfigManager = ServerConfigManager.getInstance(context)
        val baseUrl = serverConfigManager.getBaseUrl()
        
        // Criar Retrofit simples para AuthApi
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        
        return retrofit.create(AuthApi::class.java)
    }
}
