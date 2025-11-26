# Implementation Plan - Correção e Melhoria da Sincronização SQLite ↔ PostgreSQL

## Visão Geral

Este plano de implementação detalha as tarefas necessárias para corrigir e melhorar o sistema crítico de sincronização de dados entre SQLite e PostgreSQL. A implementação seguirá uma abordagem incremental, construindo componentes fundamentais primeiro e integrando-os progressivamente.

---

## Tarefas

- [x] 1. Configurar infraestrutura de testes


  - Configurar JUnit 5 e JUnit-Quickcheck para property-based testing
  - Criar estrutura de diretórios para testes
  - Configurar JaCoCo para cobertura de código
  - _Requirements: Todos (infraestrutura)_




- [ ] 2. Implementar modelos de dados e exceções
  - Criar classes de modelo: SyncResult, ValidationResult, Conflict, BackupInfo, IntegrityCheckResult, SyncStatistics, RetryPolicy
  - Criar hierarquia de exceções: SyncException e SyncErrorType
  - Implementar builders para modelos complexos


  - _Requirements: Todos (fundação)_


- [ ] 2.1 Escrever testes unitários para modelos de dados
  - Testar builders e validações básicas
  - Testar serialização/deserialização se aplicável
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 3. Implementar ValidationService



  - Criar classe ValidationService com métodos de validação
  - Implementar validateColetas() para validar IDs obrigatórios
  - Implementar validateReferentialIntegrity() para verificar FKs
  - Implementar validateTimestampFormat() para validar formato de datas
  - Implementar validateRequiredFields() para verificar campos obrigatórios
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 3.1 Escrever property test para validação de IDs

  - **Property 1: Validação Completa de Dados**
  - **Validates: Requirements 1.1, 1.2, 1.3**

- [x] 3.2 Escrever property test para validação de timestamps

  - **Property 2: Validação de Formato de Timestamps**
  - **Validates: Requirements 1.4, 13.7**

- [x] 3.3 Escrever property test para campos obrigatórios

  - **Property 3: Validação de Campos Obrigatórios**
  - **Validates: Requirements 1.5**

- [x] 3.4 Escrever property test para integridade referencial

  - **Property 6, 7, 8: Integridade Referencial**
  - **Validates: Requirements 3.1, 3.2, 3.3**

- [x] 4. Implementar BackupService



  - Criar classe BackupService
  - Implementar createBackup() para criar backup com timestamp
  - Implementar restoreBackup() para restaurar backup específico
  - Implementar listBackups() para listar backups disponíveis
  - Implementar cleanOldBackups() para manter apenas últimos 10 backups
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.6_

- [x] 4.1 Escrever property test para backup antes de modificação


  - **Property 14: Backup Antes de Modificação**
  - **Validates: Requirements 7.1**

- [x] 4.2 Escrever property test para nomenclatura de backup

  - **Property 15: Nomenclatura de Backup**
  - **Validates: Requirements 7.2**

- [x] 4.3 Escrever property test para limite de backups

  - **Property 16: Limite de Backups**
  - **Validates: Requirements 7.4**

- [x] 4.4 Escrever property test para restauração (round trip)

  - **Property 17: Restauração de Backup**
  - **Validates: Requirements 7.6**

- [x] 5. Implementar RetryManager



  - Criar classe RetryManager
  - Implementar executeWithRetry() com retry automático
  - Implementar calculateBackoffDelay() para backoff exponencial
  - Implementar isRetryableError() para classificar erros
  - Configurar MAX_RETRIES = 3 e INITIAL_DELAY_MS = 5000
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 5.1 Escrever property test para retry com backoff


  - **Property 13: Retry com Backoff Exponencial**
  - **Validates: Requirements 5.2**

- [x] 5.2 Escrever testes unitários para casos específicos de retry


  - Testar timeout de rede (exemplo)
  - Testar erro de constraint (exemplo)
  - Testar erro de autenticação (exemplo)
  - _Requirements: 5.1, 5.3, 5.4_

- [ ] 6. Implementar AuditLogger
  - Criar classe AuditLogger
  - Implementar logSyncStart() para registrar início
  - Implementar logRecordProcessed() para registrar cada registro
  - Implementar logError() para registrar erros com stack trace
  - Implementar logSyncComplete() para registrar conclusão
  - Implementar exportLogs() para exportar logs
  - Configurar rotação de logs (50MB, 30 dias)
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

- [ ] 7. Implementar IntegrityChecker
  - Criar classe IntegrityChecker
  - Implementar checkPostSyncIntegrity() para verificação completa
  - Implementar compareTotals() para comparar SQLite vs PostgreSQL
  - Implementar calculateChecksum() para calcular MD5
  - Implementar countPendingColetas() para contar pendências
  - _Requirements: 8.1, 8.2, 8.3, 8.5, 8.6_

- [ ] 7.1 Escrever property test para consistência de totais
  - **Property 18: Consistência de Totais**
  - **Validates: Requirements 8.3**

- [ ] 7.2 Escrever property test para completude de sincronização
  - **Property 19: Completude de Sincronização**
  - **Validates: Requirements 8.5**

- [ ] 8. Implementar ConflictResolver
  - Criar classe ConflictResolver
  - Implementar detectConflict() para comparar timestamps
  - Implementar resolveConflict() com estratégias (KEEP_SQLITE, KEEP_POSTGRES, MERGE)
  - Implementar showConflictDialog() para interface visual
  - Criar enum ResolutionStrategy
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [ ] 8.1 Escrever property test para resolução de conflito SQLite
  - **Property 11: Resolução de Conflito - SQLite**
  - **Validates: Requirements 4.4**

- [ ] 8.2 Escrever property test para resolução de conflito PostgreSQL
  - **Property 12: Resolução de Conflito - PostgreSQL**
  - **Validates: Requirements 4.5**

- [ ] 8.3 Escrever teste unitário para detecção de conflito
  - Testar comparação de timestamps
  - Testar identificação de campos conflitantes
  - _Requirements: 4.1, 4.2_

- [ ] 9. Implementar SQLiteManager (melhorias)
  - Adicionar suporte a transações explícitas
  - Implementar métodos para marcar coletas como sincronizadas
  - Implementar queries para buscar coletas pendentes
  - Implementar calculateChecksum() para verificação de integridade
  - Adicionar índices para performance
  - _Requirements: 2.2, 2.4, 2.6, 12.1, 12.4_

- [ ] 10. Implementar PostgreSQLManager (melhorias)
  - Adicionar suporte a transações explícitas com rollback
  - Implementar batch insert para sincronização em lote
  - Implementar métodos de verificação de existência (patrimônio, inventário, coletor)
  - Implementar queries para contar coletas por inventário
  - _Requirements: 2.1, 2.3, 2.5, 3.1, 3.2, 3.3, 8.2_

- [ ] 11. Implementar SyncCoordinator - Parte 1: Validação e Backup
  - Criar classe SyncCoordinator com injeção de dependências
  - Implementar fluxo de validação antes de sincronização
  - Integrar BackupService para criar backup automático
  - Implementar tratamento de erros de validação
  - _Requirements: 1.6, 1.7, 7.1_

- [ ] 12. Implementar SyncCoordinator - Parte 2: Sincronização de Coletas
  - Implementar syncColetasPendentes() com transações atômicas
  - Integrar ValidationService para validar antes de enviar
  - Integrar RetryManager para retry automático
  - Implementar lógica de rollback em caso de falha
  - Integrar AuditLogger para registrar operações
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 12.1 Escrever property test para atomicidade - sucesso
  - **Property 4: Atomicidade de Transação - Sucesso**
  - **Validates: Requirements 2.2, 2.6**

- [ ] 12.2 Escrever property test para atomicidade - falha
  - **Property 5: Atomicidade de Transação - Falha**
  - **Validates: Requirements 2.3, 2.4**

- [ ] 13. Implementar SyncCoordinator - Parte 3: Ordem de Sincronização
  - Implementar syncWithConflictResolution() para entidades específicas
  - Implementar lógica de ordem: responsáveis → salas → patrimônios → coletas
  - Implementar verificação de dependências antes de sincronizar
  - Sincronizar dependências automaticamente se necessário
  - _Requirements: 3.4, 3.5, 3.6, 3.7_

- [ ] 13.1 Escrever property test para ordem de dependências
  - **Property 9: Ordem de Sincronização de Dependências**
  - **Validates: Requirements 3.4, 3.5, 3.6**

- [ ] 13.2 Escrever property test para ordem global
  - **Property 10: Ordem Global de Sincronização**
  - **Validates: Requirements 3.7**

- [ ] 14. Implementar SyncCoordinator - Parte 4: Download de Dados
  - Implementar downloadDadosServidor() para sincronização reversa
  - Implementar paginação de 500 registros por requisição
  - Baixar responsáveis, salas e patrimônios do PostgreSQL
  - Substituir dados antigos no SQLite pelos novos
  - Armazenar timestamp da última sincronização
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [ ] 14.1 Escrever property test para consistência de download
  - **Property 23: Consistência de Download**
  - **Validates: Requirements 10.1, 10.2, 10.3**

- [ ] 15. Implementar SyncCoordinator - Parte 5: Verificação de Integridade
  - Integrar IntegrityChecker após sincronização
  - Implementar verificação de totais SQLite vs PostgreSQL
  - Implementar verificação de coletas pendentes
  - Implementar cálculo de checksum
  - Exibir relatório de integridade ao usuário
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [ ] 16. Implementar tratamento de timezone
  - Criar classe TimezoneConverter
  - Implementar conversão de local para UTC antes de sincronizar
  - Implementar conversão de UTC para local após download
  - Usar formato ISO 8601 para timestamps
  - Validar que timestamps não estão no futuro
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

- [ ] 16.1 Escrever property test para conversão UTC
  - **Property 26: Conversão de Timezone para UTC**
  - **Validates: Requirements 13.1**

- [ ] 16.2 Escrever property test para formato ISO 8601
  - **Property 27: Formato ISO 8601**
  - **Validates: Requirements 13.2**

- [ ] 16.3 Escrever property test para round trip de timezone
  - **Property 29: Conversão de Timezone para Local**
  - **Validates: Requirements 13.4**

- [ ] 17. Implementar sincronização incremental
  - Adicionar campo data_ultima_sincronizacao nas tabelas SQLite
  - Implementar filtro para buscar apenas registros novos ou modificados
  - Atualizar timestamp após sincronização bem-sucedida
  - Implementar opção de forçar sincronização completa
  - Manter histórico das últimas 100 sincronizações
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7_

- [ ] 17.1 Escrever property test para sincronização incremental
  - **Property 24: Sincronização Incremental**
  - **Validates: Requirements 12.3**

- [ ] 17.2 Escrever property test para atualização de timestamp
  - **Property 25: Atualização de Timestamp de Sincronização**
  - **Validates: Requirements 12.4**

- [ ] 18. Implementar modo de teste
  - Adicionar parâmetro testMode em syncColetasPendentes()
  - Implementar simulação de sincronização sem modificar dados
  - Gerar relatório detalhado do que seria sincronizado
  - Exibir estatísticas de teste
  - Permitir exportar relatório de teste
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7_

- [ ] 19. Implementar endpoints do servidor - Validação
  - Criar SyncController com endpoints REST
  - Implementar POST /api/sync/validate para validação prévia
  - Validar campos obrigatórios no servidor
  - Validar tipos de dados no servidor
  - Validar foreign keys no servidor
  - Retornar HTTP 400 com detalhes de erro se validação falhar
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [ ] 19.1 Escrever property test para validação no servidor
  - **Property 20, 21, 22: Validação no Servidor**
  - **Validates: Requirements 9.1, 9.2, 9.3**

- [ ] 20. Implementar endpoints do servidor - Sincronização em Lote
  - Implementar POST /api/sync/coletas/batch para batch sync
  - Criar SyncService para processar sincronização em lote
  - Validar cada coleta individualmente
  - Processar todas as coletas mesmo se uma falhar
  - Retornar array de resultados (sucesso/falha) para cada coleta
  - Usar transações para garantir atomicidade
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 20.1 Escrever property test para interpretação UTC no servidor
  - **Property 28: Interpretação UTC no Servidor**
  - **Validates: Requirements 13.3**

- [ ] 21. Implementar endpoints do servidor - Health Check
  - Implementar GET /api/sync/health
  - Verificar conectividade com PostgreSQL
  - Verificar espaço disponível em disco
  - Verificar carga atual do servidor
  - Retornar HTTP 200 se healthy, HTTP 503 se unhealthy
  - Incluir métricas: total de sincronizações 24h, taxa de sucesso, tempo médio
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

- [ ] 21.1 Escrever property test para health check
  - **Property 30: Health Check Disponibilidade**
  - **Validates: Requirements 14.1, 14.5**

- [ ] 22. Implementar UI - Tela de Sincronização
  - Criar SyncFrame com interface Swing
  - Exibir estatísticas de dados offline (patrimônios, salas, responsáveis, coletas pendentes)
  - Adicionar botão "Sincronizar Coletas Pendentes"
  - Adicionar botão "Baixar Dados do Servidor"
  - Adicionar botão "Testar Sincronização"
  - Adicionar checkbox "Modo de Teste"
  - Exibir data/hora da última sincronização
  - _Requirements: 1.7, 5.5, 5.6, 6.1, 6.2, 15.1, 15.7_

- [ ] 23. Implementar UI - Janela de Progresso
  - Criar ProgressDialog para exibir progresso de sincronização
  - Exibir barra de progresso com percentual
  - Exibir etapa atual (validação, backup, responsáveis, salas, patrimônios, coletas)
  - Exibir contador de registros processados e total
  - Exibir tempo decorrido e estimativa de tempo restante
  - Exibir mensagens de erro sem fechar janela
  - Permitir cancelar sincronização com rollback seguro
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

- [ ] 24. Implementar UI - Dialog de Resolução de Conflitos
  - Criar ConflictResolutionDialog
  - Exibir dados conflitantes lado a lado (SQLite vs PostgreSQL)
  - Exibir timestamps de modificação
  - Exibir campos conflitantes destacados
  - Adicionar botões: "Manter SQLite", "Manter PostgreSQL", "Mesclar Manualmente"
  - Implementar interface de mesclagem manual de campos
  - _Requirements: 4.2, 4.3, 4.6_

- [ ] 25. Implementar UI - Relatórios e Logs
  - Criar ReportDialog para exibir relatório de sincronização
  - Exibir estatísticas: total processado, sucessos, falhas, tempo total
  - Exibir lista de erros com detalhes
  - Exibir resultado de verificação de integridade
  - Adicionar botão "Exportar Relatório"
  - Adicionar botão "Ver Logs Detalhados"
  - Implementar visualizador de logs com filtros
  - _Requirements: 1.6, 5.5, 8.4, 8.7, 11.5_

- [ ] 26. Implementar UI - Gerenciamento de Backups
  - Criar BackupManagerDialog
  - Listar backups disponíveis com data, hora e tamanho
  - Adicionar botão "Restaurar Backup"
  - Adicionar botão "Excluir Backup"
  - Exibir confirmação antes de restaurar
  - Exibir progresso durante restauração
  - _Requirements: 7.5, 7.6_

- [ ] 27. Checkpoint - Testes de Integração
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 28. Implementar geradores customizados para PBT
  - Criar ColetaGenerator para gerar coletas aleatórias
  - Criar PatrimonioGenerator para gerar patrimônios aleatórios
  - Criar TimestampGenerator para gerar timestamps válidos
  - Configurar geradores para produzir dados válidos e inválidos
  - _Requirements: Todos (infraestrutura de testes)_

- [ ] 29. Documentação e guias
  - Criar guia de uso da sincronização para operadores
  - Documentar fluxos de sincronização (coletas, download, resolução de conflitos)
  - Documentar tratamento de erros e recuperação
  - Criar guia de troubleshooting
  - Documentar configurações e parâmetros
  - _Requirements: Todos (documentação)_

- [ ] 30. Testes de aceitação e validação final
  - Testar sincronização completa end-to-end
  - Testar cenários de falha e recuperação
  - Testar resolução de conflitos
  - Testar backup e restauração
  - Validar performance com grandes volumes de dados
  - Validar logs e auditoria
  - _Requirements: Todos (validação)_

- [ ] 31. Checkpoint Final - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

