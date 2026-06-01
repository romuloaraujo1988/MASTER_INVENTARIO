package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.model.EvolucaoColeta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Helper de apresentação puro (Kotlin puro, sem dependências de `View`/`Context`) que
 * transforma os modelos de domínio/estado do Dashboard em valores prontos para render.
 *
 * Toda a lógica de apresentação testável (clamp de percentual, formatação de rótulos,
 * mapeamento de KPIs, resolução de estados e mapeamento do gráfico) vive aqui, mantendo
 * o `DashboardFragment` como uma camada fina de binding (Clean Architecture + MVVM,
 * Req 9.1). A ausência de imports `android.*` torna estas funções verificáveis por
 * testes de propriedade na JVM.
 *
 * Todas as funções são totais (não lançam exceção) sobre todas as entradas.
 */
object DashboardUiMapper {

    // --- KPIs (Req 1.5, 1.6) ---

    /**
     * Mapeia [DashboardStats] para os textos dos quatro cartões de KPI, usando a
     * representação inteira de cada campo correspondente.
     *
     * - `coletados`   ← `stats.totalColetados`
     * - `pendentes`   ← `stats.totalPendentes`
     * - `divergencias`← `stats.divergencias`
     * - `coletores`   ← `stats.coletoresAtivos`
     */
    fun kpiValues(stats: DashboardStats): KpiValues = KpiValues(
        coletados = stats.totalColetados.toString(),
        pendentes = stats.totalPendentes.toString(),
        divergencias = stats.divergencias.toString(),
        coletores = stats.coletoresAtivos.toString()
    )

    // --- Indicador de conclusão (Req 2.2, 2.3, 2.5) ---

    /**
     * Arredonda [value] ao inteiro mais próximo e o restringe ao intervalo fechado
     * `[0, 100]` (Req 2.2, 2.5).
     *
     * Casos defensivos:
     * - `null`, `NaN` ou valores `<= 0` → `0`
     * - valores `>= 100` → `100`
     * - valores em `(0, 100)` → arredondamento ao inteiro mais próximo
     */
    fun clampPercent(value: Double?): Int {
        if (value == null || value.isNaN() || value <= 0.0) return 0
        if (value >= 100.0) return 100
        return value.roundToInt().coerceIn(0, 100)
    }

    /**
     * Rótulo textual de conclusão no formato "X% concluído", onde `X` é
     * [clampPercent] de [value] — um inteiro em `[0, 100]`, sem casas decimais
     * (Req 2.3).
     */
    fun completionLabel(value: Double?): String = "${clampPercent(value)}% concluído"

    // --- Cabeçalho (Req 3.1, 3.2, 3.3, 3.4, 3.5, 3.6) ---

    /** Comprimento máximo do nome do inventário exibido no cabeçalho (Req 3.2). */
    private const val MAX_INVENTORY_NAME_LENGTH = 80

    /** Caractere de reticências usado ao truncar o nome do inventário (Req 3.2). */
    private const val ELLIPSIS = "…"

    /** Texto substituto genérico quando não há inventário identificável (Req 3.6). */
    private const val NO_ACTIVE_INVENTORY = "Nenhum inventário ativo"

    /** Padrão de formatação do timestamp da última sincronização, 24h (Req 3.3). */
    private const val LAST_SYNC_PATTERN = "dd/MM/yyyy HH:mm"

    /**
     * Saudação textual não vazia, com no máximo 60 caracteres (Req 3.1), variando
     * conforme a faixa de [hourOfDay] (0..23):
     *
     * - `0..5`   → madrugada
     * - `6..11`  → manhã
     * - `12..17` → tarde
     * - `18..23` → noite
     *
     * Por ser um helper puro (sem `Context`), retorna o texto diretamente. Horas fora
     * de `0..23` recaem em uma saudação neutra, mantendo a função total.
     */
    fun greeting(hourOfDay: Int): String = when (hourOfDay) {
        in 0..5 -> "Boa madrugada"
        in 6..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        in 18..23 -> "Boa noite"
        else -> "Olá"
    }

    /**
     * Resolve o nome do inventário a ser exibido no cabeçalho (Req 3.2, 3.4, 3.6):
     *
     * - [inventarioNome] não nulo e não vazio: retorna o nome; se exceder
     *   [MAX_INVENTORY_NAME_LENGTH] caracteres, trunca preservando o prefixo e
     *   acrescentando reticências ([ELLIPSIS]), de modo que o total tenha exatamente
     *   [MAX_INVENTORY_NAME_LENGTH] caracteres.
     * - [inventarioNome] nulo/vazio e [inventarioId] não nulo: retorna um substituto
     *   identificando o inventário pelo id ("Inventário <id>").
     * - ambos nulos/indisponíveis: retorna um texto substituto genérico
     *   ([NO_ACTIVE_INVENTORY]).
     *
     * O resultado é sempre não vazio.
     */
    fun resolveInventoryName(inventarioNome: String?, inventarioId: Int?): String {
        if (!inventarioNome.isNullOrBlank()) {
            return if (inventarioNome.length > MAX_INVENTORY_NAME_LENGTH) {
                inventarioNome.take(MAX_INVENTORY_NAME_LENGTH - ELLIPSIS.length) + ELLIPSIS
            } else {
                inventarioNome
            }
        }
        return if (inventarioId != null) "Inventário $inventarioId" else NO_ACTIVE_INVENTORY
    }

    /**
     * Formata o [timestamp] (ms UTC) da última sincronização no padrão
     * "dd/MM/yyyy HH:mm" (relógio de 24 horas), usando o fuso e o locale padrão do
     * dispositivo (Req 3.3). Retorna `null` quando [timestamp] é nulo (Req 3.5),
     * permitindo ao Fragment ocultar a área correspondente.
     */
    fun formatLastSync(timestamp: Long?): String? {
        if (timestamp == null) return null
        val formatter = SimpleDateFormat(LAST_SYNC_PATTERN, Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    // --- Estado e dados do gráfico (Req 4.2, 4.4, 4.5, 4.6) ---

    /**
     * Mapeia cada [EvolucaoColeta] da lista [evolucao] para um [ChartPoint],
     * preservando a ordem da lista de origem (Req 4.2):
     *
     * - `index`: índice sequencial a partir de `0`, na ordem da lista.
     * - `value`: `quantidade` do item, convertido para `Float` (eixo Y).
     * - `label`: campo `data` do item (eixo X temporal).
     *
     * Uma lista vazia produz uma lista vazia. Função total (não lança).
     */
    fun toChartPoints(evolucao: List<EvolucaoColeta>): List<ChartPoint> =
        evolucao.mapIndexed { index, item ->
            ChartPoint(
                index = index,
                value = item.quantidade.toFloat(),
                label = item.data
            )
        }

    // --- Estado global da tela (Req 6.3, 6.4, 6.5, 6.6) ---

    /**
     * Resolve o estado global, mutuamente exclusivo, da tela do Dashboard a partir
     * de [uiState] e [stats], seguindo prioridade fixa (Req 6.3–6.6):
     *
     * 1. `uiState.isLoading == true` → [DashboardScreenState.Loading]
     * 2. senão, `uiState.error != null` → [DashboardScreenState.Error]
     *    (os `stats` previamente carregados permanecem em memória)
     * 3. senão, inventário não identificável
     *    (`stats.inventarioNome == null && stats.inventarioId == null`) →
     *    [DashboardScreenState.Empty]
     * 4. senão → [DashboardScreenState.Content]
     *
     * A prioridade garante exclusividade mútua: `Content` é retornado se e somente se
     * não há loading, não há erro e há inventário identificável. Função total (não lança).
     */
    fun resolveDashboardState(
        uiState: DashboardUiStateClean,
        stats: DashboardStats
    ): DashboardScreenState = when {
        uiState.isLoading -> DashboardScreenState.Loading
        uiState.error != null -> DashboardScreenState.Error(uiState.error)
        stats.inventarioNome == null && stats.inventarioId == null -> DashboardScreenState.Empty
        else -> DashboardScreenState.Content(stats)
    }

    /**
     * Resolve o estado, mutuamente exclusivo, da área do gráfico de evolução a partir
     * de [uiState], seguindo prioridade fixa (Req 4.2, 4.4–4.6):
     *
     * 1. `uiState.isLoadingGrafico == true` → [ChartAreaState.Loading]
     * 2. senão, `uiState.graficoError != null` → [ChartAreaState.Error]
     * 3. senão, `uiState.coletasEvolucao.isEmpty()` → [ChartAreaState.Empty]
     * 4. senão → [ChartAreaState.Data] com os pontos mapeados por [toChartPoints]
     *
     * A prioridade garante exclusividade mútua: `Data` é retornado se e somente se
     * não há loading do gráfico, não há erro do gráfico e a lista de evolução é não
     * vazia. Função total (não lança).
     */
    fun resolveChartState(uiState: DashboardUiStateClean): ChartAreaState = when {
        uiState.isLoadingGrafico -> ChartAreaState.Loading
        uiState.graficoError != null -> ChartAreaState.Error(uiState.graficoError)
        uiState.coletasEvolucao.isEmpty() -> ChartAreaState.Empty
        else -> ChartAreaState.Data(toChartPoints(uiState.coletasEvolucao))
    }

    // --- Indicador offline (Req 8.2) ---

    /**
     * Indica se o [Indicador_Offline] deve ser exibido por causa dos dados,
     * derivando diretamente do flag [DashboardStats.isOfflineData] (Req 8.2).
     */
    fun shouldShowOfflineFromData(stats: DashboardStats): Boolean = stats.isOfflineData
}
