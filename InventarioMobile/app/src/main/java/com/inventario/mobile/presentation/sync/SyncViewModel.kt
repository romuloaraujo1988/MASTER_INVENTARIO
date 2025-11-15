package com.inventario.mobile.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.repository.SyncRepository
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.domain.usecase.SincronizarDadosUseCase
import com.inventario.mobile.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de sincronização
 * Gerencia sincronização de dados e coletas pendentes
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val sincronizarDadosUseCase: SincronizarDadosUseCase,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()
    
    private val _stats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stats: StateFlow<Map<String, Int>> = _stats.asStateFlow()
    
    init {
        loadStats()
    }
    
    /**
     * Carrega estatísticas dos dados locais
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                val localStats = sincronizarDadosUseCase.getLocalStats()
                _stats.value = localStats
            } catch (e: Exception) {
                android.util.Log.e("SyncViewModel", "Erro ao carregar estatísticas", e)
            }
        }
    }
    
    /**
     * Sincroniza todos os dados do servidor
     */
    fun syncFromServer() {
        viewModelScope.launch {
            _state.value = SyncState.Loading("Sincronizando dados do servidor...")
            
            val result = sincronizarDadosUseCase()
            
            if (result.isSuccess) {
                val syncResult = result.getOrNull()!!
                _state.value = SyncState.Success(
                    message = "Sincronização concluída!",
                    patrimoniosSincronizados = syncResult.patrimoniosSincronizados,
                    salasSincronizadas = syncResult.salasSincronizadas,
                    tempoDecorrido = syncResult.tempoDecorrido
                )
                loadStats() // Atualizar estatísticas
            } else {
                val error = result.exceptionOrNull()
                _state.value = SyncState.Error(
                    error?.message ?: "Erro desconhecido na sincronização"
                )
            }
        }
    }
    
    /**
     * Sincroniza coletas pendentes
     */
    fun syncPendingColetas() {
        viewModelScope.launch {
            _state.value = SyncState.Loading("Sincronizando coletas pendentes...")
            
            val result = sincronizarColetasPendentesUseCase()
            
            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                _state.value = SyncState.ColetasSyncSuccess(
                    message = if (quantidade > 0) {
                        "$quantidade coleta(s) sincronizada(s) com sucesso!"
                    } else {
                        "Nenhuma coleta pendente para sincronizar"
                    },
                    quantidade = quantidade
                )
                loadStats() // Atualizar estatísticas
            } else {
                val error = result.exceptionOrNull()
                _state.value = SyncState.Error(
                    error?.message ?: "Erro ao sincronizar coletas"
                )
            }
        }
    }
    
    /**
     * Limpa todos os dados locais
     */
    fun clearLocalData() {
        viewModelScope.launch {
            _state.value = SyncState.Loading("Limpando dados locais...")
            
            val result = syncRepository.clearLocalData()
            
            if (result.isSuccess) {
                _state.value = SyncState.Success(
                    message = "Dados locais limpos com sucesso!",
                    patrimoniosSincronizados = 0,
                    salasSincronizadas = 0,
                    tempoDecorrido = 0
                )
                loadStats() // Atualizar estatísticas
            } else {
                val error = result.exceptionOrNull()
                _state.value = SyncState.Error(
                    error?.message ?: "Erro ao limpar dados"
                )
            }
        }
    }
    
    /**
     * Agenda sincronização periódica em background
     */
    fun schedulePeriodicSync() {
        syncManager.schedulePeriodicSync()
    }
    
    /**
     * Cancela sincronização periódica
     */
    fun cancelPeriodicSync() {
        syncManager.cancelPeriodicSync()
    }
    
    /**
     * Força sincronização imediata em background
     */
    fun forceSyncNow() {
        syncManager.forceSyncNow()
    }
    
    /**
     * Limpa estado para Idle
     */
    fun clearState() {
        _state.value = SyncState.Idle
    }
}

/**
 * Estados da tela de sincronização
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Loading(val message: String) : SyncState()
    data class Success(
        val message: String,
        val patrimoniosSincronizados: Int,
        val salasSincronizadas: Int,
        val tempoDecorrido: Long
    ) : SyncState()
    data class ColetasSyncSuccess(
        val message: String,
        val quantidade: Int
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}
