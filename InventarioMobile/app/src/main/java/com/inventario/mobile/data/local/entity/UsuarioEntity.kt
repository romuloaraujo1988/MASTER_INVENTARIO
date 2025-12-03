package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidade Room que representa um usuário
 */
@Entity(
    tableName = "usuario",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Dados básicos
    val nome: String,
    val email: String,
    val senha: String? = null, // Pode ser null para usuários sincronizados
    val ativo: Boolean = true,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor
    val servidorId: Long? = null
)
