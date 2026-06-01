package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange

/**
 * Property-Based Tests para [DashboardUiMapper.kpiValues].
 *
 * Feature: dashboard-redesign, Property 1: Mapeamento de KPIs reflete os campos corretos
 * Validates: Requirements 1.5, 1.6
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* [DashboardStats], `DashboardUiMapper.kpiValues(stats)` produz os textos
 *  dos quatro cartões iguais às representações inteiras dos campos correspondentes:
 *  `coletados == stats.totalColetados.toString()`,
 *  `pendentes == stats.totalPendentes.toString()`,
 *  `divergencias == stats.divergencias.toString()`,
 *  `coletores == stats.coletoresAtivos.toString()`.
 *
 * Nota sobre os geradores: o `init {}` de [DashboardStats] impõe não-negatividade de
 * `totalPatrimonios`, `totalColetados` e `totalPendentes` (a construção lança para
 * valores negativos). Por isso esses três campos são gerados com `@IntRange(min = 0)`.
 * Os campos `divergencias` e `coletoresAtivos` não têm essa restrição, então são
 * gerados como inteiros arbitrários — incluindo negativos, zero e valores grandes
 * (o gerador padrão de `Int` do jqwik cobre explicitamente esses casos de borda).
 */
class DashboardUiMapperKpiValuesPropertyTest {

    // Feature: dashboard-redesign, Property 1: Mapeamento de KPIs reflete os campos corretos
    // Validates: Requirements 1.5, 1.6
    @Property(tries = 200)
    fun `kpiValues reflete as representacoes inteiras dos campos de DashboardStats`(
        @ForAll @IntRange(min = 0) totalPatrimonios: Int,
        @ForAll @IntRange(min = 0) totalColetados: Int,
        @ForAll @IntRange(min = 0) totalPendentes: Int,
        @ForAll divergencias: Int,
        @ForAll coletoresAtivos: Int,
        @ForAll percentualConclusao: Double
    ): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = totalPatrimonios,
            totalColetados = totalColetados,
            totalPendentes = totalPendentes,
            percentualConclusao = percentualConclusao,
            coletoresAtivos = coletoresAtivos,
            divergencias = divergencias
        )

        val kpis = DashboardUiMapper.kpiValues(stats)

        return kpis.coletados == totalColetados.toString() &&
            kpis.pendentes == totalPendentes.toString() &&
            kpis.divergencias == divergencias.toString() &&
            kpis.coletores == coletoresAtivos.toString()
    }

    // ---- Exemplos fixos (casos de borda) complementando o teste de propriedade ----

    @Example
    fun `kpiValues com zeros mapeia para o texto zero`(): Boolean {
        val kpis = DashboardUiMapper.kpiValues(
            DashboardStats(
                totalPatrimonios = 0,
                totalColetados = 0,
                totalPendentes = 0,
                percentualConclusao = 0.0,
                coletoresAtivos = 0,
                divergencias = 0
            )
        )
        return kpis.coletados == "0" &&
            kpis.pendentes == "0" &&
            kpis.divergencias == "0" &&
            kpis.coletores == "0"
    }

    @Example
    fun `kpiValues preserva divergencias e coletores negativos`(): Boolean {
        val kpis = DashboardUiMapper.kpiValues(
            DashboardStats(
                totalPatrimonios = 10,
                totalColetados = 7,
                totalPendentes = 3,
                percentualConclusao = 70.0,
                coletoresAtivos = -2,
                divergencias = -5
            )
        )
        return kpis.coletados == "7" &&
            kpis.pendentes == "3" &&
            kpis.divergencias == "-5" &&
            kpis.coletores == "-2"
    }

    @Example
    fun `kpiValues lida com valores inteiros grandes`(): Boolean {
        val kpis = DashboardUiMapper.kpiValues(
            DashboardStats(
                totalPatrimonios = Int.MAX_VALUE,
                totalColetados = Int.MAX_VALUE,
                totalPendentes = Int.MAX_VALUE,
                percentualConclusao = 100.0,
                coletoresAtivos = Int.MAX_VALUE,
                divergencias = Int.MIN_VALUE
            )
        )
        return kpis.coletados == Int.MAX_VALUE.toString() &&
            kpis.pendentes == Int.MAX_VALUE.toString() &&
            kpis.divergencias == Int.MIN_VALUE.toString() &&
            kpis.coletores == Int.MAX_VALUE.toString()
    }
}
