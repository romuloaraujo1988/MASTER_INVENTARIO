package com.inventario.mobile.presentation.historico

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityHistoricoScansBinding
import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.presentation.search.QuickSearchActivity
import com.inventario.mobile.utils.NavigationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para exibir histórico de patrimônios escaneados/consultados
 * 
 * @since v2.11.0
 */
@AndroidEntryPoint
class HistoricoScansActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoricoScansBinding
    private val viewModel: HistoricoScansViewModel by viewModels()
    private lateinit var adapter: HistoricoScansAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricoScansBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupChipFilters()
        setupSwipeRefresh()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Histórico de Scans"
        }
    }
    
    private fun setupRecyclerView() {
        adapter = HistoricoScansAdapter { historico ->
            onItemClick(historico)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoricoScansActivity)
            adapter = this@HistoricoScansActivity.adapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupChipFilters() {
        binding.chipTodos.setOnClickListener {
            viewModel.setFiltro(FiltroHistorico.TODOS)
            updateChipSelection(FiltroHistorico.TODOS)
        }
        
        binding.chipColetados.setOnClickListener {
            viewModel.setFiltro(FiltroHistorico.COLETADOS)
            updateChipSelection(FiltroHistorico.COLETADOS)
        }
        
        binding.chipConsultas.setOnClickListener {
            viewModel.setFiltro(FiltroHistorico.CONSULTAS)
            updateChipSelection(FiltroHistorico.CONSULTAS)
        }
    }
    
    private fun updateChipSelection(filtro: FiltroHistorico) {
        binding.chipTodos.isChecked = filtro == FiltroHistorico.TODOS
        binding.chipColetados.isChecked = filtro == FiltroHistorico.COLETADOS
        binding.chipConsultas.isChecked = filtro == FiltroHistorico.CONSULTAS
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.success,
            R.color.info
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.carregarHistorico()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.swipeRefresh.isRefreshing = false
                
                when (state) {
                    is HistoricoScansState.Idle -> {
                        // Estado inicial
                    }
                    
                    is HistoricoScansState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    
                    is HistoricoScansState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        
                        adapter.submitList(state.historico)
                        
                        // Atualizar estatísticas
                        state.estatisticas?.let { stats ->
                            binding.tvScansHoje.text = stats.totalScansHoje.toString()
                            binding.tvColetasHoje.text = stats.totalColetasHoje.toString()
                            binding.tvTaxaConversao.text = String.format("%.0f%%", stats.taxaConversao)
                        }
                    }
                    
                    is HistoricoScansState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                    
                    is HistoricoScansState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@HistoricoScansActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        
        // Observar estatísticas separadamente
        lifecycleScope.launch {
            viewModel.estatisticas.collect { stats ->
                stats?.let {
                    binding.tvScansHoje.text = it.totalScansHoje.toString()
                    binding.tvColetasHoje.text = it.totalColetasHoje.toString()
                    binding.tvTaxaConversao.text = String.format("%.0f%%", it.taxaConversao)
                }
            }
        }
    }
    
    private fun onItemClick(historico: HistoricoScan) {
        // Abrir busca rápida com o número do patrimônio
        val intent = Intent(this, QuickSearchActivity::class.java).apply {
            putExtra("SEARCH_QUERY", historico.numeroPatrimonio)
            putExtra("AUTO_SEARCH", true)
        }
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_historico_scans, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavigationHelper.goBack(this)
                true
            }
            R.id.action_limpar -> {
                confirmarLimpeza()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun confirmarLimpeza() {
        // Dialog com 3 opções para dar flexibilidade ao usuário:
        //  - Apenas histórico (o que a versão anterior fazia)
        //  - Apenas contador da sessão e último coletado (sem mexer no histórico)
        //  - Tudo (combina as duas)
        val opcoes = arrayOf(
            "Apenas histórico de scans",
            "Apenas contador e último coletado",
            "Tudo (histórico + contador)"
        )

        AlertDialog.Builder(this)
            .setTitle("Limpar")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> {
                        confirmarAcao(
                            titulo = "Limpar Histórico",
                            mensagem = "Deseja limpar todo o histórico de scans?\n\n" +
                                "O contador e o último coletado NÃO serão afetados.\n\n" +
                                "Esta ação não pode ser desfeita."
                        ) {
                            viewModel.limparHistorico()
                            Toast.makeText(this, "Histórico limpo", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        confirmarAcao(
                            titulo = "Zerar Contador",
                            mensagem = "Deseja zerar o contador da sessão e o último patrimônio coletado?\n\n" +
                                "O histórico de scans NÃO será afetado."
                        ) {
                            viewModel.resetarContadorSessao()
                            Toast.makeText(this, "Contador zerado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        confirmarAcao(
                            titulo = "Limpar Tudo",
                            mensagem = "Deseja limpar o histórico de scans, zerar o contador e remover o último coletado?\n\n" +
                                "Esta ação não pode ser desfeita."
                        ) {
                            viewModel.limparTudo()
                            Toast.makeText(this, "Histórico e contador limpos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarAcao(titulo: String, mensagem: String, acao: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensagem)
            .setPositiveButton("Limpar") { _, _ -> acao() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.atualizarEstatisticas()
    }
}
