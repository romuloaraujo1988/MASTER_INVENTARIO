package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.NetworkMonitor
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
    
    private val networkMonitor = NetworkMonitor.getInstance(context)
    private val notificationManager = OfflineNotificationManager.getInstance(context)
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_coletas_pendentes"
    }
    
    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "INICIANDO SINCRONIZAÇÃO EM BACKGROUND")
        Log.d(TAG, "═══════════════════════════════════════")
        
        return try {
            // 1. Verificar conectividade
            if (!networkMonitor.isConnected()) {
                Log.w(TAG, "❌ Sem conexão de rede, adiando sincronização")
                return androidx.work.ListenableWorker.Result.retry()
            }
            
            Log.d(TAG, "✅ Conexão disponível: ${networkMonitor.getConnectionType()}")
            
            // 2. Buscar quantidade de coletas pendentes
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(applicationContext)
            val coletaDao = database.coletaDao()
            val pendentes = coletaDao.contarPendentes()
            
            Log.d(TAG, "📊 Coletas pendentes: $pendentes")
            
            if (pendentes == 0) {
                Log.d(TAG, "ℹ️ Nenhuma coleta pendente, finalizando")
                return androidx.work.ListenableWorker.Result.success()
            }
            
            // 3. Executar sincronização
            Log.d(TAG, "🔄 Iniciando sincronização de $pendentes coleta(s)...")
            val result = sincronizarColetasPendentesUseCase()
            
            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                Log.d(TAG, "✅ Sincronização concluída: $quantidade coletas")
                Log.d(TAG, "═══════════════════════════════════════")
                
                // Mostrar notificação de sucesso
                if (quantidade > 0) {
                    notificationManager.showSyncSuccessNotification(quantidade)
                }
                
                androidx.work.ListenableWorker.Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "❌ Erro na sincronização: ${error?.message}", error)
                Log.d(TAG, "═══════════════════════════════════════")
                
                // Retry em caso de erro de rede
                val isNetworkError = error?.message?.contains("network", ignoreCase = true) == true ||
                    error?.message?.contains("timeout", ignoreCase = true) == true ||
                    error?.message?.contains("connection", ignoreCase = true) == true
                
                if (isNetworkError) {
                    Log.d(TAG, "🔄 Erro de rede detectado, agendando retry...")
                    androidx.work.ListenableWorker.Result.retry()
                } else {
                    Log.e(TAG, "❌ Erro não recuperável, marcando como falha")
                    androidx.work.ListenableWorker.Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização", e)
            Log.d(TAG, "═══════════════════════════════════════")
            androidx.work.ListenableWorker.Result.retry()
        }
    }
}
