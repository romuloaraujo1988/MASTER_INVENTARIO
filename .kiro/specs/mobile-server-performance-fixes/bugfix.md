# Documento de Requisitos de Bugfix

## Introdução

Este documento descreve quatro problemas de performance no servidor mobile Spring Boot do sistema SIHCP. Os bugs causam degradação severa de desempenho em operações de leitura frequentes, com impacto direto na experiência do app Android em ambientes com volumes reais de dados (200+ coletas, 10.000+ patrimônios).

Os problemas se concentram em dois padrões recorrentes: **consultas N+1** (múltiplas queries ao banco por item de uma lista) e **carregamento desnecessário de dados completos** para retornar apenas metadados ou contagens. Ambos os padrões já foram corrigidos em outros métodos do mesmo codebase — os bugs representam métodos que ficaram para trás nessa evolução.

---

## Análise dos Bugs

### Bug 1 — Problema N+1 em `buscarColetasPendentes` e `buscarHistoricoColetas`

**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`

### Comportamento Atual (Defeito)

1.1 QUANDO `buscarColetasPendentes()` é chamado para um usuário com N coletas pendentes, ENTÃO o sistema executa 1 query para buscar todas as coletas do coletor mais N queries adicionais para buscar o inventário de cada coleta individualmente via `inventarioDAO.findById()`

1.2 QUANDO `buscarHistoricoColetas()` é chamado com um limite L para um usuário com N coletas (N > L), ENTÃO o sistema carrega todas as N coletas em memória e executa L queries adicionais de inventário, ao invés de carregar apenas as L coletas necessárias

1.3 QUANDO `buscarColetasPendentes()` é chamado para um usuário com 200 coletas pendentes, ENTÃO o sistema executa 201 queries ao banco de dados (1 + 200)

### Comportamento Esperado (Correto)

2.1 QUANDO `buscarColetasPendentes()` é chamado, ENTÃO o sistema SHALL buscar apenas coletas com status PENDENTE diretamente no DAO (filtrando no banco) e converter cada coleta usando `converterColetaParaResponseSimples()`, eliminando as queries adicionais de inventário

2.2 QUANDO `buscarHistoricoColetas()` é chamado com limite L, ENTÃO o sistema SHALL buscar no máximo L coletas diretamente do DAO (com LIMIT no banco) e converter cada coleta usando `converterColetaParaResponseSimples()`, sem carregar coletas além do limite nem executar queries adicionais de inventário

2.3 QUANDO `buscarColetasPendentes()` ou `buscarHistoricoColetas()` são chamados, ENTÃO o sistema SHALL executar no máximo 1 query ao banco de dados, independente do número de coletas retornadas

### Comportamento Inalterado (Prevenção de Regressão)

3.1 QUANDO `buscarColetasPendentes()` retorna coletas, ENTÃO o sistema SHALL CONTINUAR A retornar apenas coletas com `statusColeta = "PENDENTE"` do usuário autenticado

3.2 QUANDO `buscarHistoricoColetas()` é chamado com limite L, ENTÃO o sistema SHALL CONTINUAR A retornar no máximo L coletas, respeitando o parâmetro de limite

3.3 QUANDO `buscarColetasComPaginacaoReal()` é chamado, ENTÃO o sistema SHALL CONTINUAR A funcionar com seu comportamento otimizado atual, sem alterações

3.4 QUANDO qualquer método de coleta retorna dados, ENTÃO o sistema SHALL CONTINUAR A retornar os campos `numeroPatrimonio`, `descricaoPatrimonio`, `nomeColetor` e `nomeInventario` corretamente populados

---

### Bug 2 — Paginação incremental em memória em `buscarColetasIncrementais`

**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`

### Comportamento Atual (Defeito)

1.4 QUANDO `buscarColetasIncrementais()` é chamado com parâmetros de paginação (`offset`, `limit`), ENTÃO o sistema carrega TODAS as coletas modificadas desde a última sincronização em memória antes de aplicar a paginação via `subList()`

1.5 QUANDO `buscarColetasIncrementais()` é chamado e existem 10.000 coletas modificadas, ENTÃO o sistema aloca memória para todos os 10.000 registros mesmo que o cliente solicite apenas 100

1.6 QUANDO `buscarColetasIncrementais()` itera sobre as coletas paginadas, ENTÃO o sistema executa 2 queries adicionais por coleta — uma para `usuarioDAO.findById()` e outra para `inventarioDAO.findById()` — resultando em até 200 queries extras para uma página de 100 itens

1.7 QUANDO `buscarColetasIncrementais()` retorna o `totalCount`, ENTÃO o sistema obtém esse valor carregando todos os registros em memória ao invés de executar uma query `COUNT(*)`

### Comportamento Esperado (Correto)

2.4 QUANDO `buscarColetasIncrementais()` é chamado com `offset` e `limit`, ENTÃO o sistema SHALL delegar a paginação ao banco de dados, passando `limit` e `offset` diretamente para `ColetaDAO.buscarModificadasDesde()` (ou sobrecarga equivalente), sem carregar registros além dos solicitados

2.5 QUANDO `buscarColetasIncrementais()` converte coletas para response, ENTÃO o sistema SHALL usar `converterColetaParaResponseSimples()` para eliminar as queries N+1 de usuário e inventário por coleta

2.6 QUANDO `buscarColetasIncrementais()` precisa retornar o `totalCount`, ENTÃO o sistema SHALL obter esse valor via query `COUNT(*)` separada no DAO, sem carregar os registros completos

### Comportamento Inalterado (Prevenção de Regressão)

3.5 QUANDO `buscarColetasIncrementais()` é chamado com `dataUltimaSync` e `inventarioId`, ENTÃO o sistema SHALL CONTINUAR A retornar apenas coletas modificadas após a data informada e pertencentes ao inventário especificado

3.6 QUANDO `buscarColetasIncrementais()` retorna a resposta, ENTÃO o sistema SHALL CONTINUAR A incluir os campos `totalCount`, `offset`, `limit` e a lista de coletas na estrutura de resposta esperada pelo app Android

3.7 QUANDO `offset` ou `limit` são nulos, ENTÃO o sistema SHALL CONTINUAR A aplicar os valores padrão (offset = 0, limit = 100)

---

### Bug 3 — `/sync/status` carrega dados completos para retornar apenas metadados

**Arquivo:** `src/main/java/com/inventario/mobile/server/controller/MobileOfflineSyncController.java`

### Comportamento Atual (Defeito)

1.8 QUANDO o endpoint `GET /api/mobile/sync/status` é chamado, ENTÃO o sistema executa `buscarDadosOffline()` que carrega todos os patrimônios, salas e responsáveis em memória apenas para retornar as contagens presentes em `MetadataDTO`

1.9 QUANDO o endpoint `GET /api/mobile/sync/status` é chamado em um ambiente com 10.000 patrimônios, ENTÃO o sistema aloca memória e processa todos os 10.000 registros de patrimônio para retornar apenas o número inteiro `totalPatrimonios`

### Comportamento Esperado (Correto)

2.7 QUANDO o endpoint `GET /api/mobile/sync/status` é chamado, ENTÃO o sistema SHALL executar apenas queries `COUNT(*)` para patrimônios, salas e responsáveis, retornando o `MetadataDTO` sem carregar nenhum registro completo

2.8 QUANDO `MobileOfflineSyncService.buscarMetadadosOffline(Integer inventarioId)` é chamado, ENTÃO o sistema SHALL retornar um `MetadataDTO` populado exclusivamente com resultados de queries de contagem, sem instanciar listas de entidades

### Comportamento Inalterado (Prevenção de Regressão)

3.8 QUANDO o endpoint `GET /api/mobile/sync/status` retorna, ENTÃO o sistema SHALL CONTINUAR A retornar um `MetadataDTO` com os campos `totalPatrimonios`, `totalSalas`, `totalResponsaveis`, `versaoServidor`, `inventarioAtivoId`, `inventarioAtivoNome` e `timestamp`

3.9 QUANDO o endpoint `GET /api/mobile/sync/offline-data` é chamado, ENTÃO o sistema SHALL CONTINUAR A usar `buscarDadosOffline()` com o comportamento atual, sem alterações

3.10 QUANDO o endpoint `GET /api/mobile/sync/status` é chamado com `inventarioId` informado, ENTÃO o sistema SHALL CONTINUAR A retornar os metadados referentes ao inventário especificado

---

### Bug 4 — `/sync/stats` faz queries completas para retornar contagens

**Arquivo:** `src/main/java/com/inventario/mobile/server/controller/MobileSyncController.java`

### Comportamento Atual (Defeito)

1.10 QUANDO o endpoint `GET /api/mobile/sync/stats` é chamado, ENTÃO o sistema chama `salaService.listarTodasSalas()` que executa uma query completa retornando todos os dados de todas as salas apenas para obter `salas.size()`

1.11 QUANDO o endpoint `GET /api/mobile/sync/stats` é chamado, ENTÃO o sistema chama `patrimonioService.listarPatrimonios(0, 100)` que carrega 100 registros completos de patrimônio apenas para verificar se existem mais de 100 patrimônios no total

1.12 QUANDO o total de patrimônios é exatamente 100 ou múltiplo de 100, ENTÃO o sistema retorna `estimativaPaginas = "100+"` ao invés do número real de páginas, tornando a estimativa imprecisa e inutilizável para o app calcular o progresso de download

### Comportamento Esperado (Correto)

2.9 QUANDO o endpoint `GET /api/mobile/sync/stats` precisa do total de salas, ENTÃO o sistema SHALL chamar `salaService.contarSalas()` (método a ser criado em `MobileSalaService`) que executa `SELECT COUNT(*)` sem carregar registros completos

2.10 QUANDO o endpoint `GET /api/mobile/sync/stats` precisa do total de patrimônios, ENTÃO o sistema SHALL chamar `patrimonioService.contarPatrimoniosAtivos()` (método já existente em `MobilePatrimonioService`) para obter o total real via `COUNT(*)`

2.11 QUANDO o endpoint `GET /api/mobile/sync/stats` calcula o número de páginas, ENTÃO o sistema SHALL retornar o valor exato calculado como `(int) Math.ceil((double) totalPatrimonios / 100)` ao invés de uma estimativa imprecisa

### Comportamento Inalterado (Prevenção de Regressão)

3.11 QUANDO o endpoint `GET /api/mobile/sync/stats` retorna, ENTÃO o sistema SHALL CONTINUAR A retornar os campos `totalSalas`, `patrimoniosPorPagina` e o total/páginas de patrimônios na estrutura de resposta esperada pelo app Android

3.12 QUANDO o endpoint `GET /api/mobile/sync/patrimonios` é chamado, ENTÃO o sistema SHALL CONTINUAR A funcionar com paginação real, sem alterações

3.13 QUANDO o endpoint `GET /api/mobile/sync/salas` é chamado, ENTÃO o sistema SHALL CONTINUAR A retornar a lista completa de salas, sem alterações

---

## Derivação da Condição de Bug

### Condição de Bug Unificada

```pascal
FUNCTION isBugCondition(X)
  INPUT: X de tipo Requisição HTTP ao servidor mobile
  OUTPUT: boolean

  RETURN (
    // Bug 1: N+1 em coletas
    X.metodo = "buscarColetasPendentes" OU
    X.metodo = "buscarHistoricoColetas"
  ) OU (
    // Bug 2: Paginação em memória
    X.metodo = "buscarColetasIncrementais" E
    X.totalColetasModificadas > X.limit
  ) OU (
    // Bug 3: Status carrega dados completos
    X.endpoint = "GET /api/mobile/sync/status"
  ) OU (
    // Bug 4: Stats faz queries completas
    X.endpoint = "GET /api/mobile/sync/stats"
  )
END FUNCTION
```

### Propriedade de Correção

```pascal
// Propriedade: Fix Checking — Eficiência de Queries
FOR ALL X WHERE isBugCondition(X) DO
  resultado ← F'(X)
  ASSERT resultado.queriesExecutadas <= 2
  ASSERT resultado.registrosCarregadosEmMemoria <= resultado.registrosRetornados
  ASSERT resultado.statusHTTP = 200
  ASSERT resultado.dadosRetornados = dadosEsperados(X)
END FOR
```

### Propriedade de Preservação

```pascal
// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT F(X) = F'(X)
END FOR
```
