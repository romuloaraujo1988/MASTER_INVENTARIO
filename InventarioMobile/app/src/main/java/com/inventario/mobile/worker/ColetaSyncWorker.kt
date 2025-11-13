package com.inventario.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.sync.SyncLogger
import com.inventario.mobile.sync.SyncType
import com.inventario.mobile.sync.toSyncError
import com.inventario.mobile.utils.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronização de coletas pendentes em background
 * Usa Hilt para injeção de dependências
 * 
 * Features:
 * - Logging completo de execuções
 * - Notificações de sucesso/erro
 * - Retry inteligente baseado no tipo de erro
 * - Estatísticas de sincronização
 */
@HiltWorker
class ColetaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasUseCase: SincronizarColetasPendentesUseCase,
    private val syncLogger: SyncLogger,
    private val notificationHelper: com.inventario.mobile.sync.NotificationHelper
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Determinar tipo de sincronização baseado nas tags
            val syncType = when {
                tags.contains(TAG_MANUAL) -> SyncType.MANUAL
                tags.contains(TAG_BY_COUNT) -> SyncType.BY_COUNT
                else -> SyncType.AUTO
            }
            
            android.util.Log.i(TAG, "🔄 Iniciando sincronização ${syncType.displayName}...")
            
            // Registrar início
            syncLogger.logStart(syncType)
            
            // Executar sincronização
            val result = sincronizarColetasUseCase()
            
            result.fold(
                onSuccess = { stats ->
                    android.util.Log.i(TAG, "✅ Sincronização concluída: ${stats.synced} coletas, ${stats.failed} falhas")
                    
                    // Registrar sucesso
                    syncLogger.logSuccess(stats)
                    
                    // Notificar usuário
                    notificationHelper.showSyncSuccess(stats)
                    
                    // Retornar sucesso com dados
                    Result.success(workDataOf(
                        KEY_SYNCED to stats.synced,
                        KEY_FAILED to stats.failed
                    ))
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "❌ Erro na sincronização: ${error.message}", error)
                    
                    // Converter para SyncError
                    val syncError = error.toSyncError()
                    
                    // Registrar erro
                    syncLogger.logError(syncError)
                    
                    // Decidir se deve fazer retry
                    if (syncError.shouldRetry()) {
                        android.util.Log.w(TAG, "⏳ Agendando retry...")
                        Result.retry()
                    } else {
                        android.util.Log.e(TAG, "🚫 Erro sem retry")
                        notificationHelper.showSyncError(syncError)
                        Result.failure(workDataOf(
                            KEY_ERROR to syncError.message
                        ))
                    }
                }
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "💥 Exceção inesperada na sincronização", e)
            
            // Registrar erro
            syncLogger.logError(e)
            
            // Converter e verificar retry
            val syncError = e.toSyncError()
            if (syncError.shouldRetry()) {
                Result.retry()
            } else {
                notificationHelper.showSyncError(syncError)
                Result.failure()
            }
        }
    }
    
    companion object {
        private const val TAG = "ColetaSyncWorker"
        
        // Work names
        const val WORK_NAME = "coleta_sync_periodic"
        const val WORK_NAME_MANUAL = "coleta_sync_manual"
        const val WORK_NAME_BY_COUNT = "coleta_sync_by_count"
        
        // Tags
        const val TAG_MANUAL = "sync_manual"
        const val TAG_BY_COUNT = "sync_by_count"
        const val TAG_PERIODIC = "sync_periodic"
        
        // Output keys
        const val KEY_SYNCED = "synced"
        const val KEY_FAILED = "failed"
        const val KEY_ERROR = "error"
    }
}
