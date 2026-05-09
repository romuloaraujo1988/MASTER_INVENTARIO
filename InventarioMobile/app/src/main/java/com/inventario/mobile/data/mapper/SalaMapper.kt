package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.local.entity.SalaEntity
import com.inventario.mobile.data.remote.dto.SalaDto
import com.inventario.mobile.data.model.Sala

/**
 * Mapper para conversões entre diferentes representações de Sala
 * DTO (API) ↔ Entity (Room) ↔ Model (Domain)
 */
object SalaMapper {
    
    /**
     * Converte DTO da API para Entity do Room
     * Usa propriedades computadas do SalaDto para normalizar dados
     */
    fun dtoToEntity(dto: SalaDto): SalaEntity {
        return SalaEntity(
            id = dto.id,
            nome = dto.nome, // Usa propriedade computada que normaliza numeroSala/numero/descricao
            idSetor = dto.setorIdFinal, // Usa propriedade computada que normaliza idSetor/setorId
            nomeSetor = dto.nomeSetor,
            dataUltimaAtualizacao = System.currentTimeMillis()
        )
    }
    
    /**
     * Converte Entity do Room para Model de Data
     */
    fun entityToModel(entity: SalaEntity): Sala {
        return Sala(
            id = entity.id,
            nome = entity.nome,
            descricao = null,
            andar = null,
            bloco = null,
            ativa = true
        )
    }
    
    /**
     * Converte Model de Data para DTO da API
     */
    fun modelToDto(model: Sala): SalaDto {
        return SalaDto(
            id = model.id,
            idSala = null,
            numeroSala = model.nome,
            numero = null,
            descricao = model.descricao,
            ativo = model.ativa,
            ativa = model.ativa,
            idSetor = null,
            setorId = null,
            nomeSetor = null,
            andar = model.andar,
            bloco = model.bloco,
            tipoSala = null,
            dataCadastro = null,
            dataCriacao = null,
            dataAtualizacao = null
        )
    }
    
    /**
     * Converte lista de DTOs para lista de Entities
     */
    fun dtoListToEntityList(dtos: List<SalaDto>): List<SalaEntity> {
        return dtos.map { dtoToEntity(it) }
    }
    
    /**
     * Converte lista de Entities para lista de Models
     */
    fun entityListToModelList(entities: List<SalaEntity>): List<Sala> {
        return entities.map { entityToModel(it) }
    }
}
