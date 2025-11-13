# 🔧 Correção - Filtro de Coletas não Funciona

## 📋 Problema Identificado

Na tela de visualizar coletas do app Android, os filtros "Minhas Coletas" e "Pendentes" não estão funcionando corretamente.

### Análise do Código Atual

**ColetasViewModel.kt** (linha 32-48):
```kotlin
fun loadColetas() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        try {
            val patrimonios = repository.getAllPatrimoniosList()
            val coletados = patrimonios.filter { it.coletado == true }
            val pendentes = patrimonios.filter { it.coletado != true }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                patrimoniosColetados = coletados,
                patrimoniosPendentes = pendentes,
                totalColetados = coletados.size,
                totalPendentes = pendentes.size
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro ao carregar coletas: ${e.message}"
            )
        }
    }
}
```

### Problemas Encontrados

1. **Carrega TODOS os patrimônios**: `repository.getAllPatrimoniosList()` retorna todos os patrimônios do sistema, não apenas as coletas
2. **Não filtra por usuário**: Não há filtro para mostrar apenas as coletas do usuário logado
3. **Confusão entre patrimônios e coletas**: O código está filtrando patrimônios por `coletado == true`, mas deveria buscar as coletas registradas

---

## ✅ Solução Proposta

### Opção 1: Usar Endpoint de Coletas (Recomendado)

Modificar o ViewModel para buscar as coletas do endpoint correto:

```kotlin
fun loadColetas() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        try {
            // Buscar coletas do usuário logado
            val coletasResult = repository.getColetas()
            
            if (coletasResult.isSuccess) {
                val coletas = coletasResult.getOrNull() ?: emptyList()
                
                // Buscar patrimônios coletados
                val patrimoniosColetados = coletas.mapNotNull { coleta ->
                    // Converter Coleta para Patrimonio ou buscar patrimônio
                    Patrimonio(
                        id = coleta.patrimonioId.toLong(),
                        numeroPatrimonio = coleta.numeroPatrimonio,
                        descricao = coleta.descricaoPatrimonio,
                        coletado = true,
                        salaNome = coleta.nomeSala,
                        // ... outros campos
                    )
                }
                
                // Buscar patrimônios pendentes (não coletados)
                val todosPatrimonios = repository.getAllPatrimoniosList()
                val patrimoniosPendentes = todosPatrimonios.filter { it.coletado != true }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    patrimoniosColetados = patrimoniosColetados,
                    patrimoniosPendentes = patrimoniosPendentes,
                    totalColetados = patrimoniosColetados.size,
                    totalPendentes = patrimoniosPendentes.size
                )
            } else {
                throw Exception(coletasResult.exceptionOrNull()?.message ?: "Erro ao buscar coletas")
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro ao carregar coletas: ${e.message}"
            )
        }
    }
}
```

### Opção 2: Adicionar Filtro por Usuário

Adicionar parâmetro para filtrar por usuário:

```kotlin
fun loadColetas(filtrarPorUsuario: Boolean = true) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        try {
            val patrimonios = repository.getAllPatrimoniosList()
            
            // Obter usuário atual
            val usuarioAtual = repository.getCurrentUser()
            
            val coletados = if (filtrarPorUsuario && usuarioAtual != null) {
                // Filtrar apenas coletas do usuário logado
                patrimonios.filter { 
                    it.coletado == true && it.coletadoPor == usuarioAtual.nome 
                }
            } else {
                // Mostrar todas as coletas
                patrimonios.filter { it.coletado == true }
            }
            
            val pendentes = patrimonios.filter { it.coletado != true }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                patrimoniosColetados = coletados,
                patrimoniosPendentes = pendentes,
                totalColetados = coletados.size,
                totalPendentes = pendentes.size
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro ao carregar coletas: ${e.message}"
            )
        }
    }
}
```

### Opção 3: Adicionar Abas para Filtros

Modificar o layout para ter 3 abas:
1. **Minhas Coletas** - Coletas do usuário logado
2. **Todas as Coletas** - Todas as coletas do sistema
3. **Pendentes** - Patrimônios não coletados

```xml
<com.google.android.material.tabs.TabLayout>
    <com.google.android.material.tabs.TabItem
        android:text="Minhas Coletas" />
    
    <com.google.android.material.tabs.TabItem
        android:text="Todas" />
    
    <com.google.android.material.tabs.TabItem
        android:text="Pendentes" />
</com.google.android.material.tabs.TabLayout>
```

---

## 🔍 Verificação Necessária

### 1. Verificar Modelo de Dados

Verificar se o modelo `Patrimonio` tem o campo `coletadoPor`:

```kotlin
data class Patrimonio(
    val id: Long,
    val numeroPatrimonio: String,
    val descricao: String,
    val coletado: Boolean?,
    val coletadoPor: String?,  // ← Verificar se existe
    val dataColeta: String?,
    // ...
)
```

### 2. Verificar Endpoint de Coletas

Verificar se o endpoint `/api/mobile/coletas` retorna as coletas do usuário logado:

```kotlin
@GET("api/mobile/coletas/all")
suspend fun getColetas(): Response<ApiResponse<List<ColetaDto>>>
```

### 3. Verificar Backend

Verificar se o backend filtra as coletas por usuário:

```java
@GetMapping("/all")
public ResponseEntity<ApiResponse<List<MobileColetaResponse>>> buscarTodasColetasSemPaginacao() {
    String username = authentication.getName();
    List<MobileColetaResponse> coletas = mobileColetaService.buscarTodasColetas(username);
    // ...
}
```

---

## 📝 Implementação Recomendada

Vou implementar a **Opção 2** (Adicionar Filtro por Usuário) por ser a mais simples e não quebrar a UI existente.

### Passos:

1. ✅ Adicionar parâmetro `filtrarPorUsuario` no ViewModel
2. ✅ Verificar se o modelo `Patrimonio` tem campo `coletadoPor`
3. ✅ Adicionar botão de filtro na UI (opcional)
4. ✅ Testar com usuário logado

---

## 🎯 Resultado Esperado

Após a correção:
- ✅ Aba "Coletados" mostra apenas coletas do usuário logado
- ✅ Aba "Pendentes" mostra patrimônios não coletados
- ✅ Contadores corretos
- ✅ Filtro funcional

---

**Status**: 📋 Análise concluída - Aguardando implementação  
**Prioridade**: Alta  
**Complexidade**: Média
