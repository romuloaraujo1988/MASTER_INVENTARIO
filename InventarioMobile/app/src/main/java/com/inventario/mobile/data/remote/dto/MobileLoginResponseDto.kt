package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que corresponde exatamente ao formato retornado pelo servidor
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
    val user: MobileUserInfoDto
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
