package com.inventario.mobile.presentation.patrimonio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.FragmentPatrimonioListBinding
import com.inventario.mobile.presentation.adapter.PatrimonioAdapter
import com.inventario.mobile.presentation.util.InfiniteScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para listagem de patrimônios com scroll infinito
 * 
 * Exemplo de uso da paginação manual
 */
@AndroidEntryPoint
class PatrimonioListFragment : Fragment() {
    
    private var _binding: FragmentPatrimonioListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PatrimonioListViewModel by viewModels()
    
    private lateinit var adapter: PatrimonioAdapter
    private lateinit var scrollListener: InfiniteScrollListener
    
    // Argumentos opcionais
    private var salaId: Int? = null
    private var inventarioId: Int? = null
    private var filtroColetado: Boolean? = null
    
    companion object {
        private const val ARG_SALA_ID = "sala_id"
        private const val ARG_INVENTARIO_ID = "inventario_id"
        private const val ARG_FILTRO_COLETADO = "filtro_coletado"
        
        fun newInstance(
            salaId: Int? = null,
            inventarioId: Int? = null,
            filtroColetado: Boolean? = null
        ): PatrimonioListFragment {
            return PatrimonioListFragment().apply {
                arguments = Bundle().apply {
                    salaId?.let { putInt(ARG_SALA_ID, it) }
                    inventarioId?.let { putInt(ARG_INVENTARIO_ID, it) }
                    filtroColetado?.let { putBoolean(ARG_FILTRO_COLETADO, it) }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_SALA_ID)) salaId = it.getInt(ARG_SALA_ID)
            if (it.containsKey(ARG_INVENTARIO_ID)) inventarioId = it.getInt(ARG_INVENTARIO_ID)
            if (it.containsKey(ARG_FILTRO_COLETADO)) filtroColetado = it.getBoolean(ARG_FILTRO_COLETADO)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatrimonioListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSwipeRefresh()
        observeState()
        
        // Carregar dados iniciais
        viewModel.carregarPatrimonios(salaId, filtroColetado, inventarioId)
    }
    
    private fun setupRecyclerView() {
        adapter = PatrimonioAdapter { patrimonio ->
            // Click no item
            onPatrimonioClick(patrimonio)
        }
        
        val layoutManager = LinearLayoutManager(requireContext())
        
        binding.recyclerViewPatrimonios.apply {
            this.layoutManager = layoutManager
            this.adapter = this@PatrimonioListFragment.adapter
        }
        
        // Scroll infinito
        scrollListener = InfiniteScrollListener(layoutManager) {
            if (viewModel.canLoadMore()) {
                viewModel.carregarMais()
            }
        }
        binding.recyclerViewPatrimonios.addOnScrollListener(scrollListener)
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            scrollListener.reset()
            viewModel.refresh()
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PatrimonioListState.Idle -> {
                        hideLoading()
                    }
                    
                    is PatrimonioListState.Loading -> {
                        showLoading()
                        hideError()
                    }
                    
                    is PatrimonioListState.LoadingMore -> {
                        // Mostrar loading no rodapé
                        binding.progressBarMore.visibility = View.VISIBLE
                    }
                    
                    is PatrimonioListState.Success -> {
                        hideLoading()
                        hideError()
                        scrollListener.setLoaded()
                        
                        adapter.submitList(state.patrimonios)
                        
                        // Atualizar contador
                        val total = if (state.totalItems > 0) state.totalItems else state.patrimonios.size
                        binding.textViewTotal.text = "Total: $total patrimônios"
                        
                        // Mostrar mensagem se lista vazia
                        binding.textViewEmpty.visibility = 
                            if (state.patrimonios.isEmpty()) View.VISIBLE else View.GONE
                        
                        // Indicador de "mais itens"
                        binding.textViewHasMore.visibility = 
                            if (state.hasMore) View.VISIBLE else View.GONE
                    }
                    
                    is PatrimonioListState.Error -> {
                        hideLoading()
                        scrollListener.setLoaded()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.swipeRefresh.isRefreshing = true
        binding.progressBarMore.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.swipeRefresh.isRefreshing = false
        binding.progressBarMore.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        binding.textViewError.text = message
        binding.textViewError.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        binding.textViewError.visibility = View.GONE
    }
    
    private fun onPatrimonioClick(patrimonio: com.inventario.mobile.data.model.Patrimonio) {
        // Navegar para detalhes ou abrir dialog
        // TODO: Implementar navegação
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
