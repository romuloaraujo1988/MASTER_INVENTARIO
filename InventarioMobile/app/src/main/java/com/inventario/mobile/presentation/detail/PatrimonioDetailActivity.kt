package com.inventario.mobile.presentation.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.databinding.ActivityPatrimonioDetailBinding
import com.inventario.mobile.domain.model.PatrimonioDetalhe
import com.inventario.mobile.ui.coleta.ColetaActivity
import com.inventario.mobile.presentation.state.PatrimonioDetailState
import com.inventario.mobile.presentation.viewmodel.PatrimonioDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para exibir detalhes completos de um patrimônio
 * 
 * @see Requirements 2.2, 2.3, 2.4, 2.5, 5.1, 5.2, 5.3
 */
@AndroidEntryPoint
class PatrimonioDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatrimonioDetailBinding
    private val viewModel: PatrimonioDetailViewModel by viewModels()
    
    // Armazena o patrimônio atual para uso no dialog
    private var patrimonioAtual: PatrimonioDetalhe? = null
    
    companion object {
        const val EXTRA_PATRIMONIO_ID = "patrimonio_id"
        private const val TAG = "PatrimonioDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatrimonioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        observeViewModel()
        
        // Carregar detalhes do patrimônio
        val patrimonioId = intent.getIntExtra(EXTRA_PATRIMONIO_ID, -1)
        if (patrimonioId > 0) {
            viewModel.carregarDetalhes(patrimonioId)
        } else {
            showError("ID do patrimônio inválido")
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        binding.btnColetar.setOnClickListener {
            navegarParaColeta()
        }
        
        binding.btnVerColeta.setOnClickListener {
            mostrarDetalhesColeta()
        }
        
        binding.btnRetry.setOnClickListener {
            val patrimonioId = intent.getIntExtra(EXTRA_PATRIMONIO_ID, -1)
            if (patrimonioId > 0) {
                viewModel.carregarDetalhes(patrimonioId)
            }
        }
    }
    
    /**
     * Mostra um dialog com os detalhes completos da coleta
     */
    private fun mostrarDetalhesColeta() {
        val patrimonio = patrimonioAtual ?: return
        
        if (!patrimonio.coletado) {
            Toast.makeText(this, "Este patrimônio ainda não foi coletado", Toast.LENGTH_SHORT).show()
            return
        }
        
        val detalhes = buildString {
            appendLine("📦 Patrimônio: ${patrimonio.codigo}")
            appendLine("📝 Descrição: ${patrimonio.descricao}")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📋 INFORMAÇÕES DA COLETA")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📅 Data: ${patrimonio.dataColeta?.let { formatDate(it) } ?: "N/A"}")
            appendLine("👤 Coletado por: ${patrimonio.coletadoPor ?: "N/A"}")
            appendLine()
            appendLine("📍 Local encontrado: ${patrimonio.localizacaoEncontrada ?: "N/A"}")
            appendLine("🔧 Estado encontrado: ${patrimonio.estadoEncontrado ?: "N/A"}")
            appendLine()
            appendLine("💬 Observações: ${patrimonio.observacoesColeta ?: "Nenhuma"}")
            
            // Mostrar divergências se houver
            if (patrimonio.temDivergencias()) {
                appendLine()
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("⚠️ DIVERGÊNCIAS ENCONTRADAS")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                patrimonio.getDivergencias().forEach { divergencia ->
                    appendLine("• $divergencia")
                }
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("✅ Detalhes da Coleta")
            .setMessage(detalhes)
            .setPositiveButton("Fechar", null)
            .show()
    }
    
    private fun formatDate(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PatrimonioDetailState.Loading -> showLoading()
                    is PatrimonioDetailState.Success -> showContent(state.patrimonio)
                    is PatrimonioDetailState.Error -> showError(state.message)
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }
    
    private fun showContent(patrimonio: PatrimonioDetalhe) {
        binding.progressBar.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
        
        // Armazenar patrimônio atual para uso no dialog
        patrimonioAtual = patrimonio
        
        // Informações básicas
        binding.tvNumero.text = patrimonio.codigo
        binding.tvDescricao.text = patrimonio.descricao
        binding.tvMarca.text = patrimonio.marca ?: "N/A"
        binding.tvModelo.text = patrimonio.modelo ?: "N/A"
        binding.tvEstado.text = patrimonio.estado ?: "N/A"
        binding.tvValor.text = patrimonio.getValorFormatado()
        
        // Localização
        binding.tvSala.text = patrimonio.getLocalizacaoCompleta()
        binding.tvSetor.text = patrimonio.responsavelSetor ?: "N/A"
        
        // Responsável
        binding.tvResponsavel.text = patrimonio.getResponsavelCompleto()
        
        // Status de coleta
        binding.tvStatusColeta.text = patrimonio.getStatusColetaCompleto()
        
        // Informações adicionais da coleta
        if (patrimonio.coletado) {
            binding.layoutInfoColeta.visibility = View.VISIBLE
            binding.tvLocalizacaoEncontrada.text = "Local encontrado: ${patrimonio.localizacaoEncontrada ?: "N/A"}"
            binding.tvEstadoEncontrado.text = "Estado encontrado: ${patrimonio.estadoEncontrado ?: "N/A"}"
            binding.tvObservacoesColeta.text = "Observações: ${patrimonio.observacoesColeta ?: "Nenhuma"}"
            
            // Botões de ação
            binding.btnColetar.visibility = View.GONE
            binding.btnVerColeta.visibility = View.VISIBLE
        } else {
            binding.layoutInfoColeta.visibility = View.GONE
            
            // Botões de ação
            binding.btnColetar.visibility = View.VISIBLE
            binding.btnVerColeta.visibility = View.GONE
        }
        
        // Divergências
        if (patrimonio.temDivergencias()) {
            binding.tvDivergencia.visibility = View.VISIBLE
            val divergencias = patrimonio.getDivergencias()
            binding.tvDivergencia.text = "⚠️ ${divergencias.joinToString("\n")}"
        } else {
            binding.tvDivergencia.visibility = View.GONE
        }
        
        // Atualizar título
        supportActionBar?.title = "Patrimônio ${patrimonio.codigo}"
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.scrollView.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }
    
    private fun navegarParaColeta() {
        val patrimonioId = viewModel.getPatrimonioId()
        val numeroPatrimonio = viewModel.getNumeroPatrimonio()
        
        if (patrimonioId != null && numeroPatrimonio != null) {
            val intent = Intent(this, ColetaActivity::class.java).apply {
                putExtra("PATRIMONIO_ID", patrimonioId)
                putExtra("NUMERO_PATRIMONIO", numeroPatrimonio)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Erro ao iniciar coleta", Toast.LENGTH_SHORT).show()
        }
    }
}
