# Mobile Server Performance Fixes — Bugfix Design

## Overview

Este documento descreve o design da correção de quatro bugs de performance no servidor mobile Spring Boot do SIHCP. Os bugs compartilham dois padrões recorrentes: **consultas N+1** (múltiplas queries ao banco por item de uma lista) e **carregamento desnecessário de dados completos** para retornar apenas metadados ou contagens.

A estratégia de correção é cirúrgica: adicionar métodos DAO que delegam filtragem, paginação e contagem ao banco de dados, e refatorar os métodos de service para usar esses novos métodos junto com o conversor `converterColetaParaResponseSimples()` já existente (que não gera queries adicionais).

---

## Glossary

- **Bug_Condition (C)**: A condição que identifica uma requisição que aciona um dos quatro bugs de performance — N+1 em coletas, paginação em memória, status carregando dados completos, ou stats fazendo queries completas.
- **Property (P)**: O comportamento correto esperado — o número de queries executadas deve ser O(1) (constante), independente do volume de dados, e os dados retornados devem ser idênticos ao comportamento original.
- **Preservation**: O comportamento de todos os outros endpoints e métodos que não são afetados pelos bugs, incluindo o formato de resposta JSON, as URLs dos endpoints e os métodos já otimizados como `buscarColetasComPaginacaoReal()`.
- **ColetaDAO**: Classe em `src/main/java/com/inventario/dao/ColetaDAO.java` que gerencia operações de banco de dados para coletas. Receberá quatro novos métodos.
- **MobileColetaService**: Classe em `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java` que contém os métodos bugados `buscarColetasPendentes()`, `buscarHistoricoColetas()` e `buscarColetasIncrementais()`.
- **MobileOfflineSyncService**: Classe em `src/main/java/com/inventario/mobile/server/service/MobileOfflineSyncService.java` que receberá o novo método `buscarMetadadosOffline()`.
- **MobileOfflineSyncController**: Classe em `src/main/java/com/inventario/mobile/server/controller/MobileOfflineSyncController.java` cujo método `verificarStatus()` será refatorado.
- **MobileSyncController**: Classe em `src/main/java/com/inventario/mobile/server/controller/MobileSyncController.java` cujo método `obterEstatisticas()` será refatorado.
- **converterColetaParaResponseSimples**: Método privado em `MobileColetaService` que converte uma `Coleta` para `MobileColetaResponse` usando dados já carregados pelo JOIN, sem executar queries adicionais.
- **converterParaResponse**: Método privado em `MobileColetaService` que converte uma `Coleta` para `MobileColetaResponse` executando queries adicionais de patrimônio por ID — é o método que causa N+1.

---

## Bug Details

### Bug Condition

Os quatro bugs manifestam-se em métodos distintos, mas todos compartilham a mesma causa raiz: o código executa trabalho proporcional ao volume de dados quando deveria executar trabalho constante.

**Formal Specification:**
```
FUNCTION isBugCondition(X)
  INPUT: X de tipo Requisição HTTP ao servidor mobile
  OUTPUT: boolean

  RETURN (
    // Bug 1: N+1 em coletas pendentes e histórico
    X.metodo IN ["buscarColetasPendentes", "buscarHistoricoColetas"]
  ) OU (
    // Bug 2: Paginação incremental em memória
    X.metodo = "buscarColetasIncrementais"
    AND X.totalColetasModificadas > X.limit
  ) OU (
    // Bug 3: /sync/status carrega dados completos
    X.endpoint = "GET /api/mobile/sync/status"
  ) OU (
    // Bug 4: /sync/stats faz queries completas
    X.endpoint = "GET /api/mobile/sync/stats"
  )
END FUNCTION
```

### Examples

**Bug 1 — N+1 em `buscarColetasPendentes`:**
- Usuário com 200 coletas pendentes → sistema executa 201 queries (1 para buscar todas as coletas + 200 para `inventarioDAO.findById()` por coleta)
- Usuário com 0 coletas pendentes → sistema executa 1 query (sem N+1, mas ainda carrega todas as coletas do coletor)

**Bug 1 — N+1 em `buscarHistoricoColetas` com limit=10:**
- Usuário com 500 coletas → sistema carrega 500 coletas em memória, itera as primeiras 10, executa 10 queries de inventário → 511 queries totais
- Comportamento esperado: 1 query com `LIMIT 10` + 0 queries adicionais

**Bug 2 — Paginação em memória em `buscarColetasIncrementais`:**
- 10.000 coletas modificadas, cliente solicita `limit=100, offset=0` → sistema carrega 10.000 registros em memória, aplica `subList(0, 100)`, executa 200 queries adicionais (usuário + inventário por coleta)
- Comportamento esperado: 1 query `COUNT(*)` + 1 query com `LIMIT 100 OFFSET 0` + 0 queries adicionais

**Bug 3 — `/sync/status` carrega dados completos:**
- Ambiente com 10.000 patrimônios → `verificarStatus()` chama `buscarDadosOffline()` que carrega todos os 10.000 patrimônios, todas as salas e todos os responsáveis em memória apenas para retornar `MetadataDTO` com três inteiros
- Comportamento esperado: 3 queries `COUNT(*)` + 1 query para inventário ativo

**Bug 4 — `/sync/stats` faz queries completas:**
- `obterEstatisticas()` chama `salaService.listarTodasSalas()` (carrega todos os dados de todas as salas) e `patrimonioService.listarPatrimonios(0, 100)` (carrega 100 registros completos) apenas para retornar contagens
- Comportamento esperado: 2 queries `COUNT(*)` diretas

---

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- O endpoint `GET /api/mobile/sync/offline-data` SHALL CONTINUAR usando `buscarDadosOffline()` com comportamento atual, sem alterações
- O endpoint `GET /api/mobile/sync/patrimonios` SHALL CONTINUAR funcionando com paginação real, sem alterações
- O endpoint `GET /api/mobile/sync/salas` SHALL CONTINUAR retornando a lista completa de salas, sem alterações
- O método `buscarColetasComPaginacaoReal()` SHALL CONTINUAR funcionando com seu comportamento otimizado atual
- O formato de resposta JSON de todos os endpoints SHALL CONTINUAR idêntico ao atual
- As URLs de todos os endpoints SHALL CONTINUAR inalteradas
- Os campos `numeroPatrimonio`, `descricaoPatrimonio`, `nomeColetor` e `nomeInventario` SHALL CONTINUAR corretamente populados nas respostas de coleta
- O `MetadataDTO` retornado por `/sync/status` SHALL CONTINUAR com os campos `totalPatrimonios`, `totalSalas`, `totalResponsaveis`, `versaoServidor`, `inventarioAtivoId`, `inventarioAtivoNome` e `timestamp`
- Os campos `totalSalas`, `patrimoniosPorPagina` e total/páginas de patrimônios SHALL CONTINUAR presentes na resposta de `/sync/stats`

**Scope:**
Todas as requisições que NÃO correspondem à condição de bug devem ser completamente inalteradas por esta correção. Isso inclui:
- Todos os outros endpoints do servidor mobile
- O fluxo de registro de coletas
- A sincronização offline completa (`/offline-data`)
- A paginação de patrimônios (`/sync/patrimonios`)

---

## Hypothesized Root Cause

### Bug 1 — N+1 em `buscarColetasPendentes` e `buscarHistoricoColetas`

1. **Ausência de método DAO especializado**: `ColetaDAO` não possui `buscarPendentesPorColetor()` nem `buscarHistoricoPorColetor()`. O código usa `buscarPorColetor()` que retorna todas as coletas sem filtro de status e sem LIMIT.

2. **Uso do conversor errado**: Os métodos usam `converterParaResponse(coleta, usuario, inventario)` que executa query adicional de patrimônio por ID, ao invés de `converterColetaParaResponseSimples(coleta)` que usa dados já carregados pelo JOIN.

3. **Filtragem em memória**: `buscarColetasPendentes()` filtra `statusColeta = "PENDENTE"` em Java após carregar todas as coletas, ao invés de filtrar no SQL com `WHERE STATUS_COLETA = 'PENDENTE'`.

4. **LIMIT em memória**: `buscarHistoricoColetas()` aplica o limite via contador Java (`count >= limit`) após carregar todas as coletas, ao invés de usar `LIMIT ?` no SQL.

### Bug 2 — Paginação incremental em memória

1. **Ausência de métodos DAO com LIMIT/OFFSET**: `ColetaDAO.buscarModificadasDesde()` não aceita parâmetros de paginação. O código carrega todos os registros e aplica `subList()` em memória.

2. **Ausência de método COUNT**: Não existe `contarModificadasDesde()`. O `totalCount` é obtido via `coletas.size()` após carregar todos os registros.

3. **Uso do conversor errado**: O loop de conversão usa `converterParaResponse(coleta, usuario, inventario)` com `usuarioDAO.findById()` e `inventarioDAO.findById()` por coleta, gerando N+1.

### Bug 3 — `/sync/status` carrega dados completos

1. **Reutilização incorreta de método**: `verificarStatus()` chama `buscarDadosOffline()` que foi projetado para o endpoint `/offline-data` (que precisa de todos os dados). Para `/status`, apenas as contagens são necessárias.

2. **Ausência de método de metadados**: `MobileOfflineSyncService` não possui `buscarMetadadosOffline()` que execute apenas `COUNT(*)`.

### Bug 4 — `/sync/stats` faz queries completas

1. **Uso de métodos de listagem para obter contagens**: `obterEstatisticas()` chama `salaService.listarTodasSalas()` (retorna lista completa) e `patrimonioService.listarPatrimonios(0, 100)` (retorna 100 registros) apenas para obter tamanhos.

2. **Métodos de contagem já existem mas não são usados**: `salaService.contarSalasAtivas()` e `patrimonioService.contarPatrimoniosAtivos()` já existem e retornam `COUNT(*)`, mas não são chamados em `obterEstatisticas()`.

3. **Estimativa imprecisa de páginas**: A lógica atual retorna `"100+"` quando a primeira página tem 100 itens, ao invés de calcular o total real de páginas.

---

## Correctness Properties

Property 1: Bug Condition — Eficiência de Queries

_For any_ requisição X onde `isBugCondition(X)` é verdadeiro, a versão corrigida SHALL executar no máximo 2 queries ao banco de dados (independente do volume de dados), retornar os mesmos dados que a versão original retornaria para a mesma entrada, e retornar HTTP 200 com a estrutura de resposta JSON inalterada.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11**

Property 2: Preservation — Comportamento Inalterado

_For any_ requisição X onde `isBugCondition(X)` é falso, a versão corrigida SHALL produzir exatamente o mesmo resultado que a versão original, preservando todos os endpoints não afetados, o formato de resposta JSON, as URLs, e os métodos já otimizados.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 3.13**

---

## Fix Implementation

### Changes Required

Assumindo que a análise de causa raiz está correta:

---

#### Bug 1 — Novos métodos em `ColetaDAO`

**File:** `src/main/java/com/inventario/dao/ColetaDAO.java`

**Specific Changes:**

1. **Adicionar `buscarPendentesPorColetor(int idColetor)`**: Query com `WHERE c.ID_COLETOR = ? AND c.STATUS_COLETA = 'PENDENTE'` e todos os JOINs necessários (TABELA_PATRIMONIO, TABELA_PARTICIPANTE_INVENTARIO, TABELA_USUARIO, TABELA_INVENTARIO, TABELA_SALA) para que `converterColetaParaResponseSimples()` funcione sem queries adicionais.

2. **Adicionar `buscarHistoricoPorColetor(int idColetor, int limit)`**: Query com `WHERE c.ID_COLETOR = ?` e `LIMIT ?`, com os mesmos JOINs acima.

**Refatoração em `MobileColetaService`:**

3. **Refatorar `buscarColetasPendentes()`**: Substituir `coletaDAO.buscarPorColetor()` + filtro Java + `converterParaResponse()` por `coletaDAO.buscarPendentesPorColetor()` + `converterColetaParaResponseSimples()`.

4. **Refatorar `buscarHistoricoColetas()`**: Substituir `coletaDAO.buscarPorColetor()` + loop com contador + `converterParaResponse()` por `coletaDAO.buscarHistoricoPorColetor(usuario.getId(), limit)` + `converterColetaParaResponseSimples()`.

---

#### Bug 2 — Novos métodos em `ColetaDAO` + refatoração de `buscarColetasIncrementais`

**File:** `src/main/java/com/inventario/dao/ColetaDAO.java`

**Specific Changes:**

5. **Adicionar `contarModificadasDesde(Timestamp dataUltimaSync, Integer inventarioId)`**: Query `SELECT COUNT(*) FROM TABELA_COLETA WHERE DATA_COLETA > ?` com filtro opcional de `AND ID_INVENTARIO = ?`.

6. **Adicionar sobrecarga de `buscarModificadasDesde(Timestamp dataUltimaSync, Integer inventarioId, int limit, int offset)`**: Query com os mesmos JOINs dos métodos acima e `LIMIT ? OFFSET ?` no banco.

**Refatoração em `MobileColetaService`:**

7. **Refatorar `buscarColetasIncrementais()`**: Substituir a lógica atual (carrega tudo + `subList()` + N+1) por:
   - `int totalCount = coletaDAO.contarModificadasDesde(dataUltimaSync, inventarioId)`
   - `List<Coleta> coletas = coletaDAO.buscarModificadasDesde(dataUltimaSync, inventarioId, limit, offset)`
   - Loop de conversão usando `converterColetaParaResponseSimples(coleta)` (sem queries adicionais)

---

#### Bug 3 — Novo método em `MobileOfflineSyncService` + refatoração do controller

**File:** `src/main/java/com/inventario/mobile/server/service/MobileOfflineSyncService.java`

**Specific Changes:**

8. **Adicionar `buscarMetadadosOffline(Integer inventarioId)`**: Método que executa apenas queries `COUNT(*)`:
   - `patrimonioDAO.contarPatrimoniosAtivos()` — já existe
   - `salaDAO.contarSalas()` ou query direta via `DatabaseConnection.getConnection()` — verificar se existe; se não, usar query direta seguindo o padrão de `MobileSalaService`
   - `responsavelDAO.contarTotal()` — verificar se existe; se não, usar query direta
   - Busca do inventário ativo via `inventarioDAO.findById()` ou `inventarioDAO.buscarInventarioAtivo()`
   - Retorna `MetadataDTO` populado apenas com resultados de contagem

**File:** `src/main/java/com/inventario/mobile/server/controller/MobileOfflineSyncController.java`

9. **Refatorar `verificarStatus()`**: Substituir `offlineSyncService.buscarDadosOffline(inventarioId)` por `offlineSyncService.buscarMetadadosOffline(inventarioId)`. O tipo de retorno do endpoint permanece `MetadataDTO` — apenas a fonte dos dados muda.

---

#### Bug 4 — Refatoração de `obterEstatisticas` em `MobileSyncController`

**File:** `src/main/java/com/inventario/mobile/server/controller/MobileSyncController.java`

**Specific Changes:**

10. **Refatorar `obterEstatisticas()`**: Substituir:
    - `salaService.listarTodasSalas()` → `salaService.contarSalasAtivas()` (já existe em `MobileSalaService`)
    - `patrimonioService.listarPatrimonios(0, 100)` → `patrimonioService.contarPatrimoniosAtivos()` (já existe em `MobilePatrimonioService`, retorna `long`)
    - Cálculo de páginas: `(int) Math.ceil((double) totalPatrimonios / 100)` ao invés de `"100+"`
    - Adicionar `totalPatrimonios` e `totalPaginas` ao mapa de stats

---

## Testing Strategy

### Validation Approach

A estratégia de testes segue duas fases: primeiro, executar os testes exploratórios no código **não corrigido** para confirmar a causa raiz e observar as falhas; depois, verificar que a correção elimina os bugs (fix checking) e não quebra nada (preservation checking).

---

### Exploratory Bug Condition Checking

**Goal**: Confirmar ou refutar a análise de causa raiz executando testes no código ANTES da correção. Se os testes não falharem como esperado, a hipótese de causa raiz precisa ser revisada.

**Test Plan**: Escrever testes que instrumentam o `ColetaDAO`, `InventarioDAO` e `UsuarioDAO` com contadores de chamadas (ou usar mocks com verificação de invocações), executar os métodos bugados e verificar que o número de queries excede o esperado.

**Test Cases:**

1. **Bug 1 — Teste N+1 em `buscarColetasPendentes`**: Criar usuário com 5 coletas pendentes. Chamar `buscarColetasPendentes()`. Verificar que `inventarioDAO.findById()` foi chamado 5 vezes. (Falhará no código não corrigido — confirmará N+1)

2. **Bug 1 — Teste carregamento excessivo em `buscarHistoricoColetas`**: Criar usuário com 50 coletas. Chamar `buscarHistoricoColetas(username, 5)`. Verificar que `coletaDAO.buscarPorColetor()` retornou 50 registros mas apenas 5 foram usados. (Falhará no código não corrigido — confirmará carregamento excessivo)

3. **Bug 2 — Teste paginação em memória em `buscarColetasIncrementais`**: Simular 1000 coletas modificadas. Chamar `buscarColetasIncrementais(timestamp, null, 10, 0)`. Verificar que `coletaDAO.buscarModificadasDesde()` retornou 1000 registros. (Falhará no código não corrigido — confirmará paginação em memória)

4. **Bug 3 — Teste carregamento completo em `/sync/status`**: Chamar `verificarStatus()`. Verificar que `patrimonioDAO.listarTodosComJoins()` foi chamado. (Falhará no código não corrigido — confirmará carregamento desnecessário)

5. **Bug 4 — Teste queries completas em `/sync/stats`**: Chamar `obterEstatisticas()`. Verificar que `salaService.listarTodasSalas()` foi chamado ao invés de `contarSalasAtivas()`. (Falhará no código não corrigido — confirmará uso de método errado)

**Expected Counterexamples:**
- `inventarioDAO.findById()` chamado N vezes para N coletas (Bug 1)
- `coletaDAO.buscarModificadasDesde()` retornando todos os registros independente do `limit` (Bug 2)
- `patrimonioDAO.listarTodosComJoins()` chamado em endpoint de status (Bug 3)
- `salaService.listarTodasSalas()` chamado em endpoint de stats (Bug 4)

---

### Fix Checking

**Goal**: Verificar que para todas as entradas onde a condição de bug se aplica, a versão corrigida produz o comportamento esperado.

**Pseudocode:**
```
FOR ALL X WHERE isBugCondition(X) DO
  resultado ← F'(X)
  ASSERT resultado.queriesExecutadas <= 2
  ASSERT resultado.registrosCarregadosEmMemoria <= resultado.registrosRetornados
  ASSERT resultado.statusHTTP = 200
  ASSERT resultado.dadosRetornados = dadosEsperados(X)
END FOR
```

**Test Cases:**

1. **Bug 1 — Fix: `buscarColetasPendentes` com 200 coletas pendentes**: Verificar que apenas 1 query é executada e que todos os 200 registros retornados têm `statusColeta = "PENDENTE"`.

2. **Bug 1 — Fix: `buscarHistoricoColetas` com limit=10 e 500 coletas**: Verificar que apenas 1 query é executada e que exatamente 10 registros são retornados.

3. **Bug 2 — Fix: `buscarColetasIncrementais` com 10.000 coletas, limit=100, offset=0**: Verificar que `totalCount = 10000`, que apenas 100 registros são carregados em memória, e que nenhuma query adicional de usuário/inventário é executada.

4. **Bug 3 — Fix: `verificarStatus()`**: Verificar que `MetadataDTO` é retornado com campos corretos e que `patrimonioDAO.listarTodosComJoins()` NÃO foi chamado.

5. **Bug 4 — Fix: `obterEstatisticas()`**: Verificar que `totalSalas` é um inteiro exato (não estimativa), que `totalPaginas` é calculado corretamente, e que `listarTodasSalas()` NÃO foi chamado.

---

### Preservation Checking

**Goal**: Verificar que para todas as entradas onde a condição de bug NÃO se aplica, a versão corrigida produz o mesmo resultado que a versão original.

**Pseudocode:**
```
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT F(X) = F'(X)
END FOR
```

**Testing Approach**: Property-based testing é recomendado para preservation checking porque:
- Gera automaticamente muitos casos de teste no domínio de entrada
- Captura edge cases que testes manuais podem perder
- Fornece garantias fortes de que o comportamento é preservado para todas as entradas não-bugadas

**Test Cases:**

1. **Preservation — `buscarColetasComPaginacaoReal()`**: Verificar que o método otimizado existente continua funcionando identicamente antes e depois da correção.

2. **Preservation — `GET /api/mobile/sync/offline-data`**: Verificar que `buscarDadosOffline()` continua sendo chamado e retornando dados completos (patrimônios, salas, responsáveis).

3. **Preservation — `GET /api/mobile/sync/patrimonios`**: Verificar que a paginação de patrimônios continua funcionando com os mesmos parâmetros e resultados.

4. **Preservation — `GET /api/mobile/sync/salas`**: Verificar que a lista completa de salas continua sendo retornada.

5. **Preservation — Campos de resposta de coleta**: Para qualquer coleta retornada pelos métodos corrigidos, verificar que `numeroPatrimonio`, `descricaoPatrimonio`, `nomeColetor` e `nomeInventario` estão corretamente populados (dados carregados pelo JOIN).

6. **Preservation — Estrutura de `IncrementalSyncResponse`**: Verificar que a resposta de `buscarColetasIncrementais()` continua incluindo `totalCount`, `offset`, `limit` e a lista de coletas.

---

### Unit Tests

- Testar `ColetaDAO.buscarPendentesPorColetor()` com dados reais: verificar que apenas coletas com `STATUS_COLETA = 'PENDENTE'` são retornadas
- Testar `ColetaDAO.buscarHistoricoPorColetor()` com `limit=5`: verificar que exatamente 5 registros são retornados independente do total
- Testar `ColetaDAO.contarModificadasDesde()` com e sem `inventarioId`: verificar que o COUNT retornado é correto
- Testar `ColetaDAO.buscarModificadasDesde()` com `limit` e `offset`: verificar que a paginação no banco funciona corretamente
- Testar `MobileOfflineSyncService.buscarMetadadosOffline()`: verificar que retorna `MetadataDTO` com contagens corretas sem carregar listas
- Testar `MobileSyncController.obterEstatisticas()` após correção: verificar que `totalPaginas` é um inteiro exato

### Property-Based Tests

- Gerar estados aleatórios de banco (N coletas pendentes, M coletas no histórico) e verificar que `buscarColetasPendentes()` sempre executa exatamente 1 query independente de N
- Gerar configurações aleatórias de paginação (limit, offset) e verificar que `buscarColetasIncrementais()` nunca carrega mais registros em memória do que `limit`
- Gerar volumes aleatórios de patrimônios e salas e verificar que `obterEstatisticas()` sempre retorna o total exato via `COUNT(*)`
- Verificar que para qualquer entrada não-bugada, o resultado dos métodos corrigidos é idêntico ao resultado dos métodos originais

### Integration Tests

- Testar o fluxo completo de `GET /api/mobile/coletas/pendentes` com usuário autenticado: verificar resposta HTTP 200 com lista de coletas pendentes corretamente populadas
- Testar o fluxo completo de `GET /api/mobile/sync/status`: verificar que a resposta é rápida (< 100ms) e contém `MetadataDTO` com campos corretos
- Testar o fluxo completo de `GET /api/mobile/sync/stats`: verificar que `totalPaginas` é um número inteiro exato e que `totalSalas` corresponde ao `COUNT(*)` real
- Testar que `GET /api/mobile/sync/offline-data` continua funcionando após a introdução de `buscarMetadadosOffline()` no service
