# Implementation Plan

- [x] 1. Criar infraestrutura base do módulo Analytics




  - [x] 1.1 Criar pacote `com.inventario.analytics` com subpacotes (service, dao, model, view)

    - Estrutura: analytics/service, analytics/dao, analytics/model, analytics/view
    - _Requirements: 1.1, 4.1, 7.1_
  - [x] 1.2 Criar DTOs de Analytics (AnalyticsKPIs, DivergenciaAnalytics, MetricasColetorDTO, MetricasPeriodoDTO, RankingSetorDTO)


    - Implementar classes de modelo com getters, setters e builders
    - _Requirements: 1.1, 4.1, 7.1_

  - [x] 1.3 Criar enums TipoDivergencia e GravidadeDivergencia

    - TipoDivergencia: LOCALIZACAO, ESTADO, RESPONSAVEL, VALOR, MANUAL, OUTRO
    - GravidadeDivergencia: CRITICA, ALTA, MEDIA, BAIXA
    - _Requirements: 1.2, 1.3_

- [x] 2. Implementar DivergenciaClassificadorService



  - [x] 2.1 Implementar método classificarDivergenciaLocalizacao()

    - ALTA se mudou de setor, MÉDIA se mudou de sala, BAIXA se apenas descrição
    - _Requirements: 1.2_
  - [x] 2.2 Write property test for classificação de localização


    - **Property 2: Classificação de gravidade de localização é determinística**
    - **Validates: Requirements 1.2**

  - [x] 2.3 Implementar método classificarDivergenciaEstado()
    - CRÍTICA se piorou 2+ níveis, ALTA se piorou 1 nível, BAIXA se melhorou
    - _Requirements: 1.3_

  - [x] 2.4 Write property test for classificação de estado
    - **Property 3: Classificação de gravidade de estado é determinística**

    - **Validates: Requirements 1.3**
  - [x] 2.5 Implementar método detectarTipo()
    - Detectar automaticamente tipo de divergência comparando coleta com patrimônio
    - _Requirements: 1.1_

- [x] 3. Checkpoint - Garantir que todos os testes passam


  - Ensure all tests pass, ask the user if questions arise.



- [x] 4. Implementar AnalyticsDAO

  - [x] 4.1 Implementar buscarDivergenciasComDetalhes()
    - Query SQL com JOINs para trazer patrimônio, setor, responsável, coletor
    - _Requirements: 1.1, 1.5_

  - [x] 4.2 Implementar buscarMetricasTempoAgrupadas()
    - Query com GROUP BY para agrupar por coletor, período ou dia da semana
    - Filtrar outliers (tempo > 0 AND tempo < 600)
    - _Requirements: 4.1, 4.2, 5.1, 5.2_

  - [x] 4.3 Implementar buscarRankingSetores()
    - Query com cálculo de taxa de divergência por setor
    - _Requirements: 3.1, 3.2_

  - [x] 4.4 Implementar buscarRankingColetores()
    - Query com métricas de tempo por coletor ordenado por tempo médio
    - _Requirements: 4.3_

  - [x] 4.5 Implementar buscarKPIsGerais()
    - Query consolidada para KPIs do dashboard
    - _Requirements: 7.1_

  - [x] 4.6 Implementar buscarDivergenciasPorPeriodo()
    - Query para análise temporal com agrupamento por dia
    - _Requirements: 2.1_

- [x] 5. Implementar AnalyticsService
  - [x] 5.1 Implementar calcularKPIs()
    - Calcular taxa de divergência, tempo médio, coletas/hora, coletores ativos
    - Incluir comparação com período anterior
    - _Requirements: 7.1, 7.2_
  - [ ]* 5.2 Write property test for cálculo de KPIs
    - **Property 18: Comparação de KPIs indica direção correta**
    - **Validates: Requirements 7.2**
  - [x] 5.3 Implementar analisarDivergencias()
    - Buscar divergências e classificar gravidade usando DivergenciaClassificadorService
    - _Requirements: 1.1, 1.2, 1.3_
  - [x] 5.4 Implementar agruparDivergenciasPorTipo() e agruparDivergenciasPorGravidade()
    - Agrupar e contar divergências por tipo e gravidade
    - _Requirements: 1.1_
  - [ ]* 5.5 Write property test for agrupamento de divergências
    - **Property 1: Agrupamento de divergências preserva total**
    - **Validates: Requirements 1.1**
  - [x] 5.6 Implementar calcularMetricasPorColetor()
    - Calcular tempo médio, mín, máx, desvio padrão por coletor
    - _Requirements: 4.1_
  - [ ]* 5.7 Write property test for métricas estatísticas
    - **Property 11: Métricas estatísticas calculadas corretamente**
    - **Validates: Requirements 4.1**
  - [ ]* 5.8 Write property test for filtro de outliers
    - **Property 12: Filtro de outliers exclui valores fora do range**
    - **Validates: Requirements 4.2**
  - [x] 5.9 Implementar calcularMetricasPorPeriodo()
    - Classificar coletas por período (Manhã/Tarde/Noite) e calcular métricas
    - _Requirements: 5.1_
  - [ ]* 5.10 Write property test for classificação de período
    - **Property 14: Classificação de período baseada na hora**
    - **Validates: Requirements 5.1**
  - [x] 5.11 Implementar gerarRankingSetores()
    - Gerar ranking ordenado por taxa de divergência decrescente
    - _Requirements: 3.1, 3.2_
  - [ ]* 5.12 Write property test for ranking de setores
    - **Property 8: Ranking de setores está ordenado por taxa decrescente**
    - **Property 9: Taxa de divergência calculada corretamente**
    - **Validates: Requirements 3.1, 3.2**

- [x] 6. Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.
  - ✅ Testes DivergenciaClassificador* passaram com sucesso (06/12/2025)

- [x] 7. Implementar exportação para BI
  - [x] 7.1 Implementar exportarParaCSV()
    - Gerar CSV com todas as métricas, datas em ISO 8601
    - _Requirements: 6.1, 6.2_
  - [ ]* 7.2 Write property test for exportação CSV
    - **Property 16: CSV contém todos os registros**
    - **Property 17: Datas no CSV seguem ISO 8601**
    - **Validates: Requirements 6.1, 6.2**
  - [x] 7.3 Implementar exportarParaExcel()
    - Gerar Excel com 4 abas usando Apache POI
    - Abas: Divergências, Métricas Tempo, Ranking Coletores, Ranking Setores
    - _Requirements: 6.3_
  - [x] 7.4 Implementar exportação em background para grandes volumes
    - Usar SwingWorker para processar em background com barra de progresso
    - ✅ Implementado em AnalyticsDashboardFrame.exportarEmBackground()
    - _Requirements: 6.4_

- [x] 8. Implementar AnalyticsDashboardFrame (UI)
  - [x] 8.1 Criar layout do dashboard com cards de KPIs
    - 4 cards: Taxa Divergência, Tempo Médio, Coletas/Hora, Coletores Ativos
    - Setas indicando melhora/piora em relação ao período anterior
    - _Requirements: 7.1, 7.2_
  - [x] 8.2 Implementar atualização automática de KPIs
    - Timer para atualizar a cada 5 minutos durante inventário ativo
    - _Requirements: 7.4_
  - [x] 8.3 Implementar navegação para análises detalhadas
    - Clique em KPI navega para frame de análise correspondente
    - _Requirements: 7.3_

- [x] 9. Implementar DivergenciasAnalyticsFrame (UI)
  - [x] 9.1 Criar painel de resumo com cards por tipo e gravidade
    - Cards coloridos mostrando contagem e percentual
    - _Requirements: 1.1_
  - [x] 9.2 Criar tabela de divergências com filtros
    - Filtros por tipo, gravidade, setor, responsável
    - _Requirements: 1.4_
  - [ ]* 9.3 Write property test for filtro de gravidade
    - **Property 4: Filtro de gravidade retorna apenas itens da gravidade selecionada**
    - **Validates: Requirements 1.4**
  - [x] 9.4 Criar gráfico temporal de divergências usando JFreeChart
    - Gráfico de linha com séries por tipo de divergência
    - _Requirements: 2.1, 2.2_
  - [ ]* 9.5 Write property test for gráfico temporal
    - **Property 6: Soma de divergências por dia igual ao total**
    - **Property 7: Séries do gráfico contêm apenas divergências do tipo correspondente**
    - **Validates: Requirements 2.1, 2.2**
  - [x] 9.6 Criar painel de ranking de setores e responsáveis
    - Tabelas clicáveis que filtram a tabela principal
    - _Requirements: 3.1, 3.3_
  - [ ]* 9.7 Write property test for filtro por setor
    - **Property 10: Filtro por setor retorna apenas divergências do setor**
    - **Validates: Requirements 3.4**
  - [x] 9.8 Implementar botões de exportação (CSV e Excel)
    - Botões que chamam métodos de exportação do AnalyticsService
    - _Requirements: 1.5, 6.1, 6.3_
  - [ ]* 9.9 Write property test for exportação Excel
    - **Property 5: Exportação Excel contém todos os campos obrigatórios**
    - **Validates: Requirements 1.5**

- [x] 10. Implementar MetricasTempoAnalyticsFrame (UI)
  - [x] 10.1 Criar tabela de métricas por coletor
    - Colunas: Nome, Total Coletas, Tempo Médio, Mín, Máx, Desvio Padrão
    - Destaque verde para 3 mais rápidos, vermelho para 3 mais lentos
    - _Requirements: 4.1, 4.3_
  - [ ]* 10.2 Write property test for ranking de coletores
    - **Property 13: Ranking de coletores ordenado por tempo crescente**
    - **Validates: Requirements 4.3**
  - [x] 10.3 Criar gráfico de barras por período usando JFreeChart
    - Barras para Manhã, Tarde, Noite com quantidade e tempo médio
    - Destaque visual para períodos de baixa produtividade
    - _Requirements: 5.1, 5.4_
  - [ ]* 10.4 Write property test for identificação de baixa produtividade
    - **Property 15: Identificação de baixa produtividade**
    - **Validates: Requirements 5.4**
  - [x] 10.5 Criar gráfico de barras por dia da semana
    - Comparação de produtividade segunda a domingo
    - _Requirements: 5.2_
  - [x] 10.6 Implementar alerta de cobertura de métricas
    - Exibir percentual de coletas com tempo registrado
    - Alertar se menor que 50%
    - _Requirements: 4.4_

- [x] 11. Integrar módulo ao sistema principal

  - [x] 11.1 Adicionar menu "Analytics" no MainFrame
    - Submenu com: Dashboard, Divergências, Métricas de Tempo
    - _Requirements: 7.1_
  - [x] 11.2 Adicionar atalhos de teclado
    - Ctrl+Shift+A para abrir Dashboard de Analytics
    - _Requirements: 7.1_

- [x] 12. Checkpoint Final - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.
  - ✅ Todos os testes passaram (06/12/2025)
  - ✅ Projeto compila sem erros
  - ✅ Módulo Analytics integrado ao MainFrame

