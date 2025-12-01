package com.inventario.mobile.presentation.inventario

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityInventarioTabsBinding
import com.inventario.mobile.utils.NavigationHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de Inventário com duas abas:
 * - Por Responsável: Filtra patrimônios por responsável
 * - Por Sala: Filtra patrimônios por sala com estatísticas
 */
@AndroidEntryPoint
class InventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioTabsBinding
    private lateinit var pagerAdapter: InventarioPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("InventarioActivity", "Iniciando onCreate com TabLayout...")
            
            binding = ActivityInventarioTabsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Adicionar indicador de modo offline
            com.inventario.mobile.ui.components.OfflineIndicator.setup(this)

            android.util.Log.d("InventarioActivity", "Layout com abas inflado com sucesso")
            
            setupToolbar()
            setupViewPager()
            
            android.util.Log.d("InventarioActivity", "onCreate concluído com sucesso")
            
        } catch (e: Exception) {
            android.util.Log.e("InventarioActivity", "Erro durante onCreate", e)
            Toast.makeText(this, "Erro ao inicializar a tela: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Inventário"
            setDisplayHomeAsUpEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            NavigationHelper.goBack(this)
        }
    }

    private fun setupViewPager() {
        android.util.Log.d("InventarioActivity", "Configurando ViewPager com abas...")
        
        // Criar adapter
        pagerAdapter = InventarioPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Conectar TabLayout com ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getTabTitle(position)
        }.attach()
        
        android.util.Log.d("InventarioActivity", "ViewPager configurado com ${pagerAdapter.itemCount} abas")
    }

    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventario_tabs, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // Notificar fragments para atualizar
                Toast.makeText(this, "Atualizando dados...", Toast.LENGTH_SHORT).show()
                // Os fragments têm seus próprios mecanismos de refresh
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
