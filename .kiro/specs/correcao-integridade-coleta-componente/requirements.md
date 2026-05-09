# Correção de Integridade Referencial - Coleta de Componentes

## 1. Visão Geral

**Problema Identificado:** Existem registros órfãos na tabela `tabela_coleta_componente` que referenciam IDs inexistentes na tabela `tabela_item_composto`, causando violação de foreign key constraint.

**IDs Órfãos Identificados:**
- 4412
- 4415
- 4416
- 4420

**Impacto:** Impossibilidade de criar ou atualizar registros em `tabela_coleta_componente` devido à violação da constraint `fk_coleta_componente_item_composto`.

---

## 2. User Stories

### 2.1 Como Administrador do Sistema
**Eu quero** limpar registros órfãos da tabela de coleta de componentes  
**Para que** a integridade referencial do banco de dados seja restaurada  
**E** novas coletas de componentes possam ser registradas sem erros

**Critérios de Aceitação:**
- Todos os registros órfãos devem ser identificados
- Registros órfãos devem ser removidos ou corrigidos
- Foreign key constraint deve funcionar corretamente após a correção
- Nenhum dado válido deve ser perdido

### 2.2 Como Desenvolvedor
**Eu quero** entender a causa raiz dos registros órfãos  
**Para que** possamos prevenir que isso aconteça novamente no futuro  
**E** implementar validações adequadas

**Critérios de Aceitação:**
- Causa raiz documentada
- Validações implementadas para prevenir novos registros órfãos
- Logs de auditoria para rastrear operações futuras

---

## 3. Requisitos Funcionais

### 3.1 Identificação de Registros Órfãos
- **RF-01:** O sistema deve identificar todos os registros em `tabela_coleta_componente` que referenciam IDs inexistentes em `tabela_item_composto`
- **RF-02:** O sistema deve gerar um relatório detalhado dos registros órfãos antes da remoção

### 3.2 Limpeza de Dados
- **RF-03:** O sistema deve remover registros órfãos que não podem ser recuperados
- **RF-04:** O sistema deve criar backup dos registros antes da remoção
- **RF-05:** O sistema deve validar a integridade referencial após a limpeza

### 3.3 Prevenção de Futuros Problemas
- **RF-06:** O sistema deve validar a existência de `id_item_composto` antes de inserir em `tabela_coleta_componente`
- **RF-07:** O sistema deve registrar logs de auditoria para operações de inserção/atualização

---

## 4. Requisitos Não Funcionais

### 4.1 Segurança de Dados
- **RNF-01:** Backup completo deve ser criado antes de qualquer operação de limpeza
- **RNF-02:** Operações devem ser executadas em transação para permitir rollback

### 4.2 Performance
- **RNF-03:** A operação de limpeza deve ser executada em horário de baixo uso
- **RNF-04:** Índices devem ser recriados após a limpeza se necessário

### 4.3 Auditoria
- **RNF-05:** Todas as operações devem ser registradas em log
- **RNF-06:** Relatório de execução deve ser gerado ao final

---

## 5. Análise da Causa Raiz

### 5.1 Possíveis Causas
1. **Migração Incompleta:** Script de migração `MIGRACAO_COLETAS_MESA.sql` pode ter criado referências antes dos registros de `tabela_item_composto` existirem
2. **Deleção em Cascata Faltando:** Registros de `tabela_item_composto` podem ter sido deletados sem remover os componentes relacionados
3. **Inserção Manual:** Dados podem ter sido inseridos manualmente com IDs incorretos
4. **Problema de Sincronização:** Em ambiente distribuído, pode haver problema de sincronização entre tabelas

### 5.2 Dados dos Registros Órfãos
```
ID 4412: inventario=2, coletor=12, data=2025-11-29 13:33:07
ID 4415: inventario=2, coletor=12, data=2025-11-29 13:48:04
ID 4416: inventario=2, coletor=12, data=NULL
ID 4420: inventario=2, coletor=12, data=NULL
```

**Observação:** IDs 4416 e 4420 têm `data_coleta = NULL`, indicando possível inserção incompleta.

---

## 6. Estratégia de Correção

### 6.1 Fase 1: Análise e Backup
1. Identificar todos os registros órfãos
2. Criar backup da tabela `tabela_coleta_componente`
3. Gerar relatório detalhado

### 6.2 Fase 2: Tentativa de Recuperação
1. Verificar se existem coletas normais relacionadas que podem ser usadas para recriar os itens compostos
2. Se possível, recriar registros em `tabela_item_composto`
3. Se não possível, marcar para remoção

### 6.3 Fase 3: Limpeza
1. Remover registros órfãos que não podem ser recuperados
2. Validar integridade referencial
3. Recriar foreign key constraint se necessário

### 6.4 Fase 4: Prevenção
1. Adicionar triggers de validação
2. Implementar validação em nível de aplicação
3. Documentar processo correto de inserção

---

## 7. Critérios de Sucesso

- ✅ Todos os registros órfãos identificados e documentados
- ✅ Backup criado com sucesso
- ✅ Registros órfãos removidos ou corrigidos
- ✅ Foreign key constraint funcionando corretamente
- ✅ Nenhuma violação de integridade referencial
- ✅ Validações implementadas para prevenir futuros problemas
- ✅ Documentação completa do processo

---

## 8. Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Perda de dados válidos | Baixa | Alto | Backup completo antes da operação |
| Quebra de outras funcionalidades | Média | Médio | Testes em ambiente de desenvolvimento primeiro |
| Novos registros órfãos durante a correção | Baixa | Médio | Executar em horário de manutenção |
| Rollback necessário | Baixa | Alto | Usar transações e manter backup |

---

## 9. Dependências

- Acesso ao banco de dados PostgreSQL
- Permissões de DBA para executar operações DDL/DML
- Janela de manutenção para execução segura
- Backup do banco de dados atualizado

---

## 10. Próximos Passos

1. Revisar e aprovar este documento de requisitos
2. Criar design document com scripts SQL detalhados
3. Testar scripts em ambiente de desenvolvimento
4. Agendar janela de manutenção
5. Executar correção em produção
6. Validar resultados
7. Implementar prevenções

---

**Data de Criação:** 11/02/2026  
**Versão:** 1.0  
**Status:** Aguardando Aprovação
