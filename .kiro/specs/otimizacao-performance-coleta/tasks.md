# Implementation Plan - Otimização de Performance na Coleta Patrimonial Desktop

## Visão Geral

Este plano detalha as tarefas para implementar otimizações de performance no ColetaFrame_v2, seguindo uma abordagem incremental que permite testar cada melhoria isoladamente.

---

## Fase 1: Infraestrutura Base

- [x] 1. Configurar HikariCP Connection Pool


  - Adicionar dependência HikariCP no pom.xml
  - Criar classe `DatabaseConfig` com configuração do pool
  - Configurar pool com 5-10 conexões, timeouts apropriados
  - Migrar DAOs para usar DataSource ao invés de criar conexões
  - _Requirements: 10.1, 10.2, 10.5_



- [x] 1.1 Criar classe DatabaseConfig
  - Implementar singleton para gerenciar HikariDataSource
  - Configurar parâmetros: maximumPoolSize=10, minimumIdle=5
  - Adicionar método `getDataSource()` para acesso global
  - Implementar método `close()` para shutdown gracioso


  - _Requirements: 10.1, 10.5_

- [x] 1.2 Migrar PatrimonioDAO para usar connection pool
  - Substituir `DriverManager.getConnection()` por `dataSource.getConnection()`

  - Garantir que conexões sejam retornadas ao pool (try-with-resources)
  - Testar que conexões são reutilizadas
  - _Requirements: 10.2, 10.4_

- [x] 1.3 Migrar ColetaDAO para usar connection pool ✅ CONCLUÍDO
  - ✅ Substituído import de `DatabaseConnection` por `ConnectionManager`
  - ✅ Substituídas todas as 50+ ocorrências de `DatabaseConnection.getConnection()` por `ConnectionManager.getConnection()`
  - ✅ Todas as conexões já usam try-with-resources (fechamento automático)
  - ✅ ColetaDAO agora usa HikariCP connection pool para todas as operações
  - _Requirements: 10.2_
  - _Completed: 17/01/2025_

- [x] 1.4 Testar connection pool ✅ PRONTO PARA TESTE
  - ✅ Criado `ConnectionPoolTest.java` com 5 testes automatizados
  - ✅ Teste 1: Verifica tempo de obtenção de conexão (< 50ms)
  - ✅ Teste 2: Executa 100 operações e valida reuso de conexões
  - ✅ Teste 3: Valida que conexões são retornadas ao pool
  - ✅ Teste 4: Verifica inicialização do pool
  - ✅ Teste 5: Exibe estatísticas do pool
  - 📝 Para executar: `mvn test -Dtest=ConnectionPoolTest`
  - _Requirements: 10.1, 10.2_
  - _Completed: 17/01/2025_

---

## Fase 2: Cache em Memória

- [x] 2. Implementar CacheManager


  - Criar classe singleton `CacheManager`
  - Implementar cache de Salas com Map<Integer, CachedData<Sala>>
  - Implementar cache de Inventário Ativo
  - Adicionar TTL de 5 minutos para entradas


  - Implementar métodos get/put/invalidate/clear
  - _Requirements: 3.1, 3.2, 3.4, 3.5_

- [x] 2.1 Criar classe CachedData<T>

  - Implementar wrapper com data e timestamp
  - Adicionar método `isExpired()` baseado em TTL
  - Adicionar método `getAge()` para diagnóstico
  - _Requirements: 3.5_



- [ ] 2.2 Implementar cache de Salas
  - Método `getSala(int id)` que verifica expiração
  - Método `putSala(Sala sala)` com timestamp atual
  - Método `getAllSalas()` retornando apenas não expiradas
  - _Requirements: 3.1, 3.2_

- [ ] 2.3 Implementar cache de Inventário Ativo
  - Método `getInventarioAtivo()` com verificação de expiração
  - Método `putInventarioAtivo(Inventario inv)`
  - Invalidação automática ao mudar inventário
  - _Requirements: 3.4_

- [ ] 2.4 Integrar cache no ColetaFrame_v2
  - Ao abrir frame, verificar cache antes de consultar banco
  - Ao carregar salas, popular cache
  - Ao registrar coleta, invalidar cache relacionado
  - _Requirements: 3.2, 3.3_

- [ ]* 2.5 Testar cache
  - Verificar que cache hit retorna em < 10ms
  - Verificar que cache expira após TTL
  - Verificar que invalidação funciona corretamente
  - _Requirements: 3.2, 3.5_

---

## Fase 3: SwingWorker e Operações Assíncronas

- [ ] 3. Criar OptimizedSwingWorker base class
  - Criar classe abstrata `OptimizedSwingWorker<T, V>`
  - Implementar logging automático de performance
  - Implementar tratamento de erros padronizado
  - Adicionar métodos abstratos `performOperation()`, `onSuccess()`, `onError()`
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 12.1, 12.4_

- [ ] 3.1 Implementar logging de performance
  - Capturar tempo de início em construtor
  - Logar duração em `done()`
  - Logar warning se operação > 500ms
  - Incluir nome da operação e thread info
  - _Requirements: 12.1, 12.2, 12.4_

- [ ] 3.2 Implementar tratamento de erros
  - Capturar exceções em `doInBackground()`
  - Logar stack trace completo
  - Chamar `onError()` com exceção
  - Garantir que `hideLoading()` sempre executa
  - _Requirements: 12.3_

- [ ] 3.3 Refatorar carregamento de salas para usar SwingWorker
  - Criar worker para `carregarSalasAsync()`
  - Executar query em background thread
  - Atualizar combo box na EDT via `done()`
  - Mostrar/esconder loading indicator
  - _Requirements: 1.4, 5.1, 5.2, 8.1_

- [ ] 3.4 Refatorar busca de patrimônio para usar SwingWorker
  - Criar worker para `buscarPatrimonioAsync(String numero)`
  - Verificar cache primeiro em background
  - Se não em cache, consultar banco
  - Atualizar UI com resultado na EDT
  - _Requirements: 1.2, 5.1, 8.1_

- [ ] 3.5 Refatorar registro de coleta para usar SwingWorker
  - Criar worker para `registrarColetaAsync(Coleta coleta)`
  - Executar INSERT em background
  - Atualizar tabela e cache na EDT
  - Tocar som de sucesso
  - _Requirements: 1.3, 5.1, 8.3_

- [ ] 3.6 Refatorar atualização de tabela para usar SwingWorker
  - Criar worker para `atualizarTabelaAsync()`
  - Carregar dados em background (LIMIT 100)
  - Atualizar DefaultTableModel na EDT
  - _Requirements: 2.1, 5.2, 8.1_

- [ ]* 3.7 Testar SwingWorkers
  - Verificar que `done()` executa na EDT
  - Verificar que UI não congela durante operações
  - Verificar que erros são tratados corretamente
  - _Requirements: 1.5, 8.4_

---

## Fase 4: Busca com Debounce

- [ ] 4. Implementar DebouncedSearchField
  - Criar classe `DebouncedSearchField extends JTextField`
  - Adicionar `javax.swing.Timer` com delay de 300ms
  - Implementar interface `SearchListener` funcional
  - Cancelar timer anterior ao digitar novo caractere
  - Executar busca imediatamente ao pressionar Enter
  - _Requirements: 4.1, 4.4, 4.5_

- [ ] 4.1 Implementar lógica de debounce
  - Criar Timer que dispara após 300ms de inatividade
  - Adicionar DocumentListener para detectar mudanças
  - Cancelar timer pendente ao detectar nova mudança
  - Disparar callback `onSearch()` quando timer completa
  - _Requirements: 4.1, 4.5_

- [ ] 4.2 Adicionar atalho para busca imediata
  - Adicionar KeyListener para detectar Enter
  - Cancelar timer pendente
  - Executar busca imediatamente
  - _Requirements: 4.4_

- [ ] 4.3 Integrar DebouncedSearchField no ColetaFrame_v2
  - Substituir JTextField por DebouncedSearchField
  - Configurar SearchListener para chamar `buscarPatrimonioAsync()`
  - Remover listeners antigos de busca
  - _Requirements: 4.1_

- [ ]* 4.4 Testar debounce
  - Simular digitação rápida (10 caracteres em 1 segundo)
  - Verificar que apenas 1 busca é executada
  - Verificar que Enter dispara busca imediata
  - _Requirements: 4.1, 4.4, 4.5_

---

## Fase 5: Otimização de Queries SQL

- [ ] 5. Criar índices no PostgreSQL
  - Criar índice em `patrimonio(numero_patrimonio)`
  - Criar índice em `patrimonio(id_sala)` com filtro `WHERE ativo = true`
  - Criar índice full-text em `patrimonio(descricao)`
  - Criar índice em `coleta(id_inventario, data_coleta DESC)`
  - Executar ANALYZE após criar índices
  - _Requirements: 6.1, 6.5_

- [ ] 5.1 Otimizar query de busca por número
  - Usar PreparedStatement com placeholder
  - Adicionar LIMIT 1 para retornar apenas primeiro resultado
  - Selecionar apenas colunas necessárias (evitar SELECT *)
  - Verificar com EXPLAIN que usa índice
  - _Requirements: 6.1, 6.4_

- [ ] 5.2 Otimizar query de busca por sala
  - Adicionar paginação com LIMIT e OFFSET
  - Usar PreparedStatement
  - Adicionar ORDER BY para resultados consistentes
  - Selecionar apenas colunas necessárias
  - _Requirements: 6.2, 6.4_

- [ ] 5.3 Otimizar query de contagem
  - Usar COUNT(*) sem carregar dados
  - Adicionar filtros WHERE apropriados
  - Usar PreparedStatement
  - _Requirements: 6.3_

- [ ] 5.4 Otimizar query de listagem de coletas
  - Adicionar LIMIT 100 para carregar apenas últimas coletas
  - Usar ORDER BY data_coleta DESC
  - Fazer JOIN apenas com colunas necessárias
  - _Requirements: 6.2, 6.4_

- [ ]* 5.5 Testar performance de queries
  - Medir tempo de execução de cada query
  - Verificar que busca por número < 100ms
  - Verificar que busca por sala < 500ms
  - Usar EXPLAIN ANALYZE para validar uso de índices
  - _Requirements: 6.1, 6.2_

---

## Fase 6: Paginação e Gerenciamento de Memória

- [ ] 6. Implementar paginação na JTable
  - Limitar carregamento inicial a 100 linhas
  - Adicionar botão "Carregar Mais" no rodapé da tabela
  - Implementar carregamento incremental com OFFSET
  - Manter contador de total de registros disponíveis
  - _Requirements: 2.1, 2.2, 7.1, 7.5_

- [ ] 6.1 Criar OptimizedTableModel
  - Estender DefaultTableModel com suporte a paginação
  - Adicionar métodos `loadNextPage()` e `hasMorePages()`
  - Implementar cache de páginas carregadas
  - Limpar páginas antigas quando memória está alta
  - _Requirements: 7.1, 7.2, 7.5_

- [ ] 6.2 Adicionar controles de paginação
  - Botão "Carregar Mais" que chama `loadNextPage()`
  - Label mostrando "Exibindo X de Y registros"
  - Desabilitar botão quando não há mais páginas
  - _Requirements: 2.2_

- [ ] 6.3 Implementar limpeza de recursos
  - Adicionar WindowListener para detectar fechamento
  - Limpar cache ao fechar frame
  - Remover listeners de componentes
  - Liberar referências a objetos grandes
  - _Requirements: 7.3_

- [ ]* 6.4 Testar gerenciamento de memória
  - Carregar tabela com 10.000 registros disponíveis
  - Verificar que apenas 100 são carregados inicialmente
  - Medir uso de memória (deve ser < 50MB)
  - Verificar que memória é liberada ao fechar frame
  - _Requirements: 7.1, 7.3, 7.4_

---

## Fase 7: Feedback Visual e UX

- [ ] 7. Implementar feedback visual imediato
  - Desabilitar botões imediatamente ao clicar
  - Alterar cursor para WAIT_CURSOR durante operações
  - Adicionar JProgressBar para operações longas
  - Reabilitar botões e restaurar cursor ao completar
  - _Requirements: 9.1, 9.2, 9.5_

- [ ] 7.1 Criar métodos auxiliares de feedback
  - `showLoading()` - exibe progress bar e altera cursor
  - `hideLoading()` - esconde progress bar e restaura cursor
  - `showSuccess(String message)` - exibe mensagem e toca som
  - `showError(String message)` - exibe JOptionPane de erro
  - _Requirements: 9.2, 9.3, 9.4_

- [ ] 7.2 Integrar feedback em todas as operações
  - Chamar `showLoading()` antes de iniciar SwingWorker
  - Chamar `hideLoading()` em `done()` do SwingWorker
  - Chamar `showSuccess()` após operação bem-sucedida
  - Chamar `showError()` em `onError()`
  - _Requirements: 9.2, 9.3, 9.4, 9.5_

- [ ] 7.3 Adicionar som de sucesso
  - Usar `SoundNotification` existente
  - Tocar som ao registrar coleta com sucesso
  - Configurar volume apropriado
  - _Requirements: 9.3_

- [ ]* 7.4 Testar feedback visual
  - Verificar que botão desabilita imediatamente ao clicar
  - Verificar que cursor muda para WAIT_CURSOR
  - Verificar que progress bar aparece
  - Verificar que som toca ao completar
  - Medir tempo de feedback (deve ser < 100ms)
  - _Requirements: 9.1, 9.2, 9.3_

---

## Fase 8: Pré-carregamento e Cache Warming

- [ ] 8. Implementar cache warming na inicialização
  - Criar método `warmupCache()` executado ao abrir frame
  - Pré-carregar inventário ativo em background
  - Pré-carregar lista de salas em background
  - Usar ExecutorService para não bloquear abertura do frame
  - _Requirements: 11.1, 11.2, 11.4_

- [ ] 8.1 Implementar pré-carregamento de sala selecionada
  - Ao selecionar sala, pré-carregar patrimônios em background
  - Armazenar em cache para acesso rápido
  - Não bloquear UI durante pré-carregamento
  - _Requirements: 11.3_

- [ ] 8.2 Implementar pré-carregamento preditivo
  - Detectar salas mais acessadas
  - Pré-carregar patrimônios dessas salas em background
  - Executar durante períodos de ociosidade
  - _Requirements: 11.4, 11.5_

- [ ]* 8.3 Testar pré-carregamento
  - Verificar que cache é populado na inicialização
  - Verificar que acesso a dados pré-carregados é < 10ms
  - Verificar que pré-carregamento não bloqueia UI
  - _Requirements: 11.1, 11.2, 11.3_

---

## Fase 9: Monitoramento e Logging

- [ ] 9. Implementar PerformanceMonitor
  - Criar classe `PerformanceMonitor` com método estático `measureOperation()`
  - Capturar tempo de início e fim de operações
  - Logar operações que excedem 500ms como WARNING
  - Logar operações normais como DEBUG
  - _Requirements: 12.1, 12.2, 12.4, 12.5_

- [ ] 9.1 Integrar PerformanceMonitor em DAOs
  - Envolver queries em `measureOperation()`
  - Logar nome da query e parâmetros
  - Logar tempo de execução
  - _Requirements: 12.1, 12.4_

- [ ] 9.2 Integrar PerformanceMonitor em SwingWorkers
  - Já implementado em OptimizedSwingWorker
  - Verificar que logs estão sendo gerados
  - Ajustar níveis de log se necessário
  - _Requirements: 12.1, 12.4_

- [ ] 9.3 Criar relatório de performance
  - Adicionar menu "Ferramentas > Estatísticas de Performance"
  - Exibir dialog com métricas: operações lentas, tempo médio, etc.
  - Permitir exportar logs para análise
  - _Requirements: 12.3, 12.4_

- [ ]* 9.4 Testar monitoramento
  - Executar operações e verificar logs
  - Simular operação lenta (> 500ms) e verificar WARNING
  - Verificar que relatório exibe dados corretos
  - _Requirements: 12.1, 12.2, 12.4_

---

## Fase 10: Testes e Validação Final

- [ ] 10. Checkpoint - Executar testes de integração
  - Testar fluxo completo: abrir frame → selecionar sala → buscar → coletar
  - Verificar que todas as operações são rápidas e responsivas
  - Verificar que cache está funcionando
  - Verificar que logs estão sendo gerados
  - Medir tempos de operação e comparar com metas
  - _Requirements: Todos_

- [ ]* 10.1 Executar testes de performance
  - Abrir ColetaFrame_v2: deve ser < 500ms
  - Buscar patrimônio por número: deve ser < 100ms
  - Carregar tabela: deve ser < 500ms
  - Registrar coleta: deve ser < 300ms
  - Selecionar sala: deve ser < 200ms
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1_

- [ ]* 10.2 Executar testes de cache
  - Cache hit deve ser < 10ms
  - Cache deve expirar após 5 minutos
  - Cache deve invalidar ao atualizar dados
  - _Requirements: 3.2, 3.5_

- [ ]* 10.3 Executar testes de debounce
  - Digitar 10 caracteres rapidamente
  - Verificar que apenas 1 busca é executada
  - Pressionar Enter e verificar busca imediata
  - _Requirements: 4.1, 4.4, 4.5_

- [ ]* 10.4 Executar testes de connection pool
  - Executar 100 operações
  - Verificar que apenas 5-10 conexões são criadas
  - Verificar que conexões são reutilizadas
  - _Requirements: 10.1, 10.2, 10.5_

- [ ]* 10.5 Executar testes de índices
  - Usar EXPLAIN ANALYZE em queries
  - Verificar que "Index Scan" aparece
  - Verificar que "Seq Scan" não aparece
  - _Requirements: 6.1_

- [ ] 10.6 Documentar melhorias
  - Criar documento com antes/depois de cada métrica
  - Incluir screenshots de logs de performance
  - Documentar configurações aplicadas
  - Criar guia de troubleshooting
  - _Requirements: 12.3_

---

## Notas de Implementação

### Ordem de Prioridade

1. **Alta Prioridade** (Maior impacto):
   - Fase 1: Connection Pool (reduz latência de conexão)
   - Fase 3: SwingWorker (elimina travamentos)
   - Fase 5: Índices SQL (acelera queries)

2. **Média Prioridade**:
   - Fase 2: Cache (reduz queries repetidas)
   - Fase 4: Debounce (reduz queries desnecessárias)
   - Fase 6: Paginação (reduz uso de memória)

3. **Baixa Prioridade** (Polimento):
   - Fase 7: Feedback Visual
   - Fase 8: Pré-carregamento
   - Fase 9: Monitoramento

### Testes Incrementais

Após cada fase, executar testes para validar que:
- Performance melhorou conforme esperado
- Nenhuma funcionalidade foi quebrada
- UI permanece responsiva

### Rollback Plan

Se alguma otimização causar problemas:
1. Reverter commit específico
2. Desabilitar feature via flag de configuração
3. Investigar causa raiz
4. Aplicar correção
5. Re-testar antes de re-habilitar

### Métricas de Sucesso

Ao final da implementação, as seguintes métricas devem ser atingidas:

| Operação | Meta | Como Validar |
|----------|------|--------------|
| Abrir ColetaFrame_v2 | < 500ms | Medir com System.currentTimeMillis() |
| Buscar patrimônio | < 100ms | Log de PerformanceMonitor |
| Carregar tabela | < 500ms | Log de SwingWorker |
| Registrar coleta | < 300ms | Log de PerformanceMonitor |
| Cache hit | < 10ms | Log de CacheManager |
| Connection pool get | < 50ms | Log de HikariCP |

---

**Total de Tarefas:** 60 (40 implementação + 20 testes)
**Tempo Estimado:** 3-4 semanas
**Complexidade:** Média-Alta
