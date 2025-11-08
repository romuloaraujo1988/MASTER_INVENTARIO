package com.inventario.mobile.ui.coleta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityColetaBinding
import com.inventario.mobile.presentation.scanner.ScannerActivity
import com.inventario.mobile.presentation.viewmodel.ColetaViewModel
import kotlinx.coroutines.launch

class ColetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityColetaBinding
    private val viewModel: ColetaViewModel by viewModels()
    
    private var salaId: Long = -1L
    private var salaNome: String = ""
    
    companion object {
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

        android.util.Log.d("ColetaActivity", "=== INICIANDO COLETA ACTIVITY ===")
        android.util.Log.d("ColetaActivity", "Sala ID recebida: $salaId")
        android.util.Log.d("ColetaActivity", "Sala Nome recebida: $salaNome")
        android.util.Log.d("ColetaActivity", "EXTRA_SALA_ID = $EXTRA_SALA_ID")
        android.util.Log.d("ColetaActivity", "EXTRA_SALA_NOME = $EXTRA_SALA_NOME")

        setupToolbar()
        setupUI()
        observeViewModel()
        
        // Configurar sala selecionada
        if (salaId != -1L && salaNome.isNotEmpty()) {
            android.util.Log.d("ColetaActivity", "Configurando sala no ViewModel...")
            viewModel.setSala(salaId, salaNome)
        } else {
            android.util.Log.e("ColetaActivity", "ERRO: Sala não foi recebida corretamente!")
            android.util.Log.e("ColetaActivity", "salaId: $salaId, salaNome: $salaNome")
        }
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ColetaUiState) {
        // Atualizar campos com dados do patrimônio
        binding.etCodigo.setText(state.patrimonioCodigo)
        binding.etDescricao.setText(state.patrimonioDescricao)
        binding.etLocalizacao.setText(state.localizacao)
        binding.etObservacoes.setText(state.observacoes)

        // Atualizar estado de loading
        binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
        
        // Habilitar botão salvar apenas se tiver patrimônio escaneado e não estiver carregando
        val hasPatrimonio = state.patrimonioCodigo.isNotEmpty()
        binding.btnSalvar.isEnabled = !state.isLoading && hasPatrimonio
        
        // Atualizar visibilidade dos campos de patrimônio
        if (hasPatrimonio) {
            binding.layoutPatrimonioInfo.visibility = android.view.View.VISIBLE
            binding.tvScanInstruction.visibility = android.view.View.GONE
        } else {
            binding.layoutPatrimonioInfo.visibility = android.view.View.GONE
            binding.tvScanInstruction.visibility = android.view.View.VISIBLE
        }

        // Mostrar mensagens de erro
        if (state.errorMessage != null) {
            Toast.makeText(this, state.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Verificar se salvou com sucesso
        if (state.isSaved) {
            Toast.makeText(this, "Coleta salva com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun openScanner() {
        val intent = Intent(this, ScannerActivity::class.java).apply {
            putExtra(ScannerActivity.EXTRA_ALLOW_COLLECTION, true)
        }
        scannerLauncher.launch(intent)
    }

    private fun salvarColeta() {
        val localizacao = binding.etLocalizacao.text.toString().trim()
        val observacoes = binding.etObservacoes.text.toString().trim()

        if (localizacao.isEmpty()) {
            binding.tilLocalizacao.error = "Localização é obrigatória"
            return
        }

        binding.tilLocalizacao.error = null
        viewModel.salvarColeta(localizacao, observacoes)
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