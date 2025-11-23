# 🚀 Início Rápido - Monitor de Dispositivos Mobile

## ⚡ 3 Passos para Usar

### 1️⃣ Iniciar Servidor Mobile
```bash
java -jar target/sistema-inventario.jar --spring.profiles.active=mobile
```

### 2️⃣ Abrir Sistema Desktop
```bash
java -jar target/sistema-inventario.jar
```

### 3️⃣ Acessar Monitor
```
Menu: Sistema → Monitor de Usuários Mobile
```

---

## ✅ O Que Você Verá

```
┌─────────────────────────────────────────────────────────┐
│ URL do Servidor Mobile: [http://localhost:8080] [Salvar]│
│ 🟢 Servidor: Online                                      │
├─────────────────────────────────────────────────────────┤
│ Conectados: 3    Usuários: 2    Última Atualização: ... │
├─────────────────────────────────────────────────────────┤
│ [Tabela com dispositivos conectados]                    │
├─────────────────────────────────────────────────────────┤
│ [🔄 Atualizar] [▶️ Auto Refresh] [📋 Detalhes] ...      │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Funcionalidades Principais

### 🔄 Atualizar
Atualiza lista de dispositivos manualmente

### ▶️ Auto Refresh
Atualização automática a cada 10 segundos

### 📋 Ver Detalhes
Mostra informações completas do dispositivo selecionado

### 🔌 Desconectar
Remove dispositivo das conexões ativas

### 🧹 Limpar Inativos
Remove dispositivos sem atividade (>5 minutos)

---

## 🔧 Configuração Rápida

### Mudar URL do Servidor
1. Editar campo "URL do Servidor Mobile"
2. Clicar em "💾 Salvar URL"
3. Sistema testa conexão automaticamente

### Servidor Remoto
```
URL: http://192.168.1.100:8080
```

### Servidor Local
```
URL: http://localhost:8080
```

---

## ⚠️ Problemas Comuns

### 🔴 Servidor: Offline
**Causa:** Servidor mobile não está rodando  
**Solução:** Iniciar servidor com comando do passo 1

### Tabela Vazia
**Causa:** Nenhum dispositivo conectado  
**Solução:** Normal! Conecte um smartphone ao servidor

### Timeout
**Causa:** Servidor lento ou URL incorreta  
**Solução:** Verificar URL e firewall

---

## 📱 Conectar Smartphone

### 1. Mesmo Wi-Fi
Smartphone e PC devem estar na mesma rede

### 2. Configurar App
```
URL: http://[IP_DO_PC]:8080/inventario
Exemplo: http://192.168.1.100:8080/inventario
```

### 3. Fazer Login
Usar credenciais do sistema

### 4. Verificar no Monitor
Dispositivo aparece na tabela automaticamente

---

## 🧪 Teste Rápido

```bash
# 1. Testar servidor
curl http://localhost:8080/api/mobile/v1/connection/stats

# 2. Ver dispositivos conectados
curl http://localhost:8080/api/mobile/v1/connection/active

# 3. Executar script de teste
testar-integracao-monitor.bat
```

---

## 📚 Documentação Completa

- `IMPLEMENTACAO_GERENCIAMENTO_DISPOSITIVOS_COMPLETA.md` - Técnica
- `INTEGRACAO_MONITOR_DISPOSITIVOS_MAINFRAME.md` - Integração
- `RESUMO_SESSAO_GERENCIAMENTO_DISPOSITIVOS.md` - Resumo

---

## 🎉 Pronto!

Agora você pode monitorar dispositivos mobile em tempo real! 🚀
