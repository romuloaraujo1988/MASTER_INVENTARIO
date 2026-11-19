package com.inventario.mobile.di

import com.inventario.mobile.data.local.dao.PatrimonioDao
import com.inventario.mobile.data.mapper.ColetaMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para Mappers
 */
@Module
@InstallIn(SingletonComponent::class)
object MapperModule {
    
    @Provides
    @Singleton
    fun provideColetaMapper(
        patrimonioDao: PatrimonioDao,
        preferencesManager: com.inventario.mobile.utils.PreferencesManager
    ): ColetaMapper {
        return ColetaMapper(patrimonioDao, preferencesManager)
    }
    
    @Provides
    @Singleton
    fun providePatrimonioConsultaMapper(): com.inventario.mobile.data.mapper.PatrimonioConsultaMapper {
        return com.inventario.mobile.data.mapper.PatrimonioConsultaMapper()
    }
    
    @Provides
    @Singleton
    fun providePatrimonioDetalheMapper(): com.inventario.mobile.data.mapper.PatrimonioDetalheMapper {
        return com.inventario.mobile.data.mapper.PatrimonioDetalheMapper()
    }
}
