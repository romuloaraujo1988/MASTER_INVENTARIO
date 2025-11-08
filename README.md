# SIHCP - Sistema de Histórico e Coleta Patrimonial

Sistema de gestão de histórico e coleta patrimonial desenvolvido para o Instituto Federal de Mato Grosso (IFMT). O SIHCP permite o controle e acompanhamento de bens patrimoniais, facilitando os processos de inventário e gestão de ativos da instituição através do histórico de coletas e relatórios especializados.

## Funcionalidades

- **Gestão de Usuários**: Cadastro e controle de acesso de usuários do sistema
- **Gestão de Patrimônio**: Cadastro, edição e consulta de bens patrimoniais
- **Coleta de Inventário**: Processo de verificação e atualização do inventário
- **Relatórios**: Geração de relatórios detalhados sobre o patrimônio
- **Importação SUAP**: Integração com dados do sistema SUAP
- **Dashboard**: Painel de controle com indicadores e estatísticas

## Tecnologias Utilizadas

- **Java 21**: Linguagem de programação principal
- **Swing**: Framework nativo para interface gráfica
- **PostgreSQL**: Sistema de gerenciamento de banco de dados
- **Hibernate**: Framework ORM para persistência de dados
- **Spring Data JPA**: Abstração para acesso a dados
- **Jackson**: Biblioteca para processamento JSON
- **SLF4J + Logback**: Sistema de logging

## Pré-requisitos

### 1. Java Development Kit (JDK) 21
- Baixe e instale o OpenJDK 21 de: https://adoptium.net/
- Verifique a instalação: `java --version`
- Swing já está incluído no JDK

### 2. PostgreSQL
- Instale o PostgreSQL 12+ de: https://www.postgresql.org/download/
- Crie um banco de dados para o sistema (ex: `sispatrimonio`)

### 3. Dependências JAR
Baixe e coloque na pasta `lib/` os seguintes JARs:
- `postgresql-42.7.2.jar` (Driver PostgreSQL)
- `hibernate-core-6.4.1.Final.jar` (Hibernate ORM)
- `hibernate-entitymanager-5.6.15.Final.jar` (Hibernate Entity Manager)
- `spring-data-jpa-3.2.1.jar` (Spring Data JPA)
- `spring-context-6.1.2.jar` (Spring Context)
- `spring-orm-6.1.2.jar` (Spring ORM)
- `spring-tx-6.1.2.jar` (Spring Transactions)
- `jackson-databind-2.16.1.jar` (Jackson JSON)
- `jackson-core-2.16.1.jar` (Jackson Core)
- `jackson-annotations-2.16.1.jar` (Jackson Annotations)
- `slf4j-api-2.0.9.jar` (SLF4J API)
- `logback-classic-1.4.14.jar` (Logback)
- `logback-core-1.4.14.jar` (Logback Core)
- `jakarta.validation-api-3.0.2.jar` (Bean Validation)
- `hibernate-validator-8.0.1.Final.jar` (Hibernate Validator)
- `commons-lang3-3.14.0.jar` (Apache Commons Lang)
- `spring-security-crypto-6.2.1.jar` (Spring Security Crypto)
- E suas dependências transitivas

## Instalação

### Opção 1: Usando Maven (Recomendado)

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd MASTER_INVENTÁRIO

# Compilar e executar
mvn clean compile exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"
```

### Opção 2: Compilação Manual

1. Certifique-se de ter todas as dependências na pasta `lib/`
2. Execute o script `run.bat` (Windows) ou compile manualmente:

```bash
# Compilar
javac -cp "lib/*" -d target/classes src/main/java/com/inventario/*.java src/main/java/com/inventario/config/*.java src/main/java/com/inventario/ui/*.java

# Executar
java -cp "target/classes;lib/*" com.inventario.SistemaInventarioApplication
```

### Opção 3: IDE (Eclipse/IntelliJ)

1. Importe o projeto como "Existing Maven Project"
2. Configure o JDK 21 no projeto
3. Adicione as dependências JavaFX ao classpath
4. Execute a classe `SistemaInventarioApplication`

## Configuração Inicial

### 1. Primeira Execução
Na primeira execução, o sistema exibirá a tela de configuração do banco de dados:

- **Host**: Endereço do servidor PostgreSQL (ex: `localhost`)
- **Porta**: Porta do PostgreSQL (padrão: `5432`)
- **Banco**: Nome do banco de dados (ex: `sispatrimonio`)
- **Usuário**: Usuário do PostgreSQL
- **Senha**: Senha do usuário
- **Schema**: Schema do banco (padrão: `public`)
- **SSL**: Marque se usar conexão SSL

### 2. Teste de Conexão
- Clique em "Testar Conexão" para verificar os dados
- Se a conexão for bem-sucedida, clique em "Salvar"
- O sistema salvará a configuração em `~/.inventario/database-config.json`

### 3. Tela Principal
Após a configuração, o sistema exibirá a tela principal com os módulos:
- Gerenciar Usuários
- Gerenciar Patrimônio
- Coletar Inventário
- Relatórios
- Importar Dados SUAP
- Dashboard

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/inventario/
│   │       ├── SistemaInventarioApplication.java    # Classe principal
│   │       ├── config/
│   │       │   ├── DatabaseConfig.java              # Configuração do banco
│   │       │   └── DatabaseConfigManager.java       # Gerenciador de config
│   │       └── ui/
│   │           └── ConfiguracaoBancoController.java # Controller da config
│   └── resources/
│       └── fxml/
│           ├── configuracao-banco.fxml              # Tela de configuração
│           └── main.fxml                            # Tela principal
├── target/                                          # Arquivos compilados
├── lib/                                             # Dependências JAR
├── pom.xml                                          # Configuração Maven
├── run.bat                                          # Script Windows
└── README.md                                        # Este arquivo
```

## Configuração de Desenvolvimento

### Banco de Dados
O sistema criará automaticamente as tabelas necessárias no primeiro acesso.

### Logs
Os logs são salvos em:
- Windows: `%USERPROFILE%\.inventario\logs\`
- Linux/Mac: `~/.inventario/logs/`

### Configurações
As configurações são salvas em:
- Windows: `%USERPROFILE%\.inventario\database-config.json`
- Linux/Mac: `~/.inventario/database-config.json`

## Solução de Problemas

### Erro: "JavaFX runtime components are missing"
- Certifique-se de ter o JavaFX SDK na pasta `lib/`
- Verifique se está usando o comando correto com `--module-path`

### Erro de Conexão com Banco
- Verifique se o PostgreSQL está rodando
- Confirme host, porta, usuário e senha
- Teste a conexão manualmente com `psql` ou pgAdmin

### Erro de Compilação
- Verifique se está usando Java 21
- Confirme se todas as dependências estão na pasta `lib/`

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## Suporte

Para suporte técnico ou dúvidas:
- Abra uma issue no repositório
- Entre em contato com a equipe de desenvolvimento

---

**SIHCP - Sistema de Histórico e Coleta Patrimonial v1.0.0**  
*Instituto Federal de Mato Grosso*