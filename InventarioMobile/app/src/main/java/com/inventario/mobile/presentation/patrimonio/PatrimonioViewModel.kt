package com.inventario.mobile.presentation.patrimonio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.observer.ConnectivityObserver
import com.inventario.mobile.data.repository.PatrimonioRepositoryImpl
import com.inventario.mobile.data.strategy.DataSourceType
import com.inventario.mobile.data.model.Patrimonio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que usa o Repository com Strategy Pattern
 * Exemplo de uso dos padrões implementados
 */
class PatrimonioViewModel(
    application: Application,
    private val repository: PatrimonioRepositoryImpl
) : AndroidViewModel(application) {
    
    // Estado da UI
    private val _uiState = MutableStateFlow<PatrimonioUiState>(PatrimonioUiState.Idle)
    val uiState: StateFlow<PatrimonioUiState> = _uiState.asStateFlow()
    
    // Status de conectividade
    private val _connectivityStatus = MutableStateFlow(ConnectivityObserver.Status.UNAVAILABLE)
    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = _connectivityStatus.asStateFlow()
    
    // Fonte de dados atual
    private val _dataSource = MutableStateFlow(DataSourceType.REMOTE)
    val dataSource: StateFlow<DataSourceType> = _dataSource.asStateFlow()
    
    init {
        observeConnectivity()
        observeDataSource()
    }
    
    /**
     * Observa mudanças de conectividade
     */
    private fun observeConnectivity() {
        viewModelScope.launch {
            repository.observeConnectivity().collect { status ->
                _connectivityStatus.value = status
                
                // Atualizar UI quando conectividade mudar
                when (status) {
                    ConnectivityObserver.Status.AVAILABLE -> {
                        // Conectou - pode recarregar dados do servidor
                    }
                    ConnectivityObserver.Status.LOST,
                    ConnectivityObserver.Status.UNAVAILABLE -> {
                        // Desconectou - vai usar dados locais automaticamente
                    }
                    ConnectivityObserver.Status.LOSING -> {
                        // Perdendo conexão - avisar usuário
                    }
                }
            }
        }
    }
    
    /**
     * Observa mudanças na fonte de dados
     */
    private fun observeDataSource() {
        viewModelScope.launch {
            repository.currentDataSource.collect { source ->
                _dataSource.value = source
            }
        }
    }
    
    /**
     * Carrega patrimônios (usa estratégia automática)
     */
    fun loadPatrimonios() {
        viewModelScope.launch {
            _uiState.value = PatrimonioUiState.Loading
            
            val result = repository.getPatrimonios()
            
            _uiState.value = if (result.isSuccess) {
                val patrimonios = result.getOrNull() ?: emptyList()
                PatrimonioUiState.Success(patrimonios)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                PatrimonioUiState.Error(error)
            }
        }
    }
    
    /**
     * Busca patrimônio por número (usa estratégia automática)
     */
    fun searchPatrimonio(numero: String) {
        viewModelScope.launch {
            _uiState.value = PatrimonioUiState.Loading
            
            val result = repository.getPatrimonioPorNumero(numero)
            
            _uiState.value = if (result.isSuccess) {
                val patrimonio = result.getOrNull()
                if (patrimonio != null) {
                    PatrimonioUiState.Success(listOf(patrimonio))
                } else {
                    PatrimonioUiState.Error("Patrimônio não encontrado")
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                PatrimonioUiState.Error(error)
            }
        }
    }
    
    /**
     * Força atualização do servidor
     */
    fun forceRefreshFromServer() {
        viewModelScope.launch {
            _uiState.value = PatrimonioUiState.Loading
            
            val result = repository.getPatrimoniosFromRemote()
            
            _uiState.value = if (result.isSuccess) {
                val patrimonios = result.getOrNull() ?: emptyList()
                PatrimonioUiState.Success(patrimonios)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro ao conectar ao servidor"
                PatrimonioUiState.Error(error)
            }
        }
    }
    
    /**
     * Usa dados locais (offline)
     */
    fun useLocalData() {
        viewModelScope.launch {
            _uiState.value = PatrimonioUiState.Loading
            
            val result = repository.getPatrimoniosFromLocal()
            
            _uiState.value = if (result.isSuccess) {
                val patrimonios = result.getOrNull() ?: emptyList()
                PatrimonioUiState.Success(patrimonios)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Banco local vazio"
                PatrimonioUiState.Error(error)
            }
        }
    }
}

/**
 * Estados da UI
 */
sealed class PatrimonioUiState {
    object Idle : PatrimonioUiState()
    object Loading : PatrimonioUiState()
    data class Success(val patrimonios: List<Patrimonio>) : PatrimonioUiState()
    data class Error(val message: String) : PatrimonioUiState()
}
