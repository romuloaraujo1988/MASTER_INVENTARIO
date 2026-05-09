package com.inventario.mobile.network

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.inventario.mobile.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
        private const val BIOMETRIC_TIMEOUT_SECONDS = 30L
    }
    
    /**
     * Listener para notificar quando token expirou
     * Será chamado quando renovação falhar
     */
    var tokenExpiredListener: TokenExpiredListener? = null
    
    /**
     * Flag para evitar múltiplas renovações simultâneas
     */
    @Volatile
    private var isRenewing = false

    @Volatile
    private var currentActivity: Activity? = null

    /**
     * Application para obter Activity atual.
     * Setter customizado para registrar o listener apenas uma vez.
     */
    var application: Application? = null
        set(value) {
            field = value
            field?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity === activity) {
                        currentActivity = null
                    }
                }
            })
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
            Log.w(TAG, "Recebido 401 Unauthorized, verificando biometria...")
            
            response.close() // Fechar resposta original
            
            // Verificar se biometria está habilitada
            val biometricEnabled = preferencesManager.isBiometricEnabled()
            Log.d(TAG, "Biometria habilitada: $biometricEnabled")
            
            if (biometricEnabled) {
                // Tentar renovação via biometria
                Log.d(TAG, "Tentando renovação via biometria...")
                val biometricSuccess = tentarRenovacaoComBiometria()
                
                if (biometricSuccess) {
                    // Retry requisição original com novo token
                    Log.d(TAG, "Token renovado via biometria, retrying requisição original...")
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${preferencesManager.getAccessToken()}")
                        .build()
                    
                    return chain.proceed(newRequest)
                }
            }
            
            // Se biometria não habilitada ou falhou, tentar renovação normal
            Log.d(TAG, "Tentando renovação normal de token...")
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
    
    /**
     * Tenta renovação de token via biometria
     * Usa CountDownLatch para sincronização entre threads
     */
    @Synchronized
    private fun tentarRenovacaoComBiometria(): Boolean {
        // Evitar múltiplas renovações simultâneas
        if (isRenewing) {
            Log.d(TAG, "Renovação já em andamento, aguardando...")
            return false
        }
        
        isRenewing = true
        
        try {
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "TENTANDO RENOVAÇÃO VIA BIOMETRIA")
            
            // Obter Activity atual
            val activity = getCurrentActivity()
            
            if (activity == null) {
                Log.w(TAG, "Activity atual não encontrada, não é possível mostrar prompt de biometria")
                return false
            }
            
            Log.d(TAG, "Activity atual: ${activity.javaClass.simpleName}")
            
            // Verificar se biometria está disponível
            val biometricManager = com.inventario.mobile.security.BiometricAuthManager(activity)
            val availability = biometricManager.isBiometricAvailable()
            
            if (!availability.isAvailable()) {
                Log.w(TAG, "Biometria não disponível: ${availability.getMessage()}")
                return false
            }
            
            // Usar CountDownLatch para aguardar resultado da biometria
            val latch = CountDownLatch(1)
            var renovacaoSucesso = false
            
            // Mostrar prompt de biometria na UI thread
            activity.runOnUiThread {
                // Cast para FragmentActivity (necessário para BiometricAuthManager)
                val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity
                
                if (fragmentActivity == null) {
                    Log.w(TAG, "Activity não é FragmentActivity, não é possível mostrar biometria")
                    renovacaoSucesso = false
                    latch.countDown()
                    return@runOnUiThread
                }
                
                biometricManager.authenticateWithCancel(
                    activity = fragmentActivity,
                    title = "Renovar Sessão",
                    subtitle = "Use biometria para renovar token",
                    description = "Toque no sensor para continuar",
                    callback = object : com.inventario.mobile.security.BiometricCallback {
                        override fun onAuthenticationSucceeded(authenticationType: String) {
                            Log.d(TAG, "✅ Biometria validada, chamando Use Case de renovação...")
                            
                            try {
                                // Criar instância do Use Case manualmente
                                // Precisamos de AuthApi e PreferencesManager
                                val context = activity.applicationContext
                                
                                // Obter Retrofit para criar AuthApi
                                val serverConfigManager = com.inventario.mobile.utils.ServerConfigManager.getInstance(context)
                                val baseUrl = serverConfigManager.getBaseUrl()
                                val finalBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                                
                                val retrofit = retrofit2.Retrofit.Builder()
                                    .baseUrl(finalBaseUrl)
                                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                                    .build()
                                
                                val authApi = retrofit.create(com.inventario.mobile.data.remote.api.AuthApi::class.java)
                                
                                // Criar Use Case
                                val renovarTokenUseCase = com.inventario.mobile.domain.usecase.RenovarTokenComBiometriaUseCase(
                                    authApi, 
                                    preferencesManager,
                                    context
                                )
                                
                                // Chamar Use Case (precisa ser em coroutine)
                                kotlinx.coroutines.runBlocking {
                                    val result = renovarTokenUseCase()
                                    
                                    if (result.isSuccess) {
                                        val loginResult = result.getOrNull()
                                        Log.d(TAG, "✓ Token renovado via biometria com sucesso")
                                        Log.d(TAG, "Usuário: ${loginResult?.fullName}")
                                        Log.d(TAG, "Online: ${loginResult?.isOnline}")
                                        renovacaoSucesso = true
                                    } else {
                                        val error = result.exceptionOrNull()
                                        Log.e(TAG, "❌ Falha ao renovar token via Use Case: ${error?.message}")
                                        renovacaoSucesso = false
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Erro ao renovar token via Use Case", e)
                                renovacaoSucesso = false
                            } finally {
                                latch.countDown()
                            }
                        }
                        
                        override fun onAuthenticationFailed(message: String) {
                            Log.w(TAG, "❌ Biometria falhou: $message")
                            renovacaoSucesso = false
                            latch.countDown()
                        }
                        
                        override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                            Log.e(TAG, "❌ Erro na biometria: $errorCode - $errorMessage")
                            renovacaoSucesso = false
                            latch.countDown()
                        }
                        
                        override fun onAuthenticationCanceled() {
                            Log.d(TAG, "Biometria cancelada pelo usuário")
                            renovacaoSucesso = false
                            latch.countDown()
                        }
                        
                        override fun onAuthenticationLockout(message: String) {
                            Log.e(TAG, "🔒 Biometria bloqueada: $message")
                            renovacaoSucesso = false
                            latch.countDown()
                        }
                    }
                )
            }
            
            // Aguardar resultado com timeout
            val completed = latch.await(BIOMETRIC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            if (!completed) {
                Log.e(TAG, "⏱️ Timeout aguardando biometria ($BIOMETRIC_TIMEOUT_SECONDS segundos)")
                return false
            }
            
            Log.d(TAG, "Resultado da renovação via biometria: $renovacaoSucesso")
            Log.d(TAG, "═══════════════════════════════════════════")
            
            return renovacaoSucesso
            
        } finally {
            isRenewing = false
        }
    }
    
    /**
     * Obtém Activity atual usando ActivityLifecycleCallbacks
     */
    private fun getCurrentActivity(): Activity? {
        if (application == null) {
            Log.w(TAG, "Application não configurada no interceptor")
            return null
        }
        
        return currentActivity
    }
}
