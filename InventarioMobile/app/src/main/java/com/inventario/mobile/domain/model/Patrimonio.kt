package com.inventario.mobile.domain.model

// import androidx.room.Entity
// import androidx.room.PrimaryKey
// import androidx.room.ForeignKey
// import androidx.room.Index

/**
 * Entidade que representa um patrimônio no sistema
 */
// @Entity(
//     tableName = "patrimonio",
//     foreignKeys = [
//         ForeignKey(
//             entity = Setor::class,
//             parentColumns = ["id"],
//             childColumns = ["setorId"],
//             onDelete = ForeignKey.CASCADE
//         ),
//         ForeignKey(
//             entity = Sala::class,
//             parentColumns = ["id"],
//             childColumns = ["salaId"],
//             onDelete = ForeignKey.CASCADE
//         )
//     ],
//     indices = [
//         Index(value = ["setorId"]),
//         Index(value = ["salaId"]),
//         Index(value = ["numeroPatrimonio"], unique = true),
//         Index(value = ["qrCode"], unique = true)
//     ]
// )
data class Patrimonio(
    // @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val numeroPatrimonio: String,
    val descricao: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null, // ATIVO, INATIVO, BAIXADO
    val valor: Double? = null,
    val dataAquisicao: Long? = null,
    val observacoes: String? = null,
    
    // Relacionamentos
    val idSala: Int? = null,
    val nomeSala: String? = null,
    val idResponsavel: Int? = null,
    val nomeResponsavel: String? = null,
    val idSetor: Int? = null,
    val nomeSetor: String? = null,
    
    // QR Code
    val qrCode: String? = null,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null,
    
    // Campos para coleta
    val coletado: Boolean = false,
    val dataColeta: String? = null,
    val coletorId: Long? = null,
    val coletadoPor: String? = null,
    val dataColetaFormatada: String? = null,
    val localizacaoEncontrada: String? = null,
    val estadoEncontrado: String? = null
) {
    // Compatibilidade com código antigo
    @Deprecated("Use idSala", ReplaceWith("idSala"))
    val salaId: Long get() = idSala?.toLong() ?: 0L
    
    @Deprecated("Use idSetor", ReplaceWith("idSetor"))
    val setorId: Long get() = idSetor?.toLong() ?: 0L
}
