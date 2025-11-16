package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.remote.dto.PatrimonioDetalheDTO
import com.inventario.mobile.domain.model.PatrimonioDetalhe
import javax.inject.Inject

/**
 * Mapper para converter PatrimonioDetalheDTO (Data) → PatrimonioDetalhe (Domain)
 * 
 * Regra: Mappers ficam na camada Data
 */
class PatrimonioDetalheMapper @Inject constructor() {
    
    /**
     * Converte DTO da API para modelo de domínio
     * 
     * @param dto DTO da API
     * @return Modelo de domínio
     */
    fun toDomain(dto: PatrimonioDetalheDTO): PatrimonioDetalhe {
        return PatrimonioDetalhe(
            // Dados básicos
            id = dto.id,
            codigo = dto.codigo,
            descricao = dto.descricao,
            marca = dto.marca,
            modelo = dto.modelo,
            numeroSerie = dto.numeroSerie,
            estado = dto.estado,
            valor = dto.valor,
            observacoes = dto.observacoes,
            
            // Dados da sala
            salaId = dto.salaId,
            salaNome = dto.salaNome,
            salaBloco = dto.salaBloco,
            salaAndar = dto.salaAndar,
            
            // Dados do responsável
            responsavelId = dto.responsavelId,
            responsavelNome = dto.responsavelNome,
            responsavelMatricula = dto.responsavelMatricula,
            responsavelSetor = dto.responsavelSetor,
            responsavelEmail = dto.responsavelEmail,
            responsavelTelefone = dto.responsavelTelefone,
            
            // Status de coleta
            coletado = dto.coletado,
            dataColeta = dto.dataColeta,
            coletadoPor = dto.coletadoPor,
            localizacaoEncontrada = dto.localizacaoEncontrada,
            estadoEncontrado = dto.estadoEncontrado,
            observacoesColeta = dto.observacoesColeta,
            
            // Histórico
            totalColetas = dto.totalColetas,
            ultimaColeta = dto.ultimaColeta,
            
            // Foto
            fotoUrl = dto.fotoUrl
        )
    }
    
    /**
     * Converte modelo de domínio para DTO (se necessário)
     * 
     * @param domain Modelo de domínio
     * @return DTO da API
     */
    fun toDTO(domain: PatrimonioDetalhe): PatrimonioDetalheDTO {
        return PatrimonioDetalheDTO(
            // Dados básicos
            id = domain.id,
            codigo = domain.codigo,
            descricao = domain.descricao,
            marca = domain.marca,
            modelo = domain.modelo,
            numeroSerie = domain.numeroSerie,
            estado = domain.estado,
            valor = domain.valor,
            observacoes = domain.observacoes,
            
            // Dados da sala
            salaId = domain.salaId,
            salaNome = domain.salaNome,
            salaBloco = domain.salaBloco,
            salaAndar = domain.salaAndar,
            
            // Dados do responsável
            responsavelId = domain.responsavelId,
            responsavelNome = domain.responsavelNome,
            responsavelMatricula = domain.responsavelMatricula,
            responsavelSetor = domain.responsavelSetor,
            responsavelEmail = domain.responsavelEmail,
            responsavelTelefone = domain.responsavelTelefone,
            
            // Status de coleta
            coletado = domain.coletado,
            dataColeta = domain.dataColeta,
            coletadoPor = domain.coletadoPor,
            localizacaoEncontrada = domain.localizacaoEncontrada,
            estadoEncontrado = domain.estadoEncontrado,
            observacoesColeta = domain.observacoesColeta,
            
            // Histórico
            totalColetas = domain.totalColetas,
            ultimaColeta = domain.ultimaColeta,
            
            // Foto
            fotoUrl = domain.fotoUrl
        )
    }
}
