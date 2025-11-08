package com.inventario.mobile.presentation.sala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivitySalaSelectionBinding
import com.inventario.mobile.presentation.adapter.SalaAdapter
import com.inventario.mobile.ui.coleta.ColetaActivity
// import com.inventario.mobile.data.repository.SalaRepositoryImpl
// import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.data.remote.api.ApiService
import com.inventario.mobile.utils.NavigationHelper
import kotlinx.coroutines.launch

class SalaSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaSelectionBinding
    private lateinit var viewModel: SalaSelectionViewModel
    private lateinit var salaAdapter: SalaAdapter

    private var coletaTipo: String = "QRCODE" // QRCODE ou MANUAL

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
            
            // Inicializar ViewModel
            viewModel = ViewModelProvider(
                this,
                SalaSelectionViewModelFactory(application)
            )[SalaSelectionViewModel::class.java]
            
            setupToolbar()
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

    private fun setupRecyclerView() {
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
            layoutManager = LinearLayoutManager(this@SalaSelectionActivity)
            adapter = salaAdapter
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
        // Atualizar lista de salas
        salaAdapter.submitList(state.salas)
        
        // Atualizar estado de loading
        binding.swipeRefreshLayout.isRefreshing = state.isLoading
        
        // Mostrar/ocultar mensagem de lista vazia
        if (state.salas.isEmpty() && !state.isLoading) {
            binding.textViewEmpty.visibility = android.view.View.VISIBLE
            binding.recyclerViewSalas.visibility = android.view.View.GONE
        } else {
            binding.textViewEmpty.visibility = android.view.View.GONE
            binding.recyclerViewSalas.visibility = android.view.View.VISIBLE
        }
        
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
}