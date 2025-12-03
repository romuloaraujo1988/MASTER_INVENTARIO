package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidade Room que representa um setor
 */
@Entity(
    tableName = "setor",
    indices = [
        Index(value = ["nome"])
    ]
)
data class SetorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Dados básicos
    val nome: String,
    val descricao: String? = null,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor
    val servidorId: Long? = null
)
