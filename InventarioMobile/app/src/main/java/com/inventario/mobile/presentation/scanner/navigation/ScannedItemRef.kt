package com.inventario.mobile.presentation.scanner.navigation

/**
 * Referência ao último Item_Escaneado disponível na Tela_Coleta_Rapida.
 *
 * Espelha apenas os campos relevantes do `ScanResult` para a navegação dos atalhos.
 * Modelo puro (Kotlin, sem dependência de Android).
 *
 * @property numeroPatrimonio número do patrimônio escaneado (pode ser nulo/vazio)
 * @property descricao descrição do patrimônio escaneado (pode ser nula/vazia)
 */
data class ScannedItemRef(
    val numeroPatrimonio: String?,
    val descricao: String?
) {
    /**
     * Descrição sugerida com fallback baseado no número
     * (espelha o fluxo de duplicata existente):
     * - descrição não vazia -> descrição;
     * - senão número não vazio -> `"Patrimônio {numero}"`;
     * - senão -> `null`.
     */
    fun descricaoOuFallback(): String? = when {
        !descricao.isNullOrBlank() -> descricao
        !numeroPatrimonio.isNullOrBlank() -> "Patrimônio $numeroPatrimonio"
        else -> null
    }
}
