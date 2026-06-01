# Diagnóstico Final — Monitor ao Vivo (WebSocket) Não Funcionava

**Data:** 29/05/2026 13:44
**Versão:** 2.9.0
**Build:** sihcp-server.jar (97 MB)

---

## 🎯 CAUSA RAIZ ENCONTRADA

O monitor ao vivo nunca funcionou porque a auto-configuração de WebSocket do
Spring Boot estava **explicitamente desabilitada** no `application-mobile.properties`:

```properties
spring.autoconfigure.exclude=\
  ...,\
  org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration
```

Essa exclusão foi adicionada "para economizar memória", mas ela é **exatamente**
a auto-config responsável por registrar o `WsSci` do Tomcat — o
`ServletContainerInitializer` que cria o
`jakarta.websocket.server.ServerContainer` (JSR-356) no `ServletContext`.

### Por que isso quebrava só o WebSocket nativo (e não o STOMP)

- **STOMP (dashboard web)**: usa transportes SockJS sobre HTTP comum. NÃO
  depende do `ServerContainer`. Por isso continuou funcionando.
- **WebSocket nativo (`/ws-coletas-live`, monitor ao vivo do app)**: usa
  `TextWebSocketHandler` + `AbstractStandardUpgradeStrategy` do Spring, que
  **exige** o `ServerContainer` no handshake. Sem ele, o upgrade falhava com:
  ```
  No 'jakarta.websocket.server.ServerContainer' ServletContext attribute.
  Are you running in a Servlet container that supports JSR-356?
  ```
  O app recebia falha no handshake → status "Erro"/"Offline" no monitor.

### Por que as tentativas anteriores não resolveram

As iterações anteriores focaram em:
1. Conflito STOMP vs nativo → corrigido (path `/ws-coletas-live`)
2. Anotação `@EnableWebSocket` faltando → corrigido
3. Erro de compilação `registerWebSocketHandlers` → corrigido
4. `TomcatWebSocketCustomizerConfig` registrando `WsSci` manualmente → **era um
   workaround que competia com a exclusão**. Mesmo registrando o `WsSci` na mão,
   a exclusão da auto-config criava um estado inconsistente.

Nenhuma delas atacou a **exclusão da auto-config**, que era o bloqueio real.

---

## ✅ CORREÇÃO APLICADA

### 1. Reativar a `WebSocketServletAutoConfiguration`

**Arquivos:**
- `sihcp-server/src/main/resources/application-mobile.properties`
- `src/main/resources/application-mobile.properties` (raiz, consistência)

**Mudança:** removida a linha que excluía `WebSocketServletAutoConfiguration`
da lista `spring.autoconfigure.exclude`. Mantidas as exclusões de JMX (essas
sim são seguras e não afetam WebSocket).

Agora o Spring Boot registra o `WsSci` automaticamente no boot, pelo caminho
oficial e testado do framework.

### 2. Remover o workaround manual

**Arquivo removido:** `TomcatWebSocketCustomizerConfig.java`

Com a auto-config reativada, esse customizer registraria o `WsSci` uma
**segunda vez**, criando dois `ServerContainer` — fonte potencial de
comportamento imprevisível. Removido para deixar apenas o caminho canônico.

---

## 🔍 VALIDAÇÃO

- ✅ Dependência `tomcat-embed-websocket:10.1.16` confirmada no classpath
  (transitiva de `spring-boot-starter-websocket`) — o `WsSci` existe.
- ✅ Compilação: BUILD SUCCESS (122 arquivos, 1 a menos = customizer removido).
- ✅ JAR de produção gerado: `SISTEMA_INVENTARIO_PRODUCAO/sihcp-server.jar`
  (97 MB, 29/05 13:44).

---

## 📋 Configuração de Conexão (referência)

- **Context-path do servidor:** `/inventario` (porta 8081)
- **Endpoint WebSocket:** `ws://10.14.150.14:8081/inventario/ws-coletas-live`
- **Com filtro de inventário:** `...?inventarioId=N`
- **Segurança:** `WebSocketSecurityConfig` (HIGHEST_PRECEDENCE) libera
  `/ws-coletas-live` sem autenticação; o app envia JWT no header preparando
  validação futura.

O app (`RealtimeColetasManager`) constrói a URL corretamente a partir da
`getServerUrl()` (que já inclui `/inventario`), trocando `http→ws`/`https→wss`
e anexando `/ws-coletas-live`.

---

## 🧪 COMO VALIDAR APÓS O DEPLOY

### 1. No log de startup do servidor, procurar:
```
✓ WebSocket nativo registrado: /ws-coletas-live (monitor ao vivo)
```
(do `WebSocketConfig.registerWebSocketHandlers`)

E **não** deve aparecer nenhum erro de `ServerContainer`.

### 2. No app:
1. Abrir "Monitor ao Vivo" / visualização de coletas em tempo real
2. Status deve mudar para **🟢 AO VIVO**
3. Pedir para outro usuário (ou outra sessão) registrar uma coleta
4. A coleta deve aparecer na lista automaticamente, sem refresh

### 3. No log do servidor ao conectar:
```
WS coletas-live conectado: session=..., inventarioId=..., totalNoBucket=1
```

### 4. No log ao registrar coleta (com cliente conectado):
Broadcast é feito por `MobileColetaService.publicarEventoWebSocket()`.

---

## 🚀 DEPLOY

1. Parar o servidor atual em 10.14.150.14
2. Substituir o JAR por `SISTEMA_INVENTARIO_PRODUCAO/sihcp-server.jar`
3. Iniciar com profile mobile:
   ```
   java -jar sihcp-server.jar --spring.profiles.active=mobile
   ```
4. Validar conforme seção acima

---

**Status:** ✅ Causa raiz identificada e corrigida
**Confiança:** Alta — a exclusão da auto-config explica perfeitamente o sintoma
(STOMP funciona, nativo falha no handshake) que persistia entre todas as
tentativas anteriores.
