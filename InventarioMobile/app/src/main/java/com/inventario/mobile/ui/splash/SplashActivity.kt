package com.inventario.mobile.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.databinding.ActivitySplashBinding
import com.inventario.mobile.presentation.login.LoginActivity
import com.inventario.mobile.presentation.main.MainActivity
import com.inventario.mobile.utils.PreferencesManager
// import com.inventario.mobile.data.local.database.InventarioDatabase
// import com.inventario.mobile.utils.DatabaseInitializer
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L // 2 segundos
        private const val INITIALIZE_DATABASE = false // Desabilitado para evitar problemas de navegação
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Iniciando SplashActivity")
        
        try {
            super.onCreate(savedInstanceState)
            Log.d(TAG, "onCreate: super.onCreate() executado com sucesso")
            
            binding = ActivitySplashBinding.inflate(layoutInflater)
            Log.d(TAG, "onCreate: Binding inflado com sucesso")
            
            setContentView(binding.root)
            Log.d(TAG, "onCreate: setContentView executado com sucesso")
            
            // Inicializar PreferencesManager (versão criptografada)
            preferencesManager = PreferencesManager(this)
            Log.d(TAG, "onCreate: PreferencesManager inicializado com sucesso")

            // Esconder a action bar
            supportActionBar?.hide()
            Log.d(TAG, "onCreate: Action bar escondida")

            // Inicializar banco de dados se necessário
            if (INITIALIZE_DATABASE) {
                // initializeDatabase() // Commented out for minimal version
            } else {
                // Aguardar e navegar para a próxima tela
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "onCreate: Executando navegação após delay")
                    navigateToNextScreen()
                }, SPLASH_DELAY)
            }
            
            Log.d(TAG, "onCreate: Configuração completa")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Erro durante inicialização", e)
            // Em caso de erro, tentar navegar para login
            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } catch (ex: Exception) {
                Log.e(TAG, "onCreate: Erro crítico ao tentar navegar para login", ex)
            }
        }
    }

    /*
    private fun initializeDatabase() {
        Log.d(TAG, "initializeDatabase: Iniciando inicialização do banco")
        
        lifecycleScope.launch {
            try {
                val database = InventarioDatabase.getDatabase(this@SplashActivity)
                
                // Verificar status do banco
                val status = DatabaseInitializer.checkDatabaseStatus(database)
                Log.d(TAG, "Status do banco:\n$status")
                
                // Inicializar se estiver vazio
                if (status.salas == 0) {
                    Log.d(TAG, "Banco vazio, inicializando com dados de exemplo...")
                    
                    DatabaseInitializer.initializeDatabase(database, forceReset = false)
                    
                    // Verificar novamente
                    val newStatus = DatabaseInitializer.checkDatabaseStatus(database)
                    Log.d(TAG, "Novo status do banco:\n$newStatus")
                } else {
                    Log.d(TAG, "Banco já possui dados")
                }
                
                // Aguardar um pouco e navegar
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextScreen()
                }, 1000L)
                
            } catch (e: Exception) {
                Log.e(TAG, "initializeDatabase: Erro ao inicializar banco", e)
                // Mesmo com erro, tentar navegar
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextScreen()
                }, 1000L)
            }
        }
    }
    */

    private fun navigateToNextScreen() {
        try {
            Log.d(TAG, "navigateToNextScreen: Iniciando navegação")
            
            val isLoggedIn = preferencesManager.isLoggedIn()
            Log.d(TAG, "navigateToNextScreen: isLoggedIn = $isLoggedIn")
            
            val intent = if (isLoggedIn) {
                Log.d(TAG, "navigateToNextScreen: Navegando para MainActivity")
                Intent(this, MainActivity::class.java)
            } else {
                Log.d(TAG, "navigateToNextScreen: Navegando para LoginActivity")
                Intent(this, LoginActivity::class.java)
            }
            
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d(TAG, "navigateToNextScreen: Flags configuradas")
            
            startActivity(intent)
            Log.d(TAG, "navigateToNextScreen: startActivity executado")
            
            finish()
            Log.d(TAG, "navigateToNextScreen: finish() executado")
            
        } catch (e: Exception) {
            Log.e(TAG, "navigateToNextScreen: Erro durante navegação", e)
            // Em caso de erro, ir para LoginActivity como fallback
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}