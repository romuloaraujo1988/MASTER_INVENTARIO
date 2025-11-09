package com.inventario.mobile.presentation.descricao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.presentation.state.DescricaoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel refatorado para seleção de descrição
 * Implementação direta com Repository
 */
class DescricaoSelectionViewModelClean(
    private val repository: InventarioRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<DescricaoState>(DescricaoState.Idle)
    val state: StateFlow<DescricaoState> = _state.asStateFlow()
    
    /**
     * Carrega descrições de patrimônios não coletados
     * Apenas itens pendentes aparecem (facilita coleta)
     */
    fun carregarDescricoes() {
        viewModelScope.launch {
            _state.value = DescricaoState.Loading
            
            try {
                val descricoes = repository.buscarDescricoesNaoColetadas()
                _state.value = DescricaoState.Success(descricoes)
            } catch (e: Exception) {
                _state.value = DescricaoState.Error(
                    e.message ?: "Erro ao carregar descrições"
                )
            }
        }
    }
    
    /**
     * Limpa o estado (volta para Idle)
     */
    fun limparEstado() {
        _state.value = DescricaoState.Idle
    }
}
