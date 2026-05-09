# Guia de Migração - Coletas Normais para Coletas Compostas

## 📋 Resumo

**Objetivo:** Converter 80 coletas normais de itens compostos em coletas compostas

**Itens Afetados:**
- 80 coletas normais
- 80 patrimônios
- 142 componentes (40 mesas + 46 cadeiras + 56 outros)

**Tipos de Itens:**
- CONJUNTO ESCOLAR (maioria)
- OSCILOSCÓPIO (6 unidades com 4 componentes cada)
- CONJUNTO DIDÁTICO (2 unidades com 4 componentes cada)
- IMPRESSORA (1 unidade com 2 componentes)

---

## 🎯 O que o Script Faz

### Fase 1: Análise e Backup
- ✅ Cria backup das 80 coletas que serão migradas
- ✅ Identifica todos os componentes de cada item composto
- ✅ Gera estatísticas detalhadas

### Fase 2: Criar Coletas Compostas
- ✅ Cria registros em `tabela_coleta_componente` para cada componente
- ✅ Registra migração em log permanente
- ✅ Mantém todos os dados originais (data, coletor, localização, observações)

### Fase 3: Atualizar Status
- ✅ Atualiza status das coletas de `'COLETADO'` para `'COLETADO_COMPOSTO'`
- ✅ Mantém rastreabilidade completa

### Fase 4: Validação
- ✅ Verifica que não restaram coletas normais de itens compostos
- ✅ Valida integridade referencial
- ✅ Confirma que todos os componentes foram criados

---

## 🚀 Como Executar

### Opção 1: Via psql (Linha de Comando)
```bash
psql -h localhost -U inventario -d sispatrimonio -f MIGRACAO_COLETAS_COMPOSTAS.sql
```

### Opção 2: Via pgAdmin
1. Abrir pgAdmin
2. Conectar ao banco `sispatrimonio`
3. Abrir Query Tool (F5)
4. Abrir arquivo `MIGRACAO_COLETAS_COMPOSTAS.sql`
5. Executar (F5 ou botão Play)

---

## ⏱️ Tempo Estimado

- **2-3 minutos** para execução completa

---

## ✅ Resultado Esperado

Ao final, você verá:

```
========================================
RELATÓRIO FINAL DE MIGRAÇÃO
========================================
BACKUP: 80 registros
COLETAS MIGRADAS: 80
COMPONENTES CRIADOS: 142
COLETAS COMPOSTAS ATUAIS: 80

COMPONENTES POR TIPO:
- MESA: 40
- CADEIRA: 46
- OUTROS: 56

========================================
MIGRAÇÃO CONCLUÍDA COM SUCESSO!
========================================

TESTE 1: Coletas normais restantes = 0 ✅
TESTE 2: Total de coletas compostas = 80 ✅
TESTE 3: Total de componentes = 142 ✅
TESTE 4: Integridade = OK - SEM ÓRFÃOS ✅
```

---

## 📊 Antes vs Depois

### ANTES da Migração
```
Coleta Normal (ID 6946):
├── Patrimônio: 303752 (CONJUNTO ESCOLAR)
├── Status: COLETADO
└── Componentes: ❌ Nenhum registrado

Item Composto:
├── Mesa (ID 1234) - ❌ Não coletada
└── Cadeira (ID 1235) - ❌ Não coletada
```

### DEPOIS da Migração
```
Coleta Composta (ID 6946):
├── Patrimônio: 303752 (CONJUNTO ESCOLAR)
├── Status: COLETADO_COMPOSTO
└── Componentes: ✅ 2 registrados

Componentes de Coleta:
├── Mesa (ID 1234) - ✅ COMPLETO
└── Cadeira (ID 1235) - ✅ COMPLETO
```

---

## 🔒 Segurança

- ✅ Tudo em **transação** (se falhar, faz rollback automático)
- ✅ **Backup** criado antes de qualquer mudança
- ✅ **Log permanente** de todas as migrações
- ✅ **Validações** automáticas ao final

---

## 📁 Tabelas Criadas

### 1. backup_coletas_pre_migracao_20260211
- Backup das 80 coletas antes da migração
- Manter por 30 dias
- Pode ser usado para rollback se necessário

### 2. log_migracao_coletas_compostas_20260211
- Log permanente de todas as migrações
- Registra: coleta_id, patrimônio, componente, tipo, data
- Manter permanentemente para auditoria

---

## 🔍 Consultas Úteis Pós-Migração

### Verificar coletas compostas
```sql
SELECT 
    c.id,
    p.numero,
    p.descricao,
    c.status_coleta,
    COUNT(cc.id) as qtd_componentes
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = c.id_inventario
WHERE c.status_coleta = 'COLETADO_COMPOSTO'
  AND c.id_inventario = 2
GROUP BY c.id, p.numero, p.descricao, c.status_coleta
ORDER BY p.numero
LIMIT 20;
```

### Verificar componentes de uma coleta específica
```sql
SELECT 
    cc.id,
    ic.tipo_componente,
    ic.descricao_componente,
    cc.quantidade_encontrada,
    cc.status_componente,
    cc.data_coleta
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
INNER JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = c.id_inventario
WHERE c.id = 6946  -- Substituir pelo ID da coleta
ORDER BY ic.tipo_componente;
```

### Verificar log de migração
```sql
SELECT 
    numero_patrimonio,
    tipo_componente,
    COUNT(*) as quantidade,
    MIN(data_migracao) as primeira_migracao,
    MAX(data_migracao) as ultima_migracao
FROM log_migracao_coletas_compostas_20260211
GROUP BY numero_patrimonio, tipo_componente
ORDER BY numero_patrimonio;
```

---

## ⚠️ Importante

### Antes de Executar
- [ ] Fazer backup completo do banco de dados
- [ ] Verificar que não há processos críticos rodando
- [ ] Notificar usuários (se necessário)

### Durante a Execução
- [ ] Monitorar saída do script
- [ ] Verificar que não há erros
- [ ] Aguardar conclusão completa

### Após a Execução
- [ ] Verificar que todos os testes passaram
- [ ] Validar algumas coletas manualmente
- [ ] Confirmar que componentes foram criados corretamente
- [ ] Documentar execução

---

## 🔄 Rollback (Se Necessário)

Se precisar desfazer a migração:

```sql
BEGIN;

-- Remover componentes criados
DELETE FROM tabela_coleta_componente
WHERE id_item_composto IN (
    SELECT id_item_composto 
    FROM log_migracao_coletas_compostas_20260211
);

-- Restaurar status das coletas
UPDATE tabela_coleta
SET status_coleta = 'COLETADO'
WHERE id IN (
    SELECT DISTINCT coleta_id 
    FROM log_migracao_coletas_compostas_20260211
);

COMMIT;

SELECT 'Rollback concluído' as status;
```

---

## 📞 Suporte

Em caso de dúvidas ou problemas:
1. Verificar logs do PostgreSQL
2. Consultar tabela `log_migracao_coletas_compostas_20260211`
3. Executar rollback se necessário
4. Contatar equipe de desenvolvimento

---

## ✅ Checklist de Execução

### Pré-Execução
- [ ] Backup completo do banco
- [ ] Validar acesso ao banco
- [ ] Revisar script

### Execução
- [ ] Executar `MIGRACAO_COLETAS_COMPOSTAS.sql`
- [ ] Verificar relatório final
- [ ] Confirmar que testes passaram

### Pós-Execução
- [ ] Validar coletas compostas
- [ ] Verificar componentes criados
- [ ] Documentar execução
- [ ] Manter backups por 30 dias

---

**Data:** 11/02/2026  
**Versão:** 1.0  
**Status:** Pronto para Execução
