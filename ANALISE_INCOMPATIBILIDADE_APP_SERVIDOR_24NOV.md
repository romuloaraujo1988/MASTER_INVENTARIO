# Análise de Incompatibilidade App Android ↔ Servidor

## 🔍 Problemas Identificados

### 1. ⚠️ CRÍTICO: Formato de Data Incompatível

**Servidor (Java):**
```java
@JsonProperty("dataColeta")
private LocalDateTime dataColeta;  // Serializa como array [2025,11,24,21,30,45]
```

**App Android (Kotlin):**
```kotlin
@SerializedName("dataColeta")
val dataColeta: String? = null  // Espera String "2025-11-24T21:30:45"
```

**Problema:** 
- Servidor retorna `LocalDateTime` sem formatação
- Jackson serializa como array: `[2025, 11, 24, 21, 30, 45, 123456789]`
- App espera String formatada: `"2025-11-24T21:30:45"`

**Impacto:** App não consegue parsear a data e pode crashar ou ignorar o campo

---

### 2. ⚠️ Campo com Nome Diferente

**Servidor:**
```java
@JsonProperty("observacaoColeta")
private String observacaoColeta;
```

**App Android:**
```kotlin
@SerializedName("observacaoColeta")
val observacoes: String? = null  // Nome da variável diferente
```

**Status:** ✅ OK - O `@SerializedName` está correto, apenas o nome da variável é diferente

---

### 3. ⚠️ Campo Faltando no App

**Servidor tem:**
```java
@JsonProperty("localizacaoAtual")
private String localizacaoAtual;  // ❌ NÃO EXISTE NO SERVIDOR!
```

**App espera:**
```kotlin
@SerializedName("localizacaoAtual")
val localizacaoAtual: String? = null
```

**Status:** ⚠️ Campo não existe no servidor, mas é nullable no app (não causa erro)

---

## 🔧 Soluções

### Solução 1: Formatar Data no Servidor (RECOMENDADO)

Adicionar anotação `@JsonFormat` no campo `dataColeta`:

```java
@JsonProperty("dataColeta")
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime dataColeta;
```

**OU** converter para String antes de retornar:

```java
@JsonProperty("dataColeta")
private String dataColeta;  // Já formatado como String

// No service, ao criar o DTO:
response.setDataColeta(coleta.getDataColeta().format(
    DateTimeFormatter.ISO_LOCAL_DATE_TIME
));
```

---

### Solução 2: Adicionar Campo `localizacaoAtual` no Servidor

Se o campo for necessário, adicionar no DTO:

```java
@JsonProperty("localizacaoAtual")
private String localizacaoAtual;

// Getter e Setter
public String getLocalizacaoAtual() {
    return localizacaoAtual;
}

public void setLocalizacaoAtual(String localizacaoAtual) {
    this.localizacaoAtual = localizacaoAtual;
}
```

---

### Solução 3: Configurar Jackson Globalmente

Adicionar no `application.properties`:

```properties
# Formato de data/hora para JSON
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
```

---

## 🧪 Como Testar

### 1. Testar Resposta do Servidor

```bash
curl -X POST http://localhost:8081/api/mobile/coletas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "numeroPatrimonio": "12345",
    "localizacaoEncontrada": "Sala 101",
    "estadoEncontrado": "BOM",
    "observacaoColeta": "Teste"
  }'
```

**Verificar se retorna:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "numeroPatrimonio": "12345",
    "dataColeta": "2025-11-24T21:30:45",  // ✅ String formatada
    // OU
    "dataColeta": [2025, 11, 24, 21, 30, 45]  // ❌ Array (problema!)
  }
}
```

### 2. Testar no App Android

Adicionar logs no app para ver o que está recebendo:

```kotlin
// No Repository ou ViewModel
try {
    val response = coletaApi.registrarColeta(request)
    Log.d("ColetaApi", "Response: ${response.data}")
    Log.d("ColetaApi", "Data Coleta: ${response.data?.dataColeta}")
} catch (e: Exception) {
    Log.e("ColetaApi", "Erro ao parsear resposta", e)
}
```

---

## 📋 Checklist de Correção

### Servidor (Java)
- [ ] Adicionar `@JsonFormat` no campo `dataColeta`
- [ ] OU converter `LocalDateTime` para `String` no service
- [ ] OU configurar Jackson globalmente
- [ ] Adicionar campo `localizacaoAtual` (se necessário)
- [ ] Testar endpoint com curl
- [ ] Verificar formato JSON retornado

### App Android
- [ ] Verificar se está recebendo data como String
- [ ] Adicionar logs de debug
- [ ] Testar parsing do JSON
- [ ] Verificar se não há crashes
- [ ] Validar que coletas são salvas corretamente

---

## 🎯 Recomendação Final

**Opção Mais Simples e Segura:**

1. Mudar o tipo do campo no servidor de `LocalDateTime` para `String`
2. Formatar a data no service antes de retornar
3. Isso garante compatibilidade total com o app

**Exemplo:**

```java
// MobileColetaResponse.java
@JsonProperty("dataColeta")
private String dataColeta;  // String ao invés de LocalDateTime

// MobileColetaService.java
public MobileColetaResponse registrarColeta(...) {
    // ...
    response.setDataColeta(
        coleta.getDataColeta().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    );
    return response;
}
```

---

## 📊 Comparação de Formatos

| Formato | Servidor Atual | App Espera | Compatível? |
|---------|---------------|------------|-------------|
| `dataColeta` | `[2025,11,24,21,30]` | `"2025-11-24T21:30:45"` | ❌ NÃO |
| `numeroPatrimonio` | `"12345"` | `"12345"` | ✅ SIM |
| `observacaoColeta` | `"texto"` | `"texto"` | ✅ SIM |
| `localizacaoAtual` | ❌ não existe | `"Sala 101"` | ⚠️ Nullable |
| `sincronizado` | `true` | `true` | ✅ SIM |

---

**Status:** ⚠️ INCOMPATIBILIDADE DETECTADA  
**Prioridade:** 🔴 ALTA  
**Impacto:** App pode não conseguir parsear respostas do servidor  
**Data:** 24/11/2025
