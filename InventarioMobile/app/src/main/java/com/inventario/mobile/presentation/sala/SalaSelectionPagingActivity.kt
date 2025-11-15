package com.inventario.mobile.presentation.sala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.ActivitySalaSelectionPagingBinding
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.utils.NavigationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity para seleção de sala com paginação e busca
 * 
 * Features:
 * - Paginação com scroll infinito
 * - Busca em tempo real
 * - Loading states
 * - Retry em caso de erro
 * - SwipeRefresh
 */
@AndroidEntryPoint
class SalaSelectionPagingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaSelectionPagingBinding
    private val viewModel: SalaViewModelPaging by viewModels()
    
    private lateinit var salaPagingAdapter: SalaPagingAdapter
    private lateinit var loadStateAdapter: SalaLoadStateAdapter
    
    private var coletaTipo: String = "QRCODE"

    companion object {
        private const val TAG = "SalaSelectionPaging"
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
        const val EXTRA_COLETA_TIPO = "COLETA_TIPO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando")
        
        binding = ActivitySalaSelectionPagingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        coletaTipo = intent.getStringExtra(EXTRA_COLETA_TIPO) ?: "QRCODE"
        Log.d(TAG, "Tipo de coleta: $coletaTipo")
        
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = when (coletaTipo) {
                "MANUAL" -> "Selecionar Sala - Coleta Manual"
                "DESCRICAO" -> "Selecionar Sala - Por Descrição"
                else -> "Selecionar Sala - QR Code"
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        // Criar adapter principal
        salaPagingAdapter = SalaPagingAdapter { sala ->
            onSalaSelected(sala)
        }
        
        // Criar adapter de loading/erro
        loadStateAdapter = SalaLoadStateAdapter {
            salaPagingAdapter.retry()
        }
        
        // Configurar RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SalaSelectionPagingActivity)
            adapter = salaPagingAdapter.withLoadStateFooter(loadStateAdapter)
        }
        
        // Observar load states
        lifecycleScope.launch {
            salaPagingAdapter.loadStateFlow.collectLatest { loadStates ->
                // Exibir loading inicial
                binding.progressBar.isVisible = loadStates.refresh is LoadState.Loading
                
                // Exibir empty state
                val isEmpty = loadStates.refresh is LoadState.NotLoading 
                    && salaPagingAdapter.itemCount == 0
                binding.tvEmpty.isVisible = isEmpty
                binding.recyclerView.isVisible = !isEmpty
                
                // Exibir erro
                val errorState = loadStates.refresh as? LoadState.Error
                errorState?.let {
                    Toast.makeText(
                        this@SalaSelectionPagingActivity,
                        "Erro: ${it.error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                // Parar SwipeRefresh
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    Log.d(TAG, "Busca submetida: $it")
                    viewModel.setSearchQuery(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "Texto mudou: $newText")
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
        
        // Limpar busca ao clicar no X
        binding.searchView.setOnCloseListener {
            viewModel.clearSearch()
            false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh: Recarregando")
            salaPagingAdapter.refresh()
        }
    }

    private fun setupObservers() {
        // Observar PagingData
        lifecycleScope.launch {
            viewModel.salaPagingData.collectLatest { pagingData ->
                salaPagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun onSalaSelected(sala: Sala) {
        Log.d(TAG, "Sala selecionada: ${sala.nome} (tipo: $coletaTipo)")
        
        when (coletaTipo) {
            "MANUAL" -> {
                val intent = Intent(
                    this,
                    com.inventario.mobile.presentation.coleta.ManualCollectionActivity::class.java
                ).apply {
                    putExtra(EXTRA_SALA_ID, sala.id.toLong())
                    putExtra(EXTRA_SALA_NOME, sala.nome)
                }
                startActivity(intent)
                finish()
            }
            "DESCRICAO" -> {
                val intent = Intent(
                    this,
                    com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity::class.java
                ).apply {
                    putExtra(
                        com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity.EXTRA_SALA_ID,
                        sala.id.toLong()
                    )
                    putExtra(
                        com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity.EXTRA_SALA_NOME,
                        sala.nome
                    )
                }
                startActivity(intent)
                finish()
            }
            else -> {
                val intent = Intent(
                    this,
                    com.inventario.mobile.presentation.scanner.ScannerActivity::class.java
                ).apply {
                    putExtra(
                        com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_SALA_ID,
                        sala.id
                    )
                    putExtra(
                        com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_SALA_NOME,
                        sala.nome
                    )
                    putExtra(
                        com.inventario.mobile.presentation.scanner.ScannerActivity.EXTRA_ALLOW_COLLECTION,
                        true
                    )
                }
                startActivity(intent)
                finish()
            }
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
