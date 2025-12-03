package com.inventario.mobile.presentation.sync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityPendingCollectionsBinding
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.data.remote.api.ApiClient
import kotlinx.coroutines.launch

class PendingCollectionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingCollectionsBinding
    
    // Correção: Usar ApiClient real ao invés de MockApiService
    // e LocalDataManager.getInstance() ao invés de nova instância
    private val viewModel: PendingCollectionsViewModel by viewModels {
        PendingCollectionsViewModelFactory(
            InventarioRepository(
                ApiClient.getApiService(this),
                LocalDataManager.getInstance(this),
                this
            )
        )
    }
    private lateinit var adapter: PendingCollectionsAdapter

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PendingCollectionsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingCollectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Coletas Pendentes"
        }
    }

    private fun setupRecyclerView() {
        adapter = PendingCollectionsAdapter(
            onDeleteClick = { coleta ->
                showDeleteConfirmationDialog(coleta)
            },
            onRetryClick = { coleta ->
                // v2.6: Tentar sincronizar coleta novamente
                showRetryConfirmationDialog(coleta)
            }
        )

        binding.recyclerViewPendingCollections.apply {
            layoutManager = LinearLayoutManager(this@PendingCollectionsActivity)
            adapter = this@PendingCollectionsActivity.adapter
        }
    }
    
    /**
     * v2.6: Dialog de confirmação para tentar sincronizar novamente
     */
    private fun showRetryConfirmationDialog(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("Tentar Novamente")
            .setMessage("Deseja tentar sincronizar a coleta do patrimônio ${coleta.numeroPatrimonio ?: coleta.patrimonioId} novamente?")
            .setPositiveButton("Tentar") { _, _ ->
                viewModel.retryCollection(coleta)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupClickListeners() {
        binding.btnSyncAll.setOnClickListener {
            showSyncAllConfirmationDialog()
        }

        binding.btnDeleteAll.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }

        // Refresh será feito automaticamente no onResume
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: PendingCollectionsUiState) {
        // Log de diagnóstico
        android.util.Log.d("PendingCollectionsUI", "🔄 Atualizando UI - isLoading: ${state.isLoading}, coletas: ${state.pendingCollections.size}")
        
        // Update loading state
        binding.progressOverlay.visibility = if (state.isLoading || state.isDeleting) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Update pending count
        binding.tvPendingCount.text = "${state.pendingCollections.size}"

        // Update RecyclerView
        adapter.submitList(state.pendingCollections)
        android.util.Log.d("PendingCollectionsUI", "📋 Lista submetida ao adapter: ${state.pendingCollections.size} itens")

        // Show/hide empty state
        if (state.pendingCollections.isEmpty() && !state.isLoading) {
            android.util.Log.d("PendingCollectionsUI", "📭 Mostrando estado vazio")
            binding.recyclerViewPendingCollections.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            android.util.Log.d("PendingCollectionsUI", "📋 Mostrando lista de coletas")
            binding.recyclerViewPendingCollections.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }

        // Enable/disable action buttons based on collection count
        val hasCollections = state.pendingCollections.isNotEmpty()
        binding.btnSyncAll.isEnabled = hasCollections && !state.isLoading && !state.isDeleting
        binding.btnDeleteAll.isEnabled = hasCollections && !state.isLoading && !state.isDeleting

        // Show messages
        state.errorMessage?.let { message ->
            showSnackbar(message, isError = true)
            // Clear messages after a delay to avoid infinite loop
            lifecycleScope.launch {
                kotlinx.coroutines.delay(100)
                viewModel.clearMessages()
            }
        }

        state.successMessage?.let { message ->
            showSnackbar(message, isError = false)
            // Clear messages after a delay to avoid infinite loop
            lifecycleScope.launch {
                kotlinx.coroutines.delay(100)
                viewModel.clearMessages()
            }
        }
    }

    private fun showDeleteConfirmationDialog(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Coleta")
            .setMessage("Deseja realmente excluir a coleta do patrimônio ${coleta.patrimonioId}?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteCollection(coleta)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Todas as Coletas")
            .setMessage("Deseja realmente excluir todas as coletas pendentes? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir Todas") { _, _ ->
                viewModel.deleteAllCollections()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSyncAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sincronizar Todas as Coletas")
            .setMessage("Deseja sincronizar todas as coletas pendentes com o servidor?")
            .setPositiveButton("Sincronizar") { _, _ ->
                viewModel.syncAllCollections()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSnackbar(message: String, isError: Boolean) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(getColor(R.color.error))
        } else {
            snackbar.setBackgroundTint(getColor(R.color.success))
        }
        snackbar.show()
    }

    /**
     * v2.7: Recarrega a lista de coletas pendentes ao voltar para a tela
     * Garante que os dados estejam sempre atualizados
     */
    override fun onResume() {
        super.onResume()
        android.util.Log.d("PendingCollectionsUI", "🔄 onResume - Recarregando coletas pendentes...")
        viewModel.loadPendingCollections()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
