package com.inventario.mobile.presentation.coletas

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.inventario.mobile.R
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.databinding.ActivityColetasCleanBinding
import com.inventario.mobile.domain.model.StatusFiltro
import com.inventario.mobile.domain.usecase.ColetaListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para visualização de coletas usando Clean Architecture
 * 
 * @see Requirements 1.1, 2.1, 3.1, 4.1, 7.1, 7.2, 7.3
 */
@AndroidEntryPoint
class ColetasActivityClean : AppCompatActivity() {
    
    private lateinit var binding: ActivityColetasCleanBinding
    private val viewModel: ColetasViewModelClean by viewModels()
    private lateinit var adapter: ColetasAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetasCleanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFiltros()
        setupSwipeRefresh()
        setupObservers()
        
        // Carregar dados iniciais
        viewModel.carregarColetas()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Coletas"
            setDisplayHomeAsUpEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ColetasAdapter { coleta ->
            onColetaClick(coleta)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ColetasActivityClean)
            adapter = this@ColetasActivityClean.adapter
        }
    }
    
    private fun setupFiltros() {
        // Chip de filtro por usuário
        binding.chipFiltroUsuario.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleFiltroUsuario()
        }
        
        // ChipGroup de status
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when {
                checkedIds.contains(R.id.chipColetados) -> StatusFiltro.COLETADOS
                checkedIds.contains(R.id.chipPendentes) -> StatusFiltro.PENDENTES
                checkedIds.contains(R.id.chipSemEtiqueta) -> StatusFiltro.SEM_ETIQUETA
                else -> StatusFiltro.TODOS
            }
            viewModel.aplicarFiltroStatus(status)
        }
        
        // Spinner de salas
        binding.spinnerSala.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sala = if (position == 0) null else binding.spinnerSala.selectedItem as String
                viewModel.aplicarFiltroSala(sala)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.aplicarFiltroSala(null)
            }
        }
        
        // Switch de visualização agrupada
        binding.switchAgrupado.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleVisualizacaoAgrupada()
        }
        
        // Botão limpar filtros
        binding.btnLimparFiltros.setOnClickListener {
            viewModel.limparFiltros()
            resetarUIFiltros()
        }
        
        // Campo de pesquisa
        binding.edtPesquisa.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val texto = binding.edtPesquisa.text.toString()
                viewModel.pesquisar(texto)
                true
            } else {
                false
            }
        }
        
        // Pesquisa em tempo real (opcional)
        binding.edtPesquisa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val texto = s.toString()
                viewModel.pesquisar(texto)
            }
        })
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.carregarColetas()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: ColetasState) {
        when (state) {
            is ColetasState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
            
            is ColetasState.Loading -> {
                // Se não está fazendo pull-to-refresh, mostrar progress bar
                if (!binding.swipeRefresh.isRefreshing) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.GONE
                }
            }
            
            is ColetasState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                // Atualizar contadores
                binding.tvColetadosCount.text = state.totalColetados.toString()
                binding.tvPendentesCount.text = state.totalPendentes.toString()
                
                // Atualizar spinner de salas
                atualizarSpinnerSalas(state.salasDisponiveis)
                
                // Atualizar lista
                if (state.coletas.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = if (temFiltrosAtivos(state)) {
                        "Nenhuma coleta encontrada com os filtros aplicados"
                    } else {
                        "Nenhuma coleta encontrada"
                    }
                    binding.btnLimparFiltros.visibility = if (temFiltrosAtivos(state)) View.VISIBLE else View.GONE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    
                    // Submeter lista (agrupada ou simples)
                    if (state.visualizacaoAgrupada && state.coletasAgrupadas.isNotEmpty()) {
                        adapter.submitGroupedList(state.coletasAgrupadas)
                    } else {
                        adapter.submitColetaList(state.coletas)
                    }
                }
                
                // Atualizar estado dos filtros na UI
                atualizarUIFiltros(state)
            }
            
            is ColetasState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = state.message
                binding.btnLimparFiltros.visibility = View.GONE
                
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun atualizarSpinnerSalas(salas: List<String>) {
        val opcoes = listOf("Todas as salas") + salas
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSala.adapter = spinnerAdapter
    }
    
    private fun atualizarUIFiltros(state: ColetasState.Success) {
        // Atualizar chip de usuário
        binding.chipFiltroUsuario.isChecked = state.filtroUsuario
        
        // Atualizar chips de status
        when (state.filtroStatus) {
            StatusFiltro.TODOS -> binding.chipTodos.isChecked = true
            StatusFiltro.COLETADOS -> binding.chipColetados.isChecked = true
            StatusFiltro.PENDENTES -> binding.chipPendentes.isChecked = true
            StatusFiltro.SEM_ETIQUETA -> binding.chipSemEtiqueta.isChecked = true
        }
        
        // Atualizar switch de agrupamento
        binding.switchAgrupado.isChecked = state.visualizacaoAgrupada
    }
    
    private fun resetarUIFiltros() {
        binding.chipFiltroUsuario.isChecked = false
        binding.chipTodos.isChecked = true
        binding.spinnerSala.setSelection(0)
        binding.switchAgrupado.isChecked = false
        binding.edtPesquisa.setText("")
    }
    
    private fun temFiltrosAtivos(state: ColetasState.Success): Boolean {
        return state.filtroUsuario || 
               state.filtroSala != null || 
               state.filtroStatus != StatusFiltro.TODOS
    }
    
    private fun onColetaClick(coleta: Coleta) {
        Toast.makeText(
            this, 
            "Patrimônio: ${coleta.numeroPatrimonio}\n${coleta.descricaoPatrimonio}", 
            Toast.LENGTH_SHORT
        ).show()
    }
}
