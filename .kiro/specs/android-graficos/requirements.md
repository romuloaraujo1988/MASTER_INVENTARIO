# Requirements Document - Gráficos e Visualizações Android

## Introduction

O aplicativo Android InventarioMobile precisa de gráficos e visualizações para apresentar estatísticas e métricas de coleta de forma visual e intuitiva. Este documento define os requisitos para implementar gráficos seguindo a arquitetura Clean Architecture + MVVM já estabelecida no projeto.

## Glossary

- **MPAndroidChart**: Biblioteca Android para criação de gráficos
- **Dashboard**: Tela principal com visão geral das estatísticas
- **ViewModel**: Classe que gerencia estado da UI e coordena casos de uso
- **Use Case**: Classe de domínio que contém lógica de negócio específica
- **Repository**: Interface que abstrai acesso a dados
- **State**: Sealed class que representa estados da UI (Loading, Success, Error)
- **DTO**: Data Transfer Object usado na camada de API
- **Entity**: Classe Room que representa dados locais
- **Model**: Classe de domínio pura (sem dependências Android)

## Requirements

### Requirement 1: Dashboard com Estatísticas Gerais

**User Story:** Como usuário do app, quero ver um dashboard com estatísticas gerais do inventário, para ter uma visão rápida do progresso da coleta.

#### Acceptance Criteria

1. WHEN o usuário acessa o dashboard, THE Sistema SHALL exibir cards com métricas principais
2. WHEN as métricas são carregadas, THE Sistema SHALL mostrar: total de patrimônios, coletados, pendentes, percentual de conclusão
3. WHEN não há conexão, THE Sistema SHALL exibir dados do cache local
4. WHEN há erro ao carregar, THE Sistema SHALL exibir mensagem de erro clara
5. WHERE os dados são atualizados, THE Sistema SHALL atualizar automaticamente a UI

### Requirement 2: Gráfico de Pizza - Distribuição de Status

**User Story:** Como usuário, quero ver um gráfico de pizza mostrando a distribuição entre patrimônios coletados e pendentes, para visualizar rapidamente o progresso.

#### Acceptance Criteria

1. WHEN o dashboard é exibido, THE Sistema SHALL mostrar gráfico de pizza com status
2. WHEN o gráfico é renderizado, THE Sistema SHALL usar cores distintas (verde para coletados, vermelho para pendentes)
3. WHEN o usuário toca em uma fatia, THE Sistema SHALL exibir detalhes (quantidade e percentual)
4. WHEN não há dados, THE Sistema SHALL exibir mensagem "Nenhum dado disponível"
5. WHERE há animação, THE Sistema SHALL animar a entrada do gráfico

### Requirement 3: Gráfico de Barras - Coletas por Dia

**User Story:** Como usuário, quero ver um gráfico de barras mostrando coletas realizadas por dia, para acompanhar a evolução diária do trabalho.

#### Acceptance Criteria

1. WHEN o usuário acessa estatísticas, THE Sistema SHALL exibir gráfico de barras por dia
2. WHEN o gráfico é renderizado, THE Sistema SHALL mostrar últimos 7 dias
3. WHEN o usuário toca em uma barra, THE Sistema SHALL exibir quantidade exata do dia
4. WHEN não há coletas em um dia, THE Sistema SHALL exibir barra com valor zero
5. WHERE o período pode ser alterado, THE Sistema SHALL permitir filtrar por semana/mês

### Requirement 4: Gráfico de Linha - Evolução do Progresso

**User Story:** Como usuário, quero ver um gráfico de linha mostrando a evolução do percentual de conclusão, para visualizar a tendência de progresso.

#### Acceptance Criteria

1. WHEN o usuário acessa evolução, THE Sistema SHALL exibir gráfico de linha
2. WHEN o gráfico é renderizado, THE Sistema SHALL mostrar percentual de conclusão ao longo do tempo
3. WHEN o usuário toca em um ponto, THE Sistema SHALL exibir data e percentual
4. WHEN há meta definida, THE Sistema SHALL exibir linha de referência
5. WHERE há previsão, THE Sistema SHALL calcular e exibir data estimada de conclusão

### Requirement 5: Gráfico de Barras Horizontais - Top Coletores

**User Story:** Como usuário, quero ver um ranking dos coletores mais produtivos, para reconhecer o desempenho da equipe.

#### Acceptance Criteria

1. WHEN o usuário acessa ranking, THE Sistema SHALL exibir gráfico de barras horizontais
2. WHEN o gráfico é renderizado, THE Sistema SHALL mostrar top 10 coletores
3. WHEN o usuário toca em uma barra, THE Sistema SHALL exibir detalhes do coletor
4. WHEN há empate, THE Sistema SHALL ordenar por ordem alfabética
5. WHERE o usuário é coletor, THE Sistema SHALL destacar sua posição

### Requirement 6: Gráfico de Pizza - Distribuição por Setor

**User Story:** Como usuário, quero ver a distribuição de patrimônios por setor, para identificar áreas com mais itens.

#### Acceptance Criteria

1. WHEN o usuário acessa distribuição, THE Sistema SHALL exibir gráfico de pizza por setor
2. WHEN o gráfico é renderizado, THE Sistema SHALL usar cores diferentes para cada setor
3. WHEN há mais de 5 setores, THE Sistema SHALL agrupar menores em "Outros"
4. WHEN o usuário toca em uma fatia, THE Sistema SHALL navegar para lista filtrada do setor
5. WHERE não há setores, THE Sistema SHALL exibir mensagem apropriada

### Requirement 7: Gráfico de Barras Empilhadas - Status por Sala

**User Story:** Como usuário, quero ver o status de coleta por sala, para identificar salas que precisam de atenção.

#### Acceptance Criteria

1. WHEN o usuário acessa status por sala, THE Sistema SHALL exibir gráfico de barras empilhadas
2. WHEN o gráfico é renderizado, THE Sistema SHALL mostrar coletados e pendentes por sala
3. WHEN há muitas salas, THE Sistema SHALL permitir scroll horizontal
4. WHEN o usuário toca em uma barra, THE Sistema SHALL exibir detalhes da sala
5. WHERE há filtro, THE Sistema SHALL permitir filtrar por andar/bloco

### Requirement 8: Offline-First para Gráficos

**User Story:** Como usuário, quero que os gráficos funcionem offline, para visualizar estatísticas mesmo sem conexão.

#### Acceptance Criteria

1. WHEN não há conexão, THE Sistema SHALL carregar dados do banco local
2. WHEN os dados locais são usados, THE Sistema SHALL indicar que está offline
3. WHEN a conexão retorna, THE Sistema SHALL sincronizar dados automaticamente
4. WHEN há dados em cache, THE Sistema SHALL exibir timestamp da última atualização
5. WHERE não há dados locais, THE Sistema SHALL exibir mensagem clara

### Requirement 9: Performance e Otimização

**User Story:** Como desenvolvedor, quero que os gráficos sejam performáticos, para garantir boa experiência do usuário.

#### Acceptance Criteria

1. WHEN há muitos dados, THE Sistema SHALL paginar ou limitar resultados
2. WHEN o gráfico é renderizado, THE Sistema SHALL usar cache de cálculos
3. WHEN há animações, THE Sistema SHALL usar animações suaves (60fps)
4. WHEN há scroll, THE Sistema SHALL usar lazy loading
5. WHERE possível, THE Sistema SHALL processar dados em background thread

### Requirement 10: Acessibilidade e UX

**User Story:** Como usuário, quero que os gráficos sejam acessíveis e fáceis de usar, para ter boa experiência visual.

#### Acceptance Criteria

1. WHEN o gráfico é exibido, THE Sistema SHALL usar cores com bom contraste
2. WHEN há texto, THE Sistema SHALL usar tamanhos de fonte legíveis
3. WHEN há interação, THE Sistema SHALL fornecer feedback visual
4. WHEN há erro, THE Sistema SHALL exibir mensagem clara e ação de retry
5. WHERE há loading, THE Sistema SHALL exibir skeleton ou shimmer effect
