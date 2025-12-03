package com.inventario.mobile.presentation.coletas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.inventario.mobile.databinding.ActivityColetasBinding
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.presentation.inventario.PatrimonioAdapter
import kotlinx.coroutines.launch

class ColetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityColetasBinding
    private lateinit var viewModel: ColetasViewModel
    private lateinit var pagerAdapter: ColetasPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupViewPager()
        setupObservers()

        // Carregar dados iniciais
        viewModel.loadColetas()
    }

    private fun setupViewModel() {
        val apiService = NetworkModule.getApiService(this)
        val repository = InventarioRepository.getInstance(this, apiService)
        
        // Passar ApiService para buscar coletas diretamente do servidor
        val factory = ColetasViewModelFactory(repository, apiService)
        viewModel = ViewModelProvider(this, factory)[ColetasViewModel::class.java]
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Coletas"
            setDisplayHomeAsUpEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Adicionar menu de filtro
        binding.toolbar.inflateMenu(com.inventario.mobile.R.menu.menu_coletas)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                com.inventario.mobile.R.id.action_refresh -> {
                    viewModel.loadColetas()
                    true
                }
                com.inventario.mobile.R.id.filter_todos -> {
                    aplicarFiltro(FiltroColeta.TODOS, "Mostrando todas as coletas")
                    menuItem.isChecked = true
                    true
                }
                com.inventario.mobile.R.id.filter_com_etiqueta -> {
                    aplicarFiltro(FiltroColeta.COM_ETIQUETA, "Mostrando coletas com etiqueta")
                    menuItem.isChecked = true
                    true
                }
                com.inventario.mobile.R.id.filter_sem_etiqueta -> {
                    aplicarFiltro(FiltroColeta.SEM_ETIQUETA, "Mostrando itens sem etiqueta")
                    menuItem.isChecked = true
                    true
                }
                com.inventario.mobile.R.id.action_clear_filters -> {
                    aplicarFiltro(FiltroColeta.TODOS, "Filtros limpos")
                    true
                }
                else -> false
            }
        }
    }
    
    private fun aplicarFiltro(filtro: FiltroColeta, mensagem: String) {
        viewModel.aplicarFiltro(filtro)
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        
        // Atualizar a UI com os dados filtrados
        val coletasFiltradas = viewModel.getColetasFiltradas()
        pagerAdapter.updateData(coletasFiltradas, viewModel.uiState.value.patrimoniosPendentes)
    }

    private fun setupViewPager() {
        pagerAdapter = ColetasPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Coletados"
                1 -> "Pendentes"
                else -> ""
            }
        }.attach()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ColetasUiState) {
        // Atualizar contadores - v2.7: incluir itens sem etiqueta no total
        val totalColetadosComSemEtiqueta = state.totalColetados + state.totalSemEtiqueta
        binding.tvColetadosCount.text = totalColetadosComSemEtiqueta.toString()
        binding.tvPendentesCount.text = state.totalPendentes.toString()

        // v2.7: Usar dados filtrados baseado no filtro ativo
        val coletasFiltradas = viewModel.getColetasFiltradas()
        pagerAdapter.updateData(coletasFiltradas, state.patrimoniosPendentes)

        // Mostrar/ocultar empty view
        val hasData = coletasFiltradas.isNotEmpty() || state.patrimoniosPendentes.isNotEmpty()
        binding.viewPager.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.emptyView.visibility = if (hasData) View.GONE else View.VISIBLE

        // v2.7: Mostrar indicador de filtro ativo
        val filtroInfo = when (state.filtroAtivo) {
            FiltroColeta.TODOS -> ""
            FiltroColeta.COM_ETIQUETA -> " (com etiqueta)"
            FiltroColeta.SEM_ETIQUETA -> " (sem etiqueta: ${state.totalSemEtiqueta})"
        }
        supportActionBar?.subtitle = if (filtroInfo.isNotEmpty()) filtroInfo else null

        // Mostrar erro se houver
        state.errorMessage?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

// Adapter para o ViewPager2
class ColetasPagerAdapter(
    private val activity: ColetasActivity
) : androidx.recyclerview.widget.RecyclerView.Adapter<ColetasPagerAdapter.PageViewHolder>() {

    private var patrimoniosColetados: List<Patrimonio> = emptyList()
    private var patrimoniosPendentes: List<Patrimonio> = emptyList()

    fun updateData(coletados: List<Patrimonio>, pendentes: List<Patrimonio>) {
        patrimoniosColetados = coletados
        patrimoniosPendentes = pendentes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PageViewHolder {
        val recyclerView = RecyclerView(parent.context).apply {
            layoutManager = LinearLayoutManager(parent.context)
            clipToPadding = false
            setPadding(16, 16, 16, 16)
        }
        return PageViewHolder(recyclerView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val data = when (position) {
            0 -> patrimoniosColetados
            1 -> patrimoniosPendentes
            else -> emptyList()
        }

        val adapter = PatrimonioAdapter { patrimonio ->
            Toast.makeText(activity, "Patrimônio: ${patrimonio.numeroPatrimonio}", Toast.LENGTH_SHORT).show()
        }
        
        holder.recyclerView.adapter = adapter
        adapter.submitList(data)
    }

    override fun getItemCount(): Int = 2

    class PageViewHolder(val recyclerView: RecyclerView) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(recyclerView)
}
