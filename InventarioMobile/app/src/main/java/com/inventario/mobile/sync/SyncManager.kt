package com.inventario.mobile.sync

import android.content.Context
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada de sincronização que delega para [SyncScheduler].
 *
 * C2/F1 (auditoria-sync — maio/2026): antes, `SyncManager` agendava
 * `SyncWorker` enquanto `SyncScheduler` agendava `ColetaSyncWorker` — dois
 * workers distintos que faziam a mesma coisa e podiam rodar simultaneamente,
 * criando risco de envio duplicado de coletas ao servidor. Além disso,
 * `forceSyncNow()` usava `workManager.enqueue()` sem nome único, criando
 * múltiplos workers a cada reconexão.
 *
 * Esta classe foi reescrita como fachada estreita que delega **todas** as
 * operações para [SyncScheduler] (Hilt, único pipeline de sincronização
 * usando `ColetaSyncWorker`). A API pública (`schedulePeriodicSync`,
 * `cancelPeriodicSync`, `forceSyncNow`) foi mantida intacta para evitar
 * mudanças nos call sites (`SyncViewModel`, `NetworkConnectivityObserver`,
 * etc.).
 *
 * ## Como obter uma instância
 *
 * - **Preferência:** injetar via Hilt (`@Inject lateinit var syncManager: SyncManager`).
 * - **Legado:** chamar `SyncManager.getInstance(context)` ainda funciona;
 *   o método resolve a instância via Hilt internamente.
 */
@Singleton
class SyncManager @Inject constructor(
    private val syncScheduler: SyncScheduler
) {

    companion object {
        private const val TAG = "SyncManager"

        @Volatile
        private var instance: SyncManager? = null

        /**
         * Retorna a instância gerenciada por Hilt.
         *
         * O registro é feito em [InventarioMobileApplication.onCreate] (veja
         * [setInstance]). Se chamado antes do Hilt ser inicializado (caso
         * impossível em runtime normal), lança IllegalStateException.
         */
        @JvmStatic
        fun getInstance(context: Context): SyncManager {
            return instance ?: error(
                "SyncManager ainda não foi inicializado pelo Hilt. " +
                        "Verifique se InventarioMobileApplication.onCreate já rodou."
            )
        }

        /**
         * Registra a instância Hilt no singleton legado. Chamado pelo
         * [InventarioMobileApplication.onCreate] após Hilt estar pronto.
         */
        internal fun setInstance(syncManager: SyncManager) {
            instance = syncManager
        }
    }

    /**
     * Agenda sincronização periódica (delega para [SyncScheduler]).
     */
    fun schedulePeriodicSync() {
        Log.d(TAG, "Delegando schedulePeriodicSync → SyncScheduler")
        syncScheduler.schedulePeriodicSync()
    }

    /**
     * Cancela sincronização periódica (delega para [SyncScheduler]).
     */
    fun cancelPeriodicSync() {
        Log.d(TAG, "Delegando cancelPeriodicSync → SyncScheduler")
        syncScheduler.cancelPeriodicSync()
    }

    /**
     * Força sincronização imediata (delega para [SyncScheduler]).
     *
     * F2 (auditoria-sync — maio/2026): antes enfileirava `SyncWorker` sem
     * nome único; agora delega para [SyncScheduler.syncNow] que usa
     * `enqueueUniqueWork` com política REPLACE.
     */
    fun forceSyncNow() {
        Log.d(TAG, "Delegando forceSyncNow → SyncScheduler.syncNow()")
        syncScheduler.syncNow(byCount = false)
    }
}
