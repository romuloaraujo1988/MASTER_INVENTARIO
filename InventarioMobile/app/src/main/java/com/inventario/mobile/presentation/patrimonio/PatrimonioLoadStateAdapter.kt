package com.inventario.mobile.presentation.patrimonio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.R

/**
 * Adapter para exibir estados de carregamento (loading, erro, etc)
 * no rodapé da lista durante a paginação
 */
class PatrimonioLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<PatrimonioLoadStateAdapter.LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state, parent, false)
        return LoadStateViewHolder(view, retry)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    class LoadStateViewHolder(
        itemView: View,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvError: TextView = itemView.findViewById(R.id.tvError)
        private val btnRetry: Button = itemView.findViewById(R.id.btnRetry)
        
        init {
            btnRetry.setOnClickListener { retry() }
        }
        
        fun bind(loadState: LoadState) {
            progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
            tvError.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            btnRetry.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            
            if (loadState is LoadState.Error) {
                tvError.text = loadState.error.localizedMessage ?: "Erro ao carregar"
            }
        }
    }
}
