package com.inventario.mobile.config

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import com.inventario.mobile.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Configurações específicas para Android 14
 * Resolve problemas de conectividade em dispositivos físicos
 */
object Android14NetworkConfig {
    
    private const val TAG = "Android14NetworkConfig"
    
    /**
     * Verifica se o dispositivo pode acessar o servidor
     */
    suspend fun testServerConnectivity(
        serverIp: String,
        serverPort: Int,
        timeoutMs: Int = 10000
    ): ConnectivityTestResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                Log.d(TAG, "Testando conectividade com $serverIp:$serverPort")
                
                // 1. Teste de resolução DNS
                val dnsStartTime = System.currentTimeMillis()
                val address = InetAddress.getByName(serverIp)
                val dnsTime = System.currentTimeMillis() - dnsStartTime
                Log.d(TAG, "DNS resolvido em ${dnsTime}ms: ${address.hostAddress}")
                
                // 2. Teste de conexão TCP
                val tcpStartTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(address, serverPort), timeoutMs)
                socket.close()
                val tcpTime = System.currentTimeMillis() - tcpStartTime
                Log.d(TAG, "Conexão TCP estabelecida em ${tcpTime}ms")
                
                val totalTime = System.currentTimeMillis() - startTime
                
                ConnectivityTestResult(
                    success = true,
                    totalTime = totalTime,
                    dnsTime = dnsTime,
                    tcpTime = tcpTime,
                    error = null
                )
                
            } catch (e: Exception) {
                val totalTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "Erro na conectividade: ${e.message}", e)
                
                ConnectivityTestResult(
                    success = false,
                    totalTime = totalTime,
                    dnsTime = 0,
                    tcpTime = 0,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Configura monitoramento de rede específico para Android 14
     */
    fun setupNetworkMonitoring(context: Context, callback: NetworkMonitorCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
            Log.d(TAG, "Configurando monitoramento para Android 14+")
            
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    Log.d(TAG, "Rede disponível: $network")
                    callback.onNetworkAvailable(network)
                }
                
                override fun onLost(network: android.net.Network) {
                    Log.d(TAG, "Rede perdida: $network")
                    callback.onNetworkLost(network)
                }
                
                override fun onCapabilitiesChanged(
                    network: android.net.Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    Log.d(TAG, "Capacidades da rede alteradas: $network")
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    
                    callback.onNetworkCapabilitiesChanged(network, hasInternet, hasValidated)
                }
            }
            
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }
    
    /**
     * Obtém informações detalhadas da rede atual
     */
    fun getNetworkInfo(context: Context): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return NetworkInfo(
            isConnected = activeNetwork != null,
            hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false,
            hasValidated = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false,
            isWifi = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false,
            isCellular = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false,
            linkDownstreamBandwidthKbps = networkCapabilities?.linkDownstreamBandwidthKbps ?: 0,
            linkUpstreamBandwidthKbps = networkCapabilities?.linkUpstreamBandwidthKbps ?: 0
        )
    }
    
    /**
     * Recomendações específicas para Android 14
     */
    fun getAndroid14Recommendations(context: Context): List<String> {
        val recommendations = mutableListOf<String>()
        val networkInfo = getNetworkInfo(context)
        
        if (!networkInfo.hasValidated) {
            recommendations.add("Rede não validada - verifique a conectividade com a internet")
        }
        
        if (networkInfo.isCellular) {
            recommendations.add("Usando dados móveis - considere conectar ao WiFi para melhor estabilidade")
        }
        
        if (networkInfo.linkDownstreamBandwidthKbps < 1000) {
            recommendations.add("Largura de banda baixa - pode afetar a sincronização")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            recommendations.add("Android 14 detectado - usando configurações otimizadas")
        }
        
        return recommendations
    }
    
    data class ConnectivityTestResult(
        val success: Boolean,
        val totalTime: Long,
        val dnsTime: Long,
        val tcpTime: Long,
        val error: String?
    )
    
    data class NetworkInfo(
        val isConnected: Boolean,
        val hasInternet: Boolean,
        val hasValidated: Boolean,
        val isWifi: Boolean,
        val isCellular: Boolean,
        val linkDownstreamBandwidthKbps: Int,
        val linkUpstreamBandwidthKbps: Int
    )
    
    interface NetworkMonitorCallback {
        fun onNetworkAvailable(network: android.net.Network)
        fun onNetworkLost(network: android.net.Network)
        fun onNetworkCapabilitiesChanged(network: android.net.Network, hasInternet: Boolean, hasValidated: Boolean)
    }
}
