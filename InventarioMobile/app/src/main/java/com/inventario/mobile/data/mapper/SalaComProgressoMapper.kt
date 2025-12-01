package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.SalaComEstatisticasEntity
import com.inventario.mobile.domain.model.SalaComProgresso

/**
 * Mapper para converter SalaComEstatisticasEntity para SalaComProgresso (domain model).
 */
object SalaComProgressoMapper {
    
    /**
     * Converte uma SalaComEstatisticasEntity para SalaComProgresso.
     * Calcula pendentes e percentual automaticamente.
     */
    fun toDomain(entity: SalaComEstatisticasEntity): SalaComProgresso {
        val pendentes = entity.totalPatrimonios - entity.coletados
        val percentual = if (entity.totalPatrimonios > 0) {
            (entity.coletados.toFloat() / entity.totalPatrimonios) * 100f
        } else {
            0f
        }
        
        return SalaComProgresso(
            id = entity.sala.id,
            nome = entity.sala.nome,
            numero = null, // SalaEntity não tem campo numero, usar id como fallback se necessário
            totalPatrimonios = entity.totalPatrimonios,
            coletados = entity.coletados,
            pendentes = pendentes,
            percentualColeta = percentual
        )
    }
    
    /**
     * Converte uma lista de SalaComEstatisticasEntity para lista de SalaComProgresso.
     */
    fun toDomainList(entities: List<SalaComEstatisticasEntity>): List<SalaComProgresso> {
        return entities.map { toDomain(it) }
    }
}

/**
 * Função de extensão para converter SalaComEstatisticasEntity para SalaComProgresso.
 */
fun SalaComEstatisticasEntity.toDomain(): SalaComProgresso {
    return SalaComProgressoMapper.toDomain(this)
}

/**
 * Função de extensão para converter lista de SalaComEstatisticasEntity para lista de SalaComProgresso.
 */
fun List<SalaComEstatisticasEntity>.toDomainList(): List<SalaComProgresso> {
    return SalaComProgressoMapper.toDomainList(this)
}
