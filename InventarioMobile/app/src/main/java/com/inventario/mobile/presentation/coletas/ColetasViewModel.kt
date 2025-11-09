package com.inventario.mobile.presentation.coletas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.model.Patrimonio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ColetasUiState(
    val isLoading: Boolean = false,
    val patrimoniosColetados: List<Patrimonio> = emptyList(),
    val patrimoniosPendentes: List<Patrimonio> = emptyList(),
    val totalColetados: Int = 0,
    val totalPendentes: Int = 0,
    val errorMessage: String? = null
)

class ColetasViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColetasUiState())
    val uiState: StateFlow<ColetasUiState> = _uiState.asStateFlow()

    fun loadColetas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = Result.success(emptyList<com.inventario.mobile.data.model.Patrimonio>()) // TODO: Implementar getAllPatrimonios
                result.onSuccess { patrimonios ->
                    val coletados = patrimonios.filter { it.coletado == true }
                    val pendentes = patrimonios.filter { it.coletado != true }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        patrimoniosColetados = coletados,
                        patrimoniosPendentes = pendentes,
                        totalColetados = coletados.size,
                        totalPendentes = pendentes.size
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar coletas: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar coletas: ${e.message}"
                )
            }
        }
    }

    fun getColetasPorSala(): Map<String, List<Patrimonio>> {
        val coletados = _uiState.value.patrimoniosColetados
        return coletados.groupBy { it.salaNome ?: "Sala não definida" }
    }

    fun getPendentesPorSala(): Map<String, List<Patrimonio>> {
        val pendentes = _uiState.value.patrimoniosPendentes
        return pendentes.groupBy { it.salaNome ?: "Sala não definida" }
    }
}

class ColetasViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColetasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColetasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}