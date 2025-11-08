# 📱 Sistema de Rastreamento de Dispositivos Móveis

## 🎯 Problema Resolvido

O `MobileMonitorFrame` não conseguia listar os smartphones conectados porque:
1. ❌ O endpoint REST `/api/mobile/v1/connection/active` não existia
2. ❌ Não havia sistema de rastreamento de dispositivos
3. ❌ Não havia tabela no banco para armazenar conexões

## ✅ Solução Implementada

### 1. **ConnectedDevicesManager** (Novo)
Gerenciador em memória de dispositivos conectados

**Funcionalidades**:
- ✅ Registra dispositivos quando conectam
- ✅ Atualiza atividade a cada requisição
- ✅ Remove dispositivos inativos (timeout 5 minutos)
- ✅ Armazena informações: modelo, Android, versão app, IP
- ✅ Persiste no banco de dados
- ✅ Calcula duração de conexão
- ✅ Conta requisições por dispositivo

**Métodos Principais**:
```java
- registerDevice(deviceId, username, ipAddress)
- updateDeviceInfo(deviceId, model, androidVersion, appVersion)
- registerActivity(deviceId)
- removeDevice(deviceId)
- getConnectedDevices(): List<ConnectedDevice>
- getConnectedDevicesCount(): int
```

### 2. **MobileConnectionController** (Novo)
Controller REST para o MobileMonitorFrame

**Endpoints**:

#### GET `/api/mobile/v1/connection/active`
Retorna todas as conexões ativas
```json
{
  "success": true,
  "connections": [
    {
      "deviceId": "abc-123",
      "username": "admin",
      "deviceInfo": "Samsung Galaxy S21",
      "androidVersion": "13",
      "appVersion": "1.2.0",
      "ipAddress": "192.168.1.100",
      "hostname": "DESKTOP-ABC",
      "connectedAt": "2024-11-06T10:30:00",
      "lastHeartbeat": "2024-11-06T10:35:00",
      "connectionDuration": "5 minutos",
      "requestCount": 25,
      "isActive": true
    }
  ],
  "stats": {
    "totalConnections": 1,
    "uniqueUsers": 1
  }
}
```

#### GET `/api/mobile/v1/connection/stats`
Retorna estatísticas de conexões

#### GET `/api/mobile/v1/connection/{deviceId}`
Retorna informações de um dispositivo específico

#### DELETE `/api/mobile/v1/connection/{deviceId}`
Desconecta um dispositivo (força logout)

#### POST `/api/mobile/v1/connection/cleanup`
Limpa conexões inativas

### 3. **DeviceTrackingInterceptor** (Novo)
Interceptor que rastreia automaticamente dispositivos

**Como Funciona**:
1. Intercepta todas as requisições para `/api/mobile/**`
2. Extrai informações dos headers HTTP:
   - `X-Device-ID`: ID único do dispositivo
   - `X-Username`: Usuário logado
   - `X-Device-Model`: Modelo do dispositivo
   - `X-Android-Version`: Versão do Android
   - `X-App-Version`: Versão do app
3. Registra/atualiza dispositivo automaticamente
4. Registra atividade a cada requisição

### 4. **Tabela no Banco de Dados** (Novo)
`mobile_device_connection`

**Campos**:
- `device_id` (VARCHAR, UNIQUE): ID único do dispositivo
- `username` (VARCHAR): Usuário logado
- `device_model` (VARCHAR): Modelo do dispositivo
- `android_version` (VARCHAR): Versão do Android
- `app_version` (VARCHAR): Versão do app
- `ip_address` (VARCHAR): IP do dispositivo
- `connected_at` (TIMESTAMP): Data/hora de conexão
- `last_activity` (TIMESTAMP): Última atividade
- `disconnected_at` (TIMESTAMP): Data/hora de desconexão
- `request_count` (INTEGER): Número de requisições

**View**:
- `mobile_active_connections`: Mostra apenas conexões ativas

**Procedure**:
- `cleanup_old_mobile_connections()`: Remove conexões antigas

---

## 🔧 Como Usar

### No Aplicativo Desktop

1. **Abrir Monitor de Dispositivos**:
```java
MobileMonitorFrame monitor = new MobileMonitorFrame();
monitor.setVisible(true);
```

2. **Atualizar Manualmente**:
   - Clicar no botão "🔄 Atualizar"

3. **Auto-Refresh**:
   - Clicar em "▶️ Auto Refresh (OFF)"
   - Atualiza automaticamente a cada 10 segundos

### No Aplicativo Mobile (Android)

O app mobile precisa enviar headers em todas as requisições:

```kotlin
// Adicionar interceptor no OkHttp
class DeviceInfoInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val deviceId = getDeviceId(context) // UUID único
        val username = getUsername() // Do SharedPreferences
        
        val request = chain.request().newBuilder()
            .addHeader("X-Device-ID", deviceId)
            .addHeader("X-Username", username)
            .addHeader("X-Device-Model", Build.MODEL)
            .addHeader("X-Android-Version", Build.VERSION.RELEASE)
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .build()
        
        return chain.proceed(request)
    }
}

// Adicionar ao OkHttpClient
val client = OkHttpClient.Builder()
    .addInterceptor(DeviceInfoInterceptor(context))
    .build()
```

---

## 📊 Fluxo de Funcionamento

```
1. App Mobile faz requisição
   ↓
2. DeviceTrackingInterceptor intercepta
   ↓
3. Extrai headers (Device-ID, Username, etc)
   ↓
4. ConnectedDevicesManager registra/atualiza
   ↓
5. Salva no banco de dados
   ↓
6. MobileMonitorFrame busca via API
   ↓
7. Exibe na tabela
```

---

## 🗄️ Instalação do Banco de Dados

```bash
# Executar script SQL
psql -U postgres -d sispatrimonio -f sql/criar_tabela_mobile_device_connection.sql
```

Ou via aplicativo desktop:
1. Abrir ferramenta de SQL
2. Executar o script `criar_tabela_mobile_device_connection.sql`

---

## 🧪 Testando

### 1. Verificar se Tabela Foi Criada
```sql
SELECT * FROM mobile_device_connection;
SELECT * FROM mobile_active_connections;
```

### 2. Testar Endpoint Manualmente
```bash
curl http://localhost:8080/api/mobile/v1/connection/active
```

### 3. Simular Conexão
```sql
INSERT INTO mobile_device_connection 
(device_id, username, device_model, android_version, app_version, ip_address)
VALUES 
('test-device-001', 'admin', 'Samsung Galaxy S21', '13', '1.2.0', '192.168.1.100');
```

### 4. Abrir MobileMonitorFrame
```java
// No menu do sistema desktop
// Ou via código:
MobileMonitorFrame monitor = new MobileMonitorFrame();
monitor.setVisible(true);
```

---

## 🔍 Troubleshooting

### Problema: MobileMonitorFrame mostra erro "Erro ao buscar conexões"

**Causas Possíveis**:
1. Servidor mobile não está rodando na porta 8080
2. Endpoint não está registrado
3. Tabela não foi criada no banco

**Solução**:
```bash
# 1. Verificar se servidor está rodando
netstat -ano | findstr :8080

# 2. Testar endpoint
curl http://localhost:8080/api/mobile/v1/connection/active

# 3. Verificar tabela
psql -U postgres -d sispatrimonio -c "SELECT * FROM mobile_device_connection"
```

### Problema: Dispositivos não aparecem na lista

**Causas Possíveis**:
1. App mobile não está enviando headers
2. Interceptor não está registrado
3. Timeout de 5 minutos expirou

**Solução**:
1. Verificar logs do servidor
2. Adicionar interceptor no app mobile
3. Fazer requisição recente do app

### Problema: Tabela não existe

**Solução**:
```bash
# Executar script SQL
psql -U postgres -d sispatrimonio -f sql/criar_tabela_mobile_device_connection.sql
```

---

## 📈 Melhorias Futuras

1. **Dashboard Web**: Interface web para monitoramento
2. **Notificações**: Alertar quando dispositivo conecta/desconecta
3. **Geolocalização**: Mostrar localização dos dispositivos
4. **Histórico**: Gráficos de conexões ao longo do tempo
5. **Bloqueio**: Bloquear dispositivos específicos
6. **Sessões**: Gerenciar múltiplas sessões por usuário

---

## ✅ Checklist de Implementação

- [x] ConnectedDevicesManager criado
- [x] MobileConnectionController criado
- [x] DeviceTrackingInterceptor criado
- [x] WebMvcConfig criado
- [x] Script SQL criado
- [x] Documentação criada
- [ ] Executar script SQL no banco
- [ ] Adicionar interceptor no app mobile
- [ ] Testar MobileMonitorFrame
- [ ] Verificar logs do servidor

---

## 📝 Arquivos Criados/Modificados

### Novos Arquivos
```
✅ src/main/java/com/inventario/service/ConnectedDevicesManager.java
✅ src/main/java/com/inventario/mobile/server/controller/MobileConnectionController.java
✅ src/main/java/com/inventario/mobile/server/config/DeviceTrackingInterceptor.java
✅ src/main/java/com/inventario/mobile/server/config/WebMvcConfig.java
✅ sql/criar_tabela_mobile_device_connection.sql
✅ MOBILE_DEVICE_TRACKING.md (este arquivo)
```

### Arquivos Existentes (Não Modificados)
```
✓ src/main/java/com/inventario/view/MobileMonitorFrame.java (já estava correto)
✓ src/main/java/com/inventario/view/MobileServerPanel.java (pode ser melhorado depois)
```

---

**Status**: 🟢 IMPLEMENTADO E PRONTO PARA TESTAR

**Próximo Passo**: Executar o script SQL e testar o MobileMonitorFrame!
