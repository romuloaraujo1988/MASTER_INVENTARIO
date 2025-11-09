package com.inventario.mobile.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class StatusFilter {
    TODOS, PENDENTES, COLETADOS
}

data class InventoryUiState(
    val patrimonios: List<Patrimonio> = emptyList(),
    val filteredPatrimonios: List<Patrimonio> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedSetor: String? = null,
    val selectedSala: String? = null,
    val selectedResponsavel: String? = null,
    val selectedStatus: StatusFilter = StatusFilter.TODOS,
    val responsaveis: List<String> = emptyList(),
    val salasComColetas: List<String> = emptyList(),
    val totalPendentes: Int = 0,
    val totalColetados: Int = 0,
    val isRefreshing: Boolean = false
)

// @HiltViewModel - Temporarily disabled
class InventoryViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadPatrimonios()
    }

    fun loadPatrimonios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Carregar patrimônios do repositório
                val result = Result.success(emptyList<com.inventario.mobile.data.model.Patrimonio>()) // TODO: Implementar getAllPatrimonios
                result.fold(
                    onSuccess = { patrimonios ->
                        // Carregar coletas para obter salas com patrimônios coletados
                        val coletas = emptyList<com.inventario.mobile.data.model.Coleta>() // TODO: Implementar getColetasLocal
                        val salasComColetas = coletas
                            .mapNotNull { it.localizacaoAtual }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .sorted()
                        
                        // Extrair lista de responsáveis únicos
                        val responsaveis = patrimonios
                            .mapNotNull { it.responsavelNome }
                            .distinct()
                            .sorted()
                        
                        // Calcular contadores
                        val totalColetados = patrimonios.count { it.coletado }
                        val totalPendentes = patrimonios.size - totalColetados
                        
                        _uiState.value = _uiState.value.copy(
                            patrimonios = patrimonios,
                            filteredPatrimonios = patrimonios,
                            responsaveis = responsaveis,
                            salasComColetas = salasComColetas,
                            totalColetados = totalColetados,
                            totalPendentes = totalPendentes,
                            isLoading = false
                        )
                        applyFilters()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao carregar patrimônios: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }

    fun refreshPatrimonios() {
        loadPatrimonios() // Simplificado para reutilizar a lógica
    }

    fun searchPatrimonios(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun filterBySetor(setor: String?) {
        _uiState.value = _uiState.value.copy(selectedSetor = setor)
        applyFilters()
    }

    fun filterBySala(sala: String?) {
        _uiState.value = _uiState.value.copy(selectedSala = sala)
        applyFilters()
    }

    fun filterByResponsavel(responsavel: String?) {
        _uiState.value = _uiState.value.copy(selectedResponsavel = responsavel)
        applyFilters()
    }

    fun filterByStatus(status: StatusFilter) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedSetor = null,
            selectedSala = null,
            selectedResponsavel = null,
            selectedStatus = StatusFilter.TODOS
        )
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        
        // Aplicar filtro por sala (filtrar por patrimônios coletados nesta sala)
        if (currentState.selectedSala != null) {
            val salaNome = currentState.selectedSala
            viewModelScope.launch {
                try {
                    // Mover operação de I/O para thread de background
                    val coletas = emptyList<com.inventario.mobile.data.model.Coleta>() // TODO: Implementar getColetasLocal
                    
                    val patrimoniosColetadosNaSala: Set<Int> = coletas
                        .filter { it.localizacaoAtual == salaNome }
                        .map { it.patrimonioId }
                        .toSet()
                    
                    var filtered: List<Patrimonio> = currentState.patrimonios.filter { patrimonio ->
                        patrimoniosColetadosNaSala.contains(patrimonio.id.toInt())
                    }
                    
                    // Aplicar outros filtros após o filtro de sala
                    filtered = applyOtherFilters(filtered, currentState)
                    
                    // Atualizar o estado com os dados filtrados
                    val totalColetados = filtered.count { it.coletado }
                    val totalPendentes = filtered.count { !it.coletado }
                    
                    _uiState.value = currentState.copy(
                        filteredPatrimonios = filtered,
                        totalColetados = totalColetados,
                        totalPendentes = totalPendentes
                    )
                } catch (e: Exception) {
                    // Em caso de erro, manter filtro original por salaId
                    val salaId = currentState.selectedSala?.toLongOrNull()
                    var filtered = currentState.patrimonios.filter { it.salaId == salaId }
                    filtered = applyOtherFilters(filtered, currentState)
                    
                    val totalColetados = filtered.count { it.coletado }
                    val totalPendentes = filtered.count { !it.coletado }
                    
                    _uiState.value = currentState.copy(
                        filteredPatrimonios = filtered,
                        totalColetados = totalColetados,
                        totalPendentes = totalPendentes
                    )
                }
            }
        } else {
            // Aplicar filtros normalmente quando não há filtro de sala
            var filtered: List<Patrimonio> = currentState.patrimonios
            filtered = applyOtherFilters(filtered, currentState)
            
            // Calcular contadores baseados nos dados filtrados
            val totalColetados = filtered.count { it.coletado }
            val totalPendentes = filtered.count { !it.coletado }

            _uiState.value = currentState.copy(
                filteredPatrimonios = filtered,
                totalColetados = totalColetados,
                totalPendentes = totalPendentes
            )
        }
    }
    
    private fun applyOtherFilters(patrimonios: List<Patrimonio>, currentState: InventoryUiState): List<Patrimonio> {
        var filtered = patrimonios

        // Aplicar filtro de busca
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { patrimonio ->
                patrimonio.numeroPatrimonio.lowercase().contains(query) ||
                patrimonio.descricao.lowercase().contains(query) ||
                patrimonio.qrCode?.lowercase()?.contains(query) == true ||
                patrimonio.responsavelNome?.lowercase()?.contains(query) == true
            }
        }

        // Aplicar filtro por setor
        if (currentState.selectedSetor != null) {
            val setorId = currentState.selectedSetor?.toLongOrNull()
            filtered = filtered.filter { it.setorId == setorId }
        }

        // Aplicar filtro por responsável
        if (currentState.selectedResponsavel != null) {
            filtered = filtered.filter { it.responsavelNome == currentState.selectedResponsavel }
        }

        // Aplicar filtro por status
        when (currentState.selectedStatus) {
            StatusFilter.PENDENTES -> filtered = filtered.filter { !it.coletado }
            StatusFilter.COLETADOS -> filtered = filtered.filter { it.coletado }
            StatusFilter.TODOS -> { /* Não filtrar */ }
        }
        
        return filtered
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}