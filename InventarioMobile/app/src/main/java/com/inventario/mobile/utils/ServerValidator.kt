package com.inventario.mobile.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Validador de configuração do servidor
 * Verifica se o IP está configurado corretamente e se o servidor está acessível
 */
object ServerValidator {
    
    private const val TAG = "ServerValidator"
    
    /**
     * Resultado da validação do servidor
     */
    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
        val details: ValidationDetails? = null
    )
    
    /**
     * Detalhes da validação
     */
    data class ValidationDetails(
        val ipConfigured: Boolean,
        val ipValid: Boolean,
        val serverReachable: Boolean,
        val correctEndpoint: Boolean,
        val responseTime: Long = -1,
        val serverVersion: String? = null
    )
    
    /**
     * Valida completamente a configuração do servidor
     * 
     * @param serverConfigManager Gerenciador de configuração do servidor
     * @return Resultado da validação com detalhes
     */
    suspend fun validateServerConfiguration(
        serverConfigManager: ServerConfigManager
    ): ValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "═══════════════════════════════════════════════════════")
                Log.d(TAG, "INICIANDO VALIDAÇÃO DO SERVIDOR")
                Log.d(TAG, "═══════════════════════════════════════════════════════")
                
                // 1. Verificar se o IP está configurado
                val serverIp = serverConfigManager.getServerIp()
                if (serverIp.isNullOrBlank()) {
                    Log.e(TAG, "IP do servidor não está configurado")
                    return@withContext ValidationResult(
                        isValid = false,
                        message = "IP do servidor não está configurado. Configure nas configurações do app.",
                        details = ValidationDetails(
                            ipConfigured = false,
                            ipValid = false,
                            serverReachable = false,
                            correctEndpoint = false
                        )
                    )
                }
                
                Log.d(TAG, "IP configurado: $serverIp")
                
                // 2. Verificar se o IP é válido
                val isValidIp = ServerConfigManager.isValidIp(serverIp)
                if (!isValidIp) {
                    Log.e(TAG, "IP inválido: $serverIp")
                    return@withContext ValidationResult(
                        isValid = false,
                        message = "IP configurado é inválido: $serverIp",
                        details = ValidationDetails(
                            ipConfigured = true,
                            ipValid = false,
                            serverReachable = false,
                            correctEndpoint = false
                        )
                    )
                }
                
                Log.d(TAG, "IP válido: $serverIp")
                
                // 3. Verificar se o servidor está acessível
                val baseUrl = serverConfigManager.getBaseUrl()
                Log.d(TAG, "Base URL: $baseUrl")
                
                val connectivityResult = testServerConnectivity(serverConfigManager)
                if (!connectivityResult.success) {
                    Log.e(TAG, "Servidor não está acessível: ${connectivityResult.message}")
                    return@withContext ValidationResult(
                        isValid = false,
                        message = connectivityResult.message ?: "Servidor não está acessível",
                        details = ValidationDetails(
                            ipConfigured = true,
                            ipValid = true,
                            serverReachable = false,
                            correctEndpoint = false,
                            responseTime = connectivityResult.responseTime
                        )
                    )
                }
                
                Log.d(TAG, "Servidor acessível (${connectivityResult.responseTime}ms)")
                
                // 4. Verificar se o endpoint correto está disponível
                val endpointResult = testCorrectEndpoint(serverConfigManager)
                if (!endpointResult.success) {
                    Log.w(TAG, "Endpoint de API não está acessível: ${endpointResult.message}")
                    return@withContext ValidationResult(
                        isValid = false,
                        message = "Servidor acessível, mas API mobile não está disponível. Verifique se o servidor está rodando corretamente.",
                        details = ValidationDetails(
                            ipConfigured = true,
                            ipValid = true,
                            serverReachable = true,
                            correctEndpoint = false,
                            responseTime = connectivityResult.responseTime
                        )
                    )
                }
                
                Log.d(TAG, "Endpoint correto acessível")
                Log.d(TAG, "═══════════════════════════════════════════════════════")
                Log.d(TAG, "VALIDAÇÃO CONCLUÍDA COM SUCESSO")
                Log.d(TAG, "═══════════════════════════════════════════════════════")
                
                // Tudo OK!
                ValidationResult(
                    isValid = true,
                    message = "Servidor configurado corretamente e acessível",
                    details = ValidationDetails(
                        ipConfigured = true,
                        ipValid = true,
                        serverReachable = true,
                        correctEndpoint = true,
                        responseTime = connectivityResult.responseTime,
                        serverVersion = endpointResult.url
                    )
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante validação", e)
                ValidationResult(
                    isValid = false,
                    message = "Erro ao validar servidor: ${e.message}",
                    details = null
                )
            }
        }
    }
    
    /**
     * Testa conectividade básica com o servidor
     */
    private suspend fun testServerConnectivity(
        serverConfigManager: ServerConfigManager
    ): ConnectivityResult {
        return serverConfigManager.testServerConnectivity()
    }
    
    /**
     * Testa se o endpoint correto da API está disponível
     */
    private suspend fun testCorrectEndpoint(
        serverConfigManager: ServerConfigManager
    ): ConnectivityResult {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                // Testar endpoint de teste da API mobile
                val baseUrl = serverConfigManager.getBaseUrl()
                val testUrl = "$baseUrl/api/mobile/test/ping"
                
                Log.d(TAG, "Testando endpoint: $testUrl")
                
                val request = Request.Builder()
                    .url(testUrl)
                    .get()
                    .build()
                
                val startTime = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val responseTime = System.currentTimeMillis() - startTime
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Endpoint de teste respondeu com sucesso")
                    ConnectivityResult(
                        success = true,
                        url = testUrl,
                        responseTime = responseTime,
                        message = "API mobile está disponível"
                    )
                } else {
                    Log.w(TAG, "Endpoint de teste respondeu com código: ${response.code}")
                    ConnectivityResult(
                        success = false,
                        url = testUrl,
                        responseTime = responseTime,
                        message = "API mobile retornou código ${response.code}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao testar endpoint", e)
                ConnectivityResult(
                    success = false,
                    url = null,
                    responseTime = -1,
                    message = "Erro ao acessar API: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Valida apenas o formato do IP (sem testar conectividade)
     */
    fun validateIpFormat(ip: String?): Boolean {
        if (ip.isNullOrBlank()) return false
        return ServerConfigManager.isValidIp(ip)
    }
    
    /**
     * Sugere IPs comuns baseado na rede
     */
    fun suggestCommonIps(): List<String> {
        return listOf(
            "10.14.250.214",  // IP atual do servidor
            "192.168.1.1",
            "192.168.0.1",
            "192.168.1.100",
            "10.0.0.1",
            "localhost"
        )
    }
    
    /**
     * Gera mensagem de ajuda para configuração
     */
    fun getConfigurationHelp(): String {
        return """
            Como configurar o servidor:
            
            1. Descubra o IP do servidor:
               - No servidor, execute: ipconfig (Windows) ou ifconfig (Linux/Mac)
               - Procure por "IPv4" ou "inet"
            
            2. Configure no app:
               - Vá em Configurações
               - Digite o IP do servidor (ex: 10.14.250.214)
               - Porta padrão: 8081
            
            3. Teste a conexão:
               - O app testará automaticamente
               - Você verá se está tudo OK
            
            Dica: Certifique-se de que o smartphone e o servidor estão na mesma rede Wi-Fi!
        """.trimIndent()
    }
}
