package com.inventario.mobile.presentation.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Listener para scroll infinito em RecyclerView
 * 
 * Uso:
 * ```kotlin
 * recyclerView.addOnScrollListener(
 *     InfiniteScrollListener(layoutManager) {
 *         viewModel.carregarMais()
 *     }
 * )
 * ```
 */
class InfiniteScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val threshold: Int = 5,  // Carregar quando faltar X itens
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {
    
    private var isLoading = false
    
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        
        // Ignorar scroll para cima
        if (dy <= 0) return
        
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        
        // Carregar mais quando chegar perto do fim
        if (!isLoading && lastVisibleItem >= totalItemCount - threshold) {
            isLoading = true
            onLoadMore()
        }
    }
    
    /**
     * Chamar quando terminar de carregar
     */
    fun setLoaded() {
        isLoading = false
    }
    
    /**
     * Reset para nova busca
     */
    fun reset() {
        isLoading = false
    }
}
