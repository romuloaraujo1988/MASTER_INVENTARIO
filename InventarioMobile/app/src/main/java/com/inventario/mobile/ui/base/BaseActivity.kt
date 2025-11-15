package com.inventario.mobile.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.ui.components.OfflineIndicator

/**
 * Activity base que adiciona automaticamente o indicador de modo offline
 * 
 * Todas as activities que estendem esta classe terão o indicador
 * de modo offline na barra superior automaticamente.
 */
abstract class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("BaseActivity", "${this::class.simpleName} - onCreate")
    }
    
    override fun onResume() {
        super.onResume()
        // Atualizar indicador quando a activity volta ao foco
        OfflineIndicator.refresh(this)
        android.util.Log.d("BaseActivity", "${this::class.simpleName} - Indicador atualizado")
    }
    
    /**
     * Deve ser chamado após setContentView() nas activities filhas
     */
    protected fun setupOfflineIndicator() {
        OfflineIndicator.setup(this)
    }
}
