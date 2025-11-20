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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar managers usando Singleton
        val preferencesManager = PreferencesManager(this)
        val serverConfigManager = ServerConfigManager.getInstance(this)

        // Inicializar ViewModel
        val authRepository = AuthRepositoryImpl(NetworkModule.getApiService(this), serverConfigManager)
        viewModel = LoginViewModel(applicationContext, preferencesManager, serverConfigManager, authRepository)

        // Configurar IP do servidor
        val currentIp = serverConfigManager.getServerIp()
        if (currentIp?.isNotBlank() == true) {
            binding.etServerIp.setText(currentIp)
        } else {
            // Usar IP padrão (10.14.250.214)
            val defaultIp = serverConfigManager.getDefaultIp()
            binding.etServerIp.setText(defaultIp)
            // Salvar IP padrão para uso imediato
            serverConfigManager.setServerIp(defaultIp)
        }

        // Mostrar versão do app
        try {
            binding.tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        } catch (e: Exception) {
            binding.tvVersion.text = "v${BuildConfig.VERSION_NAME}"
        }

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

        // Observar ViewModel
        observeViewModel()
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

        // Navegar para MainActivity se login bem-sucedido
        if (state.isLoginSuccessful) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}