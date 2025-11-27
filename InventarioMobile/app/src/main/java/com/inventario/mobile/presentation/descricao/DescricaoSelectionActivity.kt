package com.inventario.mobile.presentation.descricao

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityDescricaoSelectionBinding
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
    private var descricoesCompletas: List<String> = emptyList()

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
        setupRecyclerView()
        setupSearch()
        setupObservers()
        
        // Carregar descrições
        loadDescricoes()
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
                        updateDescricoes(state.descricoes)
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
        adapter = DescricaoAdapter { descricao ->
            onDescricaoSelected(descricao)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DescricaoSelectionActivity)
            adapter = this@DescricaoSelectionActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchDescricoes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrar em tempo real conforme o usuário digita
                if (newText.isNullOrEmpty()) {
                    // Se vazio, mostrar todas as descrições
                    updateDescricoes(descricoesCompletas)
                } else {
                    // Filtrar descrições
                    searchDescricoes(newText)
                }
                return true
            }
        })
    }
    
    /**
     * Busca descrições localmente (filtro em memória)
     */
    private fun searchDescricoes(query: String) {
        val queryLower = query.lowercase().trim()
        
        // Filtrar descrições que contenham o texto buscado
        val descricoesFiltradas = descricoesCompletas.filter { descricao ->
            descricao.lowercase().contains(queryLower)
        }
        
        Log.d(TAG, "Busca: '$query' - Encontradas ${descricoesFiltradas.size} de ${descricoesCompletas.size} descrições")
        
        // Atualizar lista filtrada
        updateDescricoes(descricoesFiltradas)
    }

    /**
     * Atualiza lista de descrições
     */
    private fun updateDescricoes(descricoes: List<String>) {
        // Salvar lista completa se for a primeira vez
        if (descricoesCompletas.isEmpty() && descricoes.isNotEmpty()) {
            descricoesCompletas = descricoes
            Log.d(TAG, "Lista completa salva: ${descricoesCompletas.size} descrições")
        }
        
        // Atualizar adapter
        adapter.submitList(descricoes.map { it })
        
        // Atualizar mensagem vazia
        if (descricoes.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            // Verificar se está vazio por causa do filtro ou se realmente não há dados
            val mensagem = if (descricoesCompletas.isEmpty()) {
                "Nenhuma descrição disponível"
            } else {
                "Nenhuma descrição encontrada para a busca"
            }
            binding.tvEmpty.text = mensagem
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
        }
    }

    private fun onDescricaoSelected(descricao: String) {
        Log.d(TAG, "Descrição selecionada: $descricao")
        // Buscar patrimônios não coletados com essa descrição
        viewModel.buscarPatrimoniosPorDescricao(descricao)
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
        binding.progressBar.visibility = android.view.View.VISIBLE
    }
    
    /**
     * Esconde loading
     */
    private fun hideLoading() {
        binding.progressBar.visibility = android.view.View.GONE
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
