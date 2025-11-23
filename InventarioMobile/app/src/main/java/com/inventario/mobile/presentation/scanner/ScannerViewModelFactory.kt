package com.inventario.mobile.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.utils.PreferencesManager

/**
 * Factory para criar ScannerViewModel com dependências
 */
/**
 * Factory para criar ScannerViewModel com dependências
 * ✅ Agora recebe RegistrarColetaUseCase injetado via Hilt
 */
class ScannerViewModelFactory(
    private val inventarioRepository: InventarioRepository,
    private val preferencesManager: PreferencesManager,
    private val registrarColetaUseCase: RegistrarColetaUseCase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            return ScannerViewModel(
                inventarioRepository = inventarioRepository,
                preferencesManager = preferencesManager,
                registrarColetaUseCase = registrarColetaUseCase // ✅ Use Case injetado
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
