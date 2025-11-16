package com.inventario.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
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
    private val sincronizarColetasUseCase: SincronizarColetasPendentesUseCase
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            android.util.Log.i(TAG, "🔄 Iniciando sincronização...")
            
            // Executar sincronização
            val result = sincronizarColetasUseCase()
            
            result.fold(
                onSuccess = { quantidadeSincronizada ->
                    android.util.Log.i(TAG, "✅ Sincronização concluída: $quantidadeSincronizada coletas")
                    
                    // Retornar sucesso com dados
                    Result.success(workDataOf(
                        KEY_SYNCED to quantidadeSincronizada,
                        KEY_FAILED to 0
                    ))
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "❌ Erro na sincronização: ${error.message}", error)
                    
                    // Retry em caso de erro de rede
                    if (error.message?.contains("network", ignoreCase = true) == true ||
                        error.message?.contains("connection", ignoreCase = true) == true) {
                        android.util.Log.w(TAG, "⏳ Agendando retry...")
                        Result.retry()
                    } else {
                        android.util.Log.e(TAG, "🚫 Erro sem retry")
                        Result.failure(workDataOf(
                            KEY_ERROR to (error.message ?: "Erro desconhecido")
                        ))
                    }
                }
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "💥 Exceção inesperada na sincronização", e)
            
            // Retry em caso de erro de rede
            if (e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("connection", ignoreCase = true) == true) {
                Result.retry()
            } else {
                Result.failure(workDataOf(
                    KEY_ERROR to (e.message ?: "Erro desconhecido")
                ))
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
