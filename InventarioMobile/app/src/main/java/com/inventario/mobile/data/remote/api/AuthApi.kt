package com.inventario.mobile.data.remote.api

import com.inventario.mobile.data.remote.dto.LoginRequest
import com.inventario.mobile.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API de autenticação
 * 
 * Endpoints:
 * - POST /api/mobile/auth/login - Login com usuário e senha
 * - POST /api/mobile/auth/refresh - Renovar access token usando refresh token
 */
interface AuthApi {
    
    /**
     * Login com usuário e senha
     */
    @POST("api/mobile/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * Renovar access token usando refresh token
     * 
     * @param refreshToken Token de refresh válido
     * @return Nova resposta de login com novo access token
     */
    @POST("api/mobile/auth/refresh")
    suspend fun refreshToken(@Query("refreshToken") refreshToken: String): Response<LoginResponse>
}
