package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidade Room que representa uma sala
 */
@Entity(
    tableName = "sala",
    foreignKeys = [
        ForeignKey(
            entity = SetorEntity::class,
            parentColumns = ["id"],
            childColumns = ["setorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["nome"]),
        Index(value = ["setorId"])
    ]
)
data class SalaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Dados básicos
    val nome: String,
    val descricao: String? = null,
    
    // Relacionamento
    val setorId: Long,
    val setorNome: String? = null,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor
    val servidorId: Long? = null
)