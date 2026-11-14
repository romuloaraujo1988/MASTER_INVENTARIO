# Memorial Descritivo e Especificação Arquitetural — SIHCP Mobile

**Programa:** SIHCP Mobile - Sistema de Histórico e Coleta Patrimonial Mobile  
**Versão:** 2.29.8 (Build / versionCode 84)  
**Pacote Android:** `com.inventario.mobile`  
**Titular:** Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT)  
**Autor Principal:** Romulo Rodrigues de Araujo  
**Hash SHA-512 (PDF INPI):** `0819a3f7e83dcc8397347ba74ea3007c3ffccd9fedeaa26070162c0b0dfdb44fe78abac42f5a95517483b4ca82e8394bc104d63b8cd685a2e6051dc89dbed1b7`

---

## 1. Visão Geral

O SIHCP Mobile (Sistema de Histórico e Coleta Patrimonial Mobile) é um aplicativo nativo para plataforma Android, desenvolvido em linguagem Kotlin segundo os princípios de Clean Architecture e padrão arquitetural MVVM, destinado à automatização e auditoria do inventário físico de bens patrimoniais em instituições públicas federais. O software permite a leitura óptica de etiquetas via QR Code e código de barras, coleta manual por número de tombamento, busca textual por descrição, registro de itens compostos em lote e cadastramento de bens sem etiqueta com captura fotográfica obrigatória. Projetado sob abordagem offline-first, armazena dados localmente em banco SQLite criptografado com SQLCipher (AES-256) e gerencia chaves via Android Keystore, contando com autenticação JWT e biometria. A sincronização bidirecional com o servidor ocorre em segundo plano via WorkManager com retentativa exponencial, além de monitoramento de coletas ao vivo via WebSocket. O aplicativo detecta divergências cadastrais e de localização em tempo real, emite relatórios em PDF, Excel e CSV, e atende à IN SEDAP nº 205/1988 e ao Decreto nº 12.785/2025.

## 2. Ficha Técnica Consolidada

| Atributo | Especificação |
|---|---|
| **Plataforma** | Android (Min SDK 23 / Android 6.0 · Target SDK 34 / Android 14) |
| **Linguagens** | Kotlin, XML, SQL, Java |
| **Arquitetura** | Clean Architecture + MVVM + Offline-First |
| **Injeção de Dependência** | Hilt 2.48 (`@HiltAndroidApp`, `@HiltViewModel`, `@HiltWorker`) |
| **Banco Local** | Room 2.6.1 (v19, `inventario_offline_secure.db`) + SQLCipher 4.6.1 (AES-256) |
| **Segurança** | Android Keystore (`SqlCipherKeyManager`), `EncryptedSharedPreferences`, JWT + Refresh Token, Biometria, PIN, R8/ProGuard |
| **Sincronização** | WorkManager (`ColetaSyncWorker`, `PhotoSyncWorker`), Batch Sync + fallback individual + backoff exponencial |
| **Tempo Real** | WebSocket nativo `/ws-coletas-live` (`RealtimeColetasManager`) |
| **Leitura Óptica** | ZXing 4.3.0 / Core 3.5.2 + CameraX 1.3.0 |
| **Exportação** | PDF (iText 7.2.5), Excel (TSV/XLSX) e CSV |
| **Métricas Verificadas** | 359 Kotlin em `src/main` (62,021 linhas) + 55 testes (`414` `.kt` totais) · 201 XML (15,392 linhas; 48 layouts) |
| **Componentes** | 26 Activities · 8 Fragments · 22 ViewModels · 38 UseCases · 12/15 Repositories · 2 Workers |

## 3. Camadas Arquiteturais

1. **Presentation (`presentation/*`):** 26 Activities, 8 Fragments, 22 ViewModels com `StateFlow`/`LiveData`, `DashboardUiMapper`, Paging 3 adapters e componentes reutilizáveis.
2. **Domain (`domain/*`):** Modelos puros em Kotlin, 12 interfaces de repositório, `ColetaValidator` e 38 casos de uso de coleta, validação, sincronização, consulta e exportação.
3. **Data (`data/*`):** `AppDatabase` v19 (10 entidades ativas, 11 DAOs ativos, 15 migrações), 12 APIs Retrofit, DTOs, mappers, cache em memória, `RealtimeColetasManager` e geradores de relatórios.
4. **Infraestrutura (`security/*`, `network/*`, `worker/*`, `sync/*`, `di/*`):** 6 classes de segurança, 7 classes de monitoramento/interceptação de rede e 2 Workers em segundo plano (`ColetaSyncWorker` unificado via `SyncScheduler`).

## 4. Conformidade Normativa

- **Lei nº 9.609/1998** e **IN INPI/PR nº 099/2019** (Registro de Programa de Computador).
- **IN SEDAP nº 205/1988** e **Decreto nº 12.785/2025** (Administração patrimonial na esfera federal).
