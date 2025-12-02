# Histórico de Builds - InventarioMobile

Este arquivo registra todas as compilações do APK Android para rastreabilidade.

---

## Build #006 - 01/12/2025 16:18

- **Tipo:** Debug + Desktop JAR (thin-jar)
- **Versão:** 2.1.0
- **Build Code:** 6
- **Arquivos:** 
  - APK: `InventarioMobile/app/build/outputs/apk/debug/app-debug.apk`
  - JAR: `target/mobile-server/sistema-inventario-2.0.0.jar`
- **Tamanho:** ~11 MB (APK)
- **Mudanças:** 
  - **Servidor:** Timeouts ajustados para redes móveis instáveis
    - `keep-alive-timeout`: 60s → 300s (5 min)
    - `connection-timeout`: 30s → 60s
    - `hikari.connection-timeout`: 10s → 30s
    - `hikari.keepalive-time`: 30s → 120s
    - `hikari.maximum-pool-size`: 8 → 10
  - **Android:** Timeouts HTTP aumentados
    - `connectTimeout`: 10s → 30s
    - `readTimeout`: 15s → 60s
    - `writeTimeout`: 15s → 60s
    - `callTimeout`: 30s → 90s
    - `retryOnConnectionFailure`: true
  - **Correção de logs:** System.out/err substituídos por SLF4J em:
    - ConnectedDevicesManager.java
    - ConnectionManager.java
    - NetworkUtils.java
    - DatabaseConfigLoader.java
- **Status:** ✅ Sucesso

---

## Build #005 - 01/12/2025

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 5
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção IP:** Quando o IP do servidor muda na tela de login, o app agora reinicia automaticamente para aplicar as novas configurações
  - **LoginActivity:** Detecta mudança de IP e reinicia o app após login bem-sucedido
  - **ApiModule/ApiClient:** Código simplificado, removida complexidade desnecessária
- **Status:** ✅ Sucesso

---

## Build #004 - 29/11/2025

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 4
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Backend:** Novo endpoint `GET /api/mobile/salas/com-progresso` que retorna salas com estatísticas de coleta do servidor
  - **Backend:** Endpoint `GET /api/mobile/patrimonio/sala/{salaId}` atualizado com filtros de paginação e status de coleta
  - **Android:** SalaRepositoryImpl agora busca salas com progresso do servidor (com fallback local)
  - **Android:** BuscarPatrimoniosPorSalaUseCase busca patrimônios do servidor (com fallback local)
  - **Android:** Novo DTO SalaComProgressoDTO para receber dados do servidor
  - **Android:** Correção de injeção de dependência no UseCaseModule
- **Status:** ✅ Sucesso

---

## Build #003 - 29/11/2025 

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 4
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - Implementação da feature "Inventário por Sala" com TabLayout
  - Nova InventarioTabsActivity com abas "Por Responsável" e "Por Sala"
  - InventarioPorSalaFragment com estatísticas e filtros
  - InventarioPorResponsavelFragment extraído da InventarioActivity
  - Novos Use Cases: BuscarSalasComProgressoUseCase, BuscarPatrimoniosPorSalaUseCase, BuscarEstatisticasSalaUseCase
  - Domain models: SalaComProgresso, EstatisticasSala, FiltroColetaSala
  - Correções de compilação em DataSourceStrategyFactory e PatrimonioRepositoryAdapter
- **Status:** ✅ Sucesso

---

## Build #002 - 28/11/2025 14:45

- **Tipo:** Release
- **Versão:** 2.1.0
- **Build Code:** 4
- **Arquivo:** InventarioMobile/app/build/outputs/apk/release/app-release.apk
- **Tamanho:** 8.74 MB
- **Assinatura:** inventario-release.keystore (válida até 2053)
- **Mudanças:** 
  - Configuração de keystore de produção
  - Removido applicationIdSuffix do debug
  - APK assinado para distribuição em produção
- **Status:** ✅ Sucesso

---

## Build #001 - 28/11/2025 14:30

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 4
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.24 MB
- **Mudanças:** Build inicial com registro de histórico
- **Status:** ✅ Sucesso
