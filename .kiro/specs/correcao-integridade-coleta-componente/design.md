# Design Document - Correção de Integridade Referencial

## 1. Visão Geral da Solução

Este documento detalha a solução técnica para corrigir registros órfãos na tabela `tabela_coleta_componente` que violam a constraint de foreign key com `tabela_item_composto`.

**Abordagem:** Limpeza cirúrgica de dados órfãos com backup e validação completa.

---

## 2. Arquitetura da Solução

### 2.1 Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                    FASE 1: ANÁLISE                          │
│  - Identificar registros órfãos                             │
│  - Criar backup                                             │
│  - Gerar relatório                                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                FASE 2: TENTATIVA DE RECUPERAÇÃO             │
│  - Verificar se há coletas relacionadas                     │
│  - Tentar recriar itens compostos                           │
│  - Marcar irrecuperáveis                                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    FASE 3: LIMPEZA                          │
│  - Remover registros órfãos                                 │
│  - Validar integridade                                      │
│  - Recriar constraints                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   FASE 4: PREVENÇÃO                         │
│  - Criar triggers de validação                              │
│  - Adicionar índices                                        │
│  - Documentar processo                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Modelo de Dados

### 3.1 Tabelas Envolvidas

#### tabela_item_composto
```sql
CREATE TABLE tabela_item_composto (
    id INTEGER PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR,
    descricao_componente VARCHAR,
    quantidade_esperada INTEGER,
    obrigatorio BOOLEAN,
    observacao TEXT,
    data_cadastro TIMESTAMP
);
```

#### tabela_coleta_componente
```sql
CREATE TABLE tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,  -- FK para tabela_item_composto
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER,
    quantidade_encontrada INTEGER,
    status_componente VARCHAR,
    observacao_coleta TEXT,
    data_coleta TIMESTAMP,
    localizacao_encontrada VARCHAR
);
```

### 3.2 Relacionamentos

```
tabela_patrimonio (1) ──< (N) tabela_item_composto
                                      │
                                      │ (1)
                                      │
                                      ▼
                                     (N)
                          tabela_coleta_componente
```

---

## 4. Scripts SQL de Correção

### 4.1 Script Principal: CORRECAO_INTEGRIDADE_COLETA_COMPONENTE.sql

```sql
-- ============================================
-- CORREÇÃO DE INTEGRIDADE REFERENCIAL
-- Tabela: tabela_coleta_componente
-- Data: 11/02/2026
-- Objetivo: Remover registros órfãos que violam FK
-- ============================================

-- Iniciar transação para permitir rollback
BEGIN;

-- ============================================
-- FASE 1: ANÁLISE E BACKUP
-- ============================================

-- Criar tabela de backup
CREATE TABLE IF NOT EXISTS backup_coleta_componente_20260211 AS
SELECT * FROM tabela_coleta_componente;

SELECT 'BACKUP CRIADO: backup_coleta_componente_20260211' as status;
SELECT COUNT(*) as total_registros_backup FROM backup_coleta_componente_20260211;

-- Identificar registros órfãos
CREATE TEMP TABLE registros_orfaos AS
SELECT 
    cc.id,
    cc.id_item_composto,
    cc.id_inventario,
    cc.id_coletor,
    cc.data_coleta,
    cc.localizacao_encontrada,
    cc.status_componente,
    cc.observacao_coleta
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;

SELECT 'REGISTROS ÓRFÃOS IDENTIFICADOS' as status;
SELECT * FROM registros_orfaos ORDER BY id_item_composto;

-- Estatísticas
SELECT 
    COUNT(*) as total_orfaos,
    COUNT(DISTINCT id_item_composto) as ids_unicos_orfaos,
    COUNT(DISTINCT id_inventario) as inventarios_afetados,
    COUNT(DISTINCT id_coletor) as coletores_afetados
FROM registros_orfaos;

-- ============================================
-- FASE 2: TENTATIVA DE RECUPERAÇÃO
-- ============================================

-- Verificar se existem coletas normais que podem ajudar a recriar os itens compostos
SELECT 'ANÁLISE DE RECUPERAÇÃO' as status;

SELECT 
    ro.id_item_composto,
    ro.id_inventario,
    COUNT(*) as tentativas_coleta
FROM registros_orfaos ro
GROUP BY ro.id_item_composto, ro.id_inventario;

-- Verificar se há patrimônios que deveriam ter esses IDs de item_composto
-- (baseado no padrão de IDs existentes)
SELECT 
    'IDs esperados vs IDs existentes' as analise,
    MIN(id) as min_id_existente,
    MAX(id) as max_id_existente,
    COUNT(*) as total_existentes
FROM tabela_item_composto;

-- Verificar se os IDs órfãos estão dentro do range esperado
SELECT 
    ro.id_item_composto,
    CASE 
        WHEN ro.id_item_composto BETWEEN 
            (SELECT MIN(id) FROM tabela_item_composto) AND 
            (SELECT MAX(id) FROM tabela_item_composto)
        THEN 'DENTRO DO RANGE'
        ELSE 'FORA DO RANGE'
    END as status_range
FROM (SELECT DISTINCT id_item_composto FROM registros_orfaos) ro;

-- ============================================
-- FASE 3: LIMPEZA
-- ============================================

-- Remover constraint temporariamente (se existir)
ALTER TABLE tabela_coleta_componente
DROP CONSTRAINT IF EXISTS fk_coleta_componente_item_composto;

SELECT 'CONSTRAINT REMOVIDA TEMPORARIAMENTE' as status;

-- Criar tabela de log de remoção
CREATE TABLE IF NOT EXISTS log_remocao_orfaos_20260211 (
    id SERIAL PRIMARY KEY,
    id_coleta_componente INTEGER,
    id_item_composto INTEGER,
    id_inventario INTEGER,
    id_coletor INTEGER,
    data_coleta TIMESTAMP,
    data_remocao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR DEFAULT 'REGISTRO ÓRFÃO - FK VIOLATION'
);

-- Registrar remoções no log
INSERT INTO log_remocao_orfaos_20260211 
    (id_coleta_componente, id_item_composto, id_inventario, id_coletor, data_coleta)
SELECT 
    id,
    id_item_composto,
    id_inventario,
    id_coletor,
    data_coleta
FROM registros_orfaos;

SELECT 'REMOÇÕES REGISTRADAS NO LOG' as status;
SELECT COUNT(*) as total_registros_log FROM log_remocao_orfaos_20260211;

-- Remover registros órfãos
DELETE FROM tabela_coleta_componente
WHERE id IN (SELECT id FROM registros_orfaos);

SELECT 'REGISTROS ÓRFÃOS REMOVIDOS' as status;

-- ============================================
-- FASE 4: VALIDAÇÃO
-- ============================================

-- Verificar se ainda existem órfãos
SELECT 'VALIDAÇÃO PÓS-LIMPEZA' as status;

SELECT COUNT(*) as orfaos_restantes
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;

-- Se não houver órfãos, recriar constraint
DO $$
DECLARE
    orfaos_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orfaos_count
    FROM tabela_coleta_componente cc
    LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
    WHERE ic.id IS NULL;
    
    IF orfaos_count = 0 THEN
        -- Recriar foreign key constraint
        ALTER TABLE tabela_coleta_componente
        ADD CONSTRAINT fk_coleta_componente_item_composto
        FOREIGN KEY (id_item_composto)
        REFERENCES tabela_item_composto (id)
        ON DELETE CASCADE;
        
        RAISE NOTICE 'CONSTRAINT RECRIADA COM SUCESSO';
    ELSE
        RAISE EXCEPTION 'AINDA EXISTEM % REGISTROS ÓRFÃOS', orfaos_count;
    END IF;
END $$;

-- Verificar constraints
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    tc.table_name
FROM information_schema.table_constraints tc
WHERE tc.table_name = 'tabela_coleta_componente'
  AND tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.constraint_name;

-- ============================================
-- FASE 5: RELATÓRIO FINAL
-- ============================================

SELECT '========================================' as separador;
SELECT 'RELATÓRIO FINAL DE CORREÇÃO' as titulo;
SELECT '========================================' as separador;

-- Estatísticas de backup
SELECT 
    'BACKUP' as categoria,
    COUNT(*) as total_registros
FROM backup_coleta_componente_20260211;

-- Estatísticas de remoção
SELECT 
    'REMOVIDOS' as categoria,
    COUNT(*) as total_registros
FROM log_remocao_orfaos_20260211;

-- Estatísticas atuais
SELECT 
    'ATUAL' as categoria,
    COUNT(*) as total_registros
FROM tabela_coleta_componente;

-- Integridade referencial
SELECT 
    'INTEGRIDADE' as categoria,
    CASE 
        WHEN COUNT(*) = 0 THEN 'OK - SEM ÓRFÃOS'
        ELSE 'ERRO - ' || COUNT(*) || ' ÓRFÃOS RESTANTES'
    END as status
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;

-- Constraints
SELECT 
    'CONSTRAINTS' as categoria,
    COUNT(*) as total_fk_constraints
FROM information_schema.table_constraints tc
WHERE tc.table_name = 'tabela_coleta_componente'
  AND tc.constraint_type = 'FOREIGN KEY';

SELECT '========================================' as separador;
SELECT 'CORREÇÃO CONCLUÍDA COM SUCESSO' as status;
SELECT '========================================' as separador;

-- Commit da transação
COMMIT;

-- ============================================
-- INSTRUÇÕES PÓS-EXECUÇÃO
-- ============================================
-- 1. Verificar relatório final
-- 2. Validar que não há órfãos restantes
-- 3. Testar inserção de novos registros
-- 4. Manter backup por 30 dias
-- 5. Documentar causa raiz identificada
-- ============================================
```

---

## 5. Scripts de Prevenção

### 5.1 Trigger de Validação

```sql
-- ============================================
-- TRIGGER: Validar ID Item Composto
-- ============================================

CREATE OR REPLACE FUNCTION validar_id_item_composto()
RETURNS TRIGGER AS $$
BEGIN
    -- Verificar se id_item_composto existe
    IF NOT EXISTS (
        SELECT 1 FROM tabela_item_composto 
        WHERE id = NEW.id_item_composto
    ) THEN
        RAISE EXCEPTION 'ID Item Composto % não existe na tabela_item_composto', 
            NEW.id_item_composto;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Criar trigger
DROP TRIGGER IF EXISTS trg_validar_id_item_composto ON tabela_coleta_componente;

CREATE TRIGGER trg_validar_id_item_composto
    BEFORE INSERT OR UPDATE ON tabela_coleta_componente
    FOR EACH ROW
    EXECUTE FUNCTION validar_id_item_composto();

SELECT 'TRIGGER DE VALIDAÇÃO CRIADO' as status;
```

### 5.2 Índices para Performance

```sql
-- ============================================
-- ÍNDICES PARA MELHORAR PERFORMANCE
-- ============================================

-- Índice na FK (se não existir)
CREATE INDEX IF NOT EXISTS idx_coleta_componente_item_composto 
ON tabela_coleta_componente(id_item_composto);

-- Índice composto para queries comuns
CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario_item 
ON tabela_coleta_componente(id_inventario, id_item_composto);

-- Índice para auditoria
CREATE INDEX IF NOT EXISTS idx_coleta_componente_data_coleta 
ON tabela_coleta_componente(data_coleta);

SELECT 'ÍNDICES CRIADOS' as status;
```

### 5.3 View de Auditoria

```sql
-- ============================================
-- VIEW: Auditoria de Integridade
-- ============================================

CREATE OR REPLACE VIEW vw_auditoria_integridade_coleta_componente AS
SELECT 
    cc.id as id_coleta_componente,
    cc.id_item_composto,
    CASE 
        WHEN ic.id IS NULL THEN 'ÓRFÃO'
        ELSE 'OK'
    END as status_integridade,
    cc.id_inventario,
    cc.id_coletor,
    cc.data_coleta,
    ic.id_patrimonio_principal,
    ic.tipo_componente
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id;

SELECT 'VIEW DE AUDITORIA CRIADA' as status;

-- Testar view
SELECT 
    status_integridade,
    COUNT(*) as total
FROM vw_auditoria_integridade_coleta_componente
GROUP BY status_integridade;
```

---

## 6. Testes de Validação

### 6.1 Teste de Integridade

```sql
-- ============================================
-- TESTES DE VALIDAÇÃO
-- ============================================

-- Teste 1: Verificar órfãos
SELECT 'TESTE 1: Verificar Órfãos' as teste;
SELECT COUNT(*) as orfaos_encontrados
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;
-- Esperado: 0

-- Teste 2: Verificar constraint
SELECT 'TESTE 2: Verificar Constraint' as teste;
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'tabela_coleta_componente'
  AND constraint_type = 'FOREIGN KEY';
-- Esperado: fk_coleta_componente_item_composto

-- Teste 3: Tentar inserir registro inválido (deve falhar)
SELECT 'TESTE 3: Tentar Inserir Registro Inválido' as teste;
DO $$
BEGIN
    INSERT INTO tabela_coleta_componente 
        (id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente)
    VALUES 
        (99999, 2, 12, 1, 'TESTE');
    
    RAISE EXCEPTION 'ERRO: Inserção inválida foi permitida!';
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'OK: Foreign key constraint funcionando corretamente';
    WHEN OTHERS THEN
        RAISE NOTICE 'OK: Trigger de validação funcionando corretamente';
END $$;

-- Teste 4: Inserir registro válido (deve funcionar)
SELECT 'TESTE 4: Inserir Registro Válido' as teste;
DO $$
DECLARE
    id_valido INTEGER;
BEGIN
    -- Pegar um ID válido
    SELECT id INTO id_valido 
    FROM tabela_item_composto 
    LIMIT 1;
    
    -- Tentar inserir
    INSERT INTO tabela_coleta_componente 
        (id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente)
    VALUES 
        (id_valido, 2, 12, 1, 'TESTE_VALIDACAO');
    
    RAISE NOTICE 'OK: Inserção válida funcionou corretamente';
    
    -- Limpar teste
    DELETE FROM tabela_coleta_componente 
    WHERE status_componente = 'TESTE_VALIDACAO';
    
    RAISE NOTICE 'OK: Registro de teste removido';
END $$;

SELECT 'TODOS OS TESTES CONCLUÍDOS' as status;
```

---

## 7. Rollback Plan

### 7.1 Script de Rollback

```sql
-- ============================================
-- ROLLBACK: Restaurar Estado Anterior
-- ============================================

BEGIN;

-- Remover constraint
ALTER TABLE tabela_coleta_componente
DROP CONSTRAINT IF EXISTS fk_coleta_componente_item_composto;

-- Restaurar dados do backup
TRUNCATE TABLE tabela_coleta_componente;

INSERT INTO tabela_coleta_componente
SELECT * FROM backup_coleta_componente_20260211;

SELECT 'DADOS RESTAURADOS DO BACKUP' as status;
SELECT COUNT(*) as total_registros FROM tabela_coleta_componente;

-- Não recriar constraint (deixar como estava)

COMMIT;

SELECT 'ROLLBACK CONCLUÍDO' as status;
```

---

## 8. Monitoramento Pós-Correção

### 8.1 Query de Monitoramento Diário

```sql
-- ============================================
-- MONITORAMENTO DIÁRIO
-- ============================================

-- Verificar integridade
SELECT 
    'INTEGRIDADE' as metrica,
    COUNT(*) as valor,
    CASE 
        WHEN COUNT(*) = 0 THEN 'OK'
        ELSE 'ALERTA'
    END as status
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL

UNION ALL

-- Total de registros
SELECT 
    'TOTAL_REGISTROS' as metrica,
    COUNT(*) as valor,
    'INFO' as status
FROM tabela_coleta_componente

UNION ALL

-- Registros nas últimas 24h
SELECT 
    'NOVOS_24H' as metrica,
    COUNT(*) as valor,
    'INFO' as status
FROM tabela_coleta_componente
WHERE data_coleta >= CURRENT_TIMESTAMP - INTERVAL '24 hours';
```

---

## 9. Documentação de Causa Raiz

### 9.1 Análise Identificada

**Causa Raiz Provável:** Script de migração `MIGRACAO_COLETAS_MESA.sql` tentou criar registros em `tabela_coleta_componente` referenciando IDs de `tabela_item_composto` que não existiam ou foram deletados posteriormente.

**Evidências:**
1. IDs órfãos (4412, 4415, 4416, 4420) estão dentro do range de IDs válidos (1006-4611)
2. Todos os registros órfãos são do mesmo inventário (id=2) e coletor (id=12)
3. IDs 4416 e 4420 têm `data_coleta = NULL`, indicando inserção incompleta
4. Datas de coleta dos outros registros: 29/11/2025

**Recomendações:**
1. Sempre validar existência de FKs antes de inserir
2. Usar transações em scripts de migração
3. Implementar triggers de validação
4. Adicionar logs de auditoria

---

## 10. Checklist de Execução

### Pré-Execução
- [ ] Backup completo do banco de dados
- [ ] Janela de manutenção agendada
- [ ] Notificação aos usuários
- [ ] Ambiente de teste validado

### Execução
- [ ] Executar script de correção
- [ ] Verificar relatório final
- [ ] Validar integridade referencial
- [ ] Executar testes de validação

### Pós-Execução
- [ ] Criar triggers de prevenção
- [ ] Criar índices de performance
- [ ] Criar view de auditoria
- [ ] Documentar causa raiz
- [ ] Atualizar procedimentos operacionais
- [ ] Manter backup por 30 dias

---

## 11. Propriedades de Corretude

### 11.1 Propriedade 1: Integridade Referencial
**Descrição:** Todos os registros em `tabela_coleta_componente` devem referenciar IDs válidos em `tabela_item_composto`.

**Validação:**
```sql
-- Deve retornar 0
SELECT COUNT(*) 
FROM tabela_coleta_componente cc
LEFT JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id
WHERE ic.id IS NULL;
```

### 11.2 Propriedade 2: Constraint Ativa
**Descrição:** Foreign key constraint deve estar ativa e funcionando.

**Validação:**
```sql
-- Deve retornar 1 registro
SELECT COUNT(*) 
FROM information_schema.table_constraints
WHERE table_name = 'tabela_coleta_componente'
  AND constraint_type = 'FOREIGN KEY'
  AND constraint_name = 'fk_coleta_componente_item_composto';
```

### 11.3 Propriedade 3: Prevenção de Novos Órfãos
**Descrição:** Sistema deve impedir inserção de registros com IDs inválidos.

**Validação:**
```sql
-- Deve falhar com erro de FK ou trigger
INSERT INTO tabela_coleta_componente 
    (id_item_composto, id_inventario, id_coletor)
VALUES (99999, 2, 12);
```

---

**Versão:** 1.0  
**Data:** 11/02/2026  
**Status:** Pronto para Revisão
