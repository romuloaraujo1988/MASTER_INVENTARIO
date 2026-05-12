# 🔒 Requisitos de Segurança - SIHCP Mobile

> **Última verificação:** 09/05/2026 — Status atualizado com base no código real do projeto.

## 📋 Contexto

Sistema de inventário patrimonial para instituição pública (IFMT) que manipula:
- Dados de patrimônio público
- Informações de localização
- Dados de usuários e responsáveis
- Registros de coleta e auditoria

## 🎯 Níveis de Segurança Necessários

### ⭐ CRÍTICO (Obrigatório)
### ⚠️ IMPORTANTE (Altamente Recomendado)
### 💡 DESEJÁVEL (Boas Práticas)

---

## 1. 🔐 AUTENTICAÇÃO E AUTORIZAÇÃO

### ⭐ CRÍTICO

#### 1.1 Autenticação Forte
```
✅ Implementado:
- JWT (JSON Web Tokens) para autenticação
- Tokens com expiração
- Refresh tokens (AuthApi.refreshToken)
- Autenticação biométrica (BiometricManager.kt + SecurityUtils.kt — BiometricPrompt implementado)

❌ Pendente:
- [ ] Autenticação de dois fatores (2FA)
- [ ] Política de senha forte integrada na tela de login
       (InputValidator.validatePassword existe mas não é chamado no LoginActivity)
- [ ] Bloqueio após tentativas falhadas integrado
       (LoginAttemptManager.kt existe e completo mas não está conectado ao LoginActivity)
- [ ] Timeout de sessão por inatividade
       (SessionManager.kt existe com logout mas sem timer de 15min de inatividade)
```

#### 1.2 Controle de Acesso (RBAC)
```
✅ Implementado:
- Diferentes níveis de usuário (ADMIN, SUPERVISOR, COLETOR, CONSULTA)
- Validação de permissões no backend para cada endpoint (@RequireAdmin, @RequireSupervisor, @RequireColetor, @RequireConsulta)
- Princípio do menor privilégio (roles hierárquicas no servidor)

❌ Pendente:
- [ ] Validação de permissões no frontend antes de ações (parcial — FAB de relatório oculto por role, mas sem validação sistemática)
- [ ] Logs de acesso e ações por usuário (AuditService existe para coletas, mas não para login/logout)
```

### ⚠️ IMPORTANTE

#### 1.3 Gestão de Sessão
```
✅ Implementado:
- SessionManager.logout() limpa todos os dados e redireciona para LoginActivity
- isTokenExpiringSoon() verifica expiração do JWT
- RefreshTokenInterceptor para renovação automática

❌ Pendente:
- [ ] Logout automático após inatividade (timer de 15min não implementado)
- [ ] Invalidação de token no servidor ao fazer logout (logout apenas limpa local)
- [ ] Sessão única por dispositivo
- [ ] Notificação de login em novo dispositivo
```

---

## 2. 🔒 PROTEÇÃO DE DADOS

### ⭐ CRÍTICO

#### 2.1 Criptografia em Trânsito
```
✅ Implementado:
- HTTPS/TLS para comunicação com API
- network_security_config.xml com base-config cleartextTrafficPermitted="false"
- Apenas CAs do sistema confiadas na base-config
- usesCleartextTraffic removido do manifesto (controlado pelo network_security_config)

❌ Pendente:
- [ ] Certificate Pinning (network_security_config.xml existe mas sem <pin-set> com hash do certificado)
- [ ] Validação de certificado SSL via CertificatePinner no OkHttp (depende do pin-set)
```

#### 2.2 Criptografia em Repouso
```
✅ Implementado:
- EncryptedSharedPreferences para dados sensíveis (PreferencesManager + SecureStorage — AES256_GCM)
- Criptografia do banco SQLite local (SQLCipher 4.5.4 + SupportFactory)
- Criptografia de tokens e credenciais (EncryptedSharedPreferences)
- Keystore do Android para chaves criptográficas (SqlCipherKeyManager — passphrase 256 bits via Android Keystore)
```

#### 2.3 Proteção de Dados Sensíveis
```
✅ Implementado:
- Não armazenar senhas em texto plano (EncryptedSharedPreferences)
- Não logar dados sensíveis em release (ProGuard remove Log.d/Log.v)
- Ofuscar dados sensíveis em logs (ProGuard -assumenosideeffects)
- Limpar dados ao fazer logout (SessionManager.logout() chama clearSavedUser, clearSessionData, etc.)
```

### ⚠️ IMPORTANTE

#### 2.4 Backup Seguro
```
✅ Implementado:
- android:allowBackup="false" (AndroidManifest.xml)
- data_extraction_rules.xml exclui banco e prefs criptografadas de cloud backup e device-transfer
- backup_rules.xml exclui banco e prefs criptografadas de backup legado (Android < 12)
```

---

## 3. 🛡️ SEGURANÇA DE CÓDIGO

### ⭐ CRÍTICO

#### 3.1 Validação de Entrada
```
✅ Implementado:
- InputValidator.kt (validatePatrimonioNumber, sanitizeText, validateEmail, validatePassword, sanitizeAndTruncate)
- Room/ORM previne SQL Injection (todas as queries usam @Query parametrizado)
- Sem WebViews no app (sem risco de XSS)

❌ Pendente:
- [ ] Integração sistemática do InputValidator nos ViewModels (classe existe mas não é chamada em todos os pontos de entrada)
- [ ] Mensagens de erro claras para o usuário em todos os campos
```

#### 3.2 Proteção contra Engenharia Reversa
```
✅ Implementado:
- ProGuard/R8 ativo (minifyEnabled true + shrinkResources true)
- Logs de debug removidos em produção (ProGuard -assumenosideeffects Log.d/Log.v)
- Sem hardcoded keys/secrets (URL via ServerConfigManager, keystore via arquivo externo)
- BuildConfig para configurações sensíveis (keystore.properties não versionado)
```

### ⚠️ IMPORTANTE

#### 3.3 Segurança de Dependências
```
❌ Pendente:
- [ ] Manter bibliotecas atualizadas (algumas dependências podem ter versões mais recentes)
- [ ] Verificar vulnerabilidades conhecidas (Dependabot não configurado)
- [ ] Usar apenas bibliotecas confiáveis (todas as libs são conhecidas e confiáveis)
- [ ] Revisar permissões de bibliotecas
```

---

## 4. 📱 SEGURANÇA DO DISPOSITIVO

### ⭐ CRÍTICO

#### 4.1 Detecção de Root/Jailbreak
```
❌ Pendente:
- [ ] Detectar dispositivos com root (não implementado)
- [ ] Alertar usuário sobre riscos
- [ ] Bloquear funcionalidades críticas em dispositivos rooteados
```

#### 4.2 Proteção de Tela
```
❌ Pendente:
- [ ] FLAG_SECURE para prevenir screenshots em telas sensíveis (não implementado)
- [ ] Ofuscar conteúdo em app switcher
- [ ] Bloquear gravação de tela
```

### ⚠️ IMPORTANTE

#### 4.3 Verificação de Integridade
```
❌ Pendente:
- [ ] Verificar assinatura do APK
- [ ] Detectar modificações no app
- [ ] Google Play Integrity API
```

---

## 5. 🌐 SEGURANÇA DE REDE

### ⭐ CRÍTICO

#### 5.1 Comunicação Segura
```
✅ Implementado:
- HTTPS para todas as requisições
- Network Security Configuration (network_security_config.xml referenciado no manifesto)
- Timeout adequado para requisições (30s connect, 60s read/write, 90s call)
- Retry com backoff exponencial (retryOnConnectionFailure + OfflineFallbackInterceptor)

❌ Pendente:
- [ ] Validar certificados SSL via CertificatePinner (depende do certificate pinning)
```

#### 5.2 Proteção de API
```
✅ Implementado:
- JWT em headers de requisição (authInterceptor adiciona Bearer token)
- API versioning (endpoints sob /api/mobile/)

❌ Pendente:
- [ ] Rate limiting no backend (não implementado no servidor)
- [ ] Validação de origem das requisições
```

### ⚠️ IMPORTANTE

#### 5.3 Modo Offline Seguro
```
✅ Implementado:
- Sincronização automática (SyncWorker + WorkManager)
- Armazenamento local criptografado (SQLCipher)
- Dados offline criptografados (banco SQLCipher + EncryptedSharedPreferences)

❌ Pendente:
- [ ] Validar integridade ao sincronizar
- [ ] Resolver conflitos de forma segura
```

---

## 6. 📊 AUDITORIA E MONITORAMENTO

### ⭐ CRÍTICO

#### 6.1 Logs de Auditoria
```
✅ Implementado:
- AuditService registra criação de coletas, erros de validação, erros de sincronização
- LogColetaEntity (tabela log_coleta) com timestamp, usuário, ação, sucesso/erro
- Histórico de scans (HistoricoScanEntity)
- Campos de auditoria nos relatórios (coletadoPor, dataColeta, localizacaoEncontrada, estadoEncontrado)

❌ Pendente:
- [ ] Logs de login/logout (AuditService não registra eventos de autenticação)
- [ ] Logs de alterações de dados (apenas criação de coletas é auditada)
```

#### 6.2 Rastreabilidade
```
✅ Implementado:
- Histórico de coletas com usuário, data e localização
- Histórico de scans (HistoricoScanEntity)
- Campos de auditoria persistidos na tabela patrimônio (v2.20.7)

❌ Pendente:
- [ ] Trilha de auditoria completa (login, logout, alterações de configuração)
- [ ] Logs imutáveis
- [ ] Identificação única de dispositivo
```

### ⚠️ IMPORTANTE

#### 6.3 Monitoramento de Segurança
```
❌ Pendente:
- [ ] Alertas de tentativas de acesso suspeitas
- [ ] Monitoramento de anomalias
- [ ] Dashboard de segurança
```

---

## 7. 🔧 CONFIGURAÇÕES DE SEGURANÇA

### ⭐ CRÍTICO

#### 7.1 AndroidManifest.xml
```xml
✅ Implementado:
<!-- Prevenir backups não criptografados -->
android:allowBackup="false"
android:dataExtractionRules="@xml/data_extraction_rules"
android:fullBackupContent="@xml/backup_rules"
android:networkSecurityConfig="@xml/network_security_config"

<!-- Proteger componentes — TODAS as activities têm android:exported="false" -->
<!-- exceto SplashActivity (exported="true" — necessário para launcher) -->

<!-- Permissões mínimas necessárias — SYSTEM_ALERT_WINDOW e USE_FULL_SCREEN_INTENT removidas -->
```

#### 7.2 Network Security Config
```xml
✅ Parcialmente implementado:
<!-- base-config com cleartextTrafficPermitted="false" — OK -->
<!-- domain-config para IPs internos IFMT — OK -->

❌ Pendente:
<!-- Certificate Pinning — <pin-set> não configurado -->
<domain-config>
    <domain includeSubdomains="true">api.seudominio.com</domain>
    <pin-set>
        <pin digest="SHA-256">hash_do_certificado</pin>  <!-- FALTA -->
    </pin-set>
</domain-config>
```

#### 7.3 ProGuard/R8 Rules
```
✅ Implementado:
- minifyEnabled true + shrinkResources true
- Regras para Kotlin, Hilt, Room, Retrofit, SQLCipher, iText, ZXing
- -assumenosideeffects para Log.d e Log.v
- Regras para EncryptedSharedPreferences e SQLCipher
```

---

## 8. 💡 BOAS PRÁTICAS ADICIONAIS

### 💡 DESEJÁVEL

#### 8.1 Privacidade
```
✅ Parcialmente implementado:
- Remoção de metadados EXIF de imagens (CameraUtils.removeExifData)

❌ Pendente:
- [ ] Política de privacidade clara
- [ ] Consentimento para coleta de dados
- [ ] Opção de excluir dados
- [ ] LGPD compliance formal
```

#### 8.2 Atualizações
```
❌ Pendente:
- [ ] Verificar atualizações disponíveis
- [ ] Forçar atualização para versões críticas
- [ ] Changelog de segurança
```

#### 8.3 Educação do Usuário
```
❌ Pendente:
- [ ] Dicas de segurança no primeiro uso
- [ ] Alertas sobre práticas inseguras
- [ ] FAQ de segurança
```

---

## 📊 CHECKLIST DE IMPLEMENTAÇÃO

### Fase 1: Fundamentos (CRÍTICO) - Status Atual
- [x] Criptografia de dados sensíveis (EncryptedSharedPreferences + SQLCipher)
- [ ] Política de senha forte integrada no login (classe existe, falta integração)
- [ ] Timeout de sessão por inatividade (SessionManager existe, falta timer)
- [ ] Certificate Pinning (config existe, faltam hashes do certificado)
- [x] ProGuard/R8 configurado
- [x] Network Security Config (TLS enforcement)
- [x] Validação de entrada (InputValidator.kt)
- [x] Logs de auditoria básicos (AuditService + LogColetaEntity)

### Fase 2: Autenticação Avançada (IMPORTANTE) - Status Atual
- [x] Autenticação biométrica (BiometricManager.kt implementado)
- [ ] Bloqueio após tentativas falhadas integrado (LoginAttemptManager existe, falta integração)
- [ ] 2FA
- [ ] Gestão de sessão robusta com timeout de inatividade

### Fase 3: Proteção de Dispositivo (IMPORTANTE) - Status Atual
- [ ] Detecção de root
- [ ] FLAG_SECURE em telas sensíveis
- [ ] Verificação de integridade

### Fase 4: Auditoria e Compliance (DESEJÁVEL) - Status Atual
- [x] Trilha de auditoria de coletas (AuditService)
- [ ] Trilha de auditoria completa (login/logout/configurações)
- [ ] Monitoramento de segurança
- [ ] LGPD compliance formal
- [ ] Documentação de segurança

---

## 🎯 PRIORIZAÇÃO POR RISCO

### 🔴 RISCO ALTO (Implementar Imediatamente)
1. [x] Criptografia de dados sensíveis — **FEITO**
2. [ ] Certificate Pinning — **PENDENTE** (config existe, faltam hashes)
3. [x] Validação de entrada — **FEITO** (integração parcial)
4. [x] ProGuard/R8 — **FEITO**
5. [ ] Timeout de sessão por inatividade — **PENDENTE**

### 🟡 RISCO MÉDIO (Implementar em 1-2 meses)
1. [x] Autenticação biométrica — **FEITO**
2. [ ] Detecção de root — **PENDENTE**
3. [ ] 2FA — **PENDENTE**
4. [ ] Logs de auditoria completos (login/logout) — **PARCIAL**
5. [ ] FLAG_SECURE — **PENDENTE**
6. [ ] Integrar LoginAttemptManager no login — **PENDENTE** (classe pronta)
7. [ ] Integrar PasswordValidator no login — **PENDENTE** (classe pronta)

### 🟢 RISCO BAIXO (Implementar quando possível)
1. [ ] Monitoramento avançado
2. [ ] Dashboard de segurança
3. [ ] Educação do usuário
4. [ ] Verificação de atualizações
5. [ ] LGPD compliance formal

---

## � STATUS ATUAL (09/05/2026)

| Área | Implementado | Parcial | Pendente |
|------|-------------|---------|---------|
| Autenticação | JWT, Biometria | RBAC frontend | 2FA, Timeout inatividade |
| Criptografia | EncryptedPrefs, SQLCipher, Keystore | — | — |
| Comunicação | TLS, Timeouts | Network Config | Certificate Pinning |
| Código | ProGuard/R8, InputValidator | Integração VM | — |
| Dispositivo | — | — | Root detection, FLAG_SECURE |
| Auditoria | Log coletas, Histórico scans | — | Login/logout logs |
| Configurações | allowBackup=false, Backup rules | — | — |

**Nível de Segurança Atual**: 🟡 ~72% — MÉDIO-ALTO  
**Meta**: 🟢 85%+ — ALTO

---

## �📚 REFERÊNCIAS E PADRÕES

### Padrões de Segurança
- **OWASP Mobile Top 10**: https://owasp.org/www-project-mobile-top-10/
- **Android Security Best Practices**: https://developer.android.com/topic/security/best-practices
- **LGPD**: Lei Geral de Proteção de Dados

### Ferramentas de Análise
- **MobSF**: Mobile Security Framework
- **QARK**: Quick Android Review Kit
- **Dependency-Check**: Verificação de vulnerabilidades

---

## ✅ RESUMO EXECUTIVO

### Mínimo Aceitável — Status
1. [x] Criptografia de dados sensíveis — **FEITO**
2. [ ] HTTPS com Certificate Pinning — **PARCIAL** (TLS ok, pinning pendente)
3. [x] Validação de entrada — **FEITO** (integração parcial)
4. [x] ProGuard/R8 ativo — **FEITO**
5. [ ] Timeout de sessão — **PARCIAL** (logout ok, inatividade pendente)
6. [x] Logs de auditoria básicos — **FEITO**

### Recomendado — Status
7. [x] Autenticação biométrica — **FEITO**
8. [ ] Detecção de root — **PENDENTE**
9. [ ] 2FA — **PENDENTE**
10. [ ] Trilha de auditoria completa — **PARCIAL**
11. [ ] FLAG_SECURE — **PENDENTE**
12. [ ] Política de senha forte integrada — **PARCIAL** (classe pronta, falta integração)

### Ideal — Status
13. [ ] Monitoramento avançado — **PENDENTE**
14. [ ] LGPD compliance — **PENDENTE**
15. [ ] Certificação de segurança — **PENDENTE**
16. [ ] Penetration testing regular — **PENDENTE**

---

**Status Atual**: 🟡 PARCIALMENTE SEGURO PARA PRODUÇÃO  
A infraestrutura crítica de criptografia e TLS está implementada. As principais pendências são integrações de classes já criadas (LoginAttemptManager, PasswordValidator) e certificate pinning.

**Estimativa para segurança completa**: 3-4 semanas de desenvolvimento focado nas pendências listadas.
