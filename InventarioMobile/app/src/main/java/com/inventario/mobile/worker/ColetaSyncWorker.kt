package com.inventario.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.inventario.mobile.domain.usecase.SincronizarColetasPendentesUseCase
import com.inventario.mobile.domain.usecase.SincronizarFotosReferenciaUseCase
import com.inventario.mobile.domain.usecase.SincronizarSugestoesDescricaoUseCase
import com.inventario.mobile.utils.NetworkMonitor
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker único de sincronização em background.
 *
 * C2/F1 (auditoria-sync — maio/2026): antes existiam dois workers distintos
 * que faziam a mesma coisa e podiam rodar simultaneamente:
 *   - `SyncWorker` (agendado por `SyncManager`) — coletas + fotos + sugestões
 *   - `ColetaSyncWorker` (agendado por `SyncScheduler`) — apenas coletas
 *
 * Ambos chamavam `SincronizarColetasPendentesUseCase`, criando risco real de
 * envio duplicado de coletas ao servidor. Esta classe absorveu as três
 * responsabilidades do `SyncWorker` (agora deletado) e é o **único** worker
 * de sincronização do app. `SyncManager` foi convertido em fachada que
 * delega para `SyncScheduler`, que agenda exclusivamente este worker.
 *
 * ## Responsabilidades (em ordem)
 * 1. Sincronizar coletas pendentes (batch + fallback individual)
 * 2. Sincronizar fotos de referência (se habilitado nas configurações)
 * 3. Recarregar cache de sugestões de descrição (após coletas bem-sucedidas)
 *
 * ## Nomes de work únicos
 * Todos os caminhos de agendamento usam nomes únicos via `enqueueUniqueWork`
 * / `enqueueUniquePeriodicWork`, garantindo que nunca haja dois workers
 * simultâneos para o mesmo propósito.
 */
@HiltWorker
class ColetaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sincronizarColetasUseCase: SincronizarColetasPendentesUseCase,
    private val sincronizarFotosReferenciaUseCase: SincronizarFotosReferenciaUseCase,
    private val sincronizarSugestoesDescricaoUseCase: SincronizarSugestoesDescricaoUseCase,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(appContext, workerParams) {

    private val networkMonitor = NetworkMonitor.getInstance(appContext)
    private val notificationManager = OfflineNotificationManager.getInstance(appContext)

    override suspend fun doWork(): Result {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "INICIANDO SINCRONIZAÇÃO EM BACKGROUND")
        Log.d(TAG, "═══════════════════════════════════════")

        return try {
            // 1. Verificar conectividade
            if (!networkMonitor.isConnected()) {
                Log.w(TAG, "❌ Sem conexão de rede, adiando sincronização")
                return Result.retry()
            }
            Log.d(TAG, "✅ Conexão disponível: ${networkMonitor.getConnectionType()}")

            // 2. Sincronizar coletas pendentes
            val coletasOk = sincronizarColetas()

            // 3. Sincronizar fotos de referência (se habilitado)
            val fotosOk = sincronizarFotos()

            // 4. Recarregar cache de sugestões de descrição (após coletas ok)
            //    Falha aqui não altera o resultado do worker — cache anterior
            //    é preservado pelo use case (Req 8.7).
            if (coletasOk) {
                sincronizarSugestoesDescricao()
            }

            // 5. Determinar resultado final
            when {
                coletasOk && fotosOk -> {
                    Log.d(TAG, "✅ Sincronização completa concluída com sucesso")
                    Result.success()
                }
                !coletasOk -> {
                    Log.w(TAG, "⚠️ Sincronização de coletas falhou, agendando retry")
                    Result.retry()
                }
                else -> {
                    // Fotos falharam mas coletas ok — sucesso parcial aceitável
                    Log.w(TAG, "⚠️ Sincronização de fotos falhou, mas coletas ok")
                    Result.success()
                }
            }.also {
                Log.d(TAG, "═══════════════════════════════════════")
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização", e)
            Log.d(TAG, "═══════════════════════════════════════")
            Result.retry()
        }
    }

    // ─── Coletas ─────────────────────────────────────────────────────────────

    private suspend fun sincronizarColetas(): Boolean {
        return try {
            val database = com.inventario.mobile.data.local.database.AppDatabase
                .getInstance(applicationContext)
            val pendentes = database.coletaDao().contarPendentes()

            Log.d(TAG, "📊 Coletas pendentes: $pendentes")

            if (pendentes == 0) {
                Log.d(TAG, "ℹ️ Nenhuma coleta pendente")
                return true
            }

            Log.d(TAG, "🔄 Iniciando sincronização de $pendentes coleta(s)...")
            val result = sincronizarColetasUseCase()

            if (result.isSuccess) {
                val quantidade = result.getOrNull() ?: 0
                Log.d(TAG, "✅ Coletas sincronizadas: $quantidade")
                if (quantidade > 0) {
                    notificationManager.showSyncSuccessNotification(quantidade)
                }
                true
            } else {
                Log.e(TAG, "❌ Erro na sincronização de coletas: ${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização de coletas", e)
            false
        }
    }

    // ─── Fotos de referência ─────────────────────────────────────────────────

    private suspend fun sincronizarFotos(): Boolean {
        return try {
            if (!preferencesManager.isFotoReferenciaSyncEnabled()) {
                Log.d(TAG, "ℹ️ Sincronização de fotos desabilitada")
                return true
            }

            Log.d(TAG, "🖼️ Iniciando sincronização de fotos de referência...")
            val result = sincronizarFotosReferenciaUseCase()

            if (result.isSuccess) {
                val syncResult = result.getOrNull()
                Log.d(TAG, "✅ Fotos sincronizadas: ${syncResult?.sincronizadas ?: 0}")
                true
            } else {
                Log.e(TAG, "❌ Erro na sincronização de fotos: ${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exceção na sincronização de fotos", e)
            false
        }
    }

    // ─── Sugestões de descrição ───────────────────────────────────────────────

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
                Log.d(TAG, "✅ Cache de sugestões recarregado: ${result.getOrNull() ?: 0} sugestão(ões)")
            } else {
                // Req 8.7 — cache anterior preservado; apenas loga como warn
                Log.w(TAG, "⚠️ Falha ao recarregar sugestões (cache anterior preservado): ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            // Blindagem extra — nunca propagar para o doWork()
            Log.w(TAG, "⚠️ Exceção ao recarregar sugestões (ignorada)", e)
        }
    }

    companion object {
        private const val TAG = "ColetaSyncWorker"

        // Work names — todos únicos para evitar workers simultâneos
        const val WORK_NAME          = "coleta_sync_periodic"
        const val WORK_NAME_MANUAL   = "coleta_sync_manual"
        const val WORK_NAME_BY_COUNT = "coleta_sync_by_count"

        // Tags
        const val TAG_MANUAL   = "sync_manual"
        const val TAG_BY_COUNT = "sync_by_count"
        const val TAG_PERIODIC = "sync_periodic"

        // Output keys
        const val KEY_SYNCED = "synced"
        const val KEY_FAILED = "failed"
        const val KEY_ERROR  = "error"
    }
}
