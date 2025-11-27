# Módulo de Itens Compostos - Scripts SQL

## 📋 Descrição

Este diretório contém os scripts SQL para instalação do **Módulo de Itens Compostos** no Sistema de Inventário Patrimonial.

---

## 📁 Arquivos

### Scripts Principais

1. **`instalar_modulo_item_composto.sql`** ⭐ **USAR ESTE**
   - Script master que executa todos os outros em ordem
   - Verifica pré-requisitos
   - Exibe resumo ao final

2. **`criar_tabelas_item_composto.sql`**
   - Cria as 5 tabelas do módulo
   - Adiciona constraints e índices
   - Inclui comentários nas tabelas

3. **`criar_views_item_composto.sql`**
   - Cria 5 views otimizadas para relatórios
   - Views para estatísticas e análises

4. **`inserir_padroes_deteccao_item_composto.sql`**
   - Insere 5 padrões pré-configurados
   - Padrões: Mesa com Cadeiras, Estação de Trabalho, etc.

---

## 🚀 Como Instalar

### Opção 1: Script Master (Recomendado)

```bash
# No terminal, execute:
psql -h localhost -U inventario -d sispatrimonio -f sql/instalar_modulo_item_composto.sql
```

### Opção 2: Scripts Individuais

```bash
# 1. Criar tabelas
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabelas_item_composto.sql

# 2. Criar views
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_views_item_composto.sql

# 3. Inserir padrões
psql -h localhost -U inventario -d sispatrimonio -f sql/inserir_padroes_deteccao_item_composto.sql
```

### Opção 3: Via PowerShell (Windows)

```powershell
# Criar script de instalação
$env:PGPASSWORD = "sua_senha"
psql -h localhost -U inventario -d sispatrimonio -f sql/instalar_modulo_item_composto.sql
```

---

## ✅ Pré-requisitos

Antes de executar os scripts, certifique-se de que as seguintes tabelas existem:

- ✅ `TABELA_PATRIMONIO`
- ✅ `TABELA_INVENTARIO`
- ✅ `TABELA_COLETA`
- ✅ `TABELA_USUARIO`
- ✅ `TABELA_SALA`
- ✅ `TABELA_SETOR`
- ✅ `TABELA_RESPONSAVEL`

**Nota:** O script master verifica automaticamente esses pré-requisitos.

---

## 📊 O Que Será Criado

### Tabelas (5)

1. **TABELA_ITEM_COMPOSTO**
   - Marca patrimônios como compostos
   - Relaciona 1:1 com TABELA_PATRIMONIO

2. **TABELA_COMPONENTE**
   - Define componentes esperados
   - Relaciona 1:N com TABELA_ITEM_COMPOSTO

3. **TABELA_COMPONENTE_COLETA**
   - Registra componentes encontrados
   - Relaciona com TABELA_COLETA + TABELA_INVENTARIO

4. **TABELA_PADRAO_DETECCAO**
   - Configura padrões de detecção automática

5. **TABELA_COMPONENTE_PADRAO**
   - Define componentes de cada padrão

### Views (5)

1. **VIEW_ITEM_COMPOSTO_RESUMO**
   - Informações completas do item composto

2. **VIEW_COMPONENTE_STATUS_INVENTARIO**
   - Status de cada componente por inventário

3. **VIEW_ITEM_COMPOSTO_INTEGRIDADE**
   - Taxa de integridade por inventário

4. **VIEW_ESTATISTICA_COMPONENTE_TIPO**
   - Estatísticas agregadas por tipo

5. **VIEW_DESCRICAO_AGRUPADA**
   - Agrupa patrimônios por descrição (para aplicação em lote)

### Padrões de Detecção (5)

1. **Mesa com Cadeiras** (prioridade 10)
   - Detecta: "MESA COM CADEIRA", "MESA E CADEIRA"
   - Componentes: 1 mesa + 4 cadeiras

2. **Estação de Trabalho** (prioridade 9)
   - Detecta: "ESTAÇÃO DE TRABALHO", "WORKSTATION"
   - Componentes: 1 mesa + 1 cadeira + 1 computador

3. **Computador Completo** (prioridade 8)
   - Detecta: "COMPUTADOR COM MONITOR", "PC COMPLETO"
   - Componentes: 1 computador + 1 monitor + 1 teclado + 1 mouse

4. **Conjunto de Laboratório** (prioridade 7)
   - Detecta: "CONJUNTO LABORATÓRIO", "BANCADA COMPLETA"
   - Componentes: 1 bancada + 2 banquetas

5. **Armário com Gavetas** (prioridade 6)
   - Detecta: "ARMÁRIO COM GAVETA", "GAVETEIRO"
   - Componentes: 1 armário + 4 gavetas

---

## 🔍 Verificação Pós-Instalação

### Verificar Tabelas Criadas

```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_name LIKE '%item_composto%' 
   OR table_name LIKE '%componente%'
   OR table_name LIKE '%padrao_deteccao%'
ORDER BY table_name;
```

### Verificar Views Criadas

```sql
SELECT table_name 
FROM information_schema.views 
WHERE table_name LIKE 'view_%item_composto%'
   OR table_name LIKE 'view_%componente%'
   OR table_name LIKE 'view_%descricao%'
ORDER BY table_name;
```

### Verificar Padrões Inseridos

```sql
SELECT 
    nome,
    tipo_padrao,
    prioridade,
    ativo
FROM tabela_padrao_deteccao
ORDER BY prioridade DESC;
```

### Verificar Componentes dos Padrões

```sql
SELECT 
    pd.nome AS padrao,
    cp.tipo,
    cp.descricao,
    cp.quantidade_padrao
FROM tabela_componente_padrao cp
JOIN tabela_padrao_deteccao pd ON cp.id_padrao_deteccao = pd.id
ORDER BY pd.prioridade DESC, cp.ordem;
```

---

## 🗑️ Desinstalação (Se Necessário)

**⚠️ ATENÇÃO: Isso removerá TODOS os dados do módulo!**

```sql
-- Remover tabelas (cascade remove views e dados)
DROP TABLE IF EXISTS TABELA_COMPONENTE_COLETA CASCADE;
DROP TABLE IF EXISTS TABELA_COMPONENTE CASCADE;
DROP TABLE IF EXISTS TABELA_ITEM_COMPOSTO CASCADE;
DROP TABLE IF EXISTS TABELA_COMPONENTE_PADRAO CASCADE;
DROP TABLE IF EXISTS TABELA_PADRAO_DETECCAO CASCADE;

-- Remover views (se não foram removidas pelo cascade)
DROP VIEW IF EXISTS VIEW_ITEM_COMPOSTO_RESUMO CASCADE;
DROP VIEW IF EXISTS VIEW_COMPONENTE_STATUS_INVENTARIO CASCADE;
DROP VIEW IF EXISTS VIEW_ITEM_COMPOSTO_INTEGRIDADE CASCADE;
DROP VIEW IF EXISTS VIEW_ESTATISTICA_COMPONENTE_TIPO CASCADE;
DROP VIEW IF EXISTS VIEW_DESCRICAO_AGRUPADA CASCADE;
```

---

## 📝 Notas Importantes

1. **Backup:** Sempre faça backup antes de executar scripts em produção
2. **Permissões:** Certifique-se de ter permissões adequadas no banco
3. **Ordem:** Os scripts devem ser executados na ordem correta
4. **Idempotência:** Os scripts podem ser executados múltiplas vezes (usam `IF NOT EXISTS` e `ON CONFLICT`)
5. **Isolamento:** O módulo é completamente isolado, não modifica tabelas existentes

---

## 🐛 Troubleshooting

### Erro: "relation does not exist"
**Causa:** Tabelas principais do sistema não foram criadas  
**Solução:** Execute primeiro `IMPLEMENTAR_TABELAS_COMPLETO.sql`

### Erro: "permission denied"
**Causa:** Usuário sem permissões adequadas  
**Solução:** Execute como superusuário ou conceda permissões

### Erro: "duplicate key value"
**Causa:** Padrões já foram inseridos anteriormente  
**Solução:** Normal, o script usa `ON CONFLICT DO NOTHING`

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs do PostgreSQL
2. Execute as queries de verificação acima
3. Consulte a documentação completa em `.kiro/specs/itens-compostos/`

---

**Versão:** 1.0  
**Data:** 27/11/2025  
**Status:** ✅ Pronto para Uso
