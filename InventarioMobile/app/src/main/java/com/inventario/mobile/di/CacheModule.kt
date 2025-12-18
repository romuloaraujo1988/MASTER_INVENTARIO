package com.inventario.mobile.di

import com.inventario.mobile.data.cache.SearchCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências de cache
 * 
 * Fornece instâncias singleton de caches para toda a aplicação
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    /**
     * Fornece instância singleton do SearchCache
     * 
     * @return SearchCache com tamanho máximo de 50 entradas
     */
    @Provides
    @Singleton
    fun provideSearchCache(): SearchCache {
        return SearchCache(maxSize = 50)
    }
}
