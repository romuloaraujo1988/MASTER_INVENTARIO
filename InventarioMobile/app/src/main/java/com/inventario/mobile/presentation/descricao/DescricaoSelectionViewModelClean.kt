package com.inventario.mobile.presentation.descricao

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.usecase.BuscarDescricoesNaoColetadasUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosPorDescricaoUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaPorDescricaoUseCase
import com.inventario.mobile.presentation.state.DescricaoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados para busca de patrimônios por descrição
 */
sealed class PatrimoniosState {
    object Idle : PatrimoniosState()
    object Loading : PatrimoniosState()
    data class Found(val patrimonios: List<Patrimonio>, val descricao: String) : PatrimoniosState()
    data class Error(val message: String) : PatrimoniosState()
}

/**
 * Estados para registro de coleta
 */
sealed class ColetaState {
    object Idle : ColetaState()
    object Loading : ColetaState()
    data class Success(val patrimonio: Patrimonio) : ColetaState()
    data class Error(val message: String) : ColetaState()
}

/**
 * ViewModel refatorado para seleção de descrição
 * Clean Architecture + MVVM + Hilt
 * 
 * - Usa Use Case para lógica de negócio
 * - Gerencia estado da UI
 * - Injetado via Hilt
 */
@HiltViewModel
class DescricaoSelectionViewModelClean @Inject constructor(
    private val buscarDescricoesNaoColetadasUseCase: BuscarDescricoesNaoColetadasUseCase,
    private val buscarPatrimoniosPorDescricaoUseCase: BuscarPatrimoniosPorDescricaoUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val registrarColetaPorDescricaoUseCase: RegistrarColetaPorDescricaoUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "DescricaoSelectionVM"
    }
    
    private val _state = MutableStateFlow<DescricaoState>(DescricaoState.Idle)
    val state: StateFlow<DescricaoState> = _state.asStateFlow()
    
    private val _patrimoniosState = MutableStateFlow<PatrimoniosState>(PatrimoniosState.Idle)
    val patrimoniosState: StateFlow<PatrimoniosState> = _patrimoniosState.asStateFlow()
    
    private val _coletaState = MutableStateFlow<ColetaState>(ColetaState.Idle)
    val coletaState: StateFlow<ColetaState> = _coletaState.asStateFlow()
    
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
     * Busca patrimônios não coletados por descrição
     */
    fun buscarPatrimoniosPorDescricao(descricao: String) {
        viewModelScope.launch {
            _patrimoniosState.value = PatrimoniosState.Loading
            Log.d(TAG, "Buscando patrimônios com descrição: $descricao")
            
            buscarPatrimoniosPorDescricaoUseCase(descricao).fold(
                onSuccess = { patrimonios ->
                    Log.d(TAG, "Encontrados ${patrimonios.size} patrimônios")
                    _patrimoniosState.value = PatrimoniosState.Found(patrimonios, descricao)
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao buscar patrimônios", error)
                    _patrimoniosState.value = PatrimoniosState.Error(
                        error.message ?: "Erro ao buscar patrimônios"
                    )
                }
            )
        }
    }
    
    /**
     * Registra coleta de um patrimônio
     */
    fun registrarColeta(
        patrimonio: Patrimonio,
        salaId: Int,
        salaNome: String,
        estadoConservacao: String
    ) {
        viewModelScope.launch {
            _coletaState.value = ColetaState.Loading
            Log.d(TAG, "Registrando coleta: ${patrimonio.numeroPatrimonio}, sala=$salaId, estado=$estadoConservacao")
            
            registrarColetaUseCase(
                numeroPatrimonio = patrimonio.numeroPatrimonio,
                salaId = salaId,
                localizacaoAtual = salaNome,
                estadoEncontrado = estadoConservacao,
                observacoes = "Coleta por descrição"
            ).fold(
                onSuccess = { coleta ->
                    Log.d(TAG, "✓ Coleta registrada com sucesso")
                    _coletaState.value = ColetaState.Success(patrimonio)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao registrar coleta", error)
                    _coletaState.value = ColetaState.Error(
                        error.message ?: "Erro ao registrar coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Registra coleta por descrição (sem número de patrimônio)
     * O número do patrimônio será null e a descrição será salva
     */
    fun registrarColetaPorDescricao(
        descricao: String,
        salaId: Int,
        salaNome: String,
        estadoConservacao: String
    ) {
        viewModelScope.launch {
            _coletaState.value = ColetaState.Loading
            Log.d(TAG, "Registrando coleta por descrição: '$descricao', sala=$salaId, estado=$estadoConservacao")
            
            registrarColetaPorDescricaoUseCase(
                descricao = descricao,
                salaId = salaId,
                localizacaoAtual = salaNome,
                estadoEncontrado = estadoConservacao
            ).fold(
                onSuccess = { coleta ->
                    Log.d(TAG, "✓ Coleta por descrição registrada com sucesso")
                    // Criar patrimônio fake para mostrar sucesso
                    val patrimonioFake = Patrimonio(
                        id = 0,
                        numeroPatrimonio = "SEM ETIQUETA",
                        descricao = descricao,
                        estado = estadoConservacao,
                        setorId = 0,
                        salaId = salaId.toLong(),
                        qrCode = ""
                    )
                    _coletaState.value = ColetaState.Success(patrimonioFake)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao registrar coleta por descrição", error)
                    _coletaState.value = ColetaState.Error(
                        error.message ?: "Erro ao registrar coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Limpa o estado de descrições (volta para Idle)
     */
    fun limparEstado() {
        _state.value = DescricaoState.Idle
    }
    
    /**
     * Limpa o estado de patrimônios
     */
    fun limparPatrimoniosState() {
        _patrimoniosState.value = PatrimoniosState.Idle
    }
    
    /**
     * Limpa o estado de coleta
     */
    fun limparColetaState() {
        _coletaState.value = ColetaState.Idle
    }
}
