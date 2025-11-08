# Correção Final - Dashboard não Carrega Total de Patrimônios

## Problema Identificado

O endpoint `/api/mobile/dashboard/stats` estava configurado como **público** (`.permitAll()`) no `MobileSecurityConfig`, o que causava:

1. O filtro JWT não era executado para esse endpoint
2. O `SecurityContextHolder` não tinha o usuário autenticado
3. `authentication.getName()` retornava "anonymousUser" em vez do username real
4. A busca do usuário no banco falhava
5. O endpoint retornava erro "Usuário não encontrado"

## Solução Aplicada

### 1. Alteração no MobileSecurityConfig.java

**Antes:**
```java
// TEMPORÁRIO: Liberar dashboard e coletas sem autenticação
.requestMatchers("/api/mobile/dashboard/**").permitAll()
.requestMatchers("/api/mobile/coletas/**").permitAll()
.requestMatchers("/api/mobile/patrimonios/**").permitAll()
.requestMatchers("/api/mobile/salas/**").permitAll()
.requestMatchers("/api/mobile/setores/**").permitAll()
```

**Depois:**
```java
// Endpoints que requerem autenticação
.requestMatchers("/api/mobile/dashboard/**").authenticated()
.requestMatchers("/api/mobile/coletas/**").authenticated()
.requestMatchers("/api/mobile/patrimonios/**").authenticated()
.requestMatchers("/api/mobile/salas/**").authenticated()
.requestMatchers("/api/mobile/setores/**").authenticated()
.requestMatchers("/api/mobile/descricoes/**").authenticated()
```

### 2. Reversão no MobileDashboardController.java

O controller foi revertido para retornar erro quando o usuário não for encontrado, já que agora o endpoint sempre terá um usuário autenticado.

## Como Aplicar a Correção

### 1. Reiniciar o Servidor

```powershell
# Parar o servidor
.\kill-port-8081-force.ps1

# Iniciar novamente
.\mvnw.cmd spring-boot:run
```

### 2. Reinstalar o App (Opcional)

Se houver problemas de cache:

```powershell
cd InventarioMobile
.\gradlew clean installDebug
```

### 3. Testar no App

1. Abra o app no emulador
2. Faça login com suas credenciais
3. O Dashboard deve carregar corretamente mostrando:
   - Total de Patrimônios
   - Patrimônios Coletados
   - Patrimônios Pendentes
   - Total de Salas
   - Salas Completas
   - Salas Pendentes

## Verificação

### Testar Manualmente o Endpoint

```powershell
# 1. Fazer login
$body = '{"username":"admin","password":"admin123"}'
$response = Invoke-RestMethod -Uri "http://localhost:8081/inventario/api/mobile/auth/login" -Method Post -Body $body -ContentType "application/json"

# 2. Extrair token
$token = $response.accessToken

# 3. Buscar estatísticas (AGORA COM AUTENTICAÇÃO)
$headers = @{
    "Authorization" = "Bearer $token"
}
$stats = Invoke-RestMethod -Uri "http://localhost:8081/inventario/api/mobile/dashboard/stats" -Method Get -Headers $headers

# 4. Exibir resultado
$stats | ConvertTo-Json -Depth 3
```

### Resultado Esperado

```json
{
  "success": true,
  "message": "Estatísticas carregadas com sucesso",
  "data": {
    "totalPatrimonios": 150,
    "patrimoniosColetados": 45,
    "patrimoniosPendentes": 105,
    "totalSalas": 25,
    "salasCompletas": 10,
    "salasPendentes": 15
  }
}
```

## Endpoints Agora Protegidos

Todos os seguintes endpoints agora **requerem autenticação** (token JWT):

- `/api/mobile/dashboard/**` - Estatísticas do dashboard
- `/api/mobile/coletas/**` - Operações de coleta
- `/api/mobile/patrimonios/**` - Operações de patrimônio
- `/api/mobile/salas/**` - Listagem de salas
- `/api/mobile/setores/**` - Listagem de setores
- `/api/mobile/descricoes/**` - Listagem de descrições

## Endpoints Públicos (Sem Autenticação)

Apenas os seguintes endpoints permanecem públicos:

- `/api/mobile/auth/login` - Login
- `/api/mobile/auth/validate` - Validação de token
- `/api/mobile/health` - Health check
- `/api/mobile/test/**` - Endpoints de teste

## Fluxo de Autenticação

1. **Login:** App envia username/password para `/api/mobile/auth/login`
2. **Token:** Servidor retorna `accessToken` e `refreshToken`
3. **Requisições:** App inclui `Authorization: Bearer {accessToken}` em todas as requisições
4. **Filtro JWT:** `MobileJwtAuthenticationFilter` valida o token e popula o `SecurityContextHolder`
5. **Controller:** Acessa o usuário autenticado via `SecurityContextHolder.getContext().getAuthentication()`

## Logs de Verificação

Após reiniciar o servidor, os logs devem mostrar:

```
╔════════════════════════════════════════════════════════════════╗
║  MOBILE SECURITY CONFIG CARREGADA - PROFILE MOBILE ATIVO      ║
╚════════════════════════════════════════════════════════════════╝
Criando MobileJwtAuthenticationFilter...
Adicionando MobileJwtAuthenticationFilter à cadeia de segurança
MobileJwtAuthenticationFilter adicionado com sucesso!
```

E ao fazer uma requisição ao dashboard:

```
Buscando estatísticas do dashboard para usuário: admin
Usuário encontrado - ID: 1, Nome: Administrador do Sistema, Ativo: true
Estatísticas: Total=150, Coletados=45, Pendentes=105
```

## Problemas Comuns

### "401 Unauthorized" ao acessar o dashboard

**Causa:** Token não está sendo enviado ou é inválido

**Solução:**
1. Verifique se o app está enviando o header `Authorization: Bearer {token}`
2. Verifique se o token não expirou (validade padrão: 24 horas)
3. Faça login novamente para obter um novo token

### "Usuário não encontrado"

**Causa:** O usuário autenticado não existe no banco de dados

**Solução:**
```sql
-- Verificar se o usuário existe
SELECT * FROM TABELA_USUARIO WHERE LOGIN = 'seu_usuario' AND ATIVO = true;

-- Se não existir ou estiver inativo, corrigir
UPDATE TABELA_USUARIO SET ATIVO = true WHERE LOGIN = 'seu_usuario';
```

### App não envia o token

**Causa:** Token não está sendo salvo ou recuperado corretamente

**Solução:** Verificar `PreferencesManager` no app:
```kotlin
// Salvar token após login
preferencesManager.setAuthToken(loginResponse.accessToken)

// Recuperar token para requisições
val token = preferencesManager.getAuthToken()
```

## Melhorias Futuras

1. Implementar refresh token automático quando o access token expirar
2. Adicionar cache local de estatísticas no app
3. Implementar sincronização em background
4. Adicionar indicador de última atualização no Dashboard

## Resumo

✅ **Problema:** Endpoint do dashboard estava público, não tinha usuário autenticado  
✅ **Solução:** Mudou para `.authenticated()`, agora requer token JWT  
✅ **Resultado:** Dashboard carrega corretamente com dados do usuário logado  

## Teste Final

Execute este comando para testar tudo de uma vez:

```powershell
.\test-dashboard-endpoint.ps1
```

Se tudo estiver correto, você verá:

```
✓ Login realizado com sucesso!
✓ Estatísticas obtidas com sucesso!

Total de Patrimônios:     150
Patrimônios Coletados:    45
Patrimônios Pendentes:    105
Total de Salas:           25
Salas Completas:          10
Salas Pendentes:          15
```
