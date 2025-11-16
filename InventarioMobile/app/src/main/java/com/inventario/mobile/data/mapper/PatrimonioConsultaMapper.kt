package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.remote.dto.PatrimonioConsultaDTO
import com.inventario.mobile.domain.model.PatrimonioConsulta
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Mapper para converter PatrimonioConsultaDTO (Data) → PatrimonioConsulta (Domain)
 * 
 * Regra: Mappers ficam na camada Data
 */
class PatrimonioConsultaMapper @Inject constructor() {
    
    /**
     * Converte DTO da API para modelo de domínio
     * 
     * @param dto DTO da API
     * @return Modelo de domínio
     */
    fun toDomain(dto: PatrimonioConsultaDTO): PatrimonioConsulta {
        return PatrimonioConsulta(
            id = dto.id.toInt(),
            codigo = dto.codigo,
            descricao = dto.descricao,
            marca = dto.marca,
            modelo = dto.modelo,
            numeroSerie = dto.numeroSerie,
            estado = dto.estado,
            valor = dto.valor?.let { BigDecimal(it) },
            observacoes = dto.observacoes,
            
            // Sala
            salaId = dto.salaId?.toInt(),
            salaNome = dto.salaNome,
            
            // Responsável
            responsavelId = dto.responsavelId?.toInt(),
            responsavelNome = dto.responsavelNome,
            
            // Setor
            setorId = dto.setorId?.toInt(),
            setorNome = dto.setorNome,
            
            // Status de coleta
            coletado = dto.coletado,
            dataColeta = dto.dataColeta
        )
    }
    
    /**
     * Converte lista de DTOs para lista de modelos de domínio
     * 
     * @param dtos Lista de DTOs
     * @return Lista de modelos de domínio
     */
    fun toDomainList(dtos: List<PatrimonioConsultaDTO>): List<PatrimonioConsulta> {
        return dtos.map { toDomain(it) }
    }
    
    /**
     * Converte modelo de domínio para DTO (se necessário)
     * 
     * @param domain Modelo de domínio
     * @return DTO da API
     */
    fun toDTO(domain: PatrimonioConsulta): PatrimonioConsultaDTO {
        return PatrimonioConsultaDTO(
            id = domain.id.toLong(),
            codigo = domain.codigo,
            descricao = domain.descricao,
            marca = domain.marca,
            modelo = domain.modelo,
            numeroSerie = domain.numeroSerie,
            estado = domain.estado,
            valor = domain.valor?.toDouble(),
            observacoes = domain.observacoes,
            
            // Sala
            salaId = domain.salaId?.toLong(),
            salaNome = domain.salaNome,
            
            // Responsável
            responsavelId = domain.responsavelId?.toLong(),
            responsavelNome = domain.responsavelNome,
            
            // Setor
            setorId = domain.setorId?.toLong(),
            setorNome = domain.setorNome,
            
            // Status de coleta
            coletado = domain.coletado,
            dataColeta = domain.dataColeta
        )
    }
}
