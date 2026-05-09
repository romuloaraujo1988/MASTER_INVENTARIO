package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.api.SalaApi
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.local.dao.SalaDao
import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.domain.repository.SalaRepository
import com.inventario.mobile.domain.usecase.BuscarEstatisticasSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosPorSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarSalasComProgressoUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.domain.usecase.SincronizarSalasUseCase
import com.inventario.mobile.utils.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

/**
 * Módulo Hilt para prover Use Cases
 * 
 * ✅ Fornece RegistrarColetaUseCase com todas as dependências necessárias
 * ✅ Fornece Use Cases para Inventário por Sala
 */
@Module
@InstallIn(ActivityComponent::class)
object UseCaseModule {
    
    /**
     * Fornece RegistrarColetaUseCase
     * 
     * Usa os repositórios injetados pelo Hilt (ColetaRepositoryImpl e PatrimonioRepositoryAdapter)
     */
    @Provides
    @ActivityScoped
    fun provideRegistrarColetaUseCase(
        coletaRepository: ColetaRepository,
        patrimonioRepository: PatrimonioRepository,
        @ApplicationContext context: Context
    ): RegistrarColetaUseCase {
        val localDataManager = LocalDataManager.getInstance(context)
        
        return RegistrarColetaUseCase(
            coletaRepository = coletaRepository,
            patrimonioRepository = patrimonioRepository,
            localDataManager = localDataManager
        )
    }
    
    // ========================================
    // Use Cases para Inventário por Sala
    // ========================================
    
    /**
     * Fornece BuscarPatrimoniosPorSalaUseCase
     */
    @Provides
    @ActivityScoped
    fun provideBuscarPatrimoniosPorSalaUseCase(
        patrimonioRepository: PatrimonioRepositoryImpl,
        patrimonioApi: com.inventario.mobile.api.PatrimonioApi
    ): BuscarPatrimoniosPorSalaUseCase {
        return BuscarPatrimoniosPorSalaUseCase(patrimonioRepository, patrimonioApi)
    }
    
    /**
     * Fornece BuscarEstatisticasSalaUseCase
     */
    @Provides
    @ActivityScoped
    fun provideBuscarEstatisticasSalaUseCase(
        patrimonioRepository: PatrimonioRepositoryImpl,
        coletaRepository: ColetaRepository
    ): BuscarEstatisticasSalaUseCase {
        return BuscarEstatisticasSalaUseCase(patrimonioRepository, coletaRepository)
    }
    
    /**
     * Fornece BuscarSalasComProgressoUseCase
     */
    @Provides
    @ActivityScoped
    fun provideBuscarSalasComProgressoUseCase(
        salaRepository: SalaRepository
    ): BuscarSalasComProgressoUseCase {
        return BuscarSalasComProgressoUseCase(salaRepository)
    }
    
    /**
     * Fornece SincronizarSalasUseCase para sincronização incremental de salas
     */
    @Provides
    @ActivityScoped
    fun provideSincronizarSalasUseCase(
        salaApi: SalaApi,
        salaDao: SalaDao,
        @ApplicationContext context: Context
    ): SincronizarSalasUseCase {
        val preferencesManager = PreferencesManager(context)
        return SincronizarSalasUseCase(salaApi, salaDao, preferencesManager)
    }
}
