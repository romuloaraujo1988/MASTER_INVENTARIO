# Requirements Document - Coleta Offline

## Introduction

Este documento define os requisitos para o sistema de coleta offline no aplicativo móvel Android do SIHCP. A funcionalidade permite que coletores realizem inventário de patrimônios mesmo sem conexão com a internet, armazenando os dados localmente e sincronizando posteriormente quando a conexão for restabelecida.

## Glossary

- **Sistema Mobile**: O aplicativo Android InventarioMobile
- **Servidor API**: O backend Spring Boot que fornece a API REST para o aplicativo móvel
- **Banco Local**: O banco de dados SQLite local no dispositivo Android usando Room
- **Banco Remoto**: O banco de dados PostgreSQL no servidor
- **Coleta Pendente**: Uma coleta realizada offline que ainda não foi sincronizada com o servidor
- **Sincronização**: O processo de enviar coletas pendentes do banco local para o servidor e atualizar dados locais
- **Modo Offline**: Estado do aplicativo onde todas as operações são realizadas usando apenas dados locais
- **Download de Dados**: Processo de baixar patrimônios, responsáveis e salas do servidor para o banco local

## Requirements

### Requirement 1

**User Story:** Como um coletor de campo, eu quero baixar todos os dados necessários para o dispositivo móvel, para que eu possa trabalhar sem conexão com a internet

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir estatísticas dos dados já baixados incluindo contagem de patrimônios, responsáveis, salas e coletas pendentes
2. WHEN o coletor inicia o download de dados, THE Sistema Mobile SHALL baixar todos os responsáveis ativos do Servidor API e armazenar no Banco Local
3. WHEN o coletor inicia o download de dados, THE Sistema Mobile SHALL baixar todas as salas ativas do Servidor API e armazenar no Banco Local
4. WHEN o coletor inicia o download de dados, THE Sistema Mobile SHALL baixar todos os patrimônios do Servidor API usando paginação de 100 itens por página e armazenar no Banco Local
5. WHILE o download está em progresso, THE Sistema Mobile SHALL exibir uma barra de progresso com mensagem descritiva da etapa atual
6. IF o download falhar em qualquer etapa, THEN THE Sistema Mobile SHALL exibir mensagem de erro específica e manter os dados já baixados anteriormente
7. WHEN o download for concluído com sucesso, THE Sistema Mobile SHALL armazenar o timestamp da sincronização e exibir mensagem de sucesso com totais baixados

### Requirement 2

**User Story:** Como um coletor de campo, eu quero habilitar o modo offline no aplicativo, para que eu possa realizar coletas usando apenas dados locais

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir um switch para habilitar ou desabilitar o modo offline
2. WHEN o coletor habilita o modo offline, THE Sistema Mobile SHALL armazenar essa preferência localmente e exibir confirmação
3. WHILE o modo offline está habilitado, THE Sistema Mobile SHALL utilizar o Banco Local para todas as operações de leitura de patrimônios, responsáveis e salas
4. WHEN o coletor desabilita o modo offline, THE Sistema Mobile SHALL voltar a utilizar o Servidor API para operações de leitura
5. IF o modo offline está habilitado e não há dados no Banco Local, THEN THE Sistema Mobile SHALL exibir mensagem orientando o coletor a baixar os dados primeiro

### Requirement 3

**User Story:** Como um coletor de campo, eu quero realizar coletas de patrimônio offline, para que eu possa trabalhar em locais sem sinal de internet

#### Acceptance Criteria

1. WHILE o modo offline está habilitado, WHEN o coletor seleciona um responsável, THE Sistema Mobile SHALL buscar a lista de patrimônios do Banco Local filtrada por responsável
2. WHILE o modo offline está habilitado, WHEN o coletor escaneia um QR code, THE Sistema Mobile SHALL buscar o patrimônio correspondente no Banco Local
3. WHEN o coletor confirma uma coleta offline, THE Sistema Mobile SHALL armazenar a coleta no Banco Local com status pendente de sincronização
4. WHEN o coletor confirma uma coleta offline, THE Sistema Mobile SHALL atualizar o campo jaColetado do patrimônio no Banco Local para true
5. WHILE o modo offline está habilitado, THE Sistema Mobile SHALL exibir indicador visual de que está operando offline
6. WHEN uma coleta offline é salva, THE Sistema Mobile SHALL incrementar o contador de coletas pendentes nas estatísticas

### Requirement 4

**User Story:** Como um coletor de campo, eu quero sincronizar minhas coletas pendentes com o servidor, para que os dados sejam registrados no sistema central

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir a quantidade de coletas pendentes de sincronização
2. WHERE existem coletas pendentes, WHEN o coletor inicia a sincronização, THE Sistema Mobile SHALL enviar cada coleta pendente para o Servidor API em ordem cronológica
3. WHEN uma coleta pendente é sincronizada com sucesso, THE Sistema Mobile SHALL remover a coleta da tabela de pendentes no Banco Local
4. IF uma coleta pendente falhar na sincronização, THEN THE Sistema Mobile SHALL manter a coleta como pendente e registrar o erro
5. WHEN todas as coletas pendentes forem processadas, THE Sistema Mobile SHALL exibir resumo com total de sucessos e falhas
6. WHILE a sincronização está em progresso, THE Sistema Mobile SHALL exibir indicador de progresso e desabilitar botões de ação

### Requirement 5

**User Story:** Como um coletor de campo, eu quero visualizar o status dos dados offline, para que eu saiba quando preciso sincronizar ou baixar dados atualizados

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir a data e hora da última sincronização de dados
2. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir o total de patrimônios armazenados localmente
3. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir o total de responsáveis armazenados localmente
4. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir o total de salas armazenadas localmente
5. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir o total de coletas pendentes de sincronização
6. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir o status atual do modo offline (habilitado ou desabilitado)

### Requirement 6

**User Story:** Como um coletor de campo, eu quero limpar os dados offline do dispositivo, para que eu possa liberar espaço ou reiniciar o processo de sincronização

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de sincronização offline, THE Sistema Mobile SHALL exibir botão para limpar dados offline
2. WHERE existem dados offline armazenados, WHEN o coletor solicita limpeza, THE Sistema Mobile SHALL remover todos os patrimônios do Banco Local
3. WHERE existem dados offline armazenados, WHEN o coletor solicita limpeza, THE Sistema Mobile SHALL remover todos os responsáveis do Banco Local
4. WHERE existem dados offline armazenados, WHEN o coletor solicita limpeza, THE Sistema Mobile SHALL remover todas as salas do Banco Local
5. WHEN a limpeza é concluída, THE Sistema Mobile SHALL manter as coletas pendentes no Banco Local para não perder dados
6. WHEN a limpeza é concluída, THE Sistema Mobile SHALL limpar o timestamp da última sincronização
7. WHEN a limpeza é concluída, THE Sistema Mobile SHALL exibir mensagem de confirmação e atualizar as estatísticas

### Requirement 7

**User Story:** Como um administrador do sistema, eu quero que o servidor forneça endpoints paginados para download de dados, para que o aplicativo móvel possa baixar grandes volumes de dados eficientemente

#### Acceptance Criteria

1. WHEN o Sistema Mobile solicita patrimônios paginados, THE Servidor API SHALL retornar até 100 patrimônios por página com todos os campos necessários
2. WHEN o Sistema Mobile solicita responsáveis, THE Servidor API SHALL retornar todos os responsáveis ativos com informações completas
3. WHEN o Sistema Mobile solicita salas, THE Servidor API SHALL retornar todas as salas ativas com informações completas
4. WHEN o Sistema Mobile solicita patrimônios por responsável com filtro de coletado, THE Servidor API SHALL retornar apenas patrimônios que atendem aos critérios especificados
5. IF uma requisição de dados falhar, THEN THE Servidor API SHALL retornar código de erro HTTP apropriado e mensagem descritiva

### Requirement 8

**User Story:** Como um desenvolvedor, eu quero que o banco de dados local tenha estrutura otimizada, para que as operações offline sejam rápidas e eficientes

#### Acceptance Criteria

1. THE Sistema Mobile SHALL criar índices na coluna numero da tabela patrimonio_offline para busca rápida por QR code
2. THE Sistema Mobile SHALL criar índices na coluna responsavelId da tabela patrimonio_offline para filtragem rápida
3. THE Sistema Mobile SHALL criar índices na coluna jaColetado da tabela patrimonio_offline para filtragem de status
4. THE Sistema Mobile SHALL criar índices nas colunas nome das tabelas responsavel_offline e sala_offline para busca rápida
5. WHEN há conflito de chave primária durante inserção, THE Sistema Mobile SHALL substituir o registro existente com os novos dados
6. THE Sistema Mobile SHALL utilizar transações do Room para garantir consistência durante operações em lote
