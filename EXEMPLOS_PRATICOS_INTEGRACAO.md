# 💡 Exemplos Práticos - Integração das Melhorias Offline

**Objetivo:** Mostrar exemplos reais de como integrar as melhorias em diferentes cenários

---

## 📱 Exemplo 1: Activity Simples

### Antes (Sem Melhorias)

```kotlin
package com.inventario.mobile.ui.coleta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityColetaBinding

class ColetaActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityColetaBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Configurar UI normal
    }
}
```

### Depois (Com Melhorias)

```kotlin
package com.inventario.mobile.ui.coleta

import android.os.Bundle
import com.inventario.mobile.R
import com.inventario.mobile.databinding.ActivityColetaBinding
import com.inventario.mobile.ui.base.BaseActivity  // ← NOVO

class ColetaActivity : BaseActivity() {  // ← MUDOU
    
    private lateinit var binding: ActivityColetaBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColetaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ← NOVO: Adicionar indicador offline
        setupOfflineIndicator()
        
        setupUI()
    }
    
    private fun setupUI() {
        // Configurar UI normal
    }
    
    // ← NOVO: Reagir a mudanças de rede (opcional)
    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected) {
            // Reconectou - sincronizar dados
            showSyncingIndicator()
        }
    }
}
```

**Mudanças:**
1. Herdar `BaseActivity` ao invés de `AppCompatActivity`
2. Adicionar `setupOfflineIndicator()` no `onCreate()`
3. Opcionalmente sobrescrever `onNetworkStatusChanged()`

---

## 📱 Exemplo 2: Activity com ViewModel

### Antes

```kotlin
@AndroidEntryPoint
class SalaSelectionActivity : AppCompatActivity() {
    
    private val viewModel: SalaSelectionViewModel by viewModels()
    private lateinit var binding: ActivitySalaSelectionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        observeViewModel()
        viewModel.loadSalas()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> showLoading()
                    state.errorMessage != null -> showError(state.errorMessage)
                    else -> showSalas(state.salas)
                }
            }
        }
    }
}
```

### Depois

```kotlin
@AndroidEntryPoint
class SalaSelectionActivity : BaseActivity() {  // ← MUDOU
    
    private val viewModel: SalaSelectionViewModel by viewModels()
    private lateinit var binding: ActivitySalaSelectionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ← NOVO
        setupOfflineIndicator()
        
        observeViewModel()
        viewModel.loadSalas()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        showLoading()
                        // ← NOVO: Mostrar indicador de sync
                        if (isOnline()) {
                            showSyncingIndicator()
                        }
                    }
                    state.errorMessage != null -> {
                        showError(state.errorMessage)
                        // ← NOVO: Mostrar erro no indicador
                        if (isOffline()) {
                            showIndicatorMessage("Modo offline - ${state.errorMessage}")
                        }
                    }
                    else -> showSalas(state.salas)
                }
            }
        }
    }
    
    // ← NOVO: Recarregar ao reconectar
    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected) {
            viewModel.loadSalas(forceRefresh = true)
        }
    }
}
```

**Melhorias:**
1. Indicador offline integrado
2. Mostra "Sincronizando..." durante carregamento online
3. Mostra mensagem de erro offline
4. Recarrega dados ao reconectar

---

## 📱 Exemplo 3: Fragment

### Antes

```kotlin
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }
}
```

### Depois

```kotlin
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    // ← NOVO: Indicador offline
    private lateinit var offlineIndicator: OfflineIndicatorView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // ← NOVO: Adicionar indicador
        setupOfflineIndicator()
        
        // ← NOVO: Observar conectividade
        observeNetworkStatus()
        
        setupUI()
    }
    
    // ← NOVO
    private fun setupOfflineIndicator() {
        offlineIndicator = OfflineIndicatorView(requireContext())
        
        // Adicionar no topo do layout
        (binding.root as ViewGroup).addView(offlineIndicator, 0)
    }
    
    // ← NOVO
    private fun observeNetworkStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            NetworkUtils.observeNetworkConnectivity(requireContext())
                .collect { isConnected ->
                    if (isConnected) {
                        offlineIndicator.setStatus(OfflineIndicatorView.Status.ONLINE)
                    } else {
                        offlineIndicator.setStatus(OfflineIndicatorView.Status.OFFLINE)
                    }
                }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**Adaptações para Fragment:**
1. Criar `OfflineIndicatorView` manualmente
2. Adicionar ao layout do Fragment
3. Observar conectividade com `viewLifecycleOwner`
4. Limpar referências no `onDestroyView()`

---

## 📱 Exemplo 4: Activity com Operações de Rede

### Cenário: Salvar Coleta

```kotlin
@AndroidEntryPoint
class ColetaActivity : BaseActivity() {
    
    private val viewModel: ColetaViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coleta)
        
        setupOfflineIndicator()
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.btnSalvar.setOnClickListener {
            salvarColeta()
        }
    }
    
    private fun salvarColeta() {
        // ← NOVO: Verificar conectividade antes
        if (isOffline()) {
            // Avisar que será salvo localmente
            showIndicatorMessage("Salvando localmente (offline)")
        } else {
            // Avisar que está sincronizando
            showSyncingIndicator()
        }
        
        // Salvar (funciona online e offline)
        viewModel.salvarColeta(
            patrimonioId = patrimonioId,
            observacoes = binding.edtObservacoes.text.toString()
        )
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ColetaState.Success -> {
                        // ← NOVO: Mensagem diferente por modo
                        if (isOffline()) {
                            showIndicatorMessage("✓ Salvo localmente")
                            Toast.makeText(
                                this@ColetaActivity,
                                "Coleta salva. Será sincronizada ao conectar.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            showIndicatorMessage("✓ Sincronizado")
                            Toast.makeText(
                                this@ColetaActivity,
                                "Coleta salva e sincronizada!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        finish()
                    }
                    is ColetaState.Error -> {
                        showIndicatorMessage(state.message, isError = true)
                        Toast.makeText(
                            this@ColetaActivity,
                            "Erro: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    // ← NOVO: Tentar sincronizar ao reconectar
    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected) {
            showSyncingIndicator()
            // Sync automático já vai disparar via NetworkConnectivityObserver
        }
    }
}
```

**Melhorias:**
1. Verifica conectividade antes de salvar
2. Mostra mensagens diferentes (online vs offline)
3. Informa usuário sobre sincronização futura
4. Tenta sincronizar ao reconectar

---

## 📱 Exemplo 5: Activity com Lista

### Cenário: Listar Coletas

```kotlin
@AndroidEntryPoint
class CollectionViewActivity : BaseActivity() {
    
    private val viewModel: CollectionViewViewModel by viewModels()
    private lateinit var adapter: ColetaAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_view)
        
        setupOfflineIndicator()
        setupRecyclerView()
        observeViewModel()
        
        // Carregar dados
        viewModel.loadColetas()
    }
    
    private fun setupRecyclerView() {
        adapter = ColetaAdapter()
        binding.recyclerView.adapter = adapter
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CollectionState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        
                        // ← NOVO: Indicar fonte dos dados
                        if (isOffline()) {
                            showIndicatorMessage("Carregando dados locais...")
                        } else {
                            showSyncingIndicator()
                        }
                    }
                    is CollectionState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        adapter.submitList(state.coletas)
                        
                        // ← NOVO: Informar sobre dados
                        if (isOffline()) {
                            showIndicatorMessage(
                                "Mostrando ${state.coletas.size} coletas locais"
                            )
                        } else {
                            // Esconder indicador quando online
                            offlineIndicator?.setStatus(
                                OfflineIndicatorView.Status.ONLINE
                            )
                        }
                    }
                    is CollectionState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showIndicatorMessage(state.message, isError = true)
                    }
                }
            }
        }
    }
    
    // ← NOVO: Atualizar ao reconectar
    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected) {
            // Recarregar dados do servidor
            viewModel.loadColetas(forceRefresh = true)
        }
    }
}
```

**Melhorias:**
1. Indica fonte dos dados (local vs servidor)
2. Mostra quantidade de itens locais
3. Atualiza automaticamente ao reconectar
4. Feedback claro sobre estado

---

## 🎯 Padrões Comuns

### Padrão 1: Verificar Antes de Operação

```kotlin
fun fazerOperacao() {
    if (isOffline()) {
        // Avisar que será feito localmente
        showIndicatorMessage("Operação offline")
    } else {
        // Avisar que está sincronizando
        showSyncingIndicator()
    }
    
    // Executar operação (funciona em ambos os modos)
    viewModel.executar()
}
```

### Padrão 2: Mensagens Diferentes por Modo

```kotlin
when (result) {
    is Success -> {
        val message = if (isOffline()) {
            "Salvo localmente. Será sincronizado ao conectar."
        } else {
            "Salvo e sincronizado com sucesso!"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
```

### Padrão 3: Atualizar ao Reconectar

```kotlin
override fun onNetworkStatusChanged(isConnected: Boolean) {
    if (isConnected) {
        // Recarregar dados frescos do servidor
        viewModel.refresh()
    }
}
```

### Padrão 4: Indicar Fonte dos Dados

```kotlin
when (state) {
    is Loading -> {
        if (isOffline()) {
            showIndicatorMessage("Buscando dados locais...")
        } else {
            showSyncingIndicator()
        }
    }
    is Success -> {
        if (isOffline()) {
            showIndicatorMessage("${state.items.size} itens locais")
        }
    }
}
```

---

## 🎨 Customizações Úteis

### Customização 1: Cores por Tema

```kotlin
class MinhaActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minha)
        
        setupOfflineIndicator()
        
        // Customizar cores
        customizeIndicator()
    }
    
    private fun customizeIndicator() {
        // Acessar indicador e customizar
        // (implementar se necessário)
    }
}
```

### Customização 2: Mensagens Personalizadas

```kotlin
override fun onNetworkStatusChanged(isConnected: Boolean) {
    if (isConnected) {
        showIndicatorMessage("🌐 Conectado - Sincronizando...")
    } else {
        showIndicatorMessage("📡 Offline - Usando dados locais")
    }
}
```

### Customização 3: Ações no Indicador

```kotlin
// Adicionar botão de sync manual no indicador
binding.btnSyncManual.setOnClickListener {
    if (isOnline()) {
        showSyncingIndicator()
        viewModel.syncNow()
    } else {
        showIndicatorMessage("Sem conexão", isError = true)
    }
}
```

---

## 📊 Resumo dos Exemplos

| Exemplo | Cenário | Complexidade | Tempo |
|---------|---------|--------------|-------|
| 1 | Activity Simples | ⭐ Fácil | 2 min |
| 2 | Activity + ViewModel | ⭐⭐ Médio | 5 min |
| 3 | Fragment | ⭐⭐ Médio | 5 min |
| 4 | Operações de Rede | ⭐⭐⭐ Avançado | 10 min |
| 5 | Lista de Dados | ⭐⭐⭐ Avançado | 10 min |

---

## ✅ Checklist Rápido

Para cada Activity/Fragment:

1. [ ] Herdar `BaseActivity` (ou adicionar indicador manualmente)
2. [ ] Chamar `setupOfflineIndicator()`
3. [ ] Verificar `isOnline()`/`isOffline()` antes de operações
4. [ ] Mostrar mensagens diferentes por modo
5. [ ] Implementar `onNetworkStatusChanged()` se necessário
6. [ ] Testar com/sem internet

---

**Pronto para usar!** 🚀

