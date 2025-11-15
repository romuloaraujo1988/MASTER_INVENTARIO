package com.inventario.mobile.presentation.dashboard

import com.inventario.mobile.data.remote.dto.ColetasPorDiaDto
import com.inventario.mobile.domain.model.EvolucaoColeta

/**
 * Adapter para converter Domain Models para DTOs usados na UI
 * Necessário para compatibilidade com código legado do gráfico
 */
object DashboardAdapter {
    
    /**
     * Converte EvolucaoColeta (Domain) para ColetasPorDiaDto (DTO)
     * Usado para manter compatibilidade com o código do gráfico
     */
    fun toDto(evolucao: EvolucaoColeta): ColetasPorDiaDto {
        return ColetasPorDiaDto(
            data = evolucao.data,
            quantidade = evolucao.quantidade,
            coletoresAtivos = evolucao.coletoresAtivos,
            dataFormatada = evolucao.dataFormatada
        )
    }
    
    /**
     * Converte lista de EvolucaoColeta para lista de ColetasPorDiaDto
     */
    fun toDtoList(evolucoes: List<EvolucaoColeta>): List<ColetasPorDiaDto> {
        return evolucoes.map { toDto(it) }
    }
}
