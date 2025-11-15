package com.inventario.mobile.presentation.state

import com.inventario.mobile.data.model.Coleta

/**
 * Estados da tela de visualização de coletas
 * 
 * NOTA: Usa data.model.Coleta para ter acesso aos dados completos
 * (numeroPatrimonio, descricaoPatrimonio, nomeUsuario, etc.)
 */
sealed class CollectionViewState {
    object Idle : CollectionViewState()
    object Loading : CollectionViewState()
    data class Success(
        val coletas: List<Coleta>,
        val filteredColetas: List<Coleta>,
        val salas: List<String>,
        val totalColetas: Int,
        val sincronizadas: Int,
        val pendentes: Int
    ) : CollectionViewState()
    data class Error(val message: String) : CollectionViewState()
}
