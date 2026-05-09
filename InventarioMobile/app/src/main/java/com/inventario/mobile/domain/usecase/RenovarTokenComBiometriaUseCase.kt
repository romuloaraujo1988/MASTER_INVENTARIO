package com.inventario.mobile.domain.usecase

import android.util.Log
import com.inventario.mobile.data.remote.api.AuthApi
import com.inventario.mobile.utils.PreferencesManager
import javax.inject.Inject

/**
 * Use Case: Renovar token usando biometria
 * 
 * Quando o usuário faz login com biometria, este Use Case:
 * 1. Pega o refresh token salvo localmente
 * 2. Chama o endpoint /api/mobile/auth/refresh no servidor
 * 3. Recebe um novo access token válido
 * 4. Salva o novo token localmente
 * 5. Retorna os dados do usuário atualizados
 * 
 * Benefícios:
 * - Token sempre válido após login biométrico
 * - Sessão renovada no servidor
 * - Segurança mantida (biometria + token válido)
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
class RenovarTokenComBiometriaUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {
    
    companion object {
        private const val TAG = "RenovarTokenBiometria"
    }
    
    /**
     * Renova o token usando refresh token salvo
     * 
     * @return Result com dados do usuário ou erro
     */
    suspend operator fun invoke(): Result<BiometricLoginResult> {
        return try {
            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "RENOVANDO TOKEN COM BIOMETRIA")
            
            // 1. Verificar se há refresh token salvo
            val refreshToken = preferencesManager.getRefreshToken()
            
            if (refreshToken.isNullOrBlank()) {
                Log.e(TAG, "❌ Refresh token não encontrado")
                Log.e(TAG, "═══════════════════════════════════════════")
                return Result.failure(Exception("Refresh token não encontrado. Faça login novamente."))
            }
            
            Log.d(TAG, "✓ Refresh token encontrado: ${refreshToken.take(20)}...")
            
            // 2. Chamar endpoint de refresh
            Log.d(TAG, "Chamando /api/mobile/auth/refresh...")
            val response = authApi.refreshToken(refreshToken)
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Falha ao renovar token")
                Log.e(TAG, "Status: ${response.code()}")
                Log.e(TAG, "Erro: $errorBody")
                Log.e(TAG, "═══════════════════════════════════════════")
                
                // Se for um erro 401 (Unauthorized) ou 403 (Forbidden),
                // significa que o refresh token expirou ou é inválido no servidor.
                // NÃO podemos fazer fallback para offline, pois a sessão está morta.
                if (response.code() == 401 || response.code() == 403) {
                    Log.w(TAG, "⚠️ Refresh token expirado (${response.code()}) — tentando fallback offline...")
                    Log.w(TAG, "Biometria NÃO será desabilitada. Usuário pode renovar fazendo login com senha.")
                    // ✅ CORREÇÃO: NÃO desabilitar biometria, NÃO apagar sessão.
                    // O usuário ainda pode acessar offline com o token atual salvo.
                    // Se precisar de acesso online, o fluxo normal de senha cuidará disso.
                    return tryOfflineLogin()
                }
                
                Log.d(TAG, "Tentando fallback para login offline devido à falha do servidor...")
                return tryOfflineLogin()
            }
            
            val loginResponse = response.body()
            
            if (loginResponse == null) {
                Log.e(TAG, "❌ Resposta vazia do servidor")
                Log.e(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "Tentando fallback para login offline devido à resposta vazia...")
                return tryOfflineLogin()
            }
            
            // 3. Salvar novo access token
            val newAccessToken = loginResponse.accessToken
            val username = loginResponse.user?.username
            val fullName = loginResponse.user?.nome
            
            Log.d(TAG, "✅ Token renovado com sucesso!")
            Log.d(TAG, "Usuário: $fullName ($username)")
            Log.d(TAG, "Novo access token: ${newAccessToken?.take(20)}...")
            Log.d(TAG, "Expires in: ${loginResponse.expiresIn}s")
            
            // 4. Atualizar token salvo
            if (newAccessToken != null) {
                // Usar saveTokens para salvar access e refresh token
                preferencesManager.saveTokens(
                    accessToken = newAccessToken,
                    refreshToken = refreshToken,
                    expiresIn = loginResponse.expiresIn ?: 86400L
                )
                Log.d(TAG, "✓ Novo access token salvo no PreferencesManager")
                
                // ATUALIZAR LocalDataManager TAMBÉM para evitar que o AuthInterceptor use o token antigo
                val localDataManager = com.inventario.mobile.data.local.LocalDataManager.getInstance(context)
                val currentUser = localDataManager.getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        accessToken = newAccessToken,
                        refreshToken = refreshToken,
                        tokenExpiry = System.currentTimeMillis() + ((loginResponse.expiresIn ?: 86400L) * 1000)
                    )
                    localDataManager.saveCurrentUserSuspend(updatedUser)
                    Log.d(TAG, "✓ Novo access token salvo no LocalDataManager")
                }
            }
            
            // 5. Atualizar dados do usuário se necessário
            if (username != null && fullName != null) {
                preferencesManager.saveUserForOfflineLogin(username, fullName, newAccessToken ?: "")
                Log.d(TAG, "✓ Dados do usuário atualizados")
            }
            
            Log.d(TAG, "═══════════════════════════════════════════")
            
            // 6. Retornar resultado
            Result.success(
                BiometricLoginResult(
                    username = username ?: "",
                    fullName = fullName ?: "",
                    accessToken = newAccessToken ?: "",
                    refreshToken = refreshToken,
                    expiresIn = loginResponse.expiresIn ?: 0L,
                    isOnline = true
                )
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao renovar token", e)
            Log.e(TAG, "═══════════════════════════════════════════")
            
            // Se falhar, tentar login offline com token antigo
            Log.d(TAG, "Tentando fallback para login offline...")
            tryOfflineLogin()
        }
    }
    
    /**
     * Fallback: Login offline com token antigo (se servidor inacessível)
     */
    private fun tryOfflineLogin(): Result<BiometricLoginResult> {
        return try {
            var username = preferencesManager.getSavedUsername()
            var fullName = preferencesManager.getSavedUserFullName()
            var accessToken = preferencesManager.getAccessToken()
            val refreshToken = preferencesManager.getRefreshToken()
            
            // Fallback para LocalDataManager (não foi apagado na migração)
            if (username.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
                val localData = com.inventario.mobile.data.local.LocalDataManager.getInstance(context)
                val fallbackUser = localData.getUserName()
                val fallbackToken = localData.getToken()
                if (!fallbackUser.isNullOrEmpty() && !fallbackToken.isNullOrEmpty()) {
                    username = fallbackUser
                    fullName = fallbackUser
                    accessToken = fallbackToken
                    
                    // Re-salvar no PreferencesManager atualizado
                    preferencesManager.saveUserForOfflineLogin(username, fullName, accessToken)
                }
            }
            
            if (username != null && fullName != null && accessToken != null) {
                Log.d(TAG, "✓ Login offline com token antigo")
                Log.d(TAG, "⚠️ Token pode estar expirado")
                
                // Verificar se tem internet pra não fingir offline sem precisão
                val isDeviceOnline = com.inventario.mobile.utils.NetworkUtils.isNetworkAvailable(context)
                
                Result.success(
                    BiometricLoginResult(
                        username = username,
                        fullName = fullName,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresIn = 0L,
                        isOnline = isDeviceOnline
                    )
                )
            } else {
                Log.e(TAG, "❌ Dados locais incompletos")
                Result.failure(Exception("Dados locais inválidos. Conecte-se à internet."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro no fallback offline", e)
            Result.failure(e)
        }
    }
}

/**
 * Resultado do login biométrico
 */
data class BiometricLoginResult(
    val username: String,
    val fullName: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val isOnline: Boolean
)
