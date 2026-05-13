package com.inventario.mobile.di

import com.inventario.mobile.sync.SyncManager
import com.inventario.mobile.sync.SyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover dependências de sincronização.
 *
 * C2/F1 (auditoria-sync — maio/2026): [SyncManager] foi convertido em fachada
 * que delega para [SyncScheduler]. O Hilt constrói `SyncManager` a partir
 * de `SyncScheduler` (ambos `@Singleton`), garantindo que todo o pipeline
 * de sincronização use um único worker (`ColetaSyncWorker`).
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncManager(syncScheduler: SyncScheduler): SyncManager {
        val manager = SyncManager(syncScheduler)
        // Registra no singleton legado para que `SyncManager.getInstance(context)`
        // continue funcionando sem injeção direta.
        SyncManager.setInstance(manager)
        return manager
    }
}
