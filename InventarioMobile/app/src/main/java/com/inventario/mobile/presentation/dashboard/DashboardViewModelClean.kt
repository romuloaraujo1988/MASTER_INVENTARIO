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
 * 
 * v2.4: Adicionado suporte a Flow reativo para estatísticas
 */
@HiltViewModel
class DashboardViewModelClean @Inject constructor(
    private val buscarEstatisticasDashboardUseCase: BuscarEstatisticasDashboardUseCase,
    private val buscarEvolucaoColetasUseCase: BuscarEvolucaoColetasUseCase,
    private val dashboardRepository: com.inventario.mobile.data.repository.DashboardRepositoryImpl
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
    
    // ========================================
    // 🔄 MÉTODOS REATIVOS (v2.4)
    // ========================================
    
    /**
     * 🔄 Observa estatísticas HÍBRIDAS em tempo real
     * 
     * ESTRATÉGIA INTELIGENTE:
     * 1. Busca estatísticas base do servidor (total correto de patrimônios)
     * 2. Observa coletas locais pendentes (não sincronizadas)
     * 3. Soma: Servidor + Coletas Locais = Total Atualizado
     * 
     * Este StateFlow é atualizado AUTOMATICAMENTE quando:
     * - Uma coleta é registrada localmente
     * - Uma coleta é sincronizada com o servidor
     * - Qualquer mudança na tabela coleta
     * 
     * ✨ NÃO PRECISA CHAMAR refresh() MANUALMENTE!
     * 
     * Benefícios:
     * - Total de patrimônios sempre correto (do servidor)
     * - Coletas locais somadas instantaneamente
     * - Atualização em tempo real sem esperar sincronização
     * 
     * Uso no Fragment:
     * ```kotlin
     * viewLifecycleOwner.lifecycleScope.launch {
     *     viewModel.observarEstatisticasHibridas(inventarioId).collect { stats ->
     *         updateUI(stats)
     *     }
     * }
     * ```
     */
    fun observarEstatisticasHibridas(inventarioId: Int?): kotlinx.coroutines.flow.Flow<DashboardStats> {
        return dashboardRepository.observarEstatisticasHibridas(inventarioId)
            .also { flow ->
                viewModelScope.launch {
                    flow.collect { stats ->
                        Log.d(TAG, "🔄 Estatísticas híbridas atualizadas automaticamente!")
                        Log.d(TAG, "   Total: ${stats.totalPatrimonios}")
                        Log.d(TAG, "   Coletados: ${stats.totalColetados}")
                        Log.d(TAG, "   Pendentes: ${stats.totalPendentes}")
                        Log.d(TAG, "   Percentual: ${stats.percentualConclusao}%")
                        
                        // Atualizar o estado tradicional para compatibilidade
                        _uiState.value = _uiState.value.copy(
                            dashboardStats = stats,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
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
