package com.inventario.mobile.presentation.filtros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Responsavel
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FiltrosUiState(
    val isLoading: Boolean = false,
    val responsaveis: List<Responsavel> = emptyList(),
    val errorMessage: String? = null
)

class FiltrosViewModel(
    private val repository: InventarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FiltrosUiState())
    val uiState: StateFlow<FiltrosUiState> = _uiState.asStateFlow()
    
    fun loadResponsaveis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = repository.getResponsaveis()
                result.fold(
                    onSuccess = { responsaveis ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            responsaveis = responsaveis
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao carregar responsáveis: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar responsáveis: ${e.message}"
                )
            }
        }
    }
}