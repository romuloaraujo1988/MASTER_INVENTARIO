# 🗄️ FASE 1 - Migração do Banco de Dados (Passo a Passo)

**Data:** 16/11/2025  
**Status:** 🎯 Em Execução  
**Risco:** ⚠️ MÉDIO (mexe no banco de dados)

---

## ⚠️ IMPORTANTE - Leia Antes de Começar

- ✅ Esta migração é **SEGURA** - apenas adiciona colunas
- ✅ Não altera dados existentes
- ✅ Não remove nada
- ⚠️ Mas sempre faça **BACKUP** antes!

---

## 📋 Pré-requisitos

- [ ] Acesso ao PostgreSQL
- [ ] Permissões de ALTER TABLE
- [ ] Espaço em disco para backup
- [ ] Conexão com o banco sispatrimonio

---

## 🔍 PASSO 1: Verificar Conexão com o Banco

### Windows (PowerShell)

```powershell
# Testar conexão
psql -U inventario -d sispatrimonio -c "SELECT version();"

# Se pedir senha, digite a senha do usuário 'inventario'
```

### Resultado Esperado:
```
PostgreSQL 12.x ou superior
```

### Se der erro:
- Verificar se PostgreSQL está rodando
- Verificar usuário e senha
- Verificar se banco 'sispatrimonio' existe

---

## 💾 PASSO 2: Fazer Backup do Banco (OBRIGATÓRIO)

### Opção 1: Backup Completo (Recomendado)

```powershell
# Criar pasta para backups
New-Item -ItemType Directory -Force -Path "backups"

# Fazer backup completo
$data = Get-Date -Format "yyyyMMdd_HHmmss"
pg_dump -U inventario -d sispatrimonio -F c -f "backups/sispatrimonio_backup_$data.backup"

# Verificar se backup foi criado
Get-ChildItem backups/ | Sort-Object LastWriteTime -Descending | Select-Object -First 1
```

### Opção 2: Backup SQL (Alternativo)

```powershell
$data = Get-Date -Format "yyyyMMdd_HHmmss"
pg_dump -U inventario -d sispatrimonio > "backups/sispatrimonio_backup_$data.sql"
```

### Resultado Esperado:
```
Arquivo criado: backups/sispatrimonio_backup_20251116_HHMMSS.backup
Tamanho: XX MB
```

### ✅ Validar Backup

```powershell
# Verificar tamanho do backup (deve ser > 0)
(Get-Item "backups/sispatrimonio_backup_*.backup" | Sort-Object LastWriteTime -Descending | Select-Object -First 1).Length
```

**Se o tamanho for 0 ou muito pequeno, NÃO PROSSIGA!**

---

## 🔍 PASSO 3: Verificar Estado Atual da Tabela

```powershell
# Conectar ao banco
psql -U inventario -d sispatrimonio
```

Dentro do psql:

```sql
-- Ver estrutura atual da TABELA_COLETA
\d tabela_coleta

-- Contar coletas existentes
SELECT COUNT(*) as total_coletas FROM TABELA_COLETA;

-- Ver últimas coletas
SELECT ID, NUMERO_PATRIMONIO, DATA_COLETA 
FROM TABELA_COLETA 
ORDER BY ID DESC 
LIMIT 5;
```

### Resultado Esperado:
```
total_coletas
--------------
          24  (ou outro número)

Últimas 5 coletas exibidas
```

### ✅ Anotar:
- Total de coletas: ______
- Última coleta ID: ______

---

## 🚀 PASSO 4: Executar Migração

### Opção 1: Via Arquivo SQL (Recomendado)

```sql
-- Ainda dentro do psql
\i sql/migration_v2.1_metricas_coleta.sql
```

### Opção 2: Via Comando Externo

```powershell
# Sair do psql (Ctrl+D ou \q)
# Executar arquivo
psql -U inventario -d sispatrimonio -f sql/migration_v2.1_metricas_coleta.sql
```

### Resultado Esperado:
```
ALTER TABLE
ALTER TABLE
ALTER TABLE
COMMENT
COMMENT
...
CREATE INDEX
CREATE INDEX
...
UPDATE XX (número de coletas atualizadas)
```

### ⚠️ Se der erro:
- Ler mensagem de erro
- Verificar se colunas já existem
- Verificar permissões
- **NÃO PROSSEGUIR** até resolver

---

## ✅ PASSO 5: Verificar Se Migração Funcionou

```sql
-- Conectar ao banco (se não estiver conectado)
psql -U inventario -d sispatrimonio
```

### Teste 1: Verificar Novas Colunas

```sql
-- Ver estrutura atualizada
\d tabela_coleta

-- Ou listar colunas específicas
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
  AND column_name IN (
    'tempo_coleta_segundos',
    'tempo_scan_segundos',
    'metodo_coleta',
    'tipo_scan',
    'hora_coleta',
    'periodo_coleta',
    'qualidade_etiqueta'
  )
ORDER BY column_name;
```

### Resultado Esperado:
```
column_name              | data_type         | is_nullable
-------------------------+-------------------+-------------
hora_coleta              | integer           | YES
metodo_coleta            | character varying | YES
periodo_coleta           | character varying | YES
qualidade_etiqueta       | character varying | YES
tempo_coleta_segundos    | integer           | YES
tempo_scan_segundos      | integer           | YES
tipo_scan                | character varying | YES

(7 rows)
```

### ✅ Validar:
- [ ] 11 novas colunas criadas
- [ ] Todas são nullable (YES)
- [ ] Tipos de dados corretos

### Teste 2: Verificar Índices

```sql
-- Listar índices da tabela
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'tabela_coleta'
  AND indexname LIKE 'idx_coleta_%';
```

### Resultado Esperado:
```
indexname              | indexdef
-----------------------+------------------------------------------
idx_coleta_tempo       | CREATE INDEX idx_coleta_tempo ON ...
idx_coleta_metodo      | CREATE INDEX idx_coleta_metodo ON ...
idx_coleta_tipo_scan   | CREATE INDEX idx_coleta_tipo_scan ON ...
idx_coleta_hora        | CREATE INDEX idx_coleta_hora ON ...
idx_coleta_periodo     | CREATE INDEX idx_coleta_periodo ON ...
idx_coleta_qualidade   | CREATE INDEX idx_coleta_qualidade ON ...

(6 rows)
```

### ✅ Validar:
- [ ] 6 novos índices criados

### Teste 3: Verificar Dados Existentes

```sql
-- Verificar que coletas antigas não foram afetadas
SELECT COUNT(*) as total_coletas FROM TABELA_COLETA;

-- Ver se horário foi preenchido automaticamente
SELECT 
    COUNT(*) as total,
    COUNT(HORA_COLETA) as com_hora,
    COUNT(PERIODO_COLETA) as com_periodo,
    COUNT(METODO_COLETA) as com_metodo
FROM TABELA_COLETA;
```

### Resultado Esperado:
```
total | com_hora | com_periodo | com_metodo
------+----------+-------------+------------
   24 |       24 |          24 |         24

(Todos os valores devem ser iguais ao total)
```

### ✅ Validar:
- [ ] Total de coletas igual ao anotado no PASSO 3
- [ ] Horário preenchido automaticamente
- [ ] Método preenchido como 'QR_CODE'

---

## 🧪 PASSO 6: Testar Inserção (Validação Final)

### Teste 1: Inserção SEM Métricas (Compatibilidade)

```sql
-- Inserir coleta antiga (sem métricas)
INSERT INTO TABELA_COLETA (
    ID_INVENTARIO, 
    ID_PATRIMONIO, 
    ID_COLETOR, 
    DATA_COLETA,
    STATUS_COLETA,
    NUMERO_PATRIMONIO
) VALUES (
    2,              -- ID do inventário ativo
    1,              -- ID de um patrimônio existente
    1,              -- ID de um usuário existente
    NOW(),
    'COLETADO',
    'TESTE-001'
);

-- Verificar inserção
SELECT 
    ID,
    NUMERO_PATRIMONIO,
    TEMPO_COLETA_SEGUNDOS,
    METODO_COLETA,
    HORA_COLETA,
    PERIODO_COLETA
FROM TABELA_COLETA
WHERE NUMERO_PATRIMONIO = 'TESTE-001';
```

### Resultado Esperado:
```
id | numero_patrimonio | tempo_coleta_segundos | metodo_coleta | hora_coleta | periodo_coleta
---+-------------------+-----------------------+---------------+-------------+----------------
XX | TESTE-001         | NULL                  | QR_CODE       | 13          | TARDE

(Métricas de tempo NULL, mas horário preenchido automaticamente)
```

### ✅ Validar:
- [ ] Inserção funcionou
- [ ] Campos de métrica NULL (OK)
- [ ] Horário preenchido automaticamente

### Teste 2: Inserção COM Métricas (Nova Funcionalidade)

```sql
-- Inserir coleta nova (com métricas)
INSERT INTO TABELA_COLETA (
    ID_INVENTARIO, 
    ID_PATRIMONIO, 
    ID_COLETOR, 
    DATA_COLETA,
    STATUS_COLETA,
    NUMERO_PATRIMONIO,
    TEMPO_COLETA_SEGUNDOS,
    TEMPO_SCAN_SEGUNDOS,
    METODO_COLETA,
    TIPO_SCAN,
    TENTATIVAS_SCAN,
    ERROS_SCAN,
    HORA_COLETA,
    DIA_SEMANA,
    PERIODO_COLETA,
    QUALIDADE_ETIQUETA
) VALUES (
    2,
    1,
    1,
    NOW(),
    'COLETADO',
    'TESTE-002',
    18,             -- 18 segundos total
    3,              -- 3 segundos de scan
    'QR_CODE',
    'QR_CODE',
    1,              -- 1 tentativa
    0,              -- 0 erros
    13,             -- 13h
    7,              -- Sábado
    'TARDE',
    'OTIMA'
);

-- Verificar inserção
SELECT 
    ID,
    NUMERO_PATRIMONIO,
    TEMPO_COLETA_SEGUNDOS,
    TEMPO_SCAN_SEGUNDOS,
    METODO_COLETA,
    TIPO_SCAN,
    TENTATIVAS_SCAN,
    QUALIDADE_ETIQUETA
FROM TABELA_COLETA
WHERE NUMERO_PATRIMONIO = 'TESTE-002';
```

### Resultado Esperado:
```
id | numero_patrimonio | tempo_coleta_segundos | tempo_scan_segundos | metodo_coleta | tipo_scan | tentativas_scan | qualidade_etiqueta
---+-------------------+-----------------------+---------------------+---------------+-----------+-----------------+--------------------
XX | TESTE-002         | 18                    | 3                   | QR_CODE       | QR_CODE   | 1               | OTIMA

(Todas as métricas preenchidas)
```

### ✅ Validar:
- [ ] Inserção funcionou
- [ ] Todas as métricas salvas corretamente

---

## 🧹 PASSO 7: Limpar Dados de Teste

```sql
-- Remover coletas de teste
DELETE FROM TABELA_COLETA 
WHERE NUMERO_PATRIMONIO IN ('TESTE-001', 'TESTE-002');

-- Verificar remoção
SELECT COUNT(*) FROM TABELA_COLETA 
WHERE NUMERO_PATRIMONIO LIKE 'TESTE-%';
```

### Resultado Esperado:
```
count
-------
     0
```

---

## 📊 PASSO 8: Relatório Final

```sql
-- Gerar relatório de verificação
SELECT 
    'VERIFICACAO_FINAL' as tipo,
    COUNT(*) as total_coletas,
    COUNT(TEMPO_COLETA_SEGUNDOS) as com_tempo,
    COUNT(METODO_COLETA) as com_metodo,
    COUNT(TIPO_SCAN) as com_tipo_scan,
    COUNT(HORA_COLETA) as com_hora,
    COUNT(PERIODO_COLETA) as com_periodo,
    COUNT(QUALIDADE_ETIQUETA) as com_qualidade
FROM TABELA_COLETA;
```

### Resultado Esperado:
```
tipo              | total_coletas | com_tempo | com_metodo | com_tipo_scan | com_hora | com_periodo | com_qualidade
------------------+---------------+-----------+------------+---------------+----------+-------------+---------------
VERIFICACAO_FINAL |            24 |         0 |         24 |             0 |       24 |          24 |             0

Explicação:
- total_coletas: 24 (todas as coletas)
- com_tempo: 0 (nenhuma coleta antiga tem tempo)
- com_metodo: 24 (todas preenchidas automaticamente com QR_CODE)
- com_tipo_scan: 0 (nenhuma coleta antiga tem tipo)
- com_hora: 24 (todas preenchidas automaticamente)
- com_periodo: 24 (todas preenchidas automaticamente)
- com_qualidade: 0 (nenhuma coleta antiga tem qualidade)
```

---

## ✅ Checklist de Conclusão da FASE 1

- [ ] Backup criado e validado
- [ ] Migração executada sem erros
- [ ] 11 novas colunas criadas
- [ ] 6 novos índices criados
- [ ] Coletas antigas preservadas
- [ ] Horário preenchido automaticamente
- [ ] Inserção sem métricas funciona
- [ ] Inserção com métricas funciona
- [ ] Dados de teste removidos
- [ ] Relatório final gerado

---

## 🎯 Resultado Esperado

✅ **Banco de dados atualizado e funcionando!**

- Coletas antigas: funcionam normalmente
- Coletas novas: podem incluir métricas
- Performance: não degradada (índices criados)
- Compatibilidade: 100% retroativa

---

## 🚨 Se Algo Der Errado

### Restaurar Backup

```powershell
# Parar aplicação (se estiver rodando)
# Restaurar backup
pg_restore -U inventario -d sispatrimonio -c backups/sispatrimonio_backup_YYYYMMDD_HHMMSS.backup

# Ou se for backup SQL
psql -U inventario -d sispatrimonio < backups/sispatrimonio_backup_YYYYMMDD_HHMMSS.sql
```

### Contato

- Verificar logs do PostgreSQL
- Documentar erro encontrado
- Não prosseguir para FASE 2 até resolver

---

## 📝 Próximos Passos

Após conclusão bem-sucedida da FASE 1:

✅ **FASE 1 CONCLUÍDA** → Prosseguir para **FASE 2: Atualizar Backend (Java)**

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 📋 Guia Pronto para Execução

**🚀 Pronto para começar! Siga os passos com atenção.**
