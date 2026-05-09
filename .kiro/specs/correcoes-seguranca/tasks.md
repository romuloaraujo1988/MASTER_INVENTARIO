# Plano de Implementação: Correções de Segurança

## Visão Geral

Esta feature aplica 12 correções de segurança ao SIHCP, divididas em **três ondas de deploy** para minimizar risco e permitir revisão humana entre etapas:

- **Onda 1 — Segredos e Git**: externaliza credenciais, endurece `.gitignore`, remove `keystore.properties` do índice, rotaciona segredos e instala o `SecretsBootstrap` que falha rapidamente na ausência de variáveis. Bloqueia acesso novo **sem** invalidar tokens ativos.
- **Onda 2 — Endurecimento do Servidor**: perfil `prod` com logs sanitizados, rate limiting no login, validação de comprimento do `JWT_SECRET`, CORS explícito e contrato de erro `token_expired`. Sem quebra de contrato com apps em produção.
- **Onda 3 — Endurecimento do App**: novo APK com `SqlCipherKeyManager`, `network_security_config.xml` sem cleartext global, ProGuard/R8 ativo, permissões mínimas e backup desabilitado. **Requer sincronização forçada prévia** (banco local será recriado).

Cada onda termina com um checkpoint obrigatório de revisão humana antes de prosseguir para a próxima.

Testes baseados em propriedade (Propriedades 1–9 do design) são opcionais (`*`) e servem como rede de segurança — cada onda pode ir para produção sem eles, mas é fortemente recomendado executá-los antes do checkpoint.

Tarefas prefixadas com `[OPERACIONAL]` exigem ação humana (rotação de senhas, execução de comandos Git, configuração de variáveis de ambiente) e não devem ser executadas por um agente de código.

---

## Tasks

### Onda 1 — Segredos e Git

- [x] 1. Endurecer `.gitignore` com padrões sensíveis
  - Abrir `.gitignore` na raiz do repositório
  - Adicionar entradas: `InventarioMobile/keystore.properties`, `.env`, `.env.*`, `application-*-local.properties`, `application-local.properties`, `src/main/resources/application-mobile-local.properties`, `secrets/`, `*.pem`, `*.p12`
  - Confirmar que `keystore.properties`, `*.keystore`, `*.jks`, `*.keystore.backup` já estão presentes (não duplicar)
  - _Requisitos: 1.5, 2.1, 2.4_

- [x] 2. Externalizar segredos do servidor via placeholders
  - [x] 2.1 Substituir literais por placeholders em `application-mobile.properties`
    - Trocar `spring.datasource.password=Romulo@1919` por `spring.datasource.password=${DATASOURCE_PASSWORD:}`
    - Trocar `jwt.secret=inventario-mobile-secret-key-2024-...` por `jwt.secret=${JWT_SECRET:}`
    - Adicionar comentário apontando para `application-mobile-local.properties` como destino dos valores reais de dev
    - _Requisitos: 1.1, 1.2_

  - [x] 2.2 Criar arquivo-modelo `application-mobile-local.properties.example`
    - Arquivo em `sihcp-server/src/main/resources/application-mobile-local.properties.example` (versionado)
    - Conteúdo com instruções em comentário e placeholders vazios para `spring.datasource.password` e `jwt.secret`
    - Documentar no README que o desenvolvedor deve copiar para `application-mobile-local.properties` (não versionado) e preencher
    - _Requisitos: 1.1, 1.2, 1.5_

  - [x] 2.3 Ajustar lista explícita de CORS em `application-mobile.properties`
    - Substituir `api.mobile.cors.allowed-origins=*` pela lista explícita de IPs do IFMT e localhost (conforme design seção R6)
    - Definir `api.mobile.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS`
    - Definir `api.mobile.cors.allowed-headers=Authorization,Content-Type,Accept,X-Requested-With`
    - Trocar `api.mobile.cors.allow-credentials=true` por `api.mobile.cors.allow-credentials=false`
    - _Requisitos: 6.1, 6.2, 6.4_

- [x] 3. Criar `SecretsBootstrap` com validação fail-fast
  - [x] 3.1 Implementar a classe `SecretsBootstrap`
    - Criar `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/config/SecretsBootstrap.java`
    - Anotar com `@Configuration`; injetar `ApplicationContext`
    - Injetar `@Value("${spring.datasource.password:}")` e `@Value("${jwt.secret:}")`
    - Implementar `@PostConstruct validate()`: coletar erros para `DATASOURCE_PASSWORD` vazio, `JWT_SECRET` vazio e `JWT_SECRET.length() < 32`
    - Em caso de erro: logar com banner visível (`log.error`), chamar `SpringApplication.exit(ctx, () -> 1)` e `System.exit(1)`
    - Em caso de sucesso: logar `"SecretsBootstrap OK — DATASOURCE_PASSWORD e JWT_SECRET carregados do ambiente"`
    - _Requisitos: 1.3, 1.4, 1.6_

  - [ ]* 3.2 Teste de propriedade para `SecretsBootstrap`
    - **Propriedade 2: `SecretsBootstrap` valida a presença e o comprimento dos segredos**
    - **Valida: Requisitos 1.3, 1.4, 1.6**
    - Arquivo `sihcp-server/src/test/java/.../SecretsBootstrapPropertyTest.java` usando jqwik
    - Gerar pares `(password, secret)` com `Arbitraries.strings().ofMaxLength(200)`
    - Verificar que aborta se e somente se `password.isBlank() || secret.isBlank() || secret.length() < 32`
    - Mínimo 100 iterações

  - [ ]* 3.3 Teste de propriedade para ausência de segredos literais
    - **Propriedade 1: Ausência de segredos literais no arquivo versionado**
    - **Valida: Requisitos 1.1, 1.2**
    - Arquivo `sihcp-server/src/test/java/.../ApplicationPropertiesPropertyTest.java`
    - Carregar `application-mobile.properties` via `java.util.Properties`
    - Para cada chave em `{spring.datasource.password, jwt.secret}`, asserta que o valor é vazio OU começa com `${` e termina com `}`

  - [ ]* 3.4 Teste de propriedade para cobertura do `.gitignore`
    - **Propriedade 3: `.gitignore` cobre todos os padrões sensíveis**
    - **Valida: Requisitos 1.5, 2.1, 2.4**
    - Arquivo `sihcp-server/src/test/java/.../GitignorePropertyTest.java`
    - Usar `org.eclipse.jgit.ignore.FastIgnoreRule` para parsear o `.gitignore` da raiz
    - Gerar caminhos sintéticos dentro dos padrões sensíveis (`.env`, `*-local.properties`, `keystore.properties`, `*.jks`, `*.keystore`)
    - Assertar que todos casam com alguma regra do `.gitignore`

- [x] 4. [OPERACIONAL] Remover `keystore.properties` do índice Git
  - Executar no terminal da raiz do projeto:
    ```
    git rm --cached InventarioMobile/keystore.properties
    git commit -m "security: remove keystore.properties do indice"
    ```
  - O arquivo permanece em disco para builds locais
  - Verificar com `git ls-files | grep keystore.properties` — deve retornar vazio
  - _Requisitos: 2.1_

- [x] 5. [OPERACIONAL] Rotacionar senha do PostgreSQL em produção
  - A senha atual `Romulo@1919` está exposta no histórico Git e DEVE ser considerada comprometida
  - No servidor PostgreSQL de produção: `ALTER USER inventario WITH PASSWORD '<nova_senha_forte>';`
  - Gerar senha com pelo menos 20 caracteres aleatórios (letras, números, símbolos)
  - Registrar a nova senha no cofre de senhas institucional (nunca em texto plano em arquivos)
  - NÃO reiniciar o servidor ainda — a aplicação em produção ainda usa a senha antiga até a tarefa 7
  - _Requisitos: 1.1_

- [x] 6. [OPERACIONAL] Gerar novo `JWT_SECRET` para produção
  - Gerar segredo aleatório de 48+ bytes base64: `openssl rand -base64 48` (Linux/macOS) ou `[Convert]::ToBase64String((1..48 | %{Get-Random -Max 256}))` (PowerShell)
  - Confirmar que o resultado tem pelo menos 32 caracteres
  - Registrar no cofre institucional
  - AVISO: a troca invalidará todos os JWT ativos — todos os usuários mobile serão deslogados no próximo request e precisarão refazer login
  - Comunicar janela de manutenção antes de aplicar
  - _Requisitos: 1.2, 1.6_

- [x] 7. [OPERACIONAL] Definir variáveis de ambiente no servidor de produção e reiniciar
  - No host do servidor (ou no `systemd unit`, `docker-compose.yml`, etc.), definir:
    - `DATASOURCE_PASSWORD=<senha_nova_postgres>` (da tarefa 5)
    - `JWT_SECRET=<segredo_novo>` (da tarefa 6)
    - `SPRING_PROFILES_ACTIVE=mobile` (manter como está até Onda 2)
  - Validar que `.env` ou o arquivo de units NÃO está versionado
  - Reiniciar o serviço; confirmar no log a mensagem `"SecretsBootstrap OK — ..."`
  - Se o log mostrar falha de bootstrap, revisar as variáveis antes de abrir tráfego
  - _Requisitos: 1.1, 1.2, 1.3, 1.4_

- [x] 8. Checkpoint Onda 1 — Revisão humana e testes
  - Confirmar que `grep -R "Romulo@1919" .` na working copy retorna vazio (histórico Git ainda conterá — documentar)
  - Confirmar que `grep -R "inventario-mobile-secret-key-2024"` retorna vazio
  - Executar `mvn -pl sihcp-server test` — todos os testes devem passar
  - Verificar no log de produção que o servidor iniciou com `SecretsBootstrap OK`
  - Validar manualmente um login mobile end-to-end (os usuários terão que re-logar porque o `JWT_SECRET` mudou)
  - Garantir que todos os testes passam, perguntar ao usuário se houver dúvidas.

---

### Onda 2 — Endurecimento do Servidor

- [x] 9. Criar perfil `application-prod.properties`
  - Arquivo `sihcp-server/src/main/resources/application-prod.properties` (versionado)
  - Conteúdo conforme design seção R8: `logging.level.root=WARN`, `logging.level.com.inventario=INFO`, `logging.level.com.inventario.sihcp.mobile.server.controller=INFO`, `logging.level.com.inventario.sihcp.mobile.server.service=INFO`, `logging.level.com.inventario.sihcp.security=INFO`, `logging.level.org.springframework.security=WARN`
  - Adicionar `api.mobile.rate-limit.enabled=true`
  - Adicionar `jwt.expiration=${JWT_EXPIRATION:3600}` e `jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800}`
  - Adicionar `api.mobile.cors.allowed-origins=${API_MOBILE_CORS_ALLOWED_ORIGINS:http://localhost:8081}` e `api.mobile.cors.allow-credentials=false`
  - _Requisitos: 7.1, 8.1, 8.2, 8.3, 8.5, 9.1, 9.2_

- [x] 10. Adicionar dependência Bucket4j ao `sihcp-server/pom.xml`
  - Inserir no bloco `<dependencies>`:
    ```xml
    <dependency>
        <groupId>com.bucket4j</groupId>
        <artifactId>bucket4j-core</artifactId>
        <version>8.10.1</version>
    </dependency>
    ```
  - Rodar `mvn dependency:resolve` para baixar
  - _Requisitos: 7.2_

- [x] 11. Implementar `LoginRateLimiter`
  - [x] 11.1 Criar a classe `LoginRateLimiter`
    - Arquivo `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/security/LoginRateLimiter.java`
    - Implementar `HandlerInterceptor`; anotar com `@Component`
    - `CAPACITY=10`, `REFILL_WINDOW=Duration.ofMinutes(1)`, `EXCESS_ALERT_THRESHOLD=3`, `EXCESS_ALERT_WINDOW=Duration.ofMinutes(5)`
    - Ler `@Value("${api.mobile.rate-limit.enabled:false}")`
    - Em `preHandle`: se desabilitado ou path não é `POST /api/mobile/auth/login`, retornar `true`
    - Caso contrário, resolver IP (respeitando `X-Forwarded-For`), consumir token do bucket; em falha responder 429 com cabeçalho `Retry-After` e corpo JSON `{"error":"rate_limited","retryAfterSeconds":N}`
    - Em falhas repetidas (`>= EXCESS_ALERT_THRESHOLD` em 5 min para o mesmo IP), emitir `log.warn("RATE_LIMIT_ALERT ip={} excess_events_last_5min={}", ...)`
    - _Requisitos: 7.2, 7.3, 7.4, 7.5_

  - [x] 11.2 Criar `RateLimitConfig`
    - Arquivo `sihcp-server/src/main/java/com/inventario/sihcp/mobile/server/config/RateLimitConfig.java`
    - Anotar com `@Configuration`, implementar `WebMvcConfigurer`
    - Em `addInterceptors`, registrar o `LoginRateLimiter` no path pattern `/api/mobile/auth/login`
    - _Requisitos: 7.5_

  - [ ]* 11.3 Teste de propriedade para `LoginRateLimiter`
    - **Propriedade 7: Rate limit aplica exclusivamente ao login, com janela de 10/min**
    - **Valida: Requisitos 7.2, 7.3, 7.5**
    - Arquivo `sihcp-server/src/test/java/.../LoginRateLimiterPropertyTest.java`
    - Usar `Bucket4j` com `TimeMeter` simulado; gerar sequência `Arb.list(Arb.long(0..60000))`
    - (a) Para `POST /api/mobile/auth/login`: as 10 primeiras passam, da 11ª em diante retorna 429 com `Retry-After` inteiro positivo
    - (b) Para qualquer outro path `/api/mobile/*`, nenhuma retorna 429

- [x] 12. Sanitizar logs de tokens
  - [x] 12.1 Remover logs de substrings de token em `MobileAuthController`
    - Localizar `logger.info("Access Token: {}", ...)` e `logger.info("Refresh Token: ...")` no método `login`
    - Substituir por `logger.info("Login bem-sucedido: usuario={} expiresIn={}s", loginResponse.getUser().getUsername(), loginResponse.getExpiresIn())`
    - Aplicar a mesma sanitização em `refreshToken()` do controller
    - _Requisitos: 8.4_

  - [x] 12.2 Rebaixar logs do `MobileJwtAuthenticationFilter` para DEBUG
    - Localizar `logger.info("Token extraído: ... length=...")` e afins
    - Trocar por `log.debug(...)` condicionados a `log.isDebugEnabled()`; nunca logar o valor do token
    - Remover logs verbosos `"=== MOBILE JWT FILTER ..."` (substituir por `log.debug`)
    - _Requisitos: 8.4_

  - [ ]* 12.3 Teste de propriedade para sanitização de logs
    - **Propriedade 9: Logs não vazam tokens JWT**
    - **Valida: Requisito 8.4**
    - Arquivo `sihcp-server/src/test/java/.../JwtLogSanitizationPropertyTest.java`
    - Usar `LogCaptor` para capturar logs em perfil `prod`
    - Gerar tokens `Arb.string()` enviados ao filtro e controller
    - Asserta: nenhum log em nível `INFO+` contém o token inteiro nem um prefixo contíguo de 8+ caracteres do token

- [x] 13. Validar comprimento mínimo do `JWT_SECRET` em `JwtUtil`
  - Localizar `sihcp-server/src/main/java/com/inventario/sihcp/security/JwtUtil.java`
  - Em `@PostConstruct` (criar se não existir) ou no ponto onde o secret é carregado, lançar `IllegalStateException` se `secret.length() < 32`
  - Mensagem: `"JWT_SECRET muito curto: X chars (mínimo: 32)"`
  - Esta é redundância defensiva — `SecretsBootstrap` já verifica, mas `JwtUtil` pode ser usado em contextos de teste isolados
  - _Requisitos: 1.6_

- [x] 14. Remover `@CrossOrigin(origins = "*")` de `MobileAuthController`
  - Localizar a anotação no topo da classe
  - Remover completamente (CORS é gerenciado globalmente em `MobileSecurityConfig.corsConfigurationSource()`)
  - Adicionar comentário: `// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource()`
  - _Requisitos: 6.1_

- [x] 15. Garantir resposta `token_expired` no `JwtAuthenticationEntryPoint`
  - Localizar `sihcp-server/src/main/java/com/inventario/sihcp/security/JwtAuthenticationEntryPoint.java`
  - Em `commence()`, verificar se a exceção é `ExpiredJwtException` (ou equivalente)
  - Escrever corpo JSON: `{"error":"token_expired"}` para expirado, `{"error":"invalid_token"}` para demais `JwtException`
  - Status 401, `Content-Type: application/json`
  - _Requisitos: 9.3_

- [ ]* 16. Testes de integração do servidor (Onda 2)
  - [ ]* 16.1 `LoginRateLimitIntegrationTest`
    - `@SpringBootTest` com perfil `mobile,prod`
    - Bombardear `/api/mobile/auth/login` 15× em < 1 min via `MockMvc`
    - Asserta: requests 1–10 retornam 401 (credenciais inválidas), 11–15 retornam 429 com `Retry-After`
    - _Requisitos: 7.2, 7.3_

  - [ ]* 16.2 `CorsIntegrationTest`
    - Enviar preflight `OPTIONS` de origem não listada
    - Asserta: ausência do header `Access-Control-Allow-Origin`
    - _Requisitos: 6.3_

  - [ ]* 16.3 `TokenExpiryIntegrationTest`
    - Gerar token com `exp` no passado via `JwtUtil`
    - Chamar endpoint autenticado; asserta 401 + body `{"error":"token_expired"}`
    - _Requisitos: 9.3_

  - [ ]* 16.4 `StartupFailureIntegrationTest`
    - `@SpringBootTest` sem `JWT_SECRET` definido
    - Asserta: `ApplicationContextException` ou falha explícita no boot
    - _Requisitos: 1.4_

- [x] 17. [OPERACIONAL] Ativar perfil `prod` em produção
  - No servidor, atualizar `SPRING_PROFILES_ACTIVE=mobile,prod`
  - Opcionalmente sobrescrever `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION` e `API_MOBILE_CORS_ALLOWED_ORIGINS` no ambiente
  - Reiniciar o serviço; confirmar no log:
    - `SecretsBootstrap OK`
    - Nível de log `com.inventario` em `INFO`
    - `api.mobile.rate-limit.enabled=true`
  - _Requisitos: 7.1, 8.1, 8.2, 8.3, 8.5, 9.1, 9.2_

- [x] 18. Checkpoint Onda 2 — Revisão humana e testes
  - Enviar requisição válida ao login e confirmar que o `logger.info` em produção loga `usuario=...` mas não contém nenhum fragmento do token
  - Executar `ab -n 15 -c 1 -p login.json -T application/json http://localhost:8080/api/mobile/auth/login` ou equivalente; verificar que 11ª+ retorna 429
  - Confirmar que o app Android em produção continua funcionando (mesmo com `jwt.expiration=3600`, o refresh token cobre)
  - Garantir que todos os testes passam, perguntar ao usuário se houver dúvidas.

---

### Onda 3 — Endurecimento do App Android

- [x] 19. [OPERACIONAL] Coordenar sync forçado em todos os dispositivos em campo
  - ANTES de publicar o novo APK, enviar comunicado aos usuários
  - Forçar que cada dispositivo faça sync completa (upload de coletas pendentes) — porque a Onda 3 recria o banco SQLCipher local e coletas offline não sincronizadas serão perdidas
  - Opcional: exibir banner/notificação no app atual pedindo sync manual
  - Aguardar relatório de sync da janela combinada antes de seguir para a tarefa 20
  - _Requisitos: 3.1, 3.2_

- [x] 20. Implementar `SqlCipherKeyManager`
  - [x] 20.1 Criar a classe `SqlCipherKeyManager`
    - Arquivo `InventarioMobile/app/src/main/java/com/inventario/mobile/security/SqlCipherKeyManager.kt`
    - Singleton thread-safe (`@Volatile` + double-checked locking)
    - Usar `MasterKey.Builder(context).setKeyScheme(AES256_GCM).build()` e `EncryptedSharedPreferences.create(...)` com `AES256_SIV` / `AES256_GCM`
    - `getOrCreatePassphrase(): ByteArray`: retorna Base64-decoded existente ou gera 32 bytes via `SecureRandom`, persiste em Base64 e retorna
    - Definir `SqlCipherKeyException` interna (propagar falhas do Keystore)
    - _Requisitos: 3.1, 3.2, 3.3, 3.4_

  - [x] 20.2 Substituir passphrase hardcoded em `AppDatabase`
    - Arquivo `InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/database/AppDatabase.kt`
    - Remover `val passphrase = "inventario_secure_key".toCharArray()`
    - Trocar por `val passphraseBytes = SqlCipherKeyManager.getInstance(context).getOrCreatePassphrase()` e passar direto a `SupportFactory(passphraseBytes)`
    - Adicionar bloco de migração one-shot: usar `SharedPreferences("sqlcipher_migration")` com flag `migrated_to_keystore_v1`; na primeira execução deletar `inventario_offline_secure.db` e marcar flag
    - Garantir que a UI capture `SqlCipherKeyException` e exiba mensagem bloqueante
    - _Requisitos: 3.1, 3.2, 3.3, 3.4_

  - [ ]* 20.3 Teste de propriedade para `SqlCipherKeyManager`
    - **Propriedade 4: Passphrase do SQLCipher é gerada, persistida e única por dispositivo**
    - **Valida: Requisitos 3.1, 3.2, 3.3**
    - Arquivo `InventarioMobile/app/src/test/java/.../SqlCipherKeyManagerPropertyTest.kt` (Kotest Property)
    - Mockar `EncryptedSharedPreferences` com `mockk`
    - `checkAll(100)`: (a) 32 bytes; (b) duas instâncias com store vazio geram passphrases distintas; (c) round-trip na mesma instância retorna igual; (d) grep recursivo em `src/main` confirma ausência de `"inventario_secure_key"`

- [x] 21. Atualizar `network_security_config.xml`
  - Arquivo `InventarioMobile/app/src/main/res/xml/network_security_config.xml`
  - Substituir pelo XML do design seção R5/R10:
    - `<base-config cleartextTrafficPermitted="false">` com apenas `<certificates src="system" />`
    - `<domain-config cleartextTrafficPermitted="true">` com allowlist explícita de `localhost`, `127.0.0.1`, `10.0.2.2`, IPs IFMT (`10.14.250.228`, `10.14.250.236`, `10.14.250.238`, `192.168.10.107`, `192.168.11.136`) e `<certificates src="user" />` APENAS nesse bloco
  - Remover IPs residenciais (`192.168.1.x`, `192.168.0.x`) que não pertencem ao IFMT
  - _Requisitos: 5.1, 5.2, 5.3, 5.4, 10.1, 10.2, 10.3_

- [x] 22. Atualizar `AndroidManifest.xml`
  - Arquivo `InventarioMobile/app/src/main/AndroidManifest.xml`
  - No elemento `<application>`:
    - Remover `android:usesCleartextTraffic="true"` completamente
    - Trocar `android:allowBackup="true"` por `android:allowBackup="false"`
    - Manter `android:networkSecurityConfig="@xml/network_security_config"`
  - Remover `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`
  - Remover `<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />` APÓS confirmar via grep que nenhum `NotificationCompat.Builder.setFullScreenIntent(...)` existe em `InventarioMobile/app/src/main/java`
  - _Requisitos: 5.5, 11.1, 12.1, 12.3_

- [ ] 23. Configurar ProGuard/R8 para builds release
  - [x] 23.1 Criar `proguard-rules.pro` com regras completas
    - Arquivo `InventarioMobile/app/proguard-rules.pro`
    - Conteúdo completo conforme design seção R4: regras para Kotlin, Coroutines, Hilt/Dagger, Room, Retrofit/OkHttp, Gson, SQLCipher, Security Crypto, DTOs/Entities/Domain Models, WorkManager, Paging 3, Parcelize, ZXing, iText 7, Glide
    - Incluir bloco `-assumenosideeffects` para `Log.d` e `Log.v`
    - _Requisitos: 4.3_

  - [x] 23.2 Ativar `minifyEnabled` e `shrinkResources` no `build.gradle`
    - Arquivo `InventarioMobile/app/build.gradle`, bloco `buildTypes.release`
    - Trocar `minifyEnabled false` por `minifyEnabled true`
    - Adicionar `shrinkResources true`
    - Manter `proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'`
    - Executar `./gradlew assembleRelease` e corrigir quaisquer `-keep` faltantes
    - _Requisitos: 4.1, 4.2, 4.4_

  - [ ]* 23.3 Teste de propriedade para regras ProGuard
    - **Propriedade 6: ProGuard preserva bibliotecas reflexivas**
    - **Valida: Requisito 4.3**
    - Arquivo `InventarioMobile/app/src/test/java/.../ProguardRulesPropertyTest.kt`
    - Ler `proguard-rules.pro` como texto
    - Para cada lib em `{androidx.room, retrofit2, dagger.hilt, net.sqlcipher, com.google.gson, androidx.work, androidx.security.crypto, com.inventario.mobile.data.remote.dto, com.inventario.mobile.data.local.entity, com.inventario.mobile.domain.model}`, assertar pelo menos uma regra `-keep` (ou `-keepclassmembers`) que cubra o pacote

- [x] 24. Preencher regras de exclusão de backup
  - [x] 24.1 Atualizar `data_extraction_rules.xml`
    - Arquivo `InventarioMobile/app/src/main/res/xml/data_extraction_rules.xml`
    - Bloco `<cloud-backup>` e `<device-transfer>` com `<exclude domain="database" path="."/>`, `<exclude domain="sharedpref" path="secure_prefs.xml"/>`, `<exclude domain="sharedpref" path="sqlcipher_key_prefs.xml"/>`, `<exclude domain="file" path="datastore/"/>` (apenas em cloud-backup)
    - _Requisitos: 11.2, 11.3_

  - [x] 24.2 Atualizar `backup_rules.xml` (legado Android < 12)
    - Arquivo `InventarioMobile/app/src/main/res/xml/backup_rules.xml`
    - `<full-backup-content>` com `<exclude domain="database" path="."/>`, `<exclude domain="sharedpref" path="secure_prefs.xml"/>`, `<exclude domain="sharedpref" path="sqlcipher_key_prefs.xml"/>`
    - _Requisitos: 11.2_

- [x] 25. Incrementar `versionCode` e `versionName`
  - Arquivo `InventarioMobile/app/build.gradle`
  - Incrementar `versionCode` em 1
  - Atualizar `versionName` refletindo a release de segurança (ex.: `"2.X.0-security"` conforme padrão do projeto)
  - Registrar a build em `BUILD_HISTORY.md` com tipo `Release`, mudanças: "Correções de segurança — Onda 3 (SQLCipher keystore, network config, ProGuard, manifesto)"
  - _Requisitos: 4.1, 4.2, 4.4_

- [ ]* 26. Testes de propriedade para configuração do app
  - [ ]* 26.1 `NetworkSecurityConfigPropertyTest`
    - **Propriedade 5: Configuração de rede não permite cleartext nem user CAs em produção**
    - **Valida: Requisitos 5.1, 5.3, 5.5, 10.1, 10.2, 10.3**
    - Parsear `network_security_config.xml` e `AndroidManifest.xml` via `DocumentBuilder`
    - Assertar (a) base-config sem cleartext e sem `src="user"`; (b) `<application>` sem `android:usesCleartextTraffic`; (c) para cada `<domain-config cleartextTrafficPermitted="true">`, cada `<domain>` pertence à allowlist IFMT
    - Gerar IPs com `Arb.string()` e confirmar que apenas os conhecidos casam

  - [ ]* 26.2 `AndroidManifestPropertyTest`
    - **Propriedade 8: Manifesto do app mantém permissões mínimas e desabilita backup**
    - **Valida: Requisitos 11.1, 12.1, 12.3**
    - Parsear `AndroidManifest.xml`
    - Assertar (a) `<application android:allowBackup="false">`; (b) ausência de `SYSTEM_ALERT_WINDOW`; (c) ausência de `USE_FULL_SCREEN_INTENT` quando o grep em `src/main/java` não encontra `setFullScreenIntent`

- [ ]* 27. Teste de integração end-to-end do app
  - Arquivo `InventarioMobile/app/src/androidTest/.../SqlCipherE2ETest.kt`
  - Criar `AppDatabase`, inserir dados, fechar, reabrir com o mesmo `SqlCipherKeyManager`, ler — round-trip de ponta-a-ponta no emulador
  - Executar `./gradlew assembleRelease` em pipeline CI — valida R4.4 (APK assinado compila com minifyEnabled)
  - _Requisitos: 3.3, 4.4_

- [x] 28. Checkpoint Final — Revisão humana e distribuição
  - Executar smoke script: grep em toda a working copy por `"Romulo@1919"`, `"inventario-mobile-secret-key-2024"`, `"inventario_secure_key"` — todos vazios
  - Confirmar `git ls-files | grep -E "keystore\.properties$"` vazio
  - Instalar APK release em dispositivo de teste; fazer login, coleta, sync — confirmar funcionamento normal após banco local ser recriado
  - Monitorar logs do servidor em produção por 24h: nenhum token em log `INFO+`, rate limit ativo (verificar ocorrências de `RATE_LIMIT_ALERT`)
  - Distribuir APK via canal interno aos usuários (registrar em `BUILD_HISTORY.md`)
  - Garantir que todos os testes passam, perguntar ao usuário se houver dúvidas.

---

## Notas

- Tarefas marcadas com `*` são opcionais (testes) e podem ser puladas para um MVP mais rápido; são fortemente recomendadas antes de cada checkpoint.
- Tarefas prefixadas com `[OPERACIONAL]` exigem ação humana (rotação de senhas, `git rm --cached`, configuração de variáveis, coordenação com usuários). Um agente de código não deve executá-las autonomamente.
- Cada checkpoint é um ponto obrigatório de revisão humana antes da próxima onda.
- Testes de propriedade validam as Propriedades 1–9 do design; cada um é anotado com seu número e requisito correspondente.
- A Onda 3 é destrutiva para bancos locais dos dispositivos — a tarefa 19 (sync forçado) é pré-requisito indispensável.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "2.1", "2.2", "2.3"] },
    { "id": 1, "tasks": ["3.1"] },
    { "id": 2, "tasks": ["3.2", "3.3", "3.4", "4", "5", "6"] },
    { "id": 3, "tasks": ["7"] },
    { "id": 4, "tasks": ["9", "10"] },
    { "id": 5, "tasks": ["11.1", "13", "14", "15"] },
    { "id": 6, "tasks": ["11.2", "12.1", "12.2"] },
    { "id": 7, "tasks": ["11.3", "12.3", "16.1", "16.2", "16.3", "16.4"] },
    { "id": 8, "tasks": ["17"] },
    { "id": 9, "tasks": ["19"] },
    { "id": 10, "tasks": ["20.1", "21", "22", "23.1", "24.1", "24.2"] },
    { "id": 11, "tasks": ["20.2", "23.2"] },
    { "id": 12, "tasks": ["20.3", "23.3", "25", "26.1", "26.2", "27"] }
  ]
}
```
