# Diagrama - Mapeamento de Cabeçalho Excel

## 🎯 Visão Geral do Processo

```
┌─────────────────────────────────────────────────────────────────┐
│                    ARQUIVO EXCEL                                │
│  ┌───────┬────────┬───────────┬──────────────┬────────┬──────┐ │
│  │ Número│ Status │ Descrição │ Carga Atual  │ Setor  │ Sala │ │
│  ├───────┼────────┼───────────┼──────────────┼────────┼──────┤ │
│  │ 12345 │ Ativo  │ Notebook  │ João Silva   │ TI     │ 101  │ │
│  │ 12346 │ Ativo  │ Monitor   │ Maria Santos │ Admin  │ 102  │ │
│  └───────┴────────┴───────────┴──────────────┴────────┴──────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 1. LER ARQUIVO  │
                    └─────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              PRIMEIRA LINHA (CABEÇALHO)                         │
│  ┌───────┬────────┬───────────┬──────────────┬────────┬──────┐ │
│  │   0   │   1    │     2     │      3       │   4    │  5   │ │ ← Índices
│  ├───────┼────────┼───────────┼──────────────┼────────┼──────┤ │
│  │Número │ Status │ Descrição │ Carga Atual  │ Setor  │ Sala │ │
│  └───────┴────────┴───────────┴──────────────┴────────┴──────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 2. NORMALIZAR   │
                    │    NOMES        │
                    └─────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              NOMES NORMALIZADOS                                 │
│  ┌────────┬────────┬───────────┬──────────────┬────────┬──────┐│
│  │ NUMERO │ STATUS │ DESCRICAO │ CARGA ATUAL  │ SETOR  │ SALA ││
│  └────────┴────────┴───────────┴──────────────┴────────┴──────┘│
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 3. BUSCAR NO    │
                    │   DICIONÁRIO    │
                    └─────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              DICIONÁRIO DE VARIAÇÕES                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ "NUMERO"       → COL_NUMERO (1)                          │  │
│  │ "NO"           → COL_NUMERO (1)                          │  │
│  │ "CODIGO"       → COL_NUMERO (1)                          │  │
│  │ "STATUS"       → COL_STATUS (2)                          │  │
│  │ "DESCRICAO"    → COL_DESCRICAO (4)                       │  │
│  │ "CARGA ATUAL"  → COL_CARGA_ATUAL (6)                     │  │
│  │ "RESPONSAVEL"  → COL_CARGA_ATUAL (6)                     │  │
│  │ "SETOR"        → COL_SETOR_RESPONSAVEL (7)               │  │
│  │ "SALA"         → COL_SALA (16)                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 4. CRIAR        │
                    │   MAPEAMENTO    │
                    └─────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              MAPEAMENTO FINAL                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ COL_NUMERO (1)            → Coluna Excel 0               │  │
│  │ COL_STATUS (2)            → Coluna Excel 1               │  │
│  │ COL_DESCRICAO (4)         → Coluna Excel 2               │  │
│  │ COL_CARGA_ATUAL (6)       → Coluna Excel 3               │  │
│  │ COL_SETOR_RESPONSAVEL (7) → Coluna Excel 4               │  │
│  │ COL_SALA (16)             → Coluna Excel 5               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 5. VALIDAR      │
                    │   OBRIGATÓRIAS  │
                    └─────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              VALIDAÇÃO                                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ ✅ COL_NUMERO encontrado                                 │  │
│  │ ✅ COL_DESCRICAO encontrado                              │  │
│  │ ✅ Todas as colunas obrigatórias presentes               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ 6. PROCESSAR    │
                    │    LINHAS       │
                    └─────────────────┘
```

---

## 🔄 Fluxo Detalhado de Normalização

### Exemplo: "Nº Patrimônio"

```
┌──────────────────┐
│ "Nº Patrimônio"  │  ← Valor original da célula
└──────────────────┘
         ↓
┌──────────────────┐
│ Remove acentos   │
│ "No Patrimonio"  │
└──────────────────┘
         ↓
┌──────────────────┐
│ Remove especiais │
│ "No Patrimonio"  │
└──────────────────┘
         ↓
┌──────────────────┐
│ Remove espaços   │
│ extras           │
│ "No Patrimonio"  │
└──────────────────┘
         ↓
┌──────────────────┐
│ Converte para    │
│ maiúsculas       │
│ "NO PATRIMONIO"  │
└──────────────────┘
         ↓
┌──────────────────┐
│ Busca no         │
│ dicionário       │
│ ✅ Encontrado!   │
│ → COL_NUMERO     │
└──────────────────┘
```

---

## 📊 Exemplo Completo de Mapeamento

### Arquivo Excel Original

```
┌─────────┬──────────┬─────────────┬────────────────┬──────────────┬────────┐
│ Nº      │ Situação │ Descrição   │ Responsável    │ Departamento │ Local  │
│         │          │ do Bem      │ Atual          │              │        │
├─────────┼──────────┼─────────────┼────────────────┼──────────────┼────────┤
│ 12345   │ Ativo    │ Notebook    │ João Silva(TI) │ TI           │ Sala 1 │
│ 12346   │ Ativo    │ Monitor     │ Maria(Admin)   │ Admin        │ Sala 2 │
└─────────┴──────────┴─────────────┴────────────────┴──────────────┴────────┘
```

### Passo 1: Leitura do Cabeçalho

```
Coluna 0: "Nº"
Coluna 1: "Situação"
Coluna 2: "Descrição do Bem"
Coluna 3: "Responsável Atual"
Coluna 4: "Departamento"
Coluna 5: "Local"
```

### Passo 2: Normalização

```
Coluna 0: "Nº"                  → "NO"
Coluna 1: "Situação"            → "SITUACAO"
Coluna 2: "Descrição do Bem"    → "DESCRICAO DO BEM"
Coluna 3: "Responsável Atual"   → "RESPONSAVEL ATUAL"
Coluna 4: "Departamento"        → "DEPARTAMENTO"
Coluna 5: "Local"               → "LOCAL"
```

### Passo 3: Busca no Dicionário

```
"NO"                 → ✅ Encontrado → COL_NUMERO
"SITUACAO"           → ✅ Encontrado → COL_STATUS
"DESCRICAO DO BEM"   → ✅ Encontrado → COL_DESCRICAO
"RESPONSAVEL ATUAL"  → ✅ Encontrado → COL_CARGA_ATUAL
"DEPARTAMENTO"       → ✅ Encontrado → COL_SETOR_RESPONSAVEL
"LOCAL"              → ✅ Encontrado → COL_SALA
```

### Passo 4: Mapeamento Final

```
┌─────────────────────┬──────────────┐
│ Constante Sistema   │ Coluna Excel │
├─────────────────────┼──────────────┤
│ COL_NUMERO (1)      │      0       │
│ COL_STATUS (2)      │      1       │
│ COL_DESCRICAO (4)   │      2       │
│ COL_CARGA_ATUAL (6) │      3       │
│ COL_SETOR_RESP (7)  │      4       │
│ COL_SALA (16)       │      5       │
└─────────────────────┴──────────────┘
```

### Passo 5: Extração de Dados

Quando processar a linha 2 (primeira linha de dados):

```java
String[] campos = new String[18];

// Usa o mapeamento para extrair valores corretos
campos[COL_NUMERO]            = row.getCell(0).getStringCellValue(); // "12345"
campos[COL_STATUS]            = row.getCell(1).getStringCellValue(); // "Ativo"
campos[COL_DESCRICAO]         = row.getCell(2).getStringCellValue(); // "Notebook"
campos[COL_CARGA_ATUAL]       = row.getCell(3).getStringCellValue(); // "João Silva(TI)"
campos[COL_SETOR_RESPONSAVEL] = row.getCell(4).getStringCellValue(); // "TI"
campos[COL_SALA]              = row.getCell(5).getStringCellValue(); // "Sala 1"
```

---

## 🎨 Visualização do Dicionário de Variações

```
┌─────────────────────────────────────────────────────────────────┐
│                  DICIONÁRIO DE VARIAÇÕES                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  COL_NUMERO (1)                                                 │
│  ├─ "NUMERO"                                                    │
│  ├─ "NO"                                                        │
│  ├─ "N"                                                         │
│  ├─ "NUMERO PATRIMONIO"                                         │
│  ├─ "NO PATRIMONIO"                                             │
│  ├─ "PATRIMONIO"                                                │
│  ├─ "CODIGO"                                                    │
│  ├─ "COD"                                                       │
│  ├─ "TOMBAMENTO"                                                │
│  └─ "PLAQUETA"                                                  │
│                                                                 │
│  COL_STATUS (2)                                                 │
│  ├─ "STATUS"                                                    │
│  ├─ "SITUACAO"                                                  │
│  └─ "ESTADO"                                                    │
│                                                                 │
│  COL_DESCRICAO (4)                                              │
│  ├─ "DESCRICAO"                                                 │
│  ├─ "DESCRICAO DO BEM"                                          │
│  ├─ "BEM"                                                       │
│  ├─ "ITEM"                                                      │
│  ├─ "MATERIAL"                                                  │
│  └─ "PRODUTO"                                                   │
│                                                                 │
│  COL_CARGA_ATUAL (6)                                            │
│  ├─ "CARGA ATUAL"                                               │
│  ├─ "RESPONSAVEL"                                               │
│  ├─ "RESPONSAVEL ATUAL"                                         │
│  ├─ "CARGA"                                                     │
│  ├─ "USUARIO"                                                   │
│  ├─ "SERVIDOR"                                                  │
│  └─ "DETENTOR"                                                  │
│                                                                 │
│  COL_SALA (16)                                                  │
│  ├─ "SALA"                                                      │
│  ├─ "AMBIENTE"                                                  │
│  ├─ "LOCAL"                                                     │
│  ├─ "LOCALIZACAO FISICA"                                        │
│  ├─ "NUMERO SALA"                                               │
│  └─ "NO SALA"                                                   │
│                                                                 │
│  ... (outras colunas)                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Tratamento de Erros

### Cenário 1: Coluna Obrigatória Não Encontrada

```
┌─────────────────────────────────────────────────────────────────┐
│  ❌ ERRO: Coluna obrigatória não encontrada                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Colunas obrigatórias faltando:                                 │
│  • Número do Patrimônio                                         │
│                                                                 │
│  Verifique se o arquivo possui uma coluna com um dos nomes:     │
│  • Número                                                       │
│  • Nº                                                           │
│  • Código                                                       │
│  • Patrimônio                                                   │
│  • Tombamento                                                   │
│                                                                 │
│  [OK]                                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Cenário 2: Coluna Não Reconhecida (Aviso)

```
┌─────────────────────────────────────────────────────────────────┐
│  ⚠️  AVISO: Colunas não reconhecidas                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  As seguintes colunas serão ignoradas:                          │
│  • Observações                                                  │
│  • Foto                                                         │
│  • Garantia                                                     │
│                                                                 │
│  Deseja continuar?                                              │
│                                                                 │
│  [Sim]  [Não]                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📈 Estatísticas de Mapeamento

```
┌─────────────────────────────────────────────────────────────────┐
│              ESTATÍSTICAS DO MAPEAMENTO                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Total de colunas no Excel: 8                                   │
│  Colunas reconhecidas: 6                                        │
│  Colunas não reconhecidas: 2                                    │
│                                                                 │
│  Colunas obrigatórias: 2/2 ✅                                   │
│  Colunas opcionais: 4/15                                        │
│                                                                 │
│  Taxa de reconhecimento: 75%                                    │
│                                                                 │
│  Status: ✅ Pronto para importar                                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Código Simplificado

```java
// 1. Ler cabeçalho
Row headerRow = sheet.getRow(0);

// 2. Criar dicionário de variações
Map<String, Integer> dicionario = criarDicionarioVariacoes();

// 3. Mapear cada coluna
Map<Integer, Integer> mapping = new HashMap<>();
for (int col = 0; col < headerRow.getLastCellNum(); col++) {
    String nome = headerRow.getCell(col).getStringCellValue();
    String normalizado = normalizarCabecalho(nome);
    
    if (dicionario.containsKey(normalizado)) {
        Integer constante = dicionario.get(normalizado);
        mapping.put(constante, col);
    }
}

// 4. Validar obrigatórias
if (!mapping.containsKey(COL_NUMERO)) {
    throw new Exception("Coluna 'Número' não encontrada!");
}

// 5. Usar mapeamento para extrair dados
for (int row = 1; row <= sheet.getLastRowNum(); row++) {
    Row dataRow = sheet.getRow(row);
    
    String numero = dataRow.getCell(mapping.get(COL_NUMERO)).getStringCellValue();
    String descricao = dataRow.getCell(mapping.get(COL_DESCRICAO)).getStringCellValue();
    // ... processar patrimônio
}
```

---

**Versão**: 1.0  
**Data**: 03/11/2025  
**Tipo**: Diagrama Técnico
