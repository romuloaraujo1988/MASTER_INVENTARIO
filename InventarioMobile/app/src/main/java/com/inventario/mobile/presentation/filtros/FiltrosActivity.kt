package com.inventario.mobile.presentation.filtros

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.data.model.Responsavel
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.databinding.ActivityFiltrosBinding
import com.inventario.mobile.di.NetworkModule
import kotlinx.coroutines.launch

class FiltrosActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFiltrosBinding
    private lateinit var viewModel: FiltrosViewModel
    private var responsaveis: List<Responsavel> = emptyList()
    private var selectedResponsavelId: Int? = null
    private var selectedColetado: Boolean? = null
    
    companion object {
        const val EXTRA_RESPONSAVEL_ID = "responsavel_id"
        const val EXTRA_COLETADO = "coletado"
        const val RESULT_FILTERS_APPLIED = 100
        const val RESULT_FILTERS_CLEARED = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltrosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        setupObservers()
        loadInitialData()
    }
    
    private fun setupViewModel() {
        val apiService = NetworkModule.getApiService(this)
        val repository = InventarioRepository.getInstance(this, apiService)
        val factory = FiltrosViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FiltrosViewModel::class.java]
    }
    
    private fun setupUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Filtros"
        }
        
        // Configurar spinner de status
        val statusOptions = arrayOf("Todos", "Coletados", "Não Coletados")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        
        // Listeners
        binding.spinnerResponsavel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedResponsavelId = if (position == 0) null else responsaveis[position - 1].id
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedResponsavelId = null
            }
        }
        
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedColetado = when (position) {
                    0 -> null // Todos
                    1 -> true // Coletados
                    2 -> false // Não Coletados
                    else -> null
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedColetado = null
            }
        }
        
        binding.btnAplicarFiltros.setOnClickListener {
            applyFilters()
        }
        
        binding.btnLimparFiltros.setOnClickListener {
            clearFilters()
        }
        
        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                if (state.responsaveis.isNotEmpty()) {
                    setupResponsavelSpinner(state.responsaveis)
                }
                
                state.errorMessage?.let { message ->
                    // Mostrar erro se necessário
                    // Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun loadInitialData() {
        viewModel.loadResponsaveis()
        
        // Carregar filtros atuais se passados como extras
        intent.getIntExtra(EXTRA_RESPONSAVEL_ID, -1).takeIf { it != -1 }?.let {
            selectedResponsavelId = it
        }
        
        intent.getBooleanExtra(EXTRA_COLETADO, false).let {
            if (intent.hasExtra(EXTRA_COLETADO)) {
                selectedColetado = it
            }
        }
    }
    
    private fun setupResponsavelSpinner(responsavelList: List<Responsavel>) {
        this.responsaveis = responsavelList
        
        val responsavelNames = mutableListOf("Todos os responsáveis")
        responsavelNames.addAll(responsavelList.map { it.nome })
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, responsavelNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerResponsavel.adapter = adapter
        
        // Selecionar responsável atual se houver
        selectedResponsavelId?.let { id ->
            val position = responsavelList.indexOfFirst { it.id == id }
            if (position >= 0) {
                binding.spinnerResponsavel.setSelection(position + 1) // +1 por causa do "Todos"
            }
        }
        
        // Selecionar status atual se houver
        selectedColetado?.let { coletado ->
            val statusPosition = if (coletado) 1 else 2
            binding.spinnerStatus.setSelection(statusPosition)
        }
    }
    
    private fun applyFilters() {
        val resultIntent = Intent().apply {
            selectedResponsavelId?.let { putExtra(EXTRA_RESPONSAVEL_ID, it) }
            selectedColetado?.let { putExtra(EXTRA_COLETADO, it) }
        }
        setResult(RESULT_FILTERS_APPLIED, resultIntent)
        finish()
    }
    
    private fun clearFilters() {
        setResult(RESULT_FILTERS_CLEARED)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}