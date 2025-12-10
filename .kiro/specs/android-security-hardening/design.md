# Design Document - Android Security Hardening

## Overview

Este documento descreve o design técnico para implementação das medidas de segurança críticas no aplicativo Android do Sistema de Inventário de Patrimônio. O objetivo é elevar o nível de segurança de 30% para 85%, seguindo as melhores práticas OWASP Mobile Security.

A implementação será feita de forma incremental, aproveitando componentes já existentes (`SecureStorage`, `SessionManager`, `BiometricManager`) e adicionando novos componentes de segurança.

## Architecture

### Visão Geral da Arquitetura de Segurança

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │
│  │ LoginActivity│  │ MainActivity│  │ ScannerAct. │                  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                  │
│         │                │                │                          │
│         └────────────────┼────────────────┘                          │
│                          ▼                                           │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Security Layer                              │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐   │  │
│  │  │SecureStorage │ │SessionManager│ │LoginAttemptManager   │   │  │
│  │  │(Encrypted)   │ │(Timeout)     │ │(Brute Force Protect) │   │  │
│  │  └──────────────┘ └──────────────┘ └──────────────────────┘   │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐   │  │
│  │  │InputValidator│ │LogSanitizer  │ │BiometricManager      │   │  │
│  │  │(Sanitization)│ │(Log Safety)  │ │(Biometric Auth)      │   │  │
│  │  └──────────────┘ └──────────────┘ └──────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Data Layer                                  │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    Encrypted Storage                         │    │
│  │  ┌──────────────────┐  ┌──────────────────────────────────┐ │    │
│  │  │EncryptedShared   │  │ SQLCipher Room Database          │ │    │
│  │  │Preferences       │  │ (AES-256 Encrypted)              │ │    │
│  │  │(AES-256-GCM)     │  │                                  │ │    │
│  │  └──────────────────┘  └──────────────────────────────────┘ │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Network Layer                                 │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    Secure Communication                      │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │    │
│  │  │Certificate   │  │TLS 1.2+     │  │Timeout Config    │   │    │
│  │  │Pinning       │  │Enforcement   │  │(30s)             │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### Fluxo de Autenticação Segura

```
┌─────────┐     ┌──────────────────┐     ┌─────────────────┐
│  User   │────▶│LoginAttemptManager│────▶│ Check if Locked │
└─────────┘     └──────────────────┘     └────────┬────────┘
                                                   │
                        ┌──────────────────────────┼──────────────────────────┐
                        │                          │                          │
                        ▼                          ▼                          │
                 ┌─────────────┐           ┌─────────────┐                    │
                 │   LOCKED    │           │  NOT LOCKED │                    │
                 │Show Timeout │           │  Continue   │                    │
                 └─────────────┘           └──────┬──────┘                    │
                                                  │                           │
                                                  ▼                           │
                                          ┌─────────────┐                     │
                                          │ Validate    │                     │
                                          │ Credentials │                     │
                                          └──────┬──────┘                     │
                                                 │                            │
                        ┌────────────────────────┼────────────────────────┐   │
                        │                        │                        │   │
                        ▼                        ▼                        │   │
                 ┌─────────────┐          ┌─────────────┐                 │   │
                 │   FAILED    │          │   SUCCESS   │                 │   │
                 │Record Attempt│          │Reset Counter│                 │   │
                 └──────┬──────┘          └──────┬──────┘                 │   │
                        │                        │                        │   │
                        ▼                        ▼                        │   │
                 ┌─────────────┐          ┌─────────────┐                 │   │
                 │ >= 5 Fails? │          │Save to      │                 │   │
                 └──────┬──────┘          │SecureStorage│                 │   │
                        │                 └──────┬──────┘                 │   │
               ┌────────┴────────┐               │                        │   │
               │                 │               ▼                        │   │
               ▼                 ▼        ┌─────────────┐                 │   │
        ┌───────────┐     ┌───────────┐   │Start Session│                 │   │
        │Lock 15min │     │Show Error │   │Manager      │                 │   │
        └───────────┘     └───────────┘   └─────────────┘                 │   │
                                                                          │   │
                                                                          │   │
                                                                          │   │
                                                                          │   │
```

## Components and Interfaces

### 1. SecureStorage (Existente - Aprimorar)

```kotlin
// security/SecureStorage.kt - JÁ EXISTE
// Aprimoramentos necessários:
// - Adicionar método para migração de dados não criptografados
// - Adicionar validação de integridade

interface ISecureStorage {
    fun saveJwtToken(token: String)
    fun getJwtToken(): String?
    fun clearJwtToken()
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun saveUserId(userId: Long)
    fun getUserId(): Long
    fun saveUsername(username: String)
    fun getUsername(): String?
    fun clearAll()
    fun hasValidSession(): Boolean
    
    // Novos métodos
    fun migrateFromPlainPreferences(context: Context)
    fun saveDatabaseKey(key: ByteArray)
    fun getDatabaseKey(): ByteArray?
}
```

### 2. SessionManagerEnhanced (Novo)

```kotlin
// security/SessionManagerEnhanced.kt
interface ISessionManagerEnhanced {
    fun startSession()
    fun updateActivity()
    fun isSessionExpired(): Boolean
    fun getTimeUntilExpiry(): Long
    fun isExpiringSoon(): Boolean  // < 1 minuto
    fun logout(showMessage: Boolean = true)
    fun saveStateBeforeLogout()
    fun restoreStateAfterLogin(): Boolean
}

// Constantes
const val SESSION_TIMEOUT_MINUTES = 15L
const val EXPIRY_WARNING_MINUTES = 1L
```

### 3. LoginAttemptManager (Novo)

```kotlin
// security/LoginAttemptManager.kt
interface ILoginAttemptManager {
    fun recordFailedAttempt()
    fun recordSuccessfulLogin()
    fun isLocked(): Boolean
    fun getFailedAttemptCount(): Int
    fun getRemainingLockoutTime(): Long  // em milissegundos
    fun reset()
}

// Constantes
const val MAX_FAILED_ATTEMPTS = 5
const val LOCKOUT_DURATION_MINUTES = 15L
```

### 4. InputValidator (Novo)

```kotlin
// security/InputValidator.kt
object InputValidator {
    fun validatePatrimonioNumber(numero: String): ValidationResult
    fun sanitizeText(input: String): String
    fun validateEmail(email: String): ValidationResult
    fun validatePassword(password: String): ValidationResult
    fun truncateToMaxLength(input: String, maxLength: Int = 255): String
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
```

### 5. LogSanitizer (Novo)

```kotlin
// security/LogSanitizer.kt
object LogSanitizer {
    fun sanitize(message: String): String
    fun sanitizeMap(data: Map<String, Any?>): Map<String, Any?>
    fun isDebugBuild(): Boolean
    fun log(tag: String, message: String, level: LogLevel = LogLevel.DEBUG)
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR }
```

### 6. DatabaseEncryptionManager (Novo)

```kotlin
// security/DatabaseEncryptionManager.kt
interface IDatabaseEncryptionManager {
    fun getOrCreateDatabaseKey(): ByteArray
    fun createEncryptedDatabase(context: Context): AppDatabase
    fun migrateUnencryptedDatabase(context: Context): Boolean
    fun verifyDatabaseIntegrity(): Boolean
}
```

## Data Models

### LoginAttemptState

```kotlin
data class LoginAttemptState(
    val failedAttempts: Int = 0,
    val lastAttemptTimestamp: Long = 0L,
    val lockoutEndTimestamp: Long = 0L,
    val isLocked: Boolean = false
)
```

### SessionState

```kotlin
data class SessionState(
    val lastActivityTimestamp: Long = 0L,
    val sessionStartTimestamp: Long = 0L,
    val isActive: Boolean = false,
    val savedState: Bundle? = null
)
```

### SecurityConfig

```kotlin
data class SecurityConfig(
    val sessionTimeoutMinutes: Long = 15L,
    val maxLoginAttempts: Int = 5,
    val lockoutDurationMinutes: Long = 15L,
    val connectionTimeoutSeconds: Long = 30L,
    val maxRetryAttempts: Int = 3
)
```



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

Após análise dos critérios de aceitação, identifiquei as seguintes propriedades testáveis:

**Propriedades de Armazenamento Seguro:**
- Round-trip de tokens (salvar/recuperar)
- Limpeza completa no logout

**Propriedades de Validação de Entrada:**
- Validação de números de patrimônio
- Sanitização de caracteres perigosos
- Truncamento de strings longas

**Propriedades de Sessão:**
- Expiração após timeout
- Reset de tentativas após sucesso

**Propriedades de Bloqueio de Login:**
- Bloqueio após N tentativas
- Persistência do estado de bloqueio

**Propriedades de Log:**
- Ofuscação de dados sensíveis

**Propriedades de Retry:**
- Backoff exponencial limitado

---

### Property 1: Token Round-Trip Consistency

*For any* token JWT válido, salvar no SecureStorage e depois recuperar deve retornar o mesmo token original.

**Validates: Requirements 1.1, 1.2**

### Property 2: Logout Clears All Sensitive Data

*For any* sessão com dados sensíveis armazenados (token, credenciais, userId), após logout, nenhum dado sensível deve ser recuperável do SecureStorage.

**Validates: Requirements 1.4**

### Property 3: Patrimonio Number Validation

*For any* string de entrada, a validação de número de patrimônio deve retornar `Valid` se e somente se a string contém apenas dígitos numéricos (0-9) e tem comprimento entre 1 e 10.

**Validates: Requirements 6.2**

### Property 4: Text Sanitization Removes Dangerous Characters

*For any* string de entrada contendo caracteres perigosos (`<`, `>`, `"`, `'`), a sanitização deve retornar uma string que não contém nenhum desses caracteres.

**Validates: Requirements 6.3**

### Property 5: Text Truncation Respects Max Length

*For any* string de entrada com comprimento maior que o limite máximo, a saída truncada deve ter exatamente o comprimento máximo especificado.

**Validates: Requirements 6.4**

### Property 6: Session Expiration After Timeout

*For any* sessão ativa, se o tempo desde a última atividade exceder 15 minutos, `isSessionExpired()` deve retornar `true`.

**Validates: Requirements 8.2**

### Property 7: Login Lockout After Max Attempts

*For any* sequência de N tentativas de login falhadas consecutivas onde N >= 5, o sistema deve estar em estado bloqueado (`isLocked() == true`).

**Validates: Requirements 9.2**

### Property 8: Successful Login Resets Attempt Counter

*For any* estado com tentativas falhadas registradas, após um login bem-sucedido, o contador de tentativas deve ser zero.

**Validates: Requirements 9.4**

### Property 9: Lockout State Persists Across Restarts

*For any* estado de bloqueio ativo, após simular reinicialização (recriar LoginAttemptManager), o estado de bloqueio deve permanecer ativo se o tempo de bloqueio não expirou.

**Validates: Requirements 9.5**

### Property 10: Log Sanitization Masks Sensitive Data

*For any* string contendo padrões de dados sensíveis (tokens JWT, senhas, emails), a saída sanitizada deve substituir esses dados por asteriscos ou placeholders.

**Validates: Requirements 3.2, 3.3**

### Property 11: Retry Backoff Exponential and Limited

*For any* sequência de retries, o intervalo entre tentativas deve seguir backoff exponencial (2^n segundos) e o número total de tentativas não deve exceder 3.

**Validates: Requirements 10.4**

## Error Handling

### Estratégia de Tratamento de Erros

1. **Erros de Criptografia**
   - Se EncryptedSharedPreferences falhar na inicialização, usar fallback seguro
   - Logar erro sem expor detalhes sensíveis
   - Notificar usuário sobre problema de segurança

2. **Erros de Migração de Banco**
   - Manter backup do banco original
   - Tentar migração até 3 vezes
   - Se falhar, notificar usuário e manter banco não criptografado temporariamente

3. **Erros de Certificado**
   - Bloquear conexão imediatamente
   - Exibir mensagem clara sobre problema de segurança
   - Não permitir bypass do erro

4. **Erros de Sessão**
   - Salvar estado antes de expirar
   - Redirecionar para login com mensagem apropriada
   - Permitir recuperação de estado após re-login

### Códigos de Erro

```kotlin
enum class SecurityError(val code: Int, val message: String) {
    ENCRYPTION_INIT_FAILED(1001, "Falha ao inicializar criptografia"),
    DATABASE_MIGRATION_FAILED(1002, "Falha ao migrar banco de dados"),
    CERTIFICATE_VALIDATION_FAILED(1003, "Certificado do servidor inválido"),
    SESSION_EXPIRED(1004, "Sessão expirada"),
    ACCOUNT_LOCKED(1005, "Conta bloqueada temporariamente"),
    INVALID_INPUT(1006, "Entrada inválida"),
    NETWORK_TIMEOUT(1007, "Tempo de conexão esgotado")
}
```

## Testing Strategy

### Dual Testing Approach

A estratégia de testes combina testes unitários para casos específicos e testes baseados em propriedades para verificar comportamentos universais.

### Unit Tests

1. **SecureStorage Tests**
   - Teste de inicialização com MasterKey
   - Teste de save/load de diferentes tipos de dados
   - Teste de clearAll

2. **LoginAttemptManager Tests**
   - Teste de bloqueio exato após 5 tentativas
   - Teste de desbloqueio após 15 minutos
   - Teste de reset após sucesso

3. **InputValidator Tests**
   - Teste de validação de patrimônio com casos específicos
   - Teste de sanitização com caracteres conhecidos
   - Teste de truncamento em limites exatos

4. **SessionManager Tests**
   - Teste de expiração exata em 15 minutos
   - Teste de atualização de atividade
   - Teste de aviso 1 minuto antes

### Property-Based Tests (Kotest)

A biblioteca **Kotest** será utilizada para testes baseados em propriedades, conforme já configurado no projeto.

**Configuração:**
- Mínimo de 100 iterações por propriedade
- Cada teste deve referenciar a propriedade do design document

**Formato de Anotação:**
```kotlin
/**
 * **Feature: android-security-hardening, Property 3: Patrimonio Number Validation**
 * **Validates: Requirements 6.2**
 */
@Test
fun `patrimonio number validation accepts only numeric strings`() = ...
```

### Test Files Structure

```
InventarioMobile/app/src/test/java/com/inventario/mobile/security/
├── SecureStorageTest.kt
├── SessionManagerEnhancedTest.kt
├── LoginAttemptManagerTest.kt
├── InputValidatorTest.kt
├── InputValidatorPropertyTest.kt  // Property-based tests
├── LogSanitizerTest.kt
├── LogSanitizerPropertyTest.kt    // Property-based tests
└── SecurityPropertyTests.kt       // All property tests combined
```

## Implementation Notes

### Estado Atual vs. Necessário

| Componente | Estado Atual | Ação Necessária |
|------------|--------------|-----------------|
| SecureStorage | ✅ Implementado | Adicionar migração |
| SessionManager | ✅ Básico | Adicionar timeout de inatividade |
| LoginAttemptManager | ❌ Não existe | Criar do zero |
| InputValidator | ❌ Não existe | Criar do zero |
| LogSanitizer | ❌ Não existe | Criar do zero |
| SQLCipher | ❌ Não configurado | Adicionar dependência e configurar |
| ProGuard | ❌ minifyEnabled=false | Habilitar e configurar regras |
| Certificate Pinning | ⚠️ Parcial | Adicionar pins de produção |
| network_security_config | ⚠️ Permite HTTP | Bloquear HTTP em produção |

### Ordem de Implementação Recomendada

1. **Fase 1 - Baixo Risco** (não quebra funcionalidades)
   - InputValidator
   - LogSanitizer
   - LoginAttemptManager

2. **Fase 2 - Médio Risco** (requer testes cuidadosos)
   - SessionManagerEnhanced (timeout de inatividade)
   - ProGuard/R8 configuration
   - network_security_config (produção)

3. **Fase 3 - Alto Risco** (requer migração de dados)
   - SQLCipher integration
   - Database migration
   - SecureStorage migration

### Dependências a Adicionar

```groovy
// build.gradle (app)
dependencies {
    // SQLCipher para criptografia de banco
    implementation "net.zetetic:android-database-sqlcipher:4.5.4"
    implementation "androidx.sqlite:sqlite-ktx:2.4.0"
}
```

### Configurações de Build

```groovy
// build.gradle (app)
android {
    buildTypes {
        release {
            minifyEnabled true  // Habilitar ofuscação
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    defaultConfig {
        // Mover URLs para BuildConfig
        buildConfigField "String", "API_BASE_URL", "\"${project.findProperty('API_BASE_URL') ?: 'http://localhost:8081'}\""
    }
}
```

