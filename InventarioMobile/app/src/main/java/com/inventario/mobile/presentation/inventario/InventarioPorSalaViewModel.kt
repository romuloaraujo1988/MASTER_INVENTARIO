package com.inventario.mobile.presentation.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.EstatisticasSala
import com.inventario.mobile.domain.model.FiltroColetaSala
import com.inventario.mobile.domain.model.SalaComProgresso
import com.inventario.mobile.domain.usecase.BuscarEstatisticasSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarPatrimoniosPorSalaUseCase
import com.inventario.mobile.domain.usecase.BuscarSalasComProgressoUseCase
import com.inventario.mobile.presentation.state.InventarioPorSalaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de Inventário por Sala.
 * Gerencia estado da UI e coordena Use Cases.
 */
@HiltViewModel
class InventarioPorSalaViewModel @Inject constructor(
    private val buscarSalasComProgressoUseCase: BuscarSalasComProgressoUseCase,
    private val buscarPatrimoniosPorSalaUseCase: BuscarPatrimoniosPorSalaUseCase,
    private val buscarEstatisticasSalaUseCase: BuscarEstatisticasSalaUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "InventarioPorSalaVM"
        private const val PAGE_SIZE = 20
    }
    
    private val _state = MutableStateFlow<InventarioPorSalaState>(InventarioPorSalaState.Idle)
    val state: StateFlow<InventarioPorSalaState> = _state.asStateFlow()
    
    // Cache de salas para filtro local
    private var todasSalas: List<SalaComProgresso> = emptyList()
    
    // Sala atualmente selecionada
    private var salaAtual: SalaComProgresso? = null
    private var filtroAtual: FiltroColetaSala = FiltroColetaSala.TODOS
    
    /**
     * Carrega todas as salas com progresso de coleta.
     * Se o banco local estiver vazio, mostra mensagem para sincronizar.
     */
    fun carregarSalas() {
        android.util.Log.d(TAG, "═══════════════════════════════════")
        android.util.Log.d(TAG, "carregarSalas() chamado")
        android.util.Log.d(TAG, "═══════════════════════════════════")
        
        viewModelScope.launch {
            _state.value = InventarioPorSalaState.Loading
            android.util.Log.d(TAG, "Estado: Loading")
            
            val result = buscarSalasComProgressoUseCase()
            android.util.Log.d(TAG, "Resultado do UseCase: isSuccess=${result.isSuccess}")
            
            result.fold(
                onSuccess = { salas ->
                    android.util.Log.d(TAG, "✓ Salas carregadas: ${salas.size}")
                    
                    if (salas.isEmpty()) {
                        android.util.Log.w(TAG, "⚠️ Nenhuma sala encontrada no banco local")
                        _state.value = InventarioPorSalaState.Error(
                            "Nenhuma sala encontrada. Por favor, sincronize os dados primeiro."
                        )
                        return@fold
                    }
                    
                    salas.take(5).forEach { sala ->
                        android.util.Log.d(TAG, "  - ${sala.nome}: ${sala.coletados}/${sala.totalPatrimonios}")
                    }
                    
                    todasSalas = salas
                    _state.value = InventarioPorSalaState.SalasCarregadas(
                        salas = salas,
                        salasFiltradas = salas
                    )
                    android.util.Log.d(TAG, "Estado: SalasCarregadas")
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "✗ Erro ao carregar salas", error)
                    _state.value = InventarioPorSalaState.Error(
                        error.message ?: "Erro ao carregar salas"
                    )
                }
            )
        }
    }
    
    /**
     * Seleciona uma sala e carrega seus patrimônios.
     * 
     * NOTA: Usa os dados de estatísticas que já vieram do servidor no SalaComProgresso
     * ao invés de buscar novamente do banco local (que pode estar desatualizado).
     */
    fun selecionarSala(sala: SalaComProgresso) {
        viewModelScope.launch {
            salaAtual = sala
            filtroAtual = FiltroColetaSala.TODOS
            
            _state.value = InventarioPorSalaState.Loading
            
            android.util.Log.d(TAG, "═══════════════════════════════════")
            android.util.Log.d(TAG, "Selecionando sala: ${sala.nome}")
            android.util.Log.d(TAG, "Total: ${sala.totalPatrimonios}, Coletados: ${sala.coletados}")
            android.util.Log.d(TAG, "═══════════════════════════════════")
            
            // Usar dados que já vieram do servidor no SalaComProgresso
            // Isso evita inconsistência entre dropdown e card de estatísticas
            val estatisticas = EstatisticasSala.criar(
                salaId = sala.id,
                salaNome = sala.nome,
                totalPatrimonios = sala.totalPatrimonios,
                coletados = sala.coletados,
                coletadosHoje = 0,
                coletadosSemana = 0
            )
            
            // Buscar patrimônios
            val patrimoniosResult = buscarPatrimoniosPorSalaUseCase(
                salaId = sala.id,
                filtro = filtroAtual,
                page = 0,
                pageSize = PAGE_SIZE
            )
            
            patrimoniosResult.fold(
                onSuccess = { patrimonios ->
                    android.util.Log.d(TAG, "✓ ${patrimonios.size} patrimônios carregados")
                    
                    _state.value = InventarioPorSalaState.SalaSelecionada(
                        sala = sala,
                        estatisticas = estatisticas,
                        patrimonios = patrimonios,
                        filtroAtual = filtroAtual,
                        currentPage = 0,
                        hasMorePages = patrimonios.size >= PAGE_SIZE
                    )
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "✗ Erro ao carregar patrimônios", error)
                    _state.value = InventarioPorSalaState.Error(
                        error.message ?: "Erro ao carregar dados da sala"
                    )
                }
            )
        }
    }
    
    /**
     * Aplica filtro de status de coleta.
     */
    fun aplicarFiltro(filtro: FiltroColetaSala) {
        val sala = salaAtual ?: return
        
        viewModelScope.launch {
            filtroAtual = filtro
            
            // Manter estado atual mas indicar loading
            val currentState = _state.value
            if (currentState is InventarioPorSalaState.SalaSelecionada) {
                _state.value = currentState.copy(isLoadingMore = true)
            }
            
            val patrimoniosResult = buscarPatrimoniosPorSalaUseCase(
                salaId = sala.id,
                filtro = filtro,
                page = 0,
                pageSize = PAGE_SIZE
            )
            
            patrimoniosResult.fold(
                onSuccess = { patrimonios ->
                    if (currentState is InventarioPorSalaState.SalaSelecionada) {
                        _state.value = currentState.copy(
                            patrimonios = patrimonios,
                            filtroAtual = filtro,
                            currentPage = 0,
                            hasMorePages = patrimonios.size >= PAGE_SIZE,
                            isLoadingMore = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.value = InventarioPorSalaState.Error(
                        error.message ?: "Erro ao aplicar filtro"
                    )
                }
            )
        }
    }
    
    /**
     * Busca salas por nome ou número.
     */
    fun buscarSala(query: String) {
        val queryLower = query.lowercase().trim()
        
        val salasFiltradas = if (queryLower.isEmpty()) {
            todasSalas
        } else {
            todasSalas.filter { sala ->
                sala.nome.lowercase().contains(queryLower) ||
                sala.numero?.lowercase()?.contains(queryLower) == true ||
                sala.id.toString().contains(queryLower)
            }
        }
        
        _state.value = InventarioPorSalaState.SalasCarregadas(
            salas = todasSalas,
            salasFiltradas = salasFiltradas,
            queryBusca = query
        )
    }
    
    /**
     * Carrega mais patrimônios (paginação).
     */
    fun carregarMaisPatrimonios() {
        val currentState = _state.value
        if (currentState !is InventarioPorSalaState.SalaSelecionada) {
            android.util.Log.d(TAG, "carregarMaisPatrimonios: estado não é SalaSelecionada")
            return
        }
        if (currentState.isLoadingMore) {
            android.util.Log.d(TAG, "carregarMaisPatrimonios: já está carregando")
            return
        }
        if (!currentState.hasMorePages) {
            android.util.Log.d(TAG, "carregarMaisPatrimonios: não há mais páginas")
            return
        }
        
        val sala = salaAtual ?: return
        
        viewModelScope.launch {
            val nextPage = currentState.currentPage + 1
            android.util.Log.d(TAG, "═══════════════════════════════════")
            android.util.Log.d(TAG, "CARREGANDO MAIS PATRIMÔNIOS")
            android.util.Log.d(TAG, "Sala: ${sala.nome}, Página: $nextPage")
            android.util.Log.d(TAG, "Patrimônios atuais: ${currentState.patrimonios.size}")
            android.util.Log.d(TAG, "═══════════════════════════════════")
            
            _state.value = currentState.copy(isLoadingMore = true)
            
            val patrimoniosResult = buscarPatrimoniosPorSalaUseCase(
                salaId = sala.id,
                filtro = filtroAtual,
                page = nextPage,
                pageSize = PAGE_SIZE
            )
            
            patrimoniosResult.fold(
                onSuccess = { novosPatrimonios ->
                    android.util.Log.d(TAG, "✓ ${novosPatrimonios.size} novos patrimônios carregados")
                    
                    val todosPatrimonios = currentState.patrimonios + novosPatrimonios
                    val hasMore = novosPatrimonios.size >= PAGE_SIZE
                    
                    android.util.Log.d(TAG, "Total agora: ${todosPatrimonios.size}, hasMorePages: $hasMore")
                    
                    _state.value = currentState.copy(
                        patrimonios = todosPatrimonios,
                        currentPage = nextPage,
                        hasMorePages = hasMore,
                        isLoadingMore = false
                    )
                },
                onFailure = { error ->
                    android.util.Log.e(TAG, "✗ Erro ao carregar mais patrimônios", error)
                    _state.value = currentState.copy(isLoadingMore = false)
                }
            )
        }
    }
    
    /**
     * Volta para a lista de salas.
     */
    fun voltarParaSalas() {
        salaAtual = null
        filtroAtual = FiltroColetaSala.TODOS
        
        _state.value = InventarioPorSalaState.SalasCarregadas(
            salas = todasSalas,
            salasFiltradas = todasSalas
        )
    }
    
    /**
     * Limpa o estado de erro.
     */
    fun limparErro() {
        if (_state.value is InventarioPorSalaState.Error) {
            _state.value = InventarioPorSalaState.Idle
        }
    }
    
    /**
     * Recarrega os dados atuais.
     */
    fun recarregar() {
        val sala = salaAtual
        if (sala != null) {
            selecionarSala(sala)
        } else {
            carregarSalas()
        }
    }
}
