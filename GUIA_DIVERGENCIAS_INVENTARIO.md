# 📋 Guia de Divergências no Sistema de Inventário

## 🎯 O que é uma Divergência?

**Divergência** é qualquer diferença entre os dados cadastrados no sistema e a situação real encontrada durante o inventário físico.

---

## 🔍 Tipos de Divergências

### 1. **Divergência de Localização** 🏢
**O que é:** Patrimônio está em local diferente do cadastrado

**Exemplo:**
- **Sistema diz:** Sala 101 - Laboratório de Informática
- **Encontrado em:** Sala 205 - Sala de Professores
- **Divergência:** ✅ SIM

**Campos envolvidos:**
- `localizacao_atual` (cadastrada)
- `localizacao_encontrada` (real)

**Quando marcar:**
- Patrimônio mudou de sala
- Patrimônio mudou de setor
- Patrimônio mudou de prédio/campus

---

### 2. **Divergência de Estado de Conservação** 🔧
**O que é:** Estado físico diferente do cadastrado

**Exemplo:**
- **Sistema diz:** BOM
- **Encontrado:** RUIM (quebrado, danificado)
- **Divergência:** ✅ SIM

**Estados possíveis:**
- Ótimo
- Bom
- Regular
- Ruim
- Péssimo

**Quando marcar:**
- Equipamento deteriorado
- Equipamento danificado
- Equipamento quebrado
- Equipamento obsoleto

---

### 3. **Divergência de Responsável** 👤
**O que é:** Responsável atual diferente do cadastrado

**Exemplo:**
- **Sistema diz:** João Silva (Coordenador TI)
- **Encontrado:** Maria Santos (Diretora Administrativa)
- **Divergência:** ✅ SIM

**Quando marcar:**
- Mudança de responsável
- Transferência de setor
- Remanejamento de pessoal

---

### 4. **Patrimônio Não Encontrado** ❌
**O que é:** Patrimônio cadastrado mas não localizado fisicamente

**Exemplo:**
- **Sistema diz:** Notebook Dell - Sala 101
- **Encontrado:** NADA (não está no local)
- **Divergência:** ✅ SIM (CRÍTICA)

**Status:** `NAO_ENCONTRADO`

**Motivos possíveis:**
- Furto/roubo
- Transferência não registrada
- Baixa não registrada
- Erro de cadastro

---

### 5. **Patrimônio Encontrado Sem Cadastro** ➕
**O que é:** Item físico existe mas não está no sistema

**Exemplo:**
- **Sistema diz:** NADA
- **Encontrado:** Projetor Epson funcionando
- **Divergência:** ✅ SIM (CRÍTICA)

**Ação:** Registrar como "Item Sem Etiqueta"

**Motivos possíveis:**
- Aquisição não registrada
- Doação não cadastrada
- Transferência não registrada

---

### 6. **Divergência de Características Físicas** 📝
**O que é:** Marca, modelo ou especificações diferentes

**Exemplo:**
- **Sistema diz:** Monitor Samsung 19"
- **Encontrado:** Monitor LG 22"
- **Divergência:** ✅ SIM

**Campos envolvidos:**
- Marca
- Modelo
- Número de série
- Especificações técnicas

---

### 7. **Divergência de Quantidade** 🔢
**O que é:** Quantidade física diferente da cadastrada

**Exemplo:**
- **Sistema diz:** 10 cadeiras
- **Encontrado:** 7 cadeiras
- **Divergência:** ✅ SIM

**Aplicável a:** Bens patrimoniais agrupados

---

## 📊 Campos do Sistema

### Na tabela `tabela_coleta`:

```sql
-- Indica se há divergência
divergencia BOOLEAN

-- Descrição do motivo
motivo_divergencia TEXT

-- Localização cadastrada
localizacao_atual VARCHAR(255)

-- Localização onde foi encontrado
localizacao_encontrada VARCHAR(255)

-- Estado encontrado
estado_encontrado VARCHAR(50)

-- Status da coleta
status_coleta VARCHAR(50)
  -- 'COLETADO' - Encontrado e OK
  -- 'NAO_ENCONTRADO' - Não localizado
  -- 'DANIFICADO' - Encontrado mas danificado
  -- 'DIVERGENCIA' - Encontrado com divergências
```

---

## 🎯 Quando Marcar Divergência?

### ✅ MARCAR DIVERGÊNCIA quando:

1. **Localização diferente**
   ```
   Cadastrado: Sala 101
   Encontrado: Sala 205
   → divergencia = TRUE
   → motivo_divergencia = "Patrimônio encontrado em sala diferente"
   ```

2. **Estado pior que o cadastrado**
   ```
   Cadastrado: BOM
   Encontrado: RUIM
   → divergencia = TRUE
   → motivo_divergencia = "Estado de conservação deteriorado"
   ```

3. **Não encontrado**
   ```
   Cadastrado: Sala 101
   Encontrado: NADA
   → divergencia = TRUE
   → status_coleta = 'NAO_ENCONTRADO'
   → motivo_divergencia = "Patrimônio não localizado no local cadastrado"
   ```

4. **Características diferentes**
   ```
   Cadastrado: Monitor Samsung 19"
   Encontrado: Monitor LG 22"
   → divergencia = TRUE
   → motivo_divergencia = "Marca e tamanho diferentes do cadastrado"
   ```

### ❌ NÃO MARCAR DIVERGÊNCIA quando:

1. **Tudo está correto**
   ```
   Cadastrado: Sala 101, BOM
   Encontrado: Sala 101, BOM
   → divergencia = FALSE
   → status_coleta = 'COLETADO'
   ```

2. **Pequenas variações aceitáveis**
   ```
   Cadastrado: Estado BOM
   Encontrado: Estado ÓTIMO (melhorou)
   → divergencia = FALSE (melhoria não é divergência)
   ```

---

## 📱 No App Mobile

### Fluxo de Coleta com Divergência:

1. **Escanear QR Code**
   - Sistema mostra dados cadastrados

2. **Verificar Localização**
   - Se diferente → Marcar divergência
   - Informar localização real

3. **Verificar Estado**
   - Se pior → Marcar divergência
   - Selecionar estado real

4. **Adicionar Observações**
   - Descrever o motivo da divergência
   - Tirar foto (se necessário)

5. **Registrar Coleta**
   - Sistema salva com flag `divergencia = TRUE`

### Exemplo de Tela:

```
┌─────────────────────────────────┐
│ Patrimônio: 12345               │
│ Descrição: Notebook Dell        │
│                                 │
│ ⚠️ DIVERGÊNCIA DETECTADA        │
│                                 │
│ Localização Cadastrada:         │
│ └─ Sala 101 - Lab. Informática │
│                                 │
│ Localização Encontrada:         │
│ └─ Sala 205 - Sala Professores │
│                                 │
│ Estado Cadastrado: BOM          │
│ Estado Encontrado: REGULAR      │
│                                 │
│ Motivo da Divergência:          │
│ ┌─────────────────────────────┐ │
│ │ Patrimônio foi transferido  │ │
│ │ para outra sala sem         │ │
│ │ atualização no sistema      │ │
│ └─────────────────────────────┘ │
│                                 │
│ [Tirar Foto] [Registrar]       │
└─────────────────────────────────┘
```

---

## 📊 Relatórios de Divergências

### Relatório de Itens com Divergência

```sql
SELECT 
    p.numero_patrimonio,
    p.descricao,
    c.localizacao_atual AS local_cadastrado,
    c.localizacao_encontrada AS local_real,
    c.motivo_divergencia,
    c.data_coleta,
    u.nome_completo AS coletor
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
INNER JOIN tabela_usuario u ON c.id_participante_inventario = u.id
WHERE c.divergencia = TRUE
ORDER BY c.data_coleta DESC;
```

### Estatísticas de Divergências

```sql
SELECT 
    COUNT(*) AS total_divergencias,
    COUNT(*) * 100.0 / (SELECT COUNT(*) FROM tabela_coleta) AS percentual,
    COUNT(CASE WHEN status_coleta = 'NAO_ENCONTRADO' THEN 1 END) AS nao_encontrados,
    COUNT(CASE WHEN localizacao_atual != localizacao_encontrada THEN 1 END) AS divergencia_local
FROM tabela_coleta
WHERE divergencia = TRUE;
```

---

## 🎯 Boas Práticas

### Para Coletores:

1. **Seja criterioso** - Marque divergência apenas quando realmente houver
2. **Documente bem** - Descreva claramente o motivo
3. **Tire fotos** - Evidências visuais são importantes
4. **Seja específico** - "Sala diferente" é vago, "Transferido da Sala 101 para 205" é melhor

### Para Gestores:

1. **Analise padrões** - Muitas divergências podem indicar problemas sistêmicos
2. **Atualize cadastros** - Use o inventário para corrigir dados
3. **Investigue não encontrados** - Podem indicar furtos ou perdas
4. **Treine equipe** - Garanta que todos entendam o que é divergência

---

## 📋 Checklist de Divergências

Durante a coleta, verificar:

- [ ] Patrimônio está no local cadastrado?
- [ ] Estado de conservação está correto?
- [ ] Responsável está correto?
- [ ] Marca/modelo estão corretos?
- [ ] Número de série confere?
- [ ] Características físicas conferem?

**Se qualquer resposta for NÃO → Marcar divergência!**

---

## 🚨 Divergências Críticas

### Requerem ação imediata:

1. **Patrimônio não encontrado** - Possível furto/perda
2. **Item sem cadastro** - Falha no controle patrimonial
3. **Estado péssimo** - Risco de segurança
4. **Localização desconhecida** - Perda de controle

### Ações recomendadas:

- Notificar gestor imediatamente
- Abrir processo de investigação
- Atualizar cadastro urgentemente
- Documentar com fotos e relatórios

---

**Versão:** 2.0.0  
**Data:** 17/11/2025  
**Autor:** Sistema de Inventário IFMT
