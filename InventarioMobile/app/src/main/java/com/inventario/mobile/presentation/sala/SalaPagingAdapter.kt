package com.inventario.mobile.presentation.sala

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemSalaBinding
import com.inventario.mobile.domain.model.Sala

/**
 * Adapter para RecyclerView com paginação
 * Usa PagingDataAdapter para carregamento eficiente
 */
class SalaPagingAdapter(
    private val onSalaClick: (Sala) -> Unit
) : PagingDataAdapter<Sala, SalaPagingAdapter.SalaViewHolder>(SALA_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaViewHolder {
        val binding = ItemSalaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalaViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SalaViewHolder, position: Int) {
        val sala = getItem(position)
        sala?.let { holder.bind(it) }
    }
    
    inner class SalaViewHolder(
        private val binding: ItemSalaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(sala: Sala) {
            binding.apply {
                tvSalaNome.text = sala.nome
                tvSalaDescricao.text = sala.descricao ?: "Sem descrição"
                
                root.setOnClickListener {
                    onSalaClick(sala)
                }
            }
        }
    }
    
    companion object {
        /**
         * DiffUtil para comparação eficiente de itens
         * Atualiza apenas itens que mudaram
         */
        private val SALA_COMPARATOR = object : DiffUtil.ItemCallback<Sala>() {
            override fun areItemsTheSame(oldItem: Sala, newItem: Sala): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: Sala, newItem: Sala): Boolean {
                return oldItem == newItem
            }
        }
    }
}
