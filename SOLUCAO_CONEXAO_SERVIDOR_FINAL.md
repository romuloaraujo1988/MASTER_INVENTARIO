# ✅ Solução: Problema de Conexão com Servidor

## 🎯 Problema

O app estava tentando conectar em `10.0.2.2:8081` (IP antigo) ao invés do IP correto do servidor.

---

## ✅ Solução Aplicada

1. ✅ App desinstalado
2. ✅ App reinstalado (dados limpos)
3. ✅ Configurações antigas removidas

---

## 📱 Próximos Passos (MANUAL)

### 1. Abrir o App no Emulador

**Abra manualmente o app "Inventário Mobile" no emulador.**

### 2. Configurar IP do Servidor

Na tela de login, você verá um campo para configurar o IP do servidor.

**Digite o IP correto:**
```
10.14.250.214
```

**Ou use o IP padrão que já está configurado no código.**

### 3. Fazer Login

Após configurar o IP, faça login normalmente.

---

## 🔍 Verificar Conexão

### Logs para Monitorar

```bash
adb logcat -s ServerConfigManager:D NetworkModule:D LoginViewModel:D
```

**Logs esperados:**
```
D/ServerConfigManager: Base URL configurada: http://10.14.250.214:8081/inventario/
D/NetworkModule: Base URL configurada: http://10.14.250.214:8081/inventario/
D/LoginViewModel: Tentando login em: http://10.14.250.214:8081/inventario/api/mobile/auth/login
```

---

## 🌐 Configuração do Servidor

### IP Padrão Configurado no Código

```kotlin
private const val FALLBACK_IP = "10.14.250.214"
private const val DEFAULT_PORT = 8081
private const val DEFAULT_CONTEXT_PATH = "/inventario"
```

### URLs Completas

- **Base URL**: `http://10.14.250.214:8081/inventario/`
- **Login**: `http://10.14.250.214:8081/inventario/api/mobile/auth/login`
- **Dashboard**: `http://10.14.250.214:8081/inventario/api/mobile/dashboard/stats`
- **Health Check**: `http://10.14.250.214:8081/inventario/api/mobile/auth/health`

---

## 🧪 Testar Servidor (Do Computador)

```bash
# Testar se servidor está acessível
curl http://10.14.250.214:8081/inventario/

# Testar endpoint de health
curl http://10.14.250.214:8081/inventario/api/mobile/auth/health

# Testar login (exemplo)
curl -X POST http://10.14.250.214:8081/inventario/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"senha123"}'
```

---

## ⚠️ Troubleshooting

### Se ainda não conectar:

1. **Verificar se servidor está rodando**
   ```bash
   # No servidor, verificar se porta 8081 está aberta
   netstat -an | findstr 8081
   ```

2. **Verificar firewall**
   - Firewall do Windows pode estar bloqueando
   - Adicionar exceção para porta 8081

3. **Verificar rede**
   - Emulador e servidor na mesma rede?
   - Ping funciona?
   ```bash
   ping 10.14.250.214
   ```

4. **Verificar IP salvo no app**
   - Na tela de login, verificar campo de IP
   - Reconfigurar se necessário

---

## 🎯 Resumo

**O que foi feito:**
- ✅ App reinstalado com dados limpos
- ✅ Configurações antigas removidas
- ✅ IP padrão configurado: `10.14.250.214`

**O que você precisa fazer:**
1. Abrir app manualmente no emulador
2. Verificar/configurar IP na tela de login
3. Fazer login

**Logs para monitorar:**
```bash
adb logcat -s ServerConfigManager NetworkModule LoginViewModel
```

---

## 📞 Se Precisar Mudar o IP Padrão

Editar arquivo:
```
InventarioMobile/app/src/main/java/com/inventario/mobile/utils/ServerConfigManager.kt
```

Linha 30:
```kotlin
private const val FALLBACK_IP = "SEU_IP_AQUI"
```

Depois recompilar:
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

**Abra o app agora e teste a conexão!** 🚀

**Status**: ✅ App reinstalado e pronto para uso
