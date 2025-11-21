# Configuração do IP Padrão - 10.14.250.214

## ✅ Alteração Realizada

O IP padrão do servidor foi alterado para **10.14.250.214**.

## 📝 Detalhes da Configuração

### Arquivo Modificado
- **Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/ServerConfigManager.kt`
- **Linha:** 30

### Alteração

**Antes:**
```kotlin
private const val FALLBACK_IP = "192.168.10.107"  // Usado se resources não estiverem disponíveis
```

**Depois:**
```kotlin
private const val FALLBACK_IP = "10.14.250.214"  // IP padrão do servidor
```

## 🔧 Configurações Completas

### Servidor Padrão
- **IP:** `10.14.250.214`
- **Porta:** `8081`
- **Protocolo:** `HTTP` (rede local)
- **Context Path:** `/inventario`
- **API Path:** `/api/mobile`

### URL Base Completa
```
http://10.14.250.214:8081/inventario/
```

### Endpoints Principais
```
Login:        http://10.14.250.214:8081/inventario/api/mobile/auth/login
Patrimônios:  http://10.14.250.214:8081/inventario/api/mobile/patrimonio
Coletas:      http://10.14.250.214:8081/inventario/api/mobile/coletas
Salas:        http://10.14.250.214:8081/inventario/api/mobile/salas
Sync:         http://10.14.250.214:8081/inventario/api/mobile/sync
```

## 📱 Comportamento do App

### Primeira Inicialização
1. App usa IP padrão: `10.14.250.214:8081`
2. Usuário pode alterar nas configurações se necessário
3. Nova configuração é salva localmente

### Detecção Automática
O app detecta automaticamente se o IP é:
- **Rede Local (10.x.x.x):** Usa HTTP
- **Rede Externa:** Sugere HTTPS

### Configuração Manual
Usuário pode alterar o IP em:
- Tela de Login → Configurações
- Tela de Configurações do App

## 🧪 Teste de Conectividade

O app testa automaticamente a conectividade com:
```
http://10.14.250.214:8081/inventario/api/mobile/health
```

### Timeouts Configurados
- **Conexão:** 45 segundos
- **Leitura:** 120 segundos
- **Escrita:** 60 segundos
- **Total:** 180 segundos (3 minutos)

## 🔄 Fallback e Redundância

### Ordem de Prioridade
1. **IP configurado pelo usuário** (salvo em SharedPreferences)
2. **IP dos resources** (strings.xml)
3. **IP padrão (FALLBACK_IP):** `10.14.250.214`

### Detecção de Rede
O app detecta automaticamente:
- IP local do dispositivo
- Rede local (10.x.x.x, 192.168.x.x, 172.16-31.x.x)
- Sugere configurações apropriadas

## 📊 Informações da Rede

### Rede 10.14.250.x
- **Tipo:** Rede privada classe A
- **Range:** 10.0.0.0 - 10.255.255.255
- **Máscara típica:** 255.255.255.0
- **Gateway provável:** 10.14.250.1
- **Broadcast:** 10.14.250.255

### Características
- ✅ Rede privada (não roteável na internet)
- ✅ Ideal para redes corporativas
- ✅ Suporta grande número de dispositivos
- ✅ Segura para comunicação interna

## 🚀 APK Gerado

### Informações do Build
- **Status:** ✅ BUILD SUCCESSFUL
- **Localização:** `InventarioMobile/app/build/outputs/apk/debug/app-debug.apk`
- **Tamanho:** ~11.4 MB
- **IP Padrão:** `10.14.250.214`
- **Porta Padrão:** `8081`
- **Data:** 19/11/2025

### Como Instalar
```bash
# Via ADB
adb install -r app-debug.apk

# Ou copiar para dispositivo e instalar manualmente
```

## 🔍 Verificação

### Como Verificar o IP Configurado

**Logs do App:**
```
D/ServerConfigManager: Base URL configurada: http://10.14.250.214:8081/inventario/
D/NetworkModule: Base URL configurada: http://10.14.250.214:8081/inventario/
```

**Tela de Login:**
- O IP aparece nas configurações
- Pode ser alterado pelo usuário

**Teste de Conectividade:**
```kotlin
// O app testa automaticamente ao iniciar
ServerConfigManager.testServerConnectivity()
```

## ⚙️ Configurações Avançadas

### Alterar IP Programaticamente
```kotlin
val serverConfigManager = ServerConfigManager.getInstance(context)
serverConfigManager.setServerIp("10.14.250.214", port = 8081, useHttps = false)
```

### Obter Configuração Atual
```kotlin
val config = serverConfigManager.getServerConfig()
println("IP: ${config.ip}")
println("Porta: ${config.port}")
println("Base URL: ${config.baseUrl}")
```

### Testar Conectividade
```kotlin
lifecycleScope.launch {
    val result = serverConfigManager.testServerConnectivity()
    if (result.success) {
        println("Conectado! Tempo: ${result.responseTime}ms")
    } else {
        println("Erro: ${result.message}")
    }
}
```

## 📝 Notas Importantes

### Segurança
- ✅ IP está em rede privada (10.x.x.x)
- ✅ Não é acessível pela internet
- ✅ Requer VPN ou estar na mesma rede

### Performance
- ✅ Rede local = baixa latência
- ✅ Timeouts generosos para sincronização
- ✅ Retry automático em falhas

### Manutenção
- ✅ IP pode ser alterado sem recompilar
- ✅ Configuração salva localmente
- ✅ Fácil de atualizar para usuários

## 🔄 Próximas Ações

### Para Usuários
1. Instalar APK no dispositivo
2. Abrir app
3. Fazer login (usará IP padrão automaticamente)
4. Se necessário, alterar IP nas configurações

### Para Desenvolvedores
1. Servidor deve estar rodando em `10.14.250.214:8081`
2. Context path deve ser `/inventario`
3. Endpoints devem estar em `/api/mobile/`

### Para Administradores
1. Garantir que servidor está acessível na rede
2. Firewall deve permitir porta 8081
3. Configurar DNS se necessário (opcional)

---

**Configurado em:** 19/11/2025  
**IP Padrão:** 10.14.250.214  
**Porta:** 8081  
**Status:** ✅ CONFIGURADO E COMPILADO
