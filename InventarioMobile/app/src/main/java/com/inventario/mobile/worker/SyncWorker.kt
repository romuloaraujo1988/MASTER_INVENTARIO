package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronização em background
 * Sincroniza coletas pendentes automaticamente quando há conexão
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase
) : CoroutineWorker(context, workerParams) {
    
    private val syncNotificationManager = com.inventario.mobile.sync.SyncNotificationManager(context)
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_coletas_pendentes"
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "INICIANDO SINCRONIZAÇÃO EM BACKGROUND")
        Log.d(TAG, "═══════════════════════════════════════")
        
        return try {
            // 1. Verificar conectividade
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                Log.w(TAG, "Sem conexão de rede, adiando sincronização")
                return Result.retry()
            }
            
            // 2. Buscar quantidade de coletas pendentes
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(applicationContext)
            val coletaDao = database.coletaDao()
            val pendentes = coletaDao.contarPendentes()
            
            Log.d(TAG, "📊 Coletas pendentes: $pendentes")
            
            if (pendentes == 0) {
                Log.d(TAG, "ℹ️ Nenhuma coleta pendente, finalizando")
                syncNotificationManager.cancelSyncNotification()
                return Result.success()
            }
            
            // 3. Mostrar notificação de progresso
            syncNotificationManager.showSyncInProgress(0, pendentes)
            
            // 4. Executar sincronização
            val result = sincronizarColetasPendentesUseCase()
            
            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                Log.d(TAG, "✓ Sincronização concluída: $quantidade coletas")
                Log.d(TAG, "═══════════════════════════════════════")
                
                // Mostrar notificação de sucesso
                if (quantidade > 0) {
                    syncNotificationManager.showSyncSuccess(quantidade)
                } else {
                    syncNotificationManager.cancelSyncNotification()
                }
                
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "✗ Erro na sincronização: ${error?.message}", error)
                Log.d(TAG, "═══════════════════════════════════════")
                
                // Mostrar notificação de erro
                syncNotificationManager.showSyncError(
                    error?.message ?: "Erro desconhecido"
                )
                
                // Retry em caso de erro de rede
                if (error?.message?.contains("network", ignoreCase = true) == true ||
                    error?.message?.contains("timeout", ignoreCase = true) == true) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exceção na sincronização", e)
            Log.d(TAG, "═══════════════════════════════════════")
            
            // Mostrar notificação de erro
            syncNotificationManager.showSyncError(e.message ?: "Erro desconhecido")
            
            Result.retry()
        }
    }
}
