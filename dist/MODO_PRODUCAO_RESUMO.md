# 🚀 Sistema de Inventário - Modo Produção

## ✅ Arquivos Criados

### 1. Configuração
- `application-prod.properties` - Configurações otimizadas para produção

### 2. Scripts de Inicialização
- `start-producao.bat` - Iniciar sistema (Windows Batch)
- `start-producao.ps1` - Iniciar sistema (PowerShell)
- `build-producao.bat` - Compilar para produção

### 3. Instalação como Serviço
- `instalar-servico-windows.bat` - Instalar como serviço Windows

### 4. Documentação
- `PRODUCAO.md` - Guia completo de produção
- `MODO_PRODUCAO_RESUMO.md` - Este arquivo

---

## 🎯 Início Rápido (3 Passos)

### Passo 1: Compilar
```batch
build-producao.bat
```

### Passo 2: Iniciar
```batch
start-producao.bat
```

### Passo 3: Acessar
- **Desktop**: O sistema abrirá automaticamente
- **API Mobile**: http://localhost:8080/api/mobile

---

## 🔧 Configurações Aplicadas

### Performance
- ✅ Memória: 512MB - 2GB
- ✅ Garbage Collector: G1GC
- ✅ Pool de Conexões: 5-10
- ✅ Compressão HTTP: Habilitada

### Segurança
- ✅ Logs de erro sem stack trace
- ✅ JWT com secret configurável
- ✅ Rate limiting: 100 req/min
- ✅ Timeout de sessão: 1 hora

### Logging
- ✅ Nível: INFO
- ✅ Arquivo: logs/sistema-inventario-prod.log
- ✅ Rotação: 10MB / 30 dias
- ✅ Formato otimizado

### Banco de Dados
- ✅ Pool otimizado
- ✅ Timeout configurado
- ✅ Conexões persistentes
- ✅ Configuração externa (configuracao_banco.json)

---

## 📊 Diferenças: Desenvolvimento vs Produção

| Aspecto | Desenvolvimento | Produção |
|---------|----------------|----------|
| **Logs** | DEBUG | INFO |
| **Stack Trace** | Sim | Não |
| **SQL Logs** | Sim | Não |
| **Memória** | 256MB - 1GB | 512MB - 2GB |
| **Hot Reload** | Sim | Não |
| **Compressão** | Não | Sim |
| **Cache** | Não | Sim |
| **Pool DB** | 2-5 | 5-10 |

---

## 🔐 Checklist de Segurança

Antes de colocar em produção:

- [ ] Alterar JWT_SECRET
- [ ] Configurar senha forte do banco
- [ ] Habilitar firewall (porta 8080)
- [ ] Configurar backup automático
- [ ] Testar recuperação de desastres
- [ ] Configurar HTTPS (recomendado)
- [ ] Limitar acesso à rede (opcional)
- [ ] Configurar monitoramento (opcional)

---

## 🆘 Comandos Úteis

### Gerenciar Serviço
```batch
# Iniciar
sc start SistemaInventario

# Parar
sc stop SistemaInventario

# Status
sc query SistemaInventario

# Remover
sc delete SistemaInventario
```

### Monitorar Logs
```powershell
# Ver últimas 50 linhas
Get-Content logs\sistema-inventario-prod.log -Tail 50

# Acompanhar em tempo real
Get-Content logs\sistema-inventario-prod.log -Wait -Tail 50
```

### Verificar Porta
```batch
# Ver o que está usando a porta 8080
netstat -ano | findstr :8080
```

### Backup do Banco
```batch
# Backup manual
pg_dump -h localhost -U inventario sispatrimonio > backup.sql

# Restaurar
psql -h localhost -U inventario -d sispatrimonio < backup.sql
```

---

## 📈 Monitoramento

### Métricas Importantes

1. **Uso de Memória**
   - Normal: 512MB - 1GB
   - Alerta: > 1.5GB
   - Crítico: > 1.8GB

2. **Tempo de Resposta**
   - Bom: < 200ms
   - Aceitável: < 500ms
   - Ruim: > 1s

3. **Conexões DB**
   - Normal: 2-5 ativas
   - Alerta: > 8 ativas
   - Crítico: Pool esgotado

4. **Taxa de Erro**
   - Bom: < 1%
   - Alerta: 1-5%
   - Crítico: > 5%

---

## 🔄 Processo de Atualização

```batch
# 1. Parar sistema
sc stop SistemaInventario

# 2. Backup do banco
pg_dump sispatrimonio > backup_pre_update.sql

# 3. Atualizar código
git pull
# ou copiar novos arquivos

# 4. Recompilar
build-producao.bat

# 5. Reiniciar
sc start SistemaInventario

# 6. Verificar logs
Get-Content logs\sistema-inventario-prod.log -Tail 100
```

---

## 📞 Suporte

### Logs de Erro

Todos os erros são registrados em:
```
logs/sistema-inventario-prod.log
```

### Problemas Comuns

1. **Porta em uso**: Verificar com `netstat -ano | findstr :8080`
2. **Banco offline**: Verificar PostgreSQL com `pg_isready`
3. **Memória insuficiente**: Aumentar JAVA_OPTS
4. **Permissões**: Executar como Administrador

---

## 🎉 Pronto para Produção!

O sistema está configurado e pronto para uso em produção com:

- ✅ Performance otimizada
- ✅ Segurança reforçada
- ✅ Logs estruturados
- ✅ Fácil manutenção
- ✅ Monitoramento preparado
- ✅ Backup facilitado

**Boa sorte com o deploy! 🚀**

---

**Versão**: 2.0.0  
**Data**: 13/11/2025  
**Autor**: Sistema de Inventário
