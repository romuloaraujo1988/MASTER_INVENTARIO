package com.inventario.mobile.data.local.dto

/**
 * DTO para estatísticas do Dashboard (Room)
 * 
 * Room precisa de um DTO simples para mapear resultados de queries.
 * Este DTO é convertido para DashboardStats (domain model) no Repository.
 */
data class DashboardStatsDto(
    val totalPatrimonios: Int,
    val totalColetados: Int,
    val totalPendentes: Int,
    val percentualColetado: Float
)
