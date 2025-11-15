# Debug: Apenas 1 Coleta Aparece (de 23)

## 🐛 Problema

- **Total no banco**: 23 coletas
- **Aparece na tela**: 1 coleta
- **Filtro ativo**: "Todas" (deveria mostrar todas)

## 🔍 Possíveis Causas

### 1. Problema de Tipo de Dados
```kotlin
// ViewModel
private var usuarioAtualId: Int? = null

// Filtro
val minhas = filtradas.filter { it.usuarioId == usuarioAtualId }
```

Se `usuarioAtualId` for `null`, o filtro pode estar falhando.

### 2. Problema no BuscarColetasUseCase
```kotlin
// Use Case busca da ColetaEntity
val entities = coletaDao.buscarTodas()

// Converte para data.model.Coleta
val coletas = entities.map { entity ->
    Coleta(
        usuarioId = entity.idUsuario,  // ← Pode estar vazio/errado
        // ...
    )
}
```

Se `entity.idUsuario` estiver com valor incorreto, o filtro pode falhar.

### 3. Problema no Filtro de Usuário
```kotlin
FiltroUsuario.MINHAS -> {
    val minhas = filtradas.filter { it.usuarioId == usuarioAtualId }
    // Se usuarioAtualId não corresponder, retorna vazio
}
```

## 🔧 Solução Temporária

Forçar filtro "TODAS" para sempre mostrar todas as coletas:

```kotlin
// CollectionViewViewModelClean.kt
private fun aplicarFiltros(coletas: List<Coleta>): List<Coleta> {
    // TEMPORÁRIO: Sempre usar filtro TODAS
    var filtradas = coletas
    
    // Comentar filtro de usuário temporariamente
    /*
    filtradas = when (filtroUsuario) {
        FiltroUsuario.TODAS -> filtradas
        FiltroUsuario.MINHAS -> filtradas.filter { it.usuarioId == usuarioAtualId }
    }
    */
    
    // Aplicar apenas filtros de status e sala
    // ...
}
```

## 📊 Dados para Verificar

### No Logcat, procurar por:
```
CollectionViewVMClean: COLETAS CARREGADAS DO BANCO
CollectionViewVMClean: Total de coletas: 23
CollectionViewVMClean: APLICANDO FILTROS
CollectionViewVMClean: Filtro Usuário: TODAS
CollectionViewVMClean: Usuário Atual ID: ???
CollectionViewVMClean: Resultado final: 1 coletas  ← PROBLEMA AQUI
```

### Verificar:
1. Quantas coletas são carregadas do banco?
2. Qual é o `usuarioAtualId`?
3. Quais são os `usuarioId` nas coletas?
4. O filtro está sendo aplicado corretamente?

## 🎯 Solução Definitiva

### Opção 1: Corrigir o usuarioId nas Coletas
Atualizar o `BuscarColetasUseCase` para garantir que o `usuarioId` seja preenchido corretamente:

```kotlin
val coletas = entities.map { entity ->
    Coleta(
        id = entity.id.toInt(),
        patrimonioId = entity.idPatrimonio,
        usuarioId = entity.idUsuario,  // ← Verificar se está correto
        // ...
    )
}
```

### Opção 2: Desabilitar Filtro de Usuário
Se o filtro de usuário não for necessário, remover ou desabilitar:

```kotlin
// Sempre mostrar todas as coletas
private var filtroUsuario: FiltroUsuario = FiltroUsuario.TODAS

// No método aplicarFiltros, sempre usar TODAS
filtradas = coletas  // Sem filtro de usuário
```

### Opção 3: Corrigir Comparação
Garantir que a comparação seja feita corretamente:

```kotlin
FiltroUsuario.MINHAS -> {
    if (usuarioAtualId != null) {
        // Converter ambos para Int para garantir comparação correta
        val minhas = filtradas.filter { 
            it.usuarioId.toInt() == usuarioAtualId.toInt() 
        }
        minhas
    } else {
        filtradas  // Se não tem usuário, mostrar todas
    }
}
```

## 🚀 Ação Imediata

Vou implementar logs detalhados para identificar exatamente onde está o problema.

---

**Status**: 🔍 Investigando  
**Prioridade**: 🔴 Alta  
**Data**: 14/11/2025
