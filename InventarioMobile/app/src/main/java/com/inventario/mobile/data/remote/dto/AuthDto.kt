package com.inventario.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para autenticação
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("appVersion")
    val appVersion: String
)

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Long,
    
    @SerializedName("tokenType")
    val tokenType: String,
    
    @SerializedName("user")
    val user: UsuarioDto
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)
