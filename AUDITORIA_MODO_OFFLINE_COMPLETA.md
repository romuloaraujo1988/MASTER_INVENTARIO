# 🔍 Auditoria Completa - Modo Offline

**Data:** 22/11/2025  
**Objetivo:** Verificar se modo offline está 100% operacional

---

## 📋 **Resumo Executivo**

### **Status Geral: ⚠️ PARCIALMENTE FUNCIONAL**

**Problemas Identificados:**
1. ❌ **SalaSelectionViewModel NÃO busca do SQLite local**
2. ✅ **PatrimonioDao TEM métodos para busca local**
3. ✅ **SalaDao TEM métodos para busca local**
4. ⚠️ **InventarioRepository busca do SQLite, mas com fallback para API**

---

## 🔍 **Análise Detalhada**

### **1. DAOs (Room) - ✅ IMPLEMENTADOS CORRETAMENTE**

#### **✅ SalaDao.kt - COMPLETO**
```kotlin
interface SalaDao {
    // ✅ Busca todas as salas do SQLite local
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC")
    suspend fun buscarTodas(): List<SalaEntity>
    
    // ✅ Observa salas (Flow para updates)
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC")
    fun observarTodas(): Flow<List<SalaEntity>>
    
    // ✅ Busca por ID
    @Query("SELECT * FROM sala WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): SalaEntity?
    
    // ✅ Busca por nome
    @Query("SELECT * FROM sala WHERE ativa = 1 AND nome LIKE :query ORDER BY nome ASC")
    suspend fun buscarPorNome(query: String): List<SalaEntity>
    
    // ✅ Paginação
    @Query("SELECT * FROM sala WHERE ativa = 1 ORDER BY nome ASC LIMIT :limit OFFSET :offset")
    suspend fun buscarPaginado(limit: Int, offset: Int): List<SalaEntity>
}
```

**Conclusão:** ✅ **DAO está perfeito!**

---

#### **✅ PatrimonioDao.kt - COMPLETO**
```kotlin
interface PatrimonioDao {
    // ✅ Busca por número
    @Query("SELECT * FROM patrimonio WHERE numeroPatrimonio = :numero LIMIT 1")
    suspend fun buscarPorNumero(numero: String): PatrimonioEntity?
    
    // ✅ Busca por ID
    @Query("SELECT * FROM patrimonio WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): PatrimonioEntity?
    
    // ✅ Busca não coletados
    @Query("SELECT * FROM patrimonio WHERE coletado = 0")
    fun observarNaoColetados(): Flow<List<PatrimonioEntity>>
    
    // ✅ Busca descrições não coletadas
    @Query("SELECT DISTINCT descricao FROM patrimonio WHERE coletado = 0 ORDER BY descricao")
    suspend fun buscarDescricoesNaoColetadas(): List<String>
    
    // ✅ Busca por sala
    @Query("SELECT * FROM patrimonio WHERE idSala = :idSala AND coletado = 0")
    suspend fun buscarPorSalaNaoColetados(idSala: Int): List<PatrimonioEntity>
}
```

**Conclusão:** ✅ **DAO está perfeito!**

---

### **2. InventarioRepository - ✅ BUSCA DO SQLITE (com fallback)**

#### **Método: findPatrimonioByNumero()**

```kotlin
suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
    // ✅ PRIMEIRO: Busca do banco local (Room)
    val database = InventarioDatabase.getDatabase(context)
    val patrimonioDao = database.patrimonioDao()
    val patrimonioEntity = patrimonioDao.buscarPorNumero(numero)
    
    if (patrimonioEntity != null) {
        Log.d("InventarioRepository", "✓ Patrimônio encontrado no banco local")
        return Result.success(patrimonio)
    }
    
    // ⚠️ FALLBACK: Se não encontrou localmente, busca da API
    Log.d("InventarioRepository", "Patrimônio não encontrado localmente, buscando da API...")
    val response = apiService.getPatrimonioByNumero(numero)
    
    if (response.isSuccessful) {
        // Salvar no banco local para cache
        patrimonioDao.inserir(entity)
        return Result.success(patrimonio)
    }
    
    return Result.success(null)
}
```

**Análise:**
- ✅ **Busca PRIMEIRO do SQLite local**
- ✅ **Funciona offline se patrimônio estiver no banco**
- ⚠️ **Fallback para API se não encontrar localmente**
- ⚠️ **Se offline E não tiver no banco = Falha**

**Conclusão:** ✅ **Funciona offline SE dados estiverem sincronizados**

---

### **3. SalaSelectionViewModel - ❌ PROBLEMA CRÍTICO**

#### **Método: loadAllSalas()**

```kotlin
private suspend fun loadAllSalas() {
    Log.d(TAG, "loadAllSalas: Carregando TODAS as salas de uma vez")
    
    try {
        val apiService = ApiClient.getApiService(getApplication())
        
        // ❌ PROBLEMA: Busca DIRETO da API, não do SQLite!
        val response = withContext(Dispatchers.IO) {
            apiService.getSalasWithResponse()
        }
        
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Erro ao buscar salas: ${response.message()}")
        }
        
        // Converte e exibe
        val salas = salasDto.map { dto -> ... }
        _uiState.value = _uiState.value.copy(salas = salas, isLoading = false)
        
    } catch (e: Exception) {
        Log.e(TAG, "loadAllSalas: Erro ao carregar salas", e)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "Erro ao carregar salas: ${e.message}"
        )
    }
}
```

**Problemas:**
1. ❌ **Busca DIRETO da API** (não do SQLite)
2. ❌ **Não funciona offline**
3. ❌ **Não usa SalaDao.buscarTodas()**
4. ❌ **Não tem fallback para banco local**

**Impacto:**
- ❌ **Usuário NÃO consegue selecionar sala offline**
- ❌ **Não consegue iniciar coleta offline**
- ❌ **App quebra sem internet**

**Conclusão:** ❌ **CRÍTICO - Precisa correção urgente!**

---

### **4. ScannerViewModel - ✅ FUNCIONA OFFLINE**

```kotlin
fun searchPatrimonio(patrimonioId: Long, codigo: String) {
    viewModelScope.launch {
        // ✅ Usa InventarioRepository que busca do SQLite primeiro
        val result = inventarioRepository.findPatrimonioByNumero(codigo)
        
        result.fold(
            onSuccess = { patrimonio ->
                if (patrimonio != null) {
                    // ✅ Patrimônio encontrado (local ou API)
                    _uiState.value = _uiState.value.copy(
                        scanResult = ScanResult(patrimonio = patrimonio)
                    )
                }
            },
            onFailure = { exception ->
                // ❌ Erro (offline e não tem no banco)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao buscar patrimônio: ${exception.message}"
                )
            }
        )
    }
}
```

**Conclusão:** ✅ **Funciona offline SE patrimônio estiver no SQLite**

---

## 🔄 **Fluxo Atual (Com Problemas)**

### **Cenário 1: Usuário Offline Tenta Coletar**

```
1. Abre app
2. Clica "Nova Coleta"
3. Vai para SalaSelectionActivity
4. SalaSelectionViewModel.loadSalas()
   ├─ ❌ Tenta buscar da API
   ├─ ❌ Sem internet = ERRO
   └─ ❌ Não mostra salas do SQLite
5. ❌ USUÁRIO TRAVADO - Não consegue prosseguir
```

**Resultado:** ❌ **MODO OFFLINE NÃO FUNCIONA**

---

### **Cenário 2: Usuário Offline com Dados Sincronizados**

```
1. Abre app (já sincronizou antes)
2. Clica "Nova Coleta"
3. Vai para SalaSelectionActivity
4. SalaSelectionViewModel.loadSalas()
   ├─ ❌ Tenta buscar da API
   ├─ ❌ Sem internet = ERRO
   └─ ❌ Não mostra salas do SQLite (mesmo tendo!)
5. ❌ USUÁRIO TRAVADO
```

**Resultado:** ❌ **MESMO COM DADOS NO SQLITE, NÃO FUNCIONA**

---

## 🔧 **CORREÇÃO NECESSÁRIA**

### **Corrigir SalaSelectionViewModel**

```kotlin
// ANTES (ERRADO)
private suspend fun loadAllSalas() {
    val apiService = ApiClient.getApiService(getApplication())
    val response = apiService.getSalasWithResponse() // ❌ Busca da API
    // ...
}

// DEPOIS (CORRETO)
private suspend fun loadAllSalas() {
    try {
        // ✅ PRIMEIRO: Buscar do SQLite local
        val database = InventarioDatabase.getDatabase(getApplication())
        val salaDao = database.salaDao()
        val salasEntity = salaDao.buscarTodas()
        
        if (salasEntity.isNotEmpty()) {
            Log.d(TAG, "✓ ${salasEntity.size} salas encontradas no banco local")
            
            // Converter Entity para Domain Model
            val salas = salasEntity.map { entity ->
                Sala(
                    id = entity.id.toLong(),
                    nome = entity.nome,
                    codigo = entity.codigo,
                    descricao = entity.descricao,
                    setorId = entity.setorId?.toLong(),
                    ativo = entity.ativa,
                    sincronizado = true
                )
            }
            
            _uiState.value = _uiState.value.copy(
                salas = salas,
                isLoading = false,
                errorMessage = null
            )
            
            // ✅ Tentar atualizar do servidor em background (não bloqueia)
            tryUpdateFromServer()
            return
        }
        
        // ⚠️ FALLBACK: Se não tem no SQLite, buscar da API
        Log.d(TAG, "Nenhuma sala no banco local, buscando da API...")
        val apiService = ApiClient.getApiService(getApplication())
        val response = apiService.getSalasWithResponse()
        
        if (response.isSuccessful && response.body() != null) {
            val salasDto = response.body()!!.data
            
            // Salvar no SQLite para próxima vez
            val entities = salasDto.map { dto -> /* converter */ }
            salaDao.inserirTodas(entities)
            
            // Converter e exibir
            val salas = salasDto.map { dto -> /* converter */ }
            _uiState.value = _uiState.value.copy(salas = salas, isLoading = false)
        }
        
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao carregar salas", e)
        
        // ✅ Tentar buscar do SQLite mesmo com erro
        tryLoadFromLocalDatabase()
    }
}

private suspend fun tryLoadFromLocalDatabase() {
    try {
        val database = InventarioDatabase.getDatabase(getApplication())
        val salaDao = database.salaDao()
        val salasEntity = salaDao.buscarTodas()
        
        if (salasEntity.isNotEmpty()) {
            val salas = salasEntity.map { /* converter */ }
            _uiState.value = _uiState.value.copy(
                salas = salas,
                isLoading = false,
                errorMessage = "Modo offline - Mostrando dados locais"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sem conexão e sem dados locais. Conecte-se para sincronizar."
            )
        }
    } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "Erro ao carregar dados: ${e.message}"
        )
    }
}
```


---

## ✅ **CHECKLIST DE VERIFICAÇÃO**

### **Componentes Offline**

| Componente | Status | Observação |
|------------|--------|------------|
| **SalaDao** | ✅ OK | Métodos implementados |
| **PatrimonioDao** | ✅ OK | Métodos implementados |
| **ColetaDao** | ✅ OK | Métodos implementados |
| **InventarioRepository** | ⚠️ PARCIAL | Busca SQLite com fallback API |
| **SalaSelectionViewModel** | ❌ CRÍTICO | Busca APENAS da API |
| **ScannerViewModel** | ✅ OK | Usa repository (offline-first) |
| **ColetaRepositoryImpl** | ✅ OK | Salva no SQLite sempre |

---

### **Fluxos de Coleta**

| Fluxo | Status | Observação |
|-------|--------|------------|
| **Selecionar Sala** | ❌ NÃO FUNCIONA | ViewModel não busca do SQLite |
| **Escanear QR Code** | ✅ FUNCIONA | Se patrimônio estiver no SQLite |
| **Buscar Patrimônio** | ✅ FUNCIONA | Se patrimônio estiver no SQLite |
| **Salvar Coleta** | ✅ FUNCIONA | Sempre salva no SQLite |
| **Sincronizar** | ✅ FUNCIONA | Batch sync implementado |

---

## 🎯 **CONCLUSÃO**

### **❌ MODO OFFLINE NÃO ESTÁ 100% OPERACIONAL**

**Problema Principal:**
- ❌ **SalaSelectionViewModel não busca salas do SQLite local**
- ❌ **Usuário não consegue selecionar sala offline**
- ❌ **Não consegue iniciar coleta offline**

**O que funciona:**
- ✅ DAOs estão implementados corretamente
- ✅ Scanner busca patrimônio do SQLite
- ✅ Coleta é salva no SQLite
- ✅ Sincronização funciona

**O que NÃO funciona:**
- ❌ Seleção de sala offline
- ❌ Listagem de salas offline
- ❌ Fluxo completo de coleta offline

---

## 🚨 **AÇÃO NECESSÁRIA**

### **URGENTE: Corrigir SalaSelectionViewModel**

**Prioridade:** 🔴 **CRÍTICA**  
**Tempo Estimado:** 2-3 horas  
**Impacto:** **BLOQUEIA modo offline completamente**

**Passos:**
1. Modificar `loadAllSalas()` para buscar do SQLite primeiro
2. Adicionar fallback para API se SQLite vazio
3. Adicionar método `tryLoadFromLocalDatabase()`
4. Testar fluxo completo offline
5. Verificar que salas aparecem sem internet

---

## 📝 **CÓDIGO PARA CORREÇÃO**

### **Arquivo:** `SalaSelectionViewModel.kt`

```kotlin
package com.inventario.mobile.presentation.sala

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventario.mobile.data.local.database.InventarioDatabase
import com.inventario.mobile.domain.model.Sala
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalaSelectionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SalaSelectionUiState())
    val uiState: StateFlow<SalaSelectionUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SalaSelectionViewModel"
    }

    fun loadSalas(forceRefresh: Boolean = false) {
        Log.d(TAG, "loadSalas: Carregando salas (forceRefresh=$forceRefresh)")
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    errorMessage = null
                )
                
                loadAllSalasOfflineFirst()
                
            } catch (e: Exception) {
                Log.e(TAG, "loadSalas: Erro ao carregar salas", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar salas: ${e.message}"
                )
            }
        }
    }
    
    /**
     * ✅ CORREÇÃO: Busca do SQLite PRIMEIRO (offline-first)
     */
    private suspend fun loadAllSalasOfflineFirst() {
        Log.d(TAG, "loadAllSalasOfflineFirst: Buscando do SQLite primeiro")
        
        try {
            // ✅ PASSO 1: Buscar do banco local (Room)
            val database = InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            val salasEntity = salaDao.buscarTodas()
            
            if (salasEntity.isNotEmpty()) {
                Log.d(TAG, "✓ ${salasEntity.size} salas encontradas no banco local")
                
                // Converter Entity para Domain Model
                val salas = salasEntity.map { entity ->
                    Sala(
                        id = entity.id.toLong(),
                        nome = entity.nome,
                        codigo = entity.codigo ?: "",
                        descricao = entity.descricao ?: "",
                        setorId = entity.setorId?.toLong(),
                        setorNome = entity.setorNome,
                        ativo = entity.ativa,
                        sincronizado = true
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false,
                    errorMessage = null
                )
                
                // ✅ Tentar atualizar do servidor em background (não bloqueia UI)
                tryUpdateFromServerInBackground()
                return
            }
            
            // ⚠️ PASSO 2: Se SQLite vazio, buscar da API
            Log.d(TAG, "Nenhuma sala no banco local, buscando da API...")
            loadFromApi()
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar do SQLite", e)
            
            // ✅ Tentar API como fallback
            loadFromApi()
        }
    }
    
    /**
     * Busca salas da API e salva no SQLite
     */
    private suspend fun loadFromApi() {
        try {
            val apiService = com.inventario.mobile.data.remote.api.ApiClient.getApiService(getApplication())
            
            val response = withContext(Dispatchers.IO) {
                apiService.getSalasWithResponse()
            }
            
            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Erro ao buscar salas: ${response.message()}")
            }
            
            val apiResponse = response.body()!!
            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Erro desconhecido")
            }
            
            val salasDto = apiResponse.data
            Log.d(TAG, "✓ ${salasDto.size} salas recebidas do servidor")
            
            // Salvar no SQLite para próxima vez
            val database = InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            
            val entities = salasDto.map { dto ->
                com.inventario.mobile.data.local.entity.SalaEntity(
                    id = dto.id,
                    nome = dto.nome,
                    codigo = dto.codigo,
                    descricao = dto.descricao,
                    setorId = dto.setorIdFinal,
                    setorNome = dto.setorNome,
                    ativa = dto.ativa ?: dto.ativo,
                    sincronizado = true
                )
            }
            
            salaDao.inserirTodas(entities)
            Log.d(TAG, "✓ ${entities.size} salas salvas no SQLite")
            
            // Converter para Domain Model
            val salas = salasDto.map { dto ->
                Sala(
                    id = dto.id.toLong(),
                    nome = dto.nome,
                    codigo = dto.codigo ?: "",
                    descricao = dto.descricao ?: "",
                    setorId = dto.setorIdFinal.toLong(),
                    setorNome = dto.setorNome,
                    ativo = dto.ativa ?: dto.ativo,
                    sincronizado = true
                )
            }
            
            _uiState.value = _uiState.value.copy(
                salas = salas,
                isLoading = false,
                errorMessage = null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar da API", e)
            
            // ✅ Última tentativa: buscar do SQLite mesmo com erro
            tryLoadFromLocalDatabaseAsFallback()
        }
    }
    
    /**
     * Fallback final: buscar do SQLite mesmo com erro de rede
     */
    private suspend fun tryLoadFromLocalDatabaseAsFallback() {
        try {
            val database = InventarioDatabase.getDatabase(getApplication())
            val salaDao = database.salaDao()
            val salasEntity = salaDao.buscarTodas()
            
            if (salasEntity.isNotEmpty()) {
                Log.d(TAG, "✓ Usando ${salasEntity.size} salas do cache local (modo offline)")
                
                val salas = salasEntity.map { entity ->
                    Sala(
                        id = entity.id.toLong(),
                        nome = entity.nome,
                        codigo = entity.codigo ?: "",
                        descricao = entity.descricao ?: "",
                        setorId = entity.setorId?.toLong(),
                        setorNome = entity.setorNome,
                        ativo = entity.ativa,
                        sincronizado = true
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    salas = salas,
                    isLoading = false,
                    errorMessage = "📡 Modo offline - Mostrando dados locais"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sem conexão e sem dados locais. Conecte-se à internet para sincronizar."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar do SQLite como fallback", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Erro ao carregar dados: ${e.message}"
            )
        }
    }
    
    /**
     * Atualiza do servidor em background (não bloqueia UI)
     */
    private fun tryUpdateFromServerInBackground() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Atualizando salas do servidor em background...")
                loadFromApi()
            } catch (e: Exception) {
                Log.d(TAG, "Não foi possível atualizar do servidor (modo offline)")
            }
        }
    }
}

data class SalaSelectionUiState(
    val salas: List<Sala> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

---

## 🧪 **TESTE APÓS CORREÇÃO**

### **Teste 1: Modo Offline Puro**
```
1. Sincronizar dados (online)
2. Desconectar internet
3. Fechar e abrir app
4. Clicar "Nova Coleta"
5. ✅ DEVE mostrar lista de salas
6. Selecionar sala
7. Escanear QR Code
8. ✅ DEVE encontrar patrimônio
9. Salvar coleta
10. ✅ DEVE salvar localmente
```

### **Teste 2: Primeiro Acesso Offline**
```
1. Instalar app
2. Desconectar internet
3. Abrir app
4. Clicar "Nova Coleta"
5. ✅ DEVE mostrar mensagem: "Sem dados locais. Conecte-se para sincronizar"
```

### **Teste 3: Sincronização e Offline**
```
1. Conectar internet
2. Sincronizar dados
3. ✅ Verificar que salas foram salvas no SQLite
4. Desconectar internet
5. Clicar "Nova Coleta"
6. ✅ DEVE mostrar salas do SQLite
7. ✅ DEVE mostrar banner "Modo offline"
```

---

## 📊 **IMPACTO DA CORREÇÃO**

### **Antes (Atual)**
```
Modo Offline: ❌ NÃO FUNCIONA
- Não consegue selecionar sala
- Não consegue iniciar coleta
- App quebra sem internet
```

### **Depois (Corrigido)**
```
Modo Offline: ✅ FUNCIONA 100%
- Seleciona sala do SQLite
- Busca patrimônio do SQLite
- Salva coleta no SQLite
- Sincroniza quando conectar
```

---

## 🎉 **RESUMO FINAL**

### **Situação Atual**
- ❌ **Modo offline NÃO funciona completamente**
- ❌ **SalaSelectionViewModel busca apenas da API**
- ✅ **DAOs estão corretos**
- ✅ **Scanner funciona offline**
- ✅ **Coleta salva no SQLite**

### **Ação Necessária**
- 🔴 **URGENTE:** Corrigir SalaSelectionViewModel
- ⏱️ **Tempo:** 2-3 horas
- 📝 **Código:** Fornecido acima

### **Após Correção**
- ✅ **Modo offline 100% funcional**
- ✅ **Usuário consegue coletar offline**
- ✅ **Dados sincronizam ao conectar**

---

**Criado em:** 22/11/2025  
**Status:** ⚠️ **CORREÇÃO NECESSÁRIA**  
**Prioridade:** 🔴 **CRÍTICA**
