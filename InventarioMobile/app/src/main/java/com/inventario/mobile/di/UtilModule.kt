package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.utils.NetworkChecker
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para utilitários.
 *
 * AL-07 (correcoes-seguranca — maio/2026): removido `providePhotoHelper` que
 * duplicava o binding do `@Singleton class PhotoHelper @Inject constructor`.
 * A classe já se auto-provê via constructor injection com `@ApplicationContext`,
 * dispensando entry aqui.
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

    // NetworkMonitor é provido pelo NotificationModule.
    // PhotoHelper é provido por constructor injection (@Singleton + @Inject).
}
