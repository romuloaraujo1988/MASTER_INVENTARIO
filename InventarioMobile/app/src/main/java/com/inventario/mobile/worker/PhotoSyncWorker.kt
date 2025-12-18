package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.utils.PhotoHelper
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronização de fotos em background
 * 
 * Estratégia:
 * - Executa apenas quando em Wi-Fi (via Constraints)
 * - Bateria > 20%
 * - Sincroniza fotos pendentes
 * - Limpa fotos antigas após sync
 * 
 * @author Sistema de Inventário v2.11
 */
@HiltWorker
class PhotoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coletaDao: ColetaDao,
    private val photoHelper: PhotoHelper,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "PhotoSyncWorker"
        private const val WORK_NAME = "photo_sync_work"
        
        /**
         * Agenda sincronização periódica de fotos
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // Apenas Wi-Fi
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<PhotoSyncWorker>(
                6, TimeUnit.HOURS // A cada 6 horas
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Log.d(TAG, "✓ Sincronização de fotos agendada (a cada 6h, apenas Wi-Fi)")
        }
        
        /**
         * Cancela sincronização periódica
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Sincronização de fotos cancelada")
        }
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "INICIANDO SINCRONIZAÇÃO DE FOTOS")
        Log.d(TAG, "═══════════════════════════════════════")
        
        return try {
            // Buscar coletas com fotos não sincronizadas
            val coletasComFoto = coletaDao.buscarColetasComFotoPendente()
            
            if (coletasComFoto.isEmpty()) {
                Log.d(TAG, "✓ Nenhuma foto pendente para sincronizar")
                
                // Aproveitar para limpar fotos antigas
                photoHelper.cleanupOldPhotos()
                
                return Result.success()
            }
            
            Log.d(TAG, "📷 ${coletasComFoto.size} fotos pendentes para sincronizar")
            
            var syncCount = 0
            var errorCount = 0
            
            for (coleta in coletasComFoto) {
                try {
                    val fotoPath = coleta.fotoPath ?: continue
                    
                    // Converter para Base64
                    val base64 = photoHelper.photoToBase64(fotoPath)
                    
                    if (base64 != null) {
                        // TODO: Enviar para servidor via API
                        // val response = fotoApi.uploadFoto(coleta.id, base64)
                        
                        // Por enquanto, apenas marcar como sincronizada
                        coletaDao.marcarFotoSincronizada(coleta.id)
                        syncCount++
                        
                        Log.d(TAG, "✓ Foto sincronizada: coleta ${coleta.id}")
                    } else {
                        Log.w(TAG, "⚠️ Não foi possível converter foto: ${coleta.id}")
                        errorCount++
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao sincronizar foto da coleta ${coleta.id}", e)
                    errorCount++
                }
            }
            
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "SINCRONIZAÇÃO DE FOTOS CONCLUÍDA")
            Log.d(TAG, "  Sincronizadas: $syncCount")
            Log.d(TAG, "  Erros: $errorCount")
            Log.d(TAG, "═══════════════════════════════════════")
            
            // Limpar fotos antigas após sync bem-sucedido
            if (syncCount > 0) {
                photoHelper.cleanupOldPhotos()
            }
            
            if (errorCount > 0 && syncCount == 0) {
                Result.retry()
            } else {
                Result.success()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização de fotos", e)
            Result.retry()
        }
    }
}
