# Bugfix Requirements Document

## Introduction

O app Android de inventário possui autenticação biométrica funcional, mas apresenta uma falha crítica na experiência do usuário: quando o token JWT expira, o sistema força o usuário a digitar a senha novamente na tela de login, mesmo tendo a biometria habilitada. Este comportamento quebra a experiência esperada, pois aplicativos similares (como apps bancários) renovam o token automaticamente via biometria sem exigir senha.

O problema não está na implementação da renovação de token em si - o fluxo completo já existe e funciona perfeitamente quando o usuário clica manualmente no botão de biometria. O bug está na ausência de um gatilho automático: quando o token expira, o app não mostra automaticamente o prompt de biometria, forçando o usuário a passar pelo fluxo de login com senha.

**Requisito Adicional Crítico:** Usuários com biometria habilitada devem poder acessar o app a qualquer momento para visualizar dados offline, independentemente do tempo decorrido ou do status do token. A biometria deve servir como autenticação local permanente, permitindo acesso aos dados armazenados localmente sem necessidade de conexão com o servidor.

Esta correção visa implementar a renovação automática de token via biometria e garantir acesso offline permanente, proporcionando uma experiência fluida e sem interrupções, alinhada com as melhores práticas de UX em aplicativos móveis modernos.

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN o token JWT expira após o período configurado (ex: 24h) THEN o sistema redireciona o usuário para LoginActivity e exige digitação de senha

1.2 WHEN o usuário abre o app com token expirado THEN o sistema não verifica se biometria está habilitada antes de redirecionar para login

1.3 WHEN o RefreshTokenInterceptor detecta resposta 401 (token expirado) THEN o sistema redireciona diretamente para LoginActivity sem tentar renovação via biometria

1.4 WHEN o MainActivity é iniciado com token expirado THEN o sistema não tenta renovação automática via biometria antes de redirecionar para login

1.5 WHEN o usuário tem biometria habilitada e token expira THEN o sistema ignora a preferência de biometria e força login com senha

1.6 WHEN o usuário com biometria habilitada abre o app offline após muito tempo THEN o sistema bloqueia acesso aos dados locais e exige senha

1.7 WHEN o usuário está offline e token expirado THEN o sistema não permite acesso via biometria aos dados armazenados localmente

### Expected Behavior (Correct)

2.1 WHEN o token JWT expira e biometria está habilitada THEN o sistema SHALL mostrar automaticamente o prompt de biometria para renovação

2.2 WHEN o usuário autentica com biometria após token expirado THEN o sistema SHALL chamar automaticamente `RenovarTokenComBiometriaUseCase` para renovar o token silenciosamente

2.3 WHEN o RefreshTokenInterceptor detecta resposta 401 e biometria está habilitada THEN o sistema SHALL tentar renovação via biometria antes de redirecionar para login

2.4 WHEN o MainActivity é iniciado com token expirado e biometria habilitada THEN o sistema SHALL verificar e mostrar prompt de biometria automaticamente

2.5 WHEN a renovação via biometria é bem-sucedida THEN o sistema SHALL salvar o novo token e permitir que o usuário continue usando o app sem interrupção

2.6 WHEN a renovação via biometria falha (usuário cancela ou erro) THEN o sistema SHALL redirecionar para LoginActivity como fallback

2.7 WHEN o usuário com biometria habilitada abre o app offline (sem conexão) THEN o sistema SHALL permitir acesso via biometria aos dados locais independentemente do tempo decorrido

2.8 WHEN o usuário autentica com biometria em modo offline THEN o sistema SHALL carregar dados do banco Room local sem exigir conexão com servidor

2.9 WHEN o usuário está offline e token expirado THEN o sistema SHALL usar biometria como autenticação local permanente para acesso aos dados

2.10 WHEN o usuário com biometria habilitada não acessa o app por semanas/meses THEN o sistema SHALL AINDA permitir acesso offline via biometria sem exigir senha

### Unchanged Behavior (Regression Prevention)

3.1 WHEN o usuário NÃO tem biometria habilitada e token expira THEN o sistema SHALL CONTINUE TO redirecionar para LoginActivity normalmente

3.2 WHEN o usuário clica manualmente no botão de biometria na LoginActivity THEN o sistema SHALL CONTINUE TO funcionar exatamente como antes

3.3 WHEN o token está válido (não expirado) THEN o sistema SHALL CONTINUE TO funcionar normalmente sem mostrar prompts de autenticação

3.4 WHEN o usuário faz login com senha pela primeira vez THEN o sistema SHALL CONTINUE TO permitir habilitar biometria após login bem-sucedido

3.5 WHEN o `RenovarTokenComBiometriaUseCase` é chamado manualmente THEN o sistema SHALL CONTINUE TO funcionar exatamente como implementado

3.6 WHEN o `BiometricAuthManager.authenticateWithCancel()` é chamado THEN o sistema SHALL CONTINUE TO mostrar o prompt de biometria corretamente

3.7 WHEN o `LoginViewModel.loginWithBiometric()` é executado THEN o sistema SHALL CONTINUE TO processar a renovação de token como implementado

---

## Bug Condition Derivation

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type AppState
  OUTPUT: boolean
  
  // Retorna true quando o bug ocorre
  RETURN ((X.tokenExpirado = true) OR (X.offline = true)) AND 
         (X.biometriaHabilitada = true) AND
         (X.promptBiometriaAutomaticoMostrado = false) AND
         (X.acessoDadosLocaisBloqueado = true)
END FUNCTION
```

### Property Specification - Fix Checking

```pascal
// Property: Renovação Automática via Biometria + Acesso Offline Permanente
FOR ALL X WHERE isBugCondition(X) DO
  result ← mostrarPromptBiometriaAutomatico(X)
  ASSERT result.promptMostrado = true AND
         result.renovacaoTentada = true AND
         (result.tokenRenovado = true OR result.acessoOfflinePermitido = true OR result.redirecionadoParaLogin = true)
END FOR

// Property: Acesso Offline Permanente com Biometria
FOR ALL X WHERE (X.biometriaHabilitada = true) AND (X.offline = true) DO
  result ← autenticarComBiometria(X)
  ASSERT result.biometriaValidada = true IMPLIES result.acessoDadosLocaisPermitido = true
END FOR
```

### Property Specification - Preservation Checking

```pascal
// Property: Comportamento Existente Preservado
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT comportamentoAtual(X) = comportamentoCorrigido(X)
END FOR

// Casos específicos de preservação:
// 1. Biometria desabilitada → redireciona para login (sem mudança)
// 2. Token válido → app funciona normalmente (sem mudança)
// 3. Login manual com senha → funciona como antes (sem mudança)
// 4. Botão manual de biometria → funciona como antes (sem mudança)
```

### Key Definitions

- **F (Original)**: Código atual que redireciona para LoginActivity quando token expira ou bloqueia acesso offline
- **F' (Fixed)**: Código corrigido que verifica biometria e mostra prompt automático antes de redirecionar, e permite acesso offline permanente via biometria
- **C(X)**: (Token expirado OU Offline) + Biometria habilitada + Prompt não mostrado + Acesso bloqueado
- **P(result)**: Prompt mostrado + Renovação tentada + (Token renovado OU Acesso offline permitido OU Redirecionado para login)

---

## Componentes Afetados

### Arquivos que Precisam Modificação

1. **LoginActivity.kt**
   - Adicionar método `verificarEMostrarBiometriaSeNecessario()`
   - Verificar se chegou da expiração de token
   - Mostrar prompt automático se biometria habilitada

2. **MainActivity.kt**
   - Adicionar verificação de token ao iniciar
   - Mostrar prompt de biometria se token expirado
   - Redirecionar para login apenas se biometria falhar

3. **RefreshTokenInterceptor.kt**
   - Ao detectar 401, verificar se biometria está habilitada
   - Tentar renovação via biometria antes de redirecionar
   - Implementar callback para resultado da renovação

### Componentes Existentes (Não Modificar)

- ✅ `RenovarTokenComBiometriaUseCase.kt` - Já funciona perfeitamente
- ✅ `LoginViewModel.loginWithBiometric()` - Já implementado
- ✅ `BiometricAuthManager.authenticateWithCancel()` - Já funciona
- ✅ `PreferencesManager.kt` - Já gerencia tokens corretamente

---

## Fluxo Corrigido Esperado

### Cenário 1: Token Expira Durante Uso do App (Online)

```
1. Usuário está usando o app
2. Token JWT expira
3. App faz requisição → recebe 401
4. RefreshTokenInterceptor detecta 401
5. Verifica: biometria habilitada? → SIM
6. Mostra prompt de biometria automaticamente
7. Usuário autentica com biometria
8. Chama RenovarTokenComBiometriaUseCase
9. Token renovado silenciosamente
10. Requisição original é repetida com novo token
11. Usuário continua usando o app normalmente
```

### Cenário 2: Usuário Abre App com Token Expirado (Online)

```
1. Usuário abre o app (MainActivity)
2. MainActivity verifica token → EXPIRADO
3. Verifica: biometria habilitada? → SIM
4. Mostra prompt de biometria automaticamente
5. Usuário autentica com biometria
6. Chama RenovarTokenComBiometriaUseCase
7. Token renovado
8. App carrega normalmente
```

### Cenário 3: Usuário Abre App Offline (Sem Conexão)

```
1. Usuário abre o app sem conexão com internet
2. MainActivity detecta: offline = true
3. Verifica: biometria habilitada? → SIM
4. Mostra prompt de biometria automaticamente
5. Usuário autentica com biometria
6. Sistema valida biometria localmente
7. Carrega dados do banco Room local
8. Usuário acessa dados offline normalmente
9. Modo offline ativado (sem sincronização)
```

### Cenário 4: Usuário Não Acessa App por Semanas (Offline)

```
1. Usuário não abre o app há 30 dias
2. Token completamente expirado
3. Usuário abre app sem internet
4. MainActivity detecta: offline + token expirado
5. Verifica: biometria habilitada? → SIM
6. Mostra prompt de biometria
7. Usuário autentica com biometria
8. Sistema permite acesso aos dados locais
9. Usuário visualiza coletas, patrimônios, etc.
10. Quando conectar, token será renovado automaticamente
```

### Cenário 5: Renovação Falha (Fallback)

```
1. Token expira
2. Prompt de biometria mostrado
3. Usuário cancela OU biometria falha
4. Sistema redireciona para LoginActivity
5. Usuário digita senha manualmente
6. Login bem-sucedido
```

---

## Critérios de Aceitação

### Validação de Correção (Fix Checking)

- [ ] Quando token expira e biometria está habilitada, prompt de biometria é mostrado automaticamente
- [ ] Após autenticação biométrica bem-sucedida, token é renovado silenciosamente
- [ ] Usuário continua usando o app sem digitar senha
- [ ] Renovação funciona tanto no interceptor quanto no MainActivity
- [ ] Usuário com biometria pode acessar app offline independentemente do tempo decorrido
- [ ] Biometria funciona como autenticação local permanente para dados offline
- [ ] Dados do banco Room são acessíveis via biometria sem conexão com servidor
- [ ] App não bloqueia acesso offline mesmo após semanas/meses sem uso

### Validação de Preservação (Regression Prevention)

- [ ] Usuários sem biometria continuam sendo redirecionados para login normalmente
- [ ] Botão manual de biometria na LoginActivity continua funcionando
- [ ] Login com senha continua funcionando normalmente
- [ ] Token válido não aciona nenhum prompt desnecessário
- [ ] RenovarTokenComBiometriaUseCase continua funcionando como antes

### Validação de UX

- [ ] Experiência similar a apps bancários (renovação silenciosa)
- [ ] Sem interrupções desnecessárias no fluxo do usuário
- [ ] Mensagens claras em caso de falha
- [ ] Fallback para senha funciona corretamente
- [ ] Acesso offline permanente via biometria (sem limite de tempo)
- [ ] Usuário nunca é forçado a digitar senha se tem biometria habilitada
- [ ] Modo offline claramente indicado na UI
- [ ] Sincronização automática quando conexão é restaurada

---

## Notas Técnicas

### Arquitetura Atual

- App usa Clean Architecture + MVVM
- Hilt para injeção de dependência
- Use Cases já implementados e testados
- BiometricAuthManager já funcional

### Pontos de Atenção

1. **Thread Safety**: Renovação pode ser chamada de múltiplas threads (interceptor + UI)
2. **Race Condition**: Evitar múltiplas renovações simultâneas
3. **Lifecycle**: Garantir que prompt de biometria respeita lifecycle da Activity
4. **Fallback**: Sempre ter caminho alternativo (login com senha)
5. **Acesso Offline Permanente**: Biometria deve funcionar como autenticação local sem limite de tempo
6. **Validação de Token**: Não bloquear acesso offline mesmo com token expirado se biometria está habilitada
7. **Sincronização**: Quando conexão for restaurada, tentar renovar token automaticamente em background

### Dependências

- `androidx.biometric:biometric:1.1.0` (já instalado)
- `RenovarTokenComBiometriaUseCase` (já implementado)
- `BiometricAuthManager` (já implementado)
- `PreferencesManager` (já implementado)

---

**Última atualização:** 09/12/2025  
**Versão:** 1.1.0  
**Status:** ✅ Requisitos Atualizados - Incluído Acesso Offline Permanente
