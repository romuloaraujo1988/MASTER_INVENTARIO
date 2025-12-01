package com.inventario.mobile.presentation.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.inventario.mobile.R

/**
 * Helper para mapeamento de cores baseado no percentual de progresso.
 * 
 * Faixas de cores:
 * - 0-25%: Vermelho (crítico)
 * - 26-50%: Laranja (atenção)
 * - 51-75%: Amarelo (progresso)
 * - 76-100%: Verde (bom/completo)
 */
object ProgressColorHelper {
    
    /**
     * Retorna o resource ID da cor baseado no percentual.
     * 
     * @param percentual Valor de 0 a 100
     * @return Resource ID da cor
     */
    @ColorRes
    fun getColorRes(percentual: Float): Int {
        return when {
            percentual <= 25f -> R.color.error
            percentual <= 50f -> R.color.warning
            percentual <= 75f -> R.color.warning_light
            else -> R.color.success
        }
    }
    
    /**
     * Retorna a cor resolvida baseada no percentual.
     * 
     * @param context Context para resolver a cor
     * @param percentual Valor de 0 a 100
     * @return Cor resolvida
     */
    @ColorInt
    fun getColor(context: Context, percentual: Float): Int {
        return ContextCompat.getColor(context, getColorRes(percentual))
    }
    
    /**
     * Retorna a faixa de progresso (0-3) baseada no percentual.
     * 
     * @param percentual Valor de 0 a 100
     * @return Faixa: 0 = crítico, 1 = atenção, 2 = progresso, 3 = bom
     */
    fun getFaixa(percentual: Float): Int {
        return when {
            percentual <= 25f -> 0
            percentual <= 50f -> 1
            percentual <= 75f -> 2
            else -> 3
        }
    }
    
    /**
     * Retorna o nome da faixa de progresso.
     * 
     * @param percentual Valor de 0 a 100
     * @return Nome da faixa
     */
    fun getFaixaNome(percentual: Float): String {
        return when {
            percentual <= 25f -> "Crítico"
            percentual <= 50f -> "Atenção"
            percentual <= 75f -> "Em Progresso"
            percentual >= 100f -> "Completo"
            else -> "Bom"
        }
    }
    
    /**
     * Retorna uma cor interpolada entre vermelho e verde baseada no percentual.
     * Útil para gradientes suaves.
     * 
     * @param percentual Valor de 0 a 100
     * @return Cor interpolada
     */
    @ColorInt
    fun getInterpolatedColor(percentual: Float): Int {
        val normalizedPercent = (percentual / 100f).coerceIn(0f, 1f)
        
        // Cores base
        val redColor = Color.rgb(244, 67, 54)    // Material Red 500
        val yellowColor = Color.rgb(255, 193, 7)  // Material Amber 500
        val greenColor = Color.rgb(76, 175, 80)   // Material Green 500
        
        return if (normalizedPercent < 0.5f) {
            // Interpolar entre vermelho e amarelo
            val ratio = normalizedPercent * 2
            interpolateColor(redColor, yellowColor, ratio)
        } else {
            // Interpolar entre amarelo e verde
            val ratio = (normalizedPercent - 0.5f) * 2
            interpolateColor(yellowColor, greenColor, ratio)
        }
    }
    
    private fun interpolateColor(colorStart: Int, colorEnd: Int, ratio: Float): Int {
        val r = (Color.red(colorStart) + (Color.red(colorEnd) - Color.red(colorStart)) * ratio).toInt()
        val g = (Color.green(colorStart) + (Color.green(colorEnd) - Color.green(colorStart)) * ratio).toInt()
        val b = (Color.blue(colorStart) + (Color.blue(colorEnd) - Color.blue(colorStart)) * ratio).toInt()
        return Color.rgb(r, g, b)
    }
    
    /**
     * Verifica se o percentual indica conclusão (100%).
     */
    fun isCompleto(percentual: Float): Boolean = percentual >= 100f
    
    /**
     * Verifica se o percentual indica situação crítica (0-25%).
     */
    fun isCritico(percentual: Float): Boolean = percentual <= 25f
    
    /**
     * Verifica se o percentual indica atenção necessária (26-50%).
     */
    fun isAtencao(percentual: Float): Boolean = percentual in 26f..50f
}
