# Exemplo: Integração do Cache na Busca Rápida

## 📝 Passo a Passo

### 1. Adicionar SearchCache ao BuscarPatrimoniosUseCase

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/BuscarPatrimoniosUseCase.kt`

```kotlin
class BuscarPatrimoniosUseCase @Inject constructor(
    private val patrimonioRepository: PatrimonioRepositoryImpl,
    private val patrimonioApi: PatrimonioApi,
    private val preferencesManager: PreferencesManager,
    private val searchCache: SearchCache  // ← ADICIONAR ESTA LINHA
) {
    companion object {
        private const val TAG = "BuscarPatrimoniosUseCase"
    }
    
    /**
     * Executa o caso de uso
     * 
     * @param query Termo de busca (mínimo 3 caracteres)
     * @param filtro Filtro de status (ALL, COLETADOS, PENDENTES, DIVERGENCIAS)
     * @return Result com lista de patrimônios ou erro
     */
    suspend operator fun invoke(
        query: String,
        filtro: SearchFilter = SearchFilter.ALL
    ): Result<List<PatrimonioComColeta>> {
        return try {
            // Validações de negócio
            if (query.isBlank()) {
                return Result.failure(Exception("Termo de busca não pode estar vazio"))
            }
            
            if (query.length < 3) {
                return Result.failure(Exception("Digite ao menos 3 caracteres para buscar"))
            }
            
            // Sanitizar entrada
            val queryLimpa = sanitizarQuery(query)
            val inventarioId = preferencesManager.getInventarioAtivoId()
            
            // ← ADICIONAR: Chave do cache
            val cacheKey = "$queryLimpa:$filtro"
            
            Log.d(TAG, "Buscando patrimônios: query='$queryLimpa', filtro=$filtro, inventarioId=$inventarioId")
            
            // ← ADICIONAR: Verificar cache primeiro
            searchCache.get(cacheKey)?.let { resultadoCache ->
                Log.d(TAG, "✓ Resultado encontrado em cache: ${resultadoCache.size} patrimônios")
                return Result.success(resultadoCache)
            }
            
            // Tentar buscar do servidor primeiro
            val resultadoServidor = buscarDoServidor(queryLimpa, filtro, inventarioId)
            
            if (resultadoServidor.isSuccess) {
                val resultados = resultadoServidor.getOrNull() ?: emptyList()
                Log.d(TAG, "✓ Busca no servidor bem-sucedida: ${resultados.size} resultados")
                
                // ← ADICIONAR: Cachear resultado
                searchCache.put(cacheKey, resultados)
                
                return resultadoServidor
            }
            
            // Fallback para busca local
            Log.w(TAG, "Servidor indisponível, usando busca local")
            val resultadoLocal = buscarLocal(queryLimpa, filtro, inventarioId)
            
            // ← ADICIONAR: Cachear resultado local também
            resultadoLocal.onSuccess { resultados ->
                searchCache.put(cacheKey, resultados)
            }
            
            resultadoLocal
            
        } catch (e: CancellationException) {
            // Job foi cancelado (normal durante debounce/navegação)
            Log.d(TAG, "ℹ️ Busca cancelada - operação normal")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar patrimônios", e)
            Result.failure(Exception("Erro ao buscar patrimônios: ${e.message}", e))
        }
    }
    
    // ... resto do código permanece igual
}
```

---

## 🧪 Teste de Integração

```kotlin
// Teste para verificar se o cache está funcionando
@Test
fun testBuscaComCache() = runTest {
    val query = "patrimonio"
    val filtro = SearchFilter.ALL
    
    // Primeira busca (sem cache)
    val start1 = System.currentTimeMillis()
    val result1 = buscarPatrimoniosUseCase(query, filtro)
    val tempo1 = System.currentTimeMillis() - start1
    
    assertTrue(result1.isSuccess)
    println("Primeira busca: ${tempo1}ms")
    
    // Segunda busca (com cache)
    val start2 = System.currentTimeMillis()
    val result2 = buscarPatrimoniosUseCase(query, filtro)
    val tempo2 = System.currentTimeMillis() - start2
    
    assertTrue(result2.isSuccess)
    println("Segunda busca (cache): ${tempo2}ms")
    
    // Verificar que o cache acelerou
    assertTrue(tempo2 < tempo1 / 5, "Cache não acelerou suficientemente")
    println("Aceleração: ${tempo1 / tempo2}x mais rápido")
}
```

---

## 📊 Resultados Esperados

### Sem Cache
```
Primeira busca: 450ms
Segunda busca: 420ms
Terceira busca: 430ms
```

### Com Cache
```
Primeira busca: 450ms (sem cache)
Segunda busca: 5ms (cache hit!)
Terceira busca: 3ms (cache hit!)
Aceleração: 100-150x mais rápido
```

---

## 🔧 Limpeza de Cache

### Limpar cache ao sincronizar

```kotlin
// presentation/sync/SyncViewModel.kt
class SyncViewModel @Inject constructor(
    private val sincronizarColetasUseCase: SincronizarColetasPendentesUseCase,
    private val searchCache: SearchCache  // ← Injetar cache
) : ViewModel() {
    
    fun sincronizarColetas() {
        viewModelScope.launch {
            _state.value = SyncState.Loading
            
            sincronizarColetasUseCase().fold(
                onSuccess = { quantidade ->
                    // ← ADICIONAR: Limpar cache após sincronizar
                    searchCache.clear()
                    Log.d(TAG, "✓ Cache limpo após sincronização")
                    
                    _state.value = SyncState.Success(
                        coletasSincronizadas = quantidade,
                        timestamp = System.currentTimeMillis()
                    )
                },
                onFailure = { error ->
                    _state.value = SyncState.Error(error.message ?: "Erro desconhecido")
                }
            )
        }
    }
}
```

### Limpar cache expirado periodicamente

```kotlin
// Usar WorkManager para limpar cache a cada 1 hora
class CacheCleanupWorker(
    context: Context,
    params: WorkerParameters,
    private val searchCache: SearchCache
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            searchCache.cleanExpired()
            Log.d(TAG, "✓ Cache limpo com sucesso")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar cache", e)
            Result.retry()
        }
    }
}

// Agendar no Application
class InventarioMobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Agendar limpeza de cache a cada 1 hora
        val cacheCleanupRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
            1, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cache_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cacheCleanupRequest
        )
    }
}
```

---

## 📈 Monitoramento

### Ver estatísticas do cache

```kotlin
// Adicionar em SettingsActivity ou DeveloperSettings
private fun mostrarEstatisticasCache() {
    val stats = searchCache.getStats()
    Log.d(TAG, stats)
    
    // Ou mostrar em um dialog
    AlertDialog.Builder(this)
        .setTitle("📊 Estatísticas do Cache")
        .setMessage(stats)
        .setPositiveButton("OK", null)
        .show()
}
```

### Exemplo de saída:
```
📊 Cache Stats:
Tamanho: 12/50
Entradas:
  • patrimonio:ALL: 45 resultados (2min atrás)
  • cadeira:COLETADOS: 12 resultados (5min atrás)
  • mesa:PENDENTES: 8 resultados (10min atrás)
  • computador:ALL: 23 resultados (15min atrás)
```

---

## ✅ Checklist de Implementação

- [ ] Criar `SearchCache.kt`
- [ ] Criar `CacheModule.kt`
- [ ] Adicionar `searchCache` ao `BuscarPatrimoniosUseCase`
- [ ] Adicionar lógica de cache no Use Case
- [ ] Testar primeira busca (sem cache)
- [ ] Testar segunda busca (com cache)
- [ ] Verificar que cache está acelerando
- [ ] Implementar limpeza de cache após sincronização
- [ ] Implementar limpeza periódica com WorkManager
- [ ] Adicionar logs para debug
- [ ] Testar com 10k+ patrimônios

---

## 🚀 Próximos Passos

1. **Implementar índices no banco** (5 minutos)
   - Adicionar índices para `numeroPatrimonio`, `descricao`, `nomeSala`
   - Ganho: 2-5x mais rápido

2. **Implementar FTS5** (30 minutos)
   - Usar Full-Text Search para buscas ainda mais rápidas
   - Ganho: 10-50x mais rápido

3. **Implementar paginação** (1 hora)
   - Usar Paging 3 para carregar resultados em lotes
   - Ganho: Reduz memória em 50-80%

---

## 📝 Notas

- Cache expira após 30 minutos (configurável em `SearchCache.kt`)
- Máximo de 50 entradas em cache (configurável)
- Cache é limpo automaticamente ao sincronizar
- Logs detalhados para debug (procurar por "SearchCache" no Logcat)

