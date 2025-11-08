package com.inventario.mobile.data.model

/**
 * Representa o status de sincronização de um patrimônio
 */
data class SyncStatus(
    val patrimonioId: Long,
    val coletadoLocal: Boolean,
    val sincronizado: Boolean,
    val coletaLocal: Coleta? = null
) {
    /**
     * Indica se há inconsistência na sincronização
     */
    val temInconsistencia: Boolean
        get() = coletadoLocal && !sincronizado
    
    /**
     * Descrição do status atual
     */
    val descricaoStatus: String
        get() = when {
            !coletadoLocal -> "Não coletado"
            coletadoLocal && sincronizado -> "Coletado e sincronizado"
            coletadoLocal && !sincronizado -> "Coletado mas não sincronizado"
            else -> "Status desconhecido"
        }
}