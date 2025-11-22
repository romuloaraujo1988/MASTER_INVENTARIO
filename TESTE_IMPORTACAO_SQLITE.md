# 🧪 Guia de Teste - Importação SQLite

## ✅ Melhorias Implementadas

### 1. Logs Detalhados
- ✅ Banner visual no início do método `importarTodosDados()`
- ✅ Teste de conexão PostgreSQL antes de iniciar
- ✅ Teste do listener antes de usar
- ✅ Logs de cada etapa da importação
- ✅ Logs de estrutura das tabelas
- ✅ Logs de progresso detalhados
- ✅ Stack trace completo em erros

### 2. Validações Adicionadas
- ✅ Verificação de listener null
- ✅ Teste de conexão PostgreSQL
- ✅ Teste de conexão SQLite
- ✅ Verificação de estrutura das tabelas
- ✅ Contagem de registros após importação

### 3. Correções de Bugs
- ✅ Campo `nome_completo` corrigido
- ✅ Método `initializeDatabase()` implementado
- ✅ Logs em todos os métodos de salvamento

## 🧪 Opções de Teste

### Opção 1: Teste Direto (Recomendado)

Execute o teste standalone sem interface gráfica:

```bash
testar-importacao-direto.bat
```

**Vantagens:**
- Mais rápido
- Logs mais claros
- Sem interferência da UI
- Fácil de debugar

**O que você verá:**
```
╔════════════════════════════════════════════════════════════╗
║         TESTE DIRETO DO DATA IMPORT SERVICE               ║
╚════════════════════════════════════════════════════════════╝

1. Criando DataImportService...
   ✓ Service criado

2. Criando ProgressListener...
   ✓ Listener criado

3. Iniciando importação...

╔════════════════════════════════════════════════════════════╗
║  MÉTODO importarTodosDados() FOI CHAMADO COM SUCESSO!     ║
╚════════════════════════════════════════════════════════════╝
Thread atual: main
Listener: OK

>>> Testando listener.onProgress()...
   [0%] Teste de conexão com listener
>>> ✅ Listener funcionando!

>>> TESTE: Verificando conexão com PostgreSQL...
   [0%] Verificando conexão com servidor...
>>> ✅ Conexão PostgreSQL OK!
>>>    Database: PostgreSQL
>>>    URL: jdbc:postgresql://localhost:5432/sispatrimonio

>>> Inicializando banco SQLite...
========================================
=== INICIALIZANDO BANCO SQLITE ===
========================================
>>> Criando tabelas do sistema...
  - Criando sync_control...
  - Criando sync_metadata...
>>> ✅ Tabelas do sistema criadas

>>> ETAPA 1: Importando patrimônios...
   [5%] Importando patrimônios...
>>> Chamando patrimonioDAO.findAll()...
>>> Total de patrimônios encontrados: 1000
>>> Limpando tabela local_patrimonio...
>>> Estrutura da tabela local_patrimonio:
>>>   - id (INTEGER)
>>>   - numero (TEXT)
>>>   - descricao (TEXT)
>>> DEBUG: Primeiro patrimônio a ser salvo:
>>>   ID: 123
>>>   Número: 12345
>>> Progresso: 100/1000 patrimônios
>>> ✅ Patrimônios importados: 1000
   [30%] Patrimônios importados: 1000

╔════════════════════════════════════════════════════════════╗
║                    RESULTADO FINAL                         ║
╚════════════════════════════════════════════════════════════╝

Status: ✓ SUCESSO

Patrimônios importados: 1000
Salas importadas: 50
Responsáveis importados: 100
Inventários importados: 1
Usuários importados: 10

Tempo total: 15234ms
```

### Opção 2: Teste via Interface Gráfica

1. **Preparar ambiente:**
```bash
testar-importacao-sqlite.bat
```

2. **Executar aplicação:**
```bash
mvn exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"
```

3. **Importar dados:**
   - Menu > Modo Offline > Importar Dados
   - Clicar "Iniciar Importação"
   - Observar logs no console

### Opção 3: Verificar Dados Após Importação

```bash
sqlite3 data/inventario.db < verificar-dados-sqlite.sql
```

**Ou manualmente:**
```bash
sqlite3 data/inventario.db

sqlite> SELECT COUNT(*) FROM local_patrimonio;
sqlite> SELECT COUNT(*) FROM local_sala;
sqlite> SELECT COUNT(*) FROM local_responsavel;
sqlite> SELECT COUNT(*) FROM local_usuario;
sqlite> SELECT * FROM sync_metadata;
```

## 🔍 Diagnóstico de Problemas

### Problema: "Listener é NULL"
**Causa:** Listener não foi passado corretamente
**Solução:** Verificar criação do listener no ImportacaoDadosDialog

### Problema: "Sem conexão com PostgreSQL"
**Causa:** Servidor PostgreSQL não está rodando
**Solução:** 
```bash
# Verificar se PostgreSQL está rodando
pg_isready -h localhost -p 5432

# Iniciar PostgreSQL se necessário
net start postgresql-x64-12
```

### Problema: "Erro ao inicializar SQLite"
**Causa:** Permissões ou diretório não existe
**Solução:**
```bash
# Criar diretório manualmente
mkdir data

# Verificar permissões
icacls data
```

### Problema: "Nenhum patrimônio encontrado"
**Causa:** Banco PostgreSQL está vazio
**Solução:** Importar dados de teste ou verificar conexão

## 📊 Logs Esperados

### ✅ Sucesso
```
╔════════════════════════════════════════════════════════════╗
║  MÉTODO importarTodosDados() FOI CHAMADO COM SUCESSO!     ║
╚════════════════════════════════════════════════════════════╝
>>> ✅ Listener funcionando!
>>> ✅ Conexão PostgreSQL OK!
>>> ✅ Banco SQLite inicializado!
>>> ✅ Patrimônios importados: 1000
>>> ✅ Salas importadas: 50
>>> ✅ Responsáveis importados: 100
========================================
=== IMPORTAÇÃO CONCLUÍDA COM SUCESSO ===
========================================
```

### ❌ Falha
```
>>> ❌ ERRO: Sem conexão com PostgreSQL!
>>>    Mensagem: Connection refused
```

## 📝 Checklist de Verificação

Após a importação, verificar:

- [ ] Console mostra "MÉTODO importarTodosDados() FOI CHAMADO"
- [ ] Listener está funcionando (mostra "Listener: OK")
- [ ] Conexão PostgreSQL OK
- [ ] Banco SQLite inicializado
- [ ] Tabelas criadas (logs mostram estrutura)
- [ ] Dados sendo salvos (logs de progresso)
- [ ] Contagem final de registros
- [ ] Arquivo `data/inventario.db` existe
- [ ] Arquivo tem tamanho > 0 bytes
- [ ] Queries SQL retornam dados

## 🎯 Próximos Passos

Se tudo funcionar:
1. ✅ Dados estão no SQLite
2. ✅ Sistema pode funcionar offline
3. ✅ Testar login offline
4. ✅ Testar coleta offline

Se houver problemas:
1. Copiar logs completos do console
2. Verificar arquivo `data/inventario.db`
3. Executar `verificar-dados-sqlite.sql`
4. Reportar erro com logs
