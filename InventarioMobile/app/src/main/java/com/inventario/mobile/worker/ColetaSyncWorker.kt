package com.inventario.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronização de coletas pendentes em background
 * Usa Hilt para injeção de dependências
 */
@HiltWorker
class ColetaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasUseCase: SincronizarColetasPendentesUseCase
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            android.util.Log.i(TAG, "🔄 Iniciando sincronização de coletas...")
            
            val result = sincronizarColetasUseCase()
            
            result.fold(
                onSuccess = { quantidadeSincronizada ->
                    android.util.Log.i(TAG, "✅ Sincronização concluída: $quantidadeSincronizada coletas")
                    Result.success()
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "❌ Erro na sincronização: ${error.message}")
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "💥 Exceção na sincronização", e)
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "ColetaSyncWorker"
        const val WORK_NAME = "coleta_sync_work"
    }
}
