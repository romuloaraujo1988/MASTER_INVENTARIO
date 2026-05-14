package com.inventario.mobile.network

import android.content.Context
import android.util.Log
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.PreferencesManager
import kotlinx.coroutines.CancellationException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.InterruptedIOException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Interceptor que detecta falhas de conexão e ativa automaticamente o modo offline.
 *
 * ## Funcionalidades
 * - Detecta erros de rede (timeout, host não encontrado, etc.)
 * - Ativa modo offline forçado automaticamente após várias falhas seguidas
 * - Registra tentativas de conexão e log de diagnóstico
 * - Permite retry automático quando a conexão voltar
 * - Notifica o usuário via notificações do sistema
 *
 * ## Correções maio/2026 (correcoes-seguranca)
 *
 * **AL-05:** em qualquer sucesso (mesmo 3xx/4xx, não só 2xx), o contador e a
 * flag "Modo Offline Forçado" são resetados. Antes, apenas 2xx resetava
 * o contador, então respostas 4xx (que significam "servidor vivo mas
 * requisição rejeitada") deixavam o app preso em offline falso.
 *
 * **MD-09:** `InterruptedIOException` e `CancellationException` vindas de
 * cancelamento de coroutines (usuário navegando entre telas) deixam de
 * contar como falha de rede. Antes, 10 navegações rápidas podiam ativar
 * modo offline forçado em Wi-Fi saudável.
 *
 * **Atomicidade:** contador virou [AtomicInteger] para eliminar corridas em
 * múltiplas chamadas paralelas (a instância é `@Singleton` compartilhada
 * por todas as APIs Hilt).
 */
class OfflineFallbackInterceptor @Inject constructor(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val notificationManager: OfflineNotificationManager
) : Interceptor {

    companion object {
        private const val TAG = "OfflineFallbackInterceptor"
        // Bug-fix 08/05/2026: aumentado de 3 para 10. O limite anterior era
        // agressivo demais — travava todo o app em modo offline após três
        // falhas de rede transitórias (ex.: servidor reiniciando). 10 falhas
        // consecutivas sem nenhum sucesso é um sinal mais confiável de que
        // o servidor realmente está indisponível.
        private const val MAX_CONSECUTIVE_FAILURES = 10

        // Endpoints de busca/consulta que NÃO devem ativar modo offline forçado
        // pois são operações de leitura que podem falhar sem impacto crítico
        private val SEARCH_ENDPOINTS = listOf(
            "/buscar",
            "/patrimonio/numero/",
            "/patrimonio/buscar",
            // Endpoints de leitura do dashboard: uma falha transitória não deve
            // ativar modo offline forçado, senão a tela inteira fica travada.
            "/dashboard/stats",
            "/dashboard/evolucao",
            "/dashboard/top-itens",
            "/dashboard/distribuicao-por-sala",
            "/dashboard/estatisticas-por-status",
            "/dashboard/top-coletores",
            "/dashboard/setores-pendentes",
            "/dashboard/projecao-termino"
        )
    }

    // AL-05: AtomicInteger evita corrida em chamadas paralelas concorrentes.
    private val consecutiveFailures = AtomicInteger(0)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            val response = chain.proceed(request)

            // AL-05: QUALQUER resposta do servidor (2xx/3xx/4xx/5xx) significa
            // que o servidor está vivo — resetar o estado de offline forçado.
            // Antes, apenas 2xx resetava, então respostas 401/403/409 deixavam
            // o contador alto sem justificativa.
            onSuccessfulResponse()

            response

        } catch (e: Throwable) {
            // MD-09: cancelamento de coroutine não é falha de rede.
            // O InterruptedIOException vem quando uma chamada suspensa é
            // cancelada (navegação entre telas, onCleared de ViewModel).
            // CancellationException é a variante Kotlin. Ambas devem
            // ser re-lançadas sem contar como "falha consecutiva".
            if (e is CancellationException || e is InterruptedIOException) {
                Log.d(TAG, "Requisição cancelada (navegação/onCleared) — não conta como falha")
                throw e
            }

            // Detectar tipo de erro de rede
            val isNetworkError = when (e) {
                is UnknownHostException -> true  // Servidor não encontrado
                is SocketTimeoutException -> true  // Timeout
                is IOException -> true  // Erro de I/O genérico
                else -> false
            }

            if (isNetworkError) {
                val failures = consecutiveFailures.incrementAndGet()

                Log.w(TAG, "═══════════════════════════════════════")
                Log.w(TAG, "FALHA DE CONEXÃO DETECTADA")
                Log.w(TAG, "═══════════════════════════════════════")
                Log.w(TAG, "URL: ${request.url}")
                Log.w(TAG, "Erro: ${e.javaClass.simpleName}")
                Log.w(TAG, "Mensagem: ${e.message}")
                Log.w(TAG, "Falhas consecutivas: $failures")

                // Verificar se é um endpoint de busca/consulta
                val isSearchEndpoint = SEARCH_ENDPOINTS.any { request.url.toString().contains(it) }

                // Se atingiu o limite de falhas E não é endpoint de busca, ativar modo offline forçado
                if (failures >= MAX_CONSECUTIVE_FAILURES && !isSearchEndpoint) {
                    if (!preferencesManager.isForceOfflineMode()) {
                        Log.w(TAG, "⚠️ ATIVANDO MODO OFFLINE FORÇADO")
                        Log.w(TAG, "Motivo: $MAX_CONSECUTIVE_FAILURES falhas consecutivas")
                        preferencesManager.setForceOfflineMode(true)

                        // Notificar usuário via notificação
                        notificationManager.showOfflineModeNotification(hasLocalData = true)
                    }
                } else if (isSearchEndpoint) {
                    Log.d(TAG, "ℹ️ Falha em endpoint de busca - não ativa modo offline forçado")
                }

                Log.w(TAG, "═══════════════════════════════════════")
            }

            // Re-lançar exceção para que seja tratada normalmente
            throw e
        }
    }

    /**
     * Chamado em QUALQUER resposta HTTP recebida do servidor.
     *
     * AL-05 (maio/2026): desde que o servidor responda (qualquer código,
     * incluindo 4xx/5xx), a flag de modo offline forçado é desativada. Antes,
     * apenas respostas 2xx limpavam o estado, então o usuário podia ficar
     * preso em offline falso após uma sequência de 401/403 em chamadas
     * paralelas — cenário comum na renovação simultânea de token.
     */
    private fun onSuccessfulResponse() {
        val prev = consecutiveFailures.getAndSet(0)
        if (prev > 0) {
            Log.d(TAG, "✓ Conexão confirmada (servidor respondeu) — $prev falhas descartadas")
        }
        if (preferencesManager.isForceOfflineMode()) {
            Log.d(TAG, "Desativando modo offline forçado — servidor acessível")
            preferencesManager.setForceOfflineMode(false)
        }
    }

    /**
     * Reseta contador de falhas manualmente.
     */
    fun resetFailureCount() {
        consecutiveFailures.set(0)
        Log.d(TAG, "Contador de falhas resetado manualmente")
    }

    /**
     * Obtém número de falhas consecutivas.
     */
    fun getConsecutiveFailures(): Int = consecutiveFailures.get()
}
