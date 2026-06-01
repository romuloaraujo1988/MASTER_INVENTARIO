package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Combinators
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import net.jqwik.api.constraints.IntRange

/**
 * Property-Based Tests para [DashboardUiMapper.resolveDashboardState].
 *
 * Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
 * Validates: Requirements 6.3, 6.4, 6.5, 6.6
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* par `(DashboardUiStateClean, DashboardStats)`,
 *  `DashboardUiMapper.resolveDashboardState(uiState, stats)` retorna exatamente um
 *  dentre `Loading`, `Error`, `Empty` ou `Content`, seguindo a prioridade
 *  `Loading` (quando `isLoading`) > `Error` (quando `error != null`) >
 *  `Empty` (quando `inventarioNome` e `inventarioId` ambos nulos) > `Content`;
 *  em particular, `Content` é retornado se e somente se não há loading, não há erro
 *  e há inventário identificável.
 *
 * Geradores: cobrem explicitamente as combinações de `isLoading` (true/false),
 * `error` (null/non-null, incluindo string vazia e em branco como não nulas),
 * `inventarioNome` (null/non-null) e `inventarioId` (null/non-null). Os demais
 * campos de [DashboardStats] são gerados respeitando as invariantes de
 * não-negatividade impostas pelo `init {}` (totalPatrimonios/totalColetados/
 * totalPendentes >= 0).
 */
class DashboardUiMapperResolveDashboardStatePropertyTest {

    // ---------------------------------------------------------------------------------------
    // Geradores (smart generators que constrangem o espaço de entrada de forma inteligente)
    // ---------------------------------------------------------------------------------------

    /** Mensagens de erro opcionais: null (~40%) ou não nulas (incl. vazia/whitespace). */
    @Provide
    fun errosOpcionais(): Arbitrary<String?> =
        Arbitraries.of(
            "Falha de rede",
            "Timeout",
            "Erro 500",
            "",
            " ",
            "   "
        ).injectNull(0.4)

    /** Nomes de inventário opcionais: null (~40%) ou não nulos (incl. vazia/whitespace). */
    @Provide
    fun nomesOpcionais(): Arbitrary<String?> =
        Arbitraries.of(
            "Inventário 2025",
            "Câmpus Central",
            "",
            " ",
            "Levantamento Geral"
        ).injectNull(0.4)

    /** Ids de inventário opcionais: null (~40%) ou arbitrários (inclui zero/negativos). */
    @Provide
    fun idsOpcionais(): Arbitrary<Int?> =
        Arbitraries.integers().injectNull(0.4)

    /**
     * Pares (uiState, stats) cobrindo todas as combinações de `isLoading`, `error`,
     * `inventarioNome` e `inventarioId`. Os campos numéricos restritos por `init {}`
     * são gerados com mínimo 0.
     */
    @Provide
    fun cenarios(): Arbitrary<Pair<DashboardUiStateClean, DashboardStats>> {
        val isLoadingArb: Arbitrary<Boolean> = Arbitraries.of(true, false)
        val errorArb: Arbitrary<String?> = errosOpcionais()
        val nomeArb: Arbitrary<String?> = nomesOpcionais()
        val idArb: Arbitrary<Int?> = idsOpcionais()
        val coletadosArb: Arbitrary<Int> = Arbitraries.integers().between(0, 10_000)
        val pendentesArb: Arbitrary<Int> = Arbitraries.integers().between(0, 10_000)
        val percentualArb: Arbitrary<Double> = Arbitraries.doubles().between(-50.0, 150.0)

        return Combinators.combine(
            isLoadingArb,
            errorArb,
            nomeArb,
            idArb,
            coletadosArb,
            pendentesArb
        ).`as` { isLoading, error, nome, id, coletados, pendentes ->
            val stats = DashboardStats(
                totalPatrimonios = coletados + pendentes,
                totalColetados = coletados,
                totalPendentes = pendentes,
                percentualConclusao = 0.0,
                inventarioId = id,
                inventarioNome = nome
            )
            val uiState = DashboardUiStateClean(
                isLoading = isLoading,
                error = error,
                dashboardStats = stats
            )
            uiState to stats
        }
    }

    // ---------------------------------------------------------------------------------------
    // Propriedades
    // ---------------------------------------------------------------------------------------

    // Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
    // Validates: Requirements 6.3, 6.4, 6.5, 6.6
    @Property(tries = 300)
    fun `resolveDashboardState retorna exatamente um estado seguindo a prioridade fixa`(
        @ForAll("cenarios") cenario: Pair<DashboardUiStateClean, DashboardStats>
    ): Boolean {
        val (uiState, stats) = cenario
        val resultado = DashboardUiMapper.resolveDashboardState(uiState, stats)

        // Estado esperado segundo a prioridade Loading > Error > Empty > Content.
        val esperado: DashboardScreenState = when {
            uiState.isLoading -> DashboardScreenState.Loading
            uiState.error != null -> DashboardScreenState.Error(uiState.error!!)
            stats.inventarioNome == null && stats.inventarioId == null -> DashboardScreenState.Empty
            else -> DashboardScreenState.Content(stats)
        }

        return resultado == esperado
    }

    // Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
    // Validates: Requirements 6.3
    @Property(tries = 200)
    fun `isLoading true sempre resulta em Loading independentemente dos demais campos`(
        @ForAll("errosOpcionais") error: String?,
        @ForAll("nomesOpcionais") nome: String?,
        @ForAll("idsOpcionais") id: Int?
    ): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = 5,
            totalColetados = 2,
            totalPendentes = 3,
            percentualConclusao = 40.0,
            inventarioId = id,
            inventarioNome = nome
        )
        val uiState = DashboardUiStateClean(isLoading = true, error = error, dashboardStats = stats)

        return DashboardUiMapper.resolveDashboardState(uiState, stats) === DashboardScreenState.Loading
    }

    // Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
    // Validates: Requirements 6.5
    @Property(tries = 200)
    fun `sem loading e com erro resulta em Error com a mensagem original`(
        @ForAll @IntRange(min = 0, max = 10_000) coletados: Int,
        @ForAll @IntRange(min = 0, max = 10_000) pendentes: Int,
        @ForAll("nomesOpcionais") nome: String?,
        @ForAll("idsOpcionais") id: Int?,
        @ForAll mensagem: String
    ): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = coletados + pendentes,
            totalColetados = coletados,
            totalPendentes = pendentes,
            percentualConclusao = 0.0,
            inventarioId = id,
            inventarioNome = nome
        )
        val uiState = DashboardUiStateClean(isLoading = false, error = mensagem, dashboardStats = stats)

        val resultado = DashboardUiMapper.resolveDashboardState(uiState, stats)
        return resultado is DashboardScreenState.Error && resultado.message == mensagem
    }

    // Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
    // Validates: Requirements 6.4
    @Property(tries = 200)
    fun `sem loading sem erro e sem inventario identificavel resulta em Empty`(
        @ForAll @IntRange(min = 0, max = 10_000) coletados: Int,
        @ForAll @IntRange(min = 0, max = 10_000) pendentes: Int
    ): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = coletados + pendentes,
            totalColetados = coletados,
            totalPendentes = pendentes,
            percentualConclusao = 0.0,
            inventarioId = null,
            inventarioNome = null
        )
        val uiState = DashboardUiStateClean(isLoading = false, error = null, dashboardStats = stats)

        return DashboardUiMapper.resolveDashboardState(uiState, stats) === DashboardScreenState.Empty
    }

    // Feature: dashboard-redesign, Property 8: Estado global da tela é mutuamente exclusivo e priorizado
    // Validates: Requirements 6.6
    @Property(tries = 300)
    fun `Content e retornado se e somente se nao ha loading nem erro e ha inventario identificavel`(
        @ForAll("cenarios") cenario: Pair<DashboardUiStateClean, DashboardStats>
    ): Boolean {
        val (uiState, stats) = cenario
        val resultado = DashboardUiMapper.resolveDashboardState(uiState, stats)

        val condicaoContent = !uiState.isLoading &&
            uiState.error == null &&
            !(stats.inventarioNome == null && stats.inventarioId == null)

        // Bicondicional: resultado é Content <=> condicaoContent. E, sendo Content,
        // os stats carregados são preservados na payload.
        val ehContent = resultado is DashboardScreenState.Content
        val payloadOk = (resultado as? DashboardScreenState.Content)?.stats == stats || !ehContent

        return (ehContent == condicaoContent) && payloadOk
    }

    // ---------------------------------------------------------------------------------------
    // Exemplos fixos (casos de borda) complementando os testes de propriedade
    // ---------------------------------------------------------------------------------------

    private fun stats(nome: String?, id: Int?): DashboardStats = DashboardStats(
        totalPatrimonios = 10,
        totalColetados = 6,
        totalPendentes = 4,
        percentualConclusao = 60.0,
        inventarioId = id,
        inventarioNome = nome
    )

    @Example
    fun `loading tem prioridade sobre erro e inventario`(): Boolean {
        val s = stats("Inv", 1)
        val uiState = DashboardUiStateClean(isLoading = true, error = "boom", dashboardStats = s)
        return DashboardUiMapper.resolveDashboardState(uiState, s) === DashboardScreenState.Loading
    }

    @Example
    fun `erro tem prioridade sobre empty quando sem loading`(): Boolean {
        val s = stats(null, null)
        val uiState = DashboardUiStateClean(isLoading = false, error = "falha", dashboardStats = s)
        val r = DashboardUiMapper.resolveDashboardState(uiState, s)
        return r is DashboardScreenState.Error && r.message == "falha"
    }

    @Example
    fun `erro string vazia ainda e tratado como Error (nao nulo)`(): Boolean {
        val s = stats("Inv", 2)
        val uiState = DashboardUiStateClean(isLoading = false, error = "", dashboardStats = s)
        val r = DashboardUiMapper.resolveDashboardState(uiState, s)
        return r is DashboardScreenState.Error && r.message == ""
    }

    @Example
    fun `apenas id presente resulta em Content`(): Boolean {
        val s = stats(null, 7)
        val uiState = DashboardUiStateClean(isLoading = false, error = null, dashboardStats = s)
        val r = DashboardUiMapper.resolveDashboardState(uiState, s)
        return r is DashboardScreenState.Content && r.stats == s
    }

    @Example
    fun `apenas nome presente resulta em Content`(): Boolean {
        val s = stats("Inventário X", null)
        val uiState = DashboardUiStateClean(isLoading = false, error = null, dashboardStats = s)
        val r = DashboardUiMapper.resolveDashboardState(uiState, s)
        return r is DashboardScreenState.Content && r.stats == s
    }

    @Example
    fun `nome e id nulos sem loading nem erro resulta em Empty`(): Boolean {
        val s = stats(null, null)
        val uiState = DashboardUiStateClean(isLoading = false, error = null, dashboardStats = s)
        return DashboardUiMapper.resolveDashboardState(uiState, s) === DashboardScreenState.Empty
    }
}
