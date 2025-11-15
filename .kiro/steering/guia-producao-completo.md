# Guia Completo de Produção - Sistema de Inventário Mobile

## ✅ Checklist Pré-Deploy

### Backend
- [ ] Todos os testes passando
- [ ] Build sem erros
- [ ] Configurações de produção validadas
- [ ] Backup do banco de dados criado
- [ ] Logs configurados
- [ ] Monitoramento ativo

### Android
- [ ] APK release assinado
- [ ] ProGuard configurado
- [ ] Permissões validadas
- [ ] Testes em dispositivos reais
- [ ] Versão incrementada

### Infraestrutura
- [ ] Servidor configurado
- [ ] Banco de dados otimizado
- [ ] Firewall configurado
- [ ] SSL/TLS ativo
- [ ] Backup automático

---

## 🚀 Deploy em Produção

### Passo 1: Preparação
```bash
# Criar backup
./scripts/backup-database.sh

# Verificar status do Git
git status
git pull origin main
```

### Passo 2: Build
```bash
# Backend
mvn clean package -DskipTests

# Android
cd InventarioMobile
./gradlew assembleRelease
cd ..
```

### Passo 3: Deploy Automatizado
```bash
# Executar script de deploy
chmod +x scripts/deploy-producao.sh
./scripts/deploy-producao.sh
```

### Passo 4: Verificação
```bash
# Verificar saúde do servidor
curl http://localhost:8080/api/mobile/inventario/ativo

# Verificar logs
tail -f SISTEMA_INVENTARIO_PRODUCAO/logs/server.log
```

---

## 🧪 Testes de Carga

### Executar Testes
```bash
chmod +x scripts/teste-carga.sh
./scripts/teste-carga.sh
```

### Configurar Parâmetros
```bash
# Customizar testes
BASE_URL=http://seu-servidor.com \
NUM_REQUESTS=1000 \
CONCURRENT=50 \
./scripts/teste-carga.sh
```

### Métricas Esperadas
- **Taxa de Sucesso:** >99%
- **Tempo de Resposta:** <500ms
- **Requisições/segundo:** >100
- **Erros:** <1%

---

## 📊 Monitoramento

### Iniciar Monitoramento
```bash
chmod +x scripts/monitorar-metricas.sh
./scripts/monitorar-metricas.sh
```

### Métricas Monitoradas

#### Servidor
- Status HTTP
- Tempo de resposta
- Disponibilidade

#### Recursos
- CPU (alerta >80%)
- Memória (alerta >80%)
- Disco (alerta >90%)

#### Banco de Dados
- Conexões ativas
- Tamanho do banco
- Total de patrimônios
- Total de coletas
- Coletas pendentes

#### Aplicação
- PID do processo
- Memória Java
- Threads ativas
- Uptime

#### Logs
- Contagem de erros
- Contagem de avisos
- Últimos erros

---

## 🔧 Configurações de Produção

### application.properties
```properties
# Servidor
server.port=8080
server.compression.enabled=true

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JWT
jwt.expiration=86400
jwt.refresh-expiration=604800

# Logs
logging.level.root=INFO
logging.level.com.inventario=DEBUG
logging.file.name=logs/server.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Otimizações JVM
```bash
java -jar sistema-inventario.jar \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=logs/heapdump.hprof
```

---

## 📱 Distribuição do APK

### Assinar APK
```bash
jarsigner -verbose \
  -sigalg SHA256withRSA \
  -digestalg SHA-256 \
  -keystore inventario.keystore \
  app-release-unsigned.apk \
  inventario
```

### Otimizar APK
```bash
zipalign -v 4 \
  app-release-unsigned.apk \
  app-release.apk
```

### Distribuir
- Upload para servidor interno
- Enviar link para usuários
- Documentar versão e mudanças

---

## 🚨 Plano de Contingência

### Servidor Não Responde
```bash
# 1. Verificar processo
ps aux | grep java

# 2. Verificar logs
tail -n 100 logs/server.log

# 3. Reiniciar servidor
./scripts/restart-server.sh

# 4. Restaurar backup se necessário
./scripts/restore-backup.sh
```

### Banco de Dados Lento
```bash
# 1. Verificar conexões
psql -c "SELECT * FROM pg_stat_activity;"

# 2. Matar conexões ociosas
psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE state = 'idle';"

# 3. Reindexar tabelas
psql -c "REINDEX DATABASE sispatrimonio;"

# 4. Atualizar estatísticas
psql -c "VACUUM ANALYZE;"
```

### Memória Alta
```bash
# 1. Verificar uso
free -h

# 2. Limpar cache
sync; echo 3 > /proc/sys/vm/drop_caches

# 3. Reiniciar aplicação
./scripts/restart-server.sh
```

---

## 📈 KPIs e Métricas

### Disponibilidade
- **Meta:** 99.9% uptime
- **Monitoramento:** A cada 60s
- **Alerta:** <99%

### Performance
- **Tempo de Resposta:** <500ms (p95)
- **Throughput:** >100 req/s
- **Taxa de Erro:** <1%

### Uso de Recursos
- **CPU:** <70% média
- **Memória:** <80% média
- **Disco:** <80% uso

### Negócio
- **Coletas/dia:** Monitorar tendência
- **Taxa de Sincronização:** >95%
- **Usuários Ativos:** Diário/Semanal

---

## 🔐 Segurança

### Checklist
- [ ] HTTPS habilitado
- [ ] Tokens JWT seguros
- [ ] Senhas hasheadas (BCrypt)
- [ ] SQL Injection prevenido
- [ ] CORS configurado
- [ ] Rate limiting ativo
- [ ] Logs de auditoria

### Backup
```bash
# Backup diário automático
0 2 * * * /scripts/backup-database.sh

# Retenção: 30 dias
find backups/ -name "*.sql" -mtime +30 -delete
```

---

## 📞 Suporte

### Contatos
- **Equipe Técnica:** suporte@inventario.com
- **Emergência:** +55 (XX) XXXX-XXXX
- **Documentação:** https://docs.inventario.com

### Logs Importantes
- **Servidor:** `logs/server.log`
- **Métricas:** `logs/metrics.log`
- **Erros:** `logs/error.log`
- **Acesso:** `logs/access.log`

---

## ✅ Checklist Pós-Deploy

- [ ] Servidor respondendo
- [ ] Endpoints testados
- [ ] APK distribuído
- [ ] Monitoramento ativo
- [ ] Backup verificado
- [ ] Equipe notificada
- [ ] Documentação atualizada
- [ ] Versão taggeada no Git

---

**Sistema pronto para produção!** 🚀

**Versão:** 2.0.0  
**Data:** 15/11/2025  
**Status:** ✅ PRODUÇÃO READY
