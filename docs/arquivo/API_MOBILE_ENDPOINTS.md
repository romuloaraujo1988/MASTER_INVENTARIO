# API Mobile - Documentação dos Endpoints

## 📱 Visão Geral
Esta documentação descreve todos os endpoints disponíveis para a funcionalidade de monitoramento de usuários mobile do Sistema de Inventário.

## 🔐 Autenticação
Todos os endpoints requerem autenticação. Use Basic Auth ou JWT conforme configurado no sistema.

---

## 📡 Endpoints de Sincronização Mobile

### 1. Conectar Dispositivo Mobile
**Endpoint**: `POST /api/mobile/sync/connect`

**Descrição**: Estabelece uma nova conexão mobile e registra o dispositivo no sistema.

**Headers**:
```
Content-Type: application/json
Authorization: Basic <credentials> ou Bearer <token>
```

**Body**:
```json
{
    "username": "string",
    "deviceInfo": "string",
    "appVersion": "string"
}
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "message": "Conexão mobile estabelecida com sucesso",
    "sessionId": "uuid-string",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

**Resposta de Erro** (400/401/500):
```json
{
    "success": false,
    "message": "Descrição do erro",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

### 2. Enviar Heartbeat
**Endpoint**: `POST /api/mobile/sync/heartbeat`

**Descrição**: Atualiza o timestamp de última atividade para manter a conexão ativa.

**Headers**:
```
Content-Type: application/json
Authorization: Basic <credentials> ou Bearer <token>
```

**Body**:
```json
{
    "sessionId": "uuid-string"
}
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "message": "Heartbeat atualizado com sucesso",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

**Resposta de Erro** (404):
```json
{
    "success": false,
    "message": "Sessão não encontrada",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

### 3. Desconectar Dispositivo
**Endpoint**: `POST /api/mobile/sync/disconnect`

**Descrição**: Remove a conexão mobile do sistema.

**Headers**:
```
Content-Type: application/json
Authorization: Basic <credentials> ou Bearer <token>
```

**Body**:
```json
{
    "sessionId": "uuid-string"
}
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "message": "Dispositivo desconectado com sucesso",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

---

## 🔧 Endpoints de Gerenciamento de Conexões

### 1. Registrar Nova Conexão
**Endpoint**: `POST /api/mobile/connections/register`

**Descrição**: Registra uma nova conexão mobile com informações detalhadas.

**Headers**:
```
Content-Type: application/json
Authorization: Basic <credentials> ou Bearer <token>
```

**Body**:
```json
{
    "username": "string",
    "deviceInfo": "string",
    "appVersion": "string"
}
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "sessionId": "uuid-string",
    "connection": {
        "sessionId": "uuid-string",
        "ipAddress": "192.168.1.100",
        "hostname": "hostname",
        "username": "usuario",
        "deviceInfo": "Android 12",
        "appVersion": "1.0.0",
        "connectTime": "2024-01-01T10:00:00Z",
        "lastHeartbeat": "2024-01-01T10:00:00Z"
    }
}
```

### 2. Listar Conexões Ativas
**Endpoint**: `GET /api/mobile/connections`

**Descrição**: Retorna lista de todas as conexões mobile ativas.

**Headers**:
```
Authorization: Basic <credentials> ou Bearer <token>
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "connections": [
        {
            "sessionId": "uuid-string",
            "ipAddress": "192.168.1.100",
            "hostname": "hostname",
            "username": "usuario1",
            "deviceInfo": "Android 12",
            "appVersion": "1.0.0",
            "connectTime": "2024-01-01T10:00:00Z",
            "lastHeartbeat": "2024-01-01T10:05:00Z"
        },
        {
            "sessionId": "uuid-string-2",
            "ipAddress": "192.168.1.101",
            "hostname": "hostname2",
            "username": "usuario2",
            "deviceInfo": "iOS 16",
            "appVersion": "1.0.0",
            "connectTime": "2024-01-01T10:02:00Z",
            "lastHeartbeat": "2024-01-01T10:04:00Z"
        }
    ],
    "totalConnections": 2
}
```

### 3. Obter Estatísticas de Conexões
**Endpoint**: `GET /api/mobile/connections/stats`

**Descrição**: Retorna estatísticas detalhadas sobre as conexões mobile.

**Headers**:
```
Authorization: Basic <credentials> ou Bearer <token>
```

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "stats": {
        "totalConnections": 15,
        "activeConnections": 3,
        "uniqueUsers": 8,
        "averageConnectionTime": "00:45:30",
        "connectionsToday": 5,
        "peakConnections": 7,
        "deviceTypes": {
            "Android": 8,
            "iOS": 7
        },
        "topUsers": [
            {
                "username": "usuario1",
                "connectionCount": 5,
                "totalTime": "02:30:00"
            }
        ]
    }
}
```

### 4. Remover Conexão
**Endpoint**: `DELETE /api/mobile/connections/{sessionId}`

**Descrição**: Remove uma conexão específica do sistema.

**Headers**:
```
Authorization: Basic <credentials> ou Bearer <token>
```

**Parâmetros**:
- `sessionId` (path): ID da sessão a ser removida

**Resposta de Sucesso** (200):
```json
{
    "success": true,
    "message": "Conexão removida com sucesso"
}
```

**Resposta de Erro** (404):
```json
{
    "success": false,
    "message": "Conexão não encontrada"
}
```

---

## 📊 Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| 200 | Sucesso |
| 400 | Requisição inválida |
| 401 | Não autorizado |
| 403 | Acesso negado |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |

---

## 🔄 Fluxo Típico de Uso

### 1. Conectar Dispositivo
```
POST /api/mobile/sync/connect
→ Recebe sessionId
```

### 2. Manter Conexão Ativa
```
POST /api/mobile/sync/heartbeat (a cada 2-3 minutos)
→ Atualiza timestamp
```

### 3. Monitorar Conexões (Admin)
```
GET /api/mobile/connections
→ Lista dispositivos conectados

GET /api/mobile/connections/stats
→ Visualiza estatísticas
```

### 4. Desconectar
```
POST /api/mobile/sync/disconnect
→ Remove conexão
```

---

## ⚙️ Configurações Importantes

### Timeouts
- **Heartbeat**: Recomendado a cada 2-3 minutos
- **Expiração**: Conexões expiram após 5 minutos sem heartbeat
- **Limpeza**: Executada automaticamente a cada 2 minutos

### Limites
- Não há limite de conexões simultâneas por usuário
- Sessões são únicas por dispositivo
- Limpeza automática previne acúmulo de conexões órfãs

### Segurança
- Todos os endpoints requerem autenticação
- Logs detalhados de todas as operações
- Validação de entrada em todos os endpoints

---

## 🧪 Exemplos de Teste

### Usando cURL

**Conectar**:
```bash
curl -X POST http://localhost:8080/api/mobile/sync/connect \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dXNlcjpwYXNz" \
  -d '{"username":"testuser","deviceInfo":"Android 12","appVersion":"1.0.0"}'
```

**Heartbeat**:
```bash
curl -X POST http://localhost:8080/api/mobile/sync/heartbeat \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dXNlcjpwYXNz" \
  -d '{"sessionId":"uuid-aqui"}'
```

**Listar Conexões**:
```bash
curl -X GET http://localhost:8080/api/mobile/connections \
  -H "Authorization: Basic dXNlcjpwYXNz"
```

### Usando PowerShell
Utilize o script `test_mobile_endpoints.ps1` incluído no projeto para testes automatizados.

---

## 📝 Notas de Implementação

- Todos os endpoints retornam JSON
- Timestamps estão em formato ISO 8601 UTC
- SessionIds são UUIDs únicos
- IPs e hostnames são capturados automaticamente
- Logs detalhados estão disponíveis no console da aplicação