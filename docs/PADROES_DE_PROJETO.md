# Padrões de Projeto - Gerenciamento de Conectividade

## 🎯 Objetivo

Organizar o código de verificação de conectividade e acesso a dados usando padrões de projeto consolidados, tornando o sistema mais manutenível, testável e extensível.

---

## 🏗️ Padrões Implementados

### 1. Strategy Pattern (Padrão Estratégia)

**Problema**: Como alternar entre diferentes fontes de dados (servidor remoto vs banco local) sem código duplicado?

**Solução**: Definir uma interface comum e implementações específicas para cada fonte.

```kotlin
interface DataSourceStrategy {
    suspend fun isAvailable(): Boolean
    suspend fun getPatrimonios(): Result<List<Patrimonio>>
    suspend fun getPatrimonioPorNumero(numero: String): Result<Patrimonio>
    fun getSourceType(): DataSourceType
}
```

**Implementações**:
- `RemoteDataSourceStrategy` - Busca do servidor (API)
- `LocalDataSourceStrategy` - Busca do banco local (Room)

**Benefícios**:
- ✅ Código desacoplado
- ✅ Fácil adicionar novas fontes (cache, mock, etc)
- ✅ Testável isoladamente
- ✅ Sem if/else espalhados

---

### 2. Factory Pattern (Padrão Fábrica)

**Problema**: Como decidir qual estratégia usar baseado na conectividade?

**Solução**: Uma fábrica que cria a estratégia apropriada.

```kotlin
class DataSourceStrategyFactory {
    suspend fun getStrategy(): DataSourceStrategy {
        // Tenta remoto primeiro
        if (remoteStrategy.isAvailable()) {
            return remoteStrategy
        }
        // Fallback para local
        return localStrategy
    }
}
```

**Benefícios**:
- ✅ Lógica de decisão centralizada
- ✅ Fácil mudar prioridades
- ✅ Transparente para o cliente

---

### 3. Observer Pattern (Padrão Observador)

**Problema**: Como reagir a mudanças de conectividade em tempo real?

**Solução**: Observer que emite eventos quando a rede muda.

```kotlin
interface ConnectivityObserver {
    fun observe(): Flow<Status>
    
    enum class Status {
        AVAILABLE,      // Conectado
        UNAVAILABLE,    // Desconectado
        LOSING,         // Perdendo conexão
        LOST            // Conexão perdida
    }
}
```

**Implementação**:
```kotlin
class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {
    override fun observe(): Flow<Status> = callbackFlow {
        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(Status.AVAILABLE)
            }
            override fun onLost(network: Network) {
                trySend(Status.LOST)
            }
        }
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
```

**Benefícios**:
- ✅ Reativo (Flow)
- ✅ Múltiplos observadores
- ✅ Desacoplado
- ✅ Lifecycle-aware

---

### 4. Repository Pattern (Padrão Repositório)

**Problema**: Como abstrair a origem dos dados da camada de apresentação?

**Solução**: Repository que usa as estratégias internamente.

```kotlin
class PatrimonioRepositoryImpl(
    private val strategyFactory: DataSourceStrategyFactory
) {
    suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        val strategy = strategyFactory.getStrategy()
        return strategy.getPatrimonios()
    }
}
```

**Benefícios**:
- ✅ Camada de abstração
- ✅ Fácil trocar implementação
- ✅ Testável com mocks
- ✅ Single source of truth

---

## 📊 Arquitetura Completa

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           PatrimonioViewModel                     │  │
│  │  - observeConnectivity()                         │  │
│  │  - loadPatrimonios()                             │  │
│  │  - forceRefreshFromServer()                      │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                         │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │      PatrimonioRepositoryImpl (Repository)       │  │
│  │  - getPatrimonios()                              │  │
│  │  - getPatrimonioPorNumero()                      │  │
│  │  - observeConnectivity()                         │  │
│  └──────────────────┬───────────────────────────────┘  │
│                     │                                    │
│  ┌──────────────────┴───────────────────────────────┐  │
│  │   DataSourceStrategyFactory (Factory)            │  │
│  │  - getStrategy() → escolhe Remote ou Local       │  │
│  └──────────────────┬───────────────────────────────┘  │
└────────────────────┼────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│ RemoteStrategy   │    │  LocalStrategy   │
│  (API/Server)    │    │  (Room/SQLite)   │
└──────────────────┘    └──────────────────┘
        │                         │
        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│  PatrimonioApi   │    │  PatrimonioDao   │
│    (Retrofit)    │    │     (Room)       │
└──────────────────┘    └──────────────────┘
        │                         │
        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐
│   PostgreSQL     │    │     SQLite       │
│    (Servidor)    │    │     (Local)      │
└──────────────────┘    └──────────────────┘
```

---

## 🔄 Fluxo de Execução

### Cenário 1: Online (WiFi disponível)

```
1. ViewModel.loadPatrimonios()
   ↓
2. Repository.getPatrimonios()
   ↓
3. Factory.getStrategy()
   ├─→ RemoteStrategy.isAvailable() → true
   └─→ Retorna RemoteStrategy
   ↓
4. RemoteStrategy.getPatrimonios()
   ↓
5. PatrimonioApi.listarPatrimonios()
   ↓
6. PostgreSQL (servidor)
   ↓
7. Retorna dados para ViewModel
   ↓
8. ViewModel atualiza UI
```

### Cenário 2: Offline (Sem conexão)

```
1. ViewModel.loadPatrimonios()
   ↓
2. Repository.getPatrimonios()
   ↓
3. Factory.getStrategy()
   ├─→ RemoteStrategy.isAvailable() → false
   ├─→ LocalStrategy.isAvailable() → true
   └─→ Retorna LocalStrategy
   ↓
4. LocalStrategy.getPatrimonios()
   ↓
5. PatrimonioDao.getAll()
   ↓
6. SQLite (banco local)
   ↓
7. Retorna dados para ViewModel
   ↓
8. ViewModel atualiza UI
```

### Cenário 3: Mudança de Conectividade

```
1. ConnectivityObserver detecta mudança
   ↓
2. Emite Status.LOST via Flow
   ↓
3. ViewModel.observeConnectivity() recebe
   ↓
4. ViewModel atualiza UI (mostra indicador offline)
   ↓
5. Próxima chamada usa LocalStrategy automaticamente
```

---

## 💻 Exemplo de Uso

### No ViewModel

```kotlin
class PatrimonioViewModel(
    private val repository: PatrimonioRepositoryImpl
) : ViewModel() {
    
    init {
        // Observar conectividade
        viewModelScope.launch {
            repository.observeConnectivity().collect { status ->
                when (status) {
                    Status.AVAILABLE -> showOnlineIndicator()
                    Status.LOST -> showOfflineIndicator()
                }
            }
        }
    }
    
    fun loadPatrimonios() {
        viewModelScope.launch {
            // Usa estratégia automática (remoto ou local)
            val result = repository.getPatrimonios()
            
            if (result.isSuccess) {
                updateUI(result.getOrNull())
            }
        }
    }
    
    fun forceRefresh() {
        viewModelScope.launch {
            // Força uso do servidor
            val result = repository.getPatrimoniosFromRemote()
            
            if (result.isFailure) {
                showError("Sem conexão com servidor")
            }
        }
    }
}
```

### Na Activity/Fragment

```kotlin
class PatrimonioFragment : Fragment() {
    
    private val viewModel: PatrimonioViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Observar conectividade
        lifecycleScope.launch {
            viewModel.connectivityStatus.collect { status ->
                binding.networkIndicator.text = when (status) {
                    Status.AVAILABLE -> "✓ Online"
                    Status.UNAVAILABLE -> "✗ Offline"
                    Status.LOSING -> "⚠ Perdendo conexão"
                    Status.LOST -> "✗ Desconectado"
                }
            }
        }
        
        // Observar fonte de dados
        lifecycleScope.launch {
            viewModel.dataSource.collect { source ->
                binding.dataSourceIndicator.text = when (source) {
                    DataSourceType.REMOTE -> "Servidor"
                    DataSourceType.LOCAL -> "Banco Local"
                }
            }
        }
        
        // Carregar dados
        viewModel.loadPatrimonios()
    }
}
```

---

## 🎯 Benefícios da Arquitetura

### 1. Separação de Responsabilidades
- ✅ Cada classe tem uma única responsabilidade
- ✅ Fácil entender e manter
- ✅ Código organizado

### 2. Testabilidade
- ✅ Cada componente testável isoladamente
- ✅ Fácil criar mocks
- ✅ Testes unitários simples

```kotlin
@Test
fun `deve usar estratégia local quando offline`() = runTest {
    // Arrange
    val remoteStrategy = mock<RemoteDataSourceStrategy> {
        onBlocking { isAvailable() } doReturn false
    }
    val localStrategy = mock<LocalDataSourceStrategy> {
        onBlocking { isAvailable() } doReturn true
    }
    val factory = DataSourceStrategyFactory(...)
    
    // Act
    val strategy = factory.getStrategy()
    
    // Assert
    assertEquals(DataSourceType.LOCAL, strategy.getSourceType())
}
```

### 3. Extensibilidade
- ✅ Fácil adicionar novas estratégias
- ✅ Fácil adicionar novos observadores
- ✅ Não quebra código existente

```kotlin
// Adicionar cache em memória
class CacheDataSourceStrategy : DataSourceStrategy {
    private val cache = mutableMapOf<String, Patrimonio>()
    
    override suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        return Result.success(cache.values.toList())
    }
}

// Usar no Factory
class DataSourceStrategyFactory {
    suspend fun getStrategy(): DataSourceStrategy {
        return when {
            cacheStrategy.isAvailable() -> cacheStrategy  // Cache primeiro
            remoteStrategy.isAvailable() -> remoteStrategy
            else -> localStrategy
        }
    }
}
```

### 4. Manutenibilidade
- ✅ Mudanças localizadas
- ✅ Fácil debugar
- ✅ Logs organizados

### 5. Performance
- ✅ Fallback automático
- ✅ Sem tentativas desnecessárias
- ✅ Cache quando necessário

---

## 📚 Comparação: Antes vs Depois

### ❌ ANTES (Sem Padrões)

```kotlin
class PatrimonioRepository {
    suspend fun getPatrimonios(): List<Patrimonio> {
        return if (NetworkUtils.isOnline(context)) {
            try {
                api.getPatrimonios()
            } catch (e: Exception) {
                if (hasLocalData()) {
                    dao.getAll()
                } else {
                    throw e
                }
            }
        } else {
            if (hasLocalData()) {
                dao.getAll()
            } else {
                throw Exception("Sem dados")
            }
        }
    }
}
```

**Problemas**:
- ❌ Lógica complexa e aninhada
- ❌ Difícil testar
- ❌ Difícil adicionar novas fontes
- ❌ Código duplicado

### ✅ DEPOIS (Com Padrões)

```kotlin
class PatrimonioRepositoryImpl(
    private val strategyFactory: DataSourceStrategyFactory
) {
    suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        val strategy = strategyFactory.getStrategy()
        return strategy.getPatrimonios()
    }
}
```

**Benefícios**:
- ✅ Código limpo e simples
- ✅ Fácil testar
- ✅ Fácil estender
- ✅ Sem duplicação

---

## 🔧 Como Adicionar Nova Fonte de Dados

### Exemplo: Adicionar Cache em Memória

1. **Criar Strategy**:
```kotlin
class CacheDataSourceStrategy : DataSourceStrategy {
    private val cache = LruCache<String, Patrimonio>(100)
    
    override suspend fun isAvailable(): Boolean = cache.size() > 0
    
    override suspend fun getPatrimonios(): Result<List<Patrimonio>> {
        val list = mutableListOf<Patrimonio>()
        cache.snapshot().values.forEach { list.add(it) }
        return Result.success(list)
    }
    
    override fun getSourceType() = DataSourceType.CACHE
}
```

2. **Atualizar Factory**:
```kotlin
class DataSourceStrategyFactory {
    private val cacheStrategy = CacheDataSourceStrategy()
    
    suspend fun getStrategy(): DataSourceStrategy {
        return when {
            cacheStrategy.isAvailable() -> cacheStrategy  // Mais rápido
            remoteStrategy.isAvailable() -> remoteStrategy
            else -> localStrategy
        }
    }
}
```

3. **Pronto!** Sem mudar nenhum código existente.

---

## 📖 Referências

- **Strategy Pattern**: Gang of Four - Design Patterns
- **Factory Pattern**: Gang of Four - Design Patterns
- **Observer Pattern**: Gang of Four - Design Patterns
- **Repository Pattern**: Martin Fowler - Patterns of Enterprise Application Architecture
- **Clean Architecture**: Robert C. Martin (Uncle Bob)

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Autor**: Sistema de Inventário
