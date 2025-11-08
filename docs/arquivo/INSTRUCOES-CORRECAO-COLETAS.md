# Instruções para Corrigir Visualização de Coletas

## Problema
Erro HTTP 405 (Method Not Allowed) ao tentar buscar coletas no app mobile.

## Causa
O endpoint `/api/mobile/coletas` está configurado como público (`.permitAll()`), então o filtro JWT não é executado automaticamente. O método tentava obter o usuário do `SecurityContext`, mas não havia usuário autenticado.

## Solução Aplicada
Modificado o método `buscarTodasColetas()` para extrair o username manualmente do token JWT (similar ao Dashboard).

## Arquivos Modificados
1. ✅ `src/main/java/com/inventario/mobile/server/controller/MobileColetaController.java`
   - Adicionado `@Autowired JwtTokenProvider`
   - Modificado método `buscarTodasColetas()` para aceitar header Authorization
   - Implementada extração manual do username do token JWT

2. ✅ `src/main/java/com/inventario/mobile/server/service/MobileColetaService.java`
   - Adicionado método `buscarTodasColetas(username)`

## Passos para Testar

### 1. Reiniciar o Servidor
```powershell
# No terminal onde o servidor está rodando, pressione Ctrl+C

# Depois inicie novamente:
.\mvnw.cmd spring-boot:run
```

### 2. Aguardar o Servidor Iniciar
Aguarde até ver a mensagem:
```
Started InventarioApplication in X.XXX seconds
```

### 3. Testar o Endpoint
```powershell
.\test-coletas-endpoint.ps1
```

### 4. Resultado Esperado
```
=== Testando Endpoint de Coletas ===

1. Fazendo login...
✓ Login realizado com sucesso!

2. Buscando todas as coletas...
✓ Coletas obtidas com sucesso!

=== Resultado ===
Total de coletas: X

Primeiras coletas:
  - ID: 1
    Patrimônio: 12345
    Descrição: Notebook Dell
    Data: 2024-01-15T10:30:00
    Status: COLETADO
    Coletor: Administrador do Sistema
    Sincronizado: True
```

### 5. Testar no App Mobile

1. Abra o emulador Android
2. Abra o app Inventário
3. Faça login
4. No Dashboard, clique em "Visualizar Coletas"
5. As coletas devem aparecer na lista

## Verificação de Logs

### Logs do Servidor (Spring Boot)
Procure por estas mensagens:
```
Username extraído do token JWT: admin
Buscando todas as coletas para usuário: admin
Encontradas X coletas para o usuário admin
```

### Logs do App (Logcat)
```powershell
adb logcat | findstr "CollectionViewViewModel"
```

Procure por:
```
CollectionViewViewModel: Iniciando carregamento de coletas
CollectionViewViewModel: Coletas carregadas: X itens
```

## Troubleshooting

### Erro: "Nenhuma conexão pôde ser feita"
- O servidor não está rodando
- Inicie o servidor: `.\mvnw.cmd spring-boot:run`

### Erro: HTTP 401 (Unauthorized)
- Token expirado ou inválido
- Faça login novamente no app

### Erro: HTTP 405 (Method Not Allowed)
- O servidor não foi reiniciado após as mudanças
- Reinicie o servidor

### Nenhuma coleta aparece (lista vazia)
1. Verifique se há coletas no banco:
   ```sql
   SELECT COUNT(*) FROM COLETA;
   ```

2. Verifique se as coletas pertencem ao usuário logado:
   ```sql
   SELECT c.*, u.LOGIN 
   FROM COLETA c 
   JOIN TABELA_USUARIO u ON c.ID_COLETOR = u.ID 
   WHERE u.LOGIN = 'admin';
   ```

3. Se não houver coletas, faça uma coleta de teste no app

## Mudanças Técnicas

### Antes (causava erro 405)
```java
@GetMapping
public ResponseEntity<...> buscarTodasColetas() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName(); // ← Retornava "anonymousUser"
    // ...
}
```

### Depois (funciona corretamente)
```java
@GetMapping
public ResponseEntity<...> buscarTodasColetas(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    String username = null;
    
    // Extrai username do token JWT manualmente
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        username = jwtTokenProvider.getUsernameFromToken(token);
    }
    
    // Fallback para SecurityContext
    if (username == null) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        }
    }
    
    // Retorna erro se não houver username
    if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Autenticação necessária", "UNAUTHORIZED"));
    }
    
    // Busca coletas do usuário
    List<MobileColetaResponse> coletas = mobileColetaService.buscarTodasColetas(username);
    return ResponseEntity.ok(ApiResponse.success(coletas, "Coletas carregadas"));
}
```

## Próximos Passos

Após confirmar que está funcionando:
1. ✅ Testar criação de novas coletas
2. ✅ Testar filtros (por sala, por usuário)
3. ✅ Testar sincronização
4. ✅ Verificar performance com muitas coletas

## Contato

Se o problema persistir:
1. Verifique os logs do servidor
2. Verifique os logs do Logcat
3. Execute o script de verificação: `.\verificar-coletas.sql`
4. Teste o endpoint manualmente: `.\test-coletas-endpoint.ps1`
