package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.inventario.mobile.data.audit.AuditService
import com.inventario.mobile.data.backup.BackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker para backup automático de coletas pendentes
 * 
 * CRÍTICO: Executa backup periódico para proteger contra:
 * - Desinstalação do app
 * - Dano ao dispositivo
 * - Corrupção do banco de dados
 * 
 * Frequência: Diariamente às 2h da manhã
 * 
 * v2.2: Implementação de backup automático
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager,
    private val auditService: AuditService
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "BackupWorker"
        const val WORK_NAME = "backup_coletas_pendentes"
        
        /**
         * Agenda backup periódico (diário)
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)  // Só quando bateria não está baixa
                .build()
            
            val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                1, TimeUnit.DAYS  // A cada 24 horas
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // Manter agendamento existente
                backupRequest
            )
            
            Log.d(TAG, "✓ Backup automático agendado (diário às 2h)")
        }
        
        /**
         * Cancela backup periódico
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Backup automático cancelado")
        }
        
        /**
         * Força backup imediato
         */
        fun forceBackup(context: Context) {
            val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
                .addTag("backup_manual")
                .build()
            
            WorkManager.getInstance(context).enqueue(backupRequest)
            Log.d(TAG, "Backup manual iniciado")
        }
        
        /**
         * Calcula delay inicial para executar às 2h da manhã
         */
        private fun calculateInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                set(java.util.Calendar.HOUR_OF_DAY, 2)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                
                // Se já passou das 2h hoje, agendar para amanhã
                if (timeInMillis <= now) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            return calendar.timeInMillis - now
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔄 Iniciando backup automático...")
            
            // Criar backup de coletas pendentes
            val backupResult = backupManager.backupColetasPendentes()
            
            if (backupResult.isSuccess) {
                val info = backupResult.getOrNull()!!
                
                Log.d(TAG, "✓ Backup criado com sucesso:")
                Log.d(TAG, "  - Coletas: ${info.quantidadeColetas}")
                Log.d(TAG, "  - Tamanho: ${info.tamanhoFormatado}")
                Log.d(TAG, "  - Arquivo: ${info.nomeArquivo}")
                
                // Registrar no log de auditoria
                auditService.registrarBackup(
                    quantidadeColetas = info.quantidadeColetas,
                    tamanhoBytes = info.tamanhoBytes
                )
                
                // Retornar dados do backup
                val outputData = workDataOf(
                    "quantidade_coletas" to info.quantidadeColetas,
                    "tamanho_bytes" to info.tamanhoBytes,
                    "arquivo" to info.nomeArquivo
                )
                
                Result.success(outputData)
            } else {
                val erro = backupResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                Log.w(TAG, "⚠️ Backup não criado: $erro")
                
                // Se não há coletas pendentes, não é erro
                if (erro.contains("Nenhuma coleta pendente")) {
                    Result.success()
                } else {
                    Result.failure()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ Erro ao executar backup automático", e)
            Result.failure()
        }
    }
}
