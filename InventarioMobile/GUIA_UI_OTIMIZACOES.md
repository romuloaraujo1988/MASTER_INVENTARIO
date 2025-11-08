# 🎨 Guia de Otimizações de UI/UX

## 📋 Índice

1. [Skeleton Loading](#skeleton-loading)
2. [Pull-to-Refresh](#pull-to-refresh)
3. [Lazy Loading de Imagens](#lazy-loading-de-imagens)
4. [Estados de Loading](#estados-de-loading)

---

## 1. Skeleton Loading

### O que é?

Skeleton loading mostra placeholders animados enquanto os dados estão carregando, dando a impressão de que o app é mais rápido.

### Implementação com Views (XML)

#### Passo 1: Adicionar dependência Shimmer

```gradle
// build.gradle (app)
dependencies {
    implementation 'com.facebook.shimmer:shimmer:0.5.0'
}
```

#### Passo 2: Criar layout de skeleton

```xml
<!-- layout/item_coleta_skeleton.xml -->
<com.facebook.shimmer.ShimmerFrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/shimmer_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        android:background="@drawable/bg_card">
        
        <!-- Linha 1: Título -->
        <View
            android:layout_width="200dp"
            android:layout_height="20dp"
            android:background="@color/skeleton_gray"/>
        
        <Space
            android:layout_width="match_parent"
            android:layout_height="8dp"/>
        
        <!-- Linha 2: Subtítulo -->
        <View
            android:layout_width="150dp"
            android:layout_height="16dp"
            android:background="@color/skeleton_gray"/>
        
        <Space
            android:layout_width="match_parent"
            android:layout_height="8dp"/>
        
        <!-- Linha 3: Data -->
        <View
            android:layout_width="100dp"
            android:layout_height="14dp"
            android:background="@color/skeleton_gray"/>
    </LinearLayout>
</com.facebook.shimmer.ShimmerFrameLayout>
```

#### Passo 3: Criar layout da lista com skeleton

```xml
<!-- fragment_coletas_list.xml -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Skeleton Loading (visível durante carregamento) -->
    <LinearLayout
        android:id="@+id/skeleton_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:visibility="gone">
        
        <include layout="@layout/item_coleta_skeleton"/>
        <include layout="@layout/item_coleta_skeleton"/>
        <include layout="@layout/item_coleta_skeleton"/>
        <include layout="@layout/item_coleta_skeleton"/>
        <include layout="@layout/item_coleta_skeleton"/>
    </LinearLayout>
    
    <!-- RecyclerView (visível após carregar) -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view_coletas"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="visible"/>
    
    <!-- Mensagem de lista vazia -->
    <TextView
        android:id="@+id/text_empty"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Nenhuma coleta encontrada"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"/>
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Passo 4: Controlar visibilidade no Fragment/Activity

```kotlin
class ColetasListFragment : Fragment() {
    
    private var _binding: FragmentColetasListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ColetasViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeColetas()
    }
    
    private fun observeColetas() {
        viewModel.coletasState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Mostrar skeleton
                    binding.skeletonContainer.visibility = View.VISIBLE
                    binding.recyclerViewColetas.visibility = View.GONE
                    binding.textEmpty.visibility = View.GONE
                }
                
                is UiState.Success -> {
                    // Esconder skeleton, mostrar lista
                    binding.skeletonContainer.visibility = View.GONE
                    
                    if (state.data.isEmpty()) {
                        binding.recyclerViewColetas.visibility = View.GONE
                        binding.textEmpty.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewColetas.visibility = View.VISIBLE
                        binding.textEmpty.visibility = View.GONE
                        adapter.submitList(state.data)
                    }
                }
                
                is UiState.Error -> {
                    // Esconder skeleton, mostrar erro
                    binding.skeletonContainer.visibility = View.GONE
                    binding.recyclerViewColetas.visibility = View.GONE
                    showError(state.message)
                }
            }
        }
    }
}

// Estados de UI
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 2. Pull-to-Refresh

### Implementação com SwipeRefreshLayout

#### Passo 1: Adicionar ao layout

```xml
<!-- fragment_coletas_list.xml -->
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/swipe_refresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view_coletas"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

#### Passo 2: Configurar no Fragment/Activity

```kotlin
class ColetasListFragment : Fragment() {
    
    private var _binding: FragmentColetasListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ColetasViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSwipeRefresh()
        setupRecyclerView()
        observeColetas()
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            // Configurar cores
            setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
            )
            
            // Configurar listener
            setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }
    
    private fun observeColetas() {
        viewModel.coletasState.observe(viewLifecycleOwner) { state ->
            // Parar animação de refresh
            binding.swipeRefresh.isRefreshing = state is UiState.Loading
            
            when (state) {
                is UiState.Loading -> {
                    // Já está mostrando o refresh
                }
                
                is UiState.Success -> {
                    adapter.submitList(state.data)
                    
                    // Mostrar mensagem de sucesso (opcional)
                    if (state.isRefresh) {
                        Snackbar.make(
                            binding.root,
                            "Dados atualizados",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                
                is UiState.Error -> {
                    Snackbar.make(
                        binding.root,
                        "Erro ao atualizar: ${state.message}",
                        Snackbar.LENGTH_LONG
                    ).setAction("Tentar novamente") {
                        viewModel.refresh()
                    }.show()
                }
            }
        }
    }
}

// ViewModel
class ColetasViewModel : ViewModel() {
    
    private val _coletasState = MutableLiveData<UiState<List<Coleta>>>()
    val coletasState: LiveData<UiState<List<Coleta>>> = _coletasState
    
    fun refresh() {
        viewModelScope.launch {
            _coletasState.value = UiState.Loading
            
            try {
                val coletas = repository.getColetas(forceRefresh = true)
                _coletasState.value = UiState.Success(coletas, isRefresh = true)
            } catch (e: Exception) {
                _coletasState.value = UiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
```

---

## 3. Lazy Loading de Imagens

### Implementação com Glide (já está no projeto)

```kotlin
// Carregar imagem simples
Glide.with(context)
    .load(imageUrl)
    .placeholder(R.drawable.placeholder_patrimonio)
    .error(R.drawable.error_patrimonio)
    .into(imageView)

// Carregar com transformações
Glide.with(context)
    .load(imageUrl)
    .placeholder(R.drawable.placeholder_patrimonio)
    .error(R.drawable.error_patrimonio)
    .centerCrop()
    .transform(RoundedCorners(16))
    .into(imageView)

// Carregar com cache
Glide.with(context)
    .load(imageUrl)
    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache em disco
    .skipMemoryCache(false) // Cache em memória
    .into(imageView)

// Pré-carregar imagens
Glide.with(context)
    .load(imageUrl)
    .preload()
```

### Configuração Global do Glide

```kotlin
// GlideModule.kt
@GlideModule
class InventarioGlideModule : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Configurar tamanho do cache em disco (100 MB)
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, 100 * 1024 * 1024)
        )
        
        // Configurar tamanho do cache em memória
        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20 MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        
        // Configurar log level
        builder.setLogLevel(Log.ERROR)
    }
    
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}
```

---

## 4. Estados de Loading

### Criar classe de estados reutilizável

```kotlin
// UiState.kt
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(
        val data: T,
        val isRefresh: Boolean = false
    ) : UiState<T>()
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}

// Extensões úteis
fun <T> UiState<T>.isLoading() = this is UiState.Loading
fun <T> UiState<T>.isSuccess() = this is UiState.Success
fun <T> UiState<T>.isError() = this is UiState.Error

fun <T> UiState<T>.getDataOrNull(): T? {
    return if (this is UiState.Success) data else null
}

fun <T> UiState<T>.getErrorOrNull(): String? {
    return if (this is UiState.Error) message else null
}
```

### Usar em ViewModels

```kotlin
class ColetasViewModel(
    private val repository: ColetaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Coleta>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Coleta>>> = _uiState.asStateFlow()
    
    init {
        loadColetas()
    }
    
    fun loadColetas() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val coletas = repository.getColetas()
                _uiState.value = UiState.Success(coletas)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Erro ao carregar coletas",
                    throwable = e
                )
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val coletas = repository.getColetas(forceRefresh = true)
                _uiState.value = UiState.Success(coletas, isRefresh = true)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Erro ao atualizar",
                    throwable = e
                )
            }
        }
    }
}
```

### Observar estados no Fragment

```kotlin
class ColetasListFragment : Fragment() {
    
    private val viewModel: ColetasViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeUiState()
    }
    
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        // Estado inicial
                    }
                    
                    is UiState.Loading -> {
                        showLoading()
                    }
                    
                    is UiState.Success -> {
                        hideLoading()
                        showData(state.data)
                        
                        if (state.isRefresh) {
                            showRefreshSuccess()
                        }
                    }
                    
                    is UiState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }
    
    private fun showData(data: List<Coleta>) {
        adapter.submitList(data)
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Tentar novamente") {
                viewModel.loadColetas()
            }
            .show()
    }
    
    private fun showRefreshSuccess() {
        Snackbar.make(binding.root, "Dados atualizados", Snackbar.LENGTH_SHORT).show()
    }
}
```

---

## 📋 Checklist de Implementação

### Skeleton Loading
- [ ] Adicionar dependência Shimmer
- [ ] Criar layouts de skeleton para cada tipo de item
- [ ] Adicionar skeleton aos layouts das telas
- [ ] Controlar visibilidade baseado no estado de loading

### Pull-to-Refresh
- [ ] Adicionar SwipeRefreshLayout aos layouts
- [ ] Configurar cores do tema
- [ ] Implementar método refresh() no ViewModel
- [ ] Observar estado e parar animação

### Lazy Loading de Imagens
- [ ] Verificar se Glide está configurado
- [ ] Criar GlideModule personalizado
- [ ] Usar Glide em todos os ImageViews
- [ ] Configurar placeholders e cache

### Estados de Loading
- [ ] Criar classe UiState
- [ ] Usar UiState em todos os ViewModels
- [ ] Observar estados nos Fragments
- [ ] Mostrar feedback apropriado para cada estado

---

## 🎯 Benefícios

### Skeleton Loading
- ✨ App parece 2x mais rápido
- 😊 Melhor percepção de performance
- 🎯 Usuário sabe que está carregando

### Pull-to-Refresh
- 🔄 Atualização intuitiva
- ✅ Padrão conhecido pelos usuários
- 📱 Melhor UX

### Lazy Loading
- 🖼️ Carregamento suave de imagens
- 💾 Cache automático
- 📱 Menos uso de memória

### Estados de Loading
- 🎯 Feedback claro para o usuário
- 🐛 Mais fácil de debugar
- ✅ Código mais organizado

---

## 🎉 Conclusão

Essas otimizações de UI/UX vão transformar a experiência do usuário, tornando o app mais profissional e agradável de usar!

**Implemente uma tela por vez e teste bem antes de continuar para a próxima.**
