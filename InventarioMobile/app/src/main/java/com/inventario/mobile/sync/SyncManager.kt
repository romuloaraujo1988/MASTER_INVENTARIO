package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.inventario.mobile.worker.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de sincronização em background
 * Agenda e controla sincronização automática de coletas pendentes
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "sync_coletas_periodico"
        private const val SYNC_INTERVAL_MINUTES = 30L // Sincronizar a cada 30 minutos
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Agenda sincronização periódica em background
     */
    fun schedulePeriodicSync() {
        Log.d(TAG, "Agendando sincronização periódica (a cada $SYNC_INTERVAL_MINUTES minutos)")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Apenas com internet
            .setRequiresBatteryNotLow(true) // Apenas se bateria não estiver baixa
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
            .addTag("sync")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Manter se já existir
            syncRequest
        )
        
        Log.d(TAG, "✓ Sincronização periódica agendada")
    }
    
    /**
     * Cancela sincronização periódica
     */
    fun cancelPeriodicSync() {
        Log.d(TAG, "Cancelando sincronização periódica")
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Força sincronização imediata (one-time)
     */
    fun forceSyncNow() {
        Log.d(TAG, "Forçando sincronização imediata")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("sync_manual")
            .build()
        
        workManager.enqueueUniqueWork(
            "sync_manual_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        
        Log.d(TAG, "✓ Sincronização imediata agendada")
    }
    
    /**
     * Verifica status da sincronização
     */
    fun getSyncStatus(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData("sync")
    }
    
    /**
     * Verifica se há sincronização em andamento
     */
    suspend fun isSyncing(): Boolean {
        val workInfos = workManager.getWorkInfosByTag("sync").await()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
}
