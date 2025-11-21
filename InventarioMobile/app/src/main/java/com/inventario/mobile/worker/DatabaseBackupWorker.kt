package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.inventario.mobile.data.local.backup.DatabaseBackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker para backup automático do banco de dados
 * Executa diariamente às 3h da manhã
 * 
 * Garante que sempre há um backup recente dos dados
 */
@HiltWorker
class DatabaseBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: DatabaseBackupManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "DatabaseBackupWorker"
        const val WORK_NAME = "database_backup_work"
        
        /**
         * Agenda backup automático diário
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()
            
            val backupRequest = PeriodicWorkRequestBuilder<DatabaseBackupWorker>(
                1, TimeUnit.DAYS  // Diariamente
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    backupRequest
                )
            
            Log.d(TAG, "Backup automático agendado (diário às 3h)")
        }
        
        /**
         * Cancela backup automático
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORK_NAME)
            
            Log.d(TAG, "Backup automático cancelado")
        }
        
        /**
         * Calcula delay inicial para executar às 3h da manhã
         */
        private fun calculateInitialDelay(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 3)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                
                // Se já passou das 3h hoje, agendar para amanhã
                if (before(now)) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            return target.timeInMillis - now.timeInMillis
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando backup automático do banco de dados...")
            
            // Verificar integridade antes de fazer backup
            val isIntegro = backupManager.checkDatabaseIntegrity()
            if (!isIntegro) {
                Log.e(TAG, "✗ Banco de dados corrompido! Backup cancelado")
                return Result.failure()
            }
            
            // Criar backup
            val backupPath = backupManager.createBackup()
            
            if (backupPath != null) {
                Log.d(TAG, "✓ Backup automático criado com sucesso: $backupPath")
                
                // Obter estatísticas
                val stats = backupManager.getDatabaseStats()
                Log.d(TAG, "Estatísticas: ${stats.sizeMB} MB, ${stats.backupCount} backups")
                
                Result.success()
            } else {
                Log.e(TAG, "✗ Falha ao criar backup automático")
                Result.retry()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro no backup automático", e)
            Result.retry()
        }
    }
}
