package com.inventario.mobile.presentation.coletas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.model.Patrimonio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ColetasUiState(
    val isLoading: Boolean = false,
    val patrimoniosColetados: List<Patrimonio> = emptyList(),
    val patrimoniosPendentes: List<Patrimonio> = emptyList(),
    val totalColetados: Int = 0,
    val totalPendentes: Int = 0,
    val errorMessage: String? = null
)

class ColetasViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColetasUiState())
    val uiState: StateFlow<ColetasUiState> = _uiState.asStateFlow()

    fun loadColetas(filtrarPorUsuario: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                android.util.Log.d("ColetasViewModel", "═══════════════════════════════════════")
                android.util.Log.d("ColetasViewModel", "CARREGANDO COLETAS")
                android.util.Log.d("ColetasViewModel", "Filtrar por usuário: $filtrarPorUsuario")
                
                val patrimonios = repository.getAllPatrimoniosList()
                android.util.Log.d("ColetasViewModel", "Total de patrimônios: ${patrimonios.size}")
                
                // Obter usuário atual
                val usuarioAtual = repository.getCurrentUser()
                android.util.Log.d("ColetasViewModel", "Usuário atual: ${usuarioAtual?.nome}")
                
                val coletados = if (filtrarPorUsuario && usuarioAtual != null) {
                    // Filtrar apenas coletas do usuário logado
                    val minhasColetas = patrimonios.filter { 
                        it.coletado == true && it.coletadoPor == usuarioAtual.nome 
                    }
                    android.util.Log.d("ColetasViewModel", "Minhas coletas: ${minhasColetas.size}")
                    minhasColetas
                } else {
                    // Mostrar todas as coletas
                    val todasColetas = patrimonios.filter { it.coletado == true }
                    android.util.Log.d("ColetasViewModel", "Todas as coletas: ${todasColetas.size}")
                    todasColetas
                }
                
                val pendentes = patrimonios.filter { it.coletado != true }
                android.util.Log.d("ColetasViewModel", "Pendentes: ${pendentes.size}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    patrimoniosColetados = coletados,
                    patrimoniosPendentes = pendentes,
                    totalColetados = coletados.size,
                    totalPendentes = pendentes.size
                )
                
                android.util.Log.d("ColetasViewModel", "✓ Coletas carregadas com sucesso!")
                android.util.Log.d("ColetasViewModel", "═══════════════════════════════════════")
            } catch (e: Exception) {
                android.util.Log.e("ColetasViewModel", "✗ Erro ao carregar coletas", e)
                android.util.Log.d("ColetasViewModel", "═══════════════════════════════════════")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar coletas: ${e.message}"
                )
            }
        }
    }
    
    fun toggleFiltroUsuario() {
        viewModelScope.launch {
            val usuarioAtual = repository.getCurrentUser()
            val filtrarPorUsuario = !(_uiState.value.patrimoniosColetados.firstOrNull()?.let { 
                usuarioAtual?.nome == it.coletadoPor 
            } ?: true)
            loadColetas(filtrarPorUsuario)
        }
    }

    fun getColetasPorSala(): Map<String, List<Patrimonio>> {
        val coletados = _uiState.value.patrimoniosColetados
        return coletados.groupBy { it.salaNome ?: "Sala não definida" }
    }

    fun getPendentesPorSala(): Map<String, List<Patrimonio>> {
        val pendentes = _uiState.value.patrimoniosPendentes
        return pendentes.groupBy { it.salaNome ?: "Sala não definida" }
    }
}

class ColetasViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColetasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColetasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}