package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.ForeignKey
// import androidx.room.Index

/**
 * Entidade que representa uma coleta de patrimônio
 */
// @Entity(
//     tableName = "coleta",
//     foreignKeys = [
//         ForeignKey(
//             entity = Patrimonio::class,
//             parentColumns = ["id"],
//             childColumns = ["patrimonioId"],
//             onDelete = ForeignKey.CASCADE
//         ),
//         ForeignKey(
//             entity = Usuario::class,
//             parentColumns = ["id"],
//             childColumns = ["usuarioId"],
//             onDelete = ForeignKey.CASCADE
//         )
//     ],
//     indices = [
//         Index(value = ["patrimonioId"]),
//         Index(value = ["usuarioId"]),
//         Index(value = ["dataColeta"])
//     ]
// )
data class Coleta(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Relacionamentos
    val patrimonioId: Long,
    val numeroPatrimonio: String? = null, // Número do patrimônio (pode ser null para coleta por descrição)
    val descricaoPatrimonio: String? = null, // Descrição do patrimônio (usado em coleta por descrição)
    val usuarioId: Long,
    val salaId: Int? = null, // ID da sala onde está coletando
    
    // Dados da coleta
    val dataColeta: Long = System.currentTimeMillis(),
    val localizacaoAtual: String? = null,
    val observacoes: String? = null,
    val fotoPath: String? = null,
    
    // Status da coleta
    val status: String, // COLETADO, PENDENTE, ERRO
    
    // Coordenadas GPS (opcional)
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null
)