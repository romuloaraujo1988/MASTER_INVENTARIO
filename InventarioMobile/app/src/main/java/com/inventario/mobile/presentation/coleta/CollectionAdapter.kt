package com.inventario.mobile.presentation.coleta

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ItemColetaBinding
import com.inventario.mobile.data.model.Coleta
import java.text.SimpleDateFormat
import java.util.*

class CollectionAdapter(
    private val onItemLongClick: ((Coleta) -> Unit)? = null
) : ListAdapter<Coleta, CollectionAdapter.CollectionViewHolder>(Companion.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemColetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CollectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val coleta = getItem(position)
        Log.d(TAG, "onBindViewHolder: position=$position, patrimonioId=${coleta.patrimonioId}, sincronizado=${coleta.sincronizado}")
        holder.bind(coleta)
        
        // Configurar long click listener
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(coleta)
            true
        }
    }

    override fun submitList(list: List<Coleta>?) {
        Log.d(TAG, "submitList: recebendo ${list?.size ?: 0} coletas")
        list?.forEachIndexed { index, coleta ->
            Log.d(TAG, "submitList: [$index] patrimonioId=${coleta.patrimonioId}, sincronizado=${coleta.sincronizado}, data=${coleta.dataColeta}")
        }
        super.submitList(list)
    }

    class CollectionViewHolder(
        private val binding: ItemColetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(coleta: Coleta) {
            Log.d("CollectionViewHolder", "=== BIND COLETA ===")
            Log.d("CollectionViewHolder", "ID: ${coleta.id}")
            Log.d("CollectionViewHolder", "patrimonioId: ${coleta.patrimonioId}")
            Log.d("CollectionViewHolder", "numeroPatrimonio: '${coleta.numeroPatrimonio}'")
            Log.d("CollectionViewHolder", "descricaoPatrimonio: '${coleta.descricaoPatrimonio}'")
            Log.d("CollectionViewHolder", "observacoes: '${coleta.observacoes}'")
            Log.d("CollectionViewHolder", "nomeSala (localização ORIGINAL): '${coleta.nomeSala}'")
            Log.d("CollectionViewHolder", "localizacaoEncontrada (onde FOI ENCONTRADO): '${coleta.localizacaoEncontrada}'")
            Log.d("CollectionViewHolder", "localizacaoAtual: '${coleta.localizacaoAtual}'")
            Log.d("CollectionViewHolder", "sincronizado: ${coleta.sincronizado}")
            Log.d("CollectionViewHolder", "==================")
            
            binding.apply {
                // Patrimonio information - garantir que sempre exiba algo útil
                val numeroExibido = when {
                    !coleta.numeroPatrimonio.isNullOrBlank() -> coleta.numeroPatrimonio
                    else -> "Patrimônio ${coleta.patrimonioId}"
                }
                
                val descricaoExibida = when {
                    !coleta.descricaoPatrimonio.isNullOrBlank() -> coleta.descricaoPatrimonio
                    !coleta.observacoes.isNullOrBlank() -> coleta.observacoes
                    else -> "Patrimônio coletado"
                }
                
                Log.d("CollectionViewHolder", "Número exibido: '$numeroExibido'")
                Log.d("CollectionViewHolder", "Descrição exibida: '$descricaoExibida'")
                
                tvPatrimonioNumber.text = numeroExibido
                tvPatrimonioDescricao.text = descricaoExibida
                
                // Collection date
                tvDataColeta.text = try {
                    val timestamp = coleta.dataColeta.toLongOrNull()
                    if (timestamp != null) {
                        dateFormat.format(Date(timestamp))
                    } else {
                        coleta.dataColeta
                    }
                } catch (e: Exception) {
                    coleta.dataColeta
                }
                
                // Location information - PRIORIZAR localizacaoEncontrada (onde o item FOI ENCONTRADO)
                // localizacaoEncontrada = onde o item foi encontrado durante a coleta
                // nomeSala = localização ORIGINAL do patrimônio no cadastro
                val salaExibida = when {
                    !coleta.localizacaoEncontrada.isNullOrBlank() -> coleta.localizacaoEncontrada
                    !coleta.nomeSala.isNullOrBlank() -> coleta.nomeSala
                    !coleta.localizacaoAtual.isNullOrBlank() -> coleta.localizacaoAtual
                    else -> "Local não informado"
                }
                Log.d("CollectionViewHolder", "Sala exibida (localizacaoEncontrada): '$salaExibida'")
                tvSalaInfo.text = salaExibida
                
                // Sync status
                if (coleta.sincronizado) {
                    chipSyncStatus.text = "Sincronizado"
                    chipSyncStatus.setChipBackgroundColorResource(R.color.success_light)
                    chipSyncStatus.setChipStrokeColorResource(R.color.success)
                    viewStatusIndicator.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.success)
                    )
                } else {
                    chipSyncStatus.text = "Pendente"
                    chipSyncStatus.setChipBackgroundColorResource(R.color.warning_light)
                    chipSyncStatus.setChipStrokeColorResource(R.color.warning)
                    viewStatusIndicator.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.warning)
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "CollectionAdapter"
        
        object DiffCallback : DiffUtil.ItemCallback<Coleta>() {
            override fun areItemsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
                return oldItem == newItem
            }
        }
    }
}
