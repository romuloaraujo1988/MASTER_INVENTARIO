package com.inventario.mobile.di

import android.content.Context
import com.inventario.mobile.data.pdf.PdfGenerator
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
object PdfModule {

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
