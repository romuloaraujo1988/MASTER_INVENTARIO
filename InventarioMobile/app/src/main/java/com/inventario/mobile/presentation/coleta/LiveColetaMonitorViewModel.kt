package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.data.realtime.ColetaEvento
import com.inventario.mobile.data.realtime.RealtimeColetasManager
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel do "Monitor ao vivo" de coletas.
 *
 * Mantém em memória apenas as coletas recebidas via [RealtimeColetasManager]
 * desde que o usuário abriu a tela — não carrega histórico do banco. Pensado
 * para um supervisor acompanhar a coleta em curso de outros usuários sem ter
 * que dar pull-to-refresh.
 *
 * As coletas são adicionadas no topo da lista (ordem cronológica reversa).
 *
 * Estados expostos:
 *  - [coletas]    — lista atual (mais recente primeiro)
 *  - [conexao]    — estado da conexão WebSocket, repassado do manager
 *  - [contador]   — quantidade total recebida nesta sessão (não diminui ao
 *                    limpar a lista — útil pra mostrar "X coletas hoje" mesmo
 *                    se o usuário limpar a tela)
 */
@HiltViewModel
class LiveColetaMonitorViewModel @Inject constructor(
    private val realtimeManager: RealtimeColetasManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    /** Coletas recebidas, mais recente primeiro. */
    private val _coletas = MutableStateFlow<List<Coleta>>(emptyList())
    val coletas: StateFlow<List<Coleta>> = _coletas.asStateFlow()

    /** Quantidade total acumulada nesta sessão (não decai ao limpar lista). */
    private val _contador = MutableStateFlow(0)
    val contador: StateFlow<Int> = _contador.asStateFlow()

    val conexao: StateFlow<RealtimeColetasManager.ConnectionState> =
        realtimeManager.connectionState

    /**
     * Limite máximo de itens mantidos em memória. Evita crescimento sem fim
     * em sessões longas — coletas mais antigas saem da lista quando passam
     * desse limite (mas o contador continua subindo).
     */
    private val limiteMaximoItens = 500

    init {
        observarFeed()
    }

    /**
     * Inicia ou retoma a conexão. Idempotente — chamar em `onStart` da
     * Activity é seguro mesmo se já estava conectado.
     */
    fun iniciarMonitoramento() {
        realtimeManager.connect(preferencesManager.getInventarioAtivoId())
    }

    /**
     * Encerra a conexão WebSocket. Chamar em `onStop` da Activity para
     * economizar bateria/dados quando o usuário não está mais olhando a tela.
     */
    fun pararMonitoramento() {
        realtimeManager.disconnect()
    }

    /**
     * Limpa a lista visível mantendo o contador acumulado.
     */
    fun limparLista() {
        _coletas.update { emptyList() }
    }

    /**
     * Coleta o flow de eventos do manager e mantém a lista atualizada.
     * O Flow do manager é compartilhado (SharedFlow) — vários ViewModels
     * podem coexistir sem disputar eventos.
     */
    private fun observarFeed() {
        viewModelScope.launch {
            realtimeManager.events.collect { evento ->
                aplicarEvento(evento)
            }
        }
    }

    /**
     * Converte um [ColetaEvento] em [Coleta] (modelo da UI) e insere no topo.
     * Idempotente — se chega duas vezes o mesmo `id` (ex.: reconnect com
     * replay de algum proxy intermediário), substitui em vez de duplicar.
     */
    private fun aplicarEvento(evento: ColetaEvento) {
        if (evento.tipo != "COLETA_REGISTRADA") return

        val coleta = mapearParaColeta(evento)

        _coletas.update { atual ->
            val semDuplicata = atual.filter { it.id != coleta.id }
            val novaLista = listOf(coleta) + semDuplicata
            if (novaLista.size > limiteMaximoItens) {
                novaLista.take(limiteMaximoItens)
            } else {
                novaLista
            }
        }
        _contador.update { it + 1 }
    }

    private fun mapearParaColeta(evento: ColetaEvento): Coleta {
        return Coleta(
            id = evento.id?.toInt(),
            patrimonioId = 0, // não temos no payload — não usado pelo adapter
            usuarioId = 0,
            dataColeta = evento.dataColeta ?: System.currentTimeMillis().toString(),
            numeroPatrimonio = evento.numeroPatrimonio,
            descricaoPatrimonio = evento.descricaoPatrimonio,
            nomeColetor = evento.nomeColetor,
            nomeSala = evento.nomeSala,
            localizacaoEncontrada = evento.nomeSala,
            estadoEncontrado = evento.estadoEncontrado,
            sincronizado = true, // chega do servidor já sincronizado
            semEtiqueta = evento.semEtiqueta == true,
            descricaoItemSemEtiqueta = evento.descricaoItemSemEtiqueta,
            temFoto = evento.temFoto
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Não chamamos disconnect aqui — a Activity controla o ciclo via
        // onStart/onStop, o que é mais previsível. Se outro componente
        // precisar do feed simultaneamente, basta injetar o manager.
    }
}
