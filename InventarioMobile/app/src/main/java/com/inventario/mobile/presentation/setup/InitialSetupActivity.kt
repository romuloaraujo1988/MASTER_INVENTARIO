package com.inventario.mobile.presentation.setup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.BuildConfig
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityInitialSetupBinding
import com.inventario.mobile.presentation.login.LoginActivity
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.ServerConfigManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela de Configuração Inicial do Servidor (v2.29.5)
 *
 * Exibida na primeira utilização do app, antes da tela de login. Permite que
 * o usuário configure o IP do servidor SIHCP em vez de receber um IP fixo
 * embutido no APK.
 *
 * Fluxo:
 * 1. Usuário informa IP e porta
 * 2. Usuário clica em "Testar Conexão"
 * 3. Se a conexão for bem-sucedida, o botão "Continuar" é habilitado
 * 4. Ao continuar, a configuração é salva e a tela de login é exibida
 *
 * Após configurado, o flag `isServerConfigured()` em [PreferencesManager]
 * é marcado como `true` e esta tela não é mais exibida automaticamente.
 *
 * Para alterar o IP posteriormente, o usuário usa o campo de IP na própria
 * tela de login.
 */
@AndroidEntryPoint
class InitialSetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "InitialSetupActivity"
    }

    private lateinit var binding: ActivityInitialSetupBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var serverConfigManager: ServerConfigManager

    /** Indica se o último teste de conexão foi bem-sucedido. */
    private var lastTestSuccessful = false

    /** Configuração testada com sucesso (ip, port). Reset quando o usuário edita os campos. */
    private var testedIp: String? = null
    private var testedPort: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        preferencesManager = PreferencesManager(this)
        serverConfigManager = ServerConfigManager.getInstance(this)

        // Pré-preencher com último IP usado (caso esteja "reconfigurando")
        val currentIp = serverConfigManager.getServerIp()
        if (!currentIp.isNullOrBlank()) {
            binding.etServerIp.setText(currentIp)
        }
        binding.etServerPort.setText(serverConfigManager.getServerPort().toString())

        // Versão do app
        binding.tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        // Listeners para invalidar teste quando os campos mudam
        val resetTestWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                resetTestState()
            }
        }
        binding.etServerIp.addTextChangedListener(resetTestWatcher)
        binding.etServerPort.addTextChangedListener(resetTestWatcher)

        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        binding.btnContinue.setOnClickListener {
            continueToLogin()
        }
    }

    /**
     * Reseta o estado do teste quando os campos são editados.
     * Força o usuário a testar novamente antes de continuar.
     */
    private fun resetTestState() {
        if (lastTestSuccessful) {
            Log.d(TAG, "Campos editados, resetando estado do teste")
            lastTestSuccessful = false
            testedIp = null
            testedPort = null
            binding.btnContinue.isEnabled = false
            binding.llTestStatus.visibility = View.GONE
        }
    }

    /**
     * Valida os campos e testa a conexão com o servidor.
     */
    private fun testConnection() {
        val ip = binding.etServerIp.text?.toString()?.trim().orEmpty()
        val portText = binding.etServerPort.text?.toString()?.trim().orEmpty()

        // Validar IP
        if (ip.isBlank()) {
            binding.tilServerIp.error = getString(R.string.initial_setup_error_empty_ip)
            return
        }
        if (!ServerConfigManager.isValidIp(ip)) {
            binding.tilServerIp.error = getString(R.string.initial_setup_error_invalid_ip)
            return
        }
        binding.tilServerIp.error = null

        // Validar porta
        val port = portText.toIntOrNull()
        if (port == null || port !in 1..65535) {
            binding.tilServerPort.error = getString(R.string.initial_setup_error_invalid_port)
            return
        }
        binding.tilServerPort.error = null

        // Mostrar status: testando…
        showTestingStatus(ip, port)

        lifecycleScope.launch {
            try {
                // Salvar temporariamente para que o testServerConnectivity use os valores certos
                serverConfigManager.setServerIp(ip, port = port, useHttps = false)

                val result = serverConfigManager.testServerConnectivity()

                if (result.success) {
                    showSuccessStatus(result.responseTime)
                    lastTestSuccessful = true
                    testedIp = ip
                    testedPort = port
                    binding.btnContinue.isEnabled = true
                } else {
                    showErrorStatus(result.message ?: "Erro desconhecido")
                    lastTestSuccessful = false
                    binding.btnContinue.isEnabled = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao testar conexão", e)
                showErrorStatus(e.message ?: "Erro inesperado")
                lastTestSuccessful = false
                binding.btnContinue.isEnabled = false
            }
        }
    }

    private fun showTestingStatus(ip: String, port: Int) {
        binding.llTestStatus.visibility = View.VISIBLE
        binding.pbTestProgress.visibility = View.VISIBLE
        binding.ivTestIcon.visibility = View.GONE
        binding.tvTestStatus.text = getString(R.string.initial_setup_testing, "$ip:$port")
        binding.tvTestStatus.setTextColor(getColor(android.R.color.darker_gray))
        binding.btnTestConnection.isEnabled = false
    }

    private fun showSuccessStatus(responseTime: Long) {
        binding.pbTestProgress.visibility = View.GONE
        binding.ivTestIcon.visibility = View.VISIBLE
        binding.ivTestIcon.setImageResource(android.R.drawable.checkbox_on_background)
        binding.ivTestIcon.imageTintList = android.content.res.ColorStateList.valueOf(
            getColor(android.R.color.holo_green_dark)
        )
        binding.tvTestStatus.text = getString(R.string.initial_setup_success, responseTime)
        binding.tvTestStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        binding.btnTestConnection.isEnabled = true
    }

    private fun showErrorStatus(message: String) {
        binding.pbTestProgress.visibility = View.GONE
        binding.ivTestIcon.visibility = View.VISIBLE
        binding.ivTestIcon.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.ivTestIcon.imageTintList = android.content.res.ColorStateList.valueOf(
            getColor(android.R.color.holo_red_dark)
        )
        binding.tvTestStatus.text = getString(R.string.initial_setup_error_test_failed, message)
        binding.tvTestStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        binding.btnTestConnection.isEnabled = true
    }

    /**
     * Salva a configuração e navega para o LoginActivity.
     *
     * Requer que o teste de conexão tenha sido bem-sucedido.
     */
    private fun continueToLogin() {
        if (!lastTestSuccessful || testedIp == null || testedPort == null) {
            Snackbar.make(
                binding.root,
                R.string.initial_setup_error_test_first,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        Log.d(TAG, "Salvando configuração: IP=$testedIp, Port=$testedPort")

        // Salvar configuração definitiva
        serverConfigManager.setServerIp(testedIp!!, port = testedPort!!, useHttps = false)
        preferencesManager.setServerConfigured(true)

        // Forçar recriação do ApiService com a nova URL
        com.inventario.mobile.data.remote.api.ApiClient.recreateApiService(this)

        // Navegar para LoginActivity
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Não permitir voltar para evitar que o usuário entre no app sem
     * configurar o servidor.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // Bloqueia o back: a configuração é obrigatória.
        // Se o usuário insistir, ele pode fechar o app via task switcher.
        Snackbar.make(
            binding.root,
            "Configure o servidor para continuar",
            Snackbar.LENGTH_SHORT
        ).show()
    }
}
