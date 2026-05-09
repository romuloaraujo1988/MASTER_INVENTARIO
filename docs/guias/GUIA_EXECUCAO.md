# Guia de Execução - Correção de Integridade Referencial

## 📋 Resumo

**Problema:** 4 registros órfãos na `tabela_coleta_componente` violando foreign key constraint

**Registros Órfãos Identificados:**
- ID 71:  id_item_composto=4412, inventario=2, coletor=12, data=2025-11-29 13:33:07
- ID 102: id_item_composto=4415, inventario=2, coletor=12, data=2025-11-29 13:48:04
- ID 105: id_item_composto=4416, inventario=2, coletor=12, data=NULL
- ID 155: id_item_composto=4420, inventario=2, coletor=12, data=NULL

**Solução:** Remover registros órfãos, recriar constraint e implementar prevenções

---

## 🚀 Ordem de Execução

### 1. Script Principal de Correção
```bash
psql -h localhost -U inventario -d sispatrimonio -f CORRECAO_INTEGRIDADE_COLETA_COMPONENTE.sql
```

**O que faz:**
- ✅ Cria backup da tabela
- ✅ Identifica registros órfãos
- ✅ Remove registros órfãos (IDs: 71, 102, 105, 155)
- ✅ Recria foreign key constraint
- ✅ Gera relatório completo

**Tempo estimado:** 1-2 minutos

**Resultado esperado:**
```
BACKUP CRIADO: backup_coleta_componente_20260211
REGISTROS ÓRFÃOS IDENTIFICADOS: 4
REGISTROS ÓRFÃOS REMOVIDOS
CONSTRAINT RECRIADA COM SUCESSO
CORREÇÃO CONCLUÍDA COM SUCESSO
```

---

### 2. Script de Prevenção
```bash
psql -h localhost -U inventario -d sispatrimonio -f PREVENCAO_ORFAOS.sql
```

**O que faz:**
- ✅ Cria trigger de validação (impede inserção de IDs inválidos)
- ✅ Cria índices de performance
- ✅ Cria view de auditoria
- ✅ Cria view de monitoramento diário

**Tempo estimado:** 30 segundos

**Resultado esperado:**
```
TRIGGER DE VALIDAÇÃO CRIADO
ÍNDICES CRIADOS
VIEW DE AUDITORIA CRIADA
VIEW DE MONITORAMENTO CRIADA
PREVENÇÕES IMPLEMENTADAS
```

---

### 3. Script de Testes
```bash
psql -h localhost -U inventario -d sispatrimonio -f TESTES_VALIDACAO.sql
```

**O que faz:**
- ✅ Verifica que não há órfãos (COUNT = 0)
- ✅ Verifica constraint ativa
- ✅ Testa inserção inválida (deve falhar)
- ✅ Testa inserção válida (deve funcionar)
- ✅ Verifica índices, views e trigger

**Tempo estimado:** 1 minuto

**Resultado esperado:**
```
TESTE 1: orfaos_encontrados = 0 ✅
TESTE 2: constraint encontrada ✅
TESTE 3: inserção inválida bloqueada ✅
TESTE 4: inserção válida funcionou ✅
TODOS OS TESTES CONCLUÍDOS
```

---

## ⚠️ Em Caso de Problema

Se algo der errado durante a execução do script principal:

### Opção 1: Rollback Automático
Se o script falhar, a transação será revertida automaticamente (ROLLBACK).

### Opção 2: Rollback Manual
```bash
psql -h localhost -U inventario -d sispatrimonio -f ROLLBACK_SCRIPT.sql
```

**O que faz:**
- Restaura dados do backup
- Remove constraint
- Remove prevenções (opcional)
- Mantém logs para auditoria

---

## 📊 Validação Pós-Execução

### Query Rápida de Validação
```sql
-- Verificar integridade
SELECT COUNT(*) as orfaos
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;
-- Esperado: 0

-- Verificar constraint
SELECT constraint_name
FROM information_schema.table_constraints
WHERE table_name = 'tabela_coleta_componente'
  AND constraint_type = 'FOREIGN KEY';
-- Esperado: fk_coleta_componente_item_composto
```

### Monitoramento Diário
```sql
-- Usar view criada
SELECT * FROM vw_monitoramento_integridade_diario;
```

---

## 📁 Arquivos Criados

### Scripts SQL
1. **CORRECAO_INTEGRIDADE_COLETA_COMPONENTE.sql** - Script principal
2. **PREVENCAO_ORFAOS.sql** - Triggers, índices e views
3. **TESTES_VALIDACAO.sql** - Suite de testes
4. **ROLLBACK_SCRIPT.sql** - Desfazer correção (emergência)

### Tabelas Criadas no Banco
1. **backup_coleta_componente_20260211** - Backup completo (manter 30 dias)
2. **log_remocao_orfaos_20260211** - Log de remoções (manter permanente)

### Views Criadas
1. **vw_auditoria_integridade_coleta_componente** - Auditoria de integridade
2. **vw_monitoramento_integridade_diario** - Monitoramento diário

### Triggers Criados
1. **trg_validar_id_item_composto** - Valida IDs antes de inserir/atualizar

### Índices Criados
1. **idx_coleta_componente_item_composto** - FK index
2. **idx_coleta_componente_inventario_item** - Composite index
3. **idx_coleta_componente_data_coleta** - Audit index

---

## ✅ Checklist de Execução

### Pré-Execução
- [ ] Backup completo do banco de dados
- [ ] Notificar usuários (se necessário)
- [ ] Validar acesso ao banco

### Execução
- [ ] Executar CORRECAO_INTEGRIDADE_COLETA_COMPONENTE.sql
- [ ] Verificar relatório final (sem erros)
- [ ] Executar PREVENCAO_ORFAOS.sql
- [ ] Executar TESTES_VALIDACAO.sql
- [ ] Confirmar que todos os testes passaram

### Pós-Execução
- [ ] Validar integridade (0 órfãos)
- [ ] Testar inserção de nova coleta
- [ ] Documentar execução
- [ ] Notificar conclusão

---

## 📝 Documentação de Causa Raiz

**Causa Identificada:** Script de migração `MIGRACAO_COLETAS_MESA.sql` criou referências a IDs de `tabela_item_composto` que não existiam.

**Evidências:**
- Todos os órfãos são do mesmo inventário (2) e coletor (12)
- IDs 4416 e 4420 têm data_coleta = NULL (inserção incompleta)
- IDs estão dentro do range válido (1006-4611)

**Prevenções Implementadas:**
1. ✅ Trigger de validação (impede novos órfãos)
2. ✅ Foreign key constraint com ON DELETE CASCADE
3. ✅ Views de auditoria e monitoramento
4. ✅ Índices para performance

---

## 🔍 Monitoramento Contínuo

### Diário
```sql
SELECT * FROM vw_monitoramento_integridade_diario;
```

### Semanal
```sql
SELECT * FROM vw_auditoria_integridade_coleta_componente
WHERE status_integridade = 'ÓRFÃO';
```

### Mensal
```sql
-- Verificar log de remoções
SELECT 
    DATE_TRUNC('month', data_remocao) as mes,
    COUNT(*) as total_remocoes
FROM log_remocao_orfaos_20260211
GROUP BY mes
ORDER BY mes DESC;
```

---

## 📞 Suporte

Em caso de dúvidas ou problemas:
1. Verificar logs do PostgreSQL
2. Consultar tabela `log_remocao_orfaos_20260211`
3. Executar `ROLLBACK_SCRIPT.sql` se necessário
4. Contatar equipe de desenvolvimento

---

**Data:** 11/02/2026  
**Versão:** 1.0  
**Status:** Pronto para Execução
