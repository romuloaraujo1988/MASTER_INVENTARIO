# Sistema de Inventário - Modo Produção

## 📋 Pré-requisitos

- **Java 21** ou superior instalado
- **Maven 3.6+** instalado
- **PostgreSQL 12+** configurado e rodando
- Arquivo `configuracao_banco.json` configurado no diretório home do usuário

---

## 🚀 Inicialização Rápida

### Windows (Batch)
```batch
# 1. Compilar o sistema
build-producao.bat

# 2. Iniciar em modo produção
start-producao.bat
```

### Windows (PowerShell)
```powershell
# 1. Compilar o sistema
.\build-producao.bat

# 2. Iniciar em modo produção
.\start-producao.ps1
```

---

## 📦 Build Manual

Se preferir compilar manualmente:

```bash
# Limpar e compilar
mvn clean package -DskipTests

# O JAR será gerado em:
# target/sistema-inventario-2.0.0.jar
```

---

## ⚙️ Configurações de Produção

### Perfil Ativo
O sistema usa o perfil `prod` definido em `application-prod.properties`

### Configurações Principais

| Configuração | Valor | Descrição |
|--------------|-------|-----------|
| **Porta** | 8080 | Porta do servidor |
| **Memória** | 512MB - 2GB | Heap da JVM |
| **Logs** | logs/sistema-inventario-prod.log | Arquivo de log |
| **Pool DB** | 5-10 conexões | Pool de conexões |
| **Nível Log** | INFO | Nível de logging |

### Variáveis de Ambiente

```bash
# Memória JVM
JAVA_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC

# Perfil Spring
SPRING_PROFILES_ACTIVE=prod

# Secret JWT (opcional, sobrescreve o padrão)
JWT_SECRET=SuaChaveSecretaAqui
```

---

## 🗄️ Configuração do Banco de Dados

O sistema busca as configurações do banco no arquivo:
```
%USERPROFILE%/configuracao_banco.json  (Windows)
~/configuracao_banco.json              (Linux/Mac)
```

### Exemplo de configuracao_banco.json

```json
{
  "host": "localhost",
  "port": "5432",
  "database": "sispatrimonio",
  "username": "inventario",
  "password": "sua_senha_aqui",
  "schema": "public"
}
```

---

## 📊 Monitoramento

### Logs

Os logs são salvos em:
```
logs/sistema-inventario-prod.log
```

Rotação automática:
- Tamanho máximo: 10MB
- Histórico: 30 dias

### Verificar Status

```bash
# Verificar se está rodando
netstat -ano | findstr :8080

# Ver logs em tempo real (PowerShell)
Get-Content logs\sistema-inventario-prod.log -Wait -Tail 50
```

---

## 🔒 Segurança

### Recomendações para Produção

1. **Alterar JWT Secret**
   ```bash
   set JWT_SECRET=SuaChaveSecretaForte123!@#
   ```

2. **Usar HTTPS**
   - Configure um proxy reverso (Nginx/Apache)
   - Ou configure SSL no Spring Boot

3. **Firewall**
   ```bash
   # Permitir apenas porta 8080
   netsh advfirewall firewall add rule name="Sistema Inventario" dir=in action=allow protocol=TCP localport=8080
   ```

4. **Backup do Banco**
   ```bash
   # Agendar backup diário
   pg_dump -h localhost -U inventario sispatrimonio > backup_$(date +%Y%m%d).sql
   ```

---

## 🐛 Troubleshooting

### Erro: "Porta 8080 já está em uso"

```bash
# Windows: Encontrar processo usando a porta
netstat -ano | findstr :8080

# Matar processo
taskkill /PID <PID> /F
```

### Erro: "Não foi possível conectar ao banco"

1. Verificar se PostgreSQL está rodando
2. Verificar arquivo `configuracao_banco.json`
3. Testar conexão manualmente:
   ```bash
   psql -h localhost -U inventario -d sispatrimonio
   ```

### Erro: "OutOfMemoryError"

Aumentar memória da JVM:
```bash
set JAVA_OPTS=-Xms1g -Xmx4g
```

### Logs não estão sendo gerados

1. Verificar permissões da pasta `logs/`
2. Verificar espaço em disco
3. Verificar configuração em `application-prod.properties`

---

## 🔄 Atualização

Para atualizar o sistema:

```bash
# 1. Parar o sistema (Ctrl+C)

# 2. Fazer backup do banco
pg_dump sispatrimonio > backup_antes_atualizacao.sql

# 3. Atualizar código (git pull ou copiar arquivos)

# 4. Recompilar
build-producao.bat

# 5. Reiniciar
start-producao.bat
```

---

## 📱 API Mobile

A API Mobile está habilitada em produção:

- **Endpoint Base**: `http://localhost:8080/api/mobile`
- **Autenticação**: JWT Bearer Token
- **Rate Limit**: 100 requisições/minuto

### Endpoints Principais

```
POST   /api/mobile/auth/login
GET    /api/mobile/patrimonio
POST   /api/mobile/coletas
GET    /api/mobile/salas
GET    /api/mobile/dashboard/stats
```

---

## 📈 Performance

### Otimizações Aplicadas

- ✅ Pool de conexões otimizado (5-10)
- ✅ Garbage Collector G1GC
- ✅ Compressão HTTP habilitada
- ✅ Cache de queries
- ✅ Logs otimizados (INFO level)

### Métricas Esperadas

- Tempo de inicialização: < 30 segundos
- Uso de memória: 512MB - 1GB (normal)
- Tempo de resposta API: < 200ms
- Conexões simultâneas: até 100

---

## 🆘 Suporte

Em caso de problemas:

1. Verificar logs em `logs/sistema-inventario-prod.log`
2. Verificar configuração do banco
3. Verificar portas e firewall
4. Consultar documentação técnica

---

## 📝 Checklist de Deploy

- [ ] Java 21+ instalado
- [ ] Maven instalado
- [ ] PostgreSQL rodando
- [ ] Banco de dados criado
- [ ] Arquivo `configuracao_banco.json` configurado
- [ ] Firewall configurado (porta 8080)
- [ ] Build executado com sucesso
- [ ] Sistema iniciado sem erros
- [ ] Login funcionando
- [ ] API Mobile respondendo
- [ ] Logs sendo gerados
- [ ] Backup configurado

---

**Versão**: 2.0.0  
**Última atualização**: 13/11/2025
