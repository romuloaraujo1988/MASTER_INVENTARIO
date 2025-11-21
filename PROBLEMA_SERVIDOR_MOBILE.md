# Problema: Servidor Mobile Retornando 404

## 🔍 Diagnóstico

### Servidor Está Rodando
- ✅ Porta 8081 está aberta (LISTENING)
- ✅ Processo Java rodando (PID: 17080)
- ❌ Todos os endpoints retornam 404

### Endpoints Testados
```
❌ http://localhost:8081/ → 404
❌ http://localhost:8081/api/mobile/ → 404
❌ http://localhost:8081/api/mobile/auth/login → 404
❌ http://localhost:8081/api/mobile/patrimonio → 404
```

## 🎯 Causa Provável

O servidor está rodando mas:
1. **Não está no perfil mobile** - Pode estar rodando o perfil desktop
2. **Context path diferente** - Pode ter um prefixo que não sabemos
3. **Aplicação não iniciou corretamente** - Erro no startup

## ✅ Solução

### Opção 1: Reiniciar Servidor Mobile (Recomendado)

```bash
# Parar servidor atual
taskkill /F /PID 17080

# Iniciar servidor mobile
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile --server.port=8081
```

### Opção 2: Verificar Logs

```bash
# Ver logs do servidor
Get-Content logs/sistema-inventario-prod.log -Tail 50
```

Procurar por:
- Mensagens de erro no startup
- Porta que o servidor está usando
- Perfil ativo (mobile ou default)
- Endpoints registrados

### Opção 3: Testar Endpoint Desktop

Se o servidor estiver no modo desktop:
```bash
# Testar porta 8080 (desktop)
Invoke-WebRequest -Uri "http://localhost:8080/" -TimeoutSec 5
```

## 📋 Checklist de Verificação

- [ ] Servidor está rodando?
- [ ] Porta 8081 está aberta?
- [ ] Perfil mobile está ativo?
- [ ] Endpoints estão registrados?
- [ ] Não há erros nos logs?
- [ ] Context path está correto?

## 🚀 Comando Correto para Iniciar

```bash
# Navegar para o diretório
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO

# Iniciar servidor mobile
java -jar target/sistema-inventario-1.2.0.jar `
  --spring.profiles.active=mobile `
  --server.port=8081 `
  --logging.level.com.inventario=DEBUG
```

## 🔍 Como Verificar se Funcionou

Após reiniciar, testar:
```powershell
# 1. Testar raiz
Invoke-WebRequest -Uri "http://localhost:8081/"

# 2. Testar login (deve retornar 400 ou 401, não 404)
Invoke-WebRequest -Uri "http://localhost:8081/api/mobile/auth/login" `
  -Method POST `
  -Body '{"username":"admin","password":"admin123"}' `
  -ContentType "application/json"
```

**Resultado esperado:**
- ✅ Status 400 (Bad Request) ou 401 (Unauthorized) = Endpoint existe!
- ❌ Status 404 (Not Found) = Endpoint não existe

---

**Diagnóstico em:** 18/11/2025 01:05  
**Status:** Servidor rodando mas endpoints não respondem  
**Ação necessária:** Reiniciar servidor mobile
