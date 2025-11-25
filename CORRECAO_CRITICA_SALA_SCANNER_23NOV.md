# 🚨 Correção Crítica - Coletas Sem Sala no Scanner

## ❌ Problema Grave Identificado

**Sintoma:** Coletas realizadas via scanner (coleta rápida com câmera) estavam sendo salvas **SEM sala**, causando perda de dados críticos.

**Impacto:** 
- ⚠️ Coletas sem rastreabilidade de localização
- ⚠️ Impossível saber onde o patrimônio foi encontrado
- ⚠️ Relatórios de divergência comprometidos
- ⚠️ Dados inconsistentes no banco

---

## 🔍 Causa Raiz

### Problema 1: ScannerViewModel não passava salaId

**Arquivo:** `ScannerViewModel.kt` linha ~320

**ANTES (ERRADO):**
```kotlin
fun coletarPatrimonioComEstado(patrimonioId: Long, salaNome: String, estadoEncontrado: String) {
    // ...
    val result = registrarColetaUseCase.invoke(
        numeroPatrimonio = patrimonio.numeroPatrimonio,
        localizacaoAtual = salaNome, // ❌ Apenas o NOME, sem ID
        estadoEncontrado = estadoEncontrado,
        observacoes = null
    )
}
```

**DEPOIS (CORRETO):**
```kotlin
fun coletarPatrimonioComEstado(patrimonioId: Long, salaNome: String, estadoEncontrado: String) {
    // ✅ CRÍTICO: Obter ID da sala atual do PreferencesManager
    val salaIdAtual = preferencesManager.getCurrentSalaId()
    
    if (salaIdAtual <= 0) {
        // ✅ Validação: Não permite coletar sem sala
        _uiState.value = _uiState.value.copy(
            errorMessage = "Sala não selecionada. Selecione uma sala antes de coletar."
        )
        return@launch
    }
    
    val result = registrarColetaUseCase.invoke(
        numeroPatrimonio = patrimonio.numeroPatrimonio,
        salaId = salaIdAtual, // ✅ CRÍTICO: Passar ID da sala atual
        localizacaoAtual = salaNome,
        estadoEncontrado = estadoEncontrado,
        observacoes = null
    )
}
```

### Problema 2: Use Case não aceitava salaId

**Arquivo:** `RegistrarColetaUseCase.kt`

**ANTES (ERRADO):**
```kotlin
suspend operator fun invoke(
    numeroPatrimonio: String,
    localizacaoAtual: String?, // ❌ Apenas nome da sala
    estadoEncontrado: String? = null,
    // ...
): Result<Coleta> {
    val coleta = Coleta(
        // ...
        localizacaoAtual = localizacaoAtual, // ❌ Sem salaId
        // ...
    )
}
```

**DEPOIS (CORRETO):**
```kotlin
suspend operator fun invoke(
    numeroPatrimonio: String,
    salaId: Int? = null, // ✅ CRÍTICO: ID da sala onde está coletando
    localizacaoAtual: String?,
    estadoEncontrado: String? = null,
    // ...
): Result<Coleta> {
    val coleta = Coleta(
        // ...
        salaId = salaId, // ✅ CRÍTICO: ID da sala onde está coletando
        localizacaoAtual = localizacaoAtual,
        // ...
    )
}
```

### Problema 3: Modelo Coleta não tinha campo salaId

**Arquivo:** `domain/model/Coleta.kt`

**ANTES (ERRADO):**
```kotlin
data class Coleta(
    val patrimonioId: Long,
    val numeroPatrimonio: String? = null,
    val usuarioId: Long,
    // ❌ Sem salaId
    val localizacaoAtual: String? = null,
    // ...
)
```

**DEPOIS (CORRETO):**
```kotlin
data class Coleta(
    val patrimonioId: Long,
    val numeroPatrimonio: String? = null,
    val usuarioId: Long,
    val salaId: Int? = null, // ✅ CRÍTICO: ID da sala onde está coletando
    val localizacaoAtual: String? = null,
    // ...
)
```

### Problema 4: Mapper usava sala do patrimônio, não da coleta

**Arquivo:** `ColetaMapper.kt` linha ~89

**ANTES (ERRADO):**
```kotlin
return ColetaEntity(
    // ...
    idSala = patrimonio?.idSala, // ❌ Sala CADASTRADA do patrimônio
    nomeSala = salaReal,
    // ...
)
```

**Problema:** Se o patrimônio foi movido de sala, salvava a sala antiga (cadastrada), não a sala atual onde foi encontrado!

**DEPOIS (CORRETO):**
```kotlin
// ✅ CRÍTICO: Priorizar salaId da coleta (onde está coletando AGORA)
val salaIdReal = domain.salaId ?: patrimonio?.idSala

Log.d(TAG, "═══════════════════════════════════════")
Log.d(TAG, "MAPEANDO COLETA PARA ENTITY")
Log.d(TAG, "Sala ID da coleta (atual): ${domain.salaId}")
Log.d(TAG, "Sala ID do patrimônio (cadastrado): ${patrimonio?.idSala}")
Log.d(TAG, "Sala ID FINAL (usado): $salaIdReal")
Log.d(TAG, "═══════════════════════════════════════")

return ColetaEntity(
    // ...
    idSala = salaIdReal, // ✅ CRÍTICO: Prioriza sala atual da coleta
    nomeSala = salaReal,
    // ...
)
```

---

## ✅ Correções Aplicadas

### 1. ScannerViewModel.kt
- ✅ Adicionado `preferencesManager.getCurrentSalaId()`
- ✅ Validação: Não permite coletar sem sala
- ✅ Passa `salaId` para o Use Case
- ✅ Logs detalhados para debug

### 2. RegistrarColetaUseCase.kt
- ✅ Adicionado parâmetro `salaId: Int?`
- ✅ Passa `salaId` para o modelo Coleta
- ✅ Log mostra sala ID na criação da coleta

### 3. domain/model/Coleta.kt
- ✅ Adicionado campo `val salaId: Int? = null`

### 4. ColetaMapper.kt
- ✅ Prioriza `domain.salaId` sobre `patrimonio?.idSala`
- ✅ Logs detalhados mostrando qual sala foi usada
- ✅ Atualizado `toDomain()` para incluir `salaId`

---

## 🎯 Fluxo Correto Agora

### Coleta Rápida com Scanner

```
1. Usuário seleciona sala (SalaSelectionActivity)
   └─> PreferencesManager.setCurrentSalaId(salaId)
   └─> PreferencesManager.setCurrentSalaNome(salaNome)

2. Usuário abre Scanner (ScannerActivity)
   └─> Lê salaId e salaNome do Intent
   └─> Salva no PreferencesManager (backup)

3. Usuário escaneia QR Code
   └─> ScannerViewModel.searchPatrimonio()
   └─> Mostra dados do patrimônio

4. Usuário clica "Coletar"
   └─> ScannerActivity.buttonColetar.onClick()
   └─> Lê salaId do PreferencesManager ✅
   └─> Mostra dialog de estado

5. Usuário seleciona estado
   └─> ScannerViewModel.coletarPatrimonioComEstado()
   └─> Valida salaId > 0 ✅
   └─> Chama RegistrarColetaUseCase com salaId ✅

6. Use Case cria Coleta
   └─> Coleta.salaId = salaIdAtual ✅
   └─> Coleta.localizacaoAtual = salaNome

7. Mapper converte para Entity
   └─> ColetaEntity.idSala = domain.salaId ✅ (PRIORIDADE)
   └─> ColetaEntity.nomeSala = salaNome

8. Salva no banco Room
   └─> ✅ Coleta COM sala!
```

---

## 🧪 Como Testar

### Teste 1: Coleta Normal
```
1. Selecionar sala "Sala 101" (ID: 10)
2. Abrir Scanner
3. Escanear patrimônio
4. Coletar
5. Verificar logs:
   - "Sala ID atual (PreferencesManager): 10"
   - "Sala ID da coleta (atual): 10"
   - "Sala ID FINAL (usado): 10"
6. Verificar banco: idSala = 10 ✅
```

### Teste 2: Patrimônio Movido de Sala
```
1. Patrimônio cadastrado na "Sala 101" (ID: 10)
2. Usuário seleciona "Sala 102" (ID: 11)
3. Escaneia patrimônio
4. Coletar
5. Verificar logs:
   - "Sala ID da coleta (atual): 11" ✅
   - "Sala ID do patrimônio (cadastrado): 10"
   - "Sala ID FINAL (usado): 11" ✅
6. Verificar banco: idSala = 11 ✅ (sala ATUAL, não cadastrada)
```

### Teste 3: Sem Sala Selecionada
```
1. Abrir Scanner sem selecionar sala
2. Escanear patrimônio
3. Clicar "Coletar"
4. Verificar erro: "Sala não selecionada" ✅
5. Coleta NÃO é registrada ✅
```

---

## 📊 Logs de Debug

### Logs Adicionados

```kotlin
// ScannerViewModel
Log.d("ScannerViewModel", "Sala ID atual (PreferencesManager): $salaIdAtual")
Log.d("ScannerViewModel", "✓ Usando RegistrarColetaUseCase")
Log.d("ScannerViewModel", "  Sala ID: $salaIdAtual")
Log.d("ScannerViewModel", "  Sala Nome: $salaNome")

// RegistrarColetaUseCase
Log.d("RegistrarColetaUseCase", "✓ Coleta criada: Patrimônio ${coleta.numeroPatrimonio}, Usuário ${coleta.usuarioId}, Sala ${coleta.salaId}")

// ColetaMapper
Log.d("ColetaMapper", "═══════════════════════════════════════")
Log.d("ColetaMapper", "MAPEANDO COLETA PARA ENTITY")
Log.d("ColetaMapper", "Sala ID da coleta (atual): ${domain.salaId}")
Log.d("ColetaMapper", "Sala ID do patrimônio (cadastrado): ${patrimonio?.idSala}")
Log.d("ColetaMapper", "Sala ID FINAL (usado): $salaIdReal")
Log.d("ColetaMapper", "═══════════════════════════════════════")
```

### Como Ver Logs

```bash
# Filtrar logs de sala
adb logcat -s ScannerViewModel:* RegistrarColetaUseCase:* ColetaMapper:* | grep -i "sala"

# Logs completos
adb logcat -s ScannerViewModel:* RegistrarColetaUseCase:* ColetaMapper:*
```

---

## 🚀 Compilação e Instalação

### Compilar
```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
```

**Resultado:** ✅ BUILD SUCCESSFUL in 58s

### Instalar
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**Status:** ⏳ Aguardando emulador estar rodando

---

## 📝 Arquivos Modificados

1. ✅ `ScannerViewModel.kt` - Adicionado salaId e validação
2. ✅ `RegistrarColetaUseCase.kt` - Adicionado parâmetro salaId
3. ✅ `domain/model/Coleta.kt` - Adicionado campo salaId
4. ✅ `ColetaMapper.kt` - Prioriza salaId da coleta

**Total:** 4 arquivos modificados  
**Linhas alteradas:** ~50 linhas

---

## ⚠️ Impacto

### Coletas Antigas (Antes da Correção)
- ❌ Podem ter `idSala = null` ou sala errada
- ⚠️ Recomendado: Revisar coletas recentes

### Coletas Novas (Após Correção)
- ✅ Sempre terão `idSala` correto
- ✅ Validação impede coleta sem sala
- ✅ Logs detalhados para auditoria

---

## 🎉 Benefícios

1. ✅ **Rastreabilidade Completa:** Toda coleta tem sala
2. ✅ **Validação Preventiva:** Não permite coletar sem sala
3. ✅ **Dados Corretos:** Usa sala ATUAL, não cadastrada
4. ✅ **Auditoria:** Logs detalhados de qual sala foi usada
5. ✅ **Relatórios Confiáveis:** Divergências detectadas corretamente

---

## 📞 Próximos Passos

1. ⏳ **Iniciar emulador**
2. ⏳ **Instalar APK:** `adb install -r app-debug.apk`
3. ⏳ **Testar coleta com scanner**
4. ⏳ **Verificar logs:** Sala ID deve aparecer
5. ⏳ **Verificar banco:** `idSala` deve estar preenchido

---

**Correção implementada em:** 23/11/2025  
**Versão:** 2.1.1  
**Status:** ✅ COMPILADO - Aguardando instalação e teste  
**Prioridade:** 🚨 CRÍTICA

**TESTE ASSIM QUE POSSÍVEL!** 🚀
