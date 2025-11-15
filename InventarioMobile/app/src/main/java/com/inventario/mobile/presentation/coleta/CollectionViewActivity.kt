package com.inventario.mobile.presentation.coleta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.ActivityCollectionViewBinding
import com.inventario.mobile.presentation.state.CollectionViewState
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity para visualização de coletas
 * 
 * Migrada para Clean Architecture + MVVM
 * - Usa @AndroidEntryPoint para injeção Hilt
 * - Observa CollectionViewState (sealed class)
 * - Delega lógica para CollectionViewViewModelClean
 */
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CollectionViewActivity"
    }

    private lateinit var binding: ActivityCollectionViewBinding

    // ViewModel Clean Architecture (injetado via Hilt)
    private val viewModel: CollectionViewViewModelClean by viewModels()

    private val adapter = CollectionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: iniciando CollectionViewActivity (Clean Architecture)")
        binding = ActivityCollectionViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Adicionar indicador de modo offline
        com.inventario.mobile.ui.components.OfflineIndicator.setup(this)

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        Log.d(TAG, "onCreate: carregando coletas via Use Case")
        viewModel.carregarColetas()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: configurando RecyclerView")
        binding.recyclerViewColetas.apply {
            layoutManager = LinearLayoutManager(this@CollectionViewActivity)
            adapter = this@CollectionViewActivity.adapter
        }
    }
    
    private fun setupFilters() {
        // Filtro de Usuário
        binding.chipGroupUser.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAllUsers.id -> {
                        viewModel.filtrarPorUsuario(CollectionViewViewModelClean.FiltroUsuario.TODAS)
                    }
                    binding.chipMyCollections.id -> {
                        viewModel.filtrarPorUsuario(CollectionViewViewModelClean.FiltroUsuario.MINHAS)
                    }
                }
            }
        }
        
        // Filtro de Status
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAll.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.TODOS)
                    }
                    binding.chipSynced.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.SINCRONIZADOS)
                    }
                    binding.chipPending.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.PENDENTES)
                    }
                }
            }
        }
    }
    
    private fun setupSalaSpinner(salas: List<String>) {
        val salaOptions = mutableListOf("Todas as Salas")
        salaOptions.addAll(salas)
        
        val spinnerAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            salaOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerSalas.adapter = spinnerAdapter
        binding.spinnerSalas.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // "Todas as Salas" selecionado
                    viewModel.filtrarPorSala(null)
                } else {
                    // Sala específica selecionada
                    val sala = salas[position - 1]
                    viewModel.filtrarPorSala(sala)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                viewModel.filtrarPorSala(null)
            }
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: iniciando observação do ViewModel (Clean Architecture)")
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is CollectionViewState.Idle -> {
                        Log.d(TAG, "Estado: Idle")
                        hideLoading()
                    }
                    
                    is CollectionViewState.Loading -> {
                        Log.d(TAG, "Estado: Loading")
                        showLoading()
                    }
                    
                    is CollectionViewState.Success -> {
                        Log.d(TAG, "Estado: Success - ${state.filteredColetas.size} coletas filtradas")
                        hideLoading()
                        updateUI(state)
                    }
                    
                    is CollectionViewState.Error -> {
                        Log.e(TAG, "Estado: Error - ${state.message}")
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun updateUI(state: CollectionViewState.Success) {
        // Atualizar lista (já está em data.model.Coleta)
        Log.d(TAG, "updateUI: submetendo ${state.filteredColetas.size} coletas para o adapter")
        adapter.submitList(state.filteredColetas)
        
        // Atualizar estatísticas
        binding.tvTotalColetas.text = state.totalColetas.toString()
        binding.tvPendingSync.text = state.pendentes.toString()
        
        // Configurar spinner de salas (apenas uma vez)
        if (state.salas.isNotEmpty() && binding.spinnerSalas.adapter == null) {
            Log.d(TAG, "updateUI: configurando spinner com ${state.salas.size} salas")
            setupSalaSpinner(state.salas)
        }
        
        // Estado vazio
        val showEmptyState = state.filteredColetas.isEmpty()
        Log.d(TAG, "updateUI: showEmptyState=$showEmptyState")
        binding.layoutEmptyState.visibility = 
            if (showEmptyState) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        // TODO: Mostrar dialog ou toast com erro
        Log.e(TAG, "showError: $message")
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}