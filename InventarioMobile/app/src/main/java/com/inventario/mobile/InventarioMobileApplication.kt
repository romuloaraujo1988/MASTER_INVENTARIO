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
        
        // Inicializar ThreeTenABP para manipulação de datas
        AndroidThreeTen.init(this)
        
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
