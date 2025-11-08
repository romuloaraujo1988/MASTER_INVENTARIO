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
import com.inventario.mobile.data.remote.api.MockApiService
import kotlinx.coroutines.launch
class PendingCollectionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingCollectionsBinding
    private val viewModel: PendingCollectionsViewModel by viewModels {
        PendingCollectionsViewModelFactory(
            InventarioRepository(MockApiService(), LocalDataManager(this))
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
        adapter = PendingCollectionsAdapter { coleta ->
            showDeleteConfirmationDialog(coleta)
        }

        binding.recyclerViewPendingCollections.apply {
            layoutManager = LinearLayoutManager(this@PendingCollectionsActivity)
            adapter = this@PendingCollectionsActivity.adapter
        }
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

        // Show/hide empty state
        if (state.pendingCollections.isEmpty() && !state.isLoading) {
            binding.recyclerViewPendingCollections.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}