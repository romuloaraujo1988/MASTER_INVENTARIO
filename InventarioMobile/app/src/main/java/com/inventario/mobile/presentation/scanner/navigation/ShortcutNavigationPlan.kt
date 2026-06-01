package com.inventario.mobile.presentation.scanner.navigation

/**
 * Plano de navegação resultante da decisão pura do `ColetaShortcutNavigator`.
 *
 * A View (ScannerActivity) apenas executa o plano retornado, mantendo toda
 * interação com `Intent`, `PreferencesManager` e UI fora desta lógica pura.
 */
sealed class ShortcutNavigationPlan {

    /** Sala_Atual ausente: exibir aviso e permanecer na tela (Requisito 5.1/5.2). */
    object RequireSalaSelection : ShortcutNavigationPlan()

    /**
     * Navegar para coleta sem etiqueta, persistindo a sala e (opcionalmente)
     * extras do scan.
     */
    data class NavigateToSemEtiqueta(
        val salaParaPersistir: SalaContext,
        val descricaoSugerida: String?,
        val numeroPatrimonioOrigem: String?
    ) : ShortcutNavigationPlan()

    /** Navegar para coleta manual com extras de sala (id em Long). */
    data class NavigateToManual(
        val salaId: Long,
        val salaNome: String
    ) : ShortcutNavigationPlan()

    /** Navegar para coleta por descrição com extras de sala (id em Long). */
    data class NavigateToDescricao(
        val salaId: Long,
        val salaNome: String
    ) : ShortcutNavigationPlan()
}
