package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.FragmentStatisticsRankingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment da aba "Rankings" — exibe Top Coletores e Setores Pendentes.
 *
 * Cada lista observa um `StateFlow<RankingState<*>>` separado no ViewModel,
 * por isso uma falha em uma das chamadas não bloqueia a outra.
 *
 * Pull-to-refresh dispara `viewModel.carregar()`. O carregamento inicial
 * acontece no `onViewCreated`.
 */
@AndroidEntryPoint
class RankingsFragment : Fragment() {

    private var _binding: FragmentStatisticsRankingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RankingsViewModel by viewModels()

    private val coletoresAdapter = TopColetorAdapter()
    private val setoresAdapter = SetorPendenteAdapter()

    companion object {
        private const val ARG_INVENTARIO_ID = "inventario_id"

        /** Mantida por compatibilidade com `StatisticsPagerAdapter`. */
        fun newInstance(idInventario: Int) = RankingsFragment().apply {
            arguments = Bundle().apply { putInt(ARG_INVENTARIO_ID, idInventario) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsRankingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSwipeRefresh()
        observeStates()
        viewModel.carregar()
    }

    private fun setupRecyclerViews() {
        binding.rvColetores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = coletoresAdapter
        }
        binding.rvSetores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = setoresAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorSecondary,
            R.color.primary_dark
        )
        binding.swipeRefresh.setOnRefreshListener { viewModel.carregar() }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.coletoresState.collect(::renderColetores)
                }
                launch {
                    viewModel.setoresState.collect(::renderSetores)
                }
            }
        }
    }

    // ─── Render helpers ───────────────────────────────────────────────────

    private fun renderColetores(state: RankingState<*>) {
        binding.swipeRefresh.isRefreshing = false
        when (state) {
            is RankingState.Loading -> {
                binding.progressColetores.visibility = View.VISIBLE
                binding.tvColetoresEmpty.visibility = View.GONE
                binding.rvColetores.visibility = View.GONE
            }
            is RankingState.Empty -> {
                binding.progressColetores.visibility = View.GONE
                binding.tvColetoresEmpty.visibility = View.VISIBLE
                binding.tvColetoresEmpty.text = "Nenhuma coleta registrada ainda."
                binding.rvColetores.visibility = View.GONE
            }
            is RankingState.Error -> {
                binding.progressColetores.visibility = View.GONE
                binding.tvColetoresEmpty.visibility = View.VISIBLE
                binding.tvColetoresEmpty.text = "Erro: ${state.mensagem}"
                binding.rvColetores.visibility = View.GONE
            }
            is RankingState.Success<*> -> {
                binding.progressColetores.visibility = View.GONE
                binding.tvColetoresEmpty.visibility = View.GONE
                binding.rvColetores.visibility = View.VISIBLE
                @Suppress("UNCHECKED_CAST")
                coletoresAdapter.submitList(state.itens as List<com.inventario.mobile.domain.model.TopColetor>)
            }
        }
    }

    private fun renderSetores(state: RankingState<*>) {
        binding.swipeRefresh.isRefreshing = false
        when (state) {
            is RankingState.Loading -> {
                binding.progressSetores.visibility = View.VISIBLE
                binding.tvSetoresEmpty.visibility = View.GONE
                binding.rvSetores.visibility = View.GONE
            }
            is RankingState.Empty -> {
                binding.progressSetores.visibility = View.GONE
                binding.tvSetoresEmpty.visibility = View.VISIBLE
                binding.tvSetoresEmpty.text = "Nenhum setor com pendentes."
                binding.rvSetores.visibility = View.GONE
            }
            is RankingState.Error -> {
                binding.progressSetores.visibility = View.GONE
                binding.tvSetoresEmpty.visibility = View.VISIBLE
                binding.tvSetoresEmpty.text = "Erro: ${state.mensagem}"
                binding.rvSetores.visibility = View.GONE
            }
            is RankingState.Success<*> -> {
                binding.progressSetores.visibility = View.GONE
                binding.tvSetoresEmpty.visibility = View.GONE
                binding.rvSetores.visibility = View.VISIBLE
                @Suppress("UNCHECKED_CAST")
                setoresAdapter.submitList(state.itens as List<com.inventario.mobile.domain.model.SetorPendente>)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
