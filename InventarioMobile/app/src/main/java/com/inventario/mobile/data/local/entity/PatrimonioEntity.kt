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
        Index(value = ["numeroPatrimonio"]),
        Index(value = ["descricao"]),
        Index(value = ["idSala"]),
        Index(value = ["coletado"])
    ]
)
data class PatrimonioEntity(
    @PrimaryKey
    val id: Long,
    val numero: String,
    val numeroPatrimonio: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: Double? = null,
    val setorId: Int? = null,
    val setorNome: String? = null,
    val idSala: Int? = null,
    val nomeSala: String? = null,
    val salaId: Int? = null,
    val salaNome: String? = null,
    val idResponsavel: Int? = null,
    val nomeResponsavel: String? = null,
    val responsavelId: Int? = null,
    val responsavelNome: String? = null,
    val status: String? = null,
    val coletado: Boolean = false,
    val dataColeta: Long? = null,
    val coletadoPor: String? = null,
    val observacoesColeta: String? = null,
    val observacoes: String? = null,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
