# Validação de Itens Compostos vs. Coletas Normais - Requirements

## 1. Visão Geral

### 1.1 Problema Identificado

**Situação Atual:**
- O sistema permite que itens compostos sejam coletados como coletas normais
- Isso viola a regra de negócio: itens compostos devem ser coletados apenas através do fluxo de coleta composta
- Foram coletados **106 itens compostos** como coletas normais (verificado no banco)

**Exemplo Real:**
- Patrimônio 303752 (Conjunto Escolar): 2 componentes cadastrados
- Foi coletado como coleta normal (ID 6946)
- Componentes não foram registrados na `tabela_coleta_componente`

### 1.2 Impacto

**Dados Corrompidos:**
- 106 itens compostos coletados incorretamente
- Dados de coleta incompletos (faltam componentes)
- Relatórios de itens não coletados podem estar incorretos
- Estatísticas de coleta imprecisas

**Risco:**
- Perda de rastreabilidade dos componentes
- Dificuldade em identificar itens completos vs. parciais
- Problemas em auditorias futuras

---

## 2. User Stories

### 2.1 Identificação de Itens Compostos Coletados Normalmente
**Como** administrador do sistema,  
**Quero** identificar quais itens compostos foram coletados como coletas normais,  
**Para** corrigir os dados e garantir integridade.

**Acceptance Criteria:**
- AC 2.1.1: Sistema deve gerar relatório com todos os itens compostos coletados normalmente
- AC 2.1.2: Relatório deve incluir: número patrimônio, descrição, data da coleta, localização
- AC 2.1.3: Relatório deve indicar quantos componentes o item possui
- AC 2.1.4: Relatório deve indicar quantos componentes foram coletados (se houver)

### 2.2 Prevenção de Coletas Normais de Itens Compostos
**Como** coletor,  
**Quero** que o sistema impeça a coleta normal de itens compostos,  
**Para** garantir que todos os componentes sejam registrados.

**Acceptance Criteria:**
- AC 2.2.1: Sistema deve verificar se o patrimônio é composto antes de permitir coleta normal
- AC 2.2.2: Se for composto, sistema deve mostrar mensagem de erro clara
- AC 2.2.3: Sistema deve redirecionar para o fluxo de coleta composta
- AC 2.2.4: Validação deve ocorrer antes de salvar a coleta

### 2.3 Correção de Dados de Coletas Incorretas
**Como** administrador,  
**Quero** corrigir coletas de itens compostos que foram feitas normalmente,  
**Para** restaurar a integridade dos dados.

**Acceptance Criteria:**
- AC 2.3.1: Sistema deve permitir converter coleta normal em coleta composta (80 itens)
- AC 2.3.2: Sistema deve remover coleta normal duplicada (71 itens)
- AC 2.3.3: Deve criar registros na `tabela_coleta_componente` para cada componente
- AC 2.3.4: Deve manter histórico da correção (data, usuário, observações)
- AC 2.3.5: Deve permitir revisão antes de confirmar a correção

### 2.4 Alerta para Coletas Pendentes de Componentes
**Como** coletor,  
**Quero** ser alertado quando itens compostos tiverem componentes pendentes,  
**Para** garantir coleta completa.

**Acceptance Criteria:**
- AC 2.4.1: Sistema deve mostrar alerta visual quando itens compostos tiverem componentes não coletados
- AC 2.4.2: Alerta deve indicar quais componentes faltam
- AC 2.4.3: Alerta deve estar presente na tela de coleta e no dashboard

---

## 3. Dados Atuais

### 3.1 Itens Compostos Coletados Normalmente

**Total:** 106 itens compostos coletados como coletas normais

**Situação Real:**
- **80 itens (75.5%)**: Apenas coleta normal (sem componentes)
- **71 itens (24.5%)**: Coleta normal + coleta composta (duplicados)

**Exemplos:**
| Patrimônio | Descrição | Coleta ID | Tipo | Componentes |
|------------|-----------|-----------|------|-------------|
| 303752 | CONJUNTO ESCOLAR | 6946 | APENAS NORMAL | 2 |
| 303758 | CONJUNTO ESCOLAR | 1763 | DUPLO | 2 |
| 303765 | CONJUNTO ESCOLAR | 1766 | DUPLO | 2 |
| 303775 | CONJUNTO ESCOLAR | 8339 | APENAS NORMAL | 2 |
| 303777 | CONJUNTO ESCOLAR | 1771 | DUPLO | 2 |
| 304020 | OSCILOSCÓPIO | 6989 | APENAS NORMAL | 4 |
| 304021 | OSCILOSCÓPIO | 6973 | APENAS NORMAL | 4 |
| 304022 | OSCILOSCÓPIO | 6904 | APENAS NORMAL | 4 |
| 304023 | OSCILOSCÓPIO | 6968 | APENAS NORMAL | 4 |
| 304024 | OSCILOSCÓPIO | 6990 | APENAS NORMAL | 4 |
| 304025 | OSCILOSCÓPIO | 6978 | APENAS NORMAL | 4 |

### 3.2 Estatísticas de Itens Compostos

| Métrica | Valor |
|---------|-------|
| Total de itens compostos cadastrados | 822 |
| Total de componentes cadastrados | 1.670 |
| Itens compostos totalmente coletados | 179 |
| Itens compostos coletados normalmente (INCORRETO) | 106 |
| Itens compostos com coleta parcial | 537 |
| Itens com APENAS coleta normal | 80 (75.5%) |
| Itens com coleta DUPLA (normal + composta) | 71 (24.5%) |

---

## 4. Metodologia de Detecção

### 4.1 Query para Identificar Itens Compostos Coletados Normalmente

```sql
SELECT 
    p.id as patrimonio_id,
    p.numero as numero_patrimonio,
    p.descricao,
    c.id as coleta_id,
    c.data_coleta,
    c.metodo_coleta,
    c.localizacao_encontrada,
    c.estado_encontrado,
    COUNT(ic.id) as total_componentes,
    COUNT(cc.id) as componentes_coletados
FROM tabela_patrimonio p
INNER JOIN tabela_coleta c ON p.id = c.id_patrimonio
INNER JOIN tabela_item_composto ic ON p.id = ic.id_patrimonio_principal
LEFT JOIN tabela_coleta_componente cc ON ic.id = cc.id_item_composto AND cc.id_inventario = c.id_inventario
WHERE c.id_inventario = 2
GROUP BY p.id, p.numero, p.descricao, c.id, c.data_coleta, c.metodo_coleta, c.localizacao_encontrada, c.estado_encontrado
HAVING COUNT(ic.id) > 0
ORDER BY p.numero;
```

### 4.2 Classificação de Itens Compostos

| Status | Descrição | Query |
|--------|-----------|-------|
| **Completamente Coletado** | Todos os componentes coletados | `COUNT(cc.id) = COUNT(ic.id)` |
| **Parcialmente Coletado** | Alguns componentes coletados | `COUNT(cc.id) > 0 AND COUNT(cc.id) < COUNT(ic.id)` |
| **Não Coletado** | Nenhum componente coletado | `COUNT(cc.id) = 0` |
| **Coletado Normalmente** | Patrimônio coletado, mas não como composto | `tabela_coleta` existe, mas `tabela_coleta_componente` vazio |

---

## 5. Plano de Implementação

### Fase 1: Detecção e Relatório (Prioridade Alta)

**Objetivo:** Identificar todos os itens compostos coletados incorretamente

**Tasks:**
1. Criar query SQL para identificar itens compostos coletados normalmente
2. Criar método no `RelatorioColetaDAO`
3. Criar endpoint REST para exportar relatório
4. Gerar relatório em CSV/PDF

**Entregáveis:**
- Relatório completo de itens coletados incorretamente
- Estatísticas por tipo de item composto

### Fase 2: Prevenção (Prioridade Alta)

**Objetivo:** Impedir que novos itens compostos sejam coletados normalmente

**Tasks:**
1. Adicionar validação no `ColetaService` antes de salvar
2. Verificar se patrimônio é composto
3. Mostrar mensagem de erro e redirecionar para fluxo composto
4. Adicionar validação no frontend (ColetaFrame)

**Entregáveis:**
- Prevenção de coletas incorretas em tempo real
- Mensagens claras para usuário

### Fase 3: Correção de Dados (Prioridade Média)

**Objetivo:** Corrigir coletas já realizadas incorretamente

**Tasks:**
1. Criar serviço de conversão de coleta normal para composta
2. Criar interface para revisão e confirmação
3. Manter histórico de correções
4. Validar integridade após correção

**Entregáveis:**
- Ferramenta de correção de dados
- Histórico de auditoria

### Fase 4: Alertas e Dashboard (Prioridade Média)

**Objetivo:** Alertar sobre itens compostos com componentes pendentes

**Tasks:**
1. Criar endpoint para verificar status de itens compostos
2. Adicionar alertas visuais na tela de coleta
3. Criar dashboard com estatísticas de itens compostos
4. Implementar notificações

**Entregáveis:**
- Alertas em tempo real
- Dashboard de monitoramento

---

## 6. Restrições

### 6.1 Dados Existentes
- 106 coletas de itens compostos já foram realizadas incorretamente
- Necessário processo de correção manual ou semi-automático
- Histórico deve ser mantido para auditoria

### 6.2 Fluxo de Coleta
- Coleta normal: `tabela_coleta` apenas
- Coleta composta: `tabela_coleta` + `tabela_coleta_componente`
- Não pode haver ambiguidade entre os dois fluxos

### 6.3 Performance
- Validação deve ser rápida (< 100ms)
- Relatórios devem ser gerados em < 5s
- Alertas não devem impactar performance da coleta

---

## 7. Glossário

- **Item Composto:** Patrimônio formado por múltiplos componentes (ex: conjunto escolar)
- **Coleta Normal:** Registro apenas na `tabela_coleta`
- **Coleta Composta:** Registro na `tabela_coleta` + `tabela_coleta_componente`
- **Componente:** Parte individual de um item composto (ex: mesa, cadeira)
- **Obrigatório:** Componente que deve ser coletado para considerar o item completo

---

**Versão:** 1.0.0  
**Data:** 11/02/2026  
**Status:** ✅ Requirements Completos
