package com.inventario.mobile.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyncUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String? = null,
    val pendingSyncCount: Int = 0,
    val syncProgress: Int = 0,
    val syncMessage: String = "",
    val errorMessage: String? = null,
    val syncSuccess: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false
)

class SyncViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        loadSyncStatus()
    }

    fun loadSyncStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val lastSyncTime = repository.getLastSyncTime()
                val coletasPendentes = repository.getColetasPendentes()
                val pendingCount = coletasPendentes.size
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastSyncTime = lastSyncTime,
                    pendingSyncCount = pendingCount,
                    syncMessage = if (pendingCount == 0) "Todos os dados estão sincronizados" 
                                 else "${pendingCount} itens aguardando sincronização"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar status: ${e.message}"
                )
            }
        }
    }

    fun startSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncProgress = 0,
                syncMessage = "Iniciando sincronização...",
                errorMessage = null,
                syncSuccess = false
            )

            try {
                updateSyncProgress(10, "Verificando conexão...")
                
                // Buscar coletas pendentes antes da sincronização
                val coletasPendentesAntes = repository.getColetasPendentes()
                val countAntes = coletasPendentesAntes.size
                
                updateSyncProgress(30, "Enviando ${countAntes} coletas pendentes...")
                
                // Realizar sincronização real
                val syncResult = repository.syncData()
                
                if (syncResult.isSuccess) {
                    updateSyncProgress(60, "Recebendo atualizações do servidor...")
                    
                    // Verificar se há coletas pendentes após sincronização
                    val coletasPendentesDepois = repository.getColetasPendentes()
                    val countDepois = coletasPendentesDepois.size
                    
                    updateSyncProgress(90, "Finalizando sincronização...")
                    
                    val mensagemFinal = if (countAntes > 0) {
                        if (countDepois == 0) {
                            "Sincronização concluída! ${countAntes} coletas enviadas com sucesso."
                        } else {
                            "Sincronização parcial. ${countAntes - countDepois} de ${countAntes} coletas enviadas."
                        }
                    } else {
                        "Sincronização concluída! Dados atualizados do servidor."
                    }
                    
                    updateSyncProgress(100, mensagemFinal)
                    
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        syncSuccess = true,
                        pendingSyncCount = countDepois,
                        lastSyncTime = getCurrentTime(),
                        syncMessage = mensagemFinal
                    )
                } else {
                    throw syncResult.exceptionOrNull() ?: Exception("Erro desconhecido na sincronização")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Erro durante a sincronização: ${e.message}",
                    syncMessage = "Falha na sincronização"
                )
            }
        }
    }

    private fun updateSyncProgress(progress: Int, message: String) {
        _uiState.value = _uiState.value.copy(
            syncProgress = progress,
            syncMessage = message
        )
    }

    private fun getLastSyncTime(): String {
        // Implementar lógica para obter último tempo de sincronização
        // Por enquanto, retorna um valor simulado
        return "Hoje às 14:30"
    }

    fun clearPendingCollections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
            
            try {
                // Buscar coletas pendentes de sincronização
                val coletasPendentes = repository.getColetasPendentes()
                
                if (coletasPendentes.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = "Não há coletas pendentes para excluir"
                    )
                    return@launch
                }
                
                // Remover coletas pendentes
                for (coleta in coletasPendentes) {
                    repository.removeColeta(coleta.patrimonioId)
                }
                
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = true,
                    syncMessage = "${coletasPendentes.size} coletas pendentes foram excluídas com sucesso"
                )
                
                // Recarregar status após exclusão
                loadSyncStatus()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Erro ao excluir coletas pendentes: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            deleteSuccess = false
        )
    }

    private fun getCurrentTime(): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(syncSuccess = false)
    }
}

class SyncViewModelFactory(
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}