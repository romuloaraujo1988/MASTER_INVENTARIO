package com.inventario.mobile.presentation.statistics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.PermissaoNegadaException
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.model.SemFotosException
import com.inventario.mobile.domain.repository.ExportRepository
import com.inventario.mobile.domain.usecase.BaixarRelatorioFotoExcelUseCase
import com.inventario.mobile.domain.usecase.BaixarRelatorioFotoPdfUseCase
import com.inventario.mobile.domain.usecase.BuscarInfoRelatorioFotoUseCase
import com.inventario.mobile.domain.usecase.BuscarSalasParaExportacaoUseCase
import com.inventario.mobile.domain.usecase.GerarRelatorioUseCase
import com.inventario.mobile.domain.usecase.NoDataException
import com.inventario.mobile.presentation.state.RelatorioFotoState
import com.inventario.mobile.util.NetworkChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de exportação de relatórios
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    private val buscarSalasUseCase: BuscarSalasParaExportacaoUseCase,
    private val gerarRelatorioUseCase: GerarRelatorioUseCase,
    private val exportRepository: ExportRepository,
    private val networkChecker: NetworkChecker,
    private val buscarInfoRelatorioFotoUseCase: BuscarInfoRelatorioFotoUseCase,
    private val baixarRelatorioFotoPdfUseCase: BaixarRelatorioFotoPdfUseCase,
    private val baixarRelatorioFotoExcelUseCase: BaixarRelatorioFotoExcelUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> = _state.asStateFlow()
    
    private val _selectedSala = MutableStateFlow<Sala?>(null)
    val selectedSala: StateFlow<Sala?> = _selectedSala.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(ExportFilter.TODOS)
    val selectedFilter: StateFlow<ExportFilter> = _selectedFilter.asStateFlow()
    
    private val _selectedFormat = MutableStateFlow(ExportFormat.PDF)
    val selectedFormat: StateFlow<ExportFormat> = _selectedFormat.asStateFlow()
    
    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()
    
    private val _canExport = MutableStateFlow(false)
    val canExport: StateFlow<Boolean> = _canExport.asStateFlow()
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    private var lastExportFilePath: String? = null
    private var lastExportFormat: ExportFormat? = null

    // ─── Relatório Fotográfico (Spec relatorio-fotografico-sem-etiqueta) ────

    /** Estado isolado do Relatório Fotográfico — não interfere no estado de exportação. */
    private val _relatorioFotoState =
        MutableStateFlow<RelatorioFotoState>(RelatorioFotoState.Idle)
    val relatorioFotoState: StateFlow<RelatorioFotoState> = _relatorioFotoState.asStateFlow()
    
    init {
        loadSalas()
        checkConnectivity()
    }
    
    /**
     * Carrega lista de salas disponíveis
     */
    fun loadSalas() {
        viewModelScope.launch {
            _state.value = ExportState.LoadingSalas
            
            val result = buscarSalasUseCase()
            
            if (result.isSuccess) {
                val salas = result.getOrNull() ?: emptyList()
                _salas.value = salas
                _state.value = ExportState.SalasLoaded(salas)
            } else {
                val error = result.exceptionOrNull()
                _state.value = ExportState.Error(
                    error?.message ?: "Erro ao carregar salas"
                )
            }
        }
    }
    
    /**
     * Seleciona uma sala para exportação
     */
    fun selectSala(sala: Sala) {
        _selectedSala.value = sala
        updateCanExport()
    }
    
    /**
     * Limpa a seleção de sala
     */
    fun clearSalaSelection() {
        _selectedSala.value = null
        updateCanExport()
    }
    
    /**
     * Seleciona o filtro de exportação
     */
    fun selectFilter(filter: ExportFilter) {
        _selectedFilter.value = filter
    }
    
    /**
     * Seleciona o formato de exportação
     */
    fun selectFormat(format: ExportFormat) {
        _selectedFormat.value = format
    }
    
    /**
     * Gera o relatório com as configurações selecionadas
     */
    fun generateReport() {
        val sala = _selectedSala.value
        
        if (sala == null) {
            _state.value = ExportState.Error("Selecione uma sala para exportar")
            return
        }
        
        viewModelScope.launch {
            val format = _selectedFormat.value
            _state.value = ExportState.Generating(
                message = "Gerando relatório ${format.displayName}...",
                progress = 0
            )
            
            val result = gerarRelatorioUseCase(
                sala = sala,
                filter = _selectedFilter.value,
                format = format,
                isOffline = _isOffline.value
            )
            
            if (result.isSuccess) {
                val exportResult = result.getOrNull()!!
                lastExportFilePath = exportResult.filePath
                lastExportFormat = format
                _state.value = ExportState.Success(exportResult)
            } else {
                val error = result.exceptionOrNull()
                when (error) {
                    is NoDataException -> {
                        _state.value = ExportState.NoData(
                            error.message ?: "Nenhum patrimônio encontrado"
                        )
                    }
                    else -> {
                        _state.value = ExportState.Error(
                            error?.message ?: "Erro ao gerar relatório"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Cria Intent para abrir o último arquivo gerado
     */
    fun getOpenFileIntent(): Intent? {
        val filePath = lastExportFilePath ?: return null
        val format = lastExportFormat ?: return null
        return exportRepository.openFile(filePath, format)
    }
    
    /**
     * Cria Intent para compartilhar o último arquivo gerado
     */
    fun getShareFileIntent(): Intent? {
        val filePath = lastExportFilePath ?: return null
        val format = lastExportFormat ?: return null
        return exportRepository.shareFile(filePath, format)
    }
    
    /**
     * Verifica conectividade
     */
    fun checkConnectivity() {
        _isOffline.value = !networkChecker.isOnline()
    }
    
    /**
     * Atualiza estado de permissão para exportar
     */
    private fun updateCanExport() {
        _canExport.value = _selectedSala.value != null
    }
    
    /**
     * Limpa o estado para Idle
     */
    fun clearState() {
        _state.value = ExportState.Idle
    }
    
    /**
     * Retorna para o estado de salas carregadas
     */
    fun resetToSalasLoaded() {
        if (_salas.value.isNotEmpty()) {
            _state.value = ExportState.SalasLoaded(_salas.value)
        } else {
            loadSalas()
        }
    }

    // ─── Relatório Fotográfico — Itens Sem Etiqueta ─────────────────────────
    // Spec: relatorio-fotografico-sem-etiqueta (Req 6.2 a 6.6)

    /**
     * Busca contagens de fotos disponíveis no inventário ativo.
     *
     * Emite [RelatorioFotoState.Loading] e em seguida [RelatorioFotoState.InfoCarregada]
     * em caso de sucesso, ou [RelatorioFotoState.Erro] em caso de falha.
     */
    fun carregarInfoRelatorioFoto(inventarioId: Int) {
        viewModelScope.launch {
            _relatorioFotoState.value = RelatorioFotoState.Loading
            buscarInfoRelatorioFotoUseCase(inventarioId).fold(
                onSuccess = { info ->
                    _relatorioFotoState.value = RelatorioFotoState.InfoCarregada(info)
                },
                onFailure = {
                    _relatorioFotoState.value = RelatorioFotoState.Erro(
                        "Não foi possível carregar informações de fotos"
                    )
                }
            )
        }
    }

    /**
     * Baixa o relatório fotográfico de itens sem etiqueta no formato escolhido.
     *
     * Emite [RelatorioFotoState.Downloading], depois [RelatorioFotoState.PdfPronto]
     * (com o `File` salvo em cache — o nome do estado é mantido por compatibilidade
     * mas o arquivo pode ser PDF ou XLSX dependendo de [formato]) ou
     * [RelatorioFotoState.Erro] com mensagem específica para [SemFotosException]
     * e [PermissaoNegadaException].
     *
     * @param inventarioId ID do inventário ativo.
     * @param formato      Formato desejado (PDF ou EXCEL). Padrão: PDF.
     */
    fun baixarRelatorioFoto(
        inventarioId: Int,
        formato: ExportFormat = ExportFormat.PDF
    ) {
        viewModelScope.launch {
            _relatorioFotoState.value = RelatorioFotoState.Downloading
            val resultado = when (formato) {
                ExportFormat.PDF   -> baixarRelatorioFotoPdfUseCase(inventarioId)
                ExportFormat.EXCEL -> baixarRelatorioFotoExcelUseCase(inventarioId)
                ExportFormat.CSV   -> {
                    // Sem suporte: o servidor não gera CSV de relatório fotográfico.
                    _relatorioFotoState.value = RelatorioFotoState.Erro(
                        "Relatório fotográfico não está disponível em CSV"
                    )
                    return@launch
                }
            }
            resultado.fold(
                onSuccess = { arquivo ->
                    _relatorioFotoState.value = RelatorioFotoState.PdfPronto(arquivo)
                },
                onFailure = { error ->
                    val mensagem = when (error) {
                        is SemFotosException        -> error.message ?: "Nenhuma foto disponível"
                        is PermissaoNegadaException -> error.message ?: "Sem permissão"
                        else -> "Erro de conexão. Verifique a rede e tente novamente"
                    }
                    _relatorioFotoState.value = RelatorioFotoState.Erro(mensagem)
                }
            )
        }
    }

    /** Reseta o estado do relatório fotográfico para Idle. */
    fun limparEstadoRelatorioFoto() {
        _relatorioFotoState.value = RelatorioFotoState.Idle
    }
}
