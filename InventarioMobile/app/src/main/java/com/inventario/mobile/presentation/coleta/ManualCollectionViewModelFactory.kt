package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.utils.VibrationHelper

/**
 * Factory para ManualCollectionViewModel
 * NOTA: Este factory é mantido para compatibilidade com código legado.
 * Para novas implementações, use @HiltViewModel com injeção automática.
 * v2.10: Adicionado VibrationHelper para feedback tátil
 */
class ManualCollectionViewModelFactory(
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val repository: InventarioRepository,
    private val coletaDao: com.inventario.mobile.data.local.dao.ColetaDao,
    private val vibrationHelper: VibrationHelper // v2.10: Feedback tátil
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManualCollectionViewModel::class.java)) {
            return ManualCollectionViewModel(
                buscarPatrimonioUseCase,
                registrarColetaUseCase,
                repository,
                coletaDao,
                vibrationHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
