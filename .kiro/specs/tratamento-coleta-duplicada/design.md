# Design Document - Tratamento de Coleta Duplicada

## Overview

Este documento descreve o design para implementar o tratamento graceful de coletas duplicadas no servidor mobile. A solução permite que o servidor identifique quando uma coleta já existe e retorne uma resposta informativa ao invés de lançar exceção, evitando que a conexão do smartphone seja derrubada.

## Architecture

A solução segue a arquitetura existente do servidor mobile, adicionando uma camada de verificação de duplicatas antes da inserção:

```
┌─────────────────────────────────────────────────────────────┐
│                    MobileColetaController                    │
│  - Recebe requisições REST                                   │
│  - Retorna HTTP 200 para duplicatas (não 4xx/5xx)           │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    MobileColetaService                       │
│  - verificarColetaDuplicada() [NOVO]                        │
│  - registrarColeta() [MODIFICADO]                           │
│  - registrarColetasEmLote() [MODIFICADO]                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                       ColetaDAO                              │
│  - buscarColetaExistente() [NOVO]                           │
│  - coletaExiste() [EXISTENTE]                               │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. MobileColetaResponse (DTO Modificado)

Adicionar novos campos para indicar status de duplicata:

```java
public class MobileColetaResponse {
    // Campos existentes...
    
    // Novos campos para duplicata
    private boolean duplicada;
    private String mensagemDuplicada;
    private Long coletaOriginalId;
    private String dataColetaOriginal;
    private String coletorOriginal;
}
```

### 2. MobileColetaService (Métodos Modificados)

#### 2.1 Novo método: verificarColetaDuplicada()

```java
/**
 * Verifica se já existe uma coleta para o patrimônio no inventário
 * @return Coleta existente ou null se não houver duplicata
 */
private Coleta verificarColetaDuplicada(Integer idInventario, Integer idPatrimonio) {
    // Busca coleta existente no banco
}
```

#### 2.2 Método modificado: registrarColeta()

```java
public MobileColetaResponse registrarColeta(MobileColetaRequest request, String username) {
    // 1. Validações existentes...
    
    // 2. NOVO: Verificar duplicata ANTES de inserir
    Coleta coletaExistente = verificarColetaDuplicada(idInventario, idPatrimonio);
    if (coletaExistente != null) {
        return criarRespostaDuplicada(coletaExistente, request);
    }
    
    // 3. Inserir coleta (código existente)...
}
```

#### 2.3 Método modificado: registrarColetasEmLote()

```java
public Map<String, Object> registrarColetasEmLote(List<MobileColetaRequest> coletas, String username) {
    // Adicionar contagem de duplicatas
    int duplicadas = 0;
    List<String> coletasDuplicadas = new ArrayList<>();
    
    // Processar cada coleta, contando duplicatas separadamente
}
```

### 3. ColetaDAO (Método Novo)

```java
/**
 * Busca coleta existente por inventário e patrimônio
 * @return Coleta com dados do coletor ou null
 */
public Coleta buscarColetaExistente(Integer idInventario, Integer idPatrimonio) {
    // Query com JOIN para trazer nome do coletor
}
```

## Data Models

### Resposta de Coleta Duplicada

```json
{
    "success": true,
    "message": "Coleta já registrada anteriormente",
    "data": {
        "id": null,
        "duplicada": true,
        "mensagemDuplicada": "Este patrimônio já foi coletado neste inventário. Exclua a coleta local.",
        "coletaOriginalId": 12345,
        "dataColetaOriginal": "2025-12-01T10:30:00",
        "coletorOriginal": "João Silva",
        "numeroPatrimonio": "PAT-001",
        "idInventario": 5,
        "sincronizado": false
    }
}
```

### Resposta de Lote com Duplicatas

```json
{
    "success": true,
    "message": "Lote processado com duplicatas",
    "data": {
        "total": 10,
        "sucesso": 7,
        "falhas": 1,
        "duplicadas": 2,
        "erros": ["Patrimônio PAT-999: Erro de validação"],
        "coletasDuplicadas": ["PAT-001", "PAT-002"]
    }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Duplicate Detection Returns Valid Response

*For any* collection request where a collection already exists for the same patrimony in the same inventory, the service SHALL return a response with `duplicada=true`, HTTP status 200, and include the original collection ID, without throwing any exception.

**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Duplicate Response Contains Complete Information

*For any* duplicate collection response, the response SHALL contain all required fields: `duplicada=true`, non-empty `mensagemDuplicada`, valid `dataColetaOriginal` matching the original collection timestamp, and `coletorOriginal` matching the original collector's name.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

### Property 3: Batch Processing Handles Duplicates Gracefully

*For any* batch of collections containing N items where D items are duplicates, the service SHALL process all N items, report exactly D duplicates in the summary, and successfully insert (N - D - F) items where F is the count of items that failed for other reasons.

**Validates: Requirements 1.4**

## Error Handling

### Cenários de Erro

| Cenário | Comportamento Atual | Comportamento Novo |
|---------|--------------------|--------------------|
| Coleta duplicada | Lança SQLException | Retorna response com `duplicada=true` |
| Patrimônio não encontrado | Lança IllegalArgumentException | Mantém comportamento (erro válido) |
| Usuário não participante | Lança IllegalArgumentException | Mantém comportamento (erro válido) |
| Erro de banco | Lança SQLException | Mantém comportamento (erro de sistema) |

### Logging

- **WARN**: Coleta duplicada detectada (patrimônio, inventário, usuário)
- **INFO**: Múltiplas tentativas de duplicata do mesmo usuário
- **DEBUG**: Verificação de duplicata executada

## Testing Strategy

### Dual Testing Approach

A implementação será validada usando testes unitários e testes baseados em propriedades (PBT).

### Unit Tests

1. **Teste de detecção de duplicata**: Inserir coleta, tentar inserir novamente, verificar resposta
2. **Teste de campos da resposta**: Verificar todos os campos obrigatórios na resposta de duplicata
3. **Teste de lote com duplicatas**: Enviar lote misto, verificar contagens corretas
4. **Teste de logging**: Verificar que logs são gerados corretamente

### Property-Based Tests

Utilizaremos **JUnit 5 com jqwik** para testes baseados em propriedades.

#### PBT 1: Duplicate Detection Idempotency
- Gerar coletas aleatórias
- Inserir cada coleta
- Tentar inserir novamente
- Verificar que TODAS retornam `duplicada=true` sem exceção

#### PBT 2: Response Completeness
- Para qualquer coleta duplicada detectada
- Verificar que TODOS os campos obrigatórios estão presentes e válidos

#### PBT 3: Batch Processing Correctness
- Gerar lotes aleatórios com mix de novas coletas e duplicatas
- Verificar que a soma (sucesso + falhas + duplicadas) = total

### Test Configuration

```java
// Configuração mínima de 100 iterações por propriedade
@Property(tries = 100)
```

### Test Annotations

Cada teste de propriedade deve ser anotado com referência ao design:

```java
/**
 * Feature: tratamento-coleta-duplicada, Property 1: Duplicate Detection Returns Valid Response
 * Validates: Requirements 1.1, 1.2, 1.3
 */
@Property
void duplicateDetectionReturnsValidResponse(@ForAll ...) { }
```
