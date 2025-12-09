package com.inventario.mobile.presentation.statistics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.ExportFormat
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.ExportRepository
import com.inventario.mobile.domain.usecase.BuscarSalasParaExportacaoUseCase
import com.inventario.mobile.domain.usecase.GerarRelatorioUseCase
import com.inventario.mobile.domain.usecase.NoDataException
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
    private val networkChecker: NetworkChecker
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
}
