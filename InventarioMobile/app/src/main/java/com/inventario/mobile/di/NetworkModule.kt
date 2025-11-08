package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.network.DeviceInfoInterceptor
import com.inventario.mobile.utils.ServerConfigManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Módulo de rede simplificado - sem Hilt
 * Fornece instâncias singleton para Retrofit e ApiService
 */
object NetworkModule {

    @Volatile
    private var apiServiceInstance: ApiService? = null

    /**
     * Fornece instância singleton do ApiService
     */
    fun getApiService(context: Context): ApiService {
        return try {
            android.util.Log.d("NetworkModule", "Solicitando instância do ApiService...")
            
            apiServiceInstance ?: synchronized(this) {
                apiServiceInstance ?: createApiService(context).also { 
                    apiServiceInstance = it
                    android.util.Log.d("NetworkModule", "Nova instância do ApiService criada e armazenada")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NetworkModule", "Erro ao obter ApiService", e)
            throw RuntimeException("Falha na inicialização do serviço de rede: ${e.message}", e)
        }
    }

    /**
     * Cria nova instância do ApiService
     */
    private fun createApiService(context: Context): ApiService {
        try {
            android.util.Log.d("NetworkModule", "Criando novo ApiService")
            
            android.util.Log.d("NetworkModule", "Inicializando ServerConfigManager...")
            val serverConfigManager = try {
                ServerConfigManager.getInstance(context)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao inicializar ServerConfigManager", e)
                throw RuntimeException("Falha na configuração do servidor: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Inicializando LocalDataManager...")
            val localDataManager = try {
                LocalDataManager.getInstance(context)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao inicializar LocalDataManager", e)
                throw RuntimeException("Falha no gerenciador de dados locais: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Inicializando PreferencesManager...")
            val preferencesManager = try {
                com.inventario.mobile.utils.PreferencesManager(context)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao inicializar PreferencesManager", e)
                throw RuntimeException("Falha no gerenciador de preferências: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Criando interceptors...")
            val loggingInterceptor = try {
                createHttpLoggingInterceptor()
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar logging interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de log: ${e.message}", e)
            }
            
            val authInterceptor = try {
                createAuthInterceptor(localDataManager, preferencesManager)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar auth interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de autenticação: ${e.message}", e)
            }
            
            val deviceInfoInterceptor = try {
                DeviceInfoInterceptor(context)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar device info interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de informações do dispositivo: ${e.message}", e)
            }
            
            val compressionInterceptor = try {
                com.inventario.mobile.network.CompressionInterceptor()
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar compression interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de compressão: ${e.message}", e)
            }
            
            val cacheInterceptor = try {
                com.inventario.mobile.network.CacheInterceptor()
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar cache interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de cache: ${e.message}", e)
            }
            
            val retryInterceptor = try {
                com.inventario.mobile.network.RetryInterceptor()
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar retry interceptor", e)
                throw RuntimeException("Falha na criação do interceptor de retry: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Criando OkHttpClient...")
            val okHttpClient = try {
                createOkHttpClient(context, loggingInterceptor, authInterceptor, deviceInfoInterceptor, compressionInterceptor, cacheInterceptor, retryInterceptor)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar OkHttpClient", e)
                throw RuntimeException("Falha na criação do cliente HTTP: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Criando Retrofit...")
            val retrofit = try {
                createRetrofit(okHttpClient, serverConfigManager)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar Retrofit", e)
                throw RuntimeException("Falha na criação do cliente Retrofit: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "Criando ApiService...")
            val apiService = try {
                retrofit.create(ApiService::class.java)
            } catch (e: Exception) {
                android.util.Log.e("NetworkModule", "Erro ao criar ApiService", e)
                throw RuntimeException("Falha na criação do serviço de API: ${e.message}", e)
            }
            
            android.util.Log.d("NetworkModule", "ApiService criado com sucesso")
            return apiService
            
        } catch (e: Exception) {
            android.util.Log.e("NetworkModule", "Erro geral na criação do ApiService", e)
            throw RuntimeException("Falha crítica na inicialização do NetworkModule: ${e.message}", e)
        }
    }

    /**
     * Cria interceptor de logging
     */
    private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Cria interceptor de autenticação
     */
    private fun createAuthInterceptor(
        localDataManager: LocalDataManager,
        preferencesManager: com.inventario.mobile.utils.PreferencesManager
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            android.util.Log.d("NetworkModule", "=== AUTH INTERCEPTOR ===")
            android.util.Log.d("NetworkModule", "URL: ${originalRequest.url}")
            
            // Tentar obter token do LocalDataManager primeiro
            var currentUser: com.inventario.mobile.data.model.Usuario? = null
            var token: String? = null
            var isTokenValid = false
            
            try {
                // Como estamos em um interceptor, não podemos usar suspend functions diretamente
                // Vamos usar runBlocking para chamar a função suspend
                currentUser = kotlinx.coroutines.runBlocking {
                    localDataManager.getCurrentUser()
                }
                token = currentUser?.accessToken
                isTokenValid = currentUser?.isTokenValid() ?: false
            } catch (e: Exception) {
                android.util.Log.w("NetworkModule", "Erro ao obter usuário do LocalDataManager: ${e.message}")
            }
            
            android.util.Log.d("NetworkModule", "LocalDataManager - User: ${currentUser?.nome ?: "null"}")
            android.util.Log.d("NetworkModule", "LocalDataManager - Token: ${token?.take(20) ?: "null"}...")
            android.util.Log.d("NetworkModule", "LocalDataManager - Token Valid: $isTokenValid")
            
            // Se não encontrou no LocalDataManager, tentar PreferencesManager
            if (token == null || !isTokenValid) {
                android.util.Log.d("NetworkModule", "Tentando PreferencesManager...")
                token = preferencesManager.getAccessToken()
                isTokenValid = preferencesManager.isTokenValid()
                android.util.Log.d("NetworkModule", "PreferencesManager - Token: ${token?.take(20) ?: "null"}...")
                android.util.Log.d("NetworkModule", "PreferencesManager - Token Valid: $isTokenValid")
            }
            
            val newRequest = if (token != null && isTokenValid) {
                android.util.Log.d("NetworkModule", "✓ Adicionando token ao header")
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                android.util.Log.w("NetworkModule", "✗ Token não disponível ou inválido")
                originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
            }
            
            chain.proceed(newRequest)
        }
    }

    /**
     * Cria cliente OkHttp
     */
    private fun createOkHttpClient(
        context: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        deviceInfoInterceptor: DeviceInfoInterceptor,
        compressionInterceptor: com.inventario.mobile.network.CompressionInterceptor,
        cacheInterceptor: com.inventario.mobile.network.CacheInterceptor,
        retryInterceptor: com.inventario.mobile.network.RetryInterceptor
    ): OkHttpClient {
        android.util.Log.d("NetworkModule", "Configurando OkHttpClient com interceptors")
        
        // Configurar cache HTTP (10 MB)
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cache = okhttp3.Cache(context.cacheDir, cacheSize)
        
        return OkHttpClient.Builder()
            .cache(cache)  // Cache HTTP
            .addInterceptor(retryInterceptor)  // Retry primeiro (para tentar novamente em caso de falha)
            .addInterceptor(compressionInterceptor)  // Compressão
            .addInterceptor(deviceInfoInterceptor)  // Device info
            .addInterceptor(authInterceptor)  // Auth
            .addNetworkInterceptor(cacheInterceptor)  // Cache (network interceptor para funcionar corretamente)
            .addInterceptor(loggingInterceptor)  // Log por último para ver todos os headers
            // Timeouts aumentados para dispositivos físicos Android 14
            .connectTimeout(45, TimeUnit.SECONDS)  // Aumentado de 30 para 45
            .readTimeout(60, TimeUnit.SECONDS)     // Aumentado de 30 para 60
            .writeTimeout(60, TimeUnit.SECONDS)    // Aumentado de 30 para 60
            .callTimeout(120, TimeUnit.SECONDS)    // Timeout total da chamada
            .retryOnConnectionFailure(true)
            // Configurações adicionais para Android 14
            .followRedirects(true)
            .followSslRedirects(true)
            // Connection pool para reutilizar conexões
            .connectionPool(okhttp3.ConnectionPool(
                maxIdleConnections = 5,
                keepAliveDuration = 5,
                timeUnit = TimeUnit.MINUTES
            ))
            .build()
    }

    /**
     * Cria instância do Retrofit
     */
    private fun createRetrofit(
        okHttpClient: OkHttpClient,
        serverConfigManager: ServerConfigManager
    ): Retrofit {
        val baseUrl = serverConfigManager.getBaseUrl()
        
        // Garantir que a URL termine com /
        val finalBaseUrl = if (baseUrl.endsWith("/")) {
            baseUrl
        } else {
            "$baseUrl/"
        }
        
        android.util.Log.d("NetworkModule", "Base URL configurada: $finalBaseUrl")
        
        return Retrofit.Builder()
            .baseUrl(finalBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Limpa instância do ApiService (útil para mudanças de configuração)
     */
    fun clearApiService() {
        synchronized(this) {
            apiServiceInstance = null
        }
    }
}