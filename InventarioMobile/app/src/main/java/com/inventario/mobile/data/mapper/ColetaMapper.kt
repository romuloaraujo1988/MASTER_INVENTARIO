package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.domain.model.Coleta

/**
 * Mapper: Converte entre Entity (Room) e Model (Domain)
 */
object ColetaMapper {
    
    fun toDomain(entity: ColetaEntity): Coleta {
        return Coleta(
            id = entity.id,
            patrimonioId = entity.idPatrimonio.toLong(),
            usuarioId = entity.idUsuario.toLong(),
            dataColeta = entity.dataColeta,
            localizacaoAtual = entity.nomeSala,
            observacoes = entity.observacao,
            status = "COLETADO", // Status padrão
            latitude = entity.latitude,
            longitude = entity.longitude,
            sincronizado = entity.sincronizado
        )
    }
    
    fun toEntity(domain: Coleta): ColetaEntity {
        return ColetaEntity(
            id = domain.id,
            idPatrimonio = domain.patrimonioId.toInt(),
            numeroPatrimonio = "", // Será preenchido pelo repositório
            idSala = null, // Será preenchido se necessário
            nomeSala = domain.localizacaoAtual,
            idResponsavel = null,
            nomeResponsavel = null,
            observacao = domain.observacoes,
            estadoPatrimonio = null,
            latitude = domain.latitude,
            longitude = domain.longitude,
            dataColeta = domain.dataColeta,
            idUsuario = domain.usuarioId.toInt(),
            nomeUsuario = "", // Será preenchido pelo repositório
            sincronizado = domain.sincronizado
        )
    }
    
    fun toDomainList(entities: List<ColetaEntity>): List<Coleta> {
        return entities.map { toDomain(it) }
    }
}
