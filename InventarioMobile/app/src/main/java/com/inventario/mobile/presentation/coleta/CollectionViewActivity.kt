package com.inventario.mobile.presentation.coleta

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.databinding.ActivityCollectionViewBinding
import com.inventario.mobile.presentation.state.CollectionViewState
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity para visualização de coletas
 * 
 * Migrada para Clean Architecture + MVVM
 * - Usa @AndroidEntryPoint para injeção Hilt
 * - Observa CollectionViewState (sealed class)
 * - Delega lógica para CollectionViewViewModelClean
 * - v2.8: Menu de contexto para coletas pendentes (reenviar/excluir)
 */
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CollectionViewActivity"
    }

    private lateinit var binding: ActivityCollectionViewBinding

    // ViewModel Clean Architecture (injetado via Hilt)
    private val viewModel: CollectionViewViewModelClean by viewModels()

    // Adapter com callback para long click
    private val adapter = CollectionAdapter(
        onItemLongClick = { coleta ->
            mostrarMenuColetaPendente(coleta)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: iniciando CollectionViewActivity (Clean Architecture)")
        binding = ActivityCollectionViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Adicionar indicador de modo offline
        com.inventario.mobile.ui.components.OfflineIndicator.setup(this)

        setupRecyclerView()
        setupFilters()
        setupBackButton()
        observeViewModel()

        Log.d(TAG, "onCreate: carregando coletas via Use Case")
        viewModel.carregarColetas()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: configurando RecyclerView")
        binding.recyclerViewColetas.apply {
            layoutManager = LinearLayoutManager(this@CollectionViewActivity)
            adapter = this@CollectionViewActivity.adapter
        }
    }
    
    private fun setupBackButton() {
        Log.d(TAG, "setupBackButton: configurando botão de retorno ao Dashboard")
        binding.fabBackToDashboard.setOnClickListener {
            Log.d(TAG, "fabBackToDashboard: voltando para MainActivity")
            // Voltar para a MainActivity (Dashboard)
            val intent = android.content.Intent(this, com.inventario.mobile.presentation.main.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupFilters() {
        // Filtro de Usuário
        binding.chipGroupUser.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAllUsers.id -> {
                        viewModel.filtrarPorUsuario(CollectionViewViewModelClean.FiltroUsuario.TODAS)
                    }
                    binding.chipMyCollections.id -> {
                        viewModel.filtrarPorUsuario(CollectionViewViewModelClean.FiltroUsuario.MINHAS)
                    }
                }
            }
        }
        
        // Filtro de Status
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    binding.chipAll.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.TODOS)
                    }
                    binding.chipSynced.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.SINCRONIZADOS)
                    }
                    binding.chipPending.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.PENDENTES)
                    }
                    binding.chipSemEtiqueta.id -> {
                        viewModel.filtrarPorStatus(CollectionViewViewModelClean.FiltroStatus.SEM_ETIQUETA)
                    }
                }
            }
        }
    }
    
    private fun setupSalaSpinner(salas: List<String>) {
        val salaOptions = mutableListOf("Todas as Salas")
        salaOptions.addAll(salas)
        
        val spinnerAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            salaOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerSalas.adapter = spinnerAdapter
        binding.spinnerSalas.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // "Todas as Salas" selecionado
                    viewModel.filtrarPorSala(null)
                } else {
                    // Sala específica selecionada
                    val sala = salas[position - 1]
                    viewModel.filtrarPorSala(sala)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                viewModel.filtrarPorSala(null)
            }
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: iniciando observação do ViewModel (Clean Architecture)")
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is CollectionViewState.Idle -> {
                        Log.d(TAG, "Estado: Idle")
                        hideLoading()
                    }
                    
                    is CollectionViewState.Loading -> {
                        Log.d(TAG, "Estado: Loading")
                        showLoading()
                    }
                    
                    is CollectionViewState.Success -> {
                        Log.d(TAG, "Estado: Success - ${state.filteredColetas.size} coletas filtradas")
                        hideLoading()
                        updateUI(state)
                    }
                    
                    is CollectionViewState.Error -> {
                        Log.e(TAG, "Estado: Error - ${state.message}")
                        hideLoading()
                        showError(state.message)
                    }
                    
                    is CollectionViewState.ColetaReenviada -> {
                        Log.d(TAG, "Estado: ColetaReenviada - ${state.message}")
                        hideLoading()
                        android.widget.Toast.makeText(this@CollectionViewActivity, state.message, android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.carregarColetas()
                    }
                    
                    is CollectionViewState.ColetaExcluida -> {
                        Log.d(TAG, "Estado: ColetaExcluida - ${state.message}")
                        hideLoading()
                        android.widget.Toast.makeText(this@CollectionViewActivity, state.message, android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.carregarColetas()
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun updateUI(state: CollectionViewState.Success) {
        // Atualizar lista (já está em data.model.Coleta)
        Log.d(TAG, "updateUI: submetendo ${state.filteredColetas.size} coletas para o adapter")
        adapter.submitList(state.filteredColetas)
        
        // Atualizar estatísticas
        binding.tvTotalColetas.text = state.totalColetas.toString()
        binding.tvPendingSync.text = state.pendentes.toString()
        
        // Configurar spinner de salas (apenas uma vez)
        if (state.salas.isNotEmpty() && binding.spinnerSalas.adapter == null) {
            Log.d(TAG, "updateUI: configurando spinner com ${state.salas.size} salas")
            setupSalaSpinner(state.salas)
        }
        
        // Estado vazio
        val showEmptyState = state.filteredColetas.isEmpty()
        Log.d(TAG, "updateUI: showEmptyState=$showEmptyState")
        binding.layoutEmptyState.visibility = 
            if (showEmptyState) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        // TODO: Mostrar dialog ou toast com erro
        Log.e(TAG, "showError: $message")
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    /**
     * v2.8: Mostra menu de contexto para coletas pendentes
     * Permite reenviar ou excluir a coleta
     */
    private fun mostrarMenuColetaPendente(coleta: Coleta) {
        Log.d(TAG, "mostrarMenuColetaPendente: coleta=${coleta.id}, sincronizado=${coleta.sincronizado}")
        
        // Só mostrar menu para coletas pendentes (não sincronizadas)
        if (coleta.sincronizado) {
            Log.d(TAG, "Coleta já sincronizada, ignorando menu")
            android.widget.Toast.makeText(
                this,
                "Esta coleta já está sincronizada",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Criar dialog com opções
        // NOTA: setMessage() e setItems() não funcionam juntos, usar apenas setItems()
        val patrimonioInfo = coleta.numeroPatrimonio ?: "Patrimônio ${coleta.patrimonioId}"
        val opcoes = arrayOf("🔄 Reenviar Coleta", "🗑️ Excluir Coleta")
        
        AlertDialog.Builder(this)
            .setTitle("Coleta Pendente\n$patrimonioInfo")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> confirmarReenvio(coleta)
                    1 -> confirmarExclusao(coleta)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * v2.8: Confirma reenvio de coleta pendente
     */
    private fun confirmarReenvio(coleta: Coleta) {
        Log.d(TAG, "confirmarReenvio: coleta=${coleta.id}")
        
        AlertDialog.Builder(this)
            .setTitle("Reenviar Coleta")
            .setMessage("Deseja tentar sincronizar a coleta do patrimônio ${coleta.numeroPatrimonio ?: coleta.patrimonioId} novamente?")
            .setPositiveButton("Reenviar") { _, _ ->
                val coletaId = coleta.id?.toLong() ?: 0L
                if (coletaId > 0) {
                    Log.d(TAG, "Reenviando coleta $coletaId")
                    viewModel.reenviarColeta(coletaId)
                } else {
                    Log.e(TAG, "ID da coleta inválido: ${coleta.id}")
                    android.widget.Toast.makeText(
                        this,
                        "Erro: ID da coleta inválido",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * v2.8: Confirma exclusão de coleta pendente
     */
    private fun confirmarExclusao(coleta: Coleta) {
        Log.d(TAG, "confirmarExclusao: coleta=${coleta.id}")
        
        AlertDialog.Builder(this)
            .setTitle("⚠️ Excluir Coleta")
            .setMessage("Tem certeza que deseja EXCLUIR a coleta do patrimônio ${coleta.numeroPatrimonio ?: coleta.patrimonioId}?\n\nEsta ação não pode ser desfeita!")
            .setPositiveButton("Excluir") { _, _ ->
                val coletaId = coleta.id?.toLong() ?: 0L
                if (coletaId > 0) {
                    Log.d(TAG, "Excluindo coleta $coletaId")
                    viewModel.excluirColetaPendente(coletaId)
                } else {
                    Log.e(TAG, "ID da coleta inválido: ${coleta.id}")
                    android.widget.Toast.makeText(
                        this,
                        "Erro: ID da coleta inválido",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}