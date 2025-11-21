package com.inventario.mobile.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.inventario.mobile.R
import com.inventario.mobile.network.NetworkQuality

/**
 * Indicador visual de qualidade de rede
 * Mostra emoji + descrição da qualidade atual
 * 
 * Uso:
 * ```xml
 * <com.inventario.mobile.ui.components.NetworkQualityIndicator
 *     android:id="@+id/networkQualityIndicator"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 * 
 * ```kotlin
 * networkQualityIndicator.setQuality(NetworkQuality.BOA)
 * ```
 */
class NetworkQualityIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val tvEmoji: TextView
    private val tvDescription: TextView
    private val tvDetails: TextView
    
    init {
        orientation = HORIZONTAL
        setPadding(16, 8, 16, 8)
        
        // Criar views programaticamente (ou inflar de XML se preferir)
        tvEmoji = TextView(context).apply {
            textSize = 20f
            setPadding(0, 0, 16, 0)
        }
        
        val textContainer = LinearLayout(context).apply {
            orientation = VERTICAL
        }
        
        tvDescription = TextView(context).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
        
        tvDetails = TextView(context).apply {
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            alpha = 0.8f
        }
        
        textContainer.addView(tvDescription)
        textContainer.addView(tvDetails)
        
        addView(tvEmoji)
        addView(textContainer)
        
        // Estado inicial
        setQuality(NetworkQuality.UNKNOWN)
    }
    
    /**
     * Atualiza qualidade da rede
     */
    fun setQuality(quality: NetworkQuality, pendingCount: Int = 0) {
        tvEmoji.text = quality.toEmoji()
        tvDescription.text = quality.toDescription()
        
        // Detalhes adicionais
        val details = when {
            quality == NetworkQuality.SEM_REDE && pendingCount > 0 -> 
                "$pendingCount coletas pendentes"
            quality == NetworkQuality.RUIM && pendingCount > 0 -> 
                "Rede instável - $pendingCount pendentes"
            quality == NetworkQuality.REGULAR && pendingCount > 0 -> 
                "Salvando localmente - $pendingCount pendentes"
            pendingCount > 0 -> 
                "$pendingCount coletas aguardando sincronização"
            else -> 
                "Todas as coletas sincronizadas"
        }
        tvDetails.text = details
        
        // Cor de fundo baseada na qualidade
        setBackgroundColor(getBackgroundColor(quality))
    }
    
    /**
     * Obtém cor de fundo baseada na qualidade
     */
    private fun getBackgroundColor(quality: NetworkQuality): Int {
        return when (quality) {
            NetworkQuality.EXCELENTE, NetworkQuality.BOA -> 
                ContextCompat.getColor(context, android.R.color.holo_green_dark)
            NetworkQuality.REGULAR -> 
                ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            NetworkQuality.RUIM, NetworkQuality.MUITO_RUIM -> 
                ContextCompat.getColor(context, android.R.color.holo_red_dark)
            NetworkQuality.SEM_REDE -> 
                ContextCompat.getColor(context, android.R.color.darker_gray)
            NetworkQuality.UNKNOWN -> 
                ContextCompat.getColor(context, android.R.color.darker_gray)
        }
    }
    
    /**
     * Mostra/esconde indicador
     */
    fun show() {
        visibility = VISIBLE
    }
    
    fun hide() {
        visibility = GONE
    }
}
