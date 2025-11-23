package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
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
}
