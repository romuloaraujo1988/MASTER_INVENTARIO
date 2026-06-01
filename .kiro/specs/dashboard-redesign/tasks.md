# Implementation Plan: Dashboard Redesign

## Overview

O plano converte o redesign da camada de apresentação do Dashboard (`InventarioMobile`) em passos incrementais de código. A estratégia extrai toda a lógica de apresentação para um helper puro e testável (`DashboardUiMapper`), valida essa lógica com testes de propriedade (jqwik), redesenha os recursos e o layout `fragment_dashboard.xml` e, por fim, conecta o `DashboardFragment` (apenas render) ao mapper, preservando integralmente as funcionalidades existentes.

Premissas (já existentes e inalteradas): `DashboardViewModelClean`, `DashboardStats`, `EvolucaoColeta`, `DashboardUiStateClean`, `loadColetasEvolucao(inventarioId)` e as URLs `api/mobile/*`. Linguagem de implementação: **Kotlin** (Android), com testes JVM em `app/src/test/`. Biblioteca de PBT: **jqwik** (mínimo de 100 iterações por propriedade).

## Tasks

- [x] 1. Criar os tipos puros de apresentação
  - [x] 1.1 Criar `DashboardPresentationModels.kt` com os tipos de apoio
    - Definir `data class KpiValues`, `data class ChartPoint(index, value, label)`
    - Definir `sealed class DashboardScreenState` (Loading, Empty, Error, Content)
    - Definir `sealed class ChartAreaState` (Loading, Empty, Error, Data)
    - Kotlin puro, sem imports `android.*`; arquivo em `presentation/dashboard/`
    - _Requirements: 9.1, 9.2_

- [x] 2. Implementar a lógica de KPIs e conclusão no `DashboardUiMapper`
  - [x] 2.1 Criar `DashboardUiMapper.kt` e implementar `kpiValues`, `clampPercent` e `completionLabel`
    - `kpiValues(stats)` mapeia os 4 campos inteiros para texto
    - `clampPercent(value: Double?)` arredonda e restringe a [0,100]; `null`/`NaN`/≤0 → 0; ≥100 → 100
    - `completionLabel(value: Double?)` retorna "X% concluído" usando `clampPercent`, sem decimais
    - Objeto Kotlin puro, sem dependência de `View`/`Context`
    - _Requirements: 1.5, 1.6, 2.2, 2.3, 2.5_

  - [x]* 2.2 Escrever teste de propriedade para `kpiValues`
    - **Property 1: Mapeamento de KPIs reflete os campos corretos**
    - **Validates: Requirements 1.5, 1.6**
    - jqwik, ≥100 iterações; tag `// Feature: dashboard-redesign, Property 1`

  - [x]* 2.3 Escrever teste de propriedade para `clampPercent`
    - **Property 2: Percentual de conclusão é arredondado e restrito a [0,100]**
    - **Validates: Requirements 2.2, 2.5**
    - Geradores incluindo `null`, `NaN`, negativos, `>100`, `0`, `100` e fracionários

  - [x]* 2.4 Escrever teste de propriedade para `completionLabel`
    - **Property 3: Rótulo de conclusão contém o inteiro clampado e não tem decimais**
    - **Validates: Requirements 2.3**

- [x] 3. Implementar a lógica do cabeçalho no `DashboardUiMapper`
  - [x] 3.1 Implementar `greeting`, `resolveInventoryName` e `formatLastSync`
    - `greeting(hourOfDay)` retorna texto não vazio, ≤ 60 caracteres
    - `resolveInventoryName(nome, id)` trunca a 80 chars com reticências; substituto por `id`; placeholder genérico quando ambos nulos
    - `formatLastSync(timestamp)` retorna `null` se nulo, senão "dd/MM/yyyy HH:mm" (24h)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x]* 3.2 Escrever teste de propriedade para `greeting`
    - **Property 4: Saudação é não vazia e respeita o limite de comprimento**
    - **Validates: Requirements 3.1**

  - [x]* 3.3 Escrever teste de propriedade para `resolveInventoryName`
    - **Property 5: Nome do inventário é truncado a 80 caracteres com reticências**
    - **Validates: Requirements 3.2, 3.4, 3.6**
    - Geradores com strings de comprimento 0, ~80, >80 e caracteres unicode

  - [x]* 3.4 Escrever teste de propriedade para `formatLastSync`
    - **Property 6: Formatação da última sincronização é fiel ao instante (round-trip ao minuto)**
    - **Validates: Requirements 3.3, 3.5**

- [x] 4. Implementar a lógica de gráfico e de estado de tela no `DashboardUiMapper`
  - [x] 4.1 Implementar `toChartPoints`, `resolveDashboardState`, `resolveChartState` e `shouldShowOfflineFromData`
    - `toChartPoints(evolucao)` → um `ChartPoint` por item, ordem preservada, `value=quantidade`, `label=data`, índices sequenciais a partir de 0
    - `resolveDashboardState(uiState, stats)` com prioridade Loading > Error > Empty > Content
    - `resolveChartState(uiState)` com prioridade Loading > Error > Empty > Data
    - `shouldShowOfflineFromData(stats)` retorna `stats.isOfflineData`
    - Funções totais (não lançam) sobre todas as entradas
    - _Requirements: 4.2, 4.4, 4.5, 4.6, 6.3, 6.4, 6.5, 6.6, 8.2_

  - [x]* 4.2 Escrever teste de propriedade para `toChartPoints`
    - **Property 7: Mapeamento de pontos do gráfico preserva ordem, quantidade e rótulo**
    - **Validates: Requirements 4.2**
    - Geradores com listas vazias e não vazias de `EvolucaoColeta`

  - [x]* 4.3 Escrever teste de propriedade para `resolveDashboardState`
    - **Property 8: Estado global da tela é mutuamente exclusivo e priorizado**
    - **Validates: Requirements 6.3, 6.4, 6.5, 6.6**
    - Geradores cobrindo combinações de `isLoading`/`error`/`nome`+`id`

  - [x]* 4.4 Escrever teste de propriedade para `resolveChartState`
    - **Property 9: Estado da área do gráfico é mutuamente exclusivo e priorizado**
    - **Validates: Requirements 4.2, 4.4, 4.5, 4.6**
    - Geradores cobrindo `isLoadingGrafico`/`graficoError`/`coletasEvolucao` vazia ou não

  - [x]* 4.5 Escrever teste de propriedade para `shouldShowOfflineFromData`
    - **Property 10: Indicador offline deriva do flag dos dados**
    - **Validates: Requirements 8.2**

- [x] 5. Checkpoint - Garantir que os testes do mapper passam
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Adicionar recursos de apresentação
  - [x] 6.1 Adicionar strings do Dashboard em `strings.xml`
    - `dashboard_greeting_*`, `completion_label_format` ("%d%% concluído"), `last_sync_format` ("Última sincronização: %s")
    - `chart_title`, `chart_empty`, `chart_error`, `dashboard_empty`, `no_active_inventory` e `contentDescription` dos ícones de KPI
    - _Requirements: 2.3, 3.1, 3.3, 3.6, 4.5, 4.6, 7.1_

  - [x] 6.2 Definir estilos de cartão/botão e tokens de cor
    - `themes.xml`: `Widget.App.Dashboard.KpiCard` e `Widget.App.Dashboard.ActionButton` (corner radius e elevação centralizados)
    - `colors.xml`: reuso de cores neutras/realce do tema, sem preenchimento saturado de card
    - _Requirements: 1.1, 1.2, 5.1, 5.3, 5.4_

  - [x]* 6.3 Escrever teste unitário de razão de contraste dos pares texto/fundo do tema
    - Calcular contraste e exigir ≥ 4.5:1 (texto normal) e ≥ 3:1 (texto grande) onde viável
    - _Requirements: 7.3_

- [x] 7. Redesenhar o layout `fragment_dashboard.xml`
  - [x] 7.1 Reescrever `fragment_dashboard.xml` com a nova estrutura, preservando IDs existentes
    - Cabeçalho (`tvGreeting`, `tvInventoryName`, `tvLastSync`) e `LinearProgressIndicator` (`progressCompletion`, max=100, determinado)
    - Grade 2x2 de KPIs com `layout_weight=1` e altura idêntica, fundo `?attr/colorSurface`, sem gradientes nem `shadow*`
    - Área de gráfico: `LineChart` (MPAndroidChart) + `chartLoading`/`chartEmpty`/`chartError`
    - Botões de ação: `btnQuickScan` filled e demais outlined idênticos, mesmo corner radius/typografia, espaçamento múltiplo de 8dp
    - Preservar `swipeRefresh`, `layoutOfflineIndicator`, `progressBar`, `tvError`, `fabVoiceSearch`, `voiceListeningOverlay`; `contentDescription` em ícones/FAB; alvos ≥48dp
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 3.1, 3.2, 3.3, 4.1, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 7.1, 7.2_

- [x] 8. Conectar a renderização do Fragment ao mapper
  - [x] 8.1 Implementar `render()`, `applyDashboardState()`, `applyChartState()` e `renderChart()` e invocá-los no bloco de coleta de flows existente
    - `render()` aplica `kpiValues`, `clampPercent`/`completionLabel`, cabeçalho e visibilidade de `tvLastSync`
    - `applyDashboardState`/`applyChartState` exibem no máximo um estado especial via `when(state)`
    - `renderChart` converte `List<ChartPoint>` em `Entry`/`LineDataSet` e aplica `ValueFormatter` com `label` no eixo X
    - KPIs obtidos exclusivamente de `fonteEstatisticas`; Fragment não contém lógica de negócio
    - _Requirements: 1.5, 1.6, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.2, 4.4, 4.5, 4.6, 6.3, 6.4, 6.5, 6.6, 8.5, 9.1_

  - [x]* 8.2 Escrever teste de exemplo de re-render após nova emissão
    - Verificar que após nova emissão de `fonteEstatisticas` o KPI/percentual exibido é atualizado
    - _Requirements: 1.6, 2.4_

- [x] 9. Preservar e verificar as funcionalidades existentes no layout redesenhado
  - [x] 9.1 Reconectar as interações preservadas à nova árvore de views
    - Pull-to-refresh chamando `viewModel.refresh()` + `viewModel.loadColetasEvolucao(inventarioId)`
    - `Indicador_Offline` governado por `isOfflineData`/`shouldShowOfflineFromData`; offline-first inalterado
    - FAB de voz abrindo/fechando `voiceListeningOverlay`; navegação dos 4 botões sem alterar destinos
    - Não alterar URLs `api/mobile/*`
    - _Requirements: 4.3, 8.1, 8.2, 8.3, 8.4, 8.6, 9.3, 9.4_

  - [x]* 9.2 Escrever testes de exemplo/integração das interações preservadas
    - Pull-to-refresh invoca `refresh()` e `loadColetasEvolucao(inventarioId)`
    - FAB exibe overlay; cancelar oculta; cada botão navega para o destino atual
    - _Requirements: 4.3, 5.5, 8.1, 8.3, 8.4, 8.6_

- [x] 10. Checkpoint final - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tarefas marcadas com `*` são opcionais (testes) e podem ser puladas para um MVP mais rápido.
- Cada tarefa referencia requisitos específicos para rastreabilidade.
- Os critérios visuais/estáticos (cards neutros sem gradiente, grade 2x2 idêntica, raio de canto, espaçamento de 8dp, tipografia, `contentDescription`, alvos de 48dp, estilos filled/outlined) são atendidos pela Task 6/7 e verificados por inspeção de layout/lint e revisão, conforme a Testing Strategy do design.
- Os testes de propriedade (jqwik, ≥100 iterações) validam as 10 propriedades universais do `DashboardUiMapper`; testes de exemplo cobrem a fiação preservada e o re-render.
- Toda a lógica vive no `DashboardUiMapper` (Kotlin puro); o `DashboardFragment` apenas renderiza (Req 9.1).

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "6.1", "6.2"] },
    { "id": 1, "tasks": ["2.1", "6.3", "7.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "2.4", "3.1"] },
    { "id": 3, "tasks": ["3.2", "3.3", "3.4", "4.1"] },
    { "id": 4, "tasks": ["4.2", "4.3", "4.4", "4.5", "8.1"] },
    { "id": 5, "tasks": ["8.2", "9.1"] },
    { "id": 6, "tasks": ["9.2"] }
  ]
}
```
