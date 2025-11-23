# 🔍 Análise - Gerenciamento de Dispositivos Conectados

## 📊 Situação Atual

### ✅ O Que Já Existe

#### 1. Backend - Rastreamento em Tempo Real
- ✅ **ConnectedDevicesManager** - Gerencia dispositivos conectados em memória
  - Registra dispositivos automaticamente
  - Rastreia atividade (heartbeat)
  - Remove dispositivos inativos (timeout 5 min)
  - Armazena: deviceId, username, IP, modelo, versões, etc.

- ✅ **DeviceTrackingInterceptor** - Intercepta requisições mobile
  - Extrai headers (X-Device-ID, X-Username, etc.)
  - Registra/atualiza dispositivos automaticamente
  - Funciona em todas as requisições `/api/mobile/**`

- ✅ **MobileConnectionController** - API REST para consultar dispositivos
  - `GET /api/mobile/v1/connection/active` - Lista dispositivos conectados
  - `GET /api/mobile/v1/connection/stats` - Estatísticas
  - `GET /api/mobile/v1/connection/{deviceId}` - Info de dispositivo
  - `DELETE /api/mobile/v1/connection/{deviceId}` - Desconectar
  - `POST /api/mobile/v1/connection/cleanup` - Limpar inativos

#### 2. Backend - Registro Permanente
- ✅ **DispositivoMobileService** - Gerencia dispositivos no banco
  - Tabela: `dispositivo_mobile`
  - Campos: id, device_id, usuario, modelo, status, etc.
  - Funcionalidades: aprovar, bloquear, rejeitar

- ✅ **DispositivoMobileDAO** - Acesso ao banco
  - CRUD completo
  - Filtros por status, usuário

#### 3. Frontend Desktop
- ✅ **MobileMonitorFrame** - Tela de monitoramento
  - Interface Swing completa
  - Tabela com dispositivos
  - Botões: Atualizar, Auto Refresh, Detalhes, Bloquear
  - Estatísticas: Total, Ativos, Usuários

---

## ❌ O Problema

O **MobileMonitorFrame** está usando o **DispositivoMobileService** (banco de dados) ao invés do **MobileConnectionController** (dispositivos conectados em tempo real).

### Diferença Entre os Dois Sistemas

| Aspecto | DispositivoMobileService | ConnectedDevicesManager |
|---------|-------------------------|------------------------|
| **Armazenamento** | Banco de dados PostgreSQL | Memória (ConcurrentHashMap) |
| **Propósito** | Registro permanente | Conexões ativas em tempo real |
| **Dados** | Histórico completo | Apenas dispositivos online |
| **Atualização** | Manual (no login) | Automática (a cada requisição) |
| **Timeout** | Não tem | 5 minutos sem atividade |
| **Status** | PENDENTE/APROVADO/BLOQUEADO | Ativo/Inativo |

### O Que o Usuário Espera Ver

Quando abre "Gerenciamento de Dispositivos", o usuário quer ver:
- ✅ Quais smartphones estão **conectados AGORA**
- ✅ Última atividade de cada dispositivo
- ✅ Tempo de conexão
- ✅ Número de requisições
- ✅ IP e informações de rede

**Atualmente mostra:** Todos os dispositivos já registrados no banco (histórico)
**Deveria mostrar:** Apenas dispositivos conectados no momento

---

## 🔧 O Que Falta Implementar

### 1. Criar Cliente HTTP no MobileMonitorFrame

O `MobileMonitorFrame` precisa fazer requisições HTTP para o `MobileConnectionController` ao invés de usar o DAO.

**Arquivo:** `src/main/java/com/inventario/view/MobileMonitorFrame.java`

**Mudanças necessárias:**

```java
// ❌ ATUAL (usa banco de dados)
private final DispositivoMobileService dispositivoService;

public MobileMonitorFrame() {
    this.dispositivoService = new DispositivoMobileService();
}

private void atualizarDados() {
    dispositivos = dispositivoService.listarTodos(); // Busca do banco
}

// ✅ NOVO (usa API REST)
private final String API_BASE_URL = "http://localhost:8080"; // ou 8081
private final ObjectMapper objectMapper = new ObjectMapper();

private void atualizarDados() {
    // Fazer requisição HTTP GET para /api/mobile/v1/connection/active
    String url = API_BASE_URL + "/api/mobile/v1/connection/active";
    
    // Parsear JSON response
    // Atualizar tabela com dispositivos conectados
}
```

### 2. Adaptar Modelo de Dados

O `ConnectedDevice` retorna dados diferentes do `DispositivoMobile`.

**Response do endpoint `/active`:**
```json
{
  "success": true,
  "connections": [
    {
      "deviceId": "abc123",
      "username": "joao.silva",
      "deviceInfo": "Samsung Galaxy S21",
      "androidVersion": "13",
      "appVersion": "2.0.0",
      "ipAddress": "192.168.1.100",
      "hostname": "android-abc123",
      "connectedAt": "2025-11-23T10:30:00",
      "lastHeartbeat": "2025-11-23T10:35:00",
      "connectionDuration": "5 minutos",
      "requestCount": 15,
      "isActive": true
    }
  ],
  "stats": {
    "totalConnections": 1,
    "uniqueUsers": 1
  }
}
```

**Criar DTO para mapear:**
```java
public class ConnectedDeviceDTO {
    private String deviceId;
    private String username;
    private String deviceInfo;
    private String androidVersion;
    private String appVersion;
    private String ipAddress;
    private String hostname;
    private String connectedAt;
    private String lastHeartbeat;
    private String connectionDuration;
    private int requestCount;
    private boolean isActive;
    
    // Getters e Setters
}
```

### 3. Implementar Cliente HTTP

Usar `HttpURLConnection` ou biblioteca HTTP (Apache HttpClient, OkHttp).

**Opção 1: HttpURLConnection (nativo Java)**
```java
private List<ConnectedDeviceDTO> buscarDispositivosConectados() throws Exception {
    URL url = new URL(API_BASE_URL + "/api/mobile/v1/connection/active");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
    
    if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Erro HTTP: " + conn.getResponseCode());
    }
    
    BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream())
    );
    
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
        response.append(line);
    }
    
    conn.disconnect();
    
    // Parsear JSON
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(response.toString());
    JsonNode connections = root.get("connections");
    
    List<ConnectedDeviceDTO> devices = new ArrayList<>();
    for (JsonNode node : connections) {
        ConnectedDeviceDTO device = mapper.treeToValue(node, ConnectedDeviceDTO.class);
        devices.add(device);
    }
    
    return devices;
}
```

**Opção 2: Apache HttpClient (mais robusto)**
```java
// Adicionar dependência no pom.xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>

private List<ConnectedDeviceDTO> buscarDispositivosConectados() throws Exception {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(API_BASE_URL + "/api/mobile/v1/connection/active");
    
    CloseableHttpResponse response = httpClient.execute(request);
    String jsonResponse = EntityUtils.toString(response.getEntity());
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(jsonResponse);
    // ... parsear JSON
    
    httpClient.close();
    return devices;
}
```

### 4. Atualizar Tabela do MobileMonitorFrame

Adaptar colunas para mostrar dados de conexão em tempo real:

**Colunas atuais:**
- ID, Usuário, Modelo, Fabricante, Android, App, IP, Registro, Última Conexão, Status, Ativo

**Colunas sugeridas para dispositivos conectados:**
- Device ID, Usuário, Modelo, Android, App, IP, Hostname, Conectado Em, Última Atividade, Duração, Requisições, Status

```java
String[] colunas = {
    "Device ID", "Usuário", "Modelo", "Android", "App", 
    "IP", "Hostname", "Conectado Em", "Última Atividade", 
    "Duração", "Requisições", "Status"
};
```

### 5. Configurar URL do Servidor

Permitir que o usuário configure a URL do servidor mobile.

**Opções:**
1. Ler de `application.properties`
2. Ler de `configuracao_banco.json`
3. Criar campo de configuração na tela

```java
// Ler de properties
private String getServerUrl() {
    try {
        Properties props = new Properties();
        props.load(new FileInputStream("application.properties"));
        return props.getProperty("mobile.server.url", "http://localhost:8080");
    } catch (Exception e) {
        return "http://localhost:8080"; // fallback
    }
}
```

### 6. Tratamento de Erros

Adicionar tratamento robusto de erros:

```java
private void atualizarDados() {
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        private List<ConnectedDeviceDTO> dispositivos;
        private Exception erro;
        
        @Override
        protected Void doInBackground() throws Exception {
            try {
                dispositivos = buscarDispositivosConectados();
            } catch (java.net.ConnectException e) {
                erro = new Exception("Servidor mobile não está rodando");
            } catch (java.net.SocketTimeoutException e) {
                erro = new Exception("Timeout ao conectar ao servidor");
            } catch (Exception e) {
                erro = e;
            }
            return null;
        }
        
        @Override
        protected void done() {
            if (erro != null) {
                mostrarErro("Erro ao buscar dispositivos: " + erro.getMessage());
                // Sugerir verificar se servidor está rodando
                return;
            }
            
            if (dispositivos != null) {
                atualizarTabela(dispositivos);
                atualizarEstatisticas(dispositivos);
            }
        }
    };
    
    worker.execute();
}
```

---

## 📋 Checklist de Implementação

### Fase 1: Cliente HTTP Básico
- [ ] Adicionar dependência Jackson (JSON) no pom.xml
- [ ] Criar classe `ConnectedDeviceDTO`
- [ ] Criar método `buscarDispositivosConectados()` com HttpURLConnection
- [ ] Testar requisição HTTP manualmente

### Fase 2: Integração com MobileMonitorFrame
- [ ] Substituir `DispositivoMobileService` por cliente HTTP
- [ ] Adaptar método `atualizarDados()`
- [ ] Adaptar método `atualizarTabela()`
- [ ] Atualizar colunas da tabela

### Fase 3: Configuração
- [ ] Adicionar campo de configuração de URL do servidor
- [ ] Ler URL de arquivo de configuração
- [ ] Validar conexão ao iniciar

### Fase 4: Funcionalidades Extras
- [ ] Implementar botão "Desconectar Dispositivo" (DELETE endpoint)
- [ ] Implementar botão "Limpar Inativos" (POST /cleanup)
- [ ] Adicionar indicador visual de status (online/offline)
- [ ] Adicionar tooltip com detalhes do dispositivo

### Fase 5: Testes
- [ ] Testar com servidor rodando
- [ ] Testar com servidor parado (erro gracioso)
- [ ] Testar auto-refresh
- [ ] Testar com múltiplos dispositivos conectados

---

## 🎯 Resultado Esperado

Após implementação, o **MobileMonitorFrame** deverá:

1. ✅ Mostrar apenas dispositivos **conectados no momento**
2. ✅ Atualizar automaticamente a cada 10 segundos (auto-refresh)
3. ✅ Mostrar tempo de conexão em tempo real
4. ✅ Mostrar última atividade (heartbeat)
5. ✅ Permitir desconectar dispositivos remotamente
6. ✅ Mostrar estatísticas precisas (total conectados, usuários únicos)
7. ✅ Funcionar mesmo se servidor mobile estiver em outra máquina

---

## 🔗 Arquivos Envolvidos

### Backend (já implementado)
- ✅ `src/main/java/com/inventario/service/ConnectedDevicesManager.java`
- ✅ `src/main/java/com/inventario/mobile/server/controller/MobileConnectionController.java`
- ✅ `src/main/java/com/inventario/mobile/server/config/DeviceTrackingInterceptor.java`

### Frontend (precisa modificar)
- ⚠️ `src/main/java/com/inventario/view/MobileMonitorFrame.java` - **MODIFICAR**

### Novos arquivos (criar)
- 🆕 `src/main/java/com/inventario/dto/ConnectedDeviceDTO.java` - **CRIAR**
- 🆕 `src/main/java/com/inventario/util/MobileApiClient.java` - **CRIAR** (opcional)

---

## 💡 Alternativa: Unificar os Dois Sistemas

Outra abordagem seria fazer o `ConnectedDevicesManager` salvar no banco também:

```java
// No ConnectedDevicesManager
private static void saveDeviceConnection(ConnectedDevice device) {
    // Já existe, mas poderia ser melhorado
    // Salvar em mobile_device_connection
}

// No MobileMonitorFrame
// Buscar de mobile_device_connection ao invés de dispositivo_mobile
```

Mas isso ainda não resolveria o problema de mostrar apenas dispositivos **conectados agora**.

---

**Conclusão:** A melhor solução é fazer o **MobileMonitorFrame** consumir a API REST do **MobileConnectionController** para mostrar dispositivos conectados em tempo real.
