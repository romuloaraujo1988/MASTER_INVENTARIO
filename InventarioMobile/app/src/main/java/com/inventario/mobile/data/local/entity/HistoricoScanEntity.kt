package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity para armazenar histórico de patrimônios escaneados/consultados
 * 
 * Mantém os últimos 50 patrimônios acessados para consulta rápida
 * 
 * @since v2.11.0
 */
@Entity(
    tableName = "historico_scan",
    indices = [
        Index(value = ["numeroPatrimonio"]),
        Index(value = ["timestamp"])
    ]
)
data class HistoricoScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Número do patrimônio escaneado */
    val numeroPatrimonio: String,
    
    /** Descrição do patrimônio */
    val descricao: String?,
    
    /** Nome da sala onde está o patrimônio */
    val nomeSala: String?,
    
    /** ID da sala */
    val salaId: Int?,
    
    /** Se foi coletado neste acesso */
    val foiColetado: Boolean = false,
    
    /** Tipo de acesso: SCAN_QR, BUSCA_MANUAL, CONSULTA */
    val tipoAcesso: String = "SCAN_QR",
    
    /** Timestamp do acesso */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Estado do patrimônio no momento do scan */
    val estadoPatrimonio: String? = null,
    
    /** Se o patrimônio já estava coletado antes deste acesso */
    val jaEstaColetado: Boolean = false
)
