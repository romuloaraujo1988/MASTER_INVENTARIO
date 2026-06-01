package com.inventario.mobile.presentation.dashboard

import net.jqwik.api.Example

/**
 * Teste unitário de razão de contraste (WCAG) dos pares texto/fundo usados no
 * redesign do Dashboard.
 *
 * Feature: dashboard-redesign — contrast (Req 7.3)
 *
 * Critério de aceitação (Req 7.3): aplicar combinações de cor de texto e fundo que
 * atendam ao contraste WCAG AA "onde for viável" — texto normal ≥ 4.5:1 e texto
 * grande ≥ 3:1.
 *
 * Abordagem: o cálculo de contraste WCAG é implementado aqui no próprio teste
 * (luminância relativa sRGB → linear com gamma + razão (L1+0.05)/(L2+0.05)), sem
 * dependência de Android, de modo que roda na JVM pura (`app/src/test/`). Cada par
 * relevante do tema é verificado individualmente com asserts e o ratio calculado é
 * impresso para rastreabilidade.
 *
 * Biblioteca: jqwik (`@Example`), consistente com os demais testes deste pacote e
 * garantindo descoberta sob `useJUnitPlatform()` (não há junit-vintage no projeto;
 * @Test do JUnit4 não seria executado).
 *
 * IMPORTANTE — sincronização com colors.xml:
 * Os valores hex abaixo são CÓPIAS dos definidos em
 * `app/src/main/res/values/colors.xml`. Eles resolvem os aliases usados pelo
 * Dashboard até a cor concreta. Se qualquer cor em colors.xml mudar, estes
 * literais DEVEM ser atualizados para manter o teste fiel ao tema real.
 *
 *   Fundo neutro dos cartões / background / surface:
 *     ?attr/colorSurface ≈ md3_surface = background = surface = #F8F9FF
 *
 *   Realces de KPI (aplicados ao ícone e/ou valor numérico — "texto grande"):
 *     dashboard_kpi_coletados_accent    = @color/success = #1A6B3A
 *     dashboard_kpi_pendentes_accent    = @color/warning = #7A4F00
 *     dashboard_kpi_divergencias_accent = @color/error   = #BA1A1A
 *     dashboard_kpi_coletores_accent    = @color/info     = #1B5EA7
 *
 *   Texto neutro:
 *     dashboard_kpi_label = md3_on_surface_variant = #43474E
 *     text_primary        = #191C20
 *     text_secondary      = md3_on_surface_variant = #43474E
 */
class DashboardContrastTest {

    // ── Cores concretas lidas de colors.xml (manter em sincronia) ──────────────
    private companion object {
        const val SURFACE = "#F8F9FF" // md3_surface / background / surface

        // Realces dos KPIs (texto grande: valor numérico do KPI)
        const val ACCENT_COLETADOS = "#1A6B3A"    // success
        const val ACCENT_PENDENTES = "#7A4F00"    // warning
        const val ACCENT_DIVERGENCIAS = "#BA1A1A" // error
        const val ACCENT_COLETORES = "#1B5EA7"    // info

        // Texto neutro
        const val KPI_LABEL = "#43474E"      // md3_on_surface_variant
        const val TEXT_PRIMARY = "#191C20"   // text_primary
        const val TEXT_SECONDARY = "#43474E" // text_secondary (= md3_on_surface_variant)

        // Limiares WCAG AA
        const val MIN_NORMAL_TEXT = 4.5
        const val MIN_LARGE_TEXT = 3.0
    }

    // ── Cálculo de contraste WCAG (sem dependência de Android) ─────────────────

    /** Converte um canal sRGB de 8 bits [0,255] para o valor linear (gamma). */
    private fun srgbChannelToLinear(channel8: Int): Double {
        val c = channel8 / 255.0
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }

    /** Faz o parse de "#RRGGBB" (ou "#AARRGGBB") nos três canais R, G, B (0..255). */
    private fun parseRgb(hex: String): Triple<Int, Int, Int> {
        val h = hex.removePrefix("#")
        val rgb = if (h.length == 8) h.substring(2) else h // descarta alfa, se houver
        require(rgb.length == 6) { "Cor hex inválida: $hex" }
        val r = rgb.substring(0, 2).toInt(16)
        val g = rgb.substring(2, 4).toInt(16)
        val b = rgb.substring(4, 6).toInt(16)
        return Triple(r, g, b)
    }

    /** Luminância relativa WCAG: 0.2126*R + 0.7152*G + 0.0722*B (em espaço linear). */
    private fun relativeLuminance(hex: String): Double {
        val (r, g, b) = parseRgb(hex)
        return 0.2126 * srgbChannelToLinear(r) +
            0.7152 * srgbChannelToLinear(g) +
            0.0722 * srgbChannelToLinear(b)
    }

    /** Razão de contraste WCAG: (L_claro + 0.05) / (L_escuro + 0.05), em [1, 21]. */
    private fun contrastRatio(fg: String, bg: String): Double {
        val l1 = relativeLuminance(fg)
        val l2 = relativeLuminance(bg)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** Verifica e reporta o contraste de um par, falhando se ficar abaixo do limiar. */
    private fun assertContrast(label: String, fg: String, bg: String, minRatio: Double) {
        val ratio = contrastRatio(fg, bg)
        val rounded = Math.round(ratio * 100.0) / 100.0
        println("Contraste [$label] fg=$fg sobre bg=$bg → ${rounded}:1 (mínimo ${minRatio}:1)")
        if (ratio < minRatio) {
            throw AssertionError(
                "Contraste insuficiente em [$label]: ${rounded}:1 (fg=$fg, bg=$bg); " +
                    "exigido ≥ ${minRatio}:1 (WCAG AA, Req 7.3)"
            )
        }
    }

    // ── Sanidade da implementação WCAG ─────────────────────────────────────────

    @Example
    fun `branco sobre preto produz o contraste maximo de 21 a 1`() {
        val ratio = contrastRatio("#FFFFFF", "#000000")
        // Tolerância numérica pequena: o máximo teórico WCAG é 21:1.
        if (Math.abs(ratio - 21.0) > 0.01) {
            throw AssertionError("Esperado ~21.0:1 para branco/preto, obtido $ratio")
        }
        // Cor idêntica deve render exatamente 1:1.
        val same = contrastRatio(SURFACE, SURFACE)
        if (Math.abs(same - 1.0) > 1e-9) {
            throw AssertionError("Esperado 1.0:1 para cores idênticas, obtido $same")
        }
    }

    // ── Realces dos KPIs sobre a superfície neutra (valor numérico = texto grande) ──

    @Example
    fun `realce Coletados (success) sobre surface atende WCAG AA`() {
        // Valor numérico do KPI é texto grande; ainda assim verificamos contra o
        // limiar de texto normal (4.5:1) pois o par atinge esse nível.
        assertContrast("KPI Coletados (success)", ACCENT_COLETADOS, SURFACE, MIN_NORMAL_TEXT)
    }

    @Example
    fun `realce Pendentes (warning) sobre surface atende WCAG AA`() {
        assertContrast("KPI Pendentes (warning)", ACCENT_PENDENTES, SURFACE, MIN_NORMAL_TEXT)
    }

    @Example
    fun `realce Divergencias (error) sobre surface atende WCAG AA`() {
        assertContrast("KPI Divergencias (error)", ACCENT_DIVERGENCIAS, SURFACE, MIN_NORMAL_TEXT)
    }

    @Example
    fun `realce Coletores (info) sobre surface atende WCAG AA`() {
        assertContrast("KPI Coletores (info)", ACCENT_COLETORES, SURFACE, MIN_NORMAL_TEXT)
    }

    // ── Texto neutro sobre a superfície neutra ─────────────────────────────────

    @Example
    fun `rotulo neutro do KPI sobre surface atende WCAG AA`() {
        assertContrast("Rótulo KPI (on_surface_variant)", KPI_LABEL, SURFACE, MIN_NORMAL_TEXT)
    }

    @Example
    fun `text_primary sobre surface atende WCAG AA`() {
        assertContrast("text_primary", TEXT_PRIMARY, SURFACE, MIN_NORMAL_TEXT)
    }

    @Example
    fun `text_secondary sobre surface atende WCAG AA`() {
        assertContrast("text_secondary", TEXT_SECONDARY, SURFACE, MIN_NORMAL_TEXT)
    }

    // ── Verificação agregada: nenhum par fica abaixo do mínimo de texto grande ──

    @Example
    fun `todos os pares texto-realce sobre surface atendem ao minimo de texto grande`() {
        val pairs = listOf(
            Triple("KPI Coletados (success)", ACCENT_COLETADOS, SURFACE),
            Triple("KPI Pendentes (warning)", ACCENT_PENDENTES, SURFACE),
            Triple("KPI Divergencias (error)", ACCENT_DIVERGENCIAS, SURFACE),
            Triple("KPI Coletores (info)", ACCENT_COLETORES, SURFACE),
            Triple("Rótulo KPI (on_surface_variant)", KPI_LABEL, SURFACE),
            Triple("text_primary", TEXT_PRIMARY, SURFACE),
            Triple("text_secondary", TEXT_SECONDARY, SURFACE)
        )
        pairs.forEach { (label, fg, bg) ->
            assertContrast(label, fg, bg, MIN_LARGE_TEXT)
        }
    }
}
