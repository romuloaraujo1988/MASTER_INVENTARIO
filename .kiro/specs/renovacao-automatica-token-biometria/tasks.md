# Implementation Plan - Renovação Automática de Token via Biometria

## Overview

Este plano de implementação segue o workflow de bugfix exploratory, onde primeiro escrevemos testes para entender o bug (Bug Condition), depois testes para preservar comportamento existente (Preservation), e finalmente implementamos a correção.

---

## Tasks

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Renovação Automática via Biometria Não Ocorre
  - **CRITICAL**: Este teste DEVE FALHAR no código unfixed - a falha confirma que o bug existe
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: Este teste codifica o comportamento esperado - ele validará a correção quando passar após a implementação
  - **GOAL**: Surfacear contraexemplos que demonstram que o bug existe
  - **Scoped PBT Approach**: Para bugs determinísticos, escopo da propriedade aos casos concretos de falha para garantir reprodutibilidade
  - Testar cenários onde token expirado OU offline + biometria habilitada devem mostrar prompt automático
  - Cenário 1: LoginActivity com token expirado + biometria habilitada → deve mostrar prompt (mas não mostra no código unfixed)
  - Cenário 2: MainActivity iniciando com token expirado + biometria habilitada → deve verificar e mostrar prompt (mas não verifica no código unfixed)
  - Cenário 3: RefreshTokenInterceptor detecta 401 + biometria habilitada → deve tentar renovação via biometria (mas redireciona direto no código unfixed)
  - Cenário 4: App offline + biometria habilitada → deve permitir acesso aos dados locais via biometria (mas bloqueia no código unfixed)
  - Executar testes no código UNFIXED
  - **EXPECTED OUTCOME**: Testes FALHAM (isso é correto - prova que o bug existe)
  - Documentar contraexemplos encontrados:
    - LoginActivity mostra formulário de senha ao invés de prompt de biometria
    - MainActivity redireciona para login sem verificar biometria
    - RefreshTokenInterceptor redireciona para login sem tentar renovação
    - Acesso offline bloqueado mesmo com biometria habilitada
  - Marcar tarefa como completa quando testes estiverem escritos, executados e falhas documentadas
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10_

- [-] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Comportamento Existente Sem Biometria Preservado
  - **IMPORTANT**: Seguir metodologia observation-first
  - Observar comportamento no código UNFIXED para entradas não-buggy (casos onde isBugCondition retorna false)
  - Observar: Usuário SEM biometria + token expirado → redireciona para LoginActivity
  - Observar: Login manual com senha → funciona normalmente
  - Observar: Token válido (não expirado) → app funciona sem prompts
  - Observar: Botão manual de biometria na LoginActivity → funciona corretamente
  - Observar: RenovarTokenComBiometriaUseCase chamado manualmente → funciona como implementado
  - Observar: BiometricAuthManager.authenticateWithCancel() → mostra prompt corretamente
  - Observar: LoginViewModel.loginWithBiometric() → processa renovação corretamente
  - Escrever testes baseados em propriedades capturando os comportamentos observados dos Preservation Requirements
  - Property-based testing gera muitos casos de teste automaticamente para garantias mais fortes
  - Executar testes no código UNFIXED
  - **EXPECTED OUTCOME**: Testes PASSAM (isso confirma o comportamento baseline a preservar)
  - Marcar tarefa como completa quando testes estiverem escritos, executados e passando no código unfixed
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 3. Fix para renovação automática de token via biometria + acesso offline permanente

  - [x] 3.1 Implementar verificação automática de biometria no LoginActivity
    - Adicionar método `verificarEMostrarBiometriaSeNecessario()` no onCreate
    - Verificar intent extras para flag "TOKEN_EXPIRED" ou "OFFLINE"
    - Verificar `preferencesManager.isBiometricEnabled()`
    - Verificar `preferencesManager.hasUserSavedLocally()`
    - Se (tokenExpired OU offline) E biometriaHabilitada E hasUserSaved, mostrar prompt automaticamente
    - Mostrar mensagem apropriada: "Token expirado. Use biometria para renovar" ou "Sem conexão. Use biometria para acessar dados offline"
    - Chamar `authenticateWithBiometric()` após delay de 500ms
    - Atualizar `authenticateWithBiometric()` para chamar `viewModel.loginWithBiometric()` após sucesso
    - Implementar fallback para senha em caso de cancelamento ou falha
    - _Bug_Condition: isBugCondition(input) onde (input.tokenExpirado = true OU input.offline = true) E input.biometriaHabilitada = true E input.promptBiometriaAutomaticoMostrado = false_
    - _Expected_Behavior: result.promptMostrado = true E result.renovacaoTentada = true E (result.tokenRenovado = true OU result.acessoOfflinePermitido = true OU result.redirecionadoParaLogin = true)_
    - _Preservation: Usuários sem biometria continuam sendo redirecionados para login normalmente (Requirements 3.1, 3.2, 3.3, 3.4)_
    - _Requirements: 2.1, 2.2, 2.6, 2.7, 2.8, 3.1, 3.2, 3.3, 3.4_

  - [x] 3.2 Implementar verificação de token ao iniciar no MainActivity
    - Adicionar método `verificarTokenEMostrarBiometriaSeNecessario()` no onCreate
    - Verificar `preferencesManager.isTokenExpired()`
    - Verificar `NetworkUtils.isNetworkAvailable()`
    - Verificar `preferencesManager.isBiometricEnabled()`
    - Se (tokenExpired OU offline) E biometriaHabilitada, chamar método apropriado
    - Se offline, chamar `permitirAcessoOfflineComBiometria()`
    - Se online com token expirado, chamar `renovarTokenComBiometria()`
    - Se tokenExpired E NÃO biometriaHabilitada, redirecionar para login
    - _Bug_Condition: isBugCondition(input) onde input.tokenExpirado = true E input.biometriaHabilitada = true E input.promptBiometriaAutomaticoMostrado = false_
    - _Expected_Behavior: result.promptMostrado = true E result.renovacaoTentada = true_
    - _Preservation: Token válido não aciona prompts desnecessários (Requirement 3.3)_
    - _Requirements: 2.3, 2.4, 2.5, 3.3_

  - [x] 3.3 Implementar acesso offline permanente via biometria no MainActivity
    - Criar método `permitirAcessoOfflineComBiometria()`
    - Mostrar prompt de biometria com título "Acesso Offline" e subtitle "Use biometria para acessar dados offline"
    - Implementar callback `onAuthenticationSucceeded`: carregar dados do Room local, ativar modo offline na UI, mostrar Snackbar "📴 Modo Offline Ativado"
    - Implementar callback `onAuthenticationFailed`: redirecionar para login com mensagem "Biometria falhou. Faça login com senha."
    - Implementar callback `onAuthenticationCanceled`: redirecionar para login com mensagem "Acesso cancelado. Faça login com senha."
    - Não validar expiração de token se offline
    - Permitir acesso indefinido aos dados locais após autenticação biométrica bem-sucedida
    - _Bug_Condition: isBugCondition(input) onde input.offline = true E input.biometriaHabilitada = true E input.acessoDadosLocaisBloqueado = true_
    - _Expected_Behavior: result.acessoOfflinePermitido = true após biometria validada_
    - _Preservation: Usuários sem biometria não podem acessar offline (deve bloquear)_
    - _Requirements: 2.7, 2.8, 2.9, 2.10_

  - [x] 3.4 Implementar tentativa de renovação via biometria no RefreshTokenInterceptor
    - Adicionar verificação de `preferencesManager.isBiometricEnabled()` ao detectar 401
    - Se biometriaHabilitada, chamar `tentarRenovacaoComBiometria()` antes de redirecionar
    - Criar método `tentarRenovacaoComBiometria()` com `@Synchronized`
    - Implementar flag `isRenewing` para evitar múltiplas renovações simultâneas
    - Obter Activity atual via Application ou Context
    - Mostrar prompt de biometria com título "Renovar Sessão" e subtitle "Use biometria para renovar token"
    - Chamar `RenovarTokenComBiometriaUseCase` após autenticação bem-sucedida
    - Se renovação bem-sucedida, fazer retry da requisição original com novo token
    - Se renovação falhar ou biometria não habilitada, redirecionar para login
    - Implementar timeout de 30 segundos para aguardar resultado
    - Usar `CountDownLatch` para sincronização entre threads
    - _Bug_Condition: isBugCondition(input) onde input.tokenExpirado = true (401) E input.biometriaHabilitada = true E input.renovacaoTentada = false_
    - _Expected_Behavior: result.renovacaoTentada = true E (result.tokenRenovado = true OU result.redirecionadoParaLogin = true)_
    - _Preservation: Usuários sem biometria continuam sendo redirecionados para login (Requirement 3.1)_
    - _Requirements: 2.3, 2.5, 3.1_

  - [x] 3.5 Implementar thread safety e suporte offline no RefreshTokenInterceptor
    - Adicionar verificação de modo offline antes de tentar renovação
    - Se offline E biometriaHabilitada, permitir acesso aos dados locais
    - Não bloquear requisições se biometria foi validada em modo offline
    - Implementar queue de requisições pendentes durante renovação
    - Garantir que apenas uma renovação ocorre por vez (usar `synchronized`)
    - Adicionar logs detalhados para debug de thread safety
    - _Bug_Condition: isBugCondition(input) onde input.offline = true E input.biometriaHabilitada = true_
    - _Expected_Behavior: result.acessoOfflinePermitido = true sem bloquear requisições_
    - _Preservation: Comportamento de thread safety não afeta usuários sem biometria_
    - _Requirements: 2.7, 2.8_

  - [x] 3.6 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Renovação Automática via Biometria Funciona
    - **IMPORTANT**: Re-executar o MESMO teste da tarefa 1 - NÃO escrever um novo teste
    - O teste da tarefa 1 codifica o comportamento esperado
    - Quando este teste passar, confirma que o comportamento esperado está satisfeito
    - Executar teste de exploração da bug condition da etapa 1
    - Verificar que LoginActivity agora mostra prompt de biometria automaticamente
    - Verificar que MainActivity verifica token e mostra prompt quando necessário
    - Verificar que RefreshTokenInterceptor tenta renovação via biometria antes de redirecionar
    - Verificar que acesso offline é permitido via biometria
    - **EXPECTED OUTCOME**: Teste PASSA (confirma que o bug está corrigido)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10_

  - [x] 3.7 Verify preservation tests still pass
    - **Property 2: Preservation** - Comportamento Existente Preservado
    - **IMPORTANT**: Re-executar os MESMOS testes da tarefa 2 - NÃO escrever novos testes
    - Executar testes de preservação da etapa 2
    - Verificar que usuários SEM biometria continuam sendo redirecionados para login
    - Verificar que login manual com senha continua funcionando
    - Verificar que token válido não aciona prompts desnecessários
    - Verificar que botão manual de biometria continua funcionando
    - Verificar que RenovarTokenComBiometriaUseCase continua funcionando como antes
    - Verificar que BiometricAuthManager.authenticateWithCancel() continua funcionando
    - Verificar que LoginViewModel.loginWithBiometric() continua funcionando
    - **EXPECTED OUTCOME**: Testes PASSAM (confirma que não há regressões)
    - Confirmar que todos os testes ainda passam após a correção (sem regressões)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 4. Checkpoint - Ensure all tests pass
  - Executar todos os testes (bug condition + preservation)
  - Verificar que teste de bug condition agora PASSA (bug corrigido)
  - Verificar que testes de preservation ainda PASSAM (sem regressões)
  - Testar fluxos completos manualmente:
    - Abrir app com token expirado + biometria habilitada → deve mostrar prompt
    - Abrir app offline + biometria habilitada → deve permitir acesso
    - Fazer requisição que retorna 401 + biometria habilitada → deve renovar automaticamente
    - Cancelar prompt de biometria → deve redirecionar para login com senha
    - Usuário sem biometria + token expirado → deve redirecionar para login normalmente
  - Verificar logs para confirmar comportamento correto
  - Se houver dúvidas ou problemas, perguntar ao usuário antes de prosseguir

---

## Notes

### Bug Condition Methodology

Este bugfix usa a metodologia de Bug Condition:

- **C(X)**: Bug Condition - identifica entradas que acionam o bug
  - `(tokenExpirado OU offline) E biometriaHabilitada E promptNãoMostrado E acessoBloqueado`

- **P(result)**: Property - comportamento desejado para entradas buggy
  - `promptMostrado E renovacaoTentada E (tokenRenovado OU acessoOfflinePermitido OU redirecionadoParaLogin)`

- **¬C(X)**: Entradas não-buggy que devem ser preservadas
  - Usuários sem biometria
  - Token válido (não expirado)
  - Login manual com senha

- **F**: Função original (unfixed) - código antes da correção
- **F'**: Função corrigida (fixed) - código após a correção

### Key Concepts

| Conceito | Definição | Exemplo |
|---------|-----------|---------|
| **C(X)** | Bug Condition - identifica entradas buggy | `tokenExpirado E biometriaHabilitada` |
| **P(result)** | Property - comportamento desejado para C(X) | `promptMostrado E renovacaoTentada` |
| **¬C(X)** | Entradas não-buggy - devem ser preservadas | `biometriaDesabilitada` ou `tokenValido` |
| **F** | Função original (unfixed) | Código antes da correção |
| **F'** | Função corrigida (fixed) | Código após a correção |
| **Counterexample** | Exemplo concreto demonstrando o bug | `LoginActivity redireciona sem mostrar prompt` |

### Testing Strategy

1. **Exploration (Task 1)**: Escrever testes que FALHAM no código unfixed, confirmando o bug
2. **Preservation (Task 2)**: Escrever testes que PASSAM no código unfixed, capturando comportamento a preservar
3. **Implementation (Task 3)**: Implementar a correção
4. **Validation (Tasks 3.6-3.7)**: Verificar que testes de exploration agora PASSAM e testes de preservation ainda PASSAM

### Arquivos a Modificar

1. **LoginActivity.kt** - Adicionar verificação automática de biometria
2. **MainActivity.kt** - Adicionar verificação de token ao iniciar + acesso offline
3. **RefreshTokenInterceptor.kt** - Adicionar tentativa de renovação via biometria + thread safety

### Componentes Existentes (NÃO Modificar)

- ✅ `RenovarTokenComBiometriaUseCase.kt` - Já funciona perfeitamente
- ✅ `LoginViewModel.loginWithBiometric()` - Já implementado
- ✅ `BiometricAuthManager.authenticateWithCancel()` - Já funciona
- ✅ `PreferencesManager.kt` - Já gerencia tokens e configurações

---

**Última atualização:** 09/12/2025  
**Versão:** 1.0.0  
**Status:** ✅ Tasks Criadas - Pronto para Implementação
