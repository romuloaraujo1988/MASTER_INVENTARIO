# 🔍 Instruções de Debug - Modo Offline

**Data:** 20/11/2025  
**Problema:** Erro "Error parsing time stamp" persiste em modo offline

---

## 📋 Checklist de Verificação

### 1. Verificar se o Banco SQLite Existe

```bash
# Windows
dir data\inventario.db

# Se não existir, você precisa importar os dados primeiro!
```

### 2. Executar Script de Verificação

```bash
.\verificar-sqlite-salas.bat
```

**O que o script verifica:**
- ✅ Estrutura da tabela SALA
- ✅ Dados existentes
- ✅ Total de salas ativas
- ✅ Tipos de dados das colunas

### 3. Verificar Logs Detalhados

Quando abrir a tela de coleta em modo offline, procure por:

```
=== INÍCIO buscarSalasAbertasParaColeta ===
Tipo de banco detectado: sqlite (SQLite/Offline)
SQL preparado:
SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.NOME_SALA as DESCRICAO...

DEBUG criarSalaMinimalFromResultSet: Iniciando criação de Sala
  ✓ ID_SALA: 1
  ✓ NUMERO_SALA: 101
  ✓ DESCRICAO: Sala 101
  ✓ ID_SETOR: 0
  ✓ ATIVO: true
  Tentando ler DATA_CADASTRO...
  DATA_CADASTRO (Object): null (tipo: null)
  ✓ DATA_CADASTRO é NULL - usando data padrão do construtor
DEBUG criarSalaMinimalFromResultSet: Sala criada com sucesso
```

---

## 🐛 Possíveis Causas do Erro

### Causa 1: Banco SQLite Não Existe

**Sintoma:** Erro ao conectar ao banco

**Solução:**
```
1. Menu: Sistema → Importar Dados Offline
2. Aguardar importação completa
3. Tentar novamente
```

### Causa 2: Tabela SALA Não Existe

**Sintoma:** Erro "no such table: SALA"

**Solução:**
```bash
# Recriar banco SQLite
sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql
```

### Causa 3: Estrutura da Tabela Incorreta

**Sintoma:** Erro "no such column: NOME_SALA"

**Verificar:**
```sql
sqlite3 data\inventario.db "PRAGMA table_info(SALA);"
```

**Deve retornar:**
```
0|ID_SALA|INTEGER|0||1
1|NUMERO_SALA|TEXT|1||0
2|NOME_SALA|TEXT|0||0
3|ANDAR|TEXT|0||0
4|BLOCO|TEXT|0||0
5|CAPACIDADE|INTEGER|0||0
6|TIPO_SALA|TEXT|0||0
7|ATIVA|BOOLEAN|0||0
8|DATA_CADASTRO|DATETIME|0||0
```

### Causa 4: Campo DATA_CADASTRO com Formato Inválido

**Sintoma:** Erro ao parsear timestamp

**Verificar:**
```sql
sqlite3 data\inventario.db "SELECT DATA_CADASTRO FROM SALA LIMIT 5;"
```

**Formatos válidos:**
- `2025-11-20 23:45:00`
- `2025-11-20T23:45:00`
- `NULL`

**Formatos inválidos:**
- `0000-00-00 00:00:00`
- Strings vazias
- Valores corrompidos

**Correção:**
```sql
-- Limpar valores inválidos
UPDATE SALA SET DATA_CADASTRO = NULL WHERE DATA_CADASTRO = '0000-00-00 00:00:00';
UPDATE SALA SET DATA_CADASTRO = CURRENT_TIMESTAMP WHERE DATA_CADASTRO IS NULL;
```

---

## 🔧 Correções Aplicadas no Código

### 1. Detecção de Banco

```java
String dbType = conn.getMetaData().getDatabaseProductName().toLowerCase();
boolean isSQLite = dbType.contains("sqlite");
```

### 2. Query Específica para SQLite

```sql
SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA, s.NOME_SALA as DESCRICAO,
       0 as ID_SETOR, s.ATIVA as ATIVO,
       NULL AS DATA_CADASTRO
FROM SALA s
WHERE s.ATIVA = 1
ORDER BY s.NUMERO_SALA
```

### 3. Tratamento Ultra Seguro de Timestamp

```java
try {
    Object dataCadastroObj = rs.getObject("DATA_CADASTRO");
    if (dataCadastroObj != null) {
        Timestamp dataCadastro = rs.getTimestamp("DATA_CADASTRO");
        sala.setDataCadastro(dataCadastro);
    }
} catch (Exception e) {
    // Usa data padrão do construtor - NÃO lança exceção
}
```

---

## 📊 Teste Passo a Passo

### Passo 1: Forçar Modo Offline

```
1. Abrir sistema
2. Menu: Sistema → Forçar Modo Offline
3. Confirmar
```

### Passo 2: Verificar Banco

```bash
.\verificar-sqlite-salas.bat
```

**Resultado esperado:**
```
Banco encontrado: data\inventario.db
ESTRUTURA DA TABELA SALA
CREATE TABLE SALA (...)
DADOS DA TABELA SALA
1|101|Sala 101|1|2025-11-20 10:00:00
TOTAL DE SALAS ATIVAS
10
```

### Passo 3: Abrir Tela de Coleta

```
Menu: Inventário → Coleta de Dados
```

### Passo 4: Analisar Logs

Procure por:
- ✅ "Tipo de banco detectado: sqlite"
- ✅ "Query executada com sucesso"
- ✅ "Sala criada com sucesso"

Se aparecer:
- ❌ "ERRO ao ler DESCRICAO"
- ❌ "ERRO ao parsear DATA_CADASTRO"
- ❌ "no such column"

→ Execute as correções SQL acima

---

## 🆘 Se Nada Funcionar

### Opção 1: Recriar Banco SQLite

```bash
# Backup do banco atual
copy data\inventario.db data\inventario.db.backup

# Recriar estrutura
sqlite3 data\inventario.db < sql\criar_tabelas_sqlite_offline.sql

# Reimportar dados
Menu: Sistema → Importar Dados Offline
```

### Opção 2: Usar Modo Online

```
Menu: Sistema → Tentar Conectar Online
```

### Opção 3: Enviar Logs para Análise

Copie os logs do console e envie para análise:
```
1. Abrir tela de coleta
2. Copiar TODO o log do console
3. Salvar em arquivo logs-erro-offline.txt
4. Enviar para suporte
```

---

## 📝 Informações para Suporte

Se precisar de ajuda, forneça:

1. **Versão do sistema:** 2.0.0
2. **Sistema operacional:** Windows/Linux
3. **Modo:** Offline (SQLite)
4. **Logs completos** do console
5. **Resultado do script** `verificar-sqlite-salas.bat`
6. **Estrutura da tabela:**
   ```bash
   sqlite3 data\inventario.db ".schema SALA"
   ```

---

**Última atualização:** 20/11/2025  
**Status:** Em investigação
