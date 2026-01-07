package com.inventario.mobile.presentation.descricao

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityDescricaoSelectionBinding
import com.inventario.mobile.domain.model.DescricaoItem
import com.inventario.mobile.domain.model.Patrimonio
import com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog
import com.inventario.mobile.presentation.state.DescricaoState
import com.inventario.mobile.utils.SoundUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para seleção de descrição (coleta sem etiqueta)
 * Clean Architecture + MVVM + Hilt
 * 
 * Fluxo:
 * 1. Usuário seleciona uma descrição
 * 2. Sistema busca patrimônios não coletados com essa descrição
 * 3. Se houver apenas 1: mostra dialog de estado e registra coleta
 * 4. Se houver mais de 1: mostra lista para escolher qual patrimônio
 */
@AndroidEntryPoint
class DescricaoSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDescricaoSelectionBinding
    
    // ViewModel injetado via Hilt
    private val viewModel: DescricaoSelectionViewModelClean by viewModels()
    
    private lateinit var adapter: DescricaoAdapter

    private var salaId: Long = 0
    private var salaNome: String = ""
    
    // Lista completa de descrições (para filtrar)
    private var descricoesCompletas: List<DescricaoItem> = emptyList()
    
    // Filtro de categoria atual
    private var filtroCategoria: String? = null

    companion object {
        private const val TAG = "DescricaoSelectionActivity"
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescricaoSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter dados da sala
        salaId = intent.getLongExtra(EXTRA_SALA_ID, -1L)
        salaNome = intent.getStringExtra(EXTRA_SALA_NOME) ?: ""

        Log.d(TAG, "Sala selecionada: ID=$salaId, Nome=$salaNome")
        
        // Validar se sala foi selecionada
        if (salaId <= 0 || salaNome.isEmpty()) {
            Log.e(TAG, "Erro: Nenhuma sala selecionada (salaId=$salaId, salaNome=$salaNome)")
            Toast.makeText(this, "Erro: Nenhuma sala selecionada", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setupToolbar()
        setupResumo()
        setupRecyclerView()
        setupSearch()
        setupChipFilters()
        setupObservers()
        
        // Carregar descrições
        loadDescricoes()
    }
    
    /**
     * Configura o card de resumo
     */
    private fun setupResumo() {
        binding.tvSalaNome.text = salaNome
    }
    
    /**
     * Configura os chips de filtro por categoria
     */
    private fun setupChipFilters() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                // Se nenhum chip selecionado, selecionar "Todos"
                binding.chipTodos.isChecked = true
                return@setOnCheckedStateChangeListener
            }
            
            val chipId = checkedIds.first()
            filtroCategoria = when (chipId) {
                R.id.chipTodos -> null
                R.id.chipCadeiras -> "Cadeiras"
                R.id.chipMesas -> "Mesas"
                R.id.chipComputadores -> "Computadores"
                R.id.chipArmarios -> "Armários"
                R.id.chipOutros -> "Outros"
                else -> null
            }
            
            Log.d(TAG, "Filtro de categoria: $filtroCategoria")
            aplicarFiltros()
        }
    }
    
    /**
     * Aplica filtros de busca e categoria
     */
    private fun aplicarFiltros() {
        val query = binding.searchView.query?.toString()?.lowercase()?.trim() ?: ""
        
        var filtradas = descricoesCompletas
        
        // Filtrar por categoria
        if (filtroCategoria != null) {
            filtradas = filtradas.filter { it.categoria == filtroCategoria }
        }
        
        // Filtrar por texto de busca
        if (query.isNotEmpty()) {
            filtradas = filtradas.filter { it.descricao.lowercase().contains(query) }
        }
        
        Log.d(TAG, "Filtros aplicados: categoria=$filtroCategoria, query='$query' - ${filtradas.size} resultados")
        updateDescricoes(filtradas, atualizarCompletas = false)
    }
    
    /**
     * Observa mudanças de estado do ViewModel
     */
    private fun setupObservers() {
        // Observer para lista de descrições
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DescricaoState.Idle -> {
                        hideLoading()
                    }
                    is DescricaoState.Loading -> {
                        showLoading()
                    }
                    is DescricaoState.Success -> {
                        hideLoading()
                        // Converter strings para DescricaoItem
                        val items = state.descricoes.map { DescricaoItem.fromDescricao(it) }
                        updateDescricoes(items, atualizarCompletas = true)
                    }
                    is DescricaoState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
        
        // Observer para patrimônios encontrados por descrição
        lifecycleScope.launch {
            viewModel.patrimoniosState.collect { state ->
                when (state) {
                    is PatrimoniosState.Idle -> { /* nada */ }
                    is PatrimoniosState.Loading -> showLoading()
                    is PatrimoniosState.Found -> {
                        hideLoading()
                        handlePatrimoniosEncontrados(state.patrimonios, state.descricao)
                    }
                    is PatrimoniosState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
        
        // Observer para resultado da coleta
        lifecycleScope.launch {
            viewModel.coletaState.collect { state ->
                when (state) {
                    is ColetaState.Idle -> { /* nada */ }
                    is ColetaState.Loading -> showLoading()
                    is ColetaState.Success -> {
                        hideLoading()
                        SoundUtils.playSuccessSound()
                        Toast.makeText(this@DescricaoSelectionActivity, 
                            "✓ Coleta registrada: ${state.patrimonio.numeroPatrimonio}", 
                            Toast.LENGTH_SHORT).show()
                        // Recarregar descrições (a coletada deve sumir da lista)
                        loadDescricoes()
                        viewModel.limparColetaState()
                    }
                    is ColetaState.Error -> {
                        hideLoading()
                        showError(state.message)
                        viewModel.limparColetaState()
                    }
                }
            }
        }
    }
    
    /**
     * Trata os patrimônios encontrados para uma descrição
     */
    private fun handlePatrimoniosEncontrados(patrimonios: List<Patrimonio>, descricao: String) {
        // Sempre mostrar dialog de confirmação com a quantidade encontrada
        showConfirmacaoColetaDialog(patrimonios.size, descricao)
        viewModel.limparPatrimoniosState()
    }
    
    /**
     * Mostra dialog de confirmação para coletar por descrição
     * O número do patrimônio será null, apenas a descrição será salva
     */
    private fun showConfirmacaoColetaDialog(quantidade: Int, descricao: String) {
        AlertDialog.Builder(this)
            .setTitle("Coletar por Descrição")
            .setMessage("Encontrados $quantidade itens com a descrição:\n\n\"$descricao\"\n\nDeseja registrar a coleta?")
            .setPositiveButton("Coletar") { _, _ ->
                // Mostrar dialog de estado de conservação
                showEstadoDialogParaDescricao(descricao)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Mostra dialog para selecionar estado de conservação (coleta por descrição)
     * Neste caso, numeroPatrimonio será null e a descrição será salva
     */
    private fun showEstadoDialogParaDescricao(descricao: String) {
        val dialog = EstadoPatrimonioDialog.newInstance { estadoSelecionado ->
            Log.d(TAG, "Estado selecionado: ${estadoSelecionado.name} para descrição: $descricao")
            viewModel.registrarColetaPorDescricao(
                descricao = descricao,
                salaId = salaId.toInt(),
                salaNome = salaNome,
                estadoConservacao = estadoSelecionado.name
            )
        }
        dialog.show(supportFragmentManager, "EstadoPatrimonioDialog")
    }
    


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.select_description)
            subtitle = salaNome
        }
    }

    private fun setupRecyclerView() {
        adapter = DescricaoAdapter { item ->
            onDescricaoSelected(item)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DescricaoSelectionActivity)
            adapter = this@DescricaoSelectionActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                aplicarFiltros()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                aplicarFiltros()
                return true
            }
        })
    }

    /**
     * Atualiza lista de descrições
     */
    private fun updateDescricoes(descricoes: List<DescricaoItem>, atualizarCompletas: Boolean = false) {
        // Salvar lista completa se for a primeira vez
        if (atualizarCompletas) {
            descricoesCompletas = descricoes
            Log.d(TAG, "Lista completa salva: ${descricoesCompletas.size} descrições")
            
            // Atualizar resumo
            binding.tvTotalDescricoes.text = descricoes.size.toString()
            binding.tvTotalPendentes.text = descricoes.sumOf { it.quantidade }.toString()
        }
        
        // Atualizar adapter
        adapter.submitList(descricoes)
        
        // Atualizar mensagem vazia
        if (descricoes.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            // Verificar se está vazio por causa do filtro ou se realmente não há dados
            val mensagem = if (descricoesCompletas.isEmpty()) {
                "Nenhuma descrição disponível"
            } else {
                "Nenhuma descrição encontrada para o filtro"
            }
            binding.tvEmpty.text = mensagem
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun onDescricaoSelected(item: DescricaoItem) {
        Log.d(TAG, "Descrição selecionada: ${item.descricao}")
        // Buscar patrimônios não coletados com essa descrição
        viewModel.buscarPatrimoniosPorDescricao(item.descricao)
    }

    /**
     * Carrega descrições não coletadas
     */
    private fun loadDescricoes() {
        viewModel.carregarDescricoes()
    }
    
    /**
     * Mostra loading
     */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    /**
     * Esconde loading
     */
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    /**
     * Mostra erro
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
