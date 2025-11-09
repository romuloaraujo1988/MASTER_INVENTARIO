package com.inventario.mobile.presentation.sala

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory stub - Mantido para compatibilidade
 * TODO: Remover após migração completa para Hilt
 */
class SalaSelectionViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalaSelectionViewModel::class.java)) {
            return SalaSelectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
