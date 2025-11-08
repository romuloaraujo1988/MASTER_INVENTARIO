package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Coleta
 * Corresponde ao MobileColetaResponse do servidor
 */
data class ColetaDto(
    @SerializedName("id")
    val id: Int?,
    
    @SerializedName("numeroPatrimonio")
    val numeroPatrimonio: String?,
    
    @SerializedName("descricaoPatrimonio")
    val descricaoPatrimonio: String?,
    
    @SerializedName("idInventario")
    val idInventario: Int?,
    
    @SerializedName("nomeInventario")
    val nomeInventario: String?,
    
    @SerializedName("idSala")
    val idSala: Int?,
    
    @SerializedName("nomeSala")
    val nomeSala: String?,
    
    @SerializedName("localizacaoEncontrada")
    val localizacaoEncontrada: String?,
    
    @SerializedName("estadoEncontrado")
    val estadoEncontrado: String?,
    
    @SerializedName("observacaoColeta")
    val observacaoColeta: String?,
    
    @SerializedName("dataColeta")
    val dataColeta: String?,
    
    @SerializedName("statusColeta")
    val statusColeta: String?,
    
    @SerializedName("nomeColetor")
    val nomeColetor: String?,
    
    @SerializedName("semEtiqueta")
    val semEtiqueta: Boolean?,
    
    @SerializedName("descricaoItemSemEtiqueta")
    val descricaoItemSemEtiqueta: String?,
    
    @SerializedName("categoriaItemSemEtiqueta")
    val categoriaItemSemEtiqueta: String?,
    
    @SerializedName("sincronizado")
    val sincronizado: Boolean?,
    
    // Campos para compatibilidade com criação de coletas
    @SerializedName("patrimonio_id")
    val patrimonioId: Int? = null,
    
    @SerializedName("usuario_id")
    val usuarioId: Int? = null,
    
    @SerializedName("localizacao_atual")
    val localizacaoAtual: String? = null,
    
    @SerializedName("observacoes")
    val observacoes: String? = null,
    
    @SerializedName("foto_path")
    val fotoPath: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("data_criacao")
    val dataCriacao: String? = null,
    
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null
)