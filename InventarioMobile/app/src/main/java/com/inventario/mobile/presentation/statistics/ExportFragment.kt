package com.inventario.mobile.presentation.statistics

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inventario.mobile.R
import com.inventario.mobile.databinding.FragmentStatisticsExportBinding
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.ExportResult
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.presentation.export.SalaFilterAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment para exportar relatórios em PDF, Excel ou CSV
 */
@AndroidEntryPoint
class ExportFragment : Fragment() {

    private var _binding: FragmentStatisticsExportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ExportViewModel by viewModels()
    
    private var salaAdapter: SalaFilterAdapter? = null
    private var idInventario: Int = 0
    
    companion object {
        private const val ARG_INVENTARIO_ID = "inventario_id"
        
        fun newInstance(idInventario: Int) = ExportFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INVENTARIO_ID, idInventario)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idInventario = it.getInt(ARG_INVENTARIO_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSalaSelector()
        setupFilterOptions()
        setupFormatOptions()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupSalaSelector() {
        binding.actvSala.setOnItemClickListener { _, _, position, _ ->
            salaAdapter?.getItem(position)?.let { sala ->
                viewModel.selectSala(sala)
            }
        }
        
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
    
    private fun setupFormatOptions() {
        binding.rgFormat.setOnCheckedChangeListener { _, checkedId ->
            val format = when (checkedId) {
                R.id.rbPdf -> ExportFormat.PDF
                R.id.rbExcel -> ExportFormat.EXCEL
                R.id.rbCsv -> ExportFormat.CSV
                else -> ExportFormat.PDF
            }
            viewModel.selectFormat(format)
        }
    }
    
    private fun setupButtons() {
        binding.btnExport.setOnClickListener {
            viewModel.generateReport()
        }
        
        binding.btnOpen.setOnClickListener {
            openFile()
        }
        
        binding.btnShare.setOnClickListener {
            shareFile()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                handleState(state)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.canExport.collectLatest { canExport ->
                binding.btnExport.isEnabled = canExport
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isOffline.collectLatest { isOffline ->
                binding.cardOffline.visibility = if (isOffline) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.salas.collectLatest { salas ->
                updateSalaAdapter(salas)
            }
        }
    }
    
    private fun handleState(state: ExportState) {
        when (state) {
            is ExportState.Idle -> showIdleState()
            is ExportState.LoadingSalas -> showLoadingState("Carregando salas...")
            is ExportState.SalasLoaded -> showIdleState()
            is ExportState.Generating -> showLoadingState(state.message)
            is ExportState.Success -> showSuccessState(state.result)
            is ExportState.Error -> showErrorState(state.message)
            is ExportState.NoData -> showNoDataState(state.message)
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
        
        val summary = buildString {
            append("📄 ${result.fileName}\n\n")
            append("Total: ${result.totalItems} itens\n")
            append("✅ Coletados: ${result.coletados} (${String.format("%.1f", result.percentualColeta)}%)\n")
            append("⏳ Pendentes: ${result.naoColetados} (${String.format("%.1f", 100 - result.percentualColeta)}%)")
        }
        binding.tvResultSummary.text = summary
    }
    
    private fun showErrorState(message: String) {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.VISIBLE
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Erro")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetToSalasLoaded()
            }
            .setNegativeButton("Tentar Novamente") { dialog, _ ->
                dialog.dismiss()
                viewModel.generateReport()
            }
            .show()
    }
    
    private fun showNoDataState(message: String) {
        binding.llProgress.visibility = View.GONE
        binding.cardResult.visibility = View.GONE
        binding.btnExport.visibility = View.VISIBLE
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nenhum Dado")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetToSalasLoaded()
            }
            .show()
    }
    
    private fun updateSalaAdapter(salas: List<Sala>) {
        salaAdapter = SalaFilterAdapter(requireContext(), salas)
        binding.actvSala.setAdapter(salaAdapter)
    }
    
    private fun openFile() {
        try {
            val intent = viewModel.getOpenFileIntent()
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Nenhum arquivo para abrir", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ActivityNotFoundException) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Aplicativo não encontrado")
                .setMessage("Instale um aplicativo para visualizar este tipo de arquivo.")
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao abrir arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareFile() {
        try {
            val intent = viewModel.getShareFileIntent()
            if (intent != null) {
                startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
            } else {
                Toast.makeText(requireContext(), "Nenhum arquivo para compartilhar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
