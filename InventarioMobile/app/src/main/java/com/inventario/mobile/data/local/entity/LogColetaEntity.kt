package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity para log de auditoria de coletas
 * 
 * CRÍTICO: Rastreabilidade completa de todas as operações
 * Permite investigar problemas e recuperar dados perdidos
 * 
 * v2.2: Implementação de auditoria completa
 */
@Entity(
    tableName = "log_coleta",
    indices = [
        Index(value = ["coletaId"]),
        Index(value = ["acao"]),
        Index(value = ["timestamp"]),
        Index(value = ["usuarioId"])
    ]
)
data class LogColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val coletaId: Long,              // ID da coleta relacionada
    val acao: String,                // CRIADA, SINCRONIZADA, ERRO_SYNC, EXPORTADA, RESTAURADA
    val timestamp: Long,             // Quando aconteceu
    val detalhes: String?,           // Informações adicionais
    val usuarioId: Int,              // Quem executou a ação
    val nomeUsuario: String,         // Nome do usuário
    
    // Dados do dispositivo
    val deviceId: String?,           // Identificador do dispositivo
    val appVersion: String?,         // Versão do app
    
    // Dados de rede (para sync)
    val tipoRede: String?,           // WIFI, MOBILE, OFFLINE
    val qualidadeRede: String?,      // EXCELENTE, BOA, REGULAR, RUIM
    
    // Resultado da operação
    val sucesso: Boolean = true,     // Se a operação foi bem-sucedida
    val mensagemErro: String? = null // Mensagem de erro se falhou
)

/**
 * Tipos de ação para log
 */
object AcaoLog {
    const val CRIADA = "CRIADA"
    const val VALIDADA = "VALIDADA"
    const val SINCRONIZADA = "SINCRONIZADA"
    const val ERRO_SYNC = "ERRO_SYNC"
    const val TENTATIVA_SYNC = "TENTATIVA_SYNC"
    const val EXPORTADA = "EXPORTADA"
    const val RESTAURADA = "RESTAURADA"
    const val BACKUP_CRIADO = "BACKUP_CRIADO"
    const val DUPLICATA_DETECTADA = "DUPLICATA_DETECTADA"
    const val VALIDACAO_FALHOU = "VALIDACAO_FALHOU"
}
