# Erro ao Carregar Responsáveis - Conexão Fechada

**Data**: 26/11/2025  
**Erro**: `IllegalStateException: closed`  
**Causa**: Servidor mobile não está rodando ou conexão foi interrompida

---

## 🔴 Erro Identificado

```
❌ ERRO ao carregar responsáveis!
Tipo: IllegalStateException
Mensagem: closed

java.lang.IllegalStateException: closed
    at okhttp3.internal.connection.ExchangeResponseBodySource.read(Exchange.kt:279)
    at okio.RealBufferedSource.readAll(RealBufferedSource.kt:287)
    at retrofit2.Utils.buffer(Utils.java:323)
    at retrofit2.OkHttpCall.parseResponse(OkHttpCall.java:229)
    at retrofit2.OkHttpCall$1.onResponse(OkHttpCall.java:153)
```

---

## 🔍 Causa Raiz

O erro `closed` do OkHttp indica que:
1. **Servidor mobile NÃO está rodando** na porta 8081
2. **Conexão foi fechada** antes de completar a requisição
3. **Timeout** ou problema de rede

---

## ✅ Solução

### Passo 1: Iniciar o Servidor Mobile

```bash
# Opção 1: Usando Maven
mvn spring-boot:run -Dspring-boot.run.profiles=mobile

# Opção 2: Usando script
restart-mobile-server.bat
```

### Passo 2: Verificar se o Servidor Está Rodando

```bash
# Testar endpoint de responsáveis
curl http://localhost:8081/api/mobile/responsaveis

# Ou testar health check
curl http://localhost:8081/actuator/health
```

### Passo 3: Verificar IP no App

Se estiver usando emulador, o IP deve ser `10.0.2.2` ao invés de `localhost`:

1. Abrir app no emulador
2. Ir em Configurações
3. Verificar "IP do Servidor"
4. Deve estar: `http://10.0.2.2:8081`

---

## 🔧 Configuração Correta

### No Emulador Android

**IP do Servidor**: `10.0.2.2` (não usar `localhost` ou `127.0.0.1`)  
**Porta**: `8081`  
**URL Completa**: `http://10.0.2.2:8081`

### No Dispositivo Físico

**IP do Servidor**: IP real da máquina (ex: `192.168.1.100`)  
**Porta**: `8081`  
**URL Completa**: `http://192.168.1.100:8081`

---

## 📝 Checklist de Verificação

- [ ] Servidor mobile está rodando na porta 8081
- [ ] Endpoint `/api/mobile/responsaveis` responde com 200 OK
- [ ] IP configurado no app está correto (`10.0.2.2` para emulador)
- [ ] Firewall não está bloqueando a porta 8081
- [ ] Há responsáveis cadastrados no banco de dados

---

## 🧪 Teste Rápido

### 1. Iniciar Servidor
```bash
cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO
mvn spring-boot:run -Dspring-boot.run.profiles=mobile
```

### 2. Aguardar Inicialização
Aguarde até ver:
```
Started MobileApiApplication in X seconds
```

### 3. Testar Endpoint
```bash
curl http://localhost:8081/api/mobile/responsaveis
```

Deve retornar:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "nome": "Nome do Responsável",
      ...
    }
  ]
}
```

### 4. Reabrir Tela no App
1. Fechar tela de Inventário
2. Reabrir tela de Inventário
3. Spinner deve carregar os responsáveis

---

## 🔍 Diagnóstico Adicional

### Verificar Porta 8081
```bash
# Windows
netstat -ano | findstr :8081

# Se não retornar nada, servidor não está rodando
```

### Verificar Logs do Servidor
```bash
# Procurar por erros de inicialização
tail -f logs/server.log
```

### Verificar Banco de Dados
```sql
-- Verificar se há responsáveis ativos
SELECT id, nome, ativo FROM responsavel WHERE ativo = true;
```

---

## 🎯 Solução Rápida

Execute este comando para iniciar o servidor e testar:

```bash
# 1. Iniciar servidor
start cmd /k "cd C:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO && mvn spring-boot:run -Dspring-boot.run.profiles=mobile"

# 2. Aguardar 30 segundos

# 3. Testar endpoint
curl http://localhost:8081/api/mobile/responsaveis

# 4. Se retornar dados, reabrir tela no app
```

---

## 📊 Resultado Esperado

Após iniciar o servidor:

```
InventarioViewModel: INICIANDO CARREGAMENTO DE RESPONSÁVEIS
InventarioRepository: Buscando responsáveis...
InventarioRepository: ✓ 15 responsáveis carregados
InventarioViewModel: ✓ Responsáveis carregados com sucesso: 15 itens
InventarioActivity: ✓ Spinner configurado com sucesso com 15 responsáveis
```

---

**Problema**: Servidor mobile não está rodando  
**Solução**: Iniciar servidor na porta 8081  
**Status**: ⏳ Aguardando inicialização do servidor
