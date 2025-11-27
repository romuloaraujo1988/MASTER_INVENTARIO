package com.inventario.mobile.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.model.Coleta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PendingCollectionsUiState(
    val isLoading: Boolean = false,
    val pendingCollections: List<Coleta> = emptyList(),
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PendingCollectionsViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingCollectionsUiState())
    val uiState: StateFlow<PendingCollectionsUiState> = _uiState.asStateFlow()

    init {
        loadPendingCollections()
    }

    fun loadPendingCollections() {
        viewModelScope.launch {
            android.util.Log.d("PendingCollectionsVM", "📥 Iniciando carregamento de coletas pendentes...")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val pendingCollections = repository.getColetasPendentes()
                
                // Log de diagnóstico
                android.util.Log.d("PendingCollectionsVM", "✓ Carregamento concluído: ${pendingCollections.size} coletas pendentes")
                
                if (pendingCollections.isEmpty()) {
                    android.util.Log.d("PendingCollectionsVM", "📭 Nenhuma coleta pendente encontrada")
                } else {
                    // Log detalhado das coletas
                    pendingCollections.forEachIndexed { index, coleta ->
                        android.util.Log.d("PendingCollectionsVM", "  [$index] Patrimônio: ${coleta.numeroPatrimonio}, Erro: ${coleta.erroSincronizacao ?: "nenhum"}")
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingCollections = pendingCollections
                )
            } catch (e: Exception) {
                android.util.Log.e("PendingCollectionsVM", "❌ Erro ao carregar coletas pendentes", e)
                android.util.Log.e("PendingCollectionsVM", "  Tipo: ${e.javaClass.simpleName}")
                android.util.Log.e("PendingCollectionsVM", "  Mensagem: ${e.message}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar coletas pendentes: ${e.message}"
                )
            }
        }
    }

    fun deleteCollection(coleta: Coleta) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            
            try {
                repository.removeColeta(coleta.patrimonioId)
                
                // Update the list by removing the deleted item
                val updatedList = _uiState.value.pendingCollections.filter { 
                    it.patrimonioId != coleta.patrimonioId 
                }
                
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    pendingCollections = updatedList,
                    successMessage = "Coleta excluída com sucesso"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Erro ao excluir coleta: ${e.message}"
                )
            }
        }
    }

    fun deleteAllCollections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            
            try {
                val currentCollections = _uiState.value.pendingCollections
                
                // Delete all collections
                currentCollections.forEach { coleta ->
                    repository.removeColeta(coleta.patrimonioId)
                }
                
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    pendingCollections = emptyList(),
                    successMessage = "Todas as coletas foram excluídas com sucesso"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Erro ao excluir todas as coletas: ${e.message}"
                )
            }
        }
    }

    fun syncAllCollections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Call the repository sync method and wait for completion
                repository.sincronizarDados()
                
                // Wait a bit for sync to complete
                kotlinx.coroutines.delay(1000)
                
                // Reload the pending collections after sync
                val pendingCollections = repository.getColetasPendentes()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingCollections = pendingCollections,
                    successMessage = "Sincronização concluída com sucesso"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao sincronizar coletas: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    /**
     * v2.6/v2.7: Tenta sincronizar uma coleta específica novamente
     * Limpa o erro usando o repository (contexto correto) e tenta sincronizar
     */
    fun retryCollection(coleta: Coleta) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                android.util.Log.d("PendingCollectionsVM", "🔄 Tentando sincronizar coleta ${coleta.patrimonioId} novamente...")
                
                // v2.7: Usar método do repository ao invés de acessar database diretamente
                val coletaId = coleta.id?.toLong() ?: 0
                if (coletaId > 0) {
                    repository.limparErroColeta(coletaId)
                    android.util.Log.d("PendingCollectionsVM", "✓ Erro da coleta $coletaId limpo via repository")
                }
                
                // Tentar sincronizar todas as pendentes (incluindo esta)
                repository.sincronizarDados()
                
                // Aguardar um pouco
                kotlinx.coroutines.delay(1500)
                
                // Recarregar lista
                val pendingCollections = repository.getColetasPendentes()
                android.util.Log.d("PendingCollectionsVM", "📋 Lista recarregada: ${pendingCollections.size} coletas pendentes")
                
                // Verificar se a coleta foi sincronizada
                val coletaAindaPendente = pendingCollections.any { it.patrimonioId == coleta.patrimonioId }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingCollections = pendingCollections,
                    successMessage = if (!coletaAindaPendente) {
                        "✓ Coleta sincronizada com sucesso!"
                    } else {
                        "Sincronização tentada. Verifique se há erros."
                    }
                )
                
            } catch (e: Exception) {
                android.util.Log.e("PendingCollectionsVM", "Erro ao tentar sincronizar coleta", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao sincronizar: ${e.message}"
                )
            }
        }
    }
    
    /**
     * v2.6/v2.7: Limpa erros de todas as coletas pendentes para nova tentativa
     * Usa método do repository ao invés de acessar database diretamente
     */
    fun clearAllErrors() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // v2.7: Usar método do repository ao invés de acessar database diretamente
                val result = repository.limparTodosErrosColetas()
                
                result.fold(
                    onSuccess = { quantidade ->
                        android.util.Log.d("PendingCollectionsVM", "✓ Erros limpos de $quantidade coletas via repository")
                    },
                    onFailure = { error ->
                        android.util.Log.w("PendingCollectionsVM", "Não foi possível limpar erros: ${error.message}")
                    }
                )
                
                // Recarregar lista
                val pendingCollections = repository.getColetasPendentes()
                android.util.Log.d("PendingCollectionsVM", "📋 Lista recarregada: ${pendingCollections.size} coletas pendentes")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingCollections = pendingCollections,
                    successMessage = "Erros limpos. Tente sincronizar novamente."
                )
                
            } catch (e: Exception) {
                android.util.Log.e("PendingCollectionsVM", "Erro ao limpar erros", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao limpar: ${e.message}"
                )
            }
        }
    }
}

class PendingCollectionsViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PendingCollectionsViewModel::class.java)) {
            return PendingCollectionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}