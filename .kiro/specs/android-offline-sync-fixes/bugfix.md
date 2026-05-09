# Documento de Requisitos de Bugfix

## Introdução

Este documento descreve os requisitos para a correção de 5 bugs no fluxo de sincronização offline do app Android do sistema SIHCP (Sistema de Histórico e Coleta Patrimonial). Os bugs afetam a confiabilidade da sincronização de coletas com o servidor Spring Boot, podendo causar perda silenciosa de dados, coletas presas indefinidamente sem possibilidade de reenvio, e envio de coletas para inventários incorretos. Todos os problemas estão concentrados nos arquivos `ColetaRepositoryImpl.kt` e `EnviarColetasPendentesUseCase.kt`. As correções devem ser aplicadas sem alterar a estrutura do banco Room (`ColetaEntity`), sem alterar URLs de endpoints da API, e sem quebrar o fluxo de coleta existente.

---

## Bug Analysis

### Current Behavior (Defect)

**Bug 1 — Coletas ficam presas quando `idInventario = 0` no momento da sincronização em background**

1.1 QUANDO o usuário realiza uma coleta logo após o login, antes de o inventário ativo ser carregado do servidor, ENTÃO o sistema registra o erro `"Inventário ativo não configurado no app"` permanentemente na coleta via `coletaDao.registrarErroSincronizacao()` e encerra o bloco de sincronização com `return@withTimeout`, deixando a coleta presa com `sincronizado = false` e `erroSincronizacao` preenchido

1.2 QUANDO uma coleta fica presa com `erroSincronizacao = "Inventário ativo não configurado no app"`, ENTÃO o sistema não tenta sincronizá-la novamente automaticamente, pois o campo `erroSincronizacao` preenchido não é tratado como condição de retry pelo ciclo de sincronização periódica

1.3 QUANDO o bloco de fallback de rede instável em `registrarColeta()` é executado e `getInventarioAtivoId()` retorna `null`, ENTÃO o sistema registra o mesmo erro permanente `"Inventário ativo não configurado no app"` via `coletaDao.registrarErroSincronizacao()`, causando o mesmo travamento descrito em 1.1

1.4 QUANDO `sincronizarColetasPendentes()` processa coletas pendentes, ENTÃO o sistema obtém o `idInventario` exclusivamente do `PreferencesManager`, ignorando o campo `idInventario` já salvo na própria `ColetaEntity` no momento da coleta

---

**Bug 2 — `EnviarColetasPendentesUseCase` marca coletas como sincronizadas sem enviá-las ao servidor**

2.1 QUANDO `EnviarColetasPendentesUseCase.invoke()` é chamado e há coletas pendentes, ENTÃO o sistema chama `coletaDao.marcarSincronizada(entity.id)` para cada coleta sem realizar nenhuma chamada HTTP ao servidor, pois o bloco `val response = apiService.registrarColeta(coleta)` está comentado com `// TODO: Implementar endpoint de envio`

2.2 QUANDO `EnviarColetasPendentesUseCase` marca uma coleta como sincronizada sem enviá-la, ENTÃO o sistema define `sincronizado = true` no banco Room local, fazendo com que a coleta nunca mais seja incluída em ciclos de sincronização futuros, resultando em perda permanente e silenciosa dos dados no servidor PostgreSQL

2.3 QUANDO `EnviarColetasPendentesUseCase` é chamado com N coletas pendentes, ENTÃO o sistema incrementa o contador `sucesso` para cada coleta sem verificar nenhuma resposta do servidor, reportando 100% de sucesso mesmo sem ter enviado nenhum dado

---

**Bug 3 — `sincronizarColetasPendentes()` usa `buscarPendentes()` sem filtro de inventário, mas o uso do `idInventario` não está documentado nem garantido**

3.1 QUANDO `sincronizarColetasPendentes()` busca coletas para sincronizar, ENTÃO o sistema chama `coletaDao.buscarPendentes()` sem filtro de inventário, podendo incluir coletas de inventários anteriores que ficaram pendentes

3.2 QUANDO `sincronizarEmLote()` monta o `MobileColetaRequest` para cada coleta, ENTÃO o sistema usa `entity.idInventario.takeIf { it > 0 } ?: preferencesManager.getInventarioAtivoId() ?: 0` como `idInventario`, o que é correto para o lote, mas não está documentado nem validado explicitamente como comportamento garantido

---

**Bug 4 — Fallback de inventário em `sincronizarEmLote()` pode enviar coleta para inventário errado**

4.1 QUANDO `sincronizarEmLote()` processa uma coleta cuja `entity.idInventario = 0` (coleta salva sem inventário configurado), ENTÃO o sistema usa `preferencesManager.getInventarioAtivoId()` como fallback, que pode ser um inventário diferente daquele em que a coleta foi originalmente realizada

4.2 QUANDO o fallback de inventário em `sincronizarEmLote()` é acionado e o inventário ativo atual é diferente do inventário original da coleta, ENTÃO o sistema envia a coleta ao servidor com o `idInventario` errado, associando o patrimônio coletado a um inventário incorreto no banco PostgreSQL

4.3 QUANDO `sincronizarIndividualmente()` processa uma coleta com `entity.idInventario = 0`, ENTÃO o sistema aplica o mesmo fallback perigoso `entity.idInventario.takeIf { it > 0 } ?: preferencesManager.getInventarioAtivoId() ?: 0`, com o mesmo risco de envio para inventário errado

---

**Bug 5 — Sem mecanismo de reset automático para coletas com muitas tentativas falhas por erros recuperáveis**

5.1 QUANDO uma coleta acumula `tentativasSincronizacao >= 5` com `erroSincronizacao` contendo mensagens de erros transitórios (ex: timeout, erro de rede, `"Timeout na sincronização"`), ENTÃO o sistema não executa nenhuma ação automática para limpar o erro e permitir nova tentativa, deixando a coleta presa indefinidamente

5.2 QUANDO `sincronizarColetasPendentes()` é executado, ENTÃO o sistema não verifica nem processa coletas com muitas tentativas falhas antes de iniciar o ciclo de sincronização, mesmo que os métodos `buscarColetasComMuitasTentativas()` e `limparErroSincronizacao()` já existam no `ColetaDao`

5.3 QUANDO o usuário não tem visibilidade sobre coletas com muitas tentativas falhas, ENTÃO o sistema não fornece nenhum mecanismo automático de recuperação para erros transitórios, exigindo intervenção manual que o usuário não sabe que precisa fazer

---

### Expected Behavior (Correct)

**Bug 1 — Coletas ficam presas quando `idInventario = 0`**

2.1 QUANDO o bloco de sincronização em background de `registrarColeta()` detecta `inventarioId <= 0`, ENTÃO o sistema SHALL deixar a coleta como pendente sem registrar erro (mantendo `erroSincronizacao = null`), para que o próximo ciclo de sincronização tente novamente

2.2 QUANDO `inventarioId <= 0` é detectado no bloco de sincronização em background e a coleta já acumulou `tentativasSincronizacao >= 3`, ENTÃO o sistema SHALL registrar o erro permanente `"Inventário ativo não configurado no app"` via `coletaDao.registrarErroSincronizacao()`, sinalizando que o problema persiste após múltiplas tentativas

2.3 QUANDO o bloco de fallback de rede instável em `registrarColeta()` detecta `inventarioId <= 0`, ENTÃO o sistema SHALL aplicar a mesma lógica de 2.1 e 2.2, deixando a coleta pendente sem erro nas primeiras tentativas

2.4 QUANDO `sincronizarColetasPendentes()` processa uma coleta pendente, ENTÃO o sistema SHALL tentar obter o `idInventario` primeiro do campo `entity.idInventario` da própria `ColetaEntity`, usando o `PreferencesManager` apenas como fallback secundário quando `entity.idInventario <= 0`

---

**Bug 2 — `EnviarColetasPendentesUseCase` marca coletas como sincronizadas sem enviá-las**

2.5 QUANDO `EnviarColetasPendentesUseCase.invoke()` processa uma coleta pendente, ENTÃO o sistema SHALL converter a `ColetaEntity` para `MobileColetaRequest` com os campos corretos (`numeroPatrimonio`, `idInventario`, `usuarioId`, `estadoEncontrado`, etc.) e chamar `coletaApi.registrarColeta(request)`

2.6 QUANDO `coletaApi.registrarColeta(request)` retorna `response.success == true`, ENTÃO o sistema SHALL chamar `coletaDao.marcarSincronizada(entity.id)` e incrementar o contador de sucesso

2.7 QUANDO `coletaApi.registrarColeta(request)` retorna `response.success == false` ou lança exceção, ENTÃO o sistema SHALL chamar `coletaDao.registrarErroSincronizacao(entity.id, mensagemDeErro)` e incrementar o contador de falhas, sem marcar a coleta como sincronizada

---

**Bug 3 — Uso do `idInventario` em `sincronizarColetasPendentes()` sem garantia explícita**

2.8 QUANDO `sincronizarColetasPendentes()` busca coletas para sincronizar, ENTÃO o sistema SHALL continuar usando `coletaDao.buscarPendentes()` sem filtro de inventário, para garantir que coletas de qualquer inventário sejam processadas

2.9 QUANDO `sincronizarEmLote()` e `sincronizarIndividualmente()` montam o `MobileColetaRequest`, ENTÃO o sistema SHALL garantir e documentar explicitamente que o `idInventario` enviado ao servidor é sempre o da própria `entity.idInventario`, nunca o do `PreferencesManager` como fonte primária

---

**Bug 4 — Fallback de inventário pode enviar coleta para inventário errado**

2.10 QUANDO `sincronizarEmLote()` ou `sincronizarIndividualmente()` detecta que `entity.idInventario <= 0`, ENTÃO o sistema SHALL registrar erro na coleta via `coletaDao.registrarErroSincronizacao()` com mensagem `"idInventario inválido (0) — coleta não pode ser enviada sem inventário definido"` e pular essa coleta no lote, sem usar o inventário ativo atual como fallback

2.11 QUANDO uma coleta é pulada por `entity.idInventario <= 0`, ENTÃO o sistema SHALL emitir log de aviso (`Log.w`) com o ID da coleta e o número do patrimônio, para facilitar diagnóstico

---

**Bug 5 — Sem reset automático para coletas com muitas tentativas falhas**

2.12 QUANDO `sincronizarColetasPendentes()` é iniciado, ENTÃO o sistema SHALL verificar se há coletas com `tentativasSincronizacao >= 5` e `erroSincronizacao` contendo mensagens de erros recuperáveis (ex: `"Timeout"`, `"timeout"`, `"conexão"`, `"connection"`, `"network"`, `"rede"`)

2.13 QUANDO coletas com erros recuperáveis e `tentativasSincronizacao >= 5` são identificadas, ENTÃO o sistema SHALL chamar `coletaDao.limparErroSincronizacao(id)` para cada uma, zerando `erroSincronizacao` e `tentativasSincronizacao`, permitindo que sejam incluídas no ciclo de sincronização atual

2.14 QUANDO `erroSincronizacao` contém mensagens de erros permanentes (ex: `"Patrimônio não encontrado"`, `"idInventario inválido"`, `"Inventário ativo não configurado"` após 3+ tentativas), ENTÃO o sistema SHALL NOT limpar o erro automaticamente, preservando o diagnóstico para intervenção manual

---

### Unchanged Behavior (Regression Prevention)

**Preservação do fluxo de coleta e sincronização existente**

3.1 QUANDO o usuário realiza uma coleta com inventário ativo configurado e rede disponível, ENTÃO o sistema SHALL CONTINUE TO tentar sincronizar imediatamente em background e marcar como `sincronizado = true` em caso de sucesso, sem alteração no fluxo principal

3.2 QUANDO `sincronizarColetasPendentes()` processa coletas com `entity.idInventario > 0`, ENTÃO o sistema SHALL CONTINUE TO usar o `idInventario` da entity como fonte primária para montar o `MobileColetaRequest`, sem regressão no comportamento atual de coletas válidas

3.3 QUANDO `sincronizarEmLote()` recebe coletas com `entity.idInventario > 0`, ENTÃO o sistema SHALL CONTINUE TO enviar o lote ao servidor via `coletaApi.registrarColetasEmLote()` e marcar as coletas bem-sucedidas como sincronizadas, com o mesmo comportamento atual

3.4 QUANDO `sincronizarIndividualmente()` processa coletas com `entity.idInventario > 0`, ENTÃO o sistema SHALL CONTINUE TO enviar cada coleta individualmente via `coletaApi.registrarColeta()` e marcar como sincronizada em caso de sucesso, com o mesmo comportamento atual

3.5 QUANDO o servidor retorna `response.success == false` para uma coleta específica durante a sincronização, ENTÃO o sistema SHALL CONTINUE TO registrar o erro via `coletaDao.registrarErroSincronizacao()` e não marcar a coleta como sincronizada, preservando o comportamento atual de tratamento de erros do servidor

3.6 QUANDO `ColetaSyncWorker` executa a sincronização periódica em background via WorkManager, ENTÃO o sistema SHALL CONTINUE TO chamar `SincronizarColetasPendentesUseCase` e retornar `Result.success` ou `Result.retry` conforme o resultado, sem alteração no contrato do Worker

3.7 QUANDO coletas com `erroSincronizacao` contendo mensagens de erros permanentes (ex: `"Patrimônio não encontrado"`) são encontradas durante a sincronização, ENTÃO o sistema SHALL CONTINUE TO não tentar reenviá-las automaticamente, preservando o comportamento atual de não-retry para erros permanentes

3.8 QUANDO `ColetaEntity` é lida ou escrita no banco Room, ENTÃO o sistema SHALL CONTINUE TO usar a mesma estrutura de colunas sem nenhuma migration, pois nenhuma alteração de schema é permitida

3.9 QUANDO `registrarColeta()` salva uma coleta localmente via `coletaDao.registrarColetaComTransacao()`, ENTÃO o sistema SHALL CONTINUE TO retornar `Result.success(coleta.copy(id = id))` imediatamente após o salvamento local, sem bloquear a UI aguardando a sincronização em background

---

## Condição de Bug e Propriedades de Verificação

### Bug 1 — Coletas presas por `idInventario = 0`

```pascal
FUNCTION isBugCondition_Bug1(X)
  INPUT: X = ColetaEntity com tentativasSincronizacao = T
  OUTPUT: boolean

  RETURN X.idInventario <= 0 AND T < 3
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug1(X) DO
  result ← sincronizarEmBackground'(X)
  ASSERT result.erroSincronizacao = null
  ASSERT result.sincronizado = false
  ASSERT result.tentativasSincronizacao = T + 1
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug1(X) DO
  ASSERT F(X) = F'(X)
END FOR
```

### Bug 2 — `EnviarColetasPendentesUseCase` sem envio real

```pascal
FUNCTION isBugCondition_Bug2(X)
  INPUT: X = chamada a EnviarColetasPendentesUseCase.invoke()
  OUTPUT: boolean

  RETURN X.coletasPendentes.size > 0
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug2(X) DO
  result ← EnviarColetasPendentesUseCase'(X)
  ASSERT coletaApi.registrarColeta foi chamado para cada coleta
  ASSERT coleta.sincronizado = true SOMENTE SE response.success = true
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug2(X) DO
  ASSERT F(X) = F'(X)  // nenhuma coleta pendente: resultado idêntico
END FOR
```

### Bug 4 — Fallback de inventário errado em `sincronizarEmLote()`

```pascal
FUNCTION isBugCondition_Bug4(X)
  INPUT: X = ColetaEntity
  OUTPUT: boolean

  RETURN X.idInventario <= 0
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug4(X) DO
  result ← sincronizarEmLote'(X)
  ASSERT X NÃO foi enviado ao servidor
  ASSERT X.erroSincronizacao contém "idInventario inválido"
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug4(X) DO
  ASSERT F(X) = F'(X)  // coletas com idInventario válido: comportamento idêntico
END FOR
```

### Bug 5 — Sem reset automático para erros recuperáveis

```pascal
FUNCTION isBugCondition_Bug5(X)
  INPUT: X = ColetaEntity
  OUTPUT: boolean

  RETURN X.tentativasSincronizacao >= 5
    AND X.erroSincronizacao CONTAINS_ANY ["Timeout", "timeout", "conexão", "connection", "network", "rede"]
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug5(X) DO
  result ← sincronizarColetasPendentes'()
  ASSERT X.erroSincronizacao = null
  ASSERT X.tentativasSincronizacao = 0
  ASSERT X incluída no ciclo de sincronização atual
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug5(X) DO
  ASSERT F(X) = F'(X)  // coletas sem erro recuperável: comportamento idêntico
END FOR
```
