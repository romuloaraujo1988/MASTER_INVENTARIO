# Correção do WebSocket - Monitor ao Vivo

**Data:** 29/05/2026 11:58  
**Versão:** 2.9.0  
**Build:** sihcp-server.jar (97 MB)

---

## 🐛 Problema Identificado

O servidor não compilava devido a erro no método `registerWebSocketHandlers()`:

```
The method registerWebSocketHandlers(WebSocketHandlerRegistry) of type 
WebSocketConfig must override or implement a supertype method
```

### Causa Raiz

O método `registerWebSocketHandlers()` pertence à interface `WebSocketConfigurer`, mas a classe `WebSocketConfig` estava implementando apenas `WebSocketMessageBrokerConfigurer`.

---

## ✅ Solução Aplicada

### 1. Implementação de Ambas as Interfaces

**Arquivo:** `sihcp-server/src/main/java/com/inventario/sihcp/config/WebSocketConfig.java`

**Mudança:**
```java
// ANTES (ERRADO)
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // ...
}

// DEPOIS (CORRETO)
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
    // ...
}
```

### 2. Adição do Logger

Adicionado logger estático para logs consistentes:

```java
private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
```

### 3. Uso do Logger no Método

Substituído `org.slf4j.LoggerFactory.getLogger()` inline pelo logger da classe:

```java
@Override
public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    if (coletaLiveSocketHandler != null) {
        registry.addHandler(coletaLiveSocketHandler, "/ws-coletas-live")
                .setAllowedOriginPatterns("*");
        logger.info("✓ WebSocket nativo registrado: /ws-coletas-live (monitor ao vivo)");
    } else {
        logger.warn("⚠ ColetaLiveSocketHandler nulo — endpoint /ws-coletas-live NÃO registrado");
    }
}
```

---

## 📦 Arquivos Modificados

1. `sihcp-server/src/main/java/com/inventario/sihcp/config/WebSocketConfig.java`
   - Adicionado `@EnableWebSocket`
   - Implementado `WebSocketConfigurer`
   - Adicionado logger estático
   - Corrigido uso do logger

---

## 🚀 Build de Produção

### Compilação
```bash
.\mvnw.cmd clean package -DskipTests -P fat-jar
```

**Resultado:**
- ✅ BUILD SUCCESS
- ✅ Tempo: 25.666s
- ✅ JAR gerado: `sihcp-server/target/sihcp-server.jar`

### Arquivo de Produção
- **Localização:** `SISTEMA_INVENTARIO_PRODUCAO/sihcp-server.jar`
- **Tamanho:** 97 MB
- **Data:** 29/05/2026 11:58:16

---

## 🔍 Verificação

### Logs Esperados no Startup

Ao iniciar o servidor, você deve ver:

```
✓ WsSci do Tomcat registrado — ServerContainer JSR-356 será inicializado no boot
✓ WebSocket nativo registrado: /ws-coletas-live (monitor ao vivo)
```

### Teste de Conexão

**Endpoint:** `ws://10.14.150.14:8081/ws-coletas-live`

**No app Android:**
- Abrir "Monitor ao Vivo"
- Status deve mostrar: **🟢 AO VIVO**
- Coletas devem aparecer em tempo real

---

## 📝 Notas Técnicas

### Por que Duas Anotações?

A classe precisa de **ambas** as anotações porque:

1. **`@EnableWebSocketMessageBroker`** - Ativa STOMP para dashboard web
   - Processa `WebSocketMessageBrokerConfigurer`
   - Métodos: `configureMessageBroker()`, `registerStompEndpoints()`

2. **`@EnableWebSocket`** - Ativa WebSocket nativo para app Android
   - Processa `WebSocketConfigurer`
   - Método: `registerWebSocketHandlers()`
   - **SEM esta anotação, o método NUNCA é chamado!**

### Coexistência

O `ServerContainer` JSR-356 é criado pela auto-configuração do Spring Boot e resolvido no momento do handshake (não no startup), então as duas anotações coexistem sem conflito em fat JAR.

---

## ✅ Checklist de Deploy

- [x] Código corrigido
- [x] Compilação bem-sucedida
- [x] JAR gerado (97 MB)
- [x] JAR copiado para produção
- [ ] Substituir JAR no servidor (10.14.150.14)
- [ ] Reiniciar servidor
- [ ] Verificar logs de startup
- [ ] Testar conexão do app
- [ ] Validar monitor ao vivo

---

## 🎯 Próximos Passos

1. **Substituir o servidor:**
   ```bash
   # No servidor 10.14.150.14
   systemctl stop sihcp-server
   cp /caminho/novo/sihcp-server.jar /opt/sihcp/
   systemctl start sihcp-server
   ```

2. **Verificar logs:**
   ```bash
   tail -f /opt/sihcp/logs/server.log
   ```

3. **Testar no app:**
   - Abrir "Monitor ao Vivo"
   - Verificar status "🟢 AO VIVO"
   - Fazer uma coleta de teste
   - Verificar se aparece no monitor

---

**Status:** ✅ Correção aplicada e servidor compilado  
**Pronto para deploy:** Sim  
**Versão anterior:** 2.8.x  
**Versão nova:** 2.9.0
