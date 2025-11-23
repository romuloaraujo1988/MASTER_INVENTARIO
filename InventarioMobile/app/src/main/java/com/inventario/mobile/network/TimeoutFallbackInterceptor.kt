package com.inventario.mobile.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

/**
 * Interceptor que trata timeouts e erros de conexão de forma graciosa
 * 
 * Funcionalidades:
 * - Captura SocketTimeoutException
 * - Captura UnknownHostException
 * - Loga erros de forma clara
 * - Permite que fallback funcione sem mostrar erros ao usuário
 */
class TimeoutFallbackInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "TimeoutFallbackInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        return try {
            Log.d(TAG, "Tentando requisição: $url")
            val response = chain.proceed(request)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✓ Requisição bem-sucedida: $url (${response.code})")
            } else {
                Log.w(TAG, "⚠️ Requisição falhou: $url (${response.code})")
            }
            
            response
            
        } catch (e: SocketTimeoutException) {
            // Timeout - servidor não respondeu a tempo
            Log.w(TAG, "⏱️ Timeout na requisição: $url (${e.message})")
            Log.d(TAG, "Fallback para dados locais será acionado automaticamente")
            
            // Re-lançar exceção para que o repository possa fazer fallback
            throw e
            
        } catch (e: UnknownHostException) {
            // Servidor não encontrado - provavelmente offline
            Log.w(TAG, "🔌 Servidor não encontrado: $url")
            Log.d(TAG, "Dispositivo provavelmente está offline")
            
            // Re-lançar exceção para que o repository possa fazer fallback
            throw e
            
        } catch (e: IOException) {
            // Outros erros de I/O
            Log.w(TAG, "❌ Erro de I/O na requisição: $url (${e.javaClass.simpleName}: ${e.message})")
            
            // Re-lançar exceção para que o repository possa fazer fallback
            throw e
            
        } catch (e: Exception) {
            // Outros erros inesperados
            Log.e(TAG, "💥 Erro inesperado na requisição: $url", e)
            
            // Re-lançar exceção
            throw e
        }
    }
}
