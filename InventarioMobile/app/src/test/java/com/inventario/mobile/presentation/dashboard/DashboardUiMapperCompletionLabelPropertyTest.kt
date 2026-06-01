package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide

/**
 * Property-Based Tests para [DashboardUiMapper.completionLabel].
 *
 * Feature: dashboard-redesign, Property 3: Rótulo de conclusão contém o inteiro clampado e não tem decimais
 * Validates: Requirements 2.3
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* valor `Double?`, `DashboardUiMapper.completionLabel(value)` contém
 *  exatamente o inteiro `clampPercent(value)` e não contém separador decimal
 *  (não exibe casas decimais).
 *
 * Verificações realizadas por entrada:
 *  (a) o rótulo contém exatamente o inteiro de `clampPercent(value)`;
 *  (b) o rótulo não contém ponto (`.`) nem vírgula (`,`) — sem casas decimais;
 *  (c) o inteiro extraído do rótulo está em `[0, 100]`;
 *  (d) o rótulo tem o formato exato "X% concluído".
 */
class DashboardUiMapperCompletionLabelPropertyTest {

    /**
     * Extrai o inteiro exibido no rótulo "X% concluído" (o prefixo antes do `%`).
     * Lança se o formato não corresponder, falhando o teste de forma explícita.
     */
    private fun extrairInteiro(label: String): Int {
        val prefixo = label.substringBefore('%')
        return prefixo.toInt()
    }

    /**
     * Verifica as quatro garantias (a)–(d) da propriedade para um dado [value].
     */
    private fun verificaRotulo(value: Double?): Boolean {
        val esperado = DashboardUiMapper.clampPercent(value)
        val label = DashboardUiMapper.completionLabel(value)

        // (d) formato exato "X% concluído"
        val formatoOk = label == "$esperado% concluído"

        // (b) sem separador decimal (nem ponto nem vírgula)
        val semDecimal = !label.contains('.') && !label.contains(',')

        // (a) contém exatamente o inteiro clampado
        val inteiroExtraido = extrairInteiro(label)
        val contemInteiro = inteiroExtraido == esperado

        // (c) inteiro extraído dentro de [0, 100]
        val dentroDoIntervalo = inteiroExtraido in 0..100

        return formatoOk && semDecimal && contemInteiro && dentroDoIntervalo
    }

    /**
     * Gera valores `Double` com boa distribuição na faixa relevante (-50..150),
     * cobrindo abaixo de 0, fracionários em (0,100) e acima de 100. Casos
     * especiais (`null`, `NaN`, infinitos, exatamente 0 e 100) são cobertos pelos
     * `@Example` abaixo.
     */
    @Provide
    fun percentuais(): Arbitrary<Double> =
        Arbitraries.doubles().between(-50.0, 150.0)

    // Feature: dashboard-redesign, Property 3: Rótulo de conclusão contém o inteiro clampado e não tem decimais
    // Validates: Requirements 2.3
    @Property(tries = 200)
    fun `completionLabel contem o inteiro clampado sem casas decimais`(
        @ForAll("percentuais") value: Double
    ): Boolean = verificaRotulo(value)

    // ---- Exemplos fixos (casos de borda) complementando o teste de propriedade ----

    @Example
    fun `completionLabel com null trata como zero`(): Boolean {
        val label = DashboardUiMapper.completionLabel(null)
        return label == "0% concluído" && verificaRotulo(null)
    }

    @Example
    fun `completionLabel com NaN trata como zero`(): Boolean {
        val label = DashboardUiMapper.completionLabel(Double.NaN)
        return label == "0% concluído" && verificaRotulo(Double.NaN)
    }

    @Example
    fun `completionLabel com valor negativo trata como zero`(): Boolean {
        val label = DashboardUiMapper.completionLabel(-12.7)
        return label == "0% concluído" && verificaRotulo(-12.7)
    }

    @Example
    fun `completionLabel com valor acima de 100 satura em 100`(): Boolean {
        val label = DashboardUiMapper.completionLabel(150.9)
        return label == "100% concluído" && verificaRotulo(150.9)
    }

    @Example
    fun `completionLabel arredonda valor fracionario para baixo`(): Boolean {
        // 42.4 arredonda para 42 (mais próximo)
        val label = DashboardUiMapper.completionLabel(42.4)
        return label == "42% concluído" && verificaRotulo(42.4)
    }

    @Example
    fun `completionLabel arredonda valor fracionario para cima`(): Boolean {
        // 42.6 arredonda para 43 (mais próximo)
        val label = DashboardUiMapper.completionLabel(42.6)
        return label == "43% concluído" && verificaRotulo(42.6)
    }

    @Example
    fun `completionLabel com exatamente zero`(): Boolean {
        val label = DashboardUiMapper.completionLabel(0.0)
        return label == "0% concluído" && verificaRotulo(0.0)
    }

    @Example
    fun `completionLabel com exatamente 100`(): Boolean {
        val label = DashboardUiMapper.completionLabel(100.0)
        return label == "100% concluído" && verificaRotulo(100.0)
    }

    @Example
    fun `completionLabel com infinito positivo satura em 100`(): Boolean {
        val label = DashboardUiMapper.completionLabel(Double.POSITIVE_INFINITY)
        return label == "100% concluído" && verificaRotulo(Double.POSITIVE_INFINITY)
    }

    @Example
    fun `completionLabel com infinito negativo trata como zero`(): Boolean {
        val label = DashboardUiMapper.completionLabel(Double.NEGATIVE_INFINITY)
        return label == "0% concluído" && verificaRotulo(Double.NEGATIVE_INFINITY)
    }
}
