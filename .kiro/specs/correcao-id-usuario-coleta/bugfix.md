# Bugfix Requirements Document

## Introduction

Este documento descreve o bug crítico de ID de usuário hardcoded na `ColetaActivity`, onde todas as coletas de patrimônios estão sendo registradas com `idUsuario = 1L` ao invés de usar o ID do usuário realmente logado. Este problema causa perda de rastreabilidade, problemas de auditoria, impossibilidade de filtrar coletas por usuário real e violação de requisitos de auditoria do sistema.

**Arquivo afetado:** `InventarioMobile/app/src/main/java/com/inventario/mobile/ui/coleta/ColetaActivity.kt`  
**Linha:** 207-209  
**Impacto:** 🔴 CRÍTICO - Afeta todas as coletas realizadas no app

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN um usuário registra uma coleta de patrimônio THEN o sistema salva a coleta com `idUsuario = 1L` (hardcoded) independente de qual usuário está logado

1.2 WHEN múltiplos usuários diferentes realizam coletas THEN todas as coletas aparecem como realizadas pelo usuário com ID 1

1.3 WHEN o sistema tenta identificar quem realizou uma coleta THEN não é possível determinar o usuário real pois todas as coletas têm o mesmo ID de usuário

1.4 WHEN não há usuário logado ou o ID não está disponível THEN o sistema registra a coleta com ID 1 sem validação ou tratamento de erro

### Expected Behavior (Correct)

2.1 WHEN um usuário registra uma coleta de patrimônio THEN o sistema SHALL obter o ID do usuário logado do `PreferencesManager` usando o método `getUserId()`

2.2 WHEN o ID do usuário é obtido com sucesso THEN o sistema SHALL registrar a coleta com o ID correto do usuário que a realizou

2.3 WHEN o ID do usuário não está disponível no `PreferencesManager` (retorna null) THEN o sistema SHALL mostrar uma mensagem de erro "Usuário não identificado. Faça login novamente." e NÃO permitir o registro da coleta

2.4 WHEN o usuário não está logado THEN o sistema SHALL redirecionar para a tela de login antes de permitir qualquer coleta

### Unchanged Behavior (Regression Prevention)

3.1 WHEN um usuário registra uma coleta com todos os dados válidos (patrimônio, localização, observações) THEN o sistema SHALL CONTINUE TO salvar a coleta localmente no banco Room

3.2 WHEN uma coleta é registrada offline THEN o sistema SHALL CONTINUE TO marcar a coleta como pendente de sincronização

3.3 WHEN a conexão é restaurada THEN o sistema SHALL CONTINUE TO sincronizar as coletas pendentes com o servidor

3.4 WHEN uma coleta é registrada com sucesso THEN o sistema SHALL CONTINUE TO mostrar a mensagem "✅ Coleta registrada com sucesso!" e retornar para a tela anterior

3.5 WHEN o usuário cancela a coleta THEN o sistema SHALL CONTINUE TO descartar os dados e retornar para a tela anterior sem salvar

3.6 WHEN o usuário escaneia um QR Code THEN o sistema SHALL CONTINUE TO preencher automaticamente o número do patrimônio

3.7 WHEN a sala é selecionada antes da coleta THEN o sistema SHALL CONTINUE TO usar o nome da sala como localização atual

## Bug Condition Analysis

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type ColetaRequest
  OUTPUT: boolean
  
  // Retorna true quando o ID do usuário está hardcoded
  RETURN X.idUsuario = 1L AND NOT obtidoDePreferencesManager(X.idUsuario)
END FUNCTION
```

### Property Specification - Fix Checking

```pascal
// Property: Fix Checking - ID do Usuário Correto
FOR ALL X WHERE isBugCondition(X) DO
  idUsuarioLogado ← PreferencesManager.getUserId()
  
  IF idUsuarioLogado IS NULL THEN
    ASSERT mostrarErro("Usuário não identificado") AND NOT permitirColeta()
  ELSE
    result ← registrarColeta'(X with idUsuario = idUsuarioLogado)
    ASSERT result.idUsuario = idUsuarioLogado AND result.idUsuario ≠ 1L
  END IF
END FOR
```

### Property Specification - Preservation Checking

```pascal
// Property: Preservation Checking - Comportamento Existente
FOR ALL X WHERE NOT isBugCondition(X) DO
  // Para coletas que já usam ID correto, comportamento não muda
  ASSERT registrarColeta(X) = registrarColeta'(X)
END FOR
```

## Technical Context

### Components Affected

1. **ColetaActivity.kt** (linha 207-209)
   - Método `salvarColeta()` contém o ID hardcoded
   - Precisa injetar `PreferencesManager` via Hilt
   - Precisa adicionar validação de usuário logado

2. **PreferencesManager.kt**
   - Já possui método `getUserId(): Int?` implementado
   - Retorna `null` se ID não estiver salvo
   - Método `saveUserId(userId: Int)` disponível para salvar ID

3. **ColetaViewModelClean.kt**
   - Recebe `idUsuario` como parâmetro no método `registrarColeta()`
   - Não precisa de alteração, apenas receber o ID correto

4. **Banco de Dados**
   - Coletas já registradas com ID errado precisarão de correção manual
   - Estrutura da tabela está correta, apenas dados incorretos

### Solution Approach

1. Injetar `PreferencesManager` no `ColetaActivity` via Hilt
2. No método `salvarColeta()`, substituir `val idUsuario = 1L` por chamada ao `PreferencesManager`
3. Adicionar validação: se `getUserId()` retornar `null`, mostrar erro e não permitir coleta
4. Adicionar logs para rastreamento do ID do usuário usado
5. Testar com múltiplos usuários para garantir que cada coleta tem o ID correto

### Data Migration Consideration

**Nota importante:** Coletas históricas já registradas com `idUsuario = 1L` estão incorretas e não podem ser corrigidas automaticamente pois não há como determinar qual usuário realmente fez cada coleta. Estas coletas permanecerão com ID 1 no histórico.

## Counterexample

**Cenário:** Usuário "Maria Silva" (ID 5) faz login e registra uma coleta

**Entrada:**
- Usuário logado: Maria Silva (ID 5)
- Patrimônio: 12345
- Localização: Sala 101
- Observações: "Patrimônio em bom estado"

**Comportamento Atual (Buggy):**
```kotlin
val idUsuario = 1L // Hardcoded
viewModel.registrarColeta(
    numeroPatrimonio = "12345",
    localizacaoAtual = "Sala 101",
    observacoes = "Patrimônio em bom estado",
    idUsuario = 1L  // ← ERRADO! Deveria ser 5
)
```

**Resultado no Banco:**
```
id_coleta: 523
id_patrimonio: 150
id_usuario: 1  ← ERRADO! Deveria ser 5
localizacao: "Sala 101"
observacoes: "Patrimônio em bom estado"
```

**Comportamento Esperado (Fixed):**
```kotlin
val idUsuario = preferencesManager.getUserId() ?: run {
    Toast.makeText(this, "Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show()
    return
}

viewModel.registrarColeta(
    numeroPatrimonio = "12345",
    localizacaoAtual = "Sala 101",
    observacoes = "Patrimônio em bom estado",
    idUsuario = idUsuario  // ← CORRETO! Usa ID 5
)
```

**Resultado no Banco:**
```
id_coleta: 523
id_patrimonio: 150
id_usuario: 5  ← CORRETO!
localizacao: "Sala 101"
observacoes: "Patrimônio em bom estado"
```

## Validation Criteria

### Fix Validation

1. ✅ Coleta registrada por usuário ID 5 deve ter `id_usuario = 5` no banco
2. ✅ Coleta registrada por usuário ID 3 deve ter `id_usuario = 3` no banco
3. ✅ Tentativa de coleta sem usuário logado deve mostrar erro e não salvar
4. ✅ Logs devem mostrar qual ID de usuário foi usado em cada coleta

### Regression Validation

1. ✅ Coleta offline deve continuar funcionando
2. ✅ Sincronização de coletas pendentes deve continuar funcionando
3. ✅ Scan de QR Code deve continuar funcionando
4. ✅ Seleção de sala deve continuar funcionando
5. ✅ Mensagens de sucesso/erro devem continuar aparecendo

## Priority and Impact

**Prioridade:** 🔴 CRÍTICA  
**Impacto:** ALTO - Afeta auditoria, rastreabilidade e compliance  
**Urgência:** IMEDIATA - Todas as coletas atuais estão sendo registradas incorretamente  
**Complexidade:** BAIXA - Correção simples, apenas substituir valor hardcoded por chamada ao PreferencesManager
