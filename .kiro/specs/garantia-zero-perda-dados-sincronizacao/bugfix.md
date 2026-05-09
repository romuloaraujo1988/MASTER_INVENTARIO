# Bugfix Requirements Document

## Introduction

O sistema de sincronização do app Android apresenta múltiplos pontos de falha que podem causar perda de dados de coletas. O problema central é que a sincronização atual é assíncrona e não garante que os dados sejam enviados ao servidor, especialmente em cenários de rede instável, fechamento abrupto do app, ou falhas no WorkManager. Este bugfix visa garantir 0% de perda de dados implementando mecanismos robustos de persistência, retry inteligente, e validação de integridade.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN uma coleta é registrada e o app é fechado antes da sincronização completar THEN o sistema deixa os dados pendentes sem garantia de envio futuro

1.2 WHEN a sincronização encontra timeout de 15 segundos em redes lentas (2G/3G) THEN o sistema falha e apenas registra erro sem retry automático garantido

1.3 WHEN o WorkManager não executa devido a bateria baixa (<20%) ou falta de rede THEN o sistema não sincroniza as coletas pendentes e não notifica o usuário

1.4 WHEN ocorre falha na sincronização (timeout, erro de rede, erro do servidor) THEN o sistema apenas registra o erro em log sem notificar o usuário ou garantir retry

1.5 WHEN o batch sync falha parcialmente e o parsing de erros do servidor falha THEN o sistema pode deixar coletas em estado inconsistente (não marcadas como sincronizadas nem com erro registrado)

1.6 WHEN a verificação de rede indica apenas que há conexão mas o servidor está inacessível THEN o sistema tenta sincronizar e falha sem validação prévia de acessibilidade do servidor

1.7 WHEN múltiplas coletas são sincronizadas em lote e algumas falham THEN o sistema pode não marcar corretamente quais coletas foram sincronizadas devido à lógica complexa de parsing de erros

1.8 WHEN o timeout de fallback é de apenas 8 segundos em rede instável THEN o sistema falha mesmo com servidor funcional devido ao timeout muito curto

### Expected Behavior (Correct)

2.1 WHEN uma coleta é registrada THEN o sistema SHALL garantir que ela seja persistida localmente com transação atômica e agendada para sincronização garantida

2.2 WHEN a sincronização encontra timeout THEN o sistema SHALL usar timeout adaptativo baseado na qualidade da rede e implementar retry automático com backoff exponencial

2.3 WHEN o WorkManager não pode executar devido a restrições THEN o sistema SHALL implementar mecanismo alternativo de sincronização prioritária e notificar o usuário sobre coletas pendentes

2.4 WHEN ocorre falha na sincronização THEN o sistema SHALL registrar o erro, agendar retry automático com backoff exponencial, e notificar o usuário sobre falhas críticas

2.5 WHEN o batch sync falha parcialmente THEN o sistema SHALL identificar precisamente quais coletas falharam, marcar as bem-sucedidas, e registrar erros específicos para cada falha

2.6 WHEN antes de tentar sincronizar THEN o sistema SHALL validar se o servidor está acessível através de health check antes de iniciar o envio de dados

2.7 WHEN múltiplas coletas são sincronizadas em lote THEN o sistema SHALL usar identificadores únicos (IDs) ao invés de parsing de strings para determinar quais coletas foram sincronizadas

2.8 WHEN a rede está instável THEN o sistema SHALL usar timeout adaptativo mínimo de 30 segundos e implementar estratégia de retry inteligente

2.9 WHEN uma coleta é sincronizada com sucesso THEN o sistema SHALL validar que o servidor confirmou o recebimento e armazenou os dados antes de marcar como sincronizada

2.10 WHEN há coletas pendentes críticas (>24h sem sincronizar) THEN o sistema SHALL notificar o usuário e priorizar a sincronização dessas coletas

### Unchanged Behavior (Regression Prevention)

3.1 WHEN uma coleta é registrada com sucesso e sincronizada imediatamente THEN o sistema SHALL CONTINUE TO salvar localmente primeiro e sincronizar em background sem bloquear a UI

3.2 WHEN a rede está boa (WiFi ou 4G) e o servidor responde rapidamente THEN o sistema SHALL CONTINUE TO sincronizar coletas em lote (batch sync) para eficiência

3.3 WHEN uma coleta é sincronizada com sucesso THEN o sistema SHALL CONTINUE TO marcar como sincronizada no banco local e remover da fila de pendentes

3.4 WHEN o usuário registra múltiplas coletas offline THEN o sistema SHALL CONTINUE TO armazenar todas localmente e sincronizar quando houver conexão

3.5 WHEN a sincronização em lote é bem-sucedida para todas as coletas THEN o sistema SHALL CONTINUE TO marcar todas como sincronizadas de uma vez

3.6 WHEN o NetworkQualityMonitor detecta boa qualidade de rede THEN o sistema SHALL CONTINUE TO tentar sincronização imediata em background

3.7 WHEN uma coleta tem todos os dados válidos e o servidor está acessível THEN o sistema SHALL CONTINUE TO sincronizar com sucesso na primeira tentativa

3.8 WHEN o usuário está coletando patrimônios THEN o sistema SHALL CONTINUE TO permitir registro de coletas independentemente do status de sincronização de coletas anteriores
