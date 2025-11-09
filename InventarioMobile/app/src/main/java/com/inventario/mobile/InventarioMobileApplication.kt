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
        
        // Inicializar Feature Flags para migração Clean Architecture
        com.inventario.mobile.utils.FeatureFlags.init(this)
        com.inventario.mobile.utils.FeatureFlags.printStatus()
        
        // Inicializar sincronização automática se Clean Architecture estiver habilitada
        if (com.inventario.mobile.utils.FeatureFlags.useCleanColeta) {
            try {
                val syncManager = com.inventario.mobile.sync.SyncManager.getInstance(this)
                syncManager.agendarSincronizacaoPeriodica()
                android.util.Log.i(TAG, "✅ Sincronização automática iniciada")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Erro ao iniciar sincronização", e)
            }
        }
        
        // Log de inicialização
        android.util.Log.d("InventarioApp", "Application inicializada com sucesso")
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