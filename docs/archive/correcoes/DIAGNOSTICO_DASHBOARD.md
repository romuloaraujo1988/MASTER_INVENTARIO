# Diagnóstico do Problema da Dashboard

## Status Atual

✅ **App funcionando corretamente**
- Conecta ao servidor
- Autentica com sucesso
- Faz requisição para `/api/mobile/dashboard/stats`
- Recebe resposta HTTP 200

✅ **Servidor respondendo**
- Endpoint `/api/mobile/dashboard/stats` está ativo
- Retorna resposta válida
- Sem erros de autenticação

❌ **Problema: Dados zerados**
- Servidor retorna: `totalPatrimonios=0, patrimoniosColetados=0, etc.`
- Mesmo com patrimônios no banco de dados

## Causa Raiz Identificada

O `PatrimonioDAORefactored` usa `ConnectionManager.getConnection()` que provavelmente está configurado para usar o arquivo `configuracao_banco.json` do usuário, **NÃO** as configurações do Spring Boot (`application-mobile.properties`).

### Evidência:

1. **BaseDAO.java** (linha 60-70):
```java
public void insert(T entity) throws SQLException {
    Connection conn = null;
    try {
        conn = ConnectionManager.getConnection();  // ← Usa ConnectionManager
        // ...
    } finally {
        ConnectionManager.closeConnection(conn);
    }
}
```

2. **application-mobile.properties** define:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.username=postgres
spring.datasource.password=Romulo@2020
```

3. **Mas ConnectionManager** provavelmente lê de:
```
C:\Users\<usuario>\.inventario\configuracao_banco.json
```

## Soluções Possíveis

### Solução 1: Usar Spring JPA Repository (RECOMENDADO)

Criar um `PatrimonioRepository` usando Spring Data JPA:

```java
@Repository
public interface PatrimonioRepository extends JpaRepository<Patrimonio, Integer> {
    // Spring gera automaticamente findAll(), findById(), etc.
}
```

**Vantagens:**
- Usa automaticamente as configurações do Spring
- Menos código
- Mais moderno e mantível

### Solução 2: Configurar ConnectionManager para Mobile

Modificar `ConnectionManager` para usar as configurações do Spring quando rodando em modo mobile:

```java
public static Connection getConnection() throws SQLException {
    // Se estiver em modo mobile, usar DataSource do Spring
    if (isMobileMode()) {
        return springDataSource.getConnection();
    }
    // Senão, usar configuracao_banco.json
    return getConnectionFromFile();
}
```

### Solução 3: Criar DAO específico para Mobile

Criar `MobilePatrimonioDAO` que usa `@Autowired DataSource`:

```java
@Repository
public class MobilePatrimonioDAO {
    @Autowired
    private DataSource dataSource;
    
    public List<Patrimonio> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            // Query SQL aqui
        }
    }
}
```

## Solução Imediata (Workaround)

### Passo 1: Verificar configuracao_banco.json

Verificar se existe e está correto:
```
C:\Users\<seu-usuario>\.inventario\configuracao_banco.json
```

Deve conter:
```json
{
  "host": "localhost",
  "porta": "5432",
  "database": "sispatrimonio",
  "usuario": "postgres",
  "senha": "Romulo@2020"
}
```

### Passo 2: Testar conexão direta

Adicionar endpoint de teste no controller:

```java
@GetMapping("/test-db")
public ResponseEntity<String> testDatabase() {
    try {
        Connection conn = ConnectionManager.getConnection();
        DatabaseMetaData meta = conn.getMetaData();
        String info = "Conectado a: " + meta.getURL() + 
                     " como " + meta.getUserName();
        ConnectionManager.closeConnection(conn);
        return ResponseEntity.ok(info);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Erro: " + e.getMessage());
    }
}
```

Testar: `http://localhost:8081/inventario/api/mobile/dashboard/test-db`

## Próximos Passos

1. ✅ Alterações já feitas no código (logs adicionados)
2. ⏳ **VOCÊ PRECISA FAZER**: Recompilar e reiniciar servidor
3. ⏳ Verificar logs do servidor ao buscar estatísticas
4. ⏳ Se ainda retornar 0, implementar Solução 1 (JPA Repository)

## Como Recompilar

```powershell
# Parar servidor
netstat -ano | findstr ":8081"
taskkill /F /PID <PID>

# Recompilar
mvn clean compile -DskipTests

# Reiniciar
mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication"
```

## Logs Esperados Após Correção

```
INFO  MobileDashboardController - === INICIANDO BUSCA DE ESTATÍSTICAS ===
INFO  MobileDashboardController - PatrimonioDAO injetado: SIM
INFO  MobileDashboardController - Buscando todos os patrimônios...
INFO  MobileDashboardController - Total de patrimônios encontrados: 150
INFO  MobileDashboardController - Primeiro patrimônio: ID=1, Numero=001234, Descricao=Computador Dell
```

Se aparecer erro de conexão, confirma que o problema é o ConnectionManager.
