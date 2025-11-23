package com.inventario.mobile.ui.coleta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityColetaBinding
import com.inventario.mobile.presentation.coleta.ColetaViewModelClean
import com.inventario.mobile.presentation.scanner.ScannerActivity
import com.inventario.mobile.presentation.state.ColetaState
import com.inventario.mobile.ui.base.BaseOfflineActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para registro de coleta de patrimônio
 * Clean Architecture + MVVM + Hilt + Modo Offline Automático
 */
@AndroidEntryPoint
class ColetaActivity : BaseOfflineActivity() {

    private lateinit var binding: ActivityColetaBinding
    
    // ViewModel injetado via Hilt
    private val viewModel: ColetaViewModelClean by viewModels()
    
    private var salaId: Long = -1L
    private var salaNome: String = ""
    
    companion object {
        private const val TAG = "ColetaActivity"
        const val EXTRA_SALA_ID = "extra_sala_id"
        const val EXTRA_SALA_NOME = "extra_sala_nome"
    }

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrResult = result.data?.getStringExtra(ScannerActivity.EXTRA_QR_RESULT)
            val patrimonioId = result.data?.getLongExtra(ScannerActivity.EXTRA_PATRIMONIO_ID, -1L) ?: -1L
            val patrimonioCodigo = result.data?.getStringExtra(ScannerActivity.EXTRA_PATRIMONIO_CODIGO)
            
            if (patrimonioId != -1L && patrimonioCodigo != null) {
                viewModel.setPatrimonio(patrimonioId, patrimonioCodigo)
                Toast.makeText(this, "Patrimônio $patrimonioCodigo escaneado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao processar QR Code", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Scan cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receber dados da sala selecionada
        salaId = intent.getLongExtra(EXTRA_SALA_ID, -1L)
        salaNome = intent.getStringExtra(EXTRA_SALA_NOME) ?: ""

        android.util.Log.d(TAG, "=== INICIANDO COLETA ACTIVITY ===")
        android.util.Log.d(TAG, "Sala ID recebida: $salaId")
        android.util.Log.d(TAG, "Sala Nome recebida: $salaNome")
        
        setupToolbar()
        setupUI()
        setupObservers()
        
        // Configurar sala selecionada
        if (salaId != -1L && salaNome.isNotEmpty()) {
            android.util.Log.d(TAG, "Configurando sala...")
            configurarSala()
        } else {
            android.util.Log.e(TAG, "ERRO: Sala não foi recebida corretamente!")
        }
    }
    
    /**
     * Observa mudanças de estado do ViewModel
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ColetaState.Idle -> {
                        hideLoading()
                    }
                    is ColetaState.Loading -> {
                        showLoading()
                    }
                    is ColetaState.Success -> {
                        hideLoading()
                        handleColetaSuccess(state.coleta)
                    }
                    is ColetaState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    /**
     * Configura sala
     */
    private fun configurarSala() {
        binding.tvSalaInfo.text = "Sala: $salaNome"
    }
    
    /**
     * Mostra loading
     */
    private fun showLoading() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSalvar.isEnabled = false
    }
    
    /**
     * Esconde loading
     */
    private fun hideLoading() {
        binding.progressBar.visibility = android.view.View.GONE
    }
    
    /**
     * Mostra erro
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Trata sucesso da coleta (Clean Architecture)
     */
    private fun handleColetaSuccess(coleta: com.inventario.mobile.domain.model.Coleta) {
        Toast.makeText(this, "✅ Coleta registrada com sucesso!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (salaNome.isNotEmpty()) {
                "Coleta - $salaNome"
            } else {
                getString(R.string.coleta_title)
            }
        }
    }

    private fun setupUI() {
        // Mostrar informações da sala
        binding.tvSalaInfo.text = "Sala: $salaNome"
        
        binding.btnScanQr.setOnClickListener {
            openScanner()
        }

        binding.btnSalvar.setOnClickListener {
            salvarColeta()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
        
        // Inicialmente desabilitar botão salvar até escanear um patrimônio
        binding.btnSalvar.isEnabled = false
    }

    private fun openScanner() {
        val intent = Intent(this, ScannerActivity::class.java).apply {
            putExtra(ScannerActivity.EXTRA_ALLOW_COLLECTION, true)
        }
        scannerLauncher.launch(intent)
    }

    private fun salvarColeta() {
        val observacoes = binding.etObservacoes.text.toString().trim()
        val numeroPatrimonio = binding.etCodigo.text.toString().trim()

        if (numeroPatrimonio.isEmpty()) {
            Toast.makeText(this, "Escaneie um patrimônio primeiro", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar se sala foi selecionada
        if (salaNome.isEmpty()) {
            Toast.makeText(this, "Erro: Sala não selecionada", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Obter ID do usuário logado do PreferencesManager
        val idUsuario = 1L // Placeholder
        
        // CORREÇÃO: Usar salaNome ao invés de campo editável
        viewModel.registrarColeta(
            numeroPatrimonio = numeroPatrimonio,
            localizacaoAtual = salaNome, // ← Usar nome da sala selecionada
            observacoes = observacoes,
            latitude = null,
            longitude = null,
            idUsuario = idUsuario
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // ========== CALLBACKS DE CONECTIVIDADE ==========
    
    /**
     * Chamado quando a conexão é restaurada
     * Tenta sincronizar coletas pendentes
     */
    override fun onConnectivityRestored() {
        Log.d(TAG, "✓ Conexão restaurada! Verificando coletas pendentes...")
        
        val pendingCount = getPendingCollectionsCount()
        
        if (pendingCount > 0) {
            // Mostrar Snackbar com opção de sincronizar
            Snackbar.make(
                binding.root,
                "Conexão restaurada. $pendingCount coleta(s) pendente(s).",
                Snackbar.LENGTH_LONG
            ).setAction("Sincronizar") {
                // Abrir tela de sincronização
                val intent = Intent(this, com.inventario.mobile.presentation.sync.SyncActivity::class.java)
                intent.putExtra("AUTO_SYNC", true)
                startActivity(intent)
            }.show()
        } else {
            Snackbar.make(
                binding.root,
                "Conexão restaurada.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Chamado quando a conexão é perdida
     * Avisa que coletas serão salvas localmente
     */
    override fun onConnectivityLost() {
        Log.d(TAG, "⚠️ Conexão perdida! Coletas serão salvas localmente...")
        
        // Mostrar Snackbar informativo
        Snackbar.make(
            binding.root,
            "Sem conexão. Coletas serão salvas localmente e sincronizadas depois.",
            Snackbar.LENGTH_LONG
        ).setAction("OK") {
            // Dismiss
        }.show()
    }
}

data class ColetaUiState(
    val isLoading: Boolean = false,
    val patrimonioCodigo: String = "",
    val patrimonioDescricao: String = "",
    val localizacao: String = "",
    val observacoes: String = "",
    val salaId: Long = -1L,
    val salaNome: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)