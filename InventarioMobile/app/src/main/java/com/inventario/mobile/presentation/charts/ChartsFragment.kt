package com.inventario.mobile.presentation.charts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.databinding.FragmentChartsBinding
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment para exibição de gráficos estatísticos
 */
@AndroidEntryPoint
class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChartsViewModel by viewModels()
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private var idInventario: Int = 0

    companion object {
        private const val TAG = "ChartsFragment"
        private const val ARG_INVENTARIO_ID = "inventario_id"

        fun newInstance(idInventario: Int) = ChartsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INVENTARIO_ID, idInventario)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idInventario = it.getInt(ARG_INVENTARIO_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Se idInventario for 0, usar o inventário ativo
        if (idInventario == 0) {
            idInventario = preferencesManager.getInventarioAtivoId() ?: 0
            Log.d(TAG, "Usando inventário ativo: $idInventario")
        }
        
        setupSwipeRefresh()
        setupCharts()
        observeData()
        loadData()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Pull-to-refresh acionado")
            loadData()
        }
    }

    private fun setupCharts() {
        // Configurar gráfico de progresso
        ChartHelper.setupBarChart(binding.chartProgresso, listOf("Coletados", "Pendentes"))
        
        // Configurar gráfico de status
        ChartHelper.setupPieChart(binding.chartStatus)
        
        // Configurar gráfico de evolução
        ChartHelper.setupLineChart(binding.chartEvolucao, emptyList())
        
        // Configurar gráfico de top itens
        ChartHelper.setupBarChart(binding.chartTopItens, emptyList())
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.progressData.collect { data ->
                data?.let { updateProgressChart(it) }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statusData.collect { data ->
                data?.let { updateStatusChart(it) }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.evolutionData.collect { data ->
                if (data.isNotEmpty()) {
                    updateEvolutionChart(data)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topItemsData.collect { data ->
                if (data.isNotEmpty()) {
                    updateTopItemsChart(data)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                // Parar o swipe refresh quando terminar de carregar
                if (!isLoading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun loadData() {
        Log.d(TAG, "Carregando dados para inventário: $idInventario")
        viewModel.loadChartData(idInventario)
    }

    private fun updateProgressChart(data: ProgressData) {
        val barData = ChartHelper.createProgressBarData(data.coletados, data.pendentes)
        binding.chartProgresso.data = barData
        binding.chartProgresso.invalidate()
        
        // Atualizar estatísticas
        binding.apply {
            tvTotalPatrimonios.text = data.total.toString()
            tvColetados.text = data.coletados.toString()
            tvPendentes.text = data.pendentes.toString()
            tvPercentual.text = ChartHelper.formatPercent(data.percentual)
        }
    }

    private fun updateStatusChart(data: StatusData) {
        val pieData = ChartHelper.createStatusPieData(
            data.bom,
            data.regular,
            data.ruim,
            data.pessimo,
            data.semInfo
        )
        binding.chartStatus.data = pieData
        binding.chartStatus.centerText = "Estado de\nConservação"
        binding.chartStatus.invalidate()
    }

    private fun updateEvolutionChart(data: Map<String, Int>) {
        val (lineData, labels) = ChartHelper.createEvolutionLineData(data)
        
        ChartHelper.setupLineChart(binding.chartEvolucao, labels)
        binding.chartEvolucao.data = lineData
        binding.chartEvolucao.invalidate()
    }

    private fun updateTopItemsChart(data: Map<String, Int>) {
        val (barData, labels) = ChartHelper.createTopItemsBarData(data)
        
        ChartHelper.setupBarChart(binding.chartTopItens, labels)
        binding.chartTopItens.data = barData
        binding.chartTopItens.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
