# Resumo - Migração de Itens Compostos

## 📊 Dados Encontrados (28/11/2025)

### tabela_item_composto
- **Total**: 1.588 registros
- **ID range**: 1 a 1.760 (há gaps)

| Tipo Componente | Quantidade | Patrimônios |
|-----------------|------------|-------------|
| CADEIRA | 707 | 707 |
| MESA | 707 | 707 |
| CPU | 87 | 87 |
| MONITOR | 87 | 87 |

### tabela_coleta_componente
- **Total**: 12 registros
- **Status**: Todos COMPLETO
- **Inventário**: 2
- **Coletor**: 1

---

## 📁 Scripts Gerados

### 1. `sql/script_migrar_itens_compostos_correto.sql`
Script principal com:
- Criação das tabelas (estrutura correta)
- Amostra de dados para teste
- Dados de coleta de componentes
- Verificações finais

### 2. `sql/atualizar_estrutura_itens_compostos.sql`
Script para atualizar estrutura:
- Detecta estrutura antiga
- Cria backup automático
- Recria tabelas com estrutura correta
- Verificação final

### 3. `sql/extrair_itens_compostos_completo.sql`
Script para extrair TODOS os dados:
- Execute no banco de ORIGEM
- Gera arquivo com todos os INSERTs
- Saída: `/tmp/itens_compostos_dados.sql`

### 4. `exportar-itens-compostos.ps1`
Script PowerShell para exportação:
- Automatiza extração de dados
- Gera arquivo SQL pronto para importar

---

## 🔧 Estrutura Real das Tabelas

### tabela_item_composto
```sql
CREATE TABLE tabela_item_composto (
    id SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL,
    tipo_componente VARCHAR(100) NOT NULL,
    descricao_componente VARCHAR(500) NOT NULL,
    quantidade_esperada INTEGER NOT NULL DEFAULT 1,
    obrigatorio BOOLEAN DEFAULT TRUE,
    observacao TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### tabela_coleta_componente
```sql
CREATE TABLE tabela_coleta_componente (
    id SERIAL PRIMARY KEY,
    id_item_composto INTEGER NOT NULL,
    id_inventario INTEGER NOT NULL,
    id_coletor INTEGER NOT NULL,
    quantidade_encontrada INTEGER NOT NULL DEFAULT 0,
    status_componente VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    observacao_coleta TEXT,
    data_coleta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🚀 Processo de Migração

### Opção 1: Migração Completa (Recomendado)

```bash
# 1. No servidor ORIGEM, extrair dados
psql -h localhost -U inventario -d sispatrimonio -f sql/extrair_itens_compostos_completo.sql

# 2. Copiar arquivo gerado para servidor DESTINO
scp /tmp/itens_compostos_dados.sql usuario@destino:/tmp/

# 3. No servidor DESTINO, atualizar estrutura
psql -h localhost -U inventario -d sispatrimonio -f sql/atualizar_estrutura_itens_compostos.sql

# 4. Importar dados
psql -h localhost -U inventario -d sispatrimonio -f /tmp/itens_compostos_dados.sql
```

### Opção 2: Usando COPY (Mais Rápido)

```bash
# No servidor ORIGEM
psql -c "COPY tabela_item_composto TO '/tmp/item_composto.csv' WITH CSV HEADER;"
psql -c "COPY tabela_coleta_componente TO '/tmp/coleta_componente.csv' WITH CSV HEADER;"

# No servidor DESTINO
psql -c "COPY tabela_item_composto FROM '/tmp/item_composto.csv' WITH CSV HEADER;"
psql -c "COPY tabela_coleta_componente FROM '/tmp/coleta_componente.csv' WITH CSV HEADER;"

# Atualizar sequências
psql -c "SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));"
psql -c "SELECT setval('tabela_coleta_componente_id_seq', (SELECT MAX(id) FROM tabela_coleta_componente));"
```

---

## ✅ Verificação Pós-Migração

```sql
-- Contar registros
SELECT 'tabela_item_composto' as tabela, COUNT(*) as total FROM tabela_item_composto
UNION ALL
SELECT 'tabela_coleta_componente', COUNT(*) FROM tabela_coleta_componente;

-- Verificar tipos de componentes
SELECT tipo_componente, COUNT(*) as quantidade
FROM tabela_item_composto
GROUP BY tipo_componente
ORDER BY quantidade DESC;

-- Verificar integridade referencial
SELECT COUNT(*) as coletas_validas
FROM tabela_coleta_componente cc
INNER JOIN tabela_item_composto ic ON cc.id_item_composto = ic.id;
```

---

## ⚠️ Notas Importantes

1. **IDs Preservados**: Os IDs são mantidos para garantir integridade referencial
2. **Backup Automático**: O script cria backup antes de modificar estrutura
3. **Idempotente**: Scripts podem ser executados múltiplas vezes com segurança
4. **Foreign Keys**: Certifique-se que `tabela_patrimonio`, `tabela_inventario` e `tabela_coletor` existem antes de importar

---

**Data**: 28/11/2025  
**Status**: ✅ Scripts prontos para uso
