# Tarefas de Implementação — mobile-server-critical-fixes

## Visão Geral

Correção de 4 bugs críticos no servidor mobile Spring Boot do SIHCP. As tarefas estão ordenadas do menor risco ao maior, para que cada correção possa ser verificada de forma isolada sem afetar as demais.

---

- [x] 1. Bug 1 — Substituir instanciação manual de MobilePatrimonioService por injeção Spring
  - Abrir `MobileColetaController.java`
  - Adicionar campo `@Autowired private MobilePatrimonioService mobilePatrimonioService;` junto aos demais campos injetados no topo da classe (após o campo `mobileColetaService`)
  - No método `verificarDuplicataColeta()`, remover as duas linhas que instanciam `MobilePatrimonioService` via `new`:
    ```java
    com.inventario.mobile.server.service.MobilePatrimonioService patrimonioService = 
        new com.inventario.mobile.server.service.MobilePatrimonioService();
    ```
  - Substituir a variável local `patrimonioService` pelo campo injetado `mobilePatrimonioService` nas chamadas subsequentes do método
  - Verificar que o import de `MobilePatrimonioService` já existe ou adicioná-lo
  - Compilar e confirmar que não há erros de compilação
  - _Arquivos: `src/main/java/com/inventario/mobile/server/controller/MobileColetaController.java`_

- [x] 2. Bug 2 — Adicionar limite máximo e header de aviso no endpoint /coletas/all
  - Abrir `MobileColetaController.java`, método `buscarTodasColetasSemPaginacao()`
  - Definir constante `private static final int MAX_COLETAS_ALL = 500;` no topo da classe
  - Substituir a linha `mobileColetaService.buscarColetasComPaginacaoReal(0, Math.max(totalColetas, 1), inventarioId)` por `mobileColetaService.buscarColetasComPaginacaoReal(0, Math.min(totalColetas, MAX_COLETAS_ALL), inventarioId)` para garantir que nunca ultrapasse 500 registros
  - Após obter a resposta, adicionar o header `X-Warning` sempre presente com o texto `"Endpoint deprecated. Use GET /api/mobile/coletas?page=0&size=20 para paginacao"`
  - Quando `totalColetas > MAX_COLETAS_ALL`, adicionar ao header `X-Warning` também a informação de truncamento: `"Resultado truncado em 500 de " + totalColetas + " coletas. Use paginacao."`
  - Usar `ResponseEntity` com `.header("X-Warning", mensagem)` para incluir o header na resposta
  - Compilar e confirmar que não há erros de compilação
  - _Arquivos: `src/main/java/com/inventario/mobile/server/controller/MobileColetaController.java`_

- [x] 3. Bug 3 — Remover bean CorsConfigurationSource duplicado de CorsConfig
  - Abrir `CorsConfig.java`
  - Remover completamente o método `corsConfigurationSource()` anotado com `@Bean` (aproximadamente 40 linhas, do `@Bean` até o `return source;` e o fechamento do método)
  - Remover os imports que ficarem sem uso após a remoção: `CorsConfiguration`, `CorsConfigurationSource`, `UrlBasedCorsConfigurationSource`, `Arrays` (verificar se ainda são usados em `addCorsMappings`)
  - No método `addCorsMappings()`, corrigir o log incorreto: substituir a linha `logger.info("║ Permitindo todas as origens para aplicativo Android")` por `logger.info("║ Configurando CORS via WebMvcConfigurer (MVC layer)")`
  - Remover o bloco de logs ao final do método `addCorsMappings()` que afirma "Credenciais: permitidas" (pois `allowCredentials(false)` está configurado)
  - Confirmar que `MobileSecurityConfig.corsConfigurationSource()` permanece intacto como único bean
  - Compilar e confirmar que não há erros de compilação nem conflito de beans
  - _Arquivos: `src/main/java/com/inventario/mobile/server/config/CorsConfig.java`_

- [x] 4. Bug 4 — Implementar blacklist de tokens JWT para invalidação no logout
  - [x] 4.1 Adicionar blacklist em MobileAuthService
    - Abrir `MobileAuthService.java`
    - Adicionar campo privado: `private final java.util.concurrent.ConcurrentHashMap<String, Long> tokenBlacklist = new java.util.concurrent.ConcurrentHashMap<>();`
    - Implementar método público `invalidateToken(String token)`: extrair o tempo de expiração do token via `jwtTokenProvider`, calcular o TTL restante em milissegundos e armazenar `tokenBlacklist.put(token, System.currentTimeMillis() + ttlRestante)`
    - Implementar método público `isTokenBlacklisted(String token)`: verificar se o token está no mapa; se estiver, checar se o TTL já expirou — se expirado, remover do mapa (limpeza lazy) e retornar `false`; se ainda válido, retornar `true`
    - Implementar método privado `cleanExpiredTokens()`: iterar o mapa e remover entradas com timestamp menor que `System.currentTimeMillis()` (chamado dentro de `invalidateToken` para limpeza periódica lazy)
    - No método `validateToken(String token)`: adicionar verificação `if (isTokenBlacklisted(token)) return false;` antes de chamar `jwtTokenProvider.validateToken(token)`
    - _Arquivos: `src/main/java/com/inventario/mobile/server/service/MobileAuthService.java`_

  - [x] 4.2 Conectar logout ao mecanismo de blacklist
    - Abrir `MobileAuthController.java`, método `logout()`
    - Alterar a assinatura do parâmetro para aceitar o token do header `Authorization` além do `@RequestParam`: adicionar `@RequestHeader(value = "Authorization", required = false) String authHeader` como parâmetro adicional
    - No corpo do método, extrair o token do header (remover prefixo `"Bearer "`) ou usar o `@RequestParam token` se o header não estiver presente
    - Chamar `mobileAuthService.invalidateToken(token)` com o token extraído
    - Manter o retorno `200 OK` com a mensagem de sucesso atual — não alterar o formato da resposta
    - Adicionar log `logger.info("Token invalidado para logout")` após a chamada
    - _Arquivos: `src/main/java/com/inventario/mobile/server/controller/MobileAuthController.java`_

  - [x] 4.3 Verificar blacklist no filtro JWT
    - Abrir `MobileJwtAuthenticationFilter.java`
    - No método `doFilterInternal()`, após a linha `boolean isValid = mobileAuthService.validateToken(jwt);`, o comportamento já está correto: `validateToken` agora consulta a blacklist internamente (tarefa 4.1), então nenhuma mudança adicional é necessária no filtro
    - Verificar que o fluxo `if (isValid)` continua funcionando corretamente — tokens na blacklist retornarão `isValid = false` e o filtro não autenticará o usuário
    - Confirmar que a lógica de `shouldNotFilter` permanece inalterada
    - _Arquivos: `src/main/java/com/inventario/mobile/server/security/MobileJwtAuthenticationFilter.java`_
