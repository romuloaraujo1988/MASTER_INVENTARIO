# Requirements Document - Otimização de Performance na Coleta Patrimonial Desktop

## Introduction

Este documento especifica os requisitos para otimização de performance no processo de coleta patrimonial na aplicação desktop Java Swing (ColetaFrame_v2). O objetivo é proporcionar uma experiência ágil e responsiva, eliminando lentidões e delays que impactam negativamente a produtividade dos coletores durante o uso do sistema desktop.

## Glossary

- **ColetaFrame_v2**: Frame Swing principal para coleta de patrimônios no sistema desktop
- **Coletor**: Usuário que realiza a coleta física de patrimônios usando o sistema desktop
- **Tempo de Resposta**: Intervalo entre ação do usuário e feedback visual do sistema
- **Cache Local**: Armazenamento temporário de dados em memória para acesso rápido
- **Lazy Loading**: Carregamento sob demanda de dados conforme necessário
- **Debounce**: Técnica para limitar frequência de execução de operações (ex: busca em tempo real)
- **Indexação**: Estrutura de dados no PostgreSQL para acelerar consultas
- **EDT (Event Dispatch Thread)**: Thread principal do Swing responsável pela interface
- **SwingWorker**: Classe para executar operações pesadas fora da EDT
- **JTable**: Componente Swing para exibição de dados tabulares
- **DefaultTableModel**: Modelo de dados para JTable
- **PreparedStatement**: Statement SQL pré-compilado para melhor performance

## Requirements

### Requirement 1: Resposta Instantânea na Interface Swing

**User Story:** Como coletor, quero que a interface Swing responda instantaneamente às minhas ações, para que eu possa trabalhar de forma fluida sem travamentos ou congelamentos.

#### Acceptance Criteria

1. WHEN o coletor digita no campo de busca THEN o sistema SHALL responder sem delay perceptível mantendo EDT livre
2. WHEN o coletor clica em botão de coletar THEN o sistema SHALL exibir feedback visual em menos de 100ms
3. WHEN o coletor registra uma coleta THEN o sistema SHALL confirmar a operação em menos de 300ms
4. WHEN o coletor seleciona sala no combo THEN o sistema SHALL atualizar interface em menos de 200ms
5. WHILE o sistema processa operações pesadas THEN a interface SHALL permanecer responsiva usando SwingWorker

### Requirement 2: Carregamento Otimizado de Dados na JTable

**User Story:** Como coletor, quero que a tabela de histórico carregue rapidamente, para que eu não perca tempo esperando dados aparecerem.

#### Acceptance Criteria

1. WHEN o coletor abre ColetaFrame_v2 THEN o sistema SHALL carregar apenas últimas 100 coletas inicialmente
2. WHEN o coletor seleciona sala THEN o sistema SHALL carregar patrimônios dessa sala em menos de 500ms
3. WHEN o coletor acessa combo de salas THEN o sistema SHALL recuperar lista do cache em menos de 10ms
4. WHEN o coletor busca patrimônio THEN o sistema SHALL retornar resultado em menos de 300ms usando índice
5. WHILE o sistema carrega dados THEN o sistema SHALL exibir JProgressBar ou cursor de espera

### Requirement 3: Cache em Memória para Dados Frequentes

**User Story:** Como coletor, quero que dados usados frequentemente estejam em cache, para que eu tenha acesso instantâneo sem consultar o banco repetidamente.

#### Acceptance Criteria

1. WHEN o coletor abre ColetaFrame_v2 THEN o sistema SHALL carregar lista de salas em cache de memória
2. WHEN o coletor seleciona sala THEN o sistema SHALL verificar cache antes de consultar banco
3. WHEN o coletor registra coleta THEN o sistema SHALL atualizar cache e tabela sem recarregar tudo
4. WHEN o sistema inicia THEN o sistema SHALL pré-carregar inventário ativo e salas em cache
5. WHILE cache está ativo THEN o sistema SHALL manter dados por 5 minutos ou até mudança

### Requirement 4: Busca Otimizada com Debounce

**User Story:** Como coletor, quero buscar patrimônios rapidamente sem sobrecarregar o sistema, para que eu possa localizar itens específicos sem demora.

#### Acceptance Criteria

1. WHEN o coletor digita no campo de busca THEN o sistema SHALL aplicar debounce de 300ms usando javax.swing.Timer
2. WHEN o coletor busca por número exato THEN o sistema SHALL usar índice retornando em menos de 100ms
3. WHEN o coletor busca por descrição THEN o sistema SHALL usar ILIKE com índice retornando em menos de 500ms
4. WHEN o coletor pressiona Enter THEN o sistema SHALL executar busca imediatamente cancelando debounce
5. WHILE busca está em andamento THEN o sistema SHALL cancelar busca anterior se nova for iniciada

### Requirement 5: Operações de Banco Não-Bloqueantes

**User Story:** Como coletor, quero que operações de banco executem em background, para que eu possa continuar coletando sem esperar.

#### Acceptance Criteria

1. WHEN o coletor registra coleta THEN o sistema SHALL executar INSERT em SwingWorker retornando controle imediato
2. WHEN o coletor atualiza tabela THEN o sistema SHALL carregar dados em background thread
3. WHEN operação de banco completa THEN o sistema SHALL atualizar UI na EDT usando SwingUtilities.invokeLater
4. WHEN múltiplas operações são solicitadas THEN o sistema SHALL enfileirar usando ExecutorService
5. WHILE operação está em andamento THEN o sistema SHALL permitir cancelamento se usuário solicitar

### Requirement 6: Queries SQL Otimizadas

**User Story:** Como desenvolvedor, quero que as consultas SQL sejam otimizadas, para que o sistema responda rapidamente mesmo com grande volume de dados.

#### Acceptance Criteria

1. WHEN o sistema busca patrimônio por número THEN o sistema SHALL usar PreparedStatement com índice em numero_patrimonio
2. WHEN o sistema lista coletas THEN o sistema SHALL usar LIMIT 100 e carregar sob demanda
3. WHEN o sistema conta coletas THEN o sistema SHALL usar COUNT(*) sem carregar dados completos
4. WHEN o sistema faz joins THEN o sistema SHALL selecionar apenas colunas necessárias evitando SELECT *
5. WHILE queries executam THEN o sistema SHALL usar connection pooling para reutilizar conexões

### Requirement 7: Gerenciamento Eficiente de Memória na JTable

**User Story:** Como usuário, quero que o sistema use memória de forma eficiente, para que não trave mesmo com muitos dados.

#### Acceptance Criteria

1. WHEN o sistema carrega tabela THEN o sistema SHALL limitar a 100 linhas visíveis inicialmente
2. WHEN o sistema não usa DefaultTableModel THEN o sistema SHALL limpar dados antigos
3. WHEN o sistema fecha frame THEN o sistema SHALL liberar recursos e listeners
4. WHEN a memória está alta THEN o sistema SHALL limpar cache de salas e recarregar sob demanda
5. WHILE tabela tem muitos dados THEN o sistema SHALL usar paginação ao invés de carregar tudo

### Requirement 8: SwingWorker para Operações Pesadas

**User Story:** Como desenvolvedor, quero usar SwingWorker para operações pesadas, para que a EDT permaneça livre e a UI responsiva.

#### Acceptance Criteria

1. WHEN o sistema carrega dados do banco THEN o sistema SHALL usar SwingWorker com doInBackground
2. WHEN o sistema atualiza tabela THEN o sistema SHALL processar dados em background e atualizar na done()
3. WHEN o sistema registra coleta THEN o sistema SHALL executar INSERT em SwingWorker
4. WHEN operação completa THEN o sistema SHALL atualizar UI usando SwingUtilities.invokeLater se necessário
5. WHILE operação está em andamento THEN o sistema SHALL permitir cancelamento via SwingWorker.cancel()

### Requirement 9: Feedback Visual e Sonoro Imediato

**User Story:** Como coletor, quero feedback visual e sonoro imediato para minhas ações, para que eu saiba que o sistema está respondendo.

#### Acceptance Criteria

1. WHEN o coletor clica em botão THEN o sistema SHALL desabilitar botão imediatamente evitando duplo clique
2. WHEN o sistema processa operação THEN o sistema SHALL exibir JProgressBar ou alterar cursor para WAIT_CURSOR
3. WHEN coleta é registrada THEN o sistema SHALL tocar som de sucesso e exibir mensagem
4. WHEN erro ocorre THEN o sistema SHALL exibir JOptionPane com mensagem clara
5. WHILE operação está em andamento THEN o sistema SHALL desabilitar botões conflitantes

### Requirement 10: Connection Pooling e Reutilização

**User Story:** Como desenvolvedor, quero reutilizar conexões de banco, para que o sistema não perca tempo criando novas conexões.

#### Acceptance Criteria

1. WHEN o sistema acessa banco THEN o sistema SHALL usar HikariCP ou connection pool configurado
2. WHEN operação completa THEN o sistema SHALL retornar conexão ao pool ao invés de fechar
3. WHEN pool está cheio THEN o sistema SHALL aguardar conexão disponível com timeout de 5 segundos
4. WHEN conexão está ociosa THEN o sistema SHALL validar antes de reutilizar
5. WHILE sistema está ativo THEN o sistema SHALL manter pool de 5-10 conexões ativas

### Requirement 11: Pré-carregamento de Dados Essenciais

**User Story:** Como coletor, quero que dados essenciais sejam pré-carregados, para que eu não precise esperar ao iniciar o trabalho.

#### Acceptance Criteria

1. WHEN o sistema abre ColetaFrame_v2 THEN o sistema SHALL pré-carregar lista de salas em cache
2. WHEN o sistema inicia THEN o sistema SHALL carregar inventário ativo em background
3. WHEN o coletor seleciona sala THEN o sistema SHALL carregar patrimônios dessa sala imediatamente do cache se disponível
4. WHEN o sistema detecta sala frequente THEN o sistema SHALL manter patrimônios dessa sala em cache
5. WHILE sistema está ocioso THEN o sistema SHALL pré-carregar próximas salas em background

### Requirement 12: Logs de Performance e Diagnóstico

**User Story:** Como desenvolvedor, quero logs de performance, para identificar e corrigir gargalos rapidamente.

#### Acceptance Criteria

1. WHEN operação de banco excede 500ms THEN o sistema SHALL registrar log com tempo e query
2. WHEN o sistema carrega tabela THEN o sistema SHALL logar tempo de carregamento
3. WHEN erro ocorre THEN o sistema SHALL logar stack trace completo
4. WHEN operação completa THEN o sistema SHALL logar tempo total de execução
5. WHILE em desenvolvimento THEN o sistema SHALL exibir tempos de operação no console
