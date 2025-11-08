package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para estatísticas do dashboard
 * Corresponde ao DashboardStatsDTO do backend
 */
data class DashboardStatsDto(
    @SerializedName("totalPatrimonios")
    val totalPatrimonios: Int = 0,
    
    @SerializedName("patrimoniosColetados")
    val patrimoniosColetados: Int = 0,
    
    @SerializedName("percentualConclusao")
    val percentualConclusao: Double = 0.0,
    
    @SerializedName("patrimoniosPendentes")
    val patrimoniosPendentes: Int = 0,
    
    @SerializedName("divergencias")
    val divergencias: Int = 0,
    
    @SerializedName("valorTotal")
    val valorTotal: Double = 0.0,
    
    @SerializedName("coletoresAtivos")
    val coletoresAtivos: Int = 0,
    
    @SerializedName("ultimaAtualizacao")
    val ultimaAtualizacao: String? = null
)

/**
 * Extensões para formatação
 */
fun DashboardStatsDto.getPercentualFormatado(): String {
    return String.format("%.1f%%", percentualConclusao)
}

fun DashboardStatsDto.getValorFormatado(): String {
    return String.format("R$ %,.2f", valorTotal)
}

fun DashboardStatsDto.getUltimaAtualizacaoFormatada(): String {
    if (ultimaAtualizacao.isNullOrBlank()) return "Nunca"
    
    return try {
        val dateTime = java.time.LocalDateTime.parse(ultimaAtualizacao)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        ultimaAtualizacao
    }
}
