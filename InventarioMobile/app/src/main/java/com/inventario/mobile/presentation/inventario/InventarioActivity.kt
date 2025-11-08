package com.inventario.mobile.presentation.inventario

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityInventarioBinding
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.presentation.scanner.ScannerActivity
import com.inventario.mobile.presentation.filtros.FiltrosActivity
import com.inventario.mobile.utils.NavigationHelper
import kotlinx.coroutines.launch
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.chip.Chip
import com.inventario.mobile.data.model.Responsavel

class InventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioBinding
    private lateinit var viewModel: InventarioViewModel
    private lateinit var adapter: PatrimonioAdapter
    private var isLoadingMore = false
    private var responsavelSelecionado: Responsavel? = null
    private var statusColetaSelecionado: Boolean? = null
    
    companion object {
        private const val REQUEST_FILTERS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("InventarioActivity", "Iniciando onCreate...")
            
            binding = ActivityInventarioBinding.inflate(layoutInflater)
            setContentView(binding.root)

            android.util.Log.d("InventarioActivity", "Layout inflado com sucesso")
            
            setupViewModel()
            android.util.Log.d("InventarioActivity", "ViewModel configurado com sucesso")
            
            setupUI()
            android.util.Log.d("InventarioActivity", "UI configurada com sucesso")
            
            setupRecyclerView()
            android.util.Log.d("InventarioActivity", "RecyclerView configurado com sucesso")
            
            setupObservers()
            android.util.Log.d("InventarioActivity", "Observers configurados com sucesso")
            
            setupListeners()
            android.util.Log.d("InventarioActivity", "Listeners configurados com sucesso")
            
            setupFiltros()
            android.util.Log.d("InventarioActivity", "Filtros configurados com sucesso")
            
            // NÃO carregar patrimônios automaticamente
            // Apenas mostrar mensagem para selecionar responsável
            android.util.Log.d("InventarioActivity", "onCreate concluído com sucesso")
            
        } catch (e: Exception) {
            android.util.Log.e("InventarioActivity", "Erro durante onCreate", e)
            
            // Mostrar erro para o usuário
            Toast.makeText(this, "Erro ao inicializar a tela: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Tentar finalizar a activity graciosamente
            try {
                finish()
            } catch (finishException: Exception) {
                android.util.Log.e("InventarioActivity", "Erro ao finalizar activity", finishException)
            }
        }
    }

    private fun setupViewModel() {
        try {
            android.util.Log.d("InventarioActivity", "Iniciando configuração do ViewModel...")
            
            android.util.Log.d("InventarioActivity", "Obtendo ApiService...")
            val apiService = NetworkModule.getApiService(this)
            
            android.util.Log.d("InventarioActivity", "Obtendo InventarioRepository...")
            val repository = InventarioRepository.getInstance(this, apiService)
            
            android.util.Log.d("InventarioActivity", "Criando InventarioViewModelFactory...")
            val factory = InventarioViewModelFactory(repository)
            
            android.util.Log.d("InventarioActivity", "Criando InventarioViewModel...")
            viewModel = ViewModelProvider(this, factory)[InventarioViewModel::class.java]
            
            android.util.Log.d("InventarioActivity", "ViewModel configurado com sucesso")
            
        } catch (e: Exception) {
            android.util.Log.e("InventarioActivity", "Erro ao configurar ViewModel", e)
            throw RuntimeException("Falha na inicialização do ViewModel: ${e.message}", e)
        }
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Inventário"
            setDisplayHomeAsUpEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            NavigationHelper.goBack(this)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPatrimonios()
        }

    }

    private fun setupListeners() {
        binding.fabSearch.setOnClickListener {
            showQuickActionsDialog()
        }
    }
    
    private fun setupFiltros() {
        android.util.Log.d("InventarioActivity", "Configurando filtros...")
        
        // Configurar chips de status
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            when {
                binding.chipTodos.isChecked -> {
                    statusColetaSelecionado = null
                }
                binding.chipColetados.isChecked -> {
                    statusColetaSelecionado = true
                }
                binding.chipNaoColetados.isChecked -> {
                    statusColetaSelecionado = false
                }
            }
            aplicarFiltros()
        }
        
        // Botão limpar filtros
        binding.btnLimparFiltros.setOnClickListener {
            limparFiltros()
        }
        
        android.util.Log.d("InventarioActivity", "Filtros configurados")
    }
    
    private fun setupResponsavelSpinner(responsaveis: List<Responsavel>) {
        android.util.Log.d("InventarioActivity", "setupResponsavelSpinner chamado com ${responsaveis.size} responsáveis")
        
        // Evitar reconfigurar se já foi configurado
        val autoComplete = binding.spinnerResponsavel as? AutoCompleteTextView
        if (autoComplete?.adapter != null && autoComplete.adapter.count > 0) {
            android.util.Log.d("InventarioActivity", "Spinner já configurado, ignorando")
            return
        }
        
        val items = responsaveis.map { it.nome }
        android.util.Log.d("InventarioActivity", "Itens do spinner: $items")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoComplete?.apply {
            setAdapter(adapter)
            setText("Selecione um responsável", false)
            
            setOnItemClickListener { _, _, position, _ ->
                android.util.Log.d("InventarioActivity", "Responsável selecionado: ${responsaveis[position].nome} (ID: ${responsaveis[position].id})")
                responsavelSelecionado = responsaveis[position]
                aplicarFiltros()
            }
        }
        
        android.util.Log.d("InventarioActivity", "Spinner configurado com sucesso")
    }
    
    private fun aplicarFiltros() {
        val responsavelId = responsavelSelecionado?.id
        val coletado = statusColetaSelecionado
        
        if (responsavelId != null) {
            // Carregar patrimônios do responsável selecionado
            viewModel.loadPatrimoniosByResponsavel(responsavelId, coletado)
        } else {
            // Sem responsável selecionado, limpar lista
            viewModel.clearPatrimonios()
        }
    }
    
    private fun limparFiltros() {
        // Limpar responsável
        responsavelSelecionado = null
        (binding.spinnerResponsavel as? AutoCompleteTextView)?.setText("Selecione um responsável", false)
        
        // Limpar status
        statusColetaSelecionado = null
        binding.chipTodos.isChecked = true
        
        // Limpar lista de patrimônios
        viewModel.clearPatrimonios()
    }
    
    private fun showQuickActionsDialog() {
        val actions = arrayOf(
            "Escanear QR Code",
            "Atualizar Lista",
            "Exportar Dados"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Ações Rápidas")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> {
                        // Abrir scanner
                        val intent = Intent(this, ScannerActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        // Atualizar lista
                        viewModel.loadPatrimonios()
                    }
                    2 -> {
                        // Exportar dados (implementar futuramente)
                        Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = PatrimonioAdapter { patrimonio ->
            onPatrimonioClick(patrimonio)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InventarioActivity)
            adapter = this@InventarioActivity.adapter
            
            // Adicionar scroll listener para paginação
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    
                    // Carregar mais itens quando próximo do final da lista
                    if (!isLoadingMore && 
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                        firstVisibleItemPosition >= 0) {
                        
                        val currentState = viewModel.uiState.value
                        if (currentState.hasMorePages && !currentState.isLoading) {
                            isLoadingMore = true
                            viewModel.loadMorePatrimonios()
                        }
                    }
                }
            })
        }
    }

    private fun onPatrimonioClick(patrimonio: Patrimonio) {
        showPatrimonioDetailsDialog(patrimonio)
    }
    
    private fun showPatrimonioDetailsDialog(patrimonio: Patrimonio) {
        val details = buildString {
            append("Número: ${patrimonio.numeroPatrimonio}\n\n")
            append("Descrição: ${patrimonio.descricao}\n\n")
            
            patrimonio.marca?.let { append("Marca: $it\n") }
            patrimonio.modelo?.let { append("Modelo: $it\n") }
            patrimonio.numeroSerie?.let { append("Nº Série: $it\n") }
            patrimonio.estado?.let { append("Estado: $it\n") }
            patrimonio.valor?.let { append("Valor: R$ %.2f\n".format(it)) }
            
            append("\n")
            append("Setor: ${patrimonio.setorNome ?: "N/A"}\n")
            append("Sala: ${patrimonio.salaNome ?: "N/A"}\n")
            append("Responsável: ${patrimonio.responsavelNome ?: "N/A"}\n")
            
            append("\n")
            append("Status: ${if (patrimonio.coletado) "Coletado" else "Pendente"}\n")
            patrimonio.dataColeta?.let { append("Data Coleta: $it\n") }
            patrimonio.observacoesColeta?.let { append("Obs. Coleta: $it\n") }
            patrimonio.observacoes?.let { append("\nObservações: $it") }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Detalhes do Patrimônio")
            .setMessage(details)
            .setPositiveButton("Fechar", null)
            .setNeutralButton("Escanear QR") { _, _ ->
                // Abrir scanner para este patrimônio
                val intent = Intent(this, ScannerActivity::class.java)
                startActivity(intent)
            }
            .show()
    }

    private fun setupObservers() {
        android.util.Log.d("InventarioActivity", "Configurando observers...")
        
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                android.util.Log.d("InventarioActivity", "Estado atualizado - Responsáveis: ${state.responsaveis.size}, Patrimônios: ${state.patrimonios.size}")
                
                // Configurar spinner quando responsáveis forem carregados
                if (state.responsaveis.isNotEmpty()) {
                    android.util.Log.d("InventarioActivity", "Configurando spinner com ${state.responsaveis.size} responsáveis")
                    setupResponsavelSpinner(state.responsaveis)
                }
                
                updateUI(state)
            }
        }
        
        android.util.Log.d("InventarioActivity", "Observers configurados")
    }

    private fun updateUI(state: InventarioUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading && state.currentPage == 0
        isLoadingMore = state.isLoadingMore
        
        // Mostrar/ocultar indicador de carregamento de mais itens
        binding.loadingMoreIndicator.visibility = if (state.isLoadingMore) View.VISIBLE else View.GONE
        
        val displayList = if (state.searchQuery.isNotEmpty() || state.sortOrder != SortOrder.NUMERO_ASC) {
            state.patrimoniosFiltered
        } else {
            state.patrimonios
        }
        
        if (displayList.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            adapter.submitList(displayList)
        } else if (!state.isLoading && !state.isLoadingMore) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        }

        // Atualizar contador com informações de paginação e busca
        val totalText = if (responsavelSelecionado == null) {
            "Selecione um responsável para visualizar os patrimônios"
        } else if (state.searchQuery.isNotEmpty()) {
            "Encontrados: ${displayList.size} de ${state.patrimonios.size} patrimônios"
        } else if (state.hasMorePages) {
            "Total: ${displayList.size}+ patrimônios"
        } else if (state.totalPatrimonios > 0) {
            "Total: ${state.totalPatrimonios} patrimônios"
        } else {
            "Total: ${displayList.size} patrimônios"
        }
        binding.tvTotalCount.text = totalText

        state.errorMessage?.let { message ->
            // Não mostrar toast para mensagem de seleção de responsável
            if (!message.contains("Selecione um responsável")) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            viewModel.clearErrorMessage()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventario, menu)
        
        // Configurar SearchView
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
        
        searchView?.apply {
            queryHint = "Buscar patrimônio..."
            maxWidth = Integer.MAX_VALUE
            
            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
                
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.searchPatrimonios(newText ?: "")
                    return true
                }
            })
        }
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_filter -> {
                openFiltersScreen()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Número (Crescente)",
            "Número (Decrescente)",
            "Descrição (A-Z)",
            "Descrição (Z-A)",
            "Setor (A-Z)",
            "Setor (Z-A)"
        )
        
        val currentSort = when (viewModel.uiState.value.sortOrder) {
            SortOrder.NUMERO_ASC -> 0
            SortOrder.NUMERO_DESC -> 1
            SortOrder.DESCRICAO_ASC -> 2
            SortOrder.DESCRICAO_DESC -> 3
            SortOrder.SETOR_ASC -> 4
            SortOrder.SETOR_DESC -> 5
        }
        
        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setSingleChoiceItems(sortOptions, currentSort) { dialog, which ->
                val sortOrder = when (which) {
                    0 -> SortOrder.NUMERO_ASC
                    1 -> SortOrder.NUMERO_DESC
                    2 -> SortOrder.DESCRICAO_ASC
                    3 -> SortOrder.DESCRICAO_DESC
                    4 -> SortOrder.SETOR_ASC
                    5 -> SortOrder.SETOR_DESC
                    else -> SortOrder.NUMERO_ASC
                }
                viewModel.setSortOrder(sortOrder)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun openFiltersScreen() {
        val currentState = viewModel.uiState.value
        val intent = Intent(this, FiltrosActivity::class.java).apply {
            currentState.filtroResponsavelId?.let { 
                putExtra(FiltrosActivity.EXTRA_RESPONSAVEL_ID, it) 
            }
            currentState.filtroColetado?.let { 
                putExtra(FiltrosActivity.EXTRA_COLETADO, it) 
            }
        }
        startActivityForResult(intent, REQUEST_FILTERS)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_FILTERS) {
            when (resultCode) {
                FiltrosActivity.RESULT_FILTERS_APPLIED -> {
                    data?.let { intent ->
                        val responsavelId = if (intent.hasExtra(FiltrosActivity.EXTRA_RESPONSAVEL_ID)) {
                            intent.getIntExtra(FiltrosActivity.EXTRA_RESPONSAVEL_ID, -1)
                        } else null
                        
                        val coletado = if (intent.hasExtra(FiltrosActivity.EXTRA_COLETADO)) {
                            intent.getBooleanExtra(FiltrosActivity.EXTRA_COLETADO, false)
                        } else null
                        
                        viewModel.applyFilters(responsavelId, coletado)
                    }
                }
                FiltrosActivity.RESULT_FILTERS_CLEARED -> {
                    viewModel.clearFilters()
                }
            }
        }
    }
}