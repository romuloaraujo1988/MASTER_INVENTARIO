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
                val stats = repository.getDashboardStats()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dashboardStats = stats
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

    fun loadColetasEvolucao() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true)
            
            try {
                // TODO: Implementar carregamento de dados do gráfico
                _uiState.value = _uiState.value.copy(
                    isLoadingGrafico = false
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
    val percentualConcluido: Float = 0f
)
