package com.inventario.mobile.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.utils.PreferencesManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator que trata tokens inválidos/expirados
 * 
 * Quando recebe 401 Unauthorized:
 * 1. Limpa dados de autenticação
 * 2. Redireciona para tela de login
 * 3. Mostra mensagem ao usuário
 */
class TokenAuthenticator(
    private val context: Context,
    private val localDataManager: LocalDataManager,
    private val preferencesManager: PreferencesManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY_COUNT = 1
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.w(TAG, "═══════════════════════════════════════")
        Log.w(TAG, "TOKEN INVÁLIDO DETECTADO")
        Log.w(TAG, "═══════════════════════════════════════")
        Log.w(TAG, "Status Code: ${response.code}")
        Log.w(TAG, "URL: ${response.request.url}")
        Log.w(TAG, "Message: ${response.message}")

        // Verificar se já tentamos reautenticar
        val retryCount = response.request.header("X-Retry-Count")?.toIntOrNull() ?: 0
        
        if (retryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Máximo de tentativas atingido. Forçando logout.")
            forceLogout("Token inválido após múltiplas tentativas")
            return null
        }

        // Se é 401, o token está inválido
        if (response.code == 401) {
            Log.w(TAG, "Token expirado ou inválido. Forçando logout...")
            forceLogout("Sua sessão expirou. Por favor, faça login novamente.")
            return null
        }

        return null
    }

    /**
     * Força logout e redireciona para tela de login
     */
    private fun forceLogout(message: String) {
        try {
            Log.w(TAG, "Executando logout forçado...")
            
            // Limpar dados de autenticação
            try {
                localDataManager.clearSessionData()
                Log.d(TAG, "✓ LocalDataManager limpo")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao limpar LocalDataManager", e)
            }
            
            // Limpar PreferencesManager
            try {
                preferencesManager.clearSavedUser()
                Log.d(TAG, "✓ PreferencesManager limpo")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao limpar PreferencesManager", e)
            }
            
            // Redirecionar para tela de login
            redirectToLogin(message)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar logout forçado", e)
        }
    }

    /**
     * Redireciona para tela de login
     */
    private fun redirectToLogin(message: String) {
        try {
            Log.w(TAG, "Redirecionando para tela de login...")
            
            val intent = Intent(context, com.inventario.mobile.presentation.login.LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SESSION_EXPIRED", true)
                putExtra("ERROR_MESSAGE", message)
            }
            
            context.startActivity(intent)
            Log.d(TAG, "✓ Redirecionamento iniciado")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao redirecionar para login", e)
        }
    }
}
