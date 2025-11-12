# Implementation Plan - Gráficos Android

## Fase 1: Configuração e Dependências

- [ ] 1.1 Adicionar dependência MPAndroidChart
  - Adicionar no `build.gradle (app)`: `implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'`
  - Adicionar repositório Maven no `settings.gradle`: `maven { url 'https://jitpack.io' }`
  - Sync do Gradle
  - _Requirements: Todos_

- [ ] 1.2 Criar estrutura de pacotes
  - Criar `presentation/dashboard/`
  - Criar `domain/usecase/dashboard/`
  - Criar `domain/model/dashboard/`
  - Criar `data/repository/dashboard/`
  - _Requirements: Todos_

## Fase 2: Domain Layer - Models

- [ ] 2.1 Criar DashboardStats
  - Criar `domain/model/DashboardStats.kt`
  - Propriedades: totalPatrimonios, coletados, pendentes, percentualConclusao
  - Métodos computados: percentualColetados, percentualPendentes
  - _Requirements: 1.1, 1.2_

- [ ] 2.2 Criar ColetaPorDia
  - Criar `domain/model/ColetaPorDia.kt`
  - Propriedades: data, quantidade, dataFormatada
  - _Requirements: 3.1, 3.2_

- [ ] 2.3 Criar DistribuicaoPorSetor
  - Criar `domain/model/DistribuicaoPorSetor.kt`
  - Propriedades: idSetor, nomeSetor, quantidade, percentual
  - _Requirements: 6.1, 6.2_

- [ ] 2.4 Criar RankingColetor
  - Criar `domain/model/RankingColetor.kt`
  - Propriedades: idUsuario, nomeUsuario, quantidadeColetas, posicao
  - _Requirements: 5.1, 5.2_

- [ ] 2.5 Criar StatusPorSala
  - Criar `domain/model/StatusPorSala.kt`
  - Propriedades: idSala, nomeSala, coletados, pendentes, total
  - _Requirements: 7.1, 7.2_

## Fase 3: Domain Layer - Repository Interface

- [ ] 3.1 Criar DashboardRepository interface
  - Criar `domain/repository/DashboardRepository.kt`
  - Método: `obterEstatisticas(idInventario: Int?): Result<DashboardStats>`
  - Método: `obterColetasPorDia(dias: Int): Result<List<ColetaPorDia>>`
  - Método: `obterDistribuicaoPorSetor(): Result<List<DistribuicaoPorSetor>>`
  - Método: `obterRankingColetores(limite: Int): Result<List<RankingColetor>>`
  - Método: `obterStatusPorSala(): Result<List<StatusPorSala>>`
  - _Requirements: 1.1, 3.1, 6.1, 5.1, 7.1_

## Fase 4: Domain Layer - Use Cases

- [ ] 4.1 Criar ObterEstatisticasUseCase
  - Criar `domain/usecase/ObterEstatisticasUseCase.kt`
  - Injetar DashboardRepository
  - Validar parâmetros
  - Retornar Result<DashboardStats>
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 4.2 Criar ObterColetasPorDiaUseCase
  - Criar `domain/usecase/ObterColetasPorDiaUseCase.kt`
  - Validar dias (1-30)
  - Retornar Result<List<ColetaPorDia>>
  - _Requirements: 3.1, 3.2_

- [ ] 4.3 Criar ObterDistribuicaoPorSetorUseCase
  - Criar `domain/usecase/ObterDistribuicaoPorSetorUseCase.kt`
  - Retornar Result<List<DistribuicaoPorSetor>>
  - _Requirements: 6.1, 6.2_

- [ ] 4.4 Criar ObterRankingColetoresUseCase
  - Criar `domain/usecase/ObterRankingColetoresUseCase.kt`
  - Validar limite (1-50)
  - Retornar Result<List<RankingColetor>>
  - _Requirements: 5.1, 5.2_

- [ ] 4.5 Criar ObterStatusPorSalaUseCase
  - Criar `domain/usecase/ObterStatusPorSalaUseCase.kt`
  - Retornar Result<List<StatusPorSala>>
  - _Requirements: 7.1, 7.2_

## Fase 5: Data Layer - API

- [ ] 5.1 Criar DashboardApi interface
  - Criar `data/remote/api/DashboardApi.kt`
  - Endpoint: `@GET("api/mobile/dashboard/stats")`
  - Endpoint: `@GET("api/mobile/dashboard/coletas-evolucao")`
  - Endpoint: `@GET("api/mobile/dashboard/distribuicao-setor")`
  - Endpoint: `@GET("api/mobile/dashboard/ranking-coletores")`
  - _Requirements: 1.1, 3.1, 6.1, 5.1_

- [ ] 5.2 Adicionar DashboardApi no ApiModule
  - Editar `di/ApiModule.kt`
  - Adicionar provider para DashboardApi
  - _Requirements: Todos_

## Fase 6: Data Layer - Local Storage

- [ ] 6.1 Criar DashboardEntity
  - Criar `data/local/entity/DashboardEntity.kt`
  - Tabela: "dashboard_cache"
  - Campos: id, totalPatrimonios, coletados, pendentes, percentualConclusao, ultimaAtualizacao
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 6.2 Criar DashboardDao
  - Criar `data/local/dao/DashboardDao.kt`
  - Método: `salvarEstatisticas(entity: DashboardEntity)`
  - Método: `obterEstatisticas(): DashboardEntity?`
  - Método: `limparCache()`
  - _Requirements: 8.1, 8.2_

- [ ] 6.3 Adicionar DashboardDao no AppDatabase
  - Editar `data/local/database/AppDatabase.kt`
  - Adicionar entity DashboardEntity
  - Adicionar método `dashboardDao(): DashboardDao`
  - Incrementar versão do banco
  - _Requirements: 8.1_

## Fase 7: Data Layer - Repository Implementation

- [ ] 7.1 Criar DashboardRepositoryImpl
  - Criar `data/repository/DashboardRepositoryImpl.kt`
  - Injetar DashboardApi, DashboardDao, Context
  - Implementar offline-first pattern
  - Adicionar @Singleton
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 7.2 Implementar obterEstatisticas
  - Tentar API primeiro (se online)
  - Salvar no cache
  - Fallback para cache local
  - Retornar Result
  - _Requirements: 1.1, 1.3, 8.1, 8.2_

- [ ] 7.3 Implementar obterColetasPorDia
  - Buscar da API
  - Cache opcional
  - Fallback para dados locais
  - _Requirements: 3.1, 8.1_

- [ ] 7.4 Implementar obterDistribuicaoPorSetor
  - Buscar da API
  - Fallback para cálculo local
  - _Requirements: 6.1, 8.1_

- [ ] 7.5 Adicionar binding no RepositoryModule
  - Editar `di/RepositoryModule.kt`
  - Adicionar @Binds para DashboardRepository
  - _Requirements: Todos_

## Fase 8: Presentation Layer - State

- [ ] 8.1 Criar DashboardState
  - Criar `presentation/dashboard/DashboardState.kt`
  - Sealed class com: Idle, Loading, Success, Error
  - Success contém: stats, coletasPorDia, distribuicaoPorSetor, isOffline
  - Error contém: message, canRetry
  - _Requirements: 1.4, 1.5, 8.4_

## Fase 9: Presentation Layer - ViewModel

- [ ] 9.1 Criar DashboardViewModel
  - Criar `presentation/dashboard/DashboardViewModel.kt`
  - Adicionar @HiltViewModel
  - Injetar Use Cases
  - StateFlow<DashboardState>
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 9.2 Implementar carregarDashboard
  - Método: `carregarDashboard(idInventario: Int?)`
  - Carregar dados em paralelo (async/await)
  - Atualizar state conforme progresso
  - Tratar erros
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 9.1_

- [ ] 9.3 Implementar retry
  - Método: `retry()`
  - Recarregar dados
  - _Requirements: 1.4_

- [ ] 9.4 Implementar atualizarPeriodo
  - Método: `atualizarPeriodo(dias: Int)`
  - Recarregar coletas por dia
  - _Requirements: 3.5_

## Fase 10: Presentation Layer - UI Helper

- [ ] 10.1 Criar ChartHelper
  - Criar `presentation/dashboard/ChartHelper.kt`
  - Object com métodos estáticos
  - _Requirements: 2.5, 3.3, 4.3, 9.3, 10.3_

- [ ] 10.2 Implementar configurePieChart
  - Método: `configurePieChart(chart: PieChart)`
  - Configurar cores, animações, legendas
  - _Requirements: 2.1, 2.2, 2.3, 2.5, 10.1_

- [ ] 10.3 Implementar configureBarChart
  - Método: `configureBarChart(chart: BarChart)`
  - Configurar eixos, grid, animações
  - _Requirements: 3.1, 3.2, 3.3, 3.5, 10.1_

- [ ] 10.4 Implementar configureLineChart
  - Método: `configureLineChart(chart: LineChart)`
  - Configurar linha, pontos, animações
  - _Requirements: 4.1, 4.2, 4.3, 10.1_

- [ ] 10.5 Implementar configureHorizontalBarChart
  - Método: `configureHorizontalBarChart(chart: HorizontalBarChart)`
  - Configurar para ranking
  - _Requirements: 5.1, 5.2, 5.3, 10.1_

## Fase 11: Presentation Layer - Layouts

- [ ] 11.1 Criar fragment_dashboard.xml
  - ScrollView com cards
  - Cards de estatísticas (4 cards)
  - PieChart para status
  - BarChart para coletas por dia
  - PieChart para distribuição por setor
  - ProgressBar para loading
  - TextView para erro
  - _Requirements: 1.1, 2.1, 3.1, 6.1, 10.2_

- [ ] 11.2 Criar card_stat.xml
  - Layout reutilizável para cards de estatísticas
  - Icon, título, valor, subtítulo
  - _Requirements: 1.1, 10.2_

- [ ] 11.3 Criar layout_empty_state.xml
  - Ilustração
  - Mensagem
  - Botão de ação
  - _Requirements: 2.4, 10.5_

- [ ] 11.4 Criar layout_error_state.xml
  - Ícone de erro
  - Mensagem
  - Botão retry
  - _Requirements: 1.4, 10.5_

## Fase 12: Presentation Layer - Fragment

- [ ] 12.1 Criar DashboardFragment
  - Criar `presentation/dashboard/DashboardFragment.kt`
  - Adicionar @AndroidEntryPoint
  - ViewBinding
  - Injetar ViewModel
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 12.2 Implementar setupObservers
  - Observar viewModel.state
  - Atualizar UI conforme estado
  - _Requirements: 1.5, 10.3_

- [ ] 12.3 Implementar handleSuccess
  - Atualizar cards de estatísticas
  - Configurar gráficos
  - Indicar modo offline se necessário
  - _Requirements: 1.1, 1.2, 8.4_

- [ ] 12.4 Implementar setupPieChart
  - Criar entries para PieChart
  - Configurar cores (verde/vermelho)
  - Aplicar configurações do ChartHelper
  - Animar entrada
  - _Requirements: 2.1, 2.2, 2.3, 2.5_

- [ ] 12.5 Implementar setupBarChart
  - Criar entries para BarChart
  - Configurar eixo X com datas
  - Aplicar configurações do ChartHelper
  - Animar entrada
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 12.6 Implementar setupDistributionChart
  - Criar entries para PieChart de setores
  - Agrupar setores pequenos em "Outros" (se > 5)
  - Configurar cores distintas
  - Adicionar listener de toque
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 12.7 Implementar handleError
  - Exibir layout de erro
  - Configurar botão retry
  - _Requirements: 1.4, 10.5_

- [ ] 12.8 Implementar showLoading/hideLoading
  - Exibir/ocultar ProgressBar
  - Skeleton loading (opcional)
  - _Requirements: 10.5_

## Fase 13: Telas Adicionais

- [ ] 13.1 Criar EvolucaoFragment (gráfico de linha)
  - Layout com LineChart
  - ViewModel reutilizando Use Cases
  - Mostrar evolução do percentual
  - Linha de meta (opcional)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 13.2 Criar RankingFragment (barras horizontais)
  - Layout com HorizontalBarChart
  - ViewModel com ObterRankingColetoresUseCase
  - Destacar usuário atual
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 13.3 Criar StatusSalasFragment (barras empilhadas)
  - Layout com StackedBarChart
  - ViewModel com ObterStatusPorSalaUseCase
  - Scroll horizontal
  - Filtros por andar/bloco
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

## Fase 14: Navegação

- [ ] 14.1 Adicionar DashboardFragment no nav_graph
  - Editar `res/navigation/nav_graph.xml`
  - Adicionar fragment
  - Adicionar ações de navegação
  - _Requirements: Todos_

- [ ] 14.2 Adicionar item no menu principal
  - Editar menu de navegação
  - Ícone de dashboard
  - Navegar para DashboardFragment
  - _Requirements: Todos_

## Fase 15: Backend - Endpoints

- [ ] 15.1 Criar DashboardStatsDTO (Java)
  - Criar `src/main/java/com/inventario/mobile/server/dto/DashboardStatsDTO.java`
  - Campos: totalPatrimonios, coletados, pendentes, percentualConclusao
  - _Requirements: 1.1_

- [ ] 15.2 Criar MobileDashboardController
  - Criar `src/main/java/com/inventario/mobile/server/controller/MobileDashboardController.java`
  - Endpoint: `GET /api/mobile/dashboard/stats`
  - Endpoint: `GET /api/mobile/dashboard/coletas-evolucao`
  - Endpoint: `GET /api/mobile/dashboard/distribuicao-setor`
  - _Requirements: 1.1, 3.1, 6.1_

- [ ] 15.3 Criar MobileDashboardService
  - Criar `src/main/java/com/inventario/mobile/server/service/MobileDashboardService.java`
  - Método: `obterEstatisticas(Integer idInventario)`
  - Método: `obterColetasPorDia(int dias)`
  - Método: `obterDistribuicaoPorSetor()`
  - Usar DAOs existentes
  - _Requirements: 1.1, 3.1, 6.1_

## Fase 16: Testes

- [ ] 16.1 Testes de Use Cases
  - Testar ObterEstatisticasUseCase
  - Testar ObterColetasPorDiaUseCase
  - Testar validações
  - _Requirements: 9.1_

- [ ] 16.2 Testes de Repository
  - Testar offline-first
  - Testar fallback para cache
  - Testar sincronização
  - _Requirements: 8.1, 8.2, 8.3, 9.1_

- [ ] 16.3 Testes de ViewModel
  - Testar estados
  - Testar carregamento paralelo
  - Testar tratamento de erros
  - _Requirements: 9.1_

## Fase 17: Otimizações

- [ ] 17.1 Implementar cache de cálculos
  - Cache de percentuais
  - Cache de agregações
  - _Requirements: 9.2_

- [ ] 17.2 Implementar debounce
  - Evitar múltiplas requisições
  - Usar Flow.debounce
  - _Requirements: 9.2_

- [ ] 17.3 Otimizar queries do banco
  - Índices nas tabelas
  - Queries otimizadas
  - _Requirements: 9.1, 9.2_

## Fase 18: Acessibilidade e UX

- [ ] 18.1 Adicionar content descriptions
  - Gráficos
  - Botões
  - Cards
  - _Requirements: 10.1, 10.2_

- [ ] 18.2 Implementar skeleton loading
  - Shimmer effect
  - Placeholder para gráficos
  - _Requirements: 10.5_

- [ ] 18.3 Adicionar feedback visual
  - Ripple effects
  - Animações de transição
  - _Requirements: 10.3_

- [ ] 18.4 Validar contraste de cores
  - Verificar acessibilidade
  - Ajustar cores se necessário
  - _Requirements: 10.1_

## Fase 19: Documentação

- [ ] 19.1 Documentar Use Cases
  - KDoc em cada Use Case
  - Exemplos de uso
  - _Requirements: Todos_

- [ ] 19.2 Documentar ViewModel
  - KDoc no ViewModel
  - Documentar estados
  - _Requirements: Todos_

- [ ] 19.3 Atualizar README
  - Adicionar seção de gráficos
  - Screenshots
  - _Requirements: Todos_

## Fase 20: Validação Final

- [ ] 20.1 Testar offline
  - Desconectar rede
  - Verificar cache
  - Verificar indicador offline
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 20.2 Testar performance
  - Medir tempo de carregamento
  - Verificar animações (60fps)
  - Profiling de memória
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 20.3 Testar em diferentes dispositivos
  - Diferentes tamanhos de tela
  - Diferentes versões Android
  - _Requirements: 10.1, 10.2_

- [ ] 20.4 Code review
  - Revisar arquitetura
  - Revisar nomenclatura
  - Revisar tratamento de erros
  - _Requirements: Todos_
