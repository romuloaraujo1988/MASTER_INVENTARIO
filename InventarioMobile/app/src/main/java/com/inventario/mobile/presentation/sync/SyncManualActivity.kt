package com.inventario.mobile.presentation.sync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.databinding.ActivitySyncManualBinding

/**
 * Activity para sincronização manual de dados
 * 
 * NOTA: Esta Activity está temporariamente desabilitada.
 * Use SyncActivity para sincronização.
 * 
 * TODO: Refatorar para usar SyncViewModel atualizado
 */
class SyncManualActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySyncManualBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncManualBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        showDeprecationMessage()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sincronização (Desabilitada)"
    }
    
    private fun showDeprecationMessage() {
        android.widget.Toast.makeText(
            this,
            "Esta tela está desabilitada. Use a tela de Sincronização principal.",
            android.widget.Toast.LENGTH_LONG
        ).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
