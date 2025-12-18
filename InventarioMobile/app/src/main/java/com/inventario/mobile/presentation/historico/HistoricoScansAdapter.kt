package com.inventario.mobile.presentation.historico

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.HistoricoScan
import com.inventario.mobile.domain.model.TipoAcesso

/**
 * Adapter para lista de histórico de scans
 * 
 * @since v2.11.0
 */
class HistoricoScansAdapter(
    private val onItemClick: (HistoricoScan) -> Unit
) : ListAdapter<HistoricoScan, HistoricoScansAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico_scan, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumero: TextView = itemView.findViewById(R.id.tvNumeroPatrimonio)
        private val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricao)
        private val tvSala: TextView = itemView.findViewById(R.id.tvSala)
        private val tvTempo: TextView = itemView.findViewById(R.id.tvTempo)
        private val ivTipoAcesso: ImageView = itemView.findViewById(R.id.ivTipoAcesso)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)
        private val viewIndicador: View = itemView.findViewById(R.id.viewIndicador)
        
        fun bind(item: HistoricoScan) {
            tvNumero.text = item.numeroPatrimonio
            tvDescricao.text = item.descricao ?: "Sem descrição"
            tvSala.text = item.nomeSala ?: "Sala não informada"
            tvTempo.text = item.getTempoRelativo()
            
            // Ícone do tipo de acesso
            val iconeTipo = when (item.tipoAcesso) {
                TipoAcesso.SCAN_QR -> R.drawable.ic_qr_code
                TipoAcesso.BUSCA_MANUAL -> R.drawable.ic_search
                TipoAcesso.CONSULTA -> R.drawable.ic_info
            }
            ivTipoAcesso.setImageResource(iconeTipo)
            
            // Indicador de status (coletado ou não)
            val context = itemView.context
            if (item.foiColetado) {
                ivStatus.setImageResource(R.drawable.ic_check_circle)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.success))
                viewIndicador.setBackgroundColor(ContextCompat.getColor(context, R.color.success))
            } else if (item.jaEstaColetado) {
                // Já estava coletado antes
                ivStatus.setImageResource(R.drawable.ic_check)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.warning))
                viewIndicador.setBackgroundColor(ContextCompat.getColor(context, R.color.warning))
            } else {
                // Apenas consultado
                ivStatus.setImageResource(R.drawable.ic_visibility)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
                viewIndicador.setBackgroundColor(ContextCompat.getColor(context, R.color.divider))
            }
            
            // Click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<HistoricoScan>() {
        override fun areItemsTheSame(oldItem: HistoricoScan, newItem: HistoricoScan): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: HistoricoScan, newItem: HistoricoScan): Boolean {
            return oldItem == newItem
        }
    }
}
