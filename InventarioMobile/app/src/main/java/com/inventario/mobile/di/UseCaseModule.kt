package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.repository.InventarioRepository
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
     * Usa InventarioRepository como implementação temporária de ColetaRepository e PatrimonioRepository
     * até que a migração completa para Clean Architecture seja concluída
     */
    @Provides
    @ActivityScoped
    fun provideRegistrarColetaUseCase(
        @ApplicationContext context: Context
    ): RegistrarColetaUseCase {
        // Obter dependências necessárias
        val localDataManager = LocalDataManager.getInstance(context)
        val apiService = com.inventario.mobile.di.NetworkModule.getApiService(context)
        val inventarioRepository = InventarioRepository.getInstance(context, apiService)
        
        // InventarioRepository implementa tanto ColetaRepository quanto PatrimonioRepository
        // (temporário até migração completa)
        return RegistrarColetaUseCase(
            coletaRepository = inventarioRepository as ColetaRepository,
            patrimonioRepository = inventarioRepository as PatrimonioRepository,
            localDataManager = localDataManager
        )
    }
}
