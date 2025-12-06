# Requirements Document

## Introduction

Este documento especifica os requisitos para o módulo de Analytics Avançado do Sistema de Inventário, focando em duas áreas críticas: **Relatório de Divergências Aprimorado** e **Métricas de Tempo de Coleta**. O objetivo é fornecer insights acionáveis para gestores sobre a qualidade do inventário e a produtividade das equipes de coleta.

O sistema já possui um relatório básico de divergências e colunas de métricas de tempo no banco de dados. Esta especificação visa expandir essas funcionalidades com análises mais profundas, visualizações gráficas e exportação de dados para ferramentas de BI.

## Glossary

- **Sistema_Analytics**: Módulo de análise de dados do Sistema de Inventário
- **Divergência**: Diferença entre dados cadastrados e dados encontrados durante a coleta (localização, estado de conservação, responsável)
- **Tempo_Coleta**: Duração em segundos desde o início até a conclusão de uma coleta individual
- **Tempo_Scan**: Duração em segundos do processo de leitura do QR Code
- **Coletor**: Usuário que realiza coletas de patrimônios via aplicativo móvel
- **Inventário_Ativo**: Inventário em andamento selecionado para análise
- **KPI**: Key Performance Indicator - indicador chave de desempenho
- **Taxa_Divergência**: Percentual de coletas com divergências em relação ao total de coletas

## Requirements

### Requirement 1

**User Story:** As a gestor de inventário, I want to visualizar divergências categorizadas por tipo e gravidade, so that I can priorizar ações corretivas nos patrimônios mais críticos.

#### Acceptance Criteria

1. WHEN o usuário abre o relatório de divergências THEN o Sistema_Analytics SHALL exibir divergências agrupadas por tipo (Localização, Estado, Responsável, Valor) com contagem e percentual de cada categoria
2. WHEN o Sistema_Analytics detecta uma divergência de localização THEN o Sistema_Analytics SHALL classificar a gravidade como ALTA se o patrimônio mudou de setor, MÉDIA se mudou de sala no mesmo setor, e BAIXA se apenas a descrição da localização difere
3. WHEN o Sistema_Analytics detecta uma divergência de estado de conservação THEN o Sistema_Analytics SHALL classificar a gravidade como CRÍTICA se o estado piorou 2 ou mais níveis, ALTA se piorou 1 nível, e BAIXA se melhorou
4. WHEN o usuário filtra por gravidade THEN o Sistema_Analytics SHALL exibir apenas divergências da gravidade selecionada mantendo os totais atualizados
5. WHEN o usuário exporta o relatório THEN o Sistema_Analytics SHALL gerar arquivo Excel com todas as divergências incluindo tipo, gravidade, valores cadastrados, valores encontrados e data da coleta

### Requirement 2

**User Story:** As a gestor de inventário, I want to analisar tendências de divergências ao longo do tempo, so that I can identificar padrões e tomar medidas preventivas.

#### Acceptance Criteria

1. WHEN o usuário seleciona análise temporal THEN o Sistema_Analytics SHALL exibir gráfico de linha mostrando quantidade de divergências por dia durante o período do inventário
2. WHEN o Sistema_Analytics gera o gráfico temporal THEN o Sistema_Analytics SHALL separar as linhas por tipo de divergência permitindo comparação visual
3. WHEN o usuário passa o mouse sobre um ponto do gráfico THEN o Sistema_Analytics SHALL exibir tooltip com data, tipo de divergência e quantidade exata
4. WHEN existem mais de 30 dias de dados THEN o Sistema_Analytics SHALL permitir zoom e navegação no gráfico mantendo a legibilidade

### Requirement 3

**User Story:** As a gestor de inventário, I want to identificar setores e responsáveis com maior taxa de divergências, so that I can direcionar treinamentos e auditorias.

#### Acceptance Criteria

1. WHEN o usuário acessa análise por setor THEN o Sistema_Analytics SHALL exibir ranking de setores ordenado por taxa de divergência decrescente
2. WHEN o Sistema_Analytics calcula taxa de divergência por setor THEN o Sistema_Analytics SHALL dividir quantidade de divergências pela quantidade de coletas do setor multiplicando por 100
3. WHEN o usuário acessa análise por responsável THEN o Sistema_Analytics SHALL exibir ranking de responsáveis com patrimônios divergentes ordenado por quantidade decrescente
4. WHEN o usuário clica em um setor ou responsável do ranking THEN o Sistema_Analytics SHALL filtrar a tabela de divergências mostrando apenas os itens relacionados

### Requirement 4

**User Story:** As a gestor de inventário, I want to visualizar métricas de tempo de coleta por coletor, so that I can avaliar produtividade e identificar necessidades de treinamento.

#### Acceptance Criteria

1. WHEN o usuário acessa métricas de tempo THEN o Sistema_Analytics SHALL exibir tempo médio de coleta, tempo mínimo, tempo máximo e desvio padrão por coletor
2. WHEN o Sistema_Analytics calcula tempo médio THEN o Sistema_Analytics SHALL considerar apenas coletas com tempo_coleta_segundos maior que zero e menor que 600 segundos para excluir outliers
3. WHEN o usuário visualiza ranking de coletores THEN o Sistema_Analytics SHALL ordenar por tempo médio crescente destacando os 3 mais rápidos em verde e os 3 mais lentos em vermelho
4. WHEN existem coletas sem dados de tempo THEN o Sistema_Analytics SHALL exibir percentual de coletas com métricas disponíveis e alertar se menor que 50%

### Requirement 5

**User Story:** As a gestor de inventário, I want to analisar produtividade de coleta por período do dia, so that I can otimizar escalas de trabalho.

#### Acceptance Criteria

1. WHEN o usuário acessa análise por período THEN o Sistema_Analytics SHALL exibir gráfico de barras com quantidade de coletas e tempo médio por período (Manhã 6-12h, Tarde 12-18h, Noite 18-6h)
2. WHEN o Sistema_Analytics gera análise por dia da semana THEN o Sistema_Analytics SHALL exibir gráfico comparando produtividade entre segunda a domingo
3. WHEN o usuário seleciona um período específico THEN o Sistema_Analytics SHALL exibir detalhamento com lista de coletores ativos e suas métricas naquele período
4. WHEN o Sistema_Analytics identifica período com tempo médio 50% maior que a média geral THEN o Sistema_Analytics SHALL destacar visualmente como período de baixa produtividade

### Requirement 6

**User Story:** As a gestor de inventário, I want to exportar dados de analytics para ferramentas de BI externas, so that I can criar dashboards personalizados e integrar com outros sistemas.

#### Acceptance Criteria

1. WHEN o usuário solicita exportação para BI THEN o Sistema_Analytics SHALL gerar arquivo CSV com todas as métricas de divergências e tempo de coleta
2. WHEN o Sistema_Analytics gera CSV THEN o Sistema_Analytics SHALL incluir cabeçalhos descritivos e formatar datas no padrão ISO 8601 (yyyy-MM-dd HH:mm:ss)
3. WHEN o usuário seleciona exportação Excel THEN o Sistema_Analytics SHALL gerar arquivo .xlsx com múltiplas abas (Divergências, Métricas Tempo, Ranking Coletores, Ranking Setores)
4. WHEN a exportação contém mais de 10000 registros THEN o Sistema_Analytics SHALL processar em background exibindo barra de progresso e notificando ao concluir

### Requirement 7

**User Story:** As a gestor de inventário, I want to visualizar um dashboard consolidado de KPIs, so that I can ter visão rápida da saúde do inventário.

#### Acceptance Criteria

1. WHEN o usuário abre o módulo de analytics THEN o Sistema_Analytics SHALL exibir dashboard com KPIs principais: Taxa de Divergência Geral, Tempo Médio de Coleta, Coletas por Hora, Coletores Ativos
2. WHEN o Sistema_Analytics atualiza KPIs THEN o Sistema_Analytics SHALL comparar com período anterior exibindo seta verde se melhorou e vermelha se piorou
3. WHEN o usuário clica em um KPI THEN o Sistema_Analytics SHALL navegar para a análise detalhada correspondente
4. WHEN o inventário está em andamento THEN o Sistema_Analytics SHALL atualizar KPIs automaticamente a cada 5 minutos sem necessidade de refresh manual

