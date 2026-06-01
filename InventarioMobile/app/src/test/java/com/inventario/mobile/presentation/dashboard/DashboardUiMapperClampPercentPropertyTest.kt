package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import kotlin.math.roundToInt

/**
 * Property-Based Tests para [DashboardUiMapper.clampPercent].
 *
 * Feature: dashboard-redesign, Property 2: Percentual de conclusão é arredondado e restrito a [0,100]
 * Validates: Requirements 2.2, 2.5
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* valor `Double?` (incluindo `null`, `NaN`, negativos, maiores que 100 e
 *  valores fracionários), `DashboardUiMapper.clampPercent(value)` retorna um inteiro no
 *  intervalo fechado `[0,100]`; vale 0 para `null`/`NaN`/valores ≤ 0, vale 100 para
 *  valores ≥ 100 e, para valores em `(0,100)`, é igual ao arredondamento ao inteiro mais
 *  próximo restrito a esse intervalo.
 *
 * Estratégia de geradores: o gerador [percentInputs] cobre explicitamente as três
 * regiões relevantes — negativos, o intervalo `(0,100)` e valores `> 100` — além de
 * injetar valores especiais (`NaN`, `±Infinity`, `0.0`, `-0.0`, `100.0` e fracionários
 * de borda como `0.4`, `0.5`, `99.6`). O caso `null` é coberto por [@Example] separado,
 * pois o parâmetro `@ForAll Double` é primitivo e não nulo.
 */
class DashboardUiMapperClampPercentPropertyTest {

    /**
     * Gerador de entradas para `clampPercent`, cobrindo Double arbitrário nas três
     * regiões da especificação mais valores especiais de borda.
     */
    @Provide
    fun percentInputs(): Arbitrary<Double> {
        val negatives = Arbitraries.doubles().between(-1_000_000.0, 0.0)
        val mid = Arbitraries.doubles().between(0.0, 100.0)
        val above = Arbitraries.doubles().between(100.0, 1_000_000.0)
        val specials = Arbitraries.of(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            0.0,
            -0.0,
            100.0,
            0.4,
            0.5,
            99.6
        )
        return Arbitraries.oneOf(negatives, mid, above, specials)
    }

    // Feature: dashboard-redesign, Property 2: Percentual de conclusão é arredondado e restrito a [0,100]
    // Validates: Requirements 2.2, 2.5
    @Property(tries = 200)
    fun `clampPercent retorna inteiro em 0_100 conforme a especificacao`(
        @ForAll("percentInputs") value: Double
    ): Boolean {
        val result = DashboardUiMapper.clampPercent(value)

        // Invariante universal: o resultado está sempre no intervalo fechado [0,100].
        if (result !in 0..100) return false

        // Comportamento por região (Req 2.2, 2.5).
        return when {
            value.isNaN() || value <= 0.0 -> result == 0
            value >= 100.0 -> result == 100
            else -> result == value.roundToInt().coerceIn(0, 100)
        }
    }

    // Propriedade focada na região (0,100): equivale ao arredondamento ao inteiro
    // mais próximo, sempre dentro de [0,100].
    // Feature: dashboard-redesign, Property 2: Percentual de conclusão é arredondado e restrito a [0,100]
    // Validates: Requirements 2.2, 2.5
    @Property(tries = 200)
    fun `clampPercent em 0_100 aberto equivale ao arredondamento`(
        @ForAll("midRange") value: Double
    ): Boolean {
        val result = DashboardUiMapper.clampPercent(value)
        return result in 0..100 && result == value.roundToInt().coerceIn(0, 100)
    }

    @Provide
    fun midRange(): Arbitrary<Double> =
        // Região [0,100]. Nos extremos (0.0 e 100.0) a igualdade verificada também
        // vale, pois clampPercent(0.0)=0 e clampPercent(100.0)=100 coincidem com
        // value.roundToInt().coerceIn(0,100).
        Arbitraries.doubles().between(0.0, 100.0)

    // ---- Exemplos fixos (casos de borda) complementando os testes de propriedade ----

    @Example
    fun `clampPercent de null retorna 0`(): Boolean =
        DashboardUiMapper.clampPercent(null) == 0

    @Example
    fun `clampPercent de NaN retorna 0`(): Boolean =
        DashboardUiMapper.clampPercent(Double.NaN) == 0

    @Example
    fun `clampPercent de valor negativo retorna 0`(): Boolean =
        DashboardUiMapper.clampPercent(-5.0) == 0 &&
            DashboardUiMapper.clampPercent(-0.1) == 0 &&
            DashboardUiMapper.clampPercent(-1_000_000.0) == 0

    @Example
    fun `clampPercent de zero retorna 0`(): Boolean =
        DashboardUiMapper.clampPercent(0.0) == 0 &&
            DashboardUiMapper.clampPercent(-0.0) == 0

    @Example
    fun `clampPercent de cem retorna 100`(): Boolean =
        DashboardUiMapper.clampPercent(100.0) == 100

    @Example
    fun `clampPercent de valor maior que cem retorna 100`(): Boolean =
        DashboardUiMapper.clampPercent(100.1) == 100 &&
            DashboardUiMapper.clampPercent(150.0) == 100 &&
            DashboardUiMapper.clampPercent(1_000_000.0) == 100

    @Example
    fun `clampPercent de infinitos respeita o intervalo`(): Boolean =
        DashboardUiMapper.clampPercent(Double.POSITIVE_INFINITY) == 100 &&
            DashboardUiMapper.clampPercent(Double.NEGATIVE_INFINITY) == 0

    @Example
    fun `clampPercent de fracionarios arredonda ao inteiro mais proximo`(): Boolean =
        // 0.4 → 0 (arredonda para baixo); 0.5 → 1 (roundToInt arredonda para +infinito);
        // 99.6 → 100 (arredonda para cima, ainda dentro do intervalo).
        DashboardUiMapper.clampPercent(0.4) == 0 &&
            DashboardUiMapper.clampPercent(0.5) == 1 &&
            DashboardUiMapper.clampPercent(99.6) == 100 &&
            DashboardUiMapper.clampPercent(49.4) == 49 &&
            DashboardUiMapper.clampPercent(49.5) == 50
}
