package com.inventario.mobile.data.observer

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observer Pattern - Observa mudanças de conectividade
 */
interface ConnectivityObserver {
    
    /**
     * Observa mudanças de conectividade
     */
    fun observe(): Flow<Status>
    
    /**
     * Status de conectividade
     */
    enum class Status {
        AVAILABLE,      // Conectado
        UNAVAILABLE,    // Desconectado
        LOSING,         // Perdendo conexão
        LOST            // Conexão perdida
    }
}

/**
 * Implementação do ConnectivityObserver usando NetworkCallback
 */
class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {
    
    companion object {
        private const val TAG = "ConnectivityObserver"
    }
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "✓ Rede disponível")
                trySend(ConnectivityObserver.Status.AVAILABLE)
            }
            
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Log.w(TAG, "⚠ Perdendo conexão (${maxMsToLive}ms)")
                trySend(ConnectivityObserver.Status.LOSING)
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.w(TAG, "✗ Conexão perdida")
                trySend(ConnectivityObserver.Status.LOST)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                Log.w(TAG, "✗ Rede indisponível")
                trySend(ConnectivityObserver.Status.UNAVAILABLE)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Enviar status inicial
        val isConnected = connectivityManager.activeNetwork != null
        trySend(
            if (isConnected) ConnectivityObserver.Status.AVAILABLE 
            else ConnectivityObserver.Status.UNAVAILABLE
        )
        
        awaitClose {
            Log.d(TAG, "Desregistrando callback de conectividade")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
