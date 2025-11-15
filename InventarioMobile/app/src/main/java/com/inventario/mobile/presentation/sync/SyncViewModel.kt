package com.inventario.mobile.presentation.sync

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.migration.ColetaMigration
import com.inventario.mobile.data.migration.MigrationResult
import com.inventario.mobile.domain.usecase.SincronizarColetasDoServidorUseCase
import com.inventario.mobile.domain.usecase.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para sincronização de dados
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val sincronizarColetasUseCase: SincronizarColetasDoServidorUseCase,
    private val enviarColetasPendentesUseCase: com.inventario.mobile.domain.usecase.EnviarColetasPendentesUseCase,
    private val coletaMigration: ColetaMigration
) : ViewModel() {
    
    companion object {
        private const val TAG = "SyncViewModel"
    }
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    /**
     * Sincroniza coletas do servidor
     */
    fun sincronizarColetas() {
        viewModelScope.launch {
            Log.d(TAG, "Iniciando sincronização de coletas...")
            _syncState.value = SyncState.Syncing("Baixando coletas do servidor...")
            
            sincronizarColetasUseCase().fold(
                onSuccess = { result ->
                    Log.d(TAG, "✓ Sincronização concluída: ${result.sucesso} coletas")
                    _syncState.value = SyncState.Success(
                        message = "Sincronização concluída",
                        syncResult = result
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "✗ Erro na sincronização", error)
                    _syncState.value = SyncState.Error(
                        error.message ?: "Erro ao sincronizar coletas"
                    )
                }
            )
        }
    }
    
    /**
     * Migra coletas antigas com dados incompletos
     */
    fun migrarColetasAntigas() {
        viewModelScope.launch {
            Log.d(TAG, "Iniciando migração de coletas antigas...")
            _syncState.value = SyncState.Syncing("Atualizando coletas antigas...")
            
            coletaMigration.migrarColetasAntigas().fold(
                onSuccess = { result ->
                    Log.d(TAG, "✓ Migração concluída: ${result.atualizadas} coletas")
                    _syncState.value = SyncState.MigrationSuccess(
                        message = "Migração concluída",
                        migrationResult = result
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "✗ Erro na migração", error)
                    _syncState.value = SyncState.Error(
                        error.message ?: "Erro ao migrar coletas"
                    )
                }
            )
        }
    }
    
    /**
     * Envia coletas pendentes para o servidor
     */
    fun enviarColetasPendentes() {
        viewModelScope.launch {
            Log.d(TAG, "Enviando coletas pendentes...")
            _syncState.value = SyncState.Syncing("Enviando coletas pendentes...")
            
            enviarColetasPendentesUseCase().fold(
                onSuccess = { result ->
                    Log.d(TAG, "✓ Upload concluído: ${result.sucesso} coletas")
                    _syncState.value = SyncState.UploadSuccess(
                        message = "Upload concluído",
                        uploadResult = result
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "✗ Erro no upload", error)
                    _syncState.value = SyncState.Error(
                        error.message ?: "Erro ao enviar coletas"
                    )
                }
            )
        }
    }
    
    /**
     * Sincroniza bidirecional (download + upload + migração)
     */
    fun sincronizarCompleto() {
        viewModelScope.launch {
            Log.d(TAG, "Iniciando sincronização completa (bidirecional)...")
            
            // 1. Enviar coletas pendentes
            _syncState.value = SyncState.Syncing("Enviando coletas pendentes...")
            
            val uploadResult = enviarColetasPendentesUseCase()
            
            if (uploadResult.isFailure) {
                Log.w(TAG, "⚠ Erro ao enviar pendentes, continuando...")
            }
            
            // 2. Baixar coletas do servidor
            _syncState.value = SyncState.Syncing("Baixando coletas do servidor...")
            
            val syncResult = sincronizarColetasUseCase()
            
            if (syncResult.isFailure) {
                _syncState.value = SyncState.Error(
                    syncResult.exceptionOrNull()?.message ?: "Erro na sincronização"
                )
                return@launch
            }
            
            // 3. Migrar coletas antigas
            _syncState.value = SyncState.Syncing("Atualizando coletas antigas...")
            
            val migrationResult = coletaMigration.migrarColetasAntigas()
            
            if (migrationResult.isFailure) {
                _syncState.value = SyncState.Error(
                    migrationResult.exceptionOrNull()?.message ?: "Erro na migração"
                )
                return@launch
            }
            
            // 4. Sucesso
            _syncState.value = SyncState.CompleteSuccess(
                message = "Sincronização completa",
                syncResult = syncResult.getOrNull()!!,
                migrationResult = migrationResult.getOrNull()!!,
                uploadResult = uploadResult.getOrNull()
            )
            
            Log.d(TAG, "✓ Sincronização completa concluída")
        }
    }
    
    /**
     * Verifica se precisa migração
     */
    fun verificarMigracao() {
        viewModelScope.launch {
            val precisa = coletaMigration.precisaMigracao()
            if (precisa) {
                Log.d(TAG, "⚠ Coletas antigas precisam de migração")
                _syncState.value = SyncState.NeedsMigration
            }
        }
    }
    
    /**
     * Limpa o estado
     */
    fun limparEstado() {
        _syncState.value = SyncState.Idle
    }
}

/**
 * Estados da sincronização
 */
sealed class SyncState {
    object Idle : SyncState()
    object NeedsMigration : SyncState()
    data class Syncing(val message: String) : SyncState()
    data class Success(
        val message: String,
        val syncResult: SyncResult
    ) : SyncState()
    data class UploadSuccess(
        val message: String,
        val uploadResult: com.inventario.mobile.domain.usecase.UploadResult
    ) : SyncState()
    data class MigrationSuccess(
        val message: String,
        val migrationResult: MigrationResult
    ) : SyncState()
    data class CompleteSuccess(
        val message: String,
        val syncResult: SyncResult,
        val migrationResult: MigrationResult,
        val uploadResult: com.inventario.mobile.domain.usecase.UploadResult?
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}
