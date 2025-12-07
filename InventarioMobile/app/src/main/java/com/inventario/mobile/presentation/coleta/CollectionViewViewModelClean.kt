package com.inventario.mobile.presentation.coleta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.usecase.BuscarColetasUseCase
import com.inventario.mobile.domain.usecase.ObterUsuarioAtualUseCase
import com.inventario.mobile.domain.usecase.RemoverColetaUseCase
import com.inventario.mobile.domain.usecase.FonteDados
import com.inventario.mobile.presentation.state.CollectionViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel Clean Architecture para visualização de coletas
 * 
 * Responsabilidades:
 * - Gerenciar estado da UI
 * - Coordenar Use Cases
 * - Aplicar filtros de usuário, status e sala
 * - Notificar View sobre mudanças
 */
@HiltViewModel
class CollectionViewViewModelClean @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,
    private val buscarColetasComFallbackUseCase: com.inventario.mobile.domain.usecase.BuscarColetasComFallbackUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase,
    private val removerColetaUseCase: RemoverColetaUseCase,
    private val sincronizarColetasUseCase: com.inventario.mobile.domain.usecase.SincronizarColetasDoServidorUseCase,
    private val coletaMigration: com.inventario.mobile.data.migration.ColetaMigration,
    private val reenviarColetaUseCase: com.inventario.mobile.domain.usecase.ReenviarColetaUseCase,
    private val excluirColetaPendenteUseCase: com.inventario.mobile.domain.usecase.ExcluirColetaPendenteUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "CollectionViewVMClean"
    }

    enum class FiltroUsuario {
        TODAS,
        MINHAS
    }
    
    enum class FiltroStatus {
        TODOS,
        SINCRONIZADOS,
        PENDENTES,
        SEM_ETIQUETA
    }

    private val _state = MutableStateFlow<CollectionViewState>(CollectionViewState.Idle)
    val state: StateFlow<CollectionViewState> = _state.asStateFlow()

    // Filtros atuais
    private var filtroUsuario: FiltroUsuario = FiltroUsuario.TODAS
    private var filtroStatus: FiltroStatus = FiltroStatus.TODOS
    private var salaSelecionada: String? = null
    private var queryBusca: String = ""
    
    // Cache de coletas
    private var todasColetas: List<Coleta> = emptyList()
    private var usuarioAtualId: Int? = null
    
    /**
     * Carrega todas as coletas
     */
    fun carregarColetas() {
        viewModelScope.launch {
            Log.d(TAG, "carregarColetas: Iniciando carregamento")
            _state.value = CollectionViewState.Loading
            
            try {
                // Verificar se precisa migração de coletas antigas
                if (coletaMigration.precisaMigracao()) {
                    Log.d(TAG, "⚠ Coletas antigas precisam de migração, executando...")
                    coletaMigration.migrarColetasAntigas().fold(
                        onSuccess = { result ->
                            Log.d(TAG, "✓ Migração concluída: ${result.atualizadas} coletas atualizadas")
                        },
                        onFailure = { error ->
                            Log.w(TAG, "Erro na migração de coletas antigas", error)
                        }
                    )
                }
                
                // Obter usuário atual
                val usuario = obterUsuarioAtualUseCase()
                usuarioAtualId = usuario?.id?.toInt()
                
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "USUÁRIO ATUAL")
                Log.d(TAG, "Nome: ${usuario?.nome}")
                Log.d(TAG, "ID: $usuarioAtualId")
                Log.d(TAG, "═══════════════════════════════════════════")
                
                // ✅ Buscar coletas com fallback automático (funciona offline)
                buscarColetasComFallbackUseCase().fold(
                    onSuccess = { resultado ->
                        val coletas = resultado.coletas
                        val fonte = resultado.fonte
                        
                        Log.d(TAG, "═══════════════════════════════════════════")
                        Log.d(TAG, "COLETAS CARREGADAS")
                        Log.d(TAG, "Fonte: ${if (fonte == FonteDados.SERVIDOR) "SERVIDOR" else "LOCAL (OFFLINE)"}")
                        Log.d(TAG, "Total de coletas: ${coletas.size}")
                        coletas.take(5).forEach { coleta ->
                            Log.d(TAG, "  Coleta ID=${coleta.id}, patrimonioId=${coleta.patrimonioId}, " +
                                    "usuarioId=${coleta.usuarioId}, sincronizado=${coleta.sincronizado}")
                        }
                        Log.d(TAG, "═══════════════════════════════════════════")
                        
                        todasColetas = coletas
                        
                        // Calcular estatísticas
                        val sincronizadas = coletas.count { it.sincronizado }
                        val pendentes = coletas.size - sincronizadas
                        
                        // Extrair salas únicas
                        // Prioridade: localizacaoEncontrada (onde o item FOI ENCONTRADO) > nomeSala (localização ORIGINAL)
                        Log.d(TAG, "═══════════════════════════════════════════")
                        Log.d(TAG, "EXTRAINDO SALAS DAS COLETAS")
                        Log.d(TAG, "Total de coletas recebidas: ${coletas.size}")
                        coletas.take(10).forEach { coleta ->
                            Log.d(TAG, "  Coleta ${coleta.id}: localizacaoEncontrada='${coleta.localizacaoEncontrada}', nomeSala='${coleta.nomeSala}', localizacaoAtual='${coleta.localizacaoAtual}'")
                        }
                        
                        val salasUnicas = coletas
                            .mapNotNull { it.localizacaoEncontrada ?: it.nomeSala ?: it.localizacaoAtual }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .sorted()
                        
                        Log.d(TAG, "Salas extraídas (${salasUnicas.size}): $salasUnicas")
                        Log.d(TAG, "═══════════════════════════════════════════")
                        
                        Log.d(TAG, "Estatísticas:")
                        Log.d(TAG, "  Total: ${coletas.size}")
                        Log.d(TAG, "  Sincronizadas: $sincronizadas")
                        Log.d(TAG, "  Pendentes: $pendentes")
                        Log.d(TAG, "  Salas: ${salasUnicas.size}")
                        
                        // Aplicar filtros iniciais
                        val filtradas = aplicarFiltros(coletas)
                        
                        _state.value = CollectionViewState.Success(
                            coletas = coletas,
                            filteredColetas = filtradas,
                            salas = salasUnicas,
                            totalColetas = filtradas.size,
                            sincronizadas = filtradas.count { it.sincronizado },
                            pendentes = filtradas.count { !it.sincronizado }
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Erro ao carregar coletas", error)
                        _state.value = CollectionViewState.Error(
                            error.message ?: "Erro ao carregar coletas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erro inesperado", e)
                _state.value = CollectionViewState.Error(
                    e.message ?: "Erro inesperado"
                )
            }
        }
    }
    
    /**
     * Filtra coletas por usuário
     */
    fun filtrarPorUsuario(filtro: FiltroUsuario) {
        Log.d(TAG, "filtrarPorUsuario: $filtro")
        filtroUsuario = filtro
        atualizarFiltros()
    }
    
    /**
     * Filtra coletas por status de sincronização
     * CORREÇÃO: Quando filtro "Sem Etiqueta" é selecionado, mostrar todos os itens sem etiqueta
     * independente da sala selecionada
     */
    fun filtrarPorStatus(filtro: FiltroStatus) {
        Log.d(TAG, "filtrarPorStatus: $filtro")
        filtroStatus = filtro
        
        // Se filtro "Sem Etiqueta" foi selecionado, limpar filtro de sala para mostrar todos
        if (filtro == FiltroStatus.SEM_ETIQUETA) {
            Log.d(TAG, "filtrarPorStatus: SEM_ETIQUETA selecionado, limpando filtro de sala para mostrar todos")
            salaSelecionada = null
        }
        
        atualizarFiltros()
    }
    
    /**
     * Filtra coletas por sala
     */
    fun filtrarPorSala(sala: String?) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "FILTRAR POR SALA")
        Log.d(TAG, "Sala selecionada: '${sala ?: "NENHUMA"}'")
        Log.d(TAG, "Sala anterior: '${salaSelecionada ?: "NENHUMA"}'")
        
        salaSelecionada = sala
        
        // Debug: Mostrar salas disponíveis
        val salaAtual = sala // Cópia local para evitar smart cast issues
        if (!salaAtual.isNullOrBlank()) {
            val salasDisponiveis = todasColetas
                .mapNotNull { it.localizacaoEncontrada ?: it.nomeSala }
                .distinct()
                .sorted()
            
            Log.d(TAG, "Salas disponíveis (${salasDisponiveis.size}):")
            salasDisponiveis.forEach { s ->
                val match = s.equals(salaAtual, ignoreCase = true)
                Log.d(TAG, "  - '$s' ${if (match) "✓ MATCH" else ""}")
            }
        }
        
        Log.d(TAG, "═══════════════════════════════════════")
        
        atualizarFiltros()
    }
    
    /**
     * Limpa o filtro de sala (volta para estado inicial sem carregar tudo)
     * CORREÇÃO: Se filtro "Sem Etiqueta" estiver ativo, mostrar itens sem etiqueta de todas as salas
     */
    fun limparFiltroSala() {
        Log.d(TAG, "limparFiltroSala: Limpando filtro de sala")
        salaSelecionada = null
        
        // Se filtro "Sem Etiqueta" estiver ativo, aplicar filtros normalmente
        if (filtroStatus == FiltroStatus.SEM_ETIQUETA) {
            Log.d(TAG, "limparFiltroSala: Filtro SEM_ETIQUETA ativo, aplicando filtros")
            atualizarFiltros()
            return
        }
        
        // Quando nenhuma sala está selecionada e não é filtro "Sem Etiqueta", mostrar lista vazia com mensagem
        val currentState = _state.value
        if (currentState is CollectionViewState.Success) {
            _state.value = currentState.copy(
                filteredColetas = emptyList(),
                totalColetas = 0,
                sincronizadas = 0,
                pendentes = 0
            )
        }
    }
    
    /**
     * Atualiza os filtros aplicados
     */
    private fun atualizarFiltros() {
        val currentState = _state.value
        if (currentState !is CollectionViewState.Success) {
            Log.w(TAG, "atualizarFiltros: Estado não é Success, ignorando")
            return
        }
        
        val filtradas = aplicarFiltros(todasColetas)
        
        _state.value = currentState.copy(
            filteredColetas = filtradas,
            totalColetas = filtradas.size,
            sincronizadas = filtradas.count { it.sincronizado },
            pendentes = filtradas.count { !it.sincronizado }
        )
    }
    
    /**
     * Aplica todos os filtros ativos
     */
    private fun aplicarFiltros(coletas: List<Coleta>): List<Coleta> {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "APLICANDO FILTROS")
        Log.d(TAG, "Filtro Usuário: $filtroUsuario")
        Log.d(TAG, "Filtro Status: $filtroStatus")
        Log.d(TAG, "Sala Selecionada: '$salaSelecionada'")
        Log.d(TAG, "Query Busca: '$queryBusca'")
        Log.d(TAG, "Total de coletas: ${coletas.size}")
        Log.d(TAG, "Usuário Atual ID: $usuarioAtualId")
        
        var filtradas = coletas
        
        // Filtro de busca por texto
        if (queryBusca.isNotBlank()) {
            filtradas = filtradas.filter { coleta ->
                val patrimonioIdMatch = coleta.patrimonioId.toString().contains(queryBusca, ignoreCase = true)
                val observacoesMatch = (coleta.observacoes ?: "").contains(queryBusca, ignoreCase = true)
                // Buscar em localizacaoEncontrada (onde FOI ENCONTRADO) e nomeSala (localização ORIGINAL)
                val salaMatch = (coleta.localizacaoEncontrada ?: coleta.nomeSala ?: "").contains(queryBusca, ignoreCase = true)
                patrimonioIdMatch || observacoesMatch || salaMatch
            }
            Log.d(TAG, "Após filtro de busca: ${filtradas.size} coletas")
        }
        
        // Filtro de usuário
        filtradas = when (filtroUsuario) {
            FiltroUsuario.TODAS -> {
                Log.d(TAG, "Filtro TODAS: ${filtradas.size} coletas")
                filtradas
            }
            FiltroUsuario.MINHAS -> {
                if (usuarioAtualId != null) {
                    Log.d(TAG, "Aplicando filtro MINHAS para usuário $usuarioAtualId")
                    Log.d(TAG, "IDs de usuário nas coletas:")
                    filtradas.take(10).forEach { coleta ->
                        Log.d(TAG, "  Coleta ${coleta.id}: usuarioId=${coleta.usuarioId}")
                    }
                    
                    // CORREÇÃO: Comparar como Int
                    val minhas = filtradas.filter { coleta ->
                        val match = coleta.usuarioId == usuarioAtualId
                        if (!match) {
                            Log.d(TAG, "  Coleta ${coleta.id}: ${coleta.usuarioId} != $usuarioAtualId")
                        }
                        match
                    }
                    
                    Log.d(TAG, "Filtro MINHAS: ${minhas.size} coletas do usuário $usuarioAtualId")
                    
                    if (minhas.isEmpty()) {
                        Log.w(TAG, "⚠️ NENHUMA coleta encontrada para o usuário $usuarioAtualId!")
                        Log.w(TAG, "Mostrando TODAS as coletas como fallback")
                        // FALLBACK: Se não encontrar nenhuma, mostrar todas
                        filtradas
                    } else {
                        minhas
                    }
                } else {
                    Log.w(TAG, "Filtro MINHAS: usuarioAtualId é null, mostrando todas")
                    filtradas
                }
            }
        }
        
        Log.d(TAG, "Após filtro de usuário: ${filtradas.size} coletas")
        
        // Filtro de status
        filtradas = when (filtroStatus) {
            FiltroStatus.TODOS -> filtradas
            FiltroStatus.SINCRONIZADOS -> filtradas.filter { it.sincronizado }
            FiltroStatus.PENDENTES -> filtradas.filter { !it.sincronizado }
            FiltroStatus.SEM_ETIQUETA -> {
                // Debug: Contar itens sem etiqueta antes do filtro
                val totalSemEtiquetaFlag = filtradas.count { it.semEtiqueta }
                val totalSemNumeroComDescricao = filtradas.count { 
                    it.numeroPatrimonio.isNullOrBlank() && !it.descricaoItemSemEtiqueta.isNullOrBlank() 
                }
                Log.d(TAG, "📊 FILTRO SEM_ETIQUETA:")
                Log.d(TAG, "  - Total coletas antes: ${filtradas.size}")
                Log.d(TAG, "  - Com flag semEtiqueta=true: $totalSemEtiquetaFlag")
                Log.d(TAG, "  - Sem número + com descrição: $totalSemNumeroComDescricao")
                
                // Debug: Mostrar algumas coletas com semEtiqueta=true
                filtradas.filter { it.semEtiqueta }.take(3).forEach { coleta ->
                    Log.d(TAG, "  ✓ Coleta ${coleta.id}: semEtiqueta=${coleta.semEtiqueta}, desc='${coleta.descricaoItemSemEtiqueta?.take(30)}'")
                }
                
                filtradas.filter { coleta ->
                    // Coletas sem etiqueta: campo semEtiqueta = true OU (numeroPatrimonio vazio e descricaoItemSemEtiqueta preenchido)
                    coleta.semEtiqueta || (coleta.numeroPatrimonio.isNullOrBlank() && !coleta.descricaoItemSemEtiqueta.isNullOrBlank())
                }
            }
        }
        
        Log.d(TAG, "Após filtro de status: ${filtradas.size} coletas")
        
        // Filtro de sala
        // CORREÇÃO: Usar mesma lógica de extração e comparação case-insensitive
        val salaFiltro = salaSelecionada // Cópia local para evitar smart cast issues
        filtradas = if (salaFiltro.isNullOrBlank()) {
            Log.d(TAG, "Filtro de sala: TODAS (nenhuma selecionada)")
            filtradas
        } else {
            Log.d(TAG, "Filtrando por sala: '$salaFiltro'")
            
            val salaFiltroTrimmed = salaFiltro.trim()
            val resultado = filtradas.filter { coleta ->
                // Extrair sala da coleta usando mesma lógica
                val salaColeta = (coleta.localizacaoEncontrada ?: coleta.nomeSala)?.trim()
                val match = salaColeta.equals(salaFiltroTrimmed, ignoreCase = true)
                
                if (!match) {
                    Log.d(TAG, "  Coleta ${coleta.id}: sala='$salaColeta' != '$salaFiltroTrimmed'")
                }
                
                match
            }
            
            Log.d(TAG, "Coletas na sala '$salaFiltroTrimmed': ${resultado.size}")
            
            // Debug: Mostrar salas únicas nas coletas filtradas
            if (resultado.isEmpty()) {
                Log.w(TAG, "⚠️ NENHUMA coleta encontrada para sala '$salaFiltroTrimmed'")
                Log.w(TAG, "Salas disponíveis nas coletas:")
                filtradas.mapNotNull { it.localizacaoEncontrada ?: it.nomeSala }
                    .distinct()
                    .sorted()
                    .forEach { sala ->
                        Log.w(TAG, "  - '$sala'")
                    }
            }
            
            resultado
        }
        
        Log.d(TAG, "Resultado final: ${filtradas.size} coletas")
        Log.d(TAG, "═══════════════════════════════════════")
        
        return filtradas
    }
    
    /**
     * Busca por texto (patrimônio, observações, sala)
     */
    fun buscar(query: String) {
        Log.d(TAG, "buscar: query='$query'")
        queryBusca = query
        atualizarFiltros()
    }
    
    /**
     * Remove uma coleta
     */
    fun removerColeta(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "removerColeta: id=$id")
            
            removerColetaUseCase(id).fold(
                onSuccess = {
                    Log.d(TAG, "✓ Coleta removida com sucesso")
                    // Recarregar coletas após remoção
                    carregarColetas()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erro ao remover coleta", error)
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao remover coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Carrega coletas com fallback automático (Offline-First)
     * Tenta servidor primeiro, fallback para local se falhar
     */
    fun carregarColetasComFallback() {
        viewModelScope.launch {
            Log.d(TAG, "carregarColetasComFallback: Iniciando carregamento com fallback")
            _state.value = CollectionViewState.Loading
            
            try {
                // Verificar se precisa migração de coletas antigas
                if (coletaMigration.precisaMigracao()) {
                    Log.d(TAG, "⚠ Coletas antigas precisam de migração, executando...")
                    coletaMigration.migrarColetasAntigas()
                }
                
                // Obter usuário atual
                val usuario = obterUsuarioAtualUseCase()
                usuarioAtualId = usuario?.id?.toInt()
                
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "USUÁRIO ATUAL")
                Log.d(TAG, "Nome: ${usuario?.nome}")
                Log.d(TAG, "ID: $usuarioAtualId")
                Log.d(TAG, "═══════════════════════════════════════════")
                
                // Buscar coletas com fallback
                buscarColetasComFallbackUseCase().fold(
                    onSuccess = { result ->
                        Log.d(TAG, "✓ ${result.coletas.size} coletas carregadas")
                        Log.d(TAG, "Fonte: ${result.fonte}")
                        
                        todasColetas = result.coletas
                        
                        // Calcular estatísticas
                        val sincronizadas = result.coletas.count { it.sincronizado }
                        val pendentes = result.coletas.size - sincronizadas
                        
                        // Extrair salas únicas
                        // Prioridade: localizacaoEncontrada (onde o item FOI ENCONTRADO) > nomeSala (localização ORIGINAL) > localizacaoAtual
                        val salasUnicas = result.coletas
                            .mapNotNull { it.localizacaoEncontrada ?: it.nomeSala ?: it.localizacaoAtual }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .sorted()
                        
                        Log.d(TAG, "Salas extraídas (localizacaoEncontrada/nomeSala/localizacaoAtual): $salasUnicas")
                        
                        Log.d(TAG, "Estatísticas:")
                        Log.d(TAG, "  Total: ${result.coletas.size}")
                        Log.d(TAG, "  Sincronizadas: $sincronizadas")
                        Log.d(TAG, "  Pendentes: $pendentes")
                        Log.d(TAG, "  Salas: ${salasUnicas.size}")
                        
                        // Aplicar filtros iniciais
                        val filtradas = aplicarFiltros(result.coletas)
                        
                        _state.value = CollectionViewState.Success(
                            coletas = result.coletas,
                            filteredColetas = filtradas,
                            salas = salasUnicas,
                            totalColetas = filtradas.size,
                            sincronizadas = filtradas.count { it.sincronizado },
                            pendentes = filtradas.count { !it.sincronizado }
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Erro ao carregar coletas", error)
                        _state.value = CollectionViewState.Error(
                            error.message ?: "Erro ao carregar coletas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erro inesperado", e)
                _state.value = CollectionViewState.Error(
                    e.message ?: "Erro inesperado"
                )
            }
        }
    }
    
    /**
     * Reenviar uma coleta pendente específica
     */
    fun reenviarColeta(coletaId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "reenviarColeta: id=$coletaId")
            _state.value = CollectionViewState.Loading
            
            reenviarColetaUseCase(coletaId).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Coleta reenviada com sucesso")
                    _state.value = CollectionViewState.ColetaReenviada("Coleta reenviada com sucesso!")
                    // Recarregar coletas
                    carregarColetas()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao reenviar coleta", error)
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao reenviar coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Excluir uma coleta pendente
     * IMPORTANTE: Só permite excluir coletas NÃO sincronizadas
     */
    fun excluirColetaPendente(coletaId: Long) {
        viewModelScope.launch {
            Log.d(TAG, "excluirColetaPendente: id=$coletaId")
            _state.value = CollectionViewState.Loading
            
            excluirColetaPendenteUseCase(coletaId).fold(
                onSuccess = {
                    Log.d(TAG, "✅ Coleta excluída com sucesso")
                    _state.value = CollectionViewState.ColetaExcluida("Coleta excluída com sucesso!")
                    // Recarregar coletas
                    carregarColetas()
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Erro ao excluir coleta", error)
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao excluir coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Limpa o estado
     */
    fun limparEstado() {
        _state.value = CollectionViewState.Idle
    }
}
