package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidade Room que representa um patrimônio
 */
@Entity(
    tableName = "patrimonio",
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["salaId"]),
        Index(value = ["qrCode"], unique = true),
        Index(value = ["descricao"]),
        Index(value = ["coletado"]),
        Index(value = ["sincronizado"]),
        Index(value = ["servidorId"]),
        // Índices compostos para queries frequentes
        Index(value = ["salaId", "coletado"]),
        Index(value = ["coletado", "sincronizado"])
    ]
)
data class PatrimonioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Dados básicos
    val codigo: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: Double? = null,
    
    // Relacionamentos
    val salaId: Long? = null,
    val salaNome: String? = null,
    
    // QR Code
    val qrCode: String? = null,
    
    // Status de coleta
    val coletado: Boolean = false,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor
    val servidorId: Long? = null
)