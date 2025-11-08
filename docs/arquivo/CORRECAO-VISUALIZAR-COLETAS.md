# Correção - Visualizar Coletas Não Mostra Itens Coletados

## Problema Identificado

A tela "Visualizar Coletas" no app mobile não está exibindo as coletas existentes, mesmo havendo coletas cadastradas no banco de dados.

## Causa Raiz

O app mobile está chamando o endpoint `GET /api/mobile/coletas` para buscar todas as coletas, mas **esse endpoint não estava implementado** no backend.

O `MobileColetaController` tinha apenas:
- `GET /api/mobile/coletas/pendentes` - coletas pendentes
- `GET /api/mobile/coletas/historico` - histórico com limite

Mas faltava o endpoint básico `GET /api/mobile/coletas` para listar todas as coletas do usuário.

**Problema adicional:** O endpoint `/api/mobile/coletas/**` está configurado como público (`.permitAll()`) no `MobileSecurityConfig`, o que significa que o filtro JWT não é executado automaticamente. Isso causava erro 405 (Method Not Allowed) porque o método tentava obter o usuário do `SecurityContext`, mas não havia usuário autenticado.

## Solução Aplicada

### 1. Adicionado Endpoint no Controller

**Arquivo:** `src/main/java/com/inventario/mobile/server/controller/MobileColetaController.java`

Adicionado novo método:

```java
/**
 * Buscar todas as coletas do usuário autenticado
 * Endpoint público que aceita token JWT opcional
 * 
 * @param authHeader header de autorização com token JWT (opcional)
 * @return lista de todas as coletas
 */
@GetMapping
public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarTodasColetas(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    try {
        String username = null;
        
        // Tentar extrair username do token JWT se fornecido
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                username = jwtTokenProvider.getUsernameFromToken(token);
                logger.info("Username extraído do token JWT: {}", username);
            } catch (Exception e) {
                logger.warn("Erro ao extrair username do token: {}", e.getMessage());
            }
        }
        
        // Se não conseguiu extrair do token, tentar do SecurityContext
        if (username == null) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                    username = authentication.getName();
                    logger.info("Username extraído do SecurityContext: {}", username);
                }
            } catch (Exception e) {
                logger.warn("Erro ao extrair username do SecurityContext: {}", e.getMessage());
            }
        }
        
        // Se não temos username, retornar erro
        if (username == null) {
            logger.warn("Tentativa de buscar coletas sem autenticação");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Autenticação necessária", "UNAUTHORIZED"));
        }
        
        logger.info("Buscando todas as coletas para usuário: {}", username);
        
        List<MobileColetaResponse> coletas = mobileColetaService.buscarTodasColetas(username);
        
        return ResponseEntity.ok(
                ApiResponse.success(coletas, "Coletas carregadas com sucesso"));
        
    } catch (Exception e) {
        logger.error("Erro ao buscar coletas", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro ao buscar coletas", "FETCH_ERROR"));
    }
}
```

**Importante:** O método agora extrai o username manualmente do token JWT (similar ao Dashboard), pois o endpoint é público e o filtro JWT não é executado automaticamente.

### 2. Adicionado Método no Service

**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`

Adicionado novo método:

```java
/**
 * Busca todas as coletas do usuário
 */
public List<MobileColetaResponse> buscarTodasColetas(String username) throws SQLException {
    logger.info("Buscando todas as coletas para usuário: {}", username);

    Usuario usuario = usuarioDAO.buscarUsuarioPorLogin(username);
    if (usuario == null) {
        throw new IllegalArgumentException("Usuário não encontrado");
    }

    List<Coleta> coletas = coletaDAO.buscarPorColetor(usuario.getId());
    List<MobileColetaResponse> responses = new ArrayList<>();

    for (Coleta coleta : coletas) {
        Inventario inventario = inventarioDAO.buscarInventarioPorId(coleta.getIdInventario());
        responses.add(converterParaResponse(coleta, usuario, inventario));
    }

    logger.info("Encontradas {} coletas para o usuário {}", responses.size(), username);
    return responses;
}
```

## Como Aplicar a Correção

### 1. Reiniciar o Servidor Spring Boot

```powershell
# Parar o servidor atual (se estiver rodando)
# Pressione Ctrl+C no terminal onde está rodando

# Ou use o script
.\kill-port-8081-force.ps1

# Iniciar novamente
.\mvnw.cmd spring-boot:run
```

### 2. Testar o Endpoint

Execute o script de teste:

```powershell
.\test-coletas-endpoint.ps1
```

Ou teste manualmente:

```powershell
# 1. Fazer login
$body = '{"username":"admin","password":"admin123"}'
$response = Invoke-RestMethod -Uri "http://localhost:8081/inventario/api/mobile/auth/login" -Method Post -Body $body -ContentType "application/json"

# 2. Extrair token
$token = $response.accessToken

# 3. Buscar coletas
$headers = @{ "Authorization" = "Bearer $token" }
$coletas = Invoke-RestMethod -Uri "http://localhost:8081/inventario/api/mobile/coletas" -Method Get -Headers $headers

# 4. Exibir resultado
$coletas | ConvertTo-Json -Depth 3
```

### 3. Verificar Coletas no Banco (Opcional)

Se quiser verificar se há coletas no banco de dados:

```powershell
# Execute o script SQL (ajuste o caminho do psql conforme sua instalação)
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -d inventario -f verificar-coletas.sql
```

### 4. Testar no App Mobile

1. Abra o app no emulador
2. Faça login
3. No Dashboard, clique em "Visualizar Coletas"
4. As coletas devem aparecer na lista

## Resultado Esperado

### Resposta da API

```json
{
  "success": true,
  "message": "Coletas carregadas com sucesso",
  "data": [
    {
      "id": 1,
      "idInventario": 1,
      "nomeInventario": "Inventário 2024",
      "numeroPatrimonio": "12345",
      "descricaoPatrimonio": "Notebook Dell",
      "dataColeta": "2024-01-15T10:30:00",
      "statusColeta": "COLETADO",
      "nomeColetor": "Administrador do Sistema",
      "localizacaoEncontrada": "Sala 101",
      "observacaoColeta": "Item em bom estado",
      "sincronizado": true,
      "semEtiqueta": false
    }
  ]
}
```

### App Mobile

A tela "Visualizar Coletas" deve mostrar:
- Lista de todas as coletas realizadas
- Informações de cada coleta (patrimônio, data, status, etc.)
- Filtros por sala e usuário
- Contador de total de coletas

## Fluxo Completo

```
App Mobile (CollectionViewActivity)
  ↓
InventarioRepository.getColetas()
  ↓
ApiService.getColetas() → GET /api/mobile/coletas
  ↓
MobileColetaController.buscarTodasColetas()
  ↓
MobileColetaService.buscarTodasColetas(username)
  ↓
ColetaDAO.buscarPorColetor(usuarioId)
  ↓
Retorna List<MobileColetaResponse>
```

## Verificação de Problemas

### Se não aparecer nenhuma coleta:

1. **Verificar se há coletas no banco:**
   ```sql
   SELECT COUNT(*) FROM COLETA;
   ```

2. **Verificar se as coletas pertencem ao usuário logado:**
   ```sql
   SELECT c.*, u.LOGIN 
   FROM COLETA c 
   JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID 
   WHERE u.LOGIN = 'admin';
   ```

3. **Verificar logs do servidor:**
   - Procure por: "Buscando todas as coletas para usuário"
   - Procure por: "Encontradas X coletas para o usuário"

4. **Verificar logs do app (Logcat):**
   ```
   adb logcat | findstr "CollectionViewViewModel"
   ```

### Se aparecer erro 404:

- O servidor não foi reiniciado após as mudanças
- Reinicie o servidor Spring Boot

### Se aparecer erro 401:

- Token JWT expirado ou inválido
- Faça login novamente no app

### Se aparecer erro 500:

- Verifique os logs do servidor para detalhes
- Pode ser problema de conexão com o banco de dados

## Endpoints de Coletas Disponíveis

Após a correção, os seguintes endpoints estão disponíveis:

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/mobile/coletas` | **NOVO** - Lista todas as coletas do usuário |
| GET | `/api/mobile/coletas/pendentes` | Lista coletas pendentes de sincronização |
| GET | `/api/mobile/coletas/historico?limit=50` | Lista histórico com limite |
| GET | `/api/mobile/coletas/{id}` | Busca coleta específica por ID |
| POST | `/api/mobile/coletas` | Registra nova coleta |
| POST | `/api/mobile/coletas/batch` | Registra múltiplas coletas |
| PUT | `/api/mobile/coletas/{id}` | Atualiza coleta existente |
| DELETE | `/api/mobile/coletas/{id}` | Exclui coleta (apenas admin) |

## Scripts Criados

1. **test-coletas-endpoint.ps1** - Testa o endpoint de coletas
2. **verificar-coletas.sql** - Verifica coletas no banco de dados

## Melhorias Futuras

1. Adicionar paginação para grandes volumes de coletas
2. Adicionar filtros por data, status, sala
3. Implementar cache local no app
4. Adicionar sincronização em background
5. Implementar busca por texto

## Resumo

✅ **Problema:** Endpoint `GET /api/mobile/coletas` não existia  
✅ **Solução:** Implementado endpoint no controller e service  
✅ **Resultado:** App consegue listar todas as coletas do usuário  

Após reiniciar o servidor, a tela "Visualizar Coletas" deve funcionar corretamente!
