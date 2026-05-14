package com.inventario.mobile.presentation.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemTopColetorBinding
import com.inventario.mobile.domain.model.TopColetor
import java.util.concurrent.TimeUnit

/**
 * Adapter da lista "Top Coletores" na aba Rankings.
 *
 * Renderiza posição, nome, total de coletas, produtividade média
 * (coletas/dia) e tempo desde a última coleta ("há X horas").
 *
 * O cálculo de "há X horas/dias" é feito localmente a partir do
 * `ultimaColetaMillis`, evitando depender da string formatada do
 * servidor (que não tem essa noção relativa).
 */
class TopColetorAdapter :
    ListAdapter<TopColetor, TopColetorAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTopColetorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class VH(private val binding: ItemTopColetorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coletor: TopColetor, posicao: Int) {
            binding.tvPosicao.text = posicao.toString()
            binding.tvNome.text = coletor.nome
            binding.tvTotalColetas.text = coletor.totalColetas.toString()

            val partes = mutableListOf<String>()
            partes.add("${coletor.totalColetas} coletas")
            coletor.produtividadePorDia?.let { prod ->
                partes.add(String.format("%.1f/dia", prod).replace(".", ","))
            }
            coletor.ultimaColetaMillis?.let { ts ->
                partes.add("última: ${formatarTempoRelativo(ts)}")
            }
            binding.tvDetalhe.text = partes.joinToString(" · ")
        }

        /** Converte timestamp em ms para "há X minutos/horas/dias" relativo a agora. */
        private fun formatarTempoRelativo(timestampMillis: Long): String {
            val diff = System.currentTimeMillis() - timestampMillis
            if (diff < 0) return "agora"
            val minutos = TimeUnit.MILLISECONDS.toMinutes(diff)
            val horas = TimeUnit.MILLISECONDS.toHours(diff)
            val dias = TimeUnit.MILLISECONDS.toDays(diff)
            return when {
                minutos < 1 -> "agora"
                minutos < 60 -> "há ${minutos}min"
                horas < 24 -> "há ${horas}h"
                dias < 30 -> "há ${dias}d"
                else -> "há ${dias / 30} meses"
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TopColetor>() {
            override fun areItemsTheSame(oldItem: TopColetor, newItem: TopColetor) =
                oldItem.idUsuario == newItem.idUsuario

            override fun areContentsTheSame(oldItem: TopColetor, newItem: TopColetor) =
                oldItem == newItem
        }
    }
}
