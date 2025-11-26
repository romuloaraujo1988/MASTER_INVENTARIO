package com.inventario.mobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitor de qualidade de rede
 * Detecta quando a rede está instável e decide se deve tentar sincronizar
 * 
 * Estratégia:
 * - EXCELENTE: Sincroniza imediatamente
 * - BOA: Sincroniza imediatamente
 * - REGULAR: Salva local, tenta sync com timeout curto
 * - RUIM: Salva local, não tenta sync
 * - SEM_REDE: Salva local, não tenta sync
 */
@Singleton
class NetworkQualityMonitor @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NetworkQualityMonitor"
        
        // Thresholds de qualidade
        private const val EXCELLENT_BANDWIDTH_MBPS = 10  // 10 Mbps+
        private const val GOOD_BANDWIDTH_MBPS = 5        // 5-10 Mbps
        private const val FAIR_BANDWIDTH_MBPS = 2        // 2-5 Mbps
        // < 2 Mbps = RUIM
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkQuality = MutableStateFlow(NetworkQuality.UNKNOWN)
    val networkQuality: StateFlow<NetworkQuality> = _networkQuality.asStateFlow()
    
    private val _shouldSyncImmediately = MutableStateFlow(false)
    val shouldSyncImmediately: StateFlow<Boolean> = _shouldSyncImmediately.asStateFlow()
    
    private var lastCheckTime = 0L
    private var consecutiveFailures = 0
    private var lastSuccessTime = 0L
    
    init {
        startMonitoring()
    }
    
    /**
     * Inicia monitoramento contínuo da rede
     */
    private fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Rede disponível")
                updateNetworkQuality()
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Rede perdida")
                _networkQuality.value = NetworkQuality.SEM_REDE
                _shouldSyncImmediately.value = false
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkQuality()
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Verificação inicial
        updateNetworkQuality()
    }
    
    /**
     * Atualiza qualidade da rede baseado em múltiplos fatores
     */
    private fun updateNetworkQuality() {
        val now = System.currentTimeMillis()
        
        // Evitar verificações muito frequentes (mínimo 5s)
        if (now - lastCheckTime < 5000) {
            return
        }
        lastCheckTime = now
        
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork == null) {
            _networkQuality.value = NetworkQuality.SEM_REDE
            _shouldSyncImmediately.value = false
            Log.d(TAG, "Sem rede ativa")
            return
        }
        
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (capabilities == null) {
            _networkQuality.value = NetworkQuality.SEM_REDE
            _shouldSyncImmediately.value = false
            Log.d(TAG, "Sem capabilities")
            return
        }
        
        // Verificar se tem internet de verdade
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            _networkQuality.value = NetworkQuality.SEM_REDE
            _shouldSyncImmediately.value = false
            Log.d(TAG, "Internet não validada")
            return
        }
        
        // Calcular qualidade baseado em múltiplos fatores
        val quality = calculateQuality(capabilities)
        
        _networkQuality.value = quality
        _shouldSyncImmediately.value = quality.shouldSyncImmediately()
        
        Log.d(TAG, "Qualidade da rede: $quality (sync imediato: ${quality.shouldSyncImmediately()})")
    }
    
    /**
     * Calcula qualidade da rede baseado em capabilities
     */
    private fun calculateQuality(capabilities: NetworkCapabilities): NetworkQuality {
        var score = 0
        
        // 1. Tipo de conexão (peso: 40 pontos)
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                score += 40  // WiFi = melhor
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                score += 40  // Ethernet = melhor
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Verificar tipo de celular
                score += when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> 35
                    else -> 25  // Dados móveis = médio
                }
            }
            else -> score += 10  // Outros = ruim
        }
        
        // 2. Largura de banda (peso: 40 pontos)
        val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps / 1024  // Converter para Mbps
        score += when {
            downstreamBandwidth >= EXCELLENT_BANDWIDTH_MBPS * 1024 -> 40
            downstreamBandwidth >= GOOD_BANDWIDTH_MBPS * 1024 -> 30
            downstreamBandwidth >= FAIR_BANDWIDTH_MBPS * 1024 -> 20
            else -> 10
        }
        
        // 3. Histórico de falhas (peso: 20 pontos)
        val timeSinceLastSuccess = System.currentTimeMillis() - lastSuccessTime
        score += when {
            consecutiveFailures == 0 -> 20
            consecutiveFailures <= 2 && timeSinceLastSuccess < 60000 -> 15  // < 1 min
            consecutiveFailures <= 5 && timeSinceLastSuccess < 300000 -> 10  // < 5 min
            else -> 0
        }
        
        // Converter score para qualidade
        return when {
            score >= 80 -> NetworkQuality.EXCELENTE
            score >= 60 -> NetworkQuality.BOA
            score >= 40 -> NetworkQuality.REGULAR
            score >= 20 -> NetworkQuality.RUIM
            else -> NetworkQuality.MUITO_RUIM
        }
    }
    
    /**
     * Registra sucesso de sincronização
     * Melhora score da rede
     */
    fun registerSyncSuccess() {
        consecutiveFailures = 0
        lastSuccessTime = System.currentTimeMillis()
        Log.d(TAG, "✓ Sync bem-sucedido - Resetando contador de falhas")
        updateNetworkQuality()
    }
    
    /**
     * Registra falha de sincronização
     * Piora score da rede
     */
    fun registerSyncFailure() {
        consecutiveFailures++
        Log.d(TAG, "✗ Sync falhou - Falhas consecutivas: $consecutiveFailures")
        updateNetworkQuality()
    }
    
    /**
     * Verifica se deve tentar sincronizar agora
     * @return true se rede está boa o suficiente
     */
    fun shouldAttemptSync(): Boolean {
        val quality = _networkQuality.value
        val should = quality.shouldSyncImmediately()
        
        Log.d(TAG, "Deve tentar sync? $should (qualidade: $quality)")
        return should
    }
    
    /**
     * Obtém timeout recomendado baseado na qualidade da rede
     * v2.1: Timeouts mais generosos para garantir sincronização
     * @return Timeout em milissegundos
     */
    fun getRecommendedTimeout(): Long {
        return when (_networkQuality.value) {
            NetworkQuality.EXCELENTE -> 15_000L   // 15s
            NetworkQuality.BOA -> 15_000L          // 15s
            NetworkQuality.REGULAR -> 10_000L      // 10s (era 8s)
            NetworkQuality.RUIM -> 8_000L          // 8s (era 5s)
            NetworkQuality.MUITO_RUIM -> 5_000L    // 5s (era 3s)
            NetworkQuality.SEM_REDE -> 0L          // Não tenta
            NetworkQuality.UNKNOWN -> 15_000L      // 15s (era 10s)
        }
    }
    
    /**
     * Verifica se está em modo avião
     */
    fun isAirplaneModeOn(): Boolean {
        return try {
            android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtém tipo de conexão atual
     */
    fun getConnectionType(): ConnectionType {
        val activeNetwork = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return ConnectionType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            else -> ConnectionType.OTHER
        }
    }
    
    /**
     * Força atualização da qualidade da rede
     */
    fun forceUpdate() {
        lastCheckTime = 0  // Reseta throttle
        updateNetworkQuality()
    }
}

/**
 * Qualidade da rede
 */
enum class NetworkQuality {
    EXCELENTE,      // WiFi rápido, sem falhas
    BOA,            // WiFi ou 4G/5G estável
    REGULAR,        // 3G/4G com algumas falhas
    RUIM,           // Conexão lenta ou instável
    MUITO_RUIM,     // Conexão muito instável
    SEM_REDE,       // Sem conexão
    UNKNOWN;        // Desconhecido
    
    /**
     * Deve tentar sincronizar imediatamente?
     * v2.1: Mais agressivo - tenta sincronizar sempre que há conexão
     */
    fun shouldSyncImmediately(): Boolean {
        return when (this) {
            EXCELENTE, BOA -> true
            REGULAR -> true   // ✅ ALTERADO: Tenta sincronizar mesmo com rede regular
            RUIM -> true      // ✅ ALTERADO: Tenta sincronizar mesmo com rede ruim (com timeout curto)
            MUITO_RUIM, SEM_REDE -> false  // Só não tenta se realmente não tem rede
            UNKNOWN -> true   // ✅ ALTERADO: Tenta sincronizar (otimista)
        }
    }
    
    /**
     * Deve tentar sincronizar com timeout curto?
     */
    fun shouldTrySyncWithShortTimeout(): Boolean {
        return this == REGULAR
    }
    
    /**
     * Emoji para UI
     */
    fun toEmoji(): String {
        return when (this) {
            EXCELENTE -> "🟢"
            BOA -> "🟢"
            REGULAR -> "🟡"
            RUIM -> "🟠"
            MUITO_RUIM -> "🔴"
            SEM_REDE -> "⚫"
            UNKNOWN -> "⚪"
        }
    }
    
    /**
     * Descrição para UI
     */
    fun toDescription(): String {
        return when (this) {
            EXCELENTE -> "Excelente - Sincronizando automaticamente"
            BOA -> "Boa - Sincronizando automaticamente"
            REGULAR -> "Regular - Salvando localmente"
            RUIM -> "Ruim - Modo offline ativado"
            MUITO_RUIM -> "Muito ruim - Modo offline ativado"
            SEM_REDE -> "Sem conexão - Modo offline"
            UNKNOWN -> "Verificando conexão..."
        }
    }
}

/**
 * Tipo de conexão
 */
enum class ConnectionType {
    WIFI,
    ETHERNET,
    CELLULAR,
    OTHER,
    NONE
}
