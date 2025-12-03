package com.inventario.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemPatrimonioBinding
import com.inventario.mobile.data.model.Patrimonio

class PatrimonioAdapter(
    private val onItemClick: (Patrimonio) -> Unit
) : ListAdapter<Patrimonio, PatrimonioAdapter.PatrimonioViewHolder>(PatrimonioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatrimonioViewHolder {
        val binding = ItemPatrimonioBinding.inflate(
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
        private val binding: ItemPatrimonioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patrimonio: Patrimonio) {
            binding.apply {
                textViewNumero.text = patrimonio.numeroPatrimonio
                textViewDescricao.text = patrimonio.descricao
                // Campos ausentes no modelo atual
                textViewMarca.text = patrimonio.marca ?: "-"
                textViewModelo.text = patrimonio.modelo ?: "-"
                
                // Responsável
                if (!patrimonio.responsavelNome.isNullOrBlank()) {
                    textViewResponsavel.text = "Responsável: ${patrimonio.responsavelNome}"
                    layoutResponsavel.visibility = android.view.View.VISIBLE
                } else {
                    layoutResponsavel.visibility = android.view.View.GONE
                }
                
                // Localização
                textViewSetor.text = "Setor: ${patrimonio.setorNome ?: patrimonio.setorId?.toString() ?: "-"}"
                textViewSala.text = "Sala: ${patrimonio.salaNome ?: patrimonio.salaId?.toString() ?: "-"}"
                
                // Status baseado no campo de coleta local
                val statusText = if (patrimonio.coletado) "Coletado" else "Não coletado"
                val statusColor = if (patrimonio.coletado) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                textViewStatus.setTextColor(itemView.context.getColor(statusColor))
                textViewStatus.text = statusText
                
                // ✅ NOVO: Mostrar informações da coleta quando já foi coletado (igual ao scanner)
                if (patrimonio.coletado) {
                    layoutInfoColeta.visibility = android.view.View.VISIBLE
                    
                    // Coletado por
                    textViewColetadoPor.text = if (!patrimonio.coletadoPor.isNullOrBlank()) {
                        "Coletado por: ${patrimonio.coletadoPor}"
                    } else {
                        "Coletado por: Não informado"
                    }
                    
                    // Data da coleta
                    textViewDataColeta.text = if (!patrimonio.dataColetaFormatada.isNullOrBlank()) {
                        "Data: ${patrimonio.dataColetaFormatada}"
                    } else if (!patrimonio.dataColeta.isNullOrBlank()) {
                        "Data: ${patrimonio.dataColeta}"
                    } else {
                        "Data: Não informada"
                    }
                    
                    // Localização encontrada
                    textViewLocalizacaoEncontrada.text = if (!patrimonio.localizacaoEncontrada.isNullOrBlank()) {
                        "Local encontrado: ${patrimonio.localizacaoEncontrada}"
                    } else {
                        "Local encontrado: Não informado"
                    }
                    
                    // Estado encontrado
                    textViewEstadoEncontrado.text = if (!patrimonio.estadoEncontrado.isNullOrBlank()) {
                        "Estado: ${patrimonio.estadoEncontrado}"
                    } else {
                        "Estado: Não informado"
                    }
                } else {
                    layoutInfoColeta.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onItemClick(patrimonio)
                }
            }
        }
    }
}

class PatrimonioDiffCallback : DiffUtil.ItemCallback<Patrimonio>() {
    override fun areItemsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
        return oldItem == newItem
    }
}
