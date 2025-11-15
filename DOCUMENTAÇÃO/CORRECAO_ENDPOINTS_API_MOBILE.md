# Correção de Endpoints da API Mobile

## 📋 Problema Identificado

Os endpoints da API mobile estavam **duplicando** o prefixo `/api/mobile/` nas URLs, causando erros 404.

### Exemplo do Problema

**Base URL configurada:**
```
http://10.0.2.2:8081/inventario/api/mobile/
```

**Endpoint no código:**
```kotlin
@POST("api/mobile/auth/login")  // ❌ ERRADO - duplica o prefixo
```

**URL final gerada:**
```
http://10.0.2.2:8081/inventario/api/mobile/api/mobile/auth/login  ❌ DUPLICADO!
```

**URL esperada:**
```
http://10.0.2.2:8081/inventario/api/mobile/auth/login  ✅ CORRETO
```

## ✅ Solução Implementada

### 1. Correção do ServerConfigManager

Atualizado o método `buildBaseUrl()` para incluir `/api/mobile/` na base URL:

```kotlin
// ANTES
private fun buildBaseUrl(protocol: String, ip: String, port: Int): String {
    return "$protocol://$ip:$port$DEFAULT_CONTEXT_PATH"  
    // Resultado: http://10.0.2.2:8081/inventario
}

// DEPOIS
private fun buildBaseUrl(protocol: String, ip: String, port: Int): String {
    return "$protocol://$ip:$port$DEFAULT_CONTEXT_PATH$DEFAULT_API_PATH"
    // Resultado: http://10.0.2.2:8081/inventario/api/mobile
}
```

### 2. Correção do ApiService

Removido o prefixo `api/mobile/` de **todos** os endpoints:

#### Autenticação
```kotlin
// ANTES
@POST("api/mobile/auth/login")

// DEPOIS
@POST("auth/login")
```

#### Patrimônios
```kotlin
// ANTES
@GET("api/mobile/patrimonio")
@GET("api/mobile/patrimonio/{id}")
@GET("api/mobile/patrimonio/numero/{numero}")

// DEPOIS
@GET("patrimonio")
@GET("patrimonio/{id}")
@GET("patrimonio/numero/{numero}")
```

#### Salas
```kotlin
// ANTES
@GET("api/mobile/salas")
@GET("api/mobile/salas/{id}")

// DEPOIS
@GET("salas")
@GET("salas/{id}")
```

#### Coletas
```kotlin
// ANTES
@POST("api/mobile/coletas")
@GET("api/mobile/coletas")

// DEPOIS
@POST("coletas")
@GET("coletas")
```

#### Dashboard
```kotlin
// ANTES
@GET("api/mobile/dashboard/stats")

// DEPOIS
@GET("dashboard/stats")
```

### 3. Correção do PatrimonioApi

Também corrigido os endpoints da API legada:

```kotlin
// ANTES
@GET("patrimonios/numero/{numero}")

// DEPOIS
@GET("patrimonio/numero/{numero}")
```

## 📦 Arquivos Modificados

1. **ServerConfigManager.kt**
   - Método `buildBaseUrl()` atualizado

2. **ApiService.kt**
   - Todos os endpoints corrigidos (removido prefixo `api/mobile/`)

3. **PatrimonioApi.kt**
   - Endpoints corrigidos (singular `patrimonio`)

## 🔄 URLs Finais Corretas

### Autenticação
```
POST http://10.0.2.2:8081/inventario/api/mobile/auth/login
POST http://10.0.2.2:8081/inventario/api/mobile/auth/refresh
```

### Patrimônios
```
GET  http://10.0.2.2:8081/inventario/api/mobile/patrimonio
GET  http://10.0.2.2:8081/inventario/api/mobile/patrimonio/{id}
GET  http://10.0.2.2:8081/inventario/api/mobile/patrimonio/numero/{numero}
GET  http://10.0.2.2:8081/inventario/api/mobile/patrimonio/qr/{qrCode}
POST http://10.0.2.2:8081/inventario/api/mobile/patrimonio
```

### Salas
```
GET http://10.0.2.2:8081/inventario/api/mobile/salas
GET http://10.0.2.2:8081/inventario/api/mobile/salas/{id}
```

### Coletas
```
GET  http://10.0.2.2:8081/inventario/api/mobile/coletas
POST http://10.0.2.2:8081/inventario/api/mobile/coletas
```

### Dashboard
```
GET http://10.0.2.2:8081/inventario/api/mobile/dashboard/stats
```

## 🧪 Como Testar

### 1. Limpar Dados do App
```bash
adb shell pm clear com.inventario.mobile.debug
```

### 2. Instalar APK Atualizado
```bash
cd InventarioMobile
.\gradlew.bat installDebug
```

### 3. Configurar Servidor
- Abrir app
- Configurar IP: `10.0.2.2`
- Porta: `8081`

### 4. Testar Login
- Fazer login com credenciais válidas
- Verificar se o token é recebido

### 5. Testar Busca de Patrimônio
- Selecionar uma sala
- Ir para "Coleta Manual"
- Buscar patrimônio por número
- Verificar se encontra corretamente

## 📊 Resultado Esperado

### Login
```
✅ POST /inventario/api/mobile/auth/login → 200 OK
✅ Token JWT recebido
✅ Usuário autenticado
```

### Busca de Patrimônio
```
✅ GET /inventario/api/mobile/patrimonio/numero/9220 → 200 OK
✅ Patrimônio encontrado
✅ Dados exibidos na tela
```

## 🎯 Benefícios

1. **URLs Corretas**: Sem duplicação de prefixos
2. **Endpoints Funcionais**: Todas as chamadas API funcionam
3. **Código Limpo**: Endpoints mais legíveis
4. **Manutenibilidade**: Fácil adicionar novos endpoints

## 📝 Notas Importantes

- A base URL **sempre** inclui `/inventario/api/mobile/`
- Os endpoints **nunca** devem incluir `api/mobile/` no caminho
- Após mudanças na configuração, limpar dados do app
- O emulador usa `10.0.2.2` para acessar `localhost` do host

## 🔗 Referências

- Backend: `MobileAuthController.java` - `/api/mobile/auth`
- Backend: `MobilePatrimonioController.java` - `/api/mobile/patrimonio`
- Backend: `MobileSalaController.java` - `/api/mobile/salas`
- Backend: `MobileColetaController.java` - `/api/mobile/coletas`

---

**Data:** 14/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Implementado e Testado
