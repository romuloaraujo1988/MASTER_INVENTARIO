package com.inventario.mobile.presentation.sync

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivitySyncBinding
import com.inventario.mobile.utils.NetworkUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para gerenciar sincronização de dados
 * Usa Clean Architecture com ViewModel e Use Cases
 */
@AndroidEntryPoint
class SyncActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySyncBinding
    private val viewModel: SyncViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupListeners()
        setupObservers()
        updateNetworkStatus()
        
        // Agendar sincronização periódica
        viewModel.schedulePeriodicSync()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sincronização de Dados"
        }
    }
    
    private fun setupListeners() {
        binding.btnSyncNow.setOnClickListener {
            viewModel.syncFromServer()
        }
        
        binding.btnClearData.setOnClickListener {
            showClearDataConfirmation()
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadStats()
            updateNetworkStatus()
        }
    }
    
    private fun setupObservers() {
        // Observar estado
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SyncState.Idle -> {
                        hideLoading()
                    }
                    is SyncState.Loading -> {
                        showLoading(state.message)
                    }
                    is SyncState.Success -> {
                        hideLoading()
                        showSuccess(buildSuccessMessage(state))
                        viewModel.clearState()
                    }
                    is SyncState.ColetasSyncSuccess -> {
                        hideLoading()
                        showSuccess(state.message)
                        viewModel.clearState()
                    }
                    is SyncState.Error -> {
                        hideLoading()
                        showError(state.message)
                        viewModel.clearState()
                    }
                }
            }
        }
        
        // Observar estatísticas
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                updateStatsUI(stats)
            }
        }
    }
    
    private fun buildSuccessMessage(state: SyncState.Success): String {
        return """
            ${state.message}
            
            Patrimônios: ${state.patrimoniosSincronizados}
            Salas: ${state.salasSincronizadas}
            Tempo: ${state.tempoDecorrido}ms
        """.trimIndent()
    }
    
    private fun updateStatsUI(stats: Map<String, Int>) {
        binding.tvPatrimoniosCount.text = stats["patrimonios"]?.toString() ?: "0"
        binding.tvSalasCount.text = stats["salas"]?.toString() ?: "0"
        binding.tvColetadosCount.text = stats["coletados"]?.toString() ?: "0"
        binding.tvPendentesCount.text = stats["pendentes"]?.toString() ?: "0"
    }
    
    private fun updateNetworkStatus() {
        val isOnline = NetworkUtils.isNetworkAvailable(this)
        val connectionType = NetworkUtils.getConnectionType(this)
        
        binding.tvNetworkStatus.text = if (isOnline) {
            "✓ Online ($connectionType)"
        } else {
            "✗ Offline"
        }
        binding.tvNetworkStatus.setTextColor(
            getColor(if (isOnline) R.color.success else R.color.error)
        )
        
        // Habilitar/desabilitar botões
        binding.btnSyncNow.isEnabled = isOnline
    }
    
    private fun showClearDataConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Limpar Dados Locais")
            .setMessage("Tem certeza que deseja limpar todos os dados locais?\n\nIsso removerá todos os patrimônios e salas do banco local. Você precisará sincronizar novamente.")
            .setPositiveButton("Sim, Limpar") { _, _ ->
                viewModel.clearLocalData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showLoading(message: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvSyncProgress.visibility = View.VISIBLE
        binding.tvSyncProgress.text = message
        binding.btnSyncNow.isEnabled = false
        binding.btnClearData.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.tvSyncProgress.visibility = View.GONE
        updateNetworkStatus() // Reabilitar botões baseado na conexão
        binding.btnClearData.isEnabled = true
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.success))
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
