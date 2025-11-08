package com.inventario.mobile.data.sync

import android.content.Context
import android.util.Log
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Gerenciador de sincronização offline
 * Sincroniza automaticamente dados pendentes quando a conexão é restaurada
 */
class SyncManager(
    private val context: Context,
    private val database: InventarioDatabase,
    private val apiService: ApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val networkMonitor = NetworkMonitor(context)
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    private var isAutoSyncEnabled = true
    private var isSyncing = false

    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_RETRY_DELAY = 5000L // 5 segundos
        private const val MAX_RETRY_ATTEMPTS = 3
        
        @Volatile
        private var instance: SyncManager? = null
        
        fun getInstance(context: Context, database: InventarioDatabase, apiService: ApiService): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context, database, apiService).also { instance = it }
            }
        }
    }

    init {
        startMonitoring()
    }

    /**
     * Inicia o monitoramento de rede e sincronização automática
     */
    private fun startMonitoring() {
        scope.launch {
            // Monitorar mudanças na conectividade
            networkMonitor.isOnline.collect { isOnline ->
                Log.d(TAG, "Status de rede alterado: ${if (isOnline) "ONLINE" else "OFFLINE"}")
                
                if (isOnline && isAutoSyncEnabled) {
                    // Aguardar um pouco para garantir que a conexão está estável
                    delay(2000)
                    syncPendingData()
                }
            }
        }
        
        // Atualizar contador de pendentes periodicamente
        scope.launch {
            while (true) {
                updatePendingCount()
                delay(10000) // A cada 10 segundos
            }
        }
    }

    /**
     * Sincroniza todos os dados pendentes
     */
    suspend fun syncPendingData(): SyncResult {
        if (isSyncing) {
            Log.d(TAG, "Sincronização já em andamento")
            return SyncResult.AlreadySyncing
        }
        
        if (!networkMonitor.isCurrentlyOnline()) {
            Log.d(TAG, "Sem conexão - sincronização cancelada")
            return SyncResult.NoConnection
        }
        
        isSyncing = true
        _syncState.value = SyncState.Syncing(0, 0)
        
        return try {
            val result = performSync()
            _syncState.value = SyncState.Success(result)
            updatePendingCount()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante sincronização", e)
            _syncState.value = SyncState.Error(e.message ?: "Erro desconhecido")
            SyncResult.Error(e)
        } finally {
            isSyncing = false
        }
    }

    /**
     * Executa a sincronização de fato
     */
    private suspend fun performSync(): SyncResult {
        val coletasDao = database.coletaDao()
        val pendingColetas = coletasDao.getPendingSync()
        
        if (pendingColetas.isEmpty()) {
            Log.d(TAG, "Nenhuma coleta pendente para sincronizar")
            return SyncResult.Success(0, 0, 0)
        }
        
        Log.d(TAG, "Sincronizando ${pendingColetas.size} coletas pendentes")
        
        var successCount = 0
        var errorCount = 0
        val errors = mutableListOf<SyncError>()
        
        pendingColetas.forEachIndexed { index, coleta ->
            _syncState.value = SyncState.Syncing(index + 1, pendingColetas.size)
            
            try {
                syncColeta(coleta)
                successCount++
                
                // Marcar como sincronizada
                coletasDao.update(coleta.copy(
                    sincronizada = true,
                    tentativasSincronizacao = 0,
                    ultimaTentativaSincronizacao = System.currentTimeMillis()
                ))
                
            } catch (e: Exception) {
                errorCount++
                Log.e(TAG, "Erro ao sincronizar coleta ${coleta.id}", e)
                
                errors.add(SyncError(
                    coletaId = coleta.id,
                    error = e.message ?: "Erro desconhecido"
                ))
                
                // Incrementar tentativas
                val tentativas = coleta.tentativasSincronizacao + 1
                coletasDao.update(coleta.copy(
                    tentativasSincronizacao = tentativas,
                    ultimaTentativaSincronizacao = System.currentTimeMillis(),
                    erroSincronizacao = if (tentativas >= MAX_RETRY_ATTEMPTS) e.message else null
                ))
            }
            
            // Pequeno delay entre requisições
            delay(500)
        }
        
        Log.d(TAG, "Sincronização concluída - Sucesso: $successCount, Erros: $errorCount")
        
        return SyncResult.Success(
            total = pendingColetas.size,
            success = successCount,
            errors = errorCount,
            errorDetails = errors
        )
    }

    /**
     * Sincroniza uma coleta individual
     */
    private suspend fun syncColeta(coleta: ColetaEntity) {
        // Converter para DTO e enviar para API
        val request = coleta.toRequest()
        
        if (coleta.servidorId == null) {
            // Nova coleta - criar no servidor
            val response = apiService.createColeta(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val coletaDto = response.body()?.data
                // Atualizar ID do servidor
                database.coletaDao().update(coleta.copy(
                    servidorId = coletaDto?.id?.toLong()
                ))
            } else {
                throw Exception("Erro ao criar coleta: ${response.message()}")
            }
        } else {
            // Coleta existente - atualizar no servidor
            val response = apiService.updateColeta(coleta.servidorId!!, request)
            
            if (!response.isSuccessful || response.body()?.success != true) {
                throw Exception("Erro ao atualizar coleta: ${response.message()}")
            }
        }
    }

    /**
     * Atualiza o contador de itens pendentes
     */
    private suspend fun updatePendingCount() {
        val count = database.coletaDao().getPendingCount()
        _pendingCount.value = count
        Log.d(TAG, "Itens pendentes: $count")
    }

    /**
     * Marca uma coleta como pendente de sincronização
     */
    suspend fun markAsPending(coletaId: Long) = withContext(Dispatchers.IO) {
        val coleta = database.coletaDao().getById(coletaId)
        coleta?.let {
            database.coletaDao().update(it.copy(
                sincronizada = false,
                tentativasSincronizacao = 0
            ))
            updatePendingCount()
        }
    }

    /**
     * Habilita/desabilita sincronização automática
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        isAutoSyncEnabled = enabled
        Log.d(TAG, "Sincronização automática: ${if (enabled) "HABILITADA" else "DESABILITADA"}")
    }

    /**
     * Força sincronização imediata
     */
    fun forceSyncNow() {
        scope.launch {
            syncPendingData()
        }
    }

    /**
     * Limpa erros de sincronização e tenta novamente
     */
    suspend fun retryFailedSync() {
        val coletasDao = database.coletaDao()
        // Buscar coletas não sincronizadas (pendentes)
        val failedColetas = coletasDao.getColetasPendentes()
        
        // Não precisa fazer nada, apenas tentar sincronizar novamente
        Log.d(TAG, "Tentando sincronizar ${failedColetas.size} coletas pendentes")
        
        syncPendingData()
    }
}

/**
 * Estados da sincronização
 */
sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val current: Int, val total: Int) : SyncState()
    data class Success(val result: SyncResult) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Resultado da sincronização
 */
sealed class SyncResult {
    data class Success(
        val total: Int,
        val success: Int,
        val errors: Int,
        val errorDetails: List<SyncError> = emptyList()
    ) : SyncResult()
    
    data class Error(val exception: Exception) : SyncResult()
    object NoConnection : SyncResult()
    object AlreadySyncing : SyncResult()
}

/**
 * Erro de sincronização individual
 */
data class SyncError(
    val coletaId: Long,
    val error: String
)

/**
 * Extensão para converter ColetaEntity em MobileColetaRequest
 */
private fun ColetaEntity.toRequest(): com.inventario.mobile.data.remote.dto.MobileColetaRequest {
    // TODO: Obter número do patrimônio e ID do inventário do banco
    return com.inventario.mobile.data.remote.dto.MobileColetaRequest(
        numeroPatrimonio = patrimonioId.toString(), // TODO: buscar número real
        idInventario = 1, // TODO: obter ID do inventário ativo
        usuarioId = usuarioId.toInt(),
        idSala = null, // TODO: obter da localizacaoAtual se disponível
        localizacaoEncontrada = localizacaoAtual,
        estadoEncontrado = status,
        observacaoColeta = observacoes,
        dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(dataColeta)),
        latitude = latitude,
        longitude = longitude,
        fotoPatrimonio = fotoPath,
        semEtiqueta = false,
        descricaoItemSemEtiqueta = null,
        categoriaItemSemEtiqueta = null,
        deviceId = null,
        appVersion = null,
        divergencia = false,
        motivoDivergencia = null
    )
}
