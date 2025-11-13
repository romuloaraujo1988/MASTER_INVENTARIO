# Configuração do Banco de Dados

## Visão Geral

O sistema agora possui um **Dialog de Configuração de Banco de Dados** totalmente funcional que permite configurar a conexão com diferentes SGBDs sem precisar editar arquivos manualmente.

## Funcionalidades Implementadas

### ✅ Salvar e Carregar Configurações
- Configurações são salvas em `~/.inventario/database-config.properties`
- Configurações do SGBD em `~/.inventario/sgbd-config.properties`
- Carregamento automático na inicialização

### ✅ Suporte a Múltiplos SGBDs
- **PostgreSQL** (padrão) - Porta 5432
- **MySQL** - Porta 3306
- **SQL Server** - Porta 1433
- **Oracle** - Porta 1521

### ✅ Teste de Conexão
- Botão "Testar Conexão" valida as configurações antes de salvar
- Feedback visual com cores (verde = sucesso, vermelho = erro)
- Execução em background (não trava a interface)

### ✅ Integração com Sistema
- Atualiza `DatabaseConnection` automaticamente
- Reinicializa `ConnectionManager` com novas configurações
- Não requer reinicialização do aplicativo

### ✅ Segurança
- **Senha criptografada com AES-256-CBC**
- Chave derivada de informações da máquina (machine-specific)
- IV (Initialization Vector) aleatório para cada criptografia
- Opção de salvar ou não a senha
- Validação de campos obrigatórios
- Compatibilidade com senhas em texto plano (migração)

## Como Usar

### 1. Acessar Configurações

#### Na Tela de Login
- Clique no botão **"⚙ Configurar Banco"** no rodapé da tela

#### Programaticamente
```java
// Abrir dialog de configuração
ConfiguracaoBancoUtil.abrirDialogConfiguracao(parentFrame);

// Verificar se tem configuração válida
if (ConfiguracaoBancoUtil.temConfiguracaoValida()) {
    // Prosseguir
}

// Verificar ou configurar (abre dialog se necessário)
ConfiguracaoBancoUtil.verificarOuConfigurar(parentFrame);

// Mostrar informações da configuração
ConfiguracaoBancoUtil.mostrarInfoConfiguracao(parentFrame);
```

### 2. Configurar Conexão

1. **Selecione o SGBD** (PostgreSQL, MySQL, SQL Server, Oracle)
   - A porta padrão é atualizada automaticamente

2. **Preencha os campos:**
   - **Servidor**: hostname ou IP (ex: localhost, 192.168.1.100)
   - **Porta**: porta do banco (atualizada automaticamente por SGBD)
   - **Nome do Banco**: nome do database
   - **Usuário**: usuário do banco
   - **Senha**: senha do usuário

3. **Teste a Conexão**
   - Clique em "Testar Conexão"
   - Aguarde o resultado (verde = sucesso, vermelho = erro)

4. **Salve as Configurações**
   - Clique em "Salvar"
   - As configurações são aplicadas imediatamente

### 3. Limpar Configurações

- Clique em **"Limpar Config"** para remover todas as configurações salvas
- Confirme a ação
- Os campos voltam aos valores padrão

## Estrutura de Arquivos

### Arquivo de Configuração do Banco
**Localização**: `~/.inventario/database-config.properties`

```properties
# Database Configuration
host=localhost
port=5432
database=sispatrimonio
username=postgres
password=sua_senha_aqui
```

### Arquivo de Configuração do SGBD
**Localização**: `~/.inventario/sgbd-config.properties`

```properties
# SGBD Configuration
sgbd=PostgreSQL
salvar_senha=false
```

## Classes Envolvidas

### 1. ConfiguracaoBancoDialog
**Localização**: `com.inventario.view.ConfiguracaoBancoDialog`

Dialog Swing para configuração visual do banco de dados.

**Métodos principais:**
- `testarConexao()` - Testa conexão em background
- `salvarConfiguracao()` - Salva configurações
- `carregarConfiguracoes()` - Carrega configurações salvas
- `limparConfiguracoes()` - Remove configurações

### 2. ConfiguracaoBancoUtil
**Localização**: `com.inventario.util.ConfiguracaoBancoUtil`

Classe utilitária para facilitar acesso às configurações.

**Métodos principais:**
```java
// Abrir dialog
boolean confirmado = ConfiguracaoBancoUtil.abrirDialogConfiguracao(parent);

// Verificar configuração
boolean valida = ConfiguracaoBancoUtil.temConfiguracaoValida();

// Obter configuração atual
DatabaseConfig config = ConfiguracaoBancoUtil.getConfiguracaoAtual();

// Testar conexão
boolean conectado = ConfiguracaoBancoUtil.testarConexao();

// Obter informações
String info = ConfiguracaoBancoUtil.getInfoConfiguracao();

// Verificar ou configurar
boolean ok = ConfiguracaoBancoUtil.verificarOuConfigurar(parent);

// Recarregar configuração
ConfiguracaoBancoUtil.recarregarConfiguracao();
```

### 3. DatabaseConfigManager
**Localização**: `com.inventario.config.DatabaseConfigManager`

Gerenciador de baixo nível para persistência de configurações.

### 4. DatabaseConfig
**Localização**: `com.inventario.config.DatabaseConfig`

Modelo de dados para configuração do banco.

## Fluxo de Inicialização

```
1. Aplicação inicia
   ↓
2. DatabaseConnection carrega configuração
   ↓
3. Se configuração válida:
   - ConnectionManager.initialize()
   - Pool de conexões criado
   ↓
4. Se configuração inválida:
   - Usa fallback (localhost:5432/sispatrimonio)
   - Ou solicita configuração ao usuário
```

## Exemplos de Uso

### Exemplo 1: Verificar Configuração na Inicialização

```java
public class MinhaAplicacao {
    public static void main(String[] args) {
        // Verificar se tem configuração válida
        if (!ConfiguracaoBancoUtil.temConfiguracaoValida()) {
            JFrame frame = new JFrame();
            boolean configurado = ConfiguracaoBancoUtil.verificarOuConfigurar(frame);
            
            if (!configurado) {
                System.err.println("Aplicação não pode iniciar sem configuração de banco");
                System.exit(1);
            }
        }
        
        // Prosseguir com inicialização
        iniciarAplicacao();
    }
}
```

### Exemplo 2: Menu de Configurações

```java
JMenuItem menuConfig = new JMenuItem("Configurar Banco de Dados");
menuConfig.addActionListener(e -> {
    ConfiguracaoBancoUtil.abrirDialogConfiguracao(mainFrame);
});
```

### Exemplo 3: Verificar Status da Conexão

```java
JButton btnStatus = new JButton("Status da Conexão");
btnStatus.addActionListener(e -> {
    ConfiguracaoBancoUtil.mostrarInfoConfiguracao(mainFrame);
});
```

## Troubleshooting

### Problema: "Falha na conexão"

**Soluções:**
1. Verifique se o banco de dados está rodando
2. Confirme host e porta corretos
3. Valide usuário e senha
4. Verifique firewall/rede
5. Teste conexão direta via cliente do banco

### Problema: "Configuração não salva"

**Soluções:**
1. Verifique permissões da pasta `~/.inventario`
2. Confirme que todos os campos estão preenchidos
3. Teste a conexão antes de salvar

### Problema: "Aplicação usa configuração antiga"

**Soluções:**
1. Use `ConfiguracaoBancoUtil.recarregarConfiguracao()`
2. Reinicie a aplicação
3. Limpe as configurações e reconfigure

## Segurança

### 🔐 Criptografia de Senhas

O sistema utiliza **criptografia AES-256-CBC** para proteger as senhas salvas:

**Características:**
- **Algoritmo**: AES-256-CBC (Advanced Encryption Standard)
- **Chave**: Derivada usando PBKDF2 com 65.536 iterações
- **Base da Chave**: Informações únicas da máquina (user.name, os.name, os.arch, etc.)
- **IV**: Initialization Vector aleatório de 16 bytes para cada criptografia
- **Formato**: Base64(IV + CipherText)

**Segurança:**
- ✅ Mesma senha gera cifras diferentes a cada criptografia (devido ao IV aleatório)
- ✅ Senha só pode ser descriptografada na mesma máquina
- ✅ Não armazena chave em texto plano
- ✅ Resistente a ataques de força bruta (PBKDF2 com 65k iterações)

**Exemplo de senha criptografada:**
```properties
password=AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXp7fH1+fw==
```

### ⚠️ Importante

- **Machine-Specific**: A senha criptografada só funciona na máquina onde foi criada
- **Backup**: Ao fazer backup, a senha não funcionará em outra máquina
- **Migração**: Ao trocar de máquina, será necessário reconfigurar a senha
- **Compatibilidade**: O sistema detecta senhas em texto plano e as migra automaticamente

### Boas Práticas

1. **Desenvolvimento**: Pode salvar senha criptografada para agilizar
2. **Produção**: Ainda é recomendado não salvar senha, mas se necessário, ela estará criptografada
3. **Compartilhamento**: Nunca compartilhe arquivos de configuração (mesmo criptografados)
4. **Backup**: Senhas criptografadas não funcionarão em outras máquinas

## Próximas Melhorias

- [ ] Criptografia de senhas salvas
- [ ] Suporte a variáveis de ambiente
- [ ] Múltiplos perfis de conexão (dev, homolog, prod)
- [ ] Histórico de conexões
- [ ] Importar/exportar configurações (sem senha)
- [ ] Validação de conectividade na inicialização
- [ ] Retry automático em caso de falha

## Referências

- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [MySQL Connector/J](https://dev.mysql.com/doc/connector-j/en/)
- [SQL Server JDBC Driver](https://docs.microsoft.com/en-us/sql/connect/jdbc/)
- [Oracle JDBC Driver](https://www.oracle.com/database/technologies/appdev/jdbc.html)

---

**Versão**: 1.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT
