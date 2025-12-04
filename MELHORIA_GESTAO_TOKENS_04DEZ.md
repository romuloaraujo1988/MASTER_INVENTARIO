# Melhoria na Gestão de Tokens - 04/12/2025

## 🎯 Problema Identificado

O usuário relatou que às vezes o token está inválido e o app não redireciona adequadamente para a tela de login. Além disso, para uso offline, o token precisa ser válido por pelo menos 2 dias.

## 📊 Situação Anterior

| Token | Duração | Problema |
|-------|---------|----------|
| Access Token | 24 horas (86400s) | Insuficiente para uso offline de 2 dias |
| Refresh Token | 7 dias (604800s) | Adequado, mas sem validação local |

## ✅ Solução Implementada

### 1. Aumento da Duração dos Tokens (Backend)

**Arquivos alterados:**
- `src/main/resources/application-mobile.properties`
- `dist/desktop/application-mobile.properties`
- `dist/mobile-server/application-mobile.properties`
- `dist/producao/application-mobile.properties`

**Nova configuração:**
```properties
# Access Token: 2 dias (172800 segundos) - Permite uso offline por 2 dias
jwt.expiration=172800

# Refresh Token: 14 dias (1209600 segundos) - Permite renovação por 2 semanas
jwt.refresh-expiration=1209600
```

| Token | Duração Anterior | Nova Duração |
|-------|------------------|--------------|
| Access Token | 24 horas | **2 dias** |
| Refresh Token | 7 dias | **14 dias** |

### 2. Melhorias no TokenManager (Android)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/auth/TokenManager.kt`

**Novos métodos:**
- `isRefreshTokenExpired()` - Verifica se refresh token expirou (14 dias desde login)
- `needsRelogin()` - Verifica se precisa fazer login novamente
- `getTokenDebugInfo()` - Retorna informações de debug sobre tokens

**Lógica melhorada:**
```kotlin
fun needsRelogin(): Boolean {
    // Sem token = precisa login
    if (token.isNullOrEmpty()) return true
    
    // Token válido = não precisa login
    if (!isTokenExpired()) return false
    
    // Token expirado - verificar refresh token
    if (!canRefreshToken()) return true
    
    // Token expirado mas pode renovar
    return false
}
```

### 3. Melhorias no PreferencesManager (Android)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/PreferencesManager.kt`

**Novos métodos:**
- `getLoginTimestamp()` - Obtém timestamp do último login
- `setLoginTimestamp()` - Salva timestamp do login
- `getTokenTimeRemaining()` - Retorna tempo restante em formato legível

**Melhoria no saveTokens():**
- Agora salva o timestamp de login para calcular expiração do refresh token

### 4. Melhorias no RefreshTokenInterceptor (Android)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/network/RefreshTokenInterceptor.kt`

**Melhorias:**
- Verifica se refresh token expirou antes de tentar renovar
- Logs mais detalhados sobre o estado da renovação
- Tratamento específico para erro 401 do servidor

### 5. Melhorias na SplashActivity (Android)

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/ui/splash/SplashActivity.kt`

**Melhorias:**
- Usa novo método `needsRelogin()` para decisão mais precisa
- Logs de debug com informações completas dos tokens
- Mensagem clara quando sessão expira após 14 dias

## 🔄 Fluxo de Validação de Token

```
┌─────────────────────────────────────────────────────────────┐
│                    App Inicia (Splash)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
              ┌────────────────┐
              │ Está logado?   │
              └───────┬────────┘
                      │
         ┌────────────┴────────────┐
         │ NÃO                     │ SIM
         ▼                         ▼
    ┌─────────┐           ┌────────────────┐
    │ Login   │           │ needsRelogin() │
    └─────────┘           └───────┬────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │ FALSE                     │ TRUE
                    ▼                           ▼
           ┌────────────────┐          ┌────────────────┐
           │ Token válido?  │          │ Login          │
           └───────┬────────┘          │ (sessão        │
                   │                   │  expirou)      │
      ┌────────────┴────────────┐      └────────────────┘
      │ SIM                     │ NÃO
      ▼                         ▼
 ┌──────────┐          ┌────────────────────┐
 │ MainActivity │      │ MainActivity       │
 │ (direto)     │      │ (RefreshInterceptor│
 └──────────────┘      │  renova token)     │
                       └────────────────────┘
```

## 📱 Comportamento Esperado

### Cenário 1: Uso Normal (Online)
1. Usuário faz login
2. Access token válido por 2 dias
3. Após 2 dias, RefreshTokenInterceptor renova automaticamente
4. Renovação funciona por até 14 dias desde o login

### Cenário 2: Uso Offline (2 dias)
1. Usuário faz login
2. Trabalha offline por até 2 dias
3. Token permanece válido durante todo o período
4. Ao reconectar, sincronização funciona normalmente

### Cenário 3: Sessão Expirada (14+ dias)
1. Usuário não usa o app por mais de 14 dias
2. Ao abrir o app, detecta que refresh token expirou
3. Redireciona para login com mensagem clara
4. Usuário faz novo login

## 🧪 Como Testar

### Teste 1: Token Válido
```
1. Fazer login
2. Verificar logs: "Token válido (expira em: X dias)"
3. App deve ir para MainActivity
```

### Teste 2: Token Expirado mas Renovável
```
1. Fazer login
2. Aguardar 2+ dias (ou simular alterando timestamp)
3. Abrir app
4. Verificar logs: "Token expirado mas pode renovar"
5. App deve ir para MainActivity
6. RefreshTokenInterceptor deve renovar na primeira requisição
```

### Teste 3: Sessão Completamente Expirada
```
1. Fazer login
2. Aguardar 14+ dias (ou simular alterando login_timestamp)
3. Abrir app
4. Verificar logs: "Sessão expirada completamente"
5. App deve ir para LoginActivity
6. Mensagem: "Sua sessão expirou após 14 dias"
```

### Teste 4: Debug Info
```kotlin
// Em qualquer lugar do app:
Log.d("TokenDebug", tokenManager.getTokenDebugInfo())

// Output esperado:
// === Token Debug Info ===
// Has Access Token: true
// Access Token Expired: false
// Has Refresh Token: true
// Refresh Token Expired: false
// Login Timestamp: 04/12/2025 10:30
// Can Refresh: true
// Needs Relogin: false
// ========================
```

## 📋 Checklist de Validação

- [x] Backend: jwt.expiration = 172800 (2 dias)
- [x] Backend: jwt.refresh-expiration = 1209600 (14 dias)
- [x] Android: TokenManager.needsRelogin() implementado
- [x] Android: PreferencesManager.getLoginTimestamp() implementado
- [x] Android: RefreshTokenInterceptor verifica expiração do refresh
- [x] Android: SplashActivity usa needsRelogin()
- [x] Sem erros de compilação

## 🚀 Próximos Passos

1. **Recompilar o servidor** para aplicar nova configuração de JWT
2. **Recompilar o APK** com as melhorias no Android
3. **Testar** os cenários descritos acima
4. **Monitorar** logs em produção para validar comportamento

---

**Data:** 04/12/2025
**Versão:** 2.9.1
**Status:** ✅ Implementado
