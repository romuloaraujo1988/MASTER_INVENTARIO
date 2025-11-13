package com.inventario.mobile.sync

import android.content.Context
import androidx.work.*
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.worker.ColetaSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use Case para agendar e gerenciar sincronizações automáticas
 * Usa WorkManager para garantir execução confiável em background
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Agenda sincronização periódica baseada em tempo
     */
    fun schedulePeriodicSync() {
        val interval = preferencesManager.getSyncInterval() // em minutos
        val wifiOnly = preferencesManager.isWifiOnlyEnabled()
        val batterySaver = preferencesManager.isBatterySaverEnabled()
        
        android.util.Log.d(TAG, "Agendando sincronização periódica: intervalo=${interval}min, wifiOnly=$wifiOnly")
        
        // Criar constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .apply {
                if (batterySaver) {
                    setRequiresBatteryNotLow(true)
                }
            }
            .build()
        
        // Criar request periódico
        val syncRequest = PeriodicWorkRequestBuilder<ColetaSyncWorker>(
            repeatInterval = interval.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 15, // Flex de 15 minutos
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .addTag(ColetaSyncWorker.TAG_PERIODIC)
            .build()
        
        // Enfileirar com política REPLACE (cancela anterior se existir)
        workManager.enqueueUniquePeriodicWork(
            ColetaSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
        
        android.util.Log.i(TAG, "✅ Sincronização periódica agendada")
    }
    
    /**
     * Cancela sincronização periódica
     */
    fun cancelPeriodicSync() {
        android.util.Log.d(TAG, "Cancelando sincronização periódica")
        workManager.cancelUniqueWork(ColetaSyncWorker.WORK_NAME)
        android.util.Log.i(TAG, "✅ Sincronização periódica cancelada")
    }
    
    /**
     * Executa sincronização imediata (manual ou por contador)
     */
    fun syncNow(byCount: Boolean = false) {
        android.util.Log.d(TAG, "Executando sincronização imediata: byCount=$byCount")
        
        // Criar constraints mínimos (apenas rede)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Criar request único
        val syncRequest = OneTimeWorkRequestBuilder<ColetaSyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(if (byCount) ColetaSyncWorker.TAG_BY_COUNT else ColetaSyncWorker.TAG_MANUAL)
            .build()
        
        // Determinar nome do work
        val workName = if (byCount) {
            ColetaSyncWorker.WORK_NAME_BY_COUNT
        } else {
            ColetaSyncWorker.WORK_NAME_MANUAL
        }
        
        // Enfileirar com política REPLACE
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        
        android.util.Log.i(TAG, "✅ Sincronização imediata agendada")
    }
    
    /**
     * Verifica se deve sincronizar por contador
     */
    fun checkCounterSync() {
        val currentCount = preferencesManager.getCollectionCount()
        val limit = preferencesManager.getSyncCollectionInterval()
        
        android.util.Log.d(TAG, "Verificando contador: $currentCount/$limit")
        
        if (currentCount >= limit) {
            android.util.Log.i(TAG, "🎯 Limite de coletas atingido, sincronizando...")
            syncNow(byCount = true)
            preferencesManager.resetCollectionCount()
        }
    }
    
    /**
     * Incrementa contador de coletas
     * Verifica automaticamente se deve sincronizar
     */
    fun incrementCollectionCount() {
        val current = preferencesManager.getCollectionCount()
        val newCount = current + 1
        preferencesManager.setCollectionCount(newCount)
        
        android.util.Log.d(TAG, "Contador incrementado: $newCount")
        
        // Verificar se deve sincronizar por contador
        if (preferencesManager.isAutoSyncByCountEnabled()) {
            checkCounterSync()
        }
    }
    
    /**
     * Verifica status da sincronização periódica
     */
    fun isPeriodicSyncScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(ColetaSyncWorker.WORK_NAME).get()
        return workInfos.any { !it.state.isFinished }
    }
    
    /**
     * Obtém informações sobre o último trabalho de sincronização
     */
    fun getLastSyncInfo(): WorkInfo? {
        val workInfos = workManager.getWorkInfosForUniqueWork(ColetaSyncWorker.WORK_NAME).get()
        return workInfos.firstOrNull()
    }
    
    /**
     * Cancela todos os trabalhos de sincronização
     */
    fun cancelAllSync() {
        android.util.Log.d(TAG, "Cancelando todas as sincronizações")
        workManager.cancelUniqueWork(ColetaSyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(ColetaSyncWorker.WORK_NAME_MANUAL)
        workManager.cancelUniqueWork(ColetaSyncWorker.WORK_NAME_BY_COUNT)
        android.util.Log.i(TAG, "✅ Todas as sincronizações canceladas")
    }
    
    /**
     * Reagenda sincronização periódica com novas configurações
     */
    fun reschedulePeriodicSync() {
        android.util.Log.d(TAG, "Reagendando sincronização periódica")
        cancelPeriodicSync()
        
        if (preferencesManager.isAutoSyncEnabled()) {
            schedulePeriodicSync()
        }
    }
    
    companion object {
        private const val TAG = "SyncScheduler"
    }
}
