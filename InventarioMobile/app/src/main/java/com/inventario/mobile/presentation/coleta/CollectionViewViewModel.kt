package com.inventario.mobile.presentation.coleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectionViewViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CollectionViewViewModel"
    }

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val coletas: List<Coleta> = emptyList(),
        val filteredColetas: List<Coleta> = emptyList(),
        val salas: List<String> = emptyList(),
        val salaSelecionada: String? = null,
        val filtroUsuario: FiltroUsuario = FiltroUsuario.TODAS,
        val usuarioAtualId: Int? = null,
        val totalColetas: Int = 0,
        val sincronizadas: Int = 0,
        val pendentes: Int = 0
    )
    
    enum class FiltroUsuario {
        TODAS,
        MINHAS
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var currentPage = 0
    private val pageSize = 50
    private var isLoadingMore = false
    private var hasMorePages = true
    
    fun loadColetas() {
        viewModelScope.launch {
            Log.d(TAG, "loadColetas: Iniciando carregamento de coletas (página 0)")
            currentPage = 0
            hasMorePages = true
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                loadColetasPage(0)
            } catch (e: Exception) {
                Log.e(TAG, "loadColetas: Erro ao carregar coletas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar coletas"
                )
            }
        }
    }
    
    fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) {
            Log.d(TAG, "loadNextPage: Ignorando (isLoadingMore=$isLoadingMore, hasMorePages=$hasMorePages)")
            return
        }
        
        viewModelScope.launch {
            Log.d(TAG, "loadNextPage: Carregando página ${currentPage + 1}")
            isLoadingMore = true
            
            try {
                loadColetasPage(currentPage + 1)
            } catch (e: Exception) {
                Log.e(TAG, "loadNextPage: Erro ao carregar próxima página", e)
                isLoadingMore = false
            }
        }
    }
    
    private suspend fun loadColetasPage(page: Int) {
        Log.d(TAG, "loadColetasPage: Carregando página $page")
        
        val result = repository.getColetasPaginadas(page, pageSize)
        
        result.fold(
            onSuccess = { pagedResult ->
                Log.d(TAG, "loadColetasPage: ${pagedResult.coletas.size} coletas carregadas")
                Log.d(TAG, "loadColetasPage: Página ${pagedResult.page + 1}/${pagedResult.totalPages}")
                
                // Obter ID do usuário atual
                val usuarioAtual = repository.getCurrentUser()
                val usuarioId = usuarioAtual?.id?.toInt()
                
                // Combinar com coletas existentes se for página > 0
                val todasColetas = if (page == 0) {
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
                
                Log.d(TAG, "loadColetasPage: Total acumulado: ${todasColetas.size} coletas")
                Log.d(TAG, "loadColetasPage: ${salasUnicas.size} salas únicas")
                
                currentPage = pagedResult.page
                hasMorePages = pagedResult.hasNext
                isLoadingMore = false
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    coletas = todasColetas,
                    filteredColetas = todasColetas,
                    salas = salasUnicas,
                    usuarioAtualId = usuarioId,
                    totalColetas = todasColetas.size,
                    sincronizadas = sincronizadas,
                    pendentes = pendentes
                )
                
                Log.d(TAG, "loadColetasPage: Estado atualizado. HasMorePages: $hasMorePages")
            },
            onFailure = { exception ->
                Log.e(TAG, "loadColetasPage: Erro", exception)
                isLoadingMore = false
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Erro ao carregar coletas"
                )
            }
        )
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
            totalColetas = filtered.size,
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
                loadColetas()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Erro ao remover coleta"
                )
            }
        }
    }
}