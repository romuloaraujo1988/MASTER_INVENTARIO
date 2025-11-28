package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que corresponde exatamente ao formato retornado pelo servidor
 * 
 * v1.1.0 - Adicionado inventarioAtivo para salvar automaticamente no login
 */
data class MobileLoginResponseDto(
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Long,
    
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    
    @SerializedName("user")
    val user: MobileUserInfoDto,
    
    /**
     * Inventário ativo (em andamento) retornado automaticamente no login
     * O app deve salvar este ID localmente para usar nas coletas
     */
    @SerializedName("inventarioAtivo")
    val inventarioAtivo: MobileInventarioInfoDto? = null
)

data class MobileUserInfoDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("perfil")
    val perfil: String,
    
    @SerializedName("ativo")
    val ativo: Boolean,
    
    @SerializedName("setorId")
    val setorId: Long? = null
)

/**
 * DTO com informações do inventário ativo
 * Retornado no login para o app salvar localmente
 */
data class MobileInventarioInfoDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("ano")
    val ano: Int? = null,
    
    @SerializedName("totalPatrimonios")
    val totalPatrimonios: Int? = null,
    
    @SerializedName("patrimoniosColetados")
    val patrimoniosColetados: Int? = null,
    
    @SerializedName("percentualConclusao")
    val percentualConclusao: Double? = null
)
