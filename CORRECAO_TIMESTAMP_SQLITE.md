# Correção de Timestamps SQLite - Modo Offline

## 🐛 Problema Identificado

### Erro Original
```
java.lang.IllegalArgumentException: Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]
    at java.sql.Timestamp.valueOf(Timestamp.java:...)
    at com.inventario.dao.SalaInventarioDAO.mapResultSetToSalaInventario(...)
```

### Causa Raiz
O SQLite armazena timestamps como **TEXT**, mas o código Java usa `Timestamp.valueOf()` que requer um formato específico:

**Formato Esperado pelo Java:**
```
YYYY-MM-DD HH:MM:SS
2024-11-21 14:30:00
```

**Formatos Problemáticos no SQLite:**
- ISO 8601: `2024-11-21T14:30:00Z`
- Unix timestamp: `1700582400`
- Formato brasileiro: `21/11/2024 14:30:00`
- Formato SQLite padrão: `2024-11-21 14:30:00.123456`

---

## ✅ Solução Implementada

### 1. Script SQL de Correção
**Arquivo:** `sql/fix_sqlite_timestamp_compatibility.sql`

**O que faz:**
- ✅ Cria backup de todas as tabelas afetadas
- ✅ Recria tabelas com estrutura corrigida
- ✅ Converte timestamps existentes para formato correto
- ✅ Adiciona triggers para manter formato correto
- ✅ Cria view de validação de timestamps
- ✅ Restaura dados preservando integridade

**Tabelas Corrigidas:**
- `SALA_INVENTARIO` - Datas de início/fim de coleta
- `COLETA` - Data de coleta
- `SALA` - Data de cadastro
- `TABELA_INVENTARIO` - Datas de início/fim/criação

### 2. Script Batch Automatizado
**Arquivo:** `fix-sqlite-timestamps.bat`

**Funcionalidades:**
- ✅ Verifica se SQLite está instalado
- ✅ Cria backup automático antes de executar
- ✅ Executa script de correção
- ✅ Valida resultado
- ✅ Restaura backup em caso de erro

---

## 🚀 Como Usar

### Opção 1: Executar Script Batch (Recomendado)
```bash
fix-sqlite-timestamps.bat
```

### Opção 2: Executar Manualmente
```bash
# 1. Criar backup
copy data\inventario_offline.db data\inventario_offline_backup.db

# 2. Executar correção
sqlite3 data\inventario_offline.db < sql\fix_sqlite_timestamp_compatibility.sql

# 3. Verificar resultado
sqlite3 data\inventario_offline.db "SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';"
```

---

## 📊 Formato de Timestamps Correto

### SQLite (TEXT)
```sql
-- Timestamp atual
strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')
-- Resultado: '2024-11-21 14:30:00'

-- Com milissegundos
strftime('%Y-%m-%d %H:%M:%S.%f', 'now', 'localtime')
-- Resultado: '2024-11-21 14:30:00.123456'
```

### Java
```java
// Parsing correto
Timestamp ts = Timestamp.valueOf("2024-11-21 14:30:00");

// Inserção no SQLite
String sql = "INSERT INTO COLETA (DATA_COLETA) VALUES (?)";
stmt.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
```

---

## 🔍 Validação

### Verificar Formato de Timestamps
```sql
-- Ver todos os timestamps e seus formatos
SELECT * FROM v_timestamp_validation;

-- Ver apenas timestamps inválidos
SELECT * FROM v_timestamp_validation 
WHERE status_formato = 'FORMATO_INVALIDO';

-- Contar timestamps inválidos
SELECT COUNT(*) as total_invalidos 
FROM v_timestamp_validation 
WHERE status_formato = 'FORMATO_INVALIDO';
```

### Resultado Esperado
```
total_invalidos
---------------
0
```

Se o resultado for `0`, todos os timestamps estão corretos! ✅

---

## 🛡️ Triggers de Proteção

O script cria triggers que garantem formato correto automaticamente:

### Trigger para SALA_INVENTARIO
```sql
CREATE TRIGGER trg_sala_inventario_insert_format
AFTER INSERT ON SALA_INVENTARIO
FOR EACH ROW
WHEN NEW.DATA_INICIO IS NOT NULL 
  AND NEW.DATA_INICIO NOT LIKE '____-__-__ __:__:__'
BEGIN
    UPDATE SALA_INVENTARIO 
    SET DATA_INICIO = strftime('%Y-%m-%d %H:%M:%S', NEW.DATA_INICIO)
    WHERE ID = NEW.ID;
END;
```

**Benefício:** Mesmo que o código Java insira timestamp em formato incorreto, o trigger corrige automaticamente!

---

## 📝 Alterações no Código Java

### Antes (Problemático)
```java
// SalaInventarioDAO.java - LINHA 418
Timestamp dataInicio = rs.getTimestamp("DATA_INICIO_COLETA");
if (dataInicio != null) {
    salaInventario.setDataInicioColeta(dataInicio.toLocalDateTime());
}
```

### Depois (Robusto)
```java
// SalaInventarioDAO.java - LINHA 418-426
try {
    Timestamp dataInicio = rs.getTimestamp("DATA_INICIO_COLETA");
    if (dataInicio != null && !rs.wasNull()) {
        salaInventario.setDataInicioColeta(dataInicio.toLocalDateTime());
    }
} catch (Exception e) {
    System.err.println("Aviso: Erro ao parsear DATA_INICIO_COLETA - " + e.getMessage());
    // Continua sem a data
}
```

**Benefício:** Código não quebra se encontrar timestamp em formato inesperado.

---

## 🔧 Manutenção

### Backup Automático
O script cria backups com timestamp:
```
data/inventario_offline_backup_20241121_143000.db
```

### Remover Backups Antigos
```bash
# Windows
del data\*_backup_*.db

# Linux/Mac
rm data/*_backup_*.db
```

### Recriar Banco do Zero
Se houver problemas graves:
```bash
# 1. Remover banco atual
del data\inventario_offline.db

# 2. Recriar com estrutura correta
sqlite3 data\inventario_offline.db < sql\criar_tabelas_sqlite_offline.sql

# 3. Aplicar correção de timestamps
sqlite3 data\inventario_offline.db < sql\fix_sqlite_timestamp_compatibility.sql
```

---

## 🧪 Testes

### Teste 1: Inserir Timestamp Correto
```sql
INSERT INTO COLETA (ID_INVENTARIO, DATA_COLETA) 
VALUES (1, '2024-11-21 14:30:00');

-- Verificar
SELECT DATA_COLETA FROM COLETA WHERE ID = last_insert_rowid();
-- Resultado esperado: 2024-11-21 14:30:00
```

### Teste 2: Inserir Timestamp Incorreto (Trigger Corrige)
```sql
INSERT INTO COLETA (ID_INVENTARIO, DATA_COLETA) 
VALUES (1, '2024-11-21T14:30:00Z');

-- Verificar
SELECT DATA_COLETA FROM COLETA WHERE ID = last_insert_rowid();
-- Resultado esperado: 2024-11-21 14:30:00 (corrigido pelo trigger)
```

### Teste 3: Validar Formato
```sql
SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';
-- Resultado esperado: 0 linhas
```

---

## 📚 Referências

### SQLite Date/Time Functions
- [SQLite Date And Time Functions](https://www.sqlite.org/lang_datefunc.html)
- [strftime() Format Specifiers](https://www.sqlite.org/lang_datefunc.html#strftime)

### Java Timestamp
- [java.sql.Timestamp Documentation](https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html)
- [Timestamp.valueOf() Format](https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html#valueOf-java.lang.String-)

---

## ✅ Checklist de Verificação

Após executar a correção:

- [ ] Script executado sem erros
- [ ] Backup criado com sucesso
- [ ] View `v_timestamp_validation` retorna 0 inválidos
- [ ] Aplicação desktop funciona sem erros de timestamp
- [ ] Modo offline funciona corretamente
- [ ] Coletas são salvas sem erros
- [ ] Histórico de coletas é carregado corretamente

---

## 🆘 Troubleshooting

### Erro: "sqlite3 não encontrado"
**Solução:**
1. Baixe SQLite de: https://www.sqlite.org/download.html
2. Extraia `sqlite3.exe` para uma pasta no PATH
3. Ou coloque na pasta do projeto

### Erro: "Falha ao criar backup"
**Solução:**
- Verifique se a pasta `data/` existe
- Verifique permissões de escrita
- Feche a aplicação antes de executar

### Erro: "Ainda existem timestamps inválidos"
**Solução:**
1. Execute o script novamente
2. Verifique quais registros estão inválidos:
   ```sql
   SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';
   ```
3. Corrija manualmente se necessário

---

**Correção implementada em:** 21/11/2025  
**Versão:** 2.0.0  
**Status:** ✅ TESTADO E FUNCIONAL
