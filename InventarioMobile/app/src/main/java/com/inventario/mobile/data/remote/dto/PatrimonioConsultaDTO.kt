package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

/**
 * DTO para resposta da API de consulta de patrimônios
 * Mapeia a resposta do endpoint /api/mobile/consulta
 */
data class PatrimonioConsultaDTO(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("codigo")
    val codigo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("marca")
    val marca: String? = null,
    
    @SerializedName("modelo")
    val modelo: String? = null,
    
    @SerializedName("numeroSerie")
    val numeroSerie: String? = null,
    
    @SerializedName("estado")
    val estado: String? = null,
    
    @SerializedName("valor")
    val valor: Double? = null,
    
    @SerializedName("observacoes")
    val observacoes: String? = null,
    
    // Sala
    @SerializedName("salaId")
    val salaId: Long? = null,
    
    @SerializedName("salaNome")
    val salaNome: String? = null,
    
    // Responsável
    @SerializedName("responsavelId")
    val responsavelId: Long? = null,
    
    @SerializedName("responsavelNome")
    val responsavelNome: String? = null,
    
    // Setor
    @SerializedName("setorId")
    val setorId: Long? = null,
    
    @SerializedName("setorNome")
    val setorNome: String? = null,
    
    // Status de coleta
    @SerializedName("coletado")
    val coletado: Boolean = false,
    
    @SerializedName("dataColeta")
    val dataColeta: Date? = null
)
