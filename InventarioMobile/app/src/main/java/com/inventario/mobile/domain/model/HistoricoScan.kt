package com.inventario.mobile.domain.model

/**
 * Modelo de domínio para histórico de scans
 * 
 * @since v2.11.0
 */
data class HistoricoScan(
    val id: Long = 0,
    val numeroPatrimonio: String,
    val descricao: String?,
    val nomeSala: String?,
    val salaId: Int?,
    val foiColetado: Boolean = false,
    val tipoAcesso: TipoAcesso = TipoAcesso.SCAN_QR,
    val timestamp: Long = System.currentTimeMillis(),
    val estadoPatrimonio: String? = null,
    val jaEstaColetado: Boolean = false
) {
    /**
     * Retorna tempo relativo desde o scan
     * Ex: "Agora", "5 min", "1h", "Ontem"
     */
    fun getTempoRelativo(): String {
        val agora = System.currentTimeMillis()
        val diff = agora - timestamp
        
        val segundos = diff / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24
        
        return when {
            segundos < 60 -> "Agora"
            minutos < 60 -> "${minutos}min"
            horas < 24 -> "${horas}h"
            dias == 1L -> "Ontem"
            dias < 7 -> "${dias}d"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
    
    /**
     * Retorna data/hora formatada
     */
    fun getDataHoraFormatada(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

/**
 * Tipo de acesso ao patrimônio
 */
enum class TipoAcesso {
    SCAN_QR,        // Escaneou QR Code
    BUSCA_MANUAL,   // Digitou número manualmente
    CONSULTA        // Apenas consultou detalhes
}
