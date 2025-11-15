# Novos Endpoints de Validação - Documentação

## 📋 Visão Geral

Implementação de 3 endpoints críticos para validação e prevenção de erros no processo de coleta de patrimônios.

---

## 🎯 Endpoints Implementados

### 1. Verificar se Patrimônio Foi Coletado

**Endpoint:** `GET /api/mobile/patrimonio/numero/{numero}/coletado`

**Parâmetros:**
- `numero` (path) - Número do patrimônio
- `inventarioId` (query, opcional) - ID do inventário (usa ativo se não informado)

**Exemplo de Requisição:**
```http
GET /api/mobile/patrimonio/numero/12345/coletado?inventarioId=2
Authorization: Bearer {token}
```

**Resposta de Sucesso (200 OK):**
```json
{
  "success": true,
  "message": "Patrimônio já foi coletado",
  "data": {
    "coletado": true,
    "numeroPatrimonio": "12345",
    "patrimonioId": 150,
    "inventarioId": 2,
    "inventarioNome": "Inventário 2024",
    "dataColeta": "14/11/2024 10:30:00",
    "coletadoPor": "João Silva",
    "coletaId": 523,
    "observacoes": "Patrimônio em bom estado",
    "localizacaoEncontrada": "Sala 101",
    "estadoEncontrado": "BOM"
  }
}
```

**Resposta quando NÃO foi coletado (200 OK):**
```json
{
  "success": true,
  "message": "Patrimônio ainda não foi coletado",
  "data": {
    "coletado": false,
    "numeroPatrimonio": "12345",
    "patrimonioId": 150,
    "inventarioId": 2,
    "inventarioNome": "Inventário 2024"
  }
}
```

**Casos de Erro:**
- `400 BAD_REQUEST` - Patrimônio ou inventário não encontrado
- `500 INTERNAL_SERVER_ERROR` - Erro no servidor

---

### 2. Validar Patrimônio Antes de Coletar

**Endpoint:** `GET /api/mobile/patrimonio/numero/{numero}/validar`

**Parâmetros:**
- `numero` (path) - Número do patrimônio

**Exemplo de Requisição:**
```http
GET /api/mobile/patrimonio/numero/12345/validar
Authorization: Bearer {token}
```

**Resposta - Patrimônio Válido (200 OK):**
```json
{
  "success": true,
  "message": "Patrimônio válido e pode ser coletado",
  "data": {
    "valido": true,
    "patrimonio": {
      "id": 150,
      "codigo": "12345",
      "descricao": "Cadeira Giratória",
      "marca": "Marca X",
      "modelo": "Modelo Y",
      "estado": "BOM",
      "salaId": 10,
      "salaNome": "Sala 101",
      "responsavelId": 5,
      "responsavelNome": "Maria Santos",
      "valor": 350.00,
      "coletado": false
    },
    "jaColetado": false
  }
}
```

**Resposta - Patrimônio Já Coletado (200 OK com aviso):**
```json
{
  "success": true,
  "message": "Patrimônio válido e pode ser coletado",
  "data": {
    "valido": true,
    "patrimonio": {...},
    "jaColetado": true,
    "avisoColeta": "Este patrimônio já foi coletado anteriormente",
    "coletadoPor": "João Silva",
    "dataColeta": "14/11/2024 10:30:00"
  }
}
```

**Resposta - Patrimônio Não Encontrado (400 BAD_REQUEST):**
```json
{
  "success": true,
  "message": "Patrimônio não encontrado no sistema",
  "data": {
    "valido": false,
    "motivo": "PATRIMONIO_NAO_ENCONTRADO",
    "mensagem": "Patrimônio não encontrado no sistema"
  }
}
```

**Resposta - Patrimônio Inativo (400 BAD_REQUEST):**
```json
{
  "success": true,
  "message": "Patrimônio está inativo ou baixado",
  "data": {
    "valido": false,
    "motivo": "PATRIMONIO_INATIVO",
    "mensagem": "Patrimônio está inativo ou baixado"
  }
}
```

---

### 3. Verificar Duplicata de Coleta

**Endpoint:** `POST /api/mobile/coletas/verificar-duplicata`

**Body:**
```json
{
  "numeroPatrimonio": "12345",
  "inventarioId": 2
}
```

**Exemplo de Requisição:**
```http
POST /api/mobile/coletas/verificar-duplicata
Authorization: Bearer {token}
Content-Type: application/json

{
  "numeroPatrimonio": "12345",
  "inventarioId": 2
}
```

**Resposta - NÃO Duplicado (200 OK):**
```json
{
  "success": true,
  "message": "Patrimônio pode ser coletado",
  "data": {
    "duplicado": false,
    "numeroPatrimonio": "12345",
    "patrimonioId": 150,
    "inventarioId": 2,
    "podeRegistrar": true,
    "mensagem": "Patrimônio pode ser coletado"
  }
}
```

**Resposta - DUPLICADO (409 CONFLICT):**
```json
{
  "success": true,
  "message": "Este patrimônio já foi coletado neste inventário",
  "data": {
    "duplicado": true,
    "numeroPatrimonio": "12345",
    "patrimonioId": 150,
    "inventarioId": 2,
    "podeRegistrar": false,
    "mensagem": "Este patrimônio já foi coletado neste inventário",
    "motivo": "COLETA_DUPLICADA",
    "coletaExistente": {
      "id": 523,
      "dataColeta": "14/11/2024 10:30:00",
      "coletadoPor": "João Silva",
      "localizacao": "Sala 101",
      "estado": "BOM"
    }
  }
}
```

**Resposta - Patrimônio Não Encontrado (200 OK):**
```json
{
  "success": true,
  "message": "Patrimônio não encontrado",
  "data": {
    "duplicado": false,
    "motivo": "PATRIMONIO_NAO_ENCONTRADO",
    "mensagem": "Patrimônio não encontrado",
    "podeRegistrar": false
  }
}
```

---

## 🔄 Fluxo de Uso Recomendado

### Fluxo 1: Coleta com QR Code
```
1. Usuário escaneia QR Code
2. App chama: GET /patrimonio/numero/{numero}/validar
3. Se válido:
   - Mostrar dados do patrimônio
   - Se jaColetado=true, mostrar aviso
   - Permitir registro
4. Se inválido:
   - Mostrar mensagem de erro
   - Não permitir registro
```

### Fluxo 2: Coleta Manual
```
1. Usuário digita número do patrimônio
2. App chama: GET /patrimonio/numero/{numero}/validar
3. Se válido:
   - Mostrar dados para confirmação
   - Botão "Registrar Coleta"
4. Ao clicar "Registrar":
   - Chamar: POST /coletas/verificar-duplicata
   - Se não duplicado: registrar
   - Se duplicado: mostrar alerta
```

### Fluxo 3: Verificação Rápida
```
1. Usuário quer saber se já coletou
2. App chama: GET /patrimonio/numero/{numero}/coletado
3. Mostrar status:
   - ✅ Já coletado (com detalhes)
   - ❌ Ainda não coletado
```

---

## 📱 Integração no App Android

### Adicionar no PatrimonioApi.kt

```kotlin
interface PatrimonioApi {
    
    @GET("api/mobile/patrimonio/numero/{numero}/coletado")
    suspend fun verificarSePatrimonioFoiColetado(
        @Path("numero") numero: String,
        @Query("inventarioId") inventarioId: Int? = null
    ): ApiResponse<Map<String, Any>>
    
    @GET("api/mobile/patrimonio/numero/{numero}/validar")
    suspend fun validarPatrimonio(
        @Path("numero") numero: String
    ): ApiResponse<Map<String, Any>>
}
```

### Adicionar no ColetaApi.kt

```kotlin
interface ColetaApi {
    
    @POST("api/mobile/coletas/verificar-duplicata")
    suspend fun verificarDuplicataColeta(
        @Body request: VerificarDuplicataRequest
    ): ApiResponse<Map<String, Any>>
}

data class VerificarDuplicataRequest(
    val numeroPatrimonio: String,
    val inventarioId: Int? = null
)
```

### Use Case de Validação

```kotlin
class ValidarPatrimonioUseCase @Inject constructor(
    private val patrimonioApi: PatrimonioApi
) {
    suspend operator fun invoke(numeroPatrimonio: String): Result<ValidationResult> {
        return try {
            val response = patrimonioApi.validarPatrimonio(numeroPatrimonio)
            
            if (response.success) {
                val data = response.data
                val valido = data["valido"] as Boolean
                
                if (valido) {
                    Result.success(ValidationResult.Valid(data))
                } else {
                    val motivo = data["motivo"] as String
                    Result.success(ValidationResult.Invalid(motivo, data["mensagem"] as String))
                }
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class ValidationResult {
    data class Valid(val data: Map<String, Any>) : ValidationResult()
    data class Invalid(val motivo: String, val mensagem: String) : ValidationResult()
}
```

---

## 🎯 Benefícios

### UX Melhorada
- ✅ Feedback imediato sobre validade do patrimônio
- ✅ Aviso antes de registrar duplicata
- ✅ Informações completas sobre coletas anteriores
- ✅ Reduz frustração do usuário

### Prevenção de Erros
- ✅ Evita coletas duplicadas
- ✅ Valida patrimônio antes de coletar
- ✅ Detecta patrimônios inexistentes
- ✅ Identifica patrimônios inativos

### Performance
- ✅ Validação rápida (apenas 1 query)
- ✅ Cache de resultados possível
- ✅ Reduz requisições desnecessárias

### Auditoria
- ✅ Logs detalhados de validações
- ✅ Rastreamento de tentativas de duplicação
- ✅ Histórico de verificações

---

## 🧪 Testes Recomendados

### Teste 1: Patrimônio Válido
```
1. Chamar /validar com número existente
2. Verificar response.data.valido = true
3. Verificar dados do patrimônio presentes
```

### Teste 2: Patrimônio Já Coletado
```
1. Coletar patrimônio X
2. Chamar /validar com número X
3. Verificar jaColetado = true
4. Verificar avisoColeta presente
```

### Teste 3: Duplicata
```
1. Coletar patrimônio X
2. Chamar /verificar-duplicata com número X
3. Verificar duplicado = true
4. Verificar status 409 CONFLICT
5. Verificar coletaExistente presente
```

### Teste 4: Patrimônio Inexistente
```
1. Chamar /validar com número inválido
2. Verificar valido = false
3. Verificar motivo = "PATRIMONIO_NAO_ENCONTRADO"
```

---

## 📊 Métricas de Sucesso

- **Redução de duplicatas:** Esperado 95%+ de redução
- **Tempo de validação:** < 200ms por requisição
- **Taxa de erro:** < 1% de falsos positivos
- **Satisfação do usuário:** Feedback positivo sobre avisos

---

**Implementado em:** 14/11/2025
**Versão:** 1.0.0
**Status:** ✅ Pronto para produção
