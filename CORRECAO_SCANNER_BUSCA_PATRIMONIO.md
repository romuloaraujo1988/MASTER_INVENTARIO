# Correção - Scanner Não Encontra Patrimônios

## 🐛 Problema Identificado

O scanner não está encontrando patrimônios porque:

1. ✅ **Método `findPatrimonioByNumero` estava como stub** - CORRIGIDO
2. ✅ **PatrimonioEntity tinha campos limitados** - CORRIGIDO  
3. ✅ **DAO usava coluna errada** - CORRIGIDO
4. ❌ **Incompatibilidade de tipos entre Entity, Model e DTO** - EM CORREÇÃO

## 📋 Incompatibilidades Encontradas

### PatrimonioEntity vs Patrimonio
- `PatrimonioEntity.id`: `Long`
- `Patrimonio.id`: `Long` ✅
- `PatrimonioEntity.salaId`: `Int?`
- `Patrimonio.salaId`: `Long?` ❌

### ColetaEntity vs Coleta
- `ColetaEntity.id`: `Long`
- `Coleta.id`: `Int?` ❌
- `ColetaEntity.idPatrimonio`: `Int`
- `Coleta.patrimonioId`: `Int` ✅
- `ColetaEntity.dataColeta`: `Long` (timestamp)
- `Coleta.dataColeta`: `String` ❌

### MobileColetaRequest
- Campos corretos: `numeroPatrimonio`, `idInventario`, `usuarioId`, `localizacaoEncontrada`, `estadoEncontrado`, `observacaoColeta`

## 🔧 Correções Aplicadas

### 1. PatrimonioEntity - Campos Completos ✅
```kotlin
@Entity(tableName = "patrimonio")
data class PatrimonioEntity(
    @PrimaryKey val id: Long,
    val numero: String,
    val numeroPatrimonio: String,
    val descricao: String,
    val marca: String? = null,
    val modelo: String? = null,
    // ... todos os campos necessários
)
```

### 2. InventarioRepository.findPatrimonioByNumero() ✅
- Busca primeiro no banco local (Room)
- Fallback para API se não encontrar
- Salva no cache local após buscar da API

### 3. InventarioRepository.coletarPatrimonioComSala() ✅
- Envia para API com MobileColetaRequest correto
- Salva localmente em caso de offline
- Marca patrimônio como coletado

## ⚠️ Correções Pendentes

### 1. Ajustar tipos incompatíveis
- Padronizar IDs como `Long` em todos os lugares
- Converter `dataColeta` de `String` para `Long` no modelo Coleta
- Ajustar `salaId` para `Long?` em Patrimonio

### 2. Adicionar método no PreferencesManager
```kotlin
fun getInventarioId(): Int {
    return sharedPreferences.getInt("inventario_id", 1)
}
```

### 3. Simplificar mapeamento Entity → Model
- Criar mapper dedicado
- Evitar conversões manuais repetidas

## 🎯 Solução Imediata

Para fazer o scanner funcionar AGORA sem quebrar o código existente:

1. Manter modelo `Coleta` como está (compatibilidade)
2. Criar conversores de tipo no repositório
3. Adicionar `getInventarioId()` no PreferencesManager
4. Ajustar mapeamento de ColetaEntity

## 📝 Próximos Passos

1. Adicionar `getInventarioId()` no PreferencesManager
2. Criar conversores de tipo no repositório
3. Testar busca de patrimônio no scanner
4. Testar coleta de patrimônio
5. Validar sincronização

---

**Status**: Em correção  
**Prioridade**: ALTA  
**Impacto**: Scanner não funciona sem isso
