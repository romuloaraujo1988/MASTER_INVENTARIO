package com.inventario.mobile.presentation.descricao

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemDescricaoBinding
import com.inventario.mobile.domain.model.DescricaoItem

/**
 * Adapter para lista de descrições (Clean Architecture)
 * Exibe descrições com ícone, categoria e quantidade de itens pendentes
 */
class DescricaoAdapter(
    private val onItemClick: (DescricaoItem) -> Unit
) : ListAdapter<DescricaoItem, DescricaoAdapter.DescricaoViewHolder>(DescricaoDiffCallback()) {

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

        fun bind(item: DescricaoItem) {
            binding.apply {
                tvDescricao.text = item.descricao
                tvIcone.text = item.icone
                tvCategoria.text = "Categoria: ${item.categoria}"
                tvQuantidade.text = item.quantidade.toString()
            }
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

class DescricaoDiffCallback : DiffUtil.ItemCallback<DescricaoItem>() {
    override fun areItemsTheSame(oldItem: DescricaoItem, newItem: DescricaoItem): Boolean {
        return oldItem.descricao == newItem.descricao
    }

    override fun areContentsTheSame(oldItem: DescricaoItem, newItem: DescricaoItem): Boolean {
        return oldItem == newItem
    }
}
