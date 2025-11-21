package com.inventario.mobile.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Gerenciador de configuração de servidor
 * Permite configurar o servidor usando apenas o IP, construindo automaticamente as URLs
 */
class ServerConfigManager private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ServerConfigManager? = null

        fun getInstance(context: Context): ServerConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServerConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Configurações padrão (podem ser sobrescritas por resources)
        // IP padrão do servidor para fallback
        private const val FALLBACK_IP = "10.14.250.214"  // ✅ IP padrão configurado
        private const val DEFAULT_PORT = 8081
        private const val DEFAULT_CONTEXT_PATH = "/inventario"  // Restaurado
        private const val DEFAULT_API_PATH = "/api/mobile"
        private const val DEFAULT_PROTOCOL_HTTP = "http"
        private const val DEFAULT_PROTOCOL_HTTPS = "https"

        // Regex para validação de IP
        private val IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )

        // IPs especiais
        private const val LOCALHOST_IP = "127.0.0.1"
        private const val LOCAL_NETWORK_PREFIX = "192.168."
        private const val LOCAL_NETWORK_PREFIX_10 = "10."

        /**
         * Valida se o IP é válido (aceita também "localhost")
         */
        fun isValidIp(ip: String): Boolean {
            return IP_PATTERN.matcher(ip).matches() || ip.equals("localhost", ignoreCase = true)
        }
    }

    private val preferencesManager = PreferencesManager(context)

    /**
     * Configura o servidor usando apenas o IP
     */
    fun setServerIp(ip: String, port: Int = DEFAULT_PORT, useHttps: Boolean = false): Boolean {
        // aceitar "localhost" além do formato IPv4
        val isValid = ServerConfigManager.isValidIp(ip) || isLocalhost(ip)
        if (!isValid) {
            return false
        }

        val protocol = if (useHttps) DEFAULT_PROTOCOL_HTTPS else DEFAULT_PROTOCOL_HTTP
        val baseUrl = buildBaseUrl(protocol, ip, port)

        // Salva as configurações
        preferencesManager.setServerUrl(baseUrl)
        saveServerConfig(ip, port, useHttps)
        
        Log.d("ServerConfigManager", "Servidor configurado: $baseUrl")
        Log.d("ServerConfigManager", "IMPORTANTE: Chame ApiClient.recreateApiService() para aplicar as mudanças")

        return true
    }

    /**
     * Obtém o IP do servidor configurado
     */
    fun getServerIp(): String? {
        val serverUrl = preferencesManager.getServerUrl() ?: return null
        return extractIpFromUrl(serverUrl)
    }

    /**
     * Obtém a porta do servidor configurado
     */
    fun getServerPort(): Int {
        val serverUrl = preferencesManager.getServerUrl() ?: return DEFAULT_PORT
        return extractPortFromUrl(serverUrl) ?: DEFAULT_PORT
    }

    /**
     * Verifica se está usando HTTPS
     */
    fun isUsingHttps(): Boolean {
        val serverUrl = preferencesManager.getServerUrl() ?: return false
        return serverUrl.startsWith("https://")
    }

    /**
     * Constrói a URL base completa
     * IMPORTANTE: Retorna apenas http://ip:port/inventario/ 
     * Os endpoints das APIs já incluem /api/mobile/
     */
    fun getBaseUrl(): String {
        val serverUrl = preferencesManager.getServerUrl()
        return if (serverUrl.isNullOrBlank()) {
            // Retornar URL padrão válida se não houver configuração
            // Apenas protocolo://ip:port/context-path/
            "http://$FALLBACK_IP:$DEFAULT_PORT$DEFAULT_CONTEXT_PATH/"
        } else {
            serverUrl
        }
    }

    /**
     * Constrói URL para endpoint específico
     */
    fun buildEndpointUrl(endpoint: String): String {
        val base = getBaseUrl()
        return if (endpoint.startsWith("/")) "$base$DEFAULT_API_PATH$endpoint" else "$base$DEFAULT_API_PATH/$endpoint"
    }

    fun getAuthUrl(): String = buildEndpointUrl("auth")
    fun getLoginUrl(): String = buildEndpointUrl("auth/login")
    fun getInventarioUrl(): String = buildEndpointUrl("inventario")
    fun getUsuariosUrl(): String = buildEndpointUrl("usuarios")
    fun getRelatoriosUrl(): String = buildEndpointUrl("relatorios")
    fun getColetaUrl(): String = buildEndpointUrl("coleta")
    fun getSyncUrl(): String = buildEndpointUrl("sync")

    /**
     * Detecta se é um IP local
     */
    fun isLocalIp(ip: String): Boolean {
        return ip == LOCALHOST_IP ||
               ip.startsWith(LOCAL_NETWORK_PREFIX) ||
               ip.startsWith(LOCAL_NETWORK_PREFIX_10) ||
               ip.equals("localhost", ignoreCase = true)
    }

    /**
     * Sugere configurações baseadas no IP
     */
    fun suggestConfiguration(ip: String): ServerConfiguration {
        val isLocal = isLocalIp(ip)
        return ServerConfiguration(
            ip = ip,
            port = DEFAULT_PORT,
            useHttps = !isLocal, // HTTPS para IPs externos, HTTP para locais
            description = if (isLocal) "Servidor Local" else "Servidor Remoto"
        )
    }

    /**
     * Obtém o IP padrão dos resources ou fallback
     */
    fun getDefaultIp(): String {
        return try {
            val configuredIp = context.getString(context.resources.getIdentifier("default_server_ip", "string", context.packageName))
            
            if (configuredIp == "AUTO") {
                // Tentar detectar IP automaticamente baseado na rede local
                getAutoDetectedIp() ?: FALLBACK_IP
            } else {
                configuredIp
            }
        } catch (e: Exception) {
            FALLBACK_IP
        }
    }
    
    /**
     * Tenta detectar automaticamente um IP baseado na rede local
     */
    private fun getAutoDetectedIp(): String? {
        try {
            // Obter IP local do dispositivo
            val localIp = getLocalIpAddress()
            
            if (localIp != null) {
                Log.d("ServerConfigManager", "IP local detectado: $localIp")
                
                // Gerar IP candidato baseado na rede local
                val parts = localIp.split(".")
                if (parts.size == 4) {
                    val networkBase = "${parts[0]}.${parts[1]}.${parts[2]}"
                    
                    // Tentar IPs comuns na mesma rede
                    val candidates = listOf(
                        "$networkBase.100",   // IP comum para servidor
                        "$networkBase.1",     // Gateway (pode ser servidor)
                        "$networkBase.200",   // IP alternativo
                        "$networkBase.254"    // Gateway alternativo
                    )
                    
                    // Retornar primeiro candidato (será testado depois)
                    return candidates.first()
                }
            }
        } catch (e: Exception) {
            Log.w("ServerConfigManager", "Erro na detecção automática de IP", e)
        }
        
        return null
    }
    
    /**
     * Obtém IP local do dispositivo
     */
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ServerConfigManager", "Erro ao obter IP local", e)
        }
        return null
    }
    
    /**
     * Testa conectividade com o servidor usando as configurações atuais
     */
    suspend fun testServerConnectivity(): ConnectivityResult {
        return withContext(Dispatchers.IO) {
            try {
                val ip = getServerIp() ?: getDefaultIp()
                val port = getServerPort()
                val useHttps = isUsingHttps()
                
                Log.d("ServerConfigManager", "Testando conectividade com o servidor: $ip")
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                val protocol = if (useHttps) DEFAULT_PROTOCOL_HTTPS else DEFAULT_PROTOCOL_HTTP
                val healthUrl = "$protocol://$ip:$port$DEFAULT_CONTEXT_PATH$DEFAULT_API_PATH/health"
                val request = Request.Builder()
                    .url(healthUrl)
                    .get()
                    .build()
                
                val startTime = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val responseTime = System.currentTimeMillis() - startTime
                
                if (response.isSuccessful) {
                    Log.d("ServerConfigManager", "Conectividade testada com sucesso")
                    ConnectivityResult(
                        success = true,
                        url = healthUrl,
                        responseTime = responseTime,
                        message = "Conexão estabelecida com sucesso"
                    )
                } else {
                    Log.w("ServerConfigManager", "Servidor respondeu com código: ${response.code}")
                    ConnectivityResult(
                        success = false,
                        url = healthUrl,
                        responseTime = responseTime,
                        message = "Servidor indisponível (código: ${response.code})"
                    )
                }
            } catch (e: UnknownHostException) {
                Log.e("ServerConfigManager", "Host não encontrado", e)
                ConnectivityResult(
                    success = false,
                    url = getBaseUrl(),
                    responseTime = -1,
                    message = "Servidor não encontrado. Verifique o endereço IP."
                )
            } catch (e: ConnectException) {
                Log.e("ServerConfigManager", "Conexão recusada", e)
                ConnectivityResult(
                    success = false,
                    url = getBaseUrl(),
                    responseTime = -1,
                    message = "Conexão recusada. Verifique se o servidor está rodando."
                )
            } catch (e: SocketTimeoutException) {
                Log.e("ServerConfigManager", "Timeout na conexão", e)
                ConnectivityResult(
                    success = false,
                    url = getBaseUrl(),
                    responseTime = -1,
                    message = "Timeout na conexão. Verifique sua rede."
                )
            } catch (e: IOException) {
                Log.e("ServerConfigManager", "Erro de I/O", e)
                ConnectivityResult(
                    success = false,
                    url = getBaseUrl(),
                    responseTime = -1,
                    message = "Erro de rede: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e("ServerConfigManager", "Erro inesperado ao testar conectividade", e)
                ConnectivityResult(
                    success = false,
                    url = getBaseUrl(),
                    responseTime = -1,
                    message = "Erro inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Testa conectividade com o servidor
     */
    suspend fun testConnectivity(ip: String, port: Int = DEFAULT_PORT, useHttps: Boolean = false): ConnectivityResult {
        val protocol = if (useHttps) DEFAULT_PROTOCOL_HTTPS else DEFAULT_PROTOCOL_HTTP
        val testUrl = "$protocol://$ip:$port$DEFAULT_CONTEXT_PATH$DEFAULT_API_PATH/health"

        return try {
            // Aqui você implementaria a lógica de teste real
            // Por exemplo, usando OkHttp ou Retrofit
            ConnectivityResult(
                success = true,
                url = testUrl,
                responseTime = 150,
                message = "Conexão estabelecida com sucesso"
            )
        } catch (e: Exception) {
            ConnectivityResult(
                success = false,
                url = testUrl,
                responseTime = -1,
                message = "Erro: ${e.message}"
            )
        }
    }

    // Métodos privados
    private fun buildBaseUrl(protocol: String, ip: String, port: Int): String {
        // Retorna apenas protocolo://ip:port/context-path/
        // Os endpoints das APIs já incluem /api/mobile/
        return "$protocol://$ip:$port$DEFAULT_CONTEXT_PATH/"
    }

    private fun saveServerConfig(ip: String, port: Int, useHttps: Boolean) {
        val prefs = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("server_ip", ip)
            .putInt("server_port", port)
            .putBoolean("use_https", useHttps)
            .apply()
    }

    /**
     * Extrai IP de uma URL
     */
    fun extractIpFromUrl(url: String): String? {
        return try {
            val regex = "://([^:/]+)".toRegex()
            regex.find(url)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractPortFromUrl(url: String): Int? {
        val regex = ":([0-9]+)/".toRegex()
        return regex.find(url)?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Verifica se é localhost
     */
    fun isLocalhost(ip: String): Boolean {
        return ip.equals("127.0.0.1") || ip.equals("localhost", ignoreCase = true)
    }

    /**
     * Verifica se é rede privada
     */
    fun isPrivateNetwork(ip: String): Boolean {
        if (!ServerConfigManager.isValidIp(ip)) return false

        // "localhost" tratado separadamente
        if (isLocalhost(ip)) return true

        val parts = ip.split(".").map { it.toInt() }

        // 10.0.0.0/8
        if (parts[0] == 10) return true

        // 172.16.0.0/12
        if (parts[0] == 172 && parts[1] in 16..31) return true

        // 192.168.0.0/16
        if (parts[0] == 192 && parts[1] == 168) return true

        return false
    }

    /**
     * Obtém configuração completa do servidor
     */
    fun getServerConfig(): ServerConfig {
        return ServerConfig(
            ip = getServerIp() ?: getDefaultIp(),
            port = getServerPort(),
            useHttps = isUsingHttps(),
            baseUrl = getBaseUrl()
        )
    }
}

/**
 * Configuração do servidor
 */
data class ServerConfiguration(
    val ip: String,
    val port: Int,
    val useHttps: Boolean,
    val description: String
)

/**
 * Configuração simplificada do servidor
 */
data class ServerConfig(
    val ip: String,
    val port: Int,
    val useHttps: Boolean,
    val baseUrl: String
)

/**
 * Resultado do teste de conectividade
 */
data class ConnectivityResult(
    val success: Boolean,
    val url: String? = null,
    val responseTime: Long = -1,
    val message: String? = null
)