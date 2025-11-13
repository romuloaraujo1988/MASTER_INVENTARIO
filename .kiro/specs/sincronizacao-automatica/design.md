# Design Document - Sincronização Automática

## Overview

Sistema de sincronização automática em background para o app Android usando WorkManager. Permite sincronização baseada em tempo ou contador de coletas, com suporte a constraints (Wi-Fi, bateria), retry inteligente e notificações.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ SettingsActivity │────────▶│ SettingsViewModel│         │
│  └──────────────────┘         └──────────────────┘         │
│           │                            │                     │
│           │                            ▼                     │
│           │                   ┌──────────────────┐         │
│           │                   │ PreferencesManager│         │
│           │                   └──────────────────┘         │
└───────────┼────────────────────────────┼──────────────────┘
            │                            │
            │                            ▼
┌───────────┼────────────────────────────┼──────────────────┐
│           │         Domain Layer       │                   │
│           │                            │                   │
│           │                   ┌────────▼────────┐         │
│           │                   │ SyncScheduler   │         │
│           │                   │ (Use Case)      │         │
│           │                   └────────┬────────┘         │
│           │                            │                   │
└───────────┼────────────────────────────┼──────────────────┘
            │                            │
            ▼                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    WorkManager Layer                         │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ ColetaSyncWorker │         │ DatabaseCleanup  │         │
│  │ (PeriodicWork)   │         │ Worker           │         │
│  └────────┬─────────┘         └──────────────────┘         │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ NotificationUtils│         │ SyncLogger       │         │
│  └──────────────────┘         └──────────────────┘         │
└───────────┼──────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │ ColetaRepository │────────▶│ ColetaApi        │         │
│  └────────┬─────────┘         └──────────────────┘         │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │ ColetaDao (Room) │                                       │
│  └──────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. ColetaSyncWorker

**Responsabilidade**: Executar sincronização de coletas pendentes em background

```kotlin
@HiltWorker
class ColetaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sincronizarColetasPendentesUseCase: SincronizarColetasPendentesUseCase,
    private val notificationUtils: NotificationUtils,
    private val syncLogger: SyncLogger
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            syncLogger.logStart(SyncType.AUTO)
            
            val result = sincronizarColetasPendentesUseCase()
            
            when {
                result.isSuccess -> {
                    val stats = result.getOrNull()!!
                    syncLogger.logSuccess(stats)
                    notificationUtils.showSyncSuccess(stats)
                    Result.success(workDataOf(
                        "synced" to stats.synced,
                        "failed" to stats.failed
                    ))
                }
                else -> {
                    val error = result.exceptionOrNull()!!
                    syncLogger.logError(error)
                    
                    if (shouldRetry(error)) {
                        Result.retry()
                    } else {
                        notificationUtils.showSyncError(error)
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            syncLogger.logError(e)
            Result.retry()
        }
    }
    
    private fun shouldRetry(error: Throwable): Boolean {
        return when (error) {
            is IOException -> true // Network error
            is HttpException -> error.code() >= 500 // Server error
            else -> false
        }
    }
    
    companion object {
        const val WORK_NAME = "coleta_sync_periodic"
        const val WORK_NAME_MANUAL = "coleta_sync_manual"
        const val WORK_NAME_BY_COUNT = "coleta_sync_by_count"
    }
}
```

### 2. SyncScheduler (Use Case)

**Responsabilidade**: Agendar e gerenciar Workers de sincronização

```kotlin
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Agenda sincronização periódica baseada em tempo
     */
    fun schedulePeriodicSync() {
        val interval = preferencesManager.getSyncInterval() // em minutos
        val wifiOnly = preferencesManager.isWifiOnlyEnabled()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(preferencesManager.isBatterySaverEnabled())
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<ColetaSyncWorker>(
            repeatInterval = interval.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 15,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .addTag("sync_periodic")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            ColetaSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    /**
     * Cancela sincronização periódica
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(ColetaSyncWorker.WORK_NAME)
    }
    
    /**
     * Executa sincronização imediata (manual ou por contador)
     */
    fun syncNow(byCount: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<ColetaSyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(if (byCount) "sync_by_count" else "sync_manual")
            .build()
        
        val workName = if (byCount) {
            ColetaSyncWorker.WORK_NAME_BY_COUNT
        } else {
            ColetaSyncWorker.WORK_NAME_MANUAL
        }
        
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    /**
     * Verifica se deve sincronizar por contador
     */
    fun checkCounterSync() {
        val currentCount = preferencesManager.getCollectionCount()
        val limit = preferencesManager.getSyncCollectionInterval()
        
        if (currentCount >= limit) {
            syncNow(byCount = true)
            preferencesManager.resetCollectionCount()
        }
    }
    
    /**
     * Incrementa contador de coletas
     */
    fun incrementCollectionCount() {
        val current = preferencesManager.getCollectionCount()
        preferencesManager.setCollectionCount(current + 1)
        
        if (preferencesManager.isAutoSyncByCountEnabled()) {
            checkCounterSync()
        }
    }
}
```

### 3. SincronizarColetasPendentesUseCase

**Responsabilidade**: Lógica de negócio para sincronizar coletas

```kotlin
class SincronizarColetasPendentesUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository
) {
    suspend operator fun invoke(): Result<SyncStats> {
        return try {
            val pendingColetas = coletaRepository.buscarColetasPendentes()
            
            if (pendingColetas.isEmpty()) {
                return Result.success(SyncStats(0, 0))
            }
            
            var synced = 0
            var failed = 0
            
            pendingColetas.forEach { coleta ->
                val result = coletaRepository.sincronizarColeta(coleta)
                if (result.isSuccess) {
                    synced++
                } else {
                    failed++
                }
            }
            
            Result.success(SyncStats(synced, failed))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class SyncStats(
    val synced: Int,
    val failed: Int
)
```

### 4. NotificationUtils

**Responsabilidade**: Gerenciar notificações de sincronização

```kotlin
class NotificationUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    
    init {
        createNotificationChannel()
    }
    
    fun showSyncSuccess(stats: SyncStats) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Sincronização concluída")
            .setContentText("${stats.synced} coletas sincronizadas")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SUCCESS, notification)
    }
    
    fun showSyncError(error: Throwable) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Erro na sincronização")
            .setContentText(error.message ?: "Erro desconhecido")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_retry,
                "Tentar novamente",
                createRetryPendingIntent()
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ERROR, notification)
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronização",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificações de sincronização de coletas"
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    companion object {
        private const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID_SUCCESS = 1001
        private const val NOTIFICATION_ID_ERROR = 1002
    }
}
```

### 5. SyncLogger

**Responsabilidade**: Registrar histórico de sincronizações

```kotlin
class SyncLogger @Inject constructor(
    private val syncDao: SincronizacaoDao
) {
    suspend fun logStart(type: SyncType) {
        val log = SincronizacaoEntity(
            tipo = type.name,
            dataHora = System.currentTimeMillis(),
            status = SyncStatus.IN_PROGRESS,
            mensagem = "Iniciando sincronização..."
        )
        syncDao.inserir(log)
    }
    
    suspend fun logSuccess(stats: SyncStats) {
        val log = SincronizacaoEntity(
            tipo = SyncType.AUTO.name,
            dataHora = System.currentTimeMillis(),
            status = SyncStatus.SUCCESS,
            mensagem = "${stats.synced} coletas sincronizadas, ${stats.failed} falhas",
            coletasSincronizadas = stats.synced,
            coletasFalhadas = stats.failed
        )
        syncDao.inserir(log)
        
        // Manter apenas últimas 50 entradas
        syncDao.limparAntigos(50)
    }
    
    suspend fun logError(error: Throwable) {
        val log = SincronizacaoEntity(
            tipo = SyncType.AUTO.name,
            dataHora = System.currentTimeMillis(),
            status = SyncStatus.ERROR,
            mensagem = error.message ?: "Erro desconhecido",
            stackTrace = error.stackTraceToString()
        )
        syncDao.inserir(log)
    }
}

enum class SyncType {
    AUTO, MANUAL, BY_COUNT
}

enum class SyncStatus {
    IN_PROGRESS, SUCCESS, ERROR
}
```

## Data Models

### SincronizacaoEntity (Room)

```kotlin
@Entity(tableName = "sincronizacao_log")
data class SincronizacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val tipo: String, // AUTO, MANUAL, BY_COUNT
    val dataHora: Long,
    val status: SyncStatus,
    val mensagem: String,
    
    val coletasSincronizadas: Int = 0,
    val coletasFalhadas: Int = 0,
    
    val stackTrace: String? = null
)
```

### SharedPreferences Keys

```kotlin
object SyncPreferences {
    const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
    const val KEY_SYNC_INTERVAL = "sync_interval" // minutos
    const val KEY_WIFI_ONLY = "wifi_only"
    const val KEY_AUTO_SYNC_BY_COUNT = "auto_sync_by_count"
    const val KEY_SYNC_COLLECTION_INTERVAL = "sync_collection_interval"
    const val KEY_COLLECTION_COUNT = "collection_count"
    const val KEY_BATTERY_SAVER = "battery_saver_enabled"
    const val KEY_LAST_SYNC = "last_sync_timestamp"
}
```

## Error Handling

### Retry Strategy

```kotlin
sealed class SyncError : Exception() {
    // Retry automático
    data class NetworkError(override val message: String) : SyncError()
    data class ServerError(val code: Int) : SyncError()
    data class TimeoutError(override val message: String) : SyncError()
    
    // Sem retry
    data class AuthError(override val message: String) : SyncError()
    data class ValidationError(override val message: String) : SyncError()
    data class UnknownError(override val cause: Throwable) : SyncError()
}
```

### Error Mapping

```kotlin
fun Throwable.toSyncError(): SyncError {
    return when (this) {
        is IOException -> SyncError.NetworkError(message ?: "Network error")
        is HttpException -> {
            when (code()) {
                401, 403 -> SyncError.AuthError("Authentication failed")
                in 500..599 -> SyncError.ServerError(code())
                else -> SyncError.UnknownError(this)
            }
        }
        is SocketTimeoutException -> SyncError.TimeoutError("Request timeout")
        else -> SyncError.UnknownError(this)
    }
}
```

## Testing Strategy

### Unit Tests

1. **SyncScheduler Tests**
   - Testar agendamento com diferentes intervalos
   - Testar cancelamento de Workers
   - Testar constraints (Wi-Fi, bateria)

2. **ColetaSyncWorker Tests**
   - Testar sincronização bem-sucedida
   - Testar retry em caso de falha
   - Testar notificações

3. **SincronizarColetasPendentesUseCase Tests**
   - Testar com lista vazia
   - Testar com múltiplas coletas
   - Testar falhas parciais

### Integration Tests

1. **WorkManager Integration**
   - Testar execução periódica
   - Testar constraints em ação
   - Testar retry policy

2. **Repository Integration**
   - Testar sincronização com API real
   - Testar atualização do banco local

## Performance Considerations

1. **Batch Processing**: Enviar coletas em lotes de 10 para reduzir requisições
2. **Paginação**: Buscar coletas pendentes em páginas de 50
3. **Índices**: Criar índice em `sincronizado` e `dataColeta` para queries rápidas
4. **Cleanup**: Remover logs antigos automaticamente (manter últimas 50)
5. **Throttling**: Limitar sincronização manual a 1 vez por minuto

## Security Considerations

1. **Token Refresh**: Renovar token JWT antes de sincronizar se expirado
2. **Encryption**: Coletas pendentes devem ser criptografadas no banco local
3. **Validation**: Validar dados antes de enviar para servidor
4. **Rate Limiting**: Respeitar rate limits da API (max 100 req/min)

## Deployment Strategy

### Phase 1: Core Implementation
- Implementar ColetaSyncWorker
- Implementar SyncScheduler
- Integrar com SettingsActivity

### Phase 2: Advanced Features
- Adicionar notificações
- Implementar logging
- Adicionar constraints de bateria

### Phase 3: Optimization
- Implementar batch processing
- Adicionar retry inteligente
- Otimizar performance

## Monitoring and Logging

```kotlin
// Logs estruturados
Log.d("SyncWorker", "Starting sync: type=$type, pending=$count")
Log.i("SyncWorker", "Sync completed: synced=$synced, failed=$failed, duration=${duration}ms")
Log.e("SyncWorker", "Sync failed: error=${error.message}", error)

// Métricas
- Total de sincronizações (sucesso/falha)
- Tempo médio de sincronização
- Taxa de retry
- Coletas pendentes por período
```

## Dependencies

```gradle
// WorkManager
implementation "androidx.work:work-runtime-ktx:2.9.0"

// Hilt Worker
implementation "androidx.hilt:hilt-work:1.1.0"
ksp "androidx.hilt:hilt-compiler:1.1.0"

// Notifications
implementation "androidx.core:core-ktx:1.12.0"
```

## Migration Path

1. ✅ Criar entities e DAOs (já existe)
2. ✅ Criar Use Cases (já existe)
3. 🔄 Implementar ColetaSyncWorker
4. 🔄 Implementar SyncScheduler
5. 🔄 Integrar com SettingsActivity
6. 🔄 Adicionar notificações
7. 🔄 Implementar logging
8. 🔄 Testes

## References

- [WorkManager Documentation](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Background Work Guide](https://developer.android.com/guide/background)
- [Hilt Worker Injection](https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager)
