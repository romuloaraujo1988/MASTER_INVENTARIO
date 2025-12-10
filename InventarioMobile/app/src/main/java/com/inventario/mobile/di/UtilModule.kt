package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.util.NetworkChecker
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para utilitários
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    @Provides
    @Singleton
    fun provideNetworkChecker(
        @ApplicationContext context: Context
    ): NetworkChecker {
        return NetworkChecker(context)
    }
    
    @Provides
    @Singleton
    fun provideVibrationHelper(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): VibrationHelper {
        return VibrationHelper(context, preferencesManager)
    }
    
    // NetworkMonitor agora é provido pelo NotificationModule
}
