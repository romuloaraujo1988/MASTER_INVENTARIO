package com.inventario.mobile.presentation.scanner.navigation

/**
 * Decisão pura de navegação do Menu_Atalhos da Tela_Coleta_Rapida (ScannerActivity).
 *
 * Recebe o contexto (Sala_Atual e referência opcional ao Item_Escaneado) e o atalho
 * acionado, devolvendo um [ShortcutNavigationPlan] determinístico. É Kotlin puro:
 * não toca em Android, `Intent`, rede ou `PreferencesManager` (Clean Architecture).
 */
class ColetaShortcutNavigator {

    /**
     * Resolve o plano de navegação para um atalho da Tela_Coleta_Rapida.
     *
     * - Se [sala] for inválida (`!sala.isValid`), retorna [ShortcutNavigationPlan.RequireSalaSelection]
     *   para qualquer atalho, independentemente de [itemEscaneado] (Requisito 5.1).
     * - Caso contrário, mapeia o atalho ao plano de navegação correspondente.
     *
     * @param shortcut atalho acionado pelo Coletor
     * @param sala contexto da Sala_Atual lido do PreferencesManager
     * @param itemEscaneado referência opcional ao último Item_Escaneado
     */
    fun resolve(
        shortcut: ColetaShortcut,
        sala: SalaContext,
        itemEscaneado: ScannedItemRef?
    ): ShortcutNavigationPlan {
        if (!sala.isValid) {
            return ShortcutNavigationPlan.RequireSalaSelection
        }
        return when (shortcut) {
            ColetaShortcut.SEM_ETIQUETA -> ShortcutNavigationPlan.NavigateToSemEtiqueta(
                salaParaPersistir = sala,
                descricaoSugerida = itemEscaneado?.descricaoOuFallback(),
                numeroPatrimonioOrigem = itemEscaneado?.numeroPatrimonio
            )
            ColetaShortcut.MANUAL -> ShortcutNavigationPlan.NavigateToManual(
                salaId = sala.salaId.toLong(),
                salaNome = sala.salaNome
            )
            ColetaShortcut.DESCRICAO -> ShortcutNavigationPlan.NavigateToDescricao(
                salaId = sala.salaId.toLong(),
                salaNome = sala.salaNome
            )
        }
    }
}
