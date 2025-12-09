package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.pdf.PdfGenerator
import com.inventario.mobile.data.repository.PdfRepositoryImpl
import com.inventario.mobile.domain.repository.PdfRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências relacionadas a PDF
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PdfModule {
    
    /**
     * Bind PdfRepository para PdfRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindPdfRepository(
        impl: PdfRepositoryImpl
    ): PdfRepository
    
    companion object {
        /**
         * Provê instância do PdfGenerator
         */
        @Provides
        @Singleton
        fun providePdfGenerator(
            @ApplicationContext context: Context
        ): PdfGenerator {
            return PdfGenerator(context)
        }
    }
}
