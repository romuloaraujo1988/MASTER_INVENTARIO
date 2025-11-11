package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.remote.api.ColetaApi
import com.inventario.mobile.data.remote.api.PatrimonioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para APIs Retrofit
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun providePatrimonioApi(
        @ApplicationContext context: Context
    ): PatrimonioApi {
        return NetworkModule.getPatrimonioApi(context)
    }
    
    @Provides
    @Singleton
    fun provideColetaApi(
        @ApplicationContext context: Context
    ): ColetaApi {
        return NetworkModule.getColetaApi(context)
    }
    
    @Provides
    @Singleton
    fun providePatrimonioApiLegacy(
        @ApplicationContext context: Context
    ): com.inventario.mobile.api.PatrimonioApi {
        return NetworkModule.getPatrimonioApiLegacy(context)
    }
    
    @Provides
    @Singleton
    fun provideSalaApi(
        @ApplicationContext context: Context
    ): com.inventario.mobile.api.SalaApi {
        return NetworkModule.getSalaApi(context)
    }
}
