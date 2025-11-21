package com.inventario.mobile.data.mapper

import com.inventario.mobile.data.remote.dto.ColetasPorDiaDto
import com.inventario.mobile.data.remote.dto.DashboardStatsDto
import com.inventario.mobile.domain.model.DashboardStats
import com.inventario.mobile.domain.model.EvolucaoColeta
import javax.inject.Inject

/**
 * Mapper entre DTOs e Domain Models do Dashboard
 * Centraliza conversões entre camadas
 */
class DashboardMapper @Inject constructor() {
    
    /**
     * Converte DashboardStatsDto para DashboardStats (Domain)
     * ✅ CORRIGIDO: Agora mapeia inventarioId e inventarioNome
     */
    fun toDomain(dto: DashboardStatsDto): DashboardStats {
        return DashboardStats(
            inventarioId = dto.inventarioId,
            inventarioNome = dto.inventarioNome,
            totalPatrimonios = dto.totalPatrimonios,
            totalColetados = dto.patrimoniosColetados,
            totalPendentes = dto.patrimoniosPendentes,
            percentualConclusao = dto.percentualConclusao,
            coletoresAtivos = dto.coletoresAtivos,
            divergencias = dto.divergencias,
            valorTotal = dto.valorTotal
        )
    }
    
    /**
     * Converte ColetasPorDiaDto para EvolucaoColeta (Domain)
     */
    fun toDomain(dto: ColetasPorDiaDto): EvolucaoColeta {
        return EvolucaoColeta(
            data = dto.data,
            quantidade = dto.quantidade,
            coletoresAtivos = dto.coletoresAtivos,
            dataFormatada = dto.dataFormatada
        )
    }
    
    /**
     * Converte lista de ColetasPorDiaDto para lista de EvolucaoColeta
     */
    fun toDomainList(dtos: List<ColetasPorDiaDto>): List<EvolucaoColeta> {
        return dtos.map { toDomain(it) }
    }
    
    /**
     * Converte Map de evolução (do backend) para lista de EvolucaoColeta
     */
    fun evolucaoMapToDomain(evolucaoMap: Map<*, *>): List<EvolucaoColeta> {
        return evolucaoMap.entries.map { entry ->
            val dataFormatada = entry.key.toString()
            val quantidade = (entry.value as? Number)?.toInt() ?: 0
            
            // Converter data formatada "dd/MM" para "2025-11-dd"
            val ano = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val partes = dataFormatada.split("/")
            val dia = partes.getOrNull(0)?.padStart(2, '0') ?: "01"
            val mes = partes.getOrNull(1)?.padStart(2, '0') ?: "01"
            val dataISO = "$ano-$mes-$dia"
            
            EvolucaoColeta(
                data = dataISO,
                quantidade = quantidade,
                coletoresAtivos = 0,
                dataFormatada = dataFormatada
            )
        }.sortedBy { it.data }
    }
}
