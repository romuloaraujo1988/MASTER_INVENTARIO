package com.inventario.mobile.presentation.patrimonio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.domain.usecase.BuscarEstatisticasDashboardUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosPaginadoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para listagem de patrimônios usando Paging 3
 * 
 * Vantagens sobre paginação manual:
 * - Gerenciamento automático de cache
 * - Retry automático em caso de erro
 * - Suporte a placeholders
 * - Integração nativa com RecyclerView
 * - Menos código boilerplate
 * 
 * Uso:
 * ```kotlin
 * // No Fragment/Activity
 * lifecycleScope.launch {
 *     viewModel.patrimoniosPaging.collectLatest { pagingData ->
 *         adapter.submitData(pagingData)
 *     }
 * }
 * ```
 */
@HiltViewModel
class PatrimonioListViewModelPaging @Inject constructor(
    private val buscarPatrimoniosPaginadoUseCase: BuscarPatrimoniosPaginadoUseCase,
    private val buscarEstatisticasUseCase: BuscarEstatisticasDashboardUseCase
) : ViewModel() {
    
    // ========================================
    // Filtros
    // ========================================
    
    private val _filtroSalaId = MutableStateFlow<Int?>(null)
    private val _filtroColetado = MutableStateFlow<Boolean?>(null)
    private val _filtroInventarioId = MutableStateFlow<Int?>(null)
    
    // Trigger para refresh
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    
    init {
        // Emitir valor inicial para trigger
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
    
    // ========================================
    // Paging Data
    // ========================================
    
    /**
     * Flow de patrimônios paginados
     * 
     * Reage automaticamente a mudanças nos filtros
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val patrimoniosPaging: Flow<PagingData<Patrimonio>> = combine(
        _filtroSalaId,
        _filtroColetado,
        _filtroInventarioId,
        _refreshTrigger
    ) { salaId, coletado, inventarioId, _ ->
        Triple(salaId, coletado, inventarioId)
    }.flatMapLatest { (salaId, coletado, inventarioId) ->
        if (salaId != null) {
            // Buscar por sala
            buscarPatrimoniosPaginadoUseCase.porSala(
                salaId = salaId,
                coletado = coletado,
                inventarioId = inventarioId
            )
        } else {
            // Buscar todos
            buscarPatrimoniosPaginadoUseCase()
        }
    }.cachedIn(viewModelScope)
    
    // ========================================
    // Estatísticas (totais do servidor)
    // ========================================
    
    private val _estatisticas = MutableStateFlow<EstatisticasState>(EstatisticasState.Loading)
    val estatisticas: StateFlow<EstatisticasState> = _estatisticas.asStateFlow()
    
    /**
     * Carrega estatísticas do servidor
     * Os totais vêm do endpoint de dashboard, não da contagem local
     */
    fun carregarEstatisticas(inventarioId: Int? = null) {
        viewModelScope.launch {
            _estatisticas.value = EstatisticasState.Loading
            
            buscarEstatisticasUseCase(inventarioId).fold(
                onSuccess = { stats ->
                    _estatisticas.value = EstatisticasState.Success(
                        totalPatrimonios = stats.totalPatrimonios,
                        totalColetados = stats.totalColetados,
                        totalPendentes = stats.totalPendentes,
                        percentualConclusao = stats.percentualConclusao
                    )
                },
                onFailure = { error ->
                    _estatisticas.value = EstatisticasState.Error(
                        error.message ?: "Erro ao carregar estatísticas"
                    )
                }
            )
        }
    }
    
    // ========================================
    // Ações
    // ========================================
    
    /**
     * Aplica filtro por sala
     */
    fun filtrarPorSala(salaId: Int?) {
        _filtroSalaId.value = salaId
    }
    
    /**
     * Aplica filtro por status de coleta
     */
    fun filtrarPorColetado(coletado: Boolean?) {
        _filtroColetado.value = coletado
    }
    
    /**
     * Define o inventário ativo
     */
    fun setInventarioId(inventarioId: Int?) {
        _filtroInventarioId.value = inventarioId
    }
    
    /**
     * Força refresh dos dados
     */
    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
    
    /**
     * Limpa todos os filtros
     */
    fun limparFiltros() {
        _filtroSalaId.value = null
        _filtroColetado.value = null
    }
}

/**
 * Estado das estatísticas
 */
sealed class EstatisticasState {
    object Loading : EstatisticasState()
    
    data class Success(
        val totalPatrimonios: Int,
        val totalColetados: Int,
        val totalPendentes: Int,
        val percentualConclusao: Double
    ) : EstatisticasState()
    
    data class Error(val message: String) : EstatisticasState()
}
