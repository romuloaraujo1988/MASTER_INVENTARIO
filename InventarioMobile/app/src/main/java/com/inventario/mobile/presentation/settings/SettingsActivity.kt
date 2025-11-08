package com.inventario.mobile.presentation.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.inventario.mobile.data.repository.InventarioRepository
import com.inventario.mobile.databinding.ActivitySettingsBinding
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.NavigationHelper

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        
        // Inicializar InventarioRepository
        val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(this)
        val inventarioRepository = InventarioRepository.getInstance(this, apiService)
        
        // Inicializar ViewModel com factory
        val factory = SettingsViewModelFactory(preferencesManager, inventarioRepository)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
        
        setupToolbar()
        setupUI()
        setupObservers()
        loadCurrentSettings()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Configurações"
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Configurações de sincronização por tempo
            switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAutoSyncEnabled(isChecked)
                updateSyncTimeVisibility(isChecked)
            }
            
            switchWifiOnly.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setWifiOnlyEnabled(isChecked)
            }
            
            // Configurações de sincronização por contador
            switchAutoSyncByCount.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAutoSyncByCountEnabled(isChecked)
                updateSyncCountVisibility(isChecked)
            }
            
            // Botões para ajustar intervalos
            btnDecreaseSyncInterval.setOnClickListener {
                val currentInterval = viewModel.getSyncInterval()
                if (currentInterval > 15) {
                    viewModel.setSyncInterval(currentInterval - 15)
                    updateSyncIntervalDisplay()
                }
            }
            
            btnIncreaseSyncInterval.setOnClickListener {
                val currentInterval = viewModel.getSyncInterval()
                if (currentInterval < 480) { // máximo 8 horas
                    viewModel.setSyncInterval(currentInterval + 15)
                    updateSyncIntervalDisplay()
                }
            }
            
            btnDecreaseSyncCount.setOnClickListener {
                val currentCount = viewModel.getSyncCollectionInterval()
                if (currentCount > 5) {
                    viewModel.setSyncCollectionInterval(currentCount - 5)
                    updateSyncCountDisplay()
                }
            }
            
            btnIncreaseSyncCount.setOnClickListener {
                val currentCount = viewModel.getSyncCollectionInterval()
                if (currentCount < 100) {
                    viewModel.setSyncCollectionInterval(currentCount + 5)
                    updateSyncCountDisplay()
                }
            }
            
            // Botão para resetar contador
            btnResetCollectionCount.setOnClickListener {
                viewModel.resetCollectionCount()
                Toast.makeText(this@SettingsActivity, "Contador de coletas resetado", Toast.LENGTH_SHORT).show()
                updateCollectionCountDisplay()
            }
            
            // Botão para testar sincronização
            btnTestSync.setOnClickListener {
                viewModel.testSync()
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.syncResult.observe(this) { result ->
            Toast.makeText(this, result, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadCurrentSettings() {
        binding.apply {
            // Carregar configurações atuais
            switchAutoSync.isChecked = viewModel.isAutoSyncEnabled()
            switchWifiOnly.isChecked = viewModel.isWifiOnlyEnabled()
            switchAutoSyncByCount.isChecked = viewModel.isAutoSyncByCountEnabled()
            
            updateSyncTimeVisibility(switchAutoSync.isChecked)
            updateSyncCountVisibility(switchAutoSyncByCount.isChecked)
            updateSyncIntervalDisplay()
            updateSyncCountDisplay()
            updateCollectionCountDisplay()
        }
    }
    
    private fun updateSyncTimeVisibility(enabled: Boolean) {
        binding.apply {
            layoutSyncInterval.alpha = if (enabled) 1.0f else 0.5f
            btnDecreaseSyncInterval.isEnabled = enabled
            btnIncreaseSyncInterval.isEnabled = enabled
        }
    }
    
    private fun updateSyncCountVisibility(enabled: Boolean) {
        binding.apply {
            layoutSyncCount.alpha = if (enabled) 1.0f else 0.5f
            btnDecreaseSyncCount.isEnabled = enabled
            btnIncreaseSyncCount.isEnabled = enabled
        }
    }
    
    private fun updateSyncIntervalDisplay() {
        val interval = viewModel.getSyncInterval()
        val hours = interval / 60
        val minutes = interval % 60
        
        val text = when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
            hours > 0 -> "${hours}h"
            else -> "${minutes}min"
        }
        
        binding.tvSyncInterval.text = text
    }
    
    private fun updateSyncCountDisplay() {
        val count = viewModel.getSyncCollectionInterval()
        binding.tvSyncCount.text = "$count coletas"
    }
    
    private fun updateCollectionCountDisplay() {
        val currentCount = viewModel.getCollectionCount()
        val interval = viewModel.getSyncCollectionInterval()
        binding.tvCurrentCollectionCount.text = "$currentCount / $interval"
    }
    
    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.goBack(this)
        return true
    }
}