# ✅ Correção - Context Path do Monitor de Dispositivos

## 🐛 Problema Identificado

**Erro HTTP 404** ao tentar buscar dispositivos conectados.

### Mensagem de Erro:
```
Erro ao buscar dispositivos. Erro HTTP 404: <!doctype html><html lang="en">
<head><title>HTTP Status 404 – Not Found</title>...
```

### Causa Raiz:
A URL estava **incompleta**, faltando o **context path `/inventario`**.

---

## 🔍 Análise do Problema

### Configuração do Servidor Mobile

O servidor mobile está configurado com:
```properties
server.port=8081
server.servlet.context-path=/inventario
```

Isso significa que **todos os endpoints** devem incluir `/inventario` no caminho.

### URLs Corretas vs Incorretas

| Endpoint | URL Incorreta (404) | URL Correta (200) |
|----------|---------------------|-------------------|
| Connection Active | `http://localhost:8081/api/mobile/v1/connection/active` ❌ | `http://localhost:8081/inventario/api/mobile/v1/connection/active` ✅ |
| Stats | `http://localhost:8081/api/mobile/v1/connection/stats` ❌ | `http://localhost:8081/inventario/api/mobile/v1/connection/stats` ✅ |
| Login | `http://localhost:8081/api/mobile/auth/login` ❌ | `http://localhost:8081/inventario/api/mobile/auth/login` ✅ |

---

## 🔧 Correção Aplicada

### Arquivo: `MobileMonitorFrameV2.java`

**ANTES (Incorreto - HTTP 404):**
```java
private String carregarServerUrl() {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream("application.properties"));
        return props.getProperty("mobile.server.url", "http://localhost:8081"); // ❌ Falta /inventario
    } catch (Exception e) {
        return "http://localhost:8081"; // ❌ Falta /inventario
    }
}
```

**DEPOIS (Correto - HTTP 200):**
```java
private String carregarServerUrl() {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream("application.properties"));
        return props.getProperty("mobile.server.url", "http://localhost:8081/inventario"); // ✅ Com /inventario
    } catch (Exception e) {
        // Fallback para localhost:8081/inventario (porta e context path do servidor mobile)
        return "http://localhost:8081/inventario"; // ✅ Com /inventario
    }
}
```

---

## 📋 Estrutura de URLs Completa

### Base URL
```
http://localhost:8081/inventario
```

### Endpoints de Conexão
```
GET  /api/mobile/v1/connection/active
     → http://localhost:8081/inventario/api/mobile/v1/connection/active

GET  /api/mobile/v1/connection/stats
     → http://localhost:8081/inventario/api/mobile/v1/connection/stats

GET  /api/mobile/v1/connection/{deviceId}
     → http://localhost:8081/inventario/api/mobile/v1/connection/{deviceId}

DELETE /api/mobile/v1/connection/{deviceId}
       → http://localhost:8081/inventario/api/mobile/v1/connection/{deviceId}

POST /api/mobile/v1/connection/cleanup
     → http://localhost:8081/inventario/api/mobile/v1/connection/cleanup
```

### Outros Endpoints
```
POST /api/mobile/auth/login
     → http://localhost:8081/inventario/api/mobile/auth/login

POST /api/mobile/coletas
     → http://localhost:8081/inventario/api/mobile/coletas

GET  /api/mobile/patrimonio
     → http://localhost:8081/inventario/api/mobile/patrimonio
```

---

## ✅ Como Testar a Correção

### 1. Recompilar
```bash
mvn clean compile
```

### 2. Testar Endpoint Manualmente
```bash
# Teste com curl (deve retornar 200 OK)
curl http://localhost:8081/inventario/api/mobile/v1/connection/stats
```

**Resposta esperada:**
```json
{
  "success": true,
  "stats": {
    "totalConnections": 0,
    "uniqueUsers": 0
  }
}
```

### 3. Abrir Monitor de Dispositivos
```
Sistema → Monitor de Usuários Mobile
```

**Verificar:**
- ✅ URL exibida: `http://localhost:8081/inventario`
- ✅ Status: "🟢 Servidor: Online"
- ✅ Conectados: 0 (ou número real)
- ✅ Usuários: 0 (ou número real)
- ✅ Tabela carrega (mesmo que vazia)
- ✅ Nenhum erro exibido

### 4. Testar Auto Refresh
```
1. Clicar em "▶️ Auto Refresh (OFF)"
2. Verificar: Muda para "⏸️ Auto Refresh (ON)"
3. Aguardar 10 segundos
4. Verificar: Tabela atualiza automaticamente
5. Verificar: Nenhum erro no console
```

---

## 🔍 Verificação de Endpoints

### Swagger UI
Acesse para ver todos os endpoints disponíveis:
```
http://localhost:8081/inventario/swagger-ui.html
```

### Health Check
```bash
curl http://localhost:8081/inventario/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

---

## 📝 Configuração no application.properties

Se quiser configurar uma URL diferente, adicione em `application.properties`:

```properties
# URL completa com context path
mobile.server.url=http://localhost:8081/inventario

# Ou para servidor remoto
mobile.server.url=http://192.168.1.100:8081/inventario
```

---

## 🚨 Troubleshooting

### Problema: Ainda recebe HTTP 404

**Verificar:**

1. **Servidor está rodando com context path correto?**
   ```bash
   # Verificar logs do servidor
   # Deve mostrar: "Tomcat started on port(s): 8081 (http) with context path '/inventario'"
   ```

2. **URL no monitor está correta?**
   - Deve ser: `http://localhost:8081/inventario`
   - Não: `http://localhost:8081`

3. **Testar endpoint manualmente:**
   ```bash
   curl http://localhost:8081/inventario/api/mobile/v1/connection/stats
   ```

### Problema: Erro de conexão

**Verificar:**

1. **Servidor está rodando?**
   ```bash
   netstat -ano | findstr :8081
   ```

2. **Firewall bloqueando?**
   ```bash
   netsh advfirewall firewall add rule name="Mobile 8081" dir=in action=allow protocol=TCP localport=8081
   ```

### Problema: Context path diferente

Se o servidor estiver configurado com context path diferente:

1. **Verificar application-mobile.properties:**
   ```properties
   server.servlet.context-path=/seu-context-path
   ```

2. **Atualizar URL no monitor:**
   ```
   http://localhost:8081/seu-context-path
   ```

---

## 📊 Comparação: Antes vs Depois

### Antes (HTTP 404)
```
Base URL: http://localhost:8081
Endpoint: /api/mobile/v1/connection/active
URL Final: http://localhost:8081/api/mobile/v1/connection/active
Resultado: ❌ HTTP 404 - Not Found
```

### Depois (HTTP 200)
```
Base URL: http://localhost:8081/inventario
Endpoint: /api/mobile/v1/connection/active
URL Final: http://localhost:8081/inventario/api/mobile/v1/connection/active
Resultado: ✅ HTTP 200 - OK
```

---

## 🎯 Resultado Esperado

Após a correção:

### Interface do Monitor
```
┌─────────────────────────────────────────────────────────┐
│ URL do Servidor Mobile: [http://localhost:8081/inventario] │
│ 🟢 Servidor: Online                                      │
├─────────────────────────────────────────────────────────┤
│ Conectados: 0    Usuários: 0    Última Atualização: ... │
├─────────────────────────────────────────────────────────┤
│ [Tabela vazia ou com dispositivos]                      │
├─────────────────────────────────────────────────────────┤
│ [Botões habilitados]                                     │
└─────────────────────────────────────────────────────────┘
```

### Console (Sem Erros)
```
INFO: Buscando dispositivos conectados: http://localhost:8081/inventario/api/mobile/v1/connection/active
INFO: Encontrados 0 dispositivos conectados
```

---

## 🎉 Conclusão

A correção adiciona o **context path `/inventario`** à URL base, resolvendo o erro HTTP 404.

**Mudanças:**
- ✅ URL padrão: `http://localhost:8081` → `http://localhost:8081/inventario`
- ✅ Fallback: `http://localhost:8081` → `http://localhost:8081/inventario`
- ✅ Comentário atualizado

**Status:** ✅ CORRIGIDO

---

**Corrigido em:** 23/11/2025  
**Arquivo:** `MobileMonitorFrameV2.java`  
**Problema:** HTTP 404 - Context path faltando  
**Solução:** Adicionar `/inventario` à URL base
