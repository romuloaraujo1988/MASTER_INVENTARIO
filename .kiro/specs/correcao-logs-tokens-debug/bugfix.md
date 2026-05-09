# Bugfix Requirements Document

## Introduction

A `SplashActivity` está logando informações de debug dos tokens JWT (access token e refresh token) usando `Log.d()` na linha 138. Esses logs aparecem no Logcat e podem ser capturados por desenvolvedores com acesso ao dispositivo via ADB, apps maliciosos com permissão de leitura de logs (Android < 4.1), ferramentas de análise de logs e logs de crash reports que incluem Logcat. Isso causa exposição de tokens em texto plano, risco de session hijacking, violação de LGPD/GDPR e falha em auditorias de segurança.

**Impacto:** 🔴 CRÍTICO - Exposição de credenciais de autenticação, risco de session hijacking, violação de privacidade (LGPD/GDPR), falha em auditorias de segurança.

**Componentes Afetados:**
- `SplashActivity.kt` (linha 138) - Loga tokens em debug
- `TokenManager.kt` - Método `getTokenDebugInfo()` retorna tokens completos
- `LogSanitizer.kt` - Já existe mas não está sendo usado
- Outros arquivos que podem logar tokens

**Componentes Disponíveis:**
- `LogSanitizer.kt` - Classe pronta para sanitizar logs com método `sanitize(message: String)` e regex para detectar JWT: `eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+`

---

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN `SplashActivity` é iniciada THEN o sistema loga tokens JWT completos em texto plano via `Log.d(TAG, tokenManager.getTokenDebugInfo())`

1.2 WHEN o Logcat é acessado via ADB THEN os tokens JWT aparecem visíveis em texto plano

1.3 WHEN um crash report é gerado THEN os logs do Logcat (incluindo tokens) são incluídos no relatório

1.4 WHEN `tokenManager.getTokenDebugInfo()` é chamado THEN o método retorna access token e refresh token completos sem sanitização

1.5 WHEN logs de debug são habilitados THEN tokens sensíveis são expostos em múltiplos pontos do código

1.6 WHEN ferramentas de análise de logs são usadas THEN tokens JWT podem ser extraídos automaticamente via regex

1.7 WHEN builds de release são geradas THEN logs de debug ainda podem aparecer se não forem removidos pelo ProGuard

### Expected Behavior (Correct)

2.1 WHEN `SplashActivity` é iniciada THEN o sistema SHALL logar apenas informações não sensíveis (ex: "Token válido: true") OU usar `LogSanitizer.log()` para sanitizar automaticamente

2.2 WHEN o Logcat é acessado via ADB THEN tokens JWT SHALL aparecer mascarados (ex: "eyJ***...***abc")

2.3 WHEN um crash report é gerado THEN os logs SHALL conter apenas tokens sanitizados ou informações não sensíveis

2.4 WHEN `tokenManager.getTokenDebugInfo()` é chamado THEN o método SHALL retornar apenas informações não sensíveis (validade, expiração, tipo) sem tokens completos

2.5 WHEN logs de debug são habilitados THEN o sistema SHALL usar `LogSanitizer` para mascarar automaticamente dados sensíveis

2.6 WHEN ferramentas de análise de logs são usadas THEN tokens JWT SHALL estar mascarados e não extraíveis

2.7 WHEN builds de release são geradas THEN logs de debug SHALL ser completamente removidos pelo ProGuard/R8

2.8 WHEN `LogSanitizer.sanitize()` é chamado com mensagem contendo JWT THEN o método SHALL retornar mensagem com token mascarado

2.9 WHEN código precisa logar para debug THEN desenvolvedores SHALL usar `LogSanitizer.log()` ao invés de `Log.d()` direto

### Unchanged Behavior (Regression Prevention)

3.1 WHEN logs de informação não sensível são gerados THEN o sistema SHALL CONTINUE TO logar normalmente sem sanitização

3.2 WHEN `SplashActivity` valida tokens THEN o sistema SHALL CONTINUE TO funcionar corretamente sem afetar a lógica de autenticação

3.3 WHEN tokens são armazenados em `SharedPreferences` criptografadas THEN o sistema SHALL CONTINUE TO usar criptografia normalmente

3.4 WHEN tokens são enviados em requisições HTTP THEN o sistema SHALL CONTINUE TO usar HTTPS e headers seguros

3.5 WHEN logs de erro (não sensíveis) são gerados THEN o sistema SHALL CONTINUE TO logar erros para debug

3.6 WHEN `TokenManager` gerencia tokens THEN o sistema SHALL CONTINUE TO funcionar normalmente (apenas método de debug muda)

3.7 WHEN builds de debug são geradas THEN o sistema SHALL CONTINUE TO incluir logs (mas sanitizados)

---

## Bug Condition and Property

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type LogStatement
  OUTPUT: boolean
  
  // Retorna true quando log contém token JWT não sanitizado
  RETURN X.message CONTAINS_JWT_TOKEN 
         AND NOT X.isSanitized
         AND X.logLevel IN [DEBUG, INFO, WARN, ERROR]
END FUNCTION
```

**Explicação:** O bug ocorre quando uma mensagem de log contém um token JWT completo (detectável via regex `eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+`) e não foi sanitizada antes de ser logada.

### Property Specification - Fix Checking

```pascal
// Property: Fix Checking - Tokens Sanitizados em Logs
FOR ALL X WHERE isBugCondition(X) DO
  result ← logStatement'(X)
  ASSERT result.message NOT_CONTAINS_COMPLETE_JWT
  ASSERT result.message CONTAINS_MASKED_TOKEN OR result.message CONTAINS_NON_SENSITIVE_INFO
  ASSERT result.usedSanitizer = true OR result.containsNoSensitiveData = true
END FOR
```

**Explicação:** Para todos os logs onde o bug ocorria (logs com tokens completos), após a correção:
- A mensagem NÃO DEVE conter JWT completo
- A mensagem DEVE conter token mascarado OU apenas informações não sensíveis
- O log DEVE ter usado `LogSanitizer` OU não conter dados sensíveis

### Property Specification - Preservation Checking

```pascal
// Property: Preservation Checking - Logs Não Sensíveis Preservados
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT logStatement(X) = logStatement'(X)
END FOR
```

**Explicação:** Para todos os logs onde o bug NÃO ocorre (logs sem dados sensíveis), o comportamento DEVE permanecer idêntico.

---

## Counterexample (Demonstração do Bug)

**Cenário:** Desenvolvedor inicia app e visualiza tokens no Logcat

**Entrada:**
```kotlin
// Estado atual em SplashActivity.kt (linha 138)
Log.d(TAG, tokenManager.getTokenDebugInfo())

// TokenManager.kt
fun getTokenDebugInfo(): String {
    return """
        Access Token: ${getAccessToken()}
        Refresh Token: ${getRefreshToken()}
        Expires At: ${getTokenExpiration()}
    """.trimIndent()
}
```

**Comportamento Atual (Buggy):**
```bash
# Logcat mostra tokens completos
adb logcat | grep SplashActivity

# Resultado
D/SplashActivity: Access Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
D/SplashActivity: Refresh Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicmVmcmVzaCI6dHJ1ZSwiaWF0IjoxNTE2MjM5MDIyfQ.abc123def456ghi789jkl012mno345pqr678stu901vwx234yz

# ❌ Tokens completos expostos
# ❌ Podem ser copiados e usados para session hijacking
# ❌ Aparecem em crash reports
```

**Comportamento Esperado (Fixed):**

**Opção 1: Usar LogSanitizer**
```kotlin
// SplashActivity.kt (linha 138)
LogSanitizer.log(TAG, tokenManager.getTokenDebugInfo())

// TokenManager.kt (sem mudanças necessárias)
fun getTokenDebugInfo(): String {
    return """
        Access Token: ${getAccessToken()}
        Refresh Token: ${getRefreshToken()}
        Expires At: ${getTokenExpiration()}
    """.trimIndent()
}

// LogSanitizer.kt (já existe)
fun log(tag: String, message: String) {
    Log.d(tag, sanitize(message))
}

fun sanitize(message: String): String {
    return message.replace(
        Regex("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"),
        "eyJ***...***[MASKED]"
    )
}
```

```bash
# Logcat mostra tokens mascarados
adb logcat | grep SplashActivity

# Resultado
D/SplashActivity: Access Token: eyJ***...***[MASKED]
D/SplashActivity: Refresh Token: eyJ***...***[MASKED]
D/SplashActivity: Expires At: 2025-12-09T15:30:00Z

# ✅ Tokens mascarados
# ✅ Informações úteis para debug (expiração)
# ✅ Seguro para crash reports
```

**Opção 2: Logar Apenas Informações Não Sensíveis**
```kotlin
// SplashActivity.kt (linha 138)
Log.d(TAG, tokenManager.getTokenDebugInfo())

// TokenManager.kt (modificado)
fun getTokenDebugInfo(): String {
    val accessToken = getAccessToken()
    val refreshToken = getRefreshToken()
    
    return """
        Access Token Valid: ${accessToken != null}
        Refresh Token Valid: ${refreshToken != null}
        Token Expires At: ${getTokenExpiration()}
        Token Type: Bearer
    """.trimIndent()
}
```

```bash
# Logcat mostra apenas informações não sensíveis
adb logcat | grep SplashActivity

# Resultado
D/SplashActivity: Access Token Valid: true
D/SplashActivity: Refresh Token Valid: true
D/SplashActivity: Token Expires At: 2025-12-09T15:30:00Z
D/SplashActivity: Token Type: Bearer

# ✅ Nenhum token exposto
# ✅ Informações úteis para debug
# ✅ Totalmente seguro
```

---

## Technical Context

### Arquitetura Atual

```
SplashActivity.kt (linha 138)
    ↓ (chama)
TokenManager.getTokenDebugInfo()
    ↓ (retorna)
"Access Token: eyJ..." ❌ TOKEN COMPLETO
    ↓ (loga)
Log.d(TAG, message)
    ↓ (aparece em)
Logcat ❌ EXPOSTO
    ↓ (capturado por)
- ADB
- Crash Reports
- Apps Maliciosos
- Ferramentas de Análise
```

### Arquitetura Proposta (Opção 1: LogSanitizer)

```
SplashActivity.kt (linha 138)
    ↓ (chama)
LogSanitizer.log(TAG, tokenManager.getTokenDebugInfo())
    ↓ (sanitiza)
LogSanitizer.sanitize(message)
    ↓ (mascara tokens)
"Access Token: eyJ***...***[MASKED]" ✅ MASCARADO
    ↓ (loga)
Log.d(TAG, sanitizedMessage)
    ↓ (aparece em)
Logcat ✅ SEGURO
```

### Arquitetura Proposta (Opção 2: Informações Não Sensíveis)

```
SplashActivity.kt (linha 138)
    ↓ (chama)
TokenManager.getTokenDebugInfo()
    ↓ (retorna apenas)
"Token Valid: true, Expires: ..." ✅ NÃO SENSÍVEL
    ↓ (loga)
Log.d(TAG, message)
    ↓ (aparece em)
Logcat ✅ SEGURO
```

### Código Problemático

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/ui/splash/SplashActivity.kt`

**Linha:** 138

```kotlin
// ❌ PROBLEMA: Loga tokens completos
Log.d(TAG, tokenManager.getTokenDebugInfo())
```

**Arquivo:** `TokenManager.kt` (localização exata a confirmar)

```kotlin
// ❌ PROBLEMA: Retorna tokens completos
fun getTokenDebugInfo(): String {
    return """
        Access Token: ${getAccessToken()}
        Refresh Token: ${getRefreshToken()}
        Expires At: ${getTokenExpiration()}
    """.trimIndent()
}
```

### Solução Proposta

**Solução 1: Usar LogSanitizer (Recomendado para Debug)**

```kotlin
// SplashActivity.kt (linha 138)
// ✅ SOLUÇÃO: Usar LogSanitizer
if (BuildConfig.DEBUG) {
    LogSanitizer.log(TAG, tokenManager.getTokenDebugInfo())
}
```

**Solução 2: Modificar getTokenDebugInfo() (Recomendado para Produção)**

```kotlin
// TokenManager.kt
fun getTokenDebugInfo(): String {
    val accessToken = getAccessToken()
    val refreshToken = getRefreshToken()
    val expiresAt = getTokenExpiration()
    
    return """
        Access Token Valid: ${accessToken != null && accessToken.isNotEmpty()}
        Refresh Token Valid: ${refreshToken != null && refreshToken.isNotEmpty()}
        Token Expires At: $expiresAt
        Token Expired: ${isTokenExpired()}
    """.trimIndent()
}
```

**Solução 3: Remover Completamente em Release**

```kotlin
// SplashActivity.kt (linha 138)
// ✅ SOLUÇÃO: Remover em release
if (BuildConfig.DEBUG) {
    LogSanitizer.log(TAG, tokenManager.getTokenDebugInfo())
}
// Em release, nenhum log é gerado
```

**Solução 4: Adicionar Lint Rule (Prevenção)**

```kotlin
// lint-rules/src/main/java/com/inventario/lint/NoTokenLoggingDetector.kt
class NoTokenLoggingDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> {
        return listOf("d", "i", "w", "e", "v")
    }
    
    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val message = node.valueArguments.getOrNull(1)?.asSourceString() ?: return
        
        // Detectar se mensagem pode conter token
        if (message.contains("token", ignoreCase = true) || 
            message.contains("jwt", ignoreCase = true)) {
            context.report(
                ISSUE,
                node,
                context.getLocation(node),
                "Possível log de token sensível. Use LogSanitizer.log() ao invés de Log.d()"
            )
        }
    }
}
```

---

## Validation Criteria

### Critério 1: Tokens Não Aparecem em Logcat
- ✅ Iniciar app em modo debug
- ✅ Executar `adb logcat | grep -E "eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+"`
- ✅ Verificar que nenhum token completo aparece
- ✅ Verificar que apenas tokens mascarados ou informações não sensíveis aparecem

### Critério 2: LogSanitizer Funciona Corretamente
- ✅ Criar teste unitário para `LogSanitizer.sanitize()`
- ✅ Passar mensagem com JWT completo
- ✅ Verificar que retorna mensagem com token mascarado
- ✅ Verificar que regex detecta todos os formatos de JWT

### Critério 3: getTokenDebugInfo() Não Retorna Tokens
- ✅ Chamar `tokenManager.getTokenDebugInfo()`
- ✅ Verificar que retorno não contém tokens completos
- ✅ Verificar que retorno contém informações úteis (validade, expiração)
- ✅ Verificar que regex JWT não detecta tokens no retorno

### Critério 4: Builds de Release Não Contêm Logs
- ✅ Gerar build de release: `./gradlew assembleRelease`
- ✅ Descompilar APK: `jadx app-release.apk`
- ✅ Buscar por `Log.d` no código descompilado
- ✅ Verificar que ProGuard/R8 removeu logs de debug

### Critério 5: Crash Reports Não Expõem Tokens
- ✅ Forçar crash do app após login
- ✅ Analisar crash report gerado
- ✅ Verificar que Logcat incluído não contém tokens completos
- ✅ Verificar que apenas tokens mascarados aparecem

### Critério 6: Outros Logs de Tokens Foram Corrigidos
- ✅ Buscar no código: `grep -r "Log\\.d.*token" app/src/`
- ✅ Verificar cada ocorrência encontrada
- ✅ Aplicar sanitização onde necessário
- ✅ Documentar locais corrigidos

### Critério 7: Lint Rule Detecta Novos Problemas
- ✅ Adicionar lint rule customizada
- ✅ Executar `./gradlew lint`
- ✅ Verificar que rule detecta logs suspeitos
- ✅ Verificar que não há falsos positivos

---

## Security Checklist

### Análise de Código
- [ ] Buscar todos os logs que podem conter tokens
- [ ] Verificar `TokenManager` e classes relacionadas
- [ ] Verificar `AuthInterceptor` e classes de rede
- [ ] Verificar `SharedPreferences` (logs de save/load)
- [ ] Verificar Activities de login/splash

### Implementação
- [ ] Implementar sanitização em `SplashActivity`
- [ ] Modificar `getTokenDebugInfo()` para não retornar tokens
- [ ] Adicionar `LogSanitizer.log()` onde necessário
- [ ] Configurar ProGuard para remover logs em release
- [ ] Adicionar lint rule para prevenir novos problemas

### Testes
- [ ] Testar sanitização com tokens reais
- [ ] Testar que app funciona normalmente após mudanças
- [ ] Testar build de release (sem logs)
- [ ] Testar crash reports (tokens mascarados)
- [ ] Testar Logcat via ADB (tokens mascarados)

### Documentação
- [ ] Documentar uso de `LogSanitizer` para equipe
- [ ] Atualizar guia de desenvolvimento
- [ ] Adicionar seção de segurança no README
- [ ] Criar checklist de code review para logs

### Auditoria
- [ ] Revisar todo o código Android
- [ ] Revisar código backend (se aplicável)
- [ ] Verificar conformidade com LGPD/GDPR
- [ ] Preparar relatório de correção para auditoria

---

## ProGuard/R8 Configuration

Adicionar ao `proguard-rules.pro` para remover logs em release:

```proguard
# Remove todos os logs de debug em release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# Manter logs de erro e warning
-assumenosideeffects class android.util.Log {
    public static int e(...);
    public static int w(...);
}

# Remover LogSanitizer em release (opcional)
-assumenosideeffects class com.inventario.mobile.util.LogSanitizer {
    public static void log(...);
}
```

---

## Impacto e Riscos

### Riscos Atuais (Antes da Correção)
- 🔴 **CRÍTICO**: Tokens JWT expostos em Logcat
- 🔴 **CRÍTICO**: Risco de session hijacking
- 🔴 **ALTO**: Violação de LGPD/GDPR
- 🟡 **MÉDIO**: Falha em auditorias de segurança
- 🟡 **MÉDIO**: Tokens em crash reports

### Riscos da Correção
- 🟢 **BAIXO**: Logs de debug menos detalhados
- 🟢 **BAIXO**: Desenvolvedores precisam usar `LogSanitizer`
- 🟢 **BAIXO**: Possível impacto em debug de problemas de autenticação

### Benefícios da Correção
- ✅ Tokens não mais expostos em logs
- ✅ Conformidade com LGPD/GDPR
- ✅ Aprovação em auditorias de segurança
- ✅ Redução de risco de session hijacking
- ✅ Crash reports seguros
- ✅ Prevenção automática via lint rules

---

## Referências

- [OWASP Mobile Top 10 - M2: Insecure Data Storage](https://owasp.org/www-project-mobile-top-10/)
- [Android Developers - Log](https://developer.android.com/reference/android/util/Log)
- [Android Developers - ProGuard](https://developer.android.com/studio/build/shrink-code)
- [LGPD - Lei Geral de Proteção de Dados](http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm)
- [GDPR - General Data Protection Regulation](https://gdpr.eu/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
