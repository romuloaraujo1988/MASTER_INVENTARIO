package com.inventario.mobile.presentation.sync

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import com.inventario.mobile.R
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class SyncActivity : AppCompatActivity() {

    private lateinit var viewModel: SyncViewModel
    
    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressCard: View
    private lateinit var btnSync: Button
    private lateinit var btnRefresh: Button
    private lateinit var btnViewPendingCollections: Button
    private lateinit var btnClearPendingCollections: Button
    private lateinit var tvLastSync: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSyncMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        
        // Inicializar ViewModel
        val repository = com.inventario.mobile.data.repository.InventarioRepository(
            com.inventario.mobile.data.remote.api.MockApiService(),
            com.inventario.mobile.data.local.LocalDataManager(this)
        )
        val factory = SyncViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SyncViewModel::class.java]
        
        setupUI()
        setupObservers()
        setupListeners()
        
        // Carregar status inicial
        viewModel.loadSyncStatus()
    }

    private fun setupUI() {
        // Configurar toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Configurar listener do botão de voltar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Inicializar views
        progressCard = findViewById(R.id.progressCard)
        btnSync = findViewById(R.id.btnSync)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnViewPendingCollections = findViewById(R.id.btnViewPendingCollections)
        btnClearPendingCollections = findViewById(R.id.btnClearPendingCollections)
        tvLastSync = findViewById(R.id.tvLastSync)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        progressBar = findViewById(R.id.progressBar)
        tvSyncMessage = findViewById(R.id.tvSyncMessage)
    }

    private fun setupObservers() {
        // Observar o estado do ViewModel usando StateFlow
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { uiState ->
            // Loading state
            if (uiState.isLoading) {
                progressCard.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                tvSyncMessage.text = "Carregando..."
            } else {
                progressBar.visibility = View.GONE
            }
            
            // Syncing state
            progressCard.visibility = if (uiState.isSyncing) View.VISIBLE else View.GONE
            btnSync.isEnabled = !uiState.isSyncing
            btnRefresh.isEnabled = !uiState.isSyncing
            
            // Last sync time
            tvLastSync.text = if (uiState.lastSyncTime != null) {
                "Última sincronização: ${uiState.lastSyncTime}"
            } else {
                "Nenhuma sincronização realizada"
            }
            
            // Pending sync count
            tvPendingCount.text = when (uiState.pendingSyncCount) {
                0 -> "Todos os dados estão sincronizados"
                1 -> "1 item aguardando sincronização"
                else -> "${uiState.pendingSyncCount} itens aguardando sincronização"
            }
            
            // Sync progress
            progressBar.progress = uiState.syncProgress
            
            // Sync message
            tvSyncMessage.text = uiState.syncMessage
            
            // Error handling
             uiState.errorMessage?.let { errorMessage ->
                 Toast.makeText(this@SyncActivity, errorMessage, Toast.LENGTH_LONG).show()
                 Log.e("SyncActivity", "Erro: $errorMessage")
             }
            
            // Delete state
            btnClearPendingCollections.isEnabled = !uiState.isDeleting
            if (uiState.isDeleting) {
                btnClearPendingCollections.text = "Excluindo..."
            } else {
                btnClearPendingCollections.text = "Excluir Coletas Pendentes"
            }
            
            // Delete success
             if (uiState.deleteSuccess) {
                 Toast.makeText(this@SyncActivity, "Coletas pendentes excluídas com sucesso!", Toast.LENGTH_SHORT).show()
                 viewModel.clearMessages()
             }
            }
        }
    }

    private fun setupListeners() {
        btnSync.setOnClickListener {
            try {
                viewModel.startSync()
            } catch (e: Exception) {
                Log.e("SyncActivity", "Erro ao iniciar sincronização", e)
                Toast.makeText(this, "Erro ao iniciar sincronização", Toast.LENGTH_SHORT).show()
            }
        }

        btnRefresh.setOnClickListener {
            try {
                viewModel.loadSyncStatus()
            } catch (e: Exception) {
                Log.e("SyncActivity", "Erro ao atualizar status", e)
                Toast.makeText(this, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnViewPendingCollections.setOnClickListener {
            startActivity(PendingCollectionsActivity.newIntent(this))
        }
        
        btnClearPendingCollections.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir todas as coletas pendentes de sincronização?\n\nEsta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.clearPendingCollections()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}