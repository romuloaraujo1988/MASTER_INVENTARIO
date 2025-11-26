# 🔧 Implementação: Ações para Coletas Pendentes

## 🎯 Objetivo

Adicionar opções na tela de **Itens Coletados** para gerenciar coletas pendentes/com erro:
- ✅ Reenviar coleta específica
- ✅ Limpar erro e tentar novamente
- ✅ Excluir coleta problemática
- ✅ Ver detalhes do erro

---

## 📱 Tela: Itens Coletados (CollectionViewActivity)

### Funcionalidades a Adicionar:

#### 1. Indicador Visual de Status
```
✅ Sincronizada (verde)
⏳ Pendente (amarelo)
❌ Com Erro (vermelho)
```

#### 2. Menu de Contexto (Long Press)
```
Ao segurar uma coleta:
├── 🔄 Reenviar
├── 🗑️ Excluir
├── ℹ️ Ver Detalhes
└── 🧹 Limpar Erro
```

#### 3. Filtros
```
- Todas
- Sincronizadas
- Pendentes
- Com Erro
```

---

## 🏗️ Arquitetura da Solução

### Camada Domain (Use Cases)

#### 1. ReenviarColetaUseCase.kt
```kotlin
package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Reenviar uma coleta específica
 * Limpa erro e tenta sincronizar novamente
 */
class ReenviarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(coletaId: Long): Result<Boolean> {
        return try {
            // 1. Limpar erro da coleta
            coletaRepository.limparErroColeta(coletaId)
            
            // 2. Tentar sincronizar
            val sucesso = coletaRepository.sincronizarColetaEspecifica(coletaId)
            
            if (sucesso) {
                Result.success(true)
            } else {
                Result.failure(Exception("Falha ao reenviar coleta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 2. ExcluirColetaUseCase.kt
```kotlin
package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Excluir uma coleta pendente
 * Usado quando coleta não pode ser sincronizada
 */
class ExcluirColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(coletaId: Long): Result<Unit> {
        return try {
            coletaRepository.removerColetaPendente(coletaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 3. ObterDetalhesColetaUseCase.kt
```kotlin
package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import javax.inject.Inject

/**
 * Use Case: Obter detalhes completos de uma coleta
 * Inclui informações de erro e tentativas
 */
class ObterDetalhesColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(coletaId: Long): Result<Coleta?> {
        return try {
            val coleta = coletaRepository.getColetaById(coletaId)
            Result.success(coleta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### Camada Data (Repository)

#### Adicionar Métodos no ColetaRepository.kt

```kotlin
interface ColetaRepository {
    // ... métodos existentes ...
    
    /**
     * Sincroniza uma coleta específica
     * @param coletaId ID da coleta a sincronizar
     * @return true se sincronizou com sucesso
     */
    suspend fun sincronizarColetaEspecifica(coletaId: Long): Boolean
    
    /**
     * Obtém detalhes completos de uma coleta incluindo erros
     */
    suspend fun getColetaComDetalhes(coletaId: Long): ColetaComDetalhes?
}

/**
 * Coleta com informações detalhadas de erro
 */
data class ColetaComDetalhes(
    val coleta: Coleta,
    val erroSincronizacao: String?,
    val tentativasSincronizacao: Int,
    val ultimaTentativa: Long?
)
```

#### Implementar no ColetaRepositoryImpl.kt

```kotlin
override suspend fun sincronizarColetaEspecifica(coletaId: Long): Boolean {
    return try {
        android.util.Log.d("ColetaRepositoryImpl", "🔄 Sincronizando coleta específica: $coletaId")
        
        // Buscar coleta
        val entity = coletaDao.buscarPorId(coletaId) ?: run {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Coleta $coletaId não encontrada")
            return false
        }
        
        // Converter para domain
        val coleta = mapper.toDomain(entity)
        val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
        
        // Obter inventário ativo
        val inventarioId = preferencesManager.getInventarioAtivoId() ?: 0
        
        if (inventarioId <= 0) {
            android.util.Log.e("ColetaRepositoryImpl", "❌ Inventário não configurado")
            coletaDao.registrarErroSincronizacao(coletaId, "Inventário não configurado")
            return false
        }
        
        // Criar request
        val request = com.inventario.mobile.data.remote.dto.MobileColetaRequest(
            numeroPatrimonio = patrimonio?.numero ?: entity.numeroPatrimonio,
            idInventario = inventarioId,
            usuarioId = coleta.usuarioId.toInt(),
            idSala = patrimonio?.idSala,
            localizacaoEncontrada = coleta.localizacaoAtual,
            estadoEncontrado = coleta.status ?: "BOM",
            observacaoColeta = coleta.observacoes,
            dataColeta = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
                .format(java.util.Date(coleta.dataColeta)),
            latitude = coleta.latitude,
            longitude = coleta.longitude,
            fotoPatrimonio = null,
            semEtiqueta = false,
            descricaoItemSemEtiqueta = null,
            categoriaItemSemEtiqueta = null,
            deviceId = android.os.Build.MODEL,
            appVersion = "1.2",
            divergencia = false,
            motivoDivergencia = null
        )
        
        // Enviar para servidor
        android.util.Log.d("ColetaRepositoryImpl", "📤 Enviando coleta $coletaId para servidor...")
        val response = coletaApi.registrarColeta(request)
        
        if (response.success) {
            coletaDao.marcarSincronizada(coletaId)
            android.util.Log.d("ColetaRepositoryImpl", "✅ Coleta $coletaId sincronizada com sucesso")
            true
        } else {
            val erro = response.message ?: "Erro desconhecido"
            android.util.Log.e("ColetaRepositoryImpl", "❌ Falha ao sincronizar coleta $coletaId: $erro")
            coletaDao.registrarErroSincronizacao(coletaId, erro)
            false
        }
        
    } catch (e: Exception) {
        android.util.Log.e("ColetaRepositoryImpl", "❌ Exceção ao sincronizar coleta $coletaId", e)
        coletaDao.registrarErroSincronizacao(coletaId, e.message ?: "Erro de conexão")
        false
    }
}

override suspend fun getColetaComDetalhes(coletaId: Long): ColetaComDetalhes? {
    return try {
        val entity = coletaDao.buscarPorId(coletaId) ?: return null
        val coleta = mapper.toDomain(entity)
        
        ColetaComDetalhes(
            coleta = coleta,
            erroSincronizacao = entity.erroSincronizacao,
            tentativasSincronizacao = entity.tentativasSincronizacao,
            ultimaTentativa = entity.dataUltimaTentativa
        )
    } catch (e: Exception) {
        null
    }
}
```

#### Adicionar no ColetaDao.kt

```kotlin
@Query("SELECT * FROM coleta WHERE id = :id")
suspend fun buscarPorId(id: Long): ColetaEntity?
```

---

### Camada Presentation (ViewModel)

#### Atualizar CollectionViewViewModelClean.kt

```kotlin
@HiltViewModel
class CollectionViewViewModelClean @Inject constructor(
    private val buscarColetasUseCase: BuscarColetasUseCase,
    private val obterUsuarioAtualUseCase: ObterUsuarioAtualUseCase,
    private val reenviarColetaUseCase: ReenviarColetaUseCase,  // NOVO
    private val excluirColetaUseCase: ExcluirColetaUseCase,    // NOVO
    private val obterDetalhesColetaUseCase: ObterDetalhesColetaUseCase  // NOVO
) : ViewModel() {
    
    // ... código existente ...
    
    /**
     * Reenviar uma coleta específica
     */
    fun reenviarColeta(coletaId: Long) {
        viewModelScope.launch {
            _state.value = CollectionViewState.Loading("Reenviando coleta...")
            
            reenviarColetaUseCase(coletaId).fold(
                onSuccess = {
                    _state.value = CollectionViewState.ColetaReenviada("Coleta reenviada com sucesso!")
                    // Recarregar lista
                    carregarColetas()
                },
                onFailure = { error ->
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao reenviar coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Excluir uma coleta pendente
     */
    fun excluirColeta(coletaId: Long) {
        viewModelScope.launch {
            _state.value = CollectionViewState.Loading("Excluindo coleta...")
            
            excluirColetaUseCase(coletaId).fold(
                onSuccess = {
                    _state.value = CollectionViewState.ColetaExcluida("Coleta excluída com sucesso!")
                    // Recarregar lista
                    carregarColetas()
                },
                onFailure = { error ->
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao excluir coleta"
                    )
                }
            )
        }
    }
    
    /**
     * Obter detalhes de uma coleta
     */
    fun obterDetalhesColeta(coletaId: Long) {
        viewModelScope.launch {
            obterDetalhesColetaUseCase(coletaId).fold(
                onSuccess = { coleta ->
                    _state.value = CollectionViewState.DetalhesColeta(coleta)
                },
                onFailure = { error ->
                    _state.value = CollectionViewState.Error(
                        error.message ?: "Erro ao obter detalhes"
                    )
                }
            )
        }
    }
}

/**
 * Estados da tela de visualização de coletas
 */
sealed class CollectionViewState {
    object Idle : CollectionViewState()
    data class Loading(val message: String) : CollectionViewState()
    data class Success(val coletas: List<Coleta>) : CollectionViewState()
    data class Error(val message: String) : CollectionViewState()
    data class ColetaReenviada(val message: String) : CollectionViewState()  // NOVO
    data class ColetaExcluida(val message: String) : CollectionViewState()   // NOVO
    data class DetalhesColeta(val coleta: Coleta?) : CollectionViewState()   // NOVO
}
```

---

### Camada UI (Activity)

#### Atualizar CollectionViewActivity.kt

```kotlin
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {
    
    private val viewModel: CollectionViewViewModelClean by viewModels()
    private lateinit var adapter: ColetasAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... setup ...
        
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        adapter = ColetasAdapter(
            onItemClick = { coleta ->
                // Mostrar detalhes
                viewModel.obterDetalhesColeta(coleta.id.toLong())
            },
            onItemLongClick = { coleta ->
                // Mostrar menu de opções
                mostrarMenuOpcoes(coleta)
            }
        )
        
        binding.recyclerViewColetas.adapter = adapter
    }
    
    private fun mostrarMenuOpcoes(coleta: Coleta) {
        val opcoes = mutableListOf<String>()
        
        // Adicionar opções baseado no status
        if (!coleta.sincronizado) {
            opcoes.add("🔄 Reenviar")
            opcoes.add("🧹 Limpar Erro")
        }
        opcoes.add("ℹ️ Ver Detalhes")
        opcoes.add("🗑️ Excluir")
        
        AlertDialog.Builder(this)
            .setTitle("Ações para Coleta #${coleta.numeroPatrimonio}")
            .setItems(opcoes.toTypedArray()) { _, which ->
                when (opcoes[which]) {
                    "🔄 Reenviar" -> confirmarReenvio(coleta)
                    "🧹 Limpar Erro" -> limparErroColeta(coleta)
                    "ℹ️ Ver Detalhes" -> mostrarDetalhes(coleta)
                    "🗑️ Excluir" -> confirmarExclusao(coleta)
                }
            }
            .show()
    }
    
    private fun confirmarReenvio(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("Reenviar Coleta")
            .setMessage("Deseja tentar reenviar a coleta do patrimônio ${coleta.numeroPatrimonio}?")
            .setPositiveButton("Sim, Reenviar") { _, _ ->
                viewModel.reenviarColeta(coleta.id.toLong())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun confirmarExclusao(coleta: Coleta) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Excluir Coleta")
            .setMessage("""
                Tem certeza que deseja excluir esta coleta?
                
                Patrimônio: ${coleta.numeroPatrimonio}
                Data: ${formatarData(coleta.dataColeta)}
                
                ⚠️ Esta ação não pode ser desfeita!
            """.trimIndent())
            .setPositiveButton("Sim, Excluir") { _, _ ->
                viewModel.excluirColeta(coleta.id.toLong())
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun limparErroColeta(coleta: Coleta) {
        // Limpar erro e recarregar
        viewModel.reenviarColeta(coleta.id.toLong())
    }
    
    private fun mostrarDetalhes(coleta: Coleta) {
        val detalhes = """
            📦 Patrimônio: ${coleta.numeroPatrimonio}
            📅 Data: ${formatarData(coleta.dataColeta)}
            👤 Usuário: ${coleta.nomeUsuario}
            📍 Localização: ${coleta.localizacaoAtual}
            
            Status: ${if (coleta.sincronizado) "✅ Sincronizada" else "⏳ Pendente"}
            
            ${if (coleta.erroSincronizacao != null) 
                "❌ Erro: ${coleta.erroSincronizacao}\n🔄 Tentativas: ${coleta.tentativasSincronizacao}" 
                else ""}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Detalhes da Coleta")
            .setMessage(detalhes)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CollectionViewState.Loading -> showLoading(state.message)
                    is CollectionViewState.Success -> {
                        hideLoading()
                        adapter.submitList(state.coletas)
                    }
                    is CollectionViewState.ColetaReenviada -> {
                        hideLoading()
                        showSuccess(state.message)
                    }
                    is CollectionViewState.ColetaExcluida -> {
                        hideLoading()
                        showSuccess(state.message)
                    }
                    is CollectionViewState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                    // ... outros estados ...
                }
            }
        }
    }
}
```

---

### Adapter com Indicadores Visuais

#### ColetasAdapter.kt

```kotlin
class ColetasAdapter(
    private val onItemClick: (Coleta) -> Unit,
    private val onItemLongClick: (Coleta) -> Unit
) : ListAdapter<Coleta, ColetasAdapter.ViewHolder>(ColetaDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemColetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(coleta: Coleta) {
            binding.apply {
                tvNumeroPatrimonio.text = coleta.numeroPatrimonio
                tvDataColeta.text = formatarData(coleta.dataColeta)
                tvLocalizacao.text = coleta.localizacaoAtual
                
                // Indicador visual de status
                when {
                    coleta.sincronizado -> {
                        ivStatus.setImageResource(R.drawable.ic_check_circle)
                        ivStatus.setColorFilter(
                            ContextCompat.getColor(root.context, R.color.success)
                        )
                        tvStatus.text = "Sincronizada"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(root.context, R.color.success)
                        )
                    }
                    coleta.erroSincronizacao != null -> {
                        ivStatus.setImageResource(R.drawable.ic_error)
                        ivStatus.setColorFilter(
                            ContextCompat.getColor(root.context, R.color.error)
                        )
                        tvStatus.text = "Erro: ${coleta.erroSincronizacao}"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(root.context, R.color.error)
                        )
                        tvTentativas.visibility = View.VISIBLE
                        tvTentativas.text = "Tentativas: ${coleta.tentativasSincronizacao}"
                    }
                    else -> {
                        ivStatus.setImageResource(R.drawable.ic_pending)
                        ivStatus.setColorFilter(
                            ContextCompat.getColor(root.context, R.color.warning)
                        )
                        tvStatus.text = "Pendente"
                        tvStatus.setTextColor(
                            ContextCompat.getColor(root.context, R.color.warning)
                        )
                    }
                }
                
                // Click listeners
                root.setOnClickListener { onItemClick(coleta) }
                root.setOnLongClickListener {
                    onItemLongClick(coleta)
                    true
                }
            }
        }
    }
}
```

---

## 🎨 Layout XML

### item_coleta.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">
        
        <!-- Indicador de Status -->
        <ImageView
            android:id="@+id/ivStatus"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_gravity="center_vertical"
            android:src="@drawable/ic_check_circle"/>
        
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="16dp"
            android:orientation="vertical">
            
            <TextView
                android:id="@+id/tvNumeroPatrimonio"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Patrimônio #12345"
                android:textSize="16sp"
                android:textStyle="bold"/>
            
            <TextView
                android:id="@+id/tvDataColeta"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="26/11/2025 12:00"
                android:textSize="14sp"
                android:layout_marginTop="4dp"/>
            
            <TextView
                android:id="@+id/tvLocalizacao"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Sala 101"
                android:textSize="14sp"
                android:layout_marginTop="2dp"/>
            
            <TextView
                android:id="@+id/tvStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Sincronizada"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
            
            <TextView
                android:id="@+id/tvTentativas"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Tentativas: 3"
                android:textSize="12sp"
                android:visibility="gone"
                android:layout_marginTop="2dp"/>
            
        </LinearLayout>
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

---

## ✅ RESUMO DA SOLUÇÃO

### Funcionalidades Adicionadas:
1. ✅ **Indicador visual** de status (sincronizada/pendente/erro)
2. ✅ **Menu de contexto** ao segurar coleta
3. ✅ **Reenviar** coleta específica
4. ✅ **Excluir** coleta problemática
5. ✅ **Ver detalhes** incluindo erro
6. ✅ **Limpar erro** para nova tentativa

### Benefícios:
- ✅ Usuário vê claramente quais coletas têm problema
- ✅ Pode reenviar coletas específicas
- ✅ Pode excluir coletas que não podem ser sincronizadas
- ✅ Vê mensagem de erro detalhada
- ✅ Solução para coletas "presas"

---

**Deseja que eu implemente esta solução completa?**
