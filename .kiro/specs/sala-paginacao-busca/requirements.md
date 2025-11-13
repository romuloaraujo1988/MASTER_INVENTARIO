# Requirements Document - Paginação e Busca de Salas

## Introduction

Este documento define os requisitos para otimizar o carregamento de salas no aplicativo móvel Android, implementando paginação com scroll infinito e campo de busca para melhorar a experiência do usuário e reduzir o tempo de espera.

## Problem Statement

Atualmente, o carregamento de todas as salas leva mais de 5 segundos, causando frustração no usuário. A solução implementará:
- **Paginação**: Carregar salas em lotes de 20 itens
- **Scroll Infinito**: Carregar mais itens automaticamente ao rolar
- **Busca em Tempo Real**: Filtrar salas enquanto o usuário digita
- **Cache Local**: Armazenar salas no Room para acesso offline

## Glossary

- **Paginação**: Técnica de carregar dados em páginas/lotes menores
- **Scroll Infinito**: Carregamento automático ao atingir o fim da lista
- **Debounce**: Atraso na busca para evitar requisições excessivas
- **Room**: Biblioteca de persistência local do Android
- **Paging 3**: Biblioteca Android para paginação

## Requirements

### Requirement 1

**User Story:** Como um coletor, eu quero que a lista de salas carregue rapidamente, para que eu possa começar a trabalhar sem esperar

#### Acceptance Criteria

1. WHEN o coletor abre a tela de seleção de salas, THE Sistema Mobile SHALL carregar as primeiras 20 salas em menos de 1 segundo
2. WHEN o coletor rola a lista até o final, THE Sistema Mobile SHALL carregar automaticamente as próximas 20 salas
3. WHILE novas salas estão sendo carregadas, THE Sistema Mobile SHALL exibir indicador de loading no final da lista
4. WHEN todas as salas foram carregadas, THE Sistema Mobile SHALL exibir mensagem "Todas as salas carregadas"
5. IF ocorrer erro no carregamento, THEN THE Sistema Mobile SHALL exibir mensagem de erro e botão "Tentar novamente"

### Requirement 2

**User Story:** Como um coletor, eu quero buscar salas por nome, para que eu possa encontrar rapidamente a sala desejada

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de seleção de salas, THE Sistema Mobile SHALL exibir campo de busca no topo da tela
2. WHEN o coletor digita no campo de busca, THE Sistema Mobile SHALL filtrar salas após 300ms de inatividade (debounce)
3. WHEN o coletor digita no campo de busca, THE Sistema Mobile SHALL buscar por nome da sala (case-insensitive)
4. WHEN o coletor limpa o campo de busca, THE Sistema Mobile SHALL exibir todas as salas novamente
5. WHILE a busca está em progresso, THE Sistema Mobile SHALL exibir indicador de loading
6. WHEN nenhuma sala corresponde à busca, THE Sistema Mobile SHALL exibir mensagem "Nenhuma sala encontrada"

### Requirement 3

**User Story:** Como um coletor, eu quero que as salas sejam armazenadas localmente, para que eu possa acessá-las offline

#### Acceptance Criteria

1. WHEN o coletor carrega salas pela primeira vez, THE Sistema Mobile SHALL salvar todas as salas no banco Room
2. WHEN o coletor abre a tela de salas offline, THE Sistema Mobile SHALL carregar salas do banco local
3. WHEN o coletor está online, THE Sistema Mobile SHALL sincronizar salas do servidor e atualizar banco local
4. WHEN uma sala é atualizada no servidor, THE Sistema Mobile SHALL atualizar a sala no banco local
5. THE Sistema Mobile SHALL exibir indicador visual se está usando dados locais ou do servidor

### Requirement 4

**User Story:** Como um desenvolvedor, eu quero usar Paging 3 para paginação, para que a implementação seja robusta e eficiente

#### Acceptance Criteria

1. THE Sistema Mobile SHALL usar PagingSource para carregar salas do servidor
2. THE Sistema Mobile SHALL usar RemoteMediator para sincronizar servidor → banco local
3. THE Sistema Mobile SHALL usar PagingDataAdapter no RecyclerView
4. THE Sistema Mobile SHALL configurar tamanho de página como 20 itens
5. THE Sistema Mobile SHALL configurar prefetch distance como 5 itens
6. THE Sistema Mobile SHALL usar Flow<PagingData<Sala>> no ViewModel

### Requirement 5

**User Story:** Como um coletor, eu quero ver estatísticas de salas, para que eu saiba quantas salas existem

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de salas, THE Sistema Mobile SHALL exibir total de salas no subtítulo
2. WHEN o coletor busca salas, THE Sistema Mobile SHALL exibir quantidade de resultados encontrados
3. WHEN o coletor está offline, THE Sistema Mobile SHALL exibir quantidade de salas armazenadas localmente

### Requirement 6

**User Story:** Como um desenvolvedor, eu quero implementar cache inteligente, para que requisições repetidas sejam evitadas

#### Acceptance Criteria

1. THE Sistema Mobile SHALL cachear resultados de busca por 5 minutos
2. WHEN o coletor busca a mesma query novamente, THE Sistema Mobile SHALL usar resultado cacheado
3. WHEN o cache expira, THE Sistema Mobile SHALL buscar do servidor novamente
4. THE Sistema Mobile SHALL limpar cache ao fazer logout

### Requirement 7

**User Story:** Como um coletor, eu quero que a busca seja rápida, para que eu não precise esperar

#### Acceptance Criteria

1. WHEN o coletor digita no campo de busca, THE Sistema Mobile SHALL aplicar debounce de 300ms
2. WHEN o coletor digita rapidamente, THE Sistema Mobile SHALL cancelar requisições anteriores
3. WHEN a busca retorna resultados, THE Sistema Mobile SHALL exibir em menos de 500ms
4. THE Sistema Mobile SHALL buscar primeiro no banco local, depois no servidor

### Requirement 8

**User Story:** Como um administrador do sistema, eu quero que o servidor suporte paginação, para que o app móvel possa carregar dados eficientemente

#### Acceptance Criteria

1. WHEN o Sistema Mobile solicita salas paginadas, THE Servidor API SHALL aceitar parâmetros page e size
2. WHEN o Sistema Mobile solicita salas paginadas, THE Servidor API SHALL retornar até 20 salas por página
3. WHEN o Sistema Mobile solicita salas paginadas, THE Servidor API SHALL retornar total de páginas e total de itens
4. WHEN o Sistema Mobile busca salas, THE Servidor API SHALL aceitar parâmetro query para filtrar por nome
5. THE Servidor API SHALL retornar salas ordenadas alfabeticamente por nome

### Requirement 9

**User Story:** Como um coletor, eu quero ver indicadores visuais claros, para que eu saiba o estado do carregamento

#### Acceptance Criteria

1. WHEN salas estão sendo carregadas, THE Sistema Mobile SHALL exibir shimmer effect nos primeiros 3 itens
2. WHEN mais salas estão sendo carregadas (scroll infinito), THE Sistema Mobile SHALL exibir spinner no final da lista
3. WHEN ocorre erro, THE Sistema Mobile SHALL exibir ícone de erro e mensagem descritiva
4. WHEN não há mais salas, THE Sistema Mobile SHALL exibir mensagem "Fim da lista"
5. WHEN está offline, THE Sistema Mobile SHALL exibir badge "Offline" no topo

### Requirement 10

**User Story:** Como um desenvolvedor, eu quero implementar testes, para que a funcionalidade seja confiável

#### Acceptance Criteria

1. THE Sistema Mobile SHALL ter testes unitários para SalaViewModel
2. THE Sistema Mobile SHALL ter testes unitários para SalaPagingSource
3. THE Sistema Mobile SHALL ter testes de integração para paginação
4. THE Sistema Mobile SHALL ter testes de UI para busca
5. THE Sistema Mobile SHALL ter cobertura de testes acima de 80%

## Performance Requirements

- Carregamento inicial: < 1 segundo
- Carregamento de página adicional: < 500ms
- Busca: < 500ms após debounce
- Scroll suave: 60 FPS
- Memória: < 50MB para 1000 salas

## Technical Constraints

- Usar Paging 3 library
- Usar Room para cache local
- Usar Kotlin Coroutines e Flow
- Usar Hilt para injeção de dependência
- Manter compatibilidade com API 23+
