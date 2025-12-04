package com.inventario.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.ObterDetalhePatrimonioUseCase
import com.inventario.mobile.presentation.state.PatrimonioDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de detalhes do patrimônio
 * 
 * Regra: Gerencia estado da UI e chama Use Cases
 * 
 * @see Requirements 2.2, 2.3, 2.4
 */
@HiltViewModel
class PatrimonioDetailViewModel @Inject constructor(
    private val obterDetalhePatrimonioUseCase: ObterDetalhePatrimonioUseCase
) : ViewModel() {
    
    // Estado dos detalhes
    private val _state = MutableStateFlow<PatrimonioDetailState>(PatrimonioDetailState.Loading)
    val state: StateFlow<PatrimonioDetailState> = _state.asStateFlow()
    
    /**
     * Carrega detalhes do patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     */
    fun carregarDetalhes(patrimonioId: Int) {
        viewModelScope.launch {
            _state.value = PatrimonioDetailState.Loading
            
            obterDetalhePatrimonioUseCase(patrimonioId).fold(
                onSuccess = { detalhe ->
                    _state.value = PatrimonioDetailState.Success(detalhe)
                },
                onFailure = { error ->
                    _state.value = PatrimonioDetailState.Error(
                        error.message ?: "Erro ao carregar detalhes"
                    )
                }
            )
        }
    }
    
    /**
     * Recarrega os detalhes do patrimônio atual
     */
    fun recarregar() {
        val currentState = _state.value
        if (currentState is PatrimonioDetailState.Success) {
            carregarDetalhes(currentState.patrimonio.id)
        }
    }
    
    /**
     * Verifica se o patrimônio foi coletado
     */
    fun isColetado(): Boolean {
        val currentState = _state.value
        return if (currentState is PatrimonioDetailState.Success) {
            currentState.patrimonio.coletado
        } else {
            false
        }
    }
    
    /**
     * Verifica se o patrimônio tem divergências
     */
    fun temDivergencias(): Boolean {
        val currentState = _state.value
        return if (currentState is PatrimonioDetailState.Success) {
            currentState.patrimonio.temDivergencias()
        } else {
            false
        }
    }
    
    /**
     * Retorna o ID do patrimônio atual
     */
    fun getPatrimonioId(): Int? {
        val currentState = _state.value
        return if (currentState is PatrimonioDetailState.Success) {
            currentState.patrimonio.id
        } else {
            null
        }
    }
    
    /**
     * Retorna o número do patrimônio atual
     */
    fun getNumeroPatrimonio(): String? {
        val currentState = _state.value
        return if (currentState is PatrimonioDetailState.Success) {
            currentState.patrimonio.codigo
        } else {
            null
        }
    }
}
