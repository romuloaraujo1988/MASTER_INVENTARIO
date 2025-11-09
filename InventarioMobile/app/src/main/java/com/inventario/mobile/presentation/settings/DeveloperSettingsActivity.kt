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
        // Switch mestre
        binding.switchCleanArchitecture.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlags.useCleanArchitecture = isChecked
            updateStatus()
            showRestartDialog()
        }
        
        // Switch Descrição
        binding.switchDescricao.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlags.useCleanDescricao = isChecked
            updateStatus()
            showRestartDialog()
        }
        
        // Switch Coleta
        binding.switchColeta.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlags.useCleanColeta = isChecked
            updateStatus()
            showRestartDialog()
        }
        
        // Switch Dashboard
        binding.switchDashboard.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlags.useCleanDashboard = isChecked
            updateStatus()
            showRestartDialog()
        }
        
        // Switch Sync
        binding.switchSync.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlags.useCleanSync = isChecked
            updateStatus()
            showRestartDialog()
        }
        
        // Botão Habilitar Todas
        binding.btnEnableAll.setOnClickListener {
            FeatureFlags.enableAll()
            loadCurrentFlags()
            showRestartDialog()
        }
        
        // Botão Rollback
        binding.btnRollbackAll.setOnClickListener {
            showRollbackConfirmation()
        }
        
        // Botão Ver Status
        binding.btnViewStatus.setOnClickListener {
            FeatureFlags.printStatus()
            Toast.makeText(this, "Status impresso no Logcat", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateStatus() {
        val status = FeatureFlags.getStatus()
        val enabledCount = status.count { it.value }
        val totalCount = status.size
        
        binding.tvStatus.text = "Features habilitadas: $enabledCount/$totalCount"
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
        AlertDialog.Builder(this)
            .setTitle("⚠️ Rollback Completo")
            .setMessage("Isso desabilitará TODAS as features Clean Architecture e voltará para o código antigo. Confirma?")
            .setPositiveButton("Sim, fazer rollback") { _, _ ->
                FeatureFlags.rollbackAll()
                loadCurrentFlags()
                Toast.makeText(this, "🔄 Rollback completo realizado", Toast.LENGTH_LONG).show()
                showRestartDialog()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
