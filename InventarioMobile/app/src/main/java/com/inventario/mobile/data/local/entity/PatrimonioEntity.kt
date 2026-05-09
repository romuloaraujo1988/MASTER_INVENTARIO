package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity Room para Patrimônio
 * Armazena dados offline para coleta sem conexão
 */
@Entity(
    tableName = "patrimonio",
    indices = [
        // Índices únicos
        Index(value = ["numero"], unique = true),
        
        // Índices simples para busca rápida
        Index(value = ["numeroPatrimonio"]),
        Index(value = ["descricao"]),
        Index(value = ["idSala"]),
        Index(value = ["coletado"]),
        Index(value = ["nomeSala"]),
        Index(value = ["nomeResponsavel"]),
        
        // Índices compostos para filtros de busca (otimização)
        Index(value = ["coletado", "numeroPatrimonio"]),
        Index(value = ["coletado", "descricao"]),
        Index(value = ["coletado", "nomeSala"]),
        Index(value = ["idSala", "coletado"])
    ]
)
data class PatrimonioEntity(
    @PrimaryKey
    val id: Long,
    val numero: String,
    val numeroPatrimonio: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val estado: String? = null,
    val valor: Double? = null,
    val setorId: Int? = null,
    val setorNome: String? = null,
    val idSala: Int? = null,
    val nomeSala: String? = null,
    val idResponsavel: Int? = null,
    val nomeResponsavel: String? = null,
    val status: String? = null,
    val coletado: Boolean = false,
    val dataColeta: Long? = null,
    val coletadoPor: String? = null,
    val observacoesColeta: String? = null,
    val observacoes: String? = null,
    // v2.20.7: campos de auditoria da coleta — persistidos no patrimônio
    // para sobreviverem à limpeza de coletas sincronizadas (limparSincronizadas).
    // Assim o relatório exportado continua mostrando localização/estado mesmo
    // após a coleta ter sido apagada do banco local.
    val localizacaoEncontrada: String? = null,
    val estadoEncontrado: String? = null,
    val dataUltimaAtualizacao: Long = System.currentTimeMillis()
)
