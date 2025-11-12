package com.inventario.mobile.di

import com.inventario.mobile.data.repository.ColetaRepositoryImpl
import com.inventario.mobile.domain.repository.ColetaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}
