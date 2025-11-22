package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.inventario.mobile.worker.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de sincronização
 * Responsável por agendar e controlar sincronizações
 */
class SyncManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "periodic_sync"
        private const val SYNC_INTERVAL_MINUTES = 30L

        @Volatile
        private var instance: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Agenda sincronização periódica
     */
    fun schedulePeriodicSync() {
        Log.d(TAG, "Agendando sincronização periódica...")

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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Log.d(TAG, "✓ Sincronização periódica agendada")
    }

    /**
     * Cancela sincronização periódica
     */
    fun cancelPeriodicSync() {
        Log.d(TAG, "Cancelando sincronização periódica...")
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d(TAG, "✓ Sincronização periódica cancelada")
    }

    /**
     * Força sincronização imediata
     */
    fun forceSyncNow() {
        Log.d(TAG, "Forçando sincronização imediata...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)

        Log.d(TAG, "✓ Sincronização imediata agendada")
    }
}
