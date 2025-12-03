package com.inventario.mobile.presentation.patrimonio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.Patrimonio

/**
 * Adapter com suporte a Paging 3 para lista de patrimônios
 * 
 * Usa PagingDataAdapter que automaticamente:
 * - Carrega mais dados quando necessário
 * - Gerencia placeholders
 * - Atualiza a lista de forma eficiente
 */
class PatrimonioPagingAdapter(
    private val onItemClick: (Patrimonio) -> Unit
) : PagingDataAdapter<Patrimonio, PatrimonioPagingAdapter.PatrimonioViewHolder>(DIFF_CALLBACK) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatrimonioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patrimonio, parent, false)
        return PatrimonioViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PatrimonioViewHolder, position: Int) {
        val patrimonio = getItem(position)
        if (patrimonio != null) {
            holder.bind(patrimonio, onItemClick)
        }
    }
    
    class PatrimonioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Views do layout item_patrimonio.xml
        private val tvNumero: TextView = itemView.findViewById(R.id.textViewNumero)
        private val tvDescricao: TextView = itemView.findViewById(R.id.textViewDescricao)
        private val tvStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        private val tvMarca: TextView = itemView.findViewById(R.id.textViewMarca)
        private val tvModelo: TextView = itemView.findViewById(R.id.textViewModelo)
        private val tvResponsavel: TextView = itemView.findViewById(R.id.textViewResponsavel)
        private val tvSetor: TextView = itemView.findViewById(R.id.textViewSetor)
        private val tvSala: TextView = itemView.findViewById(R.id.textViewSala)
        private val layoutInfoColeta: LinearLayout = itemView.findViewById(R.id.layoutInfoColeta)
        private val tvColetadoPor: TextView = itemView.findViewById(R.id.textViewColetadoPor)
        private val tvDataColeta: TextView = itemView.findViewById(R.id.textViewDataColeta)
        
        fun bind(patrimonio: Patrimonio, onItemClick: (Patrimonio) -> Unit) {
            tvNumero.text = patrimonio.numeroPatrimonio
            tvDescricao.text = patrimonio.descricao ?: "Sem descrição"
            tvMarca.text = patrimonio.marca ?: "-"
            tvModelo.text = patrimonio.modelo ?: "-"
            tvResponsavel.text = "Responsável: ${patrimonio.nomeResponsavel ?: "Não definido"}"
            tvSetor.text = "Setor: -" // TODO: Adicionar setor ao modelo
            tvSala.text = "Sala: ${patrimonio.nomeSala ?: "Não definida"}"
            
            // Status de coleta
            if (patrimonio.coletado) {
                tvStatus.text = "COLETADO"
                tvStatus.setTextColor(itemView.context.getColor(R.color.success))
                tvStatus.setBackgroundColor(itemView.context.getColor(R.color.status_background_success))
                
                // Mostrar informações da coleta
                layoutInfoColeta.isVisible = true
                tvColetadoPor.text = "Coletado por: ${patrimonio.coletadoPor ?: "-"}"
                tvDataColeta.text = "Data: ${patrimonio.dataColeta ?: "-"}"
            } else {
                tvStatus.text = "PENDENTE"
                tvStatus.setTextColor(itemView.context.getColor(R.color.warning))
                tvStatus.setBackgroundColor(itemView.context.getColor(R.color.status_background_warning))
                layoutInfoColeta.isVisible = false
            }
            
            itemView.setOnClickListener { onItemClick(patrimonio) }
        }
    }
    
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Patrimonio>() {
            override fun areItemsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
                return oldItem == newItem
            }
        }
    }
}
