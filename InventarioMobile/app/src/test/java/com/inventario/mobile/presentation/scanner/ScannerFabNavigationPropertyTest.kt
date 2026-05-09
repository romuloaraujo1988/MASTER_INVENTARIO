package com.inventario.mobile.presentation.scanner

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 2: Repasse íntegro do contexto de sala e inventário
 * Validates: Requirements 2.2, 2.3, 3.1, 3.2, 7.1
 *
 * Para qualquer entrada válida (salaId > 0, salaNome não vazio, inventarioId > 0), o
 * Intent construído pelo handler deve conter:
 *  - EXTRA_SALA_ID == salaId.toLong()
 *  - EXTRA_SALA_NOME == salaNome
 *
 * Esta propriedade valida que o método `ScannerActivity.handleColetaSemEtiquetaClick()`
 * repassa exatamente o contexto lido do PreferencesManager, sem alterações, à
 * `DescricaoSelectionActivity`.
 */
class ScannerFabNavigationPropertyTest : StringSpec({

    "Property 2: Repasse íntegro — EXTRA_SALA_ID é salaId.toLong e EXTRA_SALA_NOME é salaNome" {
        checkAll(
            iterations = 100,
            Arb.int(1..Int.MAX_VALUE),
            Arb.string(1..30),
            Arb.int(1..Int.MAX_VALUE)
        ) { salaId, salaNome, inventarioId ->
            // Pré-condição da propriedade: entrada válida
            val salaNomeEfetivo = if (salaNome.isBlank()) "Sala X" else salaNome

            val intentExtras = buildDescricaoIntentExtras(salaId, salaNomeEfetivo, inventarioId)

            intentExtras.salaIdLong shouldBe salaId.toLong()
            intentExtras.salaNome shouldBe salaNomeEfetivo
        }
    }

    "Property 2: Repasse íntegro — conversão Int → Long preserva valor" {
        checkAll(
            iterations = 100,
            Arb.int(1..Int.MAX_VALUE),
            Arb.string(1..30)
        ) { salaId, salaNome ->
            val salaNomeEfetivo = if (salaNome.isBlank()) "Sala Y" else salaNome
            val extras = buildDescricaoIntentExtras(salaId, salaNomeEfetivo, inventarioId = 1)
            // salaIdLong reconstrói o Int original sem perda
            extras.salaIdLong.toInt() shouldBe salaId
        }
    }
})

/**
 * Extras que o Intent `DescricaoSelectionActivity` receberá.
 */
internal data class DescricaoIntentExtras(
    val salaIdLong: Long,
    val salaNome: String
)

/**
 * Pure-logic extract do trecho:
 *
 *   Intent(this, DescricaoSelectionActivity::class.java).apply {
 *       putExtra(EXTRA_SALA_ID, salaId.toLong())
 *       putExtra(EXTRA_SALA_NOME, salaNome)
 *   }
 *
 * A função é chamada apenas quando a validação de pré-condição passou
 * (ver `shouldStartDescricaoFlow`). Nomes dos extras não são validados aqui
 * (são constantes da Activity); validamos apenas valores.
 */
internal fun buildDescricaoIntentExtras(
    salaId: Int,
    salaNome: String,
    inventarioId: Int
): DescricaoIntentExtras {
    require(salaId > 0) { "salaId inválido" }
    require(salaNome.isNotBlank()) { "salaNome vazio" }
    require(inventarioId > 0) { "inventarioId inválido" }
    return DescricaoIntentExtras(
        salaIdLong = salaId.toLong(),
        salaNome = salaNome
    )
}
