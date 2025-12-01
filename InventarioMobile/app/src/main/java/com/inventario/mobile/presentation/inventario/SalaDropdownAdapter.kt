package com.inventario.mobile.presentation.inventario

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.SalaComProgresso

/**
 * Adapter customizado para dropdown de salas com progresso.
 * Exibe nome da sala, progresso e ícones de status.
 */
class SalaDropdownAdapter(
    context: Context,
    private val salas: MutableList<SalaComProgresso> = mutableListOf()
) : ArrayAdapter<SalaComProgresso>(context, R.layout.item_sala_dropdown, salas) {
    
    private var filteredSalas: List<SalaComProgresso> = salas
    
    override fun getCount(): Int = filteredSalas.size
    
    override fun getItem(position: Int): SalaComProgresso? {
        return if (position < filteredSalas.size) filteredSalas[position] else null
    }
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }
    
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }
    
    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_sala_dropdown, parent, false)
        
        val sala = getItem(position) ?: return view
        
        val tvNome = view.findViewById<TextView>(R.id.tvSalaNome)
        val tvProgresso = view.findViewById<TextView>(R.id.tvProgresso)
        val ivStatus = view.findViewById<ImageView>(R.id.ivStatus)
        
        // Nome da sala
        tvNome.text = sala.nomeCompleto
        
        // Progresso
        tvProgresso.text = sala.progressoTexto
        
        // Cor do progresso baseada no percentual
        val progressColor = getProgressColor(sala.percentualColeta)
        tvProgresso.setTextColor(ContextCompat.getColor(context, progressColor))
        
        // Ícone de status
        when {
            sala.isCompleta -> {
                ivStatus.setImageResource(R.drawable.ic_check_circle)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.success))
                ivStatus.visibility = View.VISIBLE
            }
            sala.isVazia -> {
                ivStatus.setImageResource(R.drawable.ic_pending)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
                ivStatus.visibility = View.VISIBLE
            }
            sala.percentualColeta == 0f -> {
                ivStatus.setImageResource(R.drawable.ic_warning)
                ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.warning))
                ivStatus.visibility = View.VISIBLE
            }
            else -> {
                ivStatus.visibility = View.GONE
            }
        }
        
        return view
    }
    
    private fun getProgressColor(percentual: Float): Int {
        return when {
            percentual <= 25f -> R.color.error
            percentual <= 50f -> R.color.warning
            percentual <= 75f -> R.color.warning_light
            else -> R.color.success
        }
    }
    
    /**
     * Atualiza a lista de salas.
     */
    fun updateSalas(newSalas: List<SalaComProgresso>) {
        salas.clear()
        salas.addAll(newSalas)
        filteredSalas = newSalas
        notifyDataSetChanged()
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                
                val filtered = if (query.isEmpty()) {
                    salas
                } else {
                    salas.filter { sala ->
                        sala.nome.lowercase().contains(query) ||
                        sala.numero?.lowercase()?.contains(query) == true ||
                        sala.id.toString().contains(query)
                    }
                }
                
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }
            
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredSalas = (results?.values as? List<SalaComProgresso>) ?: salas
                notifyDataSetChanged()
            }
            
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? SalaComProgresso)?.let { sala ->
                    "${sala.nome} - ${sala.progressoTexto}"
                } ?: ""
            }
        }
    }
}
