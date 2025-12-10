# Histórico de Builds - InventarioMobile

Este arquivo registra todas as compilações do APK Android para rastreabilidade.

---

## Build #045 - 09/12/2025 22:30

- **Tipo:** Debug
- **Versão:** 2.7.0 (Build 45)
- **Build Code:** 45
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~19.5 MB
- **Mudanças:** 
  - **Fix: Rolagem Infinita na Tela "Por Sala"**
    - **Backend:** Novo método `buscarPorSalaComPaginacao()` no `PatrimonioDAO.java`
      - Usa `LIMIT/OFFSET` no SQL para paginação eficiente no banco
      - Evita carregar todos os patrimônios em memória
    - **Backend:** `MobilePatrimonioService.buscarPorSalaComFiltro()` refatorado
      - Sem filtro de coleta: usa paginação direta no banco (mais eficiente)
      - Com filtro de coleta: busca em lotes para evitar sobrecarga de memória
    - **Android:** `InventarioPorSalaFragment.setupInfiniteScroll()` corrigido
      - Detecção correta do NestedScrollView
      - Logs de debug para diagnóstico
      - Threshold aumentado para 500px
    - **Android:** `InventarioPorSalaViewModel.carregarMaisPatrimonios()` com logs detalhados
  - **Problema resolvido:** Lista travava nos primeiros itens ao rolar
- **Status:** ✅ Sucesso

---

## Build #044 - 09/12/2025 21:45

- **Tipo:** Debug
- **Versão:** 2.7.0 (Build 44)
- **Build Code:** 44
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 19.5 MB
- **Mudanças:** 
  - **Feature: Vibração ao Coletar Patrimônio**
    - Nova opção nas Configurações: "Vibrar ao coletar"
    - `VibrationHelper.kt` - Utilitário para gerenciar vibração do dispositivo
    - `PreferencesManager.kt` - Métodos `isVibrationOnCollectionEnabled()` e `setVibrationOnCollectionEnabled()`
    - `UtilModule.kt` - Provider para VibrationHelper via Hilt
    - `activity_settings.xml` - Novo card "Feedback de Coleta" com switch de vibração
    - `SettingsActivity.kt` - Handler para switch de vibração
    - **ViewModels atualizados para vibrar ao coletar:**
      - `ColetaViewModelClean.kt` - Vibra em `registrarColeta()` e `registrarColetaComMetricas()`
      - `ScannerViewModel.kt` - Vibra em `coletarPatrimonioComEstado()`
      - `ManualCollectionViewModel.kt` - Vibra em `coletarPatrimonio()`
      - `ItemSemEtiquetaViewModel.kt` - Vibra em `registrarItemSemEtiqueta()`
    - **Factories atualizados:**
      - `ScannerViewModelFactory.kt` - Recebe VibrationHelper
      - `ManualCollectionViewModelFactory.kt` - Recebe VibrationHelper
  - **Benefícios:**
    - Feedback tátil confirma coleta bem-sucedida
    - Configurável pelo usuário (habilitado por padrão)
    - Vibração curta (50ms) não intrusiva
- **Status:** ✅ Sucesso

---

## Build #043 - 09/12/2025 20:15

- **Tipo:** Debug
- **Versão:** 2.7.0
- **Build Code:** 42
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~18 MB
- **Mudanças:** 
  - Versão atualizada para 2.7.0 (sincronizado com backend)
  - SplashScreen agora exibe versão dinamicamente via BuildConfig
  - String `app_version` agora usa placeholder `%1$s`
- **Compatibilidade:** ✅ Compatível com servidor 2.7.0 (RBAC)
- **Status:** ✅ Sucesso

---

## Build #042 - 09/12/2025 20:00

- **Tipo:** Debug
- **Versão:** 2.6.0
- **Build Code:** 41
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~18 MB
- **Mudanças:** 
  - Recompilação para teste com backend v2.7.0 (segurança por roles)
  - Nenhuma alteração no código do app Android
- **Compatibilidade:** ✅ Compatível com servidor 2.7.0 (RBAC)
- **Status:** ✅ Sucesso

---

## Build #041 - 09/12/2025 13:00

- **Tipo:** Release (Produção) ✅
- **Versão:** 2.6.0
- **Build Code:** 41
- **Arquivo:** InventarioMobile/app/build/outputs/apk/release/app-release.apk
- **Tamanho:** 15.26 MB
- **Assinatura:** inventario-release.keystore (válida até 2053)
- **Mudanças desta build:** 
  - **Fix: Exportação não encontrava patrimônios da sala**
  - **Fix: Salas não carregavam no spinner da tela de Exportação**
  - **Fix: Tela "Sobre" agora usa BuildConfig dinamicamente**
- **Versionamento Semântico 2.6.0:**
  - `2` = Arquitetura Clean Architecture + MVVM + Hilt
  - `6` = Sexta atualização de features desde 2.0:
    - 2.1: Dark Mode
    - 2.2: Informações do desenvolvedor (splash/settings)
    - 2.3: Gráficos de Dashboard (Estado, Top 10, Distribuição)
    - 2.4: Filtro "Sem Etiqueta" na tela de coletas
    - 2.5: Busca Rápida de Patrimônio + Inventário por Sala com Abas
    - 2.6: Exportação de Relatórios (PDF, Excel, CSV)
  - `0` = Versão estável, sem patches pendentes
- **Status:** ✅ Sucesso - Pronto para produção

---

## Build #038 - 09/12/2025 10:45

- **Tipo:** Debug
- **Versão:** 1.2.34
- **Build Code:** 34
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~18 MB
- **Mudanças:** 
  - **Fix: Rolagem Infinita na Tela de Inventário por Sala (Correção)**
    - Movido scroll listener do `RecyclerView` para o `NestedScrollView`
    - O layout usa `NestedScrollView` como container principal com `RecyclerView` interno
    - O `RecyclerView` tem `nestedScrollingEnabled="false"`, então o scroll é do pai
    - Agora detecta corretamente quando o usuário chega ao final da lista
    - Carrega mais patrimônios automaticamente (threshold de 300px antes do final)
- **Status:** ✅ Sucesso

---

## Build #037 - 09/12/2025 10:30

- **Tipo:** Debug
- **Versão:** 1.2.34
- **Build Code:** 34
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~18 MB
- **Mudanças:** 
  - **Fix: Rolagem Infinita na Tela de Inventário por Sala (Tentativa 1)**
    - Adicionado `OnScrollListener` no `RecyclerView` do `InventarioPorSalaFragment.kt`
    - Problema: O scroll listener estava no componente errado
- **Status:** ⚠️ Parcial (corrigido na build #038)

---

## Build #036 - 08/12/2025 16:07

- **Tipo:** Debug
- **Versão:** 1.2.34
- **Build Code:** 34
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 18.18 MB
- **Mudanças:** 
  - **Feature: Exportação de Relatórios (PDF, Excel, CSV)**
    - `ExportFormat.kt` - Enum para formatos de exportação (PDF, Excel, CSV)
    - `ExportFilter.kt` - Enum para filtros (TODOS, COLETADOS, NAO_COLETADOS)
    - `ExportResult.kt` - Modelo de resultado da exportação
    - `ExportRepository.kt` - Interface do repositório de exportação
    - `ExportRepositoryImpl.kt` - Implementação com geração de PDF, Excel (TSV), CSV
    - `ExcelGenerator.kt` - Gerador de arquivos TSV (compatível com Excel, sem Apache POI)
    - `CsvGenerator.kt` - Gerador de arquivos CSV
    - `GerarRelatorioUseCase.kt` - Use Case para gerar relatórios em múltiplos formatos
    - `BuscarSalasParaExportacaoUseCase.kt` - Use Case para buscar salas disponíveis
    - `ExportState.kt` - Sealed class para estados da UI
    - `ExportViewModel.kt` - ViewModel com `@HiltViewModel`
    - `ExportFragment.kt` - Fragment completo com seleção de sala, filtros e formatos
    - `fragment_statistics_export.xml` - Layout completo com cards de seleção
    - `SalaFilterAdapter.kt` - Adapter para dropdown de salas
    - `ic_excel.xml`, `ic_csv.xml` - Ícones para formatos de exportação
    - `RepositoryModule.kt` - Binding para ExportRepository
  - **Nota:** Excel usa formato TSV (Tab-Separated Values) pois Apache POI requer minSdk 26
- **Status:** ✅ Sucesso

---

## Build #035 - 07/12/2025 15:45

- **Tipo:** Release (Produção)
- **Versão:** 1.2.34
- **Build Code:** 34
- **Arquivo:** InventarioMobile/app/build/outputs/apk/release/app-release.apk
- **Tamanho:** 8.85 MB
- **Assinatura:** inventario-release.keystore (válida até 2053)
- **Mudanças:** 
  - Build de produção para evitar alerta de "app perigoso" no Android
  - APK assinado com keystore de release
  - Mesmo código da build #034, apenas em modo release
- **Status:** ✅ Sucesso

---

## Build #034 - 07/12/2025 08:57

- **Tipo:** Debug
- **Versão:** 1.2.34
- **Build Code:** 34
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.15 MB
- **Mudanças:** 
  - Build de rotina solicitada pelo usuário
  - Incremento de versão: 1.2.33 → 1.2.34
- **Status:** ✅ Sucesso

---

## Build #033 - 07/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.33
- **Build Code:** 33
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção FINAL: Filtro "Sem Etiqueta" na tela ColetasActivityClean**
    - `FiltrarColetasUseCase.kt`: Corrigida lógica do filtro SEM_ETIQUETA
      - Antes: usava `numeroPatrimonio.isNullOrBlank() && !descricaoPatrimonio.isNullOrBlank()` (ERRADO)
      - Depois: usa `semEtiqueta || (numeroPatrimonio.isNullOrBlank() && !descricaoItemSemEtiqueta.isNullOrBlank())` (CORRETO)
    - O filtro agora usa o campo correto `semEtiqueta` do modelo Coleta
- **Status:** ✅ Sucesso

---

## Build #032 - 07/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.32
- **Build Code:** 32
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção FINAL: Filtro "Sem Etiqueta" funcionando corretamente**
    - `CollectionViewActivity.kt`: Filtro "Sem Etiqueta" agora funciona sem exigir seleção de sala
    - `CollectionViewViewModelClean.kt`: 
      - `filtrarPorStatus()`: Quando SEM_ETIQUETA é selecionado, limpa filtro de sala automaticamente
      - `limparFiltroSala()`: Mantém filtro SEM_ETIQUETA ativo mesmo sem sala selecionada
    - `CollectionAdapter.kt`: Exibe corretamente descrição e categoria de itens sem etiqueta
      - Mostra "🏷️ SEM ETIQUETA" como número do patrimônio
      - Exibe `descricaoItemSemEtiqueta` e `categoriaItemSemEtiqueta` na descrição
- **Status:** ✅ Sucesso

---

## Build #031 - 07/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.31
- **Build Code:** 31
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção: Filtro "Sem Etiqueta" na tela de Coletas**
    - `BuscarColetasUseCase.kt`: Adicionados logs de debug para campos `semEtiqueta`, `descricaoItemSemEtiqueta`, `categoriaItemSemEtiqueta`
    - `CollectionViewViewModelClean.kt`: Adicionados logs detalhados no filtro `SEM_ETIQUETA` para diagnóstico
    - Contagem de itens sem etiqueta antes do filtro
    - Debug de coletas com flag `semEtiqueta=true`
  - **Correções anteriores na sessão:**
    - `AndroidManifest.xml`: Corrigido package da FiltrosActivity (`.presentation.filtros` ao invés de `.presentation.inventario`)
    - `navigation_drawer_menu.xml`: Removido item duplicado "Busca por Voz"
    - `MainActivity.kt`: Removido handler do menu "Busca por Voz"
    - `fragment_dashboard.xml`: Renomeado "Ações Rápidas" para "Resumo do Inventário" e "Ações de Coleta"
- **Status:** ✅ Sucesso

---

## Build #030 - 07/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.30
- **Build Code:** 30
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção: Gráfico de Estado de Conservação não recebia dados**
    - `ColetaDAO.java`: Corrigido para buscar `ESTADO_ENCONTRADO` ao invés de `STATUS_COLETA`
    - `ApiService.kt`: Corrigido endpoint de `dashboard/status` para `api/mobile/dashboard/status`
    - `DashboardRepositoryImpl.kt`: Implementado método `buscarEstatisticasPorStatus()` que estava retornando lista vazia
    - `MockApiService.kt`: Atualizado para incluir parâmetro `inventarioId`
  - **Feature: Implementado gráfico Top 10 Itens Coletados**
    - `DashboardRepositoryImpl.kt`: Implementado método `buscarTopItens()` conectando ao backend
  - **Feature: Implementado gráfico Distribuição por Sala**
    - `DashboardRepositoryImpl.kt`: Implementado método `buscarDistribuicaoPorSala()` conectando ao backend
- **Status:** ✅ Sucesso

---

## Build #029 - 06/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.29
- **Build Code:** 29
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.15 MB
- **Mudanças:** 
  - **Correção: Estatísticas não atualizavam na tela de Estatísticas**
    - `DashboardViewModel.refreshData()`: Agora invalida cache antes de recarregar
    - `ChartsFragment`: Adicionado SwipeRefreshLayout para pull-to-refresh
    - `ChartsFragment`: Usa inventário ativo do PreferencesManager quando idInventario=0
    - `ChartDataProvider.getProgressData()`: Passa null ao invés de 0 para usar inventário ativo
    - `ChartsViewModel.loadStatusData()`: Corrigido para passar idInventario
    - `fragment_charts.xml`: Adicionado SwipeRefreshLayout envolvendo o conteúdo
  - **Feature: Informações do desenvolvedor e versão**
    - Splash screen: Exibe versão (1.2.29)
    - Tela de Configurações: Nova seção "Sobre o Aplicativo" com:
      - Logo do app
      - Nome: SIHCP Mobile
      - Nome completo: Sistema de Histórico e Coleta Patrimonial
      - Versão: 1.2.29
      - Desenvolvedor: Romulo Araujo
      - Copyright: © 2025 - Todos os direitos reservados
    - `strings.xml`: Novas strings para versão, desenvolvedor e copyright
    - `activity_settings.xml`: Card "Sobre o Aplicativo" completo
- **Status:** ✅ Sucesso

---

## Build #028 - 05/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Feature: Dark Mode (Tema Escuro)**
    - Nova opção na tela de Configurações para escolher tema
    - 3 opções: Automático (seguir sistema), Claro, Escuro
    - `ThemeHelper.kt` - Helper para gerenciar tema do app
    - `PreferencesManager.kt` - Métodos para salvar/carregar preferência de tema
    - `SettingsViewModel.kt` - Métodos para aplicar tema
    - `SettingsActivity.kt` - RadioGroup para seleção de tema
    - `values-night/colors.xml` - Cores otimizadas para tema escuro
    - `values-night/themes.xml` - Tema escuro completo
    - `activity_settings.xml` - Seção "Aparência" com opções de tema
    - `strings.xml` - Strings para Dark Mode
    - `InventarioMobileApplication.kt` - Aplica tema salvo na inicialização
  - **Benefícios:**
    - Reduz fadiga visual em ambientes escuros
    - Economia de bateria em telas OLED
    - Segue preferência do sistema automaticamente
- **Status:** ✅ Sucesso

---

## Build #027 - 04/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção: App não respeitava biometria/PIN ao abrir**
    - `SplashActivity`: Agora verifica se biometria ou PIN está habilitado
    - Se habilitado, redireciona para `LoginActivity` com flag `require_local_auth`
    - `LoginActivity`: Trata flag e inicia autenticação automaticamente
    - Fluxo: Splash → verifica auth local → Login (biometria/PIN) → MainActivity
  - **Comportamento anterior:** App ia direto para MainActivity se token válido
  - **Comportamento novo:** Se biometria/PIN habilitado, exige autenticação local
- **Status:** ✅ Sucesso

---

## Build #026 - 04/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção: Tela de detalhes do patrimônio exibia erro**
    - `ObterDetalhePatrimonioUseCase`: Removida validação rígida de estados de conservação
    - Antes: Exigia estados "BOM", "REGULAR", "RUIM", "INUTILIZADO" (incorretos)
    - Agora: Aceita qualquer valor de estado (flexível)
    - Estados críticos atualizados: "IRRECUPERÁVEL", "ANTIECONÔMICO"
  - **Estados de conservação corretos do sistema:**
    - BOM, OCIOSO, ANTIECONÔMICO, RECUPERÁVEL, IRRECUPERÁVEL
  - **Feature: Long press para copiar número do patrimônio**
    - `PatrimonioSearchAdapter`: Adicionado `onLongClickListener`
    - Ao manter pressionado um item na busca rápida, copia o número para clipboard
    - Feedback tátil (vibração) ao copiar
    - Toast de confirmação "Número XXXXX copiado!"
- **Status:** ✅ Sucesso

---

## Build #025 - 04/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção de Coroutines na Busca Rápida:**
    - Adicionado tratamento de `CancellationException` no `BuscarPatrimoniosUseCase`
    - Adicionado tratamento de `CancellationException` no `QuickSearchViewModel`
    - Re-throw de `CancellationException` para não quebrar fluxo de coroutines
    - Logs informativos para cancelamentos (debounce/navegação)
    - Correção de tipo nullable em `temDivergencia`
  - **Benefícios:**
    - Evita erros de "Job was cancelled" durante debounce
    - Navegação entre telas não causa crashes
    - Melhor tratamento de lifecycle do ViewModel
- **Status:** ✅ Sucesso

---

## Build #024 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Busca Rápida com Servidor (Server-First):**
    - **Backend:** Novo endpoint `GET /api/mobile/patrimonio/buscar`
      - Parâmetros: `query`, `filtro` (ALL/COLETADOS/PENDENTES/DIVERGENCIAS), `inventarioId`, `limit`
      - Busca por número, descrição, sala ou responsável
      - Retorna status de coleta e divergências
    - **Backend:** Novo método `buscarPorQueryTexto()` no `PatrimonioDAO.java`
    - **Backend:** Novo método `buscarPorQuery()` no `MobilePatrimonioService.java`
    - **Backend:** Campo `temDivergencia` adicionado ao `MobilePatrimonioDTO.java`
    - **Android:** Endpoint `buscarPorQuery()` adicionado ao `PatrimonioApi.kt`
    - **Android:** `BuscarPatrimoniosUseCase` atualizado para usar servidor com fallback local
    - **Android:** Campo `temDivergencia` adicionado ao `MobilePatrimonioDto.kt`
  - **Estratégia:** Servidor primeiro, fallback para banco local se offline
- **Status:** ✅ Sucesso
- **Requer:** Nova versão do servidor (compilar com `mvnw compile`)

---

## Build #023 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.13 MB
- **Mudanças:** 
  - **Correção IP padrão:** Atualizado para `10.14.250.214` em todos os arquivos
    - `ServerValidator.kt` - Lista de IPs sugeridos e mensagem de ajuda
    - `NetworkDiagnosticActivity.kt` - IP de fallback para diagnóstico
- **Status:** ✅ Sucesso

---

## Build #022 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.13 MB
- **Mudanças:** 
  - **Link do menu para Busca Rápida:**
    - `MainActivity.handleNavigationItemSelected()` - Adicionado handler para `nav_quick_search`
    - Menu "Busca Rápida" agora abre `QuickSearchActivity`
    - Menu "Busca por Voz" abre `QuickSearchActivity` com busca por voz automática
    - `QuickSearchActivity.handleIntent()` - Suporte ao intent `START_VOICE_SEARCH`
- **Status:** ✅ Sucesso

---

## Build #021 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.21
- **Build Code:** 21
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** 11.13 MB
- **Mudanças:** 
  - **Atualização de versão:** 1.2.18 → 1.2.21
  - Feature Busca Rápida de Patrimônio completa
- **Status:** ✅ Sucesso

---

## Build #020 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.18
- **Build Code:** 18
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Feature: Busca Rápida de Patrimônio**
    - Nova `QuickSearchActivity` com busca por número, descrição ou sala
    - Nova `PatrimonioDetailActivity` para exibir detalhes completos do patrimônio
    - `QuickSearchViewModel` com debounce e filtros (coletados, pendentes, divergências)
    - `PatrimonioDetailViewModel` para carregar detalhes do patrimônio
    - `BuscarPatrimoniosUseCase` para busca com filtros
    - Queries no `PatrimonioDao`: `buscarPorQuery`, `buscarColetadosPorQuery`, `buscarPendentesPorQuery`, `buscarDivergenciasPorQuery`
    - Layouts: `activity_quick_search.xml`, `activity_patrimonio_detail.xml`
    - Adapter: `PatrimonioSearchAdapter` para lista de resultados
    - Indicador de última sincronização e alerta de dados desatualizados
    - Busca por voz integrada
  - **Correções:**
    - `PatrimonioDao.buscarDivergenciasPorQuery` - Corrigido nome da coluna `c.nomeSala` (era `c.localizacaoEncontrada`)
    - `PatrimonioRepositoryAdapter` - Adicionados métodos de busca rápida
    - `DatabaseModule` - Adicionado provider para `SincronizacaoDao`
    - Criado drawable `ic_visibility.xml`
    - Criado drawable `bg_warning.xml`
- **Status:** ✅ Sucesso

---

## Build #019 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.18
- **Build Code:** 18
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção de versão:** Atualizado versionName e versionCode no build.gradle
  - **Correção de versão na UI:** Atualizado strings.xml para exibir "Versão 1.2.18" na splash screen e login
- **Status:** ✅ Sucesso

---

## Build #018 - 03/12/2025

- **Tipo:** Debug
- **Versão:** 1.2.18
- **Build Code:** 18
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção: Exibição de dados de coleta no modo offline**
    - `LocalDataSourceStrategy.getPatrimonios()` - Agora usa campos da entity (coletadoPor, dataColeta, observacoesColeta)
    - `LocalDataSourceStrategy.buscarPorDescricaoNaoColetados()` - Mesma correção
    - `LocalDataSourceStrategy.buscarPorSala()` - Mesma correção
    - Formatação de data da coleta (dd/MM/yyyy HH:mm) em todos os métodos
    - Campos adicionais: marca, modelo, numeroSerie, valor, setorId, setorNome
  - **Resultado:** Scanner e Coleta Manual agora exibem corretamente os dados de itens já coletados
- **Status:** ✅ Sucesso

---

## Build #017 - 03/12/2025 

- **Tipo:** Debug
- **Versão:** 2.1.0
- **Build Code:** 17
- **Arquivo:** InventarioMobile/app/build/outputs/apk/debug/app-debug.apk
- **Tamanho:** ~11 MB
- **Mudanças:** 
  - **Correção crítica: Modo Offline no Scanner (Coleta Rápida)**
    - `ScannerViewModel` agora usa `BuscarPatrimonioUseCase` (Clean Architecture)
    - Busca de patrimônios funciona offline igual à coleta manual
    - Injeção do `BuscarPatrimonioUseCase` via Hilt na `ScannerActivity`
    - `ScannerViewModelFactory` atualizada para receber o novo Use Case
    - Método `convertToDataModel()` para converter domain → data model
  - **Unificação:** Scanner e Coleta Manual agora usam a mesma estratégia offline-first
  - **Logs:** Adicionados logs detalhados para debug do modo offline
- **Status:** ✅ Sucesso

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
