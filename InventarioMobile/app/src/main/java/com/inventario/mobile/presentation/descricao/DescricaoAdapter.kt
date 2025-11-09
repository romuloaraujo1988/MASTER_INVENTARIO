package com.inventario.mobile.presentation.descricao

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemDescricaoBinding

/**
 * Adapter para lista de descrições (Clean Architecture)
 */
class DescricaoAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<String, DescricaoAdapter.DescricaoViewHolder>(DescricaoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescricaoViewHolder {
        val binding = ItemDescricaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DescricaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DescricaoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DescricaoViewHolder(
        private val binding: ItemDescricaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(descricao: String) {
            binding.tvDescricao.text = descricao
            binding.tvQuantidade.text = "" // Não temos quantidade no modelo Clean
            
            binding.root.setOnClickListener {
                onItemClick(descricao)
            }
        }
    }
}

class DescricaoDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
