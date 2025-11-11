package com.inventario.presentation.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import kotlinx.coroutines.launch
import java.util.*

class SyncViewModel(
    private val repository: PatrimonioRepositoryImpl
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSyncing = MutableLiveData<Boolean>()
    val isSyncing: LiveData<Boolean> = _isSyncing

    private val _isDeleting = MutableLiveData<Boolean>()
    val isDeleting: LiveData<Boolean> = _isDeleting

    private val _lastSyncTime = MutableLiveData<Date?>()
    val lastSyncTime: LiveData<Date?> = _lastSyncTime

    private val _pendingSyncCount = MutableLiveData<Int>()
    val pendingSyncCount: LiveData<Int> = _pendingSyncCount

    private val _syncProgress = MutableLiveData<Int>()
    val syncProgress: LiveData<Int> = _syncProgress

    private val _syncMessage = MutableLiveData<String>()
    val syncMessage: LiveData<String> = _syncMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun loadSyncStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Verificar quantos patrimônios precisam ser sincronizados
                val patrimonios = repository.getAllPatrimoniosList()
                val pendingSync = patrimonios.filter { patrimonio -> patrimonio.coletado && !patrimonio.sincronizado }
                
                _pendingSyncCount.value = pendingSync.size
                _lastSyncTime.value = getLastSyncTime()
                _syncMessage.value = if (pendingSync.isEmpty()) "Todos os dados estão sincronizados" 
                                   else "${pendingSync.size} itens aguardando sincronização"
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro ao verificar status de sincronização: ${e.message}"
            }
        }
    }

    fun startSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncProgress.value = 0
            _errorMessage.value = null
            
            try {
                // Simular processo de sincronização
                _syncMessage.value = "Iniciando sincronização..."
                _syncProgress.value = 25
                
                // Aqui você implementaria a lógica real de sincronização
                // Por enquanto, vamos simular
                kotlinx.coroutines.delay(1000)
                _syncMessage.value = "Enviando dados..."
                _syncProgress.value = 50
                
                kotlinx.coroutines.delay(1000)
                _syncMessage.value = "Processando no servidor..."
                _syncProgress.value = 75
                
                kotlinx.coroutines.delay(1000)
                _syncMessage.value = "Finalizando..."
                _syncProgress.value = 100
                
                kotlinx.coroutines.delay(500)
                _syncMessage.value = "Sincronização concluída com sucesso!"
                _isSyncing.value = false
                
                // Recarregar status após sincronização
                loadSyncStatus()
                
            } catch (e: Exception) {
                _isSyncing.value = false
                _errorMessage.value = "Erro durante a sincronização: ${e.message}"
            }
        }
    }

    fun clearPendingCollections() {
        viewModelScope.launch {
            _isDeleting.value = true
            _errorMessage.value = null
            
            try {
                // Buscar todos os patrimônios coletados mas não sincronizados
                val allPatrimonios = repository.getAllPatrimoniosList()
                val pendingCollections = allPatrimonios.filter { patrimonio -> patrimonio.coletado && !patrimonio.sincronizado }
                
                if (pendingCollections.isEmpty()) {
                    _isDeleting.value = false
                    _errorMessage.value = "Não há coletas pendentes para excluir"
                    return@launch
                }
                
                // Marcar como não coletado (limpar coleta)
                for (patrimonio in pendingCollections) {
                    val updatedPatrimonio = patrimonio.copy(
                        coletado = false,
                        dataColeta = null,
                        coletorId = null
                    )
                    repository.updatePatrimonio(updatedPatrimonio)
                }
                
                _isDeleting.value = false
                _deleteSuccess.value = true
                _syncMessage.value = "${pendingCollections.size} coletas pendentes foram excluídas com sucesso"
                
                // Recarregar status após exclusão
                loadSyncStatus()
                
            } catch (e: Exception) {
                _isDeleting.value = false
                _errorMessage.value = "Erro ao excluir coletas pendentes: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _deleteSuccess.value = false
    }

    private fun getLastSyncTime(): Date? {
        // Implementar lógica para obter último tempo de sincronização
        // Por enquanto, retorna um valor simulado
        return Date()
    }
}

class SyncViewModelFactory(
    private val repository: PatrimonioRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}