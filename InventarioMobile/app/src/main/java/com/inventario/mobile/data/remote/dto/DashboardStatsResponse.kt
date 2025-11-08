package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardStatsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: DashboardStatsData?
)

data class DashboardStatsData(
    @SerializedName("totalPatrimonios")
    val totalPatrimonios: Int,
    
    @SerializedName("patrimoniosColetados")
    val patrimoniosColetados: Int,
    
    @SerializedName("patrimoniosPendentes")
    val patrimoniosPendentes: Int,
    
    @SerializedName("totalSalas")
    val totalSalas: Int,
    
    @SerializedName("salasCompletas")
    val salasCompletas: Int,
    
    @SerializedName("salasPendentes")
    val salasPendentes: Int
)
