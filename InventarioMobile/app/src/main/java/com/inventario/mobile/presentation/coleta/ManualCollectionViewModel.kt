package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarPatrimonioUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.domain.model.Patrimonio as DomainPatrimonio
import com.inventario.mobile.data.model.Patrimonio as DataPatrimonio
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManualCollectionViewModel @Inject constructor(
    private val buscarPatrimonioUseCase: BuscarPatrimonioUseCase,
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val inventarioRepository: InventarioRepository, // Temporário para compatibilidade
    private val coletaDao: com.inventario.mobile.data.local.dao.ColetaDao // v2.7: Para contar coletas por sala
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualCollectionUiState())
    val uiState: StateFlow<ManualCollectionUiState> = _uiState.asStateFlow()

    private var salaId: Long = -1L
    private var salaNome: String = ""

    fun setSalaInfo(salaId: Long, salaNome: String) {
        this.salaId = salaId
        this.salaNome = salaNome
        _uiState.value = _uiState.value.copy(
            salaNome = salaNome,
            salaId = salaId
        )
        loadColetasCount()
    }

    fun searchPatrimonio(numeroPatrimonio: String) {
        Log.d("ManualCollectionVM", "searchPatrimonio iniciado com número: '$numeroPatrimonio'")
        
        if (numeroPatrimonio.isBlank()) {
            Log.w("ManualCollectionVM", "Número do patrimônio está em branco")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Digite o número do patrimônio"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d("ManualCollectionVM", "Iniciando busca no repositório para número: $numeroPatrimonio")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    patrimonio = null
                )

                // Usar Use Case Clean Architecture
                val result = buscarPatrimonioUseCase(numeroPatrimonio)
                
                Log.d("ManualCollectionVM", "Resultado da busca recebido: ${if (result.isSuccess) "sucesso" else "falha"}")
                
                result.fold(
                    onSuccess = { domainPatrimonio ->
                        Log.d("ManualCollectionVM", "Patrimônio encontrado - ID: ${domainPatrimonio.id}, Número: ${domainPatrimonio.numeroPatrimonio}, Descrição: ${domainPatrimonio.descricao}")
                        
                        // Converter domain para data model (temporário)
                        val dataPatrimonio = domainPatrimonio.toDataModel()
                        
                        // Verificar se já foi coletado
                        val jaColetado = domainPatrimonio.coletado
                        Log.d("ManualCollectionVM", "Patrimônio já coletado: $jaColetado")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            patrimonio = dataPatrimonio,
                            jaColetado = jaColetado,
                            errorMessage = if (jaColetado) "Patrimônio já foi coletado" else null
                        )
                    },
                    onFailure = { exception ->
                        Log.e("ManualCollectionVM", "Erro na busca do patrimônio", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Patrimônio não encontrado"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("ManualCollectionVM", "Erro inesperado na busca", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
    
    // Converter domain model para data model (temporário até migração completa)
    private fun DomainPatrimonio.toDataModel(): DataPatrimonio {
        return DataPatrimonio(
            id = this.id,
            numeroPatrimonio = this.numeroPatrimonio,
            descricao = this.descricao,
            marca = this.marca,
            modelo = this.modelo,
            numeroSerie = this.numeroSerie,
            estado = this.estado,
            valor = this.valor,
            setorId = this.setorId,
            setorNome = null,
            salaId = this.salaId,
            salaNome = null,
            responsavelId = this.coletorId,
            responsavelNome = null,
            qrCode = this.qrCode,
            observacoes = this.observacoes,
            coletado = this.coletado,
            dataColeta = this.dataColeta,
            coletadoPor = this.coletadoPor,
            dataColetaFormatada = this.dataColetaFormatada,
            localizacaoEncontrada = this.localizacaoEncontrada,
            estadoEncontrado = this.estadoEncontrado,
            observacoesColeta = null,
            sincronizado = this.sincronizado,
            servidorId = this.servidorId
        )
    }

    fun coletarPatrimonio(estadoEncontrado: String, observacoes: String? = null) {
        val patrimonio = _uiState.value.patrimonio
        
        // Validação 1: Verificar se há patrimônio selecionado
        if (patrimonio == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "É necessário pesquisar e selecionar um patrimônio válido antes de realizar a coleta"
            )
            return
        }

        // Validação 2: Verificar se o patrimônio já foi coletado
        if (_uiState.value.jaColetado) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Este patrimônio já foi coletado anteriormente"
            )
            return
        }

        // Validação 3: Verificar se o patrimônio é válido (tem ID e número)
        if (patrimonio.id <= 0 || patrimonio.numeroPatrimonio.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Patrimônio inválido. Realize uma nova pesquisa"
            )
            return
        }

        // Validação 4: Verificar se o estado foi fornecido
        if (estadoEncontrado.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "É necessário selecionar o estado do patrimônio"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                // Usar RegistrarColetaUseCase (Clean Architecture)
                val result = registrarColetaUseCase(
                    numeroPatrimonio = patrimonio.numeroPatrimonio,
                    localizacaoAtual = salaNome,
                    estadoEncontrado = estadoEncontrado,
                    observacoes = observacoes
                )

                result.fold(
                    onSuccess = { coleta ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            coletaRealizada = true,
                            patrimonio = null,
                            jaColetado = false,
                            successMessage = "Patrimônio ${patrimonio.numeroPatrimonio} coletado com sucesso! Estado: $estadoEncontrado"
                        )
                        loadColetasCount()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao coletar patrimônio: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * v2.7: Carrega contagem de coletas da sala atual
     * Usa ColetaDao diretamente para buscar do banco local
     */
    private fun loadColetasCount() {
        viewModelScope.launch {
            try {
                Log.d("ManualCollectionVM", "Carregando contagem de coletas para sala: $salaNome (ID: $salaId)")
                
                // Buscar coletas da sala usando ColetaDao
                val coletasDaSala = if (salaId > 0) {
                    coletaDao.buscarPorSala(salaId.toInt())
                } else {
                    // Fallback: buscar todas e filtrar por nome
                    coletaDao.buscarTodas().filter { coleta ->
                        coleta.nomeSala == salaNome
                    }
                }
                
                Log.d("ManualCollectionVM", "Total de coletas na sala '$salaNome': ${coletasDaSala.size}")
                
                _uiState.value = _uiState.value.copy(
                    totalColetas = coletasDaSala.size
                )
            } catch (e: Exception) {
                Log.e("ManualCollectionVM", "Erro ao carregar contagem de coletas", e)
                // Silently fail for count - não bloqueia a funcionalidade principal
                _uiState.value = _uiState.value.copy(
                    totalColetas = 0
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            coletaRealizada = false
        )
    }

    fun clearPatrimonio() {
        _uiState.value = _uiState.value.copy(
            patrimonio = null,
            jaColetado = false,
            errorMessage = null,
            successMessage = null
        )
    }
}

data class ManualCollectionUiState(
    val isLoading: Boolean = false,
    val salaId: Long = -1L,
    val salaNome: String = "",
    val patrimonio: DataPatrimonio? = null,
    val jaColetado: Boolean = false,
    val coletaRealizada: Boolean = false,
    val totalColetas: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)