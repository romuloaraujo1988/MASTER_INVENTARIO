package com.inventario.mobile.presentation.state

import com.inventario.mobile.domain.model.Coleta

/**
 * Estados possíveis da tela de coleta
 */
sealed class ColetaState {
    object Idle : ColetaState()
    object Loading : ColetaState()
    data class Success(val coleta: Coleta) : ColetaState()
    data class Error(val message: String) : ColetaState()
}
