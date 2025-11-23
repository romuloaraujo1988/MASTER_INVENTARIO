package com.inventario.mobile.di

import com.inventario.mobile.data.mapper.DashboardMapper
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.data.repository.DashboardRepositoryImpl
import com.inventario.mobile.domain.repository.DashboardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para Dashboard
 * Configura injeção de dependências
 */
@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {
    
    /**
     * Provê DashboardMapper
     */
    @Provides
    @Singleton
    fun provideDashboardMapper(): DashboardMapper {
        return DashboardMapper()
    }
    
    /**
     * Provê implementação do DashboardRepository
     * v2.4: Adicionado dashboardDao para Room Flow reativo
     * v2.5: Adicionado preferencesManager para inventário ativo
     */
    @Provides
    @Singleton
    fun provideDashboardRepository(
        apiService: ApiService,
        mapper: DashboardMapper,
        dashboardDao: com.inventario.mobile.data.local.dao.DashboardDao,
        preferencesManager: com.inventario.mobile.utils.PreferencesManager
    ): DashboardRepository {
        return DashboardRepositoryImpl(apiService, mapper, dashboardDao, preferencesManager)
    }
}
