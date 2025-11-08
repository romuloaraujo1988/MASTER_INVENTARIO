# Mapeamento de Colunas Excel - Versão Melhorada

## 🎯 Problema Identificado

O sistema atual mapeia colunas, mas pode falhar com variações de nomes como:
- "Nº" vs "Numero" vs "Número Patrimônio"
- "Responsável" vs "Carga Atual" vs "Responsável Atual"

## ✅ Solução: Mapeamento Flexível com Múltiplas Variações

### Implementação Melhorada

```java
private Map<Integer, Integer> mapearCabecalhoFlexivel(Row headerRow) {
    Map<Integer, Integer> mapping = new HashMap<>();
    
    // Dicionário com TODAS as variações possíveis de cada coluna
    Map<String, Integer> variacoesParaConstante = criarDicionarioVariacoes();
    
    // Para cada coluna do Excel
    short lastCellNum = headerRow.getLastCellNum();
    for (int c = headerRow.getFirstCellNum(); c < lastCellNum; c++) {
        Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) continue;
        
        String valorOriginal = getCellString(cell).trim();
        String normalizado = normalizarCabecalho(valorOriginal);
        
        // Busca no dicionário de variações
        if (variacoesParaConstante.containsKey(normalizado)) {
            Integer constante = variacoesParaConstante.get(normalizado);
            mapping.put(constante, c);
            
            // Log para debug
            System.out.println(String.format(
                "Coluna %d: '%s' → '%s' → COL_%d", 
                c, valorOriginal, normalizado, constante
            ));
        } else {
            // Log de coluna não reconhecida
            System.out.println(String.format(
                "Coluna %d: '%s' → '%s' [NÃO RECONHECIDA]", 
                c, valorOriginal, normalizado
            ));
        }
    }
    
    return mapping;
}

private Map<String, Integer> criarDicionarioVariacoes() {
    Map<String, Integer> dicionario = new HashMap<>();
    
    // ========== NUMERO DO PATRIMONIO ==========
    // Aceita: Nº, Numero, Número, Nº Patrimônio, Código, etc.
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_NUMERO,
        "NUMERO",
        "NO",
        "N",
        "NUMERO PATRIMONIO",
        "NO PATRIMONIO",
        "PATRIMONIO",
        "CODIGO",
        "COD",
        "TOMBAMENTO",
        "PLAQUETA"
    );
    
    // ========== STATUS ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_STATUS,
        "STATUS",
        "SITUACAO",
        "ESTADO"
    );
    
    // ========== EDIFICACAO ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_ED,
        "ED",
        "EDIFICACAO",
        "PREDIO",
        "BLOCO"
    );
    
    // ========== DESCRICAO ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_DESCRICAO,
        "DESCRICAO",
        "DESCRICAO DO BEM",
        "BEM",
        "ITEM",
        "MATERIAL",
        "PRODUTO"
    );
    
    // ========== ROTULOS ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_ROTULOS,
        "ROTULOS",
        "ROTULO",
        "TAGS",
        "ETIQUETAS",
        "CATEGORIAS"
    );
    
    // ========== CARGA ATUAL / RESPONSAVEL ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_CARGA_ATUAL,
        "CARGA ATUAL",
        "RESPONSAVEL",
        "RESPONSAVEL ATUAL",
        "CARGA",
        "USUARIO",
        "SERVIDOR",
        "DETENTOR"
    );
    
    // ========== SETOR RESPONSAVEL ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_SETOR_RESPONSAVEL,
        "SETOR RESPONSAVEL",
        "SETOR",
        "DEPARTAMENTO",
        "UNIDADE",
        "COORDENACAO",
        "DIRETORIA"
    );
    
    // ========== CAMPUS ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_CAMPUS,
        "CAMPUS",
        "UNIDADE",
        "LOCAL",
        "LOCALIZACAO"
    );
    
    // ========== VALOR AQUISICAO ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_VALOR_AQUISICAO,
        "VALOR AQUISICAO",
        "VALOR",
        "VALOR DE AQUISICAO",
        "PRECO",
        "CUSTO",
        "VALOR ORIGINAL"
    );
    
    // ========== VALOR DEPRECIADO ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_VALOR_DEPRECIADO,
        "VALOR DEPRECIADO",
        "VALOR ATUAL",
        "VALOR RESIDUAL",
        "DEPRECIACAO"
    );
    
    // ========== NUMERO NOTA FISCAL ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_NUMERO_NOTA_FISCAL,
        "NUMERO NOTA FISCAL",
        "NOTA FISCAL",
        "NF",
        "NO NF",
        "NUMERO NF"
    );
    
    // ========== NUMERO SERIE ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_NUMERO_SERIE,
        "NUMERO SERIE",
        "SERIE",
        "NO SERIE",
        "SERIAL",
        "SN"
    );
    
    // ========== DATA ENTRADA ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_DATA_ENTRADA,
        "DATA ENTRADA",
        "DATA DE ENTRADA",
        "DATA AQUISICAO",
        "DATA COMPRA",
        "DATA INCORPORACAO"
    );
    
    // ========== DATA CARGA ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_DATA_CARGA,
        "DATA CARGA",
        "DATA DA CARGA",
        "DATA ATRIBUICAO",
        "DATA RESPONSABILIDADE"
    );
    
    // ========== FORNECEDOR ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_FORNECEDOR,
        "FORNECEDOR",
        "EMPRESA",
        "VENDEDOR"
    );
    
    // ========== SALA ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_SALA,
        "SALA",
        "AMBIENTE",
        "LOCAL",
        "LOCALIZACAO FISICA",
        "NUMERO SALA",
        "NO SALA"
    );
    
    // ========== ESTADO CONSERVACAO ==========
    adicionarVariacoes(dicionario, ImportacaoCSVConstants.COL_ESTADO_CONSERVACAO,
        "ESTADO CONSERVACAO",
        "CONSERVACAO",
        "ESTADO",
        "CONDICAO",
        "QUALIDADE"
    );
    
    return dicionario;
}

/**
 * Adiciona múltiplas variações de nome para uma mesma constante
 */
private void adicionarVariacoes(Map<String, Integer> dicionario, 
                                Integer constante, 
                                String... variacoes) {
    for (String variacao : variacoes) {
        dicionario.put(variacao, constante);
    }
}
```

## 🔍 Exemplos de Mapeamento

### Exemplo 1: Arquivo SUAP Padrão

**Cabeçalho Excel:**
```
Número | Status | Descrição | Carga Atual | Setor Responsável | Sala
```

**Normalização:**
```
NUMERO → COL_NUMERO ✅
STATUS → COL_STATUS ✅
DESCRICAO → COL_DESCRICAO ✅
CARGA ATUAL → COL_CARGA_ATUAL ✅
SETOR RESPONSAVEL → COL_SETOR_RESPONSAVEL ✅
SALA → COL_SALA ✅
```

### Exemplo 2: Arquivo com Variações

**Cabeçalho Excel:**
```
Nº Patrimônio | Situação | Descrição do Bem | Responsável | Departamento | Ambiente
```

**Normalização:**
```
NO PATRIMONIO → COL_NUMERO ✅
SITUACAO → COL_STATUS ✅
DESCRICAO DO BEM → COL_DESCRICAO ✅
RESPONSAVEL → COL_CARGA_ATUAL ✅
DEPARTAMENTO → COL_SETOR_RESPONSAVEL ✅
AMBIENTE → COL_SALA ✅
```

### Exemplo 3: Arquivo Simplificado

**Cabeçalho Excel:**
```
Código | Item | Valor | Setor | Local
```

**Normalização:**
```
CODIGO → COL_NUMERO ✅
ITEM → COL_DESCRICAO ✅
VALOR → COL_VALOR_AQUISICAO ✅
SETOR → COL_SETOR_RESPONSAVEL ✅
LOCAL → COL_SALA ✅
```

## 🎯 Validação do Mapeamento

Após mapear, o sistema valida se as colunas obrigatórias foram encontradas:

```java
private void validarMapeamento(Map<Integer, Integer> mapping) throws Exception {
    // Colunas obrigatórias
    List<Integer> obrigatorias = Arrays.asList(
        ImportacaoCSVConstants.COL_NUMERO,
        ImportacaoCSVConstants.COL_DESCRICAO
    );
    
    List<String> faltando = new ArrayList<>();
    
    for (Integer col : obrigatorias) {
        if (!mapping.containsKey(col)) {
            String nomeCampo = getNomeCampo(col);
            faltando.add(nomeCampo);
        }
    }
    
    if (!faltando.isEmpty()) {
        throw new Exception(
            "Colunas obrigatórias não encontradas no cabeçalho: " + 
            String.join(", ", faltando) + 
            "\n\nVerifique se o arquivo possui as colunas necessárias."
        );
    }
}

private String getNomeCampo(Integer constante) {
    switch (constante) {
        case ImportacaoCSVConstants.COL_NUMERO:
            return "Número do Patrimônio";
        case ImportacaoCSVConstants.COL_DESCRICAO:
            return "Descrição";
        // ... outros campos
        default:
            return "Campo " + constante;
    }
}
```

## 📊 Log de Mapeamento

O sistema gera um log detalhado do mapeamento:

```
=== MAPEAMENTO DE COLUNAS ===
Arquivo: PATRIMONIO IFMT 25.05.2025.xls

Colunas encontradas:
✅ Coluna 0: 'Número' → 'NUMERO' → COL_NUMERO
✅ Coluna 1: 'Status' → 'STATUS' → COL_STATUS
✅ Coluna 2: 'Descrição' → 'DESCRICAO' → COL_DESCRICAO
✅ Coluna 3: 'Carga Atual' → 'CARGA ATUAL' → COL_CARGA_ATUAL
✅ Coluna 4: 'Setor Responsável' → 'SETOR RESPONSAVEL' → COL_SETOR_RESPONSAVEL
⚠️  Coluna 5: 'Observações' → 'OBSERVACOES' [NÃO RECONHECIDA]
✅ Coluna 6: 'Sala' → 'SALA' → COL_SALA

Colunas obrigatórias: OK
Colunas opcionais: 6 de 15 encontradas
Colunas não reconhecidas: 1

Pronto para importar!
```

## 🔧 Tratamento de Colunas Extras

Se o Excel tiver colunas não reconhecidas, o sistema:

1. **Ignora** a coluna (não causa erro)
2. **Registra** no log
3. **Continua** o processamento

```java
// Coluna não reconhecida é simplesmente ignorada
if (!variacoesParaConstante.containsKey(normalizado)) {
    // Log apenas, não interrompe
    System.out.println("Coluna ignorada: " + valorOriginal);
    continue;
}
```

## 🎨 Interface Visual do Mapeamento

Adicionar na tela de importação uma prévia do mapeamento:

```
┌─────────────────────────────────────────────────────────┐
│ Prévia do Mapeamento                                    │
├─────────────────────────────────────────────────────────┤
│ Excel                    →  Sistema                     │
├─────────────────────────────────────────────────────────┤
│ ✅ Número                →  Número do Patrimônio        │
│ ✅ Status                →  Status                      │
│ ✅ Descrição             →  Descrição                   │
│ ✅ Carga Atual           →  Responsável                 │
│ ✅ Setor Responsável     →  Setor                       │
│ ⚠️  Observações          →  [Ignorado]                  │
│ ✅ Sala                  →  Sala                        │
├─────────────────────────────────────────────────────────┤
│ Status: Pronto para importar                            │
│ Colunas obrigatórias: 2/2 ✅                            │
│ Colunas opcionais: 5/15                                 │
└─────────────────────────────────────────────────────────┘

[Continuar Importação]  [Cancelar]
```

## 📝 Código Completo da Melhoria

```java
/**
 * Versão melhorada do mapeamento de cabeçalho
 * Aceita múltiplas variações de nomes de colunas
 */
public class ImportacaoExcelMelhorada extends ImportacaoExcel {
    
    @Override
    protected Map<Integer, Integer> mapearCabecalho(Row headerRow) {
        Map<Integer, Integer> mapping = new HashMap<>();
        Map<String, Integer> variacoes = criarDicionarioVariacoes();
        
        System.out.println("=== MAPEAMENTO DE COLUNAS ===");
        
        short lastCellNum = headerRow.getLastCellNum();
        for (int c = headerRow.getFirstCellNum(); c < lastCellNum; c++) {
            Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) continue;
            
            String valorOriginal = getCellString(cell).trim();
            String normalizado = normalizarCabecalho(valorOriginal);
            
            if (variacoes.containsKey(normalizado)) {
                Integer constante = variacoes.get(normalizado);
                mapping.put(constante, c);
                System.out.println(String.format(
                    "✅ Coluna %d: '%s' → '%s' → %s", 
                    c, valorOriginal, normalizado, getNomeCampo(constante)
                ));
            } else {
                System.out.println(String.format(
                    "⚠️  Coluna %d: '%s' → '%s' [NÃO RECONHECIDA]", 
                    c, valorOriginal, normalizado
                ));
            }
        }
        
        // Validar colunas obrigatórias
        validarMapeamento(mapping);
        
        System.out.println("\nColunas obrigatórias: OK");
        System.out.println("Pronto para importar!\n");
        
        return mapping;
    }
    
    // ... métodos auxiliares (criarDicionarioVariacoes, validarMapeamento, etc.)
}
```

## ✅ Vantagens da Solução

1. **Flexibilidade**: Aceita múltiplas variações de nomes
2. **Robustez**: Não quebra com nomes diferentes
3. **Transparência**: Log detalhado do mapeamento
4. **Validação**: Verifica colunas obrigatórias
5. **Extensibilidade**: Fácil adicionar novas variações

## 🎯 Resultado Final

Com essa melhoria, o sistema consegue importar arquivos Excel com **qualquer variação** de nomes de colunas, desde que sejam semanticamente equivalentes.

**Exemplos que funcionam:**
- "Número" ✅
- "Nº" ✅
- "Nº Patrimônio" ✅
- "Código" ✅
- "Tombamento" ✅

Todos mapeiam para `COL_NUMERO`!

---

**Versão**: 2.0  
**Data**: 03/11/2025  
**Status**: Proposta de Melhoria
