package com.inventario.mobile.presentation.scanner.navigation

/**
 * Contexto da Sala_Atual lido do PreferencesManager.
 *
 * `salaId == 0` (ou negativo) ou `salaNome` em branco significam "nenhuma sala selecionada".
 * Modelo puro (Kotlin, sem dependência de Android).
 *
 * @property salaId identificador da Sala_Atual (Int; `0` significa nenhuma sala)
 * @property salaNome nome da Sala_Atual (vazio significa nenhuma sala)
 */
data class SalaContext(
    val salaId: Int,
    val salaNome: String
) {
    /** Sala válida exige id > 0 E nome não vazio (Requisito 5.1). */
    val isValid: Boolean
        get() = salaId > 0 && salaNome.isNotBlank()
}
