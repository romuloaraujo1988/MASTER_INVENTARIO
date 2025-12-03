package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.ForeignKey
// import androidx.room.Index

/**
 * Entidade que representa uma sala no sistema
 */
// @Entity(
//     tableName = "sala",
//     foreignKeys = [
//         ForeignKey(
//             entity = Setor::class,
//             parentColumns = ["id"],
//             childColumns = ["setorId"],
//             onDelete = ForeignKey.CASCADE
//         )
//     ],
//     indices = [
//         Index(value = ["setorId"]),
//         Index(value = ["nome", "setorId"], unique = true),
//         Index(value = ["codigo"], unique = true)
//     ]
// )
data class Sala(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nome: String,
    val codigo: String,
    val descricao: String? = null,
    val ativo: Boolean = true,
    
    // Relacionamento
    val setorId: Long,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null
)
