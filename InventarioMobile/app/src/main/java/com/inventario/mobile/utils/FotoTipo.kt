package com.inventario.mobile.utils

/**
 * Tipo de coleta que originou a foto.
 *
 * Determina a subpasta usada tanto no dispositivo Android quanto no servidor:
 *
 * | Tipo          | Subpasta        | Quando usar                              |
 * |---------------|-----------------|------------------------------------------|
 * | PATRIMONIO    | patrimonio/     | Coleta normal com etiqueta               |
 * | SEM_ETIQUETA  | sem_etiqueta/   | Item encontrado sem etiqueta             |
 * | DIVERGENCIA   | divergencia/    | Coleta com divergência detectada         |
 *
 * @see PhotoHelper
 * @see FotoColetaApi
 */
enum class FotoTipo(val pasta: String) {
    PATRIMONIO("patrimonio"),
    SEM_ETIQUETA("sem_etiqueta"),
    DIVERGENCIA("divergencia");

    companion object {
        /**
         * Resolve o tipo a partir de flags da coleta.
         * Prioridade: divergência > sem etiqueta > patrimônio normal.
         */
        fun resolver(semEtiqueta: Boolean, divergencia: Boolean): FotoTipo = when {
            divergencia   -> DIVERGENCIA
            semEtiqueta   -> SEM_ETIQUETA
            else          -> PATRIMONIO
        }

        /** Converte string (vinda do servidor/banco) de volta para enum. */
        fun fromPasta(pasta: String): FotoTipo =
            values().firstOrNull { it.pasta == pasta } ?: PATRIMONIO
    }
}
