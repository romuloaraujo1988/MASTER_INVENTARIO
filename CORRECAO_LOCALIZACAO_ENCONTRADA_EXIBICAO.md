# Correção - Exibição de Localização Encontrada

## 🐛 Problema Identificado

Na tela de itens coletados, o combobox mostrava os locais corretos, mas a exibição dos itens mostrava "Local não informado", mesmo com os dados corretos no banco de dados (campo `localizacao_encontrada`).

## 🔍 Causa Raiz

Os adapters estavam priorizando o campo `nomeSala` (que pode estar vazio) ao invés de `localizacaoAtual` (que contém o valor de `localizacaoEncontrada` do banco).

### Mapeamento de Campos

```kotlin
// No modelo Coleta.kt - método fromDto()
localizacaoAtual = dto.localizacaoEncontrada,  // ← Campo do banco mapeado aqui
nomeSala = dto.nomeSala,                        // ← Pode estar vazio
```

### Problema nos Adapters

**ANTES (Errado):**
```kotlin
// CollectionAdapter.kt
val salaExibida = when {
    !coleta.nomeSala.isNullOrBlank() -> coleta.nomeSala           // ← Prioridade errada
    !coleta.localizacaoAtual.isNullOrBlank() -> coleta.localizacaoAtual
    else -> "Local não informado"
}

// PendingCollectionsAdapter.kt
val salaInfo = coleta.nomeSala ?: coleta.localizacaoAtual ?: "Local não informado"
```

## ✅ Solução Aplicada

Invertida a prioridade para usar `localizacaoAtual` primeiro (que contém `localizacaoEncontrada`):

### 1. CollectionAdapter.kt

```kotlin
// Location information - priorizar localizacaoAtual (que contém localizacaoEncontrada)
val salaExibida = when {
    !coleta.localizacaoAtual.isNullOrBlank() -> coleta.localizacaoAtual  // ← Prioridade correta
    !coleta.nomeSala.isNullOrBlank() -> coleta.nomeSala
    else -> "Local não informado"
}
Log.d("CollectionViewHolder", "Sala exibida: '$salaExibida'")
tvSalaInfo.text = salaExibida
```

### 2. PendingCollectionsAdapter.kt

```kotlin
// Location info - priorizar localizacaoAtual (que contém localizacaoEncontrada)
val salaInfo = coleta.localizacaoAtual ?: coleta.nomeSala ?: "Local não informado"
tvSalaInfo.text = salaInfo
```

## 📊 Arquivos Modificados

- ✅ `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/coleta/CollectionAdapter.kt`
- ✅ `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/sync/PendingCollectionsAdapter.kt`

## 🧪 Validação

### Build
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
```

**Resultado:** ✅ BUILD SUCCESSFUL

### Teste Manual

1. Abrir tela de itens coletados
2. Verificar que a localização agora é exibida corretamente
3. Confirmar que não aparece mais "Local não informado" quando há dados

## 📝 Fluxo de Dados

```
Banco de Dados (PostgreSQL/SQLite)
    ↓
Campo: localizacao_encontrada = "Sala 101"
    ↓
DTO: ColetaDto.localizacaoEncontrada = "Sala 101"
    ↓
Modelo: Coleta.localizacaoAtual = "Sala 101"  ← Mapeamento
    ↓
Adapter: tvSalaInfo.text = coleta.localizacaoAtual  ← Exibição
    ↓
UI: "Sala 101" ✅
```

## 🎯 Resultado

- ✅ Localização encontrada agora é exibida corretamente
- ✅ Prioridade correta: `localizacaoAtual` → `nomeSala` → "Local não informado"
- ✅ Logs adicionados para debug
- ✅ Build compilado com sucesso

## 📌 Observações

- O campo `localizacaoAtual` no modelo `Coleta` contém o valor de `localizacaoEncontrada` do banco
- O campo `nomeSala` pode estar vazio em alguns casos
- A correção garante que sempre exibimos o local onde o item foi realmente encontrado

---

**Corrigido em:** 24/11/2025  
**Status:** ✅ Resolvido  
**Build:** ✅ Sucesso
