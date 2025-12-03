package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Patrimônio
 */
data class PatrimonioDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("codigo")
    val numeroPatrimonio: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("setor_id")
    val setorId: Int?,
    
    @SerializedName("sala_id")
    val salaId: Int?,
    
    @SerializedName("qr_code")
    val qrCode: String?,
    
    @SerializedName("data_criacao")
    val dataCriacao: String?,
    
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String?
)
