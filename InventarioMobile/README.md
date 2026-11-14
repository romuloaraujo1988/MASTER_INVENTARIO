# SIHCP Mobile — Sistema de Histórico e Coleta Patrimonial

Aplicativo Android nativo para coleta e auditoria de inventário patrimonial do **Instituto Federal de Mato Grosso (IFMT)**.

**Versão Atual:** `2.29.8` (`versionCode 84`) — **Última atualização do código:** `13/11/2026`  
**Application ID:** `com.inventario.mobile`

---

## 📱 Sobre o Aplicativo

O **SIHCP Mobile** integra-se à API REST (`Spring Boot` + `PostgreSQL`) e ao sistema desktop do SIHCP para digitalizar o processo de inventário físico em campo com abordagem **offline-first**:

- **Coleta Multi-Modal:** Leitura contínua de **QR Code** e **Código de Barras** (ZXing + CameraX com troca de câmera e lanterna), **Coleta Manual** por número de tombamento, **Busca por Descrição** com sugestões e foto de referência, e **Item Sem Etiqueta** com captura fotográfica obrigatória.
- **Atalhos Rápidos na Câmera:** Barra inferior na tela de câmera ao vivo (`CustomCaptureActivity`) e na `ScannerActivity` para alternar instantaneamente para *Sem Etiqueta*, *Manual* ou *Descrição* via `ColetaShortcutNavigator`.
- **Modo Offline Seguro:** Persistência local em **Room 2.6.1 (v19)** criptografado com **SQLCipher 4.6.1 (AES-256)** e passphrase protegida no **Android Keystore** (`SqlCipherKeyManager`).
- **Sincronização Inteligente:** Worker único `ColetaSyncWorker` orquestrado por `SyncScheduler` / `SyncManager` (batch sync com fallback individual, backoff exponencial e idempotência via `clienteUuid`), além de `PhotoSyncWorker` e limpeza automática de coletas excluídas no servidor.
- **Monitor ao Vivo (WebSocket):** Feed em tempo real via `/ws-coletas-live` (`RealtimeColetasManager` + `LiveColetaMonitorViewModel`).
- **Detecção Automática de Divergências:** Compara localização encontrada e estado de conservação com o cadastro oficial (recalculado no servidor).
- **Dashboard & Exportação:** Painel redesenhado com `DashboardUiMapper`, KPIs, gráfico de evolução (`MPAndroidChart`), busca por voz e exportação em **PDF** (`iText 7`), **Excel (TSV/XLSX)** e **CSV**, além de relatório fotográfico.

---

## 🏗️ Arquitetura e Stack Real

- **Linguagem:** Kotlin `1.9.10` (Coroutines `1.7.3` + StateFlow)
- **Arquitetura:** Clean Architecture (`presentation`, `domain`, `data`) + MVVM
- **Injeção de Dependência:** Dagger Hilt `2.48` + `hilt-work:1.1.0`
- **Banco Local:** Room `2.6.1` (`inventario_offline_secure.db`, versão `19`, `15` migrações) + `net.zetetic:sqlcipher-android:4.6.1`
- **Rede & Tempo Real:** Retrofit `2.9.0` + Gson + OkHttp `4.12.0` (REST + WebSocket)
- **Segurança:** `EncryptedSharedPreferences` (`security-crypto:1.1.0-alpha06` + Tink), `androidx.biometric:biometric:1.1.0`, PIN 6 dígitos, JWT + `RefreshTokenInterceptor`, R8 + `shrinkResources`
- **Compatibilidade:** `minSdk 23` (Android 6.0) · `targetSdk 34` (Android 14) · `compileSdk 34`
- **Build:** Android Gradle Plugin `8.1.4` · Gradle `8.4` · KSP `1.9.10-1.0.13`
- **Testes:** JUnit 4, Mockito, MockK `1.12.8`, Kotest `5.8.0`, jqwik `1.8.4` (Property-Based Testing), Espresso

---

## 📦 Estrutura Real do Código (`app/src/main/java/com/inventario/mobile`)

```text
com.inventario.mobile/
├── InventarioMobileApplication.kt  # @HiltAndroidApp, Configuration.Provider, CrashHandler Keystore
├── data/                           # Room v19 (dao, entity, database), Retrofit (api, dto),
│                                   # mappers, repositories, paging, cache, export, pdf, realtime (WS)
├── di/                             # 13 módulos Hilt (ApiModule, DatabaseModule, RepositoryModule, ...)
├── domain/                         # 27 modelos puros, 12 interfaces Repository, 38 UseCases, ColetaValidator
├── network/                        # 7 classes (RefreshTokenInterceptor, OfflineFallbackInterceptor, Monitors...)
├── presentation/                   # 26 Activities, 8 Fragments, 22 ViewModels (@HiltViewModel), Mappers UI
├── security/                       # 6 classes (SqlCipherKeyManager, KeystoreRecoveryActivity, Biometric, PIN...)
├── sync/                           # SyncManager, SyncScheduler, SyncLogger, NetworkConnectivityObserver
├── utils/                          # PreferencesManager, SessionManager, PhotoHelper, LegacyPhotoMigrator...
└── worker/                         # 2 Workers (ColetaSyncWorker e PhotoSyncWorker)
```

---

## 🚀 Configuração e Build

### Configuração Inicial do Servidor (Sem IP Fixo)
Desde a `v2.29.5`, nenhum IP fica fixo no APK. Na primeira execução após a instalação:
1. `SplashActivity` detecta `isServerConfigured() == false` e direciona para `InitialSetupActivity`.
2. O usuário informa o IP/host e porta do servidor SIHCP (padrão `8081`, context path `/inventario`, API `/api/mobile`).
3. O botão **Testar Conexão** valida `/api/mobile/health` antes de liberar o acesso ao `LoginActivity`.

### Comandos de Build

```powershell
cd InventarioMobile

# Build Debug
.\gradlew.bat assembleDebug

# Build Release (requer keystore.properties na raiz de InventarioMobile/)
.\gradlew.bat assembleRelease

# Testes Unitários (JUnit + Kotest + jqwik)
.\gradlew.bat test

# Limpar e compilar
.\gradlew.bat clean assembleRelease
```

O APK de distribuição gerado pelo build de release é nomeado automaticamente como `SiHCP-release-v2.29.8.apk` (`app/build/outputs/apk/release/`). Consulte [CONFIGURACAO_KEYSTORE.md](CONFIGURACAO_KEYSTORE.md) para detalhes da assinatura.

---

## 📚 Documentação Relacionada

- **Consolidado Geral do App:** [../DOCUMENTAÇÃO/DOCUMENTACAO_COMPLETA_APP_v2.29.5.md](../DOCUMENTAÇÃO/DOCUMENTACAO_COMPLETA_APP_v2.29.5.md)
- **Documentação Técnica do Módulo Mobile:** [DOCUMENTACAO_PROJETO.md](DOCUMENTACAO_PROJETO.md)
- **Requisitos Funcionais e Não Funcionais:** [ANALISE_REQUISITOS_ANDROID.md](ANALISE_REQUISITOS_ANDROID.md)
- **Segurança em Produção:** [SECURITY_SUMMARY.md](SECURITY_SUMMARY.md) · [SECURITY_REQUIREMENTS.md](SECURITY_REQUIREMENTS.md) · [SECURITY_ACTION_PLAN.md](SECURITY_ACTION_PLAN.md)
- **Índices e Histórico de Builds:** [INDICE_DOCUMENTACAO.md](INDICE_DOCUMENTACAO.md) · [../BUILD_HISTORY.md](../BUILD_HISTORY.md)
- **Registro de Software (INPI / NIT-IFMT):** [../REGISTRO_SOFTWARE_SIHCP_MOBILE/00_LEIA_ME_GUIA_REGISTRO_INPI.md](../REGISTRO_SOFTWARE_SIHCP_MOBILE/00_LEIA_ME_GUIA_REGISTRO_INPI.md)

---

**Propriedade:** Instituto Federal de Mato Grosso (IFMT)  
**Versão:** `2.29.8` (Build `84`) · **Última atualização da documentação:** `13/11/2026` (baseada no código-fonte limpo em `13/11/2026`)
