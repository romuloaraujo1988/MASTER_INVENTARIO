# Debug - Tela Itens Coletados

## 🔴 Problema Atual

**Erro**: `JsonSyntaxException: Expected BEGIN_ARRAY but was BEGIN_OBJECT at line 1 column 25 path $.data`

**Localização**: `InventarioRepository.getAllPatrimoniosList()` ao fazer parsing da resposta da API

## 🔍 Análise do Erro

O erro indica que o Gson está tentando deserializar o campo `data` do JSON como um **array** (`List<MobilePatrimonioDto>`), mas está recebendo um **objeto**.

### Estrutura Esperada (Correta)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "codigo": "123456",
      "descricao": "Computador",
      ...
    },
    {
      "id": 2,
      "codigo": "789012",
      "descricao": "Mesa",
      ...
    }
  ],
  "message": "50 patrimônio(s) carregado(s)"
}
```

### Estrutura Recebida (Incorreta)
```json
{
  "success": true,
  "data": {
    // Objeto em vez de array
    "patrimonios": [...],
    "total": 100,
    "page": 0
  },
  "message": "..."
}
```

## 🛠️ Correções Tentadas

### 1. ✅ Implementação do Repository
- Implementado `getAllPatrimoniosList()` para chamar `apiService.getAllPatrimonios()`
- Mapeamento correto de DTOs para Models

### 2. ✅ Service Backend Otimizado
- Alterado para usar `patrimonioDAO.listarTodosComJoins()` em vez de iterar por salas
- Retorna `List<MobilePatrimonioDTO>` corretamente

### 3. ✅ Controller com Logs Detalhados
- Adicionados logs para verificar tipo e tamanho da lista retornada
- Confirmado que está retornando `List<MobilePatrimonioDTO>`

### 4. ✅ DTO Backend - Tipo de Data
- Alterado `dataColeta` de `LocalDateTime` para `String`
- Removido `@JsonFormat` que poderia causar problemas

### 5. ✅ ApiResponse - Remoção de Timestamp
- Removido campo `timestamp` do tipo `LocalDateTime`
- Simplificado para evitar problemas de serialização

### 6. ✅ Gson com Adaptador de Datas
- Configurado `GsonBuilder` com `setLenient()`
- Adicionado adaptador para deserializar datas

## 🔎 Próximos Passos para Debug

### 1. Verificar Logs do Servidor
Procurar por logs que começam com:
```
═══════════════════════════════════════════
LISTANDO PATRIMÔNIOS
```

Verificar:
- Tipo da lista retornada
- Tamanho da lista
- Tipo do ApiResponse.data

### 2. Verificar Logs do Android (Logcat)
Procurar por logs que começam com:
```
NetworkModule: === RESPONSE AFTER DECOMPRESSION ===
```

Verificar:
- Corpo completo da resposta HTTP
- Estrutura do JSON retornado
- Se o campo `data` é realmente um array

### 3. Testar Endpoint Diretamente
Usar curl ou Postman para testar:
```bash
curl -X GET "http://localhost:8080/api/mobile/patrimonio?page=0&size=10" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json"
```

Verificar se a resposta tem a estrutura correta.

## 🎯 Possíveis Causas Restantes

### 1. Interceptor Modificando Resposta
Algum interceptor (compressão, cache, etc.) pode estar alterando a estrutura do JSON.

**Solução**: Desabilitar temporariamente todos os interceptors exceto logging.

### 2. Múltiplos Endpoints com Mesmo Path
Pode haver outro endpoint com `@GetMapping` sem path específico que está sendo chamado.

**Solução**: Verificar se há conflito de rotas no controller.

### 3. Serialização Customizada no Backend
Pode haver um `@JsonSerializer` customizado que está alterando a estrutura.

**Solução**: Verificar se há configurações customizadas de Jackson no backend.

### 4. Problema de Versão do Jackson/Gson
Incompatibilidade entre versões pode causar problemas de serialização.

**Solução**: Verificar versões no `pom.xml` e `build.gradle`.

## 📋 Checklist de Verificação

- [ ] Servidor backend reiniciado após mudanças
- [ ] App Android reinstalado após mudanças
- [ ] Logs do servidor mostram lista sendo retornada
- [ ] Logs do Android mostram resposta HTTP completa
- [ ] Endpoint testado diretamente (curl/Postman)
- [ ] Estrutura do JSON verificada manualmente
- [ ] Token de autenticação válido
- [ ] Sem erros 401/403 no servidor

## 🔧 Solução Temporária

Se o problema persistir, podemos criar um endpoint específico que retorna apenas um array simples:

```java
@GetMapping("/list-simple")
public ResponseEntity<List<MobilePatrimonioDTO>> listarPatrimoniosSimples() {
    List<MobilePatrimonioDTO> patrimonios = patrimonioService.listarPatrimonios(0, 100);
    return ResponseEntity.ok(patrimonios);
}
```

E no Android, chamar esse endpoint sem `ApiResponse`:

```kotlin
@GET("api/mobile/patrimonio/list-simple")
suspend fun getAllPatrimoniosSimple(): Response<List<MobilePatrimonioDto>>
```

---

**Status**: 🔴 Em investigação  
**Última atualização**: 09/11/2025 09:08
