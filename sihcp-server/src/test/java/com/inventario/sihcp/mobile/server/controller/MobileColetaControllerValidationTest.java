package com.inventario.sihcp.mobile.server.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property test P14 — Validação de tamanho no servidor + compatibilidade
 * retroativa de coletas.
 *
 * <p><strong>Validates: Requirements 1.7, 9.8</strong></p>
 *
 * <p>Testa o método auxiliar privado
 * {@code MobileColetaController#validarDescricaoItemSemEtiqueta(String)}
 * (usado pelos endpoints {@code POST /api/mobile/coletas} e
 * {@code POST /api/mobile/coletas/batch}) cobrindo:</p>
 *
 * <ul>
 *   <li><b>Req 9.8</b> — Campo {@code descricaoItemSemEtiqueta} ausente ({@code null})
 *       é aceito silenciosamente (compat retroativa com clientes legados).</li>
 *   <li><b>Req 1.7</b> — Se presente, o valor após {@code trim()} deve ter
 *       tamanho no intervalo fechado {@code [3, 255]}; fora desse intervalo,
 *       retorna mensagem de erro.</li>
 * </ul>
 *
 * <p>Acessa o helper via reflexão para manter o encapsulamento do controller
 * intacto. Como o helper é uma função pura (não depende de campos
 * {@code @Autowired}), instancia-se o controller com {@code new} diretamente.</p>
 *
 * @author Sistema de Inventário
 * @since 2.7.0
 */
@Tag("property-test")
@DisplayName("Property Test P14: Validação server-side de descricaoItemSemEtiqueta + compat retroativa")
class MobileColetaControllerValidationTest {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 255;

    private static MobileColetaController controller;
    private static Method validarMethod;

    @BeforeAll
    static void setUp() throws NoSuchMethodException {
        controller = new MobileColetaController();
        validarMethod = MobileColetaController.class.getDeclaredMethod(
                "validarDescricaoItemSemEtiqueta", String.class);
        validarMethod.setAccessible(true);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Invoca o método privado {@code validarDescricaoItemSemEtiqueta} via
     * reflexão. Retorna {@code null} quando a descrição é válida, ou a
     * mensagem de erro quando inválida.
     */
    private static String invokeValidar(String descricao) {
        try {
            return (String) validarMethod.invoke(controller, descricao);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Falha ao acessar método privado: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("Método privado lançou exceção inesperada: "
                    + e.getTargetException().getMessage(), e.getTargetException());
        }
    }

    private static String repeat(char c, int n) {
        char[] buf = new char[n];
        for (int i = 0; i < n; i++) {
            buf[i] = c;
        }
        return new String(buf);
    }

    // =========================================================================
    // Testes de exemplo (Req 9.8)
    // =========================================================================

    @Test
    @DisplayName("descricao null retorna null (Req 9.8 — compat cliente legado)")
    void descricaoNull_retornaNull() {
        // Cliente legado que não envia o campo → null. Deve passar sem erro.
        String erro = invokeValidar(null);
        assertNull(erro, "Campo ausente (null) deve ser aceito silenciosamente "
                + "para preservar compatibilidade com clientes legados (Req 9.8)");
    }

    // =========================================================================
    // Testes de exemplo (Req 1.7) — tamanhos válidos
    // =========================================================================

    @ParameterizedTest(name = "length={0} deve ser válido")
    @ValueSource(ints = {3, 4, 10, 100, 254, 255})
    @DisplayName("descricao com tamanho em [3,255] retorna null")
    void descricaoValidaCumprindoLimites(int length) {
        String descricao = repeat('a', length);
        String erro = invokeValidar(descricao);
        assertNull(erro, String.format(
                "Descrição de tamanho %d deve ser aceita (intervalo válido [%d,%d])",
                length, MIN_LENGTH, MAX_LENGTH));
    }

    // =========================================================================
    // Testes de exemplo (Req 1.7) — tamanhos inválidos (abaixo do mínimo)
    // =========================================================================

    @ParameterizedTest(name = "length={0} deve ser rejeitado (abaixo do mínimo)")
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("descricao abaixo do mínimo retorna mensagem de erro")
    void descricaoAbaixoDoMinimoRetornaErro(int length) {
        String descricao = repeat('a', length);
        String erro = invokeValidar(descricao);
        assertNotNull(erro, String.format(
                "Descrição de tamanho %d deve ser rejeitada (mínimo = %d)",
                length, MIN_LENGTH));
        assertTrue(erro.contains("3") && erro.contains("255"),
                "Mensagem de erro deve mencionar o intervalo permitido [3, 255]: " + erro);
    }

    // =========================================================================
    // Testes de exemplo (Req 1.7) — tamanhos inválidos (acima do máximo)
    // =========================================================================

    @ParameterizedTest(name = "length={0} deve ser rejeitado (acima do máximo)")
    @ValueSource(ints = {256, 300})
    @DisplayName("descricao acima do máximo retorna mensagem de erro")
    void descricaoAcimaDoMaximoRetornaErro(int length) {
        String descricao = repeat('a', length);
        String erro = invokeValidar(descricao);
        assertNotNull(erro, String.format(
                "Descrição de tamanho %d deve ser rejeitada (máximo = %d)",
                length, MAX_LENGTH));
        assertTrue(erro.contains("3") && erro.contains("255"),
                "Mensagem de erro deve mencionar o intervalo permitido [3, 255]: " + erro);
    }

    // =========================================================================
    // Testes com whitespace — a validação aplica trim() antes de medir
    // =========================================================================

    @ParameterizedTest(name = "whitespace puro {0} caracteres deve ser rejeitado")
    @ValueSource(strings = {"   ", "\t\t", "\n  ", " ", "\t", "\n", "    \t\n  "})
    @DisplayName("descricao com apenas whitespace retorna erro (trim -> length=0)")
    void descricaoSoWhitespace_retornaErro(String whitespace) {
        // trim().length() == 0, portanto abaixo do mínimo (3).
        String erro = invokeValidar(whitespace);
        assertNotNull(erro, "Descrição com apenas whitespace deve ser rejeitada. "
                + "Input=\"" + whitespace.replace("\t", "\\t").replace("\n", "\\n") + "\"");
    }

    @Test
    @DisplayName("descricao com whitespace circundante e núcleo em [3,255] é aceita")
    void descricaoComWhitespaceCircundanteValida() {
        // "  abc  ".trim() -> "abc" (length=3) → dentro do intervalo [3,255].
        String erro = invokeValidar("  abc  ");
        assertNull(erro, "Whitespace circundante deve ser ignorado; núcleo \"abc\" "
                + "tem 3 caracteres e deve ser aceito");
    }

    // =========================================================================
    // Property-based test: varredura do intervalo [0, 300]
    // =========================================================================

    /**
     * <strong>Property P14:</strong> para todo {@code n ∈ [0, 300]}, a
     * validação do helper é equivalente a:
     *
     * <pre>
     *   erro == null  ⇔  MIN_LENGTH ≤ trim(s).length ≤ MAX_LENGTH
     * </pre>
     *
     * onde {@code s = "a".repeat(n)} (sem whitespace, portanto
     * {@code trim(s).length == n}).
     */
    @ParameterizedTest(name = "length={0}: erro=null ⇔ length ∈ [3,255]")
    @ValueSource(ints = {0, 1, 2, 3, 10, 50, 100, 254, 255, 256, 300})
    @DisplayName("Property P14: validação é equivalente ao predicado 3 ≤ length ≤ 255")
    void propertyBasedLengthRange(int n) {
        String descricao = repeat('a', n);
        String erro = invokeValidar(descricao);

        boolean dentroDoIntervalo = n >= MIN_LENGTH && n <= MAX_LENGTH;
        boolean aceitouSemErro = (erro == null);

        assertEquals(dentroDoIntervalo, aceitouSemErro, String.format(
                "Property P14 violada para length=%d: esperado %s, obtido %s (erro=%s)",
                n,
                dentroDoIntervalo ? "aceitação (erro=null)" : "rejeição (erro!=null)",
                aceitouSemErro ? "aceitação"                : "rejeição",
                erro));
    }
}
