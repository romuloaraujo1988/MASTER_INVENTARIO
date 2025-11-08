# Autenticação API Mobile - Guia Completo

## Problema Identificado

O aplicativo mobile estava acessando apenas o endpoint `/actuator/health` mas não conseguia autenticar no endpoint `/api/mobile/auth/login`.

## Solução Implementada

### 1. UserDetailsService Customizado

Criado `CustomUserDetailsService` que integra o Spring Security com o banco de dados local:
- Busca usuários no banco via `UsuarioDAO`
- Verifica se o usuário está ativo e não bloqueado
- Converte o perfil do usuário em authorities do Spring Security

### 2. Configuração de Segurança Atualizada

Adicionado `DaoAuthenticationProvider` no `SecurityConfig`:
- Usa o `CustomUserDetailsService` para carregar usuários
- Usa `BCryptPasswordEncoder` para validar senhas
- Integra com o `AuthenticationManager` do Spring Security

## Fluxo de Autenticação

### Desktop (Aplicação Java Swing)
```
JLogin → AutenticacaoServiceDB → UsuarioDAO → Banco de Dados Local
```
- Autenticação direta via banco de dados
- Não usa API REST
- Não usa Spring Security

### Mobile (Aplicativo Android)
```
App Mobile → POST /api/mobile/auth/login → MobileAuthController 
→ MobileAuthService → AuthenticationManager → CustomUserDetailsService 
→ UsuarioDAO → Banco de Dados Local → JWT Token
```

## Endpoints da API Mobile

### 1. Login
```http
POST http://localhost:8081/inventario/api/mobile/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Resposta de Sucesso (200 OK):**
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nomeCompleto": "Administrador do Sistema",
      "email": "admin@ifmt.edu.br",
      "perfil": "ADMIN",
      "ativo": true
    }
  }
}
```

**Resposta de Erro (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Credenciais inválidas",
  "errorCode": "AUTH_FAILED"
}
```

### 2. Health Check
```http
GET http://localhost:8081/inventario/actuator/health
```

**Resposta:**
```json
{
  "status": "UP"
}
```

### 3. Teste de Ping
```http
GET http://localhost:8081/inventario/api/mobile/test/ping
```

## Como Testar

### 1. Verificar se o servidor está rodando
```bash
curl http://localhost:8081/inventario/actuator/health
```

### 2. Testar autenticação via curl
```bash
curl -X POST http://localhost:8081/inventario/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

### 3. Testar autenticação via PowerShell
```powershell
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/inventario/api/mobile/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

### 4. Verificar logs do servidor

Ao fazer login, você deve ver nos logs:
```
INFO  c.i.m.s.s.MobileAuthService - Tentativa de login mobile para usuário: admin
INFO  c.i.m.s.s.MobileAuthService - Login mobile realizado com sucesso para usuário: admin
```

## Configuração do Aplicativo Mobile

### 1. URL Base
```
http://SEU_IP:8081/inventario
```

### 2. Endpoint de Login
O aplicativo deve fazer POST para:
```
/api/mobile/auth/login
```

URL completa:
```
http://SEU_IP:8081/inventario/api/mobile/auth/login
```

### 3. Payload do Login
```json
{
  "username": "admin",
  "password": "admin123"
}
```

## Troubleshooting

### Problema: "Credenciais inválidas"

**Possíveis causas:**
1. Senha incorreta
2. Usuário não existe no banco
3. Senha não está com hash BCrypt

**Solução:**
- Verificar se o usuário existe: `SELECT * FROM usuarios WHERE login = 'admin'`
- Verificar se a senha está com hash BCrypt (deve começar com `$2a$` ou `$2b$`)
- Se necessário, recriar o usuário admin via aplicação desktop

### Problema: "Erro de conexão"

**Possíveis causas:**
1. Servidor não está rodando
2. Firewall bloqueando porta 8081
3. IP incorreto

**Solução:**
```bash
# Verificar se o servidor está rodando
netstat -ano | findstr :8081

# Liberar porta no firewall
netsh advfirewall firewall add rule name="Inventario Mobile" dir=in action=allow protocol=TCP localport=8081

# Verificar IP local
ipconfig
```

### Problema: "UserDetailsService não encontrado"

**Causa:**
Spring Security não consegue encontrar o `CustomUserDetailsService`

**Solução:**
- Verificar se `CustomUserDetailsService` tem anotação `@Service`
- Verificar se está no pacote correto para ser escaneado pelo Spring
- Verificar se `UsuarioDAO` está sendo injetado corretamente

## Estrutura de Senhas

### Formato BCrypt
As senhas devem estar armazenadas no formato BCrypt:
```
$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOP
```

### Criar Hash BCrypt
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("admin123");
```

### Verificar Senha
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
boolean matches = encoder.matches("admin123", hash);
```

## Usuário Padrão

Ao iniciar a aplicação desktop pela primeira vez, é criado automaticamente:

- **Login:** admin
- **Senha:** admin123
- **Perfil:** ADMIN
- **Email:** admin@ifmt.edu.br

Este usuário pode ser usado para testar a autenticação mobile.

## Próximos Passos

1. ✅ Criar `CustomUserDetailsService`
2. ✅ Configurar `DaoAuthenticationProvider`
3. ✅ Testar endpoint de login
4. 🔲 Testar no aplicativo mobile
5. 🔲 Implementar refresh token
6. 🔲 Adicionar logs detalhados
