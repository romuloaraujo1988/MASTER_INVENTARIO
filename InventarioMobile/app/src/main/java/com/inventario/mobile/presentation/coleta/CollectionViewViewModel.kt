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

    fun loadColetas() {
        viewModelScope.launch {
            Log.d(TAG, "loadColetas: Iniciando carregamento de coletas")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Busca coletas do servidor + locais
                val coletas = repository.getColetas()
                Log.d(TAG, "loadColetas: ${coletas.size} coletas carregadas do repositório")
                
                // Obter ID do usuário atual
                val usuarioAtual = repository.getCurrentUser()
                val usuarioId = usuarioAtual?.id?.toInt()
                Log.d(TAG, "loadColetas: Usuário atual ID: $usuarioId")
                
                val sincronizadas = coletas.count { it.sincronizado }
                val pendentes = coletas.size - sincronizadas
                Log.d(TAG, "loadColetas: Sincronizadas: $sincronizadas, Pendentes: $pendentes")
                
                // Extrair salas únicas das coletas (usando localizacaoAtual)
                val salasUnicas = coletas
                    .mapNotNull { it.localizacaoAtual }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                Log.d(TAG, "loadColetas: ${salasUnicas.size} salas únicas encontradas: $salasUnicas")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    coletas = coletas,
                    filteredColetas = coletas,
                    salas = salasUnicas,
                    usuarioAtualId = usuarioId,
                    totalColetas = coletas.size,
                    sincronizadas = sincronizadas,
                    pendentes = pendentes
                )
                Log.d(TAG, "loadColetas: Estado atualizado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "loadColetas: Erro ao carregar coletas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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