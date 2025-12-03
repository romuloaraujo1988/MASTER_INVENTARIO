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
        val filtroStatus: FiltroStatus = FiltroStatus.TODOS,
        val usuarioAtualId: Int? = null,
        val totalColetas: Int = 0,
        val sincronizadas: Int = 0,
        val pendentes: Int = 0
    )
    
    enum class FiltroUsuario {
        TODAS,
        MINHAS
    }
    
    enum class FiltroStatus {
        TODOS,
        SINCRONIZADOS,
        PENDENTES
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
                
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "OBTENDO USUÁRIO ATUAL")
                Log.d(TAG, "Usuário atual: ${usuarioAtual?.nome}")
                Log.d(TAG, "Usuário ID (Long): ${usuarioAtual?.id}")
                Log.d(TAG, "Usuário ID (Int): $usuarioId")
                Log.d(TAG, "Usuário Login: ${usuarioAtual?.username}")
                Log.d(TAG, "═══════════════════════════════════════════")
                
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
                
                // Aplicar filtro inicial se estiver na primeira página
                if (page == 0) {
                    Log.d(TAG, "loadColetasPage: Aplicando filtro inicial: ${_uiState.value.filtroUsuario}")
                    applyFilters(_uiState.value.filtroUsuario, _uiState.value.filtroStatus, _uiState.value.salaSelecionada)
                }
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
            filtroStatus = current.filtroStatus,
            salaSelecionada = current.salaSelecionada
        )
    }
    
    fun filterBySala(sala: String?) {
        applyFilters(
            filtroUsuario = _uiState.value.filtroUsuario,
            filtroStatus = _uiState.value.filtroStatus,
            salaSelecionada = sala
        )
    }
    
    fun filterByStatus(filtro: FiltroStatus) {
        val current = _uiState.value
        applyFilters(
            filtroUsuario = current.filtroUsuario,
            filtroStatus = filtro,
            salaSelecionada = current.salaSelecionada
        )
    }
    
    private fun applyFilters(filtroUsuario: FiltroUsuario, filtroStatus: FiltroStatus, salaSelecionada: String?) {
        val current = _uiState.value
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "APLICANDO FILTROS")
        Log.d(TAG, "Filtro Usuário: $filtroUsuario")
        Log.d(TAG, "Sala Selecionada: $salaSelecionada")
        Log.d(TAG, "Total de coletas: ${current.coletas.size}")
        Log.d(TAG, "Usuário Atual ID: ${current.usuarioAtualId}")
        
        // Aplicar filtro de usuário
        var filtered = when (filtroUsuario) {
            FiltroUsuario.TODAS -> {
                Log.d(TAG, "Filtro TODAS: mostrando todas as ${current.coletas.size} coletas")
                current.coletas
            }
            FiltroUsuario.MINHAS -> {
                if (current.usuarioAtualId != null) {
                    val minhasColetas = current.coletas.filter { coleta ->
                        val match = coleta.usuarioId == current.usuarioAtualId
                        if (!match) {
                            Log.d(TAG, "Coleta ${coleta.id} - usuarioId=${coleta.usuarioId} != ${current.usuarioAtualId}")
                        }
                        match
                    }
                    Log.d(TAG, "Filtro MINHAS: ${minhasColetas.size} coletas do usuário ${current.usuarioAtualId}")
                    
                    // Debug: mostrar algumas coletas para verificar
                    current.coletas.take(5).forEach { coleta ->
                        Log.d(TAG, "  Coleta ID=${coleta.id}, usuarioId=${coleta.usuarioId}, nomeColetor=${coleta.nomeColetor}")
                    }
                    
                    minhasColetas
                } else {
                    Log.w(TAG, "Filtro MINHAS: usuarioAtualId é null, mostrando todas")
                    current.coletas
                }
            }
        }
        
        Log.d(TAG, "Após filtro de usuário: ${filtered.size} coletas")
        
        // Aplicar filtro de status
        filtered = when (filtroStatus) {
            FiltroStatus.TODOS -> {
                Log.d(TAG, "Filtro TODOS: mostrando todas as ${filtered.size} coletas")
                filtered
            }
            FiltroStatus.SINCRONIZADOS -> {
                val sincronizadas = filtered.filter { it.sincronizado }
                Log.d(TAG, "Filtro SINCRONIZADOS: ${sincronizadas.size} coletas")
                sincronizadas
            }
            FiltroStatus.PENDENTES -> {
                val pendentes = filtered.filter { !it.sincronizado }
                Log.d(TAG, "Filtro PENDENTES: ${pendentes.size} coletas")
                pendentes
            }
        }
        
        Log.d(TAG, "Após filtro de status: ${filtered.size} coletas")
        
        // Aplicar filtro de sala
        filtered = if (salaSelecionada == null) {
            Log.d(TAG, "Sem filtro de sala")
            filtered
        } else {
            val filteredBySala = filtered.filter { it.localizacaoAtual == salaSelecionada }
            Log.d(TAG, "Filtro de sala '$salaSelecionada': ${filteredBySala.size} coletas")
            filteredBySala
        }
        
        val sincronizadas = filtered.count { it.sincronizado }
        val pendentes = filtered.size - sincronizadas
        
        Log.d(TAG, "Resultado final: ${filtered.size} coletas")
        Log.d(TAG, "  Sincronizadas: $sincronizadas")
        Log.d(TAG, "  Pendentes: $pendentes")
        Log.d(TAG, "═══════════════════════════════════════")
        
        _uiState.value = current.copy(
            filteredColetas = filtered,
            salaSelecionada = salaSelecionada,
            filtroUsuario = filtroUsuario,
            filtroStatus = filtroStatus,
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
