package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.databinding.FragmentStatisticsOverviewBinding
import com.inventario.mobile.presentation.dashboard.DashboardViewModel
import com.inventario.mobile.presentation.dashboard.DashboardViewModelFactory
import kotlinx.coroutines.launch

/**
 * Fragment para exibir visão geral das estatísticas
 * KPIs detalhados, percentuais e valores totais
 */
class OverviewFragment : Fragment() {

    private var _binding: FragmentStatisticsOverviewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(requireActivity().application)
    }

    companion object {
        private const val TAG = "OverviewFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        
        // Carregar dados
        viewModel.loadDashboardData()
    }

    private fun setupUI() {
        // Configurar pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: com.inventario.mobile.presentation.dashboard.DashboardUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading
        
        state.dashboardStats?.let { stats ->
            // KPIs principais
            binding.tvTotalPatrimonios.text = formatNumber(stats.totalPatrimonios)
            binding.tvColetados.text = formatNumber(stats.patrimoniosColetados)
            binding.tvPendentes.text = formatNumber(stats.patrimoniosPendentes)
            binding.tvDivergencias.text = stats.divergencias.toString()
            binding.tvColetores.text = stats.coletoresAtivos.toString()
            
            // Percentuais
            binding.tvPercentualConclusao.text = String.format("%.1f%%", stats.percentualConclusao)
            binding.progressConclusao.progress = stats.percentualConclusao.toInt()
            
            val percentualPendente = 100 - stats.percentualConclusao
            binding.tvPercentualPendente.text = String.format("%.1f%%", percentualPendente)
            
            // Valores
            binding.tvValorTotal.text = formatCurrency(stats.valorTotal)
            
            val valorMedio = if (stats.totalPatrimonios > 0) {
                stats.valorTotal / stats.totalPatrimonios
            } else 0.0
            binding.tvValorMedio.text = formatCurrency(valorMedio)
            
            Log.d(TAG, "UI atualizada com estatísticas")
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number).replace(",", ".")
    }

    private fun formatCurrency(value: Double): String {
        return String.format("R$ %,.2f", value).replace(",", "X").replace(".", ",").replace("X", ".")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
