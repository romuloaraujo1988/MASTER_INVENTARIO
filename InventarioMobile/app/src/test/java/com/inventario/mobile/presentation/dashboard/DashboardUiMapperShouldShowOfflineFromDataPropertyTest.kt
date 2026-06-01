package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.domain.model.DashboardStats
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange

/**
 * Property-Based Tests para [DashboardUiMapper.shouldShowOfflineFromData].
 *
 * Feature: dashboard-redesign, Property 10: Indicador offline deriva do flag dos dados
 * Validates: Requirements 8.2
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* [DashboardStats], `DashboardUiMapper.shouldShowOfflineFromData(stats)`
 *  é igual a `stats.isOfflineData`.
 *
 * Esta propriedade também verifica que os demais campos de [DashboardStats]
 * (contagens, percentual, nome/id do inventário, timestamp etc.) não afetam o
 * resultado: para uma mesma combinação de campos, o retorno acompanha exclusivamente
 * o valor de `isOfflineData`, tanto quando `true` quanto quando `false`.
 *
 * Nota sobre os geradores: o `init {}` de [DashboardStats] impõe não-negatividade de
 * `totalPatrimonios`, `totalColetados` e `totalPendentes` (a construção lança para
 * valores negativos). Por isso esses três campos são gerados com `@IntRange(min = 0)`.
 * `isOfflineData` é gerado como `Boolean` arbitrário (jqwik cobre `true` e `false`).
 */
class DashboardUiMapperShouldShowOfflineFromDataPropertyTest {

    // Feature: dashboard-redesign, Property 10: Indicador offline deriva do flag dos dados
    // Validates: Requirements 8.2
    @Property(tries = 200)
    fun `shouldShowOfflineFromData e igual ao flag isOfflineData de DashboardStats`(
        @ForAll isOfflineData: Boolean,
        @ForAll @IntRange(min = 0) totalPatrimonios: Int,
        @ForAll @IntRange(min = 0) totalColetados: Int,
        @ForAll @IntRange(min = 0) totalPendentes: Int,
        @ForAll percentualConclusao: Double,
        @ForAll coletoresAtivos: Int,
        @ForAll divergencias: Int,
        @ForAll inventarioId: Int?,
        @ForAll inventarioNome: String?,
        @ForAll timestampUltimaSincronizacao: Long?
    ): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = totalPatrimonios,
            totalColetados = totalColetados,
            totalPendentes = totalPendentes,
            percentualConclusao = percentualConclusao,
            coletoresAtivos = coletoresAtivos,
            divergencias = divergencias,
            inventarioId = inventarioId,
            inventarioNome = inventarioNome,
            isOfflineData = isOfflineData,
            timestampUltimaSincronizacao = timestampUltimaSincronizacao
        )

        return DashboardUiMapper.shouldShowOfflineFromData(stats) == stats.isOfflineData &&
            DashboardUiMapper.shouldShowOfflineFromData(stats) == isOfflineData
    }

    /**
     * Reforça que, fixando os demais campos, o resultado acompanha apenas o flag:
     * para a mesma combinação de campos, `true`/`false` em `isOfflineData` produz
     * exatamente o mesmo valor no retorno (os outros campos não interferem).
     */
    // Feature: dashboard-redesign, Property 10: Indicador offline deriva do flag dos dados
    // Validates: Requirements 8.2
    @Property(tries = 200)
    fun `outros campos nao afetam o resultado para o mesmo flag`(
        // Faixas limitadas para que `totalColetados + totalPendentes` não estoure
        // o `Int` (o que violaria a invariante `totalPatrimonios >= 0` de DashboardStats).
        @ForAll isOfflineData: Boolean,
        @ForAll @IntRange(min = 0, max = 1_000_000) totalColetados: Int,
        @ForAll @IntRange(min = 0, max = 1_000_000) totalPendentes: Int,
        @ForAll percentualConclusao: Double,
        @ForAll inventarioId: Int?,
        @ForAll inventarioNome: String?
    ): Boolean {
        val base = DashboardStats(
            totalPatrimonios = totalColetados + totalPendentes,
            totalColetados = totalColetados,
            totalPendentes = totalPendentes,
            percentualConclusao = percentualConclusao,
            inventarioId = inventarioId,
            inventarioNome = inventarioNome,
            isOfflineData = isOfflineData
        )
        // Variando todos os outros campos, mantendo o mesmo flag offline.
        val variante = base.copy(
            coletoresAtivos = base.coletoresAtivos + 7,
            divergencias = base.divergencias + 3,
            valorTotal = base.valorTotal + 1234.5,
            coletasHoje = base.coletasHoje + 9,
            timestampUltimaSincronizacao = 1_700_000_000_000L
        )

        return DashboardUiMapper.shouldShowOfflineFromData(base) == isOfflineData &&
            DashboardUiMapper.shouldShowOfflineFromData(variante) == isOfflineData
    }

    // ---- Exemplos fixos (casos de borda) complementando o teste de propriedade ----

    @Example
    fun `shouldShowOfflineFromData retorna true quando isOfflineData true`(): Boolean {
        val stats = DashboardStats.empty(isOfflineData = true)
        return DashboardUiMapper.shouldShowOfflineFromData(stats)
    }

    @Example
    fun `shouldShowOfflineFromData retorna false quando isOfflineData false`(): Boolean {
        val stats = DashboardStats.empty(isOfflineData = false)
        return !DashboardUiMapper.shouldShowOfflineFromData(stats)
    }

    @Example
    fun `flag offline true prevalece independentemente dos demais campos`(): Boolean {
        val stats = DashboardStats(
            totalPatrimonios = 100,
            totalColetados = 80,
            totalPendentes = 20,
            percentualConclusao = 80.0,
            coletoresAtivos = 5,
            divergencias = 2,
            inventarioId = 42,
            inventarioNome = "Inventário 2025",
            isOfflineData = true,
            timestampUltimaSincronizacao = 1_700_000_000_000L
        )
        return DashboardUiMapper.shouldShowOfflineFromData(stats)
    }
}
