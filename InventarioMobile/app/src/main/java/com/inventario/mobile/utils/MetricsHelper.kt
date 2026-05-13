package com.inventario.mobile.utils

import android.util.Log
import java.util.Calendar

/**
 * Helper para coletar métricas gerais de coleta
 * Fornece métodos utilitários para cálculo de tempo, horário, etc.
 */
object MetricsHelper {
    
    private const val TAG = "MetricsHelper"
    
    // Períodos do dia
    const val PERIODO_MANHA = "MANHA"
    const val PERIODO_TARDE = "TARDE"
    const val PERIODO_NOITE = "NOITE"
    
    // Métodos de coleta
    const val METODO_QR_CODE = "QR_CODE"
    const val METODO_CODIGO_BARRAS = "CODIGO_BARRAS"
    const val METODO_MANUAL = "MANUAL"
    const val METODO_BUSCA = "BUSCA"
    const val METODO_SEM_ETIQUETA = "SEM_ETIQUETA"
    
    /**
     * Calcula horário da coleta (hora, dia da semana, período)
     * @return Triple<hora (0-23), dia (1-7), período>
     */
    fun calcularHorario(): Triple<Int, Int, String> {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val dia = calendar.get(Calendar.DAY_OF_WEEK) // 1=Dom, 2=Seg, ..., 7=Sab
        val periodo = calcularPeriodo(hora)
        
        Log.d(TAG, "Horário calculado: hora=$hora, dia=$dia, periodo=$periodo")
        return Triple(hora, dia, periodo)
    }
    
    /**
     * Calcula período do dia baseado na hora
     */
    fun calcularPeriodo(hora: Int): String {
        return when (hora) {
            in 6..11 -> PERIODO_MANHA
            in 12..17 -> PERIODO_TARDE
            else -> PERIODO_NOITE
        }
    }
    
    /**
     * Calcula tempo decorrido em segundos a partir de um marco em
     * `System.currentTimeMillis()` (wall-clock).
     *
     * AL-06 (maio/2026): **use preferencialmente [calcularTempoElapsedSegundos]**
     * com [android.os.SystemClock.elapsedRealtime]. Esta função permanece
     * apenas para compatibilidade com consumidores antigos que ainda não
     * foram migrados.
     *
     * @param inicioMs Timestamp de início em milissegundos (wall-clock)
     * @return Tempo em segundos, sempre >= 0 (coerceAtLeast)
     */
    fun calcularTempoSegundos(inicioMs: Long): Int {
        if (inicioMs <= 0) return 0

        val fimMs = System.currentTimeMillis()
        val tempoMs = (fimMs - inicioMs).coerceAtLeast(0)
        val segundos = (tempoMs / 1000).toInt()

        Log.d(TAG, "Tempo calculado (wall-clock): ${segundos}s (${tempoMs}ms)")
        return segundos
    }

    /**
     * Calcula tempo decorrido em segundos a partir de um marco em
     * `SystemClock.elapsedRealtime()` (monotônico).
     *
     * AL-06 (correcoes-seguranca — maio/2026): preferir este método para
     * medir durações de operações do usuário (coleta, scan, preenchimento).
     * `elapsedRealtime` não é afetado por mudança de fuso horário, ajuste
     * NTP ou usuário mexendo no relógio, então os tempos sempre refletem
     * o tempo real decorrido.
     *
     * @param inicioElapsedMs Marco inicial em `SystemClock.elapsedRealtime()`
     * @return Tempo em segundos, sempre >= 0
     */
    fun calcularTempoElapsedSegundos(inicioElapsedMs: Long): Int {
        if (inicioElapsedMs <= 0) return 0

        val fimMs = android.os.SystemClock.elapsedRealtime()
        val tempoMs = (fimMs - inicioElapsedMs).coerceAtLeast(0)
        return (tempoMs / 1000).toInt()
    }
    
    /**
     * Formata tempo em segundos para exibição
     */
    fun formatarTempo(segundos: Int): String {
        return if (segundos < 60) {
            "${segundos}s"
        } else if (segundos < 3600) {
            val min = segundos / 60
            val seg = segundos % 60
            "${min}m ${seg}s"
        } else {
            val horas = segundos / 3600
            val min = (segundos % 3600) / 60
            val seg = segundos % 60
            "${horas}h ${min}m ${seg}s"
        }
    }
    
    /**
     * Retorna descrição do período
     */
    fun getPeriodoDescricao(periodo: String): String {
        return when (periodo) {
            PERIODO_MANHA -> "Manhã"
            PERIODO_TARDE -> "Tarde"
            PERIODO_NOITE -> "Noite"
            else -> periodo
        }
    }
    
    /**
     * Retorna descrição do método de coleta
     */
    fun getMetodoDescricao(metodo: String): String {
        return when (metodo) {
            METODO_QR_CODE -> "QR Code"
            METODO_CODIGO_BARRAS -> "Código de Barras"
            METODO_MANUAL -> "Manual"
            METODO_BUSCA -> "Busca"
            METODO_SEM_ETIQUETA -> "Sem Etiqueta"
            else -> metodo
        }
    }
    
    /**
     * Retorna nome do dia da semana
     */
    fun getDiaSemanaDescricao(dia: Int): String {
        return when (dia) {
            1 -> "Domingo"
            2 -> "Segunda"
            3 -> "Terça"
            4 -> "Quarta"
            5 -> "Quinta"
            6 -> "Sexta"
            7 -> "Sábado"
            else -> "Desconhecido"
        }
    }
    
    /**
     * Retorna emoji do período
     */
    fun getPeriodoEmoji(periodo: String): String {
        return when (periodo) {
            PERIODO_MANHA -> "🌅"
            PERIODO_TARDE -> "☀️"
            PERIODO_NOITE -> "🌙"
            else -> "⏰"
        }
    }
    
    /**
     * Retorna emoji do método
     */
    fun getMetodoEmoji(metodo: String): String {
        return when (metodo) {
            METODO_QR_CODE -> "📱"
            METODO_CODIGO_BARRAS -> "📊"
            METODO_MANUAL -> "✍️"
            METODO_BUSCA -> "🔍"
            METODO_SEM_ETIQUETA -> "📦"
            else -> "❓"
        }
    }
    
    /**
     * Valida se o tempo é razoável (não negativo e não muito grande)
     */
    fun isTempoValido(segundos: Int): Boolean {
        return segundos in 0..3600 // Máximo 1 hora
    }
    
    /**
     * Cria resumo das métricas para log
     */
    fun criarResumoMetricas(
        tempoTotal: Int,
        tempoScan: Int?,
        tempoPreenchimento: Int?,
        metodo: String,
        periodo: String
    ): String {
        return """
            Métricas da Coleta:
            - Tempo Total: ${formatarTempo(tempoTotal)}
            - Tempo Scan: ${tempoScan?.let { formatarTempo(it) } ?: "N/A"}
            - Tempo Preenchimento: ${tempoPreenchimento?.let { formatarTempo(it) } ?: "N/A"}
            - Método: ${getMetodoDescricao(metodo)} ${getMetodoEmoji(metodo)}
            - Período: ${getPeriodoDescricao(periodo)} ${getPeriodoEmoji(periodo)}
        """.trimIndent()
    }
}
