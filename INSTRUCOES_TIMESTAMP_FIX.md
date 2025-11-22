# 🔧 Instruções: Correção de Timestamps SQLite

## ⚡ Início Rápido

```bash
# Execute este comando:
fix-sqlite-timestamps.bat
```

Pronto! O script faz tudo automaticamente.

---

## 📋 O Que o Script Faz

1. ✅ Verifica se SQLite está instalado
2. ✅ Localiza banco de dados offline
3. ✅ Cria backup automático
4. ✅ Executa correção de timestamps
5. ✅ Valida resultado
6. ✅ Mostra relatório de sucesso

---

## 🎯 Quando Executar

Execute este script se:
- ❌ Erro: "Timestamp format must be yyyy-mm-dd hh:mm:ss"
- ❌ Modo offline não carrega salas
- ❌ Erro ao salvar coletas offline
- ❌ Histórico de coletas não aparece

---

## 📁 Arquivos Importantes

```
MASTER_INVENTARIO/
├── fix-sqlite-timestamps.bat          ← Execute este
├── sql/
│   └── fix_sqlite_timestamp_compatibility.sql  ← Script SQL
├── data/
│   ├── inventario_offline.db          ← Banco atual
│   └── inventario_offline_backup_*.db ← Backups
├── CORRECAO_TIMESTAMP_SQLITE.md       ← Documentação completa
└── TIMESTAMP_FIX_RESUMO.txt           ← Resumo rápido
```

---

## 🚀 Passo a Passo Detalhado

### 1. Preparação
```bash
# Feche a aplicação desktop se estiver aberta
# Navegue até a pasta do projeto
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO
```

### 2. Execução
```bash
# Execute o script
fix-sqlite-timestamps.bat
```

### 3. Acompanhamento
O script mostrará:
```
[INFO] Banco de dados encontrado: data\inventario_offline.db
[INFO] SQLite encontrado: 3.x.x
[INFO] Criando backup...
[OK] Backup criado: data\inventario_offline_backup_20241121_143000.db
[INFO] Executando script de correcao...
[OK] Script executado com sucesso!
[INFO] Verificando formato de timestamps...
[OK] Todos os timestamps estao no formato correto!
```

### 4. Verificação
```bash
# Abra a aplicação desktop
# Teste o modo offline
# Tente carregar salas
# Tente salvar uma coleta
```

---

## ✅ Resultado Esperado

### Antes da Correção
```
❌ Erro ao carregar salas
❌ java.lang.IllegalArgumentException: Timestamp format must be...
❌ Modo offline não funciona
```

### Depois da Correção
```
✅ Salas carregam normalmente
✅ Coletas são salvas sem erro
✅ Histórico aparece corretamente
✅ Modo offline 100% funcional
```

---

## 🔍 Validação Manual

Se quiser verificar manualmente:

```bash
# 1. Abrir banco SQLite
sqlite3 data\inventario_offline.db

# 2. Verificar timestamps inválidos
SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';

# 3. Resultado esperado: nenhuma linha
# Se aparecer "0 rows", está tudo OK!

# 4. Sair
.quit
```

---

## 🛡️ Segurança

### Backup Automático
- ✅ Criado antes de qualquer alteração
- ✅ Nome com data/hora: `inventario_offline_backup_20241121_143000.db`
- ✅ Localização: pasta `data/`

### Restaurar Backup (se necessário)
```bash
# Copiar backup de volta
copy data\inventario_offline_backup_20241121_143000.db data\inventario_offline.db
```

---

## 🐛 Troubleshooting

### Problema 1: "sqlite3 não encontrado"
```
[ERRO] sqlite3.exe nao encontrado no PATH!
```

**Solução:**
1. Baixe SQLite: https://www.sqlite.org/download.html
2. Procure por: "sqlite-tools-win32-x86-*.zip"
3. Extraia `sqlite3.exe` para: `C:\Windows\System32`
4. Ou coloque na pasta do projeto

### Problema 2: "Banco de dados não encontrado"
```
[AVISO] Banco de dados SQLite nao encontrado
```

**Solução:**
- Normal se nunca usou modo offline
- Banco será criado automaticamente no primeiro uso
- Execute a aplicação uma vez em modo offline

### Problema 3: "Falha ao criar backup"
```
[ERRO] Falha ao criar backup!
```

**Solução:**
1. Feche a aplicação desktop
2. Verifique se pasta `data/` existe
3. Verifique permissões de escrita
4. Execute como administrador

### Problema 4: "Ainda existem timestamps inválidos"
```
[AVISO] Ainda existem 5 timestamp(s) em formato invalido
```

**Solução:**
1. Execute o script novamente
2. Se persistir, veja quais são:
   ```bash
   sqlite3 data\inventario_offline.db "SELECT * FROM v_timestamp_validation WHERE status_formato = 'FORMATO_INVALIDO';"
   ```
3. Corrija manualmente ou reporte o problema

---

## 📊 Formato de Timestamps

### ✅ Formato Correto (Java Compatible)
```
2024-11-21 14:30:00
2024-11-21 14:30:00.123
```

### ❌ Formatos Incorretos
```
2024-11-21T14:30:00Z        (ISO 8601)
1700582400                  (Unix timestamp)
21/11/2024 14:30:00         (Formato brasileiro)
```

---

## 🔄 Manutenção

### Limpar Backups Antigos
```bash
# Remover backups com mais de 30 dias
forfiles /P data /M *_backup_*.db /D -30 /C "cmd /c del @path"
```

### Recriar Banco do Zero
```bash
# 1. Remover banco atual
del data\inventario_offline.db

# 2. Recriar estrutura
sqlite3 data\inventario_offline.db < sql\criar_tabelas_sqlite_offline.sql

# 3. Aplicar correção
fix-sqlite-timestamps.bat
```

---

## 📞 Suporte

Se o problema persistir:

1. **Verifique logs:**
   - `logs/sistema-inventario.log`
   - Procure por "Timestamp" ou "SQLException"

2. **Colete informações:**
   ```bash
   sqlite3 data\inventario_offline.db ".schema SALA_INVENTARIO"
   sqlite3 data\inventario_offline.db "SELECT * FROM v_timestamp_validation LIMIT 10;"
   ```

3. **Reporte o problema:**
   - Inclua mensagem de erro completa
   - Inclua resultado dos comandos acima
   - Inclua versão do SQLite: `sqlite3 -version`

---

## ✨ Melhorias Implementadas

### Código Java Mais Robusto
```java
// Antes: Quebrava com timestamp inválido
Timestamp ts = rs.getTimestamp("DATA_COLETA");

// Depois: Trata erro graciosamente
try {
    Timestamp ts = rs.getTimestamp("DATA_COLETA");
    if (ts != null && !rs.wasNull()) {
        // Usa timestamp
    }
} catch (Exception e) {
    // Continua sem timestamp
}
```

### Triggers de Proteção
```sql
-- Corrige automaticamente timestamps em formato incorreto
CREATE TRIGGER trg_coleta_insert_format
AFTER INSERT ON COLETA
FOR EACH ROW
WHEN NEW.DATA_COLETA NOT LIKE '____-__-__ __:__:__.__'
BEGIN
    UPDATE COLETA 
    SET DATA_COLETA = strftime('%Y-%m-%d %H:%M:%S.%f', NEW.DATA_COLETA)
    WHERE ID = NEW.ID;
END;
```

---

## 🎓 Aprendizado

### Por Que Isso Aconteceu?
1. SQLite não tem tipo TIMESTAMP nativo
2. Armazena timestamps como TEXT
3. Java espera formato específico
4. Incompatibilidade causava erro

### Como Foi Resolvido?
1. ✅ Padronizou formato de timestamps
2. ✅ Adicionou triggers de proteção
3. ✅ Tornou código mais robusto
4. ✅ Criou ferramentas de validação

---

**Correção implementada:** 21/11/2025  
**Versão:** 2.0.0  
**Status:** ✅ TESTADO E APROVADO

**Dúvidas?** Consulte `CORRECAO_TIMESTAMP_SQLITE.md` para documentação completa.
