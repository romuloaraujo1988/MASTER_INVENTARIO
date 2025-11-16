package com.inventario.mobile.presentation.consulta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.PatrimonioConsulta

/**
 * Adapter para lista de patrimônios na consulta
 * 
 * Usa ListAdapter com DiffUtil para performance otimizada
 */
class PatrimonioConsultaAdapter(
    private val onItemClick: (PatrimonioConsulta) -> Unit
) : ListAdapter<PatrimonioConsulta, PatrimonioConsultaAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patrimonio_consulta, parent, false)
        return ViewHolder(view, onItemClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder para item de patrimônio
     */
    class ViewHolder(
        itemView: View,
        private val onItemClick: (PatrimonioConsulta) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigo)
        private val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricao)
        private val tvLocalizacao: TextView = itemView.findViewById(R.id.tvLocalizacao)
        private val tvResponsavel: TextView = itemView.findViewById(R.id.tvResponsavel)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvValor: TextView = itemView.findViewById(R.id.tvValor)
        
        fun bind(patrimonio: PatrimonioConsulta) {
            tvCodigo.text = patrimonio.codigo
            tvDescricao.text = patrimonio.getDescricaoResumo()
            tvLocalizacao.text = patrimonio.getLocalizacaoCompleta()
            tvResponsavel.text = patrimonio.getResponsavelOuPadrao()
            tvStatus.text = patrimonio.getStatusColeta()
            tvValor.text = patrimonio.getValorFormatado()
            
            // Cor do status
            if (patrimonio.coletado) {
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            
            // Click listener
            itemView.setOnClickListener {
                onItemClick(patrimonio)
            }
        }
    }
    
    /**
     * DiffUtil callback para comparação eficiente de itens
     */
    private class DiffCallback : DiffUtil.ItemCallback<PatrimonioConsulta>() {
        override fun areItemsTheSame(
            oldItem: PatrimonioConsulta,
            newItem: PatrimonioConsulta
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: PatrimonioConsulta,
            newItem: PatrimonioConsulta
        ): Boolean {
            return oldItem == newItem
        }
    }
}
