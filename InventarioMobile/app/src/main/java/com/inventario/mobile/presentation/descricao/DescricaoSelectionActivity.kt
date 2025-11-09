package com.inventario.mobile.presentation.descricao

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityDescricaoSelectionBinding
import com.inventario.mobile.presentation.coleta.ManualCollectionActivity
import com.inventario.mobile.presentation.state.DescricaoState
import com.inventario.mobile.utils.FeatureFlags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity para seleção de descrição (coleta sem etiqueta)
 * Clean Architecture + MVVM + Hilt
 */
@AndroidEntryPoint
class DescricaoSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDescricaoSelectionBinding
    
    // ViewModel injetado via Hilt
    private val viewModel: DescricaoSelectionViewModelClean by viewModels()
    
    private lateinit var adapter: DescricaoAdapter

    private var salaId: Long = 0
    private var salaNome: String = ""

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
        salaId = intent.getLongExtra(EXTRA_SALA_ID, 0)
        salaNome = intent.getStringExtra(EXTRA_SALA_NOME) ?: ""

        Log.d(TAG, "Sala selecionada: ID=$salaId, Nome=$salaNome")
        
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
                if (newText.isNullOrEmpty()) {
                    loadDescricoes()
                }
                return true
            }
        })
    }
    
    /**
     * Busca descrições
     */
    private fun searchDescricoes(query: String) {
        // TODO: Implementar busca no Use Case se necessário
        viewModel.carregarDescricoes()
    }

    /**
     * Atualiza lista de descrições
     */
    private fun updateDescricoes(descricoes: List<String>) {
        // Converter para formato do adapter (temporário)
        // TODO: Atualizar adapter para usar List<String> diretamente
        adapter.submitList(descricoes.map { it })
        
        // Atualizar mensagem vazia
        binding.tvEmpty.visibility = if (descricoes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun onDescricaoSelected(descricao: String) {
        Log.d(TAG, "Descrição selecionada: $descricao")

        // Navegar para coleta manual com a descrição pré-preenchida
        val intent = Intent(this, ManualCollectionActivity::class.java)
        intent.putExtra("SALA_ID", salaId)
        intent.putExtra("SALA_NOME", salaNome)
        intent.putExtra("DESCRICAO", descricao)
        intent.putExtra("SEM_PATRIMONIO", true)
        startActivity(intent)
        finish()
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
