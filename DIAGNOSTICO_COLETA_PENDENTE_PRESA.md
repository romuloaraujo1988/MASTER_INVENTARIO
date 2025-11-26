# 🔍 Diagnóstico: Coleta Pendente "Presa"

## 🚨 PROBLEMA

**Sintoma:** Contador mostra "Pendentes: 1" mas sincronização diz "Nenhuma coleta pendente"

**Isso indica:** Inconsistência entre métodos de contagem e busca

---

## 🔍 INVESTIGAÇÃO NECESSÁRIA

### Passo 1: Verificar Logs do App

```bash
# Ver logs detalhados da sincronização
adb logcat -s ColetaRepositoryImpl:D SyncRepository:D | grep -i "pendente\|coleta"

# Limpar logs antigos e tentar sincronizar
adb logcat -c
# Agora tente sincronizar no app
adb logcat -s ColetaRepositoryImpl:D
```

### Passo 2: Verificar Banco de Dados

```bash
# Conectar ao banco
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db"

# Dentro do SQLite, executar:
SELECT COUNT(*) FROM coleta WHERE sincronizado = 0;
SELECT * FROM coleta WHERE sincronizado = 0;
SELECT id, numeroPatrimonio, sincronizado, erroSincronizacao, tentativasSincronizacao FROM coleta;
```

---

## 🐛 POSSÍVEIS CAUSAS

### Causa 1: Coleta com Erro de Sincronização

A coleta pode ter um erro registrado que impede nova tentativa:

```sql
-- Verificar se tem erro
SELECT id, numeroPatrimonio, erroSincronizacao, tentativasSincronizacao 
FROM coleta 
WHERE sincronizado = 0;
```

**Solução:** Limpar erro para permitir nova tentativa

```sql
-- Limpar erro da coleta
UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE sincronizado = 0;
```

### Causa 2: Dados Incompletos na Coleta

A coleta pode estar faltando dados obrigatórios:

```sql
-- Verificar dados da coleta
SELECT 
    id,
    numeroPatrimonio,
    idPatrimonio,
    idInventario,
    idUsuario,
    sincronizado,
    erroSincronizacao
FROM coleta 
WHERE sincronizado = 0;
```

**Problema comum:** `idInventario` pode estar NULL ou 0

### Causa 3: Método de Busca Diferente

O contador usa um método e a sincronização usa outro:

```kotlin
// Contador usa:
coletaDao.contarPendentes()  // SELECT COUNT(*) FROM coleta WHERE sincronizado = 0

// Sincronização usa:
coletaDao.buscarPendentes()  // SELECT * FROM coleta WHERE sincronizado = 0
```

Se `buscarPendentes()` tem filtro adicional, pode não retornar a coleta!

---

## 🔧 SOLUÇÕES

### Solução 1: Limpar Erro via SQL (Rápido)

```bash
# Conectar ao banco
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE sincronizado = 0;'"

# Verificar
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT * FROM coleta WHERE sincronizado = 0;'"
```

### Solução 2: Adicionar Método de Diagnóstico no App

Vou criar um método para diagnosticar e corrigir coletas presas.

### Solução 3: Forçar Sincronização Ignorando Erros

Modificar o código para tentar sincronizar mesmo com erro registrado.

---

## 📊 COMANDOS DE DIAGNÓSTICO

### Ver Todas as Coletas:
```bash
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT id, numeroPatrimonio, sincronizado, erroSincronizacao FROM coleta;'"
```

### Ver Coletas Pendentes:
```bash
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT * FROM coleta WHERE sincronizado = 0;'"
```

### Ver Coletas com Erro:
```bash
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT id, numeroPatrimonio, erroSincronizacao, tentativasSincronizacao FROM coleta WHERE erroSincronizacao IS NOT NULL;'"
```

### Limpar Todos os Erros:
```bash
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0;'"
```

---

## 🎯 AÇÃO IMEDIATA

### Execute estes comandos e me envie o resultado:

```bash
# 1. Ver quantas coletas pendentes
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT COUNT(*) FROM coleta WHERE sincronizado = 0;'"

# 2. Ver detalhes da coleta pendente
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'SELECT id, numeroPatrimonio, idInventario, idUsuario, sincronizado, erroSincronizacao, tentativasSincronizacao FROM coleta WHERE sincronizado = 0;'"

# 3. Ver logs ao tentar sincronizar
adb logcat -c
# Agora tente sincronizar no app
adb logcat -s ColetaRepositoryImpl:D SyncRepository:D
```

---

## 🔍 O QUE PROCURAR NOS LOGS

### Logs Esperados (Sucesso):
```
ColetaRepositoryImpl: Sincronizando 1 coletas pendentes
ColetaRepositoryImpl: 📤 Enviando 1 coletas em lote...
ColetaRepositoryImpl: ✅ Batch sync: 1 coletas sincronizadas
ColetaRepositoryImpl: ✅ Coleta 123 marcada como sincronizada
```

### Logs de Problema:
```
ColetaRepositoryImpl: Nenhuma coleta pendente para sincronizar  ← PROBLEMA!
```

ou

```
ColetaRepositoryImpl: Erro ao converter coleta 123  ← PROBLEMA!
```

ou

```
ColetaRepositoryImpl: ❌ Inventário ativo não configurado  ← PROBLEMA!
```

---

## 💡 HIPÓTESE MAIS PROVÁVEL

Baseado no código, a coleta provavelmente tem:
1. **Erro de sincronização registrado** (tentou antes e falhou)
2. **idInventario = 0 ou NULL** (não configurado)
3. **Dados incompletos** (falta numeroPatrimonio ou idUsuario)

---

## 🚀 SOLUÇÃO RÁPIDA (Teste Agora)

Execute este comando para limpar erros e tentar novamente:

```bash
adb shell "run-as com.ifmt.inventariomobile sqlite3 databases/inventario_offline.db 'UPDATE coleta SET erroSincronizacao = NULL, tentativasSincronizacao = 0 WHERE sincronizado = 0;'"
```

Depois tente sincronizar novamente no app.

---

**Me envie os resultados dos comandos de diagnóstico para eu identificar o problema exato!**
