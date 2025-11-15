package com.inventario.mobile.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityMainBinding
import com.inventario.mobile.presentation.dashboard.DashboardFragment
import com.inventario.mobile.presentation.login.LoginActivity
import com.inventario.mobile.presentation.settings.SettingsActivity
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity principal do app
 * Clean Architecture + MVVM + Hilt
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Iniciando MainActivity")
        
        try {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "onCreate: super.onCreate() executado com sucesso")
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            Log.d(TAG, "onCreate: Binding inflado com sucesso")
            
            setContentView(binding.root)
            Log.d(TAG, "onCreate: setContentView executado com sucesso")

            // Inicializar PreferencesManager
            preferencesManager = PreferencesManager(this)
            Log.d(TAG, "onCreate: PreferencesManager inicializado")

            setupUI()
            Log.d(TAG, "onCreate: setupUI executado com sucesso")
            
            // Carregar fragment inicial se não há estado salvo
            if (savedInstanceState == null) {
                Log.d(TAG, "onCreate: Carregando DashboardFragment inicial")
                loadFragment(DashboardFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                Log.d(TAG, "onCreate: DashboardFragment carregado com sucesso")
            }
            
            Log.d(TAG, "onCreate: MainActivity inicializada com sucesso")
            
            // Solicitar permissões de localização (opcional, não bloqueia o app)
            requestLocationPermissionIfNeeded()
            
            // Inicializar banco de dados em background (não bloqueia a UI)
            // initializeDatabaseInBackground()
            
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Erro durante inicialização da MainActivity", e)
            e.printStackTrace()
        }
    }
    
    private fun requestLocationPermissionIfNeeded() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            PermissionHelper.requestLocationPermission(this)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        PermissionHelper.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            onGranted = {
                Log.d(TAG, "Permissão de localização concedida")
            },
            onDenied = {
                Log.d(TAG, "Permissão de localização negada")
                // Não bloqueia o app, apenas registra
            }
        )
    }
    
    /*
    private fun initializeDatabaseInBackground() {
        lifecycleScope.launch {
            try {
                val database = com.inventario.mobile.data.local.database.InventarioDatabase.getDatabase(this@MainActivity)
                val status = com.inventario.mobile.util.DatabaseInitializer.checkDatabaseStatus(database)
                
                if (status.salas == 0) {
                    Log.d(TAG, "Inicializando banco de dados com dados de exemplo...")
                    com.inventario.mobile.util.DatabaseInitializer.initializeDatabase(database, forceReset = false)
                    Log.d(TAG, "Banco de dados inicializado com sucesso")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar banco de dados", e)
            }
        }
    }
    */

    private fun setupUI() {
        // Configurar toolbar como ActionBar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Dashboard"
        
        // Configurar Navigation Drawer
        setupNavigationDrawer()
        
        // Configurar navegação bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    supportActionBar?.title = "Dashboard"
                    true
                }
                R.id.nav_inventory -> {
                    val intent = Intent(this, com.inventario.mobile.presentation.inventario.InventarioActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_collections -> {
                    val intent = Intent(this, com.inventario.mobile.presentation.coleta.CollectionViewActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_sync -> {
                    val intent = Intent(this, com.inventario.mobile.presentation.sync.SyncActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupNavigationDrawer() {
        // Configurar toggle do drawer
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Atualizar header com dados do usuário
        updateNavigationHeader()
        
        // Configurar listener do menu
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    
    private fun updateNavigationHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserProfile = headerView.findViewById<TextView>(R.id.tvUserProfile)
        
        tvUserName.text = preferencesManager.getUserName() ?: "Usuário"
        tvUserProfile.text = preferencesManager.getUserProfile() ?: "Coletor"
    }
    
    private fun handleNavigationItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.nav_dashboard -> {
                loadFragment(DashboardFragment())
                supportActionBar?.title = "Dashboard"
                binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
                true
            }
            
            R.id.nav_inventario -> {
                val intent = Intent(this, com.inventario.mobile.presentation.inventario.InventarioActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_coletas -> {
                val intent = Intent(this, com.inventario.mobile.presentation.coleta.CollectionViewActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_scanner -> {
                val intent = Intent(this, com.inventario.mobile.presentation.scanner.ScannerActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_sync -> {
                val intent = Intent(this, com.inventario.mobile.presentation.sync.SyncActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_pendentes -> {
                val intent = Intent(this, com.inventario.mobile.presentation.sync.PendingCollectionsActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_divergencias -> {
                // TODO: Implementar tela de divergências
                Snackbar.make(binding.root, "Divergências - Em desenvolvimento", Snackbar.LENGTH_SHORT).show()
                true
            }
            
            R.id.nav_voice_search -> {
                // TODO: Abrir busca por voz
                Snackbar.make(binding.root, "Use o botão de microfone no Dashboard", Snackbar.LENGTH_SHORT).show()
                true
            }
            
            R.id.nav_relatorios -> {
                val intent = Intent(this, com.inventario.mobile.presentation.statistics.StatisticsActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_network_diagnostic -> {
                val intent = Intent(this, com.inventario.mobile.presentation.activity.NetworkDiagnosticActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_about -> {
                showAboutDialog()
                true
            }
            
            R.id.nav_logout -> {
                showLogoutDialog()
                true
            }
            
            else -> false
        }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sobre o SIHCP")
            .setMessage("""
                Sistema de Histórico e Coleta Patrimonial
                
                Versão: 1.2.0
                
                Desenvolvido para Instituto Federal de Mato Grosso (IFMT)
                
                Funcionalidades:
                • Coleta de patrimônios via QR Code
                • Modo offline
                • Sincronização automática
                • Busca por voz
                • Relatórios detalhados
                
                © 2025 IFMT - Todos os direitos reservados
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.logout_cancel, null)
            .show()
    }

    private fun performLogout() {
        try {
            // Limpar dados de sessão
            preferencesManager.clearSessionData()
            
            // Mostrar mensagem de sucesso
            Snackbar.make(binding.root, R.string.logout_success, Snackbar.LENGTH_SHORT).show()
            
            // Navegar para tela de login
            navigateToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer logout", e)
            Snackbar.make(binding.root, "Erro ao fazer logout", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}