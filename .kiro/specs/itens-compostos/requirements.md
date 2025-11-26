# Requirements Document

## Introduction

Este documento especifica os requisitos para a funcionalidade de **Itens Compostos** no Sistema de Inventário Patrimonial. A funcionalidade permite gerenciar patrimônios que são compostos por múltiplos itens físicos registrados sob um único número de patrimônio (ex: computador + monitor, mesa + cadeira de sala de aula).

O objetivo é permitir que durante a coleta de inventário, o coletor possa registrar e verificar todos os componentes de um item composto, garantindo a integridade do inventário sem necessidade de reestruturação completa do banco de dados.

## Glossary

- **Item Composto**: Patrimônio que consiste em dois ou mais itens físicos distintos registrados sob um único número de patrimônio
- **Item Principal**: O patrimônio registrado na TABELA_PATRIMONIO que possui componentes associados
- **Componente**: Item físico individual que faz parte de um item composto
- **Sistema**: O Sistema de Inventário Patrimonial (SIHCP)
- **Coletor**: Usuário que realiza a coleta de inventário via app mobile
- **Coleta Completa**: Quando todos os componentes de um item composto foram verificados

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

**User Story:** As a coletor, I want to visualizar os componentes de um item composto durante a coleta, so that eu possa verificar se todos os itens físicos estão presentes.

#### Acceptance Criteria

1. WHEN um coletor escaneia um patrimônio composto THEN THE Sistema SHALL exibir a lista de componentes esperados
2. WHEN a lista de componentes é exibida THEN THE Sistema SHALL mostrar descrição, quantidade esperada e checkbox de verificação para cada componente
3. WHEN um coletor marca um componente como verificado THEN THE Sistema SHALL registrar o status de verificação do componente
4. WHEN todos os componentes são verificados THEN THE Sistema SHALL indicar visualmente que a coleta do item composto está completa

### Requirement 3

**User Story:** As a coletor, I want to registrar componentes faltantes ou extras durante a coleta, so that divergências sejam documentadas.

#### Acceptance Criteria

1. WHEN um componente esperado não é encontrado THEN THE Sistema SHALL permitir marcar o componente como faltante com observação obrigatória
2. WHEN um componente extra é encontrado THEN THE Sistema SHALL permitir adicionar o componente extra com descrição e observação
3. WHEN a coleta de um item composto é finalizada THEN THE Sistema SHALL calcular e exibir o percentual de componentes encontrados
4. IF um item composto possui componentes faltantes THEN THE Sistema SHALL marcar a coleta com status de divergência

### Requirement 4

**User Story:** As a gestor, I want to visualizar relatórios de itens compostos com divergências, so that eu possa tomar ações corretivas.

#### Acceptance Criteria

1. WHEN um gestor acessa o relatório de divergências THEN THE Sistema SHALL listar todos os itens compostos com componentes faltantes ou extras
2. WHEN o relatório é exibido THEN THE Sistema SHALL mostrar número do patrimônio, descrição, componentes esperados, encontrados e faltantes
3. WHEN um gestor exporta o relatório THEN THE Sistema SHALL gerar arquivo em formato Excel com todas as informações de divergência

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

**User Story:** As a sistema, I want to sincronizar dados de componentes entre servidor e app mobile, so that coletores tenham acesso offline aos componentes.

#### Acceptance Criteria

1. WHEN o app mobile sincroniza dados THEN THE Sistema SHALL baixar a lista de componentes de todos os patrimônios compostos
2. WHEN o app está offline THEN THE Sistema SHALL exibir componentes a partir do banco local SQLite
3. WHEN uma coleta de item composto é registrada offline THEN THE Sistema SHALL armazenar o status de cada componente localmente
4. WHEN o app reconecta THEN THE Sistema SHALL sincronizar os status de componentes coletados com o servidor

### Requirement 8

**User Story:** As a administrador, I want to visualizar indicador visual de itens compostos nas listagens, so that eu identifique rapidamente quais patrimônios possuem múltiplos componentes.

#### Acceptance Criteria

1. WHEN a listagem de patrimônios é exibida THEN THE Sistema SHALL mostrar ícone indicador para patrimônios compostos
2. WHEN o indicador é clicado THEN THE Sistema SHALL exibir tooltip com resumo dos componentes
3. WHEN um filtro de "apenas compostos" é aplicado THEN THE Sistema SHALL listar somente patrimônios marcados como compostos
