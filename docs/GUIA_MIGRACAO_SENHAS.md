# Guia de Migração de Senhas Hardcoded

## 🎯 Objetivo

Migrar senhas que estavam hardcoded no código para o arquivo de configuração criptografado.

## ⚠️ Problema Identificado

Senhas hardcoded no código são um **risco de segurança**:

```java
// ❌ ANTES (INSEGURO)
private static final String FALLBACK_PASSWORD = "Romulo@2020";
```

## ✅ Solução Implementada

Senhas agora são armazenadas **criptografadas** em arquivo:

```java
// ✅ DEPOIS (SEGURO)
private static final String FALLBACK_PASSWORD = null; // Removido
// Senha carregada de ~/.inventario/database-config.properties (criptografada)
```

## 📋 Checklist de Migração

### 1. Identificar Senhas Hardcoded

Procure por:
- `password = "..."`
- `FALLBACK_PASSWORD`
- Strings de conexão JDBC com senha
- Arquivos de propriedades com senhas

### 2. Remover do Código

```java
// Remover ou comentar
// private static final String PASSWORD = "senha123";

// Substituir por
private static final String PASSWORD = null; // Configure via interface
```

### 3. Migrar para Arquivo de Configuração

#### Opção A: Via Interface (Recomendado)

1. Execute a aplicação
2. Na tela de login, clique em **"⚙ Configurar Banco"**
3. Preencha os dados:
   - Servidor: localhost
   - Porta: 5432
   - Banco: sispatrimonio
   - Usuário: postgres
   - Senha: sua_senha_aqui
4. Marque **"Salvar senha (criptografada)"**
5. Clique em **"Testar Conexão"**
6. Clique em **"Salvar"**

#### Opção B: Via Script

1. Edite `src/main/java/com/inventario/util/ConfigurationMigration.java`
2. No método `main()`, descomente o bloco:

```java
boolean migrated = migrateConfiguration(
    "localhost",        // host
    5432,              // port
    "sispatrimonio",   // database
    "postgres",        // username
    "SUA_SENHA_AQUI"   // password (será criptografada)
);
```

3. Execute o script:

```bash
# Windows
migrar-configuracao.bat

# Linux/Mac
mvn exec:java -Dexec.mainClass="com.inventario.util.ConfigurationMigration"
```

#### Opção C: Programaticamente

```java
import com.inventario.util.ConfigurationMigration;

public class MigrarSenha {
    public static void main(String[] args) {
        boolean sucesso = ConfigurationMigration.migrateConfiguration(
            "localhost",
            5432,
            "sispatrimonio",
            "postgres",
            "minhaSenha123"
        );
        
        if (sucesso) {
            System.out.println("✓ Migração concluída!");
        }
    }
}
```

### 4. Verificar Migração

#### Verificar Arquivo Criado

**Windows:**
```cmd
cd %USERPROFILE%\.inventario
type database-config.properties
```

**Linux/Mac:**
```bash
cd ~/.inventario
cat database-config.properties
```

**Conteúdo esperado:**
```properties
# Database Configuration - Password is encrypted
host=localhost
port=5432
database=sispatrimonio
username=postgres
password=AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRo... (Base64 longo)
```

#### Testar Conexão

```java
import com.inventario.util.DatabaseConnection;

public class TestarConexao {
    public static void main(String[] args) {
        if (DatabaseConnection.testConnection()) {
            System.out.println("✓ Conexão OK!");
        } else {
            System.out.println("✗ Falha na conexão");
        }
    }
}
```

### 5. Limpar Código Antigo

Após confirmar que a migração funcionou:

1. **Remover senhas hardcoded**:
   ```java
   // Remover completamente
   // private static final String PASSWORD = "senha123";
   ```

2. **Remover comentários com senhas**:
   ```java
   // Remover
   // Senha padrão: senha123
   ```

3. **Atualizar documentação**:
   - Remover menções a senhas padrão
   - Adicionar instruções de configuração

## 🔍 Locais Comuns de Senhas Hardcoded

### Arquivos Java

```bash
# Procurar por senhas
grep -r "password.*=" src/main/java/
grep -r "PASSWORD.*=" src/main/java/
grep -r "senha.*=" src/main/java/
```

### Arquivos de Propriedades

```bash
# Verificar arquivos .properties
find . -name "*.properties" -exec grep -l "password" {} \;
```

### Arquivos de Configuração

- `application.properties`
- `database.properties`
- `config.properties`
- `hibernate.cfg.xml`

## 📊 Exemplo Completo de Migração

### Antes (Código Antigo)

```java
// DatabaseConnection.java
public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/sispatrimonio";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Romulo@2020"; // ❌ INSEGURO
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### Depois (Código Migrado)

```java
// DatabaseConnection.java
public class DatabaseConnection {
    private static DatabaseConfigManager configManager;
    private static DatabaseConfig currentConfig;
    
    static {
        configManager = new DatabaseConfigManager();
        currentConfig = configManager.getCurrentConfig();
    }
    
    public static Connection getConnection() throws SQLException {
        if (currentConfig != null && currentConfig.isValid()) {
            return createConnectionFromConfig(currentConfig);
        }
        
        throw new SQLException(
            "Nenhuma configuração encontrada. " +
            "Configure via interface ou arquivo de configuração."
        );
    }
    
    private static Connection createConnectionFromConfig(DatabaseConfig config) 
            throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
            config.getHost(), config.getPort(), config.getDatabase());
        
        // Senha descriptografada automaticamente
        return DriverManager.getConnection(
            url, 
            config.getUsername(), 
            config.getPassword() // ✅ Descriptografada do arquivo
        );
    }
}
```

### Arquivo de Configuração Criado

```properties
# ~/.inventario/database-config.properties
# Database Configuration - Password is encrypted
host=localhost
port=5432
database=sispatrimonio
username=postgres
password=AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXp7fH1+fw==
```

## 🔒 Segurança Após Migração

### Benefícios

- ✅ Senha não está mais no código-fonte
- ✅ Senha criptografada com AES-256
- ✅ Chave derivada da máquina (machine-specific)
- ✅ Não aparece em logs ou stack traces
- ✅ Não vai para repositório Git

### Boas Práticas

1. **Adicionar ao .gitignore**:
   ```gitignore
   # Configurações locais
   .inventario/
   database-config.properties
   sgbd-config.properties
   ```

2. **Documentar processo**:
   - Como configurar em nova máquina
   - Onde encontrar credenciais (cofre de senhas)
   - Quem contatar para obter acesso

3. **Backup seguro**:
   - Não fazer backup da senha criptografada
   - Armazenar credenciais em cofre de senhas
   - Documentar processo de recuperação

## 🚨 Troubleshooting

### Erro: "Nenhuma configuração encontrada"

**Causa**: Arquivo de configuração não existe

**Solução**:
1. Execute a aplicação
2. Configure via interface
3. Ou execute script de migração

### Erro: "Senha não descriptografa"

**Causa**: Arquivo copiado de outra máquina

**Solução**:
1. Limpar configuração: `rm ~/.inventario/database-config.properties`
2. Reconfigurar via interface

### Erro: "Conexão recusada"

**Causa**: Banco de dados não está rodando ou configuração incorreta

**Solução**:
1. Verificar se PostgreSQL está rodando
2. Testar conexão via cliente (pgAdmin, psql)
3. Reconfigurar via interface

## 📝 Checklist Final

Após migração, verificar:

- [ ] Senhas removidas do código
- [ ] Arquivo de configuração criado
- [ ] Senha criptografada no arquivo
- [ ] Conexão funciona
- [ ] Aplicação inicia normalmente
- [ ] Testes passam
- [ ] Documentação atualizada
- [ ] .gitignore atualizado
- [ ] Equipe informada sobre mudança

## 📚 Referências

- `CRIPTOGRAFIA_SENHAS.md` - Detalhes técnicos da criptografia
- `CONFIGURACAO_BANCO_DADOS.md` - Como usar o sistema de configuração
- `README_CRIPTOGRAFIA.md` - Resumo da implementação

---

**Versão**: 1.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT
