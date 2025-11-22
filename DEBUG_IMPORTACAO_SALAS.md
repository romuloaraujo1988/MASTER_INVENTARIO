# Debug: Importação de Salas - Logs Detalhados

## 🔍 Problema

O sistema importa apenas 11 salas, mas deveria importar 108.

## ✅ Logs Adicionados

Adicionei logs detalhados no `DataImportService.java` para identificar onde as salas estão sendo perdidas:

### 1. Antes do Loop
```
>>> DEBUG: Lista de salas tem X elementos
>>> DEBUG: Classe da lista: java.util.ArrayList
>>> DEBUG: IDs das salas na lista:
>>>   [0] ID=1, Número=101, Ativo=true
>>>   [1] ID=2, Número=102, Ativo=true
>>>   ...
```

### 2. Depois do Loop
```
>>> DEBUG: Total esperado: X
>>> DEBUG: Total importado: Y
>>> DEBUG: Diferença: (X-Y)
```

## 🧪 Como Testar

### Passo 1: Recompilar

```bash
mvn clean compile
```

### Passo 2: Executar Importação

Via interface:
1. Abrir aplicação
2. Menu → Arquivo → Importar Dados Offline
3. **Observar os logs no console**

Ou via script:
```powershell
.\sincronizar-sqlite-offline.ps1
```

### Passo 3: Analisar Logs

Procure por estas linhas no console:

```
>>> Total de salas encontradas: ???
>>> DEBUG: Lista de salas tem ??? elementos
>>> DEBUG: IDs das salas na lista:
```

## 📊 Cenários Possíveis

### Cenário A: Lista tem 108 salas, mas importa apenas 11

**Logs esperados:**
```
>>> Total de salas encontradas: 108
>>> DEBUG: Lista de salas tem 108 elementos
>>> DEBUG: IDs das salas na lista:
>>>   [0] ID=1, Número=101, Ativo=true
>>>   [1] ID=2, Número=102, Ativo=true
>>>   ...
>>>   [10] ID=11, Número=111, Ativo=true
>>>   ... e mais 97 salas
>>> Progresso: 10/108 salas
>>> Progresso: 20/108 salas
>>> ✅ Salas importadas com sucesso: 108
```

**Conclusão:** Problema resolvido! ✅

### Cenário B: Lista tem apenas 11 salas

**Logs esperados:**
```
>>> Total de salas encontradas: 11
>>> DEBUG: Lista de salas tem 11 elementos
>>> DEBUG: IDs das salas na lista:
>>>   [0] ID=1, Número=101, Ativo=true
>>>   ...
>>>   [10] ID=11, Número=111, Ativo=true
>>> ✅ Salas importadas com sucesso: 11
```

**Conclusão:** Problema está no `SalaDAO.listarTodasSalas()` ❌

### Cenário C: Lista tem 108, mas loop processa apenas 11

**Logs esperados:**
```
>>> Total de salas encontradas: 108
>>> DEBUG: Lista de salas tem 108 elementos
>>> Progresso: 10/108 salas
>>> ✅ Salas importadas com sucesso: 11
>>> DEBUG: Total esperado: 108
>>> DEBUG: Total importado: 11
>>> DEBUG: Diferença: 97
>>> ⚠️ AVISO: Nem todas as salas foram importadas!
```

**Conclusão:** Problema está no loop de importação ❌

## 🔧 Próximas Ações Baseadas nos Logs

### Se Cenário A (Resolvido)
- ✅ Nenhuma ação necessária
- ✅ Verificar SQLite: deve ter 108 salas

### Se Cenário B (Problema no DAO)
1. Verificar `SalaDAO.listarTodasSalas()`
2. Executar query diretamente no PostgreSQL
3. Verificar se há LIMIT escondido

### Se Cenário C (Problema no Loop)
1. Verificar se há exceções sendo capturadas silenciosamente
2. Verificar se `offlineDAO.salvarSala()` está falhando
3. Adicionar mais logs dentro do loop

## 📝 Comandos Úteis

### Verificar PostgreSQL
```sql
-- Total de salas
SELECT COUNT(*) FROM TABELA_SALA;

-- Primeiras 20
SELECT ID_SALA, NUMERO_SALA, ATIVO 
FROM TABELA_SALA 
ORDER BY ID_SALA 
LIMIT 20;
```

### Verificar SQLite
```bash
sqlite3 data/inventario.db "SELECT COUNT(*) FROM local_sala;"
sqlite3 data/inventario.db "SELECT COUNT(*) FROM SALA;"
```

## 🎯 O Que Procurar nos Logs

1. **"Total de salas encontradas: X"**
   - Se X = 11 → Problema no PostgreSQL ou no DAO
   - Se X = 108 → Problema no loop de importação

2. **"DEBUG: Lista de salas tem X elementos"**
   - Confirma quantas salas o DAO retornou

3. **"DEBUG: IDs das salas na lista"**
   - Mostra as primeiras 20 salas
   - Verifica se há duplicatas ou IDs estranhos

4. **"Progresso: X/Y salas"**
   - Mostra quantas foram processadas
   - Se parar em 11, há um problema no loop

5. **"⚠️ AVISO: Nem todas as salas foram importadas!"**
   - Indica que houve perda de dados no processo

---

**Status:** 🔍 AGUARDANDO EXECUÇÃO COM LOGS  
**Próxima Ação:** Executar importação e analisar logs  
**Data:** 21/11/2025
