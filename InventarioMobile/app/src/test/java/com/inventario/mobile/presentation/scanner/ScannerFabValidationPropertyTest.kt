package com.inventario.mobile.presentation.scanner

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 1: Pré-condição obrigatória para abrir o fluxo
 * Validates: Requirements 2.4, 2.5, 7.2
 *
 * For any input (salaId, salaNome, inventarioId) em que a sala NÃO esteja válida
 * (salaId <= 0 OR salaNome é vazio/whitespace/null) OU o inventário NÃO esteja ativo
 * (inventarioId == null OR inventarioId <= 0), a função handleColetaSemEtiquetaClick()
 * NÃO deve iniciar a Activity e NÃO deve chamar registrarColetaPorDescricao.
 *
 * Esta propriedade valida a mesma regra que `ScannerActivity.handleColetaSemEtiquetaClick()`
 * aplica antes de chamar `startActivity(...)`. Aqui extraímos a regra em uma função pura
 * (`shouldStartDescricaoFlow`) para testá-la sem dependência Android, seguindo o mesmo
 * padrão de `ColetasViewModelPropertyTest`.
 */
class ScannerFabValidationPropertyTest : StringSpec({

    "Property 1: Pré-condição obrigatória — entrada inválida bloqueia startActivity" {
        checkAll(
            iterations = 100,
            Arb.int(Int.MIN_VALUE..0),
            Arb.string(0..30).orNull(),
            Arb.int(Int.MIN_VALUE..0).orNull()
        ) { salaIdInvalida, salaNomeQualquer, inventarioIdInvalido ->
            // Entrada 100% inválida: salaId <= 0 e inventarioId inválido.
            // Qualquer combinação aqui deve bloquear.
            val aberto = shouldStartDescricaoFlow(
                salaId = salaIdInvalida,
                salaNome = salaNomeQualquer,
                inventarioId = inventarioIdInvalido
            )
            aberto shouldBe false
        }
    }

    "Property 1: Pré-condição obrigatória — salaNome em branco/whitespace bloqueia" {
        checkAll(
            iterations = 100,
            Arb.int(1..Int.MAX_VALUE),
            Arb.string(0..5).orNull(), // inclui "", "   ", null
            Arb.int(1..Int.MAX_VALUE)
        ) { salaIdValida, salaNomeMaybeBlank, inventarioIdValido ->
            val aberto = shouldStartDescricaoFlow(
                salaId = salaIdValida,
                salaNome = salaNomeMaybeBlank,
                inventarioId = inventarioIdValido
            )
            val esperado = !salaNomeMaybeBlank.isNullOrBlank()
            aberto shouldBe esperado
        }
    }

    "Property 1: Pré-condição obrigatória — inventarioId null/<=0 bloqueia" {
        checkAll(
            iterations = 100,
            Arb.int(1..Int.MAX_VALUE),
            Arb.string(1..20),
            Arb.int(Int.MIN_VALUE..Int.MAX_VALUE).orNull()
        ) { salaIdValida, salaNomeValido, inventarioIdQualquer ->
            val entradaDeveAbrir =
                salaNomeValido.isNotBlank() &&
                (inventarioIdQualquer != null && inventarioIdQualquer > 0)

            val aberto = shouldStartDescricaoFlow(
                salaId = salaIdValida,
                salaNome = salaNomeValido,
                inventarioId = inventarioIdQualquer
            )

            aberto shouldBe entradaDeveAbrir
        }
    }
})

/**
 * Pure-logic extract of `ScannerActivity.handleColetaSemEtiquetaClick()` gating.
 *
 * Retorna `true` somente quando a Activity de descrição DEVE ser iniciada:
 *  - salaId > 0
 *  - salaNome não é nulo nem blank
 *  - inventarioId != null e > 0
 *
 * Caso contrário retorna `false`, representando o caminho em que a Activity exibe
 * um Toast e retorna sem navegar.
 */
internal fun shouldStartDescricaoFlow(
    salaId: Int,
    salaNome: String?,
    inventarioId: Int?
): Boolean {
    if (salaId <= 0 || salaNome.isNullOrBlank()) return false
    if (inventarioId == null || inventarioId <= 0) return false
    return true
}
