package com.inventario.mobile.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.*
import java.util.concurrent.TimeUnit

/**
 * Utilitários para funcionalidades de rede e conectividade
 */
object NetworkUtils {
    
    private const val DEFAULT_TIMEOUT_MS = 5000
    private const val PING_TIMEOUT_MS = 3000
    
    /**
     * Verifica se há conexão com a internet
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Verifica se está conectado via WiFi
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Verifica se está conectado via dados móveis
     */
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * Obtém tipo de conexão atual
     */
    fun getConnectionType(context: Context): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.OTHER
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return ConnectionType.NONE
            
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                ConnectivityManager.TYPE_MOBILE -> ConnectionType.MOBILE
                ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                else -> ConnectionType.OTHER
            }
        }
    }
    
    /**
     * Obtém informações detalhadas da rede
     */
    fun getNetworkInfo(context: Context): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val connectionType = getConnectionType(context)
        
        var networkName = ""
        var signalStrength = 0
        var linkSpeed = 0
        
        when (connectionType) {
            ConnectionType.WIFI -> {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                networkName = wifiInfo.ssid.removeSurrounding("\"")
                signalStrength = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
                linkSpeed = wifiInfo.linkSpeed
            }
            ConnectionType.MOBILE -> {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                networkName = telephonyManager.networkOperatorName ?: ""
                // Signal strength seria obtido via PhoneStateListener
            }
            else -> {
                // Outros tipos de conexão
            }
        }
        
        return NetworkInfo(
            isConnected = isNetworkAvailable(context),
            connectionType = connectionType,
            networkName = networkName,
            signalStrength = signalStrength,
            linkSpeed = linkSpeed,
            isMetered = isMeteredConnection(context)
        )
    }
    
    /**
     * Verifica se a conexão é limitada (dados móveis)
     */
    fun isMeteredConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered
    }
    
    /**
     * Testa conectividade com um host específico
     */
    suspend fun pingHost(
        host: String,
        port: Int = 80,
        timeoutMs: Int = PING_TIMEOUT_MS
    ): PingResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                socket.close()
                
                val responseTime = System.currentTimeMillis() - startTime
                PingResult(true, responseTime, null)
            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                PingResult(false, responseTime, e.message)
            }
        }
    }
    
    /**
     * Testa conectividade HTTP
     */
    suspend fun testHttpConnection(
        url: String,
        timeoutMs: Int = DEFAULT_TIMEOUT_MS
    ): HttpTestResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                connection.instanceFollowRedirects = false
                
                val responseCode = connection.responseCode
                val responseTime = System.currentTimeMillis() - startTime
                
                connection.disconnect()
                
                HttpTestResult(
                    success = responseCode in 200..299,
                    responseCode = responseCode,
                    responseTime = responseTime,
                    error = null
                )
            } catch (e: Exception) {
                val responseTime = System.currentTimeMillis() - startTime
                HttpTestResult(
                    success = false,
                    responseCode = -1,
                    responseTime = responseTime,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Obtém endereço IP local
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            // Erro ao obter IP
        }
        return null
    }
    
    /**
     * Obtém endereço IP público
     */
    suspend fun getPublicIpAddress(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL("https://api.ipify.org").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = DEFAULT_TIMEOUT_MS
                connection.readTimeout = DEFAULT_TIMEOUT_MS
                
                val response = connection.inputStream.bufferedReader().readText().trim()
                connection.disconnect()
                
                if (ValidationUtils.isValidIpAddress(response)) response else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Resolve hostname para IP
     */
    suspend fun resolveHostname(hostname: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(hostname)
                address.hostAddress
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Verifica se uma URL é acessível
     */
    suspend fun isUrlAccessible(
        url: String,
        timeoutMs: Int = DEFAULT_TIMEOUT_MS
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                
                val responseCode = connection.responseCode
                connection.disconnect()
                
                responseCode in 200..299
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Obtém velocidade de download estimada
     */
    suspend fun measureDownloadSpeed(
        testUrl: String = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
        timeoutMs: Int = 10000
    ): DownloadSpeedResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                val connection = URL(testUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                
                val inputStream = connection.inputStream
                val buffer = ByteArray(1024)
                var totalBytes = 0
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytes += bytesRead
                }
                
                inputStream.close()
                connection.disconnect()
                
                val endTime = System.currentTimeMillis()
                val durationSeconds = (endTime - startTime) / 1000.0
                val speedBytesPerSecond = totalBytes / durationSeconds
                val speedKbps = (speedBytesPerSecond * 8) / 1024 // Convert to Kbps
                
                DownloadSpeedResult(
                    success = true,
                    speedKbps = speedKbps,
                    bytesDownloaded = totalBytes,
                    durationMs = endTime - startTime,
                    error = null
                )
            } catch (e: Exception) {
                val endTime = System.currentTimeMillis()
                DownloadSpeedResult(
                    success = false,
                    speedKbps = 0.0,
                    bytesDownloaded = 0,
                    durationMs = endTime - startTime,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Formata velocidade de rede
     */
    fun formatNetworkSpeed(speedKbps: Double): String {
        return when {
            speedKbps < 1024 -> String.format("%.1f Kbps", speedKbps)
            speedKbps < 1024 * 1024 -> String.format("%.1f Mbps", speedKbps / 1024)
            else -> String.format("%.1f Gbps", speedKbps / (1024 * 1024))
        }
    }
    
    /**
     * Obtém qualidade da conexão baseada na velocidade
     */
    fun getConnectionQuality(speedKbps: Double): ConnectionQuality {
        return when {
            speedKbps < 150 -> ConnectionQuality.POOR
            speedKbps < 550 -> ConnectionQuality.MODERATE
            speedKbps < 2000 -> ConnectionQuality.GOOD
            else -> ConnectionQuality.EXCELLENT
        }
    }
    
    /**
     * Monitora mudanças de conectividade
     */
    fun registerNetworkCallback(
        context: Context,
        callback: ConnectivityManager.NetworkCallback
    ) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        }
    }
    
    /**
     * Remove callback de monitoramento
     */
    fun unregisterNetworkCallback(
        context: Context,
        callback: ConnectivityManager.NetworkCallback
    ) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(callback)
    }
    
    /**
     * Verifica se deve usar cache offline
     */
    fun shouldUseCacheOnly(context: Context): Boolean {
        return !isNetworkAvailable(context)
    }
    
    /**
     * Verifica se deve sincronizar dados
     */
    fun shouldSyncData(context: Context, wifiOnlySync: Boolean = false): Boolean {
        return if (wifiOnlySync) {
            isWifiConnected(context)
        } else {
            isNetworkAvailable(context)
        }
    }
    
    /**
     * Obtém timeout recomendado baseado no tipo de conexão
     */
    fun getRecommendedTimeout(context: Context): Int {
        return when (getConnectionType(context)) {
            ConnectionType.WIFI -> 5000
            ConnectionType.MOBILE -> 10000
            ConnectionType.ETHERNET -> 3000
            else -> DEFAULT_TIMEOUT_MS
        }
    }
    
    /**
     * Enums e Data Classes
     */
    enum class ConnectionType {
        NONE, WIFI, MOBILE, ETHERNET, OTHER
    }
    
    enum class ConnectionQuality {
        POOR, MODERATE, GOOD, EXCELLENT
    }
    
    data class NetworkInfo(
        val isConnected: Boolean,
        val connectionType: ConnectionType,
        val networkName: String,
        val signalStrength: Int,
        val linkSpeed: Int,
        val isMetered: Boolean
    ) {
        fun getConnectionTypeString(): String {
            return when (connectionType) {
                ConnectionType.WIFI -> "Wi-Fi"
                ConnectionType.MOBILE -> "Dados Móveis"
                ConnectionType.ETHERNET -> "Ethernet"
                ConnectionType.OTHER -> "Outro"
                ConnectionType.NONE -> "Sem Conexão"
            }
        }
        
        fun getSignalStrengthString(): String {
            return when (signalStrength) {
                0 -> "Muito Fraco"
                1 -> "Fraco"
                2 -> "Regular"
                3 -> "Bom"
                4 -> "Excelente"
                else -> "Desconhecido"
            }
        }
    }
    
    data class PingResult(
        val success: Boolean,
        val responseTime: Long,
        val error: String?
    )
    
    data class HttpTestResult(
        val success: Boolean,
        val responseCode: Int,
        val responseTime: Long,
        val error: String?
    )
    
    data class DownloadSpeedResult(
        val success: Boolean,
        val speedKbps: Double,
        val bytesDownloaded: Int,
        val durationMs: Long,
        val error: String?
    ) {
        fun getFormattedSpeed(): String = formatNetworkSpeed(speedKbps)
        fun getQuality(): ConnectionQuality = getConnectionQuality(speedKbps)
    }
    
    /**
     * Callback simples para monitoramento de rede
     */
    abstract class SimpleNetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onNetworkAvailable()
        }
        
        override fun onLost(network: Network) {
            onNetworkLost()
        }
        
        abstract fun onNetworkAvailable()
        abstract fun onNetworkLost()
    }
    
    /**
     * Utilitários para diagnóstico de rede
     */
    object NetworkDiagnostics {
        
        suspend fun runFullDiagnostic(context: Context): NetworkDiagnosticResult {
            val networkInfo = getNetworkInfo(context)
            val localIp = getLocalIpAddress()
            val publicIp = getPublicIpAddress()
            val googlePing = pingHost("8.8.8.8", 53)
            val httpTest = testHttpConnection("https://www.google.com")
            val speedTest = measureDownloadSpeed()
            
            return NetworkDiagnosticResult(
                networkInfo = networkInfo,
                localIp = localIp,
                publicIp = publicIp,
                dnsTest = googlePing,
                httpTest = httpTest,
                speedTest = speedTest,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    data class NetworkDiagnosticResult(
        val networkInfo: NetworkInfo,
        val localIp: String?,
        val publicIp: String?,
        val dnsTest: PingResult,
        val httpTest: HttpTestResult,
        val speedTest: DownloadSpeedResult,
        val timestamp: Long
    ) {
        fun isHealthy(): Boolean {
            return networkInfo.isConnected &&
                    dnsTest.success &&
                    httpTest.success &&
                    speedTest.success
        }
        
        fun getOverallQuality(): ConnectionQuality {
            return if (!isHealthy()) {
                ConnectionQuality.POOR
            } else {
                speedTest.getQuality()
            }
        }
    }
}