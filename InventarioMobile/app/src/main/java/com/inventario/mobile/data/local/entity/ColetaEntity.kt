package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Coleta
 * Armazena coletas pendentes de sincronização
 * 
 * v2.1: Adicionados campos de métricas para análise de performance
 */
@Entity(
    tableName = "coleta",
    indices = [
        Index(value = ["idPatrimonio"]),
        Index(value = ["idInventario"]),
        Index(value = ["sincronizado"]),
        Index(value = ["dataColeta"]),
        Index(value = ["metodoColeta"]),
        Index(value = ["tipoScan"])
    ]
)
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idPatrimonio: Int,
    val numeroPatrimonio: String,
    val idInventario: Int,  // ID do inventário ativo (já existia no banco)
    val idSala: Int?,
    val nomeSala: String?,
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    val observacao: String?,
    val estadoPatrimonio: String?,
    val latitude: Double?,
    val longitude: Double?,
    val dataColeta: Long,
    val idUsuario: Int,
    val nomeUsuario: String,
    val sincronizado: Boolean = false,
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    val servidorId: Long? = null,  // ID da coleta no servidor após sincronização
    
    // ========== MÉTRICAS DE TEMPO (v2.1) ==========
    val tempoColetaSegundos: Int? = null,           // Tempo total da coleta
    val tempoScanSegundos: Int? = null,             // Tempo do scan (QR ou Barcode)
    val tempoPreenchimentoSegundos: Int? = null,    // Tempo de preenchimento
    
    // ========== MÉTRICAS DE MÉTODO (v2.1) ==========
    val metodoColeta: String? = null,               // QR_CODE, CODIGO_BARRAS, MANUAL, BUSCA, SEM_ETIQUETA
    val horaColeta: Int? = null,                    // Hora da coleta (0-23)
    val diaSemana: Int? = null,                     // Dia da semana (1=Dom, 7=Sab)
    val periodoColeta: String? = null,              // MANHA, TARDE, NOITE
    
    // ========== MÉTRICAS DE SCAN (v2.1) ==========
    val tipoScan: String? = null,                   // QR_CODE ou CODIGO_BARRAS
    val tentativasScan: Int = 1,                    // Número de tentativas
    val errosScan: Int = 0,                         // Número de erros
    val qualidadeEtiqueta: String? = null           // OTIMA, BOA, REGULAR, RUIM
)
