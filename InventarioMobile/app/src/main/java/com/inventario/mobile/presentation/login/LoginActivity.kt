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
    private lateinit var biometricManager: com.inventario.mobile.security.BiometricAuthManager
    private lateinit var preferencesManager: PreferencesManager
    
    // Rastreia se o IP mudou para reiniciar o app após login
    private var ipChangedDuringSession = false
    private var originalIp: String? = null
    
    // ✅ Rastreia se a sessão atual usou autenticação biométrica (evita prompt duplo na MainActivity)
    private var wasAuthenticatedViaBiometric = false
    
    // ✅ Proteção contra navegação dupla (StateFlow pode emitir múltiplas vezes)
    private var navigationHandled = false
    
    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Solicitar permissão de notificações (Android 13+)
        requestNotificationPermission()

        // Inicializar managers usando Singleton
        preferencesManager = PreferencesManager(this)
        val serverConfigManager = ServerConfigManager.getInstance(this)
        
        // Inicializar biometria
        biometricManager = com.inventario.mobile.security.BiometricAuthManager(this)

        // Inicializar ViewModel
        val authRepository = AuthRepositoryImpl(NetworkModule.getApiService(this), serverConfigManager)
        viewModel = LoginViewModel(applicationContext, preferencesManager, serverConfigManager, authRepository)

        // Configurar IP do servidor
        val currentIp = serverConfigManager.getServerIp()
        if (currentIp?.isNotBlank() == true) {
            binding.etServerIp.setText(currentIp)
            originalIp = currentIp  // Salvar IP original para detectar mudanças
        } else {
            // Usar IP padrão (10.14.250.214)
            val defaultIp = serverConfigManager.getDefaultIp()
            binding.etServerIp.setText(defaultIp)
            // Salvar IP padrão para uso imediato
            serverConfigManager.setServerIp(defaultIp)
            originalIp = defaultIp
        }
        
        // ✅ CARREGAR E PREENCHER ÚLTIMO USUÁRIO QUE FEZ LOGIN
        val lastUsername = preferencesManager.getLastLoginUsername()
        if (lastUsername != null) {
            binding.etLogin.setText(lastUsername)
            android.util.Log.d("LoginActivity", "✓ Último usuário carregado: $lastUsername")
        }

        // Mostrar versão do app
        try {
            binding.tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        } catch (e: Exception) {
            binding.tvVersion.text = "v${BuildConfig.VERSION_NAME}"
        }
        
        // ✅ VERIFICAR SE HÁ MENSAGEM DE LOGOUT (sessão expirada)
        val logoutMessage = intent.getStringExtra("LOGOUT_MESSAGE")
        if (logoutMessage != null) {
            android.util.Log.d("LoginActivity", "Mensagem de logout recebida: $logoutMessage")
            // Mostrar mensagem ao usuário
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                logoutMessage,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }
        
        // ✅ VERIFICAR SE PRECISA DE AUTENTICAÇÃO LOCAL (biometria/PIN)
        val requireLocalAuth = intent.getBooleanExtra("require_local_auth", false)
        val biometricEnabledFromIntent = intent.getBooleanExtra("biometric_enabled", false)
        val pinEnabledFromIntent = intent.getBooleanExtra("pin_enabled", false)
        
        if (requireLocalAuth) {
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
            android.util.Log.d("LoginActivity", "AUTENTICAÇÃO LOCAL REQUERIDA")
            android.util.Log.d("LoginActivity", "Biometria: $biometricEnabledFromIntent")
            android.util.Log.d("LoginActivity", "PIN: $pinEnabledFromIntent")
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
            
            // ✅ Marcar que esta sessão usa autenticacão biométrica (para não perguntar de novo na MainActivity)
            if (biometricEnabledFromIntent) {
                wasAuthenticatedViaBiometric = true
            }
            
            // Iniciar autenticação automaticamente após um pequeno delay
            binding.root.postDelayed({
                if (biometricEnabledFromIntent) {
                    authenticateWithBiometric()
                } else if (pinEnabledFromIntent) {
                    showPinLoginDialog()
                }
            }, 500)
        }

        // Configurar listeners dos campos de texto
        binding.etServerIp.addTextChangedListener { editable: android.text.Editable? ->
            val newIp = editable?.toString() ?: ""
            viewModel.updateServerIp(newIp)
            
            // Detectar mudança de IP e avisar usuário
            if (originalIp != null && newIp.isNotBlank() && newIp != originalIp) {
                binding.tvIpChangeWarning.visibility = android.view.View.VISIBLE
                binding.tvIpChangeWarning.text = "⚠️ IP alterado. O app será reiniciado após o login."
            } else {
                binding.tvIpChangeWarning.visibility = android.view.View.GONE
            }
        }

        binding.etLogin.addTextChangedListener { editable: android.text.Editable? ->
            viewModel.updateLogin(editable?.toString() ?: "")
        }

        binding.etPassword.addTextChangedListener { editable: android.text.Editable? ->
            viewModel.updatePassword(editable?.toString() ?: "")
        }

        // Configurar botão de login
        binding.btnLogin.setOnClickListener {
            // ✅ GARANTIR QUE OS CAMPOS SEJAM LIDOS NO MOMENTO DO CLIQUE
            val loginText = binding.etLogin.text.toString().trim()
            val passwordText = binding.etPassword.text.toString().trim()
            val ipText = binding.etServerIp.text.toString().trim()
            
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════")
            android.util.Log.d("LoginActivity", "BOTÃO LOGIN CLICADO")
            android.util.Log.d("LoginActivity", "Login digitado: '$loginText'")
            android.util.Log.d("LoginActivity", "Senha digitada: ${if (passwordText.isNotEmpty()) "***" else "(vazio)"}")
            android.util.Log.d("LoginActivity", "IP digitado: '$ipText'")
            android.util.Log.d("LoginActivity", "═══════════════════════════════════════")
            
            // Atualizar ViewModel com valores atuais dos campos
            viewModel.updateLogin(loginText)
            viewModel.updatePassword(passwordText)
            viewModel.updateServerIp(ipText)
            
            // Garantir que baseUrl atual está salva antes de criar cliente
            if (ipText.isNotBlank()) {
                // Detectar se IP mudou
                if (originalIp != null && originalIp != ipText) {
                    ipChangedDuringSession = true
                    android.util.Log.d("LoginActivity", "⚠️ IP mudou de $originalIp para $ipText")
                }
                
                serverConfigManager.setServerIp(ipText)
                // Forçar recriação do ApiService com nova URL
                com.inventario.mobile.data.remote.api.ApiClient.recreateApiService(this)
            }

            viewModel.login()
        }
        
        // Configurar botão de biometria (será mostrado/ocultado pelo ViewModel)
        binding.cardBiometric.setOnClickListener {
            authenticateWithBiometric()
        }
        
        // ========== CONFIGURAR FAB DE BIOMETRIA ==========
        binding.fabBiometric.setOnClickListener {
            authenticateWithBiometric()
        }

        // Verificar login offline disponível
        viewModel.checkOfflineLogin()
        
        // Verificar se deve oferecer configuração de PIN
        viewModel.checkPinSetup()

        // ✅ VERIFICAR SE PRECISA MOSTRAR BIOMETRIA AUTOMATICAMENTE
        // Só chama se não estiver usando a autenticação local da intent (evita prompt duplo)
        if (!requireLocalAuth) {
            verificarEMostrarBiometriaSeNecessario()
        }

        // Observar ViewModel
        observeViewModel()
    }

    /**
     * Verifica se precisa mostrar prompt de biometria automaticamente
     * Casos: token expirado OU offline + biometria habilitada + usuário salvo
     * IMPORTANTE: Só permite acesso offline se houver dados salvos localmente
     */
    private fun verificarEMostrarBiometriaSeNecessario() {
        android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
        android.util.Log.d("LoginActivity", "VERIFICANDO SE PRECISA MOSTRAR BIOMETRIA AUTOMATICAMENTE")
        
        // Verificar se chegou da expiração de token ou está offline
        val tokenExpired = intent.getBooleanExtra("TOKEN_EXPIRED", false)
        val offlineFlag = intent.getBooleanExtra("OFFLINE", false)
        val isOffline = !com.inventario.mobile.utils.NetworkUtils.isNetworkAvailable(this)
        
        android.util.Log.d("LoginActivity", "Token expirado (intent): $tokenExpired")
        android.util.Log.d("LoginActivity", "Offline (intent): $offlineFlag")
        android.util.Log.d("LoginActivity", "Offline (real): $isOffline")
        
        // Verificar se biometria está habilitada
        val biometricEnabled = preferencesManager.isBiometricEnabled()
        android.util.Log.d("LoginActivity", "Biometria habilitada: $biometricEnabled")
        
        // Verificar se há usuário salvo localmente (primeira autenticação já foi feita)
        val hasUserSaved = preferencesManager.hasUserSavedLocally()
        android.util.Log.d("LoginActivity", "Usuário salvo localmente: $hasUserSaved")
        
        // ✅ VALIDAÇÃO: Só permite acesso offline se houver dados locais
        if ((isOffline || offlineFlag) && !hasUserSaved) {
            android.util.Log.w("LoginActivity", "❌ Offline sem dados locais. Primeira autenticação necessária.")
            showError("Conecte-se à internet para fazer o primeiro login.")
            return
        }
        
        // Decidir se deve mostrar prompt automaticamente
        val shouldShowBiometric = (tokenExpired || offlineFlag || isOffline) && biometricEnabled && hasUserSaved
        
        android.util.Log.d("LoginActivity", "Deve mostrar biometria automaticamente: $shouldShowBiometric")
        android.util.Log.d("LoginActivity", "═══════════════════════════════════════════")
        
        if (shouldShowBiometric) {
            // Determinar mensagem apropriada
            val mensagem = when {
                isOffline || offlineFlag -> "Sem conexão. Use biometria para acessar dados offline"
                tokenExpired -> "Token expirado. Use biometria para renovar"
                else -> "Use biometria para continuar"
            }
            
            android.util.Log.d("LoginActivity", "✓ Mostrando prompt de biometria: $mensagem")
            
            // Mostrar mensagem ao usuário
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                mensagem,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
            
            // Mostrar prompt de biometria após delay
            binding.root.postDelayed({
                authenticateWithBiometric()
            }, 500)
        }
    }



    // ========== MÉTODOS PARA LOGIN OFFLINE COM BIOMETRIA ==========
    
    /**
     * Autentica usando biometria
     */
    private fun authenticateWithBiometric() {
        android.util.Log.d("LoginActivity", "Iniciando autenticação biométrica")
        
        // Solicitar biometria diretamente — o hardware do Android gerencia disponibilidade
        biometricManager.authenticateWithCancel(
            activity = this,
            title = "Login com Biometria",
            subtitle = "Use sua biometria para acessar o app",
            description = "Toque no sensor para continuar",
            callback = object : com.inventario.mobile.security.BiometricCallback {
                override fun onAuthenticationSucceeded(authenticationType: String) {
                    android.util.Log.d("LoginActivity", "✅ Biometria autenticada com sucesso: $authenticationType")
                    wasAuthenticatedViaBiometric = true
                    viewModel.loginWithBiometric()
                }
                
                override fun onAuthenticationFailed(message: String) {
                    android.util.Log.w("LoginActivity", "❌ Biometria falhou: $message")
                    showError("Biometria não reconhecida. Tente novamente.")
                    binding.etLogin.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                }
                
                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    android.util.Log.e("LoginActivity", "❌ Erro na biometria: $errorCode - $errorMessage")
                    showError("Erro: $errorMessage")
                    binding.etLogin.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                }
                
                override fun onAuthenticationCanceled() {
                    android.util.Log.d("LoginActivity", "Biometria cancelada pelo usuário")
                    binding.etLogin.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                    showError("Autenticação cancelada. Use senha para fazer login.")
                }
                
                override fun onAuthenticationLockout(message: String) {
                    android.util.Log.e("LoginActivity", "🔒 Biometria bloqueada: $message")
                    showError(message)
                    binding.etLogin.isEnabled = true
                    binding.etPassword.isEnabled = true
                    binding.btnLogin.isEnabled = true
                }
            }
        )
    }
    
    // ========== MÉTODOS PARA PIN ==========
    
    /**
     * Mostra dialog oferecendo configuração de PIN
     */
    private fun showPinSetupOffer() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Configurar Login Offline")
            .setMessage(
                "Seu dispositivo não possui biometria.\n\n" +
                "Deseja criar um PIN de 4 dígitos para fazer login sem internet?"
            )
            .setPositiveButton("Sim") { _, _ ->
                showPinSetupDialog()
            }
            .setNegativeButton("Agora não", null)
            .show()
    }
    
    /**
     * Mostra dialog para criar PIN
     */
    private fun showPinSetupDialog() {
        val dialog = PinSetupDialog.newInstance()
        dialog.setOnPinCreatedListener { _ ->
            android.widget.Toast.makeText(
                this,
                "✅ PIN criado com sucesso!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            // Atualizar estado
            viewModel.checkOfflineLogin()
        }
        dialog.show(supportFragmentManager, "pin_setup")
    }
    
    /**
     * Mostra dialog para login com PIN
     */
    private fun showPinLoginDialog() {
        val dialog = PinLoginDialog.newInstance()
        
        dialog.setOnPinValidatedListener {
            // PIN correto - fazer login offline via ViewModel
            android.util.Log.d("LoginActivity", "✅ PIN validado, fazendo login offline")
            
            // Chamar método de login offline do ViewModel
            lifecycleScope.launch {
                viewModel.loginWithBiometric() // Usa o mesmo método de login offline
            }
        }
        
        dialog.show(supportFragmentManager, "pin_login")
    }
    
    /**
     * Mostra dialog perguntando se usuário quer habilitar biometria
     */
    private fun showBiometricSetupDialog() {
        android.util.Log.d("LoginActivity", "Mostrando dialog de configuração de biometria")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Habilitar Login por Biometria?")
            .setMessage("Você poderá fazer login rapidamente usando sua impressão digital ou reconhecimento facial, mesmo sem conexão com a internet.")
            .setPositiveButton("Sim") { _, _ ->
                android.util.Log.d("LoginActivity", "Usuário aceitou habilitar biometria")
                
                // Solicitar biometria para confirmar
                biometricManager.authenticateWithCancel(
                    activity = this,
                    title = "Confirmar Biometria",
                    subtitle = "Confirme sua biometria para habilitar login rápido",
                    description = "Toque no sensor",
                    callback = object : com.inventario.mobile.security.BiometricCallback {
                        override fun onAuthenticationSucceeded(authenticationType: String) {
                            android.util.Log.d("LoginActivity", "✅ Biometria confirmada, habilitando...")
                            
                            val username = binding.etLogin.text.toString()
                            val fullName = viewModel.uiState.value.savedUserName ?: "Usuário"
                            val token = preferencesManager.getAccessToken() ?: ""
                            
                            viewModel.enableBiometric(username, fullName, token)
                            
                            android.widget.Toast.makeText(
                                this@LoginActivity,
                                "✅ Login por biometria habilitado!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        
                        override fun onAuthenticationFailed(message: String) {
                            android.util.Log.w("LoginActivity", "❌ Falha ao confirmar biometria")
                            android.widget.Toast.makeText(
                                this@LoginActivity,
                                "Falha ao configurar biometria",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        
                        override fun onAuthenticationError(errorCode: Int, errorMessage: String) {}
                        override fun onAuthenticationCanceled() {
                            android.util.Log.d("LoginActivity", "Configuração de biometria cancelada")
                        }
                        override fun onAuthenticationLockout(message: String) {}
                    }
                )
            }
            .setNegativeButton("Não") { _, _ ->
                android.util.Log.d("LoginActivity", "Usuário recusou habilitar biometria")
            }
            .show()
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
            showError(state.errorMessage)
        } else {
            binding.tvError.visibility = View.GONE
        }
        
        // ========== OFERECER CONFIGURAÇÃO DE PIN ==========
        if (state.shouldOfferPinSetup) {
            showPinSetupOffer()
        }
        
        // ========== ATUALIZAR COMPONENTES DE BIOMETRIA E PIN ==========
        
        // Mostrar/ocultar botão de biometria
        if (state.showBiometricButton) {
            // Mostrar FAB de biometria (ícone flutuante)
            binding.fabBiometric.visibility = View.VISIBLE
            binding.fabBiometric.show()
            
            // Também manter o card para compatibilidade
            binding.cardBiometric.visibility = View.VISIBLE
            binding.tvBiometricHint.visibility = View.VISIBLE
            binding.dividerBiometric.visibility = View.VISIBLE
            
            // Atualizar texto do hint baseado no status de conexão
            binding.tvBiometricHint.text = if (state.isOnline) {
                "Ou use biometria para login rápido"
            } else {
                "⚠️ Sem conexão. Use biometria para continuar"
            }
            
            // Mostrar nome do usuário salvo
            if (state.savedUserName != null) {
                binding.tvSavedUser.visibility = View.VISIBLE
                binding.tvSavedUser.text = "Bem-vindo, ${state.savedUserName}"
            } else {
                binding.tvSavedUser.visibility = View.GONE
            }
        } else {
            // Ocultar FAB e card
            binding.fabBiometric.visibility = View.GONE
            binding.fabBiometric.hide()
            binding.cardBiometric.visibility = View.GONE
            binding.tvBiometricHint.visibility = View.GONE
            binding.dividerBiometric.visibility = View.GONE
            binding.tvSavedUser.visibility = View.GONE
        }
        
        // ========== MOSTRAR LOGIN COM PIN SE DISPONÍVEL ==========
        if (state.showPinLogin && !state.isOnline) {
            showPinLoginDialog()
        }
        
        // Desabilitar login/senha se offline e sem usuário salvo
        if (!state.isOnline && !state.hasUserSavedLocally) {
            binding.etLogin.isEnabled = false
            binding.etPassword.isEnabled = false
            binding.etServerIp.isEnabled = false
            binding.btnLogin.isEnabled = false
            showError("⚠️ Sem conexão. Conecte-se à internet para fazer o primeiro login.")
        } else {
            binding.etLogin.isEnabled = !state.isLoading
            binding.etPassword.isEnabled = !state.isLoading
            binding.etServerIp.isEnabled = !state.isLoading
            binding.btnLogin.isEnabled = !state.isLoading
        }
        
        // Mostrar indicador de modo offline
        if (state.offlineMode) {
            binding.tvOfflineMode.visibility = View.VISIBLE
            binding.tvOfflineMode.text = "📴 Modo Offline"
        } else {
            binding.tvOfflineMode.visibility = View.GONE
        }

        // Navegar para MainActivity se login bem-sucedido
        if (state.isLoginSuccessful && !navigationHandled) {
            navigationHandled = true  // ✅ Evitar chamadas repetidas do StateFlow
            
            // Verificar se deve oferecer biometria/PIN
            val pinAuthManager = com.inventario.mobile.security.PinAuthManager(this)
            val biometricManager = com.inventario.mobile.security.BiometricAuthManager(this)
            
            val hasBiometric = biometricManager.isBiometricAvailable().isAvailable()
            val shouldOfferBiometric = !state.biometricEnabled && !state.offlineMode && state.isOnline && hasBiometric
            val shouldOfferPin = !pinAuthManager.isPinEnabled() && !state.offlineMode && state.isOnline && !hasBiometric
            
            if (shouldOfferBiometric) {
                // Oferecer biometria e só navegar depois
                android.util.Log.d("LoginActivity", "Login bem-sucedido, perguntando sobre biometria")
                showBiometricSetupDialogAndNavigate()
            } else if (shouldOfferPin) {
                // Oferecer PIN e só navegar depois
                android.util.Log.d("LoginActivity", "Login bem-sucedido, perguntando sobre PIN")
                showPinSetupOfferAndNavigate()
            } else {
                // Navegar diretamente
                // Passar flag biometricAuthCompleted se:
                // 1. O usuário autenticou via biometria nesta sessão, OU
                // 2. Biometria já está habilitada (evita prompt duplo na MainActivity)
                val skipBiometricCheck = wasAuthenticatedViaBiometric || state.biometricEnabled
                navigateToMain(biometricAuthCompleted = skipBiometricCheck)
            }
        }
    }
    
    /**
     * Mostra dialog de biometria e navega após resposta
     */
    private fun showBiometricSetupDialogAndNavigate() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Habilitar Login por Biometria?")
            .setMessage("Você poderá fazer login rapidamente usando sua impressão digital ou reconhecimento facial, mesmo sem conexão com a internet.")
            .setPositiveButton("Sim") { _, _ ->
                android.util.Log.d("LoginActivity", "Usuário aceitou habilitar biometria")
                
                // Solicitar biometria para confirmar
                biometricManager.authenticateWithCancel(
                    activity = this,
                    title = "Confirmar Biometria",
                    subtitle = "Confirme sua biometria para habilitar login rápido",
                    description = "Toque no sensor",
                    callback = object : com.inventario.mobile.security.BiometricCallback {
                        override fun onAuthenticationSucceeded(authenticationType: String) {
                            android.util.Log.d("LoginActivity", "✅ Biometria confirmada, habilitando...")
                            
                            val username = binding.etLogin.text.toString()
                            val fullName = viewModel.uiState.value.savedUserName ?: "Usuário"
                            val token = preferencesManager.getAccessToken() ?: ""
                            
                            viewModel.enableBiometric(username, fullName, token)
                            
                            android.widget.Toast.makeText(
                                this@LoginActivity,
                                "✅ Login por biometria habilitado!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            
                            // Navegar após configurar
                            navigateToMain()
                        }
                        
                        override fun onAuthenticationFailed(message: String) {
                            android.util.Log.w("LoginActivity", "❌ Falha ao confirmar biometria")
                            android.widget.Toast.makeText(
                                this@LoginActivity,
                                "Falha ao configurar biometria",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            // Navegar mesmo assim
                            navigateToMain()
                        }
                        
                        override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                            // Navegar mesmo com erro
                            navigateToMain()
                        }
                        
                        override fun onAuthenticationCanceled() {
                            android.util.Log.d("LoginActivity", "Configuração de biometria cancelada")
                            // Navegar mesmo se cancelou
                            navigateToMain()
                        }
                        
                        override fun onAuthenticationLockout(message: String) {
                            // Navegar mesmo se bloqueado
                            navigateToMain()
                        }
                    }
                )
            }
            .setNegativeButton("Não") { _, _ ->
                android.util.Log.d("LoginActivity", "Usuário recusou habilitar biometria")
                // Navegar após recusar
                navigateToMain()
            }
            .setOnCancelListener {
                // Navegar se cancelar o dialog
                navigateToMain()
            }
            .show()
    }
    
    /**
     * Mostra oferta de PIN e navega após resposta
     */
    private fun showPinSetupOfferAndNavigate() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Configurar Login Offline")
            .setMessage(
                "Seu dispositivo não possui biometria.\n\n" +
                "Deseja criar um PIN de 4 dígitos para fazer login sem internet?"
            )
            .setPositiveButton("Sim") { _, _ ->
                showPinSetupDialogAndNavigate()
            }
            .setNegativeButton("Agora não") { _, _ ->
                // Navegar após recusar
                navigateToMain()
            }
            .setOnCancelListener {
                // Navegar se cancelar o dialog
                navigateToMain()
            }
            .show()
    }
    
    /**
     * Mostra dialog de criação de PIN e navega após
     */
    private fun showPinSetupDialogAndNavigate() {
        val dialog = PinSetupDialog.newInstance()
        dialog.setOnPinCreatedListener { _ ->
            android.widget.Toast.makeText(
                this,
                "✅ PIN criado com sucesso!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            // Navegar após criar PIN
            navigateToMain()
        }
        dialog.show(supportFragmentManager, "pin_setup")
    }
    
    /**
     * Navega para MainActivity
     * Se o IP mudou durante a sessão, reinicia o app para aplicar as novas configurações
     */
    private fun navigateToMain(biometricAuthCompleted: Boolean = false) {
        if (ipChangedDuringSession) {
            android.util.Log.d("LoginActivity", "🔄 IP mudou, reiniciando app para aplicar configurações...")
            restartApp()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            // ✅ Sinalizar que autenticação biométrica já foi feita nesta sessão
            // Evita que MainActivity solicite biometria novamente (prompt duplo)
            if (biometricAuthCompleted) {
                intent.putExtra("BIOMETRIC_AUTH_COMPLETED", true)
                android.util.Log.d("LoginActivity", "✓ Flag BIOMETRIC_AUTH_COMPLETED enviada para MainActivity")
            }
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * Reinicia o app completamente para aplicar novas configurações de servidor
     */
    private fun restartApp() {
        android.util.Log.d("LoginActivity", "═══════════════════════════════════════")
        android.util.Log.d("LoginActivity", "🔄 REINICIANDO APP - IP ALTERADO")
        android.util.Log.d("LoginActivity", "═══════════════════════════════════════")
        
        // Limpar caches do ApiClient
        com.inventario.mobile.data.remote.api.ApiClient.clearInstance()
        
        // Usar AlarmManager para reiniciar o app de forma confiável
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("IP_CHANGED_RESTART", true)
            
            // Agendar reinício em 100ms usando AlarmManager
            val pendingIntent = android.app.PendingIntent.getActivity(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_CANCEL_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                pendingIntent
            )
            
            android.util.Log.d("LoginActivity", "✓ Reinício agendado via AlarmManager")
        }
        
        // Finalizar todas as activities
        finishAffinity()
        
        // Matar o processo para limpar singletons
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(0)
        }, 50)
    }
    
    /**
     * Solicita permissão de notificações (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("LoginActivity", "✅ Permissão de notificações concedida")
            } else {
                android.util.Log.w("LoginActivity", "⚠️ Permissão de notificações negada")
                android.widget.Toast.makeText(
                    this,
                    "Permissão de notificações negada. Você não receberá alertas sobre modo offline.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
