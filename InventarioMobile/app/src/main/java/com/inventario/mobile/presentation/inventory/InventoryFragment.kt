package com.inventario.mobile.presentation.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.di.NetworkModule
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.inventario.mobile.databinding.FragmentInventoryBinding
import com.inventario.mobile.presentation.adapter.PatrimonioAdapter
import com.inventario.mobile.presentation.scanner.ScannerActivity
import com.inventario.mobile.presentation.inventory.StatusFilter
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InventoryViewModel
    private lateinit var adapter: PatrimonioAdapter

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val patrimonioId = result.data?.getStringExtra(ScannerActivity.EXTRA_PATRIMONIO_ID)
            patrimonioId?.let { id ->
                // Buscar o patrimônio específico usando o campo de busca
                binding.editTextSearch.setText(id)
                viewModel.searchPatrimonios(id)
                // Mostrar toast de sucesso
                Toast.makeText(
                    requireContext(),
                    "Patrimônio encontrado: $id",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        setupFilters()
        setupClickListeners()
        observeUiState()
    }
    
    private fun setupViewModel() {
        try {
            val apiService = NetworkModule.getApiService(requireContext())
            val repository = InventarioRepository.getInstance(requireContext(), apiService)
            val factory = InventoryViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[InventoryViewModel::class.java]
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Erro ao inicializar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = PatrimonioAdapter { patrimonio ->
            // TODO: Navegar para detalhes do patrimônio
            Toast.makeText(context, "Patrimônio: ${patrimonio.numeroPatrimonio}", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerViewPatrimonios.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InventoryFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.editTextSearch.addTextChangedListener { text ->
            viewModel.searchPatrimonios(text?.toString() ?: "")
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPatrimonios()
        }
    }

    private fun setupFilters() {
        // Setup filtro de responsáveis
        binding.autoCompleteResponsavel.setOnItemClickListener { _, _, position, _ ->
            val responsavel = if (position == 0) null else binding.autoCompleteResponsavel.adapter.getItem(position) as String
            viewModel.filterByResponsavel(responsavel)
        }

        // Setup filtro de status
        val statusOptions = listOf("Todos", "Pendentes", "Coletados")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
        binding.autoCompleteStatus.setAdapter(statusAdapter)
        
        binding.autoCompleteStatus.setOnItemClickListener { _, _, position, _ ->
            val statusFilter = when (position) {
                0 -> StatusFilter.TODOS
                1 -> StatusFilter.PENDENTES
                2 -> StatusFilter.COLETADOS
                else -> StatusFilter.TODOS
            }
            viewModel.filterByStatus(statusFilter)
        }

        // Setup filtro de salas
        binding.autoCompleteSala.setOnItemClickListener { _, _, position, _ ->
            val sala = if (position == 0) null else binding.autoCompleteSala.adapter.getItem(position) as String
            viewModel.filterBySala(sala)
        }
    }

    private fun setupClickListeners() {
        binding.buttonClearFilters.setOnClickListener {
            viewModel.clearFilters()
            binding.editTextSearch.text?.clear()
            binding.autoCompleteResponsavel.setText("Todos", false)
            binding.autoCompleteStatus.setText("Todos", false)
            binding.autoCompleteSala.setText("Todas as Salas", false)
        }
        
        binding.fabScanQr.setOnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java).apply {
                putExtra(ScannerActivity.EXTRA_ALLOW_COLLECTION, true)
            }
            scannerLauncher.launch(intent)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: InventoryUiState) {
        // Atualizar lista de patrimônios
        adapter.submitList(state.filteredPatrimonios)
        
        // Atualizar contador
        binding.textViewCount.text = "Total: ${state.filteredPatrimonios.size} patrimônios"
        
        // Atualizar contadores de status
        binding.textViewPendentesCount.text = state.totalPendentes.toString()
        binding.textViewColetadosCount.text = state.totalColetados.toString()
        
        // Atualizar dropdown de responsáveis
        if (state.responsaveis.isNotEmpty()) {
            val responsaveisOptions = listOf("Todos") + state.responsaveis
            val responsaveisAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, responsaveisOptions)
            binding.autoCompleteResponsavel.setAdapter(responsaveisAdapter)
        }

        // Atualizar dropdown de salas (apenas salas com coletas)
        if (state.salasComColetas.isNotEmpty()) {
            val salasOptions = listOf("Todas as Salas") + state.salasComColetas
            val salasAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, salasOptions)
            binding.autoCompleteSala.setAdapter(salasAdapter)
        }
        
        // Atualizar estado de carregamento
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.swipeRefreshLayout.isRefreshing = state.isRefreshing
        
        // Atualizar visibilidade da lista
        if (state.filteredPatrimonios.isEmpty() && !state.isLoading) {
            binding.recyclerViewPatrimonios.visibility = View.GONE
            binding.textViewEmpty.visibility = View.VISIBLE
            binding.textViewEmpty.text = if (state.searchQuery.isNotBlank() || 
                state.selectedSetor != null || state.selectedSala != null ||
                state.selectedResponsavel != null || state.selectedStatus != StatusFilter.TODOS) {
                "Nenhum patrimônio encontrado com os filtros aplicados"
            } else {
                "Nenhum patrimônio cadastrado"
            }
        } else {
            binding.recyclerViewPatrimonios.visibility = View.VISIBLE
            binding.textViewEmpty.visibility = View.GONE
        }
        
        // Exibir erros
        state.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
        
        // Atualizar botão de limpar filtros
        val hasFilters = state.searchQuery.isNotBlank() || 
            state.selectedSetor != null || state.selectedSala != null ||
            state.selectedResponsavel != null || state.selectedStatus != StatusFilter.TODOS
        binding.buttonClearFilters.visibility = if (hasFilters) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}