# 📘 Guia de Uso - Campo ED (Elemento de Despesa)

## 🎯 O que é o ED?

**ED (Elemento de Despesa)** é um código contábil usado pelo governo federal para classificar gastos públicos. No contexto do sistema de inventário, o ED identifica a natureza da despesa que originou a aquisição do patrimônio.

---

## 📋 Formato do ED

### Estrutura
```
XXXXX.XXXX
  │     │
  │     └─ Desdobramento (4 dígitos)
  └─────── Categoria (5 dígitos)
```

### Exemplos Comuns

| ED | Descrição | Exemplos de Patrimônios |
|----|-----------|------------------------|
| `12311.0101` | Equipamentos de Processamento de Dados | Computadores, Notebooks, Servidores |
| `12311.0103` | Móveis e Utensílios | Mesas, Cadeiras, Armários, Estantes |
| `12311.0107` | Veículos | Carros, Caminhões, Motos |
| `12311.0109` | Equipamentos de Áudio, Vídeo e Foto | Câmeras, Projetores, Caixas de Som |
| `12311.0111` | Equipamentos de Laboratório | Microscópios, Balanças, Centrífugas |

---

## 💾 Como Cadastrar ED

### 1. Via Importação CSV

**Arquivo CSV:**
```csv
NUMERO,STATUS,ED,DESCRICAO,...
3241,ATIVO,12311.0101,OSCILOSCOPIO ANALOGICO,...
3260,ATIVO,12311.0111,FONTE DE ALIMENTACAO,...
16371,ATIVO,12311.0103,CONDICIONADOR DE AR,...
```

**Passos:**
1. Abrir sistema desktop
2. Menu: **Arquivo → Importar → Importar CSV**
3. Selecionar arquivo com coluna ED na posição 3
4. Aguardar processamento
5. Verificar relatório de importação

---

### 2. Via Importação Excel

**Arquivo Excel:**

| NUMERO | STATUS | ED | DESCRICAO | NUMERO NOTA FISCAL | FORNECEDOR |
|--------|--------|------------|-----------|-------------------|------------|
| 3241 | ATIVO | 12311.0101 | OSCILOSCOPIO ANALOGICO | NF-2024-001 | Fornecedor ABC |
| 3260 | ATIVO | 12311.0111 | FONTE DE ALIMENTACAO | NF-2024-002 | Fornecedor XYZ |

**Passos:**
1. Abrir sistema desktop
2. Menu: **Arquivo → Importar → Importar Excel**
3. Selecionar arquivo .xlsx ou .xls
4. Sistema mapeia automaticamente a coluna "ED"
5. Aguardar processamento
6. Verificar relatório de importação

---

### 3. Via Cadastro Manual (Futuro)

**Interface Desktop:**
```
┌─────────────────────────────────────┐
│ Cadastro de Patrimônio              │
├─────────────────────────────────────┤
│ Número: [3241                    ]  │
│ Status: [ATIVO ▼                 ]  │
│ ED:     [12311.0101              ]  │ ← NOVO CAMPO
│ Descrição: [OSCILOSCOPIO...      ]  │
│ ...                                 │
└─────────────────────────────────────┘
```

---

## 🔍 Como Consultar ED

### 1. Via SQL

```sql
-- Buscar patrimônios por ED
SELECT NUMERO, DESCRICAO, ED, NUMERO_NOTA_FISCAL, FORNECEDOR
FROM TABELA_PATRIMONIO
WHERE ED = '12311.0101';

-- Contar patrimônios por ED
SELECT ED, COUNT(*) as TOTAL
FROM TABELA_PATRIMONIO
WHERE ED IS NOT NULL
GROUP BY ED
ORDER BY TOTAL DESC;

-- Listar EDs únicos
SELECT DISTINCT ED
FROM TABELA_PATRIMONIO
WHERE ED IS NOT NULL
ORDER BY ED;
```

### 2. Via API Mobile

**Endpoint:**
```
GET /api/mobile/patrimonio/numero/{numero}
```

**Resposta:**
```json
{
  "id": 123,
  "codigo": "3241",
  "descricao": "OSCILOSCOPIO ANALOGICO",
  "ed": "12311.0101",
  "numeroNotaFiscal": "NF-2024-001",
  "fornecedor": "Fornecedor ABC LTDA",
  ...
}
```

---

## 📊 Relatórios com ED

### 1. Relatório por Elemento de Despesa

```sql
SELECT 
    ED,
    COUNT(*) as TOTAL_PATRIMONIOS,
    SUM(VALOR_AQUISICAO) as VALOR_TOTAL,
    AVG(VALOR_AQUISICAO) as VALOR_MEDIO
FROM TABELA_PATRIMONIO
WHERE ED IS NOT NULL
GROUP BY ED
ORDER BY VALOR_TOTAL DESC;
```

**Resultado:**
```
ED          | TOTAL_PATRIMONIOS | VALOR_TOTAL | VALOR_MEDIO
------------|-------------------|-------------|-------------
12311.0101  | 1.234            | R$ 2.500.000| R$ 2.025,00
12311.0103  | 3.456            | R$ 1.800.000| R$ 520,83
12311.0107  | 45               | R$ 1.200.000| R$ 26.666,67
```

### 2. Relatório de Patrimônios Sem ED

```sql
SELECT 
    NUMERO,
    DESCRICAO,
    VALOR_AQUISICAO,
    FORNECEDOR
FROM TABELA_PATRIMONIO
WHERE ED IS NULL OR ED = ''
ORDER BY VALOR_AQUISICAO DESC;
```

---

## 🔧 Validações

### Formato Válido
- ✅ `12311.0101` - Correto
- ✅ `12311.0103` - Correto
- ✅ `12311.0107` - Correto

### Formato Inválido
- ❌ `12311-0101` - Usar ponto, não hífen
- ❌ `123110101` - Falta o ponto separador
- ❌ `12311.01` - Faltam dígitos no desdobramento
- ❌ `123.0101` - Faltam dígitos na categoria

### Validação SQL
```sql
-- Verificar EDs com formato inválido
SELECT NUMERO, ED
FROM TABELA_PATRIMONIO
WHERE ED IS NOT NULL 
  AND ED NOT SIMILAR TO '[0-9]{5}\.[0-9]{4}';
```

---

## 📱 Integração com App Mobile

### Exibição no App

**Tela de Detalhes do Patrimônio:**
```
┌─────────────────────────────────────┐
│ Patrimônio #3241                    │
├─────────────────────────────────────┤
│ Descrição:                          │
│ OSCILOSCOPIO ANALOGICO              │
│                                     │
│ Marca: MINIPA                       │
│ Modelo: MO-1225                     │
│ Série: MO122501081                  │
│                                     │
│ ED: 12311.0101                      │ ← NOVO
│ Nota Fiscal: NF-2024-001            │
│ Fornecedor: Fornecedor ABC LTDA     │
│                                     │
│ Valor: R$ 1.500,00                  │
│ Estado: BOM                         │
│                                     │
│ Sala: Sala 101                      │
│ Responsável: João Silva             │
└─────────────────────────────────────┘
```

---

## 🚨 Problemas Comuns

### 1. ED não aparece após importação

**Causa:** Coluna ED não está na posição correta (CSV) ou cabeçalho incorreto (Excel)

**Solução:**
- **CSV:** Verificar se ED está na coluna 3
- **Excel:** Verificar se cabeçalho é exatamente "ED"

### 2. ED importado como vazio

**Causa:** Célula vazia no arquivo de importação

**Solução:**
- Preencher campo ED no arquivo antes de importar
- Ou atualizar via SQL depois:
```sql
UPDATE TABELA_PATRIMONIO
SET ED = '12311.0101'
WHERE NUMERO = '3241';
```

### 3. ED com formato incorreto

**Causa:** Formato diferente de `XXXXX.XXXX`

**Solução:**
- Corrigir formato no arquivo de importação
- Ou atualizar via SQL:
```sql
UPDATE TABELA_PATRIMONIO
SET ED = '12311.0101'
WHERE ED = '123110101'; -- Formato incorreto
```

---

## 📚 Referências

### Tabela de EDs Comuns

| Categoria | ED | Descrição |
|-----------|----|-----------| 
| Informática | 12311.0101 | Equipamentos de Processamento de Dados |
| Móveis | 12311.0103 | Móveis e Utensílios |
| Veículos | 12311.0107 | Veículos |
| Áudio/Vídeo | 12311.0109 | Equipamentos de Áudio, Vídeo e Foto |
| Laboratório | 12311.0111 | Equipamentos de Laboratório |
| Comunicação | 12311.0113 | Equipamentos de Comunicação |
| Médico | 12311.0115 | Equipamentos Médicos e Hospitalares |
| Segurança | 12311.0117 | Equipamentos de Segurança |

### Links Úteis

- **SIADS:** Sistema Integrado de Administração de Serviços
- **Portaria STN:** Classificação de Elementos de Despesa
- **Manual SIAFI:** Sistema Integrado de Administração Financeira

---

## ✅ Checklist de Uso

- [ ] Arquivo de importação tem coluna ED
- [ ] Formato do ED está correto (`XXXXX.XXXX`)
- [ ] ED corresponde à categoria do patrimônio
- [ ] Nota Fiscal e Fornecedor preenchidos
- [ ] Importação executada com sucesso
- [ ] Dados verificados no banco
- [ ] API mobile retornando ED
- [ ] Relatórios incluindo ED

---

**Versão:** 1.0.0  
**Data:** 16/11/2024  
**Status:** ✅ Documentação Completa
