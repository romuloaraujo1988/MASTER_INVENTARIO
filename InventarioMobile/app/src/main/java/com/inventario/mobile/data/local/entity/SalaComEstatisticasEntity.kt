package com.inventario.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Entity para resultado de query que retorna sala com estatísticas de coleta.
 * Usado em queries com JOIN para obter contagem de patrimônios.
 * 
 * Não é uma tabela do banco, apenas um POJO para resultado de query.
 */
data class SalaComEstatisticasEntity(
    /**
     * Dados da sala (embedded)
     */
    @Embedded
    val sala: SalaEntity,
    
    /**
     * Total de patrimônios alocados na sala
     */
    @ColumnInfo(name = "total_patrimonios")
    val totalPatrimonios: Int,
    
    /**
     * Quantidade de patrimônios já coletados
     */
    @ColumnInfo(name = "coletados")
    val coletados: Int
)
