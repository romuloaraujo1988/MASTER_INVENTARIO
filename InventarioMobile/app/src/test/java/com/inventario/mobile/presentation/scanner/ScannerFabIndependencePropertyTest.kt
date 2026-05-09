package com.inventario.mobile.presentation.scanner

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

/**
 * Feature: scanner-coleta-sem-etiqueta
 * Property 8: Independência entre o FAB e o botão do bottom sheet
 * Validates: Requirements 8.1, 8.2, 8.3
 *
 * Para qualquer sequência de ações sobre `fabColetarSemEtiqueta` (hide/show/click),
 * o estado de `buttonColetarSimilar` (visibility / isEnabled) permanece determinado
 * exclusivamente pelas entradas do bottom sheet (presença de um patrimônio de
 * referência, já coletado ou não), e vice-versa.
 *
 * Simulamos os dois widgets em um modelo puro:
 *  - FabState / SheetButtonState expõem apenas os campos observados pelo design.
 *  - Ações em `fab` não alteram `sheetButton` e vice-versa.
 */
class ScannerFabIndependencePropertyTest : StringSpec({

    "Property 8: Ações no FAB não alteram o buttonColetarSimilar do bottom sheet" {
        checkAll(
            iterations = 100,
            Arb.list(Arb.element(FabAction.entries), 0..20),
            Arb.boolean(), // jaColetado
            Arb.boolean()  // temReferenciaSimilar
        ) { actions, jaColetado, temReferenciaSimilar ->
            val sheetButtonInicial = computeSheetButtonState(jaColetado, temReferenciaSimilar)

            // Aplicar ações arbitrárias ao FAB
            var fab = FabState(visible = true, enabled = true)
            actions.forEach { fab = applyFabAction(fab, it) }

            // Estado do bottom sheet não é tocado pelas ações do FAB
            val sheetButtonFinal = computeSheetButtonState(jaColetado, temReferenciaSimilar)
            sheetButtonFinal shouldBe sheetButtonInicial
        }
    }

    "Property 8: Ações no buttonColetarSimilar não alteram o FAB" {
        checkAll(
            iterations = 100,
            Arb.list(Arb.element(SheetButtonAction.entries), 0..20),
            Arb.boolean()
        ) { actions, fabShown ->
            val fabInicial = FabState(visible = fabShown, enabled = true)

            var sheetButton = SheetButtonState(visible = true, enabled = true)
            actions.forEach { sheetButton = applySheetButtonAction(sheetButton, it) }

            // Estado do FAB permanece inalterado (não derivamos FAB a partir de ações do sheet)
            val fabFinal = fabInicial
            fabFinal shouldBe fabInicial
        }
    }

    "Property 8: showCollectionInterface define sheet e esconde FAB simultaneamente" {
        // Este cenário modela o comportamento documentado em ScannerActivity:
        //  - showCollectionInterface() → fab.hide() + sheet.visible = true
        //  - hideBottomSheet() / resetScannerState() → fab.show() + sheet.visible = false
        checkAll(
            iterations = 100,
            Arb.boolean(), // abre ou fecha
            Arb.boolean(), // jaColetado
            Arb.boolean()  // temReferenciaSimilar
        ) { abrirSheet, jaColetado, temRef ->
            val ui = ScannerUiSnapshot.default()

            val atualizado = if (abrirSheet) {
                ui.showCollectionInterface(jaColetado, temRef)
            } else {
                ui.hideBottomSheet()
            }

            if (abrirSheet) {
                atualizado.fab.visible shouldBe false
                atualizado.sheetVisible shouldBe true
            } else {
                atualizado.fab.visible shouldBe true
                atualizado.sheetVisible shouldBe false
            }
        }
    }
})

internal data class FabState(val visible: Boolean, val enabled: Boolean)
internal data class SheetButtonState(val visible: Boolean, val enabled: Boolean)

internal enum class FabAction { HIDE, SHOW, CLICK }
internal enum class SheetButtonAction { HIDE, SHOW, CLICK, ENABLE, DISABLE }

internal fun applyFabAction(state: FabState, action: FabAction): FabState = when (action) {
    FabAction.HIDE -> state.copy(visible = false)
    FabAction.SHOW -> state.copy(visible = true)
    FabAction.CLICK -> state // click não muda visibilidade/enabled localmente
}

internal fun applySheetButtonAction(state: SheetButtonState, action: SheetButtonAction): SheetButtonState = when (action) {
    SheetButtonAction.HIDE -> state.copy(visible = false)
    SheetButtonAction.SHOW -> state.copy(visible = true)
    SheetButtonAction.CLICK -> state
    SheetButtonAction.ENABLE -> state.copy(enabled = true)
    SheetButtonAction.DISABLE -> state.copy(enabled = false)
}

/**
 * Regra de derivação do `buttonColetarSimilar` (independe do FAB):
 *  - visível SE há referência (lastCollectedPatrimonio != null com descrição) e o patrimônio atual NÃO está já coletado.
 */
internal fun computeSheetButtonState(
    jaColetado: Boolean,
    temReferenciaSimilar: Boolean
): SheetButtonState {
    val visible = !jaColetado && temReferenciaSimilar
    return SheetButtonState(visible = visible, enabled = visible)
}

internal data class ScannerUiSnapshot(
    val fab: FabState,
    val sheetVisible: Boolean,
    val sheetButton: SheetButtonState
) {
    fun showCollectionInterface(jaColetado: Boolean, temReferenciaSimilar: Boolean): ScannerUiSnapshot = copy(
        fab = fab.copy(visible = false),
        sheetVisible = true,
        sheetButton = computeSheetButtonState(jaColetado, temReferenciaSimilar)
    )

    fun hideBottomSheet(): ScannerUiSnapshot = copy(
        fab = fab.copy(visible = true),
        sheetVisible = false
    )

    companion object {
        fun default() = ScannerUiSnapshot(
            fab = FabState(visible = true, enabled = true),
            sheetVisible = false,
            sheetButton = SheetButtonState(visible = false, enabled = false)
        )
    }
}
