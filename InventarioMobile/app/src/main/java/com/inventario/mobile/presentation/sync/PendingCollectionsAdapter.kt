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
    private val onDeleteClick: (Coleta) -> Unit,
    private val onRetryClick: ((Coleta) -> Unit)? = null  // v2.6: Callback para tentar novamente
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

                // Sync status chip - v2.6: Mostrar status baseado no erro
                val temErro = !coleta.erroSincronizacao.isNullOrBlank()
                if (temErro) {
                    chipSyncStatus.text = "Erro"
                    chipSyncStatus.setChipBackgroundColorResource(R.color.error_light)
                    chipSyncStatus.setChipStrokeColorResource(R.color.error)
                } else if (coleta.tentativasSincronizacao > 0) {
                    chipSyncStatus.text = "Tentando..."
                    chipSyncStatus.setChipBackgroundColorResource(R.color.warning_light)
                    chipSyncStatus.setChipStrokeColorResource(R.color.warning)
                } else {
                    chipSyncStatus.text = "Pendente"
                    chipSyncStatus.setChipBackgroundColorResource(R.color.warning_light)
                    chipSyncStatus.setChipStrokeColorResource(R.color.warning)
                }

                // v2.6: Mostrar erro de sincronização se existir
                if (temErro) {
                    layoutErro.visibility = android.view.View.VISIBLE
                    tvErroSincronizacao.text = coleta.erroSincronizacao
                    
                    // Botão retry
                    btnRetry.setOnClickListener {
                        onRetryClick?.invoke(coleta)
                    }
                } else {
                    layoutErro.visibility = android.view.View.GONE
                }

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
