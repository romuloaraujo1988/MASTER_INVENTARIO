package com.inventario.mobile.presentation.coletas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro
import com.inventario.mobile.domain.usecase.AgruparColetasPorSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarColetasUseCase
import com.inventario.mobile.domain.usecase.BuscarSalasComColetasUseCase
import com.inventario.mobile.domain.usecase.FiltrarColetasUseCase
import com.inventario.mobile.domain.usecase.ObterUsuarioAtualUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Clean Architecture para tela de visualização de coletas
 * 
 * Gerencia estado da UI e coordena Use Cases para:
 * - Buscar coletas
 * - Filtrar por usuário, sala e status
 * - Agrupar por sala
 * - Calcular contadores
 * 
 * @see Requirements 1.1, 2.1, 3.1, 4.1, 7.1, 7.2, 7.3
 */
@HiltViewModel
class ColetasViewModelClean @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,
    private val filtrarColetasUseCase: FiltrarColetasUseCase,
    private val agruparColetasPorSalaUseCase: AgruparColetasPorSalaUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase,
    private val buscarSalasComColetasUseCase: BuscarSalasComColetasUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "ColetasViewModelClean"
    }
    
    // Estado da UI
    private val _state = MutableStateFlow<ColetasState>(ColetasState.Idle)
    val state: StateFlow<ColetasState> = _state.asStateFlow()
    
    // Cache de todas as coletas (antes de filtrar)
    private var todasColetas: List<Coleta> = emptyList()
    
    // Cache de salas com coletas (do servidor)
    private var salasComColetas: List<String> = emptyList()
    
    // Filtros ativos
    private var filtroUsuarioAtivo = true
    private var filtroSalaAtivo: String? = null
    private var filtroStatusAtivo = StatusFiltro.TODOS
    private var visualizacaoAgrupadaAtiva = false
    private var textoPesquisa = ""
    
    // Usuário atual
    private var usuarioAtual: String? = null
    
    /**
     * Carrega coletas do servidor/banco
     * Transiciona estado: Idle → Loading → Success/Error
     * 
     * @see Requirements 7.1, 7.2, 7.3
     */
    fun carregarColetas() {
        viewModelScope.launch {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "CARREGANDO COLETAS")
            
            _state.value = ColetasState.Loading
            
            try {
                // Obter usuário atual (retorna Usuario?, extraímos o nome)
                val usuario = obterUsuarioAtualUseCase()
                usuarioAtual = usuario?.nome
                Log.d(TAG, "Usuário atual: $usuarioAtual")
                
                // Buscar salas com coletas do servidor (para o filtro)
                buscarSalasComColetasUseCase().fold(
                    onSuccess = { salas ->
                        Log.d(TAG, "✓ ${salas.size} salas com coletas carregadas do servidor")
                        salasComColetas = salas
                    },
                    onFailure = { erro ->
                        Log.w(TAG, "⚠ Erro ao buscar salas do servidor, usando fallback: ${erro.message}")
                        // Fallback: extrair salas das coletas locais
                    }
                )
                
                // Buscar coletas
                val resultado = buscarColetasUseCase()
                
                resultado.fold(
                    onSuccess = { coletas ->
                        Log.d(TAG, "✓ ${coletas.size} coletas carregadas")
                        todasColetas = coletas
                        aplicarFiltrosEAtualizarEstado()
                    },
                    onFailure = { erro ->
                        Log.e(TAG, "✗ Erro ao carregar coletas", erro)
                        _state.value = ColetasState.Error(
                            erro.message ?: "Erro desconhecido ao carregar coletas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exceção ao carregar coletas", e)
                _state.value = ColetasState.Error(
                    e.message ?: "Erro desconhecido ao carregar coletas"
                )
            }
            
            Log.d(TAG, "═══════════════════════════════════════")
        }
    }
    
    /**
     * Alterna filtro por usuário logado
     * 
     * @see Requirements 2.1, 2.2, 2.3, 2.4, 2.5
     */
    fun toggleFiltroUsuario() {
        filtroUsuarioAtivo = !filtroUsuarioAtivo
        Log.d(TAG, "Filtro usuário: $filtroUsuarioAtivo")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Aplica filtro por sala
     * 
     * @param sala Nome da sala (null para todas)
     * @see Requirements 3.1, 3.2, 3.4
     */
    fun aplicarFiltroSala(sala: String?) {
        filtroSalaAtivo = sala
        Log.d(TAG, "Filtro sala: $sala")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Aplica filtro por status
     * 
     * @param status Status de filtro (TODOS, COLETADOS, PENDENTES)
     * @see Requirements 4.1, 4.2, 4.3, 4.4, 4.5
     */
    fun aplicarFiltroStatus(status: StatusFiltro) {
        filtroStatusAtivo = status
        Log.d(TAG, "Filtro status: $status")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Alterna visualização agrupada por sala
     * 
     * @see Requirements 10.1
     */
    fun toggleVisualizacaoAgrupada() {
        visualizacaoAgrupadaAtiva = !visualizacaoAgrupadaAtiva
        Log.d(TAG, "Visualização agrupada: $visualizacaoAgrupadaAtiva")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Limpa todos os filtros
     * 
     * @see Requirements 8.5
     */
    fun limparFiltros() {
        filtroUsuarioAtivo = false
        filtroSalaAtivo = null
        filtroStatusAtivo = StatusFiltro.TODOS
        textoPesquisa = ""
        Log.d(TAG, "Filtros limpos")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Aplica filtro de pesquisa por texto
     * 
     * @param texto Texto para pesquisar (número ou descrição do patrimônio)
     */
    fun pesquisar(texto: String) {
        textoPesquisa = texto
        Log.d(TAG, "Pesquisa: $texto")
        aplicarFiltrosEAtualizarEstado()
    }
    
    /**
     * Retorna coletas agrupadas por sala
     * 
     * @see Requirements 10.1, 10.2, 10.3, 10.4, 10.5
     */
    fun agruparPorSala(): Map<String, List<Coleta>> {
        val estadoAtual = _state.value
        return if (estadoAtual is ColetasState.Success) {
            agruparColetasPorSalaUseCase(estadoAtual.coletas)
        } else {
            emptyMap()
        }
    }
    
    /**
     * Aplica filtros e atualiza estado da UI
     */
    private fun aplicarFiltrosEAtualizarEstado() {
        // Aplicar filtros
        val coletasFiltradas = filtrarColetasUseCase(
            coletas = todasColetas,
            usuarioAtual = usuarioAtual,
            filtroUsuario = filtroUsuarioAtivo,
            filtroSala = filtroSalaAtivo,
            filtroStatus = filtroStatusAtivo,
            textoPesquisa = textoPesquisa
        )
        
        // Calcular contadores (sobre coletas filtradas por usuário e sala, mas não por status)
        val coletasParaContagem = filtrarColetasUseCase(
            coletas = todasColetas,
            usuarioAtual = usuarioAtual,
            filtroUsuario = filtroUsuarioAtivo,
            filtroSala = filtroSalaAtivo,
            filtroStatus = StatusFiltro.TODOS,
            textoPesquisa = textoPesquisa
        )
        val (totalColetados, totalPendentes) = filtrarColetasUseCase.calcularContadores(coletasParaContagem)
        
        // Usar salas do servidor se disponíveis, senão extrair das coletas locais
        val salasDisponiveis = if (salasComColetas.isNotEmpty()) {
            Log.d(TAG, "Usando ${salasComColetas.size} salas do servidor")
            salasComColetas
        } else {
            Log.d(TAG, "Usando salas extraídas das coletas locais")
            filtrarColetasUseCase.extrairSalasDisponiveis(todasColetas)
        }
        
        // Agrupar se necessário
        val coletasAgrupadas = if (visualizacaoAgrupadaAtiva) {
            agruparColetasPorSalaUseCase(coletasFiltradas)
        } else {
            emptyMap()
        }
        
        Log.d(TAG, "Coletas filtradas: ${coletasFiltradas.size}")
        Log.d(TAG, "Total coletados: $totalColetados, pendentes: $totalPendentes")
        Log.d(TAG, "Salas disponíveis: ${salasDisponiveis.size}")
        
        // Atualizar estado
        _state.value = ColetasState.Success(
            coletas = coletasFiltradas,
            totalColetados = totalColetados,
            totalPendentes = totalPendentes,
            filtroUsuario = filtroUsuarioAtivo,
            filtroSala = filtroSalaAtivo,
            filtroStatus = filtroStatusAtivo,
            salasDisponiveis = salasDisponiveis,
            coletasAgrupadas = coletasAgrupadas,
            visualizacaoAgrupada = visualizacaoAgrupadaAtiva
        )
    }
}
