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
 * Interface para notificar quando token expirou e não pode ser renovado.
 */
interface TokenExpiredListener {
    fun onTokenExpired()
}

/**
 * Interceptor para renovação automática de tokens JWT.
 *
 * ## Estratégia
 *
 * - **Renovação preventiva:** se [PreferencesManager.isTokenExpiringSoon]
 *   indica que o *access token* está próximo do vencimento, uma
 *   requisição a `/auth/refresh` é feita **antes** da requisição original.
 * - **Renovação reativa:** se o servidor retornar HTTP 401 para uma
 *   requisição autenticada, o interceptor tenta renovar e repete a
 *   requisição com o novo token.
 * - **Falha terminal:** se ambas as tentativas falharem, o
 *   [tokenExpiredListener] é notificado para disparar *logout*.
 *
 * ## Correções — maio/2026 (correcoes-seguranca)
 *
 * **AL-01:** todas as renovações agora são protegidas por `synchronized`
 * (`refreshLock`). Antes, duas chamadas paralelas recebendo 401 podiam
 * disparar `POST /auth/refresh` simultaneamente — a segunda falhava porque
 * o servidor invalida *refresh tokens* de uso único após consumo, e isso
 * acionava `onTokenExpired` indevidamente (*logout* espúrio).
 *
 * **CR-05:** removido `runBlocking` dentro do callback de biometria. A
 * renovação via biometria NÃO é mais acionada por este interceptor — ela
 * passou a ser responsabilidade da `LoginActivity` e `SessionManager`,
 * chamadas explicitamente quando o [tokenExpiredListener] sinaliza falha.
 * Isso elimina o risco de *deadlock* da *thread* de I/O do OkHttp com
 * `CountDownLatch.await(30s)`.
 *
 * A lógica ficou significativamente mais simples e rastreável: o
 * interceptor agora tem uma única responsabilidade — renovar JWT quando
 * possível ou propagar a falha. Tudo que envolve UI, biometria e redirect
 * para `LoginActivity` vive fora do pipeline HTTP.
 */
@Singleton
class RefreshTokenInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    companion object {
        private const val TAG = "RefreshTokenInterceptor"
        /** Janela em ms após um refresh bem-sucedido dentro da qual novos 401 são ignorados. */
        private const val REFRESH_COOLDOWN_MS = 2_000L
    }

    /**
     * Listener chamado quando a renovação falha em definitivo (refresh
     * token ausente, expirado ou rejeitado pelo servidor).
     */
    var tokenExpiredListener: TokenExpiredListener? = null

    /**
     * AL-01: mutex para serializar tentativas de refresh. O lock é
     * intencionalmente um `Object` puro + `synchronized` (e não um
     * `Mutex` de coroutines) porque o contrato do [Interceptor] é
     * síncrono — não há como aguardar em coroutine aqui sem bloquear.
     */
    private val refreshLock = Any()

    /**
     * Timestamp do último refresh bem-sucedido. Requisições que recebem
     * 401 dentro da janela [REFRESH_COOLDOWN_MS] após esse timestamp são
     * simplesmente retentadas com o token atual — sem disparar novo
     * refresh. Previne o cenário "10 chamadas paralelas + um refresh".
     */
    @Volatile
    private var lastRefreshAt: Long = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Renovação preventiva: se sabemos de antemão que o token vence em
        // breve, adiantamos o refresh. Isso evita um 401 desnecessário em
        // requisições longas (upload de foto, batch sync).
        if (preferencesManager.isTokenExpiringSoon() && !isRefreshTokenRequest(originalRequest)) {
            Log.d(TAG, "Token expirando em breve, renovando preventivamente...")
            tryRefreshTokenLocked(chain)
        }

        val response = chain.proceed(originalRequest)

        // Reativo: 401 em endpoint autenticado.
        if (response.code == 401 && !isRefreshTokenRequest(originalRequest)) {
            // AL-01: se acabamos de renovar (outra thread paralela já
            // atualizou o token dentro da janela), apenas retentar a
            // requisição original com o token atual.
            val sinceLast = System.currentTimeMillis() - lastRefreshAt
            if (sinceLast in 0..REFRESH_COOLDOWN_MS) {
                Log.d(TAG, "401 logo após refresh recente ($sinceLast ms) — retentando com token atualizado")
                response.close()
                return chain.proceed(buildAuthenticatedRequest(originalRequest))
            }

            Log.w(TAG, "Recebido 401 Unauthorized — tentando renovar token")
            response.close()

            val refreshed = tryRefreshTokenLocked(chain)
            return if (refreshed) {
                chain.proceed(buildAuthenticatedRequest(originalRequest))
            } else {
                Log.e(TAG, "Falha ao renovar token — propagando 401 ao consumidor")
                // Reproceder a request original para o chamador ter a resposta
                // original 401 (e não uma request fechada).
                chain.proceed(originalRequest)
            }
        }

        return response
    }

    /**
     * AL-01: bloqueia demais threads durante o refresh. Se duas threads
     * entrarem simultaneamente, a segunda encontra `sinceLast` dentro da
     * janela de cooldown e retorna imediatamente sem segundo HTTP call.
     */
    private fun tryRefreshTokenLocked(chain: Interceptor.Chain): Boolean {
        synchronized(refreshLock) {
            val sinceLast = System.currentTimeMillis() - lastRefreshAt
            if (sinceLast in 0..REFRESH_COOLDOWN_MS) {
                Log.d(TAG, "Refresh já concluído há $sinceLast ms — aproveitando resultado")
                return true
            }
            return doRefreshToken(chain)
        }
    }

    /**
     * Executa a requisição HTTP de refresh. Deve ser chamado apenas de
     * dentro de [tryRefreshTokenLocked], que garante exclusão mútua.
     */
    private fun doRefreshToken(chain: Interceptor.Chain): Boolean {
        return try {
            val refreshToken = preferencesManager.getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                Log.e(TAG, "Refresh token não encontrado")
                tokenExpiredListener?.onTokenExpired()
                return false
            }

            // Guarda local: refresh token expira em 14 dias desde o login.
            val loginTimestamp = preferencesManager.getLoginTimestamp()
            val refreshTokenExpirationMs = 14L * 24 * 60 * 60 * 1000
            if (loginTimestamp > 0) {
                val now = System.currentTimeMillis()
                if (now >= loginTimestamp + refreshTokenExpirationMs) {
                    val daysExpired = (now - (loginTimestamp + refreshTokenExpirationMs)) / (24 * 60 * 60 * 1000)
                    Log.e(TAG, "Refresh token expirou há $daysExpired dias — login novo é obrigatório")
                    tokenExpiredListener?.onTokenExpired()
                    return false
                }
            }

            Log.d(TAG, "Enviando requisição de refresh token...")

            val refreshRequest = Request.Builder()
                .url("${getBaseUrl(chain)}/api/mobile/auth/refresh?refreshToken=$refreshToken")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()

            val refreshResponse = chain.proceed(refreshRequest)
            try {
                if (!refreshResponse.isSuccessful) {
                    Log.e(TAG, "Servidor recusou o refresh: HTTP ${refreshResponse.code}")
                    if (refreshResponse.code == 401) {
                        Log.e(TAG, "Refresh token inválido ou expirado no servidor")
                    }
                    tokenExpiredListener?.onTokenExpired()
                    return false
                }

                val body = refreshResponse.body?.string() ?: return run {
                    Log.e(TAG, "Resposta de refresh com corpo vazio")
                    tokenExpiredListener?.onTokenExpired()
                    false
                }

                val json = JSONObject(body)
                val newAccessToken = json.optString("accessToken")
                val newRefreshToken = json.optString("refreshToken")
                val expiresIn = json.optLong("expiresIn", 172_800L) // 2 dias (padrão backend)

                if (newAccessToken.isEmpty()) {
                    Log.e(TAG, "Resposta de refresh sem accessToken")
                    tokenExpiredListener?.onTokenExpired()
                    return false
                }

                preferencesManager.saveTokens(newAccessToken, newRefreshToken, expiresIn)
                lastRefreshAt = System.currentTimeMillis()
                Log.d(TAG, "✓ Token renovado com sucesso (expira em ${preferencesManager.getTokenTimeRemaining()})")
                true
            } finally {
                refreshResponse.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao renovar token", e)
            tokenExpiredListener?.onTokenExpired()
            false
        }
    }

    /**
     * Constrói uma requisição nova com o *access token* mais recente.
     */
    private fun buildAuthenticatedRequest(original: Request): Request =
        original.newBuilder()
            .header("Authorization", "Bearer ${preferencesManager.getAccessToken()}")
            .build()

    /**
     * Verifica se a requisição é de refresh token (evita recursão).
     */
    private fun isRefreshTokenRequest(request: Request): Boolean =
        request.url.encodedPath.contains("/auth/refresh")

    /**
     * Obtém base URL da requisição (scheme://host:port).
     */
    private fun getBaseUrl(chain: Interceptor.Chain): String {
        val request = chain.request()
        return "${request.url.scheme}://${request.url.host}:${request.url.port}"
    }
}
