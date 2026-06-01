package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property

/**
 * Property-Based Tests para [DashboardUiMapper.greeting].
 *
 * Feature: dashboard-redesign, Property 4: Saudação é não vazia e respeita o limite de comprimento
 * Validates: Requirements 3.1
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* hora do dia no intervalo `0..23`, `DashboardUiMapper.greeting(hour)`
 *  retorna um texto não vazio com no máximo 60 caracteres.
 *
 * Nota sobre os geradores: embora a propriedade do design seja enunciada para
 * `0..23`, `greeting` é uma função total (Req 9.1: lógica de apresentação robusta).
 * Portanto o gerador usa `Int` arbitrário — incluindo negativos, o intervalo válido
 * `0..23` e valores muito grandes (o gerador padrão de `Int` do jqwik cobre esses
 * casos de borda) — e a invariante "não vazio E ≤ 60 caracteres" deve valer para
 * toda entrada, garantindo que horas fora do intervalo recaiam numa saudação neutra.
 */
class DashboardUiMapperGreetingPropertyTest {

    // Feature: dashboard-redesign, Property 4: Saudação é não vazia e respeita o limite de comprimento
    // Validates: Requirements 3.1
    @Property(tries = 200)
    fun `greeting e sempre nao vazia e tem no maximo 60 caracteres`(
        @ForAll hourOfDay: Int
    ): Boolean {
        val saudacao = DashboardUiMapper.greeting(hourOfDay)
        return saudacao.isNotBlank() && saudacao.length <= 60
    }

    // ---- Exemplos fixos cobrindo cada faixa horária e valores fora do intervalo ----

    // Feature: dashboard-redesign, Property 4
    // Validates: Requirements 3.1
    @Example
    fun `greeting da madrugada (0 a 5) e valida`(): Boolean {
        return (0..5).all { hour ->
            val saudacao = DashboardUiMapper.greeting(hour)
            saudacao.isNotBlank() && saudacao.length <= 60
        }
    }

    // Feature: dashboard-redesign, Property 4
    // Validates: Requirements 3.1
    @Example
    fun `greeting da manha (6 a 11) e valida`(): Boolean {
        return (6..11).all { hour ->
            val saudacao = DashboardUiMapper.greeting(hour)
            saudacao.isNotBlank() && saudacao.length <= 60
        }
    }

    // Feature: dashboard-redesign, Property 4
    // Validates: Requirements 3.1
    @Example
    fun `greeting da tarde (12 a 17) e valida`(): Boolean {
        return (12..17).all { hour ->
            val saudacao = DashboardUiMapper.greeting(hour)
            saudacao.isNotBlank() && saudacao.length <= 60
        }
    }

    // Feature: dashboard-redesign, Property 4
    // Validates: Requirements 3.1
    @Example
    fun `greeting da noite (18 a 23) e valida`(): Boolean {
        return (18..23).all { hour ->
            val saudacao = DashboardUiMapper.greeting(hour)
            saudacao.isNotBlank() && saudacao.length <= 60
        }
    }

    // Feature: dashboard-redesign, Property 4
    // Validates: Requirements 3.1
    @Example
    fun `greeting para horas fora do intervalo 0 a 23 e valida`(): Boolean {
        val horasForaDoIntervalo = listOf(-1, -100, 24, 25, 100, Int.MIN_VALUE, Int.MAX_VALUE)
        return horasForaDoIntervalo.all { hour ->
            val saudacao = DashboardUiMapper.greeting(hour)
            saudacao.isNotBlank() && saudacao.length <= 60
        }
    }
}
