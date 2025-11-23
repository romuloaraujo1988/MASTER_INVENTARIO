# 🔧 Correção: Problema de Conexão com Servidor

## 🐛 Problema Identificado

O app está tentando conectar em `10.0.2.2:8081` (IP antigo salvo nas preferências) ao invés de usar o IP correto do servidor.

**Erro nos logs:**
```
java.net.SocketTimeoutException: failed to connect to /10.0.2.2 (port 8081) 
from /10.0.2.16 (port 59560) after 5000ms
```

---

## 🔍 Causa Raiz

O `PreferencesManager` tem uma URL antiga salva que está sobrescrevendo o IP padrão configurado no código (`10.14.250.214`).

---

## ✅ Soluções

### Solução 1: Limpar Dados do App (Mais Rápido)

```bash
# Limpar dados do app no emulador
adb shell pm clear com.inventario.mobile

# Reinstalar APK
cd InventarioMobile
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**Resultado**: App vai usar o IP padrão `10.14.250.214:8081`

---

### Solução 2: Reconfigurar IP na Tela de Login

1. Abrir app
2. Na tela de login, digitar o IP correto: `10.14.250.214`
3. Fazer login

**Resultado**: IP será salvo nas preferências

---

### Solução 3: Forçar IP via ADB (Avançado)

```bash
# Forçar configuração do servidor
adb shell am start -n com.inventario.mobile/.presentation.login.LoginActivity \
  --es server_ip "10.14.250.214" \
  --ei server_port 8081
```

---

## 🔧 Verificação

### Verificar IP Configurado

```bash
# Ver logs do app
adb logcat -s ServerConfigManager NetworkModule

# Procurar por:
# "Base URL configurada: http://10.14.250.214:8081/inventario/"
```

### Testar Conexão

```bash
# Do computador, testar se servidor está acessível
curl http://10.14.250.214:8081/inventario/api/mobile/auth/health

# Deve retornar algo como:
# {"status":"UP"}
```

---

## 📝 IP Correto do Servidor

**IP Padrão Configurado**: `10.14.250.214`  
**Porta**: `8081`  
**Context Path**: `/inventario`  
**URL Base**: `http://10.14.250.214:8081/inventario/`  
**API Path**: `/api/mobile`

**URL Completa de Login**: `http://10.14.250.214:8081/inventario/api/mobile/auth/login`

---

## 🚀 Ação Recomendada

**Execute agora:**

```bash
# 1. Limpar dados do app
adb shell pm clear com.inventario.mobile

# 2. Reinstalar
cd InventarioMobile
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 3. Abrir app
adb shell am start -n com.inventario.mobile/.presentation.login.LoginActivity

# 4. Verificar logs
adb logcat -s ServerConfigManager:D NetworkModule:D LoginViewModel:D
```

---

## 🔍 Debug Adicional

Se ainda não funcionar, verificar:

1. **Servidor está rodando?**
   ```bash
   curl http://10.14.250.214:8081/inventario/
   ```

2. **Firewall bloqueando?**
   - Verificar firewall do Windows
   - Verificar firewall do servidor

3. **Rede correta?**
   - Emulador e servidor na mesma rede?
   - VPN ativa?

---

**Aplique a Solução 1 agora para resolver rapidamente!** ✅
