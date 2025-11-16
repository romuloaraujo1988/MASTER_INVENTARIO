package com.inventario.mobile.presentation.consulta

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.PatrimonioDetalhe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para exibir detalhes completos de um patrimônio
 */
@AndroidEntryPoint
class DetalhePatrimonioActivity : AppCompatActivity() {
    
    private val viewModel: ConsultaPatrimonioViewModel by viewModels()
    
    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var layoutError: LinearLayout
    
    // Dados básicos
    private lateinit var tvCodigo: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvMarca: TextView
    private lateinit var tvModelo: TextView
    private lateinit var tvNumeroSerie: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvValor: TextView
    private lateinit var tvObservacoes: TextView
    
    // Localização
    private lateinit var tvLocalizacao: TextView
    private lateinit var tvSalaBloco: TextView
    private lateinit var tvSalaAndar: TextView
    
    // Responsável
    private lateinit var tvResponsavel: TextView
    private lateinit var tvResponsavelMatricula: TextView
    private lateinit var tvResponsavelSetor: TextView
    
    // Status de coleta
    private lateinit var cardColeta: androidx.cardview.widget.CardView
    private lateinit var tvStatusColeta: TextView
    private lateinit var tvDataColeta: TextView
    private lateinit var tvColetadoPor: TextView
    
    // Divergências
    private lateinit var cardDivergencias: androidx.cardview.widget.CardView
    private lateinit var tvDivergencias: TextView
    
    private var patrimonioId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_patrimonio)
        
        patrimonioId = intent.getIntExtra("patrimonioId", 0)
        val patrimonioCodigo = intent.getStringExtra("patrimonioCodigo") ?: ""
        
        title = "Patrimônio $patrimonioCodigo"
        
        setupViews()
        setupObservers()
        
        if (patrimonioId > 0) {
            viewModel.obterDetalhes(patrimonioId)
        } else {
            showError("ID do patrimônio inválido")
        }
    }
    
    private fun setupViews() {
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView)
        layoutError = findViewById(R.id.layoutError)
        
        // Dados básicos
        tvCodigo = findViewById(R.id.tvCodigo)
        tvDescricao = findViewById(R.id.tvDescricao)
        tvMarca = findViewById(R.id.tvMarca)
        tvModelo = findViewById(R.id.tvModelo)
        tvNumeroSerie = findViewById(R.id.tvNumeroSerie)
        tvEstado = findViewById(R.id.tvEstado)
        tvValor = findViewById(R.id.tvValor)
        tvObservacoes = findViewById(R.id.tvObservacoes)
        
        // Localização
        tvLocalizacao = findViewById(R.id.tvLocalizacao)
        tvSalaBloco = findViewById(R.id.tvSalaBloco)
        tvSalaAndar = findViewById(R.id.tvSalaAndar)
        
        // Responsável
        tvResponsavel = findViewById(R.id.tvResponsavel)
        tvResponsavelMatricula = findViewById(R.id.tvResponsavelMatricula)
        tvResponsavelSetor = findViewById(R.id.tvResponsavelSetor)
        
        // Status de coleta
        cardColeta = findViewById(R.id.cardColeta)
        tvStatusColeta = findViewById(R.id.tvStatusColeta)
        tvDataColeta = findViewById(R.id.tvDataColeta)
        tvColetadoPor = findViewById(R.id.tvColetadoPor)
        
        // Divergências
        cardDivergencias = findViewById(R.id.cardDivergencias)
        tvDivergencias = findViewById(R.id.tvDivergencias)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.detalheState.collect { state ->
                when (state) {
                    is DetalheState.Idle -> {
                        // Nada a fazer
                    }
                    is DetalheState.Loading -> {
                        showLoading()
                    }
                    is DetalheState.Success -> {
                        showSuccess(state.detalhe)
                    }
                    is DetalheState.Error -> {
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        layoutError.visibility = View.GONE
    }
    
    private fun showSuccess(detalhe: PatrimonioDetalhe) {
        progressBar.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
        
        // Dados básicos
        tvCodigo.text = detalhe.codigo
        tvDescricao.text = detalhe.descricao
        tvMarca.text = detalhe.marca ?: "Não informado"
        tvModelo.text = detalhe.modelo ?: "Não informado"
        tvNumeroSerie.text = detalhe.numeroSerie ?: "Não informado"
        tvEstado.text = detalhe.estado ?: "Não informado"
        tvValor.text = detalhe.getValorFormatado()
        tvObservacoes.text = detalhe.observacoes ?: "Sem observações"
        
        // Localização
        tvLocalizacao.text = detalhe.getLocalizacaoCompleta()
        tvSalaBloco.text = detalhe.salaBloco ?: "Não informado"
        tvSalaAndar.text = detalhe.salaAndar ?: "Não informado"
        
        // Responsável
        tvResponsavel.text = detalhe.getResponsavelCompleto()
        tvResponsavelMatricula.text = detalhe.responsavelMatricula ?: "Não informado"
        tvResponsavelSetor.text = detalhe.responsavelSetor ?: "Não informado"
        
        // Status de coleta
        if (detalhe.foiColetado()) {
            cardColeta.visibility = View.VISIBLE
            tvStatusColeta.text = detalhe.getStatusColetaCompleto()
            tvDataColeta.text = "Data: ${detalhe.dataColeta}"
            tvColetadoPor.text = "Por: ${detalhe.coletadoPor ?: "Não informado"}"
        } else {
            cardColeta.visibility = View.GONE
        }
        
        // Divergências
        if (detalhe.temDivergencias()) {
            cardDivergencias.visibility = View.VISIBLE
            val divergencias = detalhe.getDivergencias()
            tvDivergencias.text = divergencias.joinToString("\n\n")
        } else {
            cardDivergencias.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        scrollView.visibility = View.GONE
        layoutError.visibility = View.VISIBLE
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
