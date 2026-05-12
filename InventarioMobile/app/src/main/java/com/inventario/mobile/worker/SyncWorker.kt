package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.domain.usecase.SincronizarFotosReferenciaUseCase
import com.inventario.mobile.domain.usecase.SincronizarSugestoesDescricaoUseCase
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.NetworkMonitor
import com.inventario.mobile.utils.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker para sincronização em background
 * Sincroniza coletas pendentes e fotos de referência automaticamente quando há conexão
 *
 * v2.22 (feature `coleta-descricao-livre-com-sugestao`):
 *   Após coletas sincronizadas com sucesso, recarrega o cache local de
 *   sugestões de descrição (Req 8.6). Falha nessa etapa é engolida para
 *   preservar o cache anterior (Req 8.7) e não interromper o resultado
 *   do worker — coletas e fotos continuam sendo a métrica principal de
 *   sucesso.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase,
    private val sincronizarFotosReferenciaUseCase: SincronizarFotosReferenciaUseCase,
    private val sincronizarSugestoesDescricaoUseCase: SincronizarSugestoesDescricaoUseCase,
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

            // 4. Recarregar cache de sugestões de descrição
            //    Feature: coleta-descricao-livre-com-sugestao (Req 8.6)
            //    Só faz sentido após sincronizar coletas — assim o servidor
            //    já reflete o estado corrente. Falha é engolida (Req 8.7 —
            //    cache anterior permanece intocado via atualizarCache).
            if (coletasResult) {
                sincronizarSugestoesDescricao()
            }
            
            // 5. Determinar resultado final
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
            // v2.14.1: Usar AppDatabase (inventario_offline.db) ao invés de InventarioDatabase
            val database = com.inventario.mobile.data.local.database.AppDatabase.getInstance(applicationContext)
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

    /**
     * Recarrega o cache local de sugestões de descrição para o inventário
     * ativo (feature `coleta-descricao-livre-com-sugestao`).
     *
     * Falha (timeout de 10s, erro de rede, ausência de inventário ativo)
     * é engolida deliberadamente — o use case já garante que o cache
     * anterior permanece intocado em caso de erro (Req 8.7), e este passo
     * é auxiliar ao worker: não deve alterar o resultado principal
     * (sucesso/retry) baseado em coletas e fotos.
     */
    private suspend fun sincronizarSugestoesDescricao() {
        try {
            val idInventarioAtivo = preferencesManager.getInventarioAtivoId()
            if (idInventarioAtivo == null) {
                Log.d(TAG, "ℹ️ Sem inventário ativo — pulando sync de sugestões")
                return
            }
            Log.d(TAG, "🔄 Recarregando cache de sugestões para inventário $idInventarioAtivo...")
            val result = sincronizarSugestoesDescricaoUseCase(idInventarioAtivo)
            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                Log.d(TAG, "✅ Cache de sugestões recarregado: $quantidade sugestão(ões)")
            } else {
                val error = result.exceptionOrNull()
                // Req 8.7 — cache anterior preservado; apenas loga como warn
                Log.w(
                    TAG,
                    "⚠️ Falha ao recarregar cache de sugestões (cache anterior preservado): ${error?.message}"
                )
            }
        } catch (e: Exception) {
            // Blindagem extra — nunca propagar para o doWork()
            Log.w(TAG, "⚠️ Exceção ao recarregar cache de sugestões (ignorada)", e)
        }
    }
}
