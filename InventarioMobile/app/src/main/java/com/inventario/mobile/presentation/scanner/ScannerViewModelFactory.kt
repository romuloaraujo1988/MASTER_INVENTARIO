package com.inventario.mobile.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase

class ScannerViewModelFactory(
    private val repository: InventarioRepository,
    private val preferencesManager: PreferencesManager,
    private val registrarColetaUseCase: RegistrarColetaUseCase? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            return ScannerViewModel(repository, preferencesManager, registrarColetaUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
