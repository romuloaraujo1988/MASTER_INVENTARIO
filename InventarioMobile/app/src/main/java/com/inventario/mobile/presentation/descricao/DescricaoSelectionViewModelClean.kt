package com.inventario.mobile.presentation.descricao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarDescricoesNaoColetadasUseCase
import com.inventario.mobile.presentation.state.DescricaoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel refatorado para seleção de descrição
 * Segue Clean Architecture + MVVM
 * 
 * - Usa Use Cases (não acessa Repository diretamente)
 * - Gerencia estado da UI com StateFlow
 * - Injeção de dependência com Hilt
 */
@HiltViewModel
class DescricaoSelectionViewModelClean @Inject constructor(
    private val buscarDescricoesNaoColetadasUseCase: BuscarDescricoesNaoColetadasUseCase
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
            
            buscarDescricoesNaoColetadasUseCase().fold(
                onSuccess = { descricoes ->
                    _state.value = DescricaoState.Success(descricoes)
                },
                onFailure = { error ->
                    _state.value = DescricaoState.Error(
                        error.message ?: "Erro ao carregar descrições"
                    )
                }
            )
        }
    }
    
    /**
     * Limpa o estado (volta para Idle)
     */
    fun limparEstado() {
        _state.value = DescricaoState.Idle
    }
}
