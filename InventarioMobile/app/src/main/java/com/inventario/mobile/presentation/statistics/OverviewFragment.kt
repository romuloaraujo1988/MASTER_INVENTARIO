package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inventario.mobile.databinding.FragmentStatisticsOverviewBinding
import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.model.ProjecaoTermino
import com.inventario.mobile.domain.repository.RankingsRepository
import com.inventario.mobile.presentation.dashboard.DashboardViewModelClean
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment para exibir visão geral das estatísticas
 * KPIs detalhados, percentuais e valores totais
 *
 * Task 8.1 (dashboard-refactor-clean):
 * - Migrado de `DashboardViewModel` + `DashboardViewModelFactory` (legado) para
 *   `DashboardViewModelClean` via Hilt (Req 1.3, 1.6, 5.4).
 * - Consome a FONTE ÚNICA `fonteEstatisticas: StateFlow<DashboardStats>`
 *   exposta pelo ViewModel (mesma fonte usada pelo `DashboardFragment`),
 *   substituindo a coleta anterior de `uiState` (Req 1.4, 7.6).
 * - Pull-to-refresh agora dispara `viewModel.refresh()`; não há mais
 *   `loadDashboardData()` manual no `onViewCreated` — o `refreshTrigger`
 *   inicial do ViewModel já garante o primeiro fetch.
 */
@AndroidEntryPoint
class OverviewFragment : Fragment() {

    private var _binding: FragmentStatisticsOverviewBinding? = null
    private val binding get() = _binding!!

    // ViewModel Clean injetado via Hilt (Req 1.3, 1.6, 5.4).
    private val viewModel: DashboardViewModelClean by viewModels()

    // Dependências adicionais para renderizar o card de Projeção de Término.
    @Inject lateinit var rankingsRepository: RankingsRepository
    @Inject lateinit var preferencesManager: PreferencesManager

    private var idInventario: Int = 0

    companion object {
        private const val TAG = "OverviewFragment"
        private const val ARG_INVENTARIO_ID = "inventario_id"

        /**
         * Mantido para compatibilidade com chamadores existentes.
         *
         * Observação: após a migração para `DashboardViewModelClean`, o id do
         * inventário é obtido do `PreferencesManager.getInventarioAtivoId()`
         * dentro do próprio ViewModel (via `inventarioIdFlow`). O argumento
         * `idInventario` passado aqui não é usado ativamente para alimentar a
         * `fonteEstatisticas`; permanece apenas para preservar o contrato
         * público do Fragment.
         */
        fun newInstance(idInventario: Int) = OverviewFragment().apply {
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
        _binding = FragmentStatisticsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeFonteEstatisticas()
        carregarProjecaoTermino()
    }

    private fun setupUI() {
        // Pull-to-refresh: delega ao ViewModel Clean (Req 2.5).
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            carregarProjecaoTermino()
        }
    }

    /**
     * Coleta a FONTE ÚNICA de estatísticas do `DashboardViewModelClean`.
     *
     * O `StateFlow` é construído via `stateIn(WhileSubscribed(5000))`, então
     * o primeiro subscriber dispara o fetch (via `refreshTrigger` inicial)
     * sem precisar chamar `loadDashboardData()` manualmente.
     */
    private fun observeFonteEstatisticas() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fonteEstatisticas.collect(::updateUI)
            }
        }
    }

    private fun updateUI(stats: DashboardStats) {
        binding.swipeRefresh.isRefreshing = false

        // KPIs principais
        binding.tvTotalPatrimonios.text = formatNumber(stats.totalPatrimonios)
        binding.tvColetados.text = formatNumber(stats.totalColetados)
        binding.tvPendentes.text = formatNumber(stats.totalPendentes)
        binding.tvDivergencias.text = stats.divergencias.toString()
        binding.tvColetores.text = stats.coletoresAtivos.toString()

        // Percentuais
        binding.tvPercentualConclusao.text = String.format("%.1f%%", stats.percentualConclusao)
        binding.progressConclusao.progress = stats.percentualConclusao.toInt()

        val percentualPendente = 100.0 - stats.percentualConclusao
        binding.tvPercentualPendente.text = String.format("%.1f%%", percentualPendente)

        // Valores
        binding.tvValorTotal.text = formatCurrency(stats.valorTotal)

        val valorMedio = if (stats.totalPatrimonios > 0) {
            stats.valorTotal / stats.totalPatrimonios
        } else 0.0
        binding.tvValorMedio.text = formatCurrency(valorMedio)

        Log.d(TAG, "UI atualizada com estatísticas (fonte única)")
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number).replace(",", ".")
    }

    private fun formatCurrency(value: Double): String {
        return String.format("R$ %,.2f", value).replace(",", "X").replace(".", ",").replace("X", ".")
    }

    // ─── Projeção de Término ──────────────────────────────────────────────

    /**
     * Busca a projeção do servidor de forma totalmente independente da fonte
     * principal de estatísticas, em uma corrotina separada. Em caso de erro,
     * apenas mostra a mensagem padrão no card — sem afetar o resto do Overview.
     */
    private fun carregarProjecaoTermino() {
        val invId = preferencesManager.getInventarioAtivoId()
        viewLifecycleOwner.lifecycleScope.launch {
            rankingsRepository.calcularProjecaoTermino(invId).fold(
                onSuccess = { renderProjecao(it) },
                onFailure = {
                    Log.w(TAG, "Falha ao buscar projeção de término", it)
                    renderProjecaoFallback("Não foi possível calcular a projeção agora.")
                }
            )
        }
    }

    /**
     * Renderiza o card de Projeção de Término. Lida com 3 cenários:
     *  - Concluído (100%): mostra "Concluído!" como mensagem.
     *  - Dados insuficientes (sem dataInicio ou nenhuma coleta): mostra
     *    apenas a mensagem orientativa, sem dias/data.
     *  - Projeção válida: mostra data prevista, dias restantes, ritmo e
     *    badge "No prazo / Atrasado" quando o inventário tem dataFim.
     */
    private fun renderProjecao(p: ProjecaoTermino) {
        val ctx = context ?: return

        if (p.concluido) {
            binding.tvProjecaoMensagem.text = "🎉 Inventário concluído!"
            binding.layoutProjecaoDados.visibility = View.GONE
            binding.tvProjecaoRitmo.visibility = View.GONE
            binding.tvProjecaoStatus.visibility = View.GONE
            return
        }

        if (p.dadosInsuficientes) {
            renderProjecaoFallback(
                "Realize mais coletas (e configure a data de início do inventário) " +
                    "para gerar uma projeção."
            )
            return
        }

        val dias = p.diasRestantes ?: 0L
        val data = p.dataPrevistaFormatada ?: "—"

        binding.tvProjecaoMensagem.text =
            "Ao ritmo atual de coletas, o inventário será concluído em:"

        binding.layoutProjecaoDados.visibility = View.VISIBLE
        binding.tvProjecaoData.text = data
        binding.tvProjecaoDiasRestantes.text =
            if (dias == 1L) "1 dia" else "$dias dias"

        binding.tvProjecaoRitmo.visibility = View.VISIBLE
        binding.tvProjecaoRitmo.text =
            "Ritmo atual: ${String.format("%.1f", p.ritmoColetasPorDia).replace(".", ",")} coletas/dia"

        // Badge No prazo / Atrasado (só quando o inventário tem dataFim)
        when (p.dentroDoPrazo) {
            true -> {
                binding.tvProjecaoStatus.visibility = View.VISIBLE
                binding.tvProjecaoStatus.text = "✓ No prazo"
                binding.tvProjecaoStatus.setTextColor(
                    ctx.getColor(com.inventario.mobile.R.color.success)
                )
            }
            false -> {
                binding.tvProjecaoStatus.visibility = View.VISIBLE
                val atraso = p.diasDeFolgaOuAtraso?.let { Math.abs(it) }
                binding.tvProjecaoStatus.text =
                    if (atraso != null) "⚠ Atrasado ${atraso}d" else "⚠ Atrasado"
                binding.tvProjecaoStatus.setTextColor(
                    ctx.getColor(com.inventario.mobile.R.color.error)
                )
            }
            null -> binding.tvProjecaoStatus.visibility = View.GONE
        }
    }

    private fun renderProjecaoFallback(mensagem: String) {
        binding.tvProjecaoMensagem.text = mensagem
        binding.layoutProjecaoDados.visibility = View.GONE
        binding.tvProjecaoRitmo.visibility = View.GONE
        binding.tvProjecaoStatus.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
