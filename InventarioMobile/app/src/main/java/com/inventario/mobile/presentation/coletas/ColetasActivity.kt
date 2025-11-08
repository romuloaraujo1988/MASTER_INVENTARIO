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
        
        val factory = ColetasViewModelFactory(repository)
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
        // Atualizar contadores
        binding.tvColetadosCount.text = state.totalColetados.toString()
        binding.tvPendentesCount.text = state.totalPendentes.toString()

        // Atualizar dados do adapter
        pagerAdapter.updateData(state.patrimoniosColetados, state.patrimoniosPendentes)

        // Mostrar/ocultar empty view
        val hasData = state.patrimoniosColetados.isNotEmpty() || state.patrimoniosPendentes.isNotEmpty()
        binding.viewPager.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.emptyView.visibility = if (hasData) View.GONE else View.VISIBLE

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