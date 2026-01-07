package com.inventario.mobile.presentation.inventario

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inventario.mobile.databinding.ItemPatrimonioBinding
import com.inventario.mobile.data.model.Patrimonio
import com.inventario.mobile.utils.FotoReferenciaHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter para lista de patrimônios com suporte a fotos de referência
 * 
 * @param onItemClick Callback quando item é clicado
 * @param fotoReferenciaHelper Helper para buscar fotos (opcional, pode ser null)
 * @param onFotoClick Callback quando foto é clicada para ampliar (opcional)
 */
class PatrimonioAdapter(
    private val onItemClick: (Patrimonio) -> Unit,
    private val fotoReferenciaHelper: FotoReferenciaHelper? = null,
    private val onFotoClick: ((Patrimonio, Bitmap) -> Unit)? = null
) : ListAdapter<Patrimonio, PatrimonioAdapter.PatrimonioViewHolder>(PatrimonioDiffCallback()) {

    // Cache de jobs de carregamento de fotos para cancelamento
    private val fotoLoadJobs = mutableMapOf<Long, Job>()
    
    // Scope para coroutines
    private val adapterScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatrimonioViewHolder {
        val binding = ItemPatrimonioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatrimonioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatrimonioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    override fun onViewRecycled(holder: PatrimonioViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelFotoLoading()
    }

    inner class PatrimonioViewHolder(
        private val binding: ItemPatrimonioBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var currentFotoJob: Job? = null
        private var currentPatrimonioId: Long? = null

        fun bind(patrimonio: Patrimonio) {
            currentPatrimonioId = patrimonio.id
            
            binding.apply {
                textViewNumero.text = patrimonio.numeroPatrimonio
                textViewDescricao.text = patrimonio.descricao
                
                // Marca e Modelo
                textViewMarca.text = patrimonio.marca ?: "Marca: N/A"
                textViewModelo.text = patrimonio.modelo ?: "Modelo: N/A"
                
                // Responsável
                if (!patrimonio.responsavelNome.isNullOrBlank()) {
                    textViewResponsavel.text = "Responsável: ${patrimonio.responsavelNome}"
                    layoutResponsavel.visibility = View.VISIBLE
                } else {
                    layoutResponsavel.visibility = View.GONE
                }
                
                // Localização
                textViewSetor.text = patrimonio.setorNome ?: "Setor: N/A"
                textViewSala.text = patrimonio.salaNome ?: "Sala: N/A"
                
                // Indicador de status de coleta
                val isColetado = patrimonio.coletado
                textViewStatus.text = if (isColetado) "Coletado" else "Pendente"
                textViewStatus.setTextColor(
                    if (isColetado) 
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    else 
                        itemView.context.getColor(android.R.color.holo_orange_dark)
                )

                root.setOnClickListener {
                    onItemClick(patrimonio)
                }
                
                // Carregar foto de referência
                carregarFotoReferencia(patrimonio)
            }
        }
        
        /**
         * Carrega foto de referência para o patrimônio
         */
        private fun carregarFotoReferencia(patrimonio: Patrimonio) {
            // Se não tiver helper, esconder card de foto
            if (fotoReferenciaHelper == null) {
                binding.cardFotoReferencia.visibility = View.GONE
                return
            }
            
            // Cancelar job anterior se existir
            cancelFotoLoading()
            
            // Mostrar card com loading
            binding.cardFotoReferencia.visibility = View.VISIBLE
            binding.progressFotoReferencia.visibility = View.VISIBLE
            binding.imageViewFotoReferencia.setImageResource(0)
            
            // Carregar foto em background
            currentFotoJob = adapterScope.launch {
                try {
                    val descricao = patrimonio.descricao ?: return@launch
                    
                    val bitmap = withContext(Dispatchers.IO) {
                        fotoReferenciaHelper.buscarFotoPorDescricao(descricao)
                    }
                    
                    // Verificar se ainda é o mesmo patrimônio (evitar race condition)
                    if (currentPatrimonioId != patrimonio.id) return@launch
                    
                    withContext(Dispatchers.Main) {
                        binding.progressFotoReferencia.visibility = View.GONE
                        
                        if (bitmap != null) {
                            binding.imageViewFotoReferencia.setImageBitmap(bitmap)
                            
                            // Configurar click para ampliar
                            if (onFotoClick != null) {
                                binding.cardFotoReferencia.setOnClickListener {
                                    onFotoClick.invoke(patrimonio, bitmap)
                                }
                            }
                        } else {
                            // Sem foto, esconder card
                            binding.cardFotoReferencia.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    // Em caso de erro, esconder card
                    withContext(Dispatchers.Main) {
                        binding.cardFotoReferencia.visibility = View.GONE
                        binding.progressFotoReferencia.visibility = View.GONE
                    }
                }
            }
            
            // Registrar job para cancelamento
            fotoLoadJobs[patrimonio.id] = currentFotoJob!!
        }
        
        /**
         * Cancela carregamento de foto em andamento
         */
        fun cancelFotoLoading() {
            currentFotoJob?.cancel()
            currentFotoJob = null
            currentPatrimonioId?.let { fotoLoadJobs.remove(it) }
        }
    }
    
    /**
     * Limpa recursos quando adapter é destruído
     */
    fun cleanup() {
        fotoLoadJobs.values.forEach { it.cancel() }
        fotoLoadJobs.clear()
    }
}

class PatrimonioDiffCallback : DiffUtil.ItemCallback<Patrimonio>() {
    override fun areItemsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Patrimonio, newItem: Patrimonio): Boolean {
        return oldItem == newItem
    }
}
