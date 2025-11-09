# 📱 Modo Offline - Sistema de Inventário Mobile

## 🎯 Objetivo

Permitir que o app funcione **completamente offline**, sincronizando dados quando houver conexão.

---

## 🏗️ Arquitetura Offline-First

### Princípio Básico

```
┌─────────────────────────────────────────────────────────────┐
│                    OFFLINE-FIRST                            │
│                                                             │
│  1. Todas as operações são salvas PRIMEIRO no banco local  │
│  2. Sincronização com servidor acontece em background      │
│  3. App funciona normalmente sem internet                  │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de Dados

```
┌──────────────┐
│   USUÁRIO    │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│              CAMADA DE APRESENTAÇÃO                      │
│  (Activities, ViewModels, UI States)                     │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│              CAMADA DE DOMÍNIO                           │
│  (Use Cases, Modelos de Negócio)                        │
└──────────────────┬───────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────────┐
│              CAMADA DE DADOS                             │
│                                                          │
│  ┌────────────────┐         ┌────────────────┐         │
│  │  ROOM DATABASE │ ◄─────► │  RETROFIT API  │         │
│  │   (SQLite)     │         │   (Servidor)   │         │
│  └────────────────┘         └────────────────┘         │
│         ▲                            ▲                  │
│         │                            │                  │
│         │    ┌──────────────────┐   │                  │
│         └────┤  SINCRONIZAÇÃO   ├───┘                  │
│              │   (WorkManager)  │                      │
│              └──────────────────┘                      │
└──────────────────────────────────────────────────────────┘
```

---

## 📊 Banco de Dados Local (Room)

### Entidades Principais

#### 1. **SalaEntity** (Salas)
```kotlin
@Entity(tableName = "salas")
data class SalaEntity(
    @PrimaryKey val id: Long,
    val nome: String,
    val codigo: String,
    val descricao: String,
    val setorId: Long,
    val ativo: Boolean,
    val sincronizado: Boolean = false,  // ← Controle de sincronização
    val dataCriacao: Long,
    val dataAtualizacao: Long,
    val servidorId: Long? = null        // ← ID no servidor
)
```

#### 2. **PatrimonioEntity** (Patrimônios)
```kotlin
@Entity(tableName = "patrimonios")
data class PatrimonioEntity(
    @PrimaryKey val id: Long,
    val numero: String,
    val descricao: String,
    val salaId: Long?,
    val responsavelId: Long?,
    val status: String,
    val sincronizado: Boolean = false,
    val dataCriacao: Long,
    val dataAtualizacao: Long,
    val servidorId: Long? = null
)
```

#### 3. **ColetaEntity** (Coletas)
```kotlin
@Entity(tableName = "coletas")
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patrimonioId: Long,
    val numeroPatrimonio: String,
    val salaId: Long,
    val responsavelId: Long?,
    val observacoes: String?,
    val latitude: Double?,
    val longitude: Double?,
    val dataColeta: Long,
    val sincronizado: Boolean = false,  // ← Pendente de sincronização
    val tentativasSincronizacao: Int = 0,
    val erroSincronizacao: String? = null,
    val servidorId: Long? = null
)
```

#### 4. **SincronizacaoEntity** (Controle de Sincronização)
```kotlin
@Entity(tableName = "sincronizacao")
data class SincronizacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,              // "SALA", "PATRIMONIO", "COLETA"
    val entidadeId: Long,
    val operacao: String,          // "INSERT", "UPDATE", "DELETE"
    val dataCriacao: Long,
    val sincronizado: Boolean = false,
    val tentativas: Int = 0,
    val ultimaTentativa: Long? = null,
    val erro: String? = null
)
```

---

## 🔄 Estratégia de Sincronização

### 1. **Download Inicial (Primeira Sincronização)**

Quando o usuário faz login pela primeira vez:

```kotlin
suspend fun sincronizacaoInicial() {
    // 1. Baixar Salas
    val salas = api.getSalas()
    salaDao.insertAll(salas.map { it.toEntity() })
    
    // 2. Baixar Patrimônios (paginado)
    var page = 0
    do {
        val patrimonios = api.getPatrimonios(page, 100)
        patrimonioDao.insertAll(patrimonios.map { it.toEntity() })
        page++
    } while (patrimonios.isNotEmpty())
    
    // 3. Baixar Responsáveis
    val responsaveis = api.getResponsaveis()
    responsavelDao.insertAll(responsaveis.map { it.toEntity() })
    
    // 4. Marcar sincronização completa
    prefs.setLastSyncTime(System.currentTimeMillis())
}
```

### 2. **Sincronização Incremental**

Sincroniza apenas dados modificados:

```kotlin
suspend fun sincronizacaoIncremental() {
    val lastSync = prefs.getLastSyncTime()
    
    // 1. Upload de coletas pendentes
    val coletasPendentes = coletaDao.getPendentes()
    coletasPendentes.forEach { coleta ->
        try {
            val response = api.createColeta(coleta.toDto())
            coletaDao.marcarSincronizada(coleta.id, response.id)
        } catch (e: Exception) {
            coletaDao.incrementarTentativas(coleta.id, e.message)
        }
    }
    
    // 2. Download de atualizações do servidor
    val atualizacoes = api.getAtualizacoes(lastSync)
    
    // Atualizar salas modificadas
    atualizacoes.salas.forEach { sala ->
        salaDao.insertOrUpdate(sala.toEntity())
    }
    
    // Atualizar patrimônios modificados
    atualizacoes.patrimonios.forEach { patrimonio ->
        patrimonioDao.insertOrUpdate(patrimonio.toEntity())
    }
    
    prefs.setLastSyncTime(System.currentTimeMillis())
}
```

### 3. **Sincronização Automática (WorkManager)**

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Verificar conectividade
            if (!isNetworkAvailable()) {
                return Result.retry()
            }
            
            // Executar sincronização
            sincronizacaoIncremental()
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização", e)
            Result.retry()
        }
    }
}

// Agendar sincronização periódica (a cada 15 minutos)
fun agendarSincronizacao() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "sync_work",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
}
```

---

## 🎮 Fluxo de Operações Offline

### Cenário 1: Coletar Patrimônio (Offline)

```kotlin
// 1. Usuário escaneia QR Code
val patrimonio = patrimonioDao.buscarPorNumero(numero)

// 2. Criar coleta localmente
val coleta = ColetaEntity(
    patrimonioId = patrimonio.id,
    numeroPatrimonio = patrimonio.numero,
    salaId = salaAtual.id,
    dataColeta = System.currentTimeMillis(),
    sincronizado = false  // ← Marca como pendente
)

// 3. Salvar no banco local
coletaDao.insert(coleta)

// 4. Registrar para sincronização
sincronizacaoDao.insert(SincronizacaoEntity(
    tipo = "COLETA",
    entidadeId = coleta.id,
    operacao = "INSERT",
    dataCriacao = System.currentTimeMillis()
))

// 5. Tentar sincronizar (se houver internet)
if (isNetworkAvailable()) {
    syncManager.sincronizarAgora()
}
```

### Cenário 2: Buscar Patrimônio (Offline)

```kotlin
// 1. Buscar primeiro no banco local
val patrimonio = patrimonioDao.buscarPorNumero(numero)

if (patrimonio != null) {
    // Encontrado localmente
    return Result.success(patrimonio)
}

// 2. Se não encontrou e há internet, buscar no servidor
if (isNetworkAvailable()) {
    try {
        val patrimonioDto = api.getPatrimonioByNumero(numero)
        val entity = patrimonioDto.toEntity()
        
        // Salvar no banco local
        patrimonioDao.insert(entity)
        
        return Result.success(entity)
    } catch (e: Exception) {
        return Result.failure(e)
    }
}

// 3. Sem internet e não encontrado
return Result.failure(Exception("Patrimônio não encontrado"))
```

---

## 📱 Indicadores Visuais de Status

### 1. **Badge de Sincronização**

```kotlin
// Na UI, mostrar status de sincronização
when {
    !isNetworkAvailable() -> {
        showBadge("Offline", Color.GRAY)
    }
    hasPendingSync() -> {
        showBadge("${pendingCount} pendentes", Color.ORANGE)
    }
    else -> {
        showBadge("Sincronizado", Color.GREEN)
    }
}
```

### 2. **Lista de Coletas Pendentes**

```kotlin
// Mostrar coletas que ainda não foram sincronizadas
val coletasPendentes = coletaDao.getPendentes()

coletasPendentes.forEach { coleta ->
    // Mostrar com ícone de "aguardando sincronização"
    ColetaItem(
        coleta = coleta,
        icon = Icons.CloudUpload,
        badge = "Pendente"
    )
}
```

---

## 🔧 Implementação Prática

### Passo 1: Atualizar Repository

```kotlin
class InventarioRepository(
    private val patrimonioDao: PatrimonioDao,
    private val coletaDao: ColetaDao,
    private val api: ApiService,
    private val networkChecker: NetworkChecker
) {
    
    suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
        // 1. Salvar localmente SEMPRE
        val entity = coleta.toEntity()
        val id = coletaDao.insert(entity)
        
        // 2. Tentar sincronizar se houver internet
        if (networkChecker.isConnected()) {
            try {
                val response = api.createColeta(coleta.toDto())
                coletaDao.marcarSincronizada(id, response.id)
            } catch (e: Exception) {
                // Falha na sincronização, mas coleta já está salva localmente
                Log.w(TAG, "Falha ao sincronizar, será tentado depois", e)
            }
        }
        
        return Result.success(coleta.copy(id = id))
    }
    
    suspend fun buscarPatrimonio(numero: String): Patrimonio? {
        // 1. Buscar localmente primeiro
        val local = patrimonioDao.buscarPorNumero(numero)
        if (local != null) return local.toDomain()
        
        // 2. Se não encontrou e há internet, buscar no servidor
        if (networkChecker.isConnected()) {
            try {
                val dto = api.getPatrimonioByNumero(numero)
                val entity = dto.toEntity()
                patrimonioDao.insert(entity)
                return entity.toDomain()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar do servidor", e)
            }
        }
        
        return null
    }
}
```

### Passo 2: Criar NetworkChecker

```kotlin
class NetworkChecker(private val context: Context) {
    
    fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        cm.registerDefaultNetworkCallback(callback)
        
        awaitClose {
            cm.unregisterNetworkCallback(callback)
        }
    }
}
```

---

## 📊 Monitoramento e Debug

### Logs Importantes

```kotlin
// Ao salvar localmente
Log.d(TAG, "Coleta salva localmente: ID=$id, sincronizado=false")

// Ao sincronizar
Log.d(TAG, "Sincronizando coleta ID=$id com servidor...")

// Sucesso
Log.d(TAG, "Coleta ID=$id sincronizada com sucesso (servidor ID=$servidorId)")

// Falha
Log.w(TAG, "Falha ao sincronizar coleta ID=$id: ${e.message}")
```

### Tela de Debug (Opcional)

```kotlin
// Mostrar estatísticas de sincronização
- Total de coletas: 150
- Sincronizadas: 145
- Pendentes: 5
- Com erro: 0
- Última sincronização: há 2 minutos
```

---

## ✅ Checklist de Implementação

- [x] Entities Room criadas
- [x] DAOs implementados
- [x] Database configurado
- [ ] Repository com lógica offline-first
- [ ] NetworkChecker implementado
- [ ] SyncManager com WorkManager
- [ ] Indicadores visuais de status
- [ ] Tela de sincronização manual
- [ ] Tratamento de conflitos
- [ ] Testes de sincronização

---

## 🎯 Próximos Passos

1. Implementar Repository completo com offline-first
2. Criar SyncManager com WorkManager
3. Adicionar indicadores visuais
4. Testar cenários offline
5. Implementar resolução de conflitos

---

**Versão**: 1.0.0  
**Data**: 09/11/2025
