# Troubleshooting: Importação de Dados Não Funciona

## 🔴 Problema

Ao clicar em "Importar Dados Offline" na interface gráfica, a importação não funciona e o banco SQLite continua vazio.

---

## 🔍 Possíveis Causas

### **Causa 1: Erro de Conectividade com PostgreSQL**

O sistema verifica se consegue conectar ao PostgreSQL antes de importar. Se falhar, mostra erro.

**Como verificar:**
- Ao clicar "Importar Dados Offline", aparece mensagem de erro de conexão?
- PostgreSQL está rodando?
- Credenciais estão corretas?

**Solução:**
```powershell
# Testar conexão PostgreSQL
psql -h localhost -U inventario -d sispatrimonio -c "SELECT 1"
```

---

### **Causa 2: Código não foi recompilado**

As correções foram aplicadas mas o código não foi recompilado.

**Solução:**
```powershell
# Recompilar o projeto
mvn clean compile

# OU recompilar e empacotar
mvn clean package -DskipTests
```

---

### **Causa 3: Erro silencioso durante importação**

A importação inicia mas falha sem mostrar erro visível.

**Como verificar:**
1. Abrir console/terminal onde o sistema foi iniciado
2. Procurar por mensagens de erro
3. Verificar logs em `logs/sistema-inventario.log`

**Logs esperados durante importação:**
```
========================================
=== INICIANDO IMPORTAÇÃO DE DADOS ===
========================================
>>> INICIANDO IMPORTAÇÃO DE PATRIMÔNIOS
>>> Chamando patrimonioDAO.findAll()...
>>> ✅ patrimonioDAO.findAll() RETORNOU!
>>> Total de patrimônios encontrados: XXXX
...
>>> ✅ Inventário importado em AMBAS as tabelas!
========================================
=== IMPORTAÇÃO CONCLUÍDA COM SUCESSO ===
========================================
```

---

### **Causa 4: Caminho do banco SQLite incorreto**

O sistema está tentando salvar em um local diferente.

**Como verificar:**
```powershell
# Verificar se o banco existe e onde
Get-ChildItem -Path . -Filter "inventario.db" -Recurse

# Deve estar em: data/inventario.db
```

---

## ✅ SOLUÇÃO PASSO-A-PASSO

### **Passo 1: Verificar PostgreSQL**

```powershell
# Testar se PostgreSQL está acessível
psql -h localhost -U inventario -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_INVENTARIO"
```

**Resultado esperado:** Deve mostrar o número de inventários no PostgreSQL.

Se falhar: Verificar se PostgreSQL está rodando e credenciais estão corretas.

---

### **Passo 2: Recompilar o Projeto**

```powershell
# Limpar e recompilar
mvn clean compile

# Aguardar mensagem: BUILD SUCCESS
```

---

### **Passo 3: Executar Importação via Linha de Comando**

Ao invés de usar a interface gráfica, execute diretamente:

```powershell
# Executar sincronização via Maven
mvn exec:java -Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLiteV2" -Dexec.cleanupDaemonThreads=false
```

**OU** se Maven não estiver no PATH:

```powershell
# Usar Maven wrapper
.\mvnw.cmd exec:java -Dexec.mainClass="com.inventario.offline.SyncPostgresToSQLiteV2" -Dexec.cleanupDaemonThreads=false
```

**Aguardar mensagens:**
```
[1/6] Sincronizando Inventários...
   ✓ 1 inventário(s) sincronizado(s) em ambas as tabelas
[2/6] Sincronizando Salas...
   ✓ X sala(s) sincronizada(s)
...
✅ SINCRONIZAÇÃO CONCLUÍDA COM SUCESSO!
```

---

### **Passo 4: Verificar Dados**

```powershell
.\diagnosticar-sqlite-inventario.ps1
```

**Resultado esperado:**
```
=== TABELA: TABELA_INVENTARIO ===
  [1] ID: 1 | Nome: ... | Status: EM_ANDAMENTO | Data: ...
  Total: 1 registro(s)

=== TABELA: local_inventario ===
  [1] ID: 1 | Nome: ... | Status: EM_ANDAMENTO | Data: ...
  Total: 1 registro(s)
```

---

### **Passo 5: Testar Modo Offline**

1. Abrir sistema desktop
2. Clicar "Forçar Modo Offline"
3. Deve mostrar: "Deseja forçar o sistema para modo offline?"
4. Confirmar
5. Abrir ColetaFrame_v2
6. Verificar se inventário é carregado

---

## 🐛 Debug Avançado

### **Verificar Logs do Sistema**

```powershell
# Ver últimas 50 linhas do log
Get-Content logs/sistema-inventario.log -Tail 50
```

### **Verificar Estrutura do Banco SQLite**

```powershell
# Listar todas as tabelas
sqlite3 data/inventario.db ".tables"

# Ver estrutura da tabela TABELA_INVENTARIO
sqlite3 data/inventario.db ".schema TABELA_INVENTARIO"

# Contar registros em todas as tabelas importantes
sqlite3 data/inventario.db "
SELECT 'TABELA_INVENTARIO' as tabela, COUNT(*) as total FROM TABELA_INVENTARIO
UNION ALL
SELECT 'USUARIO', COUNT(*) FROM USUARIO
UNION ALL
SELECT 'PATRIMONIO', COUNT(*) FROM PATRIMONIO
UNION ALL
SELECT 'SALA', COUNT(*) FROM SALA;
"
```

### **Verificar Permissões do Diretório**

```powershell
# Verificar se pode escrever no diretório data/
Test-Path -Path "data" -PathType Container
New-Item -Path "data/teste.txt" -ItemType File -Force
Remove-Item -Path "data/teste.txt"
```

---

## 🔧 Soluções Alternativas

### **Alternativa 1: Importar Dados Manualmente via SQL**

Se tudo falhar, você pode copiar dados manualmente:

```sql
-- Conectar ao PostgreSQL
psql -h localhost -U inventario -d sispatrimonio

-- Exportar inventário
\copy (SELECT * FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO') TO 'inventario.csv' CSV HEADER;

-- Depois importar no SQLite
sqlite3 data/inventario.db
.mode csv
.import inventario.csv TABELA_INVENTARIO
```

### **Alternativa 2: Usar Banco de Exemplo**

Criar dados de exemplo diretamente no SQLite:

```sql
sqlite3 data/inventario.db

-- Inserir inventário de exemplo
INSERT INTO TABELA_INVENTARIO (ID, NOME, ANO, DATA_INICIO, STATUS_INVENTARIO, RESPONSAVEL_INVENTARIO, PERCENTUAL_CONCLUSAO)
VALUES (1, 'Inventário Teste 2024', 2024, date('now'), 'EM_ANDAMENTO', 'Administrador', 0.00);

-- Inserir usuário admin
INSERT INTO USUARIO (ID, LOGIN, SENHA, NOME_COMPLETO, EMAIL, PERFIL, ATIVO)
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'admin@ifmt.edu.br', 'ADMIN', 1);

-- Inserir sala de exemplo
INSERT INTO SALA (ID_SALA, NUMERO_SALA, NOME_SALA, ATIVA)
VALUES (1, '101', 'Sala 101', 1);
```

---

## 📞 Checklist de Diagnóstico

Execute cada item e anote o resultado:

- [ ] PostgreSQL está rodando? `psql -h localhost -U inventario -d sispatrimonio -c "SELECT 1"`
- [ ] Projeto foi recompilado? `mvn clean compile`
- [ ] Banco SQLite existe? `Test-Path data/inventario.db`
- [ ] Tabelas existem no SQLite? `sqlite3 data/inventario.db ".tables"`
- [ ] Consegue executar via linha de comando? `mvn exec:java -Dexec.mainClass="..."`
- [ ] Logs mostram erros? `Get-Content logs/sistema-inventario.log -Tail 50`
- [ ] Console mostra mensagens de debug? (Verificar terminal onde sistema foi iniciado)

---

## 🎯 Próximos Passos

1. **Execute o Passo 3** (importação via linha de comando)
2. **Copie e cole aqui** qualquer mensagem de erro que aparecer
3. **Execute o diagnóstico** e mostre o resultado

Isso nos ajudará a identificar exatamente onde está o problema!

---

**Data:** 21/11/2024  
**Status:** Guia de Troubleshooting
