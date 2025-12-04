package com.inventario.mobile.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.utils.PreferencesManager

/**
 * Factory para criar ScannerViewModel com dependências
 * ✅ v2.8: Agora recebe BuscarPatrimonioUseCase para suporte OFFLINE
 * ✅ Recebe RegistrarColetaUseCase injetado via Hilt
 */
class ScannerViewModelFactory(
    private val inventarioRepository: InventarioRepository,
    private val preferencesManager: PreferencesManager,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase // ✅ NOVO: Use Case para busca offline
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            return ScannerViewModel(
                inventarioRepository = inventarioRepository,
                preferencesManager = preferencesManager,
                registrarColetaUseCase = registrarColetaUseCase,
                buscarPatrimonioUseCase = buscarPatrimonioUseCase // ✅ Use Case para busca offline
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
