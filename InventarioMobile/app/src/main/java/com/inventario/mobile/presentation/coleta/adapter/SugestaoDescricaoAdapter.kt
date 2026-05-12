package com.inventario.mobile.presentation.coleta.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemSugestaoDescricaoBinding
import com.inventario.mobile.domain.model.SugestaoDescricao

/**
 * Adapter da lista de sugestões de descrição exibida no autocomplete da
 * tela de coleta de item sem etiqueta ([com.inventario.mobile.presentation.coleta.ItemSemEtiquetaActivity]).
 *
 * Cada item é um [SugestaoDescricao] pertencente ao inventário ativo e
 * ainda não coletado. Ao tocar em um item, [onItemClick] é invocado — a
 * Activity delega ao ViewModel (`onSugestaoSelecionada(item)`) que, por sua
 * vez, preenche o campo de descrição livre com o texto da sugestão e
 * mantém o campo editável (Requirement 3.5).
 *
 * Usa [ListAdapter] + [DiffUtil] para atualizações incrementais
 * eficientes conforme o termo de busca é refinado (Requirement 3.3, 3.7).
 * O critério de identidade é o [SugestaoDescricao.idPatrimonio], pois uma
 * mesma descrição pode aparecer múltiplas vezes ligada a patrimônios
 * distintos (Requirement 8.4).
 *
 * @property onItemClick Callback invocado ao tocar em uma sugestão.
 */
class SugestaoDescricaoAdapter(
    private val onItemClick: (SugestaoDescricao) -> Unit
) : ListAdapter<SugestaoDescricao, SugestaoDescricaoAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSugestaoDescricaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSugestaoDescricaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sugestao: SugestaoDescricao) {
            binding.tvDescricao.text = sugestao.descricao
            binding.root.setOnClickListener { onItemClick(sugestao) }
        }
    }

    companion object {
        @VisibleForTesting
        internal val DIFF = object : DiffUtil.ItemCallback<SugestaoDescricao>() {
            override fun areItemsTheSame(
                oldItem: SugestaoDescricao,
                newItem: SugestaoDescricao
            ): Boolean = oldItem.idPatrimonio == newItem.idPatrimonio

            override fun areContentsTheSame(
                oldItem: SugestaoDescricao,
                newItem: SugestaoDescricao
            ): Boolean = oldItem == newItem
        }
    }
}
