# 🔒 Resumo Executivo — Segurança do Aplicativo SIHCP Mobile

> **Versão verificada:** `v2.29.8` (`versionCode 84`) · **Atualizado em:** `13/11/2026`

---

## 📊 Status Consolidado de Segurança em Produção

```text
┌─────────────────────────────────────────────────────────────┐
│              POSTURA DE SEGURANÇA (v2.29.8)                 │
├─────────────────────────────────────────────────────────────┤
│  Controles Críticos de Produção:      IMPLEMENTADOS         │
│  Hardening Avançado Opcional:         MANTIDO EM BACKLOG    │
└─────────────────────────────────────────────────────────────┘
```

Desde a série `v2.24.0+` (especificação `correcoes-seguranca`), todas as camadas críticas de proteção de dados locais, autenticação, transporte e proteção de binário foram implementadas e validadas (última release anterior assinada: `SiHCP-release-v2.29.7.apk`; build atual: `v2.29.8` / `versionCode 84`).

---

## ✅ Controles Implementados no Código Real

### 1. Criptografia em Repouso
- **Banco SQLite Criptografado:** `AppDatabase` (Room `2.6.1`, versão `19`) usa `net.zetetic:sqlcipher-android:4.6.1` (AES-256) sobre `inventario_offline_secure.db`.
- **Chave Protegida por Hardware:** `SqlCipherKeyManager` gera uma passphrase aleatória de **256 bits** (`SecureRandom`) por instalação e a persiste em `EncryptedSharedPreferences` protegida por `MasterKey` no **Android Keystore** (sem chave literal no código).
- **Recuperação Controlada do Keystore (CR-02):** `InventarioMobileApplication.installKeystoreCrashHandler()` intercepta falhas do Keystore (ex.: troca de tela de bloqueio) e encaminha para `KeystoreRecoveryActivity` (`excludeFromRecents=true`, `noHistory=true`).
- **Migrações Seguras (CR-01):** `fallbackToDestructiveMigration()` foi removido (`15` migrações explícitas `v4 → v19`), impedindo perda silenciosa de coletas locais.
- **Armazenamento de Preferências e Tokens:** `PreferencesManager` usa `androidx.security:security-crypto:1.1.0-alpha06` (`AES256_GCM` / `AES256_SIV`).

### 2. Autenticação, Autorização e Sessão
- **JWT + Refresh Automático:** `RefreshTokenInterceptor` (injetado no pipeline Hilt) renova tokens expirados de forma thread-safe (`synchronized` + cooldown, sem `runBlocking`).
- **Biometria e PIN Local:** `BiometricManager` (`androidx.biometric:biometric:1.1.0` stable) e `PinAuthManager` (PIN de 6 dígitos) permitem acesso rápido seguro offline após login inicial.
- **RBAC por Perfil:** Controle hierárquico (`CONSULTA`, `COLETOR`, `SUPERVISOR`, `ADMIN`) validado no backend e refletido na UI (ex.: relatório fotográfico restrito a `SUPERVISOR+`).
- **Logout Seguro:** `SessionManager.logout()` limpa dados de sessão, usuário e timestamps de sincronização.

### 3. Proteção do Binário, Logs e Backup
- **R8 + ShrinkResources (MD-01 / v2.29.7+):** `minifyEnabled true` e `shrinkResources true` ativos em `release`, com `proguard-rules.pro` preservando Google Tink (`com.google.crypto.tink.**`), SQLCipher (`net.zetetic.database.**`), DTOs e `data.realtime.**`.
- **Sanitização de Logs (MD-05):** `LogSanitizer` + `HttpLoggingInterceptor.Level.NONE` em release + remoção de `Log.d`/`Log.v` via R8.
- **Bloqueio de Backups Não Criptografados (AL-03):** `android:allowBackup="false"`, `backup_rules.xml` e `data_extraction_rules.xml` excluem banco e preferências criptografadas de backups na nuvem e transferências de dispositivo.
- **Assinatura Segura:** Credenciais em `keystore.properties` externo (não versionado no Git).

### 4. Rede, Permissões e Auditoria
- **Permissões Enxutas:** `SYSTEM_ALERT_WINDOW` e `USE_FULL_SCREEN_INTENT` removidas do `AndroidManifest.xml`.
- **Network Security Config:** `network_security_config.xml` configurado para controle de tráfego e CAs confiáveis.
- **Idempotência e Auditoria Local:** `ColetaEntity.clienteUuid` previne duplicatas em reenvios; `AuditService` + `LogColetaEntity` + `HistoricoScanEntity` registram trilha operacional.

---

## ⏳ Itens de Hardening Opcionais em Backlog (Não Bloqueantes para Rede Interna)

Estes itens permanecem mapeados em [SECURITY_REQUIREMENTS.md](SECURITY_REQUIREMENTS.md) caso a instituição deseje adotá-los em ciclos futuros:
- **Certificate Pinning (`<pin-set>` / `CertificatePinner`):** aplicável quando o servidor institucional operar com domínio e certificado TLS fixos e gerenciados.
- **Bloqueio por Tentativas Falhas e Política de Senha:** integração na UI de login permanece no backlog (`LoginAttemptManager` e `InputValidator` legados foram removidos na limpeza de código morto para o hash INPI).
- **Timeout por Inatividade, `FLAG_SECURE` e Detecção de Root / Play Integrity API.**

---

## 📚 Documentos Relacionados
- [SECURITY_REQUIREMENTS.md](SECURITY_REQUIREMENTS.md) — Requisitos detalhados por área
- [SECURITY_ACTION_PLAN.md](SECURITY_ACTION_PLAN.md) — Plano técnico de implementação e status
- [CONFIGURACAO_KEYSTORE.md](CONFIGURACAO_KEYSTORE.md) — Assinatura de APK release
