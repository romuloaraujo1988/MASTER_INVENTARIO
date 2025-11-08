package com.inventario.mobile.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityLoginBinding
import com.inventario.mobile.data.repository.AuthRepositoryImpl
import com.inventario.mobile.di.NetworkModule
import com.inventario.mobile.presentation.main.MainActivity
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager
import com.inventario.mobile.utils.NetworkLocationManager
import com.inventario.mobile.BuildConfig
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var networkLocationManager: NetworkLocationManager
    private lateinit var biometricHelper: BiometricLoginHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar gerenciadores e configurar serviço de API
        val preferencesManager = PreferencesManager(this)
        val serverConfigManager = ServerConfigManager.getInstance(this)
        networkLocationManager = NetworkLocationManager.getInstance(this)
        val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(this)
        val authRepository = AuthRepositoryImpl(apiService, serverConfigManager)
        viewModel = ViewModelProvider(this, LoginViewModelFactory(this, preferencesManager, serverConfigManager, authRepository))[LoginViewModel::class.java]

        // Inicializar helper de biometria
        biometricHelper = BiometricLoginHelper(this, this)

        // Detectar mudança de rede automaticamente
        handleNetworkChange()
        
        setupUI()
        setupBiometric()
        observeViewModel()
    }

    private fun setupUI() {
        // Preencher IP atual, se disponível
        val serverConfigManager = ServerConfigManager.getInstance(this)
        serverConfigManager.getServerIp()?.let { ip ->
            if (ip.isNotBlank()) {
                binding.etServerIp.setText(ip)
            }
        }

        // Mostrar versão do app
        binding.tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        // Configurar listeners dos campos de texto
        binding.etServerIp.addTextChangedListener { editable: android.text.Editable? ->
            viewModel.updateServerIp(editable?.toString() ?: "")
        }

        binding.etLogin.addTextChangedListener { editable: android.text.Editable? ->
            viewModel.updateLogin(editable?.toString() ?: "")
        }

        binding.etPassword.addTextChangedListener { editable: android.text.Editable? ->
            viewModel.updatePassword(editable?.toString() ?: "")
        }

        // Configurar botão de login
        binding.btnLogin.setOnClickListener {
            // Garantir que baseUrl atual está salva antes de criar cliente
            val currentIp = binding.etServerIp.text.toString().trim()
            if (currentIp.isNotBlank()) {
                serverConfigManager.setServerIp(currentIp)
                // Forçar recriação do ApiService com nova URL
                com.inventario.mobile.data.remote.api.ApiClient.recreateApiService(this)
            }

            viewModel.login()
        }
    }

    private fun setupBiometric() {
        // Verificar se pode usar biometria
        val status = biometricHelper.canUseBiometricLogin()
        
        if (status is BiometricLoginStatus.Available) {
            // Mostrar botão de biometria
            binding.cardBiometric.visibility = View.VISIBLE
            binding.tvBiometricType.text = biometricHelper.getBiometricInfo()
            
            // Configurar clique
            binding.btnBiometric.setOnClickListener {
                loginWithBiometric()
            }
            
            android.util.Log.d("LoginActivity", "Biometria disponível e configurada")
        } else {
            // Ocultar botão de biometria
            binding.cardBiometric.visibility = View.GONE
            android.util.Log.d("LoginActivity", "Biometria não disponível: ${status.getMessage()}")
        }
    }

    private fun loginWithBiometric() {
        biometricHelper.loginWithBiometric(object : BiometricLoginCallback {
            override fun onBiometricLoginSuccess(username: String, password: String) {
                android.util.Log.d("LoginActivity", "Login biométrico bem-sucedido")
                
                // Preencher campos e fazer login
                binding.etLogin.setText(username)
                binding.etPassword.setText(password)
                
                // Executar login
                viewModel.updateLogin(username)
                viewModel.updatePassword(password)
                viewModel.login()
            }

            override fun onBiometricLoginFailed(message: String) {
                android.util.Log.w("LoginActivity", "Login biométrico falhou: $message")
                showError("Biometria não reconhecida. Tente novamente.")
            }

            override fun onBiometricLoginError(message: String) {
                android.util.Log.e("LoginActivity", "Erro no login biométrico: $message")
                showError(message)
            }

            override fun onBiometricLoginCanceled() {
                android.util.Log.d("LoginActivity", "Login biométrico cancelado")
                // Não fazer nada, usuário pode usar senha
            }
        })
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.setTextColor(getColor(R.color.error))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: LoginUiState) {
        // Atualizar campos de texto se necessário
        if (binding.etServerIp.text.toString() != state.serverIp) {
            binding.etServerIp.setText(state.serverIp)
        }

        // Atualizar erros dos campos
        binding.tilServerIp.error = state.serverIpError
        binding.tilLogin.error = state.loginError
        binding.tilPassword.error = state.passwordError

        // Atualizar estado de loading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (state.isLoading) "" else getString(R.string.login_button)
        binding.btnLogin.isEnabled = !state.isLoading

        // Atualizar mensagem de erro
        if (state.errorMessage != null) {
            binding.tvError.text = state.errorMessage
            binding.tvError.visibility = View.VISIBLE
        } else {
            binding.tvError.visibility = View.GONE
        }

        // Verificar se login foi bem-sucedido
        if (state.isLoginSuccessful) {
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
            android.util.Log.d("LoginActivity", "LOGIN BEM-SUCEDIDO - NAVEGANDO PARA MAIN")
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
            
            // Perguntar se quer habilitar biometria (se ainda não estiver habilitada)
            offerBiometricSetup()
            
            navigateToMain()
        }
    }

    private fun offerBiometricSetup() {
        val status = biometricHelper.canUseBiometricLogin()
        
        // Só oferecer se biometria estiver disponível mas não configurada
        if (status is BiometricLoginStatus.NotConfigured || status is BiometricLoginStatus.NoCredentials) {
            val username = binding.etLogin.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (username.isNotBlank() && password.isNotBlank()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Habilitar Biometria")
                    .setMessage("Deseja usar biometria para fazer login mais rapidamente na próxima vez?")
                    .setPositiveButton("Sim") { _, _ ->
                        setupBiometricForUser(username, password)
                    }
                    .setNegativeButton("Agora não", null)
                    .show()
            }
        }
    }

    private fun setupBiometricForUser(username: String, password: String) {
        biometricHelper.setupBiometricAfterLogin(username, password) { success, message ->
            if (success) {
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "Erro: $message", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        // Adicionar pequeno delay para garantir que os dados foram salvos
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 500) // 500ms de delay
    }
    
    /**
     * Detecta mudanças de rede e ajusta configurações automaticamente
     */
    private fun handleNetworkChange() {
        lifecycleScope.launch {
            try {
                binding.tvError.text = "Detectando rede..."
                binding.tvError.visibility = View.VISIBLE
                
                val result = networkLocationManager.handleNetworkChange()
                
                if (result.networkChanged) {
                    android.util.Log.d("LoginActivity", "Mudança de rede detectada!")
                    
                    if (result.configApplied && result.serverIp != null) {
                        // Atualizar UI com novo IP
                        binding.etServerIp.setText(result.serverIp)
                        
                        // Mostrar mensagem de sucesso
                        binding.tvError.text = result.message
                        binding.tvError.setTextColor(getColor(android.R.color.holo_green_dark))
                        
                        // Esconder mensagem após 3 segundos
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            binding.tvError.visibility = View.GONE
                        }, 3000)
                    } else {
                        // Mostrar mensagem de aviso
                        binding.tvError.text = result.message
                        binding.tvError.setTextColor(getColor(android.R.color.holo_orange_dark))
                    }
                } else {
                    // Mesma rede, esconder mensagem
                    binding.tvError.visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Erro ao detectar mudança de rede", e)
                binding.tvError.text = "Erro ao detectar rede: ${e.message}"
                binding.tvError.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
    }
    

}