package com.inventario.mobile.presentation.consulta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.PatrimonioConsulta
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity para consulta de patrimônios
 * 
 * Permite buscar patrimônios por:
 * - Código parcial
 * - Descrição
 * - Busca avançada (termo + filtros)
 */
@AndroidEntryPoint
class ConsultaPatrimonioActivity : AppCompatActivity() {
    
    private val viewModel: ConsultaPatrimonioViewModel by viewModels()
    
    // Views
    private lateinit var spinnerTipoBusca: Spinner
    private lateinit var edtBusca: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnLimpar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvSugestoes: TextView
    
    // Adapter
    private lateinit var adapter: PatrimonioConsultaAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_patrimonio)
        
        setupViews()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }
    
    private fun setupViews() {
        spinnerTipoBusca = findViewById(R.id.spinnerTipoBusca)
        edtBusca = findViewById(R.id.edtBusca)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnLimpar = findViewById(R.id.btnLimpar)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        recyclerView = findViewById(R.id.recyclerView)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        tvSugestoes = findViewById(R.id.tvSugestoes)
        
        // Configurar spinner de tipo de busca
        val tiposBusca = arrayOf("Código", "Descrição", "Busca Avançada")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposBusca)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoBusca.adapter = spinnerAdapter
    }
    
    private fun setupRecyclerView() {
        adapter = PatrimonioConsultaAdapter { patrimonio ->
            abrirDetalhes(patrimonio)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        btnBuscar.setOnClickListener {
            realizarBusca()
        }
        
        btnLimpar.setOnClickListener {
            limparBusca()
        }
        
        // Buscar ao pressionar Enter
        edtBusca.setOnEditorActionListener { _, _, _ ->
            realizarBusca()
            true
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.consultaState.collect { state ->
                when (state) {
                    is ConsultaState.Idle -> {
                        showIdle()
                    }
                    is ConsultaState.Loading -> {
                        showLoading(state.message)
                    }
                    is ConsultaState.Success -> {
                        showSuccess(state)
                    }
                    is ConsultaState.Error -> {
                        showError(state.message)
                    }
                    is ConsultaState.Empty -> {
                        showEmpty(state)
                    }
                }
            }
        }
    }
    
    private fun realizarBusca() {
        val termo = edtBusca.text.toString().trim()
        
        if (termo.isEmpty()) {
            Toast.makeText(this, "Digite algo para buscar", Toast.LENGTH_SHORT).show()
            return
        }
        
        when (spinnerTipoBusca.selectedItemPosition) {
            0 -> { // Código
                if (viewModel.isCodigoValido(termo)) {
                    viewModel.buscarPorCodigo(termo)
                } else {
                    Toast.makeText(
                        this,
                        "Código deve ter ao menos 2 caracteres alfanuméricos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            1 -> { // Descrição
                if (viewModel.isDescricaoValida(termo)) {
                    viewModel.buscarPorDescricao(termo)
                } else {
                    Toast.makeText(
                        this,
                        "Descrição deve ter ao menos 3 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            2 -> { // Busca Avançada
                viewModel.buscarAvancada(termo)
            }
        }
    }
    
    private fun limparBusca() {
        edtBusca.text.clear()
        viewModel.limparConsulta()
    }
    
    private fun abrirDetalhes(patrimonio: PatrimonioConsulta) {
        val intent = Intent(this, DetalhePatrimonioActivity::class.java)
        intent.putExtra("patrimonioId", patrimonio.id)
        intent.putExtra("patrimonioCodigo", patrimonio.codigo)
        startActivity(intent)
    }
    
    private fun showIdle() {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Digite algo para buscar"
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
    }
    
    private fun showLoading(message: String) {
        progressBar.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = message
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        btnBuscar.isEnabled = false
    }
    
    private fun showSuccess(state: ConsultaState.Success) {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Encontrados ${state.patrimonios.size} patrimônios"
        recyclerView.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        btnBuscar.isEnabled = true
        
        adapter.submitList(state.patrimonios)
    }
    
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Erro: $message"
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        tvEmptyMessage.text = "Erro ao buscar patrimônios"
        tvSugestoes.text = message
        btnBuscar.isEnabled = true
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showEmpty(state: ConsultaState.Empty) {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Nenhum resultado encontrado"
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        tvEmptyMessage.text = "Nenhum patrimônio encontrado para '${state.termoBusca}'"
        
        if (state.sugestoes.isNotEmpty()) {
            tvSugestoes.visibility = View.VISIBLE
            tvSugestoes.text = "Sugestões:\n" + state.sugestoes.joinToString("\n• ", "• ")
        } else {
            tvSugestoes.visibility = View.GONE
        }
        
        btnBuscar.isEnabled = true
    }
}
