package com.inventario.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.domain.model.Sala
import com.inventario.mobile.databinding.ItemSalaBinding

class SalaAdapter(
    private val onSalaClick: (Sala) -> Unit
) : ListAdapter<Sala, SalaAdapter.SalaViewHolder>(SalaDiffCallback()) {

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
                textViewSalaNome.text = sala.nome
                textViewSalaDescricao.text = sala.descricao ?: "Sem descrição"
                
                // Mostrar código da sala
                textViewSalaAndar.text = "Código: ${sala.codigo}"
                textViewSalaAndar.visibility = android.view.View.VISIBLE
                
                // Ocultar campo bloco já que não existe na entidade do domínio
                textViewSalaBloco.visibility = android.view.View.GONE

                // Click listener
                root.setOnClickListener {
                    onSalaClick(sala)
                }
            }
        }
    }

    private class SalaDiffCallback : DiffUtil.ItemCallback<Sala>() {
        override fun areItemsTheSame(oldItem: Sala, newItem: Sala): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Sala, newItem: Sala): Boolean {
            return oldItem == newItem
        }
    }
}