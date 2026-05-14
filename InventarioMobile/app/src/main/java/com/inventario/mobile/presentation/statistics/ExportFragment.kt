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
import com.inventario.mobile.presentation.state.RelatorioFotoState
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    /** PreferencesManager injetado via Hilt para descobrir o perfil do usuário. */
    @Inject lateinit var preferencesManager: PreferencesManager

    /**
     * Último formato disparado para o relatório fotográfico (PDF ou EXCEL).
     * Usado para escolher o MIME correto ao abrir o arquivo após o download.
     */
    private var ultimoFormatoRelatorioFoto: ExportFormat = ExportFormat.PDF
    
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
        setupRelatorioFotoCard()
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

    /**
     * Configura o card do Relatório Fotográfico — itens sem etiqueta.
     *
     * - Carrega a contagem de fotos via servidor (`/info`) ao abrir a tela
     *   se houver inventário ativo.
     * - Os botões de download (PDF e Excel) são habilitados apenas para
     *   SUPERVISOR e ADMIN; para outros perfis, mostra um aviso e desabilita
     *   ambos os botões.
     * - Os endpoints requerem rede — não funcionam offline.
     *
     * Spec: relatorio-fotografico-sem-etiqueta (Req 1.1, 2.1, 2.2, 3.1, 3.5)
     */
    private fun setupRelatorioFotoCard() {
        val perfil = preferencesManager.getUserProfile()
        val temPermissao = perfil == "SUPERVISOR" || perfil == "ADMIN"

        binding.btnRelatorioFotoPdf.isEnabled = temPermissao
        binding.btnRelatorioFotoExcel.isEnabled = temPermissao
        binding.tvRelatorioFotoPermissao.visibility =
            if (temPermissao) View.GONE else View.VISIBLE

        binding.btnRelatorioFotoPdf.setOnClickListener {
            iniciarDownloadRelatorioFoto(temPermissao, ExportFormat.PDF)
        }
        binding.btnRelatorioFotoExcel.setOnClickListener {
            iniciarDownloadRelatorioFoto(temPermissao, ExportFormat.EXCEL)
        }

        // Carregar contagem de fotos para o badge informativo
        val invId = idInventario.takeIf { it > 0 }
            ?: preferencesManager.getInventarioAtivoId()
        if (invId != null && invId > 0) {
            viewModel.carregarInfoRelatorioFoto(invId)
        }
    }

    /**
     * Resolve o `inventarioId` ativo e dispara o download do relatório
     * fotográfico no formato escolhido. Mantém o último formato disparado
     * em [ultimoFormatoRelatorioFoto] para que [handleRelatorioFotoState]
     * abra o arquivo certo após o download.
     */
    private fun iniciarDownloadRelatorioFoto(temPermissao: Boolean, formato: ExportFormat) {
        if (!temPermissao) return
        val invId = idInventario.takeIf { it > 0 }
            ?: preferencesManager.getInventarioAtivoId()
        if (invId != null && invId > 0) {
            ultimoFormatoRelatorioFoto = formato
            viewModel.baixarRelatorioFoto(invId, formato)
        } else {
            Toast.makeText(
                requireContext(),
                "Nenhum inventário ativo encontrado",
                Toast.LENGTH_SHORT
            ).show()
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

        // Observar estado do Relatório Fotográfico (itens sem etiqueta)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.relatorioFotoState.collectLatest { state ->
                handleRelatorioFotoState(state)
            }
        }
    }

    /**
     * Renderiza o estado do Relatório Fotográfico no card dedicado: badge de
     * contagem, progress bar e abertura do arquivo gerado pelo servidor
     * (PDF ou Excel) usando o MIME apropriado.
     *
     * Spec: relatorio-fotografico-sem-etiqueta (Req 1.2, 1.3, 3.3 a 3.7, 6.1)
     */
    private fun handleRelatorioFotoState(state: RelatorioFotoState) {
        when (state) {
            is RelatorioFotoState.Idle -> {
                binding.progressBarRelatorioFoto.visibility = View.GONE
                aplicarPermissaoBotoesRelatorioFoto()
            }
            is RelatorioFotoState.Loading -> {
                // Apenas aguardando info — sem indicador visual
            }
            is RelatorioFotoState.InfoCarregada -> {
                val qtd = state.info.semEtiqueta
                binding.tvFotosBadgeExport.visibility = View.VISIBLE
                binding.tvFotosBadgeExport.text = if (qtd > 0) {
                    "$qtd foto(s) disponível(is)"
                } else {
                    "Nenhum item sem etiqueta possui foto registrada"
                }
            }
            is RelatorioFotoState.Downloading -> {
                binding.btnRelatorioFotoPdf.isEnabled = false
                binding.btnRelatorioFotoExcel.isEnabled = false
                binding.progressBarRelatorioFoto.visibility = View.VISIBLE
            }
            is RelatorioFotoState.PdfPronto -> {
                aplicarPermissaoBotoesRelatorioFoto()
                binding.progressBarRelatorioFoto.visibility = View.GONE
                abrirArquivoRelatorioFoto(state.arquivo)
                viewModel.limparEstadoRelatorioFoto()
            }
            is RelatorioFotoState.Erro -> {
                aplicarPermissaoBotoesRelatorioFoto()
                binding.progressBarRelatorioFoto.visibility = View.GONE
                Toast.makeText(requireContext(), state.mensagem, Toast.LENGTH_LONG).show()
                viewModel.limparEstadoRelatorioFoto()
            }
        }
    }

    /** Sincroniza o estado dos botões PDF/Excel com a permissão do usuário. */
    private fun aplicarPermissaoBotoesRelatorioFoto() {
        val ok = isUsuarioSupervisor()
        binding.btnRelatorioFotoPdf.isEnabled = ok
        binding.btnRelatorioFotoExcel.isEnabled = ok
    }

    /**
     * Abre o arquivo baixado (PDF ou XLSX) no visualizador nativo do Android
     * via FileProvider, usando o MIME apropriado para o último formato
     * solicitado.
     */
    private fun abrirArquivoRelatorioFoto(arquivo: java.io.File) {
        val (mime, faltaApp) = when (ultimoFormatoRelatorioFoto) {
            ExportFormat.EXCEL ->
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to
                    "Nenhum app encontrado para abrir Excel. Instale um leitor de planilhas."
            else ->
                "application/pdf" to
                    "Nenhum visualizador de PDF encontrado. Instale um app para abrir PDFs"
        }
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                arquivo
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), faltaApp, Toast.LENGTH_LONG).show()
        }
    }

    private fun isUsuarioSupervisor(): Boolean {
        val perfil = preferencesManager.getUserProfile()
        return perfil == "SUPERVISOR" || perfil == "ADMIN"
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
        // Limpar PDFs/XLSX temporários do relatório fotográfico (Req 8.1)
        runCatching {
            requireContext().cacheDir.listFiles { file ->
                file.name.startsWith("relatorio_sem_etiqueta_")
            }?.forEach { it.delete() }
        }
        _binding = null
    }
}
