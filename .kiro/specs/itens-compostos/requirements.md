# Requirements Document

## Introduction

Este documento especifica os requisitos para a funcionalidade de **Itens Compostos** no Sistema de Inventário Patrimonial (aplicação desktop). A funcionalidade permite gerenciar patrimônios que são compostos por múltiplos itens físicos registrados sob um único número de patrimônio (ex: conjunto mesa + cadeira, estação de trabalho completa).

O objetivo principal é permitir que a coordenadora e gestores possam:
- Cadastrar e gerenciar patrimônios compostos por múltiplos componentes
- Gerar relatórios detalhados para identificar ausências de componentes em conjuntos
- Obter estatísticas sobre a integridade dos conjuntos patrimoniais
- Tomar decisões baseadas em dados sobre reposição e manutenção

**Escopo:** Esta funcionalidade será implementada **apenas na aplicação desktop Java Swing**, preservando a estrutura atual do banco de dados e sem impactar o aplicativo mobile.

## Glossary

- **Item Composto**: Patrimônio que consiste em dois ou mais itens físicos distintos registrados sob um único número de patrimônio
- **Item Principal**: O patrimônio registrado na TABELA_PATRIMONIO que possui componentes associados
- **Componente**: Item físico individual que faz parte de um item composto (ex: cadeira, mesa, monitor)
- **Sistema**: O Sistema de Inventário Patrimonial (SIHCP) - aplicação desktop
- **Coordenadora**: Usuária gestora que necessita de relatórios sobre integridade dos conjuntos
- **Conjunto Completo**: Item composto onde todos os componentes esperados foram encontrados durante coleta
- **Conjunto Incompleto**: Item composto com um ou mais componentes faltantes
- **Taxa de Integridade**: Percentual de componentes encontrados em relação ao total esperado

## Requirements

### Requirement 1

**User Story:** As a administrador, I want to marcar patrimônios como compostos e definir seus componentes, so that o sistema saiba quais itens físicos pertencem a cada patrimônio.

#### Acceptance Criteria

1. WHEN um administrador acessa a edição de um patrimônio THEN THE Sistema SHALL exibir uma opção para marcar o patrimônio como composto
2. WHEN um patrimônio é marcado como composto THEN THE Sistema SHALL permitir adicionar componentes com descrição, quantidade e tipo
3. WHEN um componente é adicionado THEN THE Sistema SHALL validar que a descrição do componente possui pelo menos 3 caracteres
4. WHEN um administrador remove um componente THEN THE Sistema SHALL atualizar a lista de componentes imediatamente
5. WHEN um patrimônio composto é salvo THEN THE Sistema SHALL persistir todos os componentes associados no banco de dados

### Requirement 2

**User Story:** As a coordenadora, I want to visualizar relatórios detalhados de itens compostos, so that eu possa identificar quais conjuntos estão incompletos e tomar ações corretivas.

#### Acceptance Criteria

1. WHEN a coordenadora acessa o menu de relatórios THEN THE Sistema SHALL exibir opção específica para "Relatório de Itens Compostos"
2. WHEN o relatório de itens compostos é aberto THEN THE Sistema SHALL exibir filtros por inventário, setor, sala e status de integridade
3. WHEN o relatório é gerado THEN THE Sistema SHALL listar todos os itens compostos com suas informações: número patrimônio, descrição, localização, componentes esperados, componentes encontrados e componentes faltantes
4. WHEN um item composto possui componentes faltantes THEN THE Sistema SHALL destacar visualmente a linha com cor diferenciada
5. WHEN o relatório é exibido THEN THE Sistema SHALL mostrar estatísticas resumidas no topo: total de conjuntos, conjuntos completos, conjuntos incompletos e taxa geral de integridade

### Requirement 3

**User Story:** As a coordenadora, I want to exportar relatórios de itens compostos em múltiplos formatos, so that eu possa compartilhar e analisar os dados externamente.

#### Acceptance Criteria

1. WHEN a coordenadora clica em exportar THEN THE Sistema SHALL oferecer opções de formato: Excel (.xlsx), PDF e CSV
2. WHEN o formato Excel é selecionado THEN THE Sistema SHALL gerar planilha com abas separadas: "Resumo Geral", "Conjuntos Completos", "Conjuntos Incompletos" e "Detalhamento por Componente"
3. WHEN o formato PDF é selecionado THEN THE Sistema SHALL gerar documento formatado com gráficos de pizza mostrando taxa de integridade
4. WHEN a exportação é concluída THEN THE Sistema SHALL abrir diálogo para salvar o arquivo com nome sugerido contendo data e hora
5. WHEN o arquivo é salvo THEN THE Sistema SHALL exibir mensagem de sucesso com caminho do arquivo

### Requirement 4

**User Story:** As a coordenadora, I want to visualizar estatísticas agregadas por tipo de componente, so that eu possa identificar padrões de ausência (ex: sempre faltam cadeiras).

#### Acceptance Criteria

1. WHEN o relatório estatístico é acessado THEN THE Sistema SHALL agrupar dados por tipo de componente (cadeira, mesa, monitor, etc)
2. WHEN os dados são agrupados THEN THE Sistema SHALL calcular para cada tipo: total esperado, total encontrado, total faltante e taxa de presença
3. WHEN a análise é exibida THEN THE Sistema SHALL ordenar componentes por taxa de ausência (maior para menor)
4. WHEN um tipo de componente é selecionado THEN THE Sistema SHALL detalhar quais patrimônios específicos possuem aquele componente faltante
5. WHEN o relatório estatístico é exportado THEN THE Sistema SHALL incluir gráfico de barras comparando presença vs ausência por tipo de componente

### Requirement 5

**User Story:** As a administrador, I want to que o sistema detecte automaticamente patrimônios compostos baseado na descrição, so that eu não precise marcar manualmente cada item.

#### Acceptance Criteria

1. WHEN um patrimônio é importado ou cadastrado THEN THE Sistema SHALL analisar a descrição em busca de padrões de itens compostos
2. WHEN a descrição contém palavras-chave como "COM", "E", "+" seguidas de outro item THEN THE Sistema SHALL sugerir que o patrimônio é composto
3. WHEN padrões como "MESA COM CADEIRA", "COMPUTADOR E MONITOR", "ESTAÇÃO DE TRABALHO" são detectados THEN THE Sistema SHALL extrair automaticamente os componentes sugeridos
4. WHEN componentes são sugeridos automaticamente THEN THE Sistema SHALL permitir que o administrador confirme, edite ou rejeite a sugestão
5. WHEN um administrador executa a detecção em lote THEN THE Sistema SHALL processar todos os patrimônios e listar os candidatos a itens compostos

### Requirement 6

**User Story:** As a administrador, I want to configurar padrões de detecção de itens compostos, so that o sistema reconheça os tipos específicos da minha instituição.

#### Acceptance Criteria

1. WHEN um administrador acessa configurações de detecção THEN THE Sistema SHALL exibir lista de padrões configurados
2. WHEN um novo padrão é adicionado THEN THE Sistema SHALL validar que o padrão possui descrição principal e lista de componentes
3. WHEN um padrão é configurado THEN THE Sistema SHALL aplicar automaticamente em futuras importações
4. WHEN um administrador testa um padrão THEN THE Sistema SHALL mostrar quantos patrimônios existentes seriam afetados

### Requirement 7

**User Story:** As a coordenadora, I want to filtrar relatórios por múltiplos critérios simultaneamente, so that eu possa fazer análises específicas e direcionadas.

#### Acceptance Criteria

1. WHEN a coordenadora aplica filtros THEN THE Sistema SHALL permitir combinação de: inventário, setor, sala, responsável, status de integridade e tipo de componente faltante
2. WHEN múltiplos filtros são aplicados THEN THE Sistema SHALL atualizar o relatório em tempo real mostrando apenas registros que atendem todos os critérios
3. WHEN um filtro é removido THEN THE Sistema SHALL recarregar dados automaticamente
4. WHEN filtros são aplicados THEN THE Sistema SHALL exibir contador de registros filtrados vs total
5. WHEN a coordenadora salva uma configuração de filtros THEN THE Sistema SHALL permitir nomear e reutilizar essa configuração em sessões futuras

### Requirement 8

**User Story:** As a administrador, I want to visualizar indicador visual de itens compostos nas listagens, so that eu identifique rapidamente quais patrimônios possuem múltiplos componentes.

#### Acceptance Criteria

1. WHEN a listagem de patrimônios é exibida THEN THE Sistema SHALL mostrar ícone indicador para patrimônios compostos
2. WHEN o indicador é clicado THEN THE Sistema SHALL exibir tooltip com resumo dos componentes
3. WHEN um filtro de "apenas compostos" é aplicado THEN THE Sistema SHALL listar somente patrimônios marcados como compostos
4. WHEN um patrimônio composto é visualizado THEN THE Sistema SHALL exibir badge com contagem de componentes (ex: "3 componentes")
5. WHEN um patrimônio composto possui componentes faltantes THEN THE Sistema SHALL exibir ícone de alerta adicional

### Requirement 9

**User Story:** As a coordenadora, I want to comparar integridade de conjuntos entre diferentes inventários, so that eu possa identificar tendências de perda ou deterioração ao longo do tempo.

#### Acceptance Criteria

1. WHEN a coordenadora acessa análise comparativa THEN THE Sistema SHALL permitir selecionar dois ou mais inventários para comparação
2. WHEN inventários são selecionados THEN THE Sistema SHALL exibir tabela comparativa mostrando taxa de integridade de cada conjunto em cada inventário
3. WHEN a comparação é exibida THEN THE Sistema SHALL destacar conjuntos que tiveram piora na integridade (componentes que estavam presentes e agora faltam)
4. WHEN um conjunto específico é selecionado THEN THE Sistema SHALL exibir linha do tempo mostrando histórico de presença/ausência de cada componente
5. WHEN a análise comparativa é exportada THEN THE Sistema SHALL gerar relatório com gráficos de evolução temporal da integridade

### Requirement 10

**User Story:** As a administrador, I want to aplicar configuração de componentes em lote por descrição, so that eu não precise configurar cada patrimônio individualmente.

#### Acceptance Criteria

1. WHEN o administrador acessa detecção em lote THEN THE Sistema SHALL agrupar patrimônios por descrição única e exibir contagem de patrimônios para cada descrição
2. WHEN uma descrição é selecionada THEN THE Sistema SHALL analisar automaticamente e sugerir componentes baseado em padrões detectados
3. WHEN o administrador edita os componentes sugeridos THEN THE Sistema SHALL permitir adicionar, remover ou modificar componentes antes da aplicação
4. WHEN o administrador confirma os componentes THEN THE Sistema SHALL aplicar a configuração em TODOS os patrimônios com aquela descrição em uma única transação
5. WHEN a aplicação em lote é executada THEN THE Sistema SHALL exibir barra de progresso em tempo real e permitir cancelamento da operação
6. WHEN a aplicação é concluída THEN THE Sistema SHALL exibir resumo detalhado: quantidade de patrimônios configurados, quantidade de componentes criados e tempo de execução
7. WHEN ocorre erro durante aplicação em lote THEN THE Sistema SHALL fazer rollback de todas as mudanças e exibir mensagem de erro detalhada

### Requirement 10

**User Story:** As a administrador, I want to aplicar configuração de componentes em lote por descrição, so that eu não precise configurar cada patrimônio individualmente.

#### Acceptance Criteria

1. WHEN o administrador acessa detecção em lote THEN THE Sistema SHALL agrupar patrimônios por descrição única e exibir contagem de patrimônios para cada descrição
2. WHEN uma descrição é selecionada THEN THE Sistema SHALL analisar automaticamente e sugerir componentes baseado em padrões detectados
3. WHEN o administrador edita os componentes sugeridos THEN THE Sistema SHALL permitir adicionar, remover ou modificar componentes antes de aplicar
4. WHEN o administrador confirma os componentes THEN THE Sistema SHALL aplicar a configuração em TODOS os patrimônios com aquela descrição exata em uma única transação
5. WHEN a aplicação em lote é executada THEN THE Sistema SHALL exibir barra de progresso em tempo real e permitir cancelamento da operação
6. WHEN a aplicação é concluída THEN THE Sistema SHALL exibir resumo detalhado: quantidade de patrimônios configurados, quantidade de componentes criados e tempo de execução
