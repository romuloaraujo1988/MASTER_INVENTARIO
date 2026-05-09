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

    override fun onCreate() {
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

        // Bug-fix 08/05/2026 (v2.20.5): reset defensivo ÚNICO do banco Room.
        // Várias telas estão falhando em queries do Room após instalar builds
        // anteriores (#105-#109) em dispositivos com dados antigos; sintomas
        // sugerem migration parcial ou schema inconsistente. Esta versão apaga
        // o banco uma única vez para cada dispositivo, garantindo schema
        // limpo. A flag `_schema_reset_v2205_done` marca que já foi feito,
        // evitando apagar de novo em reboots futuros.
        //
        // Este bloco deve ser REMOVIDO na versão 2.20.6 ou posterior.
        try {
            val prefs = com.inventario.mobile.utils.PreferencesManager(this)
            val alreadyDone = prefs.getBoolean("_schema_reset_v2205_done", false)
            if (!alreadyDone) {
                Log.w(TAG, "🗑️ v2.20.5 schema reset: apagando banco Room uma única vez")
                listOf(
                    "inventario_offline_secure.db",
                    "inventario_offline_secure.db-journal",
                    "inventario_offline_secure.db-shm",
                    "inventario_offline_secure.db-wal",
                    "inventario_offline.db"
                ).forEach { fileName ->
                    val dbFile = getDatabasePath(fileName)
                    if (dbFile.exists()) {
                        val ok = dbFile.delete()
                        Log.w(TAG, "   removido $fileName: $ok")
                    }
                }
                // Limpa também o CacheServerStats e flags relacionadas para
                // forçar o app a buscar tudo do servidor novamente.
                prefs.clearCacheServerStats()
                prefs.putBoolean("_schema_reset_v2205_done", true)
                Log.w(TAG, "✓ Banco apagado e CacheServerStats limpo — próxima chamada criará schema limpo")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao executar schema reset único no startup", e)
        }
        
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
            
            val syncManager = com.inventario.mobile.sync.SyncManager.getInstance(this)
            val networkObserver = com.inventario.mobile.sync.NetworkConnectivityObserver.getInstance(this, syncManager)
            
            networkObserver.startObserving()
            
            android.util.Log.i(TAG, "✅ Observador de conectividade iniciado")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Erro ao iniciar observador de rede", e)
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
     * Configuração do WorkManager para usar HiltWorkerFactory
     * Isso permite que Workers usem injeção de dependência via Hilt
     * 
     * CRÍTICO: Sem isso, Workers com @HiltWorker falham com:
     * "Could not instantiate com.inventario.mobile.worker.SyncWorker"
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
