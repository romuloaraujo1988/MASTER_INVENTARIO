package com.inventario.mobile.presentation.inventario

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
                
                // Marca e Modelo
                textViewMarca.text = patrimonio.marca ?: "Marca: N/A"
                textViewModelo.text = patrimonio.modelo ?: "Modelo: N/A"
                
                // Responsável
                if (!patrimonio.responsavelNome.isNullOrBlank()) {
                    textViewResponsavel.text = "Responsável: ${patrimonio.responsavelNome}"
                    layoutResponsavel.visibility = android.view.View.VISIBLE
                } else {
                    layoutResponsavel.visibility = android.view.View.GONE
                }
                
                // Localização
                textViewSetor.text = patrimonio.setorNome ?: "Setor: N/A"
                textViewSala.text = patrimonio.salaNome ?: "Sala: N/A"
                
                // Indicador de status de coleta
                val isColetado = patrimonio.coletado
                textViewStatus.text = if (isColetado) "Coletado" else "Pendente"
                textViewStatus.setTextColor(
                    if (isColetado) 
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    else 
                        itemView.context.getColor(android.R.color.holo_orange_dark)
                )

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
