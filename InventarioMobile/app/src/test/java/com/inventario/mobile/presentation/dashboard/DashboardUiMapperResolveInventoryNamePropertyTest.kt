package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide

/**
 * Property-Based Tests para [DashboardUiMapper.resolveInventoryName].
 *
 * Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
 * Validates: Requirements 3.2, 3.4, 3.6
 *
 * Biblioteca: jqwik (property-based testing na JUnit 5 Platform), conforme a
 * Testing Strategy do design. Os testes do mapper são JVM puros (sem Android).
 *
 * Propriedade (texto do design):
 *  *For any* `inventarioNome: String?` e `inventarioId: Int?`,
 *  `DashboardUiMapper.resolveInventoryName(nome, id)` retorna um texto não vazio com
 *  no máximo 80 caracteres; quando `nome` é não nulo e excede 80 caracteres, o
 *  resultado termina com reticências (…) e seu prefixo corresponde ao início de
 *  `nome`; quando `nome` é nulo e `id` não nulo, o resultado contém o `id`; quando
 *  ambos são nulos, retorna um texto substituto genérico não vazio.
 *
 * Geradores: cobrem explicitamente strings de comprimento 0, ~80 e >80, incluindo
 * caracteres unicode (acentos, cedilha, CJK, símbolos e — nos exemplos — pares
 * surrogate/emoji), além de `id` nulo, zero, negativo e arbitrário.
 */
class DashboardUiMapperResolveInventoryNamePropertyTest {

    private val maxLen = 80
    private val ellipsis = "…"
    private val placeholder = "Nenhum inventário ativo"

    // ---------------------------------------------------------------------------------------
    // Geradores (smart generators que constrangem o espaço de entrada de forma inteligente)
    // ---------------------------------------------------------------------------------------

    /**
     * Caracteres-base: ASCII (letras/dígitos), separadores comuns e um conjunto de
     * caracteres unicode do BMP (acentos, cedilha, CJK, símbolos). Por usar apenas
     * `Char` (1 UTF-16 code unit), o comprimento da String gerada é exatamente o
     * tamanho da lista — o que torna os limites de tamanho determinísticos.
     */
    private fun baseChars(): Arbitrary<Char> =
        Arbitraries.chars()
            .range('a', 'z')
            .range('A', 'Z')
            .range('0', '9')
            .with(
                ' ', '.', '-', '_', '/',
                'á', 'à', 'â', 'ã', 'é', 'ê', 'í', 'ó', 'ô', 'õ', 'ú',
                'ç', 'ñ', 'ü', '…', '€', '£', '你', '好', 'Ω', 'π'
            )

    private fun stringsDeTamanho(min: Int, max: Int): Arbitrary<String> =
        baseChars().list().ofMinSize(min).ofMaxSize(max).map { it.joinToString("") }

    /** Nomes opcionais cobrindo curtos/vazio, ao redor de 80 e longos; ~25% nulos. */
    @Provide
    fun nomesOpcionais(): Arbitrary<String?> =
        Arbitraries.oneOf(
            stringsDeTamanho(0, 20),
            stringsDeTamanho(70, 90),
            stringsDeTamanho(81, 200)
        ).injectNull(0.25)

    /** Nomes não vazios com comprimento estritamente maior que 80 (forçam truncamento). */
    @Provide
    fun nomesLongos(): Arbitrary<String> =
        stringsDeTamanho(81, 220).filter { it.isNotBlank() }

    /** Nomes não vazios com comprimento de 1..80 (não devem ser truncados). */
    @Provide
    fun nomesValidosCurtos(): Arbitrary<String> =
        stringsDeTamanho(1, 80).filter { it.isNotBlank() }

    /** `id` opcional arbitrário (inclui nulo, zero, negativos e grandes). */
    @Provide
    fun idsOpcionais(): Arbitrary<Int?> =
        Arbitraries.integers().injectNull(0.25)

    /** Nomes "ausentes": nulo ou em branco (string vazia/whitespace). */
    @Provide
    fun nomesAusentes(): Arbitrary<String?> =
        Arbitraries.of("", " ", "   ", "\t", "\n", "  \t ").injectNull(0.4)

    // ---------------------------------------------------------------------------------------
    // Propriedades
    // ---------------------------------------------------------------------------------------

    // Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
    // Validates: Requirements 3.2, 3.4, 3.6
    @Property(tries = 200)
    fun `resultado nunca vazio e com no maximo 80 caracteres`(
        @ForAll("nomesOpcionais") nome: String?,
        @ForAll("idsOpcionais") id: Int?
    ): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName(nome, id)
        return resultado.isNotEmpty() && resultado.length <= maxLen
    }

    // Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
    // Validates: Requirements 3.2
    @Property(tries = 200)
    fun `nome maior que 80 e truncado para exatamente 80 chars com reticencias e prefixo preservado`(
        @ForAll("nomesLongos") nome: String,
        @ForAll("idsOpcionais") id: Int?
    ): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName(nome, id)
        return resultado.length == maxLen &&
            resultado.endsWith(ellipsis) &&
            resultado.startsWith(nome.take(maxLen - ellipsis.length))
    }

    // Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
    // Validates: Requirements 3.2
    @Property(tries = 200)
    fun `nome valido com no maximo 80 caracteres e retornado inalterado`(
        @ForAll("nomesValidosCurtos") nome: String,
        @ForAll("idsOpcionais") id: Int?
    ): Boolean {
        return DashboardUiMapper.resolveInventoryName(nome, id) == nome
    }

    // Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
    // Validates: Requirements 3.4
    @Property(tries = 200)
    fun `nome ausente com id presente produz substituto que contem o id`(
        @ForAll("nomesAusentes") nome: String?,
        @ForAll id: Int
    ): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName(nome, id)
        return resultado == "Inventário $id" &&
            resultado.contains(id.toString()) &&
            resultado.isNotEmpty()
    }

    // Feature: dashboard-redesign, Property 5: Nome do inventário é truncado a 80 caracteres com reticências
    // Validates: Requirements 3.6
    @Property(tries = 100)
    fun `nome e id ausentes retornam placeholder generico nao vazio`(
        @ForAll("nomesAusentes") nome: String?
    ): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName(nome, null)
        return resultado == placeholder && resultado.isNotEmpty()
    }

    // ---------------------------------------------------------------------------------------
    // Exemplos fixos (casos de borda) complementando os testes de propriedade
    // ---------------------------------------------------------------------------------------

    @Example
    fun `nome nulo e id nulo retorna placeholder`(): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName(null, null)
        return resultado == placeholder
    }

    @Example
    fun `nome vazio com id retorna substituto com id`(): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName("", 42)
        return resultado == "Inventário 42"
    }

    @Example
    fun `nome em branco com id retorna substituto com id`(): Boolean {
        val resultado = DashboardUiMapper.resolveInventoryName("   ", 7)
        return resultado == "Inventário 7"
    }

    @Example
    fun `id zero e negativo sao exibidos no substituto`(): Boolean {
        val r0 = DashboardUiMapper.resolveInventoryName(null, 0)
        val rNeg = DashboardUiMapper.resolveInventoryName(null, -5)
        return r0 == "Inventário 0" && rNeg == "Inventário -5"
    }

    @Example
    fun `nome com exatamente 80 caracteres permanece inalterado`(): Boolean {
        val nome = "A".repeat(80)
        val resultado = DashboardUiMapper.resolveInventoryName(nome, null)
        return resultado == nome && resultado.length == 80
    }

    @Example
    fun `nome com 81 caracteres e truncado para 80 com reticencias`(): Boolean {
        val nome = "B".repeat(81)
        val resultado = DashboardUiMapper.resolveInventoryName(nome, 1)
        return resultado.length == 80 &&
            resultado.endsWith(ellipsis) &&
            resultado.startsWith("B".repeat(79)) &&
            !resultado.contains("Inventário")
    }

    @Example
    fun `nome longo com pares surrogate (emoji) e truncado a 80 chars`(): Boolean {
        // 50 emojis = 100 UTF-16 code units (cada emoji ocupa 2 chars)
        val nome = "\uD83D\uDE00".repeat(50)
        val resultado = DashboardUiMapper.resolveInventoryName(nome, null)
        return resultado.length == 80 && resultado.endsWith(ellipsis)
    }

    @Example
    fun `nome longo com acentos e truncado preservando prefixo`(): Boolean {
        val nome = "Inventário Anual de Patrimônio do Câmpus — " +
            "Levantamento Geral de Bens Móveis e Imóveis 2025"
        val resultado = DashboardUiMapper.resolveInventoryName(nome, null)
        return nome.length > 80 &&
            resultado.length == 80 &&
            resultado.endsWith(ellipsis) &&
            resultado.startsWith(nome.take(79))
    }

    @Example
    fun `nome curto unicode e retornado inalterado`(): Boolean {
        val nome = "Inventário Câmpus 你好 π"
        val resultado = DashboardUiMapper.resolveInventoryName(nome, 99)
        return resultado == nome
    }
}
