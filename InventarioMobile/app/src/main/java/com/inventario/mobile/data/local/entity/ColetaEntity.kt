package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Coleta
 * Armazena coletas pendentes de sincronização
 */
@Entity(
    tableName = "coleta",
    indices = [
        Index(value = ["idPatrimonio"]),
        Index(value = ["idInventario"]),
        Index(value = ["sincronizado"]),
        Index(value = ["dataColeta"])
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
    val servidorId: Long? = null  // ID da coleta no servidor após sincronização
)
