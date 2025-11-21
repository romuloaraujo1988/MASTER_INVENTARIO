package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para estatísticas do dashboard
 * ✅ ATUALIZADO: Adicionados campos de inventário
 */
data class DashboardStatsDto(
    @SerializedName("inventarioId")
    val inventarioId: Int? = null,
    
    @SerializedName("inventarioNome")
    val inventarioNome: String? = null,
    
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
