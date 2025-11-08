# Solução: Erro de Conexão com Banco de Dados

## 🔴 Erro Identificado

```
HikariPool-1 - jdbcUrl is required with driverClassName.
DataSource health check failed
java.lang.IllegalArgumentException: jdbcUrl is required with driverClassName.
```

## 🎯 Causa

O servidor mobile não conseguiu conectar ao banco de dados PostgreSQL devido a:
1. Configuração do HikariCP não carregada corretamente
2. Possível problema de credenciais ou banco de dados inacessível

## ✅ Soluções Implementadas

### 1. Configuração Explícita do DataSource

Criada a classe `MobileDataSourceConfig.java` que configura explicitamente o HikariCP para o perfil mobile.

### 2. Configuração Duplicada no application-mobile.properties

Adicionadas propriedades redundantes para garantir que o HikariCP receba a configuração:

```properties
# Configuração padrão
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.jdbc-url=jdbc:postgresql://localhost:5432/sispatrimonio

# Configuração específica do HikariCP
spring.datasource.hikari.jdbc-url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.hikari.username=postgres
spring.datasource.hikari.password=Romulo@2020
```

### 3. Script de Teste de Conexão

Criado `testar-conexao-banco.ps1` para diagnosticar problemas de conexão.

## 🔧 Como Resolver

### Passo 1: Verificar PostgreSQL

```powershell
# Verificar se PostgreSQL está rodando
Get-Process postgres

# Verificar porta 5432
netstat -ano | findstr :5432
```

### Passo 2: Testar Conexão

```powershell
# Executar script de teste
.\testar-conexao-banco.ps1
```

### Passo 3: Verificar Credenciais

Abra `configuracao_banco.json` e verifique:
```json
{
    "postgresql": {
        "host": "localhost",
        "database": "sispatrimonio",
        "user": "postgres",
        "password": "Romulo@2020",
        "port": 5432,
        "schema": "public"
    }
}
```

### Passo 4: Verificar se o Banco Existe

```sql
-- Conectar ao PostgreSQL e verificar
\l
-- Deve listar o banco 'sispatrimonio'

-- Conectar ao banco
\c sispatrimonio

-- Verificar tabelas
\dt
```

### Passo 5: Reiniciar o Servidor Mobile

Após verificar a conexão:

1. Abra o sistema desktop como administrador
2. Menu: `Administração → Servidor Mobile`
3. Clique em `Iniciar Servidor`
4. Aguarde a inicialização

## 🔍 Diagnóstico Detalhado

### Verificar Logs

Os logs do servidor estão em:
```
logs/sistema-inventario.log
```

Procure por:
- `HikariPool` - Informações do pool de conexões
- `DataSource` - Configuração do datasource
- `PostgreSQL` - Conexões com o banco

### Testar Conexão Manual

```powershell
# Usando psql (se instalado)
psql -h localhost -p 5432 -U postgres -d sispatrimonio

# Senha: Romulo@2020
```

### Verificar Permissões

Arquivo `pg_hba.conf` deve permitir conexões locais:
```
# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5
```

## 🚀 Inicialização Correta

Após resolver o problema de conexão, o servidor deve iniciar com sucesso:

```
✓ HikariPool-1 - Starting...
✓ HikariPool-1 - Start completed.
✓ Started MobileApiApplication in X.XXX seconds
✓ Tomcat started on port(s): 8081 (http)
```

## 📱 Testar Conexão do Smartphone

Após o servidor iniciar:

1. No navegador do smartphone, acesse:
   ```
   http://[IP_DO_SERVIDOR]:8081/inventario/api/mobile/health
   ```

2. Deve retornar:
   ```json
   {
     "status": "UP"
   }
   ```

## ⚠️ Problemas Comuns

### PostgreSQL não está rodando
**Solução:** Inicie o serviço PostgreSQL

### Senha incorreta
**Solução:** Verifique a senha em `configuracao_banco.json`

### Banco não existe
**Solução:** Crie o banco:
```sql
CREATE DATABASE sispatrimonio;
```

### Firewall bloqueando
**Solução:** Libere a porta 5432 no firewall

### Usuário sem permissão
**Solução:** Conceda permissões:
```sql
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO postgres;
```

## 📞 Suporte

Se o problema persistir:

1. Execute `testar-conexao-banco.ps1`
2. Copie os logs de erro
3. Verifique a documentação do PostgreSQL
4. Entre em contato com o suporte técnico

---

**Versão:** 1.2.0  
**Última atualização:** 2025  
**Sistema de Inventário - IFMT**
