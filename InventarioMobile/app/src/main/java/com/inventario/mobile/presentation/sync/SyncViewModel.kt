package com.inventario.mobile.presentation.sync

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.cache.SearchCache
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
 * 
 * v2.9: Adicionada limpeza de cache após sincronização
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val sincronizarDadosUseCase: SincronizarDadosUseCase,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase,
    private val syncManager: SyncManager,
    private val searchCache: SearchCache
) : ViewModel() {
    
    companion object {
        private const val TAG = "SyncViewModel"
    }
    
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
                
                // Limpar cache de busca após sincronização (dados mudaram)
                searchCache.clear()
                Log.d(TAG, "✓ Cache de busca limpo após sincronização de dados")
                
                _state.value = SyncState.Success(
                    message = "Sincronização concluída!",
                    patrimoniosSincronizados = syncResult.patrimonios,
                    salasSincronizadas = syncResult.salas,
                    responsaveisSincronizados = syncResult.responsaveis,
                    tempoDecorrido = syncResult.tempoMs
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
                
                // Limpar cache de busca após sincronização de coletas (status de coleta mudou)
                if (quantidade > 0) {
                    searchCache.clear()
                    Log.d(TAG, "✓ Cache de busca limpo após sincronização de $quantidade coleta(s)")
                }
                
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
     * Preserva coletas pendentes de sincronização
     */
    fun clearLocalData() {
        viewModelScope.launch {
            _state.value = SyncState.Loading("Limpando dados locais...")
            
            val result = sincronizarDadosUseCase.clearLocalData()
            
            if (result.isSuccess) {
                val clearResult = result.getOrNull()!!
                
                // Limpar cache de busca (dados foram removidos)
                searchCache.clear()
                Log.d(TAG, "✓ Cache de busca limpo após limpeza de dados locais")
                
                _state.value = SyncState.ClearSuccess(
                    message = buildClearSuccessMessage(clearResult),
                    patrimoniosRemovidos = clearResult.patrimoniosRemovidos,
                    salasRemovidas = clearResult.salasRemovidas,
                    responsaveisRemovidos = clearResult.responsaveisRemovidos,
                    coletasPendentesPreservadas = clearResult.coletasPendentesPreservadas
                )
                loadStats() // Atualizar estatísticas para mostrar zeros
            } else {
                val error = result.exceptionOrNull()
                _state.value = SyncState.Error(
                    error?.message ?: "Erro ao limpar dados locais"
                )
            }
        }
    }
    
    private fun buildClearSuccessMessage(result: com.inventario.mobile.data.repository.SyncRepository.ClearResult): String {
        val sb = StringBuilder("Dados locais limpos com sucesso!\n\n")
        sb.append("📦 Patrimônios removidos: ${result.patrimoniosRemovidos}\n")
        sb.append("🏢 Salas removidas: ${result.salasRemovidas}\n")
        sb.append("👤 Responsáveis removidos: ${result.responsaveisRemovidos}")
        
        if (result.coletasPendentesPreservadas > 0) {
            sb.append("\n\n⚠️ ${result.coletasPendentesPreservadas} coleta(s) pendente(s) foram preservadas")
        }
        
        return sb.toString()
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
        val responsaveisSincronizados: Int = 0,
        val tempoDecorrido: Long
    ) : SyncState()
    data class ColetasSyncSuccess(
        val message: String,
        val quantidade: Int
    ) : SyncState()
    data class ClearSuccess(
        val message: String,
        val patrimoniosRemovidos: Int,
        val salasRemovidas: Int,
        val responsaveisRemovidos: Int,
        val coletasPendentesPreservadas: Int
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}
