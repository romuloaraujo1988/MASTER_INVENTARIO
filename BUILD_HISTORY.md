# Histórico de Builds - InventarioMobile

Este arquivo registra todas as compilações do APK Android para rastreabilidade.

---

## Build #016 - 02/12/2025 19:31

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 16
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.09 MB
- **Mudanças:** 
  - **Backend:** Removida limitação de 100 coletas no endpoint `/api/mobile/coletas/all`
  - **Backend:** Agora retorna TODAS as coletas do inventário
  - **Backend:** Adicionado método `contarTotalColetas()` no MobileColetaService
  - **Android:** Logs de diagnóstico no CollectionViewViewModelClean
- **Status:** ✅ Sucesso

---

## Build #015 - 02/12/2025 19:24

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 15
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11.4 MB
- **Mudanças:** 
  - **Diagnóstico do filtro de salas:**
    - Logs adicionais no CollectionViewViewModelClean para debug
    - Log do total de coletas recebidas do servidor
    - Log detalhado das salas extraídas
- **Status:** ✅ Sucesso

---

## Build #014 - 02/12/2025 18:30

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 14
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~12.4 MB
- **Mudanças:** 
  - **Correção do filtro de salas na tela "Itens Coletados":**
    - Novo endpoint `GET /api/mobile/coletas/salas-com-coletas` no backend
    - Novo Use Case `BuscarSalasComColetasUseCase` no Android
    - ViewModel atualizado para buscar salas do servidor (não mais limitado a 100 coletas)
    - Agora mostra TODAS as salas que possuem coletas registradas
  - **Backend:** Método `buscarSalasComColetas()` no MobileColetaService
- **Status:** ✅ Sucesso

---

## Build #013 - 02/12/2025 17:45

- **Tipo:** Debug (Clean Build)
- **Versão:** 2.1.0
- **Build Code:** 13
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~12.4 MB
- **Mudanças:** 
  - **Clean build completo** para garantir que todas as funcionalidades estejam incluídas
  - **Tela de Inventário com Abas:** InventarioTabsActivity com TabLayout
    - Aba "Por Responsável" - InventarioPorResponsavelFragment
    - Aba "Por Sala" - InventarioPorSalaFragment
  - Navegação via BottomNavigation e NavigationDrawer apontando para InventarioTabsActivity
- **Status:** ✅ Sucesso

---

## Build #012 - 02/12/2025 17:15

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 12
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 12.4 MB
- **Mudanças:** 
  - Atualização geral do APK para teste de carregamento de patrimônios por responsável
  - Verificação de funcionalidades implementadas anteriormente
- **Status:** ✅ Sucesso

---

## Build #011 - 02/12/2025 16:30

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 11
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Tela de Coletas - Melhoria de Performance:**
    - Spinner de salas: Trocado "Todas as Salas" por "Selecionar sala..."
    - Não carrega todas as coletas ao abrir - evita sobrecarga
    - Novo estado visual "Selecione uma sala" quando nenhuma sala está selecionada
    - Mensagem específica quando sala selecionada não tem coletas
  - **ViewModel:** Adicionado método `limparFiltroSala()` para gerenciar estado inicial
  - **Layout:** Adicionado `layoutSelectSala` para estado de seleção de sala
- **Status:** ✅ Sucesso

---

## Build #010 - 02/12/2025 16:00

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 10
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **IP padrão corrigido:** Alterado de `192.168.10.107` para `10.14.250.214`
  - **Lista de IPs sugeridos:** Adicionado `10.14.250.214` como primeira opção
  - **Removido IP incorreto:** `10.14.250.238` removido da lista de sugestões
- **Status:** ✅ Sucesso

---

## Build #009 - 02/12/2025 15:30

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 9
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção de tipos:** Incompatibilidades entre `Int` e `Long` nos modelos de domínio e data
  - **PatrimonioRepositoryAdapter:** Conversões `.toInt()` e `.toLong()` para compatibilidade
  - **RegistrarColetaUseCase:** Conversão `patrimonio.id.toLong()` para `patrimonioId`
  - **ManualCollectionViewModel:** Conversões de tipos corrigidas
  - **DescricaoSelectionViewModelClean:** Campos `idSetor` e `idSala` corrigidos
  - **SalaPagingSource:** Corrigido para usar `PagedResponse.content` ao invés de `ApiResponse.data`
  - **MobilePatrimonioDto:** Renomeado de `MobilePatrimonioDTO` para consistência
  - **PatrimonioMapper:** Tratamento de nulls com `?: 0L` e `?: ""`
  - **BaseOfflineActivity:** Removido `@AndroidEntryPoint` de classe abstrata
  - **Cores adicionadas:** `error_red`, `status_background_success`, `status_background_warning`
  - **Layout:** Adicionado `tvEndOfList` em `item_load_state.xml`
- **Status:** ✅ Sucesso

---

## Build #008 - 02/12/2025 10:15

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 8
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção crítica:** Endpoint `/api/mobile/coletas/all` agora retorna resposta paginada
  - **Novo DTO:** `ColetasPagedResponse.kt` para deserializar resposta paginada do servidor
  - **ApiService:** Atualizado tipo de retorno de `List<MobileColetaResponseDto>` para `ColetasPagedResponse`
  - **UseCases atualizados:**
    - `BuscarColetasComFallbackUseCase` - extrai coletas de `pagedResponse.content`
    - `BuscarColetasUseCase` - extrai coletas de `pagedResponse.content`
    - `SincronizarColetasDoServidorUseCase` - extrai coletas de `pagedResponse.content`
  - **ColetasViewModel:** Atualizado para processar resposta paginada
  - **MockApiService:** Atualizado para retornar `ColetasPagedResponse`
- **Status:** ✅ Sucesso

---

## Build #007 - 01/12/2025 23:50

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 7
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.2 MB
- **Mudanças:** 
  - **Pull-to-Refresh:** Adicionado SwipeRefreshLayout na tela de Itens Coletados
    - Usuário pode arrastar para baixo para atualizar a lista
    - Indicador visual de refresh com cores do tema
    - Integração com ViewModel para recarregar coletas
  - **Layout:** RecyclerView agora está dentro do SwipeRefreshLayout
- **Status:** ✅ Sucesso

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
