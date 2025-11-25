# Implementation Plan

## 1. Configurar estrutura base e modelos de domínio

- [x] 1.1 Criar/atualizar modelo de domínio Coleta
  - Campos já existem: descricaoPatrimonio, localizacaoEncontrada, estadoEncontrado, sincronizado, coletadoPor
  - Modelo está em `domain/model/Coleta.kt` e `data/model/Coleta.kt`
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 1.2 Criar enum StatusFiltro
  - Valores: TODOS, COLETADOS, PENDENTES
  - Localização: `domain/model/StatusFiltro.kt`
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 1.3 Criar sealed class ColetasState
  - Estados: Idle, Loading, Success, Error
  - Success deve conter: coletas, totalColetados, totalPendentes, filtros ativos
  - Localização: `presentation/coletas/ColetasState.kt`
  - _Requirements: 7.1, 7.2, 7.3_

## 2. Implementar camada de dados (Room)

- [x] 2.1 Atualizar ColetaEntity com campos necessários
  - Campos já existem: descricaoPatrimonio (via numeroPatrimonio), localizacaoEncontrada (via nomeSala), estadoEncontrado (via estadoPatrimonio), coletadoPor (via nomeUsuario), sincronizado
  - Índices já configurados: idUsuario, idSala, sincronizado
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [x] 2.2 Atualizar ColetaDao com queries necessárias
  - Query buscarTodas() já existe ordenada por dataColeta DESC
  - Query buscarPendentes() já existe (sincronizado = 0)
  - Query contarPendentes() já existe
  - _Requirements: 1.1, 2.1, 3.1, 6.1, 6.2_

- [x] 2.3 Adicionar queries faltantes no ColetaDao
  - Query buscarPorUsuario(usuarioId: Int)
  - Query buscarPorSala(salaId: Int)
  - Query contarSincronizadas()
  - _Requirements: 2.1, 3.1, 6.1_

- [x]* 2.4 Escrever testes unitários para ColetaDao
  - Testar buscarTodas retorna todas coletas
  - Testar buscarPorUsuario filtra corretamente
  - Testar contadores retornam valores corretos
  - _Requirements: 1.1, 2.1, 6.1, 6.2_

- [x] 2.5 Atualizar ColetaMapper
  - Mapper já existe em `data/mapper/ColetaMapper.kt`
  - Mapeia todos os campos Entity ↔ Domain
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

## 3. Implementar Use Cases

- [x] 3.1 Criar BuscarColetasUseCase
  - Já existe em `domain/usecase/BuscarColetasUseCase.kt`
  - Busca coletas do servidor via ApiService
  - Retorna Result<List<Coleta>>
  - _Requirements: 1.1_

- [x]* 3.2 Escrever teste de propriedade para BuscarColetasUseCase
  - **Property 1: Exibição completa de coletas**
  - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**

- [x] 3.3 Criar FiltrarColetasUseCase
  - Parâmetros: coletas, usuarioAtual, filtroUsuario, filtroSala, filtroStatus
  - Aplicar filtros com operação AND
  - Retornar lista filtrada
  - Localização: `domain/usecase/FiltrarColetasUseCase.kt`
  - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3, 8.1, 8.2, 8.3, 8.4_

- [x]* 3.4 Escrever testes de propriedade para FiltrarColetasUseCase
  - **Property 2: Filtro de usuário exclusivo**
  - **Validates: Requirements 2.1, 2.2**

- [x]* 3.5 Escrever teste de propriedade para filtro desativado
  - **Property 3: Filtro de usuário desativado mostra todos**
  - **Validates: Requirements 2.3**

- [x]* 3.6 Escrever teste de propriedade para filtro de sala
  - **Property 6: Filtro de sala específica**
  - **Validates: Requirements 3.1**

- [x]* 3.7 Escrever teste de propriedade para múltiplos filtros
  - **Property 8: Múltiplos filtros simultâneos (AND lógico)**
  - **Validates: Requirements 3.4, 4.5, 8.1, 8.2, 8.3, 8.4**

- [x]* 3.8 Escrever teste de propriedade para filtro de status
  - **Property 9: Filtro de status coletados**
  - **Property 10: Filtro de status pendentes**
  - **Validates: Requirements 4.1, 4.2**

- [x] 3.9 Criar AgruparColetasPorSalaUseCase
  - Agrupar coletas por nomeSala
  - Tratar sala null como "Sala não definida"
  - Ordenar grupos alfabeticamente
  - Localização: `domain/usecase/AgruparColetasPorSalaUseCase.kt`
  - _Requirements: 10.1, 10.2, 10.3, 10.5_

- [x]* 3.10 Escrever teste de propriedade para agrupamento
  - **Property 15: Agrupamento por sala**
  - **Validates: Requirements 10.1, 10.3, 10.5**

## 4. Implementar ViewModel

- [x] 4.1 Criar ColetasViewModelClean com @HiltViewModel
  - Injetar: BuscarColetasUseCase, FiltrarColetasUseCase, AgruparColetasPorSalaUseCase, ObterUsuarioAtualUseCase
  - StateFlow<ColetasState> para estado da UI
  - Variáveis para filtros ativos
  - Localização: `presentation/coletas/ColetasViewModelClean.kt`
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [x] 4.2 Implementar método carregarColetas()
  - Transicionar estado: Idle → Loading → Success/Error
  - Buscar coletas via UseCase
  - Aplicar filtros iniciais
  - Calcular contadores
  - _Requirements: 7.1, 7.2, 7.3, 6.1, 6.2_

- [x]* 4.3 Escrever teste de propriedade para estados de carregamento
  - **Property 13: Estados de carregamento**
  - **Validates: Requirements 7.1, 7.2, 7.3**

- [x] 4.4 Implementar método toggleFiltroUsuario()
  - Alternar filtro de usuário
  - Reaplicar filtros
  - Atualizar contadores
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x]* 4.5 Escrever teste de propriedade para atualização de filtros
  - **Property 4: Atualização imediata de filtros**
  - **Validates: Requirements 2.4**

- [x] 4.6 Implementar método aplicarFiltroSala(sala: String?)
  - Aplicar filtro de sala
  - Combinar com outros filtros ativos
  - Atualizar estado
  - _Requirements: 3.1, 3.2, 3.4_

- [x] 4.7 Implementar método aplicarFiltroStatus(status: StatusFiltro)
  - Aplicar filtro de status
  - Combinar com outros filtros ativos
  - Atualizar contadores
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x]* 4.8 Escrever teste de propriedade para contadores
  - **Property 5: Contadores refletem filtros**
  - **Property 12: Contadores sempre corretos**
  - **Validates: Requirements 2.5, 6.3, 6.1, 6.2, 6.4**

- [x] 4.9 Implementar método agruparPorSala()
  - Usar AgruparColetasPorSalaUseCase
  - Retornar Map<String, List<Coleta>>
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x]* 4.10 Escrever teste de propriedade para contagem por grupo
  - **Property 16: Contagem de itens por grupo**
  - **Validates: Requirements 10.4**

## 5. Checkpoint - Verificar testes

- [x] 5. Checkpoint - Verificar testes


  - Ensure all tests pass, ask the user if questions arise.

## 6. Implementar camada de apresentação (UI)

- [x] 6.1 Criar layout activity_coletas.xml
  - Layout já existe com RecyclerView (ViewPager2), Tabs, contadores
  - ProgressBar para loading (via empty view)
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 6.1, 6.2, 7.1_

- [x] 6.2 Criar layout item_coleta.xml
  - Layout já existe com descrição, localização, status de sincronização
  - Indicador visual de sincronização (Chip)
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 6.3 Criar ColetasAdapter com DiffUtil
  - ViewHolder com binding para item_coleta
  - DiffUtil.ItemCallback para atualizações eficientes
  - Método para atualizar lista
  - Substituir uso de PatrimonioAdapter
  - _Requirements: 7.4, 7.5_

- [x]* 6.4 Escrever teste de propriedade para indicadores visuais
  - **Property 14: Indicadores de sincronização**
  - **Validates: Requirements 9.1, 9.2, 9.3, 9.4**

- [x] 6.5 Atualizar ColetasActivity com @AndroidEntryPoint
  - Adicionar @AndroidEntryPoint
  - Injetar ColetasViewModelClean via viewModels()
  - Setup de observers para state
  - Setup de listeners para filtros
  - Método updateUI(state: ColetasState)
  - Criado ColetasActivityClean.kt com layout activity_coletas_clean.xml
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 7.1, 7.2, 7.3_

- [x] 6.6 Implementar lógica de filtros na UI
  - Chip/Toggle para filtro de usuário (já existe menu)
  - Spinner/Dropdown para filtro de sala
  - ChipGroup para filtro de status (Todos, Coletados, Pendentes)
  - _Requirements: 2.1, 2.3, 3.1, 3.2, 4.1, 4.2, 4.3_

- [x] 6.7 Implementar empty states
  - Mensagem quando não há coletas (já existe emptyView)
  - Mensagem quando filtros não retornam resultados
  - Botão para limpar filtros
  - _Requirements: 3.5, 6.5_

- [x]* 6.8 Escrever teste de propriedade para remoção de filtros
  - **Property 17: Remoção de filtros expande resultados**
  - **Validates: Requirements 8.5**

- [x]* 6.9 Escrever teste de propriedade para informações detalhadas
  - **Property 11: Informações detalhadas completas**
  - **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**

## 7. Implementar visualização agrupada

- [x] 7.1 Criar layout item_coleta_header.xml
  - TextView para nome da sala
  - TextView para quantidade de itens no grupo
  - _Requirements: 10.3, 10.4_

- [x] 7.2 Atualizar ColetasAdapter para suportar headers
  - Sealed class para tipos de item (Header, Coleta)
  - ViewHolder para header
  - Lógica para inserir headers entre grupos
  - _Requirements: 10.1, 10.3, 10.4_

- [x] 7.3 Implementar toggle de visualização agrupada
  - Switch/Toggle na toolbar
  - Alternar entre lista simples e agrupada
  - _Requirements: 10.1_

- [x]* 7.4 Escrever teste de propriedade para sala não definida
  - **Property 7: Sem filtro de sala mostra todas**
  - **Validates: Requirements 3.2**

## 8. Integração e navegação

- [x] 8.1 Registrar ColetasActivity no AndroidManifest.xml
  - Activity já está registrada
  - _Requirements: 1.1_

- [x] 8.2 Adicionar navegação para ColetasActivity
  - Navegação já existe no app
  - _Requirements: 1.1_

- [x] 8.3 Configurar módulo Hilt para novos componentes
  - Hilt já está configurado no projeto
  - Use Cases são injetados automaticamente via @Inject
  - _Requirements: 1.1_

## 9. Configurar Property-Based Testing

- [x] 9.1 Adicionar dependências Kotest no build.gradle
  - Adicionar kotest-runner-junit5
  - Adicionar kotest-assertions-core
  - Adicionar kotest-property
  - _Requirements: Testing Strategy_

- [x] 9.2 Criar generators (Arb) para modelos de teste
  - Arb.coleta() para gerar coletas aleatórias
  - Arb.statusFiltro() para gerar status
  - Localização: `test/java/com/inventario/mobile/generators/ColetaGenerators.kt`
  - _Requirements: Testing Strategy_

## 10. Checkpoint Final

- [x] 10. Checkpoint Final - Verificar todos os testes









  - Ensure all tests pass, ask the user if questions arise.
