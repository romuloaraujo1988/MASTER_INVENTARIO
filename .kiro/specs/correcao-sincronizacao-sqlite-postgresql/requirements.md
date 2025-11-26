# Requirements Document - Correção e Melhoria da Sincronização SQLite ↔ PostgreSQL

## Introduction

Este documento define os requisitos para corrigir e melhorar o sistema crítico de sincronização de dados entre o banco SQLite (modo offline) e o banco PostgreSQL (servidor) no SIHCP. O sistema atual apresenta problemas de integridade de dados, perda de informações durante sincronização e inconsistências que comprometem a confiabilidade do inventário patrimonial.

## Glossary

- **Sistema Desktop**: O aplicativo Java Swing que gerencia o inventário
- **Sistema Mobile**: O aplicativo Android InventarioMobile
- **Servidor API**: O backend Spring Boot que fornece a API REST
- **SQLite**: Banco de dados local usado para modo offline no desktop e mobile
- **PostgreSQL**: Banco de dados principal do servidor
- **Sincronização Bidirecional**: Processo de enviar dados do SQLite para PostgreSQL e baixar dados atualizados do PostgreSQL para SQLite
- **Integridade Referencial**: Garantia de que relacionamentos entre tabelas (FK) são mantidos durante sincronização
- **Transação Atômica**: Operação que ou completa totalmente ou falha totalmente, sem estados intermediários
- **Checkpoint**: Ponto de verificação que permite rollback em caso de falha
- **Validação de Dados**: Processo de verificar consistência e completude dos dados antes de sincronizar
- **Reconciliação de Conflitos**: Processo de resolver dados divergentes entre SQLite e PostgreSQL
- **Auditoria de Sincronização**: Registro detalhado de todas as operações de sincronização para rastreabilidade

## Requirements

### Requirement 1

**User Story:** Como um operador do sistema, eu quero que a sincronização valide todos os dados antes de enviar, para que dados inconsistentes não corrompam o banco PostgreSQL

#### Acceptance Criteria

1. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL validar que todas as coletas pendentes possuem patrimônio_id válido
2. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL validar que todas as coletas pendentes possuem inventario_id válido
3. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL validar que todas as coletas pendentes possuem coletor_id válido
4. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL validar que todos os timestamps estão no formato correto (yyyy-MM-dd HH:mm:ss)
5. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL validar que não há valores NULL em campos obrigatórios
6. IF qualquer validação falhar, THEN THE Sistema Desktop SHALL exibir relatório detalhado dos erros e impedir sincronização
7. WHEN todas as validações passarem, THE Sistema Desktop SHALL exibir resumo dos dados validados e solicitar confirmação

### Requirement 2

**User Story:** Como um operador do sistema, eu quero que a sincronização use transações atômicas, para que falhas não deixem dados pela metade no PostgreSQL

#### Acceptance Criteria

1. WHEN o operador inicia sincronização de coletas, THE Sistema Desktop SHALL iniciar uma transação no PostgreSQL antes de inserir qualquer dado
2. WHEN uma coleta é inserida com sucesso no PostgreSQL, THE Sistema Desktop SHALL marcar a coleta como sincronizada no SQLite dentro da mesma transação
3. IF qualquer inserção falhar durante sincronização, THEN THE Sistema Desktop SHALL executar ROLLBACK completo no PostgreSQL
4. IF qualquer inserção falhar durante sincronização, THEN THE Sistema Desktop SHALL manter todas as coletas como pendentes no SQLite
5. WHEN todas as coletas são sincronizadas com sucesso, THE Sistema Desktop SHALL executar COMMIT no PostgreSQL
6. WHEN o COMMIT é bem-sucedido, THE Sistema Desktop SHALL marcar todas as coletas como sincronizadas no SQLite
7. THE Sistema Desktop SHALL registrar log detalhado de cada etapa da transação para auditoria

### Requirement 3

**User Story:** Como um operador do sistema, eu quero que a sincronização preserve a integridade referencial, para que relacionamentos entre tabelas sejam mantidos corretamente

#### Acceptance Criteria

1. WHEN o operador sincroniza coletas, THE Sistema Desktop SHALL verificar que o patrimônio referenciado existe no PostgreSQL antes de inserir
2. WHEN o operador sincroniza coletas, THE Sistema Desktop SHALL verificar que o inventário referenciado existe no PostgreSQL antes de inserir
3. WHEN o operador sincroniza coletas, THE Sistema Desktop SHALL verificar que o coletor referenciado existe no PostgreSQL antes de inserir
4. IF um patrimônio não existe no PostgreSQL mas existe no SQLite, THEN THE Sistema Desktop SHALL sincronizar o patrimônio primeiro
5. IF um responsável não existe no PostgreSQL mas existe no SQLite, THEN THE Sistema Desktop SHALL sincronizar o responsável primeiro
6. IF uma sala não existe no PostgreSQL mas existe no SQLite, THEN THE Sistema Desktop SHALL sincronizar a sala primeiro
7. THE Sistema Desktop SHALL manter ordem de sincronização: responsáveis → salas → patrimônios → coletas

### Requirement 4

**User Story:** Como um operador do sistema, eu quero que a sincronização detecte e resolva conflitos de dados, para que informações divergentes sejam tratadas adequadamente

#### Acceptance Criteria

1. WHEN o operador sincroniza patrimônios, THE Sistema Desktop SHALL comparar timestamps de última modificação entre SQLite e PostgreSQL
2. WHEN um patrimônio foi modificado em ambos os bancos, THE Sistema Desktop SHALL exibir dialog de resolução de conflito
3. WHEN o operador resolve conflito, THE Sistema Desktop SHALL permitir escolher: manter versão SQLite, manter versão PostgreSQL, ou mesclar manualmente
4. WHEN o operador escolhe manter versão SQLite, THE Sistema Desktop SHALL atualizar o PostgreSQL com dados do SQLite
5. WHEN o operador escolhe manter versão PostgreSQL, THE Sistema Desktop SHALL atualizar o SQLite com dados do PostgreSQL
6. WHEN o operador escolhe mesclar, THE Sistema Desktop SHALL exibir interface para edição manual dos campos conflitantes
7. THE Sistema Desktop SHALL registrar todas as resoluções de conflito em log de auditoria

### Requirement 5

**User Story:** Como um operador do sistema, eu quero que a sincronização tenha retry automático inteligente, para que falhas temporárias não impeçam a sincronização

#### Acceptance Criteria

1. WHEN uma sincronização falha por timeout de rede, THE Sistema Desktop SHALL tentar novamente após 5 segundos
2. WHEN uma sincronização falha por erro de conexão, THE Sistema Desktop SHALL tentar novamente até 3 vezes com backoff exponencial
3. WHEN uma sincronização falha por erro de constraint do banco, THE Sistema Desktop SHALL registrar erro e pular para próximo registro
4. WHEN uma sincronização falha por erro de autenticação, THE Sistema Desktop SHALL interromper e solicitar reautenticação
5. WHEN todas as tentativas de retry falharem, THE Sistema Desktop SHALL exibir relatório detalhado dos erros
6. THE Sistema Desktop SHALL permitir que operador escolha tentar novamente manualmente ou cancelar
7. THE Sistema Desktop SHALL manter registro de todas as tentativas e falhas para diagnóstico

### Requirement 6

**User Story:** Como um operador do sistema, eu quero visualizar progresso detalhado da sincronização, para que eu saiba exatamente o que está acontecendo

#### Acceptance Criteria

1. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL exibir janela de progresso com barra de progresso
2. WHILE sincronização está em andamento, THE Sistema Desktop SHALL exibir etapa atual (validação, responsáveis, salas, patrimônios, coletas)
3. WHILE sincronização está em andamento, THE Sistema Desktop SHALL exibir contador de registros processados e total
4. WHILE sincronização está em andamento, THE Sistema Desktop SHALL exibir tempo decorrido e estimativa de tempo restante
5. WHEN um erro ocorre, THE Sistema Desktop SHALL exibir mensagem de erro na janela de progresso sem fechar a janela
6. WHEN sincronização é concluída, THE Sistema Desktop SHALL exibir resumo detalhado: total sincronizado, erros, tempo total
7. THE Sistema Desktop SHALL permitir cancelar sincronização a qualquer momento com rollback seguro

### Requirement 7

**User Story:** Como um operador do sistema, eu quero que a sincronização mantenha backup automático, para que eu possa recuperar dados em caso de problema

#### Acceptance Criteria

1. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL criar backup automático do SQLite antes de qualquer modificação
2. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL nomear backup com timestamp (inventario_backup_yyyyMMdd_HHmmss.db)
3. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL armazenar backups em diretório configurável
4. THE Sistema Desktop SHALL manter últimos 10 backups e remover backups mais antigos automaticamente
5. IF sincronização falhar criticamente, THEN THE Sistema Desktop SHALL oferecer opção de restaurar último backup
6. WHEN operador solicita restauração, THE Sistema Desktop SHALL substituir SQLite atual pelo backup selecionado
7. THE Sistema Desktop SHALL registrar todas as operações de backup e restauração em log de auditoria

### Requirement 8

**User Story:** Como um operador do sistema, eu quero que a sincronização verifique integridade dos dados após conclusão, para que eu tenha certeza de que tudo foi sincronizado corretamente

#### Acceptance Criteria

1. WHEN sincronização é concluída, THE Sistema Desktop SHALL contar total de coletas no SQLite marcadas como sincronizadas
2. WHEN sincronização é concluída, THE Sistema Desktop SHALL contar total de coletas no PostgreSQL para o mesmo inventário
3. WHEN sincronização é concluída, THE Sistema Desktop SHALL comparar os dois totais e exibir resultado
4. IF os totais não coincidirem, THEN THE Sistema Desktop SHALL exibir alerta de inconsistência com detalhes
5. WHEN sincronização é concluída, THE Sistema Desktop SHALL verificar que não há coletas pendentes no SQLite
6. WHEN sincronização é concluída, THE Sistema Desktop SHALL calcular checksum MD5 dos dados sincronizados
7. THE Sistema Desktop SHALL exibir relatório de integridade com status OK ou FALHA e detalhes

### Requirement 9

**User Story:** Como um administrador do sistema, eu quero que o servidor valide dados recebidos antes de inserir, para que dados inválidos sejam rejeitados

#### Acceptance Criteria

1. WHEN o Servidor API recebe requisição de sincronização, THE Servidor API SHALL validar que todos os campos obrigatórios estão presentes
2. WHEN o Servidor API recebe requisição de sincronização, THE Servidor API SHALL validar que tipos de dados estão corretos
3. WHEN o Servidor API recebe requisição de sincronização, THE Servidor API SHALL validar que foreign keys referenciam registros existentes
4. WHEN o Servidor API recebe requisição de sincronização, THE Servidor API SHALL validar que timestamps estão em formato válido
5. IF qualquer validação falhar, THEN THE Servidor API SHALL retornar HTTP 400 com detalhes específicos do erro
6. WHEN todas as validações passarem, THE Servidor API SHALL processar a sincronização
7. THE Servidor API SHALL registrar todas as validações e resultados em log estruturado

### Requirement 10

**User Story:** Como um operador do sistema, eu quero sincronizar dados do PostgreSQL para SQLite, para que eu tenha dados atualizados no modo offline

#### Acceptance Criteria

1. WHEN o operador solicita download de dados, THE Sistema Desktop SHALL baixar todos os patrimônios ativos do PostgreSQL
2. WHEN o operador solicita download de dados, THE Sistema Desktop SHALL baixar todos os responsáveis ativos do PostgreSQL
3. WHEN o operador solicita download de dados, THE Sistema Desktop SHALL baixar todas as salas ativas do PostgreSQL
4. WHEN o operador solicita download de dados, THE Sistema Desktop SHALL usar paginação de 500 registros por requisição
5. WHILE download está em progresso, THE Sistema Desktop SHALL exibir progresso com total baixado e total disponível
6. WHEN download é concluído, THE Sistema Desktop SHALL substituir dados antigos no SQLite pelos novos dados
7. WHEN download é concluído, THE Sistema Desktop SHALL armazenar timestamp da última sincronização

### Requirement 11

**User Story:** Como um desenvolvedor, eu quero que a sincronização tenha logs detalhados, para que eu possa diagnosticar problemas rapidamente

#### Acceptance Criteria

1. WHEN sincronização inicia, THE Sistema Desktop SHALL registrar log com timestamp, usuário e tipo de sincronização
2. WHEN cada registro é processado, THE Sistema Desktop SHALL registrar log com ID do registro e resultado
3. WHEN erro ocorre, THE Sistema Desktop SHALL registrar stack trace completo e contexto do erro
4. WHEN sincronização é concluída, THE Sistema Desktop SHALL registrar log com estatísticas finais
5. THE Sistema Desktop SHALL armazenar logs em arquivo rotativo com máximo de 50MB por arquivo
6. THE Sistema Desktop SHALL manter últimos 30 dias de logs
7. THE Sistema Desktop SHALL permitir exportar logs para análise externa

### Requirement 12

**User Story:** Como um operador do sistema, eu quero que a sincronização seja incremental, para que apenas dados novos ou modificados sejam sincronizados

#### Acceptance Criteria

1. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL identificar coletas com sincronizado = false no SQLite
2. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL identificar patrimônios modificados desde última sincronização
3. WHEN o operador inicia sincronização, THE Sistema Desktop SHALL sincronizar apenas registros identificados como novos ou modificados
4. WHEN um registro é sincronizado, THE Sistema Desktop SHALL atualizar campo data_ultima_sincronizacao no SQLite
5. WHEN sincronização incremental é concluída, THE Sistema Desktop SHALL exibir total de registros novos e modificados sincronizados
6. THE Sistema Desktop SHALL permitir forçar sincronização completa ignorando timestamps
7. THE Sistema Desktop SHALL manter histórico das últimas 100 sincronizações com estatísticas

### Requirement 13

**User Story:** Como um operador do sistema, eu quero que a sincronização trate corretamente campos de data e hora, para que não haja problemas de timezone ou formato

#### Acceptance Criteria

1. WHEN o operador sincroniza coletas, THE Sistema Desktop SHALL converter timestamps do SQLite para UTC antes de enviar
2. WHEN o operador sincroniza coletas, THE Sistema Desktop SHALL usar formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss'Z') para timestamps
3. WHEN o Servidor API recebe timestamps, THE Servidor API SHALL interpretar como UTC e armazenar no PostgreSQL
4. WHEN o operador baixa dados do PostgreSQL, THE Sistema Desktop SHALL converter timestamps de UTC para timezone local
5. THE Sistema Desktop SHALL exibir timestamps na interface sempre no timezone local do usuário
6. THE Sistema Desktop SHALL armazenar timezone usado na sincronização para auditoria
7. THE Sistema Desktop SHALL validar que timestamps não estão no futuro antes de sincronizar

### Requirement 14

**User Story:** Como um administrador do sistema, eu quero que a sincronização tenha endpoint de health check, para que eu possa monitorar o status do serviço

#### Acceptance Criteria

1. THE Servidor API SHALL fornecer endpoint GET /api/sync/health que retorna status do serviço de sincronização
2. WHEN o endpoint é chamado, THE Servidor API SHALL verificar conectividade com PostgreSQL
3. WHEN o endpoint é chamado, THE Servidor API SHALL verificar espaço disponível em disco
4. WHEN o endpoint é chamado, THE Servidor API SHALL verificar carga atual do servidor
5. WHEN todos os checks passarem, THE Servidor API SHALL retornar HTTP 200 com status "healthy"
6. IF qualquer check falhar, THEN THE Servidor API SHALL retornar HTTP 503 com detalhes do problema
7. THE Servidor API SHALL incluir métricas: total de sincronizações nas últimas 24h, taxa de sucesso, tempo médio

### Requirement 15

**User Story:** Como um operador do sistema, eu quero que a sincronização tenha modo de teste, para que eu possa validar sem modificar dados reais

#### Acceptance Criteria

1. WHEN o operador acessa configurações de sincronização, THE Sistema Desktop SHALL exibir checkbox "Modo de Teste"
2. WHEN modo de teste está habilitado, THE Sistema Desktop SHALL executar todas as validações normalmente
3. WHEN modo de teste está habilitado, THE Sistema Desktop SHALL simular envio de dados sem realmente inserir no PostgreSQL
4. WHEN modo de teste está habilitado, THE Sistema Desktop SHALL exibir relatório detalhado do que seria sincronizado
5. WHEN modo de teste é concluído, THE Sistema Desktop SHALL exibir resumo: X coletas seriam sincronizadas, Y erros encontrados
6. THE Sistema Desktop SHALL permitir exportar relatório de teste para análise
7. THE Sistema Desktop SHALL marcar claramente na interface quando modo de teste está ativo
