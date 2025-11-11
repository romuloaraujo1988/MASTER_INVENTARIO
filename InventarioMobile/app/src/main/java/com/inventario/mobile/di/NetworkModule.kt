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
            
            android.util.Log.d("NetworkModule", "Criando OkHttpClient...")
            val okHttpClient = try {
                createOkHttpClient(context, loggingInterceptor, authInterceptor, deviceInfoInterceptor)
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
        deviceInfoInterceptor: DeviceInfoInterceptor
    ): OkHttpClient {
        android.util.Log.d("NetworkModule", "Configurando OkHttpClient com interceptors")
        
        // Configurar cache HTTP (10 MB)
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cache = okhttp3.Cache(context.cacheDir, cacheSize)
        
        // Interceptor para logar resposta DEPOIS da descompressão
        val rawResponseInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            // Logar resposta descomprimida
            val responseBody = response.body
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source?.buffer
            
            val responseString = buffer?.clone()?.readString(Charsets.UTF_8) ?: ""
            android.util.Log.d("NetworkModule", "═══ RESPONSE AFTER DECOMPRESSION ═══")
            android.util.Log.d("NetworkModule", "URL: ${request.url}")
            android.util.Log.d("NetworkModule", "Status: ${response.code}")
            android.util.Log.d("NetworkModule", "Content-Encoding: ${response.header("Content-Encoding")}")
            android.util.Log.d("NetworkModule", "Body length: ${responseString.length}")
            android.util.Log.d("NetworkModule", "Body (first 1000 chars): ${responseString.take(1000)}")
            android.util.Log.d("NetworkModule", "═══════════════════════════════════")
            
            response
        }
        
        return OkHttpClient.Builder()
            .cache(cache)  // Cache HTTP
            .addInterceptor(rawResponseInterceptor)  // Log da resposta
            .addInterceptor(deviceInfoInterceptor)  // Device info
            .addInterceptor(authInterceptor)  // Auth
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
        
        // Configurar Gson com adaptador para datas
        val gson = com.google.gson.GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(java.util.Date::class.java, com.google.gson.JsonDeserializer { json, _, _ ->
                try {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    dateFormat.parse(json.asString)
                } catch (e: Exception) {
                    null
                }
            })
            .create()
        
        return Retrofit.Builder()
            .baseUrl(finalBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
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
    
    /**
     * Fornece instância do PatrimonioApi
     */
    fun getPatrimonioApi(context: Context): com.inventario.mobile.data.remote.api.PatrimonioApi {
        val retrofit = createRetrofit(
            createOkHttpClient(
                context,
                createHttpLoggingInterceptor(),
                createAuthInterceptor(
                    LocalDataManager.getInstance(context),
                    com.inventario.mobile.utils.PreferencesManager(context)
                ),
                DeviceInfoInterceptor(context)
            ),
            ServerConfigManager.getInstance(context)
        )
        return retrofit.create(com.inventario.mobile.data.remote.api.PatrimonioApi::class.java)
    }
    
    /**
     * Fornece instância do ColetaApi
     */
    fun getColetaApi(context: Context): com.inventario.mobile.data.remote.api.ColetaApi {
        val retrofit = createRetrofit(
            createOkHttpClient(
                context,
                createHttpLoggingInterceptor(),
                createAuthInterceptor(
                    LocalDataManager.getInstance(context),
                    com.inventario.mobile.utils.PreferencesManager(context)
                ),
                DeviceInfoInterceptor(context)
            ),
            ServerConfigManager.getInstance(context)
        )
        return retrofit.create(com.inventario.mobile.data.remote.api.ColetaApi::class.java)
    }
    
    /**
     * Fornece instância do PatrimonioApi (legacy)
     */
    fun getPatrimonioApiLegacy(context: Context): com.inventario.mobile.api.PatrimonioApi {
        val retrofit = createRetrofit(
            createOkHttpClient(
                context,
                createHttpLoggingInterceptor(),
                createAuthInterceptor(
                    LocalDataManager.getInstance(context),
                    com.inventario.mobile.utils.PreferencesManager(context)
                ),
                DeviceInfoInterceptor(context)
            ),
            ServerConfigManager.getInstance(context)
        )
        return retrofit.create(com.inventario.mobile.api.PatrimonioApi::class.java)
    }
    
    /**
     * Fornece instância do SalaApi
     */
    fun getSalaApi(context: Context): com.inventario.mobile.api.SalaApi {
        val retrofit = createRetrofit(
            createOkHttpClient(
                context,
                createHttpLoggingInterceptor(),
                createAuthInterceptor(
                    LocalDataManager.getInstance(context),
                    com.inventario.mobile.utils.PreferencesManager(context)
                ),
                DeviceInfoInterceptor(context)
            ),
            ServerConfigManager.getInstance(context)
        )
        return retrofit.create(com.inventario.mobile.api.SalaApi::class.java)
    }
}