package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.SalaEntity
import com.inventario.mobile.domain.model.Sala

/**
 * Mapper: Converte entre Entity (Room) e Model (Domain)
 */
object SalaMapper {
    
    fun toDomain(entity: SalaEntity): Sala {
        return Sala(
            id = entity.id.toLong(),
            nome = entity.nome,
            codigo = entity.id.toString(), // Usando ID como código temporariamente
            setorId = entity.idSetor?.toLong() ?: 0L
        )
    }
    
    fun toEntity(domain: Sala): SalaEntity {
        return SalaEntity(
            id = domain.id.toInt(),
            nome = domain.nome,
            idSetor = domain.setorId.toInt(),
            nomeSetor = null // Será preenchido por join se necessário
        )
    }
    
    fun toDomainList(entities: List<SalaEntity>): List<Sala> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(domains: List<Sala>): List<SalaEntity> {
        return domains.map { toEntity(it) }
    }
}
