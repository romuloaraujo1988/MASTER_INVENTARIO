package com.inventario.mobile.presentation.coleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel com suporte a paginação para visualização de coletas
 */
class CollectionViewViewModelPaginated(
    private val repository: InventarioRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CollectionViewVMPaged"
        private const val PAGE_SIZE = 20
    }

    data class UiState(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: String? = null,
        val coletas: List<Coleta> = emptyList(),
        val filteredColetas: List<Coleta> = emptyList(),
        val salas: List<String> = emptyList(),
        val salaSelecionada: String? = null,
        val filtroUsuario: FiltroUsuario = FiltroUsuario.TODAS,
        val usuarioAtualId: Int? = null,
        val totalColetas: Int = 0,
        val sincronizadas: Int = 0,
        val pendentes: Int = 0,
        // Paginação
        val currentPage: Int = 0,
        val totalPages: Int = 0,
        val hasNextPage: Boolean = false,
        val hasPreviousPage: Boolean = false
    )
    
    enum class FiltroUsuario {
        TODAS,
        MINHAS
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Carrega a primeira página de coletas
     */
    fun loadColetas() {
        loadPage(0, clearExisting = true)
    }
    
    /**
     * Carrega a próxima página
     */
    fun loadNextPage() {
        val current = _uiState.value
        if (current.hasNextPage && !current.isLoadingMore) {
            loadPage(current.currentPage + 1, clearExisting = false)
        }
    }
    
    /**
     * Recarrega os dados (pull-to-refresh)
     */
    fun refresh() {
        loadPage(0, clearExisting = true)
    }
    
    /**
     * Carrega uma página específica
     */
    private fun loadPage(page: Int, clearExisting: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "loadPage: Carregando página $page (clearExisting=$clearExisting)")
            
            // Define estado de loading
            _uiState.value = _uiState.value.copy(
                isLoading = clearExisting,
                isLoadingMore = !clearExisting,
                error = null
            )
            
            try {
                val result = repository.getColetasPaginadas(page, PAGE_SIZE)
                
                result.onSuccess { pagedResult ->
                    Log.d(TAG, "loadPage: ${pagedResult.coletas.size} coletas carregadas")
                    
                    // Obter ID do usuário atual
                    val usuarioAtual = repository.getCurrentUser()
                    val usuarioId = usuarioAtual?.id?.toInt()
                    
                    // Combinar com coletas existentes se não for para limpar
                    val todasColetas = if (clearExisting) {
                        pagedResult.coletas
                    } else {
                        _uiState.value.coletas + pagedResult.coletas
                    }
                    
                    val sincronizadas = todasColetas.count { it.sincronizado }
                    val pendentes = todasColetas.size - sincronizadas
                    
                    // Extrair salas únicas
                    val salasUnicas = todasColetas
                        .mapNotNull { it.localizacaoAtual }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        coletas = todasColetas,
                        filteredColetas = todasColetas,
                        salas = salasUnicas,
                        usuarioAtualId = usuarioId,
                        totalColetas = pagedResult.totalElements,
                        sincronizadas = sincronizadas,
                        pendentes = pendentes,
                        currentPage = pagedResult.page,
                        totalPages = pagedResult.totalPages,
                        hasNextPage = pagedResult.hasNext,
                        hasPreviousPage = pagedResult.hasPrevious
                    )
                    
                    Log.d(TAG, "loadPage: Página ${page + 1}/${pagedResult.totalPages} carregada com sucesso")
                }
                
                result.onFailure { error ->
                    Log.e(TAG, "loadPage: Erro ao carregar página", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "Erro ao carregar coletas"
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "loadPage: Exceção ao carregar página", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Erro ao carregar coletas"
                )
            }
        }
    }
    
    fun filterByUsuario(filtro: FiltroUsuario) {
        val current = _uiState.value
        applyFilters(
            filtroUsuario = filtro,
            salaSelecionada = current.salaSelecionada
        )
    }
    
    fun filterBySala(sala: String?) {
        applyFilters(
            filtroUsuario = _uiState.value.filtroUsuario,
            salaSelecionada = sala
        )
    }
    
    private fun applyFilters(filtroUsuario: FiltroUsuario, salaSelecionada: String?) {
        val current = _uiState.value
        
        // Aplicar filtro de usuário
        var filtered = when (filtroUsuario) {
            FiltroUsuario.TODAS -> current.coletas
            FiltroUsuario.MINHAS -> {
                current.usuarioAtualId?.let { userId ->
                    current.coletas.filter { it.usuarioId == userId }
                } ?: current.coletas
            }
        }
        
        // Aplicar filtro de sala
        filtered = if (salaSelecionada == null) {
            filtered
        } else {
            filtered.filter { it.localizacaoAtual == salaSelecionada }
        }
        
        val sincronizadas = filtered.count { it.sincronizado }
        val pendentes = filtered.size - sincronizadas
        
        _uiState.value = current.copy(
            filteredColetas = filtered,
            salaSelecionada = salaSelecionada,
            filtroUsuario = filtroUsuario,
            sincronizadas = sincronizadas,
            pendentes = pendentes
        )
    }

    fun filter(query: String) {
        val current = _uiState.value
        val filtered = if (query.isBlank()) {
            current.coletas
        } else {
            current.coletas.filter { coleta ->
                val patrimonioIdMatch = coleta.patrimonioId.toString().contains(query, ignoreCase = true)
                val descricaoMatch = (coleta.observacoes ?: "").contains(query, ignoreCase = true)
                val salaMatch = (coleta.localizacaoAtual ?: "").contains(query, ignoreCase = true)
                patrimonioIdMatch || descricaoMatch || salaMatch
            }
        }

        _uiState.value = current.copy(
            filteredColetas = filtered
        )
    }

    fun removeColeta(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeColeta(id)
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erro ao remover coleta"
                )
            }
        }
    }
}
