package com.inventario.mobile.data.realtime

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente WebSocket que recebe o feed de coletas em tempo real do servidor.
 *
 * Endpoint: `ws[s]://{servidor}/ws/coletas-live?inventarioId={id}` quando há
 * inventário ativo, sem query string para receber eventos de todos os
 * inventários (modo administrativo / monitor global).
 *
 * ### Por que OkHttp puro e não STOMP?
 * O backend expõe um handler raw em `/ws/coletas-live` justamente para o app
 * Android — STOMP exigiria mais uma dependência (NaikSoftware/StompProtocol)
 * só para um feed unidirecional trivial. OkHttp 4 já está no projeto.
 *
 * ### Reconnect
 * - Tentativas com backoff exponencial: 1s, 2s, 4s, 8s, 16s, 30s (cap)
 * - Reset do backoff a cada conexão bem-sucedida
 * - `disconnect()` cancela o job e impede reconnect — chamado quando a
 *   tela "monitor ao vivo" sai de foreground
 *
 * ### Threading
 * - OkHttp executa callbacks em pool próprio
 * - Conversão JSON → `ColetaEvento` é leve, feita no callback diretamente
 * - Distribuição via `MutableSharedFlow` com replay=0 (apenas eventos a partir
 *   da assinatura) e `extraBufferCapacity=64` para não bloquear o emissor se
 *   o consumidor está lento
 *
 * ### Singleton
 * Apenas uma conexão ativa por instalação do app, compartilhada entre
 * eventuais telas que queiram observar (atualmente só
 * `CollectionViewActivity` no modo live).
 */
@Singleton
class RealtimeColetasManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {

    companion object {
        private const val TAG = "RealtimeColetasManager"
        private const val PING_INTERVAL_SECONDS = 25L
        private const val MAX_BACKOFF_MS = 30_000L
    }

    /**
     * Estados expostos para a UI. Distinção entre `Connecting` e `Connected`
     * permite mostrar um indicador visual sutil ("conectando..." vs "ao vivo").
     */
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val mensagem: String) : ConnectionState()
    }

    private val gson = Gson()

    /**
     * OkHttp dedicado para o WebSocket. Usar a instância compartilhada do
     * Retrofit funciona, mas duplicar aqui isola tuning (ping interval) sem
     * afetar o resto da app.
     */
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // sem timeout — conexão persistente
            .build()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentSocket: WebSocket? = null
    private var connectionJob: Job? = null

    /** Flag pra impedir reconexão após `disconnect()` explícito. */
    private val shouldReconnect = AtomicBoolean(false)

    private val _events = MutableSharedFlow<ColetaEvento>(
        replay = 0,
        extraBufferCapacity = 64
    )

    /** Stream de eventos. Cada coleta registrada chega aqui. */
    val events: SharedFlow<ColetaEvento> = _events.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /**
     * Conta o número de tentativas consecutivas de conexão sem sucesso.
     * Usado para o backoff exponencial.
     */
    private var attemptCount = 0

    /**
     * Inicia (ou retoma) a conexão. Idempotente — chamar várias vezes em
     * sequência não cria múltiplos sockets.
     *
     * @param inventarioId quando informado, recebe apenas eventos do inventário.
     *                     Quando `null`, inscreve-se no bucket global.
     */
    fun connect(inventarioId: Int? = preferencesManager.getInventarioAtivoId()) {
        if (currentSocket != null && _connectionState.value == ConnectionState.Connected) {
            Log.d(TAG, "Conexão já ativa — ignorando connect()")
            return
        }
        shouldReconnect.set(true)
        attemptCount = 0

        connectionJob?.cancel()
        connectionJob = scope.launch { abrirConexao(inventarioId) }
    }

    /**
     * Encerra a conexão e impede reconexões automáticas.
     * Chamar em `onPause` ou quando a tela live sai de foco.
     */
    fun disconnect() {
        shouldReconnect.set(false)
        connectionJob?.cancel()
        connectionJob = null
        currentSocket?.close(1000, "client disconnect")
        currentSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private suspend fun abrirConexao(inventarioId: Int?) {
        val baseUrl = preferencesManager.getServerUrl()?.trim()?.trimEnd('/')
        if (baseUrl.isNullOrBlank()) {
            _connectionState.value = ConnectionState.Error("Servidor não configurado")
            return
        }

        val wsUrl = construirWsUrl(baseUrl, inventarioId)
        Log.d(TAG, "Abrindo WS: $wsUrl")

        _connectionState.value = ConnectionState.Connecting

        val request = Request.Builder()
            .url(wsUrl)
            // O servidor não exige JWT no handshake hoje (ver TODO em
            // MobileSecurityConfig). Já mandamos o header pra estar pronto
            // quando o middleware de auth for adicionado.
            .apply {
                preferencesManager.getAccessToken()?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        currentSocket = httpClient.newWebSocket(request, listener)
    }

    /**
     * Converte a URL HTTP do servidor para o esquema WebSocket equivalente:
     *   http base  -> ws://host:porta/inventario/ws-coletas-live
     *   https base -> wss://host:porta/inventario/ws-coletas-live
     *
     * Remove barra final antes de apender o path para evitar barra dupla.
     *
     * NOTA: o path usa hifen ("ws-coletas-live", fora do namespace do endpoint
     * STOMP) porque o STOMP usa SockJS, que captura todo o subcaminho e
     * rejeitava o handshake do handler raw com "Invalid SockJS path".
     */
    private fun construirWsUrl(httpBaseUrl: String, inventarioId: Int?): String {
        val schemeReplaced = when {
            httpBaseUrl.startsWith("https://") -> httpBaseUrl.replaceFirst("https://", "wss://")
            httpBaseUrl.startsWith("http://") -> httpBaseUrl.replaceFirst("http://", "ws://")
            else -> "ws://$httpBaseUrl"
        }
        // Remove barra final — getServerUrl() retorna "http://ip:porta/inventario/"
        val base = schemeReplaced.trimEnd('/')
        val query = if (inventarioId != null) "?inventarioId=$inventarioId" else ""
        return "$base/ws-coletas-live$query"
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WS conectado: ${response.code}")
            attemptCount = 0
            _connectionState.value = ConnectionState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Cada mensagem é um JSON de ColetaEvento. Parse defensivo —
            // mensagens inválidas são logadas e descartadas, sem derrubar
            // a conexão (o servidor pode enviar pings/headers no futuro).
            try {
                val evento = gson.fromJson(text, ColetaEvento::class.java)
                if (evento != null) {
                    val emitido = _events.tryEmit(evento)
                    if (!emitido) {
                        Log.w(TAG, "Buffer cheio — evento descartado: ${evento.id}")
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.w(TAG, "JSON inválido recebido: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao processar mensagem WS: ${e.message}")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WS fechando: $code / $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WS fechado: $code / $reason")
            _connectionState.value = ConnectionState.Disconnected
            currentSocket = null
            agendarReconexaoSeNecessario()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val msg = t.message ?: "falha desconhecida"
            Log.w(TAG, "WS falha: $msg (HTTP=${response?.code})")
            _connectionState.value = ConnectionState.Error(msg)
            currentSocket = null
            agendarReconexaoSeNecessario()
        }
    }

    /**
     * Agenda a próxima tentativa de conexão respeitando o backoff exponencial.
     * Não faz nada se `disconnect()` foi chamado.
     */
    private fun agendarReconexaoSeNecessario() {
        if (!shouldReconnect.get()) return

        val delayMs = (1_000L shl attemptCount.coerceAtMost(5)).coerceAtMost(MAX_BACKOFF_MS)
        attemptCount++

        Log.d(TAG, "Reconectando em ${delayMs}ms (tentativa $attemptCount)")

        connectionJob?.cancel()
        connectionJob = scope.launch {
            delay(delayMs)
            if (shouldReconnect.get()) {
                abrirConexao(preferencesManager.getInventarioAtivoId())
            }
        }
    }
}
