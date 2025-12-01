package com.inventario.mobile.presentation.inventario

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityInventarioTabsBinding
import com.inventario.mobile.ui.components.OfflineIndicator
import com.inventario.mobile.utils.NavigationHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de Inventário com TabLayout.
 * Contém duas abas: "Por Responsável" e "Por Sala".
 */
@AndroidEntryPoint
class InventarioTabsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityInventarioTabsBinding
    private lateinit var pagerAdapter: InventarioPagerAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityInventarioTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        setupOfflineIndicator()
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
        pagerAdapter = InventarioPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Conectar TabLayout com ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getTabTitle(position)
        }.attach()
        
        // Preservar estado das abas
        binding.viewPager.offscreenPageLimit = 2
    }
    
    private fun setupOfflineIndicator() {
        OfflineIndicator.setup(this)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventario_tabs, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // Recarregar dados do fragment atual
                refreshCurrentFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun refreshCurrentFragment() {
        val currentPosition = binding.viewPager.currentItem
        val fragment = supportFragmentManager.findFragmentByTag("f$currentPosition")
        
        when (fragment) {
            is InventarioPorResponsavelFragment -> {
                // Recarregar responsáveis
            }
            is InventarioPorSalaFragment -> {
                // Recarregar salas
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
}
