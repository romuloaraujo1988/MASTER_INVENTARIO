package com.inventario.mobile.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.api.PatrimonioApi
import com.inventario.mobile.network.DeviceInfoInterceptor
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager
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
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    /**
     * Fornece instância do Retrofit
     */
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
     * Fornece instância do OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        deviceInfoInterceptor: DeviceInfoInterceptor
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024L
        val cache = okhttp3.Cache(context.cacheDir, cacheSize)
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(deviceInfoInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }
    
    /**
     * Fornece interceptor de logging
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * Fornece interceptor de autenticação
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context,
        localDataManager: LocalDataManager,
        preferencesManager: PreferencesManager
    ): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            var token: String? = null
            var isTokenValid = false
            
            try {
                val currentUser = kotlinx.coroutines.runBlocking {
                    localDataManager.getCurrentUser()
                }
                token = currentUser?.accessToken
                isTokenValid = currentUser?.isTokenValid() ?: false
            } catch (e: Exception) {
                // Fallback para PreferencesManager
            }
            
            if (token == null || !isTokenValid) {
                token = preferencesManager.getAccessToken()
                isTokenValid = preferencesManager.isTokenValid()
            }
            
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
    
    /**
     * Fornece interceptor de informações do dispositivo
     */
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
    
    /**
     * Provider para ApiService (legado)
     * NOTA: Mantido para compatibilidade com código legado.
     */
    @Provides
    @Singleton
    fun provideApiService(
        @ApplicationContext context: Context
    ): com.inventario.mobile.data.remote.api.ApiService {
        return NetworkModule.getApiService(context)
    }
}
