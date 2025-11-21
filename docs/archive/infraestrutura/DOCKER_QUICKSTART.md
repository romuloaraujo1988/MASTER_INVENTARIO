# 🐳 Docker Quick Start - Sistema Escalável

## 🚀 Iniciar Sistema Completo

### Opção 1: Desenvolvimento (1 instância)

```bash
docker-compose -f docker-compose-scalable.yml up -d
```

### Opção 2: Produção (3 instâncias)

```bash
docker-compose -f docker-compose-scalable.yml up -d --scale mobile-api=3
```

### Opção 3: Alta Carga (5 instâncias)

```bash
docker-compose -f docker-compose-scalable.yml up -d --scale mobile-api=5
```

---

## 📊 Acessar Serviços

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API Mobile** | http://localhost/inventario | - |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | - |
| **PostgreSQL** | localhost:5432 | postgres / Romulo@2020 |
| **Redis** | localhost:6379 | - |

---

## 🔍 Monitorar Sistema

### Ver logs de todas as instâncias

```bash
docker-compose -f docker-compose-scalable.yml logs -f mobile-api
```

### Ver logs de um serviço específico

```bash
docker-compose -f docker-compose-scalable.yml logs -f nginx
docker-compose -f docker-compose-scalable.yml logs -f postgres
docker-compose -f docker-compose-scalable.yml logs -f redis
```

### Ver status dos containers

```bash
docker-compose -f docker-compose-scalable.yml ps
```

### Ver uso de recursos

```bash
docker stats
```

---

## 🔧 Gerenciar Instâncias

### Escalar para mais instâncias (sem downtime)

```bash
docker-compose -f docker-compose-scalable.yml up -d --scale mobile-api=5 --no-recreate
```

### Reduzir número de instâncias

```bash
docker-compose -f docker-compose-scalable.yml up -d --scale mobile-api=2 --no-recreate
```

### Reiniciar apenas a API (sem afetar banco)

```bash
docker-compose -f docker-compose-scalable.yml restart mobile-api
```

### Rebuild após mudanças no código

```bash
docker-compose -f docker-compose-scalable.yml build mobile-api
docker-compose -f docker-compose-scalable.yml up -d --no-deps mobile-api
```

---

## 🛑 Parar Sistema

### Parar todos os serviços (mantém dados)

```bash
docker-compose -f docker-compose-scalable.yml stop
```

### Parar e remover containers (mantém volumes)

```bash
docker-compose -f docker-compose-scalable.yml down
```

### Parar e remover TUDO (incluindo dados)

```bash
docker-compose -f docker-compose-scalable.yml down -v
```

---

## 🧪 Testar Sistema

### Health check da API

```bash
curl http://localhost/inventario/actuator/health
```

### Testar load balancing

```bash
# Fazer 10 requisições e ver qual instância responde
for i in {1..10}; do
  curl -s http://localhost/inventario/actuator/info | grep instance
done
```

### Verificar métricas

```bash
curl http://localhost/inventario/actuator/metrics
```

---

## 📈 Dashboards Grafana

### Importar dashboards recomendados:

1. Acessar http://localhost:3000
2. Login: admin / admin
3. Ir em **Dashboards** → **Import**
4. Importar IDs:
   - **4701** - JVM Micrometer
   - **11378** - Spring Boot Statistics
   - **9628** - PostgreSQL Database
   - **763** - Redis Dashboard

---

## 🔥 Troubleshooting

### Container não inicia

```bash
# Ver logs detalhados
docker-compose -f docker-compose-scalable.yml logs mobile-api

# Verificar se portas estão em uso
netstat -ano | findstr "8081"
netstat -ano | findstr "5432"
```

### Banco de dados não conecta

```bash
# Verificar se PostgreSQL está rodando
docker-compose -f docker-compose-scalable.yml ps postgres

# Testar conexão
docker exec -it inventario-postgres psql -U postgres -d sispatrimonio -c "SELECT 1"
```

### Redis não conecta

```bash
# Verificar se Redis está rodando
docker-compose -f docker-compose-scalable.yml ps redis

# Testar conexão
docker exec -it inventario-redis redis-cli ping
```

### Limpar tudo e recomeçar

```bash
# Parar tudo
docker-compose -f docker-compose-scalable.yml down -v

# Remover imagens antigas
docker image prune -a

# Rebuild completo
docker-compose -f docker-compose-scalable.yml build --no-cache
docker-compose -f docker-compose-scalable.yml up -d
```

---

## 🎯 Comandos Úteis

### Entrar em um container

```bash
# API
docker exec -it <container_id> sh

# PostgreSQL
docker exec -it inventario-postgres psql -U postgres -d sispatrimonio

# Redis
docker exec -it inventario-redis redis-cli
```

### Backup do banco de dados

```bash
docker exec inventario-postgres pg_dump -U postgres sispatrimonio > backup.sql
```

### Restaurar banco de dados

```bash
docker exec -i inventario-postgres psql -U postgres -d sispatrimonio < backup.sql
```

### Ver configuração do Nginx

```bash
docker exec inventario-nginx cat /etc/nginx/nginx.conf
```

### Recarregar configuração do Nginx (sem downtime)

```bash
docker exec inventario-nginx nginx -s reload
```

---

## 📊 Monitoramento em Tempo Real

### CPU e Memória

```bash
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

### Requisições por segundo (Nginx)

```bash
docker exec inventario-nginx cat /var/log/nginx/access.log | tail -100
```

### Conexões ativas no PostgreSQL

```bash
docker exec inventario-postgres psql -U postgres -d sispatrimonio -c "SELECT count(*) FROM pg_stat_activity"
```

### Chaves no Redis

```bash
docker exec inventario-redis redis-cli DBSIZE
```

---

## 🚀 Deploy em Produção

### 1. Configurar variáveis de ambiente

```bash
# Criar arquivo .env
cat > .env << EOF
JWT_SECRET=seu-secret-super-seguro-aqui
DATABASE_PASSWORD=senha-forte-aqui
CORS_ORIGINS=https://seu-dominio.com
EOF
```

### 2. Usar Docker Swarm (orquestração)

```bash
# Inicializar swarm
docker swarm init

# Deploy do stack
docker stack deploy -c docker-compose-scalable.yml inventario

# Ver serviços
docker service ls

# Escalar serviço
docker service scale inventario_mobile-api=5
```

### 3. Ou usar Kubernetes

```bash
# Converter docker-compose para Kubernetes
kompose convert -f docker-compose-scalable.yml

# Deploy no Kubernetes
kubectl apply -f .
```

---

## ✅ Checklist de Produção

Antes de colocar em produção:

- [ ] Alterar senhas padrão (PostgreSQL, Grafana)
- [ ] Configurar JWT_SECRET via variável de ambiente
- [ ] Configurar CORS para domínio específico
- [ ] Configurar SSL/TLS (HTTPS)
- [ ] Configurar backup automático do banco
- [ ] Configurar alertas no Prometheus
- [ ] Testar failover (derrubar uma instância)
- [ ] Executar teste de carga
- [ ] Configurar logs centralizados
- [ ] Documentar procedimentos de rollback

---

## 🎉 Pronto!

Seu sistema está rodando de forma escalável com:

- ✅ Load balancer (Nginx)
- ✅ Múltiplas instâncias da API
- ✅ Cache distribuído (Redis)
- ✅ Banco de dados PostgreSQL
- ✅ Monitoramento (Prometheus + Grafana)
- ✅ Health checks automáticos
- ✅ Graceful shutdown
- ✅ Auto-restart em caso de falha

**Capacidade estimada:** 500-1000 usuários simultâneos
