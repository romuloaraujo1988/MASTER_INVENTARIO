package com.inventario.mobile.presentation.sync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ItemPendingColetaBinding
import com.inventario.mobile.data.model.Coleta
import java.text.SimpleDateFormat
import java.util.*

class PendingCollectionsAdapter(
    private val onDeleteClick: (Coleta) -> Unit
) : ListAdapter<Coleta, PendingCollectionsAdapter.PendingColetaViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingColetaViewHolder {
        val binding = ItemPendingColetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PendingColetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingColetaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PendingColetaViewHolder(
        private val binding: ItemPendingColetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coleta: Coleta) {
            with(binding) {
                // Patrimonio number
                tvPatrimonioNumber.text = coleta.numeroPatrimonio ?: coleta.patrimonioId.toString()

                // Collection date
                val formattedDate = try {
                    val timestamp = coleta.dataColeta.toLongOrNull() ?: System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                } catch (e: Exception) {
                    "Data não disponível"
                }
                tvDataColeta.text = "Coletado em: $formattedDate"

                // Patrimonio description
                tvPatrimonioDescricao.text = coleta.descricaoPatrimonio ?: "Descrição não disponível"

                // Location info - priorizar localizacaoAtual (que contém localizacaoEncontrada)
                val salaInfo = coleta.localizacaoAtual ?: coleta.nomeSala ?: "Local não informado"
                tvSalaInfo.text = salaInfo

                // User info
                tvUserInfo.text = "Coletado por: ${coleta.nomeColetor ?: coleta.usuarioId.toString()}"

                // Sync status chip
                chipSyncStatus.text = "Pendente"

                // Delete button click
                btnDeleteColeta.setOnClickListener {
                    onDeleteClick(coleta)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Coleta>() {
        override fun areItemsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
            return oldItem.patrimonioId == newItem.patrimonioId
        }

        override fun areContentsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
            return oldItem == newItem
        }
    }
}