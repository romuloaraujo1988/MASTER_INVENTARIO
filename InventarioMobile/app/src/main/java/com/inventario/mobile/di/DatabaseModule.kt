package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.local.dao.*
import com.inventario.mobile.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para configuração do banco de dados Room
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun providePatrimonioDao(database: AppDatabase): PatrimonioDao {
        return database.patrimonioDao()
    }
    
    @Provides
    fun provideSalaDao(database: AppDatabase): SalaDao {
        return database.salaDao()
    }
    
    @Provides
    fun provideResponsavelDao(database: AppDatabase): ResponsavelDao {
        return database.responsavelDao()
    }
    
    @Provides
    fun provideColetaDao(database: AppDatabase): ColetaDao {
        return database.coletaDao()
    }
}
