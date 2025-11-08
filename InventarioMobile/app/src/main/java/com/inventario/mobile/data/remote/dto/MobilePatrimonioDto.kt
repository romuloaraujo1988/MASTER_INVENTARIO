package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Patrimônio Mobile
 * Compatível com MobilePatrimonioDTO do servidor
 */
data class MobilePatrimonioDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("codigo")
    val codigo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("marca")
    val marca: String?,
    
    @SerializedName("modelo")
    val modelo: String?,
    
    @SerializedName("numeroSerie")
    val numeroSerie: String?,
    
    @SerializedName("estado")
    val estado: String?,
    
    @SerializedName("valor")
    val valor: Double?,
    
    @SerializedName("setorId")
    val setorId: Long?,
    
    @SerializedName("setorNome")
    val setorNome: String?,
    
    @SerializedName("salaId")
    val salaId: Long?,
    
    @SerializedName("salaNome")
    val salaNome: String?,
    
    @SerializedName("responsavelId")
    val responsavelId: Long?,
    
    @SerializedName("responsavelNome")
    val responsavelNome: String?,
    
    @SerializedName("qrCode")
    val qrCode: String?,
    
    @SerializedName("coletado")
    val coletado: Boolean = false,
    
    @SerializedName("dataColeta")
    val dataColeta: String?,
    
    @SerializedName("coletadoPor")
    val coletadoPor: String?,
    
    @SerializedName("dataColetaFormatada")
    val dataColetaFormatada: String?,
    
    @SerializedName("observacoes")
    val observacoes: String?
)