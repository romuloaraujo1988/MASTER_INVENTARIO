package com.inventario.mobile.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.GzipSource
import okio.buffer

/**
 * Interceptor para adicionar suporte a compressão GZIP
 * Reduz significativamente o tamanho dos dados transferidos
 */
class CompressionInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "CompressionInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Adicionar header Accept-Encoding para solicitar compressão
        val compressedRequest = originalRequest.newBuilder()
            .header("Accept-Encoding", "gzip")
            .build()
        
        val response = chain.proceed(compressedRequest)
        
        // Log do tamanho da resposta
        val contentLength = response.body?.contentLength() ?: 0
        val encoding = response.header("Content-Encoding")
        
        if (encoding == "gzip") {
            Log.d(TAG, "Resposta comprimida (gzip): ${contentLength} bytes")
        } else {
            Log.d(TAG, "Resposta não comprimida: ${contentLength} bytes")
        }
        
        return response
    }
}
