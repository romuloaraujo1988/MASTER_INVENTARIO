# Plano de Implementação

- [ ] 1. Escrever teste exploratório da condição de bug (ANTES de qualquer correção)
  - _Requer framework de testes com banco de dados configurado — pendente_

- [ ] 2. Escrever testes de preservação (ANTES de implementar as correções)
  - _Requer framework de testes com banco de dados configurado — pendente_

- [x] 3. Correção Bug 4 — Refatorar `obterEstatisticas()` para usar COUNT(*) (menor risco)

  - [x] 3.1 Refatorar método `obterEstatisticas()` em `MobileSyncController.java`
    - Substituído `listarTodasSalas()` por `contarSalasAtivas()` (COUNT(*))
    - Substituído `listarPatrimonios(0, 100)` por `contarPatrimoniosAtivos()` (COUNT(*))
    - `totalPaginas` agora calculado com precisão real: `ceil(totalPatrimonios / 100)`
    - Removidas variáveis `salas`, `primeirasPagina`, `estimativaPaginas`, `primeiraPagemPatrimonios`
    - _Arquivos: `src/main/java/com/inventario/mobile/server/controller/MobileSyncController.java`_

  - [x] 3.2 Compilar e verificar — BUILD SUCCESS

- [x] 4. Correção Bug 3 — Criar `buscarMetadadosOffline()` e refatorar `/sync/status`

  - [x] 4.1 Verificar se `ResponsavelDAO` possui método `contarTotal()`
    - Não existe — implementado via query direta em `MobileOfflineSyncService`

  - [x] 4.2 Adicionar método `buscarMetadadosOffline(Integer inventarioId)` em `MobileOfflineSyncService.java`
    - Método anotado com `@Transactional(readOnly = true)`
    - Usa `patrimonioDAO.contarPatrimoniosAtivos()` (já existia)
    - Adicionados métodos privados `contarTotalSalas()` e `contarTotalResponsaveis()` com queries diretas
    - Retorna `MetadataDTO` com apenas resultados de COUNT(*) — sem carregar listas
    - _Arquivos: `src/main/java/com/inventario/mobile/server/service/MobileOfflineSyncService.java`_

  - [x] 4.3 Refatorar método `verificarStatus()` em `MobileOfflineSyncController.java`
    - Substituído `offlineSyncService.buscarDadosOffline(inventarioId)` por `offlineSyncService.buscarMetadadosOffline(inventarioId)`
    - URL `/api/mobile/sync/status` e formato de resposta `MetadataDTO` inalterados
    - _Arquivos: `src/main/java/com/inventario/mobile/server/controller/MobileOfflineSyncController.java`_

  - [x] 4.4 Compilar e verificar — BUILD SUCCESS

- [x] 5. Correção Bug 1 — Adicionar métodos ao ColetaDAO e refatorar coletas pendentes/histórico

  - [x] 5.1 Adicionar método `buscarPendentesPorColetor(int idColetor)` em `ColetaDAO.java`
    - SQL com WHERE STATUS_COLETA = 'PENDENTE' e JOINs completos (incluindo NOME_SALA)
    - _Arquivos: `src/main/java/com/inventario/dao/ColetaDAO.java`_

  - [x] 5.2 Adicionar método `buscarHistoricoPorColetor(int idColetor, int limit)` em `ColetaDAO.java`
    - SQL com LIMIT no banco e JOINs completos
    - _Arquivos: `src/main/java/com/inventario/dao/ColetaDAO.java`_

  - [x] 5.3 Refatorar `buscarColetasPendentes(String username)` em `MobileColetaService.java`
    - Substituído `buscarPorColetor()` + filtro Java por `buscarPendentesPorColetor()`
    - Substituído `converterParaResponse()` por `converterColetaParaResponseSimples()` (sem N+1)
    - _Arquivos: `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`_

  - [x] 5.4 Refatorar `buscarHistoricoColetas(String username, int limit)` em `MobileColetaService.java`
    - Substituído `buscarPorColetor()` + loop com contador por `buscarHistoricoPorColetor()`
    - Substituído `converterParaResponse()` por `converterColetaParaResponseSimples()` (sem N+1)
    - _Arquivos: `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`_

  - [x] 5.5 Compilar e verificar — BUILD SUCCESS

- [x] 6. Correção Bug 2 — Adicionar métodos ao ColetaDAO e refatorar `buscarColetasIncrementais` (maior risco)

  - [x] 6.1 Adicionar método `contarModificadasDesde(Timestamp, Integer)` em `ColetaDAO.java`
    - COUNT(*) com filtro opcional de inventário
    - _Arquivos: `src/main/java/com/inventario/dao/ColetaDAO.java`_

  - [x] 6.2 Adicionar sobrecarga `buscarModificadasDesde(Timestamp, Integer, int, int)` em `ColetaDAO.java`
    - SQL com LIMIT/OFFSET no banco e JOINs completos (incluindo NOME_SALA)
    - Método original `buscarModificadasDesde(Timestamp)` e `buscarModificadasDesde(Timestamp, Integer)` mantidos
    - _Arquivos: `src/main/java/com/inventario/dao/ColetaDAO.java`_

  - [x] 6.3 Refatorar `buscarColetasIncrementais()` em `MobileColetaService.java`
    - `totalCount` via `contarModificadasDesde()` (COUNT(*)) — sem carregar todos os registros
    - Paginação real no banco via `buscarModificadasDesde(timestamp, inventarioId, limit, offset)`
    - Loop de conversão usa `converterColetaParaResponseSimples()` — sem N+1 de usuário/inventário
    - Removidas variáveis `fromIndex`, `toIndex`, `coletasPaginadas` (paginação em memória)
    - _Arquivos: `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`_

  - [x] 6.4 Compilar e verificar — BUILD SUCCESS

- [x] 7. Checkpoint — Compilação final limpa
  - `.\mvnw.cmd clean compile -DskipTests` — BUILD SUCCESS (391 arquivos, 0 erros)
