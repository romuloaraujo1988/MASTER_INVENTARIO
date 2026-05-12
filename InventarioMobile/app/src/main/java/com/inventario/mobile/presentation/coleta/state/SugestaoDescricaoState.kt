package com.inventario.mobile.presentation.coleta.state

import com.inventario.mobile.domain.model.OrigemSugestoes
import com.inventario.mobile.domain.model.SugestaoDescricao

/**
 * Estados possíveis do autocomplete de sugestões de descrição na
 * [com.inventario.mobile.presentation.coleta.ItemSemEtiquetaActivity].
 *
 * O ViewModel expõe um [kotlinx.coroutines.flow.StateFlow] desta sealed class
 * que a UI observa para controlar a visibilidade e o conteúdo do
 * `RecyclerView` de sugestões, bem como mensagens contextuais (origem
 * "cache" ou "sem resultados").
 *
 * Invariantes de UI:
 * - O [com.inventario.mobile.presentation.coleta.ItemSemEtiquetaActivity] **nunca** desabilita o campo de descrição
 *   livre em função deste estado (Requirement 7.4).
 * - A descrição final persistida é sempre o conteúdo corrente do campo
 *   livre após `trim()`, independentemente deste estado (Requirement 4.1).
 */
sealed class SugestaoDescricaoState {

    /**
     * Estado inicial e estado quando o toggle de sugestão está desativado.
     *
     * Enquanto neste estado:
     * - Nenhuma consulta é feita ao endpoint de sugestões (Requirement 2.2).
     * - O autocomplete permanece oculto na UI (Requirement 2.2).
     */
    object Oculto : SugestaoDescricaoState()

    /**
     * Consulta em andamento — o coletor ativou o toggle ou alterou o
     * termo de busca e o ViewModel está aguardando resposta do
     * [com.inventario.mobile.domain.usecase.BuscarSugestoesDescricaoUseCase].
     *
     * A UI deve exibir um indicador de carregamento inline próximo ao
     * autocomplete (Requirement 3.3) sem bloquear o campo de descrição livre.
     */
    object Carregando : SugestaoDescricaoState()

    /**
     * Sugestões carregadas com sucesso, provenientes do servidor ou do
     * cache local.
     *
     * A UI renderiza os itens no `RecyclerView` respeitando os limites
     * definidos em [Requirement 3.3] (máximo 50 simultâneos online,
     * máximo 10 offline — Requirement 7.2). Quando [origem] é
     * [OrigemSugestoes.CACHE], a UI deve sinalizar ao coletor que os
     * dados podem estar desatualizados (Requirement 3.2).
     *
     * @property sugestoes Lista de sugestões a exibir, já ordenada e
     *                     filtrada pelo termo de busca.
     * @property origem Origem da lista — permite mensagens contextuais
     *                  na UI (Requirements 3.2, 3.3).
     */
    data class Carregado(
        val sugestoes: List<SugestaoDescricao>,
        val origem: OrigemSugestoes
    ) : SugestaoDescricaoState()

    /**
     * Consulta concluída com sucesso, mas o filtro pelo termo de busca
     * não retornou nenhuma sugestão.
     *
     * A UI exibe mensagem textual informando a ausência de resultados e
     * mantém o campo de descrição livre habilitado para entrada livre
     * (Requirement 3.6). A mensagem deve ser removida assim que o filtro
     * voltar a produzir ao menos uma sugestão (transição para
     * [Carregado]).
     *
     * @property origem Origem da consulta que resultou em lista vazia —
     *                  útil para diferenciar "zero resultados do servidor"
     *                  de "zero resultados do cache offline"
     *                  (Requirements 3.6, 7.3).
     */
    data class SemResultados(val origem: OrigemSugestoes) : SugestaoDescricaoState()

    /**
     * Falha transitória ao carregar sugestões (ex.: exceção inesperada
     * na alternância do toggle — Requirement 2.5).
     *
     * A UI exibe a [mensagem] em snackbar ou banner e mantém o campo de
     * descrição livre editável para que o coletor possa prosseguir
     * digitando livremente (Requirement 2.5, 7.4). Falhas de rede
     * normalmente são tratadas pelo repositório via fallback para o
     * cache, resultando em [Carregado] com origem
     * [OrigemSugestoes.CACHE] ou [SemResultados] com origem
     * [OrigemSugestoes.VAZIO_SEM_CACHE], não em [Erro].
     *
     * @property mensagem Texto curto e acionável a ser exibido ao coletor.
     */
    data class Erro(val mensagem: String) : SugestaoDescricaoState()
}
