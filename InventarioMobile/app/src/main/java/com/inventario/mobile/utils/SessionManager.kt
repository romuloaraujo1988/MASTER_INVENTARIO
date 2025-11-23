package com.inventario.mobile.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inventario.mobile.presentation.login.LoginActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de Sessão
 * Responsável por gerenciar login, logout e validação de sessão
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Singleton
class SessionManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "SessionManager"
    }
    
    /**
     * Limpa sessão e redireciona para login
     * 
     * @param showMessage Se true, mostra mensagem de sessão expirada
     * @param message Mensagem customizada (opcional)
     */
    fun logout(showMessage: Boolean = true, message: String? = null) {
        Log.d(TAG, "═══════════════════════════════════")
        Log.d(TAG, "LOGOUT: Limpando sessão...")
        Log.d(TAG, "═══════════════════════════════════")
        
        // Limpar todos os dados de sessão
        preferencesManager.clearSavedUser()
        preferencesManager.clearInventarioAtivo()
        preferencesManager.clearSessionData()
        preferencesManager.clearSyncTimestamps()
        
        Log.d(TAG, "✓ Dados de sessão limpos")
        
        // Redirecionar para login
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                      Intent.FLAG_ACTIVITY_CLEAR_TASK or
                      Intent.FLAG_ACTIVITY_CLEAR_TOP
        
        if (showMessage) {
            val logoutMessage = message ?: "Sessão expirada. Faça login novamente."
            intent.putExtra("LOGOUT_MESSAGE", logoutMessage)
            Log.d(TAG, "✓ Mensagem de logout: $logoutMessage")
        }
        
        context.startActivity(intent)
        Log.d(TAG, "✓ Redirecionado para LoginActivity")
        Log.d(TAG, "═══════════════════════════════════")
    }
    
    /**
     * Verifica se sessão está válida
     * 
     * @return true se sessão é válida, false caso contrário
     */
    fun isSessionValid(): Boolean {
        val hasToken = preferencesManager.getAccessToken() != null
        val isExpired = preferencesManager.isTokenExpired()
        val isLoggedIn = preferencesManager.isLoggedIn()
        
        val isValid = hasToken && !isExpired && isLoggedIn
        
        Log.d(TAG, "Verificação de sessão:")
        Log.d(TAG, "  - Tem token: $hasToken")
        Log.d(TAG, "  - Token expirado: $isExpired")
        Log.d(TAG, "  - Está logado: $isLoggedIn")
        Log.d(TAG, "  - Sessão válida: $isValid")
        
        return isValid
    }
    
    /**
     * Verifica se o token está próximo de expirar
     * 
     * @return true se token expira em menos de 5 minutos
     */
    fun isTokenExpiringSoon(): Boolean {
        return preferencesManager.isTokenExpiringSoon()
    }
    
    /**
     * Obtém informações do usuário logado
     * 
     * @return Map com dados do usuário ou null se não logado
     */
    fun getUserInfo(): Map<String, String>? {
        if (!isSessionValid()) {
            return null
        }
        
        return mapOf(
            "username" to (preferencesManager.getSavedUsername() ?: ""),
            "fullName" to (preferencesManager.getSavedUserFullName() ?: ""),
            "profile" to preferencesManager.getUserProfile()
        )
    }
    
    /**
     * Força logout sem mensagem (para uso interno)
     */
    fun forceLogout() {
        logout(showMessage = false)
    }
}
