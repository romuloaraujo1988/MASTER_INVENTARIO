package com.inventario.mobile.presentation.coletas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.databinding.ItemColetaBinding
import com.inventario.mobile.databinding.ItemColetaHeaderBinding
import com.inventario.mobile.domain.usecase.ColetaListItem

/**
 * Adapter para lista de coletas com suporte a headers de grupo
 * Usa DiffUtil para atualizações eficientes
 * 
 * @see Requirements 7.4, 7.5, 10.1, 10.3, 10.4
 */
class ColetasAdapter(
    private val onColetaClick: (Coleta) -> Unit = {}
) : ListAdapter<ColetaListItem, RecyclerView.ViewHolder>(ColetaListItemDiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ColetaListItem.Header -> VIEW_TYPE_HEADER
            is ColetaListItem.Item -> VIEW_TYPE_ITEM
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemColetaHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = ItemColetaBinding.inflate(inflater, parent, false)
                ColetaViewHolder(binding, onColetaClick)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ColetaListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ColetaListItem.Item -> (holder as ColetaViewHolder).bind(item.coleta)
        }
    }
    
    /**
     * Submete lista simples de coletas (sem headers)
     */
    fun submitColetaList(coletas: List<Coleta>) {
        val items = coletas.map { ColetaListItem.Item(it) }
        submitList(items)
    }
    
    /**
     * Submete lista agrupada com headers
     */
    fun submitGroupedList(coletasAgrupadas: Map<String, List<Coleta>>) {
        val items = mutableListOf<ColetaListItem>()
        coletasAgrupadas.forEach { (sala, coletas) ->
            items.add(ColetaListItem.Header(sala, coletas.size))
            coletas.forEach { coleta ->
                items.add(ColetaListItem.Item(coleta))
            }
        }
        submitList(items)
    }
    
    /**
     * ViewHolder para header de grupo
     */
    class HeaderViewHolder(
        private val binding: ItemColetaHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(header: ColetaListItem.Header) {
            binding.tvSalaNome.text = header.nomeSala
            binding.tvQuantidade.text = "${header.quantidade} ${if (header.quantidade == 1) "item" else "itens"}"
        }
    }
    
    /**
     * ViewHolder para item de coleta
     */
    class ColetaViewHolder(
        private val binding: ItemColetaBinding,
        private val onColetaClick: (Coleta) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(coleta: Coleta) {
            val context = binding.root.context
            
            // Número do patrimônio - Para itens sem etiqueta, mostrar categoria ou "SEM ETIQUETA"
            val numeroExibicao = if (coleta.semEtiqueta) {
                coleta.categoriaItemSemEtiqueta ?: "SEM ETIQUETA"
            } else {
                coleta.numeroPatrimonio ?: "N/A"
            }
            binding.tvPatrimonioNumber.text = numeroExibicao
            
            // Descrição - Para itens sem etiqueta, usar descricaoItemSemEtiqueta
            val descricaoExibicao = if (coleta.semEtiqueta) {
                coleta.descricaoItemSemEtiqueta ?: coleta.descricaoPatrimonio ?: "Item sem etiqueta"
            } else {
                coleta.descricaoPatrimonio ?: "Sem descrição"
            }
            binding.tvPatrimonioDescricao.text = descricaoExibicao
            
            // Data da coleta
            binding.tvDataColeta.text = formatarData(coleta.dataColeta)
            
            // Localização/Sala - PRIORIZAR localizacaoEncontrada (onde o item FOI ENCONTRADO)
            // localizacaoEncontrada = onde o item foi encontrado durante a coleta
            // nomeSala = localização ORIGINAL do patrimônio no cadastro
            val localizacao = coleta.localizacaoEncontrada ?: coleta.nomeSala ?: coleta.localizacaoAtual ?: "Não informada"
            binding.tvSalaInfo.text = localizacao
            
            // Status de sincronização (Requirements 9.1, 9.2, 9.3, 9.4)
            if (coleta.sincronizado) {
                binding.chipSyncStatus.text = "Sincronizado"
                binding.chipSyncStatus.setChipBackgroundColorResource(R.color.success_light)
                binding.chipSyncStatus.setChipStrokeColorResource(R.color.success)
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.success)
                )
            } else {
                binding.chipSyncStatus.text = "Pendente"
                binding.chipSyncStatus.setChipBackgroundColorResource(R.color.warning_light)
                binding.chipSyncStatus.setChipStrokeColorResource(R.color.warning)
                binding.viewStatusIndicator.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.warning)
                )
            }
            
            // Click listener
            binding.root.setOnClickListener {
                onColetaClick(coleta)
            }
        }
        
        private fun formatarData(dataColeta: String?): String {
            if (dataColeta.isNullOrBlank()) return "Data não informada"
            
            return try {
                // Se já está formatado, retorna como está
                if (dataColeta.contains("/")) {
                    dataColeta
                } else {
                    // Tenta converter timestamp
                    val timestamp = dataColeta.toLongOrNull()
                    if (timestamp != null) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestamp))
                    } else {
                        dataColeta
                    }
                }
            } catch (e: Exception) {
                dataColeta
            }
        }
    }
}

/**
 * DiffUtil callback para ColetaListItem
 * Permite atualizações eficientes da lista
 */
class ColetaListItemDiffCallback : DiffUtil.ItemCallback<ColetaListItem>() {
    
    override fun areItemsTheSame(oldItem: ColetaListItem, newItem: ColetaListItem): Boolean {
        return when {
            oldItem is ColetaListItem.Header && newItem is ColetaListItem.Header -> {
                oldItem.nomeSala == newItem.nomeSala
            }
            oldItem is ColetaListItem.Item && newItem is ColetaListItem.Item -> {
                oldItem.coleta.id == newItem.coleta.id
            }
            else -> false
        }
    }
    
    override fun areContentsTheSame(oldItem: ColetaListItem, newItem: ColetaListItem): Boolean {
        return oldItem == newItem
    }
}
