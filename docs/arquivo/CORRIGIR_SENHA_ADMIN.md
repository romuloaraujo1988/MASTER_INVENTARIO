# Como Corrigir a Senha do Usuário Admin

## Problema
O erro "Encoded password does not look like BCrypt" ocorre porque a senha do usuário admin no banco de dados não está em formato BCrypt, que é o formato esperado pelo Spring Security.

## Solução

### Opção 1: Usar o Script Batch (Windows)
Execute o arquivo `UpdateAdminPassword.bat` que foi criado. Ele irá:
1. Conectar ao PostgreSQL
2. Atualizar a senha do usuário admin para o hash BCrypt correto
3. Confirmar a atualização

### Opção 2: Executar SQL Manualmente
Se o script batch não funcionar (por exemplo, se o psql não estiver no PATH), execute o seguinte SQL diretamente no PostgreSQL:

```sql
UPDATE usuario 
SET senha_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE login = 'admin';
```

### Opção 3: Usar pgAdmin ou DBeaver
1. Abra o pgAdmin ou DBeaver
2. Conecte ao banco `sispatrimonio`
3. Execute o SQL acima
4. Confirme que 1 linha foi atualizada

## Verificação
Após executar qualquer uma das opções acima, você poderá fazer login com:
- **Usuário:** admin
- **Senha:** admin

## Informação Técnica
O hash BCrypt `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` corresponde à senha "admin" criptografada com BCrypt usando 10 rounds de hashing.

## Próximos Passos
Depois de corrigir a senha:
1. Reinicie o servidor mobile (se estiver rodando)
2. Tente fazer login novamente no aplicativo mobile
3. O login deve funcionar corretamente

## Prevenção
Para evitar este problema no futuro, sempre use o `PasswordEncoder` do Spring Security ao criar ou atualizar senhas:

```java
@Autowired
private PasswordEncoder passwordEncoder;

// Ao criar/atualizar usuário
usuario.setSenhaHash(passwordEncoder.encode(senhaTextoPlano));
```
