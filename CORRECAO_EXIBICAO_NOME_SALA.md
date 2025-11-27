# 🐛 Correção: Exibição do Nome da Sala

## 📋 Problema Identificado

**Sintoma:** Ao pesquisar um patrimônio para coleta, aparece o código/ID da sala ao invés do nome da sala.

**Exemplo:**
- ❌ **Errado:** "Sala: 10"
- ✅ **Correto:** "Sala: Laboratório de Informática"

---

## 🔍 Localização do Problema

### ScannerActivity.kt - Linha ~565

```kotlin
private fun displayPatrimonioInfo(result: ScanResult) {
    binding.cardPatrimonioInfo.visibility = View.VISIBLE
    binding.textPatrimonioNumero.text = "Número: ${result.patrimonioCodigo}"
    binding.textPatrimonioDescricao.text = "Descrição: ${result.patrimonio?.descricao ?: "N/A"}"
    
    // ❌ PROBLEMA: Exibe salaId (número) ao invés de salaNome (texto)
    binding.textPatrimonioSala.text = "Sala: ${result.patrimonio?.salaId?.toString() ?: "N/A"}"
    
    // ... resto do código
}
```

---

## ✅ Solução

### Correção no ScannerActivity.kt

```kotlin
private fun displayPatrimonioInfo(result: ScanResult) {
    binding.cardPatrimonioInfo.visibility = View.VISIBLE
    binding.textPatrimonioNumero.text = "Número: ${result.patrimonioCodigo}"
    binding.textPatrimonioDescricao.text = "Descrição: ${result.patrimonio?.descricao ?: "N/A"}"
    
    // ✅ CORREÇÃO: Usar salaNome ao invés de salaId
    val salaInfo = result.patrimonio?.salaNome ?: "Sala não informada"
    binding.textPatrimonioSala.text = "Sala: $salaInfo"
    
    val statusText = if (result.jaColetado) {
        buildString {
            append("Status: COLETADO")
            if (!result.coletadoPor.isNullOrBlank()) {
                append("\nColetado por: ${result.coletadoPor}")
            }
            if (!result.dataColetaFormatada.isNullOrBlank()) {
                append("\nEm: ${result.dataColetaFormatada}")
            }
        }
    } else {
        "Status: Disponível para coleta"
    }
    binding.textPatrimonioStatus.text = statusText
    binding.textPatrimonioStatus.setTextColor(
        if (result.jaColetado) 
            getColor(android.R.color.holo_orange_dark)
        else 
            getColor(android.R.color.holo_green_dark)
    )
}
```

---

## 🔍 Verificar se `salaNome` está sendo populado

### Problema Potencial: Campo `salaNome` pode estar NULL

Se após a correção ainda aparecer "Sala não informada", o problema está no **Repository** que não está buscando o nome da sala.

### Verificar no InventarioRepository

```kotlin
// InventarioRepository.kt
suspend fun findPatrimonioByNumero(numero: String): Result<Patrimonio?> {
    return try {
        val patrimonioEntity = patrimonioDao.buscarPorNumero(numero)
        
        if (patrimonioEntity != null) {
            // ✅ VERIFICAR: salaNome deve ser populado aqui
            val patrimonio = Patrimonio(
                id = patrimonioEntity.id,
                numeroPatrimonio = patrimonioEntity.numeroPatrimonio,
                descricao = patrimonioEntity.descricao,
                salaId = patrimonioEntity.salaId,
                salaNome = patrimonioEntity.salaNome,  // ✅ Deve estar preenchido
                // ... outros campos
            )
            
            Result.success(patrimonio)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🎯 Solução Completa: Buscar Nome da Sala com JOIN

Se `salaNome` não está sendo populado, use a **Opção 2 (JOIN)** que já resolve ambos os problemas:

### PatrimonioDao.kt - Query Otimizada

```kotlin
@Query("""
    SELECT p.*, 
           s.nome as salaNome,
           CASE WHEN c.id IS NOT NULL THEN 1 ELSE 0 END as coletado,
           u.nome_completo as coletadoPor,
           datetime(c.data_coleta / 1000, 'unixepoch', 'localtime') as dataColetaFormatada
    FROM patrimonio p
    LEFT JOIN sala s ON s.id = p.sala_id
    LEFT JOIN coleta c ON c.patrimonio_id = p.id 
                       AND c.inventario_id = :inventarioId
    LEFT JOIN usuario u ON u.id = c.usuario_id
    WHERE p.numero_patrimonio = :numero
    LIMIT 1
""")
suspend fun buscarPorNumeroComStatusColeta(
    numero: String,
    inventarioId: Int
): PatrimonioComColetaEntity?
```

**Benefícios desta query:**
1. ✅ Busca o **nome da sala** (não apenas o ID)
2. ✅ Verifica se foi **coletado**
3. ✅ Busca **quem coletou**
4. ✅ Busca **quando foi coletado**
5. ✅ Tudo em **1 única query** (mais eficiente)

---

## 🧪 Testes

### Teste 1: Patrimônio com Sala Cadastrada
```
1. Buscar patrimônio que tem sala associada
2. Verificar exibição: "Sala: Laboratório de Informática"
3. ✅ Deve mostrar o NOME da sala, não o ID
```

### Teste 2: Patrimônio sem Sala
```
1. Buscar patrimônio sem sala associada
2. Verificar exibição: "Sala: Sala não informada"
3. ✅ Deve mostrar mensagem amigável
```

### Teste 3: Coleta Manual
```
1. Buscar patrimônio manualmente
2. Verificar se nome da sala aparece corretamente
3. ✅ Mesma correção deve ser aplicada
```

---

## 📝 Arquivos a Modificar

### 1. ScannerActivity.kt
```kotlin
// Linha ~565
- binding.textPatrimonioSala.text = "Sala: ${result.patrimonio?.salaId?.toString() ?: "N/A"}"
+ val salaInfo = result.patrimonio?.salaNome ?: "Sala não informada"
+ binding.textPatrimonioSala.text = "Sala: $salaInfo"
```

### 2. ManualCollectionActivity.kt (se aplicável)

Verificar se o mesmo problema existe na coleta manual:

```kotlin
// Procurar por salaId.toString() e substituir por salaNome
```

### 3. PatrimonioDao.kt (se salaNome estiver NULL)

Adicionar JOIN com tabela `sala`:

```kotlin
@Query("""
    SELECT p.*, s.nome as salaNome
    FROM patrimonio p
    LEFT JOIN sala s ON s.id = p.sala_id
    WHERE p.numero_patrimonio = :numero
    LIMIT 1
""")
suspend fun buscarPorNumeroComSala(numero: String): PatrimonioEntity?
```

---

## 🎯 Prioridade

**Prioridade:** 🟡 MÉDIA  
**Impacto:** Médio (UX ruim, mas não impede funcionalidade)  
**Esforço:** Baixo (5-10 minutos)  
**Recomendação:** Corrigir junto com a validação de coleta duplicada

---

## 📊 Checklist de Implementação

- [ ] Modificar `ScannerActivity.kt` linha ~565
- [ ] Verificar se `ManualCollectionActivity.kt` tem o mesmo problema
- [ ] Testar com patrimônio que tem sala
- [ ] Testar com patrimônio sem sala
- [ ] Se `salaNome` estiver NULL, adicionar JOIN no DAO
- [ ] Compilar e gerar APK
- [ ] Testar em dispositivo real

---

**Data:** 26/11/2025  
**Status:** 🔍 Identificado  
**Solução:** Simples (trocar `salaId` por `salaNome`)
