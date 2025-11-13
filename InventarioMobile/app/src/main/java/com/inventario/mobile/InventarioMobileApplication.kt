package com.inventario.mobile

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application principal do app Inventário Mobile
 * Responsável por inicializar componentes globais e dependências
 * 
 * @HiltAndroidApp - Habilita injeção de dependência com Hilt
 */
@HiltAndroidApp
class InventarioMobileApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar ThreeTenABP para manipulação de datas
        AndroidThreeTen.init(this)
        
        // Criar canais de notificação
        com.inventario.mobile.utils.NotificationUtils.createNotificationChannels(this)
        
        // Inicializar Feature Flags para migração Clean Architecture
        com.inventario.mobile.utils.FeatureFlags.init(this)
        com.inventario.mobile.utils.FeatureFlags.printStatus()
        
        // Inicializar sincronização automática
        initializeSyncScheduler()
        
        // Log de inicialização
        android.util.Log.d("InventarioApp", "Application inicializada com sucesso")
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
     * Limpa todos os caches da aplicação
     * Deve ser chamado ao fazer logout
     */
    fun clearAllCaches() {
        android.util.Log.d(TAG, "clearAllCaches: Limpando todos os caches")
        com.inventario.mobile.data.cache.SalaCache.clear()
        // Adicionar outros caches aqui conforme necessário
    }
    
    companion object {
        const val TAG = "InventarioMobileApp"
        const val DATABASE_NAME = "inventario_mobile_db"
        const val PREFERENCES_NAME = "inventario_mobile_prefs"
    }
}