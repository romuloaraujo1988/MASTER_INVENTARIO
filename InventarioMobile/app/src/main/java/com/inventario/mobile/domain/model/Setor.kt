package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.Index

/**
 * Entidade que representa um setor no sistema
 */
// @Entity(
//     tableName = "setor",
//     indices = [
//         Index(value = ["nome"], unique = true),
//         Index(value = ["codigo"], unique = true)
//     ]
// )
data class Setor(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nome: String,
    val codigo: String,
    val descricao: String? = null,
    val ativo: Boolean = true,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null
)