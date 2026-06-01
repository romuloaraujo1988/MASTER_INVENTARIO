package com.inventario.mobile.presentation.scanner.navigation

/**
 * Identifica o atalho do Menu_Atalhos acionado na Tela_Coleta_Rapida (ScannerActivity).
 *
 * Desacopla a lógica pura de navegação das classes `Activity` do Android, permitindo
 * que a decisão de navegação seja testável sem dependência do framework Android
 * (Clean Architecture — lógica pura).
 */
enum class ColetaShortcut {
    /** Atalho_Sem_Etiqueta -> ItemSemEtiquetaActivity */
    SEM_ETIQUETA,

    /** Atalho_Manual -> ManualCollectionActivity */
    MANUAL,

    /** Atalho_Descricao -> DescricaoSelectionActivity */
    DESCRICAO
}
