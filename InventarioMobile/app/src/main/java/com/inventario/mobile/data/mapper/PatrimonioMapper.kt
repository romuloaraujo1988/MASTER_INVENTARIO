package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.PatrimonioEntity
import com.inventario.mobile.data.remote.dto.MobilePatrimonioDto
import com.inventario.mobile.data.model.Patrimonio

/**
 * Mapper para conversões entre diferentes representações de Patrimônio
 * DTO (API) ↔ Entity (Room) ↔ Model (Domain)
 */
object PatrimonioMapper {
    
    /**
     * Converte DTO da API para Entity do Room
     * Usado durante sincronização para salvar dados no banco local
     */
    fun dtoToEntity(dto: MobilePatrimonioDto): PatrimonioEntity {
        return PatrimonioEntity(
            id = dto.id.toInt(), // Conversão Long → Int
            numero = dto.codigo, // DTO usa 'codigo', Entity usa 'numero'
            descricao = dto.descricao,
            idSala = dto.salaId?.toInt(), // DTO usa Long, Entity usa Int
            nomeSala = dto.salaNome,
            idResponsavel = dto.responsavelId?.toInt(),
            nomeResponsavel = dto.responsavelNome,
            status = dto.estado ?: "ATIVO", // DTO usa 'estado', Entity usa 'status'
            coletado = dto.coletado,
            dataUltimaAtualizacao = System.currentTimeMillis()
        )
    }
    
    /**
     * Converte Entity do Room para Model de Data
     * Usado para fornecer dados para a camada de apresentação
     */
    fun entityToModel(entity: PatrimonioEntity): Patrimonio {
        return Patrimonio(
            id = entity.id.toLong(), // Entity usa Int, Model usa Long
            numeroPatrimonio = entity.numero,
            descricao = entity.descricao,
            marca = null, // Entity não tem marca
            modelo = null, // Entity não tem modelo
            numeroSerie = null, // Entity não tem numeroSerie
            estado = entity.status,
            valor = null, // Entity não tem valor
            setorId = null, // Entity não tem setorId direto
            setorNome = null,
            salaId = entity.idSala?.toLong(),
            salaNome = entity.nomeSala,
            responsavelId = entity.idResponsavel?.toLong(),
            responsavelNome = entity.nomeResponsavel,
            qrCode = entity.numero, // Usar numero como qrCode temporariamente
            observacoes = null, // Entity não tem observacoes
            coletado = entity.coletado,
            dataColeta = null,
            coletadoPor = null,
            dataColetaFormatada = null,
            observacoesColeta = null,
            sincronizado = true, // Dados do banco local são considerados sincronizados
            servidorId = null
        )
    }
    
    /**
     * Converte Model de Domínio para DTO da API
     * Usado para enviar dados para o servidor
     */
    fun modelToDto(model: Patrimonio): MobilePatrimonioDto {
        return MobilePatrimonioDto(
            id = model.id,
            codigo = model.numeroPatrimonio,
            descricao = model.descricao,
            marca = model.marca,
            modelo = model.modelo,
            numeroSerie = model.numeroSerie,
            estado = model.estado,
            valor = model.valor,
            setorId = model.setorId,
            setorNome = null,
            salaId = model.salaId,
            salaNome = null,
            responsavelId = model.responsavelId,
            responsavelNome = null,
            qrCode = model.qrCode,
            coletado = model.coletado,
            dataColeta = model.dataColeta,
            coletadoPor = null,
            dataColetaFormatada = null,
            observacoes = model.observacoes
        )
    }
    
    /**
     * Converte lista de DTOs para lista de Entities
     */
    fun dtoListToEntityList(dtos: List<MobilePatrimonioDto>): List<PatrimonioEntity> {
        return dtos.map { dtoToEntity(it) }
    }
    
    /**
     * Converte lista de Entities para lista de Models
     */
    fun entityListToModelList(entities: List<PatrimonioEntity>): List<Patrimonio> {
        return entities.map { entityToModel(it) }
    }
}
