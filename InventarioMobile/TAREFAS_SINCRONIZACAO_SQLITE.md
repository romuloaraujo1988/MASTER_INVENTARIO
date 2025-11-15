# Tarefas: Sincronização SQLite ↔ PostgreSQL

## 🎯 Objetivo

Tornar o banco SQLite local funcional e compatível com o servidor PostgreSQL, permitindo:
- ✅ Modo offline completo
- ✅ Sincronização bidirecional
- ✅ Dados consistentes entre local e servidor
- ✅ Fallback automático quando servidor estiver offline

---

## 📋 Tarefas Prioritárias

### 1. Sincronização de Coletas (Download)

**Objetivo**: Baixar coletas do servidor e salvar no SQLite local com todos os dados

#### Tarefa 1.1: Criar Use Case de Sincronização
```kotlin
// domain/usecase/SincronizarColetasDoServidorUseCase.kt
class SincronizarColetasDoServidorUseCase @Inject constructor(
    private val apiService: ApiService,
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao
) {
    suspend operator fun invoke(): Result<Int> {
        // 1. Buscar coletas do servidor
        // 2. Converter para ColetaEntity
        // 3. Salvar no banco local
        // 4. Retornar quantidade sincronizada
    }
}
```

**Arquivos a criar/modificar**:
- `domain/usecase/SincronizarColetasDoServidorUseCase.kt` ✨ CRIAR
- `data/repository/ColetaRepositoryImpl.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Baixa todas as coletas do servidor
- [ ] Salva no SQLite com todos os campos preenchidos
- [ ] Não duplica coletas existentes
- [ ] Atualiza coletas modificadas no servidor

---

#### Tarefa 1.2: Preencher Campos Completos na ColetaEntity
```kotlin
// Ao salvar coleta do servidor no SQLite
val entity = ColetaEntity(
    id = dto.id,
    idPatrimonio = dto.patrimonioId,
    numeroPatrimonio = dto.numeroPatrimonio,  // ✅ Preenchido
    idInventario = dto.idInventario,
    idSala = dto.idSala,
    nomeSala = dto.nomeSala,                  // ✅ Preenchido
    idResponsavel = null,
    nomeResponsavel = null,
    observacao = dto.observacoes,
    estadoPatrimonio = dto.estadoEncontrado,
    latitude = null,
    longitude = null,
    dataColeta = parseDataColeta(dto.dataColeta),
    idUsuario = dto.usuarioId,
    nomeUsuario = dto.nomeColetor,            // ✅ Preenchido
    sincronizado = true                       // ✅ Já sincronizado
)
```

**Arquivos a modificar**:
- `data/mapper/ColetaMapper.kt` ✏️ MODIFICAR
- `data/repository/ColetaRepositoryImpl.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Todos os campos da ColetaEntity são preenchidos
- [ ] Datas são convertidas corretamente
- [ ] IDs são mapeados corretamente

---

### 2. Sincronização de Coletas (Upload)

**Objetivo**: Enviar coletas locais pendentes para o servidor

#### Tarefa 2.1: Implementar Upload de Coletas Pendentes
```kotlin
// domain/usecase/EnviarColetasPendentesUseCase.kt
class EnviarColetasPendentesUseCase @Inject constructor(
    private val coletaDao: ColetaDao,
    private val coletaApi: ColetaApi
) {
    suspend operator fun invoke(): Result<SyncResult> {
        // 1. Buscar coletas não sincronizadas (sincronizado = false)
        // 2. Enviar para servidor
        // 3. Marcar como sincronizada se sucesso
        // 4. Registrar erro se falha
    }
}

data class SyncResult(
    val total: Int,
    val sucesso: Int,
    val falhas: Int,
    val erros: List<String>
)
```

**Arquivos a criar/modificar**:
- `domain/usecase/EnviarColetasPendentesUseCase.kt` ✨ CRIAR
- `data/repository/ColetaRepositoryImpl.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Envia apenas coletas não sincronizadas
- [ ] Marca como sincronizada após sucesso
- [ ] Registra erro em caso de falha
- [ ] Não perde dados em caso de erro

---

### 3. Estratégia Offline-First

**Objetivo**: App funciona offline e sincroniza quando online

#### Tarefa 3.1: Implementar Estratégia de Fallback
```kotlin
// domain/usecase/BuscarColetasComFallbackUseCase.kt
class BuscarColetasComFallbackUseCase @Inject constructor(
    private val apiService: ApiService,
    private val coletaDao: ColetaDao,
    private val networkChecker: NetworkChecker
) {
    suspend operator fun invoke(): Result<List<Coleta>> {
        return if (networkChecker.isOnline()) {
            // 1. Tentar buscar do servidor
            try {
                buscarDoServidor()
            } catch (e: Exception) {
                // 2. Se falhar, buscar do local
                buscarDoLocal()
            }
        } else {
            // 3. Se offline, buscar do local
            buscarDoLocal()
        }
    }
}
```

**Arquivos a criar/modificar**:
- `domain/usecase/BuscarColetasComFallbackUseCase.kt` ✨ CRIAR
- `util/NetworkChecker.kt` ✨ CRIAR
- `presentation/coleta/CollectionViewViewModelClean.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Tenta servidor primeiro se online
- [ ] Fallback para local se servidor falhar
- [ ] Usa local diretamente se offline
- [ ] Indica fonte dos dados (servidor/local)

---

### 4. Sincronização Automática

**Objetivo**: Sincronizar automaticamente em background

#### Tarefa 4.1: Implementar WorkManager para Sync
```kotlin
// sync/SyncWorker.kt
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // 1. Baixar coletas do servidor
        // 2. Enviar coletas pendentes
        // 3. Sincronizar patrimônios
        // 4. Retornar resultado
    }
}

// Agendar sync periódico
fun agendarSyncPeriodico() {
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        15, TimeUnit.MINUTES
    ).setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    ).build()
    
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "sync_coletas",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
}
```

**Arquivos a criar/modificar**:
- `sync/SyncWorker.kt` ✨ CRIAR
- `sync/SyncScheduler.kt` ✏️ MODIFICAR
- `Application.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Sync automático a cada 15 minutos
- [ ] Apenas quando conectado à internet
- [ ] Não drena bateria
- [ ] Pode ser cancelado/reagendado

---

### 5. Resolução de Conflitos

**Objetivo**: Resolver conflitos quando mesma coleta é modificada local e servidor

#### Tarefa 5.1: Implementar Estratégia de Conflitos
```kotlin
// domain/usecase/ResolverConflitosUseCase.kt
class ResolverConflitosUseCase @Inject constructor(
    private val coletaDao: ColetaDao,
    private val apiService: ApiService
) {
    suspend operator fun invoke(): Result<ConflictResolution> {
        // Estratégia: Servidor sempre ganha
        // 1. Identificar coletas conflitantes
        // 2. Comparar timestamps
        // 3. Manter versão mais recente
        // 4. Notificar usuário se necessário
    }
}

data class ConflictResolution(
    val conflitos: Int,
    val resolvidosAutomaticamente: Int,
    val requeremAtencao: List<Coleta>
)
```

**Arquivos a criar**:
- `domain/usecase/ResolverConflitosUseCase.kt` ✨ CRIAR

**Critérios de Aceite**:
- [ ] Detecta conflitos automaticamente
- [ ] Resolve conflitos simples (servidor ganha)
- [ ] Notifica usuário em conflitos complexos
- [ ] Não perde dados

---

### 6. Migração de Dados Antigos

**Objetivo**: Atualizar coletas antigas no SQLite com dados completos

#### Tarefa 6.1: Criar Script de Migração
```kotlin
// data/migration/ColetaMigration.kt
class ColetaMigration @Inject constructor(
    private val coletaDao: ColetaDao,
    private val patrimonioDao: PatrimonioDao,
    private val apiService: ApiService
) {
    suspend fun migrarColetasAntigas(): Result<Int> {
        // 1. Buscar coletas com campos vazios
        val coletasIncompletas = coletaDao.buscarColetasIncompletas()
        
        var atualizadas = 0
        
        for (coleta in coletasIncompletas) {
            // 2. Buscar dados do patrimônio
            val patrimonio = patrimonioDao.buscarPorId(coleta.idPatrimonio)
            
            // 3. Atualizar campos vazios
            if (patrimonio != null) {
                val coletaAtualizada = coleta.copy(
                    numeroPatrimonio = patrimonio.numero,
                    nomeSala = patrimonio.nomeSala
                )
                coletaDao.inserir(coletaAtualizada)
                atualizadas++
            }
        }
        
        return Result.success(atualizadas)
    }
}
```

**Arquivos a criar/modificar**:
- `data/migration/ColetaMigration.kt` ✨ CRIAR
- `data/local/dao/ColetaDao.kt` ✏️ MODIFICAR (adicionar `buscarColetasIncompletas()`)

**Critérios de Aceite**:
- [ ] Identifica coletas com dados incompletos
- [ ] Preenche dados do patrimônio
- [ ] Não quebra coletas existentes
- [ ] Pode ser executado múltiplas vezes

---

### 7. Indicadores de Sincronização

**Objetivo**: Mostrar status de sincronização para o usuário

#### Tarefa 7.1: Adicionar Indicadores na UI
```kotlin
// presentation/state/SyncState.kt
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(
        val coletasBaixadas: Int,
        val coletasEnviadas: Int,
        val timestamp: Long
    ) : SyncState()
    data class Error(val message: String) : SyncState()
    data class Partial(
        val sucesso: Int,
        val falhas: Int
    ) : SyncState()
}

// UI
when (syncState) {
    is SyncState.Syncing -> {
        showSyncIndicator()
        disableActions()
    }
    is SyncState.Success -> {
        showSuccessMessage("${state.coletasBaixadas} coletas sincronizadas")
    }
    // ...
}
```

**Arquivos a criar/modificar**:
- `presentation/state/SyncState.kt` ✨ CRIAR
- `presentation/sync/SyncViewModel.kt` ✏️ MODIFICAR
- `presentation/main/MainActivity.kt` ✏️ MODIFICAR

**Critérios de Aceite**:
- [ ] Mostra ícone de sincronização
- [ ] Indica quando está sincronizando
- [ ] Mostra última sincronização
- [ ] Alerta sobre coletas pendentes

---

## 📊 Priorização das Tarefas

### 🔴 Prioridade ALTA (Fazer Primeiro)
1. **Tarefa 1.1**: Sincronização de Coletas (Download)
2. **Tarefa 1.2**: Preencher Campos Completos
3. **Tarefa 3.1**: Estratégia Offline-First
4. **Tarefa 6.1**: Migração de Dados Antigos

### 🟡 Prioridade MÉDIA (Fazer Depois)
5. **Tarefa 2.1**: Upload de Coletas Pendentes
6. **Tarefa 7.1**: Indicadores de Sincronização

### 🟢 Prioridade BAIXA (Fazer Quando Possível)
7. **Tarefa 4.1**: Sincronização Automática
8. **Tarefa 5.1**: Resolução de Conflitos

---

## 🔧 Implementação Sugerida

### Fase 1: Sincronização Básica (1-2 dias)
```
✅ Tarefa 1.1: Download de coletas
✅ Tarefa 1.2: Preencher campos
✅ Tarefa 6.1: Migração de dados antigos
```

### Fase 2: Modo Offline (1 dia)
```
✅ Tarefa 3.1: Fallback automático
✅ Tarefa 2.1: Upload de pendentes
```

### Fase 3: UX e Automação (1-2 dias)
```
✅ Tarefa 7.1: Indicadores na UI
✅ Tarefa 4.1: Sync automático
```

### Fase 4: Refinamento (1 dia)
```
✅ Tarefa 5.1: Resolução de conflitos
✅ Testes e ajustes
```

---

## 🧪 Testes Necessários

### Testes de Sincronização
- [ ] Sincronizar com servidor online
- [ ] Sincronizar com servidor offline
- [ ] Sincronizar com dados parciais
- [ ] Sincronizar com conflitos

### Testes de Offline
- [ ] Criar coleta offline
- [ ] Visualizar coletas offline
- [ ] Sincronizar ao voltar online
- [ ] Perda de conexão durante sync

### Testes de Migração
- [ ] Migrar coletas antigas
- [ ] Não duplicar coletas
- [ ] Preservar dados existentes
- [ ] Executar múltiplas vezes

---

## 📝 Queries SQL Úteis

### Verificar Coletas Incompletas
```sql
-- SQLite
SELECT COUNT(*) FROM coleta 
WHERE numeroPatrimonio = '' OR numeroPatrimonio IS NULL;
```

### Verificar Coletas Pendentes
```sql
-- SQLite
SELECT COUNT(*) FROM coleta WHERE sincronizado = 0;
```

### Limpar Coletas Duplicadas
```sql
-- SQLite
DELETE FROM coleta 
WHERE id NOT IN (
    SELECT MIN(id) 
    FROM coleta 
    GROUP BY idPatrimonio, dataColeta
);
```

---

## 🎯 Resultado Esperado

Após implementar todas as tarefas:

### ✅ Funcionalidades
- App funciona 100% offline
- Sincronização automática em background
- Dados sempre atualizados
- Sem perda de dados

### ✅ Experiência do Usuário
- Coletas aparecem instantaneamente (local)
- Sincronização transparente
- Indicadores claros de status
- Sem travamentos ou lentidão

### ✅ Confiabilidade
- Dados consistentes entre local e servidor
- Resolução automática de conflitos
- Recuperação de erros
- Logs para debug

---

## 📚 Referências

- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Sync Strategies](https://developer.android.com/topic/architecture/data-layer#sync)

---

**Criado em**: 14/11/2025  
**Versão**: 1.0.0  
**Status**: 📋 Planejamento Completo
