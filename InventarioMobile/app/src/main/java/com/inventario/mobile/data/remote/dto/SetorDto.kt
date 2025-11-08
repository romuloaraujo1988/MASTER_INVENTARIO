package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para transferência de dados de Setor
 */
data class SetorDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("codigo")
    val codigo: String?,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("data_criacao")
    val dataCriacao: String?,
    
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String?
)