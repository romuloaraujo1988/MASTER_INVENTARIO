# 🔍 DEBUG COMPLETO - Importação SQLite

## ✅ Sistema Recompilado com Logs Detalhados

O sistema foi recompilado com **logs de debug em TODOS os pontos críticos** para identificar exatamente onde está o problema.

---

## 🚀 Como Executar com Debug

### Passo 1: Limpar Banco Antigo
```batch
del data\inventario_offline.db
```

### Passo 2: Executar Sistema via Terminal
```batch
java -jar target\sistema-inventario-2.0.0-exec.jar
```

**IMPORTANTE:** Execute via terminal (não duplo-clique) para ver os logs!

### Passo 3: Fazer Importação
1. Menu → Arquivo → Importar Dados Offline
2. Clicar "Iniciar Importação"
3. **OBSERVAR O CONSOLE** - Você verá logs detalhados

---

## 📊 Logs Que Você Deve Ver

### Quando Clicar em "Iniciar Importação"
```
>>> DEBUG: iniciarImportacao() CHAMADO!
>>> DEBUG: Criando SwingWorker para importação...
>>> DEBUG: Executando SwingWorker...
>>> DEBUG: SwingWorker.execute() chamado!
```

### Quando o Worker Iniciar
```
>>> DEBUG: SwingWorker.doInBackground() INICIADO!
>>> DEBUG: Chamando importService.importarTodosDados()...
```

### Durante a Importação
```
========================================
=== INICIANDO IMPORTAÇÃO DE DADOS ===
========================================

>>> Importando patrimônios do PostgreSQL...
>>> Total de patrimônios encontrados: 11428

>>> DEBUG: onProgress() - Importando patrimônios: 100/11428 (0%)
>>> DEBUG: onProgress() - Importando patrimônios: 200/11428 (0%)
...
>>> DEBUG: onProgress() - Patrimônios importados: 11428 (25%)

>>> ✅ Patrimônios importados com sucesso: 11428
```

### Quando Concluir
```
>>> DEBUG: importService.importarTodosDados() RETORNOU!
>>> DEBUG: Resultado - Success: true
>>> DEBUG: Patrimônios: 11428
>>> DEBUG: Salas: 122
>>> DEBUG: Responsáveis: 96

========================================
=== IMPORTAÇÃO CONCLUÍDA COM SUCESSO ===
Patrimônios: 11428
Salas: 122
Responsáveis: 96
Usuários: 8
========================================

>>> DEBUG: SwingWorker.done() CHAMADO!
>>> DEBUG: Obtendo resultado...
>>> DEBUG: Finalizando importação...
```

---

## 🔍 Diagnóstico por Logs

### Cenário 1: Nenhum Log Aparece
**Significa:** O botão não está funcionando ou o método não está sendo chamado

**Verificar:**
- Sistema está rodando?
- Clicou no botão correto?
- Há algum erro na tela?

**Solução:**
- Verificar se o JAR foi recompilado corretamente
- Executar via terminal para ver logs

---

### Cenário 2: Logs Param em "Chamando importService"
```
>>> DEBUG: SwingWorker.doInBackground() INICIADO!
>>> DEBUG: Chamando importService.importarTodosDados()...
[PARA AQUI - SEM MAIS LOGS]
```

**Significa:** O `DataImportService` está travando ou dando erro silencioso

**Verificar:**
- PostgreSQL está rodando? `netstat -an | findstr ":5432"`
- Conexão funciona? `testar-conexao-postgresql.bat`

**Solução:**
- Verificar stack trace no console
- Verificar se há exceção sendo lançada

---

### Cenário 3: Logs Mostram "Total: 0"
```
>>> Total de patrimônios encontrados: 0
>>> Total de salas encontradas: 0
```

**Significa:** DAOs não estão retornando dados

**Verificar:**
- Banco PostgreSQL tem dados?
- Conexão está correta?
- Usuário tem permissão?

**Solução:**
- Executar `testar-conexao-postgresql.bat`
- Verificar configuração do banco

---

### Cenário 4: Logs Mostram Dados Mas SQLite Vazio
```
>>> ✅ Patrimônios importados com sucesso: 11428
[MAS SQLite ESTÁ VAZIO]
```

**Significa:** `OfflineDAO.salvarPatrimonio()` está falhando silenciosamente

**Verificar:**
- Há exceções no console?
- Arquivo `data\inventario_offline.db` foi criado?
- Permissões de escrita na pasta `data\`?

**Solução:**
- Verificar stack trace
- Verificar se pasta `data\` existe
- Criar pasta manualmente: `mkdir data`

---

### Cenário 5: ExecutionException
```
>>> DEBUG: ExecutionException - [mensagem de erro]
[STACK TRACE]
```

**Significa:** Erro durante a importação

**Ação:**
- Copiar o stack trace completo
- Identificar a causa raiz
- Corrigir o problema específico

---

## 🧪 Testes Adicionais

### Teste 1: Verificar Se Método É Chamado
Execute o sistema e clique em "Iniciar Importação".

**Esperado:**
```
>>> DEBUG: iniciarImportacao() CHAMADO!
```

**Se não aparecer:** Problema no botão ou no ActionListener

---

### Teste 2: Verificar Se Worker Executa
**Esperado:**
```
>>> DEBUG: SwingWorker.doInBackground() INICIADO!
```

**Se não aparecer:** Problema no SwingWorker

---

### Teste 3: Verificar Se Service É Chamado
**Esperado:**
```
========================================
=== INICIANDO IMPORTAÇÃO DE DADOS ===
========================================
```

**Se não aparecer:** Problema no `DataImportService`

---

### Teste 4: Verificar Se DAOs Retornam Dados
**Esperado:**
```
>>> Total de patrimônios encontrados: 11428
```

**Se mostrar 0:** Problema na conexão ou nos DAOs

---

### Teste 5: Verificar Se Dados São Salvos
Após a importação, executar:
```batch
testar-importacao-offline.bat
```

**Esperado:**
```
Patrimonios: 11428
Salas: 122
Responsaveis: 96
```

**Se mostrar 0:** Problema no `OfflineDAO`

---

## 📝 Checklist de Debug

Execute cada passo e marque:

- [ ] Sistema executado via terminal
- [ ] Cliquei em "Iniciar Importação"
- [ ] Vi log: "iniciarImportacao() CHAMADO!"
- [ ] Vi log: "SwingWorker.doInBackground() INICIADO!"
- [ ] Vi log: "INICIANDO IMPORTAÇÃO DE DADOS"
- [ ] Vi log: "Total de patrimônios encontrados: XXXX"
- [ ] Vi log: "Patrimônios importados com sucesso: XXXX"
- [ ] Vi log: "IMPORTAÇÃO CONCLUÍDA COM SUCESSO"
- [ ] Executei `testar-importacao-offline.bat`
- [ ] Dados estão no SQLite

---

## 🎯 Próximos Passos

### Se TODOS os logs aparecerem mas SQLite estiver vazio:
1. Verificar se há exceções no `OfflineDAO`
2. Verificar permissões da pasta `data\`
3. Verificar se SQLite JDBC está funcionando

### Se logs pararem em algum ponto:
1. Copiar o último log visto
2. Copiar qualquer stack trace
3. Identificar onde está travando
4. Corrigir o problema específico

---

## 🔧 Comandos Úteis

### Verificar PostgreSQL
```batch
netstat -an | findstr ":5432"
testar-conexao-postgresql.bat
```

### Verificar SQLite
```batch
dir data\inventario_offline.db
sqlite3 data\inventario_offline.db ".tables"
sqlite3 data\inventario_offline.db "SELECT COUNT(*) FROM local_patrimonio;"
```

### Limpar e Tentar Novamente
```batch
del data\inventario_offline.db
java -jar target\sistema-inventario-2.0.0-exec.jar
```

---

**Sistema recompilado com debug completo!**

**Agora execute e observe TODOS os logs no console. Eles dirão exatamente onde está o problema.**

**Versão:** 2.0.0 DEBUG  
**Data:** 21/11/2024 11:20  
**Status:** ✅ PRONTO PARA DEBUG COMPLETO
