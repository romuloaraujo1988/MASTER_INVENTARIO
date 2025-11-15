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
 * Interceptor para renovação automática de token
 * Intercepta requisições com erro 401 e tenta renovar o token automaticamente
 */
@Singleton
class RefreshTokenInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    companion object {
        private const val TAG = "RefreshTokenInterceptor"
        private const val MAX_RETRY_ATTEMPTS = 1
    }
    
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
                Log.e(TAG, "Refresh token não encontrado")
                return false
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
                    val expiresIn = json.optLong("expiresIn", 86400L)
                    
                    if (newAccessToken.isNotEmpty()) {
                        // Salvar novos tokens
                        preferencesManager.saveTokens(newAccessToken, newRefreshToken, expiresIn)
                        
                        Log.d(TAG, "✓ Token renovado com sucesso!")
                        return true
                    }
                }
            } else {
                Log.e(TAG, "Falha ao renovar token: ${refreshResponse.code}")
            }
            
            refreshResponse.close()
            false
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao renovar token", e)
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
