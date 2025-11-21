package com.inventario.mobile.presentation.statistics

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity para exibir estatísticas e relatórios detalhados
 * Organizada em tabs: Visão Geral, Gráficos, Rankings, Exportar
 */
@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var pagerAdapter: StatisticsPagerAdapter

    companion object {
        private const val TAG = "StatisticsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate: Iniciando StatisticsActivity")
        
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        
        Log.d(TAG, "onCreate: StatisticsActivity inicializada")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Estatísticas e Relatórios"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupViewPager() {
        // Obter ID do inventário (pode vir de Intent ou usar inventário ativo)
        val idInventario = intent.getIntExtra("INVENTARIO_ID", 0)
        
        pagerAdapter = StatisticsPagerAdapter(this, idInventario)
        binding.viewPager.adapter = pagerAdapter
        
        // Configurar tabs
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Visão Geral"
                1 -> "Gráficos"
                2 -> "Rankings"
                3 -> "Exportar"
                else -> "Tab $position"
            }
            
            tab.icon = when (position) {
                0 -> getDrawable(R.drawable.ic_dashboard)
                1 -> getDrawable(R.drawable.ic_trending_up)
                2 -> getDrawable(R.drawable.ic_list)
                3 -> getDrawable(R.drawable.ic_download)
                else -> null
            }
        }.attach()
        
        Log.d(TAG, "ViewPager configurado com ${pagerAdapter.itemCount} tabs")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
