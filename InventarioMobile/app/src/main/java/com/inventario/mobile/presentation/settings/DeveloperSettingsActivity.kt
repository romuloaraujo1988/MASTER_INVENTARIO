package com.inventario.mobile.presentation.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.databinding.ActivityDeveloperSettingsBinding
import com.inventario.mobile.utils.FeatureFlags

/**
 * Activity para configurações de desenvolvedor
 * Permite controlar Feature Flags da migração Clean Architecture
 */
class DeveloperSettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeveloperSettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadCurrentFlags()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Configurações de Desenvolvedor"
        }
    }
    
    private fun loadCurrentFlags() {
        binding.switchCleanArchitecture.isChecked = FeatureFlags.useCleanArchitecture
        binding.switchDescricao.isChecked = FeatureFlags.useCleanDescricao
        binding.switchColeta.isChecked = FeatureFlags.useCleanColeta
        binding.switchDashboard.isChecked = FeatureFlags.useCleanDashboard
        binding.switchSync.isChecked = FeatureFlags.useCleanSync
        
        updateStatus()
    }
    
    private fun setupListeners() {
        // NOTA: Todos os FeatureFlags são const val e não podem ser alterados em runtime
        // Clean Architecture está sempre ativo
        
        // Switch mestre - Desabilitado (sempre true)
        binding.switchCleanArchitecture.isEnabled = false
        binding.switchCleanArchitecture.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Clean Architecture está sempre ativo", Toast.LENGTH_SHORT).show()
        }
        
        // Switch Descrição - Desabilitado (sempre true)
        binding.switchDescricao.isEnabled = false
        binding.switchDescricao.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Feature sempre ativa", Toast.LENGTH_SHORT).show()
        }
        
        // Switch Coleta - Desabilitado (sempre true)
        binding.switchColeta.isEnabled = false
        binding.switchColeta.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Feature sempre ativa", Toast.LENGTH_SHORT).show()
        }
        
        // Switch Dashboard - Desabilitado (sempre true)
        binding.switchDashboard.isEnabled = false
        binding.switchDashboard.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Feature sempre ativa", Toast.LENGTH_SHORT).show()
        }
        
        // Switch Sync - Desabilitado (sempre true)
        binding.switchSync.isEnabled = false
        binding.switchSync.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Feature sempre ativa", Toast.LENGTH_SHORT).show()
        }
        
        // Botão Habilitar Todas - Desabilitado (já estão todas ativas)
        binding.btnEnableAll.isEnabled = false
        binding.btnEnableAll.setOnClickListener {
            Toast.makeText(this, "Todas as features já estão ativas", Toast.LENGTH_SHORT).show()
        }
        
        // Botão Rollback - Desabilitado (não há mais rollback)
        binding.btnRollbackAll.isEnabled = false
        binding.btnRollbackAll.setOnClickListener {
            Toast.makeText(this, "Rollback não disponível - Clean Architecture é permanente", Toast.LENGTH_LONG).show()
        }
        
        // Botão Ver Status
        binding.btnViewStatus.setOnClickListener {
            FeatureFlags.printStatus()
            Toast.makeText(this, "Status impresso no Logcat", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateStatus() {
        binding.tvStatus.text = "Clean Architecture: 100% Ativo (Permanente)"
    }
    
    private fun showRestartDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reiniciar App")
            .setMessage("As mudanças serão aplicadas após reiniciar o app. Deseja reiniciar agora?")
            .setPositiveButton("Reiniciar") { _, _ ->
                restartApp()
            }
            .setNegativeButton("Depois", null)
            .show()
    }
    
    private fun showRollbackConfirmation() {
        // Rollback não disponível - Clean Architecture é permanente
        Toast.makeText(this, "Rollback não disponível", Toast.LENGTH_SHORT).show()
    }
    
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        Runtime.getRuntime().exit(0)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
