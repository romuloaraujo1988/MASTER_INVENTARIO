package com.inventario.mobile.presentation.charts

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

/**
 * Helper para configuração e estilização de gráficos MPAndroidChart
 */
object ChartHelper {

    // Cores modernas do Material Design
    private val CHART_COLORS = intArrayOf(
        Color.rgb(52, 152, 219),   // Azul
        Color.rgb(46, 204, 113),   // Verde
        Color.rgb(241, 196, 15),   // Amarelo
        Color.rgb(231, 76, 60),    // Vermelho
        Color.rgb(155, 89, 182),   // Roxo
        Color.rgb(149, 165, 166),  // Cinza
        Color.rgb(26, 188, 156),   // Turquesa
        Color.rgb(230, 126, 34)    // Laranja
    )

    /**
     * Configura gráfico de pizza
     */
    fun setupPieChart(chart: PieChart) {
        chart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawHoleEnabled(true)
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            // Legenda
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 10f
                textSize = 12f
            }
            
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            
            animateY(1000)
        }
    }

    /**
     * Cria dataset de pizza
     */
    fun createPieDataSet(entries: List<PieEntry>, label: String): PieDataSet {
        return PieDataSet(entries, label).apply {
            setDrawIcons(false)
            sliceSpace = 3f
            selectionShift = 5f
            colors = CHART_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
    }

    /**
     * Configura gráfico de barras
     */
    fun setupBarChart(chart: BarChart, labels: List<String>) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            // Eixo X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = labels.size
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 10f
                labelRotationAngle = -45f
            }
            
            // Eixo Y esquerdo
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textSize = 10f
                axisMinimum = 0f
            }
            
            // Eixo Y direito (desabilitar)
            axisRight.isEnabled = false
            
            // Legenda
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
                textSize = 12f
            }
            
            animateY(1000)
        }
    }

    /**
     * Cria dataset de barras
     */
    fun createBarDataSet(entries: List<BarEntry>, label: String, color: Int? = null): BarDataSet {
        return BarDataSet(entries, label).apply {
            this.color = color ?: CHART_COLORS[0]
            valueTextSize = 10f
            valueTextColor = Color.BLACK
        }
    }

    /**
     * Configura gráfico de linhas
     */
    fun setupLineChart(chart: LineChart, labels: List<String>) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setPinchZoom(false)
            setScaleEnabled(true)
            
            // Eixo X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = labels.size
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 10f
                labelRotationAngle = -45f
            }
            
            // Eixo Y esquerdo
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                textSize = 10f
                axisMinimum = 0f
            }
            
            // Eixo Y direito (desabilitar)
            axisRight.isEnabled = false
            
            // Legenda
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
                textSize = 12f
            }
            
            animateX(1000)
        }
    }

    /**
     * Cria dataset de linhas
     */
    fun createLineDataSet(entries: List<Entry>, label: String, color: Int? = null): LineDataSet {
        return LineDataSet(entries, label).apply {
            this.color = color ?: CHART_COLORS[0]
            setCircleColor(color ?: CHART_COLORS[0])
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setDrawFilled(true)
            fillColor = color ?: CHART_COLORS[0]
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
    }

    /**
     * Cria gráfico de progresso (barras horizontais)
     */
    fun createProgressBarData(coletados: Int, pendentes: Int): BarData {
        val entries = listOf(
            BarEntry(0f, coletados.toFloat()),
            BarEntry(1f, pendentes.toFloat())
        )
        
        val dataSet = BarDataSet(entries, "Progresso").apply {
            colors = listOf(CHART_COLORS[1], CHART_COLORS[3]) // Verde e Vermelho
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }
        
        return BarData(dataSet).apply {
            barWidth = 0.5f
        }
    }

    /**
     * Cria gráfico de estado de conservação (pizza)
     * Estados conforme legislação: Bom, Ocioso, Recuperável, Antieconômico, Irrecuperável
     */
    fun createStatusPieData(
        bom: Int,
        ocioso: Int,
        recuperavel: Int,
        antieconomico: Int,
        irrecuperavel: Int,
        semInfo: Int = 0
    ): PieData {
        val entries = mutableListOf<PieEntry>()
        
        // Cores específicas para cada estado conforme legislação
        val colors = mutableListOf<Int>()
        
        if (bom > 0) {
            entries.add(PieEntry(bom.toFloat(), "Bom"))
            colors.add(Color.parseColor("#4CAF50")) // Verde
        }
        if (ocioso > 0) {
            entries.add(PieEntry(ocioso.toFloat(), "Ocioso"))
            colors.add(Color.parseColor("#2196F3")) // Azul
        }
        if (recuperavel > 0) {
            entries.add(PieEntry(recuperavel.toFloat(), "Recuperável"))
            colors.add(Color.parseColor("#FFC107")) // Amarelo
        }
        if (antieconomico > 0) {
            entries.add(PieEntry(antieconomico.toFloat(), "Antieconômico"))
            colors.add(Color.parseColor("#FF9800")) // Laranja
        }
        if (irrecuperavel > 0) {
            entries.add(PieEntry(irrecuperavel.toFloat(), "Irrecuperável"))
            colors.add(Color.parseColor("#F44336")) // Vermelho
        }
        if (semInfo > 0) {
            entries.add(PieEntry(semInfo.toFloat(), "Sem Info"))
            colors.add(Color.parseColor("#9E9E9E")) // Cinza
        }
        
        // Se não houver dados, mostrar placeholder
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Sem dados"))
            colors.add(Color.parseColor("#BDBDBD"))
        }
        
        val dataSet = PieDataSet(entries, "Estado de Conservação").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.3f
            valueLinePart2Length = 0.4f
        }
        
        return PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
    }

    /**
     * Cria gráfico de evolução (linhas)
     */
    fun createEvolutionLineData(dataPoints: Map<String, Int>): Pair<LineData, List<String>> {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        dataPoints.entries.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }
        
        val dataSet = createLineDataSet(entries, "Coletas Acumuladas", CHART_COLORS[0])
        
        return Pair(LineData(dataSet), labels)
    }

    /**
     * Cria gráfico de top itens (barras)
     */
    fun createTopItemsBarData(items: Map<String, Int>): Pair<BarData, List<String>> {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        items.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }
        
        val dataSet = createBarDataSet(entries, "Quantidade", CHART_COLORS[1])
        
        return Pair(BarData(dataSet).apply { barWidth = 0.8f }, labels)
    }

    /**
     * Formata número para exibição
     */
    fun formatNumber(value: Int): String {
        return when {
            value >= 1000000 -> String.format("%.1fM", value / 1000000.0)
            value >= 1000 -> String.format("%.1fK", value / 1000.0)
            else -> value.toString()
        }
    }

    /**
     * Formata percentual
     */
    fun formatPercent(value: Double): String {
        return String.format("%.1f%%", value)
    }
}
