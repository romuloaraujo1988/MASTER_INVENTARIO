# Correção - Descrição e Estado nas Coletas

## 🐛 Problema Identificado

**Sintoma:** Tabela de histórico de coletas mostrando "-" na coluna Descrição e Estado vazio.

### Causa Raiz

O método `carregarHistoricoColeta()` estava usando **observações** ao invés da **descrição do patrimônio**:

```java
// ❌ ERRADO (antes)
String descricao = coleta.isSemEtiqueta() 
    ? coleta.getDescricaoItemSemEtiqueta() 
    : (coleta.getObservacaoColeta() != null ? coleta.getObservacaoColeta() : "-");
```

**Problema:** Observações geralmente estão vazias, então sempre mostrava "-".

## ✅ Solução Aplicada

### 1. Buscar Patrimônio para Obter Descrição

```java
// ✅ CORRETO (depois)
if (coleta.isSemEtiqueta()) {
    numeroPatrimonio = "SEM ETIQUETA";
    descricao = coleta.getDescricaoItemSemEtiqueta();
} else {
    // Buscar patrimônio do banco
    Patrimonio patrimonio = patrimonioDAO.findById(coleta.getIdPatrimonio());
    if (patrimonio != null) {
        numeroPatrimonio = patrimonio.getNumero();
        descricao = patrimonio.getDescricao();  // ✅ Descrição real do patrimônio
    }
}
```

### 2. Tratar Estado Null

```java
// ✅ Tratar coletas antigas sem estado
String estadoEncontrado = coleta.getEstadoEncontrado() != null 
    ? coleta.getEstadoEncontrado() 
    : "-";
```

## 📊 Resultado Esperado

### Antes (Errado)
| Data/Hora | Patrimônio | Descrição | Estado |
|-----------|------------|-----------|--------|
| 21/11/2025 17:44 | 11425 | - | |
| 21/11/2025 17:15 | 11428 | - | |

### Depois (Correto)
| Data/Hora | Patrimônio | Descrição | Estado |
|-----------|------------|-----------|--------|
| 21/11/2025 17:44 | 509827 | OSCILOSCÓPIO TABLET DIGITAL... | BOM |
| 21/11/2025 17:15 | 515531 | Switch KVM VGA-PS2 | BOM |

## 🔧 Dados no SQLite

### Coletas Atuais (sem estado)
```sql
SELECT c.id, c.estado_encontrado, p.numero, p.descricao 
FROM local_coleta c 
LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id;

Resultado:
1 | NULL | 108019 | MONITOR LCD L200HX DE 20"...
2 | NULL | 107994 | MICROCOMPUTADOR COMPAQ 6200...
```

**Nota:** Coletas antigas não têm `estado_encontrado` salvo (NULL).

### Novas Coletas (com estado)
Quando você coletar novos patrimônios, o sistema salvará:
- ✅ `estado_encontrado` (BOM, OCIOSO, etc.)
- ✅ `observacoes` (se houver)
- ✅ `localizacao_encontrada`

## 🎯 Como Testar

### 1. Verificar Coletas Antigas
```
1. Abrir ColetaFrame_v2
2. Selecionar sala "Área do Campus"
3. Verificar tabela:
   - ✅ Descrição deve mostrar "OSCILOSCÓPIO TABLET DIGITAL..."
   - ✅ Estado deve mostrar "-" (coletas antigas)
```

### 2. Fazer Nova Coleta
```
1. Buscar patrimônio
2. Selecionar estado (ex: "BOM")
3. Adicionar observação (opcional)
4. Registrar
5. Verificar tabela:
   - ✅ Descrição completa do patrimônio
   - ✅ Estado "BOM"
```

### 3. Verificar no SQLite
```bash
sqlite3 data/inventario.db "SELECT c.id, c.estado_encontrado, p.numero, SUBSTR(p.descricao, 1, 30) FROM local_coleta c LEFT JOIN local_patrimonio p ON c.id_patrimonio = p.id;"
```

## 📝 Código Completo

```java
private void carregarHistoricoColeta(String identificacaoSala) {
    modeloTabelaHistorico.setRowCount(0);
    
    Sala salaAtual = (Sala) comboSalas.getSelectedItem();
    List<Coleta> coletas = coletaDAO.buscarColetasPorSala(salaAtual.getIdSala());
    
    for (Coleta coleta : coletas) {
        // Buscar patrimônio para descrição
        String numeroPatrimonio = "-";
        String descricao = "-";
        
        if (coleta.isSemEtiqueta()) {
            numeroPatrimonio = "SEM ETIQUETA";
            descricao = coleta.getDescricaoItemSemEtiqueta();
        } else {
            Patrimonio patrimonio = patrimonioDAO.findById(coleta.getIdPatrimonio());
            if (patrimonio != null) {
                numeroPatrimonio = patrimonio.getNumero();
                descricao = patrimonio.getDescricao();
            }
        }
        
        // Estado (pode ser null em coletas antigas)
        String estadoEncontrado = coleta.getEstadoEncontrado() != null 
            ? coleta.getEstadoEncontrado() 
            : "-";
        
        // Adicionar linha na tabela
        modeloTabelaHistorico.addRow(new Object[]{
            dataFormatada,
            numeroPatrimonio,
            descricao,
            estadoEncontrado
        });
    }
}
```

## ✅ Benefícios

1. ✅ **Descrição completa** do patrimônio aparece na tabela
2. ✅ **Estado de conservação** visível (quando salvo)
3. ✅ **Compatibilidade** com coletas antigas (mostra "-")
4. ✅ **Performance** - busca patrimônio apenas quando necessário
5. ✅ **Tratamento de erros** - não quebra se patrimônio não existir

## 🔄 Próximas Coletas

Quando você fizer novas coletas:
- ✅ Descrição será buscada do patrimônio
- ✅ Estado será salvo e exibido
- ✅ Observações serão salvas (se houver)
- ✅ Tudo aparecerá corretamente na tabela

---

**Data:** 21/11/2025  
**Status:** ✅ CORRIGIDO  
**Versão:** 2.0.3
