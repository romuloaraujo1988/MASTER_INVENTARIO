package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.LongRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Property-Based Tests para [DashboardUiMapper.formatLastSync].
 *
 * Feature: dashboard-redesign, Property 6: Formatação da última sincronização é fiel
 *          ao instante (round-trip ao minuto)
 * Validates: Requirements 3.3, 3.5
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a Testing
 * Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* `timestamp: Long?`, `DashboardUiMapper.formatLastSync(timestamp)` retorna
 *  `null` quando `timestamp` é nulo; caso contrário, retorna uma string no padrão
 *  `dd/MM/yyyy HH:mm` (24h) que, ao ser reparseada com o mesmo padrão e fuso,
 *  corresponde ao `timestamp` original truncado ao minuto.
 *
 * Notas sobre os geradores e o fuso/locale:
 *  - A implementação constrói `SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())`
 *    SEM definir um `TimeZone` explícito — portanto usa `TimeZone.getDefault()` do
 *    dispositivo. O formatter de referência deste teste é construído de forma IDÊNTICA
 *    (mesmo padrão, `Locale.getDefault()` e o `TimeZone.getDefault()` implícito do
 *    `SimpleDateFormat`), eliminando qualquer flutuação de fuso/locale entre a
 *    formatação (implementação) e o reparse (teste).
 *  - Os timestamps são gerados numa faixa realista e positiva — de 2020-01-01 a
 *    2100-01-01 (em ms desde a epoch). Nessa faixa, o deslocamento do fuso padrão é
 *    constante e múltiplo de minuto inteiro, de modo que a relação "truncado ao minuto"
 *    (diferença em `[0, 60000) ms` e mesma representação de minuto) vale de forma
 *    determinística.
 */
class DashboardUiMapperFormatLastSyncPropertyTest {

    private val pattern = "dd/MM/yyyy HH:mm"

    /** Regex do padrão dd/MM/yyyy HH:mm (24h). */
    private val patternRegex = Regex("""\d{2}/\d{2}/\d{4} \d{2}:\d{2}""")

    private companion object {
        /** 2020-01-01T00:00:00Z em ms desde a epoch (início da faixa realista). */
        const val MIN_REALISTIC_MILLIS = 1_577_836_800_000L

        /** 2100-01-01T00:00:00Z em ms desde a epoch (fim da faixa realista). */
        const val MAX_REALISTIC_MILLIS = 4_102_444_800_000L

        const val ONE_MINUTE_MILLIS = 60_000L
    }

    /**
     * Cria um formatter de referência IDÊNTICO ao usado pela implementação:
     * mesmo padrão, `Locale.getDefault()` e o `TimeZone.getDefault()` implícito do
     * `SimpleDateFormat`.
     */
    private fun referenceFormatter(): SimpleDateFormat =
        SimpleDateFormat(pattern, Locale.getDefault())

    // Feature: dashboard-redesign, Property 6: Formatação da última sincronização é fiel ao instante (round-trip ao minuto)
    // Validates: Requirements 3.3, 3.5
    @Property(tries = 200)
    fun `formatLastSync produz string no padrao que reparseia para o instante truncado ao minuto`(
        @ForAll @LongRange(min = MIN_REALISTIC_MILLIS, max = MAX_REALISTIC_MILLIS) timestamp: Long
    ) {
        val formatted = DashboardUiMapper.formatLastSync(timestamp)

        // Timestamp não nulo => resultado não nulo (Req 3.3).
        assertNotNull(formatted, "formatLastSync deve retornar não-nulo para timestamp não-nulo")

        // O resultado segue o padrão dd/MM/yyyy HH:mm (24h).
        assertTrue(
            patternRegex.matches(formatted!!),
            "Esperado padrão dd/MM/yyyy HH:mm, obtido: '$formatted'"
        )

        // Reparse com formatter IDÊNTICO (mesmo padrão, fuso e locale).
        val reparsed: Date = referenceFormatter().parse(formatted)

        // Round-trip ao minuto: reparsear e reformatar produz a MESMA representação
        // (invariante de fidelidade ao minuto, imune a fuso/locale).
        val reformatted = referenceFormatter().format(reparsed)
        assertEquals(
            formatted,
            reformatted,
            "Reformatação do instante reparseado deve coincidir com a string original"
        )

        // O instante original e o reparseado caem na mesma representação de minuto.
        val originalFormatted = referenceFormatter().format(Date(timestamp))
        assertEquals(
            originalFormatted,
            formatted,
            "A string formatada deve representar o mesmo minuto do timestamp original"
        )

        // Diferença < 60000 ms: o instante reparseado é o início do minuto exibido,
        // portanto está dentro do mesmo minuto do timestamp original.
        val diff = timestamp - reparsed.time
        assertTrue(
            diff in 0L until ONE_MINUTE_MILLIS,
            "Diferença esperada em [0, 60000) ms, obtido: $diff ms " +
                "(timestamp=$timestamp, reparsed=${reparsed.time})"
        )
    }

    // ---- Exemplos fixos (casos de borda) complementando o teste de propriedade ----

    // Feature: dashboard-redesign, Property 6: timestamp null → null
    // Validates: Requirements 3.5
    @Example
    fun `formatLastSync retorna null quando timestamp e null`() {
        assertNull(DashboardUiMapper.formatLastSync(null))
    }

    // Feature: dashboard-redesign, Property 6: round-trip ao minuto (instante conhecido)
    // Validates: Requirements 3.3
    @Example
    fun `formatLastSync trunca segundos e milissegundos ao minuto`() {
        // Instante base alinhado ao minuto + 37,123 s dentro do mesmo minuto.
        // Usa o mesmo fuso padrão para construir e reparsear, garantindo consistência.
        val baseMinute = 1_700_000_040_000L // múltiplo de 60000 (alinhado ao minuto em UTC)
        val withinMinute = baseMinute + 37_123L

        val formattedBase = DashboardUiMapper.formatLastSync(baseMinute)
        val formattedWithin = DashboardUiMapper.formatLastSync(withinMinute)

        assertNotNull(formattedBase)
        assertNotNull(formattedWithin)
        // Ambos no mesmo minuto => mesma string (segundos/millis descartados).
        assertEquals(formattedBase, formattedWithin)
        assertTrue(patternRegex.matches(formattedWithin!!))
    }

    // Feature: dashboard-redesign, Property 6: padrão 24h
    // Validates: Requirements 3.3
    @Example
    fun `formatLastSync usa relogio de 24 horas`() {
        // Verifica que a parte de hora respeita o intervalo 00..23 para um instante à tarde.
        val timestamp = 1_700_000_000_000L
        val formatted = DashboardUiMapper.formatLastSync(timestamp)
        assertNotNull(formatted)
        assertTrue(patternRegex.matches(formatted!!))

        // A representação deve ser idêntica à de um formatter de referência idêntico.
        val expected = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
        assertEquals(expected, formatted)
    }

    init {
        // Garante determinismo da formatação/parse: ambos usam TimeZone.getDefault().
        // (Sanity guard — não altera o fuso, apenas referencia o padrão atual.)
        TimeZone.getDefault()
    }
}
