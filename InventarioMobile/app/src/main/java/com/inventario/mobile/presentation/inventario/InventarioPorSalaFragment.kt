package com.inventario.mobile.presentation.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.FragmentInventarioPorSalaBinding
import com.inventario.mobile.domain.model.EstatisticasSala
import com.inventario.mobile.domain.model.FiltroColetaSala
import com.inventario.mobile.domain.model.SalaComProgresso
import com.inventario.mobile.presentation.state.InventarioPorSalaState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para exibir inventário filtrado por sala.
 * Mostra estatísticas de coleta e lista de patrimônios da sala selecionada.
 */
@AndroidEntryPoint
class InventarioPorSalaFragment : Fragment() {
    
    private var _binding: FragmentInventarioPorSalaBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InventarioPorSalaViewModel by viewModels()
    
    private lateinit var patrimonioAdapter: PatrimonioAdapter
    private var salaAdapter: ArrayAdapter<String>? = null
    private var salasDisponiveis: List<SalaComProgresso> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioPorSalaBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupChips()
        setupSwipeRefresh()
        setupObservers()
        
        // Carregar salas ao iniciar
        viewModel.carregarSalas()
    }
    
    private fun setupRecyclerView() {
        patrimonioAdapter = PatrimonioAdapter { patrimonio ->
            // Click no patrimônio - pode abrir detalhes
            Toast.makeText(
                requireContext(),
                "Patrimônio: ${patrimonio.numeroPatrimonio}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.recyclerViewPatrimonios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = patrimonioAdapter
        }
        
        // Adicionar scroll listener no NestedScrollView (pai do RecyclerView)
        // O RecyclerView está com nestedScrollingEnabled=false, então o scroll é do NestedScrollView
        setupInfiniteScroll()
    }
    
    /**
     * Configura rolagem infinita no NestedScrollView.
     * Detecta quando o usuário chega ao final da lista e carrega mais itens.
     */
    private fun setupInfiniteScroll() {
        // O NestedScrollView é o primeiro filho do SwipeRefreshLayout
        val nestedScrollView = binding.swipeRefresh.getChildAt(0) as? androidx.core.widget.NestedScrollView
            ?: return
        
        nestedScrollView.setOnScrollChangeListener(
            androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
                // Só carregar mais se estiver rolando para baixo
                if (scrollY > oldScrollY) {
                    val childHeight = v.getChildAt(0)?.height ?: 0
                    val scrollViewHeight = v.height
                    
                    // Verificar se chegou perto do final (300px de margem para carregar antes)
                    val threshold = 300
                    if (scrollY + scrollViewHeight >= childHeight - threshold) {
                        viewModel.carregarMaisPatrimonios()
                    }
                }
            }
        )
    }
    
    private fun setupChips() {
        binding.chipTodos.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColetaSala.TODOS)
        }
        
        binding.chipColetados.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColetaSala.COLETADOS)
        }
        
        binding.chipNaoColetados.setOnClickListener {
            viewModel.aplicarFiltro(FiltroColetaSala.NAO_COLETADOS)
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.recarregar()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                handleState(state)
            }
        }
    }
    
    private fun handleState(state: InventarioPorSalaState) {
        binding.swipeRefresh.isRefreshing = false
        
        when (state) {
            is InventarioPorSalaState.Idle -> {
                showEmptyState("Selecione uma sala para visualizar os patrimônios")
            }
            
            is InventarioPorSalaState.Loading -> {
                showLoading()
            }
            
            is InventarioPorSalaState.SalasCarregadas -> {
                hideLoading()
                setupSalaDropdown(state.salasFiltradas)
                showEmptyState("Selecione uma sala para visualizar os patrimônios")
            }
            
            is InventarioPorSalaState.SalaSelecionada -> {
                hideLoading()
                showSalaDetails(state)
            }
            
            is InventarioPorSalaState.Error -> {
                hideLoading()
                showError(state.message)
            }
        }
    }
    
    private fun setupSalaDropdown(salas: List<SalaComProgresso>) {
        salasDisponiveis = salas
        
        // Mostrar apenas o nome da sala (estatísticas aparecem no card)
        val items = salas.map { sala -> sala.nome }
        
        salaAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            items
        )
        
        binding.actvSala.setAdapter(salaAdapter)
        binding.actvSala.setOnItemClickListener { _, _, position, _ ->
            if (position < salasDisponiveis.size) {
                viewModel.selecionarSala(salasDisponiveis[position])
            }
        }
    }
    
    private fun showSalaDetails(state: InventarioPorSalaState.SalaSelecionada) {
        // Mostrar card de estatísticas
        binding.cardEstatisticas.visibility = View.VISIBLE
        binding.chipGroupFiltro.visibility = View.VISIBLE
        binding.recyclerViewPatrimonios.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        
        // Atualizar estatísticas
        updateEstatisticas(state.estatisticas)
        
        // Atualizar chip selecionado
        updateChipSelection(state.filtroAtual)
        
        // Atualizar lista de patrimônios
        patrimonioAdapter.submitList(state.patrimonios)
        
        // Mostrar loading more se necessário
        binding.loadingMoreIndicator.visibility = 
            if (state.isLoadingMore) View.VISIBLE else View.GONE
    }
    
    private fun updateEstatisticas(estatisticas: EstatisticasSala) {
        binding.tvSalaNome.text = estatisticas.salaNome
        binding.tvTotal.text = estatisticas.totalPatrimonios.toString()
        binding.tvColetados.text = estatisticas.coletados.toString()
        binding.tvPendentes.text = estatisticas.pendentes.toString()
        binding.tvPercentual.text = estatisticas.percentualTexto
        
        // Atualizar progress bar
        binding.progressBar.progress = estatisticas.percentualColeta.toInt()
        
        // Aplicar cor baseada no percentual
        val progressColor = getProgressColor(estatisticas.percentualColeta)
        binding.progressBar.progressTintList = 
            ContextCompat.getColorStateList(requireContext(), progressColor)
        binding.tvPercentual.setTextColor(
            ContextCompat.getColor(requireContext(), progressColor)
        )
        
        // Mostrar ícone de conclusão se 100%
        binding.ivConcluido.visibility = 
            if (estatisticas.isCompleta) View.VISIBLE else View.GONE
    }
    
    private fun getProgressColor(percentual: Float): Int {
        return when {
            percentual <= 25f -> R.color.error
            percentual <= 50f -> R.color.warning
            percentual <= 75f -> R.color.warning_light
            else -> R.color.success
        }
    }
    
    private fun updateChipSelection(filtro: FiltroColetaSala) {
        when (filtro) {
            FiltroColetaSala.TODOS -> binding.chipTodos.isChecked = true
            FiltroColetaSala.COLETADOS -> binding.chipColetados.isChecked = true
            FiltroColetaSala.NAO_COLETADOS -> binding.chipNaoColetados.isChecked = true
        }
    }
    
    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }
    
    private fun showEmptyState(message: String) {
        binding.cardEstatisticas.visibility = View.GONE
        binding.chipGroupFiltro.visibility = View.GONE
        binding.recyclerViewPatrimonios.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showEmptyState("Erro ao carregar dados. Tente novamente.")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): InventarioPorSalaFragment {
            return InventarioPorSalaFragment()
        }
    }
}
