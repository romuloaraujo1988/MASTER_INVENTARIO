package com.inventario.mobile.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.model.EvolucaoColeta
import com.inventario.mobile.domain.usecase.BuscarEstatisticasDashboardUseCase
import com.inventario.mobile.domain.usecase.BuscarEvolucaoColetasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Clean para Dashboard
 * Usa Clean Architecture com Use Cases
 * Injetado via Hilt
 */
@HiltViewModel
class DashboardViewModelClean @Inject constructor(
    private val buscarEstatisticasDashboardUseCase: BuscarEstatisticasDashboardUseCase,
    private val buscarEvolucaoColetasUseCase: BuscarEvolucaoColetasUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "DashboardViewModelClean"
    }
    
    private val _uiState = MutableStateFlow(DashboardUiStateClean())
    val uiState: StateFlow<DashboardUiStateClean> = _uiState.asStateFlow()
    
    /**
     * Carrega dados do dashboard
     */
    fun loadDashboardData(inventarioId: Int? = null) {
        viewModelScope.launch {
            Log.d(TAG, "Carregando dados do dashboard...")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            buscarEstatisticasDashboardUseCase(inventarioId).fold(
                onSuccess = { stats ->
                    Log.d(TAG, "Estatísticas carregadas: ${stats.percentualConclusao}%")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboardStats = stats,
                        error = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao carregar estatísticas", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    
    /**
     * Carrega evolução de coletas
     */
    fun loadColetasEvolucao(inventarioId: Int? = null, dias: Int = 30) {
        viewModelScope.launch {
            Log.d(TAG, "Carregando evolução de coletas (últimos $dias dias)...")
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true, graficoError = null)
            
            buscarEvolucaoColetasUseCase(inventarioId, dias).fold(
                onSuccess = { evolucao ->
                    Log.d(TAG, "Evolução carregada: ${evolucao.size} dias")
                    _uiState.value = _uiState.value.copy(
                        isLoadingGrafico = false,
                        coletasEvolucao = evolucao,
                        graficoError = null
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao carregar evolução", error)
                    _uiState.value = _uiState.value.copy(
                        isLoadingGrafico = false,
                        graficoError = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Atualiza todos os dados
     */
    fun refreshData(inventarioId: Int? = null) {
        loadDashboardData(inventarioId)
        loadColetasEvolucao(inventarioId)
    }
}

/**
 * Estado da UI do Dashboard (Clean)
 */
data class DashboardUiStateClean(
    val isLoading: Boolean = false,
    val isLoadingGrafico: Boolean = false,
    val dashboardStats: DashboardStats? = null,
    val coletasEvolucao: List<EvolucaoColeta> = emptyList(),
    val error: String? = null,
    val graficoError: String? = null
)
