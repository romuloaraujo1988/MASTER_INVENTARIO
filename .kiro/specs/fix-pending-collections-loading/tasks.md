# Implementation Plan

- [x] 1. Corrigir dependências na PendingCollectionsActivity




  - [ ] 1.1 Substituir MockApiService por ApiClient.getApiService()
    - Alterar a criação do ViewModel para usar ApiClient real

    - Importar ApiClient no arquivo
    - _Requirements: 2.1_




  - [ ] 1.2 Usar LocalDataManager.getInstance() ao invés de nova instância
    - Alterar para usar o singleton do LocalDataManager


    - _Requirements: 2.2_



- [x] 2. Corrigir acesso ao Room no PendingCollectionsViewModel




  - [ ] 2.1 Remover instanciação incorreta do InventarioDatabase
    - Remover linhas que usam `android.app.Application()` como contexto
    - _Requirements: 2.3_


  - [ ] 2.2 Criar métodos no InventarioRepository para limpar erros
    - Adicionar método `limparErroColeta(coletaId: Long)` no repository

    - Adicionar método `limparTodosErrosColetas()` no repository
    - _Requirements: 2.4_
  - [x] 2.3 Atualizar ViewModel para usar métodos do repository




    - Modificar `retryCollection()` para usar repository


    - Modificar `clearAllErrors()` para usar repository
    - _Requirements: 2.3, 2.4_

- [ ] 3. Adicionar logs de diagnóstico
  - [ ] 3.1 Adicionar logs no loadPendingCollections()
    - Log ao iniciar busca
    - Log com quantidade de coletas encontradas
    - Log em caso de erro
    - _Requirements: 3.1, 3.2_
  - [ ] 3.2 Adicionar logs na atualização da UI
    - Log quando a lista é atualizada
    - Log quando estado vazio é mostrado
    - _Requirements: 3.3_

- [ ] 4. Checkpoint - Verificar funcionamento
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Adicionar método onResume para recarregar dados
  - [ ] 5.1 Implementar onResume na Activity
    - Chamar viewModel.loadPendingCollections() no onResume
    - Garantir que dados são recarregados ao voltar para a tela
    - _Requirements: 4.4_

- [ ] 6. Final Checkpoint - Testar todas as funcionalidades
  - Ensure all tests pass, ask the user if questions arise.

