package com.inventario.mobile.presentation.sync

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.databinding.ActivitySyncManualBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para sincronização manual de dados
 * 
 * Permite ao usuário:
 * - Sincronizar coletas do servidor
 * - Enviar coletas pendentes
 * - Migrar coletas antigas
 * - Ver estatísticas de sincronização
 */
@AndroidEntryPoint
class SyncManualActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySyncManualBinding
    private val viewModel: SyncViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncManualBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        observeViewModel()
        
        // Verificar se precisa migração
        viewModel.verificarMigracao()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sincronização"
    }
    
    private fun setupButtons() {
        // Baixar do servidor
        binding.btnDownload.setOnClickListener {
            viewModel.sincronizarColetas()
        }
        
        // Enviar pendentes
        binding.btnUpload.setOnClickListener {
            viewModel.enviarColetasPendentes()
        }
        
        // Migrar antigas
        binding.btnMigrate.setOnClickListener {
            viewModel.migrarColetasAntigas()
        }
        
        // Sincronização completa
        binding.btnSyncComplete.setOnClickListener {
            viewModel.sincronizarCompleto()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                when (state) {
                    is SyncState.Idle -> {
                        hideLoading()
                        enableButtons()
                    }
                    
                    is SyncState.NeedsMigration -> {
                        showWarning("Há coletas antigas que precisam ser atualizadas")
                    }
                    
                    is SyncState.Syncing -> {
                        showLoading(state.message)
                        disableButtons()
                    }
                    
                    is SyncState.Success -> {
                        hideLoading()
                        enableButtons()
                        showSuccess(
                            "Download concluído!\n" +
                            "Novas: ${state.syncResult.novas}\n" +
                            "Atualizadas: ${state.syncResult.atualizadas}\n" +
                            "Total: ${state.syncResult.total}"
                        )
                    }
                    
                    is SyncState.UploadSuccess -> {
                        hideLoading()
                        enableButtons()
                        showSuccess(
                            "Upload concluído!\n" +
                            "Enviadas: ${state.uploadResult.sucesso}\n" +
                            "Falhas: ${state.uploadResult.falhas}\n" +
                            "Total: ${state.uploadResult.total}"
                        )
                    }
                    
                    is SyncState.MigrationSuccess -> {
                        hideLoading()
                        enableButtons()
                        showSuccess(
                            "Migração concluída!\n" +
                            "Atualizadas: ${state.migrationResult.atualizadas}\n" +
                            "Sem patrimônio: ${state.migrationResult.semPatrimonio}"
                        )
                    }
                    
                    is SyncState.CompleteSuccess -> {
                        hideLoading()
                        enableButtons()
                        val upload = state.uploadResult
                        showSuccess(
                            "Sincronização completa!\n\n" +
                            "Download:\n" +
                            "  Novas: ${state.syncResult.novas}\n" +
                            "  Atualizadas: ${state.syncResult.atualizadas}\n\n" +
                            if (upload != null) {
                                "Upload:\n" +
                                "  Enviadas: ${upload.sucesso}\n" +
                                "  Falhas: ${upload.falhas}\n\n"
                            } else "" +
                            "Migração:\n" +
                            "  Atualizadas: ${state.migrationResult.atualizadas}"
                        )
                    }
                    
                    is SyncState.Error -> {
                        hideLoading()
                        enableButtons()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading(message: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = message
        binding.tvStatus.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun enableButtons() {
        binding.btnDownload.isEnabled = true
        binding.btnUpload.isEnabled = true
        binding.btnMigrate.isEnabled = true
        binding.btnSyncComplete.isEnabled = true
    }
    
    private fun disableButtons() {
        binding.btnDownload.isEnabled = false
        binding.btnUpload.isEnabled = false
        binding.btnMigrate.isEnabled = false
        binding.btnSyncComplete.isEnabled = false
    }
    
    private fun showSuccess(message: String) {
        binding.tvStatus.text = "✓ $message"
        binding.tvStatus.visibility = View.VISIBLE
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun showError(message: String) {
        binding.tvStatus.text = "✗ $message"
        binding.tvStatus.visibility = View.VISIBLE
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun showWarning(message: String) {
        binding.tvStatus.text = "⚠ $message"
        binding.tvStatus.visibility = View.VISIBLE
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
