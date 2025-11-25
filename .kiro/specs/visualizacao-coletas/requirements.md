# Requirements Document

## Introduction

Este documento especifica os requisitos para a funcionalidade de visualização de coletas no aplicativo Android de inventário patrimonial. A funcionalidade permite aos usuários visualizar, filtrar e analisar todas as coletas realizadas, tanto as próprias quanto as de outros coletores, com informações detalhadas sobre cada patrimônio coletado.

## Glossary

- **Sistema**: Aplicativo Android de Inventário Patrimonial
- **Coleta**: Registro de verificação física de um patrimônio durante um inventário
- **Patrimônio**: Bem material registrado no sistema de inventário
- **Usuário Logado**: Coletor autenticado atualmente utilizando o aplicativo
- **Estado de Conservação**: Condição física do patrimônio (BOM, REGULAR, RUIM)
- **Sincronização**: Processo de envio de dados locais para o servidor remoto
- **Localização Encontrada**: Local físico onde o patrimônio foi encontrado durante a coleta
- **Sala**: Ambiente físico onde patrimônios estão alocados
- **Status de Coleta**: Indicador se o patrimônio foi coletado ou está pendente

## Requirements

### Requirement 1

**User Story:** Como um coletor, eu quero visualizar todas as coletas realizadas, para que eu possa acompanhar o progresso do inventário e verificar quais patrimônios já foram coletados.

#### Acceptance Criteria

1. WHEN o usuário acessa a tela de coletas THEN o Sistema SHALL exibir uma lista de todos os patrimônios coletados
2. WHEN a lista de coletas é exibida THEN o Sistema SHALL mostrar a descrição do patrimônio para cada coleta
3. WHEN a lista de coletas é exibida THEN o Sistema SHALL mostrar a localização encontrada para cada coleta
4. WHEN a lista de coletas é exibida THEN o Sistema SHALL mostrar o estado de conservação para cada coleta
5. WHEN a lista de coletas é exibida THEN o Sistema SHALL indicar se cada coleta está sincronizada com o servidor

### Requirement 2

**User Story:** Como um coletor, eu quero filtrar as coletas por usuário logado, para que eu possa visualizar apenas as minhas próprias coletas e acompanhar meu trabalho individual.

#### Acceptance Criteria

1. WHEN o usuário ativa o filtro por usuário logado THEN o Sistema SHALL exibir apenas coletas realizadas pelo usuário atual
2. WHEN o filtro por usuário logado está ativo THEN o Sistema SHALL ocultar coletas de outros usuários
3. WHEN o usuário desativa o filtro por usuário logado THEN o Sistema SHALL exibir coletas de todos os usuários
4. WHEN o filtro por usuário é alternado THEN o Sistema SHALL atualizar a lista de coletas imediatamente
5. WHEN o filtro por usuário é aplicado THEN o Sistema SHALL atualizar os contadores de totais exibidos

### Requirement 3

**User Story:** Como um coletor, eu quero filtrar as coletas por sala, para que eu possa visualizar o progresso de coleta em ambientes específicos e organizar meu trabalho por localização.

#### Acceptance Criteria

1. WHEN o usuário seleciona uma sala específica THEN o Sistema SHALL exibir apenas coletas de patrimônios dessa sala
2. WHEN nenhuma sala está selecionada THEN o Sistema SHALL exibir coletas de todas as salas
3. WHEN uma sala é selecionada THEN o Sistema SHALL agrupar coletas por sala na visualização
4. WHEN o filtro de sala é aplicado THEN o Sistema SHALL manter outros filtros ativos simultaneamente
5. WHEN uma sala sem coletas é selecionada THEN o Sistema SHALL exibir mensagem indicando ausência de coletas

### Requirement 4

**User Story:** Como um coletor, eu quero filtrar patrimônios por status de coleta (coletado ou pendente), para que eu possa focar nos itens que ainda precisam ser verificados ou revisar os já coletados.

#### Acceptance Criteria

1. WHEN o usuário seleciona o filtro "coletados" THEN o Sistema SHALL exibir apenas patrimônios que foram coletados
2. WHEN o usuário seleciona o filtro "pendentes" THEN o Sistema SHALL exibir apenas patrimônios que não foram coletados
3. WHEN o usuário seleciona "todos" THEN o Sistema SHALL exibir patrimônios coletados e pendentes
4. WHEN o filtro de status é alterado THEN o Sistema SHALL atualizar contadores de totais
5. WHEN o filtro de status é aplicado THEN o Sistema SHALL preservar outros filtros ativos

### Requirement 5

**User Story:** Como um coletor, eu quero ver informações detalhadas de cada coleta, para que eu possa verificar dados completos sobre o patrimônio e a coleta realizada.

#### Acceptance Criteria

1. WHEN uma coleta é exibida THEN o Sistema SHALL mostrar a descrição completa do patrimônio
2. WHEN uma coleta é exibida THEN o Sistema SHALL mostrar a localização onde o patrimônio foi encontrado
3. WHEN uma coleta é exibida THEN o Sistema SHALL mostrar o estado de conservação encontrado
4. WHEN uma coleta é exibida THEN o Sistema SHALL indicar visualmente se está sincronizada
5. WHEN uma coleta é exibida THEN o Sistema SHALL mostrar o nome do coletor responsável

### Requirement 6

**User Story:** Como um coletor, eu quero ver contadores de totais de coletas, para que eu possa acompanhar rapidamente o progresso geral do inventário.

#### Acceptance Criteria

1. WHEN a tela de coletas é exibida THEN o Sistema SHALL mostrar o total de patrimônios coletados
2. WHEN a tela de coletas é exibida THEN o Sistema SHALL mostrar o total de patrimônios pendentes
3. WHEN filtros são aplicados THEN o Sistema SHALL atualizar os contadores para refletir apenas itens filtrados
4. WHEN a lista é recarregada THEN o Sistema SHALL recalcular e atualizar os contadores
5. WHEN não há coletas THEN o Sistema SHALL exibir contadores com valor zero

### Requirement 7

**User Story:** Como um coletor, eu quero que a lista de coletas seja carregada de forma eficiente, para que eu possa acessar as informações rapidamente mesmo com grande volume de dados.

#### Acceptance Criteria

1. WHEN a tela de coletas é aberta THEN o Sistema SHALL exibir indicador de carregamento durante busca de dados
2. WHEN os dados são carregados THEN o Sistema SHALL ocultar o indicador de carregamento
3. WHEN ocorre erro no carregamento THEN o Sistema SHALL exibir mensagem de erro clara
4. WHEN a lista contém muitos itens THEN o Sistema SHALL implementar rolagem eficiente
5. WHEN os dados são atualizados THEN o Sistema SHALL preservar a posição de rolagem do usuário

### Requirement 8

**User Story:** Como um coletor, eu quero que múltiplos filtros funcionem simultaneamente, para que eu possa realizar buscas específicas combinando diferentes critérios.

#### Acceptance Criteria

1. WHEN múltiplos filtros são aplicados THEN o Sistema SHALL aplicar todos os filtros simultaneamente usando operação AND
2. WHEN filtro de usuário e sala são ativos THEN o Sistema SHALL exibir apenas coletas do usuário na sala especificada
3. WHEN filtro de status e sala são ativos THEN o Sistema SHALL exibir apenas patrimônios com status especificado na sala
4. WHEN todos os filtros são ativos THEN o Sistema SHALL aplicar todas as condições de filtragem
5. WHEN filtros são removidos THEN o Sistema SHALL expandir gradualmente os resultados exibidos

### Requirement 9

**User Story:** Como um coletor, eu quero identificar visualmente coletas não sincronizadas, para que eu possa priorizar a sincronização de dados pendentes com o servidor.

#### Acceptance Criteria

1. WHEN uma coleta não está sincronizada THEN o Sistema SHALL exibir indicador visual distintivo
2. WHEN uma coleta está sincronizada THEN o Sistema SHALL exibir indicador de confirmação
3. WHEN o status de sincronização muda THEN o Sistema SHALL atualizar o indicador visual imediatamente
4. WHEN múltiplas coletas não sincronizadas existem THEN o Sistema SHALL destacar todas claramente
5. WHEN o usuário toca no indicador de sincronização THEN o Sistema SHALL exibir detalhes do status

### Requirement 10

**User Story:** Como um coletor, eu quero que os dados sejam agrupados por sala, para que eu possa visualizar a organização espacial das coletas e facilitar a navegação.

#### Acceptance Criteria

1. WHEN a visualização agrupada é ativada THEN o Sistema SHALL agrupar coletas por nome da sala
2. WHEN patrimônios sem sala definida existem THEN o Sistema SHALL agrupá-los em categoria "Sala não definida"
3. WHEN grupos são exibidos THEN o Sistema SHALL mostrar o nome da sala como cabeçalho do grupo
4. WHEN um grupo é exibido THEN o Sistema SHALL mostrar a quantidade de itens no grupo
5. WHEN grupos são ordenados THEN o Sistema SHALL ordenar alfabeticamente por nome da sala
