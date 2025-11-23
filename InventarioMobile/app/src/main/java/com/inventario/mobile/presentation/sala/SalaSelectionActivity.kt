package com.inventario.mobile.presentation.sala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivitySalaSelectionBinding
import com.inventario.mobile.presentation.adapter.SalaAdapter
import com.inventario.mobile.ui.base.BaseOfflineActivity
import com.inventario.mobile.utils.NavigationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para seleção de sala
 * Clean Architecture + MVVM + Hilt + Modo Offline Automático
 */
@AndroidEntryPoint
class SalaSelectionActivity : BaseOfflineActivity() {

    private lateinit var binding: ActivitySalaSelectionBinding
    
    // ViewModel antigo (mantido temporariamente para compatibilidade)
    private lateinit var viewModel: SalaSelectionViewModel
    
    private lateinit var salaAdapter: SalaAdapter

    private var coletaTipo: String = "QRCODE" // QRCODE ou MANUAL
    private var allSalas: List<com.inventario.mobile.domain.model.Sala> = emptyList() // Cache de todas as salas

    companion object {
        private const val TAG = "SalaSelectionActivity"
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
        const val EXTRA_COLETA_TIPO = "COLETA_TIPO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando SalaSelectionActivity")
        
        try {
            binding = ActivitySalaSelectionBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Obter tipo de coleta
            coletaTipo = intent.getStringExtra(EXTRA_COLETA_TIPO) ?: "QRCODE"
            Log.d(TAG, "onCreate: Tipo de coleta = $coletaTipo")
            
            // Inicializar ViewModel (mantendo abordagem antiga por enquanto)
            // TODO: Migrar completamente para SalaSelectionViewModelClean com Paging 3
            viewModel = androidx.lifecycle.ViewModelProvider(
                this,
                SalaSelectionViewModelFactory(application)
            )[SalaSelectionViewModel::class.java]
            
            setupToolbar()
            setupSearchView()
            setupRecyclerView()
            setupObservers()
            
            // Carregar salas
            viewModel.loadSalas()
            
            Log.d(TAG, "onCreate: SalaSelectionActivity inicializada com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Erro ao inicializar SalaSelectionActivity", e)
            Toast.makeText(this, "Erro ao inicializar tela: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    /*
    private fun initializeViewModel() {
        // Criar instância do banco de dados
        val database = InventarioDatabase.getDatabase(this)
        
        // Criar instância do ApiService (usando mock temporário)
        val apiService: ApiService = MockApiService()
        
        // Criar repositório com todos os DAOs necessários
        /*val salaRepository = SalaRepositoryImpl(
            database.salaDao(), 
            database.sincronizacaoDao(),
            apiService
        )*/
        
        // Criar ViewModel usando factory
        val factory = SalaSelectionViewModelFactory(salaRepository)
        viewModel = ViewModelProvider(this, factory)[SalaSelectionViewModel::class.java]
    }
    */

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = when (coletaTipo) {
                "MANUAL" -> "Selecionar Sala - Coleta Manual"
                "DESCRICAO" -> "Selecionar Sala - Coleta por Descrição"
                else -> "Selecionar Sala - Scan QR Code"
            }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null
    
    private fun setupSearchView() {
        // Listener para mudanças no texto
        binding.editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                
                // Mostrar/ocultar botão de limpar
                binding.buttonClearSearch.visibility = if (query.isNotEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Cancelar busca anterior
                searchJob?.cancel()
                
                // Debounce: esperar 500ms após parar de digitar
                searchJob = lifecycleScope.launch {
                    kotlinx.coroutines.delay(500)
                    filterSalas(query)
                }
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Botão para limpar busca
        binding.buttonClearSearch.setOnClickListener {
            binding.editTextSearch.text.clear()
            viewModel.loadSalas() // Recarregar lista normal
        }
    }
    
    private fun filterSalas(query: String) {
        if (query.isBlank()) {
            // Sem busca: carregar lista paginada normal
            viewModel.loadSalas()
        } else {
            // Com busca: filtrar apenas localmente (mais rápido)
            // Filtra apenas as salas já carregadas
            val filtered = allSalas.filter { sala ->
                sala.nome.contains(query, ignoreCase = true)
            }
            salaAdapter.submitList(filtered.toList())
            
            // Mostrar mensagem informativa
            if (filtered.isEmpty()) {
                if (allSalas.isEmpty()) {
                    Toast.makeText(this, "Carregue as salas primeiro", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nenhuma sala encontrada. Role para baixo para carregar mais.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "filterSalas: ${filtered.size} salas encontradas para '$query'")
            }
        }
    }

    private fun setupRecyclerView() {
        // Configurar SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh: Recarregando salas")
            viewModel.refreshSalas()
        }
        
        val layoutManager = LinearLayoutManager(this)
        
        salaAdapter = SalaAdapter { sala ->
            // Quando uma sala for selecionada
            Log.d(TAG, "Sala selecionada: ${sala.nome} para coleta tipo: $coletaTipo")
            
            // Navegar para a activity apropriada baseado no tipo de coleta
            when (coletaTipo) {
                "MANUAL" -> {
                    // Navegar para ManualCollectionActivity
                    val intent = Intent(this, com.inventario.mobile.presentation.coleta.ManualCollectionActivity::class.java).apply {
                        putExtra(EXTRA_SALA_ID, sala.id)
                        putExtra(EXTRA_SALA_NOME, sala.nome)
                    }
                    startActivity(intent)
                    finish()
                }
                "DESCRICAO" -> {
                    // Navegar para DescricaoSelectionActivity
                    val intent = Intent(this, com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity::class.java).apply {
                        putExtra(com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity.EXTRA_SALA_ID, sala.id)
                        putExtra(com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity.EXTRA_SALA_NOME, sala.nome)
                    }
                    startActivity(intent)
                    finish()
                }
                else -> {
                    // Navegar para ScannerActivity (com QR Code)
                    val intent = Intent(this, com.inventario.mobile.presentation.scanner.ScannerActivity::class.java).apply {
                        putExtra(com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_SALA_ID, sala.id.toInt())
                        putExtra(com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_SALA_NOME, sala.nome)
                        putExtra(com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_ALLOW_COLLECTION, true)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
        
        binding.recyclerViewSalas.apply {
            this.layoutManager = layoutManager
            adapter = salaAdapter
            // Todas as salas são carregadas de uma vez - sem scroll infinito
        }
    }

    private fun setupObservers() {
        try {
            lifecycleScope.launch {
                try {
                    viewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "setupObservers: Erro ao coletar estado", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupObservers: Erro ao configurar observers", e)
        }
    }

    private fun updateUI(state: SalaSelectionUiState) {
        // Atualizar cache de todas as salas carregadas
        allSalas = state.salas
        
        // Só atualizar lista se não estiver buscando
        val currentQuery = binding.editTextSearch.text.toString()
        if (currentQuery.isBlank()) {
            // Sem busca: mostrar lista normal do estado
            salaAdapter.submitList(state.salas.toList())
        }
        // Se estiver buscando, não atualiza a lista (mantém filtro)
        
        // Atualizar estado de loading (apenas para pull-to-refresh)
        binding.swipeRefreshLayout.isRefreshing = state.isLoading && state.salas.isEmpty()
        
        // Mostrar/ocultar mensagem de lista vazia
        if (state.salas.isEmpty() && !state.isLoading) {
            binding.textViewEmpty.visibility = android.view.View.VISIBLE
            binding.recyclerViewSalas.visibility = android.view.View.GONE
        } else {
            binding.textViewEmpty.visibility = android.view.View.GONE
            binding.recyclerViewSalas.visibility = android.view.View.VISIBLE
        }
        
        // Log para debug
        Log.d(TAG, "updateUI: ${state.salas.size} salas, loading=${state.isLoading}, loadingMore=${state.isLoadingMore}")
        
        // Mostrar mensagem de erro
        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavigationHelper.goBack(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // ========== CALLBACKS DE CONECTIVIDADE ==========
    
    /**
     * Chamado quando a conexão é restaurada
     * Recarrega salas do servidor
     */
    override fun onConnectivityRestored() {
        Log.d(TAG, "✓ Conexão restaurada! Recarregando salas do servidor...")
        
        // Mostrar Snackbar informativo
        Snackbar.make(
            binding.root,
            "Conexão restaurada. Atualizando dados...",
            Snackbar.LENGTH_SHORT
        ).show()
        
        // Recarregar salas do servidor
        viewModel.refreshSalas()
    }
    
    /**
     * Chamado quando a conexão é perdida
     * Usa apenas dados locais
     */
    override fun onConnectivityLost() {
        Log.d(TAG, "⚠️ Conexão perdida! Usando dados locais...")
        
        // Mostrar Snackbar informativo
        Snackbar.make(
            binding.root,
            "Sem conexão. Usando dados locais.",
            Snackbar.LENGTH_LONG
        ).setAction("OK") {
            // Dismiss
        }.show()
        
        // Garantir que está usando dados locais
        // O ViewModel já deve estar configurado para fallback automático
        if (allSalas.isEmpty()) {
            // Se não há salas carregadas, tentar carregar do banco local
            viewModel.loadSalas()
        }
    }
}