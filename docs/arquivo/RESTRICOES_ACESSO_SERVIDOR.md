# Restrições que Podem Impedir o App Android de Acessar o Servidor

## Status Atual do Servidor
✅ **Servidor está rodando na porta 8081** (processo PID: 20172)

## 1. Restrições de Rede no Android

### 1.1 Network Security Config
**Localização:** `InventarioMobile/app/src/main/res/xml/network_security_config.xml`

**Status Atual:**
- ✅ `cleartextTrafficPermitted="true"` habilitado no AndroidManifest
- ✅ IPs locais permitidos: 10.14.250.238, 192.168.1.100, 192.168.0.100, 192.168.10.107, localhost, 127.0.0.1, 10.0.2.2
- ⚠️ **PROBLEMA POTENCIAL:** Se o IP do servidor não estiver na lista, o Android 14+ bloqueará a conexão HTTP

**Solução:**
```xml
<!-- Adicionar o IP real do servidor à lista -->
<domain includeSubdomains="false">SEU_IP_AQUI</domain>
```

### 1.2 Permissões no AndroidManifest
**Status:**
- ✅ `INTERNET` - Permitida
- ✅ `ACCESS_NETWORK_STATE` - Permitida
- ✅ `usesCleartextTraffic="true"` - Habilitado

## 2. Configuração do Servidor no App

### 2.1 URL Base Configurada
**Gerenciador:** `ServerConfigManager.kt`

**Configuração Padrão:**
- IP: `10.0.2.2` (emulador) ou configurado pelo usuário
- Porta: `8081`
- Protocolo: `http://`
- Context Path: `/inventario`
- API Path: `/api/mobile`

**URL Completa:** `http://IP:8081/inventario/api/mobile/`

**Verificar:**
```kotlin
// O usuário precisa configurar o IP correto no app
// Tela de configuração ou primeira execução
```

### 2.2 Endpoints Disponíveis
```
✅ /api/mobile/health - Público (teste de conectividade)
✅ /api/mobile/test - Público
✅ /api/mobile/auth/login - Público
✅ /api/mobile/dashboard/** - Público (temporário)
✅ /api/mobile/coletas/** - Público (temporário)
✅ /api/mobile/patrimonios/** - Público (temporário)
✅ /api/mobile/salas/** - Público (temporário)
✅ /api/mobile/setores/** - Público (temporário)
✅ /api/mobile/descricoes/** - Público (temporário)
🔒 /api/mobile/sync/** - Requer autenticação
```

## 3. Restrições de Firewall

### 3.1 Firewall do Windows
**Verificar se a porta 8081 está liberada:**

```powershell
# Verificar regras existentes
netsh advfirewall firewall show rule name=all | Select-String "8081"

# Liberar porta (se necessário)
netsh advfirewall firewall add rule name="Sistema Inventario Mobile" dir=in action=allow protocol=TCP localport=8081
```

### 3.2 Firewall do Roteador
- Verificar se o roteador permite conexões na porta 8081
- Verificar se há isolamento de rede WiFi (AP Isolation)
- Alguns roteadores bloqueiam comunicação entre dispositivos WiFi

## 4. Configuração do Servidor Backend

### 4.1 Binding do Servidor
**Arquivo:** `application-mobile.properties`

```properties
server.port=8081
server.address=0.0.0.0  ✅ Correto - aceita conexões de qualquer IP
```

### 4.2 CORS Configuration
**Status:** ✅ Configurado corretamente

```java
// MobileSecurityConfig.java
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(Arrays.asList("*"));
configuration.setAllowCredentials(true);
```

## 5. Problemas Específicos do Android 14+

### 5.1 Restrições de Cleartext Traffic
**Android 14 (API 34)** é mais restritivo com HTTP:

**Verificações:**
1. ✅ `android:usesCleartextTraffic="true"` no AndroidManifest
2. ✅ `android:networkSecurityConfig="@xml/network_security_config"` configurado
3. ⚠️ IP do servidor deve estar explicitamente na lista de domínios permitidos

### 5.2 Timeouts Aumentados
**NetworkModule.kt** já está configurado com timeouts maiores:
```kotlin
.connectTimeout(45, TimeUnit.SECONDS)
.readTimeout(60, TimeUnit.SECONDS)
.writeTimeout(60, TimeUnit.SECONDS)
.callTimeout(120, TimeUnit.SECONDS)
```

## 6. Problemas de Rede Local

### 6.1 Dispositivo e Servidor na Mesma Rede?
**Verificar:**
- Smartphone e computador devem estar na mesma rede WiFi
- Obter IP do computador: `ipconfig` (Windows) ou `ifconfig` (Linux/Mac)
- Configurar esse IP no app Android

### 6.2 IP Dinâmico
**Problema:** O IP do computador pode mudar
**Solução:** 
- Configurar IP estático no roteador
- Ou reconfigurar o app sempre que o IP mudar

### 6.3 VPN Ativa
- VPNs podem bloquear conexões locais
- Desabilitar VPN durante testes

## 7. Problemas de Autenticação

### 7.1 Token JWT
**NetworkModule.kt** adiciona automaticamente o token:
```kotlin
.addHeader("Authorization", "Bearer $token")
```

**Verificar:**
- Token está sendo salvo após login?
- Token não expirou?
- Token é válido?

### 7.2 Endpoints Públicos vs Protegidos
**Temporariamente liberados sem autenticação:**
- Dashboard
- Coletas
- Patrimônios
- Salas
- Setores
- Descrições

## 8. Checklist de Diagnóstico

### Passo 1: Verificar Servidor
```powershell
# Servidor está rodando?
netstat -ano | Select-String ":8081"

# Testar localmente
curl http://localhost:8081/inventario/api/mobile/health
```

### Passo 2: Verificar IP do Computador
```powershell
ipconfig
# Anotar o IPv4 Address da rede WiFi
```

### Passo 3: Verificar Conectividade do Smartphone
```bash
# No smartphone, usar app de terminal ou navegador
# Testar: http://IP_DO_COMPUTADOR:8081/inventario/api/mobile/health
```

### Passo 4: Adicionar IP ao Network Security Config
```xml
<!-- InventarioMobile/app/src/main/res/xml/network_security_config.xml -->
<domain includeSubdomains="false">192.168.X.X</domain>
```

### Passo 5: Recompilar e Reinstalar App
```bash
cd InventarioMobile
.\gradlew.bat clean assembleDebug
.\gradlew.bat installDebug
```

### Passo 6: Configurar IP no App
- Abrir app
- Ir para configurações
- Inserir IP do computador
- Porta: 8081
- Testar conexão

## 9. Ferramentas de Diagnóstico

### 9.1 No App Android
**NetworkDiagnosticActivity** - Tela de diagnóstico de rede
- Testa conectividade
- Mostra informações da rede
- Valida configuração

### 9.2 Android14NetworkConfig
```kotlin
// Testa conectividade com o servidor
Android14NetworkConfig.testServerConnectivity(serverIp, serverPort)

// Obtém informações da rede
Android14NetworkConfig.getNetworkInfo(context)
```

### 9.3 ServerConfigManager
```kotlin
// Testa conectividade
serverConfigManager.testServerConnectivity()

// Valida configuração
serverConfigManager.getServerConfig()
```

## 10. Soluções Rápidas

### Problema: "Connection refused"
**Causa:** Servidor não está rodando ou firewall bloqueando
**Solução:**
1. Iniciar servidor: `mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication"`
2. Liberar firewall: `netsh advfirewall firewall add rule name="Inventario" dir=in action=allow protocol=TCP localport=8081`

### Problema: "Unknown host"
**Causa:** IP incorreto ou DNS não resolve
**Solução:**
1. Verificar IP: `ipconfig`
2. Usar IP numérico, não hostname
3. Adicionar IP ao network_security_config.xml

### Problema: "Cleartext HTTP traffic not permitted"
**Causa:** Android 14+ bloqueando HTTP
**Solução:**
1. Adicionar IP ao network_security_config.xml
2. Recompilar app
3. Ou usar HTTPS (requer certificado)

### Problema: "Timeout"
**Causa:** Rede lenta ou servidor sobrecarregado
**Solução:**
1. Verificar conexão WiFi
2. Timeouts já estão aumentados no código
3. Verificar logs do servidor

### Problema: "401 Unauthorized"
**Causa:** Token inválido ou expirado
**Solução:**
1. Fazer login novamente
2. Verificar se endpoints estão liberados (MobileSecurityConfig)
3. Verificar logs de autenticação

## 11. Logs Importantes

### Backend (Servidor)
```
logging.level.com.inventario.controller.mobile=DEBUG
```

### Android App
```kotlin
Log.d("NetworkModule", "...")
Log.d("ServerConfigManager", "...")
Log.d("Android14NetworkConfig", "...")
```

## 12. Recomendações Finais

1. **Sempre usar IP numérico** (não hostname) para evitar problemas de DNS
2. **Adicionar IP ao network_security_config.xml** antes de compilar
3. **Verificar firewall** do Windows e roteador
4. **Mesma rede WiFi** para smartphone e computador
5. **Desabilitar VPN** durante desenvolvimento
6. **Usar tela de diagnóstico** do app para validar conectividade
7. **Verificar logs** tanto do servidor quanto do app
8. **Testar endpoint /health** primeiro antes de outros endpoints

## 13. Configuração Ideal para Desenvolvimento

```properties
# Backend - application-mobile.properties
server.port=8081
server.address=0.0.0.0
```

```xml
<!-- Android - network_security_config.xml -->
<domain includeSubdomains="false">192.168.1.XXX</domain>
```

```kotlin
// Android - Configuração no app
IP: 192.168.1.XXX (IP real do computador)
Porta: 8081
Protocolo: HTTP
```

**URL Final:** `http://192.168.1.XXX:8081/inventario/api/mobile/`
