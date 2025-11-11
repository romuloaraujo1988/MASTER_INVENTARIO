package com.inventario.mobile.presentation.sync

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivitySyncBinding
import com.inventario.mobile.data.local.database.AppDatabase
import com.inventario.mobile.data.repository.SyncRepository
import com.inventario.mobile.utils.NetworkUtils
import com.inventario.mobile.utils.PreferencesManager
import com.google.android.material.snackbar.Snackbar
import android.widget.Toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para gerenciar sincronização de dados
 */
class SyncActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySyncBinding
    private lateinit var syncRepository: SyncRepository
    private lateinit var preferencesManager: PreferencesManager
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        initializeRepository()
        setupListeners()
        updateUI()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sincronização de Dados"
        }
    }
    
    private fun initializeRepository() {
        preferencesManager = PreferencesManager(this)
        
        val database = AppDatabase.getInstance(this)
        // TODO: Implementar criação correta das APIs
        // val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(this)
        
        // Criar instâncias das APIs
        // val patrimonioApi = apiService.create(com.inventario.mobile.api.PatrimonioApi::class.java)
        // val salaApi = apiService.create(com.inventario.mobile.api.SalaApi::class.java)
        
        // TODO: Descomentar quando APIs estiverem disponíveis
        /*
        syncRepository = SyncRepository(
            context = this,
            patrimonioApi = patrimonioApi,
            salaApi = salaApi,
            patrimonioDao = database.patrimonioDao(),
            salaDao = database.salaDao(),
            sincronizacaoDao = database.sincronizacaoDao(),
            preferencesManager = preferencesManager
        )
        */
    }
    
    private fun setupListeners() {
        binding.btnSyncNow.setOnClickListener {
            // TODO: Implementar quando syncRepository estiver disponível
            // forceSyncFromServer()
            Toast.makeText(this, "Sincronização temporariamente desabilitada", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnClearData.setOnClickListener {
            // TODO: Implementar quando syncRepository estiver disponível
            // showClearDataConfirmation()
            Toast.makeText(this, "Limpeza temporariamente desabilitada", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }
    
    private fun updateUI() {
        lifecycleScope.launch {
            try {
                // Status de rede
                val isOnline = NetworkUtils.isNetworkAvailable(this@SyncActivity)
                val connectionType = NetworkUtils.getConnectionType(this@SyncActivity)
                
                binding.tvNetworkStatus.text = if (isOnline) {
                    "✓ Online ($connectionType)"
                } else {
                    "✗ Offline"
                }
                binding.tvNetworkStatus.setTextColor(
                    getColor(if (isOnline) R.color.success else R.color.error)
                )
                
                // TODO: Implementar quando syncRepository estiver disponível
                /*
                // Estatísticas locais
                val stats = syncRepository.getLocalStats()
                binding.tvPatrimoniosCount.text = stats["patrimonios"]?.toString() ?: "0"
                binding.tvSalasCount.text = stats["salas"]?.toString() ?: "0"
                binding.tvColetadosCount.text = stats["coletados"]?.toString() ?: "0"
                binding.tvPendentesCount.text = stats["pendentes"]?.toString() ?: "0"
                
                // Última sincronização
                val lastSync = syncRepository.getLastSync()
                if (lastSync != null) {
                    binding.tvLastSyncDate.text = dateFormat.format(lastSync.dataHora)
                    binding.tvLastSyncStatus.text = if (lastSync.sincronizado) {
                        "✓ Sucesso"
                    } else {
                        "✗ Falha"
                    }
                    binding.tvLastSyncStatus.setTextColor(
                        getColor(if (lastSync.sincronizado) R.color.success else R.color.error)
                    )
                    binding.tvLastSyncDetails.text = lastSync.mensagem
                    
                    binding.cardLastSync.visibility = View.VISIBLE
                } else {
                    binding.cardLastSync.visibility = View.GONE
                }
                */
                
                // Habilitar/desabilitar botão de sincronização
                binding.btnSyncNow.isEnabled = isOnline
                
            } catch (e: Exception) {
                showError("Erro ao atualizar interface: ${e.message}")
            }
        }
    }
    
    private fun forceSyncFromServer() {
        lifecycleScope.launch {
            try {
                // Mostrar loading
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSyncNow.isEnabled = false
                binding.tvSyncProgress.visibility = View.VISIBLE
                binding.tvSyncProgress.text = "Sincronizando dados do servidor..."
                
                // Executar sincronização
                val result = syncRepository.forceSyncFromServer()
                
                if (result.isSuccess) {
                    val syncResult = result.getOrNull()!!
                    
                    val message = """
                        Sincronização concluída com sucesso!
                        
                        Patrimônios: ${syncResult.patrimoniosSincronizados}
                        Salas: ${syncResult.salasSincronizadas}
                        Tempo: ${syncResult.tempoDecorrido}ms
                    """.trimIndent()
                    
                    showSuccess(message)
                    updateUI()
                } else {
                    val error = result.exceptionOrNull()
                    showError("Erro na sincronização: ${error?.message}")
                }
                
            } catch (e: Exception) {
                showError("Erro ao sincronizar: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnSyncNow.isEnabled = true
                binding.tvSyncProgress.visibility = View.GONE
            }
        }
    }
    
    private fun showClearDataConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Limpar Dados Locais")
            .setMessage("Tem certeza que deseja limpar todos os dados locais?\n\nIsso removerá todos os patrimônios e salas do banco local. Você precisará sincronizar novamente.")
            .setPositiveButton("Sim, Limpar") { _, _ ->
                clearLocalData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun clearLocalData() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                
                val result = syncRepository.clearLocalData()
                
                if (result.isSuccess) {
                    showSuccess("Dados locais limpos com sucesso!")
                    updateUI()
                } else {
                    showError("Erro ao limpar dados: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                showError("Erro ao limpar dados: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
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
