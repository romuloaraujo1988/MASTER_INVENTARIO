package com.inventario.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.local.dao.SincronizacaoDao
import com.inventario.mobile.domain.model.PatrimonioComColeta
import com.inventario.mobile.domain.model.SearchFilter
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosUseCase
import com.inventario.mobile.presentation.state.QuickSearchState
import com.inventario.mobile.presentation.state.SearchStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel para tela de busca rápida de patrimônios
 * 
 * Regra: Gerencia estado da UI e chama Use Cases
 * 
 * @see Requirements 1.1, 1.2, 4.1, 4.2, 4.3
 */
@HiltViewModel
class QuickSearchViewModel @Inject constructor(
    private val buscarPatrimoniosUseCase: BuscarPatrimoniosUseCase,
    private val sincronizacaoDao: SincronizacaoDao
) : ViewModel() {
    
    companion object {
        private const val TAG = "QuickSearchViewModel"
        private const val DEBOUNCE_DELAY_MS = 300L
        private const val MIN_QUERY_LENGTH = 3
        private const val SYNC_WARNING_HOURS = 24L
    }
    
    // Estado da busca
    private val _state = MutableStateFlow<QuickSearchState>(QuickSearchState.Idle)
    val state: StateFlow<QuickSearchState> = _state.asStateFlow()
    
    // Estatísticas da busca
    private val _searchStats = MutableStateFlow(SearchStats())
    val searchStats: StateFlow<SearchStats> = _searchStats.asStateFlow()
    
    // Filtro atual
    private val _currentFilter = MutableStateFlow(SearchFilter.ALL)
    val currentFilter: StateFlow<SearchFilter> = _currentFilter.asStateFlow()
    
    // Query atual
    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()
    
    // Job para debounce
    private var searchJob: Job? = null
    
    // Estado de sincronização
    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()
    
    private val _isSyncOutdated = MutableStateFlow(false)
    val isSyncOutdated: StateFlow<Boolean> = _isSyncOutdated.asStateFlow()
    
    init {
        carregarUltimaSincronizacao()
    }
    
    /**
     * Carrega informações da última sincronização
     * @see Requirements 6.3
     */
    private fun carregarUltimaSincronizacao() {
        viewModelScope.launch {
            try {
                val ultimaSync = sincronizacaoDao.getUltimaSincronizacao()
                
                if (ultimaSync != null) {
                    val dataHora = ultimaSync.dataHora
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    _lastSyncTime.value = "Última sincronização: ${sdf.format(Date(dataHora))}"
                    
                    // Verificar se está desatualizado (> 24h)
                    val horasDesdeSync = TimeUnit.MILLISECONDS.toHours(
                        System.currentTimeMillis() - dataHora
                    )
                    _isSyncOutdated.value = horasDesdeSync >= SYNC_WARNING_HOURS
                } else {
                    _lastSyncTime.value = "Nunca sincronizado"
                    _isSyncOutdated.value = true
                }
            } catch (e: CancellationException) {
                // Job cancelado - ignorar
                Log.d(TAG, "ℹ️ Carregamento de sync cancelado")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar última sincronização", e)
                _lastSyncTime.value = null
                _isSyncOutdated.value = false
            }
        }
    }
    
    /**
     * Executa busca com debounce
     * 
     * @param query Termo de busca
     * @param filtro Filtro de status (opcional, usa filtro atual se não informado)
     */
    fun buscar(query: String, filtro: SearchFilter? = null) {
        // Atualizar query atual
        _currentQuery.value = query
        
        // Atualizar filtro se informado
        filtro?.let { _currentFilter.value = it }
        
        // Cancelar busca anterior
        searchJob?.cancel()
        
        // Validar query
        if (query.isBlank()) {
            _state.value = QuickSearchState.Idle
            _searchStats.value = SearchStats()
            return
        }
        
        if (query.length < MIN_QUERY_LENGTH) {
            // Aguardar mais caracteres
            return
        }
        
        // Agendar nova busca com debounce
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            executarBusca(query, _currentFilter.value)
        }
    }
    
    /**
     * Executa busca imediatamente (sem debounce)
     */
    fun buscarImediato(query: String, filtro: SearchFilter? = null) {
        _currentQuery.value = query
        filtro?.let { _currentFilter.value = it }
        
        searchJob?.cancel()
        
        if (query.isBlank() || query.length < MIN_QUERY_LENGTH) {
            _state.value = QuickSearchState.Idle
            _searchStats.value = SearchStats()
            return
        }
        
        viewModelScope.launch {
            executarBusca(query, _currentFilter.value)
        }
    }
    
    /**
     * Altera o filtro e re-executa a busca
     */
    fun alterarFiltro(filtro: SearchFilter) {
        _currentFilter.value = filtro
        
        val query = _currentQuery.value
        if (query.length >= MIN_QUERY_LENGTH) {
            viewModelScope.launch {
                executarBusca(query, filtro)
            }
        }
    }
    
    /**
     * Limpa a busca e retorna ao estado inicial
     */
    fun limparBusca() {
        searchJob?.cancel()
        _currentQuery.value = ""
        _state.value = QuickSearchState.Idle
        _searchStats.value = SearchStats()
    }
    
    /**
     * Executa a busca efetivamente
     */
    private suspend fun executarBusca(query: String, filtro: SearchFilter) {
        _state.value = QuickSearchState.Loading
        
        val startTime = System.currentTimeMillis()
        
        try {
            buscarPatrimoniosUseCase(query, filtro).fold(
                onSuccess = { resultados ->
                    val tempoMs = System.currentTimeMillis() - startTime
                    
                    if (resultados.isEmpty()) {
                        _state.value = QuickSearchState.Empty(query)
                        _searchStats.value = SearchStats(tempoMs = tempoMs)
                    } else {
                        _state.value = QuickSearchState.Success(resultados, tempoMs)
                        _searchStats.value = calcularEstatisticas(resultados, tempoMs)
                    }
                },
                onFailure = { error ->
                    _state.value = QuickSearchState.Error(error.message ?: "Erro desconhecido")
                    _searchStats.value = SearchStats()
                }
            )
        } catch (e: CancellationException) {
            // Job cancelado (debounce ou navegação) - não atualizar estado
            Log.d(TAG, "ℹ️ Busca cancelada - ignorando")
            // Não re-throw aqui pois estamos no contexto do viewModelScope
        }
    }
    
    /**
     * Calcula estatísticas dos resultados
     * 
     * @see Requirements 4.1, 4.3
     */
    private fun calcularEstatisticas(
        resultados: List<PatrimonioComColeta>,
        tempoMs: Long
    ): SearchStats {
        val coletados = resultados.count { it.coletado }
        val pendentes = resultados.count { !it.coletado }
        val divergencias = resultados.count { it.temDivergencia }
        
        return SearchStats(
            totalResultados = resultados.size,
            coletados = coletados,
            pendentes = pendentes,
            divergencias = divergencias,
            tempoMs = tempoMs
        )
    }
    
    /**
     * Verifica se a query é válida para busca
     */
    fun isQueryValida(query: String): Boolean {
        return buscarPatrimoniosUseCase.isQueryValida(query)
    }
    
    /**
     * Retorna mensagem de validação
     */
    fun getMensagemValidacao(query: String): String? {
        return buscarPatrimoniosUseCase.getMensagemValidacao(query)
    }
}
