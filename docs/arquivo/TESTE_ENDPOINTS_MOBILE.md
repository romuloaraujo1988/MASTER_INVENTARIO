# Teste de Endpoints da API Mobile

## Configuração Inicial

**URL Base:** `http://[SEU_IP]:8081/inventario`

Substitua `[SEU_IP]` pelo IP mostrado quando o servidor iniciar (ex: 192.168.1.100)

---

## 1. Teste de Conectividade Básica

### 1.1 Ping (Mais Simples)
```bash
curl http://[SEU_IP]:8081/inventario/api/mobile/ping
```

**Resposta Esperada:**
```json
{
  "success": true,
  "message": "Servidor respondendo",
  "data": "pong",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.2 Health Check
```bash
curl http://[SEU_IP]:8081/inventario/api/mobile/health
```

**Resposta Esperada:**
```json
{
  "success": true,
  "message": "Servidor mobile operacional",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-15T10:30:00",
    "application": "Sistema de Inventário",
    "version": "1.0.0",
    "mobile-api": "ACTIVE"
  }
}
```

### 1.3 Informações do Servidor
```bash
curl http://[SEU_IP]:8081/inventario/api/mobile/info
```

---

## 2. Teste de Autenticação

### 2.1 Login
```bash
curl -X POST http://[SEU_IP]:8081/inventario/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**Resposta Esperada:**
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nomeCompleto": "Administrador",
      "perfil": "ADMIN",
      "email": "admin@example.com"
    }
  }
}
```

**IMPORTANTE:** Guarde o token retornado para usar nos próximos testes!

---

## 3. Teste de Endpoints Protegidos

### 3.1 Sincronização de Dados
```bash
curl -X POST http://[SEU_IP]:8081/inventario/api/mobile/sync/data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer [SEU_TOKEN]" \
  -d '{
    "idInventario": 1,
    "lastSyncTimestamp": null
  }'
```

### 3.2 Buscar Patrimônio por Código
```bash
curl http://[SEU_IP]:8081/inventario/api/mobile/sync/patrimonio/123456 \
  -H "Authorization: Bearer [SEU_TOKEN]"
```

### 3.3 Registrar Coleta
```bash
curl -X POST http://[SEU_IP]:8081/inventario/api/mobile/coleta \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer [SEU_TOKEN]" \
  -d '{
    "idInventario": 1,
    "numeroPatrimonio": "123456",
    "dataColeta": "2024-01-15T10:30:00",
    "localizacaoEncontrada": "Sala 101",
    "estadoEncontrado": "BOM",
    "observacaoColeta": "Item em bom estado",
    "latitude": -15.123456,
    "longitude": -56.123456
  }'
```

---

## 4. Teste no Navegador do Smartphone

### 4.1 Teste Básico
Abra o navegador do smartphone e acesse:
```
http://[SEU_IP]:8081/inventario/api/mobile/ping
```

Deve mostrar a resposta JSON com "pong".

### 4.2 Teste de Health
```
http://[SEU_IP]:8081/inventario/api/mobile/health
```

Deve mostrar status "UP".

### 4.3 Swagger UI (Documentação Interativa)
```
http://[SEU_IP]:8081/inventario/swagger-ui.html
```

---

## 5. Teste no Aplicativo Mobile

### 5.1 Configuração no App
1. Abra o aplicativo mobile
2. Vá em Configurações
3. Configure o servidor como: `http://[SEU_IP]:8081/inventario`
4. Teste a conexão

### 5.2 Login no App
1. Use as credenciais:
   - Usuário: `admin`
   - Senha: `admin`
2. O app deve retornar sucesso e armazenar o token

### 5.3 Sincronização
1. Após login, tente sincronizar dados
2. O app deve baixar inventários, salas e patrimônios

---

## 6. Troubleshooting

### Erro: "Connection refused" ou "Timeout"

**Possíveis causas:**
1. Servidor não está rodando
2. Firewall bloqueando porta 8081
3. Smartphone em rede diferente do PC
4. IP incorreto

**Soluções:**
```bash
# Verificar se o servidor está rodando
netstat -ano | findstr :8081

# Liberar porta no firewall (PowerShell como Admin)
netsh advfirewall firewall add rule name="Servidor Mobile Inventário" dir=in action=allow protocol=TCP localport=8081

# Verificar seu IP
ipconfig
```

### Erro: "401 Unauthorized"

**Causa:** Token inválido ou expirado

**Solução:** Faça login novamente para obter um novo token

### Erro: "404 Not Found"

**Causa:** Endpoint incorreto ou context-path errado

**Solução:** Verifique se está usando `/inventario` no caminho

---

## 7. Endpoints Disponíveis

### Autenticação (Público)
- `POST /api/mobile/auth/login` - Login
- `POST /api/mobile/auth/validate` - Validar token
- `POST /api/mobile/auth/logout` - Logout

### Health (Público)
- `GET /api/mobile/ping` - Ping simples
- `GET /api/mobile/health` - Health check
- `GET /api/mobile/info` - Informações do servidor

### Sincronização (Requer autenticação)
- `POST /api/mobile/sync/data` - Sincronizar dados
- `GET /api/mobile/sync/patrimonios/setor/{id}` - Patrimônios por setor
- `GET /api/mobile/sync/patrimonios/sala/{id}` - Patrimônios por sala
- `GET /api/mobile/sync/patrimonio/{codigo}` - Buscar patrimônio

### Coleta (Requer autenticação)
- `POST /api/mobile/coleta` - Registrar coleta
- `POST /api/mobile/coleta/batch` - Registrar múltiplas coletas
- `GET /api/mobile/coleta/pendentes` - Buscar coletas pendentes
- `GET /api/mobile/coleta/historico` - Histórico de coletas

---

## 8. Teste Rápido com PowerShell

```powershell
# Definir IP
$IP = "192.168.1.100"  # Substitua pelo seu IP
$BASE_URL = "http://${IP}:8081/inventario"

# Teste 1: Ping
Write-Host "Teste 1: Ping" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/api/mobile/ping" -Method Get

# Teste 2: Health
Write-Host "`nTeste 2: Health Check" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/api/mobile/health" -Method Get

# Teste 3: Login
Write-Host "`nTeste 3: Login" -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/mobile/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token

Write-Host "Token obtido: $($token.Substring(0, 50))..." -ForegroundColor Green

# Teste 4: Endpoint protegido
Write-Host "`nTeste 4: Sincronização (com token)" -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}
$syncBody = @{
    idInventario = 1
    lastSyncTimestamp = $null
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BASE_URL/api/mobile/sync/data" -Method Post -Headers $headers -Body $syncBody -ContentType "application/json"

Write-Host "`n✓ Todos os testes passaram!" -ForegroundColor Green
```

---

## 9. Checklist de Verificação

- [ ] Servidor iniciou na porta 8081
- [ ] IP local foi detectado corretamente
- [ ] Firewall permite conexões na porta 8081
- [ ] Smartphone está na mesma rede Wi-Fi
- [ ] Endpoint `/api/mobile/ping` responde
- [ ] Endpoint `/api/mobile/health` retorna status UP
- [ ] Login retorna token válido
- [ ] Endpoints protegidos funcionam com token
- [ ] Aplicativo mobile consegue se conectar
- [ ] Sincronização de dados funciona

---

## 10. Contato e Suporte

Se todos os testes passarem, o aplicativo mobile está pronto para uso!

**Versão da API:** 1.0.0  
**Última atualização:** 2024
