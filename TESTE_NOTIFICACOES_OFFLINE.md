# Como Testar Notificações de Modo Offline

## 🧪 Passos para Testar

### 1. Preparar o Emulador
```bash
# Verificar se emulador está rodando
adb devices

# Instalar APK
adb install -r InventarioMobile\app\build\outputs\apk\debug\app-debug.apk

# Iniciar app
adb shell monkey -p com.inventario.mobile.debug -c android.intent.category.LAUNCHER 1
```

### 2. Conceder Permissão de Notificações

**No emulador:**
1. Quando o app abrir, aparecerá um popup pedindo permissão de notificações
2. Clique em **"Permitir"** / **"Allow"**

**Ou manualmente:**
1. Configurações → Apps → Inventário Mobile
2. Notificações → Ativar

### 3. Simular Perda de Conexão

**Opção A: Via Emulador (Recomendado)**
1. No emulador, arraste de cima para baixo (barra de notificações)
2. Clique no ícone de WiFi para desativar
3. Ou: Configurações → Rede e Internet → WiFi → Desligar

**Opção B: Via ADB**
```bash
# Desabilitar WiFi
adb shell svc wifi disable

# Desabilitar dados móveis
adb shell svc data disable

# Modo avião
adb shell cmd connectivity airplane-mode enable
```

### 4. Verificar Notificações

Você deve ver:
- 📴 **Notificação:** "Modo Offline"
- **Texto:** "Trabalhando em modo offline..." ou "Sem dados locais..."

### 5. Restaurar Conexão

**Via Emulador:**
1. Ativar WiFi novamente

**Via ADB:**
```bash
# Habilitar WiFi
adb shell svc wifi enable

# Habilitar dados
adb shell svc data enable

# Desativar modo avião
adb shell cmd connectivity airplane-mode disable
```

### 6. Verificar Notificação de Reconexão

Você deve ver:
- 🌐 **Notificação:** "Conexão Restaurada"

---

## 🔍 Verificar Logs

```bash
# Ver logs do app
adb logcat -s LoginViewModel:* NetworkMonitor:* OfflineNotificationManager:*

# Ver todas as notificações
adb shell dumpsys notification
```

---

## ⚠️ Troubleshooting

### Notificações não aparecem?

**1. Verificar permissão:**
```bash
adb shell dumpsys package com.inventario.mobile.debug | findstr "POST_NOTIFICATIONS"
```

**2. Verificar se canal foi criado:**
```bash
adb shell dumpsys notification | findstr "offline_mode_channel"
```

**3. Forçar permissão via ADB:**
```bash
adb shell pm grant com.inventario.mobile.debug android.permission.POST_NOTIFICATIONS
```

**4. Limpar dados do app e reinstalar:**
```bash
adb shell pm clear com.inventario.mobile.debug
adb install -r InventarioMobile\app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 Comportamento Esperado

### Cenário 1: Sem Conexão + Sem Dados Locais
- ❌ Campos de login desabilitados
- 📴 Notificação: "Sem conexão e sem dados locais"
- ⚠️ Notificação: "Sincronização Necessária" (com botão)
- 💬 Mensagem na tela: "Conecte-se à internet para fazer o primeiro login"

### Cenário 2: Sem Conexão + Com Dados Locais
- ✅ Botão de biometria/PIN habilitado
- 📴 Notificação: "Trabalhando em modo offline"
- ✅ Login offline funciona
- 💾 Coletas salvas localmente

### Cenário 3: Conexão Restaurada
- 🌐 Notificação: "Conexão Restaurada"
- 🔄 Se tem dados pendentes: botão "Sincronizar Agora"
- ⚡ Sincronização automática em background

---

## 🎯 Indicadores Visuais

### Na Tela de Login:
- **Canto superior esquerdo:**
  - 🟢 "ONLINE" (verde) quando conectado
  - 🟡 "OFFLINE" (amarelo) quando desconectado

### Notificações:
- 📴 Ícone offline (vermelho)
- 🌐 Ícone online (verde)
- 🔄 Ícone sync (verde)
- ✅ Ícone check (verde)

---

**Última atualização:** 22/11/2025
**Status:** ✅ Implementado e pronto para testes

