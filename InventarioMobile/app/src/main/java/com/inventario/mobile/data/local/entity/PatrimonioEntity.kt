package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Patrimônio
 * Armazena dados offline para coleta sem conexão
 */
@Entity(
    tableName = "patrimonio",
    indices = [
        Index(value = ["numero"], unique = true),
        Index(value = ["descricao"]),
        Index(value = ["idSala"]),
        Index(value = ["coletado"])
    ]
)
data class PatrimonioEntity(
    @PrimaryKey
    val id: Int,
    val numero: String,
    val descricao: String,
    val idSala: Int?,
    val nomeSala: String?,
    val idResponsavel: Int?,
    val nomeResponsavel: String?,
    val status: String,
    val coletado: Boolean = false,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
