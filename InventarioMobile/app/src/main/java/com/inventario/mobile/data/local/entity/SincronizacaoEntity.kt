package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room para Sincronização
 * Armazena operações pendentes de sincronização com o servidor
 */
@Entity(tableName = "sincronizacao")
data class SincronizacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entidade: String,           // "coleta", "patrimonio", etc
    val entidadeId: Long,            // ID da entidade
    val operacao: String,            // "INSERT", "UPDATE", "DELETE"
    val sincronizado: Boolean = false,
    val dataHora: Long,              // Timestamp da operação
    val erro: String? = null         // Mensagem de erro se falhou
)
