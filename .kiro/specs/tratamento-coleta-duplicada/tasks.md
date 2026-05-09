# Implementation Plan

- [x] 1. Adicionar campos de duplicata no MobileColetaResponse




  - [x] 1.1 Adicionar campo `duplicada` (boolean) no DTO

    - Adicionar getter e setter
    - _Requirements: 2.1_

  - [x] 1.2 Adicionar campo `mensagemDuplicada` (String) no DTO
    - Adicionar getter e setter




    - _Requirements: 2.2_
  - [x] 1.3 Adicionar campos `coletaOriginalId`, `dataColetaOriginal`, `coletorOriginal` no DTO
    - Adicionar getters e setters para os três campos
    - _Requirements: 2.3, 2.4, 1.2_





- [ ] 2. Implementar método de busca de coleta existente no ColetaDAO
  - [x] 2.1 Criar método `buscarColetaExistente(Integer idInventario, Integer idPatrimonio)`

    - Query SQL com JOIN para trazer dados do coletor
    - Retorna Coleta com dados populados ou null
    - _Requirements: 1.1_
  - [ ]* 2.2 Write property test for duplicate detection
    - **Property 1: Duplicate Detection Returns Valid Response**
    - **Validates: Requirements 1.1, 1.2, 1.3**





- [ ] 3. Implementar verificação de duplicata no MobileColetaService
  - [x] 3.1 Criar método privado `verificarColetaDuplicada(Integer idInventario, Integer idPatrimonio)`

    - Chamar ColetaDAO.buscarColetaExistente()
    - Retornar Coleta existente ou null
    - _Requirements: 1.1_




  - [x] 3.2 Criar método privado `criarRespostaDuplicada(Coleta coletaExistente, MobileColetaRequest request)`
    - Preencher todos os campos de duplicata no response
    - Incluir mensagem explicativa para o usuário

    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [ ]* 3.3 Write property test for response completeness
    - **Property 2: Duplicate Response Contains Complete Information**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**





- [ ] 4. Modificar método registrarColeta para tratar duplicatas
  - [x] 4.1 Adicionar verificação de duplicata antes da inserção

    - Chamar verificarColetaDuplicada() após validações
    - Se duplicata, retornar criarRespostaDuplicada()


    - Adicionar log de warning para duplicatas
    - _Requirements: 1.1, 1.3, 3.1_
  - [x] 4.2 Garantir que não lança exceção para duplicatas
    - Capturar possíveis exceções de constraint violation
    - Converter para resposta de duplicata
    - _Requirements: 1.1_

- [ ] 5. Modificar método registrarColetasEmLote para tratar duplicatas
  - [x] 5.1 Adicionar contagem de duplicatas no resultado
    - Novo campo `duplicadas` no Map de resultado
    - Nova lista `coletasDuplicadas` com números dos patrimônios
    - _Requirements: 1.4_


  - [x] 5.2 Processar duplicatas sem interromper o lote
    - Continuar processando após detectar duplicata
    - Incrementar contador de duplicatas
    - _Requirements: 1.4_
  - [ ]* 5.3 Write property test for batch processing
    - **Property 3: Batch Processing Handles Duplicates Gracefully**
    - **Validates: Requirements 1.4**

- [ ] 6. Implementar logging de duplicatas
  - [x] 6.1 Adicionar log de warning para cada duplicata detectada
    - Incluir número do patrimônio, ID do inventário, usuário
    - _Requirements: 3.1_
  - [ ] 6.2 Adicionar log informativo para múltiplas tentativas
    - Detectar padrão de múltiplas duplicatas do mesmo usuário
    - Sugerir limpeza de cache local
    - _Requirements: 3.2_

- [ ] 7. Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 8. Testes unitários
  - [ ]* 8.1 Teste de detecção de duplicata individual
    - Inserir coleta, tentar inserir novamente, verificar resposta
    - _Requirements: 1.1, 1.2, 1.3_
  - [ ]* 8.2 Teste de campos da resposta de duplicata
    - Verificar todos os campos obrigatórios presentes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [ ]* 8.3 Teste de lote com duplicatas
    - Enviar lote misto, verificar contagens corretas
    - _Requirements: 1.4_

- [ ] 9. Final Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.
