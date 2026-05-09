# Renovação Automática de Token via Biometria - Design Técnico

## Overview

Este documento detalha o design técnico para implementar a renovação automática de token JWT via biometria e acesso offline permanente no app Android de inventário. O sistema atual possui todos os componentes necessários funcionando perfeitamente (RenovarTokenComBiometriaUseCase, BiometricAuthManager, PreferencesManager), mas falta a integração automática nos pontos críticos do fluxo.

O bug não está na implementação da renovação - está na ausência de gatilhos automáticos. Quando o token expira, o app não mostra automaticamente o prompt de biometria, forçando o usuário a digitar senha novamente. Além disso, usuários com biometria habilitada devem poder acessar o app offline indefinidamente, sem limite de tempo.

## Glossary

- **Bug_Condition (C)**: (Token expirado OU Offline) + Biometria habilitada + Prompt não mostrado + Acesso bloqueado
- **Property (P)**: Prompt mostrado + Renovação tentada + (Token renovado OU Acesso offline permitido OU Redirecionado para login)
- **Preservation**: Comportamento existente para usuários sem biometria (redirecionar para login) e login manual com senha
- **RenovarTokenComBiometriaUseCase**: Use Case que renova token via refresh token ou permite acesso offline
- **BiometricAuthManager**: Gerenciador de autenticação biométrica (impressão digital/facial)
- **RefreshTokenInterceptor**: Interceptor OkHttp que detecta 401 e pode renovar token
- **PreferencesManager**: Gerencia tokens, dados do usuário e configurações de biometria
- **Token Expirado**: Access token JWT expirou (após 2 dias por padrão)
- **Acesso Offline Permanente**: Biometria funciona como autenticação local sem limite de tempo

## Bug Details

### Bug Condition

O bug manifesta quando o usuário tem biometria habilitada e o token expira (ou está offline), mas o sistema não mostra automaticamente o prompt de biometria, forçando login com senha. Além disso, o acesso offline é bloqueado mesmo com biometria habilitada.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type AppState
  OUTPUT: boolean
  
  RETURN ((input.tokenExpirado = true) OR (input.offline = true)) AND 
         (input.biometriaHabilitada = true) AND
         (input.promptBiometriaAutomaticoMostrado = false) AND
         (input.acessoDadosLocaisBloqueado = true)
END FUNCTION
```

### Examples

- **Cenário 1**: Usuário abre app após 3 dias (token expirado), tem biometria habilitada, mas é redirecionado para LoginActivity sem prompt de biometria
- **Cenário 2**: RefreshTokenInterceptor detecta 401, mas redireciona para login sem tentar renovação via biometria
- **Cenário 3**: MainActivity inicia com token expirado, mas não verifica biometria antes de redirecionar
- **Cenário 4**: Usuário abre app offline após 1 mês, tem biometria habilitada, mas acesso aos dados locais é bloqueado
- **Edge case**: Usuário cancela prompt de biometria → deve redirecionar para login com senha (fallback)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Usuários SEM biometria habilitada continuam sendo redirecionados para LoginActivity normalmente
- Login manual com senha continua funcionando exatamente como antes
- Botão manual de biometria na LoginActivity continua funcionando
- Token válido (não expirado) não aciona nenhum prompt desnecessário
- RenovarTokenComBiometriaUseCase continua funcionando como implementado
- BiometricAuthManager.authenticateWithCancel() continua funcionando
- LoginViewModel.loginWithBiometric() continua funcionando

**Scope:**
Todas as funcionalidades existentes de autenticação devem permanecer inalteradas. As mudanças afetam APENAS o comportamento quando:
1. Token está expirado E biometria está habilitada
2. App está offline E biometria está habilitada
3. Usuário tem dados salvos localmente

## Hypothesized Root Cause

Based on the bug description, the most likely issues are:

1. **LoginActivity Não Verifica Biometria Automaticamente**: Quando chegada da expiração de token, não verifica se biometria está habilitada antes de mostrar formulário de login
   - Falta verificação de `preferencesManager.isBiometricEnabled()` no onCreate
   - Falta chamada automática para `authenticateWithBiometric()` quando token expirado

2. **MainActivity Não Verifica Token ao Iniciar**: Não há verificação de token expirado no onCreate
   - Falta método `verificarTokenEMostrarBiometriaSeNecessario()`
   - Não chama `RenovarTokenComBiometriaUseCase` preventivamente

3. **RefreshTokenInterceptor Não Tenta Biometria**: Ao detectar 401, redireciona diretamente para login sem verificar biometria
   - Falta verificação de `preferencesManager.isBiometricEnabled()`
   - Falta tentativa de renovação via biometria antes de redirecionar
   - Não há callback para resultado da renovação

4. **Acesso Offline Bloqueado**: Sistema não permite acesso aos dados locais via biometria quando offline ou token expirado
   - Falta lógica para permitir acesso offline permanente
   - Validação de token bloqueia acesso mesmo com biometria habilitada
   - Não há diferenciação entre "precisa renovar token" e "pode acessar offline"

## Correctness Properties

Property 1: Bug Condition - Renovação Automática via Biometria + Acesso Offline Permanente

_For any_ app state where the token is expired OR the app is offline, AND biometric authentication is enabled, the system SHALL automatically show the biometric prompt and either renew the token (if online) OR allow offline access to local data (if offline), without requiring password input.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10**

Property 2: Preservation - Comportamento Existente Sem Biometria

_For any_ app state where biometric authentication is NOT enabled, the system SHALL produce exactly the same behavior as the original code, redirecting to LoginActivity and requiring password input.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `LoginActivity.kt`

**Function**: `onCreate()` e novos métodos

**Specific Changes**:
1. **Adicionar Verificação Automática de Biometria**:
   - No onCreate, após inicialização, verificar se chegou da expiração de token
   - Verificar se biometria está habilitada via `preferencesManager.isBiometricEnabled()`
   - Se sim, chamar automaticamente `authenticateWithBiometric()`
   - Mostrar mensagem "Token expirado. Use biometria para renovar"

2. **Criar Método `verificarEMostrarBiometriaSeNecessario()`**:
   - Verificar intent extras para flag "TOKEN_EXPIRED"
   - Verificar `preferencesManager.isBiometricEnabled()`
   - Verificar `preferencesManager.hasUserSavedLocally()`
   - Se todas condições verdadeiras, mostrar prompt automaticamente

3. **Atualizar `authenticateWithBiometric()`**:
   - Após sucesso, chamar `viewModel.loginWithBiometric()`
   - Tratar erro e cancelamento com fallback para senha

4. **Adicionar Suporte a Acesso Offline Permanente**:
   - Não bloquear acesso se biometria está habilitada
   - Permitir autenticação local via biometria independente do tempo
   - Carregar dados do Room local após autenticação biométrica

**File**: `MainActivity.kt`

**Function**: `onCreate()` e novo método

**Specific Changes**:
1. **Adicionar Verificação de Token ao Iniciar**:
   - No onCreate, após setupUI, verificar token
   - Chamar `verificarTokenEMostrarBiometriaSeNecessario()`

2. **Criar Método `verificarTokenEMostrarBiometriaSeNecessario()`**:
   - Verificar se token está expirado via `preferencesManager.isTokenExpired()`
   - Verificar se biometria está habilitada
   - Se sim, mostrar prompt de biometria
   - Após sucesso, chamar `RenovarTokenComBiometriaUseCase`
   - Se falhar, redirecionar para LoginActivity

3. **Adicionar Verificação de Modo Offline**:
   - Verificar se está offline via `NetworkUtils.isNetworkAvailable()`
   - Se offline E biometria habilitada, permitir acesso via biometria
   - Não redirecionar para login se biometria está habilitada

4. **Criar Método `permitirAcessoOfflineComBiometria()`**:
   - Mostrar prompt de biometria
   - Após sucesso, carregar dados do Room local
   - Ativar modo offline na UI
   - Não exigir conexão com servidor

**File**: `RefreshTokenInterceptor.kt`

**Function**: `intercept()` e novo método

**Specific Changes**:
1. **Adicionar Verificação de Biometria no Interceptor**:
   - Ao detectar 401, verificar `preferencesManager.isBiometricEnabled()`
   - Se habilitada, tentar renovação via biometria antes de redirecionar

2. **Criar Método `tentarRenovacaoComBiometria()`**:
   - Obter Activity atual (via Application ou Context)
   - Mostrar prompt de biometria
   - Chamar `RenovarTokenComBiometriaUseCase`
   - Se sucesso, retry requisição original
   - Se falha, redirecionar para login

3. **Adicionar Callback para Resultado**:
   - Interface `BiometricRenewalCallback`
   - Métodos: `onRenewalSuccess()`, `onRenewalFailed()`, `onRenewalCanceled()`
   - Notificar Activity sobre resultado

4. **Thread Safety**:
   - Usar `synchronized` para evitar múltiplas renovações simultâneas
   - Flag `isRenewing` para controlar estado
   - Queue de requisições pendentes durante renovação

5. **Adicionar Suporte a Modo Offline**:
   - Se offline, não tentar renovar token
   - Verificar se biometria está habilitada
   - Permitir acesso aos dados locais via biometria
   - Não bloquear requisições se biometria validada

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Simular cenários de token expirado e offline com biometria habilitada. Executar testes no código UNFIXED para observar falhas e confirmar root cause.

**Test Cases**:
1. **Token Expirado + Biometria Habilitada**: Abrir app após 3 dias (will fail on unfixed code - redireciona para login sem prompt)
2. **MainActivity com Token Expirado**: Iniciar MainActivity com token expirado (will fail on unfixed code - não verifica biometria)
3. **Interceptor Detecta 401**: Fazer requisição que retorna 401 (will fail on unfixed code - redireciona sem tentar biometria)
4. **App Offline + Biometria**: Abrir app sem internet após 1 mês (will fail on unfixed code - bloqueia acesso)

**Expected Counterexamples**:
- LoginActivity mostra formulário de senha ao invés de prompt de biometria
- MainActivity redireciona para login sem verificar biometria
- RefreshTokenInterceptor redireciona para login sem tentar renovação
- Acesso offline bloqueado mesmo com biometria habilitada
- Possible causes: falta de verificação automática, falta de integração com Use Case existente

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := mostrarPromptBiometriaAutomatico(input)
  ASSERT result.promptMostrado = true AND
         result.renovacaoTentada = true AND
         (result.tokenRenovado = true OR result.acessoOfflinePermitido = true OR result.redirecionadoParaLogin = true)
END FOR
```

**Test Cases**:
1. **LoginActivity com Token Expirado**: Verificar que prompt de biometria é mostrado automaticamente
2. **MainActivity Verifica Token**: Verificar que token é verificado ao iniciar e biometria é solicitada
3. **Interceptor Tenta Biometria**: Verificar que 401 aciona tentativa de renovação via biometria
4. **Acesso Offline Permitido**: Verificar que biometria permite acesso aos dados locais offline
5. **Renovação Bem-Sucedida**: Verificar que token é renovado e usuário continua usando app
6. **Fallback para Senha**: Verificar que cancelamento de biometria redireciona para login

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT comportamentoOriginal(input) = comportamentoCorrigido(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observar comportamento no código UNFIXED para usuários sem biometria, então escrever testes capturando esse comportamento.

**Test Cases**:
1. **Usuário Sem Biometria + Token Expirado**: Observar que redireciona para login (deve continuar após fix)
2. **Login Manual com Senha**: Observar que funciona normalmente (deve continuar após fix)
3. **Token Válido**: Observar que app funciona sem prompts (deve continuar após fix)
4. **Botão Manual de Biometria**: Observar que funciona na LoginActivity (deve continuar após fix)

### Unit Tests

- Testar `verificarEMostrarBiometriaSeNecessario()` com diferentes estados
- Testar `tentarRenovacaoComBiometria()` com sucesso e falha
- Testar thread safety do interceptor (múltiplas requisições simultâneas)
- Testar acesso offline com biometria habilitada
- Testar acesso offline sem biometria (deve bloquear)

### Property-Based Tests

- Gerar estados aleatórios de app (token expirado/válido, biometria habilitada/desabilitada, online/offline)
- Verificar que comportamento correto ocorre para cada combinação
- Gerar sequências de ações do usuário e verificar consistência
- Testar que acesso offline funciona indefinidamente com biometria

### Integration Tests

- Testar fluxo completo: abrir app → token expirado → biometria → renovação → continuar usando
- Testar fluxo de fallback: abrir app → token expirado → biometria cancelada → login com senha
- Testar interceptor em requisições reais (com mock server retornando 401)
- Testar acesso offline: abrir app sem internet → biometria → acessar dados locais
- Testar sincronização: conectar internet após acesso offline → renovar token automaticamente

## Technical Notes

### Arquitetura Atual

- **Clean Architecture + MVVM**: App usa camadas bem definidas
- **Hilt**: Injeção de dependência configurada
- **Use Cases**: RenovarTokenComBiometriaUseCase já implementado e testado
- **BiometricAuthManager**: Gerenciador de biometria funcional
- **PreferencesManager**: Gerencia tokens e configurações

### Pontos de Atenção

1. **Thread Safety**: Renovação pode ser chamada de múltiplas threads (interceptor + UI)
   - Usar `synchronized` no interceptor
   - Flag `isRenewing` para evitar race conditions
   - Queue de requisições pendentes

2. **Lifecycle**: Garantir que prompt de biometria respeita lifecycle da Activity
   - Verificar se Activity está em foreground
   - Não mostrar prompt se app está em background
   - Cancelar prompt se Activity é destruída

3. **Fallback**: Sempre ter caminho alternativo (login com senha)
   - Usuário pode cancelar biometria
   - Biometria pode falhar (sensor sujo, etc)
   - Dispositivo pode não ter biometria

4. **Acesso Offline Permanente**: Biometria deve funcionar como autenticação local sem limite de tempo
   - Não validar expiração de token se offline
   - Permitir acesso ao Room local após biometria
   - Sincronizar quando conexão for restaurada

5. **Validação de Token**: Não bloquear acesso offline mesmo com token expirado
   - Verificar se biometria está habilitada
   - Permitir acesso aos dados locais
   - Renovar token em background quando conectar

6. **Sincronização**: Quando conexão for restaurada, tentar renovar token automaticamente
   - Usar WorkManager para sync em background
   - Não bloquear UI durante renovação
   - Mostrar notificação se renovação falhar

### Dependências

- `androidx.biometric:biometric:1.1.0` (já instalado)
- `RenovarTokenComBiometriaUseCase` (já implementado)
- `BiometricAuthManager` (já implementado)
- `PreferencesManager` (já implementado)
- `RefreshTokenInterceptor` (já implementado, precisa modificação)

### Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│                    LoginActivity                             │
│  1. onCreate() verifica token expirado                       │
│  2. Verifica biometria habilitada                            │
│  3. Mostra prompt automaticamente                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              BiometricAuthManager                            │
│  1. Mostra prompt de biometria                               │
│  2. Valida biometria do usuário                              │
│  3. Retorna callback de sucesso/falha                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│         RenovarTokenComBiometriaUseCase                      │
│  1. Obtém refresh token do PreferencesManager                │
│  2. Chama API /auth/refresh                                  │
│  3. Salva novo access token                                  │
│  4. OU permite acesso offline se sem conexão                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 PreferencesManager                           │
│  1. Salva novo access token                                  │
│  2. Atualiza timestamp de expiração                          │
│  3. Marca token como válido                                  │
└─────────────────────────────────────────────────────────────┘
```

### Exemplo de Código

**LoginActivity.kt - Verificação Automática**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ... inicialização existente ...
    
    // Verificar se precisa mostrar biometria automaticamente
    verificarEMostrarBiometriaSeNecessario()
}

private fun verificarEMostrarBiometriaSeNecessario() {
    // Verificar se chegou da expiração de token
    val tokenExpired = intent.getBooleanExtra("TOKEN_EXPIRED", false)
    val isOffline = !NetworkUtils.isNetworkAvailable(this)
    
    // Verificar se biometria está habilitada
    val biometricEnabled = preferencesManager.isBiometricEnabled()
    val hasUserSaved = preferencesManager.hasUserSavedLocally()
    
    if ((tokenExpired || isOffline) && biometricEnabled && hasUserSaved) {
        Log.d(TAG, "Token expirado/offline + biometria habilitada, mostrando prompt automaticamente")
        
        // Mostrar mensagem ao usuário
        val mensagem = if (isOffline) {
            "Sem conexão. Use biometria para acessar dados offline"
        } else {
            "Token expirado. Use biometria para renovar"
        }
        
        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_LONG).show()
        
        // Mostrar prompt de biometria após delay
        binding.root.postDelayed({
            authenticateWithBiometric()
        }, 500)
    }
}
```

**MainActivity.kt - Verificação ao Iniciar**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ... inicialização existente ...
    
    // Verificar token ao iniciar
    verificarTokenEMostrarBiometriaSeNecessario()
}

private fun verificarTokenEMostrarBiometriaSeNecessario() {
    lifecycleScope.launch {
        try {
            val tokenExpired = preferencesManager.isTokenExpired()
            val isOffline = !NetworkUtils.isNetworkAvailable(this@MainActivity)
            val biometricEnabled = preferencesManager.isBiometricEnabled()
            
            if ((tokenExpired || isOffline) && biometricEnabled) {
                Log.d(TAG, "Token expirado/offline, solicitando biometria")
                
                if (isOffline) {
                    permitirAcessoOfflineComBiometria()
                } else {
                    renovarTokenComBiometria()
                }
            } else if (tokenExpired && !biometricEnabled) {
                // Sem biometria, redirecionar para login
                redirecionarParaLogin("Token expirado. Faça login novamente.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar token", e)
        }
    }
}

private fun permitirAcessoOfflineComBiometria() {
    biometricManager.authenticateWithCancel(
        activity = this,
        title = "Acesso Offline",
        subtitle = "Use biometria para acessar dados offline",
        callback = object : BiometricCallback {
            override fun onAuthenticationSucceeded(authenticationType: String) {
                Log.d(TAG, "✅ Biometria validada, permitindo acesso offline")
                // Carregar dados do Room local
                // Ativar modo offline na UI
                Snackbar.make(binding.root, "📴 Modo Offline Ativado", Snackbar.LENGTH_SHORT).show()
            }
            
            override fun onAuthenticationFailed(message: String) {
                redirecionarParaLogin("Biometria falhou. Faça login com senha.")
            }
            
            override fun onAuthenticationCanceled() {
                redirecionarParaLogin("Acesso cancelado. Faça login com senha.")
            }
            
            // ... outros métodos ...
        }
    )
}
```

**RefreshTokenInterceptor.kt - Tentativa de Biometria**:
```kotlin
override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val response = chain.proceed(originalRequest)
    
    // Se receber 401 e biometria está habilitada, tentar renovar
    if (response.code == 401 && !isRefreshTokenRequest(originalRequest)) {
        val biometricEnabled = preferencesManager.isBiometricEnabled()
        
        if (biometricEnabled) {
            Log.d(TAG, "401 detectado + biometria habilitada, tentando renovação")
            
            response.close()
            
            // Tentar renovação com biometria
            val renewalSuccess = tentarRenovacaoComBiometria()
            
            if (renewalSuccess) {
                // Retry requisição com novo token
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${preferencesManager.getAccessToken()}")
                    .build()
                return chain.proceed(newRequest)
            }
        }
        
        // Se não tem biometria ou falhou, redirecionar para login
        notificarTokenExpirado()
    }
    
    return response
}

@Synchronized
private fun tentarRenovacaoComBiometria(): Boolean {
    if (isRenewing) {
        Log.d(TAG, "Renovação já em andamento, aguardando...")
        return false
    }
    
    isRenewing = true
    
    try {
        // Obter Activity atual
        val activity = getCurrentActivity() ?: return false
        
        // Mostrar prompt de biometria
        var renovacaoSucesso = false
        val latch = CountDownLatch(1)
        
        activity.runOnUiThread {
            biometricManager.authenticateWithCancel(
                activity = activity,
                title = "Renovar Sessão",
                subtitle = "Use biometria para renovar token",
                callback = object : BiometricCallback {
                    override fun onAuthenticationSucceeded(authenticationType: String) {
                        // Chamar Use Case
                        val result = runBlocking {
                            renovarTokenUseCase()
                        }
                        
                        renovacaoSucesso = result.isSuccess
                        latch.countDown()
                    }
                    
                    override fun onAuthenticationFailed(message: String) {
                        renovacaoSucesso = false
                        latch.countDown()
                    }
                    
                    // ... outros métodos ...
                }
            )
        }
        
        // Aguardar resultado (com timeout)
        latch.await(30, TimeUnit.SECONDS)
        
        return renovacaoSucesso
        
    } finally {
        isRenewing = false
    }
}
```

---

**Última atualização:** 09/12/2025  
**Versão:** 1.0.0  
**Status:** ✅ Design Técnico Completo
