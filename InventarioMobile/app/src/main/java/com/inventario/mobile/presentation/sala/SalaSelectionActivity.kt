package com.inventario.mobile.presentation.sala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivitySalaSelectionBinding
import com.inventario.mobile.presentation.adapter.SalaAdapter
import com.inventario.mobile.ui.base.BaseOfflineActivity
import com.inventario.mobile.utils.NavigationHelper
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity para seleção de sala
 * Clean Architecture + MVVM + Hilt + Modo Offline Automático
 * 
 * v2.9: Suporte a "Sala Fixada" - permite fixar uma sala para coleta rápida
 */
@AndroidEntryPoint
class SalaSelectionActivity : BaseOfflineActivity() {

    private lateinit var binding: ActivitySalaSelectionBinding
    
    // ViewModel antigo (mantido temporariamente para compatibilidade)
    private lateinit var viewModel: SalaSelectionViewModel
    
    private lateinit var salaAdapter: SalaAdapter
    
    @Inject
    lateinit var preferencesManager: PreferencesManager

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
            setupSalaFixada()
            setupFixarEstado()
            
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
        
        salaAdapter = SalaAdapter(
            onSalaClick = { sala ->
                // Quando uma sala for selecionada
                Log.d(TAG, "Sala selecionada: ${sala.nome} para coleta tipo: $coletaTipo")
                navegarParaColeta(sala)
            },
            onSalaLongClick = { sala ->
                // v2.9: Long click para fixar/desfixar sala
                mostrarMenuSala(sala)
            }
        )
        
        binding.recyclerViewSalas.apply {
            this.layoutManager = layoutManager
            adapter = salaAdapter
            // Todas as salas são carregadas de uma vez - sem scroll infinito
        }
    }
    
    /**
     * Navega para a tela de coleta apropriada
     */
    private fun navegarParaColeta(sala: com.inventario.mobile.domain.model.Sala) {
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
                // Navegar diretamente para DescricaoSelectionActivity (bypass da tela intermediária)
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
    
    // ========================================
    // SALA FIXADA (v2.9)
    // ========================================
    
    /**
     * Configura a UI para sala fixada
     */
    private fun setupSalaFixada() {
        // Verificar se há sala fixada
        if (preferencesManager.hasSalaFixada()) {
            val salaId = preferencesManager.getSalaFixadaId()
            val salaNome = preferencesManager.getSalaFixadaNome()
            
            Log.d(TAG, "✓ Sala fixada encontrada: $salaNome (ID: $salaId)")
            
            // Mostrar banner de sala fixada
            mostrarBannerSalaFixada(salaNome)
        }
    }
    
    /**
     * Mostra banner informando que há uma sala fixada
     */
    private fun mostrarBannerSalaFixada(salaNome: String) {
        binding.layoutSalaFixada?.visibility = android.view.View.VISIBLE
        binding.tvSalaFixadaNome?.text = "📌 $salaNome"
        
        // Botão para ir direto para a sala fixada
        binding.btnUsarSalaFixada?.setOnClickListener {
            val salaId = preferencesManager.getSalaFixadaId()
            val sala = com.inventario.mobile.domain.model.Sala(
                id = salaId,
                nome = salaNome,
                codigo = "",
                descricao = null,
                setorId = 0
            )
            navegarParaColeta(sala)
        }
        
        // Botão para remover sala fixada
        binding.btnRemoverSalaFixada?.setOnClickListener {
            preferencesManager.clearSalaFixada()
            binding.layoutSalaFixada?.visibility = android.view.View.GONE
            Toast.makeText(this, "Sala fixada removida", Toast.LENGTH_SHORT).show()
            
            // Recarregar lista completa
            viewModel.loadSalas()
        }
    }
    
    /**
     * Mostra menu de opções para a sala (long click)
     */
    private fun mostrarMenuSala(sala: com.inventario.mobile.domain.model.Sala) {
        val salaFixadaId = preferencesManager.getSalaFixadaId()
        val isFixada = sala.id == salaFixadaId
        
        val opcoes = if (isFixada) {
            arrayOf("📌 Remover fixação", "✓ Selecionar sala")
        } else {
            arrayOf("📌 Fixar esta sala", "✓ Selecionar sala")
        }
        
        AlertDialog.Builder(this)
            .setTitle(sala.nome)
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> {
                        if (isFixada) {
                            // Remover fixação
                            preferencesManager.clearSalaFixada()
                            binding.layoutSalaFixada?.visibility = android.view.View.GONE
                            Toast.makeText(this, "Sala fixada removida", Toast.LENGTH_SHORT).show()
                            viewModel.loadSalas()
                        } else {
                            // Fixar sala
                            preferencesManager.setSalaFixada(sala.id, sala.nome)
                            mostrarBannerSalaFixada(sala.nome)
                            Toast.makeText(this, "📌 Sala \"${sala.nome}\" fixada!", Toast.LENGTH_SHORT).show()
                            
                            // Filtrar para mostrar apenas a sala fixada
                            filtrarSalaFixada()
                        }
                    }
                    1 -> {
                        // Selecionar sala normalmente
                        navegarParaColeta(sala)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Filtra a lista para mostrar apenas a sala fixada
     */
    private fun filtrarSalaFixada() {
        val salaFixadaId = preferencesManager.getSalaFixadaId()
        if (salaFixadaId > 0) {
            val salasFiltradas = allSalas.filter { it.id == salaFixadaId }
            salaAdapter.submitList(salasFiltradas)
            Log.d(TAG, "Lista filtrada para sala fixada: ${salasFiltradas.size} sala(s)")
        }
    }

    /**
     * Configura o card "Modo rápido" (fixar estado de conservação).
     *
     * Contexto: o `ScannerActivity` abre imediatamente a `CaptureActivity` do ZXing
     * (tela cheia), escondendo o switch `switchFixarEstado` que existe no layout do
     * scanner mas nunca é visto. Expondo essa preferência aqui — uma tela por onde
     * o coletor obrigatoriamente passa antes de escanear — garantimos acesso ao
     * "modo rápido" (pular o dialog de estado em cada coleta).
     *
     * Ao tocar no switch:
     * - Se ligar: abre o `EstadoPatrimonioDialog` para escolher qual estado fixar.
     *   Só marca como habilitado se o usuário confirmar um estado.
     * - Se desligar: limpa a preferência e esconde o chip do estado.
     *
     * O chip abaixo do switch mostra o estado escolhido e também é clicável para
     * trocar o estado sem precisar desligar/religar.
     */
    private fun setupFixarEstado() {
        atualizarUIFixarEstado()

        binding.switchFixarEstadoSala.setOnCheckedChangeListener { buttonView, isChecked ->
            // `isPressed` evita reagir a mudanças programáticas (atualizarUIFixarEstado)
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            if (isChecked) {
                abrirDialogEstado { estadoSelecionado ->
                    preferencesManager.setEstadoFixoEnabled(true)
                    preferencesManager.setEstadoFixo(estadoSelecionado.name)
                    atualizarUIFixarEstado()
                }.also {
                    // Se o usuário cancelar o dialog, desmarcar o switch
                    it.dialog?.setOnCancelListener {
                        binding.switchFixarEstadoSala.isChecked = false
                    }
                }
            } else {
                preferencesManager.setEstadoFixoEnabled(false)
                preferencesManager.setEstadoFixo(null)
                atualizarUIFixarEstado()
            }
        }

        // Permite trocar o estado sem precisar desligar/religar o switch
        binding.chipEstadoFixoSala.setOnClickListener {
            abrirDialogEstado { estadoSelecionado ->
                preferencesManager.setEstadoFixoEnabled(true)
                preferencesManager.setEstadoFixo(estadoSelecionado.name)
                atualizarUIFixarEstado()
            }
        }
    }

    private fun abrirDialogEstado(
        onSelecionado: (com.inventario.mobile.data.model.EstadoPatrimonio) -> Unit
    ): com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog {
        val dialog = com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog
            .newInstance(onSelecionado)
        dialog.show(supportFragmentManager, "EstadoFixoSalaDialog")
        return dialog
    }

    private fun atualizarUIFixarEstado() {
        val habilitado = preferencesManager.isEstadoFixoEnabled()
        val estadoFixo = preferencesManager.getEstadoFixo()

        binding.switchFixarEstadoSala.isChecked = habilitado && !estadoFixo.isNullOrEmpty()

        if (habilitado && !estadoFixo.isNullOrEmpty()) {
            val descricaoAmigavel = try {
                com.inventario.mobile.data.model.EstadoPatrimonio.valueOf(estadoFixo).descricao
            } catch (e: Exception) {
                estadoFixo
            }
            binding.chipEstadoFixoSala.text = "Estado: $descricaoAmigavel"
            binding.chipEstadoFixoSala.visibility = android.view.View.VISIBLE
            binding.tvFixarEstadoDescricao.text =
                "Modo rápido ligado: não pede confirmação de estado em cada coleta."
        } else {
            binding.chipEstadoFixoSala.visibility = android.view.View.GONE
            binding.tvFixarEstadoDescricao.text =
                "Fixa o estado da coleta. Não pede confirmação a cada scan."
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
