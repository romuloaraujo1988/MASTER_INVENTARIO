package com.inventario.mobile.presentation.descricao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarDescricoesNaoColetadasUseCase
import com.inventario.mobile.presentation.state.DescricaoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel refatorado para seleção de descrição
 * Segue Clean Architecture + MVVM
 * 
 * - Usa Use Cases (não acessa Repository diretamente)
 * - Gerencia estado da UI com StateFlow
 */
class DescricaoSelectionViewModelClean(
    private val buscarDescricoesNaoColetadasUseCase: BuscarDescricoesNaoColetadasUseCase?
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
            
            if (buscarDescricoesNaoColetadasUseCase != null) {
                buscarDescricoesNaoColetadasUseCase.invoke().fold(
                    onSuccess = { descricoes ->
                        _state.value = DescricaoState.Success(descricoes)
                    },
                    onFailure = { error ->
                        _state.value = DescricaoState.Error(
                            error.message ?: "Erro ao carregar descrições"
                        )
                    }
                )
            } else {
                // Implementação temporária sem Use Case
                _state.value = DescricaoState.Error("Funcionalidade em desenvolvimento")
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
