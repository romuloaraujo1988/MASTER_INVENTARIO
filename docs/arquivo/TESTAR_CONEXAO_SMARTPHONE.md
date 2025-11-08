# Guia de Teste de Conexão do Smartphone

## Problema Atual
O smartphone não está conseguindo se conectar ao servidor. Os logs mostram apenas acessos ao `/actuator/health`, mas nenhuma tentativa de login.

## Novos Recursos Adicionados

### 1. Filtro de Log de Requisições
Agora TODAS as requisições HTTP são logadas com detalhes completos:
- Método HTTP
- URI
- IP de origem
- Headers
- Content-Type

### 2. Endpoints de Teste
Criados endpoints específicos para testar a conectividade:

#### `/api/mobile/test/ping` (GET)
Endpoint mais simples possível - não requer autenticação
```
GET http://[IP_SERVIDOR]:8081/inventario/api/mobile/test/ping
```

#### `/api/mobile/test/echo` (POST)
Testa envio de dados JSON
```
POST http://[IP_SERVIDOR]:8081/inventario/api/mobile/test/echo
Content-Type: application/json

{
  "teste": "dados do smartphone"
}
```

#### `/api/mobile/test/login-info` (GET)
Retorna informações sobre como fazer login
```
GET http://[IP_SERVIDOR]:8081/inventario/api/mobile/test/login-info
```

## Passo a Passo para Testar

### Passo 1: Descobrir o IP do Servidor
```cmd
ipconfig
```
Anote o IPv4 (exemplo: `10.14.250.238`)

### Passo 2: Verificar se o Servidor Está Rodando
```cmd
netstat -ano | findstr :8081
```
Deve mostrar algo como:
```
TCP    0.0.0.0:8081    0.0.0.0:0    LISTENING    12345
```

### Passo 3: Testar do Próprio Servidor
Abra o navegador e acesse:
```
http://localhost:8081/inventario/api/mobile/test/ping
```

Deve retornar:
```json
{
  "success": true,
  "data": {
    "message": "Pong! Servidor está funcionando",
    "timestamp": "2025-10-22T21:40:00",
    "server": "Sistema de Inventário - API Mobile",
    "version": "1.2.0"
  },
  "message": "Conexão estabelecida com sucesso"
}
```

### Passo 4: Testar do Smartphone

#### Opção A: Usar o Navegador do Smartphone
1. Conecte o smartphone na mesma rede Wi-Fi
2. Abra o navegador
3. Acesse: `http://[IP_SERVIDOR]:8081/inventario/api/mobile/test/ping`
4. Deve mostrar o JSON de resposta

#### Opção B: Usar o Aplicativo
Configure a URL base no app:
```
http://[IP_SERVIDOR]:8081/inventario
```

### Passo 5: Verificar os Logs do Servidor

Quando o smartphone fizer uma requisição, você verá nos logs:

```
╔════════════════════════════════════════════════════════════════
║ REQUISIÇÃO RECEBIDA
╠════════════════════════════════════════════════════════════════
║ Método: GET
║ URI: /inventario/api/mobile/test/ping
║ Remote Address: 10.14.250.XXX
║ Content-Type: application/json
╠════════════════════════════════════════════════════════════════
║ HEADERS:
║   Host: 10.14.250.238:8081
║   User-Agent: ...
╚════════════════════════════════════════════════════════════════
╔════════════════════════════════════════════════════════════════
║ PING RECEBIDO DO SMARTPHONE!
╚════════════════════════════════════════════════════════════════
```

## Diagnóstico de Problemas

### Se NÃO aparecer nada nos logs:
❌ **Problema**: O smartphone não está conseguindo se conectar ao servidor

**Possíveis causas:**
1. IP incorreto no aplicativo
2. Smartphone em rede diferente
3. Firewall bloqueando a porta 8081
4. Servidor não está rodando

**Soluções:**
```cmd
# Verificar firewall
netsh advfirewall firewall add rule name="Servidor Mobile" dir=in action=allow protocol=TCP localport=8081

# Verificar se servidor está rodando
netstat -ano | findstr :8081

# Testar do próprio servidor
curl http://localhost:8081/inventario/api/mobile/test/ping
```

### Se aparecer nos logs mas retornar erro:
✓ **Conexão OK** - O problema está na aplicação

**Verifique:**
1. URL correta no app
2. Formato do JSON
3. Headers corretos (Content-Type: application/json)

### Se o /ping funcionar mas o /login não:
✓ **Conexão OK** - Problema na autenticação

**Verifique:**
1. Senha está em BCrypt? Execute: `atualizar-senha-admin.bat`
2. Formato do JSON de login está correto?
3. Endpoint correto: `/api/mobile/auth/login`

## Teste Completo de Login

### 1. Testar com curl (do servidor)
```bash
curl -X POST http://localhost:8081/inventario/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

### 2. Verificar resposta esperada
**Sucesso (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nomeCompleto": "Administrador do Sistema",
      ...
    }
  },
  "message": "Login realizado com sucesso"
}
```

**Erro de senha (401 Unauthorized):**
```json
{
  "success": false,
  "error": "Credenciais inválidas",
  "errorCode": "AUTH_FAILED"
}
```

## URLs Importantes

### Endpoints de Teste (não requerem autenticação)
- `GET /api/mobile/test/ping` - Teste básico
- `POST /api/mobile/test/echo` - Teste de envio de dados
- `GET /api/mobile/test/login-info` - Informações de login
- `GET /actuator/health` - Status do servidor

### Endpoints de Autenticação
- `POST /api/mobile/auth/login` - Login
- `POST /api/mobile/auth/validate` - Validar token
- `POST /api/mobile/auth/logout` - Logout

### Documentação
- `GET /swagger-ui.html` - Documentação interativa da API

## Checklist de Verificação

- [ ] Servidor está rodando na porta 8081
- [ ] IP do servidor está correto
- [ ] Smartphone na mesma rede Wi-Fi
- [ ] Firewall permite porta 8081
- [ ] `/test/ping` funciona do navegador do servidor
- [ ] `/test/ping` funciona do navegador do smartphone
- [ ] Senha do admin está em BCrypt
- [ ] Logs mostram requisições do smartphone
- [ ] Aplicativo usa URL correta

## Comandos Rápidos

```cmd
# Ver IP
ipconfig

# Ver porta 8081
netstat -ano | findstr :8081

# Abrir firewall
netsh advfirewall firewall add rule name="Servidor Mobile" dir=in action=allow protocol=TCP localport=8081

# Atualizar senha admin
atualizar-senha-admin.bat

# Testar ping
curl http://localhost:8081/inventario/api/mobile/test/ping

# Testar login
curl -X POST http://localhost:8081/inventario/api/mobile/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

## Próximos Passos

1. **Reinicie o servidor** para aplicar as mudanças
2. **Teste o endpoint /ping** do navegador do smartphone
3. **Verifique os logs** - agora você verá TODAS as requisições
4. **Se o ping funcionar**, teste o login
5. **Se o login falhar**, execute `atualizar-senha-admin.bat`
