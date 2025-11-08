package com.inventario.mobile.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.inventario.mobile.worker.DatabaseCleanupWorker
import com.inventario.mobile.worker.SmartSyncWorker
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de agendamento de sincronização e limpeza
 */
object SyncScheduler {
    
    private const val TAG = "SyncScheduler"
    
    /**
     * Agenda sincronização inteligente em background
     * 
     * @param context Contexto da aplicação
     * @param intervalMinutes Intervalo entre sincronizações (padrão: 15 minutos)
     */
    fun scheduleSmartSync(context: Context, intervalMinutes: Long = 15) {
        Log.d(TAG, "Agendando sincronização inteligente (intervalo: ${intervalMinutes}min)")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SmartSyncWorker>(
            intervalMinutes, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // Flex interval - pode executar até 5 min antes/depois
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("sync")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SmartSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Manter trabalho existente
                syncRequest
            )
        
        Log.d(TAG, "Sincronização inteligente agendada com sucesso")
    }
    
    /**
     * Cancela sincronização inteligente
     */
    fun cancelSmartSync(context: Context) {
        Log.d(TAG, "Cancelando sincronização inteligente")
        
        WorkManager.getInstance(context)
            .cancelUniqueWork(SmartSyncWorker.WORK_NAME)
        
        Log.d(TAG, "Sincronização inteligente cancelada")
    }
    
    /**
     * Agenda limpeza automática do banco de dados
     * 
     * @param context Contexto da aplicação
     * @param intervalDays Intervalo entre limpezas (padrão: 7 dias)
     */
    fun scheduleDatabaseCleanup(context: Context, intervalDays: Long = 7) {
        Log.d(TAG, "Agendando limpeza do banco de dados (intervalo: ${intervalDays} dias)")
        
        val cleanupRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(
            intervalDays, TimeUnit.DAYS
        )
            .addTag("cleanup")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DatabaseCleanupWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )
        
        Log.d(TAG, "Limpeza do banco de dados agendada com sucesso")
    }
    
    /**
     * Cancela limpeza automática
     */
    fun cancelDatabaseCleanup(context: Context) {
        Log.d(TAG, "Cancelando limpeza do banco de dados")
        
        WorkManager.getInstance(context)
            .cancelUniqueWork(DatabaseCleanupWorker.WORK_NAME)
        
        Log.d(TAG, "Limpeza do banco de dados cancelada")
    }
    
    /**
     * Força sincronização imediata (one-time)
     */
    fun forceSyncNow(context: Context) {
        Log.d(TAG, "Forçando sincronização imediata")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SmartSyncWorker>()
            .setConstraints(constraints)
            .addTag("sync")
            .addTag("manual")
            .build()
        
        WorkManager.getInstance(context)
            .enqueue(syncRequest)
        
        Log.d(TAG, "Sincronização imediata agendada")
    }
    
    /**
     * Força limpeza imediata (one-time)
     */
    fun forceCleanupNow(context: Context) {
        Log.d(TAG, "Forçando limpeza imediata")
        
        val cleanupRequest = OneTimeWorkRequestBuilder<DatabaseCleanupWorker>()
            .addTag("cleanup")
            .addTag("manual")
            .build()
        
        WorkManager.getInstance(context)
            .enqueue(cleanupRequest)
        
        Log.d(TAG, "Limpeza imediata agendada")
    }
    
    /**
     * Verifica status dos workers
     */
    fun getWorkersStatus(context: Context): WorkersStatus {
        val workManager = WorkManager.getInstance(context)
        
        val syncInfo = workManager.getWorkInfosForUniqueWork(SmartSyncWorker.WORK_NAME).get()
        val cleanupInfo = workManager.getWorkInfosForUniqueWork(DatabaseCleanupWorker.WORK_NAME).get()
        
        return WorkersStatus(
            syncScheduled = syncInfo.isNotEmpty() && syncInfo.any { !it.state.isFinished },
            cleanupScheduled = cleanupInfo.isNotEmpty() && cleanupInfo.any { !it.state.isFinished },
            syncState = syncInfo.firstOrNull()?.state?.name ?: "NOT_SCHEDULED",
            cleanupState = cleanupInfo.firstOrNull()?.state?.name ?: "NOT_SCHEDULED"
        )
    }
    
    /**
     * Cancela todos os workers
     */
    fun cancelAll(context: Context) {
        Log.d(TAG, "Cancelando todos os workers")
        
        WorkManager.getInstance(context).cancelAllWork()
        
        Log.d(TAG, "Todos os workers cancelados")
    }
}

/**
 * Status dos workers
 */
data class WorkersStatus(
    val syncScheduled: Boolean,
    val cleanupScheduled: Boolean,
    val syncState: String,
    val cleanupState: String
)
