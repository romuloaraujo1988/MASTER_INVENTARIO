package com.inventario.mobile.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.inventario.mobile.R
import com.google.android.material.card.MaterialCardView

/**
 * Componente visual que indica quando o app está em modo offline
 * 
 * Funcionalidades:
 * - Mostra banner no topo da tela quando offline
 * - Animação suave de entrada/saída
 * - Cores diferenciadas para chamar atenção
 * - Mensagem informativa
 */
class OfflineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: MaterialCardView
    private val tvMessage: TextView
    private val tvDetails: TextView

    init {
        // Criar views programaticamente
        cardView = MaterialCardView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(8, 8, 8, 8)
            }
            cardElevation = 4f * resources.displayMetrics.density
            radius = 8f * resources.displayMetrics.density
            setCardBackgroundColor(0xFFFFF3E0.toInt())
        }
        
        val linearLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }
        
        tvMessage = TextView(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "📴 Modo Offline"
            setTextColor(0xFFE65100.toInt())
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        tvDetails = TextView(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4
            }
            text = "Suas coletas serão salvas localmente"
            setTextColor(0xFFBF360C.toInt())
            textSize = 13f
            gravity = android.view.Gravity.CENTER
        }
        
        linearLayout.addView(tvMessage)
        linearLayout.addView(tvDetails)
        cardView.addView(linearLayout)
        addView(cardView)
        
        // Inicialmente oculto
        visibility = View.GONE
    }

    /**
     * Mostra o indicador de modo offline
     */
    fun show(message: String = "📴 Modo Offline", details: String = "Suas coletas serão salvas localmente") {
        tvMessage.text = message
        tvDetails.text = details
        
        // Animação de entrada
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    /**
     * Oculta o indicador
     */
    fun hide() {
        // Animação de saída
        animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                visibility = View.GONE
            }
            .start()
    }

    /**
     * Atualiza a mensagem
     */
    fun updateMessage(message: String, details: String? = null) {
        tvMessage.text = message
        if (details != null) {
            tvDetails.text = details
            tvDetails.visibility = View.VISIBLE
        } else {
            tvDetails.visibility = View.GONE
        }
    }
}
