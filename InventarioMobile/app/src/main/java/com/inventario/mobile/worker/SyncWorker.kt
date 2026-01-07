package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.domain.usecase.SincronizarFotosReferenciaUseCase
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.NetworkMonitor
import com.inventario.mobile.utils.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronização em background
 * Sincroniza coletas pendentes e fotos de referência automaticamente quando há conexão
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase,
    private val sincronizarFotosReferenciaUseCase: SincronizarFotosReferenciaUseCase,
    private val preferencesManager: PreferencesManager
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
            
            // 2. Sincronizar coletas pendentes
            val coletasResult = sincronizarColetas()
            
            // 3. Sincronizar fotos de referência (se habilitado)
            val fotosResult = sincronizarFotos()
            
            // 4. Determinar resultado final
            if (coletasResult && fotosResult) {
                Log.d(TAG, "✅ Sincronização completa concluída com sucesso")
                Log.d(TAG, "═══════════════════════════════════════")
                androidx.work.ListenableWorker.Result.success()
            } else if (!coletasResult) {
                Log.w(TAG, "⚠️ Sincronização de coletas falhou, agendando retry")
                Log.d(TAG, "═══════════════════════════════════════")
                androidx.work.ListenableWorker.Result.retry()
            } else {
                // Fotos falharam mas coletas ok - sucesso parcial
                Log.w(TAG, "⚠️ Sincronização de fotos falhou, mas coletas ok")
                Log.d(TAG, "═══════════════════════════════════════")
                androidx.work.ListenableWorker.Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização", e)
            Log.d(TAG, "═══════════════════════════════════════")
            androidx.work.ListenableWorker.Result.retry()
        }
    }
    
    /**
     * Sincroniza coletas pendentes
     * @return true se sucesso ou sem pendentes, false se erro
     */
    private suspend fun sincronizarColetas(): Boolean {
        return try {
            // Buscar quantidade de coletas pendentes
            val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(applicationContext)
            val coletaDao = database.coletaDao()
            val pendentes = coletaDao.contarPendentes()
            
            Log.d(TAG, "📊 Coletas pendentes: $pendentes")
            
            if (pendentes == 0) {
                Log.d(TAG, "ℹ️ Nenhuma coleta pendente")
                return true
            }
            
            // Executar sincronização
            Log.d(TAG, "🔄 Iniciando sincronização de $pendentes coleta(s)...")
            val result = sincronizarColetasPendentesUseCase()
            
            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                Log.d(TAG, "✅ Coletas sincronizadas: $quantidade")
                
                // Mostrar notificação de sucesso
                if (quantidade > 0) {
                    notificationManager.showSyncSuccessNotification(quantidade)
                }
                true
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "❌ Erro na sincronização de coletas: ${error?.message}", error)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização de coletas", e)
            false
        }
    }
    
    /**
     * Sincroniza fotos de referência
     * @return true se sucesso ou desabilitado, false se erro
     */
    private suspend fun sincronizarFotos(): Boolean {
        return try {
            // Verificar se sincronização de fotos está habilitada
            if (!preferencesManager.isFotoReferenciaSyncEnabled()) {
                Log.d(TAG, "ℹ️ Sincronização de fotos desabilitada")
                return true
            }
            
            Log.d(TAG, "🖼️ Iniciando sincronização de fotos de referência...")
            val result = sincronizarFotosReferenciaUseCase()
            
            if (result.isSuccess) {
                val syncResult = result.getOrNull()
                Log.d(TAG, "✅ Fotos sincronizadas: ${syncResult?.sincronizadas ?: 0}")
                Log.d(TAG, "📦 Total no cache: ${syncResult?.totalNoCache ?: 0} (${syncResult?.tamanhoFormatado ?: "0 MB"})")
                true
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "❌ Erro na sincronização de fotos: ${error?.message}", error)
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização de fotos", e)
            false
        }
    }
}
