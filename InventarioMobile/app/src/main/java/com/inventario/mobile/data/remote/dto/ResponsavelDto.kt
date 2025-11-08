package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResponsavelDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("cpf")
    val cpf: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("telefone")
    val telefone: String? = null,
    
    @SerializedName("cargo")
    val cargo: String? = null,
    
    @SerializedName("idSetor")
    val idSetor: Int? = null,
    
    @SerializedName("nomeSetor")
    val nomeSetor: String? = null,
    
    @SerializedName("ativo")
    val ativo: Boolean = true,
    
    @SerializedName("dataCadastro")
    val dataCadastro: String? = null
)
