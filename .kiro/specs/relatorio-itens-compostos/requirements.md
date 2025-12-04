# Requirements Document

## Introduction

Este documento especifica os requisitos para o **Relatório de Integridade de Itens Compostos** no Sistema de Inventário Patrimonial (aplicação desktop). O relatório permite visualizar e analisar a integridade dos conjuntos patrimoniais, identificando quais patrimônios compostos estão completos (todos os componentes presentes) ou incompletos (componentes faltantes).

O objetivo principal é permitir que a coordenadora e gestores possam:
- Identificar rapidamente quais conjuntos patrimoniais estão incompletos
- Visualizar detalhes dos componentes faltantes em cada conjunto
- Filtrar e ordenar os dados por diferentes critérios
- Exportar os resultados para análise externa
- Tomar decisões sobre reposição de componentes

**Escopo:** Esta funcionalidade será implementada na aplicação desktop Java Swing, utilizando a estrutura de dados existente nas tabelas `tabela_item_composto` e `tabela_coleta_componente`.

## Glossary

- **Item Composto**: Patrimônio que consiste em dois ou mais componentes físicos distintos registrados sob um único número de patrimônio (ex: conjunto mesa + cadeira)
- **Componente**: Item físico individual que faz parte de um item composto (ex: cadeira, mesa, monitor, CPU)
- **Conjunto Completo**: Item composto onde todos os componentes esperados foram encontrados durante a coleta do inventário
- **Conjunto Incompleto**: Item composto com um ou mais componentes faltantes ou com quantidade encontrada menor que a esperada
- **Taxa de Integridade**: Percentual de componentes encontrados em relação ao total esperado para um item composto
- **Sistema**: O Sistema de Inventário Patrimonial (SIHCP) - aplicação desktop
- **Coordenadora**: Usuária gestora que necessita de relatórios sobre integridade dos conjuntos
- **Inventário**: Processo de levantamento patrimonial em andamento ou finalizado

## Requirements

### Requirement 1

**User Story:** As a coordenadora, I want to acessar o relatório de integridade de itens compostos através do menu, so that eu possa visualizar rapidamente o status dos conjuntos patrimoniais.

#### Acceptance Criteria

1. WHEN a coordenadora acessa o menu "Relatórios" THEN THE Sistema SHALL exibir a opção "Relatório de Itens Compostos"
2. WHEN a opção "Relatório de Itens Compostos" é selecionada THEN THE Sistema SHALL abrir uma nova janela com o relatório de integridade
3. WHEN a janela do relatório é aberta THEN THE Sistema SHALL carregar automaticamente os dados do inventário ativo
4. WHEN não existe inventário ativo THEN THE Sistema SHALL exibir mensagem informativa e permitir selecionar um inventário

### Requirement 2

**User Story:** As a coordenadora, I want to filtrar os itens compostos por diferentes critérios, so that eu possa focar na análise de conjuntos específicos.

#### Acceptance Criteria

1. WHEN o relatório é exibido THEN THE Sistema SHALL disponibilizar filtros por: inventário, setor, sala, responsável e status de integridade
2. WHEN um filtro de inventário é selecionado THEN THE Sistema SHALL atualizar a lista mostrando apenas itens compostos daquele inventário
3. WHEN um filtro de status é selecionado (Completo/Incompleto/Todos) THEN THE Sistema SHALL filtrar os resultados pelo status de integridade
4. WHEN múltiplos filtros são aplicados simultaneamente THEN THE Sistema SHALL combinar os filtros usando operação AND lógica
5. WHEN o botão "Limpar Filtros" é clicado THEN THE Sistema SHALL remover todos os filtros e exibir todos os registros

### Requirement 3

**User Story:** As a coordenadora, I want to visualizar uma tabela detalhada dos itens compostos, so that eu possa identificar quais conjuntos estão incompletos e quais componentes faltam.

#### Acceptance Criteria

1. WHEN o relatório é gerado THEN THE Sistema SHALL exibir uma tabela com as colunas: Nº Patrimônio, Descrição, Sala, Responsável, Componentes Esperados, Componentes Encontrados, Componentes Faltantes, Taxa de Integridade e Status
2. WHEN um item composto possui componentes faltantes THEN THE Sistema SHALL destacar a linha com cor vermelha ou laranja
3. WHEN um item composto está completo THEN THE Sistema SHALL destacar a linha com cor verde
4. WHEN a coluna "Componentes Faltantes" é exibida THEN THE Sistema SHALL listar os tipos de componentes que faltam separados por vírgula
5. WHEN a coordenadora clica em uma linha da tabela THEN THE Sistema SHALL exibir um painel de detalhes com todos os componentes do item e seus status individuais

### Requirement 4

**User Story:** As a coordenadora, I want to visualizar estatísticas resumidas no topo do relatório, so that eu tenha uma visão geral rápida da situação dos conjuntos.

#### Acceptance Criteria

1. WHEN o relatório é exibido THEN THE Sistema SHALL mostrar no topo: Total de Conjuntos, Conjuntos Completos, Conjuntos Incompletos e Taxa Geral de Integridade
2. WHEN os filtros são aplicados THEN THE Sistema SHALL atualizar as estatísticas para refletir apenas os dados filtrados
3. WHEN a taxa de integridade geral é menor que 80% THEN THE Sistema SHALL destacar o valor em vermelho como alerta
4. WHEN a taxa de integridade geral é maior ou igual a 80% THEN THE Sistema SHALL exibir o valor em verde

### Requirement 5

**User Story:** As a coordenadora, I want to exportar o relatório de integridade em múltiplos formatos, so that eu possa compartilhar e analisar os dados externamente de diferentes formas.

#### Acceptance Criteria

1. WHEN a coordenadora clica no botão "Exportar" THEN THE Sistema SHALL exibir opções de formato: Excel (.xlsx), PDF e CSV
2. WHEN o formato Excel é selecionado THEN THE Sistema SHALL gerar arquivo com duas abas: "Resumo" com estatísticas gerais e "Detalhamento" com todos os itens
3. WHEN o formato PDF é selecionado THEN THE Sistema SHALL gerar documento formatado com cabeçalho institucional, estatísticas resumidas e tabela de dados
4. WHEN o formato CSV é selecionado THEN THE Sistema SHALL gerar arquivo com separador ponto-e-vírgula e encoding UTF-8
5. WHEN a exportação é concluída THEN THE Sistema SHALL abrir diálogo para salvar o arquivo com nome sugerido contendo data e hora
6. WHEN o arquivo é salvo com sucesso THEN THE Sistema SHALL exibir mensagem de confirmação com o caminho do arquivo

### Requirement 6

**User Story:** As a coordenadora, I want to ordenar os resultados do relatório por diferentes colunas, so that eu possa priorizar a análise dos conjuntos mais críticos.

#### Acceptance Criteria

1. WHEN a coordenadora clica no cabeçalho de uma coluna THEN THE Sistema SHALL ordenar a tabela por aquela coluna em ordem crescente
2. WHEN a coordenadora clica novamente no mesmo cabeçalho THEN THE Sistema SHALL inverter a ordenação para decrescente
3. WHEN a ordenação é aplicada THEN THE Sistema SHALL manter os filtros ativos
4. WHEN a ordenação por "Taxa de Integridade" é selecionada THEN THE Sistema SHALL ordenar numericamente (não alfabeticamente)

### Requirement 7

**User Story:** As a coordenadora, I want to visualizar o detalhamento de componentes de um item composto específico, so that eu possa entender exatamente o que está faltando.

#### Acceptance Criteria

1. WHEN a coordenadora dá duplo clique em uma linha da tabela THEN THE Sistema SHALL abrir um diálogo de detalhamento
2. WHEN o diálogo de detalhamento é aberto THEN THE Sistema SHALL exibir: número do patrimônio, descrição, localização e lista de todos os componentes
3. WHEN a lista de componentes é exibida THEN THE Sistema SHALL mostrar para cada componente: tipo, descrição, quantidade esperada, quantidade encontrada e status
4. WHEN um componente está faltando THEN THE Sistema SHALL destacar a linha do componente em vermelho
5. WHEN o diálogo é fechado THEN THE Sistema SHALL retornar ao relatório principal mantendo a seleção atual

