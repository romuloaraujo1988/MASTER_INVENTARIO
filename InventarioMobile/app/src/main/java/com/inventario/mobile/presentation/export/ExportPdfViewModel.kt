package com.inventario.mobile.presentation.export

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.ExportConfig
import com.inventario.mobile.domain.model.ExportFilter
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.domain.repository.PdfRepository
import com.inventario.mobile.domain.usecase.BuscarSalasParaExportacaoUseCase
import com.inventario.mobile.domain.usecase.GerarRelatorioPdfUseCase
import com.inventario.mobile.domain.usecase.NoDataException
import com.inventario.mobile.util.NetworkChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para tela de exportação de PDF
 */
@HiltViewModel
class ExportPdfViewModel @Inject constructor(
    private val buscarSalasUseCase: BuscarSalasParaExportacaoUseCase,
    private val gerarRelatorioPdfUseCase: GerarRelatorioPdfUseCase,
    private val pdfRepository: PdfRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {
    
    private val _state = MutableStateFlow<ExportPdfState>(ExportPdfState.Idle)
    val state: StateFlow<ExportPdfState> = _state.asStateFlow()
    
    private val _selectedSala = MutableStateFlow<Sala?>(null)
    val selectedSala: StateFlow<Sala?> = _selectedSala.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow(ExportFilter.TODOS)
    val selectedFilter: StateFlow<ExportFilter> = _selectedFilter.asStateFlow()
    
    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()
    
    private val _canExport = MutableStateFlow(false)
    val canExport: StateFlow<Boolean> = _canExport.asStateFlow()
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    init {
        loadSalas()
        checkConnectivity()
    }
    
    /**
     * Carrega lista de salas disponíveis
     */
    fun loadSalas() {
        viewModelScope.launch {
            _state.value = ExportPdfState.LoadingSalas
            
            val result = buscarSalasUseCase()
            
            if (result.isSuccess) {
                val salas = result.getOrNull() ?: emptyList()
                _salas.value = salas
                _state.value = ExportPdfState.SalasLoaded(salas)
            } else {
                val error = result.exceptionOrNull()
                _state.value = ExportPdfState.Error(
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
     * Gera o PDF com as configurações selecionadas
     */
    fun generatePdf() {
        val sala = _selectedSala.value
        
        if (sala == null) {
            _state.value = ExportPdfState.Error("Selecione uma sala para exportar")
            return
        }
        
        viewModelScope.launch {
            _state.value = ExportPdfState.Generating("Gerando relatório PDF...")
            
            val config = ExportConfig(
                sala = sala,
                filter = _selectedFilter.value,
                inventarioId = null, // TODO: Obter do PreferencesManager se necessário
                isOffline = _isOffline.value
            )
            
            val result = gerarRelatorioPdfUseCase(config)
            
            if (result.isSuccess) {
                val exportResult = result.getOrNull()!!
                _state.value = ExportPdfState.Success(exportResult)
            } else {
                val error = result.exceptionOrNull()
                when (error) {
                    is NoDataException -> {
                        _state.value = ExportPdfState.NoData(
                            error.message ?: "Nenhum patrimônio encontrado"
                        )
                    }
                    else -> {
                        _state.value = ExportPdfState.Error(
                            error?.message ?: "Erro ao gerar PDF"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Cria Intent para abrir o PDF
     */
    fun getOpenPdfIntent(filePath: String): Intent {
        return pdfRepository.openPdf(filePath)
    }
    
    /**
     * Cria Intent para compartilhar o PDF
     */
    fun getSharePdfIntent(filePath: String): Intent {
        return pdfRepository.sharePdf(filePath)
    }
    
    /**
     * Verifica conectividade
     */
    private fun checkConnectivity() {
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
        _state.value = ExportPdfState.Idle
    }
    
    /**
     * Retorna para o estado de salas carregadas
     */
    fun resetToSalasLoaded() {
        if (_salas.value.isNotEmpty()) {
            _state.value = ExportPdfState.SalasLoaded(_salas.value)
        } else {
            loadSalas()
        }
    }
}
