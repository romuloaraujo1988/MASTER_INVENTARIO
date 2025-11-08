package com.inventario.mobile.presentation.coleta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.ActivityCollectionViewBinding
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.data.repository.InventarioRepository

class CollectionViewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CollectionViewActivity"
    }

    private lateinit var binding: ActivityCollectionViewBinding

    private val viewModel: CollectionViewViewModel by viewModels {
        val apiService = NetworkModule.getApiService(this)
        val repository = InventarioRepository.getInstance(this, apiService)
        CollectionViewViewModelFactory(repository)
    }

    private val adapter = CollectionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: iniciando CollectionViewActivity")
        binding = ActivityCollectionViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUserFilter()
        observeViewModel()

        Log.d(TAG, "onCreate: chamando viewModel.loadColetas()")
        viewModel.loadColetas()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: configurando RecyclerView")
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewColetas.apply {
            this.layoutManager = layoutManager
            adapter = this@CollectionViewActivity.adapter
            
            // Adicionar scroll listener para paginação infinita
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    // Verificar se chegou perto do fim da lista
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    
                    // TODO: Implementar paginação quando necessário
                    // Carregar mais quando estiver a 5 itens do fim
                    // if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                    //     && firstVisibleItemPosition >= 0) {
                    //     Log.d(TAG, "onScrolled: Próximo do fim, carregando mais...")
                    //     viewModel.loadNextPage()
                    // }
                }
            })
        }
    }
    
    private fun setupUserFilter() {
        binding.chipGroupUser.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAllUsers.id -> {
                        viewModel.filterByUsuario(CollectionViewViewModel.FiltroUsuario.TODAS)
                    }
                    binding.chipMyCollections.id -> {
                        viewModel.filterByUsuario(CollectionViewViewModel.FiltroUsuario.MINHAS)
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
                    viewModel.filterBySala(null)
                } else {
                    // Sala específica selecionada
                    val sala = salas[position - 1]
                    viewModel.filterBySala(sala)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                viewModel.filterBySala(null)
            }
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: iniciando observação do ViewModel")
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                Log.d(TAG, "observeViewModel: novo estado recebido - isLoading=${state.isLoading}, totalColetas=${state.totalColetas}, filteredColetas.size=${state.filteredColetas.size}")
                
                // Loading principal (primeira carga)
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Loading de mais itens (paginação)
                // TODO: Adicionar um footer no RecyclerView para mostrar loading
                // Por enquanto, o progressBar principal serve para ambos

                // Error handling (TODO: show dialog/toast)
                // state.error?.let { }

                // List
                Log.d(TAG, "observeViewModel: submetendo ${state.filteredColetas.size} coletas para o adapter")
                adapter.submitList(state.filteredColetas)

                // Header stats
                binding.tvTotalColetas.text = state.totalColetas.toString()
                binding.tvPendingSync.text = state.pendentes.toString()
                
                // Setup sala spinner when salas are loaded
                if (state.salas.isNotEmpty() && binding.spinnerSalas.adapter == null) {
                    Log.d(TAG, "observeViewModel: configurando spinner com ${state.salas.size} salas")
                    setupSalaSpinner(state.salas)
                }
                
                // Empty state
                val showEmptyState = state.filteredColetas.isEmpty() && !state.isLoading
                Log.d(TAG, "observeViewModel: showEmptyState=$showEmptyState")
                binding.layoutEmptyState.visibility = 
                    if (showEmptyState) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}