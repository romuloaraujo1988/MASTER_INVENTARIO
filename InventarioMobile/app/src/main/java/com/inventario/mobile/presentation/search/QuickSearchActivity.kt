package com.inventario.mobile.presentation.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityQuickSearchBinding
import com.inventario.mobile.domain.model.PatrimonioComColeta
import com.inventario.mobile.domain.model.SearchFilter
import com.inventario.mobile.presentation.adapter.PatrimonioSearchAdapter

import com.inventario.mobile.presentation.detail.PatrimonioDetailActivity
import com.inventario.mobile.presentation.filtros.FiltrosActivity
import com.inventario.mobile.presentation.state.QuickSearchState
import com.inventario.mobile.presentation.viewmodel.QuickSearchViewModel
import com.inventario.mobile.utils.VoiceSearchManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para busca rápida de patrimônios
 * 
 * Refatorada para Clean Architecture + MVVM
 * 
 * @see Requirements 1.1, 1.2, 1.3, 1.4
 */
@AndroidEntryPoint
class QuickSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickSearchBinding
    private lateinit var adapter: PatrimonioSearchAdapter
    private lateinit var voiceSearchManager: VoiceSearchManager
    
    private val viewModel: QuickSearchViewModel by viewModels()
    
    companion object {
        private const val TAG = "QuickSearchActivity"
        const val REQUEST_FILTERS = 1001
    }
    
    // Launcher para solicitar permissão de áudio
    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceSearch()
        } else {
            Toast.makeText(
                this,
                "Permissão de áudio necessária para busca por voz",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        voiceSearchManager = VoiceSearchManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupSearchField()
        setupFilters()
        setupVoiceSearch()
        observeViewModel()
        
        // Processar intent se veio de busca por voz
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }
    
    private fun handleIntent(intent: Intent) {
        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
        val autoSearch = intent.getBooleanExtra("AUTO_SEARCH", false)
        val startVoiceSearch = intent.getBooleanExtra("START_VOICE_SEARCH", false)
        
        if (searchQuery != null && autoSearch) {
            binding.etSearch.setText(searchQuery)
            viewModel.buscar(searchQuery)
        }
        
        // Se veio do menu "Busca por Voz", iniciar busca por voz automaticamente
        if (startVoiceSearch) {
            binding.etSearch.postDelayed({
                requestAudioPermissionIfNeeded()
            }, 500) // Pequeno delay para garantir que a UI está pronta
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = PatrimonioSearchAdapter(
            onItemClick = { patrimonio ->
                navegarParaDetalhes(patrimonio)
            }
        )
        
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = adapter
    }
    
    private fun setupSearchField() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                
                // Mostrar/ocultar botão de limpar
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                // Delegar busca para ViewModel
                viewModel.buscar(query)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            viewModel.limparBusca()
        }
        
        // Focar no campo de busca
        binding.etSearch.requestFocus()
    }
    
    private fun setupFilters() {
        binding.chipColetados.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipPendentes.isChecked = false
                binding.chipDivergencias.isChecked = false
                viewModel.alterarFiltro(SearchFilter.COLETADOS)
            } else if (!binding.chipPendentes.isChecked && !binding.chipDivergencias.isChecked) {
                viewModel.alterarFiltro(SearchFilter.ALL)
            }
        }
        
        binding.chipPendentes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipColetados.isChecked = false
                binding.chipDivergencias.isChecked = false
                viewModel.alterarFiltro(SearchFilter.PENDENTES)
            } else if (!binding.chipColetados.isChecked && !binding.chipDivergencias.isChecked) {
                viewModel.alterarFiltro(SearchFilter.ALL)
            }
        }
        
        binding.chipDivergencias.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipColetados.isChecked = false
                binding.chipPendentes.isChecked = false
                viewModel.alterarFiltro(SearchFilter.DIVERGENCIAS)
            } else if (!binding.chipColetados.isChecked && !binding.chipPendentes.isChecked) {
                viewModel.alterarFiltro(SearchFilter.ALL)
            }
        }
        
        binding.chipFiltrosAvancados.setOnClickListener {
            val intent = Intent(this, FiltrosActivity::class.java)
            startActivityForResult(intent, REQUEST_FILTERS)
        }
    }
    
    private fun setupVoiceSearch() {
        binding.btnVoiceSearch.setOnClickListener {
            requestAudioPermissionIfNeeded()
        }
    }
    
    /**
     * Observa mudanças no ViewModel e atualiza a UI
     */
    private fun observeViewModel() {
        // Observar estado da busca
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is QuickSearchState.Idle -> showEmptyState()
                    is QuickSearchState.Loading -> showLoading()
                    is QuickSearchState.Success -> showResults(state)
                    is QuickSearchState.Empty -> showNoResults(state.query)
                    is QuickSearchState.Error -> showError(state.message)
                }
            }
        }
        
        // Observar última sincronização
        lifecycleScope.launch {
            viewModel.lastSyncTime.collect { lastSync ->
                if (lastSync != null) {
                    binding.layoutSyncIndicator.visibility = View.VISIBLE
                    binding.tvLastSync.text = lastSync
                } else {
                    binding.layoutSyncIndicator.visibility = View.GONE
                }
            }
        }
        
        // Observar se dados estão desatualizados
        lifecycleScope.launch {
            viewModel.isSyncOutdated.collect { isOutdated ->
                binding.cardSyncWarning.visibility = if (isOutdated) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun navegarParaDetalhes(patrimonio: PatrimonioComColeta) {
        val intent = Intent(this, PatrimonioDetailActivity::class.java).apply {
            putExtra(PatrimonioDetailActivity.EXTRA_PATRIMONIO_ID, patrimonio.id.toInt())
        }
        startActivity(intent)
    }
    
    private fun requestAudioPermissionIfNeeded() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceSearch()
            }
            
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    this,
                    "Permissão de áudio necessária para busca por voz",
                    Toast.LENGTH_LONG
                ).show()
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            
            else -> {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startVoiceSearch() {
        if (!voiceSearchManager.isAvailable()) {
            Toast.makeText(
                this,
                "Reconhecimento de voz não disponível",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        voiceSearchManager.startListening(object : VoiceSearchManager.VoiceSearchListener {
            override fun onResults(text: String) {
                binding.etSearch.setText(text)
                viewModel.buscar(text)
            }
            
            override fun onError(error: String) {
                Toast.makeText(this@QuickSearchActivity, error, Toast.LENGTH_SHORT).show()
            }
            
            override fun onReadyForSpeech() {
                Toast.makeText(this@QuickSearchActivity, "Pode falar...", Toast.LENGTH_SHORT).show()
            }
            
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(text: String) {
                binding.etSearch.setText(text)
            }
        })
    }
    
    private fun showResults(state: QuickSearchState.Success) {
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.cardSearchStats.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        
        binding.tvResultCount.text = state.resultados.size.toString()
        binding.tvSearchTime.text = "${state.tempoMs}ms"
        
        adapter.submitList(state.resultados)
    }
    
    private fun showNoResults(query: String) {
        binding.layoutEmptyState.visibility = View.GONE
        binding.cardSearchStats.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.layoutNoResults.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        
        binding.tvNoResultsMessage.text = "Nenhum resultado para \"$query\""
    }
    
    private fun showEmptyState() {
        binding.cardSearchStats.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        
        adapter.submitList(emptyList())
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showEmptyState()
    }
    
    override fun onDestroy() {
        voiceSearchManager.destroy()
        super.onDestroy()
    }
}
