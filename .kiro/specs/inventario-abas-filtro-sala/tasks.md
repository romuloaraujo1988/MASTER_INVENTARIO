# Implementation Plan

## 1. Criar Domain Models

- [x] 1.1 Criar model SalaComProgresso
  - Criar arquivo `domain/model/SalaComProgresso.kt`
  - Incluir campos: id, nome, numero, totalPatrimonios, coletados, pendentes, percentualColeta
  - Adicionar propriedades computadas: isCompleta, isVazia, temPendentes
  - _Requirements: 2.1, 4.1_

- [x] 1.2 Criar model EstatisticasSala
  - Criar arquivo `domain/model/EstatisticasSala.kt`
  - Incluir campos: salaId, salaNome, totalPatrimonios, coletados, pendentes, percentualColeta, coletadosHoje, coletadosSemana
  - _Requirements: 2.1, 6.4_

- [x] 1.3 Criar enum FiltroColetaSala
  - Criar arquivo `domain/model/FiltroColetaSala.kt`
  - Valores: TODOS, COLETADOS, NAO_COLETADOS
  - Nota: Usar nome diferente de FiltroColeta existente em ColetasViewModel para evitar conflito
  - _Requirements: 3.1_

## 2. Atualizar Data Layer (DAOs e Entities)

- [x] 2.1 Criar entity SalaComEstatisticasEntity
  - Criar arquivo `data/local/entity/SalaComEstatisticasEntity.kt`
  - Usar @Embedded para SalaEntity
  - Adicionar campos total_patrimonios e coletados
  - _Requirements: 4.1_

- [x] 2.2 Adicionar queries no PatrimonioDao
  - Adicionar método `buscarPorSala(salaId, coletado, pageSize, offset)` com filtro opcional
  - Adicionar método `contarPorSala(salaId)` para total de patrimônios
  - Adicionar método `contarColetadosPorSala(salaId)` para patrimônios coletados
  - _Requirements: 1.3, 1.4_

- [x] 2.3 Adicionar queries no SalaDao
  - Adicionar método `buscarComEstatisticas()` com JOIN em patrimonio para contar totais
  - Adicionar método `buscarPorNomeOuNumero(query)` para busca combinada
  - _Requirements: 4.1, 4.4_

- [ ]* 2.4 Write property test for statistics calculation
  - **Property 2: Statistics Calculation Consistency**
  - **Validates: Requirements 1.4, 2.1**

## 3. Criar Mappers

- [x] 3.1 Criar SalaComProgressoMapper
  - Criar arquivo `data/mapper/SalaComProgressoMapper.kt`
  - Implementar função de extensão `SalaComEstatisticasEntity.toDomain()`
  - Calcular pendentes e percentual no mapper
  - _Requirements: 4.1_

- [ ]* 3.2 Write property test for progress display consistency
  - **Property 5: Room Progress Display Consistency**
  - **Validates: Requirements 4.1**

## 4. Atualizar Repository Interfaces e Implementações

- [x] 4.1 Atualizar PatrimonioRepository interface
  - Adicionar método `buscarPorSala(salaId, coletado, page, pageSize): Result<List<Patrimonio>>`
  - Adicionar método `contarPorSala(salaId): Result<Int>`
  - Adicionar método `contarColetadosPorSala(salaId): Result<Int>`
  - _Requirements: 1.3_

- [x] 4.2 Implementar métodos em PatrimonioRepositoryImpl
  - Implementar `buscarPorSala` com estratégia offline-first
  - Implementar `contarPorSala` e `contarColetadosPorSala`
  - _Requirements: 1.3, 5.1_

- [x] 4.3 Atualizar SalaRepository interface
  - Adicionar método `buscarComProgresso(): Result<List<SalaComProgresso>>`
  - Adicionar método `buscarPorNomeOuNumero(query): Result<List<Sala>>`
  - _Requirements: 4.1, 4.4_

- [x] 4.4 Implementar métodos em SalaRepositoryImpl
  - Implementar `buscarComProgresso` usando SalaDao
  - Implementar `buscarPorNomeOuNumero`
  - _Requirements: 4.1, 4.4_

## 5. Criar Use Cases

- [x] 5.1 Criar BuscarPatrimoniosPorSalaUseCase
  - Criar arquivo `domain/usecase/BuscarPatrimoniosPorSalaUseCase.kt`
  - Injetar PatrimonioRepository
  - Implementar invoke com parâmetros salaId, coletado, page, pageSize
  - _Requirements: 1.3_

- [x] 5.2 Criar BuscarEstatisticasSalaUseCase
  - Criar arquivo `domain/usecase/BuscarEstatisticasSalaUseCase.kt`
  - Injetar PatrimonioRepository e ColetaRepository
  - Calcular estatísticas incluindo coletadosHoje e coletadosSemana
  - _Requirements: 2.1, 6.4_

- [x] 5.3 Criar BuscarSalasComProgressoUseCase
  - Criar arquivo `domain/usecase/BuscarSalasComProgressoUseCase.kt`
  - Injetar SalaRepository
  - Retornar lista de SalaComProgresso ordenada por nome
  - _Requirements: 4.1_

- [ ]* 5.4 Write property test for filter correctness
  - **Property 1: Filter Correctness**
  - **Validates: Requirements 3.2, 3.3, 3.4**

## 6. Checkpoint - Verificar camada de dados
- [x] 6. Ensure all tests pass, ask the user if questions arise.

## 7. Criar Presentation Layer - State e ViewModel

- [x] 7.1 Criar InventarioPorSalaState
  - Criar arquivo `presentation/state/InventarioPorSalaState.kt`
  - Implementar sealed class com estados: Idle, Loading, SalasCarregadas, SalaSelecionada, Error
  - _Requirements: 1.2, 2.5_

- [x] 7.2 Criar InventarioPorSalaViewModel
  - Criar arquivo `presentation/inventario/InventarioPorSalaViewModel.kt`
  - Usar @HiltViewModel e injetar Use Cases
  - Implementar métodos: carregarSalas, selecionarSala, aplicarFiltro, buscarSala, carregarMaisPatrimonios
  - _Requirements: 1.2, 1.3, 3.1, 4.4_

- [ ]* 7.3 Write property test for room search filtering
  - **Property 4: Room Search Filtering**
  - **Validates: Requirements 4.4**

## 8. Criar UI Components

- [x] 8.1 Criar layout fragment_inventario_por_sala.xml
  - Adicionar AutoCompleteTextView para seleção de sala
  - Adicionar CardView para estatísticas
  - Adicionar ChipGroup para filtros (Todos, Coletados, Não Coletados)
  - Adicionar ProgressBar para percentual de coleta
  - Adicionar RecyclerView para lista de patrimônios
  - _Requirements: 1.2, 2.1, 3.1, 6.1_

- [x] 8.2 Criar InventarioPorSalaFragment
  - Criar arquivo `presentation/inventario/InventarioPorSalaFragment.kt`
  - Usar @AndroidEntryPoint
  - Implementar observação do ViewModel state
  - Configurar dropdown de salas com progresso
  - Configurar chips de filtro
  - Configurar RecyclerView com paginação
  - _Requirements: 1.2, 1.3, 3.1_

- [x] 8.3 Criar SalaDropdownAdapter
  - Criar adapter customizado para exibir salas com progresso
  - Exibir nome da sala + "X/Y coletados"
  - Adicionar ícones de status (checkmark para 100%, warning para 0%)
  - _Requirements: 4.1, 4.2, 4.3_

- [ ]* 8.4 Write property test for color mapping
  - **Property 3: Color Mapping Correctness**
  - **Validates: Requirements 2.3, 6.2**

## 9. Refatorar InventarioActivity para usar TabLayout

- [x] 9.1 Criar layout atualizado activity_inventario_tabs.xml
  - Adicionar TabLayout com duas abas: "Por Responsável" e "Por Sala"
  - Adicionar ViewPager2 para conteúdo
  - _Requirements: 1.1_

- [x] 9.2 Criar InventarioPagerAdapter
  - Criar adapter para ViewPager2
  - Configurar dois fragments: InventarioPorResponsavelFragment e InventarioPorSalaFragment
  - _Requirements: 1.1_

- [x] 9.3 Extrair InventarioPorResponsavelFragment
  - Mover lógica existente de InventarioActivity para novo Fragment
  - Manter ViewModel e funcionalidade existentes (filtro por responsável)
  - Preservar chips de status (Todos, Coletados, Não Coletados)
  - _Requirements: 1.1, 1.5_

- [x] 9.4 Atualizar InventarioActivity
  - Configurar TabLayout + ViewPager2
  - Remover lógica movida para fragments
  - Preservar estado das abas durante navegação
  - _Requirements: 1.1, 1.5_

- [ ]* 9.5 Write property test for tab state preservation
  - **Property 6: Tab State Preservation**
  - **Validates: Requirements 1.5**

## 10. Implementar funcionalidades de estatísticas visuais

- [x] 10.1 Criar helper para mapeamento de cores
  - Criar arquivo `presentation/util/ProgressColorHelper.kt`
  - Implementar função que retorna cor baseada no percentual
  - Red (0-25%), Orange (26-50%), Yellow (51-75%), Green (76-100%)
  - _Requirements: 2.3, 6.2_

- [x] 10.2 Implementar card de estatísticas
  - Criar layout item_estatisticas_sala.xml
  - Exibir total, coletados, pendentes, percentual
  - Aplicar cores conforme percentual
  - Adicionar ícone de conclusão quando 100%
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 10.3 Implementar progress bar com cores
  - Configurar ProgressBar horizontal
  - Aplicar cor dinâmica baseada no percentual
  - Adicionar click listener para exibir detalhes
  - _Requirements: 6.1, 6.2, 6.3_

## 11. Implementar suporte offline

- [x] 11.1 Atualizar estratégia de data source
  - Verificar conectividade antes de buscar dados
  - Usar LocalDataSourceStrategy quando offline
  - _Requirements: 5.1_

- [x] 11.2 Implementar indicador de dados desatualizados
  - Verificar timestamp da última sincronização
  - Exibir warning se dados > 24 horas
  - _Requirements: 5.4_

- [ ]* 11.3 Write property test for offline data loading
  - **Property 7: Offline Data Loading**
  - **Validates: Requirements 5.1**

- [ ]* 11.4 Write property test for data staleness detection
  - **Property 8: Data Staleness Detection**
  - **Validates: Requirements 5.4**

## 12. Checkpoint - Verificar integração completa
- [x] 12. Ensure all tests pass, ask the user if questions arise.

## 13. Registrar módulos no Hilt

- [x] 13.1 Atualizar UseCaseModule
  - Adicionar providers para novos Use Cases: BuscarPatrimoniosPorSalaUseCase, BuscarEstatisticasSalaUseCase, BuscarSalasComProgressoUseCase
  - _Requirements: 1.3, 2.1, 4.1_

- [x] 13.2 Verificar injeção de dependências
  - Testar que ViewModel recebe Use Cases corretamente
  - Testar que Fragment recebe ViewModel corretamente
  - _Requirements: 1.2_

## 14. Final Checkpoint - Verificar tudo funcionando
- [x] 14. Ensure all tests pass, ask the user if questions arise.
