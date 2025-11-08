package com.inventario.mobile.presentation.offline

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityOfflineManagerBinding
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.remote.api.ApiClient
import com.inventario.mobile.data.sync.SyncManager
import com.inventario.mobile.data.sync.SyncResult
import com.inventario.mobile.data.sync.SyncState
import com.inventario.mobile.presentation.sync.PendingCollectionsActivity
import com.inventario.mobile.utils.NetworkMonitor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OfflineManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfflineManagerBinding
    private lateinit var syncManager: SyncManager
    private lateinit var networkMonitor: NetworkMonitor
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        initializeManagers()
        setupListeners()
        observeStates()
        
        // Sincronizar automaticamente se veio com flag
        if (intent.getBooleanExtra("AUTO_SYNC", false)) {
            syncManager.forceSyncNow()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeManagers() {
        val database = InventarioDatabase.getDatabase(this)
        val apiService = ApiClient.getApiService(this)
        
        syncManager = SyncManager.getInstance(this, database, apiService)
        networkMonitor = NetworkMonitor(this)
    }

    private fun setupListeners() {
        // Switch de sincronização automática
        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            syncManager.setAutoSyncEnabled(isChecked)
        }
        
        // Botão sincronizar agora
        binding.btnSyncNow.setOnClickListener {
            lifecycleScope.launch {
                syncManager.syncPendingData()
            }
        }
        
        // Botão tentar novamente erros
        binding.btnRetryFailed.setOnClickListener {
            lifecycleScope.launch {
                syncManager.retryFailedSync()
            }
        }
        
        // Botão ver pendentes
        binding.btnViewPending.setOnClickListener {
            val intent = Intent(this, PendingCollectionsActivity::class.java)
            startActivity(intent)
        }
        
        // Switch WiFi only
        binding.switchSyncOnWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implementar lógica de WiFi only
        }
        
        // Switch notificações
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implementar lógica de notificações
        }
    }

    private fun observeStates() {
        // Observar status de rede
        lifecycleScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                updateConnectionStatus(isOnline)
            }
        }
        
        // Observar estado de sincronização
        lifecycleScope.launch {
            syncManager.syncState.collect { state ->
                updateSyncState(state)
            }
        }
        
        // Observar contador de pendentes
        lifecycleScope.launch {
            syncManager.pendingCount.collect { count ->
                updatePendingCount(count)
            }
        }
    }

    private fun updateConnectionStatus(isOnline: Boolean) {
        if (isOnline) {
            binding.ivConnectionStatus.setImageResource(R.drawable.ic_sync_done)
            binding.ivConnectionStatus.setColorFilter(getColor(R.color.success))
            binding.tvConnectionStatus.text = "Online"
            binding.tvConnectionStatus.setTextColor(getColor(R.color.success))
            
            val connectionType = networkMonitor.getConnectionType()
            binding.tvConnectionType.text = when (connectionType) {
                NetworkMonitor.ConnectionType.WIFI -> "WiFi conectado"
                NetworkMonitor.ConnectionType.CELLULAR -> "Dados móveis"
                NetworkMonitor.ConnectionType.ETHERNET -> "Ethernet"
                else -> "Conectado"
            }
            
            binding.cardConnectionStatus.setCardBackgroundColor(getColor(R.color.success_light))
            binding.btnSyncNow.isEnabled = true
            
        } else {
            binding.ivConnectionStatus.setImageResource(R.drawable.ic_sync_error)
            binding.ivConnectionStatus.setColorFilter(getColor(R.color.error))
            binding.tvConnectionStatus.text = "Offline"
            binding.tvConnectionStatus.setTextColor(getColor(R.color.error))
            binding.tvConnectionType.text = "Sem conexão com a internet"
            
            binding.cardConnectionStatus.setCardBackgroundColor(getColor(R.color.error_light))
            binding.btnSyncNow.isEnabled = false
        }
    }

    private fun updateSyncState(state: SyncState) {
        when (state) {
            is SyncState.Idle -> {
                binding.cardSyncProgress.visibility = View.GONE
                binding.btnSyncNow.isEnabled = networkMonitor.isCurrentlyOnline()
                binding.btnSyncNow.text = "Sincronizar Agora"
            }
            
            is SyncState.Syncing -> {
                binding.cardSyncProgress.visibility = View.VISIBLE
                binding.tvSyncProgress.text = "${state.current}/${state.total}"
                
                val progress = if (state.total > 0) {
                    (state.current * 100) / state.total
                } else {
                    0
                }
                binding.progressBarSync.progress = progress
                
                binding.btnSyncNow.isEnabled = false
                binding.btnSyncNow.text = "Sincronizando..."
            }
            
            is SyncState.Success -> {
                binding.cardSyncProgress.visibility = View.GONE
                binding.btnSyncNow.isEnabled = true
                binding.btnSyncNow.text = "Sincronizar Agora"
                
                val result = state.result
                if (result is SyncResult.Success) {
                    updateLastSyncInfo(result)
                    
                    // Mostrar botão de retry se houver erros
                    binding.btnRetryFailed.visibility = if (result.errors > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
            
            is SyncState.Error -> {
                binding.cardSyncProgress.visibility = View.GONE
                binding.btnSyncNow.isEnabled = true
                binding.btnSyncNow.text = "Sincronizar Agora"
                
                binding.tvLastSyncResult.visibility = View.VISIBLE
                binding.tvLastSyncResult.text = "Erro: ${state.message}"
                binding.tvLastSyncResult.setTextColor(getColor(R.color.error))
            }
        }
    }

    private fun updatePendingCount(count: Int) {
        binding.tvPendingCount.text = count.toString()
        
        // Atualizar visibilidade do botão de ver pendentes
        binding.btnViewPending.isEnabled = count > 0
    }

    private fun updateLastSyncInfo(result: SyncResult.Success) {
        binding.tvLastSync.text = dateFormat.format(Date())
        
        binding.tvLastSyncResult.visibility = View.VISIBLE
        binding.tvLastSyncResult.text = buildString {
            append("${result.success} sincronizados")
            if (result.errors > 0) {
                append(", ${result.errors} erros")
            }
        }
        
        binding.tvLastSyncResult.setTextColor(
            if (result.errors > 0) getColor(R.color.warning) else getColor(R.color.success)
        )
        
        // Atualizar contadores
        binding.tvSyncedCount.text = result.success.toString()
        binding.tvErrorCount.text = result.errors.toString()
    }

    override fun onResume() {
        super.onResume()
        // Atualizar contadores ao retornar
        lifecycleScope.launch {
            updatePendingCount(syncManager.pendingCount.value)
        }
    }
}
