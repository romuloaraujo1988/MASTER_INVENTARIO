# Requirements Document - Analytics Básico

## Introduction

O módulo de Analytics Básico fornecerá visualizações e métricas essenciais sobre o inventário patrimonial, permitindo que gestores tomem decisões baseadas em dados. O sistema coletará, processará e apresentará informações sobre patrimônios, coletas, responsáveis e tendências ao longo do tempo.

## Glossary

- **Sistema**: Sistema de Histórico e Coleta Patrimonial (SIHCP)
- **Dashboard**: Painel visual com gráficos e métricas
- **KPI**: Key Performance Indicator (Indicador-Chave de Performance)
- **Widget**: Componente visual individual no dashboard (gráfico, card, tabela)
- **Período**: Intervalo de tempo para análise (dia, semana, mês, ano)
- **Drill-down**: Capacidade de clicar em um dado agregado para ver detalhes
- **Patrimônio**: Item físico rastreado pelo sistema
- **Coleta**: Registro de verificação de um patrimônio durante inventário
- **Responsável**: Usuário designado como responsável por patrimônios
- **Setor**: Divisão organizacional que agrupa patrimônios
- **Inventário**: Processo de verificação periódica de patrimônios

## Requirements

### Requirement 1: Dashboard Executivo

**User Story:** Como gestor, eu quero visualizar um dashboard executivo com métricas principais, para que eu possa ter uma visão geral rápida do estado do inventário.

#### Acceptance Criteria

1. WHEN o gestor acessa o dashboard, THE Sistema SHALL exibir cards com as seguintes métricas:
   - Total de patrimônios cadastrados
   - Total de patrimônios coletados no inventário ativo
   - Percentual de conclusão do inventário ativo
   - Total de patrimônios com divergências
   - Valor total do patrimônio (soma dos valores)
   - Número de coletores ativos

2. WHEN o gestor visualiza um card de métrica, THE Sistema SHALL exibir a variação percentual em relação ao período anterior

3. WHEN o gestor clica em um card de métrica, THE Sistema SHALL navegar para uma tela com detalhes daquela métrica

4. WHILE o inventário está em andamento, THE Sistema SHALL atualizar as métricas automaticamente a cada 5 minutos

5. WHERE o gestor tem permissão de administrador, THE Sistema SHALL exibir métricas de todos os setores

### Requirement 2: Gráfico de Evolução de Coletas

**User Story:** Como gestor, eu quero visualizar um gráfico de evolução das coletas ao longo do tempo, para que eu possa acompanhar o progresso do inventário.

#### Acceptance Criteria

1. WHEN o gestor acessa o dashboard, THE Sistema SHALL exibir um gráfico de linha mostrando coletas por dia nos últimos 30 dias

2. WHEN o gestor passa o mouse sobre um ponto do gráfico, THE Sistema SHALL exibir tooltip com data e quantidade de coletas

3. WHERE o gestor seleciona um período customizado, THE Sistema SHALL atualizar o gráfico com dados do período selecionado

4. WHEN o gestor clica em "Exportar", THE Sistema SHALL gerar arquivo PNG ou PDF do gráfico

5. WHILE o gráfico está sendo carregado, THE Sistema SHALL exibir indicador de loading

### Requirement 3: Distribuição de Patrimônios por Setor

**User Story:** Como gestor, eu quero visualizar a distribuição de patrimônios por setor, para que eu possa identificar concentrações e planejar recursos.

#### Acceptance Criteria

1. WHEN o gestor acessa o dashboard, THE Sistema SHALL exibir gráfico de pizza ou barras com distribuição por setor

2. WHEN o gestor clica em uma fatia/barra do gráfico, THE Sistema SHALL exibir lista de patrimônios daquele setor

3. WHERE um setor tem mais de 30% do total, THE Sistema SHALL destacar visualmente esse setor

4. WHEN o gestor seleciona "Ver Tabela", THE Sistema SHALL exibir dados em formato tabular com colunas:
   - Nome do setor
   - Quantidade de patrimônios
   - Percentual do total
   - Valor total
   - Status de coleta

5. WHEN o gestor exporta os dados, THE Sistema SHALL gerar arquivo Excel com a tabela completa

### Requirement 4: Top Responsáveis

**User Story:** Como gestor, eu quero visualizar os responsáveis com mais patrimônios, para que eu possa identificar quem precisa de mais atenção durante o inventário.

#### Acceptance Criteria

1. WHEN o gestor acessa o dashboard, THE Sistema SHALL exibir ranking dos top 10 responsáveis por quantidade de patrimônios

2. WHEN o gestor visualiza o ranking, THE Sistema SHALL exibir para cada responsável:
   - Nome completo
   - Quantidade de patrimônios sob responsabilidade
   - Quantidade coletada
   - Percentual de conclusão
   - Setor

3. WHEN o gestor clica em um responsável, THE Sistema SHALL exibir lista detalhada dos patrimônios daquele responsável

4. WHERE um responsável tem menos de 50% de coletas concluídas, THE Sistema SHALL destacar em vermelho

5. WHEN o gestor clica em "Notificar", THE Sistema SHALL enviar notificação ao responsável selecionado

### Requirement 5: Análise de Divergências

**User Story:** Como auditor, eu quero visualizar análise de divergências encontradas, para que eu possa priorizar ações corretivas.

#### Acceptance Criteria

1. WHEN o auditor acessa a análise de divergências, THE Sistema SHALL exibir gráfico com tipos de divergências:
   - Localização diferente
   - Estado de conservação ruim
   - Patrimônio não encontrado
   - Responsável diferente

2. WHEN o auditor visualiza uma divergência, THE Sistema SHALL exibir:
   - Número do patrimônio
   - Tipo de divergência
   - Valor esperado vs encontrado
   - Data da coleta
   - Coletor responsável
   - Observações

3. WHERE existem mais de 10 divergências do mesmo tipo, THE Sistema SHALL gerar alerta automático

4. WHEN o auditor clica em "Resolver", THE Sistema SHALL abrir formulário para resolução da divergência

5. WHEN o auditor exporta relatório, THE Sistema SHALL gerar PDF com todas as divergências e fotos anexadas

### Requirement 6: Performance de Coletores

**User Story:** Como coordenador de inventário, eu quero visualizar a performance dos coletores, para que eu possa otimizar a distribuição de trabalho.

#### Acceptance Criteria

1. WHEN o coordenador acessa performance de coletores, THE Sistema SHALL exibir tabela com:
   - Nome do coletor
   - Total de coletas realizadas
   - Média de coletas por dia
   - Tempo médio por coleta
   - Taxa de divergências encontradas
   - Última atividade

2. WHEN o coordenador ordena por coluna, THE Sistema SHALL reordenar a tabela mantendo paginação

3. WHERE um coletor está inativo por mais de 3 dias, THE Sistema SHALL destacar em amarelo

4. WHEN o coordenador seleciona período customizado, THE Sistema SHALL recalcular métricas para o período

5. WHEN o coordenador clica em "Comparar", THE Sistema SHALL exibir gráfico comparativo entre coletores selecionados

### Requirement 7: Filtros e Períodos

**User Story:** Como usuário do analytics, eu quero filtrar dados por período e outros critérios, para que eu possa analisar informações específicas.

#### Acceptance Criteria

1. WHEN o usuário acessa qualquer tela de analytics, THE Sistema SHALL exibir barra de filtros com:
   - Seletor de período (últimos 7 dias, 30 dias, 90 dias, customizado)
   - Filtro por setor
   - Filtro por inventário
   - Filtro por status

2. WHEN o usuário altera um filtro, THE Sistema SHALL atualizar todos os widgets do dashboard em até 2 segundos

3. WHERE o usuário seleciona período customizado, THE Sistema SHALL exibir date picker com validação de datas

4. WHEN o usuário clica em "Limpar Filtros", THE Sistema SHALL restaurar valores padrão

5. WHEN o usuário clica em "Salvar Visualização", THE Sistema SHALL salvar configuração de filtros para uso futuro

### Requirement 8: Exportação de Dados

**User Story:** Como gestor, eu quero exportar dados do analytics, para que eu possa usar em apresentações e relatórios externos.

#### Acceptance Criteria

1. WHEN o gestor clica em "Exportar", THE Sistema SHALL exibir opções:
   - Excel (.xlsx)
   - PDF
   - CSV
   - Imagem (PNG)

2. WHEN o gestor seleciona formato Excel, THE Sistema SHALL gerar arquivo com múltiplas abas:
   - Resumo executivo
   - Dados detalhados
   - Gráficos
   - Metadados (período, filtros aplicados)

3. WHEN o gestor seleciona formato PDF, THE Sistema SHALL gerar documento formatado com:
   - Cabeçalho com logo e data
   - Todos os gráficos visíveis
   - Tabelas formatadas
   - Rodapé com paginação

4. WHERE a exportação demora mais de 5 segundos, THE Sistema SHALL exibir barra de progresso

5. WHEN a exportação é concluída, THE Sistema SHALL fazer download automático do arquivo

### Requirement 9: Alertas e Notificações

**User Story:** Como gestor, eu quero receber alertas sobre situações críticas, para que eu possa agir rapidamente.

#### Acceptance Criteria

1. WHEN o inventário está com menos de 30% de conclusão faltando 7 dias para o prazo, THE Sistema SHALL gerar alerta crítico

2. WHEN são encontradas mais de 20 divergências em um dia, THE Sistema SHALL notificar o gestor

3. WHERE um setor tem 0% de coletas após 3 dias de inventário, THE Sistema SHALL gerar alerta

4. WHEN um coletor está inativo por mais de 5 dias, THE Sistema SHALL notificar o coordenador

5. WHEN o gestor acessa o dashboard, THE Sistema SHALL exibir badge com número de alertas não lidos

### Requirement 10: Comparação de Inventários

**User Story:** Como gestor, eu quero comparar resultados entre diferentes inventários, para que eu possa identificar tendências e melhorias.

#### Acceptance Criteria

1. WHEN o gestor acessa comparação de inventários, THE Sistema SHALL exibir seletor para escolher 2 ou mais inventários

2. WHEN o gestor seleciona inventários, THE Sistema SHALL exibir gráficos comparativos de:
   - Tempo total de execução
   - Número de coletas
   - Taxa de divergências
   - Patrimônios não encontrados
   - Performance de coletores

3. WHERE há melhoria significativa (>20%), THE Sistema SHALL destacar em verde

4. WHERE há piora significativa (>20%), THE Sistema SHALL destacar em vermelho

5. WHEN o gestor exporta comparação, THE Sistema SHALL gerar relatório PDF com análise detalhada

### Requirement 11: Valor Patrimonial

**User Story:** Como contador, eu quero visualizar análises de valor patrimonial, para que eu possa gerar relatórios contábeis.

#### Acceptance Criteria

1. WHEN o contador acessa análise de valor, THE Sistema SHALL exibir:
   - Valor total do patrimônio
   - Distribuição de valor por setor
   - Distribuição de valor por categoria
   - Top 10 patrimônios mais valiosos
   - Depreciação acumulada (se disponível)

2. WHEN o contador filtra por período, THE Sistema SHALL calcular valor considerando aquisições e baixas

3. WHERE um patrimônio tem valor acima de R$ 100.000, THE Sistema SHALL destacar como "Alto Valor"

4. WHEN o contador exporta relatório, THE Sistema SHALL gerar Excel com fórmulas para auditoria

5. WHEN o contador clica em "Depreciação", THE Sistema SHALL calcular depreciação linear baseada em vida útil

### Requirement 12: Mobile Analytics

**User Story:** Como gestor mobile, eu quero visualizar analytics no app Android, para que eu possa acompanhar métricas em campo.

#### Acceptance Criteria

1. WHEN o gestor acessa o app mobile, THE Sistema SHALL exibir dashboard simplificado com:
   - Cards de métricas principais
   - Gráfico de evolução de coletas (últimos 7 dias)
   - Lista de alertas críticos

2. WHEN o gestor puxa para atualizar, THE Sistema SHALL recarregar dados do servidor

3. WHERE não há conexão, THE Sistema SHALL exibir última versão em cache com indicador "offline"

4. WHEN o gestor clica em métrica, THE Sistema SHALL exibir detalhes em tela cheia

5. WHEN o gestor compartilha métrica, THE Sistema SHALL gerar imagem para WhatsApp/Email

## Technical Requirements

### Performance
- Dashboard deve carregar em menos de 3 segundos
- Gráficos devem renderizar em menos de 1 segundo
- Exportações devem completar em menos de 10 segundos para até 10.000 registros
- API deve responder em menos de 500ms para queries de analytics

### Escalabilidade
- Suportar até 100.000 patrimônios sem degradação
- Suportar até 50 usuários simultâneos no dashboard
- Cache de dados agregados por 5 minutos

### Segurança
- Apenas usuários com perfil "Gestor" ou superior podem acessar analytics
- Logs de todas as exportações de dados
- Dados sensíveis devem ser mascarados em exportações

### Compatibilidade
- Desktop: Chrome 90+, Firefox 88+, Edge 90+
- Mobile: Android 8.0+, iOS 13+
- Responsivo para tablets

### Acessibilidade
- Gráficos devem ter texto alternativo
- Cores devem ter contraste mínimo 4.5:1
- Navegação por teclado deve funcionar
- Suporte a leitores de tela

## Out of Scope

- Machine Learning e predições avançadas
- Integração com BI tools externos (Power BI, Tableau)
- Analytics em tempo real (< 1 minuto de latência)
- Dashboards customizáveis por usuário
- Análise de sentimento em observações
- Geolocalização e mapas de calor
