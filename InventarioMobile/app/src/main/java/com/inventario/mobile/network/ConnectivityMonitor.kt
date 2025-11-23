package com.inventario.mobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Monitor de conectividade que detecta automaticamente quando:
 * 1. Internet está disponível
 * 2. Servidor está acessível
 * 
 * Emite eventos via StateFlow para que o app possa reagir automaticamente
 */
class ConnectivityMonitor private constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "ConnectivityMonitor"
        private const val SERVER_CHECK_INTERVAL_MS = 30_000L // 30 segundos
        private const val SERVER_TIMEOUT_MS = 3_000L // 3 segundos
        
        @Volatile
        private var instance: ConnectivityMonitor? = null
        
        fun getInstance(context: Context): ConnectivityMonitor {
            return instance ?: synchronized(this) {
                instance ?: ConnectivityMonitor(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    // Estados de conectividade
    sealed class ConnectivityState {
        object NoInternet : ConnectivityState()
        object InternetAvailable : ConnectivityState()
        object ServerAvailable : ConnectivityState()
        object ServerUnavailable : ConnectivityState()
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // StateFlow para observar mudanças de conectividade
    private val _connectivityState = MutableStateFlow<ConnectivityState>(ConnectivityState.NoInternet)
    val connectivityState: StateFlow<ConnectivityState> = _connectivityState.asStateFlow()
    
    // StateFlow para saber se servidor está disponível
    private val _isServerAvailable = MutableStateFlow(false)
    val isServerAvailable: StateFlow<Boolean> = _isServerAvailable.asStateFlow()
    
    // StateFlow para saber se tem internet
    private val _hasInternet = MutableStateFlow(false)
    val hasInternet: StateFlow<Boolean> = _hasInternet.asStateFlow()
    
    private var isMonitoring = false
    private var lastServerCheckTime = 0L
    
    // Callback de rede do Android
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "✅ Rede disponível")
            _hasInternet.value = true
            _connectivityState.value = ConnectivityState.InternetAvailable
            
            // Verificar servidor imediatamente quando internet volta
            checkServerAvailability()
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "❌ Rede perdida")
            _hasInternet.value = false
            _isServerAvailable.value = false
            _connectivityState.value = ConnectivityState.NoInternet
        }
        
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                             capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            Log.d(TAG, "🔄 Capacidades mudaram - Internet: $hasInternet")
            _hasInternet.value = hasInternet
            
            if (hasInternet) {
                _connectivityState.value = ConnectivityState.InternetAvailable
                checkServerAvailability()
            } else {
                _connectivityState.value = ConnectivityState.NoInternet
                _isServerAvailable.value = false
            }
        }
    }
    
    /**
     * Inicia monitoramento de conectividade
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "⚠️ Monitoramento já está ativo")
            return
        }
        
        Log.d(TAG, "🚀 Iniciando monitoramento de conectividade")
        isMonitoring = true
        
        // Registrar callback de rede
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            Log.d(TAG, "✅ Callback de rede registrado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao registrar callback de rede", e)
        }
        
        // Verificar estado inicial
        checkInitialConnectivity()
        
        // Iniciar verificação periódica do servidor
        startPeriodicServerCheck()
    }
    
    /**
     * Para monitoramento de conectividade
     */
    fun stopMonitoring() {
        if (!isMonitoring) return
        
        Log.d(TAG, "🛑 Parando monitoramento de conectividade")
        isMonitoring = false
        
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.d(TAG, "✅ Callback de rede removido")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao remover callback de rede", e)
        }
    }
    
    /**
     * Verifica conectividade inicial
     */
    private fun checkInitialConnectivity() {
        val hasInternet = isInternetAvailable()
        _hasInternet.value = hasInternet
        
        if (hasInternet) {
            _connectivityState.value = ConnectivityState.InternetAvailable
            checkServerAvailability()
        } else {
            _connectivityState.value = ConnectivityState.NoInternet
            _isServerAvailable.value = false
        }
    }
    
    /**
     * Verifica se tem internet disponível
     */
    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Inicia verificação periódica do servidor
     */
    private fun startPeriodicServerCheck() {
        scope.launch {
            while (isMonitoring) {
                if (_hasInternet.value) {
                    val now = System.currentTimeMillis()
                    
                    // Verificar apenas se passou o intervalo mínimo
                    if (now - lastServerCheckTime >= SERVER_CHECK_INTERVAL_MS) {
                        checkServerAvailability()
                        lastServerCheckTime = now
                    }
                }
                
                // Aguardar antes da próxima verificação
                delay(SERVER_CHECK_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Verifica se o servidor está acessível
     */
    fun checkServerAvailability() {
        if (!_hasInternet.value) {
            Log.d(TAG, "⚠️ Sem internet, pulando verificação do servidor")
            return
        }
        
        scope.launch {
            try {
                Log.d(TAG, "🔍 Verificando disponibilidade do servidor...")
                
                val serverConfigManager = com.inventario.mobile.utils.ServerConfigManager.getInstance(context)
                val baseUrl = serverConfigManager.getBaseUrl()
                
                // Criar cliente HTTP simples para verificação
                val client = OkHttpClient.Builder()
                    .connectTimeout(SERVER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .readTimeout(SERVER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .writeTimeout(SERVER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .build()
                
                // Tentar fazer uma requisição simples ao servidor
                val request = Request.Builder()
                    .url("$baseUrl/api/mobile/auth/health") // Endpoint de health check
                    .head() // HEAD é mais leve que GET
                    .build()
                
                val response = client.newCall(request).execute()
                val isAvailable = response.isSuccessful
                
                response.close()
                
                if (isAvailable) {
                    Log.d(TAG, "✅ Servidor DISPONÍVEL")
                    _isServerAvailable.value = true
                    _connectivityState.value = ConnectivityState.ServerAvailable
                } else {
                    Log.d(TAG, "❌ Servidor INDISPONÍVEL (HTTP ${response.code})")
                    _isServerAvailable.value = false
                    _connectivityState.value = ConnectivityState.ServerUnavailable
                }
                
            } catch (e: Exception) {
                Log.d(TAG, "❌ Servidor INDISPONÍVEL (${e.message})")
                _isServerAvailable.value = false
                _connectivityState.value = ConnectivityState.ServerUnavailable
            }
        }
    }
    
    /**
     * Força verificação imediata do servidor
     */
    fun forceServerCheck() {
        lastServerCheckTime = 0L // Reset timer
        checkServerAvailability()
    }
}
