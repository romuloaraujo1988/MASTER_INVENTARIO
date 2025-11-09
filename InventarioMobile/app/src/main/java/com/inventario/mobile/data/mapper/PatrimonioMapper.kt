package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.PatrimonioEntity
import com.inventario.mobile.domain.model.Patrimonio

/**
 * Mapper: Converte entre Entity (Room) e Model (Domain)
 */
object PatrimonioMapper {
    
    fun toDomain(entity: PatrimonioEntity): Patrimonio {
        return Patrimonio(
            id = entity.id.toLong(),
            numeroPatrimonio = entity.numero,
            descricao = entity.descricao,
            estado = entity.status,
            setorId = 0L, // TODO: Adicionar setorId na entity se necessário
            salaId = entity.idSala?.toLong() ?: 0L,
            qrCode = entity.numero, // Usando número como QR code temporariamente
            coletado = entity.coletado
        )
    }
    
    fun toEntity(domain: Patrimonio, coletado: Boolean = false): PatrimonioEntity {
        return PatrimonioEntity(
            id = domain.id.toInt(),
            numero = domain.numeroPatrimonio,
            descricao = domain.descricao,
            idSala = domain.salaId.toInt(),
            nomeSala = null, // Será preenchido por join se necessário
            idResponsavel = null, // TODO: Adicionar se necessário
            nomeResponsavel = null,
            status = domain.estado,
            coletado = coletado
        )
    }
    
    fun toDomainList(entities: List<PatrimonioEntity>): List<Patrimonio> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(domains: List<Patrimonio>): List<PatrimonioEntity> {
        return domains.map { toEntity(it) }
    }
}
