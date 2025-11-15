package com.inventario.mobile.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para Dashboard
 * TODO: Migrar para Clean Architecture com Use Cases
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventarioRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = repository.getDashboardStats()
                result.fold(
                    onSuccess = { repoStats ->
                        // Converter DashboardStats do repository para DashboardStats da UI
                        val stats = DashboardStats(
                            patrimoniosColetados = repoStats.coletados,
                            patrimoniosPendentes = repoStats.naoColetados,
                            totalPatrimonios = repoStats.totalPatrimonios,
                            percentualConcluido = repoStats.percentualColetado.toFloat(),
                            percentualConclusao = repoStats.percentualColetado.toFloat(),
                            coletoresAtivos = repoStats.coletoresAtivos,
                            divergencias = repoStats.divergencias,
                            valorTotal = repoStats.valorTotal
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dashboardStats = stats
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    fun loadColetasEvolucao(dias: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true)
            
            try {
                val result = repository.getColetasEvolucao(dias)
                result.fold(
                    onSuccess = { evolucao ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingGrafico = false,
                            coletasEvolucao = evolucao,
                            graficoError = null
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingGrafico = false,
                            graficoError = e.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingGrafico = false,
                    graficoError = e.message
                )
            }
        }
    }
}

/**
 * Estado da UI do Dashboard
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isLoadingGrafico: Boolean = false,
    val dashboardStats: DashboardStats? = null,
    val coletasEvolucao: List<com.inventario.mobile.data.remote.dto.ColetasPorDiaDto> = emptyList(),
    val error: String? = null,
    val graficoError: String? = null
)

/**
 * Estatísticas do Dashboard
 */
data class DashboardStats(
    val patrimoniosColetados: Int = 0,
    val patrimoniosPendentes: Int = 0,
    val divergencias: Int = 0,
    val coletoresAtivos: Int = 0,
    val totalPatrimonios: Int = 0,
    val percentualConcluido: Float = 0f,
    val percentualConclusao: Float = 0f,
    val valorTotal: Double = 0.0
)
