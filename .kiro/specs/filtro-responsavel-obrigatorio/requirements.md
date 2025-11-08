# Requirements Document

## Introduction

Este documento especifica os requisitos para modificar o comportamento da tela de Inventário no aplicativo mobile Android. Atualmente, o sistema carrega todos os patrimônios automaticamente ao abrir a tela, o que causa problemas de performance e usabilidade. A nova funcionalidade exigirá que o usuário selecione um responsável antes de carregar qualquer patrimônio, melhorando a experiência do usuário e a performance do aplicativo.

## Glossary

- **Sistema Mobile**: O aplicativo Android de inventário patrimonial (InventarioMobile)
- **Tela de Inventário**: A activity/fragment que exibe a lista de patrimônios (InventarioActivity)
- **Responsável**: Pessoa física responsável por um ou mais patrimônios
- **Patrimônio**: Item/bem patrimonial cadastrado no sistema
- **ComboBox de Responsáveis**: Componente de interface (AutoCompleteTextView) que permite selecionar um responsável
- **Lista de Patrimônios**: RecyclerView que exibe os patrimônios filtrados
- **Estado Vazio**: Estado inicial da tela onde nenhum patrimônio é exibido
- **Filtro de Status**: Filtro adicional para mostrar patrimônios coletados, não coletados ou todos

## Requirements

### Requirement 1

**User Story:** Como usuário do aplicativo mobile, eu quero que a lista de patrimônios esteja vazia ao abrir a tela de Inventário, para que eu possa escolher qual responsável visualizar sem carregar dados desnecessários.

#### Acceptance Criteria

1. WHEN o usuário abre a tela de Inventário, THE Sistema Mobile SHALL exibir uma lista vazia de patrimônios
2. WHEN o usuário abre a tela de Inventário, THE Sistema Mobile SHALL exibir o ComboBox de Responsáveis carregado com todos os responsáveis disponíveis
3. WHEN o usuário abre a tela de Inventário, THE Sistema Mobile SHALL exibir uma mensagem informativa indicando que é necessário selecionar um responsável
4. WHEN o usuário abre a tela de Inventário, THE Sistema Mobile SHALL exibir o contador de patrimônios como "Total: 0 patrimônios"

### Requirement 2

**User Story:** Como usuário do aplicativo mobile, eu quero selecionar um responsável no ComboBox para visualizar apenas os patrimônios daquela pessoa, para que eu possa focar no inventário específico que preciso realizar.

#### Acceptance Criteria

1. WHEN o usuário seleciona um responsável no ComboBox de Responsáveis, THE Sistema Mobile SHALL carregar os patrimônios daquele responsável específico
2. WHEN o usuário seleciona um responsável no ComboBox de Responsáveis, THE Sistema Mobile SHALL exibir um indicador de carregamento durante a busca dos dados
3. WHEN o carregamento dos patrimônios é concluído, THE Sistema Mobile SHALL exibir a lista de patrimônios na Lista de Patrimônios
4. WHEN o carregamento dos patrimônios é concluído, THE Sistema Mobile SHALL atualizar o contador com o número total de patrimônios carregados
5. IF o responsável selecionado não possui patrimônios, THEN THE Sistema Mobile SHALL exibir uma mensagem informando que não há patrimônios para aquele responsável

### Requirement 3

**User Story:** Como usuário do aplicativo mobile, eu quero que os filtros de status (Todos, Coletados, Não Coletados) funcionem apenas após selecionar um responsável, para que eu possa refinar minha busca dentro dos patrimônios do responsável escolhido.

#### Acceptance Criteria

1. WHILE nenhum responsável está selecionado, THE Sistema Mobile SHALL desabilitar visualmente os Filtros de Status
2. WHEN o usuário seleciona um responsável, THE Sistema Mobile SHALL habilitar os Filtros de Status
3. WHEN o usuário aplica um Filtro de Status após selecionar um responsável, THE Sistema Mobile SHALL filtrar os patrimônios já carregados de acordo com o status selecionado
4. WHEN o usuário altera o Filtro de Status, THE Sistema Mobile SHALL atualizar a Lista de Patrimônios imediatamente sem recarregar do servidor
5. WHEN o usuário altera o Filtro de Status, THE Sistema Mobile SHALL atualizar o contador de patrimônios com o total filtrado

### Requirement 4

**User Story:** Como usuário do aplicativo mobile, eu quero que o botão "Limpar Filtros" remova a seleção do responsável e limpe a lista, para que eu possa começar uma nova busca do zero.

#### Acceptance Criteria

1. WHEN o usuário clica no botão "Limpar Filtros", THE Sistema Mobile SHALL remover a seleção do ComboBox de Responsáveis
2. WHEN o usuário clica no botão "Limpar Filtros", THE Sistema Mobile SHALL limpar a Lista de Patrimônios retornando ao Estado Vazio
3. WHEN o usuário clica no botão "Limpar Filtros", THE Sistema Mobile SHALL resetar o Filtro de Status para "Todos"
4. WHEN o usuário clica no botão "Limpar Filtros", THE Sistema Mobile SHALL desabilitar novamente os Filtros de Status
5. WHEN o usuário clica no botão "Limpar Filtros", THE Sistema Mobile SHALL exibir novamente a mensagem informativa para selecionar um responsável

### Requirement 5

**User Story:** Como usuário do aplicativo mobile, eu quero que a paginação funcione corretamente ao selecionar um responsável, para que eu possa navegar por grandes listas de patrimônios sem problemas de performance.

#### Acceptance Criteria

1. WHEN o usuário seleciona um responsável com mais de 20 patrimônios, THE Sistema Mobile SHALL carregar os primeiros 20 patrimônios
2. WHEN o usuário faz scroll até próximo ao final da lista, THE Sistema Mobile SHALL carregar automaticamente os próximos 20 patrimônios
3. WHEN o Sistema Mobile está carregando mais patrimônios, THE Sistema Mobile SHALL exibir um indicador de carregamento no final da lista
4. WHEN todos os patrimônios do responsável foram carregados, THE Sistema Mobile SHALL desabilitar o carregamento automático de mais itens
5. WHEN o usuário seleciona um novo responsável, THE Sistema Mobile SHALL resetar a paginação para a primeira página

### Requirement 6

**User Story:** Como usuário do aplicativo mobile, eu quero que o sistema mantenha a seleção do responsável ao rotacionar a tela ou retornar de outra activity, para que eu não perca meu contexto de trabalho.

#### Acceptance Criteria

1. WHEN o usuário rotaciona o dispositivo, THE Sistema Mobile SHALL manter o responsável selecionado
2. WHEN o usuário rotaciona o dispositivo, THE Sistema Mobile SHALL manter os patrimônios carregados na Lista de Patrimônios
3. WHEN o usuário retorna de outra tela usando o botão voltar, THE Sistema Mobile SHALL manter o responsável selecionado
4. WHEN o usuário retorna de outra tela usando o botão voltar, THE Sistema Mobile SHALL manter os patrimônios carregados na Lista de Patrimônios
5. WHEN o usuário usa o gesto de swipe-to-refresh, THE Sistema Mobile SHALL recarregar os patrimônios do responsável atualmente selecionado

### Requirement 7

**User Story:** Como desenvolvedor do sistema, eu quero que o ViewModel não carregue patrimônios automaticamente no init, para que o carregamento seja controlado pela seleção do usuário e melhore a performance do aplicativo.

#### Acceptance Criteria

1. WHEN o ViewModel é inicializado, THE Sistema Mobile SHALL carregar apenas a lista de responsáveis
2. WHEN o ViewModel é inicializado, THE Sistema Mobile SHALL NOT carregar nenhum patrimônio automaticamente
3. WHEN o ViewModel é inicializado, THE Sistema Mobile SHALL definir o estado inicial como Estado Vazio
4. WHEN o método loadPatrimonios é chamado sem parâmetros, THE Sistema Mobile SHALL NOT executar nenhuma ação
5. WHEN o método loadPatrimoniosByResponsavel é chamado com um ID válido, THE Sistema Mobile SHALL carregar os patrimônios daquele responsável
