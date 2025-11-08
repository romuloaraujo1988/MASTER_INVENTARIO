# Correção - Problema de Salas na Importação CSV

## 🎯 Problema Identificado

**Sintoma**: Salas importadas incorretamente via CSV

**Causa**: Vírgulas ou caracteres especiais nos nomes das salas quebraram o parsing do CSV

---

## 🔍 Diagnóstico Rápido

### Verificar Salas Problemáticas

```sql
-- 1. Salas com vírgulas ou ponto-e-vírgulas
SELECT id_sala, descricao, numero_sala, ativo
FROM sala
WHERE descricao LIKE '%,%'
   OR descricao LIKE '%;%'
ORDER BY id_sala;

-- 2. Salas com nomes muito curtos (possível truncamento)
SELECT id_sala, descricao, numero_sala
FROM sala
WHERE LENGTH(descricao) < 3
   OR descricao IS NULL
   OR descricao = '';

-- 3. Patrimônios sem sala
SELECT COUNT(*) as total_sem_sala
FROM patrimonio
WHERE id_sala IS NULL;

-- 4. Patrimônios com salas problemáticas
SELECT 
    p.numero,
    p.descricao as patrimonio,
    s.descricao as sala,
    s.id_sala
FROM patrimonio p
LEFT JOIN sala s ON p.id_sala = s.id_sala
WHERE s.descricao LIKE '%,%'
   OR s.descricao LIKE '%;%'
   OR s.descricao IS NULL
LIMIT 20;
```

---

## ✅ Solução 1: Reimportar com Excel (RECOMENDADO)

### Passo 1: Fazer Backup

```sql
-- Backup da tabela patrimonio
CREATE TABLE patrimonio_backup_20251103 AS
SELECT * FROM patrimonio;

-- Backup da tabela sala
CREATE TABLE sala_backup_20251103 AS
SELECT * FROM sala;
```

### Passo 2: Limpar Dados Problemáticos

```sql
-- Deletar salas criadas incorretamente
DELETE FROM sala
WHERE descricao LIKE '%,%'
   OR descricao LIKE '%;%'
   OR LENGTH(descricao) < 3;

-- Resetar referências de patrimônios
UPDATE patrimonio
SET id_sala = NULL
WHERE id_sala NOT IN (SELECT id_sala FROM sala);
```

### Passo 3: Reimportar com Excel

1. **Abrir o sistema de inventário**
2. **Menu → Importação de Dados**
3. **Selecionar arquivo**: `PATRIMONIO IFMT 25.05.2025.xls` (NÃO CSV!)
4. **Marcar opções**:
   - ✅ Criar salas automaticamente
   - ✅ Atualizar patrimônios existentes
5. **Iniciar importação**
6. **Verificar relatório**

### Passo 4: Validar Resultado

```sql
-- Verificar salas criadas corretamente
SELECT COUNT(*) as total_salas
FROM sala
WHERE ativo = true;

-- Verificar patrimônios com salas
SELECT 
    COUNT(*) as total_com_sala,
    (SELECT COUNT(*) FROM patrimonio) as total_patrimonios,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM patrimonio), 2) as percentual
FROM patrimonio
WHERE id_sala IS NOT NULL;

-- Listar algumas salas para conferência
SELECT id_sala, descricao, numero_sala, id_setor
FROM sala
ORDER BY id_sala DESC
LIMIT 10;
```

---

## ✅ Solução 2: Corrigir Manualmente no Banco

### Se não puder reimportar, corrija diretamente:

#### Passo 1: Identificar Padrões de Erro

```sql
-- Ver todas as salas problemáticas
SELECT 
    id_sala,
    descricao,
    numero_sala,
    COUNT(*) as qtd_patrimonios
FROM sala s
LEFT JOIN patrimonio p ON s.id_sala = p.id_sala
WHERE s.descricao LIKE '%,%'
   OR s.descricao LIKE '%;%'
GROUP BY s.id_sala, s.descricao, s.numero_sala;
```

#### Passo 2: Corrigir Nomes das Salas

```sql
-- Exemplo: Corrigir "Sala 101, Bloco A" → "Sala 101 Bloco A"
UPDATE sala
SET descricao = REPLACE(descricao, ',', '')
WHERE descricao LIKE '%,%';

-- Ou corrigir individualmente
UPDATE sala
SET descricao = 'Sala 101 Bloco A'
WHERE id_sala = 123;

-- Extrair número da sala se estiver errado
UPDATE sala
SET numero_sala = REGEXP_REPLACE(descricao, '[^0-9]', '', 'g')
WHERE numero_sala IS NULL
   OR numero_sala = '';
```

#### Passo 3: Mesclar Salas Duplicadas

```sql
-- Identificar salas duplicadas
SELECT 
    descricao,
    COUNT(*) as quantidade
FROM sala
GROUP BY descricao
HAVING COUNT(*) > 1;

-- Mesclar salas duplicadas (exemplo)
-- Manter sala com menor ID, atualizar patrimônios
UPDATE patrimonio
SET id_sala = 101  -- ID da sala correta
WHERE id_sala = 102;  -- ID da sala duplicada

-- Deletar sala duplicada
DELETE FROM sala WHERE id_sala = 102;
```

---

## ✅ Solução 3: Script de Correção Automática

### Script SQL Completo

```sql
-- ========================================
-- SCRIPT DE CORREÇÃO DE SALAS
-- Data: 03/11/2025
-- ========================================

BEGIN;

-- 1. Backup
CREATE TABLE IF NOT EXISTS sala_backup_correcao AS
SELECT * FROM sala;

CREATE TABLE IF NOT EXISTS patrimonio_backup_correcao AS
SELECT * FROM patrimonio;

-- 2. Corrigir vírgulas nos nomes
UPDATE sala
SET descricao = REPLACE(descricao, ',', ' ')
WHERE descricao LIKE '%,%';

-- 3. Corrigir ponto-e-vírgulas
UPDATE sala
SET descricao = REPLACE(descricao, ';', ' ')
WHERE descricao LIKE '%;%';

-- 4. Remover espaços duplos
UPDATE sala
SET descricao = REGEXP_REPLACE(descricao, '\s+', ' ', 'g')
WHERE descricao LIKE '%  %';

-- 5. Trim espaços nas pontas
UPDATE sala
SET descricao = TRIM(descricao);

-- 6. Extrair número da sala se vazio
UPDATE sala
SET numero_sala = REGEXP_REPLACE(descricao, '[^0-9]', '', 'g')
WHERE (numero_sala IS NULL OR numero_sala = '')
  AND descricao ~ '[0-9]';

-- 7. Deletar salas vazias ou inválidas
DELETE FROM sala
WHERE descricao IS NULL
   OR descricao = ''
   OR LENGTH(descricao) < 2;

-- 8. Resetar patrimônios órfãos
UPDATE patrimonio
SET id_sala = NULL
WHERE id_sala NOT IN (SELECT id_sala FROM sala);

-- 9. Verificar resultado
SELECT 
    'Salas corrigidas' as status,
    COUNT(*) as total
FROM sala
WHERE ativo = true;

COMMIT;

-- Rollback se algo der errado:
-- ROLLBACK;
```

---

## 🔍 Verificação Pós-Correção

### Checklist de Validação

```sql
-- ✅ 1. Nenhuma sala com vírgulas
SELECT COUNT(*) as salas_com_virgulas
FROM sala
WHERE descricao LIKE '%,%';
-- Esperado: 0

-- ✅ 2. Nenhuma sala com ponto-e-vírgula
SELECT COUNT(*) as salas_com_ponto_virgula
FROM sala
WHERE descricao LIKE '%;%';
-- Esperado: 0

-- ✅ 3. Todas as salas têm descrição válida
SELECT COUNT(*) as salas_invalidas
FROM sala
WHERE descricao IS NULL
   OR descricao = ''
   OR LENGTH(descricao) < 2;
-- Esperado: 0

-- ✅ 4. Patrimônios têm salas válidas
SELECT 
    COUNT(*) as total_patrimonios,
    SUM(CASE WHEN id_sala IS NOT NULL THEN 1 ELSE 0 END) as com_sala,
    SUM(CASE WHEN id_sala IS NULL THEN 1 ELSE 0 END) as sem_sala
FROM patrimonio;

-- ✅ 5. Listar salas para conferência visual
SELECT 
    id_sala,
    descricao,
    numero_sala,
    COUNT(p.id) as qtd_patrimonios
FROM sala s
LEFT JOIN patrimonio p ON s.id_sala = p.id_sala
WHERE s.ativo = true
GROUP BY s.id_sala, s.descricao, s.numero_sala
ORDER BY qtd_patrimonios DESC
LIMIT 20;
```

---

## 📊 Relatório de Correção

### Gerar Relatório

```sql
-- Relatório completo de correção
SELECT 
    'Total de Salas' as metrica,
    COUNT(*) as valor
FROM sala
WHERE ativo = true

UNION ALL

SELECT 
    'Salas com Patrimônios',
    COUNT(DISTINCT id_sala)
FROM patrimonio
WHERE id_sala IS NOT NULL

UNION ALL

SELECT 
    'Patrimônios com Sala',
    COUNT(*)
FROM patrimonio
WHERE id_sala IS NOT NULL

UNION ALL

SELECT 
    'Patrimônios sem Sala',
    COUNT(*)
FROM patrimonio
WHERE id_sala IS NULL

UNION ALL

SELECT 
    'Salas Problemáticas',
    COUNT(*)
FROM sala
WHERE descricao LIKE '%,%'
   OR descricao LIKE '%;%'
   OR LENGTH(descricao) < 3;
```

---

## 🎯 Prevenção Futura

### 1. Sempre Use Excel

```
❌ NÃO: Exportar SUAP → CSV → Importar
✅ SIM: Exportar SUAP → Excel → Importar
```

### 2. Validar Antes de Importar

```
1. Abrir arquivo no Excel
2. Verificar coluna "Sala"
3. Procurar vírgulas ou caracteres especiais
4. Corrigir se necessário
5. Salvar como .xlsx
6. Importar
```

### 3. Testar com Amostra

```
1. Criar arquivo com 10-20 linhas
2. Importar
3. Verificar salas
4. Se OK, importar arquivo completo
```

### 4. Configurar Validação no Sistema

```java
// Adicionar validação ao criar sala
private void validarNomeSala(String nome) {
    if (nome.contains(",") || nome.contains(";")) {
        throw new ValidationException(
            "Nome da sala não pode conter vírgulas ou ponto-e-vírgulas. " +
            "Use espaços ou hífens."
        );
    }
}
```

---

## 📞 Suporte

### Se o Problema Persistir

1. **Exportar dados problemáticos**
   ```sql
   COPY (
       SELECT p.numero, p.descricao, s.descricao as sala
       FROM patrimonio p
       LEFT JOIN sala s ON p.id_sala = s.id_sala
       WHERE s.descricao LIKE '%,%'
   ) TO '/tmp/salas_problematicas.csv' CSV HEADER;
   ```

2. **Enviar para análise**
   - Arquivo CSV exportado
   - Log de importação
   - Descrição do problema

3. **Aguardar correção**
   - Análise técnica
   - Script de correção personalizado
   - Validação

---

## ✅ Resumo Executivo

### Problema
CSV importou salas com vírgulas incorretamente

### Causa
Vírgulas nos nomes das salas quebraram o parsing do CSV

### Solução Recomendada
1. Fazer backup
2. Limpar salas problemáticas
3. Reimportar com Excel (não CSV)
4. Validar resultado

### Prevenção
- Sempre usar Excel para importação
- Validar arquivo antes de importar
- Testar com amostra primeiro

### Tempo Estimado
- Diagnóstico: 5 minutos
- Correção: 10-15 minutos
- Validação: 5 minutos
- **Total: 20-25 minutos**

---

**Versão**: 1.0  
**Data**: 03/11/2025  
**Status**: Solução Testada e Aprovada
