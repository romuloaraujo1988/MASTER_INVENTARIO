# Correção ID Usuário Coleta - Bugfix Design

## Overview

Este documento descreve a correção do bug crítico de ID de usuário hardcoded na `ColetaActivity`. Atualmente, todas as coletas de patrimônios são registradas com `idUsuario = 1L` ao invés de usar o ID do usuário realmente logado, causando perda de rastreabilidade, problemas de auditoria e impossibilidade de filtrar coletas por usuário real.

A correção envolve injetar o `PreferencesManager` via Hilt no `ColetaActivity`, substituir o valor hardcoded por uma chamada ao método `getUserId()`, e adicionar validação para garantir que o usuário está logado antes de permitir coletas.

## Glossary

- **Bug_Condition (C)**: A condição que dispara o bug - quando o ID do usuário está hardcoded como `1L` ao invés de ser obtido do `PreferencesManager`
- **Property (P)**: O comportamento desejado - coletas devem ser registradas com o ID correto do usuário logado obtido via `PreferencesManager.getUserId()`
- **Preservation**: Comportamento existente que deve permanecer inalterado - salvamento local, sincronização offline, scan de QR Code, seleção de sala
- **PreferencesManager**: Classe utilitária em `utils/PreferencesManager.kt` que gerencia SharedPreferences, incluindo o método `getUserId(): Int?` que retorna o ID do usuário logado ou `null` se não estiver disponível
- **ColetaActivity**: Activity em `ui/coleta/ColetaActivity.kt` responsável por registrar coletas de patrimônios, atualmente na linha 207-209 contém o ID hardcoded
- **ColetaViewModelClean**: ViewModel que recebe o `idUsuario` como parâmetro no método `registrarColeta()` e não precisa de alteração
- **Hilt**: Framework de injeção de dependência usado no projeto, a Activity já possui `@AndroidEntryPoint`

## Bug Details

### Bug Condition

O bug manifesta quando um usuário registra uma coleta de patrimônio e o sistema salva a coleta com `idUsuario = 1L` (hardcoded) independente de qual usuário está logado. O método `salvarColeta()` no `ColetaActivity` não está obtendo o ID do usuário do `PreferencesManager`, resultando em todas as coletas sendo atribuídas ao usuário com ID 1.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type ColetaRequest
  OUTPUT: boolean
  
  RETURN input.idUsuario == 1L
         AND NOT obtidoDePreferencesManager(input.idUsuario)
         AND usuarioLogadoExiste()
END FUNCTION
```

### Examples

- **Exemplo 1**: Usuário "Maria Silva" (ID 5) faz login e registra coleta do patrimônio 12345
  - **Esperado**: Coleta salva com `id_usuario = 5`
  - **Atual**: Coleta salva com `id_usuario = 1` (INCORRETO)

- **Exemplo 2**: Usuário "João Santos" (ID 3) faz login e registra coleta do patrimônio 67890
  - **Esperado**: Coleta salva com `id_usuario = 3`
  - **Atual**: Coleta salva com `id_usuario = 1` (INCORRETO)

- **Exemplo 3**: Múltiplos usuários diferentes realizam coletas ao longo do dia
  - **Esperado**: Cada coleta com o ID do respectivo usuário
  - **Atual**: Todas as coletas com `id_usuario = 1` (INCORRETO)

- **Edge Case**: Usuário não está logado ou ID não está disponível no PreferencesManager
  - **Esperado**: Sistema mostra erro "Usuário não identificado. Faça login novamente." e não permite coleta
  - **Atual**: Sistema registra coleta com `id_usuario = 1` sem validação (INCORRETO)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Salvamento local de coletas no banco Room deve continuar funcionando exatamente como antes
- Coletas offline devem continuar sendo marcadas como pendentes de sincronização
- Sincronização de coletas pendentes quando conexão é restaurada deve continuar funcionando
- Scan de QR Code deve continuar preenchendo automaticamente o número do patrimônio
- Seleção de sala deve continuar sendo usada como localização atual
- Mensagens de sucesso "✅ Coleta registrada com sucesso!" devem continuar aparecendo
- Botão cancelar deve continuar descartando dados e retornando para tela anterior

**Scope:**
Todas as funcionalidades que NÃO envolvem a obtenção do ID do usuário devem ser completamente inalteradas. Isto inclui:
- Validação de campos (patrimônio, localização, observações)
- Fluxo de navegação entre telas
- Integração com scanner de QR Code
- Salvamento e sincronização de dados
- Tratamento de erros de rede
- Interface do usuário e feedback visual

## Hypothesized Root Cause

Baseado na análise do código, as causas mais prováveis são:

1. **Valor Placeholder Esquecido**: O desenvolvedor colocou `val idUsuario = 1L` como placeholder temporário durante o desenvolvimento e esqueceu de substituir pela implementação real usando `PreferencesManager`

2. **Falta de Injeção de Dependência**: O `PreferencesManager` não foi injetado no `ColetaActivity`, então o desenvolvedor não tinha acesso fácil ao método `getUserId()` e usou um valor hardcoded

3. **Ausência de Validação**: Não há validação para verificar se o usuário está logado antes de permitir coletas, permitindo que o código execute com ID inválido

4. **Falta de Testes**: Não há testes automatizados que verificam se o ID do usuário correto está sendo usado, permitindo que o bug passasse despercebido

## Correctness Properties

Property 1: Bug Condition - ID do Usuário Correto na Coleta

_For any_ coleta registrada onde o usuário está logado e o ID está disponível no PreferencesManager, o sistema fixado SHALL obter o ID via `preferencesManager.getUserId()` e registrar a coleta com esse ID correto, garantindo rastreabilidade e auditoria adequadas.

**Validates: Requirements 2.1, 2.2**

Property 2: Preservation - Comportamento de Salvamento e Sincronização

_For any_ coleta registrada com ID de usuário válido, o sistema fixado SHALL produzir exatamente o mesmo comportamento que o sistema original em relação a salvamento local, sincronização offline, scan de QR Code, seleção de sala e feedback visual, preservando todas as funcionalidades existentes.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

Assumindo que nossa análise de causa raiz está correta:

**File**: `InventarioMobile/app/src/main/java/com/inventario/mobile/ui/coleta/ColetaActivity.kt`

**Function**: `salvarColeta()` (linha 207-209)

**Specific Changes**:

1. **Injetar PreferencesManager via Hilt**:
   - Adicionar campo `@Inject lateinit var preferencesManager: PreferencesManager` na classe
   - A Activity já possui `@AndroidEntryPoint`, então a injeção funcionará automaticamente

2. **Substituir ID Hardcoded por Chamada ao PreferencesManager**:
   - Remover linha: `val idUsuario = 1L // Placeholder`
   - Adicionar código para obter ID do usuário:
     ```kotlin
     val idUsuario = preferencesManager.getUserId()
     ```

3. **Adicionar Validação de Usuário Logado**:
   - Verificar se `getUserId()` retornou `null`
   - Se `null`, mostrar Toast com mensagem de erro e retornar sem salvar
   - Código:
     ```kotlin
     if (idUsuario == null) {
         Toast.makeText(this, "Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show()
         return
     }
     ```

4. **Adicionar Logs para Rastreamento**:
   - Adicionar log antes de registrar coleta:
     ```kotlin
     Log.d(TAG, "Registrando coleta com ID do usuário: $idUsuario")
     ```

5. **Passar ID Correto para ViewModel**:
   - Manter chamada existente ao ViewModel, mas com ID correto:
     ```kotlin
     viewModel.registrarColeta(
         numeroPatrimonio = numeroPatrimonio,
         localizacaoAtual = salaNome,
         observacoes = observacoes,
         latitude = null,
         longitude = null,
         idUsuario = idUsuario  // Agora com ID correto
     )
     ```

## Testing Strategy

### Validation Approach

A estratégia de testes segue uma abordagem de duas fases: primeiro, demonstrar o bug no código não corrigido através de testes exploratórios, depois verificar que a correção funciona corretamente e preserva o comportamento existente.

### Exploratory Bug Condition Checking

**Goal**: Demonstrar o bug ANTES de implementar a correção. Confirmar ou refutar a análise de causa raiz. Se refutarmos, precisaremos re-hipotizar.

**Test Plan**: Executar testes manuais no app não corrigido para observar que todas as coletas são salvas com `id_usuario = 1` independente do usuário logado. Verificar no banco de dados SQLite local e nos logs.

**Test Cases**:
1. **Teste Usuário Maria (ID 5)**: Fazer login como Maria (ID 5), registrar coleta, verificar banco - esperado falha com `id_usuario = 1` ao invés de 5
2. **Teste Usuário João (ID 3)**: Fazer login como João (ID 3), registrar coleta, verificar banco - esperado falha com `id_usuario = 1` ao invés de 3
3. **Teste Múltiplos Usuários**: Alternar entre usuários e registrar coletas - esperado falha com todas tendo `id_usuario = 1`
4. **Teste Sem Login**: Tentar registrar coleta sem fazer login - esperado falha permitindo coleta com `id_usuario = 1` sem validação

**Expected Counterexamples**:
- Coletas registradas por usuário ID 5 aparecem no banco com `id_usuario = 1`
- Coletas registradas por usuário ID 3 aparecem no banco com `id_usuario = 1`
- Possíveis causas confirmadas: valor hardcoded, falta de injeção do PreferencesManager, ausência de validação

### Fix Checking

**Goal**: Verificar que para todas as entradas onde a condição de bug existe, a função corrigida produz o comportamento esperado.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  idUsuarioLogado := preferencesManager.getUserId()
  
  IF idUsuarioLogado IS NULL THEN
    ASSERT mostrarErro("Usuário não identificado") 
    ASSERT NOT permitirColeta()
  ELSE
    result := salvarColeta_fixed(input with idUsuario = idUsuarioLogado)
    ASSERT result.idUsuario = idUsuarioLogado
    ASSERT result.idUsuario ≠ 1L (a menos que usuário logado seja realmente ID 1)
  END IF
END FOR
```

### Preservation Checking

**Goal**: Verificar que para todas as entradas onde a condição de bug NÃO existe (comportamentos não relacionados ao ID do usuário), a função corrigida produz o mesmo resultado que a função original.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT salvarColeta_original(input) = salvarColeta_fixed(input)
END FOR
```

**Testing Approach**: Testes manuais são recomendados para verificação de preservação porque:
- O comportamento existente é principalmente visual e de fluxo de navegação
- Envolve interações com hardware (câmera para QR Code)
- Requer verificação de sincronização offline que depende de estado de rede
- Testes manuais permitem validação completa da experiência do usuário

**Test Plan**: Observar comportamento no código CORRIGIDO para todas as funcionalidades não relacionadas ao ID do usuário, comparando com comportamento documentado do código original.

**Test Cases**:
1. **Salvamento Local**: Registrar coleta offline, verificar que é salva no Room e marcada como pendente
2. **Sincronização**: Restaurar conexão, verificar que coletas pendentes são sincronizadas automaticamente
3. **Scan QR Code**: Escanear QR Code, verificar que número do patrimônio é preenchido automaticamente
4. **Seleção de Sala**: Selecionar sala antes da coleta, verificar que nome da sala é usado como localização
5. **Mensagens de Sucesso**: Registrar coleta com sucesso, verificar mensagem "✅ Coleta registrada com sucesso!"
6. **Botão Cancelar**: Clicar em cancelar, verificar que dados são descartados e volta para tela anterior
7. **Validação de Campos**: Tentar salvar sem patrimônio, verificar mensagem de erro apropriada

### Unit Tests

- Testar obtenção de ID do usuário do PreferencesManager com usuário logado
- Testar obtenção de ID do usuário quando não há usuário logado (retorna null)
- Testar validação que impede coleta quando ID é null
- Testar que ID correto é passado para o ViewModel

### Property-Based Tests

- Gerar múltiplos cenários de usuários diferentes (IDs 1-100) e verificar que cada coleta tem o ID correto
- Gerar cenários de coletas offline e online e verificar que ID do usuário é preservado em ambos
- Testar que comportamento de sincronização funciona corretamente com IDs de usuário variados

### Integration Tests

- Testar fluxo completo: login → seleção de sala → scan QR Code → registro de coleta → verificação no banco
- Testar alternância entre usuários: login usuário A → coleta → logout → login usuário B → coleta → verificar IDs diferentes
- Testar cenário offline: login → desconectar rede → registrar coleta → verificar ID correto no banco local → reconectar → sincronizar → verificar ID correto no servidor
