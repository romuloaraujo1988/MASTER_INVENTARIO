package com.inventario.mobile

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.inventario.mobile.network.RefreshTokenInterceptor
import com.inventario.mobile.network.TokenExpiredListener
import com.inventario.mobile.utils.SessionManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Classe Application principal do app Inventário Mobile
 * Responsável por inicializar componentes globais e dependências
 * 
 * @HiltAndroidApp - Habilita injeção de dependência com Hilt
 * 
 * IMPORTANTE: Implementa Configuration.Provider para configurar WorkManager com Hilt
 * Isso permite que Workers usem @HiltWorker e @AssistedInject
 * 
 * v2.1: Implementa TokenExpiredListener para logout automático quando token expira
 */
@HiltAndroidApp
class InventarioMobileApplication : Application(), Configuration.Provider, TokenExpiredListener {
    
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var refreshTokenInterceptor: RefreshTokenInterceptor

    // C2/F1 (auditoria-sync — maio/2026): SyncManager agora é fachada que
    // delega para SyncScheduler. Injetar via Hilt garante que o provider
    // rode e registre a instância no singleton legado antes que qualquer
    // chamada a `SyncManager.getInstance(context)` aconteça.
    @Inject
    lateinit var syncManager: com.inventario.mobile.sync.SyncManager

    // CR-03 (correcoes-seguranca — maio/2026): migra fotos do layout antigo
    // (plano, ≤ v2.20) para o layout hierárquico (v2.21+). Roda uma única
    // vez, de forma idempotente, em coroutine de background.
    @Inject
    lateinit var legacyPhotoMigrator: com.inventario.mobile.utils.LegacyPhotoMigrator

    override fun onCreate() {
        // CR-02 (correcoes-seguranca — maio/2026): instalar
        // UncaughtExceptionHandler ANTES de qualquer outra inicialização.
        // Se o Keystore estiver invalidado, qualquer acesso a EncryptedSharedPreferences,
        // ao SqlCipherKeyManager ou ao AppDatabase vai lançar SqlCipherKeyException
        // (ou suas causas típicas: KeyStoreException, UnrecoverableKeyException,
        // InvalidKeyException). Sem este handler, o app crasha silenciosamente
        // no splash e o usuário não tem caminho de recuperação.
        installKeystoreCrashHandler()

        super.onCreate()
        
        Log.d(TAG, "═══════════════════════════════════")
        Log.d(TAG, "Inventário Mobile App iniciando...")
        Log.d(TAG, "═══════════════════════════════════")
        
        // ✅ APLICAR TEMA SALVO (Dark Mode) - DEVE SER ANTES DE QUALQUER UI
        com.inventario.mobile.utils.ThemeHelper.applyTheme(this)
        Log.d(TAG, "✓ Tema aplicado: ${com.inventario.mobile.utils.ThemeHelper.getCurrentThemeName(this)}")
        
        // Inicializar ThreeTenABP para manipulação de datas
        AndroidThreeTen.init(this)

        // Bug-fix 08/05/2026: desativa automaticamente o Modo Offline Forçado
        // no startup. O OfflineFallbackInterceptor podia ativá-lo após 3 falhas
        // (limite agora elevado para 10, mas o app já estava em produção com
        // a flag ligada em muitos dispositivos). Forçar desativação no startup
        // garante que o usuário começa online e só cai em offline de novo se
        // realmente perder conexão por muito tempo.
        try {
            val prefs = com.inventario.mobile.utils.PreferencesManager(this)
            if (prefs.isForceOfflineMode()) {
                Log.w(TAG, "⚠️ Modo Offline Forçado detectado no startup — desativando automaticamente")
                prefs.setForceOfflineMode(false)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao resetar modo offline forçado no startup", e)
        }

        // Bug-fix 08/05/2026: invalida o cache HTTP do OkHttp no startup para
        // evitar que respostas antigas (ex.: um GET /dashboard/stats que caiu
        // em erro na versão anterior) continuem sendo servidas do cache em
        // vez de ir ao servidor. Custo: na primeira chamada após o startup,
        // o OkHttp buscará tudo da rede (o que é exatamente o que queremos).
        try {
            val cacheDir = java.io.File(cacheDir, "okhttp")
            if (cacheDir.exists()) {
                val removed = cacheDir.deleteRecursively()
                Log.d(TAG, "✓ Cache HTTP do OkHttp limpo no startup: $removed")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao limpar cache HTTP no startup", e)
        }

        // AL-13 (correcoes-seguranca — maio/2026): removido o bloco
        // "v2.20.5 schema reset" que apagava o banco Room uma única vez com
        // flag `_schema_reset_v2205_done`. O bloco era uma medida temporária
        // para limpar schemas inconsistentes das builds #105-#109 e tinha
        // compromisso explícito de remoção na v2.20.6. Permaneceu até a
        // v2.23.0 por lapso.
        //
        // Motivo da remoção:
        //   - A flag fica em `EncryptedSharedPreferences`. Quando o Keystore
        //     é invalidado, o fallback do `PreferencesManager` apaga o arquivo
        //     de prefs — a flag volta a `false` e o banco é apagado de novo
        //     a cada reinstância do Keystore, zerando coletas pendentes
        //     silenciosamente em dispositivos com Keystore instável (Samsung
        //     após update do sistema, troca de lock screen, factory reset
        //     parcial, restore de backup).
        //   - O problema de migração parcial foi resolvido pela nova
        //     estratégia em `AppDatabase.getInstance()` (CR-01), que trata
        //     `IllegalStateException` de forma rastreável em vez de apagar
        //     silenciosamente.

        // Criar canais de notificação
        com.inventario.mobile.utils.NotificationUtils.createNotificationChannels(this)
        
        // Inicializar Feature Flags para migração Clean Architecture
        com.inventario.mobile.utils.FeatureFlags.init(this)
        com.inventario.mobile.utils.FeatureFlags.printStatus()
        
        // ✅ REGISTRAR LISTENER DE TOKEN EXPIRADO
        refreshTokenInterceptor.tokenExpiredListener = this
        Log.d(TAG, "✓ TokenExpiredListener registrado")
        
        // Inicializar sincronização automática
        initializeSyncScheduler()

        // Inicializar observador de conectividade
        initializeNetworkObserver()

        // CR-03 (correcoes-seguranca — maio/2026): migrar fotos antigas
        // (layout plano ≤ v2.20) para o layout hierárquico v2.21+.
        // Rodamos em coroutine de background — o método é idempotente e
        // termina rapidamente se a migração já foi feita.
        initializeLegacyPhotoMigration()
        
        // Log de inicialização
        Log.d(TAG, "✓ Application inicializada com sucesso")
        Log.d(TAG, "═══════════════════════════════════")
    }
    
    /**
     * Callback chamado quando token expira e não pode ser renovado
     * Faz logout automático e redireciona para tela de login
     */
    override fun onTokenExpired() {
        Log.w(TAG, "═══════════════════════════════════")
        Log.w(TAG, "⚠️ TOKEN EXPIRADO!")
        Log.w(TAG, "Fazendo logout automático...")
        Log.w(TAG, "═══════════════════════════════════")
        
        sessionManager.logout(
            showMessage = true,
            message = "Sua sessão expirou. Por favor, faça login novamente."
        )
    }
    
    /**
     * Inicializa o agendador de sincronização automática
     */
    private fun initializeSyncScheduler() {
        try {
            val preferencesManager = com.inventario.mobile.utils.PreferencesManager(this)
            val syncScheduler = com.inventario.mobile.sync.SyncScheduler(this, preferencesManager)
            
            // Verificar se sincronização por tempo está habilitada
            if (preferencesManager.isAutoSyncEnabled()) {
                android.util.Log.i(TAG, "🔄 Agendando sincronização periódica...")
                syncScheduler.schedulePeriodicSync()
                android.util.Log.i(TAG, "✅ Sincronização automática iniciada")
            } else {
                android.util.Log.d(TAG, "ℹ️ Sincronização automática desabilitada")
            }
            
            // v2.11: Agendar sincronização de fotos (se habilitado)
            if (preferencesManager.isPhotoOnCollectionEnabled()) {
                android.util.Log.i(TAG, "📷 Agendando sincronização de fotos...")
                com.inventario.mobile.worker.PhotoSyncWorker.schedule(this)
                android.util.Log.i(TAG, "✅ Sincronização de fotos agendada (a cada 6h, apenas Wi-Fi)")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro ao iniciar sincronização", e)
        }
    }
    
    /**
     * Inicializa o observador de conectividade de rede
     * Dispara sincronização automática ao reconectar
     */
    private fun initializeNetworkObserver() {
        try {
            android.util.Log.i(TAG, "🌐 Inicializando observador de conectividade...")

            // syncManager foi injetado via Hilt; usa a instância já registrada
            // no singleton pelo SyncModule (C2/F1 maio/2026).
            val networkObserver = com.inventario.mobile.sync.NetworkConnectivityObserver.getInstance(this, syncManager)

            networkObserver.startObserving()

            android.util.Log.i(TAG, "✅ Observador de conectividade iniciado")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro ao iniciar observador de rede", e)
        }
    }
    
    /**
     * CR-03 (correcoes-seguranca — maio/2026): dispara migração idempotente
     * de fotos legacy em coroutine de IO. Se falhar, a próxima execução do
     * app tentará de novo — a flag de conclusão só é gravada em caso de
     * sucesso.
     *
     * Uso de [kotlinx.coroutines.GlobalScope] é aceitável neste contexto
     * porque o Application tem ciclo de vida coincidente com o processo —
     * não há risco de *leak* e o cancelamento prematuro causaria justamente
     * o problema que queremos evitar.
     */
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    private fun initializeLegacyPhotoMigration() {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val migradas = legacyPhotoMigrator.migrateIfNeeded()
                if (migradas > 0) {
                    Log.i(TAG, "✓ $migradas fotos legacy migradas para novo layout")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao migrar fotos legacy", e)
            }
        }
    }

    /**
     * Limpa todos os caches da aplicação
     * Deve ser chamado ao fazer logout
     */
    fun clearAllCaches() {
        android.util.Log.d(TAG, "clearAllCaches: Limpando todos os caches")
        com.inventario.mobile.data.cache.SalaCache.clear()
        // Adicionar outros caches aqui conforme necessário
    }

    /**
     * CR-02 (correcoes-seguranca — maio/2026): instala UncaughtExceptionHandler
     * global que intercepta falhas do Android Keystore / SQLCipher e redireciona
     * para [com.inventario.mobile.security.KeystoreRecoveryActivity] em vez de
     * deixar o processo crashar.
     *
     * O handler delega TODAS as demais exceções para o handler padrão do
     * Android, preservando os relatórios de crash normais. Só entra em ação
     * quando o padrão da exceção aponta para falha de chave/criptografia.
     */
    private fun installKeystoreCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isKeystoreFailure(throwable)) {
                Log.e(
                    TAG,
                    "═══ Falha do Keystore/SQLCipher capturada — redirecionando para recovery ═══",
                    throwable
                )
                try {
                    val intent = com.inventario.mobile.security.KeystoreRecoveryActivity
                        .createIntent(applicationContext)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao iniciar KeystoreRecoveryActivity", e)
                }
                // Encerrar processo imediatamente — a nova Activity sobe em
                // novo processo limpo. Se continuarmos, os @Inject singletons
                // que falharam ficam em estado corrompido.
                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(10)
            } else {
                // Qualquer outra exceção: delegar ao handler padrão.
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
        Log.d(TAG, "✓ UncaughtExceptionHandler para Keystore instalado")
    }

    /**
     * Detecta se uma exceção (ou qualquer uma de suas causas na cadeia) é
     * sintoma de falha do Android Keystore ou do SQLCipher.
     *
     * Inclui: nossa própria [SqlCipherKeyManager.SqlCipherKeyException],
     * exceções do JCE (`KeyStoreException`, `UnrecoverableKeyException`,
     * `InvalidKeyException`, `BadPaddingException`) e a mensagem típica
     * "SQLCipher invalid passphrase" do net.sqlcipher.
     */
    private fun isKeystoreFailure(throwable: Throwable): Boolean {
        var current: Throwable? = throwable
        var depth = 0
        while (current != null && depth < 10) {
            when (current) {
                is com.inventario.mobile.security.SqlCipherKeyManager.SqlCipherKeyException,
                is java.security.KeyStoreException,
                is java.security.UnrecoverableKeyException,
                is java.security.InvalidKeyException,
                is javax.crypto.BadPaddingException -> return true
            }
            val message = current.message.orEmpty().lowercase(java.util.Locale.ROOT)
            if (message.contains("androidkeystore") ||
                message.contains("keystore") && message.contains("invalidated") ||
                message.contains("sqlcipher") && message.contains("passphrase") ||
                message.contains("file is not a database")
            ) {
                return true
            }
            current = current.cause
            depth++
        }
        return false
    }
    
    /**
     * Configuração do WorkManager para usar HiltWorkerFactory
     * Isso permite que Workers usem injeção de dependência via Hilt
     *
     * CRÍTICO: Sem isso, Workers com @HiltWorker falham com:
     * "Could not instantiate com.inventario.mobile.worker.ColetaSyncWorker"
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    
    companion object {
        const val TAG = "InventarioMobileApp"
        const val DATABASE_NAME = "inventario_mobile_db"
        const val PREFERENCES_NAME = "inventario_mobile_prefs"
    }
}
