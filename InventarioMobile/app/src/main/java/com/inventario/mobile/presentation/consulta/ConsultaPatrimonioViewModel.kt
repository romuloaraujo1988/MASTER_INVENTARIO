package com.inventario.mobile.presentation.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.usecase.BuscarPatrimonioAvancadaUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimonioPorCodigoUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimonioPorDescricaoUseCase
import com.inventario.mobile.domain.usecase.ObterDetalhePatrimonioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para consulta de patrimônios
 * 
 * Gerencia estados da UI e coordena Use Cases
 * 
 * @param buscarPorCodigoUseCase Use case para busca por código
 * @param buscarPorDescricaoUseCase Use case para busca por descrição
 * @param buscarAvancadaUseCase Use case para busca avançada
 * @param obterDetalheUseCase Use case para obter detalhes
 */
@HiltViewModel
class ConsultaPatrimonioViewModel @Inject constructor(
    private val buscarPorCodigoUseCase: BuscarPatrimonioPorCodigoUseCase,
    private val buscarPorDescricaoUseCase: BuscarPatrimonioPorDescricaoUseCase,
    private val buscarAvancadaUseCase: BuscarPatrimonioAvancadaUseCase,
    private val obterDetalheUseCase: ObterDetalhePatrimonioUseCase
) : ViewModel() {
    
    // Estado da consulta
    private val _consultaState = MutableStateFlow<ConsultaState>(ConsultaState.Idle)
    val consultaState: StateFlow<ConsultaState> = _consultaState.asStateFlow()
    
    // Estado dos detalhes
    private val _detalheState = MutableStateFlow<DetalheState>(DetalheState.Idle)
    val detalheState: StateFlow<DetalheState> = _detalheState.asStateFlow()
    
    // Último termo de busca
    private var ultimoTermo: String = ""
    
    /**
     * Busca patrimônios por código parcial
     * 
     * @param codigo Código parcial (mínimo 2 caracteres)
     * @param limit Quantidade máxima de resultados
     */
    fun buscarPorCodigo(codigo: String, limit: Int = 10) {
        viewModelScope.launch {
            _consultaState.value = ConsultaState.Loading("Buscando por código '$codigo'...")
            ultimoTermo = codigo
            
            buscarPorCodigoUseCase(codigo, limit).fold(
                onSuccess = { patrimonios ->
                    if (patrimonios.isEmpty()) {
                        _consultaState.value = ConsultaState.Empty(
                            termoBusca = codigo,
                            sugestoes = listOf(
                                "Tente usar menos caracteres",
                                "Verifique se o código está correto",
                                "Tente buscar por descrição"
                            )
                        )
                    } else {
                        _consultaState.value = ConsultaState.Success(
                            patrimonios = patrimonios,
                            termoBusca = codigo
                        )
                    }
                },
                onFailure = { error ->
                    _consultaState.value = ConsultaState.Error(
                        message = error.message ?: "Erro ao buscar por código",
                        throwable = error
                    )
                }
            )
        }
    }
    
    /**
     * Busca patrimônios por descrição
     * 
     * @param descricao Descrição ou parte dela (mínimo 3 caracteres)
     * @param limit Quantidade máxima de resultados
     */
    fun buscarPorDescricao(descricao: String, limit: Int = 10) {
        viewModelScope.launch {
            _consultaState.value = ConsultaState.Loading("Buscando por '$descricao'...")
            ultimoTermo = descricao
            
            buscarPorDescricaoUseCase(descricao, limit).fold(
                onSuccess = { patrimonios ->
                    if (patrimonios.isEmpty()) {
                        // Gerar sugestões
                        val sugestoes = buscarPorDescricaoUseCase.gerarSugestoes(descricao)
                        
                        _consultaState.value = ConsultaState.Empty(
                            termoBusca = descricao,
                            sugestoes = if (sugestoes.isNotEmpty()) {
                                listOf("Você quis dizer:") + sugestoes
                            } else {
                                listOf(
                                    "Tente usar menos caracteres",
                                    "Verifique a ortografia",
                                    "Tente buscar por código"
                                )
                            }
                        )
                    } else {
                        _consultaState.value = ConsultaState.Success(
                            patrimonios = patrimonios,
                            termoBusca = descricao
                        )
                    }
                },
                onFailure = { error ->
                    _consultaState.value = ConsultaState.Error(
                        message = error.message ?: "Erro ao buscar por descrição",
                        throwable = error
                    )
                }
            )
        }
    }
    
    /**
     * Busca avançada com múltiplos critérios
     * 
     * @param termo Termo de busca geral
     * @param salaId ID da sala (opcional)
     * @param responsavelId ID do responsável (opcional)
     * @param limit Quantidade máxima de resultados
     */
    fun buscarAvancada(
        termo: String,
        salaId: Int? = null,
        responsavelId: Int? = null,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            val descricao = buscarAvancadaUseCase.gerarDescricaoCriterios(
                termo, salaId, responsavelId
            )
            _consultaState.value = ConsultaState.Loading(descricao)
            ultimoTermo = termo
            
            buscarAvancadaUseCase(termo, salaId, responsavelId, limit).fold(
                onSuccess = { patrimonios ->
                    if (patrimonios.isEmpty()) {
                        _consultaState.value = ConsultaState.Empty(
                            termoBusca = termo,
                            sugestoes = listOf(
                                "Tente remover alguns filtros",
                                "Use um termo mais genérico",
                                "Verifique se os filtros estão corretos"
                            )
                        )
                    } else {
                        // Gerar sugestões de refinamento
                        val sugestoes = buscarAvancadaUseCase.sugerirRefinamentos(patrimonios)
                        
                        _consultaState.value = ConsultaState.Success(
                            patrimonios = patrimonios,
                            termoBusca = termo
                        )
                    }
                },
                onFailure = { error ->
                    _consultaState.value = ConsultaState.Error(
                        message = error.message ?: "Erro na busca avançada",
                        throwable = error
                    )
                }
            )
        }
    }
    
    /**
     * Obtém detalhes completos de um patrimônio
     * 
     * @param patrimonioId ID do patrimônio
     */
    fun obterDetalhes(patrimonioId: Int) {
        viewModelScope.launch {
            _detalheState.value = DetalheState.Loading(patrimonioId)
            
            obterDetalheUseCase(patrimonioId).fold(
                onSuccess = { detalhe ->
                    _detalheState.value = DetalheState.Success(detalhe)
                },
                onFailure = { error ->
                    _detalheState.value = DetalheState.Error(
                        message = error.message ?: "Erro ao obter detalhes",
                        patrimonioId = patrimonioId
                    )
                }
            )
        }
    }
    
    /**
     * Limpa o estado da consulta
     */
    fun limparConsulta() {
        _consultaState.value = ConsultaState.Idle
        ultimoTermo = ""
    }
    
    /**
     * Limpa o estado dos detalhes
     */
    fun limparDetalhes() {
        _detalheState.value = DetalheState.Idle
    }
    
    /**
     * Retorna o último termo de busca
     */
    fun getUltimoTermo(): String = ultimoTermo
    
    /**
     * Valida se o código é válido para busca
     */
    fun isCodigoValido(codigo: String): Boolean {
        return buscarPorCodigoUseCase.isCodigoValido(codigo)
    }
    
    /**
     * Valida se a descrição é válida para busca
     */
    fun isDescricaoValida(descricao: String): Boolean {
        return buscarPorDescricaoUseCase.isDescricaoValida(descricao)
    }
    
    /**
     * Sanitiza o código de entrada
     */
    fun sanitizarCodigo(codigo: String): String {
        return buscarPorCodigoUseCase.sanitizarCodigo(codigo)
    }
    
    /**
     * Sanitiza a descrição de entrada
     */
    fun sanitizarDescricao(descricao: String): String {
        return buscarPorDescricaoUseCase.sanitizarDescricao(descricao)
    }
}
