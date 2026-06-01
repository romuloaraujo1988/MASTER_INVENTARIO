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
 * Property-Based Tests para [DashboardUiMapper.toChartPoints].
 *
 * Feature: dashboard-redesign, Property 7: Mapeamento de pontos do gráfico preserva ordem, quantidade e rótulo
 * Validates: Requirements 4.2
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* `List<EvolucaoColeta>`, `DashboardUiMapper.toChartPoints(lista)` produz
 *  exatamente um ponto por item, na mesma ordem da lista, com `value` igual a
 *  `quantidade` do item e `label` igual ao campo `data` do item (e índices sequenciais
 *  a partir de 0).
 *
 * Geradores: cobrem explicitamente listas vazias e não vazias de [EvolucaoColeta],
 * com `data` (rótulo) em formatos variados (ISO, vazio, unicode) e `quantidade`
 * arbitrária (incluindo zero, negativos e valores grandes).
 */
class DashboardUiMapperToChartPointsPropertyTest {

    // ---------------------------------------------------------------------------------------
    // Geradores (smart generators que constrangem o espaço de entrada de forma inteligente)
    // ---------------------------------------------------------------------------------------

    /**
     * Rótulos `data` variados: datas ISO plausíveis, strings curtas, vazia e unicode.
     */
    @Provide
    fun datas(): Arbitrary<String> =
        Arbitraries.oneOf(
            Arbitraries.of(
                "2025-11-15", "2025-01-01", "2024-12-31", "2025-06-30",
                "", "15/11", "data-x", "Câmpus 你好"
            ),
            Arbitraries.strings().ofMinLength(0).ofMaxLength(12)
        )

    /** Um [EvolucaoColeta] com `data` e `quantidade` (inclui zero/negativos/grandes). */
    @Provide
    fun evolucaoColeta(): Arbitrary<EvolucaoColeta> =
        Combinators.combine(datas(), Arbitraries.integers())
            .`as` { data, quantidade -> EvolucaoColeta(data = data, quantidade = quantidade) }

    /** Listas (vazias e não vazias) de [EvolucaoColeta]. */
    @Provide
    fun listasEvolucao(): Arbitrary<List<EvolucaoColeta>> =
        evolucaoColeta().list().ofMinSize(0).ofMaxSize(50)

    /** Listas estritamente não vazias de [EvolucaoColeta]. */
    @Provide
    fun listasNaoVazias(): Arbitrary<List<EvolucaoColeta>> =
        evolucaoColeta().list().ofMinSize(1).ofMaxSize(50)

    // ---------------------------------------------------------------------------------------
    // Propriedades
    // ---------------------------------------------------------------------------------------

    // Feature: dashboard-redesign, Property 7: Mapeamento de pontos do gráfico preserva ordem, quantidade e rótulo
    // Validates: Requirements 4.2
    @Property(tries = 200)
    fun `toChartPoints preserva tamanho ordem quantidade e rotulo`(
        @ForAll("listasEvolucao") evolucao: List<EvolucaoColeta>
    ): Boolean {
        val points = DashboardUiMapper.toChartPoints(evolucao)

        // Um ponto por item, mesma quantidade.
        if (points.size != evolucao.size) return false

        // Para cada i: index sequencial, value = quantidade, label = data.
        return evolucao.indices.all { i ->
            val point = points[i]
            val item = evolucao[i]
            point.index == i &&
                point.value == item.quantidade.toFloat() &&
                point.label == item.data
        }
    }

    // Feature: dashboard-redesign, Property 7: Mapeamento de pontos do gráfico preserva ordem, quantidade e rótulo
    // Validates: Requirements 4.2
    @Property(tries = 200)
    fun `toChartPoints gera indices sequenciais comecando em zero`(
        @ForAll("listasNaoVazias") evolucao: List<EvolucaoColeta>
    ): Boolean {
        val points = DashboardUiMapper.toChartPoints(evolucao)
        return points.map { it.index } == evolucao.indices.toList()
    }

    // Feature: dashboard-redesign, Property 7: Mapeamento de pontos do gráfico preserva ordem, quantidade e rótulo
    // Validates: Requirements 4.2
    @Property(tries = 200)
    fun `toChartPoints preserva a ordem dos rotulos data`(
        @ForAll("listasEvolucao") evolucao: List<EvolucaoColeta>
    ): Boolean {
        val points = DashboardUiMapper.toChartPoints(evolucao)
        return points.map { it.label } == evolucao.map { it.data }
    }

    // ---------------------------------------------------------------------------------------
    // Exemplos fixos (casos de borda) complementando os testes de propriedade
    // ---------------------------------------------------------------------------------------

    @Example
    fun `lista vazia produz lista vazia`(): Boolean {
        return DashboardUiMapper.toChartPoints(emptyList()).isEmpty()
    }

    @Example
    fun `lista de um item mapeia para um unico ponto`(): Boolean {
        val item = EvolucaoColeta(data = "2025-11-15", quantidade = 42)
        val points = DashboardUiMapper.toChartPoints(listOf(item))
        return points.size == 1 &&
            points[0].index == 0 &&
            points[0].value == 42f &&
            points[0].label == "2025-11-15"
    }

    @Example
    fun `lista com varios itens preserva ordem e indices`(): Boolean {
        val itens = listOf(
            EvolucaoColeta(data = "2025-11-15", quantidade = 10),
            EvolucaoColeta(data = "2025-11-16", quantidade = 0),
            EvolucaoColeta(data = "2025-11-17", quantidade = -3),
            EvolucaoColeta(data = "2025-11-18", quantidade = 7)
        )
        val points = DashboardUiMapper.toChartPoints(itens)
        return points.size == 4 &&
            points[0].index == 0 && points[0].value == 10f && points[0].label == "2025-11-15" &&
            points[1].index == 1 && points[1].value == 0f && points[1].label == "2025-11-16" &&
            points[2].index == 2 && points[2].value == -3f && points[2].label == "2025-11-17" &&
            points[3].index == 3 && points[3].value == 7f && points[3].label == "2025-11-18"
    }

    @Example
    fun `quantidades extremas sao convertidas para float`(): Boolean {
        val itens = listOf(
            EvolucaoColeta(data = "min", quantidade = Int.MIN_VALUE),
            EvolucaoColeta(data = "max", quantidade = Int.MAX_VALUE)
        )
        val points = DashboardUiMapper.toChartPoints(itens)
        return points[0].value == Int.MIN_VALUE.toFloat() &&
            points[1].value == Int.MAX_VALUE.toFloat()
    }
}
