package com.inventario.mobile.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.data.remote.api.RelatorioFotoApi
import com.inventario.mobile.network.DeviceInfoInterceptor
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager
import com.inventario.mobile.utils.OfflineNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo Hilt para APIs Retrofit
 * 
 * NOTA: Se o IP do servidor mudar, o app precisa ser reiniciado
 * para que as novas configurações sejam aplicadas.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): Retrofit {
        val serverConfigManager = ServerConfigManager.getInstance(context)
        val baseUrl = serverConfigManager.getBaseUrl()
        val finalBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(java.util.Date::class.java, com.google.gson.JsonDeserializer { json, _, _ ->
                try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(json.asString)
                } catch (e: Exception) { null }
            })
            .create()
        
        return Retrofit.Builder()
            .baseUrl(finalBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        deviceInfoInterceptor: DeviceInfoInterceptor,
        offlineFallbackInterceptor: com.inventario.mobile.network.OfflineFallbackInterceptor
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024L
        val cache = okhttp3.Cache(context.cacheDir, cacheSize)
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineFallbackInterceptor)
            .addInterceptor(deviceInfoInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // TIMEOUTS AJUSTADOS PARA REDES MÓVEIS INSTÁVEIS (WiFi/4G)
            .connectTimeout(30, TimeUnit.SECONDS)   // 30s para conectar (era 10s)
            .readTimeout(60, TimeUnit.SECONDS)      // 60s para ler dados (era 15s)
            .writeTimeout(60, TimeUnit.SECONDS)     // 60s para escrever (era 15s)
            .callTimeout(90, TimeUnit.SECONDS)      // 90s timeout total (era 30s)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            // Pool de conexões: 5 conexões, mantidas por 5 minutos
            .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOfflineFallbackInterceptor(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager,
        offlineNotificationManager: OfflineNotificationManager
    ): com.inventario.mobile.network.OfflineFallbackInterceptor {
        return com.inventario.mobile.network.OfflineFallbackInterceptor(context, preferencesManager, offlineNotificationManager)
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        preferencesManager: PreferencesManager
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = preferencesManager.getAccessToken()
            val isTokenValid = preferencesManager.isTokenValid()
            
            val newRequest = if (token != null && isTokenValid) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
            }
            chain.proceed(newRequest)
        }
    }
    
    @Provides
    @Singleton
    fun provideDeviceInfoInterceptor(
        @ApplicationContext context: Context
    ): DeviceInfoInterceptor {
        return DeviceInfoInterceptor(context)
    }
    
    @Provides
    @Singleton
    fun providePatrimonioApi(retrofit: Retrofit): PatrimonioApi {
        return retrofit.create(PatrimonioApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideColetaApi(retrofit: Retrofit): ColetaApi {
        return retrofit.create(ColetaApi::class.java)
    }

    /**
     * BUGFIX F6 (07/05/2026): FotoColetaApi injetado para habilitar upload real
     * de fotos de coleta no `PhotoSyncWorker`, corrigindo perda silenciosa.
     */
    @Provides
    @Singleton
    fun provideFotoColetaApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.FotoColetaApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.FotoColetaApi::class.java)
    }
    
    @Provides
    @Singleton
    fun providePatrimonioApiLegacy(retrofit: Retrofit): com.inventario.mobile.api.PatrimonioApi {
        return retrofit.create(com.inventario.mobile.api.PatrimonioApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideSalaApi(retrofit: Retrofit): com.inventario.mobile.api.SalaApi {
        return retrofit.create(com.inventario.mobile.api.SalaApi::class.java)
    }
    
    @Provides
    @Singleton
    fun providePatrimonioConsultaApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.PatrimonioConsultaApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.PatrimonioConsultaApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.SyncApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.SyncApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideOfflineSyncApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.OfflineSyncApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.OfflineSyncApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideApiService(
        @ApplicationContext context: Context
    ): com.inventario.mobile.data.remote.api.ApiService {
        return NetworkModule.getApiService(context)
    }
    
    @Provides
    @Singleton
    fun provideFotoReferenciaApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.FotoReferenciaApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.FotoReferenciaApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): com.inventario.mobile.data.remote.api.AuthApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.AuthApi::class.java)
    }

    /**
     * Provider da API Retrofit de sugestões de descrição.
     *
     * Feature: coleta-descricao-livre-com-sugestao (tarefa 14.2).
     * Endpoint: `GET /api/mobile/descricoes/sugestoes` (Req 5.1).
     *
     * Usa o mesmo `Retrofit` já configurado (mesma baseUrl, mesmo OkHttp com
     * interceptors de auth/offline/device), garantindo que a chamada herde a
     * matriz de segurança `@RequireColetor` e o fallback offline existente.
     */
    @Provides
    @Singleton
    fun provideDescricaoSugestaoApi(
        retrofit: Retrofit
    ): com.inventario.mobile.data.remote.api.DescricaoSugestaoApi {
        return retrofit.create(com.inventario.mobile.data.remote.api.DescricaoSugestaoApi::class.java)
    }

    /**
     * Provider da API Retrofit de Relatório Fotográfico.
     *
     * Feature: relatorio-fotografico-sem-etiqueta (tarefa 1.2).
     * Endpoints:
     *   - `GET api/mobile/relatorios/fotos/{inventarioId}/info` (Req 4.1)
     *   - `GET api/mobile/relatorios/fotos/{inventarioId}?tipo=sem_etiqueta` (Req 4.2)
     *
     * Usa o mesmo `Retrofit` já configurado, herdando os interceptors de
     * autenticação JWT, device info e fallback offline (Req 4.3).
     */
    @Provides
    @Singleton
    fun provideRelatorioFotoApi(retrofit: Retrofit): RelatorioFotoApi {
        return retrofit.create(RelatorioFotoApi::class.java)
    }
}
