package com.inventario.mobile.presentation.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemPatrimonioSearchBinding
import com.inventario.mobile.domain.model.PatrimonioComColeta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para lista de patrimônios na busca rápida
 * Usa PatrimonioComColeta do domain model
 * 
 * Features:
 * - Click: navega para detalhes
 * - Long press: copia número do patrimônio
 * 
 * @see Requirements 2.1
 */
class PatrimonioSearchAdapter(
    private val onItemClick: (PatrimonioComColeta) -> Unit,
    private val onItemLongClick: ((PatrimonioComColeta) -> Unit)? = null
) : ListAdapter<PatrimonioComColeta, PatrimonioSearchAdapter.PatrimonioViewHolder>(PatrimonioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatrimonioViewHolder {
        val binding = ItemPatrimonioSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatrimonioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatrimonioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PatrimonioViewHolder(
        private val binding: ItemPatrimonioSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            // Long press para copiar número do patrimônio
            binding.root.setOnLongClickListener { view ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val patrimonio = getItem(position)
                    
                    // Copiar número para clipboard
                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Número Patrimônio", patrimonio.numero)
                    clipboard.setPrimaryClip(clip)
                    
                    // Feedback tátil
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    
                    // Toast de confirmação
                    Toast.makeText(
                        view.context,
                        "Número ${patrimonio.numero} copiado!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Callback opcional
                    onItemLongClick?.invoke(patrimonio)
                    
                    true
                } else {
                    false
                }
            }
        }

        fun bind(patrimonio: PatrimonioComColeta) {
            binding.apply {
                // Número do patrimônio
                textViewNumero.text = patrimonio.numero
                
                // Descrição
                textViewDescricao.text = patrimonio.descricao
                
                // Sala
                textViewSala.text = patrimonio.salaNome ?: "Sem sala"
                
                // Responsável
                textViewResponsavel.text = patrimonio.responsavelNome ?: "Sem responsável"
                textViewResponsavel.visibility = if (patrimonio.responsavelNome != null) View.VISIBLE else View.GONE
                
                // Status de coleta
                if (patrimonio.coletado) {
                    textViewStatus.text = "✅ Coletado"
                    textViewStatus.setTextColor(root.context.getColor(android.R.color.holo_green_dark))
                    
                    // Mostrar informações da coleta
                    layoutInfoColeta.visibility = View.VISIBLE
                    textViewColetadoPor.text = "Por: ${patrimonio.coletadoPor ?: "N/A"}"
                    textViewDataColeta.text = "Em: ${formatarData(patrimonio.dataColeta)}"
                } else {
                    textViewStatus.text = "⏳ Pendente"
                    textViewStatus.setTextColor(root.context.getColor(android.R.color.holo_orange_dark))
                    layoutInfoColeta.visibility = View.GONE
                }
                
                // Indicador de divergência
                if (patrimonio.temDivergencia) {
                    textViewDivergencia.visibility = View.VISIBLE
                    textViewDivergencia.text = "⚠️ Divergência"
                } else {
                    textViewDivergencia.visibility = View.GONE
                }
            }
        }
        
        private fun formatarData(timestamp: Long?): String {
            if (timestamp == null) return "N/A"
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            } catch (e: Exception) {
                "N/A"
            }
        }
    }

    class PatrimonioDiffCallback : DiffUtil.ItemCallback<PatrimonioComColeta>() {
        override fun areItemsTheSame(oldItem: PatrimonioComColeta, newItem: PatrimonioComColeta): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatrimonioComColeta, newItem: PatrimonioComColeta): Boolean {
            return oldItem == newItem
        }
    }
}
