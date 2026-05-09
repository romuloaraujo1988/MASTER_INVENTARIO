# Gerenciamento de Dispositivos Móveis

## 📱 Visão Geral

Sistema de controle e autorização de dispositivos móveis que acessam o sistema de inventário. Permite que administradores aprovem, bloqueiem ou rejeitem dispositivos antes que possam coletar dados.

---

## 🎯 Funcionalidades

### 1. Registro Automático
- Dispositivo é registrado automaticamente no primeiro login
- Captura informações do dispositivo (modelo, fabricante, Android, IP, MAC)
- Status inicial: **PENDENTE**

### 2. Aprovação de Dispositivos
- Administrador visualiza dispositivos pendentes
- Pode aprovar, bloquear ou rejeitar
- Dispositivos aprovados podem coletar dados

### 3. Controle de Acesso
- Apenas dispositivos aprovados podem sincronizar
- Dispositivos bloqueados são impedidos de acessar
- Histórico de conexões e sincronizações

---

## 🔄 Fluxo de Registro

```
┌─────────────────┐
│  Login Mobile   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Registra Device │ ← Automático
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Status: APROVADO│ ← Auto-aprovado
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Pode Coletar    │ ✅
└─────────────────┘

Admin pode:
- Visualizar dispositivos
- Bloquear se necessário
- Ver histórico de conexões
```

---

## 📊 Status de Dispositivos

| Status | Descrição | Pode Coletar? |
|--------|-----------|---------------|
| **APROVADO** | Autorizado a coletar dados (padrão) | ✅ Sim |
| **BLOQUEADO** | Bloqueado pelo administrador | ❌ Não |
| **PENDENTE** | (Não usado - auto-aprovação ativa) | - |
| **REJEITADO** | (Não usado - auto-aprovação ativa) | - |

---

## 🗄️ Estrutura do Banco de Dados

### Tabela: `dispositivo_mobile`

```sql
CREATE TABLE dispositivo_mobile (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    id_usuario INTEGER NOT NULL,
    modelo VARCHAR(100),
    fabricante VARCHAR(100),
    versao_android VARCHAR(20),
    versao_app VARCHAR(20),
    endereco_ip VARCHAR(45),
    endereco_mac VARCHAR(17),
    status status_dispositivo NOT NULL DEFAULT 'PENDENTE',
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_conexao TIMESTAMP,
    data_ultima_sincronizacao TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    token_atual TEXT,
    data_expiracao_token TIMESTAMP,
    observacoes TEXT,
    
    CONSTRAINT fk_dispositivo_usuario 
        FOREIGN KEY (id_usuario) 
        REFERENCES tabela_usuario(id) 
        ON DELETE CASCADE
);
```

### ENUM: `status_dispositivo`

```sql
CREATE TYPE status_dispositivo AS ENUM (
    'PENDENTE', 
    'APROVADO', 
    'BLOQUEADO', 
    'REJEITADO'
);
```

---

## 🔌 API Endpoints

### Backend (Java/Spring Boot)

#### 1. Registrar Dispositivo
```http
POST /api/mobile/dispositivos/registrar
Content-Type: application/json

{
  "deviceId": "abc123def456",
  "idUsuario": 1,
  "modelo": "Galaxy S21",
  "fabricante": "Samsung",
  "versaoAndroid": "13",
  "versaoApp": "1.2.0",
  "enderecoIp": "192.168.1.100",
  "enderecoMac": "AA:BB:CC:DD:EE:FF"
}
```

**Resposta:**
```json
{
  "success": true,
  "message": "Dispositivo registrado com sucesso",
  "data": {
    "id": 1,
    "deviceId": "abc123def456",
    "status": "PENDENTE",
    "modelo": "Galaxy S21",
    ...
  }
}
```

#### 2. Verificar Status
```http
GET /api/mobile/dispositivos/status/{deviceId}
```

#### 3. Verificar Autorização
```http
GET /api/mobile/dispositivos/autorizado/{deviceId}
```

**Resposta:**
```json
{
  "success": true,
  "message": "Dispositivo autorizado",
  "data": true
}
```

#### 4. Listar Pendentes (Admin)
```http
GET /api/mobile/dispositivos/pendentes
Authorization: Bearer {token}
```

#### 5. Aprovar Dispositivo (Admin)
```http
PUT /api/mobile/dispositivos/{id}/aprovar
Authorization: Bearer {token}
```

#### 6. Bloquear Dispositivo (Admin)
```http
PUT /api/mobile/dispositivos/{id}/bloquear
Authorization: Bearer {token}
```

#### 7. Desbloquear Dispositivo (Admin)
```http
PUT /api/mobile/dispositivos/{id}/desbloquear
Authorization: Bearer {token}
```

#### 8. Remover Dispositivo (Admin)
```http
DELETE /api/mobile/dispositivos/{id}
Authorization: Bearer {token}
```

---

## 📱 Integração Android

### 1. Registro Automático no Login

```kotlin
// LoginViewModel.kt
private fun registrarDispositivoAutomaticamente(idUsuario: Int) {
    viewModelScope.launch {
        try {
            val dispositivoApi = ApiClient.getApiService(context)
                .create(DispositivoApi::class.java)
            
            val dispositivoRepository = DispositivoRepository(
                dispositivoApi,
                context
            )
            
            val result = dispositivoRepository.registrarDispositivoAtual(idUsuario)
            
            if (result.isSuccess) {
                val dispositivo = result.getOrNull()
                Log.d("Login", "✓ Dispositivo registrado: ${dispositivo?.modelo}")
                
                // Salvar ID do dispositivo
                dispositivo?.let {
                    preferencesManager.saveDispositivoId(it.id)
                    preferencesManager.saveDispositivoStatus(it.status)
                }
            }
        } catch (e: Exception) {
            Log.e("Login", "Erro ao registrar dispositivo", e)
        }
    }
}
```

### 2. Verificar Autorização Antes de Sincronizar

```kotlin
// SyncRepository.kt
suspend fun sincronizarDados(): Result<Unit> {
    // Verificar se dispositivo está autorizado
    val autorizado = dispositivoRepository.verificarAutorizacao()
    
    if (!autorizado.getOrDefault(false)) {
        return Result.failure(
            Exception("Dispositivo não autorizado. Aguarde aprovação do administrador.")
        )
    }
    
    // Prosseguir com sincronização...
}
```

### 3. Capturar Informações do Dispositivo

```kotlin
// DeviceInfoHelper.kt
object DeviceInfoHelper {
    
    data class DeviceInfo(
        val deviceId: String,
        val model: String,
        val manufacturer: String,
        val androidVersion: String,
        val appVersion: String,
        val ipAddress: String,
        val macAddress: String?
    )
    
    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            deviceId = getDeviceId(context),
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            appVersion = getAppVersion(context),
            ipAddress = getIPAddress(),
            macAddress = getMacAddress()
        )
    }
}
```

---

## 🖥️ Interface Desktop (Admin)

### MobileMonitorFrame - Monitor de Dispositivos

**Localização**: `src/main/java/com/inventario/view/MobileMonitorFrame.java`

**Funcionalidades:**
- ✅ Listar todos os dispositivos registrados
- ✅ Auto-refresh (atualização automática a cada 10 segundos)
- ✅ Visualizar detalhes completos do dispositivo
- ✅ Bloquear dispositivos (se necessário)
- ✅ Desbloquear dispositivos bloqueados
- ✅ Ver histórico de conexões e sincronizações
- ✅ Estatísticas em tempo real:
  - Total de dispositivos
  - Dispositivos ativos
  - Usuários únicos
  - Última atualização

**Campos Exibidos:**
- ID
- Usuário
- Modelo
- Fabricante
- Versão Android
- Versão App
- Endereço IP
- Data de Registro
- Última Conexão
- Status (APROVADO/BLOQUEADO)
- Ativo (Sim/Não)

**Cores da Interface:**
- 🟢 Verde claro: Dispositivos aprovados e ativos
- 🔴 Vermelho claro: Dispositivos inativos ou bloqueados

---

## 🔒 Segurança

### 1. Validações
- Device ID único por dispositivo
- Apenas um dispositivo ativo por Device ID
- Validação de usuário existente

### 2. Controle de Acesso
- Apenas admins podem aprovar/bloquear
- Dispositivos não aprovados não podem sincronizar
- Token JWT vinculado ao dispositivo

### 3. Auditoria
- Registro de todas as conexões
- Histórico de sincronizações
- Log de mudanças de status

---

## 📝 Casos de Uso

### Caso 1: Novo Coletor
1. Coletor instala o app
2. Faz login com suas credenciais
3. Dispositivo é registrado automaticamente (APROVADO)
4. Coletor pode começar a trabalhar imediatamente
5. Admin pode visualizar o dispositivo no sistema

### Caso 2: Dispositivo Perdido
1. Usuário reporta perda do dispositivo
2. Admin bloqueia o dispositivo
3. Dispositivo não pode mais sincronizar
4. Usuário recebe novo dispositivo
5. Novo dispositivo é registrado e aprovado

### Caso 3: Troca de Dispositivo
1. Usuário recebe novo celular
2. Instala o app e faz login
3. Novo dispositivo é registrado (APROVADO)
4. Usuário pode usar imediatamente
5. Admin pode bloquear dispositivo antigo se necessário

---

## 🚀 Instalação

### 1. Criar Tabela no Banco

```bash
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabela_dispositivo_mobile.sql
```

### 2. Compilar Backend

```bash
mvn clean compile
```

### 3. Compilar Android

```bash
cd InventarioMobile
./gradlew assembleDebug
```

---

## 🧪 Testes

### Testar Registro de Dispositivo

```bash
curl -X POST http://localhost:8080/api/mobile/dispositivos/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test123",
    "idUsuario": 1,
    "modelo": "Test Device",
    "fabricante": "Test",
    "versaoAndroid": "13",
    "versaoApp": "1.0.0",
    "enderecoIp": "127.0.0.1"
  }'
```

### Testar Verificação de Status

```bash
curl http://localhost:8080/api/mobile/dispositivos/status/test123
```

### Testar Aprovação (Admin)

```bash
curl -X PUT http://localhost:8080/api/mobile/dispositivos/1/aprovar \
  -H "Authorization: Bearer {admin_token}"
```

---

## 📊 Monitoramento

### Queries Úteis

```sql
-- Contar dispositivos por status
SELECT status, COUNT(*) 
FROM dispositivo_mobile 
GROUP BY status;

-- Dispositivos pendentes
SELECT d.*, u.nome_completo
FROM dispositivo_mobile d
JOIN tabela_usuario u ON d.id_usuario = u.id
WHERE d.status = 'PENDENTE'
ORDER BY d.data_registro DESC;

-- Dispositivos inativos há mais de 30 dias
SELECT *
FROM dispositivo_mobile
WHERE data_ultima_conexao < NOW() - INTERVAL '30 days'
  AND ativo = TRUE;

-- Histórico de conexões por usuário
SELECT u.nome_completo, 
       COUNT(d.id) as total_dispositivos,
       MAX(d.data_ultima_conexao) as ultima_conexao
FROM dispositivo_mobile d
JOIN tabela_usuario u ON d.id_usuario = u.id
GROUP BY u.nome_completo
ORDER BY ultima_conexao DESC;
```

---

## 🔧 Manutenção

### Limpar Dispositivos Antigos

```sql
-- Desativar dispositivos inativos há mais de 90 dias
UPDATE dispositivo_mobile
SET ativo = FALSE,
    status = 'BLOQUEADO'::status_dispositivo
WHERE data_ultima_conexao < NOW() - INTERVAL '90 days'
  AND ativo = TRUE;
```

### Remover Dispositivos Rejeitados

```sql
-- Remover dispositivos rejeitados há mais de 1 ano
DELETE FROM dispositivo_mobile
WHERE status = 'REJEITADO'
  AND data_registro < NOW() - INTERVAL '1 year';
```

---

## 📚 Referências

### Backend (Java)
- **Model**: `src/main/java/com/inventario/model/DispositivoMobile.java`
- **Enum**: `src/main/java/com/inventario/model/StatusDispositivo.java`
- **DAO**: `src/main/java/com/inventario/dao/DispositivoMobileDAO.java`
- **Service**: `src/main/java/com/inventario/service/DispositivoMobileService.java`
- **Controller**: `src/main/java/com/inventario/mobile/server/controller/MobileDispositivoController.java`
- **DTO**: `src/main/java/com/inventario/mobile/server/dto/DispositivoRegistroDTO.java`
- **View**: `src/main/java/com/inventario/view/MobileMonitorFrame.java`
- **SQL**: `sql/criar_tabela_dispositivo_mobile.sql`

### Android (Kotlin)
- **Model**: `InventarioMobile/app/src/main/java/com/inventario/mobile/model/DispositivoMobile.kt`
- **API**: `InventarioMobile/app/src/main/java/com/inventario/mobile/api/DispositivoApi.kt`
- **Repository**: `InventarioMobile/app/src/main/java/com/inventario/mobile/repository/DispositivoRepository.kt`
- **Helper**: `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/DeviceInfoHelper.kt`
- **Integration**: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/login/LoginViewModel.kt`

---

**Versão**: 1.0.0  
**Data**: 09/11/2025  
**Autor**: Sistema de Inventário
