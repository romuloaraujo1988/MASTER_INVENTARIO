package com.inventario.mobile.presentation.sala

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemLoadStateBinding

/**
 * Adapter para exibir loading/erro no footer da lista
 * Usado com PagingDataAdapter
 */
class SalaLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<SalaLoadStateAdapter.LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    inner class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loadState: LoadState) {
            binding.apply {
                // Exibir loading
                progressBar.isVisible = loadState is LoadState.Loading
                
                // Exibir erro
                btnRetry.isVisible = loadState is LoadState.Error
                tvError.isVisible = loadState is LoadState.Error
                
                if (loadState is LoadState.Error) {
                    tvError.text = loadState.error.localizedMessage ?: "Erro ao carregar"
                }
                
                // Exibir mensagem de fim
                tvEndOfList.isVisible = loadState is LoadState.NotLoading && loadState.endOfPaginationReached
                
                // Botão retry
                btnRetry.setOnClickListener {
                    retry()
                }
            }
        }
    }
}
