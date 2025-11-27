package com.inventario.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemSalaBinding
import com.inventario.mobile.domain.model.Sala

/**
 * Adapter simples para lista de salas
 * Mantido para compatibilidade com código legado
 * 
 * v2.9: Suporte a long click para fixar sala
 * 
 * Para novas implementações, use SalaPagingAdapter
 */
class SalaAdapter(
    private val onSalaClick: (Sala) -> Unit,
    private val onSalaLongClick: ((Sala) -> Unit)? = null
) : ListAdapter<Sala, SalaAdapter.SalaViewHolder>(SALA_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalaViewHolder {
        val binding = ItemSalaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalaViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SalaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SalaViewHolder(
        private val binding: ItemSalaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(sala: Sala) {
            binding.apply {
                tvSalaNome.text = sala.nome
                
                // Mostrar descrição apenas se for diferente do nome e não estiver vazia
                val descricao = sala.descricao?.takeIf { 
                    it.isNotBlank() && it != sala.nome 
                }
                
                if (descricao != null) {
                    tvSalaDescricao.text = descricao
                    tvSalaDescricao.visibility = android.view.View.VISIBLE
                } else {
                    // Se não há descrição diferenciada, ocultar o TextView
                    tvSalaDescricao.visibility = android.view.View.GONE
                }
                
                // Click normal - selecionar sala
                root.setOnClickListener {
                    onSalaClick(sala)
                }
                
                // v2.9: Long click - mostrar menu de opções (fixar/desfixar)
                root.setOnLongClickListener {
                    onSalaLongClick?.invoke(sala)
                    true
                }
            }
        }
    }
    
    companion object {
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
