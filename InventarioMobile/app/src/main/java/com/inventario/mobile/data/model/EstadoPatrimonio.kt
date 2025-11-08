package com.inventario.mobile.data.model

/**
 * Enum que representa os possíveis estados de um patrimônio durante a coleta.
 * Os valores devem ser armazenados em UPPERCASE conforme requisito do sistema.
 */
enum class EstadoPatrimonio(val descricao: String) {
    BOM("Bom"),
    OCIOSO("Ocioso"),
    ANTIECONOMICO("Antieconômico"),
    RECUPERAVEL("Recuperável"),
    IRRECUPERAVEL("Irrecuperável");

    companion object {
        fun fromString(value: String): EstadoPatrimonio? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
