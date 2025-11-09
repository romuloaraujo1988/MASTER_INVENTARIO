# Solução: Timeout na Conexão do App Mobile

## 🔍 Erro Identificado

```
java.net.SocketTimeoutException: failed to connect to /10.0.2.2 (port 8081) 
from /10.0.2.16 (port 59784) after 5000ms
```

## 📊 Análise

### O que está acontecendo:
- **App está tentando**: `10.0.2.2:8081`
- **Servidor está em**: Outro IP (provavelmente `192.168.x.x`)
- **Timeout**: 5 segundos

### Por que `10.0.2.2`?
O `10.0.2.2` é um IP especial do emulador Android que aponta para o `localhost` (127.0.0.1) da máquina host. Funciona **APENAS** se o servidor estiver rodando na mesma máquina do emulador.

## ✅ Soluções

### Solução 1: Descobrir IP Real do Servidor (RECOMENDADO)

#### Passo 1: Descobrir IP da Máquina
No Windows, abra o Prompt de Comando e execute:
```cmd
ipconfig
```

Procure por **"Endereço IPv4"** na seção da sua rede ativa (Wi-Fi ou Ethernet).

Exemplo de saída:
```
Adaptador de Rede sem Fio Wi-Fi:
   Endereço IPv4. . . . . . . . . . : 192.168.10.107
```

#### Passo 2: Configurar no App

**Opção A: Via Interface do App**
1. Abra o app no emulador
2. Vá em **Configurações** ou **Settings**
3. Procure por **"Configuração do Servidor"** ou **"Server Config"**
4. Digite o IP encontrado: `192.168.10.107`
5. Porta: `8081`
6. Salve e teste a conexão

**Opção B: Limpar Dados do App**
1. No emulador, vá em **Settings → Apps**
2. Encontre o app **Inventário Mobile**
3. Clique em **Storage → Clear Data**
4. Abra o app novamente
5. Configure o IP correto

### Solução 2: Usar IP Padrão Correto

Edite o arquivo de configuração do app:

**Arquivo**: `InventarioMobile/app/src/main/res/values/server_config.xml`

```xml
<!-- IP padrão do servidor (pode ser alterado aqui) -->
<string name="default_server_ip">192.168.10.107</string>  <!-- ← SEU IP AQUI -->
```

Depois recompile e reinstale o app:
```bash
cd InventarioMobile
.\gradlew.bat clean assembleDebug installDebug
```

### Solução 3: Servidor Acessível de Qualquer IP

Se o servidor estiver configurado para aceitar apenas localhost, precisa aceitar conexões externas.

**Verificar configuração do Spring Boot**:

**Arquivo**: `application-mobile.properties`

```properties
# Aceitar conexões de qualquer IP
server.address=0.0.0.0
server.port=8081
```

Reinicie o servidor após a mudança.

### Solução 4: Usar Emulador com Rede Bridge (Avançado)

Configure o emulador para usar rede bridge, assim ele terá um IP real na rede:

1. Feche o emulador
2. Abra AVD Manager
3. Edite o dispositivo virtual
4. Em **Advanced Settings → Network**
5. Mude de **NAT** para **Bridge**
6. Reinicie o emulador

## 🧪 Testar Conexão

### Teste 1: Ping do Emulador
No emulador, abra o navegador e acesse:
```
http://192.168.10.107:8081/api/mobile/dashboard/stats
```

Se retornar JSON ou erro 404, a conexão está OK.

### Teste 2: Telnet (Windows)
No Prompt de Comando:
```cmd
telnet 192.168.10.107 8081
```

Se conectar, o servidor está acessível.

### Teste 3: Curl
```cmd
curl http://192.168.10.107:8081/api/mobile/dashboard/stats
```

## 📝 Configuração Atual do App

### IP Padrão (server_config.xml)
```xml
<string name="default_server_ip">192.168.10.107</string>
<integer name="default_server_port">8081</integer>
```

### IPs Sugeridos
```xml
<string-array name="suggested_server_ips">
    <item>10.14.250.228</item>
    <item>192.168.11.136</item>
    <item>10.14.250.238</item>
    <item>192.168.10.107</item>  <!-- ← IP padrão atual -->
    <item>192.168.1.100</item>
    <item>192.168.0.100</item>
    <item>10.0.0.100</item>
    <item>localhost</item>
</string-array>
```

## 🔧 Debug: Ver IP Configurado no App

Adicione logs no `ServerConfigManager`:

```kotlin
fun getBaseUrl(): String {
    val serverUrl = preferencesManager.getServerUrl()
    Log.d("ServerConfig", "URL configurada: $serverUrl")
    
    return if (serverUrl.isNullOrBlank()) {
        val fallback = "http://$FALLBACK_IP:$DEFAULT_PORT"
        Log.d("ServerConfig", "Usando fallback: $fallback")
        fallback
    } else {
        serverUrl
    }
}
```

Depois verifique os logs no Logcat:
```
adb logcat | findstr "ServerConfig"
```

## ⚠️ Problemas Comuns

### 1. Firewall Bloqueando
O Windows Firewall pode estar bloqueando conexões na porta 8081.

**Solução**: Adicionar regra no firewall:
```cmd
netsh advfirewall firewall add rule name="Inventario Mobile API" dir=in action=allow protocol=TCP localport=8081
```

### 2. Servidor Não Está Rodando
Verifique se o servidor está realmente rodando:
```cmd
netstat -ano | findstr "8081"
```

Deve mostrar algo como:
```
TCP    0.0.0.0:8081    0.0.0.0:0    LISTENING    12600
```

### 3. IP Mudou (DHCP)
Se o IP da máquina muda frequentemente (DHCP), considere:
- Configurar IP estático no roteador
- Usar hostname em vez de IP (se suportado)

## 🎯 Checklist de Resolução

- [ ] Descobrir IP real da máquina (`ipconfig`)
- [ ] Verificar se servidor está rodando (`netstat -ano | findstr 8081`)
- [ ] Configurar IP correto no app (Settings ou `server_config.xml`)
- [ ] Limpar dados do app se necessário
- [ ] Recompilar e reinstalar app
- [ ] Testar conexão no navegador do emulador
- [ ] Verificar firewall do Windows
- [ ] Verificar logs do app (`adb logcat`)

## 📱 Tela de Configuração no App

O app tem uma tela de configuração onde o usuário pode:
1. Ver IP atual configurado
2. Testar conexão com o servidor
3. Alterar IP manualmente
4. Escolher de uma lista de IPs sugeridos

**Acesso**: Menu → Configurações → Servidor

---

**Data**: 09/11/2025  
**Erro**: SocketTimeoutException ao conectar em 10.0.2.2:8081  
**Causa**: IP incorreto configurado no app  
**Solução**: Configurar IP real da máquina (192.168.x.x)
