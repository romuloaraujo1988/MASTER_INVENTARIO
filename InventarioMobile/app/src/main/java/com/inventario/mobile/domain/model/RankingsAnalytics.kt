package com.inventario.mobile.domain.model

/**
 * Modelos de domínio puros para os relatórios analíticos do dashboard:
 * Top Coletores, Setores Pendentes e Projeção de Término.
 *
 * Mantidos no mesmo arquivo por compartilharem a feature
 * "Rankings + Projeção" (ver `RankingsFragment` e `OverviewFragment`).
 *
 * Nenhuma dependência Android — Kotlin puro, idiomático para serem
 * mockáveis em testes unitários.
 */

/**
 * Item do ranking de coletores.
 *
 * @property idUsuario ID do usuário no servidor
 * @property nome Nome de exibição (cai no `login` se o nome não estiver setado)
 * @property totalColetas Total de coletas feitas pelo usuário no inventário
 * @property ultimaColetaMillis Timestamp em ms da última coleta (`null` se nunca coletou)
 * @property ultimaColetaFormatada Texto pronto "dd/MM/yyyy HH:mm" para exibição
 * @property produtividadePorDia Coletas/dia calculadas pelo servidor a partir da
 *           data de início do inventário; `null` se o inventário não tem dataInicio
 */
data class TopColetor(
    val idUsuario: Int,
    val nome: String,
    val totalColetas: Int,
    val ultimaColetaMillis: Long?,
    val ultimaColetaFormatada: String?,
    val produtividadePorDia: Double?
)

/**
 * Item do ranking de setores com mais pendentes.
 *
 * @property idSetor ID do setor no servidor
 * @property nome Nome do setor
 * @property totalPatrimonios Total de patrimônios pertencentes ao setor que entram no inventário
 * @property totalColetados Total já coletado no inventário
 * @property totalPendentes Pendentes restantes
 * @property percentualConcluido Percentual já coletado (0..100)
 */
data class SetorPendente(
    val idSetor: Int,
    val nome: String,
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val totalPendentes: Int,
    val percentualConcluido: Double
)

/**
 * Projeção de término do inventário.
 *
 * Estrutura espelha a resposta do endpoint `/api/mobile/dashboard/projecao-termino`.
 * Uso típico na UI:
 *   - Se [concluido]: mostrar "Concluído" no card.
 *   - Se [dadosInsuficientes]: mostrar mensagem orientando a coletar mais antes
 *     de projetar.
 *   - Caso contrário: usar [diasRestantes] e [dataPrevistaFormatada] para o card,
 *     e [dentroDoPrazo] para colorir o status (verde/vermelho).
 */
data class ProjecaoTermino(
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val totalPendentes: Int,
    val percentualConclusao: Double,
    val ritmoColetasPorDia: Double,
    val diasDecorridos: Long?,
    val diasRestantes: Long?,
    val dataPrevistaFormatada: String?,
    val dataPrevistaMillis: Long?,
    val concluido: Boolean,
    val dadosInsuficientes: Boolean,
    /** `true` se a previsão cabe dentro de `dataFim` do inventário; `null` se inventário não tem `dataFim`. */
    val dentroDoPrazo: Boolean?,
    /** Dias de folga (positivo) ou atraso (negativo) em relação ao `dataFim`; `null` se sem `dataFim`. */
    val diasDeFolgaOuAtraso: Long?,
    val dataFimInventarioFormatada: String?
)
