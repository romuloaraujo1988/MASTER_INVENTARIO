# Implementation Plan - Sincronização Automática

## Task List

- [x] 1. Criar entidades e DAOs para logging de sincronização


  - Criar `SincronizacaoEntity` com campos: id, tipo, dataHora, status, mensagem, coletasSincronizadas, coletasFalhadas, stackTrace
  - Criar `SincronizacaoDao` com métodos: inserir, buscarTodas, buscarPorTipo, limparAntigos
  - Adicionar `SincronizacaoEntity` no `AppDatabase`
  - Criar migração do banco (versão 2 → 3)
  - _Requirements: 10.1, 10.2, 10.3_



- [ ] 2. Implementar SyncLogger para histórico de sincronizações
  - Criar classe `SyncLogger` com injeção de `SincronizacaoDao`
  - Implementar método `logStart(type: SyncType)`
  - Implementar método `logSuccess(stats: SyncStats)`
  - Implementar método `logError(error: Throwable)`
  - Criar enums `SyncType` (AUTO, MANUAL, BY_COUNT) e `SyncStatus` (IN_PROGRESS, SUCCESS, ERROR)



  - Implementar limpeza automática mantendo últimas 50 entradas
  - _Requirements: 10.1, 10.2, 10.3, 10.4_



- [ ] 3. Criar data class SyncStats e error handling
  - Criar `data class SyncStats(synced: Int, failed: Int)`
  - Criar sealed class `SyncError` com subclasses: NetworkError, ServerError, TimeoutError, AuthError, ValidationError, UnknownError
  - Implementar extension function `Throwable.toSyncError()`
  - _Requirements: 4.6, 5.1, 5.2_

- [ ] 4. Implementar ColetaSyncWorker
  - Criar classe `ColetaSyncWorker` estendendo `CoroutineWorker`
  - Adicionar anotação `@HiltWorker` e injetar dependências via `@AssistedInject`
  - Injetar `SincronizarColetasPendentesUseCase`, `NotificationUtils`, `SyncLogger`


  - Implementar método `doWork()` com try-catch
  - Chamar `syncLogger.logStart()` no início
  - Executar `sincronizarColetasPendentesUseCase()`
  - Tratar resultado: success → notificar e retornar Result.success(), failure → verificar retry
  - Implementar método `shouldRetry(error: Throwable)` para decidir retry baseado no tipo de erro
  - Adicionar constantes: WORK_NAME, WORK_NAME_MANUAL, WORK_NAME_BY_COUNT
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ] 5. Implementar SyncScheduler Use Case
  - Criar classe `SyncScheduler` com injeção de `Context` e `PreferencesManager`
  - Obter instância do `WorkManager`
  - Implementar método `schedulePeriodicSync()`:
    - Ler intervalo e preferências (Wi-Fi only, battery saver)

    - Criar `Constraints` com NetworkType e BatteryNotLow
    - Criar `PeriodicWorkRequest` com intervalo configurado e flex de 15min
    - Configurar backoff exponencial (30s inicial)
    - Enfileirar com `enqueueUniquePeriodicWork` usando REPLACE policy
  - Implementar método `cancelPeriodicSync()` para cancelar work periódico
  - Implementar método `syncNow(byCount: Boolean)` para sincronização imediata
  - Implementar método `checkCounterSync()` para verificar se atingiu limite de coletas
  - Implementar método `incrementCollectionCount()` para incrementar contador após coleta
  - _Requirements: 1.4, 1.5, 2.5, 3.2, 3.3, 7.1, 7.2, 7.3_

- [ ] 6. Implementar NotificationUtils
  - Criar classe `NotificationUtils` com injeção de `Context`
  - Obter `NotificationManager` via getSystemService
  - Implementar método `createNotificationChannel()` no init
  - Implementar método `showSyncSuccess(stats: SyncStats)`:
    - Criar notificação com título "Sincronização concluída"


    - Exibir quantidade de coletas sincronizadas

    - Adicionar PendingIntent para abrir app
    - Prioridade LOW, auto-cancel
  - Implementar método `showSyncError(error: Throwable)`:
    - Criar notificação com título "Erro na sincronização"
    - Exibir mensagem de erro
    - Adicionar action "Tentar novamente" com PendingIntent
    - Prioridade HIGH, auto-cancel
  - Adicionar constantes: CHANNEL_ID, NOTIFICATION_ID_SUCCESS, NOTIFICATION_ID_ERROR
  - _Requirements: 8.1, 8.2, 8.3, 8.4_



- [ ] 7. Atualizar PreferencesManager com novas configurações
  - Adicionar constantes em `SyncPreferences`: KEY_AUTO_SYNC_ENABLED, KEY_SYNC_INTERVAL, KEY_WIFI_ONLY, KEY_AUTO_SYNC_BY_COUNT, KEY_SYNC_COLLECTION_INTERVAL, KEY_COLLECTION_COUNT, KEY_BATTERY_SAVER, KEY_LAST_SYNC
  - Implementar getter/setter para `autoSyncEnabled: Boolean`
  - Implementar getter/setter para `syncInterval: Int` (default 30 minutos)
  - Implementar getter/setter para `wifiOnlyEnabled: Boolean`
  - Implementar getter/setter para `autoSyncByCountEnabled: Boolean`
  - Implementar getter/setter para `syncCollectionInterval: Int` (default 10 coletas)
  - Implementar getter/setter para `collectionCount: Int`
  - Implementar getter/setter para `batterySaverEnabled: Boolean`
  - Implementar getter/setter para `lastSyncTimestamp: Long`
  - Implementar método `resetCollectionCount()`
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 2.7, 3.1, 7.6_


- [-] 8. Integrar SyncScheduler com SettingsActivity

  - Injetar `SyncScheduler` no `SettingsViewModel`
  - Quando switch de sincronização por tempo é alterado:
    - Se habilitado: chamar `syncScheduler.schedulePeriodicSync()`
    - Se desabilitado: chamar `syncScheduler.cancelPeriodicSync()`


  - Quando intervalo de sincronização é alterado:

    - Salvar no PreferencesManager
    - Reagendar chamando `syncScheduler.schedulePeriodicSync()`
  - Quando switch Wi-Fi only é alterado:
    - Salvar no PreferencesManager
    - Reagendar chamando `syncScheduler.schedulePeriodicSync()`
  - Quando botão "Testar Sincronização" é clicado:
    - Chamar `syncScheduler.syncNow(byCount = false)`
    - Observar resultado via WorkInfo
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 7.1, 7.2_

- [ ] 9. Integrar contador de coletas com ColetaActivity
  - Injetar `SyncScheduler` no `ColetaViewModelClean`
  - Após registrar coleta com sucesso:
    - Chamar `syncScheduler.incrementCollectionCount()`
    - Verificar se deve sincronizar via `syncScheduler.checkCounterSync()`
  - _Requirements: 2.4, 2.5, 2.6_

- [ ] 10. Inicializar sincronização no Application
  - No `InventarioMobileApplication.onCreate()`:
    - Injetar `SyncScheduler`
    - Verificar se sincronização por tempo está habilitada
    - Se sim, chamar `syncScheduler.schedulePeriodicSync()`
  - _Requirements: 7.2_

- [ ] 11. Implementar tela de histórico de sincronizações
  - Criar `SyncHistoryActivity` com RecyclerView
  - Criar `SyncHistoryAdapter` para exibir logs
  - Criar `SyncHistoryViewModel` com LiveData de `List<SincronizacaoEntity>`
  - Buscar logs via `SincronizacaoDao.buscarTodas()` ordenado por data DESC
  - Exibir: data/hora, tipo, status, mensagem, estatísticas
  - Adicionar botão para exportar logs
  - _Requirements: 10.4, 10.5_

- [ ]* 12. Implementar DatabaseCleanupWorker
  - Criar `DatabaseCleanupWorker` estendendo `CoroutineWorker`
  - Adicionar anotação `@HiltWorker`
  - Injetar `ColetaDao` e `SincronizacaoDao`
  - Implementar limpeza de coletas sincronizadas com mais de 7 dias
  - Implementar limpeza de logs de sincronização mantendo últimas 50
  - Agendar execução diária via `PeriodicWorkRequest`
  - _Requirements: 12.4, 12.5_

- [ ]* 13. Adicionar constraint de bateria
  - No `SyncScheduler.schedulePeriodicSync()`:
    - Verificar se `batterySaverEnabled` está true
    - Se sim, adicionar `setRequiresBatteryNotLow(true)` nos constraints
  - Implementar lógica para reduzir frequência em modo economia de bateria
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ]* 14. Implementar sincronização em lote (batch)
  - Atualizar `ColetaApi` com endpoint `POST /coletas/batch`
  - Criar DTO `BatchColetaRequest` com lista de coletas
  - Criar DTO `BatchColetaResponse` com lista de resultados
  - Atualizar `ColetaSyncWorker` para enviar coletas em lotes de 10
  - Processar resposta individual de cada coleta no lote
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ]* 15. Adicionar testes unitários
  - Testar `SyncScheduler`:
    - Teste de agendamento periódico
    - Teste de cancelamento
    - Teste de sincronização imediata
    - Teste de incremento de contador
  - Testar `ColetaSyncWorker`:
    - Teste de sincronização bem-sucedida
    - Teste de retry em caso de falha
    - Teste de shouldRetry com diferentes erros
  - Testar `SincronizarColetasPendentesUseCase`:
    - Teste com lista vazia
    - Teste com múltiplas coletas
    - Teste com falhas parciais
  - _Requirements: Todos_

- [ ]* 16. Adicionar testes de integração
  - Testar WorkManager com TestDriver
  - Testar execução periódica
  - Testar constraints (Wi-Fi, bateria)
  - Testar retry policy
  - _Requirements: Todos_

## Notes

- Tasks marcados com `*` são opcionais e podem ser implementados posteriormente
- Priorizar tasks 1-10 para MVP funcional
- Tasks 11-16 são melhorias e otimizações
- Seguir ordem sequencial para evitar dependências quebradas
- Cada task deve ser testado individualmente antes de prosseguir

## Estimated Effort

| Task | Complexity | Estimated Time |
|------|-----------|----------------|
| 1-3  | Low       | 2 hours        |
| 4-6  | Medium    | 4 hours        |
| 7-10 | Medium    | 3 hours        |
| 11   | Low       | 2 hours        |
| 12-14| Medium    | 4 hours        |
| 15-16| High      | 6 hours        |

**Total MVP (tasks 1-10):** ~9 hours
**Total Complete (all tasks):** ~21 hours
