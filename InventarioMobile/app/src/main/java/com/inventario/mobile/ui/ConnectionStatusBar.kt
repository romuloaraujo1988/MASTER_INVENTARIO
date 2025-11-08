package com.inventario.mobile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ViewConnectionStatusBarBinding

/**
 * Barra de status de conexão que aparece no topo da tela
 * Mostra quando está offline ou sincronizando
 */
class ConnectionStatusBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewConnectionStatusBarBinding
    private var currentStatus: Status = Status.ONLINE

    init {
        binding = ViewConnectionStatusBarBinding.inflate(LayoutInflater.from(context), this, true)
        visibility = View.GONE
    }

    /**
     * Atualiza o status da conexão
     */
    fun setStatus(status: Status, message: String? = null) {
        if (currentStatus == status && message == null) return
        
        currentStatus = status
        
        when (status) {
            Status.ONLINE -> {
                hide()
            }
            
            Status.OFFLINE -> {
                show()
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.error))
                binding.ivStatusIcon.setImageResource(R.drawable.ic_sync_error)
                binding.tvStatusMessage.text = message ?: "Sem conexão - Modo offline ativo"
                binding.progressBar.visibility = View.GONE
            }
            
            Status.SYNCING -> {
                show()
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.info))
                binding.ivStatusIcon.setImageResource(R.drawable.ic_sync)
                binding.tvStatusMessage.text = message ?: "Sincronizando dados..."
                binding.progressBar.visibility = View.VISIBLE
                
                // Animar ícone de sincronização
                val rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_continuous)
                binding.ivStatusIcon.startAnimation(rotation)
            }
            
            Status.SYNC_SUCCESS -> {
                show()
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.success))
                binding.ivStatusIcon.setImageResource(R.drawable.ic_sync_done)
                binding.tvStatusMessage.text = message ?: "Sincronização concluída"
                binding.progressBar.visibility = View.GONE
                
                // Auto-ocultar após 3 segundos
                postDelayed({ hide() }, 3000)
            }
            
            Status.SYNC_ERROR -> {
                show()
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.warning))
                binding.ivStatusIcon.setImageResource(R.drawable.ic_warning_circle)
                binding.tvStatusMessage.text = message ?: "Erro na sincronização"
                binding.progressBar.visibility = View.GONE
                
                // Auto-ocultar após 5 segundos
                postDelayed({ hide() }, 5000)
            }
        }
    }

    /**
     * Mostra a barra com animação
     */
    private fun show() {
        if (visibility == View.VISIBLE) return
        
        visibility = View.VISIBLE
        alpha = 0f
        translationY = -height.toFloat()
        
        animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    /**
     * Oculta a barra com animação
     */
    private fun hide() {
        if (visibility == View.GONE) return
        
        animate()
            .alpha(0f)
            .translationY(-height.toFloat())
            .setDuration(300)
            .withEndAction {
                visibility = View.GONE
                binding.ivStatusIcon.clearAnimation()
            }
            .start()
    }

    /**
     * Define um listener para cliques na barra
     */
    fun setOnStatusClickListener(listener: OnClickListener) {
        binding.root.setOnClickListener(listener)
    }

    enum class Status {
        ONLINE,
        OFFLINE,
        SYNCING,
        SYNC_SUCCESS,
        SYNC_ERROR
    }
}
