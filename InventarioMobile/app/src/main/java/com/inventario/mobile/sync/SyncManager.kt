package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.dao.SincronizacaoDao
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.remote.api.ApiClient
import com.inventario.mobile.utils.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de sincronização offline
 * Responsável por sincronizar dados locais com o servidor
 */
class SyncManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "inventario_sync_work"
        private const val SYNC_INTERVAL_MINUTES = 15L
        
        @Volatile
        private var instance: SyncManager? = null
        
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val workManager = WorkManager.getInstance(context)
    private val networkChecker = NetworkChecker.getInstance(context)
    private val database = InventarioDatabase.getDatabase(context)
    
    /**
     * Agenda sincronização periódica em background
     */
    fun agendarSincronizacaoPeriodica() {
        Log.d(TAG, "Agendando sincronização periódica (a cada $SYNC_INTERVAL_MINUTES minutos)")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        Log.d(TAG, "Sincronização periódica agendada com sucesso")
    }
    
    /**
     * Cancela sincronização periódica
     */
    fun cancelarSincronizacaoPeriodica() {
        Log.d(TAG, "Cancelando sincronização periódica")
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Força sincronização imediata
     */
    fun sincronizarAgora() {
        Log.d(TAG, "Forçando sincronização imediata")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniqueWork(
            "sync_now",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    /**
     * Verifica se há dados pendentes de sincronização
     */
    suspend fun hasPendingSync(): Boolean = withContext(Dispatchers.IO) {
        val coletasPendentes = database.coletaDao().countPendentes()
        Log.d(TAG, "Dados pendentes: $coletasPendentes coletas")
        return@withContext coletasPendentes > 0
    }
    
    /**
     * Obtém contagem de itens pendentes
     */
    suspend fun getPendingCount(): Int = withContext(Dispatchers.IO) {
        return@withContext database.coletaDao().countPendentes()
    }
    
    /**
     * Executa sincronização manual
     */
    suspend fun executarSincronizacao(): SyncResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando sincronização manual")
        
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sem conexão com internet, sincronização cancelada")
            return@withContext SyncResult.NoConnection
        }
        
        try {
            val apiService = ApiClient.getApiService(context)
            val coletaDao = database.coletaDao()
            
            // Buscar coletas pendentes
            val coletasPendentes = coletaDao.getPendentes()
            Log.d(TAG, "Encontradas ${coletasPendentes.size} coletas pendentes")
            
            var sucessos = 0
            var falhas = 0
            
            // Sincronizar cada coleta
            coletasPendentes.forEach { coleta ->
                try {
                    // Converter para DTO e enviar
                    val dto = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
                        numeroPatrimonio = coleta.numeroPatrimonio,
                        idInventario = 0, // TODO: Obter do contexto
                        usuarioId = coleta.idUsuario,
                        idSala = coleta.idSala,
                        localizacaoEncontrada = coleta.nomeSala,
                        estadoEncontrado = coleta.estadoPatrimonio ?: "BOM",
                        observacaoColeta = coleta.observacao,
                        dataColeta = null, // Será preenchido pelo servidor
                        latitude = coleta.latitude,
                        longitude = coleta.longitude
                    )
                    
                    val response = apiService.createColeta(dto)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val servidorId = response.body()?.data?.id?.toLong()
                        coletaDao.marcarSincronizada(coleta.id, servidorId)
                        sucessos++
                        Log.d(TAG, "Coleta ${coleta.id} sincronizada com sucesso (servidor ID: $servidorId)")
                    } else {
                        coletaDao.incrementarTentativas(coleta.id, response.message())
                        falhas++
                        Log.w(TAG, "Falha ao sincronizar coleta ${coleta.id}: ${response.message()}")
                    }
                } catch (e: Exception) {
                    coletaDao.incrementarTentativas(coleta.id, e.message)
                    falhas++
                    Log.e(TAG, "Erro ao sincronizar coleta ${coleta.id}", e)
                }
            }
            
            Log.d(TAG, "Sincronização concluída: $sucessos sucessos, $falhas falhas")
            
            return@withContext if (falhas == 0) {
                SyncResult.Success(sucessos)
            } else {
                SyncResult.PartialSuccess(sucessos, falhas)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização", e)
            return@withContext SyncResult.Error(e.message ?: "Erro desconhecido")
        }
    }
}

/**
 * Resultado da sincronização
 */
sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data class PartialSuccess(val success: Int, val failures: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    object NoConnection : SyncResult()
}

/**
 * Worker para sincronização em background
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronização em background")
        
        val syncManager = SyncManager.getInstance(applicationContext)
        
        return when (val result = syncManager.executarSincronizacao()) {
            is SyncResult.Success -> {
                Log.d(TAG, "Sincronização bem-sucedida: ${result.count} itens")
                Result.success()
            }
            is SyncResult.PartialSuccess -> {
                Log.w(TAG, "Sincronização parcial: ${result.success} sucessos, ${result.failures} falhas")
                Result.retry()
            }
            is SyncResult.Error -> {
                Log.e(TAG, "Erro na sincronização: ${result.message}")
                Result.retry()
            }
            is SyncResult.NoConnection -> {
                Log.w(TAG, "Sem conexão, tentando novamente depois")
                Result.retry()
            }
        }
    }
}
