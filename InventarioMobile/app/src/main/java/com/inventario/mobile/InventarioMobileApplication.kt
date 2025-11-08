package com.inventario.mobile

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Classe Application principal do app Inventário Mobile
 * Responsável por inicializar componentes globais e dependências
 */
class InventarioMobileApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar ThreeTenABP para manipulação de datas
        AndroidThreeTen.init(this)
        
        // Log de inicialização
        android.util.Log.d("InventarioApp", "Application inicializada com sucesso")
    }
    
    companion object {
        const val TAG = "InventarioMobileApp"
        const val DATABASE_NAME = "inventario_mobile_db"
        const val PREFERENCES_NAME = "inventario_mobile_prefs"
    }
}