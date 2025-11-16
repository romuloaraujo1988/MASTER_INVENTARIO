# Inconsistências - MobileConsultaService.java

## 📋 Análise Completa

**Arquivo:** `src/main/java/com/inventario/mobile/server/service/MobileConsultaService.java`  
**Data:** 15/11/2025  
**Total de Erros:** 29 erros de compilação

---

## ❌ Problemas Identificados

### 1. **Métodos Inexistentes no Modelo Patrimonio**

#### Problema: `getEstado()` não existe
**Linhas:** 100, 178

**Código Atual:**
```java
detalhes.setEstado(patrimonio.getEstado());  // ERRO!
dto.setEstado(patrimonio.getEstado());       // ERRO!
```

**Método Correto:**
```java
patrimonio.getEstadoConservacao()  // ✅ EXISTE
```

---

#### Problema: `getSala()` não existe
**Linhas:** 105, 106, 107, 108, 109, 183, 184, 185

**Código Atual:**
```java
if (patrimonio.getSala() != null) {  // ERRO!
    detalhes.setSalaId(patrimonio.getSala().getId());
    detalhes.setSalaNome(patrimonio.getSala().getNome());
    // ...
}
```

**Realidade:**
- O modelo `Patrimonio` **NÃO** tem objeto `Sala`
- Tem apenas: `getIdSala()` e `getNomeSala()` (campo transiente)

**Solução:**
```java
// Usar campos diretos
detalhes.setSalaId(patrimonio.getIdSala());
detalhes.setSalaNome(patrimonio.getNomeSala());
```

---

#### Problema: `getResponsavel()` não existe
**Linhas:** 113, 114, 115, 116, 117, 118, 189, 190, 191

**Código Atual:**
```java
if (patrimonio.getResponsavel() != null) {  // ERRO!
    detalhes.setResponsavelId(patrimonio.getResponsavel().getId());
    detalhes.setResponsavelNome(patrimonio.getResponsavel().getNome());
    // ...
}
```

**Realidade:**
- O modelo `Patrimonio` **NÃO** tem objeto `Responsavel`
- Tem apenas: `getIdResponsavel()` e `getNomeResponsavel()` (campo transiente)

**Solução:**
```java
// Usar campos diretos
detalhes.setResponsavelId(patrimonio.getIdResponsavel());
detalhes.setResponsavelNome(patrimonio.getNomeResponsavel());
detalhes.setResponsavelMatricula(patrimonio.getMatriculaResponsavel());
```

---

#### Problema: `getSetor()` não existe
**Linhas:** 195, 196, 197

**Código Atual:**
```java
if (patrimonio.getSetor() != null) {  // ERRO!
    dto.setSetorId(patrimonio.getSetor().getId());
    dto.setSetorNome(patrimonio.getSetor().getNome());
}
```

**Realidade:**
- O modelo `Patrimonio` **NÃO** tem objeto `Setor`
- Tem apenas: `getNomeSetor()` (campo transiente)
- **NÃO** tem `idSetor`

**Solução:**
```java
// Usar campo direto (apenas nome)
if (patrimonio.getNomeSetor() != null) {
    dto.setSetorNome(patrimonio.getNomeSetor());
}
// Não há setorId disponível
```

---

#### Problema: `isColetado()` não existe
**Linhas:** 123, 201

**Código Atual:**
```java
detalhes.setColetado(patrimonio.isColetado());  // ERRO!
dto.setColetado(patrimonio.isColetado());       // ERRO!
```

**Realidade:**
- O modelo `Patrimonio` **NÃO** tem campo `coletado`
- Essa informação vem da tabela `COLETA`, não de `PATRIMONIO`

**Solução:**
```java
// Buscar da tabela COLETA
// Precisa consultar ColetaDAO para verificar se foi coletado
```

---

#### Problema: `getDataColeta()` não existe
**Linhas:** 124, 202

**Código Atual:**
```java
detalhes.setDataColeta(patrimonio.getDataColeta());  // ERRO!
dto.setDataColeta(patrimonio.getDataColeta());       // ERRO!
```

**Realidade:**
- O modelo `Patrimonio` **NÃO** tem campo `dataColeta`
- Essa informação vem da tabela `COLETA`

**Solução:**
```java
// Buscar da tabela COLETA
// Precisa consultar ColetaDAO
```

---

### 2. **Método Inexistente no DAO**

#### Problema: `buscarAvancada()` não existe no PatrimonioDAO
**Linha:** 153

**Código Atual:**
```java
List<Patrimonio> patrimonios = patrimonioDAO.buscarAvancada(
        termo, salaId, responsavelId, limit);  // ERRO!
```

**Realidade:**
- Criamos o método `buscarAvancada()` no PatrimonioDAO
- Mas pode não estar compilado ainda

**Solução:**
- Verificar se o método foi adicionado corretamente
- Recompilar o projeto

---

### 3. **Incompatibilidade de Tipos**

#### Problema: `setId()` espera Long, mas recebe int
**Linha:** 172

**Código Atual:**
```java
dto.setId(patrimonio.getId());  // getId() retorna int
```

**DTO Espera:**
```java
public void setId(Long id)  // Espera Long
```

**Solução:**
```java
dto.setId((long) patrimonio.getId());  // Cast para Long
```

---

#### Problema: `setValor()` espera Double, mas recebe BigDecimal
**Linha:** 179

**Código Atual:**
```java
dto.setValor(patrimonio.getValor());  // getValor() retorna BigDecimal
```

**DTO Espera:**
```java
public void setValor(Double valor)  // Espera Double
```

**Solução:**
```java
BigDecimal valor = patrimonio.getValor();
dto.setValor(valor != null ? valor.doubleValue() : null);
```

---

## 📊 Resumo dos Erros

| Categoria | Quantidade | Severidade |
|-----------|------------|------------|
| Métodos inexistentes | 24 | 🔴 Alta |
| Incompatibilidade de tipos | 2 | 🟡 Média |
| Método DAO faltando | 1 | 🟡 Média |
| Lógica incorreta | 2 | 🔴 Alta |
| **TOTAL** | **29** | - |

---

## 🔧 Plano de Correção

### Passo 1: Corrigir Acesso aos Campos
- ✅ Usar `getEstadoConservacao()` ao invés de `getEstado()`
- ✅ Usar `getIdSala()` e `getNomeSala()` ao invés de `getSala()`
- ✅ Usar `getIdResponsavel()` e `getNomeResponsavel()` ao invés de `getResponsavel()`
- ✅ Usar `getNomeSetor()` ao invés de `getSetor()`

### Passo 2: Adicionar Consulta de Coleta
- ✅ Injetar `ColetaDAO` no service
- ✅ Buscar informações de coleta separadamente
- ✅ Verificar se patrimônio foi coletado
- ✅ Obter data da coleta

### Passo 3: Corrigir Conversões de Tipo
- ✅ Cast `int` para `Long` no `setId()`
- ✅ Converter `BigDecimal` para `Double` no `setValor()`

### Passo 4: Verificar Método DAO
- ✅ Confirmar que `buscarAvancada()` existe
- ✅ Recompilar se necessário

---

## 🎯 Código Corrigido

### Versão Corrigida do `converterParaDTO()`

```java
private MobilePatrimonioDTO converterParaDTO(Patrimonio patrimonio) {
    MobilePatrimonioDTO dto = new MobilePatrimonioDTO();
    
    // ID com cast
    dto.setId((long) patrimonio.getId());
    
    // Dados básicos
    dto.setCodigo(patrimonio.getNumero());
    dto.setDescricao(patrimonio.getDescricao());
    dto.setMarca(patrimonio.getMarca());
    dto.setModelo(patrimonio.getModelo());
    dto.setNumeroSerie(patrimonio.getNumeroSerie());
    
    // Estado (método correto)
    dto.setEstado(patrimonio.getEstadoConservacao());
    
    // Valor com conversão
    BigDecimal valor = patrimonio.getValor();
    dto.setValor(valor != null ? valor.doubleValue() : null);
    
    dto.setObservacoes(patrimonio.getObservacoes());
    
    // Sala (campos diretos)
    if (patrimonio.getIdSala() > 0) {
        dto.setSalaId(patrimonio.getIdSala());
        dto.setSalaNome(patrimonio.getNomeSala());
    }
    
    // Responsável (campos diretos)
    if (patrimonio.getIdResponsavel() > 0) {
        dto.setResponsavelId(patrimonio.getIdResponsavel());
        dto.setResponsavelNome(patrimonio.getNomeResponsavel());
    }
    
    // Setor (apenas nome)
    if (patrimonio.getNomeSetor() != null) {
        dto.setSetorNome(patrimonio.getNomeSetor());
    }
    
    // Status de coleta (buscar do ColetaDAO)
    try {
        boolean coletado = coletaDAO.verificarSePatrimonioFoiColetado(
            patrimonio.getId(), 
            inventarioAtivo
        );
        dto.setColetado(coletado);
        
        if (coletado) {
            Date dataColeta = coletaDAO.obterDataColeta(
                patrimonio.getId(), 
                inventarioAtivo
            );
            dto.setDataColeta(dataColeta);
        }
    } catch (SQLException e) {
        logger.warn("Erro ao verificar coleta do patrimônio {}: {}", 
            patrimonio.getId(), e.getMessage());
        dto.setColetado(false);
    }
    
    return dto;
}
```

---

## ✅ Checklist de Correção

- [ ] Substituir `getEstado()` por `getEstadoConservacao()`
- [ ] Substituir `getSala()` por `getIdSala()` e `getNomeSala()`
- [ ] Substituir `getResponsavel()` por `getIdResponsavel()` e `getNomeResponsavel()`
- [ ] Substituir `getSetor()` por `getNomeSetor()`
- [ ] Adicionar cast `(long)` no `setId()`
- [ ] Adicionar conversão `doubleValue()` no `setValor()`
- [ ] Injetar `ColetaDAO` no service
- [ ] Implementar busca de status de coleta
- [ ] Implementar busca de data de coleta
- [ ] Testar compilação
- [ ] Testar endpoints

---

**Conclusão:** A classe tem **29 erros críticos** que impedem a compilação. Todos são causados por:
1. Uso incorreto do modelo `Patrimonio` (assumindo objetos que não existem)
2. Incompatibilidade de tipos entre modelo e DTO
3. Falta de consulta à tabela `COLETA` para informações de coleta

**Tempo estimado de correção:** 30 minutos

