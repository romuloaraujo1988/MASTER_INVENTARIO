# Implementation Plan: Busca Global Inteligente

## Overview

Este plano implementa um sistema de busca unificado e otimizado para o aplicativo Android de inventário patrimonial, seguindo Clean Architecture + MVVM. A implementação está dividida em 7 fases ao longo de 4 semanas, com foco em performance (< 300ms para 10.000 itens), funcionamento offline-first, e UX otimizada com autocomplete e histórico.

**Tecnologias principais:**
- Room FTS4 para full-text search
- Hilt para injeção de dependência
- Kotlin Coroutines e Flow
- Kotest para property-based testing
- Android Keystore para criptografia

## Tasks

- [ ] 1. Fase 1: Infraestrutura (Semana 1)
  - [ ] 1.1 Criar entities Room para busca
    - Criar `PatrimonioFtsEntity` com anotação @Fts4
    - Criar `SearchHistoryEntity` com índices de timestamp
    - Configurar tokenizer unicode61 para FTS
    - _Requirements: 7.1, 7.2_
  
  - [ ] 1.2 Criar DAOs com queries otimizadas
    - Implementar `PatrimonioSearchDao` com query FTS usando MATCH
    - Implementar `SearchHistoryDao` com limite de 10 itens
    - Adicionar query para sugestões de autocomplete
    - Adicionar query para verificar duplicatas
    - _Requirements: 1.2, 1.3, 2.1, 3.1_
  
  - [ ] 1.3 Criar migration do banco de dados
    - Criar `MIGRATION_15_16` para adicionar tabela FTS
    - Popular índice FTS com dados existentes
    - Criar tabela `search_history` com índices
    - Atualizar `AppDatabase` para versão 16
    - _Requirements: 7.1, 7.2_

  - [ ] 1.4 Configurar módulos Hilt
    - Criar `SearchModule` com providers de configuração
    - Criar `SearchRepositoryModule` com binding de interfaces
    - Configurar provider para LRU cache de sugestões
    - Configurar provider para RelevanceCalculator
    - _Requirements: 1.1, 2.1_
  
  - [ ] 1.5 Implementar utilitários de busca
    - Criar `RelevanceCalculator` com algoritmo de pontuação
    - Criar `SearchQueryPreprocessor` para sanitização de queries
    - Implementar lógica de wildcard para FTS
    - Implementar normalização de queries
    - _Requirements: 1.4, 5.1, 5.2, 5.3_
  
  - [ ]* 1.6 Escrever testes unitários da infraestrutura
    - Testar RelevanceCalculator com diferentes cenários
    - Testar SearchQueryPreprocessor com caracteres especiais
    - Testar DAOs com queries complexas
    - Testar migration do banco de dados

- [ ] 2. Fase 2: Domain Layer (Semana 1-2)
  - [ ] 2.1 Criar interfaces de Repository
    - Criar `SearchRepository` interface com métodos de busca
    - Definir assinaturas para autocomplete e histórico
    - Definir método para rebuild de índice
    - _Requirements: 1.1, 2.1, 3.1_
  
  - [ ] 2.2 Criar models de domínio
    - Criar `PatrimonioSearchResult` com snippet e relevância
    - Criar `SearchSuggestion` com tipo e ícone
    - Criar `SearchHistoryItem` com timestamp
    - Criar `SearchFilters` com validações
    - Criar `MatchedField` para destacar correspondências
    - _Requirements: 1.4, 1.5, 2.1, 6.1_
  
  - [ ] 2.3 Implementar Use Case de busca principal
    - Criar `BuscarPatrimoniosUseCase` com validações
    - Validar query mínima de 2 caracteres
    - Validar query não vazia
    - Aplicar filtros se fornecidos
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [ ]* 2.4 Escrever property test para busca principal
    - **Property 1: Validação de tamanho mínimo de query**
    - **Validates: Requirements 1.1**
  
  - [ ]* 2.5 Escrever property test para busca multi-campo
    - **Property 2: Busca Multi-Campo**
    - **Validates: Requirements 1.2**

  - [ ] 2.6 Implementar Use Case de autocomplete
    - Criar `ObterSugestoesAutocompleteUseCase`
    - Limitar a 5 sugestões
    - Priorizar por correspondência exata, histórico e frequência
    - _Requirements: 2.1, 2.3_
  
  - [ ]* 2.7 Escrever property test para autocomplete
    - **Property 6: Limite de Sugestões de Autocomplete**
    - **Validates: Requirements 2.1**
  
  - [ ] 2.8 Implementar Use Case de histórico
    - Criar `GerenciarHistoricoBuscaUseCase`
    - Implementar método salvar com limite de 10
    - Implementar método obter ordenado por timestamp
    - Implementar métodos remover e limpar
    - _Requirements: 3.1, 3.3, 3.5, 3.6_
  
  - [ ]* 2.9 Escrever property test para histórico
    - **Property 9: Limite de Histórico**
    - **Validates: Requirements 3.1**
  
  - [ ]* 2.10 Escrever property test para ordenação de histórico
    - **Property 10: Ordenação de Histórico**
    - **Validates: Requirements 3.3**
  
  - [ ] 2.11 Implementar Use Case de filtros
    - Criar `AplicarFiltrosUseCase`
    - Implementar filtro por sala
    - Implementar filtro por responsável
    - Implementar filtro por estado
    - Implementar filtro por não coletados
    - _Requirements: 6.2, 6.3_
  
  - [ ]* 2.12 Escrever property test para filtros múltiplos
    - **Property 19: Aplicação de Múltiplos Filtros**
    - **Validates: Requirements 6.2**
  
  - [ ]* 2.13 Escrever testes unitários dos Use Cases
    - Testar validações de entrada
    - Testar casos extremos
    - Testar tratamento de erros

- [ ] 3. Checkpoint - Validar Domain Layer
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Fase 3: Data Layer (Semana 2)
  - [ ] 4.1 Implementar SearchRepository
    - Criar `SearchRepositoryImpl` com offline-first
    - Implementar método search com FTS
    - Implementar cálculo de relevância
    - Implementar ordenação de resultados
    - Implementar extração de campos correspondentes
    - _Requirements: 1.2, 1.3, 1.4, 1.5_

  - [ ]* 4.2 Escrever property test para ordenação por relevância
    - **Property 3: Ordenação por Relevância - Correspondência Exata**
    - **Validates: Requirements 1.4, 5.2**
  
  - [ ] 4.3 Implementar cache de sugestões
    - Configurar LRU cache com limite de 50 itens
    - Implementar getSuggestions com cache hit/miss
    - Implementar invalidação de cache
    - _Requirements: 2.1, 2.2_
  
  - [ ] 4.4 Implementar criptografia de histórico
    - Criar `SearchHistoryCrypto` com Android Keystore
    - Configurar MasterKey com AES256-GCM
    - Implementar métodos encrypt/decrypt
    - Integrar com SearchHistoryDao
    - _Requirements: 13.5, Security_
  
  - [ ]* 4.5 Escrever property test para criptografia
    - **Property 27: Criptografia de Dados Salvos**
    - **Validates: Requirements 13.5**
  
  - [ ] 4.6 Implementar coleta de métricas
    - Criar `SearchMetrics` data class
    - Criar `SearchMetricsCollector`
    - Implementar registro de queries e tempos
    - Implementar limpeza de métricas antigas (> 30 dias)
    - _Requirements: 14.1, 14.2, 14.3_
  
  - [ ]* 4.7 Escrever property test para métricas
    - **Property 28: Registro de Métricas de Busca**
    - **Validates: Requirements 14.1, 14.2, 14.3**
  
  - [ ] 4.8 Implementar graceful degradation
    - Implementar fallback de FTS para LIKE queries
    - Implementar retry com backoff para índice corrompido
    - Implementar reconstrução automática de índice
    - _Requirements: Reliability_
  
  - [ ]* 4.9 Escrever testes do Repository
    - Testar busca com FTS
    - Testar fallback para LIKE
    - Testar cache de sugestões
    - Testar criptografia de histórico
    - Testar coleta de métricas

- [ ] 5. Fase 4: Presentation Layer (Semana 2-3)
  - [ ] 5.1 Criar UI States
    - Criar `SearchState` sealed class (Idle, Loading, Success, Error, Empty)
    - Criar estados para sugestões
    - Criar estados para histórico
    - Criar estados para filtros
    - _Requirements: 8.1, 8.3, 8.4_
  
  - [ ] 5.2 Implementar SearchViewModel
    - Criar `SearchViewModel` com @HiltViewModel
    - Implementar StateFlow para searchState
    - Implementar StateFlow para suggestions
    - Implementar StateFlow para history
    - Implementar StateFlow para appliedFilters
    - _Requirements: 1.1, 2.1, 3.1, 6.1_

  - [ ] 5.3 Implementar debounce de busca
    - Configurar Flow com debounce de 200ms
    - Filtrar queries com menos de 2 caracteres
    - Implementar distinctUntilChanged
    - _Requirements: 1.1, 2.2_
  
  - [ ]* 5.4 Escrever property test para estado de loading
    - **Property 24: Estado de Loading**
    - **Validates: Requirements 8.3**
  
  - [ ] 5.5 Criar SearchFragment com layout
    - Criar layout XML com Material Design 3
    - Adicionar SearchBar com animação de expansão
    - Adicionar RecyclerView para resultados
    - Adicionar RecyclerView para sugestões
    - Adicionar ChipGroup para filtros rápidos
    - Adicionar layout para histórico
    - _Requirements: 8.1, 8.2, 8.5, 8.6_
  
  - [ ] 5.6 Implementar SearchResultAdapter
    - Criar ViewHolder para resultados
    - Implementar destaque de correspondências com Spannable
    - Adicionar indicador visual de patrimônio coletado
    - Implementar click listener para navegação
    - _Requirements: 1.5, 9.1, 9.2, 9.4, 10.1, 10.5_
  
  - [ ]* 5.7 Escrever property test para destaque de correspondências
    - **Property 4: Destaque de Correspondências**
    - **Validates: Requirements 1.5, 9.1, 9.2**
  
  - [ ]* 5.8 Escrever property test para múltiplas ocorrências
    - **Property 5: Destaque de Múltiplas Ocorrências**
    - **Validates: Requirements 9.4**
  
  - [ ] 5.9 Implementar SuggestionAdapter
    - Criar ViewHolder para sugestões
    - Adicionar ícones por tipo de sugestão
    - Implementar click listener para seleção
    - _Requirements: 2.1, 2.4, 2.5_
  
  - [ ]* 5.10 Escrever property test para ícones de sugestões
    - **Property 8: Ícones por Tipo de Sugestão**
    - **Validates: Requirements 2.5**
  
  - [ ] 5.11 Implementar HistoryAdapter
    - Criar ViewHolder para histórico
    - Adicionar botão de remoção individual
    - Implementar click listener para executar busca
    - _Requirements: 3.2, 3.4, 3.5_
  
  - [ ] 5.12 Adicionar animações e transições
    - Implementar animação de expansão do SearchBar (< 200ms)
    - Adicionar transições entre estados
    - Implementar animação de loading
    - _Requirements: 8.2, 8.3_
  
  - [ ]* 5.13 Escrever testes do ViewModel
    - Testar fluxo de busca completo
    - Testar debounce
    - Testar aplicação de filtros
    - Testar gerenciamento de histórico

- [ ] 6. Checkpoint - Validar Presentation Layer
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Fase 5: Integração (Semana 3)
  - [ ] 7.1 Integrar SearchFragment no MainActivity
    - Adicionar SearchFragment ao navigation graph
    - Configurar transição da tela principal
    - Adicionar botão de busca na toolbar
    - _Requirements: 8.1_
  
  - [ ] 7.2 Configurar navegação para detalhes
    - Implementar navegação ao clicar em resultado
    - Passar contexto de busca (query e filtros)
    - Implementar preservação de estado ao retornar
    - Implementar navegação por gestos entre resultados
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [ ]* 7.3 Escrever property test para contexto de navegação
    - **Property 31: Passagem de Contexto na Navegação**
    - **Validates: Requirements 10.2**
  
  - [ ]* 7.4 Escrever property test para preservação de estado
    - **Property 32: Preservação de Estado Após Navegação**
    - **Validates: Requirements 10.3**
  
  - [ ] 7.5 Implementar persistência de estado
    - Salvar query e filtros ao sair da tela
    - Restaurar estado ao retornar
    - Implementar limpeza após 24 horas
    - Adicionar opção de desabilitar nas configurações
    - _Requirements: 13.1, 13.2, 13.3, 13.4_
  
  - [ ]* 7.6 Escrever property test para round-trip de persistência
    - **Property 25: Round-Trip de Persistência de Estado**
    - **Validates: Requirements 13.1, 13.2**
  
  - [ ]* 7.7 Escrever property test para desabilitação de persistência
    - **Property 26: Desabilitação de Persistência**
    - **Validates: Requirements 13.4**
  
  - [ ] 7.8 Adicionar suporte a TalkBack
    - Adicionar contentDescription em todos os componentes
    - Configurar importantForAccessibility
    - Implementar anúncios de mudanças de estado
    - Testar navegação com TalkBack
    - _Requirements: Usability_
  
  - [ ] 7.9 Implementar indicador de modo offline
    - Adicionar flag de offline no SearchState
    - Mostrar indicador visual quando offline
    - Manter performance em modo offline (< 300ms)
    - _Requirements: 4.2, 4.3, 4.4_
  
  - [ ]* 7.10 Escrever property test para busca offline
    - **Property 13: Busca Offline Funcional**
    - **Validates: Requirements 4.1, 4.2**
  
  - [ ]* 7.11 Escrever property test para indicador offline
    - **Property 14: Indicador de Modo Offline**
    - **Validates: Requirements 4.3**
  
  - [ ]* 7.12 Escrever testes de integração end-to-end
    - Testar fluxo completo de busca
    - Testar aplicação de filtros
    - Testar navegação para detalhes
    - Testar persistência de estado
    - Testar modo offline

- [ ] 8. Fase 6: Otimização (Semana 4)
  - [ ] 8.1 Executar testes de performance
    - Testar busca com 1.000 patrimônios (meta: < 100ms)
    - Testar busca com 10.000 patrimônios (meta: < 300ms)
    - Testar busca com 50.000 patrimônios (meta: < 500ms)
    - Testar autocomplete (meta: < 150ms)
    - Testar aplicação de filtros (meta: < 100ms)
    - _Requirements: 1.3, 2.2, 6.3, Performance_
  
  - [ ] 8.2 Otimizar queries do banco
    - Analisar query plans com EXPLAIN QUERY PLAN
    - Adicionar índices compostos se necessário
    - Otimizar queries FTS
    - Verificar uso de índices
    - _Requirements: 7.3, Performance_
  
  - [ ] 8.3 Realizar profiling de memória
    - Usar Android Profiler para análise
    - Verificar consumo de memória (meta: < 100MB)
    - Identificar memory leaks
    - Otimizar cache se necessário
    - _Requirements: Performance_
  
  - [ ] 8.4 Otimizar tamanho do índice FTS
    - Verificar tamanho do índice (meta: < 50MB)
    - Implementar compressão se necessário
    - Implementar limpeza de dados históricos
    - _Requirements: 7.4, 7.5_
  
  - [ ] 8.5 Testar em dispositivos reais
    - Testar em dispositivo Android 6.0 (API 23)
    - Testar em dispositivo Android 14 (API 34)
    - Testar em dispositivo com pouca memória
    - Testar em dispositivo com tela pequena
    - _Requirements: Usability_
  
  - [ ]* 8.6 Escrever property test para pontuação de relevância
    - **Property 15: Pontuação de Relevância Não-Negativa**
    - **Validates: Requirements 5.1**
  
  - [ ]* 8.7 Escrever property test para ordenação por posição
    - **Property 16: Ordenação por Posição de Correspondência**
    - **Validates: Requirements 5.3, 5.4**
  
  - [ ]* 8.8 Escrever property test para desempate alfabético
    - **Property 17: Desempate Alfabético**
    - **Validates: Requirements 5.5**

- [ ] 9. Fase 7: Polimento (Semana 4)
  - [ ] 9.1 Implementar exibição de filtros rápidos
    - Mostrar filtros quando > 10 resultados
    - Implementar contadores de resultados por filtro
    - Implementar remoção individual de filtros
    - Implementar botão "Limpar todos"
    - _Requirements: 6.1, 6.4, 6.5_
  
  - [ ]* 9.2 Escrever property test para exibição de filtros
    - **Property 18: Exibição de Filtros Rápidos**
    - **Validates: Requirements 6.1**
  
  - [ ]* 9.3 Escrever property test para contadores de filtros
    - **Property 20: Contadores de Filtros Precisos**
    - **Validates: Requirements 6.4**

  - [ ]* 9.4 Escrever property test para remoção de filtros
    - **Property 21: Remoção Individual de Filtros**
    - **Validates: Requirements 6.5**
  
  - [ ]* 9.5 Escrever property test para persistência de filtros
    - **Property 22: Persistência de Filtros Entre Buscas**
    - **Validates: Requirements 6.6**
  
  - [ ] 9.6 Implementar priorização de sugestões
    - Implementar ordenação por correspondência exata
    - Implementar boost para histórico
    - Implementar boost para frequência de acesso
    - _Requirements: 2.3_
  
  - [ ]* 9.7 Escrever property test para priorização
    - **Property 7: Priorização de Sugestões**
    - **Validates: Requirements 2.3**
  
  - [ ] 9.8 Implementar atualização automática de índices
    - Adicionar trigger para atualizar FTS ao inserir patrimônio
    - Adicionar trigger para atualizar FTS ao atualizar patrimônio
    - Adicionar trigger para remover de FTS ao deletar patrimônio
    - _Requirements: 7.2_
  
  - [ ]* 9.9 Escrever property test para atualização de índices
    - **Property 23: Atualização Automática de Índices**
    - **Validates: Requirements 7.2**
  
  - [ ] 9.10 Implementar remoção de histórico
    - Implementar remoção de item individual
    - Implementar limpeza completa de histórico
    - Adicionar confirmação antes de limpar tudo
    - _Requirements: 3.5, 3.6_
  
  - [ ]* 9.11 Escrever property test para remoção de histórico
    - **Property 11: Remoção de Item do Histórico**
    - **Validates: Requirements 3.5**
  
  - [ ]* 9.12 Escrever property test para limpeza de histórico
    - **Property 12: Limpeza Completa de Histórico**
    - **Validates: Requirements 3.6**
  
  - [ ] 9.13 Implementar indicador de patrimônio coletado
    - Adicionar flag visual nos resultados
    - Implementar filtro "apenas não coletados"
    - _Requirements: 10.5_
  
  - [ ]* 9.14 Escrever property test para indicador de coletado
    - **Property 33: Indicador de Patrimônios Coletados**
    - **Validates: Requirements 10.5**
  
  - [ ] 9.15 Implementar registro de uso de filtros
    - Registrar filtros aplicados nas métricas
    - Implementar agregação de filtros mais usados
    - _Requirements: 14.4_
  
  - [ ]* 9.16 Escrever property test para registro de filtros
    - **Property 29: Registro de Uso de Filtros**
    - **Validates: Requirements 14.4**
  
  - [ ] 9.17 Implementar desabilitação de métricas
    - Adicionar opção nas configurações
    - Verificar flag antes de coletar métricas
    - _Requirements: 14.6_
  
  - [ ]* 9.18 Escrever property test para desabilitação de métricas
    - **Property 30: Desabilitação de Métricas**
    - **Validates: Requirements 14.6**

  - [ ] 9.19 Ajustes de UX baseados em feedback
    - Revisar mensagens de erro
    - Ajustar cores de destaque
    - Melhorar feedback visual de loading
    - Otimizar tamanho de fontes e espaçamentos
    - _Requirements: 8.4, 9.5_
  
  - [ ] 9.20 Correção de bugs identificados
    - Revisar issues reportados
    - Corrigir bugs críticos
    - Corrigir bugs de UX
    - _Requirements: Reliability_
  
  - [ ] 9.21 Documentação final
    - Documentar arquitetura implementada
    - Documentar APIs públicas
    - Criar guia de uso para desenvolvedores
    - Atualizar README com instruções
    - _Requirements: All_
  
  - [ ] 9.22 Preparação para release
    - Verificar cobertura de testes (meta: > 85%)
    - Executar análise estática de código
    - Verificar acessibilidade
    - Criar release notes
    - _Requirements: All_

- [ ] 10. Checkpoint Final - Validação Completa
  - Ensure all tests pass, ask the user if questions arise.

## Notes

### Tecnologias e Dependências

**Novas dependências necessárias:**
```gradle
// Kotest para property-based testing
testImplementation "io.kotest:kotest-runner-junit5:5.8.0"
testImplementation "io.kotest:kotest-assertions-core:5.8.0"
testImplementation "io.kotest:kotest-property:5.8.0"

// Criptografia
implementation "androidx.security:security-crypto:1.1.0-alpha06"
```

### Arquitetura

A implementação segue **Clean Architecture + MVVM** com três camadas:

1. **Domain Layer**: Use Cases, Repository interfaces, Models puros
2. **Data Layer**: Repository implementations, DAOs, Entities, Mappers
3. **Presentation Layer**: ViewModels, Fragments, Adapters, States

### Performance Targets

- Busca com 10.000 itens: < 300ms (95% das buscas)
- Autocomplete: < 150ms
- Aplicação de filtros: < 100ms
- Consumo de memória: < 100MB
- Tamanho do índice FTS: < 50MB

### Testing Strategy

- **Property-Based Tests**: Validam propriedades universais (33 propriedades)
- **Unit Tests**: Validam exemplos específicos e casos extremos
- **Integration Tests**: Validam fluxos completos end-to-end
- **Performance Tests**: Validam métricas de performance

### Security

- Histórico de buscas criptografado com Android Keystore (AES256-GCM)
- Queries sanitizadas para prevenir SQL injection
- Logs não expõem dados sensíveis
- Respeita permissões de acesso do usuário

### Offline-First

- Room Database com FTS4 para busca local
- Funciona completamente sem conexão
- Sincronização automática em background
- Graceful degradation (fallback para LIKE se FTS falhar)

### Acessibilidade

- Suporte completo a TalkBack
- ContentDescription em todos os componentes
- Navegação por teclado
- Contraste adequado (WCAG AA)

### Métricas de Sucesso

- Taxa de uso da busca: > 80% dos usuários
- Taxa de sucesso: > 90% das buscas retornam resultados
- Uso de autocomplete: > 50% das buscas
- Satisfação do usuário: > 4.5/5
- Crash rate: < 0.1%

---

**Total de tarefas**: 100+ (incluindo sub-tarefas)  
**Tarefas opcionais (testes)**: 33 property tests + testes unitários  
**Checkpoints**: 3 pontos de validação  
**Duração estimada**: 4 semanas  
**Complexidade**: Alta (sistema crítico com requisitos de performance)
