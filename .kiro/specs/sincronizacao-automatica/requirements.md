# Requirements Document - Sincronização Automática

## Introduction

Este documento define os requisitos para o sistema de sincronização automática no aplicativo móvel Android do SIHCP. A funcionalidade permite que coletas pendentes sejam sincronizadas automaticamente com o servidor em segundo plano, baseado em configurações de tempo ou quantidade de coletas, utilizando WorkManager para garantir execução confiável mesmo com o app fechado.

## Glossary

- **Sistema Mobile**: O aplicativo Android InventarioMobile
- **WorkManager**: Biblioteca Android para agendamento de tarefas em background
- **Coleta Pendente**: Uma coleta realizada offline que ainda não foi sincronizada com o servidor
- **Sincronização Automática**: Processo de enviar coletas pendentes automaticamente sem intervenção do usuário
- **Sincronização por Tempo**: Sincronização automática baseada em intervalo de tempo configurável
- **Sincronização por Contador**: Sincronização automática baseada em quantidade de coletas realizadas
- **Worker**: Classe do WorkManager que executa a tarefa de sincronização em background
- **Constraint**: Condição que deve ser atendida para o Worker executar (ex: Wi-Fi disponível)
- **Retry Policy**: Política de reexecução em caso de falha na sincronização

## Requirements

### Requirement 1

**User Story:** Como um coletor de campo, eu quero configurar sincronização automática por tempo, para que minhas coletas sejam enviadas periodicamente sem intervenção manual

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de configurações, THE Sistema Mobile SHALL exibir um switch para habilitar sincronização automática por tempo
2. WHEN o coletor habilita sincronização por tempo, THE Sistema Mobile SHALL exibir controles para ajustar o intervalo de sincronização
3. WHEN o coletor ajusta o intervalo, THE Sistema Mobile SHALL permitir valores entre 15 minutos e 8 horas em incrementos de 15 minutos
4. WHEN o coletor confirma as configurações, THE Sistema Mobile SHALL agendar um PeriodicWorkRequest no WorkManager com o intervalo configurado
5. WHEN o intervalo de tempo é atingido, THE Sistema Mobile SHALL executar o Worker de sincronização em background
6. IF o coletor desabilita sincronização por tempo, THEN THE Sistema Mobile SHALL cancelar o PeriodicWorkRequest agendado

### Requirement 2

**User Story:** Como um coletor de campo, eu quero configurar sincronização automática por contador de coletas, para que o sistema sincronize automaticamente após um número específico de coletas

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de configurações, THE Sistema Mobile SHALL exibir um switch para habilitar sincronização automática por contador
2. WHEN o coletor habilita sincronização por contador, THE Sistema Mobile SHALL exibir controles para ajustar a quantidade de coletas
3. WHEN o coletor ajusta a quantidade, THE Sistema Mobile SHALL permitir valores entre 5 e 100 coletas em incrementos de 5
4. WHEN uma coleta é registrada, THE Sistema Mobile SHALL incrementar o contador de coletas realizadas
5. WHEN o contador atinge o limite configurado, THE Sistema Mobile SHALL executar sincronização imediata via OneTimeWorkRequest
6. WHEN a sincronização por contador é concluída, THE Sistema Mobile SHALL resetar o contador para zero
7. WHEN o coletor acessa configurações, THE Sistema Mobile SHALL exibir o contador atual e o limite configurado

### Requirement 3

**User Story:** Como um coletor de campo, eu quero configurar sincronização apenas via Wi-Fi, para que eu não consuma meus dados móveis

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de configurações, THE Sistema Mobile SHALL exibir um switch para habilitar sincronização apenas via Wi-Fi
2. WHEN o coletor habilita Wi-Fi only, THE Sistema Mobile SHALL adicionar NetworkType.UNMETERED como constraint nos WorkRequests
3. WHEN o coletor desabilita Wi-Fi only, THE Sistema Mobile SHALL adicionar NetworkType.CONNECTED como constraint nos WorkRequests
4. WHILE Wi-Fi only está habilitado e não há Wi-Fi disponível, THE Sistema Mobile SHALL adiar a sincronização até Wi-Fi estar disponível
5. WHEN Wi-Fi se torna disponível e há sincronização pendente, THE Sistema Mobile SHALL executar o Worker automaticamente

### Requirement 4

**User Story:** Como um desenvolvedor, eu quero implementar um Worker robusto de sincronização, para que as coletas sejam enviadas de forma confiável mesmo em condições adversas

#### Acceptance Criteria

1. THE Sistema Mobile SHALL criar um ColetaSyncWorker que estende CoroutineWorker do WorkManager
2. WHEN o Worker é executado, THE Sistema Mobile SHALL buscar todas as coletas pendentes do banco local ordenadas por data
3. WHEN o Worker processa coletas, THE Sistema Mobile SHALL enviar cada coleta para o servidor via API REST
4. WHEN uma coleta é sincronizada com sucesso, THE Sistema Mobile SHALL marcar a coleta como sincronizada no banco local
5. IF uma coleta falha na sincronização, THEN THE Sistema Mobile SHALL manter a coleta como pendente e continuar com as próximas
6. WHEN todas as coletas são processadas, THE Sistema Mobile SHALL retornar Result.success() com estatísticas
7. IF ocorrer erro crítico, THEN THE Sistema Mobile SHALL retornar Result.retry() para reexecução automática

### Requirement 5

**User Story:** Como um desenvolvedor, eu quero implementar política de retry inteligente, para que falhas temporárias não impeçam a sincronização

#### Acceptance Criteria

1. WHEN um Worker falha, THE Sistema Mobile SHALL aplicar backoff exponencial com delay inicial de 30 segundos
2. WHEN um Worker falha, THE Sistema Mobile SHALL limitar tentativas de retry a 3 execuções
3. IF todas as tentativas falharem, THEN THE Sistema Mobile SHALL retornar Result.failure() e registrar erro no log
4. WHEN há falha de rede temporária, THE Sistema Mobile SHALL retornar Result.retry() para nova tentativa
5. WHEN há erro de autenticação (401), THE Sistema Mobile SHALL retornar Result.failure() sem retry
6. WHEN há erro de servidor (500), THE Sistema Mobile SHALL retornar Result.retry() com backoff

### Requirement 6

**User Story:** Como um coletor de campo, eu quero testar a sincronização manualmente, para que eu possa verificar se está funcionando corretamente

#### Acceptance Criteria

1. WHEN o coletor acessa a tela de configurações, THE Sistema Mobile SHALL exibir botão "Testar Sincronização"
2. WHEN o coletor clica em testar, THE Sistema Mobile SHALL executar OneTimeWorkRequest imediatamente ignorando constraints
3. WHILE o teste está em execução, THE Sistema Mobile SHALL exibir indicador de progresso
4. WHEN o teste é concluído, THE Sistema Mobile SHALL exibir resultado com total de coletas sincronizadas e erros
5. IF não há coletas pendentes, THEN THE Sistema Mobile SHALL exibir mensagem informativa

### Requirement 7

**User Story:** Como um desenvolvedor, eu quero integrar o Worker com as configurações do usuário, para que as preferências sejam respeitadas

#### Acceptance Criteria

1. WHEN configurações são alteradas, THE Sistema Mobile SHALL cancelar Workers existentes e reagendar com novas configurações
2. WHEN o app é iniciado, THE Sistema Mobile SHALL verificar configurações e agendar Workers apropriados
3. WHEN sincronização por tempo está habilitada, THE Sistema Mobile SHALL usar PeriodicWorkRequest com intervalo configurado
4. WHEN sincronização por contador está habilitada, THE Sistema Mobile SHALL monitorar contador e disparar OneTimeWorkRequest quando atingir limite
5. WHEN ambas sincronizações estão habilitadas, THE Sistema Mobile SHALL executar a que ocorrer primeiro
6. THE Sistema Mobile SHALL persistir configurações usando SharedPreferences

### Requirement 8

**User Story:** Como um coletor de campo, eu quero receber notificações sobre sincronização, para que eu saiba quando minhas coletas foram enviadas

#### Acceptance Criteria

1. WHEN sincronização automática é concluída com sucesso, THE Sistema Mobile SHALL exibir notificação com total de coletas sincronizadas
2. WHEN sincronização automática falha, THE Sistema Mobile SHALL exibir notificação de erro com ação para tentar novamente
3. WHEN o coletor clica na notificação de sucesso, THE Sistema Mobile SHALL abrir tela de estatísticas
4. WHEN o coletor clica na notificação de erro, THE Sistema Mobile SHALL abrir tela de configurações
5. WHERE o coletor desabilitou notificações, THE Sistema Mobile SHALL respeitar a preferência e não exibir notificações

### Requirement 9

**User Story:** Como um administrador do sistema, eu quero que o servidor suporte sincronização em lote, para que múltiplas coletas possam ser enviadas eficientemente

#### Acceptance Criteria

1. WHEN o Sistema Mobile envia coletas, THE Servidor API SHALL aceitar array de coletas em uma única requisição
2. WHEN o Servidor API processa lote, THE Servidor API SHALL validar cada coleta individualmente
3. WHEN o Servidor API processa lote, THE Servidor API SHALL retornar array de resultados indicando sucesso ou falha de cada coleta
4. IF uma coleta no lote falhar, THEN THE Servidor API SHALL processar as demais coletas normalmente
5. WHEN todas as coletas são processadas, THE Servidor API SHALL retornar status 200 com detalhes de cada resultado

### Requirement 10

**User Story:** Como um desenvolvedor, eu quero implementar monitoramento de sincronização, para que eu possa diagnosticar problemas

#### Acceptance Criteria

1. WHEN um Worker é executado, THE Sistema Mobile SHALL registrar log com timestamp, tipo de sincronização e resultado
2. WHEN ocorre erro, THE Sistema Mobile SHALL registrar stack trace completo no log
3. THE Sistema Mobile SHALL manter histórico das últimas 50 execuções de sincronização
4. WHEN o coletor acessa configurações avançadas, THE Sistema Mobile SHALL exibir histórico de sincronizações
5. WHEN o coletor acessa histórico, THE Sistema Mobile SHALL exibir data, hora, tipo, resultado e mensagem de cada execução
6. THE Sistema Mobile SHALL permitir exportar logs de sincronização para análise

### Requirement 11

**User Story:** Como um coletor de campo, eu quero que a sincronização respeite o estado da bateria, para que não descarregue meu dispositivo rapidamente

#### Acceptance Criteria

1. WHEN bateria está abaixo de 15%, THE Sistema Mobile SHALL adiar sincronização automática até bateria estar acima de 20%
2. WHEN dispositivo está em modo economia de bateria, THE Sistema Mobile SHALL reduzir frequência de sincronização pela metade
3. WHEN dispositivo está carregando, THE Sistema Mobile SHALL executar sincronização normalmente sem restrições
4. THE Sistema Mobile SHALL adicionar BatteryNotLow como constraint opcional nos WorkRequests
5. WHEN o coletor habilita "Sincronizar apenas com bateria OK", THE Sistema Mobile SHALL aplicar constraint de bateria

### Requirement 12

**User Story:** Como um desenvolvedor, eu quero implementar sincronização incremental, para que apenas dados novos sejam enviados

#### Acceptance Criteria

1. WHEN o Worker busca coletas pendentes, THE Sistema Mobile SHALL filtrar apenas coletas com sincronizado = false
2. WHEN uma coleta é sincronizada, THE Sistema Mobile SHALL atualizar campo sincronizado para true e armazenar timestamp
3. WHEN o Worker é executado, THE Sistema Mobile SHALL ignorar coletas já sincronizadas
4. THE Sistema Mobile SHALL manter coletas sincronizadas no banco por 7 dias para histórico
5. WHEN coletas sincronizadas têm mais de 7 dias, THE Sistema Mobile SHALL removê-las automaticamente via DatabaseCleanupWorker
