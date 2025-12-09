package com.inventario.mobile.presentation.export

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.inventario.mobile.R
import com.inventario.mobile.domain.model.Sala

/**
 * Adapter para AutoCompleteTextView com filtro de busca de salas
 */
class SalaFilterAdapter(
    context: Context,
    private val salas: List<Sala>
) : ArrayAdapter<Sala>(context, R.layout.item_sala_dropdown, salas), Filterable {
    
    private var filteredSalas: List<Sala> = salas
    
    override fun getCount(): Int = filteredSalas.size
    
    override fun getItem(position: Int): Sala? {
        return if (position in filteredSalas.indices) {
            filteredSalas[position]
        } else {
            null
        }
    }
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_sala_dropdown, parent, false)
        
        val sala = getItem(position)
        val textView = view.findViewById<TextView>(R.id.tvSalaNome)
        textView.text = sala?.nome ?: ""
        
        return view
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim() ?: ""
                
                val filtered = if (query.isBlank()) {
                    salas
                } else {
                    salas.filter { sala ->
                        sala.nome.contains(query, ignoreCase = true)
                    }
                }
                
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }
            
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredSalas = (results?.values as? List<Sala>) ?: salas
                notifyDataSetChanged()
            }
            
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? Sala)?.nome ?: ""
            }
        }
    }
}
