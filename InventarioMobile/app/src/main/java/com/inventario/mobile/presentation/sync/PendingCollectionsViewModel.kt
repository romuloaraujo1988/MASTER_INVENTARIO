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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val pendingCollections = repository.getColetasPendentes()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingCollections = pendingCollections
                )
            } catch (e: Exception) {
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