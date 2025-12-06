# Implementation Plan

- [x] 1. Criar infraestrutura base do sistema de eventos




  - [ ] 1.1 Criar enum DashboardEventType com os 5 tipos de eventos
    - Implementar enum com valores: COLETA_SINCRONIZADA, PATRIMONIO_ATUALIZADO, INVENTARIO_ALTERADO, SALA_ATUALIZADA, RESPONSAVEL_ATUALIZADO
    - Adicionar descrição e métodos utilitários


    - _Requirements: 5.1_

  - [ ] 1.2 Criar classe DashboardEvent como modelo de domínio
    - Implementar campos: type, timestamp, source, metadata, affectedEntityIds
    - Implementar construtor, getters, equals, hashCode, toString
    - Garantir imutabilidade da classe
    - _Requirements: 5.2_


  - [x]* 1.3 Write property test for DashboardEvent



    - **Property 10: Event Log Completeness**
    - Verificar que eventos criados sempre têm type, timestamp e source não-nulos
    - **Validates: Requirements 4.3**

- [ ] 2. Implementar serialização de eventos (round-trip)
  - [ ] 2.1 Criar classe DashboardEventSerializer
    - Implementar método serialize(DashboardEvent) -> String




    - Implementar método deserialize(String) -> DashboardEvent
    - Usar formato JSON para serialização
    - _Requirements: 5.4_

  - [ ]* 2.2 Write property test for event serialization round-trip
    - **Property 12: Event Serialization Round-Trip**
    - Para qualquer DashboardEvent válido, serialize -> deserialize deve retornar evento igual ao original
    - **Validates: Requirements 5.4**





- [ ] 3. Implementar CircularEventBuffer para log de eventos
  - [ ] 3.1 Criar classe CircularEventBuffer
    - Implementar buffer circular com capacidade fixa de 10 elementos
    - Implementar métodos add(event), getAll(), size()
    - Garantir que eventos mais antigos são descartados quando buffer está cheio
    - _Requirements: 4.1_

  - [ ]* 3.2 Write property test for bounded size
    - **Property 9: Event Log Bounded Size**
    - Para qualquer sequência de N eventos (N > 10), o log deve conter exatamente os últimos 10 eventos em ordem
    - **Validates: Requirements 4.1**


- [ ] 4. Implementar EventDebouncer para rate limiting
  - [x] 4.1 Criar classe EventDebouncer




    - Implementar lógica de debounce com threshold de 5 eventos por segundo
    - Implementar métodos shouldDebounce(event), getConsolidatedEvents(), reset()
    - Limitar atualizações de UI a máximo de 2 por segundo


    - _Requirements: 8.1, 8.2_

  - [ ]* 4.2 Write property test for rate limiting
    - **Property 18: Debounce Rate Limiting**
    - Para qualquer burst de mais de 5 eventos em 1 segundo, dashboard deve fazer no máximo 2 atualizações de UI

    - **Validates: Requirements 8.1, 8.2**

  - [ ]* 4.3 Write property test for batch consolidation
    - **Property 2: Batch Event Consolidation**
    - Para qualquer batch de N eventos (N > 1) publicados em janela de 100ms, dashboard deve receber no máximo uma notificação consolidada

    - **Validates: Requirements 1.2**

- [ ] 5. Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implementar DashboardEventBus (Singleton)
  - [ ] 6.1 Criar interface DashboardObserver
    - Definir método onDashboardEvent(DashboardEvent event)
    - Definir método default getSubscribedEventTypes() retornando todos os tipos
    - _Requirements: 2.1, 5.3_

  - [ ] 6.2 Criar classe DashboardEventBus como Singleton
    - Implementar padrão Singleton thread-safe com double-checked locking
    - Implementar Map<EventType, List<Observer>> para gerenciar observers
    - Implementar ExecutorService para entrega assíncrona
    - Integrar EventDebouncer para rate limiting
    - _Requirements: 2.3, 2.5_

  - [ ] 6.3 Implementar métodos register/unregister
    - Implementar register(DashboardObserver) com suporte a filtro por tipo
    - Implementar unregister(DashboardObserver)
    - Garantir thread-safety nas operações
    - _Requirements: 2.1, 2.2_

  - [ ] 6.4 Implementar método publish com tratamento de erros
    - Implementar publish(DashboardEvent) assíncrono
    - Capturar exceções de observers e logar sem interromper outros
    - Garantir que publish() retorna rapidamente (< 10ms)
    - _Requirements: 2.3, 2.4_

  - [ ]* 6.5 Write property test for singleton consistency
    - **Property 8: Singleton Consistency**
    - Para qualquer número de chamadas a getInstance(), todas devem retornar a mesma instância
    - **Validates: Requirements 2.5**


  - [x]* 6.6 Write property test for observer registration round-trip




    - **Property 4: Observer Registration Round-Trip**
    - Para qualquer observer registrado, ele deve receber eventos dos tipos subscritos, e após unregister não deve receber mais eventos
    - **Validates: Requirements 2.1**


  - [ ]* 6.7 Write property test for multiple observers
    - **Property 5: Multiple Observer Support**
    - Para qualquer conjunto de N observers registrados para o mesmo tipo, quando um evento desse tipo é publicado, todos N observers devem receber exatamente uma vez
    - **Validates: Requirements 2.2**



  - [ ]* 6.8 Write property test for async delivery
    - **Property 6: Async Event Delivery**
    - Para qualquer evento publicado, o método publish() deve retornar em menos de 10ms independente do tempo de processamento dos observers
    - **Validates: Requirements 2.3**

  - [ ]* 6.9 Write property test for exception isolation
    - **Property 7: Observer Exception Isolation**
    - Para qualquer conjunto de observers onde um lança exceção, todos os outros devem ainda receber o evento com sucesso
    - **Validates: Requirements 2.4**

  - [x]* 6.10 Write property test for event type filtering




    - **Property 11: Event Type Filtering**
    - Para qualquer observer registrado para um subconjunto específico de tipos, ele deve receber apenas eventos desses tipos
    - **Validates: Requirements 5.3**

- [x] 7. Checkpoint - Garantir que todos os testes passam

  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Integrar EventBus nos serviços existentes
  - [x] 8.1 Integrar no MobileColetaService

    - Publicar evento COLETA_SINCRONIZADA após sincronização bem-sucedida
    - Incluir metadata: coletaId, patrimonioId, inventarioId, coletorNome
    - Garantir que falha na publicação não afeta operação original
    - _Requirements: 7.1, 7.4, 7.5_


  - [ ] 8.2 Integrar no PatrimonioService (se existir) ou PatrimonioDAO
    - Publicar evento PATRIMONIO_ATUALIZADO após atualização
    - Incluir metadata: patrimonioId, campo alterado, valores anterior/novo
    - _Requirements: 7.2_

  - [ ] 8.3 Integrar no InventarioService ou InventarioDAO
    - Publicar evento INVENTARIO_ALTERADO após mudança de status
    - Incluir metadata: inventarioId, status, percentualConcluido

    - _Requirements: 7.3_


  - [ ]* 8.4 Write property test for service event publishing
    - **Property 16: Service Event Publishing**
    - Para qualquer método de serviço que modifica dados, um evento correspondente deve ser publicado no EventBus
    - **Validates: Requirements 7.1, 7.2, 7.3**


  - [ ]* 8.5 Write property test for event publishing resilience
    - **Property 17: Event Publishing Resilience**
    - Para qualquer operação de serviço, se a publicação de evento falhar, a operação original deve completar com sucesso
    - **Validates: Requirements 7.4**


- [ ] 9. Refatorar DashboardColetaFrame para usar Observer
  - [ ] 9.1 Implementar interface DashboardObserver no DashboardColetaFrame
    - Implementar método onDashboardEvent(DashboardEvent)
    - Definir tipos de eventos de interesse

    - Registrar no EventBus ao inicializar
    - Desregistrar ao fechar
    - _Requirements: 1.1, 1.3_

  - [x] 9.2 Remover Timer de polling de 30 segundos

    - Remover código do Timer existente

    - Manter apenas atualização inicial ao abrir
    - _Requirements: 1.1_

  - [x] 9.3 Implementar atualização parcial de componentes

    - Atualizar apenas cards/gráficos afetados pelo evento
    - Evitar refresh completo da página
    - _Requirements: 1.3_

  - [x] 9.4 Implementar fila de eventos quando dashboard não visível

    - Detectar quando janela está minimizada ou em background
    - Enfileirar eventos recebidos
    - Aplicar eventos enfileirados ao tornar-se visível
    - _Requirements: 1.4_


  - [ ]* 9.5 Write property test for event queuing when hidden
    - **Property 3: Event Queuing When Hidden**
    - Para qualquer sequência de eventos publicados enquanto dashboard não está visível, todos devem ser enfileirados e entregues quando visível, preservando ordem
    - **Validates: Requirements 1.4**

- [ ] 10. Implementar indicadores visuais de atualização
  - [ ] 10.1 Adicionar indicador de refresh na dashboard
    - Criar JLabel ou componente para mostrar animação/indicador de atualização
    - Exibir ao receber evento de atualização
    - Ocultar após 500ms da conclusão
    - _Requirements: 3.1, 3.2_

  - [ ] 10.2 Implementar indicador de erro com retry
    - Exibir indicador de erro quando atualização falhar

    - Adicionar botão de retry
    - _Requirements: 3.3_




  - [ ] 10.3 Implementar tooltip com histórico de eventos
    - Usar CircularEventBuffer para manter últimos 10 eventos
    - Exibir tooltip ao passar mouse sobre área de status
    - Mostrar tipo, timestamp e source de cada evento
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ] 10.4 Implementar indicador de "Batch update in progress"
    - Detectar quando debouncing está ativo
    - Exibir indicador apropriado

    - _Requirements: 8.3_

- [ ] 11. Implementar funcionalidade de pause/resume
  - [ ] 11.1 Adicionar botão toggle pause/resume
    - Criar JToggleButton para pausar/resumir atualizações

    - Atualizar estado visual do botão
    - _Requirements: 6.1_

  - [ ] 11.2 Implementar lógica de pause
    - Quando pausado, não aplicar atualizações automáticas

    - Exibir indicador "Paused"
    - Enfileirar eventos recebidos durante pause
    - _Requirements: 6.1, 6.2_

  - [ ] 11.3 Implementar lógica de resume
    - Ao resumir, buscar dados mais recentes imediatamente
    - Aplicar eventos enfileirados
    - _Requirements: 6.3_

  - [ ] 11.4 Implementar auto-resume após 5 minutos
    - Criar Timer para auto-resume
    - Resetar timer quando usuário interage
    - _Requirements: 6.4_

  - [ ]* 11.5 Write property test for pause state consistency
    - **Property 13: Pause State Consistency**
    - Para qualquer dashboard em estado pausado, nenhuma atualização automática deve ocorrer até resumir
    - **Validates: Requirements 6.1**

  - [ ]* 11.6 Write property test for resume triggers refresh
    - **Property 14: Resume Triggers Refresh**
    - Para qualquer dashboard que é resumido do estado pausado, um refresh de dados deve ser disparado em até 100ms
    - **Validates: Requirements 6.3**

  - [ ]* 11.7 Write property test for auto-resume after timeout
    - **Property 15: Auto-Resume After Timeout**
    - Para qualquer dashboard pausado por 5 minutos, deve automaticamente resumir e disparar refresh
    - **Validates: Requirements 6.4**

- [ ] 12. Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Testes de integração e ajustes finais
  - [ ] 13.1 Testar fluxo completo de sincronização mobile -> dashboard
    - Simular sincronização de coleta do app mobile
    - Verificar que dashboard atualiza em menos de 2 segundos
    - _Requirements: 1.1_

  - [ ]* 13.2 Write property test for event delivery timeliness
    - **Property 1: Event Delivery Timeliness**
    - Para qualquer evento de sincronização de coleta publicado, o observer da dashboard deve receber em até 2 segundos
    - **Validates: Requirements 1.1**

  - [ ] 13.3 Testar cenários de alta frequência de eventos
    - Simular burst de eventos
    - Verificar debouncing funciona corretamente
    - Verificar UI permanece responsiva
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ] 13.4 Documentar uso do sistema de eventos
    - Criar documentação para desenvolvedores
    - Incluir exemplos de como adicionar novos tipos de eventos
    - Incluir exemplos de como criar novos observers
    - _Requirements: 2.1_

- [ ] 14. Final Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.
