# Documento de Requisitos de Bugfix

## Introdução

Este documento descreve os requisitos para a correção de 4 bugs críticos no servidor mobile Spring Boot do sistema SIHCP (Sistema de Histórico e Coleta Patrimonial). Os bugs afetam a integridade do contêiner Spring IoC, o consumo de memória em endpoints de listagem, a configuração de CORS e a segurança do mecanismo de logout com JWT. Todos os problemas foram identificados no módulo `com.inventario.mobile.server` e devem ser corrigidos sem quebrar a compatibilidade com o app Android existente.

---

## Bug Analysis

### Current Behavior (Defect)

**Bug 1 — MobilePatrimonioService instanciado manualmente no controller**

1.1 QUANDO o endpoint `POST /api/mobile/coletas/verificar-duplicata` é chamado, ENTÃO o sistema instancia `MobilePatrimonioService` via `new` diretamente no método `verificarDuplicataColeta()`, bypassando o contêiner Spring IoC

1.2 QUANDO `MobilePatrimonioService` é instanciado via `new`, ENTÃO o sistema cria novos objetos DAO a cada requisição, sem compartilhar estado, conexões ou ciclo de vida com o bean gerenciado pelo Spring

1.3 QUANDO `MobilePatrimonioService` é instanciado via `new`, ENTÃO o sistema ignora quaisquer dependências injetadas (`@Autowired`) que o serviço possa ter, resultando em comportamento inconsistente em relação ao restante da aplicação

---

**Bug 2 — Endpoint GET /api/mobile/coletas/all carrega tudo em memória**

2.1 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado com um inventário contendo muitas coletas, ENTÃO o sistema conta o total de coletas e usa esse número como tamanho de página, carregando todos os registros em memória de uma só vez

2.2 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado com 10.000 ou mais coletas, ENTÃO o sistema não aplica nenhum limite máximo de registros retornados, podendo causar estouro de memória (OutOfMemoryError) no servidor

2.3 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado, ENTÃO o sistema não retorna nenhum aviso ao cliente de que o endpoint está depreciado e que deve ser usada a versão paginada

---

**Bug 3 — CORS configurado em dois lugares com configurações conflitantes**

3.1 QUANDO a aplicação inicializa, ENTÃO o sistema registra dois beans com o nome `corsConfigurationSource` — um em `CorsConfig.java` e outro em `MobileSecurityConfig.java` — resultando em comportamento imprevisível sobre qual configuração o Spring Security efetivamente aplica

3.2 QUANDO o Spring Security resolve o bean `corsConfigurationSource`, ENTÃO o sistema pode aplicar a configuração de `CorsConfig.java` (que registra o mapeamento em `/**`) em vez da configuração de `MobileSecurityConfig.java` (que restringe a `api/mobile/**`), abrindo origens não intencionais

3.3 QUANDO `CorsConfig.java` inicializa o bean `corsConfigurationSource`, ENTÃO o sistema registra no log a mensagem "Credenciais: permitidas", mas o código define `setAllowCredentials(false)`, gerando informação incorreta nos logs de diagnóstico

3.4 QUANDO `CorsConfig.addCorsMappings()` é executado via `WebMvcConfigurer`, ENTÃO o sistema permite `allowedOriginPatterns("*")` (qualquer origem) para o MVC layer, enquanto o bean de segurança usa IPs específicos, criando inconsistência entre as camadas de CORS

---

**Bug 4 — Logout não invalida tokens JWT**

4.1 QUANDO o endpoint `POST /api/mobile/auth/logout` é chamado com um token válido, ENTÃO o sistema retorna sucesso sem realizar nenhuma ação de invalidação, deixando o token ativo por até 24 horas após o logout

4.2 QUANDO uma conta de usuário é comprometida ou desativada e o usuário realiza logout, ENTÃO o sistema não impede que o token JWT anterior continue sendo usado para autenticar requisições até sua expiração natural

4.3 QUANDO o filtro `MobileJwtAuthenticationFilter` processa uma requisição com um token de um usuário que já realizou logout, ENTÃO o sistema autentica a requisição normalmente, pois não existe mecanismo de verificação de tokens invalidados

---

### Expected Behavior (Correct)

**Bug 1 — MobilePatrimonioService instanciado manualmente no controller**

2.1 QUANDO o endpoint `POST /api/mobile/coletas/verificar-duplicata` é chamado, ENTÃO o sistema SHALL utilizar o bean `MobilePatrimonioService` gerenciado pelo Spring, injetado via `@Autowired` no `MobileColetaController`, da mesma forma que `MobileColetaService` já é injetado

2.2 QUANDO `MobilePatrimonioService` é injetado via `@Autowired`, ENTÃO o sistema SHALL compartilhar o mesmo ciclo de vida, estado e dependências do bean singleton gerenciado pelo contêiner Spring IoC

2.3 QUANDO `MobilePatrimonioService` é injetado via `@Autowired`, ENTÃO o sistema SHALL garantir que todas as dependências internas do serviço sejam resolvidas corretamente pelo Spring, sem instanciar objetos adicionais desnecessários por requisição

---

**Bug 2 — Endpoint GET /api/mobile/coletas/all carrega tudo em memória**

2.4 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado, ENTÃO o sistema SHALL aplicar um limite máximo de 500 registros retornados, independentemente do total de coletas existentes no inventário

2.5 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado e o total de coletas excede 500, ENTÃO o sistema SHALL retornar o header `X-Warning` na resposta HTTP com uma mensagem indicando que o resultado foi truncado e que o endpoint está depreciado

2.6 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado, ENTÃO o sistema SHALL retornar o header `X-Warning` indicando que o endpoint está depreciado e que o cliente deve usar `GET /api/mobile/coletas` com paginação (`page`, `size`)

---

**Bug 3 — CORS configurado em dois lugares com configurações conflitantes**

2.7 QUANDO a aplicação inicializa, ENTÃO o sistema SHALL possuir apenas um bean `corsConfigurationSource`, definido exclusivamente em `MobileSecurityConfig.java`, sendo esta a fonte única de verdade para a configuração de CORS do Spring Security

2.8 QUANDO `CorsConfig.java` é carregado, ENTÃO o sistema SHALL manter apenas a implementação de `WebMvcConfigurer.addCorsMappings()` para o MVC layer, sem declarar o bean `@Bean CorsConfigurationSource`, eliminando o conflito de beans duplicados

2.9 QUANDO `CorsConfig.java` registra mensagens de log sobre a configuração de CORS, ENTÃO o sistema SHALL exibir informações corretas e consistentes com o código, sem afirmar que credenciais estão habilitadas quando `setAllowCredentials(false)` está configurado

---

**Bug 4 — Logout não invalida tokens JWT**

2.10 QUANDO o endpoint `POST /api/mobile/auth/logout` é chamado com um token válido, ENTÃO o sistema SHALL adicionar o token a uma blacklist em memória (`ConcurrentHashMap`) no `MobileAuthService`, com TTL baseado no tempo de expiração restante do token

2.11 QUANDO o filtro `MobileJwtAuthenticationFilter` processa uma requisição com um token JWT, ENTÃO o sistema SHALL verificar se o token está presente na blacklist antes de autenticar o usuário, rejeitando a requisição com HTTP 401 caso o token tenha sido invalidado por logout

2.12 QUANDO um token expirado é removido da blacklist (por TTL), ENTÃO o sistema SHALL garantir que tokens expirados naturalmente não causem acúmulo de memória, limpando entradas vencidas da blacklist de forma periódica ou lazy

---

### Unchanged Behavior (Regression Prevention)

**Preservação geral de endpoints e compatibilidade**

3.1 QUANDO qualquer endpoint existente em `/api/mobile/**` é chamado com um token JWT válido e não invalidado, ENTÃO o sistema SHALL CONTINUE TO autenticar e processar a requisição normalmente, sem regressões no fluxo de autenticação

3.2 QUANDO o endpoint `POST /api/mobile/auth/login` é chamado com credenciais válidas, ENTÃO o sistema SHALL CONTINUE TO retornar access token, refresh token e informações do usuário no mesmo formato de resposta atual

3.3 QUANDO o endpoint `POST /api/mobile/auth/refresh` é chamado com um refresh token válido, ENTÃO o sistema SHALL CONTINUE TO renovar o access token sem exigir nova autenticação

3.4 QUANDO o endpoint `GET /api/mobile/coletas` é chamado com parâmetros de paginação (`page`, `size`), ENTÃO o sistema SHALL CONTINUE TO retornar coletas paginadas com o comportamento atual, sem alterações

3.5 QUANDO o endpoint `POST /api/mobile/coletas/verificar-duplicata` é chamado com dados válidos, ENTÃO o sistema SHALL CONTINUE TO retornar o resultado de verificação de duplicata no mesmo formato de resposta atual (`duplicado`, `mensagem`, `podeRegistrar`, etc.)

3.6 QUANDO qualquer endpoint protegido é chamado sem token ou com token inválido (não relacionado a logout), ENTÃO o sistema SHALL CONTINUE TO retornar HTTP 401 com a resposta de erro padrão atual

3.7 QUANDO a aplicação inicializa, ENTÃO o sistema SHALL CONTINUE TO aplicar as origens CORS permitidas definidas em `MobileSecurityConfig.java` (`192.168.10.107:8081`, `localhost:8081`, `10.0.2.2:8081`) para os endpoints `/api/mobile/**`

3.8 QUANDO o endpoint `GET /api/mobile/coletas/all` é chamado com um inventário contendo 500 ou menos coletas, ENTÃO o sistema SHALL CONTINUE TO retornar todas as coletas sem truncamento, mantendo compatibilidade com clientes que dependem desse comportamento para volumes pequenos

3.9 QUANDO os endpoints de coleta, patrimônio, sala, setor, inventário e autenticação são chamados com as URLs atuais, ENTÃO o sistema SHALL CONTINUE TO responder nas mesmas URLs sem alteração de path, preservando a compatibilidade com o app Android

---

## Condição de Bug e Propriedades de Verificação

### Bug 1 — Instanciação Manual de Bean

```pascal
FUNCTION isBugCondition_Bug1(X)
  INPUT: X = chamada ao método verificarDuplicataColeta()
  OUTPUT: boolean
  
  RETURN X contém instanciação via "new MobilePatrimonioService()"
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug1(X) DO
  result ← verificarDuplicataColeta'(X)
  ASSERT result usa bean Spring injetado via @Autowired
  ASSERT result NÃO instancia MobilePatrimonioService via new
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug1(X) DO
  ASSERT F(X) = F'(X)  // demais endpoints não são afetados
END FOR
```

### Bug 2 — Carregamento Irrestrito em Memória

```pascal
FUNCTION isBugCondition_Bug2(X)
  INPUT: X = chamada a GET /api/mobile/coletas/all com totalColetas registros
  OUTPUT: boolean
  
  RETURN X.totalColetas > 500
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug2(X) DO
  result ← buscarTodasColetasSemPaginacao'(X)
  ASSERT result.content.size() <= 500
  ASSERT result.headers contém "X-Warning"
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug2(X) DO
  ASSERT F(X).content = F'(X).content  // retorno idêntico para <= 500 coletas
END FOR
```

### Bug 3 — Beans CORS Duplicados

```pascal
FUNCTION isBugCondition_Bug3(X)
  INPUT: X = contexto Spring com CorsConfig e MobileSecurityConfig carregados
  OUTPUT: boolean
  
  RETURN X contém dois beans com nome "corsConfigurationSource"
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug3(X) DO
  result ← applicationContext'(X)
  ASSERT result.getBeansOfType(CorsConfigurationSource).size() = 1
  ASSERT bean único está em MobileSecurityConfig
END FOR
```

### Bug 4 — Logout Sem Invalidação de Token

```pascal
FUNCTION isBugCondition_Bug4(X)
  INPUT: X = token JWT que foi submetido ao endpoint de logout
  OUTPUT: boolean
  
  RETURN X foi enviado a POST /api/mobile/auth/logout com sucesso
END FUNCTION

// Propriedade: Fix Checking
FOR ALL X WHERE isBugCondition_Bug4(X) DO
  result ← autenticarComToken'(X)
  ASSERT result = HTTP 401 (token na blacklist)
END FOR

// Propriedade: Preservation Checking
FOR ALL X WHERE NOT isBugCondition_Bug4(X) DO
  ASSERT F(X) = F'(X)  // tokens válidos não afetados pela blacklist
END FOR
```
