package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.HistoricoScanEntity
import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.model.TipoAcesso
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper para conversão entre Entity e Domain model do histórico
 * 
 * @since v2.11.0
 */
@Singleton
class HistoricoScanMapper @Inject constructor() {
    
    /**
     * Converte Entity para Domain
     */
    fun toDomain(entity: HistoricoScanEntity): HistoricoScan {
        return HistoricoScan(
            id = entity.id,
            numeroPatrimonio = entity.numeroPatrimonio,
            descricao = entity.descricao,
            nomeSala = entity.nomeSala,
            salaId = entity.salaId,
            foiColetado = entity.foiColetado,
            tipoAcesso = try {
                TipoAcesso.valueOf(entity.tipoAcesso)
            } catch (e: Exception) {
                TipoAcesso.SCAN_QR
            },
            timestamp = entity.timestamp,
            estadoPatrimonio = entity.estadoPatrimonio,
            jaEstaColetado = entity.jaEstaColetado
        )
    }
    
    /**
     * Converte Domain para Entity
     */
    fun toEntity(domain: HistoricoScan): HistoricoScanEntity {
        return HistoricoScanEntity(
            id = domain.id,
            numeroPatrimonio = domain.numeroPatrimonio,
            descricao = domain.descricao,
            nomeSala = domain.nomeSala,
            salaId = domain.salaId,
            foiColetado = domain.foiColetado,
            tipoAcesso = domain.tipoAcesso.name,
            timestamp = domain.timestamp,
            estadoPatrimonio = domain.estadoPatrimonio,
            jaEstaColetado = domain.jaEstaColetado
        )
    }
    
    /**
     * Converte lista de Entity para lista de Domain
     */
    fun toDomainList(entities: List<HistoricoScanEntity>): List<HistoricoScan> {
        return entities.map { toDomain(it) }
    }
}
