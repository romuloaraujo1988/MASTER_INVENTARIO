# Guia de Deploy em Produção - SIHCP Modular

**Versão:** 2.7.0  
**Arquitetura:** Multi-módulo Maven  
**Data:** 01/05/2026

---

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Build para Produção](#build-para-produção)
3. [Deploy do Desktop](#deploy-do-desktop)
4. [Deploy do Server](#deploy-do-server)
5. [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
6. [Monitoramento](#monitoramento)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 Pré-requisitos

### Servidor/Cliente

- **Java:** JDK 17 ou superior
- **PostgreSQL:** 12 ou superior
- **Memória RAM:** Mínimo 2 GB (recomendado 4 GB)
- **Disco:** Mínimo 500 MB livres

### Desenvolvimento

- **Maven:** 3.6+ (ou usar mvnw incluído)
- **Git:** Para controle de versão

---

## 🚀 Build para Produção

### Opção 1: Build Completo (Desktop + Server)

#### Windows
```powershell
.\build-producao-modular.ps1
```

#### Linux/Mac
```bash
chmod +x build-producao-modular.sh
./build-producao-modular.sh
```

### Opção 2: Build Seletivo

#### Apenas Desktop
```powershell
# Windows
.\build-producao-modular.ps1 -Desktop

# Linux/Mac
./build-producao-modular.sh --desktop
```

#### Apenas Server
```powershell
# Windows
.\build-producao-modular.ps1 -Server

# Linux/Mac
./build-producao-modular.sh --server
```

### Opção 3: Build Manual

```bash
# Build completo
mvn clean package

# Build sem testes (mais rápido)
mvn clean package -DskipTests

# Build apenas um módulo
cd sihcp-server
mvn clean package
```

### Resultado do Build

Após o build, os artefatos estarão em:

```
dist/sihcp-YYYYMMDD_HHMMSS/
├── desktop/
│   ├── sihcp-desktop-2.7.0.jar
│   ├── lib/
│   ├── iniciar-desktop.bat (Windows)
│   ├── iniciar-desktop.sh (Linux/Mac)
│   └── README.md
│
└── server/
    ├── sihcp-server-2.7.0.jar
    ├── application.properties
    ├── application-mobile.properties
    ├── iniciar-server.bat (Windows)
    ├── iniciar-server.sh (Linux/Mac)
    ├── sihcp-server.service (systemd)
    └── README.md
```

---

## 🖥️ Deploy do Desktop

### 1. Preparação

```bash
# Extrair o pacote de distribuição
unzip sihcp-YYYYMMDD_HHMMSS.zip
# ou
tar -xzf sihcp-YYYYMMDD_HHMMSS.tar.gz

# Navegar para o diretório desktop
cd desktop
```

### 2. Configurar Banco de Dados

Criar arquivo `~/configuracao_banco.json`:

```json
{
  "host": "localhost",
  "port": 5432,
  "database": "sispatrimonio",
  "username": "inventario",
  "password": "sua_senha_aqui",
  "schema": "public"
}
```

**Localização do arquivo:**
- **Windows:** `C:\Users\SeuUsuario\configuracao_banco.json`
- **Linux/Mac:** `~/configuracao_banco.json`

### 3. Executar

#### Windows
```cmd
iniciar-desktop.bat
```

#### Linux/Mac
```bash
chmod +x iniciar-desktop.sh
./iniciar-desktop.sh
```

#### Manual
```bash
java -Xms512m -Xmx2g -jar sihcp-desktop-2.7.0.jar
```

### 4. Verificação

- A interface gráfica Swing deve abrir
- Tela de login deve aparecer
- Verificar conexão com banco de dados

---

## 🌐 Deploy do Server

### Opção 1: Execução Direta (Desenvolvimento/Teste)

```bash
cd server

# Linux/Mac
chmod +x iniciar-server.sh
./iniciar-server.sh

# Windows
iniciar-server.bat

# Manual
java -Xms512m -Xmx2g -XX:+UseG1GC \
  -jar sihcp-server-2.7.0.jar \
  --spring.profiles.active=mobile \
  --server.port=8081
```

### Opção 2: Instalação como Serviço (Produção - Linux)

#### 1. Copiar arquivos

```bash
# Criar diretório
sudo mkdir -p /opt/sihcp-server

# Copiar JAR
sudo cp sihcp-server-2.7.0.jar /opt/sihcp-server/

# Copiar configurações
sudo cp application*.properties /opt/sihcp-server/

# Ajustar permissões
sudo chown -R root:root /opt/sihcp-server
sudo chmod 755 /opt/sihcp-server
sudo chmod 644 /opt/sihcp-server/*.jar
sudo chmod 644 /opt/sihcp-server/*.properties
```

#### 2. Criar usuário do sistema

```bash
sudo useradd -r -s /bin/false sihcp
```

#### 3. Configurar systemd

```bash
# Copiar service file
sudo cp sihcp-server.service /etc/systemd/system/

# Recarregar systemd
sudo systemctl daemon-reload

# Habilitar para iniciar no boot
sudo systemctl enable sihcp-server

# Iniciar serviço
sudo systemctl start sihcp-server

# Verificar status
sudo systemctl status sihcp-server
```

#### 4. Comandos úteis

```bash
# Parar serviço
sudo systemctl stop sihcp-server

# Reiniciar serviço
sudo systemctl restart sihcp-server

# Ver logs
sudo journalctl -u sihcp-server -f

# Ver logs das últimas 100 linhas
sudo journalctl -u sihcp-server -n 100
```

### Opção 3: Docker (Opcional)

Criar `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY sihcp-server-2.7.0.jar app.jar
COPY application*.properties ./

EXPOSE 8081

ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
ENV SPRING_OPTS="--spring.profiles.active=mobile --server.port=8081"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar $SPRING_OPTS"]
```

Build e execução:

```bash
# Build da imagem
docker build -t sihcp-server:2.7.0 .

# Executar container
docker run -d \
  --name sihcp-server \
  -p 8081:8081 \
  -v ~/configuracao_banco.json:/root/configuracao_banco.json:ro \
  --restart unless-stopped \
  sihcp-server:2.7.0

# Ver logs
docker logs -f sihcp-server
```

### Verificação do Server

```bash
# Health check
curl http://localhost:8081/api/mobile/health

# Swagger UI
# Abrir no navegador: http://localhost:8081/swagger-ui.html

# API Docs
curl http://localhost:8081/v3/api-docs
```

**Resposta esperada do health check:**
```json
{
  "status": "UP",
  "timestamp": "2026-05-01T10:30:00Z"
}
```

---

## 🗄️ Configuração do Banco de Dados

### 1. Criar Banco de Dados

```sql
-- Conectar como postgres
psql -U postgres

-- Criar usuário
CREATE USER inventario WITH PASSWORD 'senha_segura_aqui';

-- Criar banco
CREATE DATABASE sispatrimonio OWNER inventario;

-- Conceder privilégios
GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario;

-- Conectar ao banco
\c sispatrimonio

-- Conceder privilégios no schema public
GRANT ALL ON SCHEMA public TO inventario;
```

### 2. Executar Scripts SQL

```bash
# Navegar para o diretório sql
cd sql

# Executar script de criação de tabelas
psql -h localhost -U inventario -d sispatrimonio -f criar_tabelas_sispatrimonio.sql

# Executar outros scripts necessários
psql -h localhost -U inventario -d sispatrimonio -f criar_tabela_usuario.sql
```

### 3. Configurar application-mobile.properties

Editar `server/application-mobile.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.username=inventario
spring.datasource.password=senha_segura_aqui

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Server
server.port=8081

# JWT
jwt.secret=sua_chave_secreta_jwt_aqui_minimo_256_bits
jwt.expiration=86400
jwt.refresh-expiration=604800

# Logging
logging.level.com.inventario.sihcp=INFO
logging.level.com.inventario.sihcp.mobile.server.controller=DEBUG
logging.level.com.inventario.sihcp.mobile.server.service=DEBUG
```

### 4. Backup e Restore

#### Backup
```bash
# Backup completo
pg_dump -h localhost -U inventario sispatrimonio > backup_$(date +%Y%m%d).sql

# Backup compactado
pg_dump -h localhost -U inventario sispatrimonio | gzip > backup_$(date +%Y%m%d).sql.gz
```

#### Restore
```bash
# Restore de backup
psql -h localhost -U inventario -d sispatrimonio < backup_20260501.sql

# Restore de backup compactado
gunzip -c backup_20260501.sql.gz | psql -h localhost -U inventario -d sispatrimonio
```

---

## 📊 Monitoramento

### Logs do Server

#### Systemd (Linux)
```bash
# Logs em tempo real
sudo journalctl -u sihcp-server -f

# Últimas 100 linhas
sudo journalctl -u sihcp-server -n 100

# Logs de hoje
sudo journalctl -u sihcp-server --since today

# Logs com filtro de erro
sudo journalctl -u sihcp-server | grep ERROR
```

#### Logs em arquivo
```bash
# Configurar em application-mobile.properties
logging.file.name=/var/log/sihcp-server/server.log
logging.file.max-size=10MB
logging.file.max-history=30

# Ver logs
tail -f /var/log/sihcp-server/server.log
```

### Métricas (Actuator)

Adicionar ao `application-mobile.properties`:

```properties
# Actuator
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

Endpoints disponíveis:
- Health: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/actuator/metrics`
- Prometheus: `http://localhost:8081/actuator/prometheus`

### Monitoramento de Recursos

```bash
# CPU e Memória do processo Java
ps aux | grep sihcp-server

# Uso de memória detalhado
jstat -gc <PID>

# Threads ativas
jstack <PID> | grep "java.lang.Thread.State" | wc -l

# Conexões de rede
netstat -an | grep 8081
```

---

## 🔧 Troubleshooting

### Problema: Server não inicia

**Sintomas:**
```
Error: Could not find or load main class com.inventario.sihcp.MobileApiApplication
```

**Solução:**
```bash
# Verificar se o JAR está correto
jar tf sihcp-server-2.7.0.jar | grep MobileApiApplication

# Verificar manifest
unzip -p sihcp-server-2.7.0.jar META-INF/MANIFEST.MF
```

### Problema: Erro de conexão com banco

**Sintomas:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solução:**
```bash
# Verificar se PostgreSQL está rodando
sudo systemctl status postgresql

# Verificar se a porta está aberta
netstat -an | grep 5432

# Testar conexão
psql -h localhost -U inventario -d sispatrimonio

# Verificar configuracao_banco.json
cat ~/configuracao_banco.json
```

### Problema: Porta 8081 já em uso

**Sintomas:**
```
Web server failed to start. Port 8081 was already in use.
```

**Solução:**
```bash
# Verificar o que está usando a porta
sudo lsof -i :8081
# ou
sudo netstat -tulpn | grep 8081

# Matar o processo
sudo kill -9 <PID>

# Ou usar outra porta
java -jar sihcp-server-2.7.0.jar --server.port=8082
```

### Problema: OutOfMemoryError

**Sintomas:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solução:**
```bash
# Aumentar memória heap
java -Xms1g -Xmx4g -jar sihcp-server-2.7.0.jar

# Ou editar o script de inicialização
# Alterar JAVA_OPTS para valores maiores
```

### Problema: Desktop não encontra configuracao_banco.json

**Sintomas:**
```
Arquivo de configuração não encontrado
```

**Solução:**
```bash
# Verificar localização esperada
# Windows: C:\Users\SeuUsuario\configuracao_banco.json
# Linux/Mac: ~/configuracao_banco.json

# Criar o arquivo no local correto
cat > ~/configuracao_banco.json << 'EOF'
{
  "host": "localhost",
  "port": 5432,
  "database": "sispatrimonio",
  "username": "inventario",
  "password": "sua_senha"
}
EOF
```

### Logs de Debug

Para ativar logs detalhados, editar `application-mobile.properties`:

```properties
# Logs de debug
logging.level.root=INFO
logging.level.com.inventario.sihcp=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## 📋 Checklist de Deploy

### Pré-Deploy
- [ ] Backup do banco de dados criado
- [ ] Versão do Java verificada (17+)
- [ ] PostgreSQL instalado e rodando
- [ ] Portas necessárias liberadas (8081)
- [ ] Firewall configurado

### Deploy Desktop
- [ ] Pacote extraído
- [ ] configuracao_banco.json criado
- [ ] Aplicação inicia sem erros
- [ ] Login funciona
- [ ] Conexão com banco OK

### Deploy Server
- [ ] JAR copiado para /opt/sihcp-server
- [ ] Usuário sihcp criado
- [ ] Service file instalado
- [ ] Serviço habilitado e iniciado
- [ ] Health check retorna 200 OK
- [ ] Swagger UI acessível
- [ ] App Android consegue conectar

### Pós-Deploy
- [ ] Monitoramento configurado
- [ ] Logs sendo gerados
- [ ] Backup automático agendado
- [ ] Documentação atualizada
- [ ] Equipe treinada

---

## 📞 Suporte

### Logs Importantes

- **Server:** `/var/log/sihcp-server/server.log` ou `journalctl -u sihcp-server`
- **Desktop:** Console da aplicação
- **PostgreSQL:** `/var/log/postgresql/postgresql-*.log`

### Comandos Úteis

```bash
# Status do serviço
sudo systemctl status sihcp-server

# Reiniciar serviço
sudo systemctl restart sihcp-server

# Ver logs em tempo real
sudo journalctl -u sihcp-server -f

# Testar health check
curl http://localhost:8081/api/mobile/health

# Verificar conexões ativas
sudo netstat -an | grep 8081

# Verificar uso de memória
ps aux | grep sihcp-server
```

---

**Versão do Guia:** 1.0  
**Data:** 01/05/2026  
**Status:** ✅ Produção Ready
