package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.Index

/**
 * Entidade que representa um usuário no sistema
 */
// @Entity(
//     tableName = "usuario",
//     indices = [
//         Index(value = ["login"], unique = true),
//         Index(value = ["email"], unique = true)
//     ]
// )
data class Usuario(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nome: String,
    val login: String,
    val email: String,
    val senha: String? = null, // Não armazenar senha em produção
    val ativo: Boolean = true,
    val perfil: String, // ADMIN, COLETOR, VISUALIZADOR
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null
)
