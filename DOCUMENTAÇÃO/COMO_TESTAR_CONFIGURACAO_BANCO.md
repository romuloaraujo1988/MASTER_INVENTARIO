# Como Testar a Configuração do Banco de Dados

## Teste Manual via Interface

### 1. Executar a Aplicação

```bash
mvn clean compile exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"
```

### 2. Na Tela de Login

1. Procure o botão **"⚙ Configurar Banco"** no rodapé inferior direito
2. Clique no botão
3. O dialog de configuração será aberto

### 3. Testar Funcionalidades

#### Teste 1: Configurar PostgreSQL (Padrão)
1. SGBD: PostgreSQL
2. Servidor: localhost
3. Porta: 5432
4. Nome do Banco: sispatrimonio
5. Usuário: postgres
6. Senha: sua_senha
7. Clique em "Testar Conexão"
8. Aguarde resultado (verde = sucesso)
9. Clique em "Salvar"

#### Teste 2: Configurar MySQL
1. SGBD: MySQL
2. Servidor: localhost
3. Porta: 3306 (atualizada automaticamente)
4. Nome do Banco: inventario
5. Usuário: root
6. Senha: sua_senha
7. Clique em "Testar Conexão"
8. Clique em "Salvar"

#### Teste 3: Limpar Configurações
1. Clique em "Limpar Config"
2. Confirme a ação
3. Verifique que os campos voltaram aos valores padrão

## Teste Programático

### Executar Classe de Teste

```bash
mvn clean compile exec:java -Dexec.mainClass="com.inventario.view.ConfiguracaoBancoDialogTest"
```

### Funcionalidades do Teste

A classe de teste fornece 5 botões:

1. **Abrir Configuração**: Abre o dialog de configuração
2. **Verificar Configuração**: Verifica se existe configuração válida
3. **Mostrar Informações**: Exibe informações da configuração atual
4. **Testar Conexão**: Testa a conexão com o banco
5. **Verificar ou Configurar**: Verifica e abre dialog se necessário

## Teste via Código

### Exemplo 1: Teste Básico

```java
import com.inventario.util.ConfiguracaoBancoUtil;
import javax.swing.JFrame;

public class TesteConfiguracao {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        
        // Verificar se tem configuração
        if (ConfiguracaoBancoUtil.temConfiguracaoValida()) {
            System.out.println("✓ Configuração válida encontrada");
            System.out.println(ConfiguracaoBancoUtil.getInfoConfiguracao());
            
            // Testar conexão
            if (ConfiguracaoBancoUtil.testarConexao()) {
                System.out.println("✓ Conexão bem-sucedida");
            } else {
                System.out.println("✗ Falha na conexão");
            }
        } else {
            System.out.println("✗ Nenhuma configuração encontrada");
            
            // Abrir dialog para configurar
            ConfiguracaoBancoUtil.abrirDialogConfiguracao(frame);
        }
    }
}
```

### Exemplo 2: Teste de Integração

```java
import com.inventario.config.DatabaseConfig;
import com.inventario.config.DatabaseConfigManager;
import com.inventario.util.DatabaseConnection;

public class TesteIntegracao {
    public static void main(String[] args) {
        DatabaseConfigManager manager = new DatabaseConfigManager();
        
        // Criar configuração de teste
        DatabaseConfig config = new DatabaseConfig(
            "localhost",
            5432,
            "sispatrimonio",
            "postgres",
            "senha123"
        );
        
        // Testar conexão
        if (manager.testConnection(config)) {
            System.out.println("✓ Teste de conexão bem-sucedido");
            
            // Salvar configuração
            if (manager.saveConfiguration(config)) {
                System.out.println("✓ Configuração salva");
                
                // Atualizar DatabaseConnection
                DatabaseConnection.updateConfig(config);
                System.out.println("✓ DatabaseConnection atualizado");
            }
        } else {
            System.out.println("✗ Falha no teste de conexão");
        }
    }
}
```

## Verificar Arquivos de Configuração

### Windows

```cmd
cd %USERPROFILE%\.inventario
dir
type database-config.properties
type sgbd-config.properties
```

### Linux/Mac

```bash
cd ~/.inventario
ls -la
cat database-config.properties
cat sgbd-config.properties
```

## Checklist de Testes

### Funcionalidades Básicas
- [ ] Abrir dialog de configuração
- [ ] Preencher todos os campos
- [ ] Testar conexão com sucesso
- [ ] Salvar configuração
- [ ] Fechar e reabrir (verificar se carregou)
- [ ] Limpar configurações

### Validações
- [ ] Campos obrigatórios vazios (deve mostrar erro)
- [ ] Porta inválida (deve mostrar erro)
- [ ] Conexão falha (deve mostrar erro vermelho)
- [ ] Conexão sucesso (deve mostrar verde)

### Múltiplos SGBDs
- [ ] PostgreSQL (porta 5432)
- [ ] MySQL (porta 3306)
- [ ] SQL Server (porta 1433)
- [ ] Oracle (porta 1521)
- [ ] Troca de SGBD atualiza porta automaticamente

### Integração
- [ ] Configuração salva em arquivo
- [ ] Configuração carregada na inicialização
- [ ] DatabaseConnection atualizado
- [ ] ConnectionManager reinicializado
- [ ] Aplicação funciona sem reiniciar

### Segurança
- [ ] Opção "Salvar senha" funciona
- [ ] Senha não salva quando desmarcado
- [ ] Senha carregada quando marcado

## Troubleshooting

### Erro: "Driver não encontrado"

**Solução**: Verifique se o driver JDBC está no classpath

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

### Erro: "Permissão negada ao criar arquivo"

**Solução**: Verifique permissões da pasta `~/.inventario`

```bash
# Linux/Mac
chmod 755 ~/.inventario

# Windows
# Verificar permissões da pasta no Explorer
```

### Erro: "Configuração não carrega"

**Solução**: Verifique se o arquivo existe e está válido

```bash
# Verificar conteúdo
cat ~/.inventario/database-config.properties

# Recriar se necessário
rm ~/.inventario/database-config.properties
# Abrir aplicação e configurar novamente
```

## Logs de Debug

Para ativar logs de debug, adicione ao início da aplicação:

```java
System.setProperty("inventario.debug", "true");
```

Ou via linha de comando:

```bash
java -Dinventario.debug=true -jar sistema-inventario.jar
```

## Resultados Esperados

### Sucesso
```
✓ Connection pool inicializado com sucesso
✓ Configuração de banco carregada com sucesso
✓ Teste de conexão bem-sucedido com: localhost
✓ Configuração de banco salva com sucesso
```

### Falha
```
✗ Erro ao conectar com o banco de dados: Connection refused
✗ Falha no teste de conexão: FATAL: password authentication failed
✗ Erro ao salvar configuração de banco: Permission denied
```

## Próximos Passos

Após testar com sucesso:

1. Configure o banco de produção
2. Teste todas as funcionalidades do sistema
3. Verifique logs de conexão
4. Monitore performance do pool
5. Configure backup das configurações

---

**Versão**: 1.0.0  
**Data**: 12/11/2025
