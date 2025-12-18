# Instruções de Compilação e Teste

## 🔨 Compilação

### Pré-requisitos
- Java 21 ou superior
- Maven 3.6+
- PostgreSQL 12+ (para testes com banco real)

### Compilar Projeto

```bash
# Limpar e compilar
mvn clean compile

# Compilar com testes
mvn clean test

# Compilar sem testes
mvn clean compile -DskipTests
```

### Verificar Erros

```bash
# Apenas compilação
mvn compile

# Com análise de código
mvn clean compile checkstyle:check
```

---

## 🏗️ Build

### Build de Desenvolvimento

```bash
# Gerar JAR executável
mvn clean package -DskipTests

# Resultado
target/sistema-inventario-1.2.0.jar
```

### Build de Produção

```bash
# Executar script de build
cd .
.\build-producao-completo.ps1

# Resultado
dist\producao\
├── sihcp-desktop.jar
├── mobile-server.jar
├── lib\
├── iniciar-desktop.ps1
├── iniciar-servidor-mobile.ps1
└── README.txt
```

---

## 🧪 Testes

### Teste 1: Carregamento de Salas (VPN)

**Objetivo**: Verificar que interface não trava ao carregar salas

**Passos**:
1. Conectar via VPN (ou simular conexão lenta)
2. Executar aplicação desktop
3. Abrir frame de coleta
4. Observar dialog de loading
5. Aguardar carregamento
6. Verificar que combo foi preenchido

**Resultado Esperado**: ✅ Interface responsiva, salas carregadas

---

### Teste 2: Busca de Patrimônio (VPN)

**Objetivo**: Verificar que busca não trava a interface

**Passos**:
1. Conectar via VPN
2. Selecionar uma sala
3. Digitar número de patrimônio
4. Observar cursor mudar para WAIT_CURSOR
5. Aguardar resultado
6. Verificar que informações foram exibidas

**Resultado Esperado**: ✅ Interface responsiva, patrimônio encontrado

---

### Teste 3: Retry em Erro

**Objetivo**: Verificar que retry funciona corretamente

**Passos**:
1. Desconectar internet (ou simular erro)
2. Tentar carregar salas
3. Observar dialog de erro
4. Clicar "Tentar Novamente"
5. Reconectar internet
6. Verificar que operação foi bem-sucedida

**Resultado Esperado**: ✅ Retry funcionou, dados carregados

---

### Teste 4: Configuração SQLite

**Objetivo**: Verificar que configuração está correta

**Passos**:
1. Abrir aplicação desktop
2. Ir para menu de configuração
3. Abrir diálogo de configuração do banco
4. Salvar configuração
5. Abrir arquivo `configuracao_banco.json`
6. Verificar conteúdo

**Resultado Esperado**: ✅ Banco é `inventario.db`, backup tem data

**Arquivo**: `~/.inventario/configuracao_banco.json`

**Conteúdo Esperado**:
```json
{
    "sqlite": {
        "database": "inventario.db",
        "backup_dir": "backups/2025-12-17"
    }
}
```

---

### Teste 5: Sincronização Offline

**Objetivo**: Verificar que sincronização offline funciona

**Passos**:
1. Desconectar internet
2. Coletar alguns patrimônios
3. Verificar que foram salvos localmente
4. Reconectar internet
5. Sincronizar dados
6. Verificar que foram enviados ao servidor

**Resultado Esperado**: ✅ Dados sincronizados com sucesso

---

## 🚀 Executar Aplicação

### Desktop

```bash
# Opção 1: Via Maven
mvn exec:java -Dexec.mainClass="com.inventario.SistemaInventarioApplication"

# Opção 2: Via JAR
java -jar target/sistema-inventario-1.2.0.jar

# Opção 3: Via script de produção
cd dist\producao
.\iniciar-desktop.ps1
```

### Servidor Mobile

```bash
# Opção 1: Via Maven
mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication" \
  -Dspring.profiles.active=mobile

# Opção 2: Via JAR
java -jar target/mobile-server.jar --spring.profiles.active=mobile

# Opção 3: Via script de produção
cd dist\producao
.\iniciar-servidor-mobile.ps1
```

---

## 📊 Verificar Compilação

### Sem Erros

```bash
mvn clean compile
# [INFO] BUILD SUCCESS
```

### Com Warnings (Aceitável)

```bash
mvn clean compile
# [WARNING] Some warnings...
# [INFO] BUILD SUCCESS
```

### Com Erros (Não Aceitável)

```bash
mvn clean compile
# [ERROR] COMPILATION ERROR
# [INFO] BUILD FAILURE
```

---

## 🔍 Verificar Configuração

### Arquivo de Configuração

```bash
# Localização
~/.inventario/configuracao_banco.json

# Conteúdo esperado
{
    "postgresql": { ... },
    "sqlite": {
        "database": "inventario.db",
        "backup_dir": "backups/2025-12-17"
    },
    "mysql": { ... }
}
```

### Banco de Dados SQLite

```bash
# Localização
./data/inventario.db

# Verificar se existe
ls -la ./data/inventario.db
```

### Backups

```bash
# Localização
./backups/2025-12-17/

# Listar backups
ls -la ./backups/
```

---

## 📝 Logs

### Localização

```bash
# Desktop
logs/sistema-inventario.log

# Servidor Mobile
logs/mobile-server.log
```

### Ver Logs em Tempo Real

```bash
# Desktop
tail -f logs/sistema-inventario.log

# Servidor Mobile
tail -f logs/mobile-server.log
```

### Procurar por Erros

```bash
# Erros
grep ERROR logs/sistema-inventario.log

# Warnings
grep WARN logs/sistema-inventario.log

# Async operations
grep "ASYNC\|SwingWorker" logs/sistema-inventario.log
```

---

## 🧪 Testes Unitários

### Executar Todos os Testes

```bash
mvn test
```

### Executar Teste Específico

```bash
mvn test -Dtest=TestClassName
```

### Executar com Cobertura

```bash
mvn clean test jacoco:report
# Resultado: target/site/jacoco/index.html
```

---

## 🐛 Troubleshooting

### Problema: Compilação Falha

**Solução**:
```bash
# Limpar cache Maven
mvn clean

# Atualizar dependências
mvn dependency:resolve

# Compilar novamente
mvn compile
```

### Problema: Timeout ao Compilar

**Solução**:
```bash
# Aumentar timeout
mvn -DskipTests -T 1C clean package

# Ou compilar sem testes
mvn clean compile -DskipTests
```

### Problema: Erro de Memória

**Solução**:
```bash
# Aumentar memória da JVM
export MAVEN_OPTS="-Xmx2g"
mvn clean package
```

### Problema: Porta 8080 em Uso

**Solução**:
```bash
# Matar processo na porta 8080
lsof -i :8080
kill -9 <PID>

# Ou usar porta diferente
java -jar target/mobile-server.jar --server.port=8081
```

---

## ✅ Checklist de Teste

- [ ] Compilação sem erros
- [ ] Build de produção executado
- [ ] Carregamento de salas funciona
- [ ] Busca de patrimônio funciona
- [ ] Retry em erro funciona
- [ ] Configuração SQLite correta
- [ ] Sincronização offline funciona
- [ ] Logs sem erros críticos
- [ ] Interface responsiva com VPN
- [ ] Testes unitários passam

---

## 📞 Suporte

### Se Encontrar Problemas

1. Verificar logs em `logs/sistema-inventario.log`
2. Verificar compilação com `mvn clean compile`
3. Verificar configuração em `~/.inventario/configuracao_banco.json`
4. Verificar conexão com banco de dados
5. Verificar conexão VPN

### Documentação Relacionada

- `RESUMO_SESSAO_VPNFIX.md` - Detalhes do fix VPN
- `GUIA_RAPIDO_ASYNC_VPN.md` - Guia de async
- `RESUMO_CORRECAO_SQLITE.md` - Detalhes da correção SQLite

---

**Versão**: 1.0.0  
**Data**: 17/12/2025  
**Status**: ✅ Pronto para uso

