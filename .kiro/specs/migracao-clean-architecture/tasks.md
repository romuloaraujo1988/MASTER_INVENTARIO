# Implementation Plan - Migração Clean Architecture

## Fase 0: Setup e Preparação (1 dia)

- [x] 1. Configurar ambiente de build


  - Adicionar dependências Hilt no build.gradle (project level)
  - Adicionar dependências Hilt no build.gradle (app level)
  - Adicionar dependências Room
  - Executar Gradle Sync e verificar compilação
  - _Requirements: 1.1, 1.2, 1.3_


- [ ] 2. Configurar Application para Hilt
  - Verificar se @HiltAndroidApp já está no Application
  - Testar inicialização do app
  - Verificar logs de inicialização do Hilt


  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 3. Criar sistema de Feature Flags
  - Criar classe FeatureFlags com SharedPreferences
  - Adicionar flags para cada feature (descrição, coleta, dashboard)


  - Criar método rollbackAll()
  - Testar toggle de flags
  - _Requirements: 8.1, 8.2_

- [x] 4. Criar MigrationCoordinator



  - Implementar classe MigrationCoordinator
  - Adicionar métodos de backup e rollback
  - Criar enum Feature
  - Criar sealed class MigrationResult
  - _Requirements: 8.3, 8.4_

- [ ] 5. Configurar sistema de Smoke Tests
  - Criar interface SmokeTest
  - Criar data class TestResult


  - Implementar framework básico de testes
  - _Requirements: 7.1, 7.2_

---



## Fase 1: Piloto - Feature Descrição (3 dias)

### Dia 1: Preparação

- [x] 6. Criar DescricaoSmokeTest



  - Implementar DescricaoSmokeTest
  - Adicionar teste de carregamento de descrições
  - Adicionar teste de descrições não coletadas
  - Adicionar teste de seleção de descrição
  - _Requirements: 3.7, 7.3_



- [ ] 7. Criar ViewModel Adapter para Descrição
  - Implementar AdaptiveViewModelFactory
  - Adicionar lógica de decisão baseada em feature flag
  - Testar criação de ViewModel antigo


  - Testar criação de ViewModel novo
  - _Requirements: 3.6_

- [x] 8. Preparar backup de dados



  - Implementar backup de descrições
  - Criar tabela migration_backup no Room
  - Testar processo de backup
  - _Requirements: 8.3_



### Dia 2: Migração

- [ ] 9. Migrar DescricaoSelectionActivity
  - Adicionar @AndroidEntryPoint na Activity


  - Trocar ViewModel para usar AdaptiveViewModelFactory
  - Atualizar observação de estado para usar sealed class
  - Manter código antigo comentado (para referência)
  - _Requirements: 3.1, 3.2_



- [ ] 10. Implementar lógica de descrições não coletadas
  - Verificar se endpoint /nao-coletadas está funcionando
  - Testar busca de descrições do servidor
  - Testar fallback para banco local
  - _Requirements: 3.2, 3.4, 3.5_


- [ ] 11. Configurar feature flag
  - Adicionar toggle no menu de desenvolvedor
  - Implementar listener de mudança de flag
  - Testar troca entre ViewModels em runtime
  - _Requirements: 3.6_




### Dia 3: Validação

- [ ] 12. Executar smoke tests automatizados
  - Rodar DescricaoSmokeTest
  - Verificar todos os casos de teste
  - Documentar resultados
  - _Requirements: 3.7, 7.1_

- [x] 13. Testes manuais de descrição


  - Testar carregamento de descrições online
  - Testar carregamento de descrições offline
  - Testar seleção de descrição
  - Testar navegação para próxima tela
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_


- [ ] 14. Testes de performance
  - Medir tempo de carregamento antes/depois
  - Verificar uso de memória
  - Verificar tempo de resposta da UI


  - Documentar métricas
  - _Requirements: 10.2, 10.3_

- [ ] 15. Testar rollback
  - Desabilitar feature flag
  - Verificar se volta para ViewModel antigo




  - Testar funcionalidade com código antigo
  - Medir tempo de rollback
  - _Requirements: 8.1, 8.2_


- [ ] 16. Documentar lições aprendidas
  - Criar documento de retrospectiva
  - Listar problemas encontrados
  - Listar soluções aplicadas

  - Atualizar processo para próximas features
  - _Requirements: 9.1, 9.2_

---

## Fase 2: Feature Coleta (5 dias)



### Dia 1: Preparação

- [ ] 17. Criar ColetaSmokeTest
  - Implementar teste de scan QR Code

  - Implementar teste de registro de coleta
  - Implementar teste de sincronização
  - Implementar teste de coleta duplicada
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_



- [ ] 18. Criar backup de coletas
  - Implementar backup de coletas pendentes
  - Implementar backup de patrimônios coletados
  - Testar restauração de backup
  - _Requirements: 8.3, 8.4_

- [ ] 19. Preparar testes de sincronização offline
  - Criar cenários de teste offline
  - Criar cenários de teste de reconexão
  - Criar cenários de teste de conflito
  - _Requirements: 5.1, 5.2, 5.3_

### Dia 2: Migração - Parte 1

- [ ] 20. Migrar ColetaActivity
  - Adicionar @AndroidEntryPoint
  - Trocar para ColetaViewModelClean
  - Atualizar observação de estado
  - Manter código antigo comentado
  - _Requirements: 4.1_

- [ ] 21. Implementar registro de coleta offline-first
  - Verificar salvamento local imediato
  - Implementar marcação de patrimônio como coletado
  - Testar sem conexão
  - _Requirements: 4.2, 4.6_

- [ ] 22. Implementar validação de coleta duplicada
  - Verificar se patrimônio já foi coletado
  - Exibir mensagem de erro clara
  - Testar cenário de duplicação
  - _Requirements: 4.5_

### Dia 3: Migração - Parte 2

- [ ] 23. Implementar sincronização automática
  - Configurar tentativa de sync após registro
  - Implementar retry em caso de falha
  - Adicionar indicador visual de sync pendente
  - _Requirements: 4.3, 4.7, 5.4_

- [ ] 24. Implementar sincronização em background
  - Configurar WorkManager para sync periódico
  - Implementar worker de sincronização
  - Testar sync em background
  - _Requirements: 5.3, 10.6_

- [ ] 25. Implementar tratamento de conflitos
  - Definir estratégia de resolução de conflitos
  - Implementar priorização de dados locais
  - Testar cenários de conflito
  - _Requirements: 5.7_

### Dia 4: Validação

- [ ] 26. Executar smoke tests de coleta
  - Rodar ColetaSmokeTest completo
  - Verificar todos os cenários
  - Documentar resultados
  - _Requirements: 7.3, 7.4_




- [ ] 27. Testes manuais de coleta
  - Testar scan de QR Code
  - Testar registro de coleta online

  - Testar registro de coleta offline
  - Testar sincronização automática
  - Testar coleta duplicada
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_


- [ ] 28. Testes de sincronização offline
  - Testar coleta completamente offline
  - Testar reconexão e sync automático
  - Testar múltiplas coletas pendentes
  - Testar indicador de pendências

  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

### Dia 5: Validação Final

- [ ] 29. Testes de performance de coleta
  - Medir tempo de registro de coleta
  - Medir tempo de sincronização
  - Verificar performance com muitas coletas pendentes

  - Documentar métricas
  - _Requirements: 10.3, 10.4, 10.6_

- [ ] 30. Testes com usuários beta
  - Selecionar grupo de usuários beta
  - Distribuir versão com feature flag habilitada
  - Coletar feedback
  - Analisar métricas de uso

  - _Requirements: 7.5_

- [ ] 31. Validação de dados
  - Verificar integridade de coletas
  - Verificar sincronização completa
  - Verificar que nenhuma coleta foi perdida
  - _Requirements: 8.3_


- [ ] 32. Testar rollback de coleta
  - Desabilitar feature flag
  - Verificar preservação de coletas pendentes
  - Testar funcionalidade com código antigo
  - _Requirements: 8.1, 8.2, 8.4_


---

## Fase 3: Features Secundárias (2 semanas)

### Dashboard (3 dias)

- [x] 33. Preparar migração do Dashboard


  - Criar DashboardSmokeTest
  - Criar backup de dados do dashboard
  - Preparar feature flag
  - _Requirements: 6.1, 6.2_

- [ ] 34. Migrar DashboardFragment
  - Adicionar @AndroidEntryPoint
  - Trocar para DashboardViewModelClean
  - Implementar carregamento offline
  - _Requirements: 6.2, 6.3_




- [ ] 35. Implementar atualização de estatísticas
  - Implementar pull-to-refresh
  - Implementar atualização automática

  - Testar com dados locais e remotos
  - _Requirements: 6.3, 6.4_


- [x] 36. Validar Dashboard



  - Executar smoke tests
  - Testes manuais completos
  - Testes de performance

  - Testar rollback
  - _Requirements: 6.1, 6.5, 7.1_

### Estatísticas (2 dias)

- [ ] 37. Migrar tela de Estatísticas
  - Preparar migração

  - Migrar Activity/Fragment
  - Implementar carregamento de dados
  - Validar e testar
  - _Requirements: 6.1, 6.2_

### Configurações (2 dias)


- [ ] 38. Migrar tela de Configurações
  - Preparar migração
  - Migrar Activity
  - Implementar salvamento de preferências

  - Validar e testar
  - _Requirements: 2.1, 2.2_

### Sincronização Manual (2 dias)

- [ ] 39. Implementar tela de sincronização manual
  - Criar SyncActivity
  - Implementar indicador de progresso

  - Implementar listagem de pendências
  - Implementar botão de sincronizar tudo
  - _Requirements: 5.3, 5.4, 5.5_

- [ ] 40. Validar sincronização manual
  - Testar sync de coletas pendentes

  - Testar sync de dados offline
  - Testar indicadores visuais
  - _Requirements: 5.6, 7.4_

### Buffer e Ajustes (1 dia)


- [ ] 41. Ajustes finais das features secundárias
  - Corrigir bugs encontrados
  - Otimizar performance
  - Atualizar documentação
  - _Requirements: 10.1, 10.2_


---

## Fase 4: Cleanup e Finalização (3 dias)

### Dia 1: Remoção de Código Legado



- [ ] 42. Remover ViewModels antigos
  - Deletar DescricaoSelectionViewModel (antigo)
  - Deletar ColetaViewModel (antigo)
  - Deletar outros ViewModels legados
  - Renomear *ViewModelClean para *ViewModel
  - _Requirements: 9.4_

- [ ] 43. Remover feature flags
  - Remover classe FeatureFlags
  - Remover AdaptiveViewModelFactory
  - Remover código de toggle
  - _Requirements: 9.4_

- [ ] 44. Remover código morto
  - Identificar código não utilizado
  - Remover imports desnecessários
  - Remover comentários de código antigo
  - _Requirements: 9.4_

### Dia 2: Documentação e Testes

- [ ] 45. Atualizar documentação técnica
  - Atualizar README com nova arquitetura
  - Documentar estrutura de pastas
  - Documentar fluxo de dados
  - Criar diagramas atualizados
  - _Requirements: 9.1, 9.2, 9.4_

- [ ] 46. Criar guia de onboarding
  - Criar guia para novos desenvolvedores
  - Documentar padrões de código
  - Criar exemplos de implementação
  - _Requirements: 9.5_

- [ ] 47. Executar suite completa de testes
  - Rodar todos os unit tests
  - Rodar todos os integration tests
  - Rodar todos os smoke tests
  - Verificar cobertura de testes
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

### Dia 3: Code Review e Release

- [ ] 48. Code review final
  - Revisar toda a arquitetura
  - Verificar padrões de código
  - Verificar documentação
  - Aprovar mudanças
  - _Requirements: 9.1, 9.4_

- [ ] 49. Testes de regressão completos
  - Executar checklist de regressão
  - Testar todos os fluxos principais
  - Testar cenários edge case
  - Documentar resultados
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 50. Preparar release
  - Atualizar versão do app
  - Criar release notes
  - Preparar documentação de release
  - Criar tag no Git
  - _Requirements: 9.4_

- [ ] 51. Validação final de performance
  - Executar benchmarks finais
  - Comparar com métricas iniciais
  - Verificar se objetivos foram atingidos
  - Documentar melhorias
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [ ] 52. Celebrar e retrospectiva
  - Reunião de retrospectiva da migração
  - Documentar lições aprendidas
  - Celebrar conquistas da equipe
  - Planejar próximas melhorias
  - _Requirements: 9.1, 9.2_

---

## Tarefas Contínuas (Durante Toda a Migração)

- [ ] 53. Monitoramento de métricas
  - Monitorar crash rate diariamente
  - Monitorar performance diariamente
  - Monitorar feedback de usuários
  - Ajustar estratégia conforme necessário
  - _Requirements: 10.1, 10.2, 10.3_

- [ ] 54. Comunicação com stakeholders
  - Atualizar status semanalmente
  - Reportar problemas imediatamente
  - Compartilhar sucessos
  - _Requirements: 9.1_

- [ ] 55. Backup contínuo
  - Fazer backup de dados críticos antes de cada fase
  - Manter backups por 30 dias
  - Testar restauração de backups
  - _Requirements: 8.3, 8.4_

---

## Critérios de Aceitação Geral

Cada fase só pode avançar se:
- ✅ Todos os testes passam
- ✅ Performance está dentro dos limites aceitáveis
- ✅ Rollback foi testado e funciona
- ✅ Documentação está atualizada
- ✅ Equipe aprova a migração

## Estimativa Total

- **Fase 0**: 1 dia
- **Fase 1**: 3 dias
- **Fase 2**: 5 dias
- **Fase 3**: 10 dias (2 semanas)
- **Fase 4**: 3 dias
- **Buffer**: 2 dias

**Total**: ~24 dias úteis (~5 semanas)

## Notas Importantes

- ⚠️ Tarefas marcadas com * são opcionais e podem ser puladas se necessário
- 🔴 Se qualquer smoke test falhar, fazer rollback imediato
- 🟡 Se performance degradar > 20%, pausar e otimizar
- 🟢 Cada feature migrada deve ser validada antes de prosseguir
- 📊 Métricas devem ser coletadas em cada fase
