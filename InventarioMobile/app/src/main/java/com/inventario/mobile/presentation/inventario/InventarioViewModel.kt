package com.inventario.mobile.presentation.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.Responsavel
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOrder {
    NUMERO_ASC,
    NUMERO_DESC,
    DESCRICAO_ASC,
    DESCRICAO_DESC,
    SETOR_ASC,
    SETOR_DESC
}

data class InventarioUiState(
    val isLoading: Boolean = false,
    val patrimonios: List<Patrimonio> = emptyList(),
    val patrimoniosFiltered: List<Patrimonio> = emptyList(),
    val responsaveis: List<Responsavel> = emptyList(),
    val errorMessage: String? = null,
    val filtroResponsavelId: Int? = null,
    val filtroColetado: Boolean? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NUMERO_ASC,
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalPatrimonios: Int = 0,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false
)

class InventarioViewModel(private val repository: InventarioRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()
    
    init {
        // Não carregar patrimônios automaticamente
        // Apenas carregar responsáveis
        loadResponsaveis()
    }
    
    fun loadPatrimonios() {
        // Não faz nada - patrimônios só devem ser carregados quando um responsável for selecionado
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            patrimonios = emptyList(),
            patrimoniosFiltered = emptyList(),
            errorMessage = "Selecione um responsável para visualizar os patrimônios"
        )
    }
    
    fun loadPatrimoniosByResponsavel(
        responsavelId: Int,
        coletado: Boolean? = null,
        page: Int = 0,
        loadMore: Boolean = false
    ) {
        viewModelScope.launch {
            if (loadMore) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentPage = 0,
                    patrimonios = emptyList()
                )
            }
            
            val result = repository.getPatrimoniosByResponsavel(
                responsavelId = responsavelId,
                page = page,
                size = _uiState.value.pageSize,
                coletado = coletado
            )
            
            result.fold(
                onSuccess = { patrimonios ->
                    val currentPatrimonios = if (loadMore) _uiState.value.patrimonios else emptyList()
                    val newPatrimonios = currentPatrimonios + patrimonios
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        patrimonios = newPatrimonios,
                        patrimoniosFiltered = newPatrimonios,
                        currentPage = page,
                        hasMorePages = patrimonios.size == _uiState.value.pageSize,
                        filtroResponsavelId = responsavelId,
                        filtroColetado = coletado
                    )
                    applyFiltersAndSort()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = exception.message ?: "Erro desconhecido"
                    )
                }
            )
        }
    }
    
    fun loadMorePatrimonios() {
        val currentState = _uiState.value
        if (currentState.hasMorePages && !currentState.isLoadingMore) {
            val nextPage = currentState.currentPage + 1
            
            if (currentState.filtroResponsavelId != null) {
                loadPatrimoniosByResponsavel(
                    responsavelId = currentState.filtroResponsavelId,
                    coletado = currentState.filtroColetado,
                    page = nextPage,
                    loadMore = true
                )
            }
        }
    }
    
    fun loadResponsaveis() {
        viewModelScope.launch {
            android.util.Log.d("InventarioViewModel", "Iniciando carregamento de responsáveis...")
            val result = repository.getResponsaveis()
            result.fold(
                onSuccess = { responsaveis ->
                    android.util.Log.d("InventarioViewModel", "Responsáveis carregados com sucesso: ${responsaveis.size} itens")
                    responsaveis.forEachIndexed { index, resp ->
                        android.util.Log.d("InventarioViewModel", "  [$index] ID: ${resp.id}, Nome: ${resp.nome}")
                    }
                    _uiState.value = _uiState.value.copy(responsaveis = responsaveis)
                    android.util.Log.d("InventarioViewModel", "Estado atualizado com responsáveis")
                },
                onFailure = { exception ->
                    android.util.Log.e("InventarioViewModel", "Erro ao carregar responsáveis: ${exception.message}", exception)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao carregar responsáveis: ${exception.message}"
                    )
                }
            )
        }
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filtroResponsavelId = null,
            filtroColetado = null,
            currentPage = 0,
            patrimonios = emptyList(),
            patrimoniosFiltered = emptyList()
        )
    }
    
    fun clearPatrimonios() {
        _uiState.value = _uiState.value.copy(
            patrimonios = emptyList(),
            patrimoniosFiltered = emptyList(),
            filtroResponsavelId = null,
            filtroColetado = null,
            currentPage = 0,
            hasMorePages = false,
            errorMessage = "Selecione um responsável para visualizar os patrimônios"
        )
    }
    
    fun applyFilters(responsavelId: Int?, coletado: Boolean?) {
        if (responsavelId != null) {
            loadPatrimoniosByResponsavel(responsavelId, coletado)
        } else {
            _uiState.value = _uiState.value.copy(
                filtroResponsavelId = null,
                filtroColetado = coletado
            )
            loadPatrimonios()
        }
    }
    
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun searchPatrimonios(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun setSortOrder(sortOrder: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        applyFiltersAndSort()
    }
    
    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filtered = currentState.patrimonios
        
        // Aplicar busca
        if (currentState.searchQuery.isNotEmpty()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { patrimonio ->
                patrimonio.numeroPatrimonio.lowercase().contains(query) ||
                patrimonio.descricao.lowercase().contains(query) ||
                patrimonio.marca?.lowercase()?.contains(query) == true ||
                patrimonio.modelo?.lowercase()?.contains(query) == true ||
                patrimonio.setorNome?.lowercase()?.contains(query) == true ||
                patrimonio.salaNome?.lowercase()?.contains(query) == true
            }
        }
        
        // Aplicar ordenação
        filtered = when (currentState.sortOrder) {
            SortOrder.NUMERO_ASC -> filtered.sortedBy { it.numeroPatrimonio }
            SortOrder.NUMERO_DESC -> filtered.sortedByDescending { it.numeroPatrimonio }
            SortOrder.DESCRICAO_ASC -> filtered.sortedBy { it.descricao }
            SortOrder.DESCRICAO_DESC -> filtered.sortedByDescending { it.descricao }
            SortOrder.SETOR_ASC -> filtered.sortedBy { it.setorNome ?: "" }
            SortOrder.SETOR_DESC -> filtered.sortedByDescending { it.setorNome ?: "" }
        }
        
        _uiState.value = currentState.copy(patrimoniosFiltered = filtered)
    }
}

class InventarioViewModelFactory(private val repository: InventarioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}