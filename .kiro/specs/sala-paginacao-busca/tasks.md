# Implementation Plan - Paginação e Busca de Salas

## Task List

- [x] 1. Atualizar SalaApi com endpoints paginados


  - Adicionar método `listarSalasPaginado(page, size)`
  - Adicionar método `buscarSalasPorNome(query, page, size)`
  - Criar DTO `PagedResponse<T>`
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_




- [ ] 2. Atualizar SalaDao com queries otimizadas
  - Adicionar índice em coluna `nome` para busca rápida
  - Adicionar query `buscarPorNome(query)` com LIKE
  - Adicionar query `contar()` para estatísticas
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 3. Criar SalaPagingSource
  - Implementar `load(params: LoadParams)` com paginação
  - Implementar `getRefreshKey(state: PagingState)`
  - Tratar erros e retornar `LoadResult.Error`
  - Suportar busca por query
  - _Requirements: 4.1, 4.2_

- [ ] 4. Criar BuscarSalasUseCase
  - Receber query como parâmetro
  - Retornar `Flow<PagingData<Sala>>`
  - Configurar `PagingConfig` (pageSize=20, prefetchDistance=5)
  - _Requirements: 4.3, 4.4, 4.5_

- [ ] 5. Atualizar SalaViewModel
  - Adicionar `StateFlow<String>` para searchQuery
  - Implementar debounce de 300ms na busca
  - Usar `flatMapLatest` para cancelar buscas anteriores
  - Usar `cachedIn(viewModelScope)` para cache
  - Adicionar método `setSearchQuery(query)`
  - Adicionar método `clearSearch()`
  - _Requirements: 2.2, 2.3, 2.4, 4.6, 7.1, 7.2_

- [ ] 6. Criar SalaPagingAdapter
  - Estender `PagingDataAdapter<Sala, ViewHolder>`
  - Implementar `DiffUtil.ItemCallback` para comparação eficiente
  - Criar ViewHolder com binding
  - Adicionar callback `onSalaClick`
  - _Requirements: 1.1, 1.2_

- [ ] 7. Criar SalaLoadStateAdapter
  - Estender `LoadStateAdapter<ViewHolder>`
  - Exibir ProgressBar quando `LoadState.Loading`
  - Exibir erro e botão retry quando `LoadState.Error`
  - Exibir mensagem "Fim da lista" quando `LoadState.NotLoading` e `endOfPaginationReached`
  - _Requirements: 1.3, 1.4, 1.5, 9.2, 9.3, 9.4_

- [ ] 8. Atualizar SalaSelectionActivity
  - Adicionar SearchView no layout
  - Configurar RecyclerView com PagingDataAdapter
  - Adicionar SwipeRefreshLayout
  - Observar `salaPagingData` do ViewModel
  - Implementar listener do SearchView com debounce
  - Adicionar LoadStateAdapter no footer
  - Observar LoadState para exibir empty state
  - _Requirements: 2.1, 2.5, 2.6, 9.1_

- [ ] 9. Criar layouts XML
  - Atualizar `activity_sala_selection.xml` com SearchView
  - Criar `item_sala.xml` para item da lista
  - Criar `item_load_state.xml` para loading/erro
  - Adicionar shimmer effect (opcional)
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 10. Implementar indicadores visuais
  - Adicionar badge "Offline" quando sem conexão
  - Adicionar contador de resultados no subtítulo
  - Adicionar shimmer effect no carregamento inicial
  - Adicionar animações de transição
  - _Requirements: 3.5, 5.1, 5.2, 5.3, 9.5_

- [ ]* 11. Implementar cache inteligente
  - Criar `SalaCache` com TTL de 5 minutos
  - Cachear resultados de busca
  - Limpar cache ao fazer logout
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ]* 12. Adicionar testes unitários
  - Testar SalaViewModel com diferentes queries
  - Testar SalaPagingSource com mock
  - Testar BuscarSalasUseCase
  - Testar debounce e cancelamento
  - _Requirements: 10.1, 10.2, 10.3_

- [ ]* 13. Adicionar testes de UI
  - Testar busca com Espresso
  - Testar scroll infinito
  - Testar retry em caso de erro
  - _Requirements: 10.4_

- [ ]* 14. Otimizações de performance
  - Adicionar índices no banco Room
  - Implementar RecyclerView.RecycledViewPool
  - Otimizar layouts (usar ConstraintLayout)
  - Medir performance com Profiler
  - _Requirements: Performance Requirements_

## Notes

- Tasks marcados com `*` são opcionais
- Priorizar tasks 1-10 para MVP funcional
- Testar cada task antes de prosseguir
- Medir tempo de carregamento antes e depois

## Estimated Effort

| Task | Complexity | Estimated Time |
|------|-----------|----------------|
| 1-2  | Low       | 1 hour         |
| 3-4  | Medium    | 2 hours        |
| 5-7  | Medium    | 3 hours        |
| 8-10 | Medium    | 3 hours        |
| 11-14| Low       | 2 hours        |

**Total MVP (tasks 1-10):** ~9 hours
**Total Complete (all tasks):** ~11 hours

## Success Metrics

- ✅ Carregamento inicial < 1s (vs 5s+ atual)
- ✅ Busca < 500ms após debounce
- ✅ Scroll suave 60 FPS
- ✅ Memória < 50MB para 1000 salas
- ✅ Cobertura de testes > 8