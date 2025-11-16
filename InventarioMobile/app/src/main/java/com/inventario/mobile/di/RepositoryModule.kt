package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.repository.ColetaRepositoryImpl
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.domain.repository.ColetaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para binding de Repositories
 * Conecta interfaces (Domain) com implementações (Data)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPatrimonioRepository(
        adapter: com.inventario.mobile.data.repository.PatrimonioRepositoryAdapter
    ): com.inventario.mobile.domain.repository.PatrimonioRepository
    
    @Binds
    @Singleton
    abstract fun bindColetaRepository(
        impl: ColetaRepositoryImpl
    ): ColetaRepository
    
    @Binds
    @Singleton
    abstract fun bindSalaRepository(
        impl: com.inventario.mobile.data.repository.SalaRepositoryImpl
    ): com.inventario.mobile.domain.repository.SalaRepository
    
    @Binds
    @Singleton
    abstract fun bindPatrimonioConsultaRepository(
        impl: com.inventario.mobile.data.repository.PatrimonioConsultaRepositoryImpl
    ): com.inventario.mobile.domain.repository.PatrimonioConsultaRepository
    
    companion object {
        /**
         * Provider para InventarioRepository (stub temporário)
         * NOTA: Este é mantido para compatibilidade com código legado.
         * Novas funcionalidades devem usar os repositórios Clean Architecture específicos.
         */
        @Provides
        @Singleton
        fun provideInventarioRepository(
            @ApplicationContext context: Context,
            apiService: ApiService
        ): InventarioRepository {
            val localDataManager = LocalDataManager.getInstance(context)
            return InventarioRepository(apiService, localDataManager, context)
        }
    }
}
