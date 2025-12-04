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
 * 
 * Configuração de tokens (backend):
 * - Access Token: 2 dias (172800 segundos)
 * - Refresh Token: 14 dias (1209600 segundos)
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "TokenManager"
        
        // Tempo de expiração do refresh token em milissegundos (14 dias)
        private const val REFRESH_TOKEN_EXPIRATION_MS = 14L * 24 * 60 * 60 * 1000
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
     * Considera tanto a existência quanto a validade do refresh token
     */
    fun canRefreshToken(): Boolean {
        val refreshToken = preferencesManager.getRefreshToken()
        
        if (refreshToken.isNullOrEmpty()) {
            Log.d(TAG, "❌ Refresh token não encontrado")
            return false
        }
        
        // Verificar se refresh token não expirou
        // O refresh token é salvo junto com o access token, então usamos o timestamp de login
        // mais o tempo de expiração do refresh token (14 dias)
        if (isRefreshTokenExpired()) {
            Log.d(TAG, "❌ Refresh token expirado (mais de 14 dias desde o login)")
            return false
        }
        
        Log.d(TAG, "✓ Pode renovar token")
        return true
    }
    
    /**
     * Verifica se o refresh token expirou
     * O refresh token expira 14 dias após o login
     */
    fun isRefreshTokenExpired(): Boolean {
        val loginTimestamp = preferencesManager.getLoginTimestamp()
        
        if (loginTimestamp == 0L) {
            // Se não temos timestamp de login, assumir que pode renovar
            // (compatibilidade com versões anteriores)
            return false
        }
        
        val now = System.currentTimeMillis()
        val refreshTokenExpiresAt = loginTimestamp + REFRESH_TOKEN_EXPIRATION_MS
        
        val isExpired = now >= refreshTokenExpiresAt
        
        if (isExpired) {
            val daysExpired = (now - refreshTokenExpiresAt) / (24 * 60 * 60 * 1000)
            Log.d(TAG, "Refresh token expirou há $daysExpired dias")
        }
        
        return isExpired
    }
    
    /**
     * Verifica token e redireciona para login se inválido
     * @return true se token é válido, false se redirecionou para login
     */
    fun validateTokenOrRedirectToLogin(): Boolean {
        if (!isTokenValid()) {
            // Verificar se pode renovar
            if (canRefreshToken()) {
                Log.d(TAG, "⚠️ Token expirado mas pode renovar - não redirecionar")
                return true // Deixar o RefreshTokenInterceptor renovar
            }
            
            Log.w(TAG, "⚠️ Token inválido e não pode renovar, redirecionando para login...")
            redirectToLogin("Sua sessão expirou. Faça login novamente.")
            return false
        }
        return true
    }
    
    /**
     * Verifica se precisa fazer login novamente
     * Retorna true se:
     * - Não tem token
     * - Token expirado E refresh token também expirado
     */
    fun needsRelogin(): Boolean {
        val token = preferencesManager.getAccessToken()
        
        // Sem token = precisa login
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "needsRelogin: Sem token")
            return true
        }
        
        // Token válido = não precisa login
        if (!preferencesManager.isTokenExpired()) {
            Log.d(TAG, "needsRelogin: Token válido")
            return false
        }
        
        // Token expirado - verificar refresh token
        if (!canRefreshToken()) {
            Log.d(TAG, "needsRelogin: Token expirado e não pode renovar")
            return true
        }
        
        Log.d(TAG, "needsRelogin: Token expirado mas pode renovar")
        return false
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
    
    /**
     * Obtém informações de debug sobre o estado dos tokens
     */
    fun getTokenDebugInfo(): String {
        val hasToken = preferencesManager.getAccessToken() != null
        val isTokenExpired = preferencesManager.isTokenExpired()
        val hasRefreshToken = preferencesManager.getRefreshToken() != null
        val isRefreshExpired = isRefreshTokenExpired()
        val loginTimestamp = preferencesManager.getLoginTimestamp()
        
        val loginDate = if (loginTimestamp > 0) {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(loginTimestamp))
        } else "N/A"
        
        return """
            |=== Token Debug Info ===
            |Has Access Token: $hasToken
            |Access Token Expired: $isTokenExpired
            |Has Refresh Token: $hasRefreshToken
            |Refresh Token Expired: $isRefreshExpired
            |Login Timestamp: $loginDate
            |Can Refresh: ${canRefreshToken()}
            |Needs Relogin: ${needsRelogin()}
            |========================
        """.trimMargin()
    }
}
