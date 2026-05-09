package com.inventario.mobile.presentation.descricao

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 4: Origem do estado de conservação
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4
 *
 * For any (estadoFixoEnabled, estadoFixo, estadoSelecionadoNoDialog):
 *   - Se estadoFixoEnabled == true E estadoFixo não é nulo nem blank,
 *     o parâmetro estadoConservacao passado ao ViewModel é IGUAL a estadoFixo.
 *   - Caso contrário, o parâmetro é IGUAL a estadoSelecionadoNoDialog.
 *
 * Esta propriedade espelha a lógica que `DescricaoSelectionActivity.showConfirmacaoColetaDialog`
 * aplica no callback do botão "Coletar" e o que `showEstadoDialogParaDescricao` faz no retorno
 * do EstadoPatrimonioDialog.
 */
class DescricaoSelectionStatePropertyTest : StringSpec({

    "Property 4: Origem do estado — usa estadoFixo quando habilitado e válido" {
        checkAll(
            iterations = 100,
            Arb.boolean(),
            Arb.string(0..15).orNull(),
            Arb.string(1..15) // estado selecionado via dialog é sempre não-vazio
        ) { enabled, fixo, selecionadoDialog ->
            val estadoUsado = resolveEstadoConservacao(
                estadoFixoEnabled = enabled,
                estadoFixo = fixo,
                estadoSelecionadoNoDialog = selecionadoDialog
            )

            val deveUsarFixo = enabled && !fixo.isNullOrBlank()
            val esperado = if (deveUsarFixo) fixo!! else selecionadoDialog

            estadoUsado shouldBe esperado
        }
    }

    "Property 4: Origem do estado — quando desabilitado, sempre usa o do dialog" {
        checkAll(
            iterations = 100,
            Arb.string(0..15).orNull(),
            Arb.string(1..15)
        ) { fixo, selecionadoDialog ->
            val estadoUsado = resolveEstadoConservacao(
                estadoFixoEnabled = false,
                estadoFixo = fixo,
                estadoSelecionadoNoDialog = selecionadoDialog
            )
            estadoUsado shouldBe selecionadoDialog
        }
    }

    "Property 4: Origem do estado — habilitado com fixo blank/null cai no dialog" {
        checkAll(
            iterations = 100,
            Arb.string(1..15)
        ) { selecionadoDialog ->
            // estadoFixo == null
            resolveEstadoConservacao(
                estadoFixoEnabled = true,
                estadoFixo = null,
                estadoSelecionadoNoDialog = selecionadoDialog
            ) shouldBe selecionadoDialog

            // estadoFixo == ""  (blank)
            resolveEstadoConservacao(
                estadoFixoEnabled = true,
                estadoFixo = "",
                estadoSelecionadoNoDialog = selecionadoDialog
            ) shouldBe selecionadoDialog

            // estadoFixo == "   " (whitespace)
            resolveEstadoConservacao(
                estadoFixoEnabled = true,
                estadoFixo = "   ",
                estadoSelecionadoNoDialog = selecionadoDialog
            ) shouldBe selecionadoDialog
        }
    }
})

/**
 * Pure-logic extract de:
 *
 *   if (estadoFixoHabilitado && !estadoFixo.isNullOrBlank()) {
 *       viewModel.registrarColetaPorDescricao(..., estadoConservacao = estadoFixo)
 *   } else {
 *       showEstadoDialogParaDescricao(descricao) // o dialog devolve o selecionado
 *   }
 */
internal fun resolveEstadoConservacao(
    estadoFixoEnabled: Boolean,
    estadoFixo: String?,
    estadoSelecionadoNoDialog: String
): String {
    return if (estadoFixoEnabled && !estadoFixo.isNullOrBlank()) {
        estadoFixo
    } else {
        estadoSelecionadoNoDialog
    }
}
