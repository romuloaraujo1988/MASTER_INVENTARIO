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
    
    // Estado de conservação do patrimônio conforme legislação (BOM, OCIOSO, RECUPERÁVEL, ANTIECONÔMICO, IRRECUPERÁVEL)
    val estadoEncontrado: String? = null,
    
    // Coordenadas GPS (opcional)
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Controle de sincronização
    val sincronizado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null,
    
    // v2.6: Diagnóstico de erros de sincronização
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    
    // v2.7: Campos para itens sem etiqueta
    val semEtiqueta: Boolean = false,
    val descricaoItemSemEtiqueta: String? = null,
    val categoriaItemSemEtiqueta: String? = null,
    
    // v2.1: Métricas de Tempo
    val tempoColetaSegundos: Int? = null,
    val tempoScanSegundos: Int? = null,
    val tempoPreenchimentoSegundos: Int? = null,
    val metodoColeta: String? = null,
    val tipoScan: String? = null,

    // v2.13: Divergência automática
    val divergencia: Boolean = false,
    val motivoDivergencia: String? = null,  // LOCALIZACAO_DIFERENTE, ESTADO_DIFERENTE, LOCALIZACAO_E_ESTADO

    // v2.20.12: Local onde o item foi REALMENTE encontrado durante a coleta.
    // Distinto de `localizacaoAtual` (que é a sala de origem do patrimônio).
    val localizacaoEncontrada: String? = null
)
