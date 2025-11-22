package com.inventario.mobile.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.inventario.mobile.R

/**
 * Componente visual para indicar status de conexão
 * 
 * Estados:
 * - ONLINE (verde): Conectado ao servidor
 * - OFFLINE (laranja): Sem conexão, usando dados locais
 * - SYNCING (azul): Sincronizando dados
 */
class OfflineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val statusText: TextView
    private val statusIcon: TextView

    enum class Status {
        ONLINE,
        OFFLINE,
        SYNCING
    }

    init {
        // Inflar layout
        LayoutInflater.from(context).inflate(R.layout.view_offline_indicator, this, true)
        
        statusText = findViewById(R.id.tvStatusText)
        statusIcon = findViewById(R.id.tvStatusIcon)
        
        // Estado inicial: offline
        setStatus(Status.OFFLINE)
    }

    /**
     * Atualiza o status visual do indicador
     */
    fun setStatus(status: Status) {
        when (status) {
            Status.ONLINE -> {
                statusIcon.text = "●"
                statusIcon.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                statusText.text = "Online"
                statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                visibility = GONE // Esconder quando online
            }
            Status.OFFLINE -> {
                statusIcon.text = "●"
                statusIcon.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                statusText.text = "Modo Offline"
                statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                visibility = VISIBLE
            }
            Status.SYNCING -> {
                statusIcon.text = "⟳"
                statusIcon.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                statusText.text = "Sincronizando..."
                statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                visibility = VISIBLE
            }
        }
    }

    /**
     * Mostra mensagem customizada
     */
    fun setCustomMessage(message: String, isError: Boolean = false) {
        statusText.text = message
        if (isError) {
            statusIcon.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        }
        visibility = VISIBLE
    }
}
