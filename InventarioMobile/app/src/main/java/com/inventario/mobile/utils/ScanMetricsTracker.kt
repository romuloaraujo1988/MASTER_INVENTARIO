package com.inventario.mobile.utils

import android.os.SystemClock
import android.util.Log

/**
 * Tracker para coletar métricas de scan (QR Code vs Código de Barras).
 *
 * AL-06 (correcoes-seguranca — maio/2026): os marcos de tempo passaram de
 * `System.currentTimeMillis()` (wall-clock, afetado por mudança de fuso
 * horário e ajuste NTP) para [SystemClock.elapsedRealtime], que é monotônico
 * e imune a ajustes de relógio. Com isso, deixa de ser possível a métrica
 * registrar um tempo negativo ou inflacionado se o relógio do dispositivo
 * saltar durante a coleta.
 *
 * **Observação:** `elapsedRealtime()` começa no boot do dispositivo — os
 * valores absolutos não têm significado calendárico e NÃO devem ser usados
 * para datar coletas no servidor. Apenas diferenças entre dois marcos
 * (`fimScan - inicioScan`) são válidas.
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
     * Inicia tracking de scan.
     */
    fun iniciarScan(tipo: String) {
        tipoScan = tipo
        tentativas = 0
        erros = 0
        inicioScan = SystemClock.elapsedRealtime()
        fimScan = 0

        Log.d(TAG, "Scan iniciado: tipo=$tipo, elapsedRealtime=$inicioScan")
    }

    /**
     * Registra uma tentativa de scan.
     */
    fun registrarTentativa() {
        tentativas++
        Log.d(TAG, "Tentativa #$tentativas registrada")
    }

    /**
     * Registra um erro de scan.
     */
    fun registrarErro() {
        erros++
        Log.d(TAG, "Erro #$erros registrado")
    }

    /**
     * Finaliza tracking e retorna métricas coletadas.
     */
    fun finalizarScan(): ScanMetrics {
        fimScan = SystemClock.elapsedRealtime()
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
     * Calcula tempo decorrido em segundos.
     *
     * AL-06: proteção explícita contra valores negativos. Em teoria,
     * `elapsedRealtime()` é monotônico, mas ainda blindamos com `coerceAtLeast(0)`
     * para o caso de restauração do estado (onde `inicioScan` veio do
     * SavedStateHandle e `fimScan` é de um boot posterior).
     */
    private fun calcularTempoSegundos(): Int {
        if (inicioScan == 0L) return 0

        val fim = if (fimScan > 0) fimScan else SystemClock.elapsedRealtime()
        val tempoMs = (fim - inicioScan).coerceAtLeast(0)
        return (tempoMs / 1000).toInt()
    }

    /**
     * Avalia qualidade da etiqueta baseado em tentativas e erros.
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
     * Reseta o tracker para novo scan.
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
     * Verifica se o scan está em andamento.
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
