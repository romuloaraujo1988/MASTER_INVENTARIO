# Mapa Completo de Endpoints - Servidor Mobile

## 🌐 URL Base
```
http://10.0.2.2:8081/inventario/api/mobile
```

## 📋 Endpoints Disponíveis

### 🔐 Autenticação (`MobileAuthController`)
**Base:** `/api/mobile/auth`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/login` | Login do usuário | ❌ Não |
| POST | `/refresh` | Renovar access token | ❌ Não |

**Exemplos:**
```
POST /inventario/api/mobile/auth/login
POST /inventario/api/mobile/auth/refresh
```

---

### 📦 Patrimônios (`MobilePatrimonioController`)
**Base:** `/api/mobile/patrimonio`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/` | Listar todos (paginado) | ✅ Sim |
| GET | `/{id}` | Buscar por ID | ✅ Sim |
| GET | `/qr/{qrCode}` | Buscar por QR Code | ✅ Sim |
| GET | `/numero/{numero}` | Buscar por número | ✅ Sim |
| GET | `/sala/{salaId}` | Buscar por sala | ✅ Sim |
| GET | `/setor/{setorId}` | Buscar por setor | ✅ Sim |
| GET | `/responsavel/{id}` | Buscar por responsável | ✅ Sim |
| GET | `/responsavel/{id}/count` | Contar por responsável | ✅ Sim |
| GET | `/numero/{numero}/coletado` | Verificar se foi coletado | ✅ Sim |
| GET | `/numero/{numero}/validar` | Validar patrimônio | ✅ Sim |

**Exemplos:**
```
GET /inventario/api/mobile/patrimonio?page=0&size=50
GET /inventario/api/mobile/patrimonio/123
GET /inventario/api/mobile/patrimonio/numero/12345
```

---

### 🏢 Salas (`MobileSalaController`)
**Base:** `/api/mobile/salas`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/` | Listar todas (paginado) | ✅ Sim |
| GET | `/{id}` | Buscar por ID | ✅ Sim |

**Exemplos:**
```
GET /inventario/api/mobile/salas?page=0&size=10
GET /inventario/api/mobile/salas/54
```

---

### 👤 Responsáveis (`MobileResponsavelController`)
**Base:** `/api/mobile/responsaveis`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/` | Listar todos | ✅ Sim |
| GET | `/{id}` | Buscar por ID | ✅ Sim |

---

### 📊 Coletas (`MobileColetaController`)
**Base:** `/api/mobile/coletas`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/` | Registrar coleta | ✅ Sim |
| POST | `/lote` | Registrar lote (batch) | ✅ Sim |
| POST | `/verificar-duplicata` | Verificar duplicata | ✅ Sim |
| GET | `/pendentes` | Listar pendentes | ✅ Sim |

---

### 📝 Descrições (`MobileDescricaoController`)
**Base:** `/api/mobile/descricoes`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/nao-coletadas` | Descrições não coletadas | ✅ Sim |

---

### 📈 Dashboard (`MobileDashboardController`)
**Base:** `/api/mobile/dashboard`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/stats` | Estatísticas gerais | ✅ Sim |

---

### 📋 Inventário (`MobileInventarioController`)
**Base:** `/api/mobile/inventario`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/ativo` | Buscar inventário ativo | ✅ Sim |
| GET | `/{id}` | Buscar por ID | ✅ Sim |
| GET | `/` | Listar todos | ✅ Sim |
| GET | `/{id}/estatisticas` | Estatísticas do inventário | ✅ Sim |

---

### 🔄 Sincronização (`MobileSyncController`)
**Base:** `/api/mobile/sync`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/` | Sincronizar dados | ✅ Sim |

---

### ❤️ Health Check (`MobileHealthController`)
**Base:** `/api/mobile/health`

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| GET | `/` | Status do servidor | ❌ Não |

---

## 🎯 Endpoints Usados pela Sincronização

### App Android Chama:
```
1. GET /inventario/api/mobile/patrimonio?page=0&size=50
   ↓
2. GET /inventario/api/mobile/salas?page=0&size=10
```

### Servidor Deve Retornar:
```json
{
  "success": true,
  "message": "X patrimônio(s) carregado(s)",
  "data": [
    {
      "id": 1,
      "codigo": "12345",
      "descricao": "Cadeira",
      "salaId": 10,
      "salaNome": "Sala 101",
      ...
    }
  ]
}
```

---

## 🧪 Teste Manual dos Endpoints

### 1. Testar Patrimônios (COM Token)
```powershell
# Pegar token do app
$token = "eyJhbGciOiJIUzUxMiJ9..." # Do SharedPreferences

# Testar endpoint
Invoke-WebRequest -Uri "http://localhost:8081/inventario/api/mobile/patrimonio" `
  -Headers @{"Authorization"="Bearer $token"} `
  -Method GET
```

### 2. Testar Salas (COM Token)
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/inventario/api/mobile/salas" `
  -Headers @{"Authorization"="Bearer $token"} `
  -Method GET
```

---

## 📊 Status dos Endpoints

| Endpoint | Existe no Backend | Usado pelo App | Status |
|----------|-------------------|----------------|--------|
| `/auth/login` | ✅ Sim | ✅ Sim | ✅ Funcionando |
| `/patrimonio` (GET) | ✅ Sim | ✅ Sim | ⏳ Testando |
| `/salas` (GET) | ✅ Sim | ✅ Sim | ⏳ Testando |

---

**Criado em:** 18/11/2025 01:20  
**Versão:** 1.0.0  
**Propósito:** Referência de endpoints para desenvolvimento
