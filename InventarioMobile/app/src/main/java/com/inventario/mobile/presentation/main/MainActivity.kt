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
import com.inventario.mobile.BuildConfig
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

            // Adicionar indicador de modo offline
            com.inventario.mobile.ui.components.OfflineIndicator.setup(this)
            Log.d(TAG, "onCreate: Indicador de modo offline configurado")

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
            
            // Verificar se o inventário ativo está configurado corretamente
            verificarInventarioAtivo()
            
            // Inicializar banco de dados em background (não bloqueia a UI)
            // initializeDatabaseInBackground()
            
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Erro durante inicialização da MainActivity", e)
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // O indicador já está observando mudanças automaticamente
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
                    // Usar nova tela com abas (Por Responsável / Por Sala)
                    val intent = Intent(this, com.inventario.mobile.presentation.inventario.InventarioTabsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_collections -> {
                    // Usar nova tela de coletas com Clean Architecture
                    val intent = Intent(this, com.inventario.mobile.presentation.coletas.ColetasActivityClean::class.java)
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
                // Usar nova tela com abas (Por Responsável / Por Sala)
                val intent = Intent(this, com.inventario.mobile.presentation.inventario.InventarioTabsActivity::class.java)
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
            
            R.id.nav_quick_search -> {
                val intent = Intent(this, com.inventario.mobile.presentation.search.QuickSearchActivity::class.java)
                startActivity(intent)
                true
            }
            
            R.id.nav_historico_scans -> {
                val intent = Intent(this, com.inventario.mobile.presentation.historico.HistoricoScansActivity::class.java)
                startActivity(intent)
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
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        
        AlertDialog.Builder(this)
            .setTitle("Sobre o SIHCP")
            .setMessage("""
                Sistema de Histórico e Coleta Patrimonial
                
                Versão: $versionName (Build $versionCode)
                
                Desenvolvido para Instituto Federal de Mato Grosso (IFMT)
                
                Funcionalidades:
                • Coleta de patrimônios via QR Code
                • Modo offline
                • Sincronização automática
                • Busca por voz
                • Relatórios detalhados
                • Exportação de relatórios (PDF, Excel, CSV)
                
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
    
    /**
     * Verifica se o inventário ativo está configurado corretamente
     * Se não estiver, mostra um diálogo e redireciona para o login
     * 
     * Isso resolve o problema de usuários que logaram antes da atualização
     * e ainda têm o ID do inventário incorreto (ou nenhum ID salvo)
     */
    private fun verificarInventarioAtivo() {
        lifecycleScope.launch {
            try {
                val inventarioId = preferencesManager.getInventarioAtivoId()
                val inventarioNome = preferencesManager.getInventarioAtivoNome()
                val inventarioStatus = preferencesManager.getInventarioAtivoStatus()
                
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "VERIFICANDO INVENTÁRIO ATIVO")
                Log.d(TAG, "ID salvo: $inventarioId")
                Log.d(TAG, "Nome salvo: $inventarioNome")
                Log.d(TAG, "Status salvo: $inventarioStatus")
                Log.d(TAG, "═══════════════════════════════════════════")
                
                // Caso 1: Inventário não configurado
                if (inventarioId == null || inventarioId <= 0) {
                    Log.w(TAG, "⚠️ Inventário ativo não configurado! Solicitando relogin...")
                    
                    runOnUiThread {
                        mostrarDialogoReloginNecessario(
                            titulo = "Inventário Não Configurado",
                            mensagem = "Nenhum inventário ativo foi encontrado.\n\n" +
                                    "Para realizar coletas, é necessário fazer login novamente para carregar o inventário atual.",
                            icone = android.R.drawable.ic_dialog_alert
                        )
                    }
                    return@launch
                }
                
                // Caso 2: Inventário finalizado ou cancelado (precisa atualizar)
                if (inventarioStatus != null && inventarioStatus != "EM_ANDAMENTO") {
                    Log.w(TAG, "⚠️ Inventário '$inventarioNome' não está mais em andamento (status: $inventarioStatus)")
                    
                    val statusTexto = when(inventarioStatus) {
                        "CONCLUIDO" -> "concluído"
                        "CANCELADO" -> "cancelado"
                        "FINALIZADO" -> "finalizado"
                        else -> "alterado"
                    }
                    
                    runOnUiThread {
                        mostrarDialogoReloginNecessario(
                            titulo = "Inventário $statusTexto",
                            mensagem = "O inventário '$inventarioNome' foi $statusTexto.\n\n" +
                                    "Faça login novamente para carregar o inventário atual.",
                            icone = android.R.drawable.ic_dialog_info
                        )
                    }
                    return@launch
                }
                
                Log.d(TAG, "✅ Inventário ativo OK: $inventarioNome (ID: $inventarioId)")
                
                // Mostrar informação do inventário ativo na UI
                runOnUiThread {
                    mostrarInfoInventarioAtivo(inventarioNome ?: "Inventário #$inventarioId")
                }
                
                // Verificar com o servidor se o inventário ainda é o correto (em background)
                verificarInventarioComServidor(inventarioId)
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar inventário ativo", e)
                
                // Em caso de erro, mostrar aviso mas não bloquear
                runOnUiThread {
                    Snackbar.make(
                        binding.root,
                        "⚠️ Não foi possível verificar o inventário ativo",
                        Snackbar.LENGTH_LONG
                    ).setAction("Verificar") {
                        verificarInventarioAtivo()
                    }.show()
                }
            }
        }
    }
    
    /**
     * Mostra informação do inventário ativo na toolbar ou snackbar
     */
    private fun mostrarInfoInventarioAtivo(nomeInventario: String) {
        // Atualizar subtítulo da toolbar com nome do inventário
        supportActionBar?.subtitle = "📋 $nomeInventario"
    }
    
    /**
     * Verifica com o servidor se o inventário local ainda é o ativo
     * Se o servidor tiver um inventário diferente, atualiza localmente
     * 
     * Isso é útil quando o inventário muda no servidor (ex: novo inventário criado)
     */
    private fun verificarInventarioComServidor(inventarioIdLocal: Int) {
        lifecycleScope.launch {
            try {
                // Verificar se está online
                val isOnline = com.inventario.mobile.utils.NetworkUtils.isNetworkAvailable(this@MainActivity)
                if (!isOnline) {
                    Log.d(TAG, "Offline - pulando verificação com servidor")
                    return@launch
                }
                
                Log.d(TAG, "Verificando inventário ativo com o servidor...")
                
                // Chamar API para buscar inventário ativo
                val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(this@MainActivity)
                val response = apiService.getInventarioAtivo()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val inventarioServidor = response.body()?.data
                    
                    if (inventarioServidor != null) {
                        val idServidor = inventarioServidor.id
                        val nomeServidor = inventarioServidor.nome
                        
                        Log.d(TAG, "Inventário no servidor: ID=$idServidor, Nome=$nomeServidor")
                        
                        // Verificar se é diferente do local
                        if (idServidor != inventarioIdLocal) {
                            Log.w(TAG, "⚠️ Inventário no servidor ($idServidor) diferente do local ($inventarioIdLocal)")
                            
                            runOnUiThread {
                                mostrarDialogoInventarioAtualizado(
                                    nomeNovo = nomeServidor,
                                    idNovo = idServidor
                                )
                            }
                        } else {
                            Log.d(TAG, "✅ Inventário sincronizado com servidor")
                        }
                    }
                } else {
                    Log.w(TAG, "Não foi possível verificar inventário com servidor: ${response.message()}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao verificar inventário com servidor (não crítico)", e)
                // Não mostrar erro para o usuário - é apenas uma verificação em background
            }
        }
    }
    
    /**
     * Mostra diálogo informando que há um novo inventário no servidor
     */
    private fun mostrarDialogoInventarioAtualizado(nomeNovo: String, idNovo: Int) {
        AlertDialog.Builder(this)
            .setTitle("Novo Inventário Disponível")
            .setMessage("Um novo inventário foi iniciado no servidor:\n\n" +
                    "📋 $nomeNovo\n\n" +
                    "Deseja atualizar para o novo inventário?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Atualizar Agora") { _, _ ->
                Log.d(TAG, "Usuário escolheu atualizar para novo inventário")
                
                // Limpar dados e fazer relogin
                preferencesManager.clearSessionData()
                preferencesManager.clearInventarioAtivo()
                navigateToLogin()
            }
            .setNegativeButton("Depois") { _, _ ->
                Log.d(TAG, "Usuário adiou atualização do inventário")
                // Mostrar aviso
                Snackbar.make(
                    binding.root,
                    "⚠️ Você está usando um inventário desatualizado",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .show()
    }
    
    /**
     * Mostra diálogo informando que o relogin é necessário
     * O usuário não pode cancelar - precisa fazer login novamente
     */
    private fun mostrarDialogoReloginNecessario(
        titulo: String, 
        mensagem: String,
        icone: Int = android.R.drawable.ic_dialog_info
    ) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensagem)
            .setIcon(icone)
            .setCancelable(false) // Não permite fechar sem clicar no botão
            .setPositiveButton("Fazer Login") { _, _ ->
                Log.d(TAG, "Usuário aceitou fazer relogin")
                
                // Limpar dados de sessão para forçar novo login
                preferencesManager.clearSessionData()
                preferencesManager.clearInventarioAtivo()
                
                // Navegar para tela de login com mensagem
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("LOGOUT_MESSAGE", "Faça login para configurar o inventário ativo")
                startActivity(intent)
                finish()
            }
            .show()
    }
}
