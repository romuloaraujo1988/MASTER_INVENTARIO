# Sistema de Inventário - Aplicação Desktop

## 📦 Thin JAR Build

Este projeto usa **thin-jar** para a aplicação desktop, que gera:
- JAR pequeno (~5-10 MB) com apenas o código da aplicação
- Pasta `lib/` com todas as dependências (174 arquivos)
- Startup mais rápido e builds incrementais eficientes

## 🚀 Como Compilar

### Windows
```bash
build-desktop.bat
```

### Linux/Mac
```bash
chmod +x build-desktop.sh
./build-desktop.sh
```

### Manual
```bash
mvnw clean package -P thin-jar -DskipTests
```

## ▶️ Como Executar

### Windows
```bash
run-desktop.bat
```

### Linux/Mac
```bash
chmod +x run-desktop.sh
./run-desktop.sh
```

### Manual
```bash
cd target/mobile-server
java -jar sistema-inventario-2.0.0.jar
```

## 📁 Estrutura Gerada

```
target/
├── mobile-server/
│   ├── sistema-inventario-2.0.0.jar    # JAR principal (~5-10 MB)
│   ├── application.properties          # Configurações
│   ├── start-mobile-server.bat         # Script servidor mobile
│   └── README.txt
└── lib/                                 # Dependências (174 arquivos)
    ├── spring-boot-*.jar
    ├── postgresql-*.jar
    ├── poi-*.jar
    └── ...
```

## 🔧 Requisitos

- **Java 21** ou superior
- **Maven** (wrapper incluído: `mvnw.cmd` / `mvnw`)
- **PostgreSQL** configurado e rodando

## 📊 Comparação: Thin JAR vs Fat JAR

| Característica | Thin JAR | Fat JAR |
|----------------|----------|---------|
| Tamanho do JAR | ~5-10 MB | ~150-200 MB |
| Build Time | Rápido (incremental) | Lento (sempre completo) |
| Startup | Rápido | Normal |
| Distribuição | JAR + lib/ | Apenas JAR |
| Ideal para | Desenvolvimento | Produção |

## 🎯 Profiles Maven Disponíveis

### 1. thin-jar (Desktop - Desenvolvimento)
```bash
mvnw clean package -P thin-jar -DskipTests
```
- JAR pequeno + lib/
- Ideal para desenvolvimento
- Build incremental rápido

### 2. fat-jar (Desktop - Produção)
```bash
mvnw clean package -P fat-jar -DskipTests
```
- JAR único com tudo incluído
- Ideal para distribuição
- Mais fácil de distribuir

### 3. mobile (Servidor Mobile API)
```bash
mvnw clean package -P mobile -DskipTests
```
- Servidor REST para app Android
- Porta 8080/8081
- JWT authentication

### 4. producao (Pacote Completo)
```bash
mvnw clean package -P producao -DskipTests
```
- Pacote ZIP completo
- Scripts de instalação
- Documentação incluída

## 🔍 Verificar Build

### Verificar JAR criado
```bash
# Windows
dir target\mobile-server\sistema-inventario-2.0.0.jar

# Linux/Mac
ls -lh target/mobile-server/sistema-inventario-2.0.0.jar
```

### Verificar dependências
```bash
# Windows
dir target\lib | find /c ".jar"

# Linux/Mac
ls target/lib/*.jar | wc -l
```

### Testar execução
```bash
java -jar target/mobile-server/sistema-inventario-2.0.0.jar --version
```

## 🐛 Troubleshooting

### Erro: "Java não encontrado"
**Solução:** Instale Java 21 ou configure JAVA_HOME
```bash
# Verificar versão
java -version

# Deve mostrar: openjdk version "21.x.x"
```

### Erro: "JAR não encontrado"
**Solução:** Execute o build primeiro
```bash
mvnw clean package -P thin-jar -DskipTests
```

### Erro: "lib/ não encontrado"
**Solução:** O profile thin-jar copia dependências automaticamente
```bash
# Verificar se lib/ existe
dir target\lib
```

### Erro: "Classe principal não encontrada"
**Solução:** Verifique o MANIFEST.MF
```bash
# Windows
jar xf target\mobile-server\sistema-inventario-2.0.0.jar META-INF\MANIFEST.MF
type META-INF\MANIFEST.MF

# Deve conter:
# Main-Class: com.inventario.SistemaInventarioApplication
# Class-Path: lib/spring-boot-*.jar lib/...
```

## 📝 Configuração

### application.properties
Localizado em: `target/mobile-server/application.properties`

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.username=inventario
spring.datasource.password=sua_senha

# Logs
logging.level.root=INFO
logging.level.com.inventario=DEBUG
```

## 🎨 Customização

### Alterar porta do servidor (se usar modo web)
```properties
server.port=8080
```

### Alterar pool de conexões
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### Habilitar cache
```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m
```

## 📚 Documentação Adicional

- [GUIA_RAPIDO.md](GUIA_RAPIDO.md) - Guia rápido de uso
- [MANUAL_DO_USUARIO.md](DOCUMENTAÇÃO/MANUAL_DO_USUARIO.md) - Manual completo
- [tech.md](.kiro/steering/tech.md) - Stack tecnológica

## 🆘 Suporte

Em caso de problemas:
1. Verifique os logs em `logs/sistema-inventario.log`
2. Consulte a documentação técnica
3. Entre em contato com a equipe de desenvolvimento

---

**Versão:** 2.0.0  
**Última atualização:** 21/11/2025  
**Build:** Thin JAR (Desktop)
