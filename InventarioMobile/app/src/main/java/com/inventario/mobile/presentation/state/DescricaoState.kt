package com.inventario.mobile.presentation.state

/**
 * Estados possíveis da tela de seleção de descrição
 */
sealed class DescricaoState {
    object Idle : DescricaoState()
    object Loading : DescricaoState()
    data class Success(val descricoes: List<String>) : DescricaoState()
    data class Error(val message: String) : DescricaoState()
}
