# Implementation Plan - Android Security Hardening

## Fase 1: Componentes de Baixo Risco (Não quebram funcionalidades existentes)

- [x] 1. Implementar InputValidator para validação e sanitização de entradas




  - [ ] 1.1 Criar classe InputValidator em `security/InputValidator.kt`
    - Implementar `validatePatrimonioNumber()` que aceita apenas dígitos numéricos
    - Implementar `sanitizeText()` que remove caracteres perigosos (<, >, ", ')
    - Implementar `truncateToMaxLength()` com limite padrão de 255 caracteres
    - Implementar `validateEmail()` usando Patterns.EMAIL_ADDRESS
    - Criar sealed class `ValidationResult` com `Valid` e `Invalid`


    - _Requirements: 6.1, 6.2, 6.3, 6.4_


  - [ ] 1.2 Escrever property test para validação de número de patrimônio
    - **Property 3: Patrimonio Number Validation**
    - **Validates: Requirements 6.2**


  - [x] 1.3 Escrever property test para sanitização de texto




    - **Property 4: Text Sanitization Removes Dangerous Characters**
    - **Validates: Requirements 6.3**

  - [ ] 1.4 Escrever property test para truncamento de strings
    - **Property 5: Text Truncation Respects Max Length**
    - **Validates: Requirements 6.4**


- [-] 2. Implementar LogSanitizer para proteção de logs



  - [ ] 2.1 Criar classe LogSanitizer em `security/LogSanitizer.kt`
    - Implementar `sanitize()` que ofusca tokens JWT, senhas e emails
    - Implementar `sanitizeMap()` para sanitizar mapas de dados
    - Implementar `isDebugBuild()` para verificar tipo de build
    - Implementar `log()` que só loga em debug builds
    - Usar regex para detectar padrões sensíveis
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ] 2.2 Escrever property test para sanitização de logs
    - **Property 10: Log Sanitization Masks Sensitive Data**
    - **Validates: Requirements 3.2, 3.3**

- [ ] 3. Implementar LoginAttemptManager para proteção contra força bruta
  - [ ] 3.1 Criar classe LoginAttemptManager em `security/LoginAttemptManager.kt`
    - Implementar `recordFailedAttempt()` que incrementa contador
    - Implementar `recordSuccessfulLogin()` que reseta contador
    - Implementar `isLocked()` que verifica se está bloqueado
    - Implementar `getFailedAttemptCount()` para obter número de falhas
    - Implementar `getRemainingLockoutTime()` para tempo restante
    - Persistir estado em SharedPreferences para sobreviver a reinicializações
    - Constantes: MAX_ATTEMPTS=5, LOCKOUT_DURATION=15min
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 3.2 Escrever property test para bloqueio após tentativas
    - **Property 7: Login Lockout After Max Attempts**
    - **Validates: Requirements 9.2**

  - [ ] 3.3 Escrever property test para reset após sucesso
    - **Property 8: Successful Login Resets Attempt Counter**
    - **Validates: Requirements 9.4**

  - [ ] 3.4 Escrever property test para persistência de bloqueio
    - **Property 9: Lockout State Persists Across Restarts**
    - **Validates: Requirements 9.5**

- [ ] 4. Checkpoint - Fase 1
  - Ensure all tests pass, ask the user if questions arise.

## Fase 2: Componentes de Médio Risco (Requerem testes cuidadosos)

- [ ] 5. Aprimorar SessionManager com timeout de inatividade
  - [ ] 5.1 Criar SessionManagerEnhanced em `security/SessionManagerEnhanced.kt`
    - Implementar `startSession()` que inicia monitoramento
    - Implementar `updateActivity()` que atualiza timestamp de última atividade
    - Implementar `isSessionExpired()` que verifica se passou 15 minutos
    - Implementar `getTimeUntilExpiry()` para tempo restante
    - Implementar `isExpiringSoon()` para aviso de 1 minuto
    - Implementar `saveStateBeforeLogout()` para salvar estado
    - Implementar `restoreStateAfterLogin()` para restaurar estado
    - Integrar com SessionManager existente
    - _Requirements: 8.1, 8.2, 8.4_

  - [ ] 5.2 Escrever property test para expiração de sessão
    - **Property 6: Session Expiration After Timeout**
    - **Validates: Requirements 8.2**

  - [ ] 5.3 Integrar SessionManagerEnhanced nas Activities principais
    - Adicionar chamada a `updateActivity()` em `onUserInteraction()`
    - Adicionar verificação de expiração em `onResume()`
    - Implementar aviso de expiração iminente
    - _Requirements: 8.3, 8.5_

- [ ] 6. Integrar LoginAttemptManager na tela de login
  - [ ] 6.1 Modificar LoginActivity para usar LoginAttemptManager
    - Verificar `isLocked()` antes de permitir tentativa
    - Chamar `recordFailedAttempt()` em caso de falha
    - Chamar `recordSuccessfulLogin()` em caso de sucesso
    - Exibir tempo restante quando bloqueado
    - _Requirements: 9.2, 9.3, 9.4_

- [ ] 7. Configurar ProGuard/R8 para builds de release
  - [ ] 7.1 Criar arquivo proguard-rules.pro com regras necessárias
    - Manter classes do Retrofit, Room, Hilt
    - Remover logs de debug em release
    - Ofuscar classes de segurança
    - Manter classes de modelo para serialização
    - _Requirements: 5.1, 5.2, 5.3, 5.5_

  - [ ] 7.2 Habilitar minifyEnabled e shrinkResources no build.gradle
    - Configurar `minifyEnabled true` para release
    - Configurar `shrinkResources true` para release
    - Testar build de release
    - _Requirements: 5.1_

- [ ] 8. Configurar network_security_config para produção
  - [ ] 8.1 Atualizar network_security_config.xml para bloquear HTTP
    - Configurar `cleartextTrafficPermitted="false"` na base-config
    - Manter exceções apenas para IPs de desenvolvimento
    - Adicionar comentários sobre configuração de produção
    - _Requirements: 4.2_

  - [ ] 8.2 Preparar configuração de Certificate Pinning
    - Documentar processo para obter hash do certificado
    - Criar template de configuração com pins
    - Adicionar certificado de backup
    - _Requirements: 4.1, 4.3, 4.4_

- [ ] 9. Configurar timeouts de rede adequados
  - [ ] 9.1 Atualizar NetworkModule com timeouts de 30 segundos
    - Configurar connectTimeout de 30 segundos
    - Configurar readTimeout de 30 segundos
    - Configurar writeTimeout de 30 segundos
    - _Requirements: 10.1, 10.2, 10.3_

  - [ ] 9.2 Implementar retry com backoff exponencial
    - Criar RetryInterceptor com backoff exponencial
    - Limitar a 3 tentativas máximas
    - Implementar cancelamento em timeout
    - _Requirements: 10.4, 10.5_

  - [ ] 9.3 Escrever property test para retry com backoff
    - **Property 11: Retry Backoff Exponential and Limited**
    - **Validates: Requirements 10.4**

- [ ] 10. Checkpoint - Fase 2
  - Ensure all tests pass, ask the user if questions arise.

## Fase 3: Componentes de Alto Risco (Requerem migração de dados)

- [ ] 11. Aprimorar SecureStorage com migração de dados
  - [ ] 11.1 Adicionar método de migração ao SecureStorage
    - Implementar `migrateFromPlainPreferences()` para migrar dados não criptografados
    - Verificar se migração já foi feita antes de executar
    - Limpar dados antigos após migração bem-sucedida
    - _Requirements: 1.5_

  - [ ] 11.2 Escrever property test para round-trip de tokens
    - **Property 1: Token Round-Trip Consistency**
    - **Validates: Requirements 1.1, 1.2**

  - [ ] 11.3 Escrever property test para limpeza no logout
    - **Property 2: Logout Clears All Sensitive Data**
    - **Validates: Requirements 1.4**

- [ ] 12. Configurar SQLCipher para criptografia do banco
  - [ ] 12.1 Adicionar dependência SQLCipher ao build.gradle
    - Adicionar `net.zetetic:android-database-sqlcipher:4.5.4`
    - Adicionar `androidx.sqlite:sqlite-ktx:2.4.0`
    - Sincronizar projeto
    - _Requirements: 2.1_

  - [ ] 12.2 Criar DatabaseEncryptionManager
    - Implementar `getOrCreateDatabaseKey()` que gera chave única
    - Armazenar chave no SecureStorage
    - Implementar `createEncryptedDatabase()` com SQLCipher
    - _Requirements: 2.1, 2.2_

  - [ ] 12.3 Implementar migração de banco não criptografado
    - Criar backup do banco original antes de migrar
    - Implementar migração com tratamento de erros
    - Notificar usuário em caso de falha
    - Manter banco original se migração falhar
    - _Requirements: 2.3, 2.4_

  - [ ] 12.4 Atualizar DatabaseModule para usar banco criptografado
    - Modificar provider do AppDatabase para usar SQLCipher
    - Adicionar verificação de integridade na inicialização
    - _Requirements: 2.5_

- [ ] 13. Configurar AndroidManifest para segurança
  - [ ] 13.1 Desabilitar backup de dados
    - Configurar `android:allowBackup="false"`
    - Remover ou atualizar `data_extraction_rules`
    - _Requirements: 3.4_

- [ ] 14. Mover configurações sensíveis para BuildConfig
  - [ ] 14.1 Configurar BuildConfig fields no build.gradle
    - Mover URL base da API para BuildConfig
    - Usar variáveis de ambiente ou local.properties
    - Documentar variáveis necessárias
    - _Requirements: 7.1, 7.2, 7.4_

  - [ ] 14.2 Atualizar código para usar BuildConfig
    - Substituir URLs hardcoded por BuildConfig.API_BASE_URL
    - Verificar que local.properties está no .gitignore
    - _Requirements: 7.1, 7.3_

- [ ] 15. Checkpoint Final - Fase 3
  - Ensure all tests pass, ask the user if questions arise.

## Fase 4: Validação e Documentação

- [ ] 16. Validação final de segurança
  - [ ] 16.1 Executar todos os testes de propriedade
    - Verificar que todas as 11 propriedades passam
    - Documentar resultados dos testes
    - _Requirements: Todos_

  - [ ] 16.2 Verificar configurações de segurança
    - Confirmar que minifyEnabled está ativo em release
    - Confirmar que allowBackup está false
    - Confirmar que cleartextTrafficPermitted está false (produção)
    - Verificar que logs de debug são removidos em release
    - _Requirements: 3.4, 3.5, 4.2, 5.1_

  - [ ] 16.3 Gerar build de release e testar
    - Compilar APK de release
    - Testar funcionalidades principais
    - Verificar que ofuscação foi aplicada
    - _Requirements: 5.4_

- [ ] 17. Atualizar documentação de segurança
  - [ ] 17.1 Atualizar SECURITY_ACTION_PLAN.md com status
    - Marcar tarefas concluídas
    - Documentar decisões de implementação
    - Listar próximos passos pendentes

