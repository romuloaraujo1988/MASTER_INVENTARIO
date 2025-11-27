---
inclusion: always
---

# Database Verification - MCP PostgreSQL

## 🎯 Regra de Steering

**SEMPRE que precisar verificar a estrutura ou dados do banco PostgreSQL, use o MCP PostgreSQL.**

---

## 📋 Comandos Úteis

### Verificar Estrutura de Tabela

```sql
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'nome_da_tabela'
ORDER BY ordinal_position;
```

### Verificar Constraints

```sql
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'nome_da_tabela';
```

### Verificar Foreign Keys

```sql
SELECT 
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.table_name = 'nome_da_tabela' 
AND tc.constraint_type = 'FOREIGN KEY';
```

### Verificar Índices

```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'nome_da_tabela';
```

### Verificar Dados

```sql
-- Últimos registros
SELECT * FROM nome_da_tabela 
ORDER BY id DESC 
LIMIT 10;

-- Contar registros
SELECT COUNT(*) FROM nome_da_tabela;

-- Verificar valores NULL
SELECT column_name, COUNT(*) as nulls
FROM nome_da_tabela
WHERE column_name IS NULL
GROUP BY column_name;
```

---

## 🚫 NÃO Fazer

❌ **NÃO** tentar usar `psql` diretamente (pode não estar no PATH)  
❌ **NÃO** assumir estrutura sem verificar  
❌ **NÃO** confiar apenas em scripts SQL antigos  

---

## ✅ Fazer

✅ **SEMPRE** usar MCP para verificar estrutura real  
✅ **SEMPRE** verificar tipos de dados e constraints  
✅ **SEMPRE** verificar se colunas são nullable  
✅ **SEMPRE** documentar diferenças encontradas  

---

## 📝 Exemplo de Uso

### Situação
Preciso verificar se a coluna `id_participante_inventario` existe na tabela `tabela_coleta`.

### Ação
```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
AND column_name = 'id_participante_inventario';
```

### Resultado
```json
{
  "column_name": "id_participante_inventario",
  "data_type": "integer",
  "is_nullable": "NO"
}
```

### Conclusão
✅ Coluna existe  
✅ É do tipo integer  
✅ É NOT NULL (obrigatória)  

---

## 🔧 Tabelas Principais do Sistema

### Inventário
- `tabela_inventario`
- `tabela_participante_inventario`

### Patrimônio
- `tabela_patrimonio`
- `tabela_sala`
- `tabela_setor`
- `tabela_responsavel`

### Coleta
- `tabela_coleta` ⭐ **MAIS CRÍTICA**
- `tabela_coletor`

### Usuários
- `tabela_usuario`
- `tabela_perfil`

---

## 📊 Verificações Comuns

### Antes de Sincronizar
```sql
-- Verificar se IDs existem
SELECT COUNT(*) FROM tabela_patrimonio WHERE id = ?;
SELECT COUNT(*) FROM tabela_inventario WHERE id = ?;
SELECT COUNT(*) FROM tabela_participante_inventario WHERE id = ?;
```

### Após Sincronizar
```sql
-- Verificar última coleta
SELECT * FROM tabela_coleta 
ORDER BY id DESC 
LIMIT 1;

-- Contar coletas por inventário
SELECT id_inventario, COUNT(*) as total
FROM tabela_coleta
GROUP BY id_inventario;
```

### Diagnóstico de Problemas
```sql
-- Coletas sem patrimônio
SELECT COUNT(*) FROM tabela_coleta 
WHERE id_patrimonio IS NULL;

-- Coletas duplicadas
SELECT id_inventario, id_patrimonio, COUNT(*) as duplicatas
FROM tabela_coleta
GROUP BY id_inventario, id_patrimonio
HAVING COUNT(*) > 1;

-- Coletas com participante inválido
SELECT COUNT(*) FROM tabela_coleta c
LEFT JOIN tabela_participante_inventario p 
ON c.id_participante_inventario = p.id
WHERE p.id IS NULL;
```

---

## 🎓 Lições Aprendidas

### 26/11/2025 - Análise de Sincronização

**Problema:** Coletas não sincronizavam  
**Causa:** `id_participante = 0` no SQLite  
**Solução:** Buscar ID correto via DAO  

**Descoberta via MCP:**
- Confirmado que `id_participante_inventario` existe no PostgreSQL
- Confirmado que é NOT NULL
- Identificado que SQLite usa nome diferente (`id_participante`)

**Conclusão:** MCP foi essencial para entender a estrutura real e diagnosticar o problema.

---

**Última atualização:** 26/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Ativo
