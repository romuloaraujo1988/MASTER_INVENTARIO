package com.inventario.mobile.presentation.export

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityExportPdfBinding
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Sala
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity para exportação de relatórios em PDF
 */
@AndroidEntryPoint
class ExportPdfActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityExportPdfBinding
    private val viewModel: ExportPdfViewModel by viewModels()
    
    private var salaAdapter: SalaFilterAdapter? = null
    private var currentFilePath: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSalaSelector()
        setupFilterOptions()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun setupSalaSelector() {
        binding.actvSala.setOnItemClickListener { _, _, position, _ ->
            salaAdapter?.getItem(position)?.let { sala ->
                viewModel.selectSala(sala)
            }
        }
        
        // Limpar seleção quando o texto é apagado
        binding.actvSala.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.actvSala.text.isNullOrBlank()) {
                viewModel.clearSalaSelection()
            }
        }
    }
    
    private fun setupFilterOptions() {
        binding.rgFilter.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.rbTodos -> ExportFilter.TODOS
                R.id.rbColetados -> ExportFilter.COLETADOS
                R.id.rbNaoColetados -> ExportFilter.NAO_COLETADOS
                else -> ExportFilter.TODOS
            }
            viewModel.selectFilter(filter)
        }
    }
    
    private fun setupButtons() {
        binding.btnExport.setOnClickListener {
            viewModel.generatePdf()
        }
        
        binding.btnOpen.setOnClickListener {
            currentFilePath?.let { path ->
                openPdf(path)
            }
        }
        
        binding.btnShare.setOnClickListener {
            currentFilePath?.let { path ->
                sharePdf(path)
            }
        }
    }
    
    private fun observeViewModel() {
        // Observar estado
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                handleState(state)
            }
        }
        
        // Observar permissão de exportar
        lifecycleScope.launch {
            viewModel.canExport.collectLatest { canExport ->
                binding.btnExport.isEnabled = canExport
            }
        }
        
        // Observar modo offline
        lifecycleScope.launch {
            viewModel.isOffline.collectLatest { isOffline ->
                binding.cardOffline.visibility = if (isOffline) View.VISIBLE else View.GONE
                // Desabilitar compartilhamento quando offline
                binding.btnShare.isEnabled = !isOffline
            }
        }
        
        // Observar lista de salas
        lifecycleScope.launch {
            viewModel.salas.collectLatest { salas ->
                updateSalaAdapter(salas)
            }
        }
    }
    
    private fun handleState(state: ExportPdfState) {
        when (state) {
            is ExportPdfState.Idle -> {
                showIdleState()
            }
            is ExportPdfState.LoadingSalas -> {
                showLoadingState("Carregando salas...")
            }
            is ExportPdfState.SalasLoaded -> {
                showIdleState()
            }
            is ExportPdfState.Generating -> {
                showLoadingState(state.message)
            }
            is ExportPdfState.Success -> {
                showSuccessState(state.result)
            }
            is ExportPdfState.Error -> {
                showErrorState(state.message)
            }
            is ExportPdfState.NoData -> {
                showNoDataState(state.message)
            }
        }
    }
    
    private fun showIdleState() {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.VISIBLE
    }
    
    private fun showLoadingState(message: String) {
        binding.llProgress.visibility = View.VISIBLE
        binding.tvProgress.text = message
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.GONE
    }
    
    private fun showSuccessState(result: ExportResult) {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.VISIBLE
        binding.btnExport.visibility = View.VISIBLE
        
        currentFilePath = result.filePath
        
        // Atualizar resumo
        val summary = buildString {
            append("Total: ${result.totalItems} itens\n")
            append("Coletados: ${result.coletados} (${String.format("%.1f", result.percentualColeta)}%)\n")
            append("Não Coletados: ${result.naoColetados} (${String.format("%.1f", 100 - result.percentualColeta)}%)")
        }
        binding.tvResultSummary.text = summary
    }
    
    private fun showErrorState(message: String) {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.VISIBLE
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Erro")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetToSalasLoaded()
            }
            .setNegativeButton("Tentar Novamente") { dialog, _ ->
                dialog.dismiss()
                viewModel.generatePdf()
            }
            .show()
    }
    
    private fun showNoDataState(message: String) {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.VISIBLE
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Nenhum Dado")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetToSalasLoaded()
            }
            .show()
    }
    
    private fun updateSalaAdapter(salas: List<Sala>) {
        salaAdapter = SalaFilterAdapter(this, salas)
        binding.actvSala.setAdapter(salaAdapter)
    }
    
    private fun openPdf(filePath: String) {
        try {
            val intent = viewModel.getOpenPdfIntent(filePath)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Visualizador não encontrado")
                .setMessage("Instale um aplicativo leitor de PDF para visualizar o arquivo.")
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sharePdf(filePath: String) {
        try {
            val intent = viewModel.getSharePdfIntent(filePath)
            startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao compartilhar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
