package com.inventario.mobile.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Utilitário para verificar status de conectividade de rede
 */
class NetworkChecker(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkChecker"
        
        @Volatile
        private var instance: NetworkChecker? = null
        
        fun getInstance(context: Context): NetworkChecker {
            return instance ?: synchronized(this) {
                instance ?: NetworkChecker(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Verifica se há conexão com a internet
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        Log.d(TAG, "isConnected: hasInternet=$hasInternet, hasValidated=$hasValidated")
        
        return hasInternet && hasValidated
    }
    
    /**
     * Verifica se está conectado via WiFi
     */
    fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Verifica se está conectado via dados móveis
     */
    fun isMobileConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    /**
     * Obtém o tipo de conexão atual
     */
    fun getConnectionType(): ConnectionType {
        if (!isConnected()) return ConnectionType.NONE
        
        return when {
            isWifiConnected() -> ConnectionType.WIFI
            isMobileConnected() -> ConnectionType.MOBILE
            else -> ConnectionType.OTHER
        }
    }
    
    /**
     * Observa mudanças no status de conectividade
     */
    fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                trySend(NetworkStatus.Available(getConnectionType()))
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost: $network")
                trySend(NetworkStatus.Lost)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d(TAG, "Network capabilities changed: $network")
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                if (hasInternet && hasValidated) {
                    trySend(NetworkStatus.Available(getConnectionType()))
                } else {
                    trySend(NetworkStatus.Lost)
                }
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Enviar status inicial
        if (isConnected()) {
            trySend(NetworkStatus.Available(getConnectionType()))
        } else {
            trySend(NetworkStatus.Lost)
        }
        
        awaitClose {
            Log.d(TAG, "Unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

/**
 * Status de conectividade de rede
 */
sealed class NetworkStatus {
    data class Available(val type: ConnectionType) : NetworkStatus()
    object Lost : NetworkStatus()
}

/**
 * Tipo de conexão
 */
enum class ConnectionType {
    WIFI,
    MOBILE,
    OTHER,
    NONE
}
