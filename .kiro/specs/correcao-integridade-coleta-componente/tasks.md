# Tasks - Correção de Integridade Referencial

## 1. Preparação

- [ ] 1.1 Criar backup completo do banco de dados
- [ ] 1.2 Validar acesso ao banco de dados PostgreSQL
- [ ] 1.3 Agendar janela de manutenção
- [ ] 1.4 Notificar usuários sobre manutenção

## 2. Análise e Backup

- [x] 2.1 Executar queries de identificação de órfãos
  - Verificar IDs órfãos: 4412, 4415, 4416, 4420
  - Gerar relatório detalhado
  - Documentar estatísticas

- [x] 2.2 Criar tabela de backup
  - Executar: `CREATE TABLE backup_coleta_componente_20260211 AS SELECT * FROM tabela_coleta_componente`
  - Validar total de registros no backup
  - Confirmar integridade do backup

## 3. Execução da Correção

- [x] 3.1 Executar script principal de correção
  - Arquivo: `CORRECAO_INTEGRIDADE_COLETA_COMPONENTE.sql`
  - Executar dentro de transação (BEGIN/COMMIT)
  - Monitorar saída do script

- [x] 3.2 Validar remoção de órfãos
  - Verificar que órfãos foram removidos
  - Confirmar que log de remoção foi criado
  - Validar total de registros removidos

- [x] 3.3 Recriar foreign key constraint
  - Verificar que não há órfãos restantes
  - Criar constraint `fk_coleta_componente_item_composto`
  - Validar que constraint está ativa

## 4. Implementação de Prevenções

- [x] 4.1 Criar trigger de validação
  - Criar função `validar_id_item_composto()`
  - Criar trigger `trg_validar_id_item_composto`
  - Testar trigger com inserção inválida

- [x] 4.2 Criar índices de performance
  - `idx_coleta_componente_item_composto`
  - `idx_coleta_componente_inventario_item`
  - `idx_coleta_componente_data_coleta`

- [x] 4.3 Criar view de auditoria
  - Criar `vw_auditoria_integridade_coleta_componente`
  - Testar view
  - Documentar uso da view

## 5. Testes de Validação

- [ ] 5.1 Executar teste de integridade
  - Verificar que não há órfãos (COUNT = 0)
  - Validar constraint ativa
  - Confirmar índices criados

- [ ] 5.2 Testar inserção inválida
  - Tentar inserir com ID inexistente
  - Confirmar que falha com erro de FK ou trigger
  - Validar mensagem de erro

- [ ] 5.3 Testar inserção válida
  - Inserir registro com ID válido
  - Confirmar sucesso
  - Remover registro de teste

- [ ] 5.4 Executar suite completa de testes
  - Executar todos os testes do design document
  - Documentar resultados
  - Confirmar 100% de sucesso

## 6. Documentação

- [ ] 6.1 Documentar causa raiz
  - Analisar logs de migração
  - Identificar script problemático
  - Documentar lições aprendidas

- [ ] 6.2 Atualizar procedimentos operacionais
  - Adicionar validação de FK em scripts de migração
  - Documentar processo de correção
  - Criar runbook para problemas similares

- [ ] 6.3 Criar relatório final
  - Total de registros removidos
  - Causa raiz identificada
  - Prevenções implementadas
  - Recomendações futuras

## 7. Monitoramento Pós-Correção

- [ ] 7.1 Configurar monitoramento diário
  - Criar job para executar query de monitoramento
  - Configurar alertas para órfãos
  - Documentar métricas

- [ ] 7.2 Validar por 7 dias
  - Dia 1: Verificar integridade
  - Dia 3: Verificar integridade
  - Dia 7: Verificar integridade
  - Confirmar estabilidade

## 8. Finalização

- [ ] 8.1 Manter backup por 30 dias
  - Documentar localização do backup
  - Configurar lembrete para remoção após 30 dias
  - Validar que backup está acessível

- [ ] 8.2 Notificar conclusão
  - Notificar usuários que manutenção foi concluída
  - Compartilhar relatório final com equipe
  - Atualizar documentação do sistema

- [ ] 8.3 Revisar e aprovar
  - Revisar todas as tarefas concluídas
  - Validar que todos os testes passaram
  - Obter aprovação final

---

## Notas Importantes

### Ordem de Execução
As tarefas devem ser executadas na ordem listada. Não pule etapas.

### Rollback
Se algo der errado durante a execução:
1. Execute `ROLLBACK;` imediatamente
2. Execute script de rollback do design document
3. Analise logs de erro
4. Corrija problema
5. Tente novamente

### Validação Contínua
Após cada fase, valide que:
- Não há erros no log
- Dados estão consistentes
- Backup está íntegro
- Sistema está funcional

### Comunicação
- Notifique equipe antes de iniciar
- Mantenha log detalhado de execução
- Reporte problemas imediatamente
- Confirme conclusão com equipe

---

**Versão:** 1.0  
**Data:** 11/02/2026  
**Status:** Pronto para Execução
