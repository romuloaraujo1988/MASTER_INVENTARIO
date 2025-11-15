package com.inventario.mobile.presentation.validation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para validação de patrimônios antes da coleta
 * Gerencia validações, verificações de duplicata e status de coleta
 */
@HiltViewModel
class ValidationViewModel @Inject constructor(
    private val validarPatrimonioUseCase: ValidarPatrimonioUseCase,
    private val verificarDuplicataColetaUseCase: VerificarDuplicataColetaUseCase,
    private val verificarSePatrimonioFoiColetadoUseCase: VerificarSePatrimonioFoiColetadoUseCase
) : ViewModel() {
    
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Idle)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()
    
    /**
     * Valida um patrimônio antes de coletar
     */
    fun validarPatrimonio(numeroPatrimonio: String) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            
            val result = validarPatrimonioUseCase(numeroPatrimonio)
            
            if (result.isSuccess) {
                when (val validationResult = result.getOrNull()) {
                    is ValidationResult.Valid -> {
                        _validationState.value = ValidationState.Valid(
                            data = validationResult.data,
                            jaColetado = validationResult.jaColetado
                        )
                    }
                    is ValidationResult.Invalid -> {
                        _validationState.value = ValidationState.Invalid(
                            motivo = validationResult.motivo,
                            mensagem = validationResult.mensagem
                        )
                    }
                    null -> {
                        _validationState.value = ValidationState.Error("Erro desconhecido")
                    }
                }
            } else {
                _validationState.value = ValidationState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao validar patrimônio"
                )
            }
        }
    }
    
    /**
     * Verifica se coleta seria duplicada
     */
    fun verificarDuplicata(numeroPatrimonio: String, inventarioId: Int? = null) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            
            val result = verificarDuplicataColetaUseCase(numeroPatrimonio, inventarioId)
            
            if (result.isSuccess) {
                when (val duplicataResult = result.getOrNull()) {
                    is DuplicataResult.PodeRegistrar -> {
                        _validationState.value = ValidationState.PodeRegistrar
                    }
                    is DuplicataResult.Duplicado -> {
                        _validationState.value = ValidationState.Duplicado(
                            mensagem = duplicataResult.mensagem,
                            coletaExistente = duplicataResult.coletaExistente
                        )
                    }
                    is DuplicataResult.NaoPodeRegistrar -> {
                        _validationState.value = ValidationState.NaoPodeRegistrar(
                            motivo = duplicataResult.motivo,
                            mensagem = duplicataResult.mensagem
                        )
                    }
                    null -> {
                        _validationState.value = ValidationState.Error("Erro desconhecido")
                    }
                }
            } else {
                _validationState.value = ValidationState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao verificar duplicata"
                )
            }
        }
    }
    
    /**
     * Verifica se patrimônio já foi coletado
     */
    fun verificarSeJaFoiColetado(numeroPatrimonio: String, inventarioId: Int? = null) {
        viewModelScope.launch {
            _validationState.value = ValidationState.Loading
            
            val result = verificarSePatrimonioFoiColetadoUseCase(numeroPatrimonio, inventarioId)
            
            if (result.isSuccess) {
                when (val coletaInfo = result.getOrNull()) {
                    is ColetaInfo.Coletado -> {
                        _validationState.value = ValidationState.JaColetado(coletaInfo)
                    }
                    is ColetaInfo.NaoColetado -> {
                        _validationState.value = ValidationState.AindaNaoColetado(coletaInfo)
                    }
                    null -> {
                        _validationState.value = ValidationState.Error("Erro desconhecido")
                    }
                }
            } else {
                _validationState.value = ValidationState.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao verificar coleta"
                )
            }
        }
    }
    
    /**
     * Limpa o estado de validação
     */
    fun clearState() {
        _validationState.value = ValidationState.Idle
    }
}

/**
 * Estados da validação de patrimônio
 */
sealed class ValidationState {
    object Idle : ValidationState()
    object Loading : ValidationState()
    
    // Estados de validação de patrimônio
    data class Valid(
        val data: Map<String, Any>,
        val jaColetado: Boolean
    ) : ValidationState()
    
    data class Invalid(
        val motivo: String,
        val mensagem: String
    ) : ValidationState()
    
    // Estados de verificação de duplicata
    object PodeRegistrar : ValidationState()
    
    data class Duplicado(
        val mensagem: String,
        val coletaExistente: Map<String, Any>?
    ) : ValidationState()
    
    data class NaoPodeRegistrar(
        val motivo: String,
        val mensagem: String
    ) : ValidationState()
    
    // Estados de verificação de coleta
    data class JaColetado(
        val info: ColetaInfo.Coletado
    ) : ValidationState()
    
    data class AindaNaoColetado(
        val info: ColetaInfo.NaoColetado
    ) : ValidationState()
    
    // Estado de erro
    data class Error(val message: String) : ValidationState()
}
