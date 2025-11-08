# Guia de Execução da Aplicação - Versão 1.2.0

**Data**: 04/11/2025  
**Versão**: 1.2.0  
**Status**: ✅ Funcionando Perfeitamente

---

## ✅ Status Atual

O JAR executável **ESTÁ FUNCIONANDO PERFEITAMENTE** quando executado diretamente:

```bash
java -jar target/sistema-inventario-1.2.0-exec.jar
```

**Teste realizado**: ✅ Sucesso
- Aplicação iniciou corretamente
- Banco de dados conectado
- Login funcionando
- Interface Swing carregada

---

## 🚀 Formas de Executar

### 1. Execução Direta (Linha de Comando)

**Desktop**:
```bash
java -jar target\sistema-inventario-1.2.0-exec.jar
```

**Mobile API**:
```bash
cd target\mobile-server
java -jar sistema-inventario-1.2.0.jar --spring.profiles.active=mobile
```

### 2. Scripts .BAT (Recomendado para Windows)

#### Desktop
```bash
start-desktop.bat
```

Conteúdo do script:
```batch
@echo off
echo ========================================
echo Sistema de Inventario - Versao 1.2.0
echo ========================================
echo.
echo Iniciando aplicacao desktop...
echo.

java -jar target\sistema-inventario-1.2.0-exec.jar

echo.
echo Aplicacao encerrada.
pause
```

#### Mobile API (usando JAR)
```bash
start-mobile-api-jar.bat
```

Conteúdo do script:
```batch
@echo off
echo ========================================
echo Mobile API Server - Versao 1.2.0
echo ========================================
echo.
echo Iniciando Mobile API Server...
echo Porta: 8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.

cd target\mobile-server
java -jar sistema-inventario-1.2.0.jar --spring.profiles.active=mobile

echo.
echo Mobile API Server encerrado.
pause
```

#### Mobile API (usando Maven - Desenvolvimento)
```bash
start-mobile-api.bat
```

Conteúdo do script:
```batch
@echo off
echo Iniciando Mobile API Server...
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile
```

### 3. Duplo Clique no JAR (Windows)

Se o Java estiver associado aos arquivos .jar no Windows, você pode:
1. Navegar até `target/`
2. Dar duplo clique em `sistema-inventario-1.2.0-exec.jar`

**Nota**: Isso pode não mostrar o console. Use os scripts .bat para ver logs.

---

## 📋 Pré-requisitos

### 1. Java Runtime Environment (JRE)
```bash
# Verificar versão instalada
java -version
```

**Versão necessária**: JDK/JRE 21 ou superior

**Saída esperada**:
```
java version "21.0.x" 2024-xx-xx LTS
Java(TM) SE Runtime Environment (build 21.0.x+xx-LTS-xxx)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.x+xx-LTS-xxx, mixed mode, sharing)
```

### 2. Banco de Dados PostgreSQL

**Configuração necessária**:
- Host: localhost
- Porta: 5432
- Database: sispatrimonio
- User: inventario
- Password: (configurada)

**Arquivo de configuração**:
```
Windows: C:\Users\[seu_usuario]\configuracao_banco.json
Linux: ~/configuracao_banco.json
```

**Conteúdo**:
```json
{
  "host": "localhost",
  "porta": "5432",
  "database": "sispatrimonio",
  "usuario": "inventario",
  "senha": "sua_senha_aqui"
}
```

### 3. PostgreSQL em Execução

```bash
# Windows - Verificar serviço
sc query postgresql-x64-12

# Ou verificar conexão
psql -h localhost -U inventario -d sispatrimonio
```

---

## 🔍 Verificação de Funcionamento

### Teste 1: Verificar JAR
```bash
java -jar target\sistema-inventario-1.2.0-exec.jar
```

**Saída esperada**:
```
Iniciando Sistema de Inventário...
Configuração de banco carregada com sucesso.
DatabaseConnection inicializado com sucesso.
Usuário autenticado com sucesso: admin
Conexão detectada com servidor do banco de dados
Estado alterado: INITIALIZING -> ONLINE
```

### Teste 2: Verificar Manifest
```bash
jar -xf target\sistema-inventario-1.2.0-exec.jar META-INF\MANIFEST.MF
type META-INF\MANIFEST.MF
```

**Deve conter**:
```
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.inventario.SistemaInventarioApplication
```

### Teste 3: Verificar Tamanho
```bash
dir target\sistema-inventario-1.2.0-exec.jar
```

**Tamanho esperado**: ~131 MB (131,435,043 bytes)

---

## 🐛 Troubleshooting

### Problema: "java não é reconhecido como comando"

**Causa**: Java não está no PATH do sistema

**Solução**:
1. Verificar instalação do Java:
   ```bash
   "C:\Program Files\Java\jdk-21\bin\java.exe" -version
   ```

2. Adicionar ao PATH ou usar caminho completo:
   ```bash
   "C:\Program Files\Java\jdk-21\bin\java.exe" -jar target\sistema-inventario-1.2.0-exec.jar
   ```

### Problema: "Could not find or load main class"

**Causa**: Arquivo JAR corrompido ou incompleto

**Solução**: Recompilar
```bash
.\mvnw.cmd clean package -DskipTests
```

### Problema: "Connection refused" ao conectar no banco

**Causa**: PostgreSQL não está rodando ou configuração incorreta

**Solução**:
1. Verificar se PostgreSQL está rodando
2. Verificar arquivo `configuracao_banco.json`
3. Testar conexão manual:
   ```bash
   psql -h localhost -U inventario -d sispatrimonio
   ```

### Problema: "Port 8080 already in use" (Mobile API)

**Causa**: Porta 8080 já está sendo usada

**Solução**: Usar porta alternativa
```bash
java -jar sistema-inventario-1.2.0.jar --spring.profiles.active=mobile --server.port=8081
```

### Problema: Interface não aparece (Windows)

**Causa**: Executando sem console visível

**Solução**: Use os scripts .bat que mantêm o console aberto

---

## 📊 Comparação: Antes vs Agora

### Antes (Desenvolvimento)
```bash
# Executava via Maven
mvnw.cmd spring-boot:run

# Ou via IDE (NetBeans, IntelliJ)
```

**Características**:
- ❌ Precisa do código fonte
- ❌ Precisa do Maven
- ❌ Mais lento (compila toda vez)
- ✅ Bom para desenvolvimento

### Agora (Produção)
```bash
# Executa JAR compilado
java -jar target\sistema-inventario-1.2.0-exec.jar
```

**Características**:
- ✅ Não precisa do código fonte
- ✅ Não precisa do Maven
- ✅ Mais rápido (já compilado)
- ✅ Pronto para distribuição
- ✅ Todas as dependências incluídas

---

## 📦 Distribuição para Usuários

### Opção 1: JAR Executável (Recomendado)

**Arquivos para distribuir**:
```
sistema-inventario-1.2.0/
├── sistema-inventario-1.2.0-exec.jar  (131 MB)
├── start-desktop.bat                   (script de inicialização)
├── README.txt                          (instruções)
└── configuracao_banco_exemplo.json     (exemplo de configuração)
```

**Instruções para o usuário**:
1. Instalar Java 21 ou superior
2. Configurar PostgreSQL
3. Criar arquivo `configuracao_banco.json` no diretório home
4. Executar `start-desktop.bat`

### Opção 2: Instalador (Futuro)

Criar instalador com jpackage que inclui:
- JRE embutido (não precisa instalar Java)
- Ícone da aplicação
- Atalho no menu iniciar
- Desinstalador

---

## 🎯 Conclusão

O JAR executável **ESTÁ FUNCIONANDO PERFEITAMENTE**. Não há problemas com a execução.

**Formas de executar** (todas funcionam):
1. ✅ Linha de comando: `java -jar target\sistema-inventario-1.2.0-exec.jar`
2. ✅ Script .bat: `start-desktop.bat`
3. ✅ Duplo clique no JAR (se Java estiver associado)

**Recomendação**: Use os scripts .bat criados para facilitar a execução e visualizar logs.

---

## 📝 Scripts Criados

### 1. start-desktop.bat
Executa a aplicação desktop com interface Swing

### 2. start-mobile-api-jar.bat
Executa o Mobile API Server usando o JAR compilado

### 3. start-mobile-api.bat (já existia)
Executa o Mobile API Server via Maven (desenvolvimento)

---

**Desenvolvido por**: Sistema SIHCP  
**Para**: Instituto Federal de Mato Grosso (IFMT)  
**Versão**: 1.2.0  
**Status**: ✅ Funcionando Perfeitamente
