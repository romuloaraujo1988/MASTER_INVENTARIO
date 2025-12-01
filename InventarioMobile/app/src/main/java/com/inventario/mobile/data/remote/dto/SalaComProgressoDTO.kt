package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para receber dados de sala com progresso do servidor.
 */
data class SalaComProgressoDTO(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("numeroSala")
    val numeroSala: String? = null,
    
    @SerializedName("descricao")
    val descricao: String? = null,
    
    @SerializedName("andar")
    val andar: String? = null,
    
    @SerializedName("bloco")
    val bloco: String? = null,
    
    @SerializedName("ativa")
    val ativa: Boolean = true,
    
    @SerializedName("totalPatrimonios")
    val totalPatrimonios: Int = 0,
    
    @SerializedName("coletados")
    val coletados: Int = 0,
    
    @SerializedName("pendentes")
    val pendentes: Int = 0,
    
    @SerializedName("percentualColeta")
    val percentualColeta: Float = 0f
)
