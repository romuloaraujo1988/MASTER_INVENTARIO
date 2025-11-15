package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para resposta de coleta do servidor
 */
data class MobileColetaResponseDto(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("numeroPatrimonio")
    val numeroPatrimonio: String? = null,
    
    @SerializedName("descricaoPatrimonio")
    val descricaoPatrimonio: String? = null,
    
    @SerializedName("idInventario")
    val idInventario: Int? = null,
    
    @SerializedName("nomeInventario")
    val nomeInventario: String? = null,
    
    @SerializedName("idSala")
    val idSala: Int? = null,
    
    @SerializedName("nomeSala")
    val nomeSala: String? = null,
    
    @SerializedName("localizacaoEncontrada")
    val localizacaoEncontrada: String? = null,
    
    @SerializedName("localizacaoAtual")
    val localizacaoAtual: String? = null,
    
    @SerializedName("estadoEncontrado")
    val estadoEncontrado: String? = null,
    
    @SerializedName("observacaoColeta")
    val observacoes: String? = null,
    
    @SerializedName("dataColeta")
    val dataColeta: String? = null,
    
    @SerializedName("statusColeta")
    val statusColeta: String? = null,
    
    @SerializedName("nomeColetor")
    val nomeColetor: String? = null,
    
    @SerializedName("usuarioId")
    val usuarioId: Int = 0,
    
    @SerializedName("patrimonioId")
    val patrimonioId: Int = 0,
    
    @SerializedName("semEtiqueta")
    val semEtiqueta: Boolean? = null,
    
    @SerializedName("descricaoItemSemEtiqueta")
    val descricaoItemSemEtiqueta: String? = null,
    
    @SerializedName("categoriaItemSemEtiqueta")
    val categoriaItemSemEtiqueta: String? = null,
    
    @SerializedName("sincronizado")
    val sincronizado: Boolean = true
)
