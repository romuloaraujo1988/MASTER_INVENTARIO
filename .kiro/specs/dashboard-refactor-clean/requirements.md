# Requirements Document

## Introduction

Este spec descreve o refactor técnico da dashboard do app Android `InventarioMobile`, cobrindo os três débitos técnicos deixados em aberto no Build #102 (07/05/2026):

- **Item 1 (P7)**: consolidação dos dois ViewModels coexistentes (`DashboardViewModel` legado e `DashboardViewModelClean`) e das duas classes `DashboardStats` duplicadas em uma única fonte alinhada à Clean Architecture.
- **Item 2 (P1/P2)**: eliminação do duplo collector no `DashboardFragment`, que gera flickering e duplica requisições ao servidor, substituído por uma única fonte de verdade baseada em `StateFlow` exposto pelo ViewModel.
- **Item 3 (P6)**: implementação real das queries offline em `DashboardRepositoryImpl.buscarEstatisticasLocais`, que hoje é um stub retornando zeros e zera o dashboard quando o servidor está inacessível.

O objetivo final é uma dashboard tecnicamente mais simples, sem duplicações, com uma única fonte de verdade, comportamento offline correto e tela de Estatísticas (`OverviewFragment`) preservando o mesmo dado visível hoje. Nenhum endpoint de API, nenhum contrato HTTP e nenhum layout visível ao usuário final deve ser alterado.

## Glossary

- **DashboardFragment**: Fragment da tela inicial do app, anotado com `@AndroidEntryPoint`, que atualmente coleta `uiState` e `observarEstatisticasHibridas` em paralelo.
- **OverviewFragment**: Fragment da aba Estatísticas, que hoje usa `DashboardViewModel` legado com `DashboardViewModelFactory` e `InventarioRepository` stub.
- **DashboardViewModelClean**: ViewModel Hilt atual do `DashboardFragment`, baseado em Use Cases e `DashboardRepository`.
- **DashboardViewModel_Legado**: ViewModel `AndroidViewModel` legado definido em `presentation/dashboard/DashboardViewModel.kt`, usado apenas pelo `OverviewFragment`. Após esta feature, deve ser removido.
- **DashboardStats_Domain**: Data class `com.inventario.mobile.domain.model.DashboardStats` (fonte de verdade após o refactor).
- **DashboardStats_Legada**: Data class `com.inventario.mobile.presentation.dashboard.DashboardStats` (a ser removida).
- **DashboardRepository**: Interface em `domain/repository/DashboardRepository.kt` (contrato Clean Architecture).
- **DashboardRepositoryImpl**: Implementação em `data/repository/DashboardRepositoryImpl.kt`.
- **DashboardDao**: DAO Room já existente com consultas `observarEstatisticas`, `observarTotalPatrimonios` e `observarTotalColetas`.
- **ColetaDao**: DAO Room para a tabela `coleta`, com `contarPendentes`, `buscarTodas`, entre outras.
- **PatrimonioDao**: DAO Room para a tabela `patrimonio`, com `contarColetados` e queries relacionadas.
- **FonteEstatisticas**: StateFlow único exposto pelo ViewModel que combina dados do servidor com dados locais e representa a única origem dos KPIs do dashboard.
- **CacheServerStats**: Último valor de estatísticas do servidor persistido localmente com timestamp de sincronização (via `PreferencesManager` ou tabela Room dedicada).
- **PullToRefresh**: Ação do usuário via `SwipeRefreshLayout` do `DashboardFragment`.
- **KPI**: Indicadores exibidos na tela (total de patrimônios, coletados, pendentes, divergências, coletores ativos, percentual de conclusão).
- **ModoOffline**: Situação em que `NetworkMonitor.isConnected()` retorna `false` ou o servidor está inacessível.
- **Clean_Architecture_Rule**: Regra definida em `.kiro/steering/clean-architecture.md`: interfaces em domain, implementações em data, ViewModels injetam interfaces.
- **Endpoints_Rule**: Regra definida em `.kiro/steering/endpoints-nao-alterar.md`: nenhuma URL de API pode ser alterada.
- **BuildTracking_Rule**: Regra definida em `.kiro/steering/build-tracking.md`: toda build deve ser registrada em `docs/historico_e_reunioes/BUILD_HISTORY.md`.
- **Stakeholder_Coletor**: Usuário final do app que utiliza o `DashboardFragment` como tela inicial.
- **Stakeholder_Supervisor**: Usuário que utiliza o `OverviewFragment` na aba Estatísticas.

## Requirements

### Requirement 1: Consolidação do ViewModel e do model DashboardStats (Item 1 / P7)

**User Story:** Como desenvolvedor do projeto InventarioMobile, quero uma única implementação de ViewModel e um único tipo `DashboardStats` para a dashboard, para que não haja risco de import errado, código morto ou divergência de cálculo entre telas.

#### Acceptance Criteria

1. WHEN a feature `dashboard-refactor-clean` é concluída, THE busca recursiva por `data class DashboardStats` no diretório `InventarioMobile/app/src/main/java/` SHALL retornar exatamente uma ocorrência, localizada em `com.inventario.mobile.domain.model.DashboardStats`.
2. WHEN a feature `dashboard-refactor-clean` é concluída, THE caminho `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardViewModel.kt` SHALL não existir no repositório e THE caminho `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dashboard/DashboardViewModelFactory.kt` SHALL não existir no repositório.
3. WHEN o `OverviewFragment` é inicializado após a migração, THE `OverviewFragment` SHALL obter seu ViewModel via `@HiltViewModel` do tipo `DashboardViewModelClean` e SHALL NOT referenciar qualquer classe de nome `DashboardViewModel` sem o sufixo `Clean` nem qualquer instância de `DashboardViewModelFactory`.
4. WHEN o `OverviewFragment` é aberto após a migração com o mesmo inventário ativo e o mesmo stub de servidor usado antes do refactor, THE `OverviewFragment` SHALL exibir os campos `total de patrimônios`, `coletados`, `pendentes`, `divergências`, `coletores ativos`, `percentual de conclusão`, `valor total` e `valor médio` com igualdade exata nos campos inteiros e com diferença absoluta ≤ 0,01 nos campos de ponto flutuante (valor total, valor médio, percentual) em relação aos valores que seriam exibidos pelo `DashboardViewModel` legado consumindo o mesmo endpoint.
5. WHEN o comando `gradlew assembleDebug` é executado ao final desta feature, THE build SHALL retornar exit code 0 e THE output do compilador SHALL NOT conter erros de "unresolved reference" para os símbolos `com.inventario.mobile.presentation.dashboard.DashboardStats`, `com.inventario.mobile.presentation.dashboard.DashboardViewModel` ou `com.inventario.mobile.presentation.dashboard.DashboardViewModelFactory`.
6. THE `DashboardViewModelClean` SHALL declarar em seu construtor Hilt o parâmetro `dashboardRepository: com.inventario.mobile.domain.repository.DashboardRepository` e SHALL NOT declarar nenhum parâmetro de construtor cujo tipo seja `com.inventario.mobile.data.repository.DashboardRepositoryImpl`, conforme `Clean_Architecture_Rule`.
7. IF qualquer arquivo Kotlin no diretório `InventarioMobile/app/src/main/java/` declarar um `import com.inventario.mobile.presentation.dashboard.DashboardStats`, `import com.inventario.mobile.presentation.dashboard.DashboardViewModel` ou `import com.inventario.mobile.presentation.dashboard.DashboardViewModelFactory` após a migração, THEN THE compilação SHALL falhar com erro explícito apontando para o import não resolvido.

---

### Requirement 2: Fonte única de verdade para os KPIs no DashboardFragment (Item 2 / P1 / P2)

**User Story:** Como coletor usando o DashboardFragment como tela inicial, quero que os KPIs sejam atualizados de forma consistente a partir de uma única origem, para que não haja flickering quando a rede responde depois do Flow local e para que o pull-to-refresh não dispare requisições duplicadas ao servidor.

#### Acceptance Criteria

1. THE `DashboardViewModelClean` SHALL expor exatamente um `StateFlow<DashboardStats>` público como `FonteEstatisticas`, e THE `DashboardFragment` SHALL consumir exclusivamente esta `FonteEstatisticas` para renderização dos KPIs, sem consumir nenhum outro Flow ou LiveData alternativo para os mesmos valores.
2. THE `FonteEstatisticas` SHALL ser construída via operador `stateIn` com `SharingStarted.WhileSubscribed(5000)` (5000 ms de janela de persistência após o último subscriber ativo) sobre o fluxo híbrido (servidor base + Room Flow de coletas locais).
3. WHEN a `FonteEstatisticas` emite um novo valor, THE `DashboardFragment` SHALL atualizar em uma única passagem os TextViews correspondentes aos KPIs `total de patrimônios`, `total de coletados`, `total de pendentes` e `percentual de conclusão` para que eles reflitam os valores emitidos.
4. THE `DashboardFragment` SHALL conter exatamente um `collect` (ou `collectLatest`) sobre a `FonteEstatisticas` e SHALL NOT coletar `viewModel.uiState` e `viewModel.observarEstatisticasHibridas` em paralelo para o mesmo conjunto de TextViews após o refactor.
5. WHEN o usuário aciona PullToRefresh, THE `DashboardViewModelClean` SHALL invalidar o `CacheServerStats` e reemitir novos valores pela `FonteEstatisticas` sem que o `DashboardFragment` dispare uma segunda requisição HTTP direta ao servidor por esse mesmo PullToRefresh.
6. IF o usuário aciona PullToRefresh enquanto já existe uma requisição de refresh em andamento originada do mesmo ciclo de inscrição, THEN THE `DashboardViewModelClean` SHALL deduplicar a segunda solicitação e SHALL NOT disparar uma nova chamada HTTP concorrente ao endpoint base.
7. WHEN o `DashboardFragment` é criado a partir de uma navegação ou rotação de tela dentro da janela de 5000 ms de inscrição ativa, THE `DashboardViewModelClean` SHALL disparar no máximo uma chamada ao endpoint base do servidor por ciclo de inscrição da `FonteEstatisticas`.
8. WHILE a `FonteEstatisticas` está ativa e uma nova coleta local é registrada em Room, THE `DashboardFragment` SHALL receber um novo valor de `DashboardStats` refletindo o incremento em `totalColetados` em até 1000 ms após o commit da transação Room, sem requisição adicional ao servidor.
9. IF a chamada ao endpoint base do servidor falhar com `IOException`, `SocketTimeoutException`, `UnknownHostException` ou resposta HTTP de erro (status ≥ 400), THEN THE `FonteEstatisticas` SHALL continuar emitindo valores baseados em `buscarEstatisticasLocais` (ver Requirement 3) com `isOfflineData = true`, e THE `DashboardFragment` SHALL exibir visualmente o indicador de modo offline (ver Requirement 4).
10. THE `FonteEstatisticas` SHALL preservar a invariante: para qualquer emissão `stats`, `stats.totalColetados + stats.totalPendentes == stats.totalPatrimonios` sempre que `stats.totalPatrimonios > 0`.
11. THE `FonteEstatisticas` SHALL preservar a invariante: para qualquer emissão `stats`, `stats.percentualConclusao` é igual a `(stats.totalColetados * 100.0) / stats.totalPatrimonios` quando `stats.totalPatrimonios > 0`, e igual a `0.0` quando `stats.totalPatrimonios == 0`.
12. THE `FonteEstatisticas` SHALL preservar a invariante: `stats.totalColetados >= 0`, `stats.totalPendentes >= 0`, `stats.totalPatrimonios >= 0`.

---

### Requirement 3: Estatísticas offline reais a partir do Room (Item 3 / P6)

**User Story:** Como coletor trabalhando em campo sem rede, quero que os KPIs da dashboard reflitam as coletas que já fiz localmente, para que o dashboard não zere no modo offline e eu mantenha visibilidade do meu progresso.

#### Acceptance Criteria

1. THE `DashboardRepositoryImpl` SHALL implementar `buscarEstatisticasLocais(inventarioId)` calculando `totalColetados`, `totalPendentes` e `percentualConclusao` exclusivamente a partir de queries Room (`PatrimonioDao`, `ColetaDao`, `DashboardDao`), sem realizar chamadas HTTP.
2. THE `DashboardRepositoryImpl` SHALL calcular `percentualConclusao` como `(totalColetados * 100.0) / totalPatrimonios` arredondado a 2 casas decimais quando `totalPatrimonios > 0`, e como `0.0` quando `totalPatrimonios == 0`.
3. THE `DashboardRepositoryImpl` SHALL obter `totalPatrimonios` em `buscarEstatisticasLocais` a partir do `CacheServerStats` (última sincronização bem-sucedida do endpoint base) quando disponível, e a partir de `PatrimonioDao.countAll()` (ou equivalente Room já existente) como fallback quando o cache estiver vazio.
4. WHEN `buscarEstatisticasLocais(inventarioId)` é chamado e existem coletas locais registradas em Room para o inventário, THE `DashboardRepositoryImpl` SHALL computar `totalColetados` como a quantidade de `idPatrimonio` distintos observados em `ColetaDao.buscarTodas(inventarioId)`, evitando contagem duplicada quando o mesmo `idPatrimonio` aparecer em múltiplas linhas com `sincronizado` diferente.
5. THE `DashboardRepositoryImpl` SHALL definir `isOfflineData = true` em todo `DashboardStats` retornado por `buscarEstatisticasLocais`.
6. THE `DashboardRepositoryImpl` SHALL incluir em `DashboardStats` retornado por `buscarEstatisticasLocais` o timestamp da última sincronização bem-sucedida do servidor, representado como milissegundos desde a epoch Unix, obtido do `CacheServerStats`; WHEN o `CacheServerStats` estiver vazio, THE campo SHALL ser `null`.
7. WHEN `buscarEstatisticas(inventarioId)` lança `IOException`, `HttpException`, `SocketTimeoutException` ou `UnknownHostException`, THE `DashboardRepositoryImpl` SHALL retornar o resultado de `buscarEstatisticasLocais(inventarioId)` como fallback à camada chamadora.
8. THE `DashboardRepositoryImpl` SHALL persistir o `CacheServerStats` após cada chamada bem-sucedida a `buscarEstatisticas(inventarioId)`, registrando pelo menos `totalPatrimonios`, `totalColetados`, `divergencias`, `coletoresAtivos`, `valorTotal` e o timestamp da sincronização.
9. IF o `CacheServerStats` ainda não existe (primeira execução do app sem nenhuma sincronização bem-sucedida) E o app está em `ModoOffline`, THEN THE `DashboardRepositoryImpl` SHALL retornar `DashboardStats` com `totalPatrimonios` igual ao resultado de `PatrimonioDao.countAll()`, `isOfflineData = true` e timestamp `null`.
10. WHEN tanto o `CacheServerStats` está vazio quanto `PatrimonioDao.countAll()` retorna 0, THE `DashboardRepositoryImpl` SHALL retornar `DashboardStats` com todos os contadores zerados, `percentualConclusao = 0.0` e `isOfflineData = true`.
11. IF qualquer query Room invocada por `buscarEstatisticasLocais` lança `SQLiteException` ou equivalente, THEN THE `DashboardRepositoryImpl` SHALL capturar a exceção e retornar `DashboardStats` com todos os contadores zerados, `isOfflineData = true` e registrar o erro via log estruturado.
12. WHEN uma nova coleta é inserida, atualizada ou deletada na tabela `coleta` via Room, THE flow subjacente usado pela `FonteEstatisticas` SHALL emitir valores atualizados em até 1000 ms após o commit da transação, sem necessidade de chamada HTTP.
13. THE `DashboardRepositoryImpl` SHALL preservar a invariante de consistência: para qualquer `DashboardStats` retornado por `buscarEstatisticasLocais`, vale `totalColetados + totalPendentes == totalPatrimonios` sempre que `totalPatrimonios > 0`; THE `totalPendentes` SHALL ser calculado como `max(0, totalPatrimonios - totalColetados)` para proteger contra cache stale.
14. THE `DashboardRepositoryImpl` SHALL respeitar a estrutura atual das tabelas Room (`patrimonio`, `coleta`, `dashboard_stats`) verificada via `database-verification.md` antes de criar novas queries; nenhuma nova coluna ou migração de schema deve ser introduzida pela feature.

---

### Requirement 4: Exibição do estado offline na UI

**User Story:** Como coletor, quero ver claramente quando os dados exibidos na dashboard são offline e quando foram atualizados pela última vez, para que eu saiba se preciso sincronizar antes de tomar decisões.

#### Acceptance Criteria

1. WHEN a `FonteEstatisticas` emite um `DashboardStats` com `isOfflineData = true`, THE `DashboardFragment` SHALL exibir o indicador de modo offline já existente (`updateOfflineIndicator`) no estado visível ao usuário em até 500 ms após a emissão.
2. WHEN a `FonteEstatisticas` emite um `DashboardStats` com `isOfflineData = false`, THE `DashboardFragment` SHALL atualizar o indicador de modo offline para o estado não visível ao usuário em até 500 ms após a emissão.
3. WHEN a `FonteEstatisticas` emite um `DashboardStats` cujo campo `timestampUltimaSincronizacao` é não nulo, THE `DashboardFragment` SHALL expor esse timestamp via estado observável da UI (por exemplo, propriedade `ultimaSyncMillis` no state class) para renderização posterior, sem alterar o contrato dos endpoints.
4. IF a `FonteEstatisticas` emite um `DashboardStats` cujo campo `timestampUltimaSincronizacao` é nulo, THEN THE `DashboardFragment` SHALL manter vazia/oculta a área de renderização do timestamp da última sincronização, sem lançar exceção e sem afetar a visibilidade do indicador offline.
5. WHILE o `DashboardFragment` está visível e ainda não recebeu nenhuma emissão de `FonteEstatisticas` (antes da primeira emissão do StateFlow), THE `DashboardFragment` SHALL manter o indicador de modo offline oculto e o timestamp de última sincronização ausente.

---

### Requirement 5: Preservação de contratos externos e regras de steering

**User Story:** Como mantenedor do projeto, quero garantir que o refactor da dashboard não altere contratos externos nem viole regras de steering obrigatórias, para que outras telas e integrações não sejam afetadas.

#### Acceptance Criteria

1. THE `dashboard-refactor-clean` SHALL NOT alterar nenhuma URL de endpoint da `ApiService` usada pelo `DashboardRepositoryImpl`; para cada endpoint consumido pelo `DashboardRepositoryImpl` antes do refactor, a URL completa (prefixo `api/mobile/` preservado), o método HTTP e os parâmetros de path/query SHALL permanecer idênticos após o refactor, conforme `Endpoints_Rule`.
2. IF qualquer endpoint consumido pelo `DashboardRepositoryImpl` tiver sua URL, método HTTP ou parâmetros alterados por este refactor, THEN THE pull request SHALL ser rejeitado em revisão de código e THE build SHALL NOT ser publicada.
3. THE `dashboard-refactor-clean` SHALL NOT alterar o conjunto de campos, nomes ou tipos de dado de requisição e resposta do endpoint base de estatísticas do dashboard consumido por `buscarEstatisticas`; nenhum campo pode ser adicionado, removido ou renomeado no DTO correspondente.
4. THE `DashboardViewModelClean` e qualquer ViewModel derivado usado pelo `OverviewFragment` SHALL importar apenas símbolos dos pacotes `com.inventario.mobile.domain.*` para dependências de repositórios e modelos, e SHALL NOT conter imports diretos de `com.inventario.mobile.data.repository.DashboardRepositoryImpl` ou qualquer outra implementação concreta de repositório, conforme `Clean_Architecture_Rule`.
5. THE `DashboardRepositoryImpl` SHALL permanecer localizada no diretório `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/`.
6. THE interface `DashboardRepository` SHALL permanecer localizada no diretório `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/repository/`.
7. WHEN o APK desta feature é gerado com sucesso via `gradlew assembleDebug` ou `gradlew assembleRelease`, THE equipe SHALL registrar em `docs/historico_e_reunioes/BUILD_HISTORY.md` uma nova entrada contendo os campos `número da build`, `data`, `versionName`, `versionCode`, `caminho do APK`, `tamanho` e `lista de mudanças`, conforme `BuildTracking_Rule`.
8. THE `dashboard-refactor-clean` SHALL NOT modificar arquivos fora dos pacotes `presentation.dashboard`, `presentation.statistics.OverviewFragment`, `data.repository.DashboardRepositoryImpl`, `domain.repository.DashboardRepository`, `domain.model.DashboardStats`, `di.RepositoryModule`, `di.DashboardModule`, e arquivos correlatos de teste.
9. IF um smoke test manual ou automatizado cobrindo os fluxos `login`, `sync`, `coleta (scanner QR)`, `seleção de sala` e `exportação de relatório` é executado antes e depois desta feature, THEN os resultados do smoke test SHALL ser idênticos em pass/fail para cada fluxo, sem nenhuma regressão funcional introduzida.

---

### Requirement 6: Comportamento em rotação de tela e navegação

**User Story:** Como coletor, quero girar o dispositivo ou navegar para outra tela e voltar ao dashboard sem ver comportamentos anômalos, para que a tela continue confiável no uso em campo.

#### Acceptance Criteria

1. WHEN o dispositivo é rotacionado enquanto o `DashboardFragment` está visível, THE `DashboardFragment` SHALL reexibir os KPIs com os mesmos valores da última emissão observada de `FonteEstatisticas` antes da rotação, sem disparar nova chamada ao endpoint base do servidor, dentro da janela de 5000 ms definida por `WhileSubscribed(5000)`.
2. WHEN o usuário navega do `DashboardFragment` para outra tela e retorna em até 5000 ms, THE `DashboardViewModelClean` SHALL reaproveitar o último valor em cache emitido por `FonteEstatisticas` e SHALL NOT disparar nova chamada ao endpoint base do servidor.
3. WHEN o usuário navega do `DashboardFragment` para outra tela e retorna após mais de 5000 ms, THE `DashboardViewModelClean` SHALL recriar o upstream de `FonteEstatisticas` e, como parte dessa recriação, SHALL permitir que `FonteEstatisticas` dispare nova chamada ao endpoint base conforme sua política de cache, refletindo na UI os novos valores quando emitidos.
4. WHILE o `DashboardFragment` está visível, THE `DashboardFragment` SHALL manter exatamente 1 (um) collector ativo sobre `FonteEstatisticas`, independentemente do número de rotações sucessivas ocorridas na mesma sessão do fragment.
5. IF o `DashboardFragment` é rotacionado enquanto está em estado de carregamento ou exibindo mensagem de erro, THEN THE `DashboardFragment` SHALL preservar o mesmo estado (carregamento ou erro, com a mesma mensagem) após a rotação, sem reiniciar o carregamento nem limpar a indicação de erro, dentro da janela de 5000 ms definida por `WhileSubscribed(5000)`.

---

### Requirement 7: Critérios de qualidade testáveis para regressão

**User Story:** Como QA, quero critérios de aceite verificáveis por teste automatizado ou manual que cubram os três problemas P1/P2, P6 e P7, para que uma regressão futura seja detectada antes de ir a campo.

#### Acceptance Criteria

1. WHEN um teste automatizado abre o `DashboardFragment` em cenário online (servidor stub respondendo HTTP 2xx dentro de 2000 ms), THE teste SHALL observar exatamente uma chamada ao endpoint base de estatísticas do dashboard durante a inicialização do Fragment, dentro de uma janela de observação de 5000 ms, validando Requirement 2 critério 7.
2. WHEN um teste automatizado dispara um PullToRefresh no `DashboardFragment` em cenário online, THE teste SHALL observar exatamente uma chamada adicional ao endpoint base de estatísticas dentro de uma janela de observação de 5000 ms após o gesto, validando Requirement 2 critério 5.
3. WHEN um teste automatizado registra 5 coletas locais em Room com o servidor inacessível (stub lançando `IOException` após 2000 ms ou timeout), THE teste SHALL observar que o último `DashboardStats` emitido pela `FonteEstatisticas` dentro de uma janela de 5000 ms possui `totalColetados >= 5` e `isOfflineData = true`, validando Requirement 3 critérios 1, 4 e 5.
4. THE suite de testes SHALL incluir um teste property-based executando no mínimo 100 casos gerados onde, para qualquer lista de 0 a 100 entidades `ColetaEntity` com `sincronizado ∈ {true, false}` e qualquer `CacheServerStats` com `totalPatrimonios ∈ [0, 1.000.000]`, o `DashboardStats` calculado por `buscarEstatisticasLocais` satisfaz simultaneamente: (a) `totalColetados + totalPendentes == totalPatrimonios` quando `totalPatrimonios > 0`, (b) a fórmula de `percentualConclusao` do Requirement 2 critério 11, e (c) `totalColetados >= 0`, `totalPendentes >= 0`, `totalPatrimonios >= 0`.
5. THE suite de testes SHALL incluir um teste property-based executando no mínimo 100 casos gerados onde, para qualquer sequência de `N ∈ [1, 50]` operações `{refresh, coleta_local, invalidar_cache}` aplicadas sobre o fluxo híbrido com um servidor stub determinístico (mesma resposta para cada chamada), THE `DashboardStats` final emitido SHALL ser idempotente em relação a múltiplas invalidações consecutivas do `CacheServerStats`: duas invalidações seguidas produzem o mesmo resultado final que uma única invalidação.
6. THE suite de testes SHALL incluir um teste de migração que abre o `OverviewFragment` após a remoção do `DashboardViewModel_Legado` e verifica que os campos `totalPatrimonios`, `totalColetados`, `totalPendentes` e `percentualProgresso` renderizados mantêm igualdade exata nos campos inteiros e diferença absoluta ≤ 0,01 em `percentualProgresso`, em relação aos valores esperados a partir do mesmo stub de servidor usado antes do refactor, validando Requirement 1 critério 4.
