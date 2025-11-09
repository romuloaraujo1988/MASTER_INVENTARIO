package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Sala
 */
@Entity(
    tableName = "sala",
    indices = [Index(value = ["nome"])]
)
data class SalaEntity(
    @PrimaryKey
    val id: Int,
    val nome: String,
    val idSetor: Int?,
    val nomeSetor: String?,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
