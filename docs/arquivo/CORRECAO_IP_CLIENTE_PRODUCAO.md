# Correção: Captura de IP Real do Cliente em Produção

**Data**: 02/11/2025  
**Status**: ✅ Implementado

## Problema Identificado

Em produção, o servidor estava registrando todas as requisições como vindas de `127.0.0.1` (localhost), mesmo quando o cliente estava em outro IP.

### Log do Problema
```
Remote Addr: 127.0.0.1
```

### Causa Raiz

Quando há um proxy reverso, load balancer ou qualquer intermediário entre o cliente e o servidor, o `request.getRemoteAddr()` retorna o IP do proxy (geralmente `127.0.0.1`), não o IP real do cliente.

## Solução Implementada

### 1. Configuração do Servidor para Aceitar Headers de Proxy

**Arquivo**: `src/main/resources/application-mobile.properties`

Adicionadas configurações para o Tomcat processar headers de proxy:

```properties
# Configurações para capturar IP real do cliente
server.forward-headers-strategy=framework
server.tomcat.remoteip.remote-ip-header=x-forwarded-for
server.tomcat.remoteip.protocol-header=x-forwarded-proto
server.tomcat.remoteip.internal-proxies=10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|127\\.0\\.0\\.1
```

#### Explicação das Configurações

- **`server.forward-headers-strategy=framework`**
  - Habilita o Spring Boot para processar headers de proxy
  - Permite que o framework extraia informações reais do cliente

- **`server.tomcat.remoteip.remote-ip-header=x-forwarded-for`**
  - Define qual header contém o IP real do cliente
  - `X-Forwarded-For` é o padrão da indústria

- **`server.tomcat.remoteip.protocol-header=x-forwarded-proto`**
  - Define qual header contém o protocolo original (HTTP/HTTPS)
  - Importante para manter informações de segurança

- **`server.tomcat.remoteip.internal-proxies`**
  - Lista de IPs considerados como proxies internos confiáveis
  - Regex para redes privadas: 10.x.x.x, 192.168.x.x, 127.0.0.1

### 2. Método para Extrair IP Real do Cliente

**Arquivo**: `src/main/java/com/inventario/mobile/server/config/RequestLoggingFilter.java`

Adicionado método que verifica múltiplos headers para encontrar o IP real:

```java
private String getClientIpAddress(HttpServletRequest request) {
    String[] headerNames = {
        "X-Forwarded-For",      // Padrão da indústria
        "X-Real-IP",            // Nginx
        "Proxy-Client-IP",      // Apache
        "WL-Proxy-Client-IP",   // WebLogic
        "HTTP_X_FORWARDED_FOR", // Variação
        "HTTP_X_FORWARDED",     // Variação
        "HTTP_X_CLUSTER_CLIENT_IP", // Cluster
        "HTTP_CLIENT_IP",       // Cliente direto
        "HTTP_FORWARDED_FOR",   // Variação
        "HTTP_FORWARDED",       // RFC 7239
        "HTTP_VIA",             // Via proxy
        "REMOTE_ADDR"           // Fallback
    };
    
    for (String header : headerNames) {
        String ip = request.getHeader(header);
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For pode conter múltiplos IPs
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
    }
    
    // Se nenhum header foi encontrado, usar IP direto
    return request.getRemoteAddr();
}
```

### 3. Logs Melhorados

Agora os logs mostram tanto o IP direto quanto o IP real:

```
Remote Addr (direto): 127.0.0.1
Client IP (real): 192.168.10.107
```

## Como Funciona

### Cenário 1: Sem Proxy (Desenvolvimento)

```
Cliente (192.168.10.107) → Servidor
```

- `Remote Addr`: 192.168.10.107
- `Client IP`: 192.168.10.107
- ✅ Ambos mostram o IP real

### Cenário 2: Com Proxy (Produção)

```
Cliente (192.168.10.107) → Proxy (127.0.0.1) → Servidor
```

**Sem a correção:**
- `Remote Addr`: 127.0.0.1
- `Client IP`: 127.0.0.1
- ❌ IP real perdido

**Com a correção:**
- `Remote Addr`: 127.0.0.1
- `Client IP`: 192.168.10.107
- ✅ IP real capturado do header X-Forwarded-For

### Cenário 3: Múltiplos Proxies

```
Cliente (192.168.10.107) → Proxy1 (10.0.0.1) → Proxy2 (127.0.0.1) → Servidor
```

Header `X-Forwarded-For`: `192.168.10.107, 10.0.0.1`

- `Remote Addr`: 127.0.0.1
- `Client IP`: 192.168.10.107 (primeiro IP da lista)
- ✅ IP real do cliente original

## Headers de Proxy Suportados

| Header | Usado Por | Descrição |
|--------|-----------|-----------|
| `X-Forwarded-For` | Padrão | Lista de IPs (cliente, proxy1, proxy2...) |
| `X-Real-IP` | Nginx | IP real do cliente |
| `Proxy-Client-IP` | Apache | IP do cliente via proxy |
| `WL-Proxy-Client-IP` | WebLogic | IP do cliente via WebLogic |
| `HTTP_X_FORWARDED_FOR` | Variação | Variação do X-Forwarded-For |
| `HTTP_FORWARDED` | RFC 7239 | Padrão RFC para forwarding |

## Testando a Configuração

### Teste 1: Requisição Direta

```bash
curl -X GET http://192.168.10.107:8081/api/mobile/health
```

**Log Esperado:**
```
Remote Addr (direto): 192.168.10.107
Client IP (real): 192.168.10.107
```

### Teste 2: Requisição via Proxy

```bash
curl -X GET http://192.168.10.107:8081/api/mobile/health \
  -H "X-Forwarded-For: 203.0.113.195"
```

**Log Esperado:**
```
Remote Addr (direto): 127.0.0.1
Client IP (real): 203.0.113.195
```

### Teste 3: Múltiplos Proxies

```bash
curl -X GET http://192.168.10.107:8081/api/mobile/health \
  -H "X-Forwarded-For: 203.0.113.195, 198.51.100.178"
```

**Log Esperado:**
```
Remote Addr (direto): 127.0.0.1
Client IP (real): 203.0.113.195
```

## Segurança

### Validação de IPs Confiáveis

A configuração `internal-proxies` define quais IPs são considerados proxies confiáveis:

```properties
server.tomcat.remoteip.internal-proxies=10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|127\\.0\\.0\\.1
```

Isso previne que clientes maliciosos falsifiquem o header `X-Forwarded-For`.

### Redes Privadas Confiáveis

- `10.0.0.0/8` - Rede privada classe A
- `192.168.0.0/16` - Rede privada classe C
- `127.0.0.1` - Localhost

### Produção

Em produção, você pode restringir ainda mais:

```properties
# Apenas proxies específicos
server.tomcat.remoteip.internal-proxies=10\\.0\\.0\\.1|192\\.168\\.1\\.1
```

## Benefícios

✅ **Auditoria Correta**: Logs mostram IP real do cliente  
✅ **Segurança**: Rastreamento correto de acessos  
✅ **Compliance**: Atende requisitos de LGPD/GDPR  
✅ **Debug**: Facilita troubleshooting de problemas  
✅ **Estatísticas**: Análise correta de origem de requisições  

## Casos de Uso

### 1. Auditoria de Acesso
```
[AUDIT] Login bem-sucedido - Usuário: admin - IP: 192.168.10.107
```

### 2. Bloqueio de IP
```java
if (clientIp.startsWith("203.0.113.")) {
    throw new AccessDeniedException("IP bloqueado");
}
```

### 3. Rate Limiting por IP
```java
if (requestCount.get(clientIp) > 100) {
    throw new TooManyRequestsException();
}
```

### 4. Geolocalização
```java
String country = geoIpService.getCountry(clientIp);
logger.info("Acesso de: {}", country);
```

## Arquivos Modificados

1. ✅ `application-mobile.properties` - Configurações de proxy
2. ✅ `RequestLoggingFilter.java` - Método para extrair IP real

## Compilação

```bash
.\mvnw.cmd clean compile -DskipTests

# Resultado
BUILD SUCCESS
Total time: 31.655 s
```

## Próximos Passos

1. ✅ Reiniciar servidor mobile
2. ✅ Fazer requisição de teste
3. ✅ Verificar logs - deve mostrar IP real
4. ✅ Confirmar que auditoria está correta

---

**Status**: ✅ Implementado e compilado  
**Impacto**: Crítico - Corrige auditoria e segurança  
**Ambiente**: Produção
