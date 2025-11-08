package com.inventario.mobile.domain.repository

import com.inventario.mobile.data.model.LoginResponse
import com.inventario.mobile.data.remote.dto.RefreshTokenRequest
import com.inventario.mobile.data.remote.dto.LoginRequest
import com.inventario.mobile.utils.ConnectivityResult

/**
 * Repository para operações de autenticação
 */
interface AuthRepository {
    
    /**
     * Realiza login do usuário
     */
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
    
    /**
     * Renova o token de acesso
     */
    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Result<LoginResponse>
    
    /**
     * Testa conectividade com o servidor
     */
    suspend fun testConnectivity(): ConnectivityResult
}