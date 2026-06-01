package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats

/**
 * Tipos puros de apresentação do Dashboard (Kotlin puro, sem dependências Android).
 *
 * Estes tipos são detalhes internos da camada de View usados pelo `DashboardUiMapper`
 * para transformar `DashboardStats` / `DashboardUiStateClean` em valores prontos para
 * render. Não fazem parte do contrato de domínio nem de API (Req 9.1, 9.2).
 *
 * A ausência de imports `android.*` mantém a lógica de apresentação testável na JVM
 * (testes de propriedade), satisfazendo a restrição de arquitetura (Fragment apenas
 * renderiza).
 */

/**
 * Textos já formatados (valores inteiros) dos quatro cartões de KPI do Dashboard.
 *
 * @property coletados texto do cartão "Coletados" (de `DashboardStats.totalColetados`).
 * @property pendentes texto do cartão "Pendentes" (de `DashboardStats.totalPendentes`).
 * @property divergencias texto do cartão "Divergências" (de `DashboardStats.divergencias`).
 * @property coletores texto do cartão "Coletores" (de `DashboardStats.coletoresAtivos`).
 */
data class KpiValues(
    val coletados: String,
    val pendentes: String,
    val divergencias: String,
    val coletores: String
)

/**
 * Ponto puro do gráfico de evolução, intermediário entre `EvolucaoColeta` e o
 * `Entry` do MPAndroidChart. Mantém o mapeamento testável sem instanciar tipos Android.
 *
 * @property index índice sequencial do ponto, a partir de 0, na ordem da lista de origem.
 * @property value valor do ponto (quantidade de coletas) no eixo Y.
 * @property label rótulo do ponto (campo `data` da `EvolucaoColeta`) usado no eixo X.
 */
data class ChartPoint(
    val index: Int,
    val value: Float,
    val label: String
)

/**
 * Estado global, mutuamente exclusivo, da tela do Dashboard (Req 6.3–6.6).
 *
 * No máximo um estado especial (Loading/Empty/Error) é exibido por vez; o conteúdo
 * populado só aparece em [Content].
 */
sealed class DashboardScreenState {
    /** Carregamento global: exibir skeleton/placeholder. */
    object Loading : DashboardScreenState()

    /** Sem inventário identificável: exibir estado vazio. */
    object Empty : DashboardScreenState()

    /** Falha ao carregar os dados do Dashboard. */
    data class Error(val message: String) : DashboardScreenState()

    /** Conteúdo populado com os dados atuais. */
    data class Content(val stats: DashboardStats) : DashboardScreenState()
}

/**
 * Estado, mutuamente exclusivo, da área do gráfico de evolução (Req 4.2, 4.4–4.6).
 *
 * No máximo um estado especial (Loading/Empty/Error) é exibido por vez; o gráfico
 * só é renderizado em [Data].
 */
sealed class ChartAreaState {
    /** Carregamento dos dados de evolução: exibir placeholder na área do gráfico. */
    object Loading : ChartAreaState()

    /** Lista de evolução vazia após carregamento sem erro. */
    object Empty : ChartAreaState()

    /** Falha ao carregar os dados de evolução. */
    data class Error(val message: String) : ChartAreaState()

    /** Pontos prontos para render no gráfico. */
    data class Data(val points: List<ChartPoint>) : ChartAreaState()
}
