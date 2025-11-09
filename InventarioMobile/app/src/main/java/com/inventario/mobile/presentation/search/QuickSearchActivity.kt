package com.inventario.mobile.presentation.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityQuickSearchBinding
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.presentation.adapter.PatrimonioAdapter
import com.inventario.mobile.presentation.filtros.FiltrosActivity
import com.inventario.mobile.utils.VoiceSearchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickSearchBinding
    private lateinit var adapter: PatrimonioAdapter
    private lateinit var database: InventarioDatabase
    private lateinit var voiceSearchManager: VoiceSearchManager
    
    private var searchJob: Job? = null
    private var currentFilter: SearchFilter = SearchFilter.ALL
    
    companion object {
        private const val TAG = "QuickSearchActivity"
        private const val SEARCH_DELAY_MS = 300L
        const val REQUEST_FILTERS = 1001
    }
    
    enum class SearchFilter {
        ALL, COLETADOS, PENDENTES, DIVERGENCIAS
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
        
        database = InventarioDatabase.getDatabase(this)
        voiceSearchManager = VoiceSearchManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupSearchField()
        setupFilters()
        setupVoiceSearch()
        
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
        
        if (searchQuery != null && autoSearch) {
            binding.etSearch.setText(searchQuery)
            performSearch(searchQuery)
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
        adapter = PatrimonioAdapter(
            onItemClick = { patrimonio ->
                // TODO: Abrir detalhes do patrimônio
                Toast.makeText(this, "Patrimônio: ${patrimonio.numeroPatrimonio}", Toast.LENGTH_SHORT).show()
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
                
                // Cancelar busca anterior
                searchJob?.cancel()
                
                // Agendar nova busca com delay
                if (query.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(SEARCH_DELAY_MS)
                        performSearch(query)
                    }
                } else {
                    showEmptyState()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
        }
        
        // Focar no campo de busca
        binding.etSearch.requestFocus()
    }
    
    private fun setupFilters() {
        binding.chipColetados.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = SearchFilter.COLETADOS
                binding.chipPendentes.isChecked = false
                binding.chipDivergencias.isChecked = false
                performSearch(binding.etSearch.text.toString())
            } else if (currentFilter == SearchFilter.COLETADOS) {
                currentFilter = SearchFilter.ALL
                performSearch(binding.etSearch.text.toString())
            }
        }
        
        binding.chipPendentes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = SearchFilter.PENDENTES
                binding.chipColetados.isChecked = false
                binding.chipDivergencias.isChecked = false
                performSearch(binding.etSearch.text.toString())
            } else if (currentFilter == SearchFilter.PENDENTES) {
                currentFilter = SearchFilter.ALL
                performSearch(binding.etSearch.text.toString())
            }
        }
        
        binding.chipDivergencias.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = SearchFilter.DIVERGENCIAS
                binding.chipColetados.isChecked = false
                binding.chipPendentes.isChecked = false
                performSearch(binding.etSearch.text.toString())
            } else if (currentFilter == SearchFilter.DIVERGENCIAS) {
                currentFilter = SearchFilter.ALL
                performSearch(binding.etSearch.text.toString())
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
                performSearch(text)
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
    
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            showEmptyState()
            return
        }
        
        showLoading()
        
        val startTime = System.currentTimeMillis()
        
        lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    searchPatrimonios(query)
                }
                
                val searchTime = System.currentTimeMillis() - startTime
                
                withContext(Dispatchers.Main) {
                    hideLoading()
                    displayResults(results, searchTime)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar patrimônios", e)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    showError("Erro ao buscar: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun searchPatrimonios(query: String): List<Patrimonio> {
        val dao = database.patrimonioDao()
        
        // Buscar todos os patrimônios e filtrar localmente
        val allPatrimonios = when (currentFilter) {
            SearchFilter.ALL -> dao.getAllPatrimoniosList()
            SearchFilter.COLETADOS -> emptyList() // TODO: Implementar getPatrimoniosColetados
            SearchFilter.PENDENTES -> emptyList() // TODO: Implementar getPatrimoniosNaoColetados
            SearchFilter.DIVERGENCIAS -> dao.getAllPatrimoniosList() // TODO: implementar filtro de divergências
        }
        
        // Converter PatrimonioEntity para Patrimonio e filtrar por query
        return allPatrimonios.map { entity ->
            Patrimonio(
                id = entity.id.toLong(),
                numeroPatrimonio = entity.numero,
                descricao = entity.descricao,
                marca = "", // TODO: Adicionar campo ao Entity
                modelo = "", // TODO: Adicionar campo ao Entity
                numeroSerie = "", // TODO: Adicionar campo ao Entity
                estado = entity.status,
                valor = 0.0, // TODO: Adicionar campo ao Entity
                salaId = entity.idSala?.toLong(),
                salaNome = entity.nomeSala,
                qrCode = "", // TODO: Adicionar campo ao Entity
                coletado = entity.coletado,
                sincronizado = false, // TODO: Adicionar campo ao Entity
                servidorId = null // TODO: Adicionar campo ao Entity
            )
        }.filter { patrimonio ->
            patrimonio.numeroPatrimonio.contains(query, ignoreCase = true) ||
            patrimonio.descricao.contains(query, ignoreCase = true) ||
            patrimonio.salaNome?.contains(query, ignoreCase = true) == true
        }
    }
    
    private fun displayResults(results: List<Patrimonio>, searchTime: Long) {
        if (results.isEmpty()) {
            showNoResults()
        } else {
            showResults(results, searchTime)
        }
    }
    
    private fun showResults(results: List<Patrimonio>, searchTime: Long) {
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.cardSearchStats.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.VISIBLE
        
        binding.tvResultCount.text = results.size.toString()
        binding.tvSearchTime.text = "${searchTime}ms"
        
        adapter.submitList(results)
    }
    
    private fun showNoResults() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.cardSearchStats.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.layoutNoResults.visibility = View.VISIBLE
        
        val query = binding.etSearch.text.toString()
        binding.tvNoResultsMessage.text = "Nenhum resultado para \"$query\""
    }
    
    private fun showEmptyState() {
        binding.cardSearchStats.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        
        adapter.submitList(emptyList())
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showEmptyState()
    }
    
    override fun onDestroy() {
        voiceSearchManager.destroy()
        super.onDestroy()
    }
}
