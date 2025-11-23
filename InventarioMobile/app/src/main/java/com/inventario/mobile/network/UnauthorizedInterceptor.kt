package com.inventario.mobile.network

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.inventario.mobile.data.local.LocalDataManager
import com.inventario.mobile.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que detecta respostas 401 Unauthorized
 * e força o usuário a fazer login novamente
 * 
 * Funcionalidades:
 * - Detecta token inválido/expirado
 * - Limpa dados de autenticação
 * - Mostra notificação ao usuário
 * - Redireciona para tela de login
 */
class UnauthorizedInterceptor(
    private val context: Context,
    private val localDataManager: LocalDataManager,
    private val preferencesManager: PreferencesManager
) : Interceptor {

    companion object {
        private const val TAG = "UnauthorizedInterceptor"
        
        @Volatile
        private var isHandlingUnauthorized = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Verificar se é 401 Unauthorized
        if (response.code == 401) {
            Log.w(TAG, "═══════════════════════════════════════")
            Log.w(TAG, "401 UNAUTHORIZED DETECTADO")
            Log.w(TAG, "═══════════════════════════════════════")
            Log.w(TAG, "URL: ${request.url}")
            Log.w(TAG, "Method: ${request.method}")
            
            // Evitar múltiplos tratamentos simultâneos
            if (!isHandlingUnauthorized) {
                isHandlingUnauthorized = true
                handleUnauthorized()
            }
        }

        return response
    }

    /**
     * Trata resposta 401 Unauthorized
     */
    private fun handleUnauthorized() {
        try {
            Log.w(TAG, "Tratando 401 Unauthorized...")
            
            // Limpar dados de autenticação
            clearAuthData()
            
            // Mostrar notificação ao usuário
            showSessionExpiredNotification()
            
            // Redirecionar para login
            redirectToLogin()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tratar 401", e)
        } finally {
            // Resetar flag após 2 segundos
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isHandlingUnauthorized = false
            }, 2000)
        }
    }

    /**
     * Limpa dados de autenticação
     */
    private fun clearAuthData() {
        try {
            Log.w(TAG, "Limpando dados de autenticação...")
            
            // Limpar LocalDataManager
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
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar dados de autenticação", e)
        }
    }

    /**
     * Mostra notificação de sessão expirada
     */
    private fun showSessionExpiredNotification() {
        try {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "⚠️ Sessão expirada. Por favor, faça login novamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
            Log.d(TAG, "✓ Notificação mostrada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar notificação", e)
        }
    }

    /**
     * Redireciona para tela de login
     */
    private fun redirectToLogin() {
        try {
            Log.w(TAG, "Redirecionando para tela de login...")
            
            val intent = Intent(context, com.inventario.mobile.presentation.login.LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SESSION_EXPIRED", true)
                putExtra("ERROR_MESSAGE", "Sua sessão expirou. Por favor, faça login novamente.")
            }
            
            context.startActivity(intent)
            Log.d(TAG, "✓ Redirecionamento iniciado")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao redirecionar para login", e)
        }
    }
}
