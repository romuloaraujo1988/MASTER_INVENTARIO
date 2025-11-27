# Requirements Document

## Introduction

Este documento especifica os requisitos para corrigir o problema de carregamento de coletas pendentes na tela `PendingCollectionsActivity` do aplicativo Android. A tela não está exibindo as coletas pendentes armazenadas no banco de dados local Room, devido a problemas de instanciação incorreta de dependências e uso de MockApiService.

## Glossary

- **PendingCollectionsActivity**: Tela do app Android que exibe coletas que ainda não foram sincronizadas com o servidor
- **Room Database**: Banco de dados SQLite local gerenciado pelo Room (biblioteca Android)
- **ColetaEntity**: Entidade Room que representa uma coleta no banco local
- **InventarioRepository**: Repositório que gerencia operações de coleta e patrimônio
- **MockApiService**: Implementação mock da API que retorna dados vazios (usado incorretamente)
- **ApiClient**: Cliente real da API que se conecta ao servidor
- **LocalDataManager**: Gerenciador de dados locais (SharedPreferences e cache)
- **Hilt**: Framework de injeção de dependência do Android

## Requirements

### Requirement 1

**User Story:** Como usuário do app, eu quero visualizar todas as coletas pendentes de sincronização, para que eu possa acompanhar quais coletas ainda precisam ser enviadas ao servidor.

#### Acceptance Criteria

1. WHEN o usuário abre a tela de Coletas Pendentes THEN o sistema SHALL buscar todas as coletas com `sincronizado = false` do banco Room local
2. WHEN existem coletas pendentes no banco local THEN o sistema SHALL exibir a lista de coletas na RecyclerView
3. WHEN não existem coletas pendentes THEN o sistema SHALL exibir o estado vazio com mensagem "Nenhuma coleta pendente"
4. WHEN ocorre erro ao buscar coletas THEN o sistema SHALL exibir mensagem de erro ao usuário

### Requirement 2

**User Story:** Como desenvolvedor, eu quero que a tela use as dependências corretas, para que o carregamento de dados funcione corretamente.

#### Acceptance Criteria

1. WHEN a Activity é criada THEN o sistema SHALL usar ApiClient.getApiService() ao invés de MockApiService
2. WHEN a Activity é criada THEN o sistema SHALL usar LocalDataManager.getInstance() ao invés de nova instância
3. WHEN o ViewModel acessa o banco Room THEN o sistema SHALL usar o contexto correto da aplicação
4. WHEN o ViewModel limpa erros de sincronização THEN o sistema SHALL usar o contexto do repositório

### Requirement 3

**User Story:** Como desenvolvedor, eu quero adicionar logs de diagnóstico, para que seja possível identificar problemas de carregamento.

#### Acceptance Criteria

1. WHEN o método loadPendingCollections() é chamado THEN o sistema SHALL registrar log com quantidade de coletas encontradas
2. WHEN ocorre erro ao buscar coletas THEN o sistema SHALL registrar log detalhado do erro
3. WHEN a lista de coletas é atualizada na UI THEN o sistema SHALL registrar log com o tamanho da lista

### Requirement 4

**User Story:** Como usuário, eu quero que a tela mantenha compatibilidade com funcionalidades existentes, para que nenhuma funcionalidade seja quebrada.

#### Acceptance Criteria

1. WHEN o usuário clica em "Sincronizar Todas" THEN o sistema SHALL manter o comportamento atual de sincronização
2. WHEN o usuário clica em "Excluir" em uma coleta THEN o sistema SHALL manter o comportamento atual de exclusão
3. WHEN o usuário clica em "Tentar Novamente" THEN o sistema SHALL manter o comportamento atual de retry
4. WHEN a tela é reaberta após navegação THEN o sistema SHALL recarregar a lista de coletas pendentes

