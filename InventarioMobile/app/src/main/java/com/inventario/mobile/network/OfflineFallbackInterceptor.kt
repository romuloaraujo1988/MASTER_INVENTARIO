package com.inventario.mobile.network

import android.content.Context
import android.util.Log
import com.inventario.mobile.utils.OfflineNotificationManager
import com.inventario.mobile.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Interceptor que detecta falhas de conexão e ativa automaticamente o modo offline
 * 
 * Funcionalidades:
 * - Detecta erros de rede (timeout, host não encontrado, etc)
 * - Ativa modo offline forçado automaticamente
 * - Registra tentativas de conexão
 * - Permite retry automático quando conexão voltar
 * - Notifica usuário via notificações
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
            "/dashboard/estatisticas-por-status"
        )
    }
    
    // Instância por objeto (não estático) para evitar estado compartilhado entre testes
    private var consecutiveFailures = 0
    


    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            // Tentar fazer a requisição
            val response = chain.proceed(request)
            
            // Se sucesso, resetar contador de falhas
            if (response.isSuccessful) {
                if (consecutiveFailures > 0) {
                    Log.d(TAG, "✓ Conexão restaurada após $consecutiveFailures falhas")
                    consecutiveFailures = 0
                    
                    // Desativar modo offline forçado se estava ativo
                    if (preferencesManager.isForceOfflineMode()) {
                        Log.d(TAG, "Desativando modo offline forçado - conexão restaurada")
                        preferencesManager.setForceOfflineMode(false)
                    }
                }
            }
            
            response
            
        } catch (e: Exception) {
            // Detectar tipo de erro de rede
            val isNetworkError = when (e) {
                is UnknownHostException -> true  // Servidor não encontrado
                is SocketTimeoutException -> true  // Timeout
                is IOException -> true  // Erro de I/O genérico
                else -> false
            }
            
            if (isNetworkError) {
                consecutiveFailures++
                
                Log.w(TAG, "═══════════════════════════════════════")
                Log.w(TAG, "FALHA DE CONEXÃO DETECTADA")
                Log.w(TAG, "═══════════════════════════════════════")
                Log.w(TAG, "URL: ${request.url}")
                Log.w(TAG, "Erro: ${e.javaClass.simpleName}")
                Log.w(TAG, "Mensagem: ${e.message}")
                Log.w(TAG, "Falhas consecutivas: $consecutiveFailures")
                
                // Verificar se é um endpoint de busca/consulta
                val isSearchEndpoint = SEARCH_ENDPOINTS.any { request.url.toString().contains(it) }
                
                // Se atingiu o limite de falhas E não é endpoint de busca, ativar modo offline forçado
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES && !isSearchEndpoint) {
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
     * Reseta contador de falhas manualmente
     */
    fun resetFailureCount() {
        consecutiveFailures = 0
        Log.d(TAG, "Contador de falhas resetado")
    }
    
    /**
     * Obtém número de falhas consecutivas
     */
    fun getConsecutiveFailures(): Int = consecutiveFailures
}
