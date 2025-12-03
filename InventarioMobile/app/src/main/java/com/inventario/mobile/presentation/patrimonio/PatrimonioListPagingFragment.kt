package com.inventario.mobile.presentation.patrimonio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.Patrimonio
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment de exemplo que demonstra o uso de Paging 3 para listar patrimônios
 * 
 * Características:
 * - Scroll infinito automático
 * - Indicador de loading no rodapé
 * - Retry automático em caso de erro
 * - Filtros por status de coleta
 * - Estatísticas vindas do servidor (não da contagem local)
 */
@AndroidEntryPoint
class PatrimonioListPagingFragment : Fragment() {
    
    private val viewModel: PatrimonioListViewModelPaging by viewModels()
    
    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotal: TextView
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipTodos: Chip
    private lateinit var chipColetados: Chip
    private lateinit var chipPendentes: Chip
    
    // Adapter com Paging
    private lateinit var pagingAdapter: PatrimonioPagingAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patrimonio_list_paging, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupFilters()
        observeData()
        
        // Carregar estatísticas do servidor
        viewModel.carregarEstatisticas()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvTotal = view.findViewById(R.id.tvTotal)
        chipGroup = view.findViewById(R.id.chipGroupFiltros)
        chipTodos = view.findViewById(R.id.chipTodos)
        chipColetados = view.findViewById(R.id.chipColetados)
        chipPendentes = view.findViewById(R.id.chipPendentes)
    }
    
    private fun setupRecyclerView() {
        pagingAdapter = PatrimonioPagingAdapter { patrimonio ->
            onPatrimonioClick(patrimonio)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pagingAdapter.withLoadStateFooter(
                footer = PatrimonioLoadStateAdapter { pagingAdapter.retry() }
            )
        }
        
        // Observar estados de carregamento
        pagingAdapter.addLoadStateListener { loadState ->
            // Loading inicial
            progressBar.isVisible = loadState.refresh is LoadState.Loading
            
            // Lista vazia
            val isEmpty = loadState.refresh is LoadState.NotLoading && 
                          pagingAdapter.itemCount == 0
            tvEmpty.isVisible = isEmpty
            
            // Erro
            val errorState = loadState.refresh as? LoadState.Error
            errorState?.let {
                showError(it.error.message ?: "Erro ao carregar")
            }
        }
    }
    
    private fun setupFilters() {
        chipTodos.setOnClickListener {
            viewModel.filtrarPorColetado(null)
        }
        
        chipColetados.setOnClickListener {
            viewModel.filtrarPorColetado(true)
        }
        
        chipPendentes.setOnClickListener {
            viewModel.filtrarPorColetado(false)
        }
    }
    
    private fun observeData() {
        // Observar patrimônios paginados
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.patrimoniosPaging.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
        
        // Observar estatísticas (totais do servidor)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.estatisticas.collect { state ->
                when (state) {
                    is EstatisticasState.Loading -> {
                        tvTotal.text = "Carregando..."
                    }
                    is EstatisticasState.Success -> {
                        tvTotal.text = "Total: ${state.totalPatrimonios} | " +
                                "Coletados: ${state.totalColetados} | " +
                                "Pendentes: ${state.totalPendentes} | " +
                                "${String.format("%.1f", state.percentualConclusao)}%"
                    }
                    is EstatisticasState.Error -> {
                        tvTotal.text = "Erro ao carregar estatísticas"
                    }
                }
            }
        }
    }
    
    private fun onPatrimonioClick(patrimonio: Patrimonio) {
        // Navegar para detalhes ou abrir dialog
        android.util.Log.d(TAG, "Patrimônio clicado: ${patrimonio.numeroPatrimonio}")
        
        // TODO: Implementar navegação para detalhes
        // findNavController().navigate(
        //     PatrimonioListPagingFragmentDirections.actionToDetalhe(patrimonio.id)
        // )
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Força refresh dos dados
     */
    fun refresh() {
        viewModel.refresh()
        viewModel.carregarEstatisticas()
    }
    
    companion object {
        private const val TAG = "PatrimonioListPaging"
        
        fun newInstance() = PatrimonioListPagingFragment()
    }
}
