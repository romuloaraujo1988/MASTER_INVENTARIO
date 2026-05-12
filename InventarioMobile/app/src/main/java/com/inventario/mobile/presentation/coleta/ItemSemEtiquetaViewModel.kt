@file:OptIn(kotlinx.coroutines.FlowPreview::class)

package com.inventario.mobile.presentation.coleta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao
import com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase
import com.inventario.mobile.domain.usecase.LimparCacheDeOutrosInventariosUseCase
import com.inventario.mobile.domain.usecase.MarcarPatrimonioColetadoLocalmenteUseCase
import com.inventario.mobile.domain.usecase.RegistrarColetaUseCase
import com.inventario.mobile.presentation.coleta.state.SugestaoDescricaoState
import com.inventario.mobile.utils.PreferencesManager
import com.inventario.mobile.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para registro de itens sem etiqueta.
 *
 * Histórico:
 * - v2.10: Feedback tátil (vibração) ao coletar
 * - v2.22 (feature `coleta-descricao-livre-com-sugestao`):
 *     - Autocomplete opcional de descrições de patrimônios não coletados
 *       (toggle + debounce 300ms + StateFlow `sugestaoState`).
 *     - Validação de tamanho da descrição livre (3..255 após trim)
 *       antes do envio à camada de domínio.
 *     - Marcação do patrimônio vinculado como coletado localmente após
 *       coleta bem-sucedida (Req 8.1).
 *
 * A descrição final persistida é SEMPRE o conteúdo corrente do campo livre
 * após `trim()`, independentemente do estado do toggle ou de a seleção ter
 * vindo do autocomplete (Requirements 4.1, 4.2).
 */
@HiltViewModel
class ItemSemEtiquetaViewModel @Inject constructor(
    private val registrarColetaUseCase: RegistrarColetaUseCase,
    private val buscarSugestoesDescricaoUseCase: BuscarSugestoesDescricaoUseCase,
    private val marcarPatrimonioColetadoLocalmenteUseCase: MarcarPatrimonioColetadoLocalmenteUseCase,
    private val limparCacheDeOutrosInventariosUseCase: LimparCacheDeOutrosInventariosUseCase,
    private val preferencesManager: PreferencesManager,
    private val vibrationHelper: VibrationHelper // v2.10: Feedback tátil
) : ViewModel() {

    // ------------------------------------------------------------------
    // Estado existente: ciclo de vida do registro da coleta
    // ------------------------------------------------------------------

    private val _state = MutableStateFlow<ItemSemEtiquetaState>(ItemSemEtiquetaState.Idle)
    val state: StateFlow<ItemSemEtiquetaState> = _state.asStateFlow()

    // ------------------------------------------------------------------
    // Estado do autocomplete de sugestões (feature nova)
    // Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.2, 3.3, 3.6
    // ------------------------------------------------------------------

    private val _sugestaoState =
        MutableStateFlow<SugestaoDescricaoState>(SugestaoDescricaoState.Oculto)
    val sugestaoState: StateFlow<SugestaoDescricaoState> = _sugestaoState.asStateFlow()

    private val _toggleAtivo = MutableStateFlow(false)
    val toggleAtivo: StateFlow<Boolean> = _toggleAtivo.asStateFlow()

    private val _termoBusca = MutableStateFlow("")

    /**
     * Eventos one-shot emitidos para a UI (ex.: preencher o campo livre
     * com a descrição selecionada). Usar [SharedFlow] evita que o evento
     * seja re-entregue em rotações ou recomposições e desacopla o vínculo
     * com a sugestão — a UI apenas aplica o texto e segue observando o
     * conteúdo do próprio `EditText` (Requirement 4.2).
     */
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 8)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    init {
        // Req 7.1 — isolamento por inventário: ao abrir a tela, limpar
        // cache de sugestões de inventários diferentes do ativo. Falha é
        // apenas logada (a consulta do DAO também filtra por idInventario
        // como defesa em profundidade, portanto a ausência de limpeza
        // nunca compromete a correção funcional).
        viewModelScope.launch {
            val idInventarioAtivo = preferencesManager.getInventarioAtivoId()
            if (idInventarioAtivo != null) {
                runCatching {
                    limparCacheDeOutrosInventariosUseCase(idInventarioAtivo)
                }.onFailure { e ->
                    android.util.Log.w(
                        "ItemSemEtiquetaVM",
                        "⚠️ Falha ao limpar cache de outros inventários (Req 7.1)",
                        e
                    )
                }
            }
        }

        // Req 3.7 — debounce 300ms + distinctUntilChanged; carregamento
        // só ocorre quando o toggle está ativo. `collectLatest` cancela a
        // corrotina anterior ao receber um novo termo, descartando
        // respostas obsoletas.
        viewModelScope.launch {
            val termoBuscaDebounced = _termoBusca
                .debounce(300L)
                .distinctUntilChanged()

            combine(_toggleAtivo, termoBuscaDebounced) { ativo, termo -> ativo to termo }
                .filter { (ativo, _) -> ativo }
                .collectLatest { (_, termo) -> carregarSugestoes(termo) }
        }
    }

    // ------------------------------------------------------------------
    // Métodos públicos do autocomplete
    // ------------------------------------------------------------------

    /**
     * Alterna a visibilidade do autocomplete de sugestões.
     *
     * - Quando [ativo] é `false`, emite imediatamente [SugestaoDescricaoState.Oculto]
     *   e interrompe o ciclo de carregamento (Requirements 2.2, 2.4).
     * - Quando [ativo] é `true`, o carregamento é acionado pelo fluxo
     *   de debounce em [init] (Requirements 2.3, 3.7).
     * - Em caso de exceção inesperada, emite [SugestaoDescricaoState.Erro]
     *   transitório sem propagar o erro — o campo livre permanece
     *   habilitado (Requirement 2.5).
     */
    fun setToggleSugestao(ativo: Boolean) {
        try {
            _toggleAtivo.value = ativo
            if (!ativo) {
                _sugestaoState.value = SugestaoDescricaoState.Oculto
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "ItemSemEtiquetaVM",
                "❌ Falha ao alternar toggle de sugestão",
                e
            )
            _sugestaoState.value = SugestaoDescricaoState.Erro(
                e.message ?: "Falha ao alternar sugestões"
            )
        }
    }

    /**
     * Atualiza o termo de busca corrente. O debounce de 300ms no fluxo
     * interno evita execuções em cascata (Requirement 3.7).
     */
    fun onTermoBuscaChange(termo: String) {
        _termoBusca.value = termo
    }

    /**
     * Emite evento para a UI preencher o campo livre com a descrição
     * selecionada. O ViewModel NÃO armazena vínculo forte com a sugestão
     * selecionada — a descrição final continua sendo o conteúdo corrente
     * do campo livre no momento da confirmação (Requirements 3.5, 4.2).
     */
    fun onSugestaoSelecionada(s: SugestaoDescricao) {
        viewModelScope.launch {
            _events.emit(Event.PreencherCampoLivre(s.descricao))
        }
    }

    /**
     * Carrega sugestões do repositório e mapeia o resultado em
     * [SugestaoDescricaoState]. Emite [SugestaoDescricaoState.Carregando]
     * antes da chamada (Requirements 2.2, 2.3, 2.4, 3.5, 3.6, 3.7, 4.2).
     *
     * O repositório é offline-first: falhas de rede retornam
     * [Result.success] com origem [OrigemSugestoes.CACHE] ou
     * [OrigemSugestoes.VAZIO_SEM_CACHE]. Apenas exceções inesperadas
     * produzem [SugestaoDescricaoState.Erro].
     */
    private suspend fun carregarSugestoes(termo: String) {
        _sugestaoState.value = SugestaoDescricaoState.Carregando

        val idInventario = preferencesManager.getInventarioAtivoId()
        if (idInventario == null) {
            // Sem inventário ativo não há universo de sugestões.
            _sugestaoState.value =
                SugestaoDescricaoState.SemResultados(OrigemSugestoes.VAZIO_SEM_CACHE)
            return
        }

        val resultado = buscarSugestoesDescricaoUseCase(idInventario, termo)
        resultado.fold(
            onSuccess = { res ->
                _sugestaoState.value = if (res.sugestoes.isEmpty()) {
                    SugestaoDescricaoState.SemResultados(res.origem)
                } else {
                    SugestaoDescricaoState.Carregado(res.sugestoes, res.origem)
                }
            },
            onFailure = { error ->
                _sugestaoState.value = SugestaoDescricaoState.Erro(
                    error.message ?: "Erro ao carregar sugestões"
                )
            }
        )
    }

    // ------------------------------------------------------------------
    // Registro da coleta (API pública existente, estendida)
    // ------------------------------------------------------------------

    /**
     * Registra item sem etiqueta.
     *
     * Recebe o caminho do arquivo de foto (gerado pelo `PhotoHelper` na Activity)
     * em vez de Base64. O upload para o servidor é feito pelo `PhotoSyncWorker`
     * em background após a coleta ser sincronizada.
     *
     * v2.21: substituído `fotoBase64` por `fotoPath` + `fotoThumbnailPath`.
     * v2.22: aplica `trim()` e valida `3..255` antes de delegar à camada de
     * domínio (Requirements 1.3, 1.4, 1.5, 1.6, 4.1, 4.3, 4.4, 4.5); após
     * sucesso, marca o patrimônio vinculado como coletado localmente no
     * cache de sugestões (Requirement 8.1). Para coletas totalmente livres
     * (sem vínculo a patrimônio existente), a marcação é um no-op.
     */
    fun registrarItemSemEtiqueta(
        descricao: String,
        categoria: String,
        estado: String,
        localizacao: String,
        observacoes: String,
        fotoPath: String,
        fotoThumbnailPath: String? = null
    ) {
        // Validação de tamanho antes de acionar qualquer efeito colateral
        // (Requirements 1.3, 1.4, 1.6, 4.1, 4.4, 4.5).
        val descricaoTrim = descricao.trim()
        if (descricaoTrim.length < 3) {
            _state.value = ItemSemEtiquetaState.Error(
                "A descrição é obrigatória com no mínimo 3 caracteres"
            )
            return
        }
        if (descricaoTrim.length > 255) {
            _state.value = ItemSemEtiquetaState.Error(
                "A descrição deve ter no máximo 255 caracteres"
            )
            return
        }

        viewModelScope.launch {
            _state.value = ItemSemEtiquetaState.Loading

            try {
                android.util.Log.d("ItemSemEtiquetaVM", "═══════════════════════════════════════════")
                android.util.Log.d("ItemSemEtiquetaVM", "🏷️ Registrando item SEM ETIQUETA")
                android.util.Log.d("ItemSemEtiquetaVM", "   Descrição: $descricaoTrim")
                android.util.Log.d("ItemSemEtiquetaVM", "   Categoria: $categoria")
                android.util.Log.d("ItemSemEtiquetaVM", "   Estado: $estado")
                android.util.Log.d("ItemSemEtiquetaVM", "   Localização: $localizacao")
                android.util.Log.d("ItemSemEtiquetaVM", "   Foto: $fotoPath")

                val result = registrarColetaUseCase.registrarItemSemEtiqueta(
                    descricao = descricaoTrim,
                    categoria = categoria,
                    salaId = null,
                    localizacaoAtual = localizacao,
                    estadoEncontrado = estado,
                    observacoes = observacoes,
                    latitude = null,
                    longitude = null,
                    fotoPath = fotoPath,
                    fotoThumbnailPath = fotoThumbnailPath
                )

                if (result.isSuccess) {
                    android.util.Log.d("ItemSemEtiquetaVM", "✓ Item sem etiqueta registrado com sucesso!")

                    // Atualizar cache de sugestões (Requirement 8.1).
                    // Item sem etiqueta livre não possui vínculo com
                    // patrimônio identificado, portanto o use case é um
                    // no-op nesse caso — é mantido aqui para manter o
                    // contrato coerente caso um vínculo seja introduzido
                    // no futuro (ex.: coleta por seleção de sugestão
                    // preservando `idPatrimonio`).
                    runCatching {
                        val idInventario = preferencesManager.getInventarioAtivoId()
                        if (idInventario != null) {
                            val idPatrimonioVinculado: Int? = null
                            marcarPatrimonioColetadoLocalmenteUseCase(
                                idInventario,
                                idPatrimonioVinculado
                            )
                        }
                    }.onFailure { e ->
                        // Falha no cache local não deve invalidar o sucesso
                        // da coleta já persistida — apenas logar.
                        android.util.Log.w(
                            "ItemSemEtiquetaVM",
                            "⚠️ Falha ao atualizar cache de sugestões após coleta",
                            e
                        )
                    }

                    // v2.10: Vibrar ao coletar (se habilitado nas configurações)
                    vibrationHelper.vibrateOnCollection()

                    _state.value = ItemSemEtiquetaState.Success
                } else {
                    val error = result.exceptionOrNull()
                    android.util.Log.e("ItemSemEtiquetaVM", "❌ Erro: ${error?.message}")
                    _state.value = ItemSemEtiquetaState.Error(
                        error?.message ?: "Erro ao registrar item sem etiqueta"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ItemSemEtiquetaVM", "❌ Exceção: ${e.message}", e)
                _state.value = ItemSemEtiquetaState.Error(
                    e.message ?: "Erro desconhecido"
                )
            }
        }
    }

    /**
     * Limpa o estado
     */
    fun clearState() {
        _state.value = ItemSemEtiquetaState.Idle
    }

    // ------------------------------------------------------------------
    // Eventos one-shot
    // ------------------------------------------------------------------

    /**
     * Eventos one-shot emitidos pelo ViewModel para a UI consumir via
     * `SharedFlow`. Distintos do estado persistente ([sugestaoState],
     * [state]), não devem ser re-entregues em rotações/recomposições.
     */
    sealed class Event {
        /**
         * Pede à UI para preencher o campo de descrição livre com o
         * [texto] informado (descrição escolhida no autocomplete).
         * A UI deve aplicar `setText(texto)` e reposicionar o cursor,
         * sem desabilitar o campo (Requirements 3.5, 4.2, 7.4).
         */
        data class PreencherCampoLivre(val texto: String) : Event()
    }
}

/**
 * Estados da tela de item sem etiqueta
 */
sealed class ItemSemEtiquetaState {
    object Idle : ItemSemEtiquetaState()
    object Loading : ItemSemEtiquetaState()
    object Success : ItemSemEtiquetaState()
    data class Error(val message: String) : ItemSemEtiquetaState()
}
