package com.inventario.mobile.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gerenciador inteligente de localização de rede
 * Detecta mudanças de rede e ajusta automaticamente as configurações do servidor
 */
class NetworkLocationManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkLocationManager? = null
        
        fun getInstance(context: Context): NetworkLocationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkLocationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val TAG = "NetworkLocationManager"
        private const val PREF_NETWORK_CONFIGS = "network_configs"
        private const val PREF_LAST_NETWORK_ID = "last_network_id"
    }
    
    private val preferencesManager = PreferencesManager(context)
    private val serverConfigManager = ServerConfigManager.getInstance(context)
    
    /**
     * Configuração de rede específica
     */
    data class NetworkConfig(
        val networkId: String,
        val networkName: String,
        val serverIp: String,
        val serverPort: Int = 8081,
        val useHttps: Boolean = false,
        val lastUsed: Long = System.currentTimeMillis(),
        val isWorking: Boolean = true
    )
    
    /**
     * Detecta mudança de rede e ajusta configurações automaticamente
     */
    suspend fun handleNetworkChange(): NetworkChangeResult {
        return withContext(Dispatchers.IO) {
            try {
                val currentNetworkId = getCurrentNetworkId()
                val lastNetworkId = getLastNetworkId()
                
                Log.d(TAG, "Rede atual: $currentNetworkId, Última rede: $lastNetworkId")
                
                if (currentNetworkId != lastNetworkId) {
                    Log.d(TAG, "Mudança de rede detectada!")
                    
                    // Salvar nova rede como atual
                    saveLastNetworkId(currentNetworkId)
                    
                    // Tentar carregar configuração salva para esta rede
                    val networkConfig = getNetworkConfig(currentNetworkId)
                    
                    if (networkConfig != null && networkConfig.isWorking) {
                        Log.d(TAG, "Configuração encontrada para rede: ${networkConfig.networkName}")
                        
                        // Aplicar configuração salva
                        serverConfigManager.setServerIp(
                            networkConfig.serverIp,
                            networkConfig.serverPort,
                            networkConfig.useHttps
                        )
                        
                        // Testar se ainda funciona
                        val testResult = serverConfigManager.testServerConnectivity()
                        
                        if (testResult.success) {
                            Log.d(TAG, "Configuração aplicada com sucesso!")
                            return@withContext NetworkChangeResult(
                                networkChanged = true,
                                configApplied = true,
                                serverIp = networkConfig.serverIp,
                                message = "Conectado automaticamente ao servidor ${networkConfig.serverIp}"
                            )
                        } else {
                            Log.w(TAG, "Configuração salva não funciona mais, tentando alternativas...")
                            // Marcar como não funcionando
                            saveNetworkConfig(networkConfig.copy(isWorking = false))
                        }
                    }
                    
                    // Se não há configuração ou não funciona, tentar IPs conhecidos
                    val workingIp = findWorkingServerIp()
                    
                    if (workingIp != null) {
                        Log.d(TAG, "IP funcionando encontrado: $workingIp")
                        
                        // Salvar nova configuração para esta rede
                        val newConfig = NetworkConfig(
                            networkId = currentNetworkId,
                            networkName = getCurrentNetworkName(),
                            serverIp = workingIp,
                            serverPort = 8081,
                            useHttps = false,
                            isWorking = true
                        )
                        saveNetworkConfig(newConfig)
                        
                        // Aplicar configuração
                        serverConfigManager.setServerIp(workingIp)
                        
                        return@withContext NetworkChangeResult(
                            networkChanged = true,
                            configApplied = true,
                            serverIp = workingIp,
                            message = "Servidor encontrado automaticamente: $workingIp"
                        )
                    } else {
                        Log.w(TAG, "Nenhum servidor acessível encontrado")
                        return@withContext NetworkChangeResult(
                            networkChanged = true,
                            configApplied = false,
                            message = "Mudança de rede detectada, mas nenhum servidor acessível foi encontrado. Configure manualmente."
                        )
                    }
                } else {
                    // Mesma rede, verificar se servidor atual ainda funciona
                    val testResult = serverConfigManager.testServerConnectivity()
                    
                    if (!testResult.success) {
                        Log.w(TAG, "Servidor atual não está acessível, tentando alternativas...")
                        
                        val workingIp = findWorkingServerIp()
                        if (workingIp != null) {
                            serverConfigManager.setServerIp(workingIp)
                            
                            // Atualizar configuração da rede atual
                            val currentConfig = getNetworkConfig(currentNetworkId)
                            if (currentConfig != null) {
                                saveNetworkConfig(currentConfig.copy(
                                    serverIp = workingIp,
                                    isWorking = true,
                                    lastUsed = System.currentTimeMillis()
                                ))
                            }
                            
                            return@withContext NetworkChangeResult(
                                networkChanged = false,
                                configApplied = true,
                                serverIp = workingIp,
                                message = "Servidor alternativo encontrado: $workingIp"
                            )
                        }
                    }
                    
                    return@withContext NetworkChangeResult(
                        networkChanged = false,
                        configApplied = false,
                        message = "Mesma rede, configuração mantida"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao processar mudança de rede", e)
                return@withContext NetworkChangeResult(
                    networkChanged = false,
                    configApplied = false,
                    message = "Erro ao detectar mudança de rede: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Tenta encontrar um IP de servidor que funcione
     */
    private suspend fun findWorkingServerIp(): String? {
        val candidateIps = getAllKnownIps()
        
        Log.d(TAG, "Testando ${candidateIps.size} IPs candidatos...")
        
        for (ip in candidateIps) {
            try {
                Log.d(TAG, "Testando IP: $ip")
                
                // Configurar temporariamente
                serverConfigManager.setServerIp(ip)
                
                // Testar conectividade
                val result = serverConfigManager.testServerConnectivity()
                
                if (result.success) {
                    Log.d(TAG, "IP $ip está funcionando!")
                    return ip
                }
            } catch (e: Exception) {
                Log.d(TAG, "IP $ip falhou: ${e.message}")
            }
        }
        
        return null
    }
    
    /**
     * Obtém todos os IPs conhecidos (configurados + sugeridos + rede atual)
     */
    private fun getAllKnownIps(): List<String> {
        val knownIps = mutableSetOf<String>()
        
        // IP atual configurado
        serverConfigManager.getServerIp()?.let { knownIps.add(it) }
        
        // IPs salvos em configurações de rede
        getAllNetworkConfigs().forEach { config ->
            knownIps.add(config.serverIp)
        }
        
        // IPs da rede atual (dinâmico)
        knownIps.addAll(getCurrentNetworkIpCandidates())
        
        // IPs padrão dos resources
        knownIps.addAll(getResourceIpCandidates())
        
        // IPs comuns como fallback
        knownIps.addAll(getCommonIpCandidates())
        
        return knownIps.toList()
    }
    
    /**
     * Obtém IPs candidatos baseados na rede atual
     */
    private fun getCurrentNetworkIpCandidates(): List<String> {
        val candidates = mutableListOf<String>()
        
        try {
            // TODO: Implementar getLocalIpAddress no NetworkUtils
            // Obter IP local do dispositivo
            val localIp: String? = null // NetworkUtils.getLocalIpAddress()
            
            if (localIp != null) {
                Log.d(TAG, "IP local do dispositivo: $localIp")
                
                // Gerar IPs candidatos baseados na rede local
                val parts = localIp.split(".")
                if (parts.size == 4) {
                    val networkBase = "${parts[0]}.${parts[1]}.${parts[2]}"
                    
                    // IPs comuns na mesma rede
                    candidates.addAll(listOf(
                        "$networkBase.1",     // Gateway comum
                        "$networkBase.100",   // IP comum servidor
                        "$networkBase.101",   // IP alternativo
                        "$networkBase.102",   // IP alternativo
                        "$networkBase.200",   // IP alternativo
                        "$networkBase.254"    // Gateway alternativo
                    ))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao obter IPs da rede atual", e)
        }
        
        return candidates
    }
    
    /**
     * Obtém IPs dos resources (server_config.xml)
     */
    private fun getResourceIpCandidates(): List<String> {
        val candidates = mutableListOf<String>()
        
        try {
            // IP padrão do resource
            val defaultIp = serverConfigManager.getDefaultIp()
            candidates.add(defaultIp)
            
            // IPs sugeridos do resource
            val suggestedIps = context.resources.getStringArray(
                context.resources.getIdentifier("suggested_server_ips", "array", context.packageName)
            )
            candidates.addAll(suggestedIps)
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao obter IPs dos resources", e)
        }
        
        return candidates
    }
    
    /**
     * IPs comuns como último recurso
     */
    private fun getCommonIpCandidates(): List<String> {
        return listOf(
            "192.168.1.1",     // Gateway comum
            "192.168.1.100",   // IP comum
            "192.168.0.1",     // Gateway comum
            "192.168.0.100",   // IP comum
            "10.0.0.1",        // Gateway corporativo
            "127.0.0.1"        // Localhost
        )
    }
    
    /**
     * Obtém ID único da rede atual
     */
    private fun getCurrentNetworkId(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        
        if (network != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                // Para WiFi, usar SSID como ID
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                return "wifi_${wifiInfo.ssid.removeSurrounding("\"")}"
            } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                // Para dados móveis, usar operadora
                return "mobile_${android.telephony.TelephonyManager::class.java.name}"
            } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
                return "ethernet"
            }
        }
        
        return "unknown_network"
    }
    
    /**
     * Obtém nome amigável da rede atual
     */
    private fun getCurrentNetworkName(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        
        if (network != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                return wifiInfo.ssid.removeSurrounding("\"")
            } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                return "Dados Móveis"
            } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
                return "Ethernet"
            }
        }
        
        return "Rede Desconhecida"
    }
    
    /**
     * Salva configuração para uma rede específica
     */
    private fun saveNetworkConfig(config: NetworkConfig) {
        val configs = getAllNetworkConfigs().toMutableList()
        
        // Remove configuração existente para esta rede
        configs.removeAll { it.networkId == config.networkId }
        
        // Adiciona nova configuração
        configs.add(config)
        
        // Manter apenas as 10 configurações mais recentes
        val sortedConfigs = configs.sortedByDescending { it.lastUsed }.take(10)
        
        // Salvar no SharedPreferences
        val prefs = context.getSharedPreferences(PREF_NETWORK_CONFIGS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        sortedConfigs.forEachIndexed { index, networkConfig ->
            editor.putString("config_${index}_id", networkConfig.networkId)
            editor.putString("config_${index}_name", networkConfig.networkName)
            editor.putString("config_${index}_ip", networkConfig.serverIp)
            editor.putInt("config_${index}_port", networkConfig.serverPort)
            editor.putBoolean("config_${index}_https", networkConfig.useHttps)
            editor.putLong("config_${index}_lastUsed", networkConfig.lastUsed)
            editor.putBoolean("config_${index}_working", networkConfig.isWorking)
        }
        
        editor.putInt("config_count", sortedConfigs.size)
        editor.apply()
        
        Log.d(TAG, "Configuração salva para rede: ${config.networkName} -> ${config.serverIp}")
    }
    
    /**
     * Obtém configuração salva para uma rede específica
     */
    private fun getNetworkConfig(networkId: String): NetworkConfig? {
        return getAllNetworkConfigs().find { it.networkId == networkId }
    }
    
    /**
     * Obtém todas as configurações de rede salvas
     */
    private fun getAllNetworkConfigs(): List<NetworkConfig> {
        val prefs = context.getSharedPreferences(PREF_NETWORK_CONFIGS, Context.MODE_PRIVATE)
        val count = prefs.getInt("config_count", 0)
        val configs = mutableListOf<NetworkConfig>()
        
        for (i in 0 until count) {
            val id = prefs.getString("config_${i}_id", null)
            val name = prefs.getString("config_${i}_name", null)
            val ip = prefs.getString("config_${i}_ip", null)
            
            if (id != null && name != null && ip != null) {
                configs.add(NetworkConfig(
                    networkId = id,
                    networkName = name,
                    serverIp = ip,
                    serverPort = prefs.getInt("config_${i}_port", 8081),
                    useHttps = prefs.getBoolean("config_${i}_https", false),
                    lastUsed = prefs.getLong("config_${i}_lastUsed", 0),
                    isWorking = prefs.getBoolean("config_${i}_working", true)
                ))
            }
        }
        
        return configs
    }
    
    /**
     * Salva ID da última rede
     */
    private fun saveLastNetworkId(networkId: String) {
        val prefs = context.getSharedPreferences(PREF_NETWORK_CONFIGS, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LAST_NETWORK_ID, networkId).apply()
    }
    
    /**
     * Obtém ID da última rede
     */
    private fun getLastNetworkId(): String? {
        val prefs = context.getSharedPreferences(PREF_NETWORK_CONFIGS, Context.MODE_PRIVATE)
        return prefs.getString(PREF_LAST_NETWORK_ID, null)
    }
    
    /**
     * Obtém resumo das configurações salvas
     */
    fun getNetworkConfigsSummary(): String {
        val configs = getAllNetworkConfigs()
        val currentNetworkId = getCurrentNetworkId()
        
        if (configs.isEmpty()) {
            return "Nenhuma configuração de rede salva"
        }
        
        val summary = StringBuilder()
        summary.append("Configurações de rede salvas:\n\n")
        
        configs.sortedByDescending { it.lastUsed }.forEach { config ->
            val isCurrent = config.networkId == currentNetworkId
            val status = if (config.isWorking) "✅" else "❌"
            val current = if (isCurrent) " (ATUAL)" else ""
            
            summary.append("$status ${config.networkName}$current\n")
            summary.append("   IP: ${config.serverIp}:${config.serverPort}\n")
            summary.append("   Última vez: ${DateUtils.formatDateTime(java.util.Date(config.lastUsed))}\n\n")
        }
        
        return summary.toString()
    }
}

/**
 * Resultado da detecção de mudança de rede
 */
data class NetworkChangeResult(
    val networkChanged: Boolean,
    val configApplied: Boolean,
    val serverIp: String? = null,
    val message: String
)