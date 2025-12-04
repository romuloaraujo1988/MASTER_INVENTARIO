# Implementation Plan

- [x] 1. Criar modelos de domínio para busca rápida
  - [x] 1.1 Criar `PatrimonioComColeta.kt` no pacote domain/model
    - Campos: id, numero, descricao, salaNome, responsavelNome, coletado, coletadoPor, dataColeta, temDivergencia
    - **NOTA:** Pode reutilizar `PatrimonioDetalhe.kt` existente que já possui todos esses campos
    - _Requirements: 2.1_
  - [x] 1.2 Criar `PatrimonioDetalhado.kt` no pacote domain/model
    - Campos completos do patrimônio incluindo informações de coleta
    - **NOTA:** `PatrimonioDetalhe.kt` já existe com todos os campos necessários
    - _Requirements: 2.2, 2.3, 2.4_
  - [x] 1.3 Criar `ColetaInfo.kt` no pacote domain/model
    - Campos: id, dataColeta, coletadoPor, localizacaoEncontrada, estadoEncontrado, observacoes, temDivergencia
    - **NOTA:** `Coleta.kt` já existe com campos equivalentes
    - _Requirements: 2.4, 2.5_
  - [x] 1.4 Criar `SearchFilter.kt` enum no pacote domain/model
    - Valores: ALL, COLETADOS, PENDENTES, DIVERGENCIAS
    - **NOTA:** Já existe dentro de `QuickSearchActivity.kt`, precisa mover para domain/model
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 2. Implementar queries no PatrimonioDao





  - [x] 2.1 Adicionar query `buscarPorQuery(query: String)` para busca geral



    - Buscar por número, descrição ou nome da sala
    - Limite de 100 resultados
    - _Requirements: 1.1_

  - [x] 2.2 Adicionar query `buscarColetadosPorQuery(query: String)` para filtro de coletados

    - Filtrar apenas patrimônios com coletado = true
    - _Requirements: 3.1_

  - [x] 2.3 Adicionar query `buscarPendentesPorQuery(query: String)` para filtro de pendentes

    - Filtrar apenas patrimônios com coletado = false
    - _Requirements: 3.2_
  - [x] 2.4 Adicionar query `buscarPorId(id: Long)` para detalhes
    - Retornar patrimônio completo por ID
    - **NOTA:** Já existe `buscarPorId(id: Int)` no PatrimonioDao
    - _Requirements: 2.2_

- [x] 3. Implementar query no ColetaDao
  - [x] 3.1 Adicionar query `buscarColetaDoPatrimonio(patrimonioId, inventarioId)` 
    - Retornar última coleta do patrimônio no inventário
    - **NOTA:** Já existe `buscarColetaExistente(idPatrimonio, idInventario)` no ColetaDao
    - _Requirements: 2.4_

- [x] 4. Criar Use Cases de busca





  - [x] 4.1 Criar `BuscarPatrimoniosUseCase.kt`



    - Receber query e filtro como parâmetros
    - Chamar repository com query apropriada baseada no filtro
    - Mapear resultados para PatrimonioComColeta
    - **NOTA:** Pode adaptar `BuscarPatrimonioAvancadaUseCase.kt` existente
    - _Requirements: 1.1, 3.1, 3.2, 3.3_
  - [ ]* 4.2 Escrever property test para BuscarPatrimoniosUseCase
    - **Property 1: Busca retorna resultados correspondentes**
    - **Validates: Requirements 1.1**
  - [ ]* 4.3 Escrever property test para filtro de coletados
    - **Property 2: Filtro de coletados retorna apenas coletados**
    - **Validates: Requirements 3.1**
  - [ ]* 4.4 Escrever property test para filtro de pendentes
    - **Property 3: Filtro de pendentes retorna apenas pendentes**
    - **Validates: Requirements 3.2**

- [x] 5. Criar Use Case de detalhes
  - [x] 5.1 Criar `BuscarPatrimonioDetalhadoUseCase.kt`
    - Buscar patrimônio por ID
    - Buscar coleta associada se existir
    - Montar PatrimonioDetalhado com todas as informações
    - **NOTA:** `ObterDetalhePatrimonioUseCase.kt` já existe com essa funcionalidade
    - _Requirements: 2.2, 2.3, 2.4, 2.5_
  - [ ]* 5.2 Escrever property test para detalhes de patrimônio coletado
    - **Property 5: Detalhes de patrimônio coletado incluem informações da coleta**
    - **Validates: Requirements 2.4**

- [x] 6. Checkpoint - Verificar Use Cases


  - Ensure all tests pass, ask the user if questions arise.


- [x] 7. Criar UI States para busca rápida




  - [x] 7.1 Criar `QuickSearchState.kt` sealed class



    - Estados: Idle, Loading, Success, Empty, Error
    - _Requirements: 1.3, 4.1_

  - [x] 7.2 Criar `SearchStats.kt` data class

    - Campos: totalResultados, coletados, pendentes, divergencias, tempoMs
    - _Requirements: 4.1, 4.2, 4.3_
  - [x] 7.3 Criar `PatrimonioDetailState.kt` sealed class


    - Estados: Loading, Success, Error
    - _Requirements: 2.2_

- [x] 8. Criar ViewModels




  - [x] 8.1 Criar `QuickSearchViewModel.kt` com @HiltViewModel




    - Injetar BuscarPatrimoniosUseCase
    - Implementar buscar(query, filtro) com debounce
    - Calcular estatísticas dos resultados
    - _Requirements: 1.1, 1.2, 4.1, 4.2, 4.3_
  - [ ]* 8.2 Escrever property test para estatísticas
    - **Property 4: Estatísticas correspondem aos resultados**
    - **Validates: Requirements 4.1, 4.3**


  - [x] 8.3 Criar `PatrimonioDetailViewModel.kt` com @HiltViewModel

    - Injetar ObterDetalhePatrimonioUseCase (já existente)
    - Implementar carregarDetalhes(patrimonioId)
    - _Requirements: 2.2, 2.3, 2.4_

- [x] 9. Refatorar QuickSearchActivity para Clean Architecture



  - [x] 9.1 Adicionar @AndroidEntryPoint na QuickSearchActivity




    - Injetar QuickSearchViewModel via by viewModels()
    - _Requirements: 1.1_

  - [x] 9.2 Substituir acesso direto ao DAO pelo ViewModel

    - Remover database = InventarioDatabase.getDatabase(this)
    - Usar viewModel.buscar() ao invés de searchPatrimonios()
    - _Requirements: 1.1, 6.1, 6.2_


  - [x] 9.3 Implementar observação de estados com StateFlow
    - Observar viewModel.state para atualizar UI
    - Observar viewModel.searchStats para estatísticas

    - _Requirements: 4.1, 4.2, 4.3_
  - [x] 9.4 Implementar navegação para detalhes ao clicar no item

    - Passar patrimonioId para PatrimonioDetailActivity
    - _Requirements: 2.2_

- [x] 10. Criar tela de detalhes do patrimônio



  - [x] 10.1 Criar layout `activity_patrimonio_detail.xml`



    - Card com informações básicas (número, descrição, marca, modelo)
    - Card com localização (sala, setor)
    - Card com responsável
    - Card com status de coleta (se coletado)
    - Botões de ação condicionais
    - _Requirements: 2.2, 2.3, 2.4, 2.5_

  - [x] 10.2 Criar `PatrimonioDetailActivity.kt` com @AndroidEntryPoint

    - Injetar PatrimonioDetailViewModel
    - Observar estado e atualizar UI
    - Implementar botões de ação
    - _Requirements: 2.2, 5.1, 5.2, 5.3_
  - [ ]* 10.3 Escrever property test para ações condicionais
    - **Property 6: Ações condicionais baseadas no status de coleta**
    - **Validates: Requirements 5.1, 5.2**

- [x] 11. Atualizar item de lista para exibir mais informações
  - [x] 11.1 Atualizar layout `item_patrimonio.xml`
    - Adicionar campo de responsável
    - Adicionar indicador visual de status (coletado/pendente)
    - Adicionar indicador de divergência
    - **NOTA:** Layout já possui seção `layoutInfoColeta` com coletadoPor, dataColeta, localizacaoEncontrada, estadoEncontrado
    - _Requirements: 2.1, 2.5_

  - [x] 11.2 Atualizar `PatrimonioAdapter.kt`

    - Usar PatrimonioComColeta ao invés de Patrimonio
    - Exibir responsável e status de coleta
    - _Requirements: 2.1_

- [x] 12. Implementar indicador de última sincronização
  - [x] 12.1 Adicionar campo de última sincronização no layout
    - Exibir data/hora da última sincronização
    - Destacar se dados estão desatualizados (> 24h)
    - _Requirements: 6.3_
  - [x] 12.2 Implementar lógica de verificação de atualização

    - Buscar timestamp da última sincronização do SincronizacaoDao
    - Calcular diferença e exibir indicador apropriado
    - _Requirements: 6.3_

- [x] 13. Checkpoint - Verificar integração completa


  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 14. Escrever property test para funcionamento offline
  - **Property 7: Busca funciona offline**
  - **Validates: Requirements 6.1, 6.2**

- [x] 15. Final Checkpoint - Verificar todos os testes



  - Ensure all tests pass, ask the user if questions arise.
