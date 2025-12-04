package com.inventario.mobile.network

import android.util.Log
import com.inventario.mobile.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface para notificar quando token expirou e não pode ser renovado
 */
interface TokenExpiredListener {
    fun onTokenExpired()
}

/**
 * Interceptor para renovação automática de token
 * Intercepta requisições com erro 401 e tenta renovar o token automaticamente
 * Se renovação falhar, notifica listener para fazer logout
 */
@Singleton
class RefreshTokenInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    companion object {
        private const val TAG = "RefreshTokenInterceptor"
        private const val MAX_RETRY_ATTEMPTS = 1
    }
    
    /**
     * Listener para notificar quando token expirou
     * Será chamado quando renovação falhar
     */
    var tokenExpiredListener: TokenExpiredListener? = null
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Verificar se token está próximo de expirar antes mesmo de fazer a requisição
        if (preferencesManager.isTokenExpiringSoon() && !isRefreshTokenRequest(originalRequest)) {
            Log.d(TAG, "Token expirando em breve, renovando preventivamente...")
            tryRefreshToken(chain)
        }
        
        // Fazer requisição original
        val response = chain.proceed(originalRequest)
        
        // Se receber 401 (Unauthorized), tentar renovar token
        if (response.code == 401 && !isRefreshTokenRequest(originalRequest)) {
            Log.w(TAG, "Recebido 401 Unauthorized, tentando renovar token...")
            
            response.close() // Fechar resposta original
            
            // Tentar renovar token
            val refreshSuccess = tryRefreshToken(chain)
            
            if (refreshSuccess) {
                // Retry requisição original com novo token
                Log.d(TAG, "Token renovado, retrying requisição original...")
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${preferencesManager.getAccessToken()}")
                    .build()
                
                return chain.proceed(newRequest)
            } else {
                Log.e(TAG, "Falha ao renovar token, retornando 401")
                // Retornar resposta 401 original
                return response
            }
        }
        
        return response
    }
    
    /**
     * Tenta renovar o token usando refresh token
     */
    private fun tryRefreshToken(chain: Interceptor.Chain): Boolean {
        return try {
            val refreshToken = preferencesManager.getRefreshToken()
            
            if (refreshToken.isNullOrEmpty()) {
                Log.e(TAG, "❌ Refresh token não encontrado")
                // Notificar que token expirou
                tokenExpiredListener?.onTokenExpired()
                return false
            }
            
            // Verificar se refresh token não expirou (14 dias desde o login)
            val loginTimestamp = preferencesManager.getLoginTimestamp()
            val refreshTokenExpirationMs = 14L * 24 * 60 * 60 * 1000 // 14 dias
            
            if (loginTimestamp > 0) {
                val now = System.currentTimeMillis()
                val refreshTokenExpiresAt = loginTimestamp + refreshTokenExpirationMs
                
                if (now >= refreshTokenExpiresAt) {
                    val daysExpired = (now - refreshTokenExpiresAt) / (24 * 60 * 60 * 1000)
                    Log.e(TAG, "❌ Refresh token expirou há $daysExpired dias (mais de 14 dias desde o login)")
                    tokenExpiredListener?.onTokenExpired()
                    return false
                }
            }
            
            Log.d(TAG, "Enviando requisição de refresh token...")
            
            // Construir requisição de refresh
            val refreshRequest = Request.Builder()
                .url("${getBaseUrl(chain)}/api/mobile/auth/refresh?refreshToken=$refreshToken")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()
            
            // Executar requisição de refresh
            val refreshResponse = chain.proceed(refreshRequest)
            
            if (refreshResponse.isSuccessful) {
                val responseBody = refreshResponse.body?.string()
                
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val newAccessToken = json.optString("accessToken")
                    val newRefreshToken = json.optString("refreshToken")
                    // Usar 172800 (2 dias) como padrão, conforme configuração do backend
                    val expiresIn = json.optLong("expiresIn", 172800L)
                    
                    if (newAccessToken.isNotEmpty()) {
                        // Salvar novos tokens
                        preferencesManager.saveTokens(newAccessToken, newRefreshToken, expiresIn)
                        
                        val tokenTimeRemaining = preferencesManager.getTokenTimeRemaining()
                        Log.d(TAG, "✓ Token renovado com sucesso! Expira em: $tokenTimeRemaining")
                        return true
                    }
                }
            } else {
                Log.e(TAG, "❌ Falha ao renovar token: ${refreshResponse.code}")
                
                // Se o servidor retornou 401, o refresh token é inválido
                if (refreshResponse.code == 401) {
                    Log.e(TAG, "   → Refresh token inválido ou expirado no servidor")
                }
                
                // Notificar que token expirou (não pode ser renovado)
                tokenExpiredListener?.onTokenExpired()
            }
            
            refreshResponse.close()
            false
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao renovar token", e)
            // Notificar que token expirou (erro na renovação)
            tokenExpiredListener?.onTokenExpired()
            false
        }
    }
    
    /**
     * Verifica se a requisição é de refresh token
     */
    private fun isRefreshTokenRequest(request: Request): Boolean {
        return request.url.encodedPath.contains("/auth/refresh")
    }
    
    /**
     * Obtém base URL da requisição
     */
    private fun getBaseUrl(chain: Interceptor.Chain): String {
        val request = chain.request()
        return "${request.url.scheme}://${request.url.host}:${request.url.port}"
    }
}
