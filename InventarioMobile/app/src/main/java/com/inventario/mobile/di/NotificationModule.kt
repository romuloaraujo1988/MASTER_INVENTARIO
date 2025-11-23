package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para notificações e monitoramento de rede
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideOfflineNotificationManager(
        @ApplicationContext context: Context
    ): OfflineNotificationManager {
        return OfflineNotificationManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor.getInstance(context)
    }
}
