package com.inventario.mobile.presentation.descricao

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityDescricaoSelectionBinding
import com.inventario.mobile.presentation.coleta.ManualCollectionActivity
import kotlinx.coroutines.launch

class DescricaoSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDescricaoSelectionBinding
    private lateinit var viewModel: DescricaoSelectionViewModel
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

        // Inicializar ViewModel
        viewModel = ViewModelProvider(
            this,
            DescricaoSelectionViewModelFactory(application)
        )[DescricaoSelectionViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupSearch()

        // Carregar descrições
        viewModel.loadDescricoes()
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

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchDescricoes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.loadDescricoes()
                }
                return true
            }
        })
    }

    private fun updateUI(state: DescricaoSelectionUiState) {
        // Atualizar lista
        adapter.submitList(state.descricoes)

        // Atualizar loading
        binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE

        // Atualizar mensagem vazia
        binding.tvEmpty.visibility = if (state.descricoes.isEmpty() && !state.isLoading) android.view.View.VISIBLE else android.view.View.GONE

        // Atualizar erro
        if (state.errorMessage != null) {
            Toast.makeText(this, state.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDescricaoSelected(descricao: DescricaoItem) {
        Log.d(TAG, "Descrição selecionada: ${descricao.descricao}")

        // Navegar para coleta manual com a descrição pré-preenchida
        val intent = Intent(this, ManualCollectionActivity::class.java)
        intent.putExtra("SALA_ID", salaId)
        intent.putExtra("SALA_NOME", salaNome)
        intent.putExtra("DESCRICAO", descricao.descricao)
        intent.putExtra("SEM_PATRIMONIO", true)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
