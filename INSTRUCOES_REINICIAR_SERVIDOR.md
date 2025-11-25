# 🔄 Como Reiniciar o Servidor Mobile

## Problema Identificado

Os logs do Android mostram:
```
nomeSala: 'null'
localizacaoAtual: 'null'
```

Isso significa que o servidor está rodando com o **código antigo** (antes da correção).

## ✅ Solução: Reiniciar o Servidor

### Passo 1: Parar o Servidor Atual

**Opção A - Pelo terminal onde está rodando:**
- Vá até o terminal/console onde o servidor está rodando
- Pressione `Ctrl + C` para parar

**Opção B - Pelo Gerenciador de Tarefas:**
1. Abrir Gerenciador de Tarefas (Ctrl + Shift + Esc)
2. Procurar por "java.exe" ou "javaw.exe"
3. Clicar com botão direito → Finalizar tarefa

**Opção C - Pelo PowerShell (como Admin):**
```powershell
Get-Process -Name java | Where-Object {$_.MainWindowTitle -like "*inventario*"} | Stop-Process -Force
```

### Passo 2: Iniciar o Servidor com Código Corrigido

Abra um novo terminal PowerShell e execute:

```powershell
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO
java -jar target\mobile-server\sistema-inventario-2.0.0.jar --spring.profiles.active=mobile --server.port=8081
```

### Passo 3: Aguardar Inicialização

Aguarde até ver a mensagem:
```
Started MobileApiApplication in X.XXX seconds
```

### Passo 4: Verificar se a Correção Foi Aplicada

Execute este comando em outro terminal:

```powershell
curl http://localhost:8081/api/mobile/coletas/all | ConvertFrom-Json | Select-Object -ExpandProperty data | Select-Object -First 1 | Format-List id, numeroPatrimonio, localizacaoEncontrada, nomeSala
```

**Deve mostrar:**
```
id                     : 44
numeroPatrimonio       : 450588
localizacaoEncontrada  : AUDITÓRIO    ← DEVE TER VALOR!
nomeSala              : DIRECAO DE ENSINO
```

Se `localizacaoEncontrada` tiver valor, a correção foi aplicada! ✅

### Passo 5: Testar no App Android

1. Abrir o app
2. Ir para "Visualizar Coletas"
3. Pull to refresh (arrastar para baixo)
4. Verificar se as localizações aparecem

## 🔍 O Que Foi Corrigido

No arquivo `ColetaDAO.java`, os métodos `buscarPorColetor()` e `buscarTodas()` foram corrigidos para incluir explicitamente o campo `LOCALIZACAO_ENCONTRADA` no SELECT:

**ANTES:**
```java
SELECT c.*, p.NUMERO as NUMERO_PATRIMONIO...
```

**DEPOIS:**
```java
SELECT c.ID, c.ID_INVENTARIO, c.ID_PATRIMONIO, c.ID_COLETOR, 
       c.LOCALIZACAO_ENCONTRADA, c.ESTADO_ENCONTRADO, ...
```

## ❓ Dúvidas?

Se após reiniciar ainda não funcionar, verifique:
1. O JAR correto está sendo usado? (target/mobile-server/sistema-inventario-2.0.0.jar)
2. O servidor iniciou sem erros?
3. A porta 8081 está livre?
