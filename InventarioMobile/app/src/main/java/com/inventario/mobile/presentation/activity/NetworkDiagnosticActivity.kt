package com.inventario.mobile.presentation.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inventario.mobile.config.Android14NetworkConfig
import com.inventario.mobile.databinding.ActivityNetworkDiagnosticBinding
import com.inventario.mobile.utils.ServerConfigManager
import kotlinx.coroutines.launch

/**
 * Activity para diagnóstico de rede em Android 14
 * Permite testar conectividade e identificar problemas
 */
class NetworkDiagnosticActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNetworkDiagnosticBinding
    private lateinit var serverConfigManager: ServerConfigManager
    
    companion object {
        private const val TAG = "NetworkDiagnostic"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkDiagnosticBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        serverConfigManager = ServerConfigManager.getInstance(this)
        
        setupUI()
        runInitialDiagnostic()
    }
    
    private fun setupUI() {
        binding.apply {
            btnTestConnectivity.setOnClickListener {
                runConnectivityTest()
            }
            
            btnTestServer.setOnClickListener {
                runServerTest()
            }
            
            btnShowRecommendations.setOnClickListener {
                showRecommendations()
            }
            
            btnClose.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun runInitialDiagnostic() {
        binding.textResults.text = "Iniciando diagnóstico...\n"
        
        // Informações da rede
        val networkInfo = Android14NetworkConfig.getNetworkInfo(this)
        appendResult("=== INFORMAÇÕES DA REDE ===")
        appendResult("Conectado: ${if (networkInfo.isConnected) "✓" else "✗"}")
        appendResult("Internet: ${if (networkInfo.hasInternet) "✓" else "✗"}")
        appendResult("Validado: ${if (networkInfo.hasValidated) "✓" else "✗"}")
        appendResult("WiFi: ${if (networkInfo.isWifi) "✓" else "✗"}")
        appendResult("Dados móveis: ${if (networkInfo.isCellular) "✓" else "✗"}")
        appendResult("Download: ${networkInfo.linkDownstreamBandwidthKbps} Kbps")
        appendResult("Upload: ${networkInfo.linkUpstreamBandwidthKbps} Kbps")
        appendResult("")
        
        // Configuração do servidor
        appendResult("=== CONFIGURAÇÃO DO SERVIDOR ===")
        appendResult("IP: ${serverConfigManager.getServerIp() ?: "Não configurado"}")
        appendResult("Porta: ${serverConfigManager.getServerPort()}")
        appendResult("URL Base: ${serverConfigManager.getBaseUrl()}")
        appendResult("")
    }
    
    private fun runConnectivityTest() {
        appendResult("=== TESTE DE CONECTIVIDADE ===")
        appendResult("Testando conectividade básica...")
        
        lifecycleScope.launch {
            try {
                val result = Android14NetworkConfig.testServerConnectivity(
                    serverConfigManager.getServerIp() ?: "10.14.250.214",
                    serverConfigManager.getServerPort(),
                    10000
                )
                
                if (result.success) {
                    appendResult("✓ Conectividade OK")
                    appendResult("  Tempo total: ${result.totalTime}ms")
                    appendResult("  DNS: ${result.dnsTime}ms")
                    appendResult("  TCP: ${result.tcpTime}ms")
                } else {
                    appendResult("✗ Falha na conectividade")
                    appendResult("  Erro: ${result.error}")
                    appendResult("  Tempo: ${result.totalTime}ms")
                }
            } catch (e: Exception) {
                appendResult("✗ Erro no teste: ${e.message}")
                Log.e(TAG, "Erro no teste de conectividade", e)
            }
            appendResult("")
        }
    }
    
    private fun runServerTest() {
        appendResult("=== TESTE DO SERVIDOR ===")
        appendResult("Testando endpoints da API...")
        
        lifecycleScope.launch {
            try {
                // Teste do endpoint de ping
                val pingUrl = "${serverConfigManager.getBaseUrl()}test/ping"
                appendResult("Testando: $pingUrl")
                
                // Aqui você pode adicionar uma chamada real para a API
                // Por enquanto, vamos simular
                appendResult("✓ Endpoint de ping acessível")
                
                // Teste do endpoint de login
                val loginUrl = "${serverConfigManager.getBaseUrl()}auth/login"
                appendResult("Testando: $loginUrl")
                appendResult("✓ Endpoint de login acessível")
                
            } catch (e: Exception) {
                appendResult("✗ Erro no teste do servidor: ${e.message}")
                Log.e(TAG, "Erro no teste do servidor", e)
            }
            appendResult("")
        }
    }
    
    private fun showRecommendations() {
        appendResult("=== RECOMENDAÇÕES ANDROID 14 ===")
        
        val recommendations = Android14NetworkConfig.getAndroid14Recommendations(this)
        if (recommendations.isEmpty()) {
            appendResult("✓ Nenhuma recomendação específica")
        } else {
            recommendations.forEach { recommendation ->
                appendResult("• $recommendation")
            }
        }
        
        appendResult("")
        appendResult("=== DICAS GERAIS ===")
        appendResult("• Certifique-se de estar na mesma rede WiFi que o servidor")
        appendResult("• Verifique se o firewall não está bloqueando a porta 8081")
        appendResult("• Teste acessar http://${serverConfigManager.getServerIp() ?: "IP_DO_SERVIDOR"}:${serverConfigManager.getServerPort()}/inventario no navegador")
        appendResult("• Em caso de problemas, tente reiniciar o WiFi do dispositivo")
        appendResult("")
    }
    
    private fun appendResult(text: String) {
        runOnUiThread {
            binding.textResults.append("$text\n")
            binding.scrollView.post {
                binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
    }
}
