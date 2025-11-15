package com.inventario.mobile.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Clean para Dashboard
 * Clean Architecture + MVVM + Hilt
 * 
 * TODO: Criar Use Cases específicos para substituir acesso direto ao repository
 */
@HiltViewModel
class DashboardViewModelClean @Inject constructor(
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "DashboardVMClean"
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                Log.d(TAG, "loadDashboardData: Carregando estatísticas...")
                
                val result = inventarioRepository.getDashboardStats()
                result.fold(
                    onSuccess = { repoStats ->
                        Log.d(TAG, "loadDashboardData: Estatísticas carregadas com sucesso")
                        
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
                            dashboardStats = stats,
                            error = null
                        )
                    },
                    onFailure = { e ->
                        Log.e(TAG, "loadDashboardData: Erro ao carregar estatísticas", e)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Erro desconhecido"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadDashboardData: Exceção ao carregar estatísticas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }

    fun refreshData() {
        Log.d(TAG, "refreshData: Atualizando dados do dashboard")
        loadDashboardData()
    }

    fun loadColetasEvolucao() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGrafico = true)
            
            try {
                // TODO: Implementar carregamento de dados do gráfico via Use Case
                Log.d(TAG, "loadColetasEvolucao: Carregamento de gráfico não implementado")
                _uiState.value = _uiState.value.copy(
                    isLoadingGrafico = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadColetasEvolucao: Erro ao carregar gráfico", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingGrafico = false,
                    graficoError = e.message
                )
            }
        }
    }
}
