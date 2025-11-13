package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room para histórico de sincronizações
 * Armazena logs de execuções de sincronização automática
 */
@Entity(tableName = "sync_log")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val tipo: String,                    // AUTO, MANUAL, BY_COUNT
    val dataHora: Long,                  // Timestamp da execução
    val status: String,                  // IN_PROGRESS, SUCCESS, ERROR
    val mensagem: String,                // Mensagem descritiva
    
    val coletasSincronizadas: Int = 0,  // Quantidade de coletas sincronizadas
    val coletasFalhadas: Int = 0,       // Quantidade de coletas que falharam
    
    val stackTrace: String? = null,      // Stack trace em caso de erro
    val duracao: Long = 0                // Duração em milissegundos
)
