package com.inventario.mobile.data.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.Locale

/**
 * Property-Based Tests para [TextNormalizer].
 *
 * Feature: coleta-descricao-livre-com-sugestao (task 9.2)
 *
 * Propriedade auxiliar (suporte a P7):
 *  - Idempotência: `normalize(normalize(x)) == normalize(x)` para qualquer `x`.
 *  - Preservação ASCII: `normalize(s) == s.lowercase()` quando `s` só contém
 *    caracteres ASCII alfanuméricos (`A-Z`, `a-z`, `0-9`), pois esses caracteres
 *    não possuem marcas diacríticas a serem removidas e a normalização NFD
 *    é no-op sobre eles. O passo `lowercase` independe de locale para ASCII.
 *  - Conversão ASCII uppercase: `normalize(s) == s.lowercase(pt-BR)` quando `s`
 *    só contém caracteres ASCII maiúsculos (`A-Z`), confirmando que o pipeline
 *    aplica `lowercase` com o locale pt-BR especificado em [TextNormalizer].
 *
 * **Validates: Requirements 3.4**
 */
class TextNormalizerPropertyTest : FunSpec({

    val normalizer = TextNormalizer()
    val ptBr: Locale = Locale.forLanguageTag("pt-BR")

    /**
     * Gerador de strings contendo apenas caracteres ASCII alfanuméricos.
     *
     * Escrito manualmente em vez de usar `Codepoint.alphanumeric()` para
     * manter o teste resiliente a variações da API do Kotest e restrito
     * ao subconjunto ASCII relevante para a propriedade.
     */
    val asciiAlphanumericArb: Arb<String> = arbitrary { rs ->
        val size = Arb.int(0, 64).next(rs)
        val chars = buildString(size) {
            repeat(size) {
                val bucket = rs.random.nextInt(62)
                val ch = when {
                    bucket < 26 -> ('A' + bucket)            // 0..25  -> A-Z
                    bucket < 52 -> ('a' + (bucket - 26))     // 26..51 -> a-z
                    else        -> ('0' + (bucket - 52))     // 52..61 -> 0-9
                }
                append(ch)
            }
        }
        chars
    }

    /**
     * Gerador de strings contendo apenas letras ASCII maiúsculas `[A-Z]{0,50}`.
     *
     * Equivalente semântico de `Arb.stringPattern("[A-Z]{0,50}")`, mas
     * escrito manualmente para evitar dependência de `kotest-property-arbs`
     * (que não está no classpath deste módulo).
     */
    val asciiUppercaseArb: Arb<String> = arbitrary { rs ->
        val size = Arb.int(0, 50).next(rs)
        buildString(size) {
            repeat(size) {
                append('A' + rs.random.nextInt(26))
            }
        }
    }

    /**
     * Idempotência: aplicar `normalize` mais de uma vez não altera o resultado.
     *
     * Essencial para P7: qualquer cache (SugestaoDescricaoEntity.descricaoNormalizada)
     * e qualquer termo de busca normalizado devem convergir para um ponto fixo.
     */
    test("idempotence: normalize(normalize(x)) == normalize(x)") {
        checkAll(Arb.string(minSize = 0, maxSize = 100)) { s ->
            val once = normalizer.normalize(s)
            val twice = normalizer.normalize(once)
            twice shouldBe once
        }
    }

    /**
     * Preservação ASCII: entradas compostas apenas por `[A-Za-z0-9]`
     * devem ser normalizadas exatamente para sua versão em caixa baixa.
     *
     * - NFD não decompõe ASCII básico.
     * - A regex de marcas combinantes não remove nada.
     * - `lowercase` sobre ASCII é estável e independente de locale.
     */
    test("preserves ASCII alphanumeric in lowercase") {
        checkAll(asciiAlphanumericArb) { s ->
            normalizer.normalize(s) shouldBe s.lowercase()
        }
    }

    /**
     * ASCII uppercase torna-se lowercase: entradas compostas apenas por `[A-Z]{0,50}`
     * devem ser normalizadas para a versão em caixa baixa usando o locale `pt-BR`,
     * confirmando o contrato documentado em [TextNormalizer.normalize] de que
     * `lowercase` é aplicado com [Locale] brasileiro.
     */
    test("ASCII uppercase becomes lowercase under pt-BR locale") {
        checkAll(asciiUppercaseArb) { s ->
            normalizer.normalize(s) shouldBe s.lowercase(ptBr)
        }
    }

    /**
     * Exemplos fixos de remoção de diacríticos comuns em pt-BR.
     * Não é um property test, mas ancora o comportamento esperado contra
     * regressões silenciosas da combinação NFD + regex + lowercase.
     */
    test("removes common diacritics") {
        normalizer.normalize("Árvore") shouldBe "arvore"
        normalizer.normalize("CAFÉ") shouldBe "cafe"
        normalizer.normalize("ônibus") shouldBe "onibus"
        normalizer.normalize("Ñoño") shouldBe "nono"
        normalizer.normalize("Cadeira Giratória") shouldBe "cadeira giratoria"
        normalizer.normalize("") shouldBe ""
    }
})
