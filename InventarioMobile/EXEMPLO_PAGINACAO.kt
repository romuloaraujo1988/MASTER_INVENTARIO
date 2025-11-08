/**
 * EXEMPLO DE IMPLEMENTAÇÃO DE PAGINAÇÃO (Paging 3)
 * 
 * Este arquivo contém exemplos de como implementar paginação
 * em diferentes camadas da aplicação.
 * 
 * NÃO COMPILAR - Apenas referência
 */

// ===== 1. REPOSITORY =====

package com.inventario.mobile.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.inventario.mobile.data.local.dao.ColetaDao
import com.inventario.mobile.data.local.entity.ColetaEntity
import com.inventario.mobile.domain.model.Coleta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColetaRepository(
    private val coletaDao: ColetaDao
) {
    
    /**
     * Retorna todas as coletas paginadas
     */
    fun getAllColetasPaged(): Flow<PagingData<Coleta>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,              // Carregar 50 itens por vez
                enablePlaceholders = false, // Não mostrar placeholders
                prefetchDistance = 10       // Carregar próxima página quando faltar 10 itens
            ),
            pagingSourceFactory = { coletaDao.getAllColetasPaged() }
        ).flow.map { pagingData ->
            // Converter Entity para Domain Model
            pagingData.map { it.toDomain() }
        }
    }
    
    /**
     * Retorna coletas de um usuário específico, paginadas
     */
    fun getColetasByUsuarioPaged(usuarioId: Long): Flow<PagingData<Coleta>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { coletaDao.getColetasByUsuarioPaged(usuarioId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
    
    /**
     * Retorna coletas pendentes de sincronização, paginadas
     */
    fun getColetasPendentesPaged(): Flow<PagingData<Coleta>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { coletaDao.getColetasPendentesPaged() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}

// Função de extensão para converter Entity em Domain Model
private fun ColetaEntity.toDomain(): Coleta {
    return Coleta(
        id = this.id,
        patrimonioId = this.patrimonioId,
        usuarioId = this.usuarioId,
        dataColeta = this.dataColeta,
        localizacaoAtual = this.localizacaoAtual,
        observacoes = this.observacoes,
        status = this.status,
        sincronizado = this.sincronizado
    )
}


// ===== 2. VIEWMODEL =====

package com.inventario.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.inventario.mobile.data.repository.ColetaRepository
import com.inventario.mobile.domain.model.Coleta
import kotlinx.coroutines.flow.Flow

class ColetasListViewModel(
    private val repository: ColetaRepository
) : ViewModel() {
    
    /**
     * Flow de coletas paginadas
     * 
     * cachedIn(viewModelScope) mantém os dados em cache
     * mesmo quando a tela é recriada (rotação, etc)
     */
    val coletas: Flow<PagingData<Coleta>> = 
        repository.getAllColetasPaged()
            .cachedIn(viewModelScope)
    
    /**
     * Flow de coletas pendentes paginadas
     */
    val coletasPendentes: Flow<PagingData<Coleta>> =
        repository.getColetasPendentesPaged()
            .cachedIn(viewModelScope)
}


// ===== 3. ADAPTER (RecyclerView) =====

package com.inventario.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemColetaBinding
import com.inventario.mobile.domain.model.Coleta

/**
 * Adapter paginado para lista de coletas
 * 
 * Usa PagingDataAdapter ao invés de RecyclerView.Adapter
 */
class ColetasPagingAdapter(
    private val onItemClick: (Coleta) -> Unit
) : PagingDataAdapter<Coleta, ColetasPagingAdapter.ColetaViewHolder>(ColetaDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColetaViewHolder {
        val binding = ItemColetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColetaViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ColetaViewHolder, position: Int) {
        // getItem() pode retornar null durante o carregamento
        val coleta = getItem(position)
        if (coleta != null) {
            holder.bind(coleta)
        }
    }
    
    inner class ColetaViewHolder(
        private val binding: ItemColetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(coleta: Coleta) {
            binding.apply {
                textPatrimonioId.text = "Patrimônio: ${coleta.patrimonioId}"
                textDataColeta.text = formatDate(coleta.dataColeta)
                textStatus.text = coleta.status
                
                // Indicador de sincronização
                iconSincronizado.visibility = if (coleta.sincronizado) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                root.setOnClickListener {
                    onItemClick(coleta)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

/**
 * DiffUtil para comparar itens
 * Usado pelo Paging para determinar quais itens mudaram
 */
class ColetaDiffCallback : DiffUtil.ItemCallback<Coleta>() {
    
    override fun areItemsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
        // Comparar IDs
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Coleta, newItem: Coleta): Boolean {
        // Comparar conteúdo completo
        return oldItem == newItem
    }
}


// ===== 4. FRAGMENT/ACTIVITY =====

package com.inventario.mobile.presentation.coletas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.databinding.FragmentColetasListBinding
import com.inventario.mobile.presentation.adapter.ColetasPagingAdapter
import com.inventario.mobile.presentation.viewmodel.ColetasListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ColetasListFragment : Fragment() {
    
    private var _binding: FragmentColetasListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ColetasListViewModel by viewModels()
    
    private lateinit var adapter: ColetasPagingAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeColetas()
    }
    
    private fun setupRecyclerView() {
        adapter = ColetasPagingAdapter { coleta ->
            // Ação ao clicar em uma coleta
            navigateToColetaDetail(coleta)
        }
        
        binding.recyclerViewColetas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ColetasListFragment.adapter
        }
    }
    
    private fun observeColetas() {
        // Coletar dados paginados
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.coletas.collectLatest { pagingData ->
                // Submeter dados ao adapter
                adapter.submitData(pagingData)
            }
        }
    }
    
    private fun navigateToColetaDetail(coleta: Coleta) {
        // Navegar para tela de detalhes
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


// ===== 5. LOADING STATE (OPCIONAL) =====

package com.inventario.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemLoadingStateBinding

/**
 * Adapter para mostrar estado de carregamento
 * (loading, erro, etc)
 */
class ColetasLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ColetasLoadStateAdapter.LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadingStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding, retry)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    
    class LoadStateViewHolder(
        private val binding: ItemLoadingStateBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(loadState: LoadState) {
            binding.apply {
                // Mostrar progress quando carregando
                progressBar.isVisible = loadState is LoadState.Loading
                
                // Mostrar botão de retry quando erro
                buttonRetry.isVisible = loadState is LoadState.Error
                textError.isVisible = loadState is LoadState.Error
                
                if (loadState is LoadState.Error) {
                    textError.text = loadState.error.localizedMessage
                }
                
                buttonRetry.setOnClickListener {
                    retry()
                }
            }
        }
    }
}

// Usar no Fragment:
private fun setupRecyclerView() {
    adapter = ColetasPagingAdapter { coleta ->
        navigateToColetaDetail(coleta)
    }
    
    // Adicionar load state adapter
    binding.recyclerViewColetas.adapter = adapter.withLoadStateFooter(
        footer = ColetasLoadStateAdapter { adapter.retry() }
    )
}


// ===== 6. SWIPE TO REFRESH =====

private fun setupSwipeRefresh() {
    binding.swipeRefreshLayout.setOnRefreshListener {
        // Refresh dos dados
        adapter.refresh()
        binding.swipeRefreshLayout.isRefreshing = false
    }
}


// ===== RESUMO =====

/**
 * BENEFÍCIOS DA PAGINAÇÃO:
 * 
 * 1. Performance:
 *    - Carrega apenas 50 itens por vez
 *    - Queries 10-100x mais rápidas
 *    - Usa menos memória
 * 
 * 2. UX:
 *    - Carregamento instantâneo
 *    - Scroll infinito suave
 *    - Indicadores de loading
 * 
 * 3. Escalabilidade:
 *    - Funciona com milhares de registros
 *    - Não trava o app
 *    - Cache automático
 * 
 * PRÓXIMOS PASSOS:
 * 
 * 1. Adicionar PagingSource no DAO ✅
 * 2. Criar Repository com Pager ✅
 * 3. Criar ViewModel com cachedIn ✅
 * 4. Criar PagingDataAdapter ✅
 * 5. Usar no Fragment/Activity ✅
 * 6. (Opcional) Adicionar LoadStateAdapter
 * 7. (Opcional) Adicionar SwipeRefresh
 */
