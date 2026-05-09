# Implementation Plan: dashboard-refactor-clean

## Overview

Refactor técnico do Android `InventarioMobile` que consolida dois `ViewModel`s da dashboard em `DashboardViewModelClean`, substitui o duplo collector por uma única `FonteEstatisticas: StateFlow<DashboardStats>` (via `stateIn(WhileSubscribed(5000))`), e implementa `DashboardRepositoryImpl.buscarEstatisticasLocais` real lendo Room + `CacheServerStats` persistido no `PreferencesManager`.

Linguagem de implementação: **Kotlin** (conforme design e stack Android do projeto). Biblioteca de property-based testing: **Kotest Property** (`io.kotest:kotest-property`).

Regras invioláveis ao longo das tasks:
- Não alterar nenhuma URL da `ApiService` — o prefixo `api/mobile/` **deve** ser preservado (Req 5.1, `endpoints-nao-alterar.md`).
- `DashboardViewModelClean` injeta a interface `DashboardRepository` do pacote `domain`, nunca a classe `DashboardRepositoryImpl` (Req 1.6, 5.4, `clean-architecture.md`).
- Somente `AppDatabase` (`inventario_offline.db`). Nada de `InventarioDatabase` depreciada.
- Todas as novas Activities/Fragments migradas usam `@AndroidEntryPoint`; ViewModels usam `@HiltViewModel`.
- Não alterar schema Room (Req 3.14).

## Tasks

- [x] 1. Preparar dependências e modelos de domínio
  - [x] 1.1 Adicionar dependências Kotest Property Testing ao `InventarioMobile/app/build.gradle`
    - Adicionar em `dependencies { ... }` o bloco:
      ```gradle
      testImplementation "io.kotest:kotest-property:5.8.0"
      testImplementation "io.kotest:kotest-runner-junit5:5.8.0"
      testImplementation "io.kotest:kotest-assertions-core:5.8.0"
      ```
    - Garantir `useJUnitPlatform()` no `android.testOptions.unitTests.all { }` (ou equivalente do módulo) para que Kotest rode
    - Rodar `.\gradlew.bat :app:dependencies --configuration testRuntimeClasspath` apenas para verificar resolução
    - _Requirements: 7.4, 7.5_

  - [x] 1.2 Atualizar `domain/model/DashboardStats.kt` com novo campo e invariantes
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/model/DashboardStats.kt`
    - Acrescentar `val timestampUltimaSincronizacao: Long? = null` como novo parâmetro
    - Atualizar `companion object.empty(...)` para aceitar `isOfflineData` (default `false`) e propagar `timestampUltimaSincronizacao = null`
    - Adicionar bloco `init { require(totalColetados >= 0); require(totalPendentes >= 0); require(totalPatrimonios >= 0) }`
    - Manter todos os campos existentes (Req 5.3 — DTO e mapper não mudam)
    - _Requirements: 2.12, 3.6, 4.3, 4.4_

  - [x] 1.3 Criar `data/model/CacheServerStats.kt`
    - Arquivo novo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/model/CacheServerStats.kt`
    - Data class POJO com campos `totalPatrimonios: Int`, `totalColetados: Int`, `divergencias: Int`, `coletoresAtivos: Int`, `valorTotal: Double`, `inventarioNome: String?`, `inventarioId: Int?`, `timestamp: Long` (ms UTC)
    - _Requirements: 3.6, 3.8_

- [x] 2. Estender `PreferencesManager` para persistir `CacheServerStats`
  - [x] 2.1 Acrescentar métodos `saveCacheServerStats`, `getCacheServerStats`, `getCacheServerStatsTimestamp`, `clearCacheServerStats` em `util/PreferencesManager.kt`
    - Usar as chaves `cache_server_total_patrimonios`, `cache_server_total_coletados`, `cache_server_divergencias`, `cache_server_coletores_ativos`, `cache_server_valor_total`, `cache_server_inventario_nome`, `cache_server_inventario_id`, `cache_server_timestamp` no `EncryptedSharedPreferences` já existente
    - `getCacheServerStats()` retorna `null` quando `cache_server_timestamp == 0L` (primeira execução) — Req 3.6 e 3.9 dependem disso
    - `saveCacheServerStats(stats: DashboardStats)` persiste `System.currentTimeMillis()` em `cache_server_timestamp`
    - `clearCacheServerStats()` remove todas as chaves `cache_server_*`
    - _Requirements: 3.6, 3.8, 3.9_

  - [ ]* 2.2 Escrever `CacheServerStatsExampleTest` (exemplos de roundtrip)
    - Arquivo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/CacheServerStatsExampleTest.kt`
    - Casos: (a) `getCacheServerStats` retorna `null` antes de qualquer save; (b) roundtrip `save → get` preserva todos os campos; (c) `clearCacheServerStats` faz `get` voltar a `null`
    - _Requirements: 3.6, 3.9, 3.10_

- [ ] 3. Property-based tests das invariantes puras de `DashboardStats`
  - [ ]* 3.1 Property test P2 — Invariantes estruturais de `DashboardStats`
    - Arquivo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/DashboardStatsPropertiesTest.kt`
    - **Property 2: Invariantes estruturais do DashboardStats**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 2: Non-negativity invariants of DashboardStats emissions`
    - Gerar `Arb.int(Int.MIN_VALUE..Int.MAX_VALUE)` para `totalColetados`, `totalPendentes`, `totalPatrimonios`; assertar que `require` lança para negativos e aceita ≥ 0
    - Mínimo 100 iterações
    - **Validates: Requirements 2.12**

  - [ ]* 3.2 Property test P6 — Invariante de soma
    - Arquivo: mesmo `DashboardStatsPropertiesTest.kt`
    - **Property 6: Sum invariant (totalColetados + totalPendentes == totalPatrimonios when totalPatrimonios > 0)**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 6: totalColetados + totalPendentes == totalPatrimonios`
    - Para qualquer `DashboardStats` construído via o construtor canônico do refactor (usando `totalPendentes = max(0, totalPatrimonios - totalColetados)`), verificar a invariante
    - Mínimo 100 iterações; cobrir `totalPatrimonios = 0` explicitamente
    - **Validates: Requirements 2.10, 3.13**

  - [ ]* 3.3 Property test P7 — Fórmula de `percentualConclusao`
    - Arquivo: mesmo `DashboardStatsPropertiesTest.kt`
    - **Property 7: percentualConclusao == (totalColetados * 100.0) / totalPatrimonios rounded to 2 decimals**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 7: Percentage formula correctness`
    - `Arb.int(0..1_000_000)` para `totalPatrimonios`, `Arb.int(0..totalPatrimonios)` para `totalColetados`; verificar `|percentual - esperado| <= 0.01`
    - **Validates: Requirements 2.11, 3.2**

- [x] 4. Reimplementar `DashboardRepositoryImpl` (offline real + cache do servidor)
  - [x] 4.1 Injetar `PatrimonioDao`, `ColetaDao`, `PreferencesManager` e persistir `CacheServerStats` em `buscarEstatisticas` bem-sucedido
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/DashboardRepositoryImpl.kt`
    - Acrescentar ao construtor Hilt: `patrimonioDao: PatrimonioDao`, `coletaDao: ColetaDao`, `preferencesManager: PreferencesManager`
    - Verificar `DashboardModule`/`RepositoryModule` em `di/` — o binding é da interface `DashboardRepository` (não alterar, só confirmar)
    - No caminho de sucesso de `buscarEstatisticas(inventarioId)`: chamar `preferencesManager.saveCacheServerStats(stats.copy(timestampUltimaSincronizacao = System.currentTimeMillis(), isOfflineData = false))`
    - Manter endpoints `api/mobile/dashboard/stats` (com e sem `?inventarioId=`) inalterados (Req 5.1)
    - _Requirements: 3.8, 5.1, 5.5_

  - [x] 4.2 Implementar `buscarEstatisticasLocais(inventarioId)` real
    - Mesmo arquivo `DashboardRepositoryImpl.kt`
    - `runCatching { ... }.recoverCatching { ... }` conforme design (seção "DashboardRepositoryImpl")
    - Ordem: `cache?.totalPatrimonios ?: patrimonioDao.countAll() ?: 0` para `totalPatrimonios`; `coletaDao.buscarTodas(invId).distinctBy { it.idPatrimonio }.size` para `totalColetados`; `totalPendentes = max(0, totalPatrimonios - totalColetados)`
    - Percentual: `roundTo2((totalColetados * 100.0) / totalPatrimonios)` quando `> 0`, senão `0.0`
    - Sempre `isOfflineData = true`; `timestampUltimaSincronizacao = cache?.timestamp`
    - `recoverCatching` captura `SQLiteException` e retorna `DashboardStats.empty(inventarioId, isOfflineData = true)` + log via `Log.e(TAG, ...)`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.9, 3.10, 3.11, 3.13_

  - [x] 4.3 Refatorar `observarEstatisticasHibridas(inventarioId)` com `combine + flatMapLatest + catch`
    - Mesmo arquivo `DashboardRepositoryImpl.kt`
    - Pipeline: `flow { val base = buscarEstatisticas(id).getOrThrow(); emitAll(dashboardDao.observarTotalColetas(invId).map { base + totalLocais }) }.catch { emit(local fallback); emitAll(dashboardDao.observarTotalColetas + local.copy(isOfflineData=true)) }`
    - Aplicar invariante `totalPendentes = max(0, totalPatrimonios - coletadosAtualizado)` em cada emissão
    - Garantir que, em falha do servidor, **uma** emissão imediata de `buscarEstatisticasLocais` precede as emissões reativas do Room (Req 2.9)
    - _Requirements: 2.8, 2.9, 2.10, 2.11, 3.12, 3.13_

  - [ ]* 4.4 Property test P8 — Contrato offline e fallback de rede
    - Arquivo novo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/DashboardRepositoryImplPropertiesTest.kt`
    - **Property 8: Offline contract — on network failure, fonteEstatisticas emission equals buscarEstatisticasLocais and has isOfflineData=true**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 8: Offline fallback contract`
    - Gerar exceção via `Arb.choice(IOException, SocketTimeoutException, UnknownHostException, HttpException(400..599))`; assertar que `observarEstatisticasHibridas(invId).first()` equivale ao `buscarEstatisticasLocais(invId).getOrThrow()` (campos), com `isOfflineData == true`
    - Usar `runTest` + `TestCoroutineScheduler`
    - **Validates: Requirements 2.9, 3.5, 3.7**

  - [ ]* 4.5 Property test P9 — Fonte de `totalPatrimonios` em modo offline
    - Arquivo: mesmo `DashboardRepositoryImplPropertiesTest.kt`
    - **Property 9: Source of totalPatrimonios — CacheServerStats > PatrimonioDao.countAll() > 0**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 9: totalPatrimonios source precedence in offline mode`
    - Três cenários em um único property generator:
      - cache não vazio → `totalPatrimonios == cache.totalPatrimonios`
      - cache vazio + `patrimonioDao.countAll() = N` → `totalPatrimonios == N`
      - cache vazio + `patrimonioDao.countAll() = 0` → `totalPatrimonios == 0 ∧ percentualConclusao == 0.0 ∧ isOfflineData == true`
    - Mínimo 100 iterações
    - **Validates: Requirements 3.3, 3.9, 3.10**

  - [ ]* 4.6 Property test P10 — `totalColetados` como distinct `idPatrimonio`
    - Arquivo: mesmo `DashboardRepositoryImplPropertiesTest.kt`
    - **Property 10: totalColetados equals count of distinct idPatrimonio in ColetaDao.buscarTodas(invId)**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 10: distinct idPatrimonio count`
    - Gerar `Arb.list(Arb.coletaEntity(), 0..100)` com `idPatrimonio ∈ [1, 10]` e `sincronizado ∈ {true, false}` para gerar duplicatas deliberadamente
    - Assertar `buscarEstatisticasLocais(invId).totalColetados == list.distinctBy { it.idPatrimonio }.size`
    - **Validates: Requirements 3.4**

  - [ ]* 4.7 Property test P11 — Persistência de `CacheServerStats` após sucesso do servidor
    - Arquivo: mesmo `DashboardRepositoryImplPropertiesTest.kt`
    - **Property 11: After successful buscarEstatisticas, getCacheServerStats reflects DTO and timestamp ≥ t0**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 11: CacheServerStats persistence after successful server fetch`
    - Gerar `Arb.dashboardStatsDto()` (todos os campos válidos); capturar `t0 = System.currentTimeMillis()`, chamar `buscarEstatisticas(invId)`, e verificar igualdade campo-a-campo em `getCacheServerStats()` + `timestamp ≥ t0`
    - **Validates: Requirements 3.6, 3.8**

  - [ ]* 4.8 Property test P12 — Segurança sob `SQLiteException`
    - Arquivo: mesmo `DashboardRepositoryImplPropertiesTest.kt`
    - **Property 12: SQLiteException in any DAO never propagates; returns zeros with isOfflineData=true**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 12: SQLiteException safety`
    - Mockar `PatrimonioDao`/`ColetaDao`/`DashboardDao` com `Arb.choice(true, false)` para cada query lançar `SQLiteException`; assertar que `buscarEstatisticasLocais(invId)` sempre retorna `Result.success(DashboardStats(0, 0, 0, 0.0, ..., isOfflineData=true))` e nunca propaga exceção
    - **Validates: Requirements 3.11**

- [x] 5. Checkpoint - Ensure all repository tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Refatorar `DashboardViewModelClean` com `FonteEstatisticas`
  - [x] 6.1 Expor `fonteEstatisticas: StateFlow<DashboardStats>` única via `stateIn(WhileSubscribed(5000))`
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardViewModelClean.kt`
    - Confirmar construtor Hilt recebendo `dashboardRepository: DashboardRepository` (interface), nunca `DashboardRepositoryImpl` (Req 1.6, 5.4)
    - Declarar `private val inventarioIdFlow: StateFlow<Int?> = MutableStateFlow(preferencesManager.getInventarioAtivoId())`
    - Declarar `private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply { tryEmit(Unit) }` — Req 2.6
    - Construir `fonteEstatisticas = combine(inventarioIdFlow, refreshTrigger) { id, _ -> id }.distinctUntilChanged().flatMapLatest { dashboardRepository.observarEstatisticasHibridas(it) }.catch { emit(fallback) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardStats.empty(...))` — Req 2.1, 2.2, 2.9, 6.1–6.3
    - Implementar `fun refresh() { refreshTrigger.tryEmit(Unit) }` — Req 2.5
    - Remover exposição pública separada de `observarEstatisticasHibridas` no ViewModel (Req 2.4) — Fragment e OverviewFragment não devem conseguir coletá-lo como Flow distinto
    - Manter `evolucaoState` separado (gráfico de evolução não é parte da FonteEstatisticas)
    - _Requirements: 1.6, 2.1, 2.2, 2.4, 2.5, 2.6, 2.7, 2.9, 5.4, 6.1, 6.2, 6.3, 6.4_

  - [ ]* 6.2 Property test P3 — Deduplicação por `WhileSubscribed(5000)`
    - Arquivo novo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/FonteEstatisticasPropertiesTest.kt`
    - **Property 3: Within a 5000 ms subscription window without PullToRefresh, HTTP calls to dashboard/stats == 1**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 3: WhileSubscribed(5000) deduplication`
    - Gerar sequências de eventos `{subscribe, cancel, rotate, navigate}` com timings `Arb.int(0..4999)` mantendo assinatura ativa; usar `MockWebServer` para contar hits em `/api/mobile/dashboard/stats*`
    - Usar `runTest` + `TestCoroutineScheduler.advanceTimeBy` para simular janela sem tempo real
    - **Validates: Requirements 2.7, 6.1, 6.2, 6.3**

  - [ ]* 6.3 Property test P4 — Deduplicação de PullToRefresh concorrente
    - Arquivo: mesmo `FonteEstatisticasPropertiesTest.kt`
    - **Property 4: N consecutive refresh() calls produce ≤ 1 active concurrent HTTP call (flatMapLatest cancels prior)**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 4: Concurrent PullToRefresh deduplication`
    - `Arb.int(2..20)` para `N`; disparar `N` refreshes dentro do tempo de resposta do mock; assertar que o número máximo de respostas HTTP concorrentes ativas observadas ≤ 1
    - **Validates: Requirements 2.5, 2.6**

  - [ ]* 6.4 Property test P5 — Reatividade de coleta local sem rede adicional
    - Arquivo: mesmo `FonteEstatisticasPropertiesTest.kt`
    - **Property 5: Inserting a ColetaEntity causes fonteEstatisticas emission within 1000 ms with no extra HTTP calls**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 5: Local coleta reactivity without HTTP`
    - Gerar `Arb.coletaEntity(invId=invId, sincronizado=false)`, inserir via `ColetaDao.insert`; capturar `httpCallCountBefore`; `advanceTimeBy(1000)`; assertar nova emissão com `totalColetados` incrementado e `httpCallCountAfter == httpCallCountBefore`
    - **Validates: Requirements 2.8, 3.12**

  - [ ]* 6.5 Property test P13 — Idempotência de invalidação do `CacheServerStats`
    - Arquivo: mesmo `FonteEstatisticasPropertiesTest.kt`
    - **Property 13: Double invalidate_cache produces same final DashboardStats as single invalidate_cache**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 13: Cache invalidation idempotence`
    - `Arb.list(Arb.choice("refresh", "coleta_local", "invalidar_cache"), 1..50)`; aplicar sequência e a variante onde toda subsequência `[invalidar, invalidar]` é reduzida a `[invalidar]`; assertar que o último `DashboardStats` emitido é idêntico
    - **Validates: Requirements 7.5**

  - [ ]* 6.6 Property test P14 — Exatamente um collector sobre `fonteEstatisticas`
    - Arquivo: mesmo `FonteEstatisticasPropertiesTest.kt`
    - **Property 14: For K ∈ [1, 20] Fragment rotations, fonteEstatisticas.subscriptionCount.value == 1 while STARTED**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 14: Exactly one collector on fonteEstatisticas`
    - Usar Robolectric para simular rotações; observar `(fonteEstatisticas as StateFlow).subscriptionCount` (MutableStateFlow expõe via reflexão ou wrap) durante `STARTED`
    - **Validates: Requirements 2.4, 6.4**

- [x] 7. Refatorar `DashboardFragment` para collector único
  - [x] 7.1 Consumir apenas `viewModel.fonteEstatisticas` com `repeatOnLifecycle(STARTED)`
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardFragment.kt`
    - Remover qualquer `viewLifecycleOwner.lifecycleScope.launch { viewModel.observarEstatisticasHibridas(...)... }` e qualquer coleta separada de `viewModel.uiState` para os mesmos TextViews (Req 2.4)
    - Implementar um único `repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.fonteEstatisticas.collect { stats -> renderizarKpis(stats); atualizarIndicadorOffline(stats.isOfflineData); atualizarTimestampUltimaSync(stats.timestampUltimaSincronizacao) } }`
    - `binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }`; remover qualquer chamada HTTP direta do Fragment (Req 2.5)
    - `renderizarKpis` atualiza TextViews `total de patrimônios`, `total de coletados`, `total de pendentes`, `percentual de conclusão`, `divergências`, `coletores ativos` em uma única passagem (Req 2.3)
    - `atualizarTimestampUltimaSync(null)` mantém vazia/oculta a área de timestamp (Req 4.4, 4.5)
    - Garantir `@AndroidEntryPoint` permanece e `@HiltViewModel` é resolvido
    - _Requirements: 2.1, 2.3, 2.4, 2.5, 4.1, 4.2, 4.3, 4.4, 4.5, 6.5_

  - [ ]* 7.2 Example test `DashboardFragmentOfflineIndicatorTest` (Robolectric)
    - Arquivo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/DashboardFragmentOfflineIndicatorTest.kt`
    - Casos: (a) `isOfflineData=true` → indicador visível em ≤ 500 ms; (b) `isOfflineData=false` → indicador oculto em ≤ 500 ms; (c) `timestampUltimaSincronizacao=null` → área de timestamp oculta sem exceção; (d) antes da primeira emissão → indicador oculto e timestamp ausente; (e) rotação preservando estado de erro/carregamento
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 6.5_

- [x] 8. Migrar `OverviewFragment` para `DashboardViewModelClean`
  - [x] 8.1 Substituir `DashboardViewModel` legado + `DashboardViewModelFactory` por `DashboardViewModelClean` via Hilt
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/statistics/OverviewFragment.kt`
    - Adicionar `@AndroidEntryPoint` no Fragment
    - Substituir a construção manual do ViewModel por `private val viewModel: DashboardViewModelClean by viewModels()`
    - Remover imports de `com.inventario.mobile.presentation.dashboard.DashboardViewModel` e `DashboardViewModelFactory`
    - Remover qualquer referência a `InventarioRepository` stub para dashboard (usar apenas `DashboardRepository` via ViewModel)
    - Coletar `viewModel.fonteEstatisticas` em `onViewCreated` usando `repeatOnLifecycle(STARTED)` — mesma fonte única que o `DashboardFragment`
    - `updateUI(stats)` preenche todos os campos de UI do OverviewFragment preservando paridade exata: `totalPatrimonios`, `totalColetados`, `totalPendentes`, `divergencias`, `coletoresAtivos`, `percentualConclusao` (`%.1f%%`), `percentualPendente = 100.0 - percentualConclusao`, `valorTotal` (via `formatCurrency`), `valorMedio = if (totalPatrimonios>0) valorTotal/totalPatrimonios else 0.0`
    - `binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }`
    - _Requirements: 1.3, 1.4, 1.6, 1.7, 5.4_

  - [ ]* 8.2 Property test P1 — Paridade visual `OverviewFragment` vs legado
    - Arquivo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/OverviewParityPropertyTest.kt`
    - **Property 1: For any valid DashboardStatsDto, OverviewFragment fields match legacy within tolerance**
    - Cabeçalho: `// Feature: dashboard-refactor-clean, Property 1: OverviewFragment visual parity with legacy ViewModel`
    - Gerar `Arb.dashboardStatsDto()`; renderizar via `OverviewFragment` (Robolectric) com `DashboardViewModelClean` + stub MockWebServer; calcular valores esperados usando a fórmula do ViewModel legado (referenciar snapshot textual do legado arquivado em `testResources/legacy/DashboardViewModelLegacy.snapshot.kt` ou recomputar manualmente seguindo o contrato)
    - Assertar igualdade exata para inteiros e `|diff| ≤ 0.01` para `percentualConclusao`, `valorTotal`, `valorMedio`, `percentualPendente`
    - **Validates: Requirements 1.4, 7.6**

- [x] 9. Remover código legado
  - [x] 9.1 Deletar `presentation/dashboard/DashboardViewModel.kt`
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardViewModel.kt`
    - Pré-condição: tarefa 8.1 concluída (nenhum consumidor restante do ViewModel legado)
    - Após remoção, buscar imports remanescentes: `grep_search` por `com\.inventario\.mobile\.presentation\.dashboard\.DashboardViewModel` (sem `Clean`) em todo `InventarioMobile/app/src/main/java/` — resultado esperado: zero matches (Req 1.7)
    - Buscar também a `data class DashboardStats` interna ao arquivo (garantir que não há segunda definição em `presentation/dashboard/`) — resultado esperado: zero ocorrências de `data class DashboardStats` fora de `domain/model/` (Req 1.1)
    - _Requirements: 1.1, 1.2, 1.7, 5.8_

  - [x] 9.2 Deletar `presentation/dashboard/DashboardViewModelFactory.kt`
    - Arquivo: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardViewModelFactory.kt`
    - Pré-condição: tarefa 9.1 concluída
    - Buscar imports remanescentes de `DashboardViewModelFactory` — resultado esperado: zero matches (Req 1.2, 1.7)
    - _Requirements: 1.2, 1.7, 5.8_

- [ ] 10. Testes estruturais e de contrato
  - [ ]* 10.1 Smoke test `DashboardMigrationSmokeTest`
    - Arquivo: `InventarioMobile/app/src/androidTest/java/com/inventario/mobile/dashboard/DashboardMigrationSmokeTest.kt` (usa Konsist ou reflexão + `File.walk` sobre `src/main/java`)
    - Casos:
      - `data class DashboardStats` só existe em `com.inventario.mobile.domain.model` (Req 1.1)
      - Arquivos `DashboardViewModel.kt` e `DashboardViewModelFactory.kt` não existem em `presentation/dashboard/` (Req 1.2)
      - `DashboardViewModelClean` construtor não recebe `DashboardRepositoryImpl` concreto (Req 1.6, 5.4)
      - `DashboardRepositoryImpl` permanece em `data/repository/` e `DashboardRepository` em `domain/repository/` (Req 5.5, 5.6)
      - Nenhum arquivo em `src/main/java` importa `com.inventario.mobile.data.repository.DashboardRepositoryImpl` fora de `di/*` (Req 5.4)
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 1.6, 1.7, 5.4, 5.5, 5.6_

  - [ ]* 10.2 Snapshot test `EndpointContractSnapshotTest`
    - Arquivo: `InventarioMobile/app/src/androidTest/java/com/inventario/mobile/dashboard/EndpointContractSnapshotTest.kt`
    - Usar reflexão sobre `ApiService`: assertar que `getDashboardStats` e `getDashboardStatsWithInventario` ainda existem, com path `api/mobile/dashboard/stats` preservado (Req 5.1)
    - Assertar que `DashboardStatsDto` (DTO de resposta) não teve campos adicionados, removidos ou renomeados comparado com snapshot textual `testResources/snapshots/DashboardStatsDto.snapshot` (Req 5.3)
    - _Requirements: 5.1, 5.2, 5.3, 3.14_

- [ ] 11. Testes de integração end-to-end (Espresso/Robolectric + MockWebServer)
  - [ ]* 11.1 Integration test — Exatamente uma chamada HTTP na inicialização
    - Arquivo: `InventarioMobile/app/src/test/java/com/inventario/mobile/dashboard/DashboardFragmentIntegrationTest.kt`
    - Stub do `MockWebServer` respondendo HTTP 200 em ≤ 2000 ms; abrir `DashboardFragment`; `advanceTimeBy(5000)`; assertar que `/api/mobile/dashboard/stats*` recebeu exatamente 1 requisição
    - _Requirements: 7.1_

  - [ ]* 11.2 Integration test — PullToRefresh dispara 1 chamada adicional
    - Arquivo: mesmo `DashboardFragmentIntegrationTest.kt`
    - Após inicialização + estabilização, disparar `SwipeRefreshLayout` via `onActivity { fragment -> fragment.binding.swipeRefresh.isRefreshing = true; ... }` ou evento Espresso; assertar exatamente 1 chamada adicional em janela de 5000 ms
    - _Requirements: 7.2, 2.5_

  - [ ]* 11.3 Integration test — 5 coletas offline, `isOfflineData=true`
    - Arquivo: mesmo `DashboardFragmentIntegrationTest.kt`
    - `MockWebServer` configurado para lançar `SocketTimeoutException` após 2000 ms em toda requisição; inserir 5 `ColetaEntity` em `AppDatabase.coletaDao()`; observar última emissão de `viewModel.fonteEstatisticas` em janela de 5000 ms; assertar `totalColetados >= 5 ∧ isOfflineData == true`
    - _Requirements: 7.3, 2.9, 3.1, 3.4, 3.5_

- [x] 12. Checkpoint final — Build e registro de build
  - Executar `.\gradlew.bat :app:assembleDebug` na pasta `InventarioMobile/` e verificar exit code 0 e ausência de "unresolved reference" para `com.inventario.mobile.presentation.dashboard.DashboardStats`, `com.inventario.mobile.presentation.dashboard.DashboardViewModel`, `com.inventario.mobile.presentation.dashboard.DashboardViewModelFactory` (Req 1.5).
  - Executar `.\gradlew.bat :app:testDebugUnitTest` e verificar que todos os property tests e exemplos acima passam.
  - Registrar nova entrada em `docs/historico_e_reunioes/BUILD_HISTORY.md` com `número da build`, `data`, `versionName`, `versionCode`, `caminho do APK`, `tamanho` e `lista de mudanças` conforme `BuildTracking_Rule` (Req 5.7).
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marcadas com `*` são opcionais (testes) e podem ser puladas para um MVP mais rápido; o core funcional fica nas tasks sem `*`.
- Cada property test é uma sub-task individual, anotada com o número da propriedade (P1–P14) e a cláusula de requisito que valida, conforme design.
- Tasks 4.1, 4.2, 4.3 modificam o mesmo arquivo (`DashboardRepositoryImpl.kt`) e por isso aparecem em ondas distintas no grafo abaixo — mesma regra vale para arquivos de teste compartilhados (`DashboardStatsPropertiesTest.kt`, `DashboardRepositoryImplPropertiesTest.kt`, `FonteEstatisticasPropertiesTest.kt`, `DashboardFragmentIntegrationTest.kt`).
- Checkpoints intermediários (5) e finais (12) permitem validação incremental sem bloquear o progresso.
- Property tests usam Kotest com `config(iterations = 100)` por default e `runTest` + `TestCoroutineScheduler` para determinismo temporal.
- A regra `endpoints-nao-alterar.md` é verificada automaticamente via snapshot test em 10.2 — qualquer PR que alterar URL é rejeitado.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1", "3.1"] },
    { "id": 2, "tasks": ["2.2", "3.2", "4.1"] },
    { "id": 3, "tasks": ["3.3", "4.2", "10.2"] },
    { "id": 4, "tasks": ["4.3"] },
    { "id": 5, "tasks": ["4.4", "6.1"] },
    { "id": 6, "tasks": ["4.5", "6.2", "7.1", "8.1"] },
    { "id": 7, "tasks": ["4.6", "6.3", "7.2", "8.2", "9.1"] },
    { "id": 8, "tasks": ["4.7", "6.4", "9.2", "11.1"] },
    { "id": 9, "tasks": ["4.8", "6.5", "10.1", "11.2"] },
    { "id": 10, "tasks": ["6.6", "11.3"] }
  ]
}
```
