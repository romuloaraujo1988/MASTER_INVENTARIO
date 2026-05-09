package com.inventario.mobile.presentation.coletas

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.databinding.ActivityColetasUnificadaBinding
import com.inventario.mobile.domain.model.StatusFiltro
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela unificada de coletas — substitui [ColetasActivityClean] e
 * [com.inventario.mobile.presentation.coleta.CollectionViewActivity].
 *
 * Concentra todas as funcionalidades de ambas as telas:
 * - Campo de busca por texto (número de patrimônio / descrição)
 * - Chips de filtro por status: Todos, Coletados, Pendentes, Sem Etiqueta
 * - Chip "Minhas Coletas"
 * - Spinner de filtro por sala
 * - Toggle de visualização agrupada por sala
 * - Pull-to-refresh
 * - Contadores de total e pendentes
 * - Long-click em coleta pendente → menu de contexto (Reenviar / Excluir)
 * - Long-click em coleta sincronizada → Toast informativo
 *
 * Requisitos: 2.1–2.12, 3.4, 4.6–4.8
 */
@AndroidEntryPoint
class ColetasUnificadaActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ColetasUnificadaActivity"
    }

    private lateinit var binding: ActivityColetasUnificadaBinding

    /** ViewModel unificado injetado via Hilt. */
    private val viewModel: ColetasUnificadaViewModel by viewModels()

    /**
     * Adapter reutilizado de [ColetasAdapter], com adição do callback [onItemLongClick].
     * Requisito 2.7, 2.10
     */
    private lateinit var adapter: ColetasAdapter

    // ─── Controle de estado do spinner ───────────────────────────────────────

    private var salasAtuais: List<String> = emptyList()
    private var salaSelecionadaAtual: String? = null

    // ─── Ciclo de vida ───────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetasUnificadaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupBusca()
        setupChipsStatus()
        setupChipMinhasColetas()
        setupSpinnerSala()
        setupToggleAgrupado()
        setupSwipeRefresh()
        setupObservers()

        // Carregamento inicial
        viewModel.carregarColetas()
    }

    // ─── Setup de componentes ────────────────────────────────────────────────

    /**
     * Configura a toolbar com título "Coletas" e botão de voltar.
     * Requisito 3.4
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Coletas"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    /**
     * Configura o RecyclerView com o [ColetasAdapter].
     *
     * O adapter recebe dois callbacks:
     * - [onColetaClick]: clique simples (exibe informações básicas)
     * - [onItemLongClick]: long-click → menu de contexto ou Toast
     *
     * Requisitos: 2.7, 2.10
     */
    private fun setupRecyclerView() {
        adapter = ColetasAdapter(
            onColetaClick = { coleta -> onColetaClick(coleta) },
            onItemLongClick = { coleta -> onColetaLongClick(coleta) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ColetasUnificadaActivity)
            adapter = this@ColetasUnificadaActivity.adapter
        }
    }

    /**
     * Configura o campo de busca por texto com listener em tempo real.
     * Requisito 2.2
     */
    private fun setupBusca() {
        binding.edtBusca.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.filtrarPorTexto(s?.toString() ?: "")
            }
        })

        binding.edtBusca.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                viewModel.filtrarPorTexto(binding.edtBusca.text?.toString() ?: "")
                true
            } else {
                false
            }
        }
    }

    /**
     * Configura os chips de filtro por status: Todos, Coletados, Pendentes, Sem Etiqueta.
     * Requisito 2.3
     */
    private fun setupChipsStatus() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when {
                checkedIds.contains(R.id.chipColetados) -> StatusFiltro.COLETADOS
                checkedIds.contains(R.id.chipPendentes) -> StatusFiltro.PENDENTES
                checkedIds.contains(R.id.chipSemEtiqueta) -> StatusFiltro.SEM_ETIQUETA
                else -> StatusFiltro.TODOS
            }
            viewModel.filtrarPorStatus(status)
        }
    }

    /**
     * Configura o chip "Minhas Coletas".
     * Requisito 2.4
     */
    private fun setupChipMinhasColetas() {
        binding.chipMinhasColetas.setOnCheckedChangeListener { _, isChecked ->
            viewModel.filtrarPorUsuario(isChecked)
        }
    }

    /**
     * Configura o spinner de filtro por sala.
     * Requisito 2.5
     */
    private fun setupSpinnerSala() {
        binding.spinnerSala.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sala = if (position == 0) null else binding.spinnerSala.selectedItem as? String
                if (sala != salaSelecionadaAtual) {
                    salaSelecionadaAtual = sala
                    viewModel.filtrarPorSala(sala)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (salaSelecionadaAtual != null) {
                    salaSelecionadaAtual = null
                    viewModel.filtrarPorSala(null)
                }
            }
        }
    }

    /**
     * Configura o toggle de visualização agrupada por sala.
     * Requisito 2.6 / 4.8
     */
    private fun setupToggleAgrupado() {
        binding.switchAgrupado.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleVisualizacaoAgrupada()
        }
    }

    /**
     * Configura o pull-to-refresh para recarregar os dados.
     * Requisito 2.12
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorSecondary,
                R.color.primary_dark
            )
            setOnRefreshListener {
                viewModel.carregarColetas()
            }
        }
    }

    // ─── Observadores ────────────────────────────────────────────────────────

    /**
     * Observa o [ColetasUnificadaState] emitido pelo ViewModel e atualiza a UI.
     * Usa [lifecycleScope.launch] conforme especificado nos requisitos.
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                atualizarUI(state)
            }
        }
    }

    // ─── Atualização da UI ───────────────────────────────────────────────────

    private fun atualizarUI(state: ColetasUnificadaState) {
        when (state) {
            is ColetasUnificadaState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }

            is ColetasUnificadaState.Loading -> {
                if (!binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }

            is ColetasUnificadaState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                // Contadores (Requisito 2.11)
                binding.tvTotalColetas.text = state.totalColetas.toString()
                binding.tvTotalPendentes.text = state.totalPendentes.toString()

                // Spinner de salas (Requisito 2.5)
                atualizarSpinnerSalas(state.salasDisponiveis, state.filtroSala)

                // Lista de coletas
                if (state.coletas.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE

                    // Visualização agrupada ou plana (Requisito 2.6)
                    if (state.visualizacaoAgrupada && state.coletasAgrupadas.isNotEmpty()) {
                        adapter.submitGroupedList(state.coletasAgrupadas)
                    } else {
                        adapter.submitColetaList(state.coletas)
                    }
                }

                // Sincronizar estado dos controles de filtro com o ViewModel
                sincronizarControlesFiltro(state)
            }

            is ColetasUnificadaState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }

            is ColetasUnificadaState.ColetaReenviada -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }

            is ColetasUnificadaState.ColetaExcluida -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Atualiza o spinner de salas apenas quando a lista de salas muda.
     * Evita loops de seleção ao usar [false] no setSelection.
     */
    private fun atualizarSpinnerSalas(salas: List<String>, salaSelecionada: String?) {
        if (salas != salasAtuais) {
            salasAtuais = salas
            val opcoes = listOf("Todas as salas") + salas
            val spinnerAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSala.adapter = spinnerAdapter
        }

        if (salaSelecionada != salaSelecionadaAtual) {
            salaSelecionadaAtual = salaSelecionada
            val posicao = if (salaSelecionada != null) {
                salasAtuais.indexOf(salaSelecionada) + 1
            } else {
                0
            }
            if (binding.spinnerSala.selectedItemPosition != posicao) {
                binding.spinnerSala.setSelection(posicao, false)
            }
        }
    }

    /**
     * Sincroniza o estado visual dos controles de filtro com o estado do ViewModel.
     * Evita disparar callbacks ao atualizar programaticamente.
     */
    private fun sincronizarControlesFiltro(state: ColetasUnificadaState.Success) {
        // Chip "Minhas Coletas"
        if (binding.chipMinhasColetas.isChecked != state.filtroUsuario) {
            binding.chipMinhasColetas.isChecked = state.filtroUsuario
        }

        // Chips de status
        val chipStatusId = when (state.filtroStatus) {
            StatusFiltro.COLETADOS -> R.id.chipColetados
            StatusFiltro.PENDENTES -> R.id.chipPendentes
            StatusFiltro.SEM_ETIQUETA -> R.id.chipSemEtiqueta
            else -> R.id.chipTodos
        }
        if (binding.chipGroupStatus.checkedChipId != chipStatusId) {
            binding.chipGroupStatus.check(chipStatusId)
        }

        // Switch de agrupamento
        if (binding.switchAgrupado.isChecked != state.visualizacaoAgrupada) {
            binding.switchAgrupado.isChecked = state.visualizacaoAgrupada
        }
    }

    // ─── Interações com itens da lista ───────────────────────────────────────

    /**
     * Clique simples em uma coleta — exibe informações básicas.
     */
    private fun onColetaClick(coleta: Coleta) {
        val info = buildString {
            append("Patrimônio: ${coleta.numeroPatrimonio ?: "Sem etiqueta"}")
            if (!coleta.descricaoPatrimonio.isNullOrBlank()) {
                append("\n${coleta.descricaoPatrimonio}")
            }
        }
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show()
    }

    /**
     * Long-click em uma coleta:
     * - Coleta pendente → menu de contexto com "Reenviar Coleta" e "Excluir Coleta"
     * - Coleta sincronizada → Toast informativo
     *
     * Requisitos: 2.7, 2.10
     */
    private fun onColetaLongClick(coleta: Coleta) {
        if (coleta.sincronizado) {
            // Requisito 2.10: coleta já sincronizada
            Toast.makeText(
                this,
                "Esta coleta já está sincronizada e não pode ser gerenciada.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Requisito 2.7: coleta pendente → menu de contexto
            mostrarMenuColetaPendente(coleta)
        }
    }

    /**
     * Exibe o menu de contexto para coletas pendentes com as opções
     * "Reenviar Coleta" e "Excluir Coleta".
     * Requisito 2.7
     */
    private fun mostrarMenuColetaPendente(coleta: Coleta) {
        val patrimonioInfo = coleta.numeroPatrimonio ?: "Patrimônio ${coleta.patrimonioId}"
        val opcoes = arrayOf("🔄 Reenviar Coleta", "🗑️ Excluir Coleta")

        AlertDialog.Builder(this)
            .setTitle("Coleta Pendente\n$patrimonioInfo")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> confirmarReenvio(coleta)
                    1 -> confirmarExclusao(coleta)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Exibe diálogo de confirmação antes de reenviar a coleta.
     * Requisito 2.8
     */
    private fun confirmarReenvio(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("Reenviar Coleta")
            .setMessage(
                "Deseja tentar sincronizar a coleta do patrimônio " +
                        "${coleta.numeroPatrimonio ?: coleta.patrimonioId} novamente?"
            )
            .setPositiveButton("Reenviar") { _, _ ->
                val coletaId = coleta.id?.toLong() ?: 0L
                if (coletaId > 0) {
                    viewModel.reenviarColeta(coletaId)
                } else {
                    Toast.makeText(this, "Erro: ID da coleta inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Exibe diálogo de confirmação antes de excluir a coleta.
     * Requisito 2.9
     */
    private fun confirmarExclusao(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Excluir Coleta")
            .setMessage(
                "Tem certeza que deseja EXCLUIR a coleta do patrimônio " +
                        "${coleta.numeroPatrimonio ?: coleta.patrimonioId}?\n\n" +
                        "Esta ação não pode ser desfeita!"
            )
            .setPositiveButton("Excluir") { _, _ ->
                val coletaId = coleta.id?.toLong() ?: 0L
                if (coletaId > 0) {
                    viewModel.excluirColeta(coletaId)
                } else {
                    Toast.makeText(this, "Erro: ID da coleta inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ─── Navegação ───────────────────────────────────────────────────────────

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
