# Implementation Plan

- [ ] 1. Atualizar InventarioUiState com novos campos de controle
  - Adicionar campo `hasResponsavelSelected: Boolean = false` para indicar se há responsável selecionado
  - Adicionar campo `showEmptyStateMessage: Boolean = true` para controlar exibição da mensagem inicial
  - Localização: `InventarioViewModel.kt` - data class InventarioUiState
  - _Requirements: 1.1, 1.3, 7.3_

- [ ] 2. Modificar ViewModel para não carregar patrimônios automaticamente
  - [ ] 2.1 Remover chamada `loadPatrimonios()` do bloco `init`
    - Manter apenas `loadResponsaveis()` no init
    - Adicionar log de debug para confirmar mudança
    - Localização: `InventarioViewModel.kt` - bloco init (linha ~44)
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [ ] 2.2 Modificar método `loadPatrimonios()` para não executar ação por padrão
    - Adicionar log de warning quando método for chamado sem responsável
    - Manter método para compatibilidade mas não carregar dados automaticamente
    - Documentar que método não deve ser usado sem responsável selecionado
    - Localização: `InventarioViewModel.kt` - método loadPatrimonios() (linha ~49)
    - _Requirements: 7.4_
  
  - [ ] 2.3 Atualizar método `loadPatrimoniosByResponsavel()`
    - Definir `hasResponsavelSelected = true` ao iniciar carregamento
    - Definir `showEmptyStateMessage = false` ao iniciar carregamento
    - Manter lógica existente de paginação e filtros
    - Localização: `InventarioViewModel.kt` - método loadPatrimoniosByResponsavel() (linha ~68)
    - _Requirements: 2.1, 2.2, 2.3, 7.5_
  
  - [ ] 2.4 Atualizar método `clearFilters()`
    - Limpar listas `patrimonios` e `patrimoniosFiltered`
    - Definir `hasResponsavelSelected = false`
    - Definir `showEmptyStateMessage = true`
    - Remover chamada a `loadPatrimonios()`
    - Localização: `InventarioViewModel.kt` - método clearFilters() (linha ~139)
    - _Requirements: 4.1, 4.2, 4.4_

- [ ] 3. Atualizar layout XML para suportar mensagem dinâmica
  - Adicionar `android:id="@+id/tvEmptyMessage"` ao TextView existente no emptyView
  - Alterar texto padrão para: "Selecione um responsável para visualizar os patrimônios"
  - Ajustar espaçamento e alinhamento para melhor legibilidade
  - Localização: `activity_inventario.xml` - LinearLayout emptyView (linha ~227)
  - _Requirements: 1.3_

- [ ] 4. Modificar InventarioActivity para gerenciar estado vazio
  - [ ] 4.1 Remover carregamento automático no `onCreate()`
    - Remover linha `viewModel.loadPatrimonios()` do onCreate (linha ~68)
    - Manter apenas setup de UI e observers
    - Localização: `InventarioActivity.kt` - método onCreate()
    - _Requirements: 1.1, 1.2_
  
  - [ ] 4.2 Implementar habilitação/desabilitação de filtros de status
    - Observar `state.hasResponsavelSelected` no setupFiltros
    - Habilitar chips apenas quando responsável estiver selecionado
    - Aplicar alpha 0.5f quando desabilitado para feedback visual
    - Localização: `InventarioActivity.kt` - método setupFiltros() (linha ~113)
    - _Requirements: 3.1, 3.2_
  
  - [ ] 4.3 Atualizar método `aplicarFiltros()`
    - Verificar se `responsavelId` não é nulo antes de carregar
    - Se responsável for nulo e status for selecionado, apenas resetar filtro de status
    - Não chamar `loadPatrimonios()` sem responsável selecionado
    - Localização: `InventarioActivity.kt` - método aplicarFiltros() (linha ~152)
    - _Requirements: 3.3, 3.4_
  
  - [ ] 4.4 Atualizar método `updateUI()` para exibir estado vazio
    - Verificar `state.showEmptyStateMessage` e `state.hasResponsavelSelected`
    - Exibir mensagem "Selecione um responsável..." quando apropriado
    - Exibir mensagem "Nenhum patrimônio encontrado..." quando responsável selecionado mas lista vazia
    - Atualizar contador para "Total: 0 patrimônios" no estado inicial
    - Adicionar referência ao TextView tvEmptyMessage no binding
    - Localização: `InventarioActivity.kt` - método updateUI() (linha ~289)
    - _Requirements: 1.3, 1.4, 2.5_
  
  - [ ] 4.5 Atualizar método `limparFiltros()`
    - Resetar seleção do AutoCompleteTextView para "Todos os responsáveis"
    - Resetar chips de status para "Todos"
    - Chamar `viewModel.clearFilters()` para limpar estado
    - Localização: `InventarioActivity.kt` - método limparFiltros() (linha ~168)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 5. Ajustar comportamento de swipe-to-refresh
  - Modificar listener do swipeRefresh no setupUI()
  - Verificar se há responsável selecionado antes de recarregar
  - Se houver responsável, recarregar patrimônios do responsável atual
  - Se não houver, apenas recarregar lista de responsáveis
  - Localização: `InventarioActivity.kt` - método setupUI() (linha ~97)
  - _Requirements: 6.5_

- [ ] 6. Ajustar comportamento de paginação
  - Verificar que paginação só funciona quando responsável está selecionado
  - Confirmar que `currentPage` é resetado para 0 ao selecionar novo responsável (já implementado)
  - Verificar que indicador de "carregando mais..." funciona corretamente
  - Localização: `InventarioActivity.kt` - RecyclerView.OnScrollListener (linha ~234)
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 7. Atualizar tratamento de erros
  - Diferenciar erro de carregamento de estado vazio válido no updateUI
  - Garantir que mensagens de erro apropriadas são exibidas via Toast
  - Permitir retry através de nova seleção de responsável
  - Localização: `InventarioActivity.kt` - método updateUI()
  - _Requirements: 2.5_

- [ ]* 8. Criar testes unitários para ViewModel
  - [ ]* 8.1 Testar que init não carrega patrimônios
    - Verificar que `patrimonios` está vazio após init
    - Verificar que `responsaveis` é carregado
    - _Requirements: 7.1, 7.2_
  
  - [ ]* 8.2 Testar que loadPatrimonios() não carrega sem responsável
    - Chamar método e verificar que estado não muda
    - _Requirements: 7.4_
  
  - [ ]* 8.3 Testar loadPatrimoniosByResponsavel
    - Verificar carregamento com ID válido
    - Verificar que `hasResponsavelSelected = true`
    - _Requirements: 7.5_
  
  - [ ]* 8.4 Testar clearFilters
    - Verificar reset completo do estado
    - Verificar que `hasResponsavelSelected = false`
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ]* 9. Criar testes de integração para Activity
  - [ ]* 9.1 Testar estado inicial
    - Verificar que emptyView está visível
    - Verificar mensagem correta
    - _Requirements: 1.1, 1.3_
  
  - [ ]* 9.2 Testar seleção de responsável
    - Verificar carregamento de patrimônios
    - Verificar habilitação de filtros
    - _Requirements: 2.1, 2.2, 2.3, 3.2_
  
  - [ ]* 9.3 Testar limpar filtros
    - Verificar volta ao estado inicial
    - Verificar desabilitação de filtros
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 10. Realizar testes manuais completos
  - Testar fluxo completo: estado vazio → seleção → filtros → limpar
  - Testar rotação de tela com e sem responsável selecionado
  - Testar performance: medir tempo de abertura e uso de memória
  - Testar com responsável sem patrimônios
  - Testar paginação com responsável com muitos patrimônios
  - _Requirements: Todos_
