package com.inventario.mobile.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Interceptor para configurar cache HTTP
 * 
 * Reduz requisições ao servidor em 50-70% ao cachear respostas
 * que não mudam frequentemente.
 */
class CacheInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Determinar tempo de cache baseado no endpoint
        val cacheControl = when {
            // Dashboard stats: NÃO cachear — a FonteEstatisticas do
            // DashboardViewModelClean depende de que cada refresh bata o
            // servidor de verdade (se mudou alguma coleta, o dashboard
            // precisa refletir em tempo real). Bug 07/05/2026: o cache de
            // 2 min estava fazendo as requisições sumirem após a primeira
            // chamada, impedindo o dashboard de atualizar. (Req 5.1 —
            // endpoint `api/mobile/dashboard/stats` permanece intacto; só
            // a política de cache HTTP é que muda.)
            request.url.encodedPath.contains("/dashboard/stats") -> {
                CacheControl.Builder()
                    .noCache()
                    .noStore()
                    .build()
            }
            
            // Lista de salas: cache por 30 minutos (muda raramente)
            request.url.encodedPath.contains("/salas") -> {
                CacheControl.Builder()
                    .maxAge(30, TimeUnit.MINUTES)
                    .build()
            }
            
            // Lista de inventários: cache por 15 minutos
            request.url.encodedPath.contains("/inventarios") -> {
                CacheControl.Builder()
                    .maxAge(15, TimeUnit.MINUTES)
                    .build()
            }
            
            // Coletas: cache por 1 minuto (muda frequentemente)
            request.url.encodedPath.contains("/coletas") -> {
                CacheControl.Builder()
                    .maxAge(1, TimeUnit.MINUTES)
                    .build()
            }
            
            // POST/PUT/DELETE: não cachear
            request.method != "GET" -> {
                CacheControl.Builder()
                    .noCache()
                    .noStore()
                    .build()
            }
            
            // Padrão: cache por 5 minutos
            else -> {
                CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES)
                    .build()
            }
        }
        
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .removeHeader("Pragma") // Remove header que pode interferir
            .build()
    }
}
