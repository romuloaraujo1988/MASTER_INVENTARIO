# Plano de Simplificação da Configuração

## 🎯 Objetivo
Simplificar drasticamente a configuração do sistema, tornando-a mais intuitiva, centralizada e fácil de gerenciar.

## 📊 Situação Atual (PROBLEMÁTICA)

### Arquivos de Configuração Espalhados
```
📁 Raiz do Projeto (100+ arquivos!)
├── configuracao_banco.json          # Config banco manual
├── config_postgresql.txt            # Config PostgreSQL
├── application.properties           # Spring Boot
├── application-mobile.properties    # Mobile API
├── application-mobile-test.properties
├── pom.xml                          # Maven
├── nb-configuration.xml             # NetBeans
├── .classpath                       # Eclipse
├── .project                         # Eclipse
└── 90+ arquivos de documentação/scripts
```

### Problemas Identificados

#### 1. **Múltiplos Arquivos de Configuração** ❌
- `configuracao_banco.json` - Config manual do banco
- `application.properties` - Config Spring Boot
- `application-mobile.properties` - Config API mobile
- `config_postgresql.txt` - Instruções PostgreSQL
- Sem hierarquia clara

#### 2. **Configuração Manual Complexa** ❌
```json
{
    "postgresql": {
        "host": "localhost",
        "database": "sispatrimonio",
        "user": "postgres",
        "password": "Romulo@2020",  // ⚠️ Senha hardcoded!
        "port": 5432
    }
}
```

#### 3. **Sem Wizard de Instalação** ❌
- Usuário precisa editar arquivos manualmente
- Risco de erros de sintaxe
- Difícil para não-técnicos

#### 4. **Dependências Não Gerenciadas** ❌
- JARs na pasta `lib/`
- Sem controle de versão claro
- Difícil atualização

#### 5. **Documentação Fragmentada** ❌
- 100+ arquivos na raiz
- Difícil encontrar informação
- Duplicação de conteúdo

## 🏗️ Solução Proposta

### Fase 1: Centralizar Configurações (2 dias)

#### 1.1 Criar Estrutura Unificada
```
📁 config/
├── application.yml                  # Config principal (YAML)
├── database.yml                     # Config banco
├── security.yml                     # Config segurança
├── mobile-api.yml                   # Config API mobile
└── README.md                        # Guia de configuração
```

#### 1.2 Migrar para YAML
**Antes (application.properties)**:
```properties
app.name=Sistema de Inventário
app.version=1.2.0
app.database.config.file=configuracao_banco.json
logging.file.name=logs/sistema-inventario.log
```

**Depois (application.yml)**:
```yaml
app:
  name: Sistema de Inventário
  version: 1.2.0
  database:
    config-file: config/database.yml
  logging:
    file: logs/sistema-inventario.log

spring:
  profiles:
    active: ${SPRING_PROFILE:desktop}
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sispatrimonio}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

#### 1.3 Variáveis de Ambiente
```bash
# .env (não commitado)
DB_URL=jdbc:postgresql://localhost:5432/sispatrimonio
DB_USER=postgres
DB_PASSWORD=sua_senha_aqui
SPRING_PROFILE=desktop
```

### Fase 2: Wizard de Configuração (3 dias)

#### 2.1 Tela de Primeiro Acesso
```java
public class ConfigurationWizard extends JFrame {
    
    private enum Step {
        WELCOME,           // Boas-vindas
        DATABASE,          // Config banco
        ADMIN_USER,        // Criar admin
        MOBILE_API,        // Config API (opcional)
        SUMMARY,           // Resumo
        FINISH             // Conclusão
    }
    
    // Wizard com navegação Next/Back
    // Validação em tempo real
    // Teste de conexão automático
    // Salva config automaticamente
}
```

**Fluxo do Wizard**:
```
┌─────────────────────────────────────────┐
│  1. Bem-vindo ao Sistema de Inventário  │
│                                         │
│  Este assistente irá configurar:       │
│  ✓ Conexão com banco de dados         │
│  ✓ Usuário administrador              │
│  ✓ API Mobile (opcional)              │
│                                         │
│              [Próximo >]                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  2. Configuração do Banco de Dados      │
│                                         │
│  Tipo: [PostgreSQL ▼]                  │
│  Host: [localhost        ]              │
│  Porta: [5432           ]              │
│  Banco: [sispatrimonio  ]              │
│  Usuário: [postgres     ]              │
│  Senha: [**********     ]              │
│                                         │
│  [Testar Conexão]  Status: ✓ OK        │
│                                         │
│  [< Voltar]              [Próximo >]    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  3. Usuário Administrador               │
│                                         │
│  Nome: [Administrador   ]              │
│  Login: [admin          ]              │
│  Email: [admin@ifmt.edu.br]            │
│  Senha: [**********     ]              │
│  Confirmar: [**********     ]          │
│                                         │
│  Força da senha: ████████░░ Forte      │
│                                         │
│  [< Voltar]              [Próximo >]    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  4. API Mobile (Opcional)               │
│                                         │
│  ☑ Habilitar API Mobile                │
│                                         │
│  Porta: [8080           ]              │
│  IP do Servidor: [192.168.1.100]       │
│                                         │
│  ☑ Iniciar automaticamente             │
│  ☐ Habilitar HTTPS                     │
│                                         │
│  [< Voltar]              [Próximo >]    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  5. Resumo da Configuração              │
│                                         │
│  Banco: PostgreSQL (localhost:5432)    │
│  Admin: admin                          │
│  API Mobile: Habilitada (porta 8080)   │
│                                         │
│  ☑ Criar tabelas automaticamente       │
│  ☑ Inserir dados de exemplo            │
│                                         │
│  [< Voltar]              [Concluir]     │
└─────────────────────────────────────────┘
```

#### 2.2 Validações Automáticas
```java
public class ConfigurationValidator {
    
    // Testa conexão com banco
    public ValidationResult testDatabaseConnection(DatabaseConfig config) {
        try {
            Connection conn = DriverManager.getConnection(
                config.getUrl(), 
                config.getUsername(), 
                config.getPassword()
            );
            conn.close();
            return ValidationResult.success("Conexão estabelecida com sucesso!");
        } catch (SQLException e) {
            return ValidationResult.error("Erro: " + e.getMessage());
        }
    }
    
    // Valida força da senha
    public ValidationResult validatePassword(String password) {
        if (password.length() < 8) {
            return ValidationResult.error("Senha deve ter no mínimo 8 caracteres");
        }
        // ... outras validações
        return ValidationResult.success("Senha forte");
    }
    
    // Verifica se porta está disponível
    public ValidationResult checkPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return ValidationResult.success("Porta " + port + " disponível");
        } catch (IOException e) {
            return ValidationResult.error("Porta " + port + " já está em uso");
        }
    }
}
```

#### 2.3 Criação Automática de Tabelas
```java
public class DatabaseInitializer {
    
    public void initializeDatabase(DatabaseConfig config) {
        // 1. Conectar ao banco
        // 2. Verificar se tabelas existem
        // 3. Executar scripts SQL automaticamente
        // 4. Inserir dados iniciais
        // 5. Criar usuário admin
        
        executeSqlScript("sql/criar_tabelas_sispatrimonio.sql");
        executeSqlScript("sql/criar_tabelas_sistema_inventario.sql");
        executeSqlScript("sql/criar_tabelas_mobile.sql");
        
        createAdminUser(adminConfig);
    }
}
```

### Fase 3: Gerenciador de Dependências (1 dia)

#### 3.1 Maven Completo
```xml
<!-- pom.xml - Todas as dependências gerenciadas -->
<dependencies>
    <!-- Não mais JARs na pasta lib/ -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.1</version>
    </dependency>
    <!-- ... todas as outras -->
</dependencies>

<build>
    <plugins>
        <!-- Plugin para criar JAR executável com todas as dependências -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.inventario.SistemaInventarioApplication</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### 3.2 Remover Pasta lib/
```bash
# Antes
lib/
├── postgresql-42.7.1.jar
├── poi-5.2.3.jar
├── itext-7.2.5.jar
└── ... 20+ JARs

# Depois
# Tudo gerenciado pelo Maven
# JARs baixados automaticamente
```

### Fase 4: Organizar Documentação (1 dia)

#### 4.1 Nova Estrutura
```
📁 Projeto
├── 📁 config/                    # Configurações
│   ├── application.yml
│   ├── database.yml
│   └── README.md
│
├── 📁 docs/                      # Documentação
│   ├── 📁 instalacao/
│   │   ├── GUIA_INSTALACAO.md
│   │   ├── REQUISITOS.md
│   │   └── FAQ.md
│   ├── 📁 usuario/
│   │   ├── MANUAL_USUARIO.md
│   │   └── TUTORIAIS.md
│   ├── 📁 desenvolvedor/
│   │   ├── ARQUITETURA.md
│   │   ├── API.md
│   │   └── CONTRIBUINDO.md
│   └── 📁 tecnica/
│       ├── BANCO_DADOS.md
│       ├── SEGURANCA.md
│       └── PERFORMANCE.md
│
├── 📁 sql/                       # Scripts SQL
├── 📁 src/                       # Código fonte
├── pom.xml                       # Maven
├── README.md                     # Início rápido
└── CHANGELOG.md                  # Histórico de versões
```

#### 4.2 Mover Arquivos
```bash
# Mover 90+ arquivos da raiz para docs/
mv ANALISE_*.md docs/tecnica/
mv GUIA_*.md docs/usuario/
mv IMPLEMENTACAO_*.md docs/desenvolvedor/
mv CORRECAO_*.md docs/tecnica/historico/
```

### Fase 5: Instalador Automático (2 dias)

#### 5.1 Script de Instalação Windows
```batch
@echo off
echo ========================================
echo  Sistema de Inventário - Instalador
echo ========================================
echo.

REM Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Java não encontrado!
    echo Por favor, instale Java 21 ou superior.
    pause
    exit /b 1
)

REM Verificar PostgreSQL
psql --version >nul 2>&1
if errorlevel 1 (
    echo [AVISO] PostgreSQL não encontrado.
    echo Você precisará instalá-lo manualmente.
)

REM Criar diretórios
mkdir config 2>nul
mkdir logs 2>nul
mkdir data 2>nul
mkdir backup 2>nul

REM Copiar arquivos de configuração padrão
if not exist config\application.yml (
    copy config-templates\application.yml config\
)

REM Executar wizard de configuração
echo.
echo Iniciando wizard de configuração...
java -jar sistema-inventario.jar --wizard

echo.
echo ========================================
echo  Instalação concluída!
echo ========================================
echo.
echo Para iniciar o sistema, execute:
echo   start-sistema.bat
echo.
pause
```

#### 5.2 Instalador Linux/Mac
```bash
#!/bin/bash

echo "========================================"
echo " Sistema de Inventário - Instalador"
echo "========================================"
echo

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java não encontrado!"
    echo "Por favor, instale Java 21 ou superior."
    exit 1
fi

# Verificar PostgreSQL
if ! command -v psql &> /dev/null; then
    echo "[AVISO] PostgreSQL não encontrado."
    echo "Você precisará instalá-lo manualmente."
fi

# Criar diretórios
mkdir -p config logs data backup

# Copiar configurações padrão
if [ ! -f config/application.yml ]; then
    cp config-templates/application.yml config/
fi

# Executar wizard
echo
echo "Iniciando wizard de configuração..."
java -jar sistema-inventario.jar --wizard

echo
echo "========================================"
echo " Instalação concluída!"
echo "========================================"
echo
echo "Para iniciar o sistema, execute:"
echo "  ./start-sistema.sh"
echo
```

## 📋 Implementação Detalhada

### Passo 1: Criar ConfigurationWizard (Dia 1)

```java
package com.inventario.config;

import javax.swing.*;
import java.awt.*;

public class ConfigurationWizard extends JDialog {
    
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    
    private DatabaseConfigPanel databasePanel;
    private AdminUserPanel adminPanel;
    private MobileApiPanel mobilePanel;
    private SummaryPanel summaryPanel;
    
    private ConfigurationData configData;
    
    public ConfigurationWizard(Frame parent) {
        super(parent, "Assistente de Configuração", true);
        this.configData = new ConfigurationData();
        initComponents();
    }
    
    private void initComponents() {
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Painel de cards
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        
        // Adicionar painéis
        cardsPanel.add(new WelcomePanel(this), "WELCOME");
        cardsPanel.add(databasePanel = new DatabaseConfigPanel(this), "DATABASE");
        cardsPanel.add(adminPanel = new AdminUserPanel(this), "ADMIN");
        cardsPanel.add(mobilePanel = new MobileApiPanel(this), "MOBILE");
        cardsPanel.add(summaryPanel = new SummaryPanel(this), "SUMMARY");
        
        add(cardsPanel, BorderLayout.CENTER);
        
        // Painel de navegação
        add(createNavigationPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnBack = new JButton("< Voltar");
        JButton btnNext = new JButton("Próximo >");
        JButton btnCancel = new JButton("Cancelar");
        
        btnBack.addActionListener(e -> previousStep());
        btnNext.addActionListener(e -> nextStep());
        btnCancel.addActionListener(e -> cancel());
        
        panel.add(btnCancel);
        panel.add(btnBack);
        panel.add(btnNext);
        
        return panel;
    }
    
    public void nextStep() {
        // Validar step atual
        if (!validateCurrentStep()) {
            return;
        }
        
        // Avançar para próximo step
        cardLayout.next(cardsPanel);
    }
    
    public void previousStep() {
        cardLayout.previous(cardsPanel);
    }
    
    private boolean validateCurrentStep() {
        // Implementar validação
        return true;
    }
    
    public void finish() {
        try {
            // Salvar configurações
            ConfigurationManager.save(configData);
            
            // Inicializar banco de dados
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize(configData.getDatabaseConfig());
            
            // Criar usuário admin
            UserService userService = new UserService();
            userService.createAdminUser(configData.getAdminConfig());
            
            JOptionPane.showMessageDialog(this,
                "Configuração concluída com sucesso!\n" +
                "O sistema será iniciado agora.",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar configuração: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public ConfigurationData getConfigData() {
        return configData;
    }
}
```

### Passo 2: Migrar para YAML (Dia 2)

```yaml
# config/application.yml
app:
  name: Sistema de Inventário
  version: 1.2.0
  vendor: IFMT
  
  ui:
    look-and-feel: system
    theme: default
    splash:
      enabled: true
      duration: 2000
  
  database:
    connection:
      timeout: 30000
      retry-attempts: 3
  
  offline:
    enabled: true
    database-file: data/inventario_offline.db
    sync-interval: 300000
  
  security:
    session-timeout: 3600000
    password:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-number: true
      require-special: false
    login:
      max-attempts: 3
      lockout-duration: 900000
  
  backup:
    enabled: true
    interval: 86400000
    retention-days: 30
    path: backup/
  
  performance:
    cache:
      enabled: true
      size: 1000
    thread-pool-size: 10

spring:
  application:
    name: inventario-api
  
  profiles:
    active: ${SPRING_PROFILE:desktop}
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sispatrimonio}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  security:
    user:
      name: ${ADMIN_USER:admin}
      password: ${ADMIN_PASSWORD:admin}
      roles: ADMIN

logging:
  file:
    name: logs/sistema-inventario.log
    max-size: 10MB
    max-history: 30
  level:
    root: INFO
    com.inventario: DEBUG
    org.springframework: INFO
```

### Passo 3: Organizar Documentação (Dia 3)

```bash
# Script para reorganizar
#!/bin/bash

# Criar estrutura
mkdir -p docs/{instalacao,usuario,desenvolvedor,tecnica/historico}

# Mover arquivos
mv ANALISE_*.md docs/tecnica/
mv GUIA_*.md docs/usuario/
mv IMPLEMENTACAO_*.md docs/desenvolvedor/
mv CORRECAO_*.md docs/tecnica/historico/
mv RESUMO_*.md docs/tecnica/historico/
mv SOLUCAO_*.md docs/tecnica/historico/

# Criar índice
cat > docs/README.md << 'EOF'
# Documentação do Sistema de Inventário

## 📚 Índice

### Para Usuários
- [Manual do Usuário](usuario/MANUAL_USUARIO.md)
- [Guia de Instalação](instalacao/GUIA_INSTALACAO.md)
- [F2).
e Fas (rdo Wizameçando pelmente, coar imediatalementdação**: Impmeneco**

**R do usuáriosfaçãode sati*+200% o**
- *figuraçã con erros de
- **-95% den → 5min)miação** (30nstalempo de i t
- **-80% de10)* (100+ → <s na raiz*arquivo% de 90

- **-do sistema:para adoção CA**  é **CRÍTIcação simplifistaão

Eclus
## 🎓 Con
ostic JARs críuir
- Inclrosileir bracal
- Mirro- Cache loção**:
tigalentas
**Micias Maven  DependênRisco 3:

### ionalvançado opcdo agentes
- Moeli padrão intValores
- ios reais usuár- Testes comgação**:
s
**Mitilexo demai compzardsco 2: Wi

### Rimigrarantes de p ica
- Backução automátrago
- Migrmato antiidade com fo compatibiler: 
- Mantigação**it
**Mistentesgurações exonfi Quebrar c Risco 1:

###tigaçõesos e Misc|

## 🚨 Ri Survey 0 | 9/10 |4/1lação | tatisfação ins Sa
|suários |k uacFeedb0% | <5% | ção | ~5figura de con` |
| Erros -ls \| wc10 | `l100+ | <iz | ravos na |
| Arquitro n | Cronôme min | 5 milação | 30de instaTempo ----|
| --|--------|----------|-|---------ir |
edo Mometa | Cs | Mrica | Ante

| Métssoe Sucecas d📈 Métri

## nos errosda
- ✅ Me policia ✅ Experiên
-tomáticonstalador auderno
- ✅ I ✅ Wizard momo
-sionalisProfis### 

alização Fácil atu
- ✅nciadasereias gêncnd
- ✅ Depeo organizadantaçãDocumeada
- ✅ raliz centãoaçgur✅ Confiidade
- bilteni## Manu

# automáticaãodaçVali ✅ quivos
-ão de ar✅ Sem ediçual)
- s mano guiada (vonfiguraçã
- ✅ Cs) 30 minutos (vstoem 5 minunstalação so
- ✅ I Ucilidade deos

### FaEsperad Benefícios  |

## 🎯* | | **9 dias*OTAL** | **T|
| 🟡 MÉDIA ias | 2 dautomático |talador  5 | Ins |
|IXA | 🟢 BAdiao | 1 documentaçãOrganizar | DIA |
| 4 MÉ 1 dia | 🟡 dências |de depennciador 
| 3 | GereALTA | dias | 🔴  3ção |configurazard de 
| 2 | Wi | 🔴 ALTA || 2 diasigurações  confralizar
| 1 | Cent----|-----------|-----|------|-------
|-----|rioridade ração | Pde | Duse | Atividama

| Fa Cronogra
## 📊```
OF
NCE.md)
EMA/PERFORce](tecnicaan[Perform- 
CA.md)RANca/SEGU](tecni [Segurança.md)
-O_DADOSNCnica/BAec](toso de Dad [Banca
-écnicumentação TDoc

### .md)UINDONTRIBolvedor/COsenvndo](de [Contribui)
-vedor/API.mddesenvolI]( [AP.md)
-ITETURAr/ARQUnvolvedotetura](deserqui
- [Anvolvedores Para Dese.md)

###ao/FAQAQ](instalac