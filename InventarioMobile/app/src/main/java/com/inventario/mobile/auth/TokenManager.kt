package com.inventario.mobile.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inventario.mobile.presentation.login.LoginActivity
import com.inventario.mobile.utils.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador centralizado de tokens
 * 
 * Responsável por:
 * - Verificar validade de tokens
 * - Redirecionar para login quando necessário
 * - Limpar sessão expirada
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "TokenManager"
    }
    
    /**
     * Verifica se o token é válido
     */
    fun isTokenValid(): Boolean {
        val token = preferencesManager.getAccessToken()
        
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "❌ Token não encontrado")
            return false
        }
        
        if (preferencesManager.isTokenExpired()) {
            Log.d(TAG, "❌ Token expirado")
            return false
        }
        
        Log.d(TAG, "✓ Token válido")
        return true
    }
    
    /**
     * Verifica se pode renovar token usando refresh token
     */
    fun canRefreshToken(): Boolean {
        val refreshToken = preferencesManager.getRefreshToken()
        val canRefresh = !refreshToken.isNullOrEmpty()
        
        Log.d(TAG, if (canRefresh) "✓ Pode renovar token" else "❌ Não pode renovar token")
        return canRefresh
    }
    
    /**
     * Verifica token e redireciona para login se inválido
     * @return true se token é válido, false se redirecionou para login
     */
    fun validateTokenOrRedirectToLogin(): Boolean {
        if (!isTokenValid()) {
            Log.w(TAG, "⚠️ Token inválido, redirecionando para login...")
            redirectToLogin("Sua sessão expirou. Faça login novamente.")
            return false
        }
        return true
    }
    
    /**
     * Redireciona para tela de login
     */
    fun redirectToLogin(message: String = "Sessão expirada") {
        Log.w(TAG, "🔄 Redirecionando para login: $message")
        
        // Limpar dados de sessão
        preferencesManager.clearSavedUser()
        preferencesManager.clearSessionData()
        preferencesManager.clearInventarioAtivo()
        
        // Criar intent para LoginActivity
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("token_expired", true)
            putExtra("message", message)
        }
        
        context.startActivity(intent)
    }
    
    /**
     * Verifica se está logado
     */
    fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn()
    }
    
    /**
     * Verifica se token está próximo de expirar
     */
    fun isTokenExpiringSoon(): Boolean {
        return preferencesManager.isTokenExpiringSoon()
    }
}
