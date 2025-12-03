package com.inventario.mobile.presentation.patrimonio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.api.PatrimonioApi
import com.inventario.mobile.data.model.Patrimonio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para listagem de patrimônios com scroll infinito
 * 
 * Implementa paginação manual simples:
 * - Carrega página inicial
 * - Carrega mais ao chegar no fim da lista
 * - Mantém estado de loading e erro
 */
@HiltViewModel
class PatrimonioListViewModel @Inject constructor(
    private val patrimonioApi: PatrimonioApi
) : ViewModel() {
    
    companion object {
        const val PAGE_SIZE = 20
    }
    
    // Estado da UI
    private val _state = MutableStateFlow<PatrimonioListState>(PatrimonioListState.Idle)
    val state: StateFlow<PatrimonioListState> = _state.asStateFlow()
    
    // Lista acumulada de patrimônios
    private val _patrimonios = MutableStateFlow<List<Patrimonio>>(emptyList())
    val patrimonios: StateFlow<List<Patrimonio>> = _patrimonios.asStateFlow()
    
    // Estatísticas (vem do servidor, não da contagem local)
    private val _totalPatrimonios = MutableStateFlow(0)
    val totalPatrimonios: StateFlow<Int> = _totalPatrimonios.asStateFlow()
    
    // Controle de paginação
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    
    // Filtros atuais
    private var salaIdAtual: Int? = null
    private var coletadoFiltro: Boolean? = null
    private var inventarioIdAtual: Int? = null
    
    /**
     * Carrega primeira página (reset)
     */
    fun carregarPatrimonios(
        salaId: Int? = null,
        coletado: Boolean? = null,
        inventarioId: Int? = null
    ) {
        // Salvar filtros
        salaIdAtual = salaId
        coletadoFiltro = coletado
        inventarioIdAtual = inventarioId
        
        // Reset paginação
        currentPage = 0
        isLastPage = false
        _patrimonios.value = emptyList()
        
        carregarPagina()
    }
    
    /**
     * Carrega próxima página (scroll infinito)
     */
    fun carregarMais() {
        if (isLoading || isLastPage) return
        carregarPagina()
    }
    
    /**
     * Recarrega do início
     */
    fun refresh() {
        carregarPatrimonios(salaIdAtual, coletadoFiltro, inventarioIdAtual)
    }
    
    /**
     * Carrega uma página de dados
     */
    private fun carregarPagina() {
        if (isLoading) return
        
        viewModelScope.launch {
            isLoading = true
            
            // Mostrar loading apenas na primeira página
            if (currentPage == 0) {
                _state.value = PatrimonioListState.Loading
            } else {
                _state.value = PatrimonioListState.LoadingMore
            }
            
            try {
                val response = if (salaIdAtual != null) {
                    // Buscar por sala
                    patrimonioApi.buscarPorSala(
                        salaId = salaIdAtual!!,
                        page = currentPage,
                        size = PAGE_SIZE,
                        coletado = coletadoFiltro,
                        inventarioId = inventarioIdAtual
                    )
                } else {
                    // Buscar todos
                    patrimonioApi.listarPatrimoniosPaginado(
                        page = currentPage,
                        size = PAGE_SIZE
                    )
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val novosPatrimonios = response.body()?.data ?: emptyList()
                    
                    // Verificar se é última página
                    isLastPage = novosPatrimonios.size < PAGE_SIZE
                    
                    // Acumular patrimônios
                    _patrimonios.value = _patrimonios.value + novosPatrimonios
                    
                    // Extrair total do response (se disponível)
                    response.body()?.let { body ->
                        // O servidor pode retornar totalElements no response
                        (body as? Map<*, *>)?.get("totalElements")?.let { total ->
                            _totalPatrimonios.value = (total as? Number)?.toInt() ?: 0
                        }
                    }
                    
                    currentPage++
                    
                    _state.value = PatrimonioListState.Success(
                        patrimonios = _patrimonios.value,
                        hasMore = !isLastPage,
                        totalItems = _totalPatrimonios.value
                    )
                } else {
                    val errorMsg = response.body()?.message ?: "Erro ao carregar patrimônios"
                    _state.value = PatrimonioListState.Error(errorMsg)
                }
                
            } catch (e: Exception) {
                _state.value = PatrimonioListState.Error(
                    e.message ?: "Erro de conexão"
                )
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Verifica se pode carregar mais
     */
    fun canLoadMore(): Boolean = !isLoading && !isLastPage
}

/**
 * Estados da UI para listagem com paginação
 */
sealed class PatrimonioListState {
    object Idle : PatrimonioListState()
    object Loading : PatrimonioListState()          // Carregando primeira página
    object LoadingMore : PatrimonioListState()      // Carregando mais itens
    
    data class Success(
        val patrimonios: List<Patrimonio>,
        val hasMore: Boolean,
        val totalItems: Int = 0
    ) : PatrimonioListState()
    
    data class Error(val message: String) : PatrimonioListState()
}
