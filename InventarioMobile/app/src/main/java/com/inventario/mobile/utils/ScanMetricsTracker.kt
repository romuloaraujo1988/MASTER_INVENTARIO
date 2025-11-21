package com.inventario.mobile.utils

import android.util.Log

/**
 * Tracker para coletar métricas de scan (QR Code vs Código de Barras)
 * Coleta dados de forma transparente para análise posterior
 */
class ScanMetricsTracker {
    
    companion object {
        private const val TAG = "ScanMetricsTracker"
        
        // Tipos de scan
        const val TIPO_QR_CODE = "QR_CODE"
        const val TIPO_CODIGO_BARRAS = "CODIGO_BARRAS"
        
        // Qualidade da etiqueta
        const val QUALIDADE_OTIMA = "OTIMA"
        const val QUALIDADE_BOA = "BOA"
        const val QUALIDADE_REGULAR = "REGULAR"
        const val QUALIDADE_RUIM = "RUIM"
    }
    
    private var tipoScan: String = TIPO_QR_CODE
    private var tentativas: Int = 0
    private var erros: Int = 0
    private var inicioScan: Long = 0
    private var fimScan: Long = 0
    
    /**
     * Inicia tracking de scan
     */
    fun iniciarScan(tipo: String) {
        tipoScan = tipo
        tentativas = 0
        erros = 0
        inicioScan = System.currentTimeMillis()
        fimScan = 0
        
        Log.d(TAG, "Scan iniciado: tipo=$tipo, timestamp=$inicioScan")
    }
    
    /**
     * Registra uma tentativa de scan
     */
    fun registrarTentativa() {
        tentativas++
        Log.d(TAG, "Tentativa #$tentativas registrada")
    }
    
    /**
     * Registra um erro de scan
     */
    fun registrarErro() {
        erros++
        Log.d(TAG, "Erro #$erros registrado")
    }
    
    /**
     * Finaliza tracking e retorna métricas coletadas
     */
    fun finalizarScan(): ScanMetrics {
        fimScan = System.currentTimeMillis()
        val tempoScan = calcularTempoSegundos()
        val qualidade = avaliarQualidade()
        
        val metrics = ScanMetrics(
            tipoScan = tipoScan,
            tempoSegundos = tempoScan,
            tentativas = tentativas,
            erros = erros,
            qualidadeEtiqueta = qualidade
        )
        
        Log.d(TAG, """
            Scan finalizado:
            - Tipo: ${metrics.tipoScan}
            - Tempo: ${metrics.tempoSegundos}s
            - Tentativas: ${metrics.tentativas}
            - Erros: ${metrics.erros}
            - Qualidade: ${metrics.qualidadeEtiqueta}
        """.trimIndent())
        
        return metrics
    }
    
    /**
     * Calcula tempo decorrido em segundos
     */
    private fun calcularTempoSegundos(): Int {
        if (inicioScan == 0L) return 0
        
        val fim = if (fimScan > 0) fimScan else System.currentTimeMillis()
        val tempoMs = fim - inicioScan
        return (tempoMs / 1000).toInt()
    }
    
    /**
     * Avalia qualidade da etiqueta baseado em tentativas e erros
     */
    private fun avaliarQualidade(): String {
        return when {
            // Ótima: sucesso na primeira tentativa sem erros
            erros == 0 && tentativas <= 1 -> QUALIDADE_OTIMA
            
            // Boa: sucesso em até 2 tentativas com no máximo 1 erro
            erros <= 1 && tentativas <= 2 -> QUALIDADE_BOA
            
            // Regular: sucesso em até 3 tentativas com até 2 erros
            erros <= 2 && tentativas <= 3 -> QUALIDADE_REGULAR
            
            // Ruim: mais de 3 tentativas ou mais de 2 erros
            else -> QUALIDADE_RUIM
        }
    }
    
    /**
     * Reseta o tracker para novo scan
     */
    fun reset() {
        tipoScan = TIPO_QR_CODE
        tentativas = 0
        erros = 0
        inicioScan = 0
        fimScan = 0
        Log.d(TAG, "Tracker resetado")
    }
    
    /**
     * Verifica se o scan está em andamento
     */
    fun isEmAndamento(): Boolean {
        return inicioScan > 0 && fimScan == 0L
    }
}

/**
 * Data class com métricas coletadas do scan
 */
data class ScanMetrics(
    val tipoScan: String,
    val tempoSegundos: Int,
    val tentativas: Int,
    val erros: Int,
    val qualidadeEtiqueta: String
) {
    /**
     * Retorna descrição formatada do tempo
     */
    fun getTempoFormatado(): String {
        return if (tempoSegundos < 60) {
            "${tempoSegundos}s"
        } else {
            val min = tempoSegundos / 60
            val seg = tempoSegundos % 60
            "${min}m ${seg}s"
        }
    }
    
    /**
     * Retorna descrição do tipo de scan
     */
    fun getTipoScanDescricao(): String {
        return when (tipoScan) {
            ScanMetricsTracker.TIPO_QR_CODE -> "QR Code"
            ScanMetricsTracker.TIPO_CODIGO_BARRAS -> "Código de Barras"
            else -> tipoScan
        }
    }
    
    /**
     * Retorna emoji baseado na qualidade
     */
    fun getQualidadeEmoji(): String {
        return when (qualidadeEtiqueta) {
            ScanMetricsTracker.QUALIDADE_OTIMA -> "⭐"
            ScanMetricsTracker.QUALIDADE_BOA -> "✅"
            ScanMetricsTracker.QUALIDADE_REGULAR -> "⚠️"
            ScanMetricsTracker.QUALIDADE_RUIM -> "❌"
            else -> "❓"
        }
    }
    
    /**
     * Verifica se o scan foi bem-sucedido na primeira tentativa
     */
    fun isSucessoPrimeiraTentativa(): Boolean {
        return tentativas == 1 && erros == 0
    }
    
    override fun toString(): String {
        return "ScanMetrics(tipo=$tipoScan, tempo=${getTempoFormatado()}, " +
               "tentativas=$tentativas, erros=$erros, qualidade=$qualidadeEtiqueta)"
    }
}
