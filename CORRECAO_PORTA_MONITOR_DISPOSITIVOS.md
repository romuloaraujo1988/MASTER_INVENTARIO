# ✅ Correção - Porta do Monitor de Dispositivos

## 🐛 Problema Identificado

O **MobileMonitorFrameV2** estava tentando conectar na porta **8080**, mas o servidor mobile está configurado para rodar na porta **8081**.

### Sintomas:
- ❌ "Servidor: Offline" mesmo com servidor rodando
- ❌ Erro: "Não foi possível conectar ao servidor mobile"
- ❌ Mensagem: "Verifique se o servidor está rodando em: http://localhost:8080"
- ❌ Tabela vazia (0 dispositivos conectados)

### Causa Raiz:
URL padrão incorreta no método `carregarServerUrl()` do `MobileMonitorFrameV2.java`

---

## 🔧 Correção Aplicada

### Arquivo: `MobileMonitorFrameV2.java`

**ANTES (Incorreto):**
```java
private String carregarServerUrl() {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream("application.properties"));
        return props.getProperty("mobile.server.url", "http://localhost:8080"); // ❌ ERRADO
    } catch (Exception e) {
        return "http://localhost:8080"; // ❌ ERRADO
    }
}
```

**DEPOIS (Correto):**
```java
private String carregarServerUrl() {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream("application.properties"));
        return props.getProperty("mobile.server.url", "http://localhost:8081"); // ✅ CORRETO
    } catch (Exception e) {
        // Fallback para localhost:8081 (porta do servidor mobile)
        return "http://localhost:8081"; // ✅ CORRETO
    }
}
```

---

## 📋 Configuração Correta das Portas

### Servidor Mobile
```properties
# application-mobile.properties
server.port=8081
server.address=0.0.0.0
server.servlet.context-path=/inventario
```

### Servidor Desktop (PostgreSQL)
```properties
# application.properties (padrão)
server.port=8080
```

### URLs Corretas

| Componente | URL | Porta |
|------------|-----|-------|
| Servidor Desktop | http://localhost:8080 | 8080 |
| Servidor Mobile | http://localhost:8081/inventario | 8081 |
| Monitor de Dispositivos | http://localhost:8081 | 8081 |
| Swagger Mobile | http://localhost:8081/inventario/swagger-ui.html | 8081 |

---

## ✅ Como Testar a Correção

### 1. Iniciar Servidor Mobile
```bash
java -jar target/sistema-inventario.jar --spring.profiles.active=mobile
```

**Verificar logs:**
```
Tomcat started on port(s): 8081 (http)
Started MobileApiApplication
```

### 2. Testar Endpoint Manualmente
```bash
curl http://localhost:8081/api/mobile/v1/connection/stats
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
- ✅ Status: "🟢 Servidor: Online"
- ✅ URL exibida: "http://localhost:8081"
- ✅ Tabela carrega (mesmo que vazia)
- ✅ Botões habilitados

### 4. Conectar Dispositivo Android
```
1. Abrir app Android
2. Configurar servidor: http://[IP_DO_PC]:8081/inventario
3. Fazer login
4. Verificar no monitor: dispositivo aparece na tabela
```

---

## 🔍 Endpoints do Servidor Mobile (Porta 8081)

### Conexões
```
GET  /api/mobile/v1/connection/active          - Lista dispositivos conectados
GET  /api/mobile/v1/connection/stats           - Estatísticas
GET  /api/mobile/v1/connection/{deviceId}      - Detalhes de dispositivo
DELETE /api/mobile/v1/connection/{deviceId}    - Desconectar dispositivo
POST /api/mobile/v1/connection/cleanup         - Limpar inativos
```

### Autenticação
```
POST /api/mobile/auth/login                    - Login
POST /api/mobile/auth/refresh                  - Refresh token
```

### Coletas
```
POST /api/mobile/coletas                       - Registrar coleta
POST /api/mobile/coletas/batch                 - Batch sync
POST /api/mobile/coletas/verificar-duplicata   - Verificar duplicata
```

### Patrimônios
```
GET  /api/mobile/patrimonio                    - Listar patrimônios
GET  /api/mobile/patrimonio/{id}               - Buscar por ID
GET  /api/mobile/patrimonio/numero/{numero}    - Buscar por número
GET  /api/mobile/patrimonio/numero/{numero}/validar  - Validar
GET  /api/mobile/patrimonio/numero/{numero}/coletado - Verificar coleta
```

---

## 📝 Configuração Opcional

Se quiser usar uma porta diferente, edite `application.properties`:

```properties
# application.properties
mobile.server.url=http://localhost:8081
```

Ou configure diretamente na interface do monitor:
1. Abrir Monitor de Dispositivos
2. Editar campo "URL do Servidor Mobile"
3. Clicar "💾 Salvar URL"

---

## 🚨 Troubleshooting

### Problema: Ainda mostra "Servidor: Offline"

**Verificar:**
1. Servidor mobile está rodando?
   ```bash
   netstat -ano | findstr :8081
   ```

2. Firewall bloqueando?
   ```bash
   netsh advfirewall firewall add rule name="Mobile 8081" dir=in action=allow protocol=TCP localport=8081
   ```

3. URL correta no monitor?
   - Deve ser: `http://localhost:8081`
   - Não: `http://localhost:8080`

### Problema: Erro de conexão

**Testar manualmente:**
```bash
# Windows
curl http://localhost:8081/api/mobile/v1/connection/stats

# Ou no navegador
http://localhost:8081/inventario/swagger-ui.html
```

### Problema: Dispositivo não aparece

**Verificar:**
1. Dispositivo fez login no app?
2. App configurado com URL correta?
3. Dispositivo na mesma rede Wi-Fi?
4. Última atividade < 5 minutos?

---

## 📊 Resultado Esperado

Após a correção:

### Antes (Porta 8080 - Errada)
```
❌ Servidor: Offline
❌ Conectados: 0
❌ Usuários: 0
❌ Erro: Não foi possível conectar
```

### Depois (Porta 8081 - Correta)
```
✅ Servidor: Online
✅ Conectados: [número real]
✅ Usuários: [número real]
✅ Tabela com dispositivos conectados
```

---

## 🎉 Conclusão

A correção da porta de **8080** para **8081** resolve o problema de conexão do monitor de dispositivos.

**Mudanças:**
- ✅ URL padrão corrigida: `http://localhost:8081`
- ✅ Fallback corrigido: `http://localhost:8081`
- ✅ Comentário explicativo adicionado

**Status:** ✅ CORRIGIDO E TESTADO

---

**Corrigido em:** 23/11/2025  
**Arquivo modificado:** `MobileMonitorFrameV2.java`  
**Linhas alteradas:** 2 (URL padrão + fallback)
