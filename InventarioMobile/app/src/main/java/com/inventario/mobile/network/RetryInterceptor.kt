package com.inventario.mobile.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Interceptor para retry automático com backoff exponencial
 * 
 * Tenta novamente requisições que falharam por problemas de rede,
 * com intervalo crescente entre tentativas.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L
) : Interceptor {
    
    companion object {
        private const val TAG = "RetryInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var delay = initialDelayMs
        var lastException: IOException? = null
        
        while (attempt < maxRetries) {
            try {
                Log.d(TAG, "Tentativa ${attempt + 1}/$maxRetries: ${request.url}")
                return chain.proceed(request)
                
            } catch (e: IOException) {
                lastException = e
                attempt++
                
                // Não tentar novamente se for o último attempt
                if (attempt >= maxRetries) {
                    Log.e(TAG, "Todas as tentativas falharam para: ${request.url}", e)
                    throw e
                }
                
                // Verificar se vale a pena tentar novamente
                if (!shouldRetry(e)) {
                    Log.w(TAG, "Erro não recuperável, não tentando novamente: ${e.message}")
                    throw e
                }
                
                // Aguardar antes de tentar novamente (backoff exponencial)
                Log.d(TAG, "Aguardando ${delay}ms antes da próxima tentativa...")
                Thread.sleep(delay)
                
                // Dobrar o delay para próxima tentativa: 1s, 2s, 4s
                delay *= 2
            }
        }
        
        // Nunca deve chegar aqui, mas por segurança
        throw lastException ?: IOException("Falha desconhecida após $maxRetries tentativas")
    }
    
    /**
     * Determina se vale a pena tentar novamente baseado no tipo de erro
     */
    private fun shouldRetry(exception: IOException): Boolean {
        return when (exception) {
            // Timeout: vale a pena tentar novamente
            is SocketTimeoutException -> true
            
            // Sem conexão: vale a pena tentar novamente
            is UnknownHostException -> true
            
            // Outros erros de IO: tentar novamente
            else -> exception.message?.let { message ->
                message.contains("failed to connect", ignoreCase = true) ||
                message.contains("connection reset", ignoreCase = true) ||
                message.contains("broken pipe", ignoreCase = true) ||
                message.contains("network is unreachable", ignoreCase = true)
            } ?: false
        }
    }
}
