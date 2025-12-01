package com.inventario.mobile.presentation.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.databinding.FragmentInventarioPorResponsavelBinding
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.data.model.Responsavel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para exibir inventário filtrado por responsável.
 * Extraído da InventarioActivity original para uso com TabLayout.
 */
@AndroidEntryPoint
class InventarioPorResponsavelFragment : Fragment() {
    
    private var _binding: FragmentInventarioPorResponsavelBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: InventarioViewModel
    private lateinit var adapter: PatrimonioAdapter
    private var isLoadingMore = false
    private var responsavelSelecionado: Responsavel? = null
    private var statusColetaSelecionado: Boolean? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioPorResponsavelBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupFiltros()
        
        // Carregar responsáveis
        viewModel.loadResponsaveis()
    }
    
    private fun setupViewModel() {
        val apiService = NetworkModule.getApiService(requireContext())
        val repository = InventarioRepository.getInstance(requireContext(), apiService)
        val factory = InventarioViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[InventarioViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = PatrimonioAdapter { patrimonio ->
            showPatrimonioDetailsDialog(patrimonio)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InventarioPorResponsavelFragment.adapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    
                    if (!isLoadingMore && 
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                        firstVisibleItemPosition >= 0) {
                        
                        val currentState = viewModel.uiState.value
                        if (currentState.hasMorePages && !currentState.isLoading) {
                            isLoadingMore = true
                            viewModel.loadMorePatrimonios()
                        }
                    }
                }
            })
        }
    }
    
    private fun setupFiltros() {
        // Configurar chips de status
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            when {
                binding.chipTodos.isChecked -> statusColetaSelecionado = null
                binding.chipColetados.isChecked -> statusColetaSelecionado = true
                binding.chipNaoColetados.isChecked -> statusColetaSelecionado = false
            }
            aplicarFiltros()
        }
        
        // Botão limpar filtros
        binding.btnLimparFiltros.setOnClickListener {
            limparFiltros()
        }
    }
    
    private fun setupResponsavelSpinner(responsaveis: List<Responsavel>) {
        if (responsaveis.isEmpty()) return
        
        val autoComplete = binding.spinnerResponsavel as? AutoCompleteTextView ?: return
        
        if (autoComplete.adapter != null && autoComplete.adapter.count == responsaveis.size) {
            return
        }
        
        val items = responsaveis.map { it.nome }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        
        autoComplete.setAdapter(spinnerAdapter)
        autoComplete.setText("Selecione um responsável", false)
        
        autoComplete.setOnItemClickListener { _, _, position, _ ->
            responsavelSelecionado = responsaveis[position]
            aplicarFiltros()
        }
    }
    
    private fun aplicarFiltros() {
        val responsavelId = responsavelSelecionado?.id
        val coletado = statusColetaSelecionado
        
        if (responsavelId != null) {
            viewModel.loadPatrimoniosByResponsavel(responsavelId, coletado)
        } else {
            viewModel.clearPatrimonios()
        }
    }
    
    private fun limparFiltros() {
        responsavelSelecionado = null
        (binding.spinnerResponsavel as? AutoCompleteTextView)?.setText("Selecione um responsável", false)
        
        statusColetaSelecionado = null
        binding.chipTodos.isChecked = true
        
        viewModel.clearPatrimonios()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.responsaveis.isNotEmpty()) {
                    setupResponsavelSpinner(state.responsaveis)
                }
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: InventarioUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading && state.currentPage == 0
        isLoadingMore = state.isLoadingMore
        
        binding.loadingMoreIndicator.visibility = if (state.isLoadingMore) View.VISIBLE else View.GONE
        
        val displayList = if (state.searchQuery.isNotEmpty() || state.sortOrder != SortOrder.NUMERO_ASC) {
            state.patrimoniosFiltered
        } else {
            state.patrimonios
        }
        
        if (displayList.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            adapter.submitList(displayList)
        } else if (!state.isLoading && !state.isLoadingMore) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        }

        val totalText = if (responsavelSelecionado == null) {
            "Selecione um responsável para visualizar os patrimônios"
        } else if (state.searchQuery.isNotEmpty()) {
            "Encontrados: ${displayList.size} de ${state.patrimonios.size} patrimônios"
        } else if (state.hasMorePages) {
            "Total: ${displayList.size}+ patrimônios"
        } else if (state.totalPatrimonios > 0) {
            "Total: ${state.totalPatrimonios} patrimônios"
        } else {
            "Total: ${displayList.size} patrimônios"
        }
        binding.tvTotalCount.text = totalText

        state.errorMessage?.let { message ->
            if (!message.contains("Selecione um responsável")) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
            viewModel.clearErrorMessage()
        }
    }
    
    private fun showPatrimonioDetailsDialog(patrimonio: Patrimonio) {
        val details = buildString {
            append("Número: ${patrimonio.numeroPatrimonio}\n\n")
            append("Descrição: ${patrimonio.descricao}\n\n")
            
            patrimonio.marca?.let { append("Marca: $it\n") }
            patrimonio.modelo?.let { append("Modelo: $it\n") }
            patrimonio.numeroSerie?.let { append("Nº Série: $it\n") }
            patrimonio.estado?.let { append("Estado: $it\n") }
            patrimonio.valor?.let { append("Valor: R$ %.2f\n".format(it)) }
            
            append("\n")
            append("Setor: ${patrimonio.setorNome ?: "N/A"}\n")
            append("Sala: ${patrimonio.salaNome ?: "N/A"}\n")
            append("Responsável: ${patrimonio.responsavelNome ?: "N/A"}\n")
            
            append("\n")
            append("Status: ${if (patrimonio.coletado) "Coletado" else "Pendente"}\n")
            patrimonio.dataColeta?.let { append("Data Coleta: $it\n") }
            patrimonio.observacoesColeta?.let { append("Obs. Coleta: $it\n") }
            patrimonio.observacoes?.let { append("\nObservações: $it") }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Detalhes do Patrimônio")
            .setMessage(details)
            .setPositiveButton("Fechar", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): InventarioPorResponsavelFragment {
            return InventarioPorResponsavelFragment()
        }
    }
}
