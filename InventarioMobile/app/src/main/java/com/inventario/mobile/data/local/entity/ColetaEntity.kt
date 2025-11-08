package com.inventario.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidade Room que representa uma coleta de patrimônio
 */
@Entity(
    tableName = "coleta",
    indices = [
        Index(value = ["patrimonioId"]),
        Index(value = ["usuarioId"]),
        Index(value = ["dataColeta"]),
        Index(value = ["sincronizado"]),
        Index(value = ["status"]),
        Index(value = ["servidorId"]),
        // Índices compostos para queries frequentes
        Index(value = ["usuarioId", "sincronizado"]),
        Index(value = ["usuarioId", "dataColeta"]),
        Index(value = ["sincronizado", "dataColeta"])
    ]
)
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Relacionamentos
    val patrimonioId: Long,
    val usuarioId: Long,
    
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
    val sincronizada: Boolean = false, // Alias para compatibilidade
    val dataCriacao: Long = System.currentTimeMillis(),
    val dataAtualizacao: Long = System.currentTimeMillis(),
    
    // ID do servidor (quando sincronizado)
    val servidorId: Long? = null,
    
    // Controle de tentativas de sincronização
    val tentativasSincronizacao: Int = 0,
    val ultimaTentativaSincronizacao: Long? = null,
    val erroSincronizacao: String? = null
)