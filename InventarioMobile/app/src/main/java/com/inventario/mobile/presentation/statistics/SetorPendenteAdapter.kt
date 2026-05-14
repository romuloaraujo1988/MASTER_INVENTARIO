package com.inventario.mobile.presentation.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemSetorPendenteBinding
import com.inventario.mobile.domain.model.SetorPendente

/**
 * Adapter da lista "Setores com Mais Pendentes" na aba Rankings.
 *
 * Renderiza nome do setor, número de pendentes (destacado), total/coletados
 * e barra de progresso da conclusão. A ordem é definida pelo servidor
 * (mais pendentes primeiro), por isso o adapter não reordena.
 */
class SetorPendenteAdapter :
    ListAdapter<SetorPendente, SetorPendenteAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSetorPendenteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemSetorPendenteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(setor: SetorPendente) {
            binding.tvNome.text = setor.nome
            binding.tvPendentes.text = setor.totalPendentes.toString()

            val percentual = setor.percentualConcluido
            binding.tvDetalhe.text = String.format(
                "%d / %d coletados · %.1f%%",
                setor.totalColetados,
                setor.totalPatrimonios,
                percentual
            ).replace(".", ",")
            // Limita 0..100 para evitar bug visual em casos extremos
            binding.progressConclusao.progress = percentual.coerceIn(0.0, 100.0).toInt()
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SetorPendente>() {
            override fun areItemsTheSame(oldItem: SetorPendente, newItem: SetorPendente) =
                oldItem.idSetor == newItem.idSetor

            override fun areContentsTheSame(oldItem: SetorPendente, newItem: SetorPendente) =
                oldItem == newItem
        }
    }
}
