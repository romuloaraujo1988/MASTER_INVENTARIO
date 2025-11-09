package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Responsável
 */
@Entity(
    tableName = "responsavel",
    indices = [Index(value = ["nome"])]
)
data class ResponsavelEntity(
    @PrimaryKey
    val id: Int,
    val nome: String,
    val cpf: String?,
    val email: String?,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
