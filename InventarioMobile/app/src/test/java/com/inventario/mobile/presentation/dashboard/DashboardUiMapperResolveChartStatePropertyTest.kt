package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.EvolucaoColeta
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Combinators
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide

/**
 * Property-Based Tests para [DashboardUiMapper.resolveChartState].
 *
 * Feature: dashboard-redesign, Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado
 * Validates: Requirements 4.2, 4.4, 4.5, 4.6
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* `DashboardUiStateClean`, `DashboardUiMapper.resolveChartState(uiState)`
 *  retorna exatamente um dentre `Loading`, `Error`, `Empty` ou `Data`, seguindo a
 *  prioridade `Loading` (quando `isLoadingGrafico`) > `Error` (quando
 *  `graficoError != null`) > `Empty` (quando `coletasEvolucao` vazia) > `Data`; em
 *  particular, `Data` é retornado se e somente se não há loading do gráfico, não há
 *  erro do gráfico e a lista de evolução é não vazia. Quando `Data`, os pontos são
 *  exatamente `toChartPoints(coletasEvolucao)`.
 *
 * Estratégia de geradores: os três parâmetros `@ForAll` cobrem todas as combinações de
 * `isLoadingGrafico` (true/false), `graficoError` (null/não-null) e `coletasEvolucao`
 * (vazia/não-vazia), garantindo que cada ramo de prioridade seja exercido. Campos
 * irrelevantes para `resolveChartState` (`isLoading`, `error`, `dashboardStats`)
 * permanecem nos defaults, pois não influenciam o resultado.
 */
class DashboardUiMapperResolveChartStatePropertyTest {

    // ---------------------------------------------------------------------------------------
    // Geradores (smart generators que constrangem o espaço de entrada de forma inteligente)
    // ---------------------------------------------------------------------------------------

    /** `isLoadingGrafico`: ambos os valores são relevantes para a prioridade. */
    @Provide
    fun loadingFlags(): Arbitrary<Boolean> = Arbitraries.of(true, false)

    /** Mensagens de erro do gráfico opcionais; ~40% nulas, incluindo string vazia. */
    @Provide
    fun graficoErrors(): Arbitrary<String?> =
        Arbitraries.oneOf(
            Arbitraries.of("", " ", "Falha ao carregar evolução", "timeout", "HTTP 500"),
            Arbitraries.strings().ofMinLength(1).ofMaxLength(40)
        ).injectNull(0.4)

    /** Item arbitrário de evolução (data + quantidade e coletores não negativos). */
    private fun evolucaoItem(): Arbitrary<EvolucaoColeta> =
        Combinators.combine(
            Arbitraries.strings().ofMinLength(0).ofMaxLength(12),
            Arbitraries.integers().between(0, 10_000),
            Arbitraries.integers().between(0, 500)
        ).`as` { data, quantidade, coletores ->
            EvolucaoColeta(data = data, quantidade = quantidade, coletoresAtivos = coletores)
        }

    /** Listas de evolução cobrindo o caso vazio e não vazio (até 30 itens). */
    @Provide
    fun evolucaoLists(): Arbitrary<List<EvolucaoColeta>> =
        evolucaoItem().list().ofMinSize(0).ofMaxSize(30)

    // ---------------------------------------------------------------------------------------
    // Propriedades
    // ---------------------------------------------------------------------------------------

    // Feature: dashboard-redesign, Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado
    // Validates: Requirements 4.2, 4.4, 4.5, 4.6
    @Property(tries = 300)
    fun `resolveChartState segue a prioridade Loading maior que Error maior que Empty maior que Data`(
        @ForAll("loadingFlags") isLoadingGrafico: Boolean,
        @ForAll("graficoErrors") graficoError: String?,
        @ForAll("evolucaoLists") coletasEvolucao: List<EvolucaoColeta>
    ): Boolean {
        val uiState = DashboardUiStateClean(
            isLoadingGrafico = isLoadingGrafico,
            graficoError = graficoError,
            coletasEvolucao = coletasEvolucao
        )
        val state = DashboardUiMapper.resolveChartState(uiState)

        return when {
            // Req 4.4: loading do gráfico tem prioridade máxima.
            isLoadingGrafico -> state is ChartAreaState.Loading
            // Req 4.6: erro do gráfico vem em seguida.
            graficoError != null -> state is ChartAreaState.Error && state.message == graficoError
            // Req 4.5: lista vazia (sem loading/erro) → estado vazio.
            coletasEvolucao.isEmpty() -> state is ChartAreaState.Empty
            // Req 4.2: caso default → dados mapeados.
            else -> state is ChartAreaState.Data
        }
    }

    // Exclusividade mútua: o resultado é exatamente uma das quatro variantes.
    // Feature: dashboard-redesign, Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado
    // Validates: Requirements 4.2, 4.4, 4.5, 4.6
    @Property(tries = 300)
    fun `resolveChartState retorna exatamente um estado`(
        @ForAll("loadingFlags") isLoadingGrafico: Boolean,
        @ForAll("graficoErrors") graficoError: String?,
        @ForAll("evolucaoLists") coletasEvolucao: List<EvolucaoColeta>
    ): Boolean {
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = isLoadingGrafico,
                graficoError = graficoError,
                coletasEvolucao = coletasEvolucao
            )
        )
        val matches = listOf(
            state is ChartAreaState.Loading,
            state is ChartAreaState.Empty,
            state is ChartAreaState.Error,
            state is ChartAreaState.Data
        ).count { it }
        return matches == 1
    }

    // Data é retornado se e somente se não há loading, não há erro e a lista é não vazia.
    // Feature: dashboard-redesign, Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado
    // Validates: Requirements 4.2
    @Property(tries = 300)
    fun `Data ocorre se e somente se sem loading sem erro e lista nao vazia`(
        @ForAll("loadingFlags") isLoadingGrafico: Boolean,
        @ForAll("graficoErrors") graficoError: String?,
        @ForAll("evolucaoLists") coletasEvolucao: List<EvolucaoColeta>
    ): Boolean {
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = isLoadingGrafico,
                graficoError = graficoError,
                coletasEvolucao = coletasEvolucao
            )
        )
        val deveriaSerData = !isLoadingGrafico && graficoError == null && coletasEvolucao.isNotEmpty()
        return (state is ChartAreaState.Data) == deveriaSerData
    }

    // Quando Data, os pontos carregados são exatamente toChartPoints(coletasEvolucao).
    // Feature: dashboard-redesign, Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado
    // Validates: Requirements 4.2
    @Property(tries = 300)
    fun `quando Data os pontos sao toChartPoints da lista de evolucao`(
        @ForAll("evolucaoLists") evolucao: List<EvolucaoColeta>
    ): Boolean {
        // Sem loading e sem erro: garante o ramo Data quando a lista é não vazia.
        val uiState = DashboardUiStateClean(
            isLoadingGrafico = false,
            graficoError = null,
            coletasEvolucao = evolucao
        )
        val state = DashboardUiMapper.resolveChartState(uiState)
        return if (evolucao.isEmpty()) {
            state is ChartAreaState.Empty
        } else {
            state is ChartAreaState.Data &&
                state.points == DashboardUiMapper.toChartPoints(evolucao)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Exemplos fixos (casos de borda) complementando os testes de propriedade
    // ---------------------------------------------------------------------------------------

    private val sampleEvolucao = listOf(
        EvolucaoColeta(data = "2025-11-15", quantidade = 3),
        EvolucaoColeta(data = "2025-11-16", quantidade = 7)
    )

    @Example
    fun `loading tem prioridade mesmo com erro e lista presentes`(): Boolean {
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = true,
                graficoError = "erro",
                coletasEvolucao = sampleEvolucao
            )
        )
        return state is ChartAreaState.Loading
    }

    @Example
    fun `erro tem prioridade sobre empty e data quando nao ha loading`(): Boolean {
        val comLista = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = false,
                graficoError = "Falha",
                coletasEvolucao = sampleEvolucao
            )
        )
        val semLista = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = false,
                graficoError = "Falha",
                coletasEvolucao = emptyList()
            )
        )
        return comLista is ChartAreaState.Error && comLista.message == "Falha" &&
            semLista is ChartAreaState.Error && semLista.message == "Falha"
    }

    @Example
    fun `lista vazia sem loading e sem erro produz Empty`(): Boolean {
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = false,
                graficoError = null,
                coletasEvolucao = emptyList()
            )
        )
        return state is ChartAreaState.Empty
    }

    @Example
    fun `lista nao vazia sem loading e sem erro produz Data com pontos mapeados`(): Boolean {
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = false,
                graficoError = null,
                coletasEvolucao = sampleEvolucao
            )
        )
        return state is ChartAreaState.Data &&
            state.points == DashboardUiMapper.toChartPoints(sampleEvolucao)
    }

    @Example
    fun `graficoError string vazia ainda e tratado como erro (nao nulo)`(): Boolean {
        // graficoError == "" é não nulo: a prioridade do design usa `!= null`.
        val state = DashboardUiMapper.resolveChartState(
            DashboardUiStateClean(
                isLoadingGrafico = false,
                graficoError = "",
                coletasEvolucao = sampleEvolucao
            )
        )
        return state is ChartAreaState.Error && state.message == ""
    }

    @Example
    fun `estado default vazio produz Empty`(): Boolean {
        // DashboardUiStateClean() => isLoadingGrafico=false, graficoError=null, lista vazia.
        val state = DashboardUiMapper.resolveChartState(DashboardUiStateClean())
        return state is ChartAreaState.Empty
    }
}
