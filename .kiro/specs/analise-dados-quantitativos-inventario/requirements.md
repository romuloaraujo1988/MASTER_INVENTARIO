# Análise de Dados Quantitativos do Inventário Patrimonial - Requirements

## 1. Visão Geral

### 1.1 Objetivo
Realizar análise quantitativa completa dos dados do inventário patrimonial 2024/2025 do IFMT Campus Primavera do Leste, com foco em:
- Estado de conservação real dos patrimônios coletados
- Métricas de eficiência do processo de coleta
- Análise espacial (distribuição por salas/setores)
- Divergências e inconsistências
- Dados para artigo científico e TCC

### 1.2 Contexto
O sistema SIHCP realizou inventário em **2025** com **8.639 coletas** de **8.533 patrimônios únicos**, gerando dados ricos sobre:
- Estado de conservação encontrado vs. cadastrado
- Tempo de coleta por item
- Divergências de localização
- Itens sem etiqueta
- Distribuição espacial dos bens

### 1.3 Dados Disponíveis

#### Tabela: tabela_coleta (8.639 registros)
**Campos Críticos:**
- `estado_encontrado` - Estado real do patrimônio (BOM, IRRECUPERÁVEL, RECUPERÁVEL, OCIOSO)
- `divergencia` - Boolean indicando divergência de localização
- `tempo_coleta_segundos` - Tempo total da coleta
- `tempo_scan_segundos` - Tempo de escaneamento QR
- `tempo_preenchimento_segundos` - Tempo de preenchimento do formulário
- `metodo_coleta` - QR_CODE, MANUAL, SEM_ETIQUETA
- `qualidade_etiqueta` - BOA, DANIFICADA, ILEGIVEL
- `sem_etiqueta` - Boolean para itens sem identificação
- `localizacao_encontrada` vs `localizacao_atual` - Divergências espaciais

#### Tabela: tabela_patrimonio (11.570 registros)
**Campos Críticos:**
- `estado_conservacao` - Estado cadastrado (maioria vazio)
- `valor_aquisicao` - Valor original
- `valor_depreciado` - Valor atual
- `data_entrada` - Data de aquisição
- `categoria` - Tipo do bem

#### Tabela: tabela_sala (múltiplas salas)
**Campos Críticos:**
- `tipo_sala` - Laboratório, Administrativa, Biblioteca, etc.
- `bloco`, `andar` - Localização física
- `area_m2` - Área da sala

---

## 2. User Stories

### 2.1 Análise de Estado de Conservação
**Como** pesquisador,  
**Quero** analisar o estado de conservação real dos patrimônios,  
**Para** identificar taxa de deterioração e necessidade de manutenção/substituição.

**Acceptance Criteria:**
- AC 2.1.1: Sistema deve consolidar estados de conservação (normalizar IRRECUPERAVEL/IRRECUPERÁVEL)
- AC 2.1.2: Calcular percentuais por estado: BOM, IRRECUPERÁVEL, RECUPERÁVEL, OCIOSO
- AC 2.1.3: Comparar estado cadastrado vs. estado encontrado
- AC 2.1.4: Gerar estatísticas por categoria de patrimônio
- AC 2.1.5: Identificar patrimônios com maior taxa de deterioração

### 2.2 Análise de Eficiência de Coleta
**Como** gestor do inventário,  
**Quero** analisar métricas de tempo e eficiência,  
**Para** otimizar processos futuros e identificar gargalos.

**Acceptance Criteria:**
- AC 2.2.1: Calcular tempo médio de coleta por método (QR, Manual, Sem Etiqueta)
- AC 2.2.2: Analisar tempo de scan vs. tempo de preenchimento
- AC 2.2.3: Identificar salas com maior tempo médio de coleta
- AC 2.2.4: Correlacionar qualidade da etiqueta com tempo de coleta
- AC 2.2.5: Calcular taxa de coletas por hora/dia

### 2.3 Análise Espacial
**Como** pesquisador,  
**Quero** analisar distribuição espacial dos patrimônios,  
**Para** identificar concentração de bens e padrões de deterioração por ambiente.

**Acceptance Criteria:**
- AC 2.3.1: Calcular densidade de patrimônios por sala (itens/m²)
- AC 2.3.2: Identificar salas com maior concentração de bens
- AC 2.3.3: Analisar estado de conservação por tipo de sala
- AC 2.3.4: Mapear divergências de localização por setor
- AC 2.3.5: Calcular taxa de ocupação por bloco/andar

### 2.4 Análise de Divergências
**Como** auditor,  
**Quero** analisar divergências encontradas,  
**Para** identificar problemas de gestão e controle patrimonial.

**Acceptance Criteria:**
- AC 2.4.1: Calcular taxa de divergências (17.7% = 1.529/8.639)
- AC 2.4.2: Classificar tipos de divergência (localização, estado, etc.)
- AC 2.4.3: Identificar salas com maior taxa de divergência
- AC 2.4.4: Analisar correlação entre divergência e estado de conservação
- AC 2.4.5: Gerar relatório de itens não localizados

### 2.5 Análise de Itens Sem Etiqueta
**Como** gestor patrimonial,  
**Quero** analisar itens sem etiqueta identificados,  
**Para** planejar ações de regularização.

**Acceptance Criteria:**
- AC 2.5.1: Quantificar itens sem etiqueta (106 registros)
- AC 2.5.2: Classificar por categoria
- AC 2.5.3: Estimar valor total de itens não identificados
- AC 2.5.4: Identificar salas com maior incidência
- AC 2.5.5: Propor plano de regularização

### 2.6 Análise Financeira
**Como** gestor financeiro,  
**Quero** analisar valores dos patrimônios,  
**Para** calcular depreciação real e valor patrimonial atualizado.

**Acceptance Criteria:**
- AC 2.6.1: Calcular valor total de aquisição
- AC 2.6.2: Calcular valor depreciado total
- AC 2.6.3: Estimar valor de bens irrecuperáveis
- AC 2.6.4: Calcular taxa de depreciação por categoria
- AC 2.6.5: Projetar necessidade de investimento em reposição

### 2.7 Geração de Relatórios para Artigo Científico
**Como** pesquisador acadêmico,  
**Quero** gerar relatórios estatísticos formatados,  
**Para** incluir no artigo científico e TCC.

**Acceptance Criteria:**
- AC 2.7.1: Gerar tabelas estatísticas em formato LaTeX
- AC 2.7.2: Criar gráficos de distribuição (PNG/SVG)
- AC 2.7.3: Calcular intervalos de confiança (95%)
- AC 2.7.4: Realizar testes estatísticos (qui-quadrado, ANOVA)
- AC 2.7.5: Gerar dataset CSV para análise em R/Python

---

## 3. Dados Quantitativos Atuais

### 3.1 Resumo Geral do Inventário 2025
```
Total de Coletas: 8.639
Patrimônios Únicos Coletados: 8.533
Itens Sem Etiqueta: 106 (1.23%)
Coletas com Divergência: 1.529 (17.7%)
Tempo Médio de Coleta: 6.54 segundos
Período: Novembro 2024 - Janeiro 2026
Ano de Referência: 2025
```

### 3.2 Distribuição por Estado de Conservação
```
BOM:            7.240 (83.81%)
IRRECUPERÁVEL:  1.130 (13.08%) [740 + 390 normalizado]
RECUPERÁVEL:       30 (0.35%)  [18 + 12 normalizado]
OCIOSO:            89 (1.03%)
PENDENTE:          81 (0.94%)
N/A:               68 (0.79%)
COLETADO:           1 (0.01%)
```

### 3.3 Top 5 Salas por Volume de Coletas
```
1. BIBLIOTECA(IFMT - PDL):        4.151 coletas (48.05%)
2. Secretaria:                    1.195 coletas (13.83%)
3. AUDITÓRIO(IFMT - PDL):           218 coletas (2.52%)
4. GABINETE (IFMT - PDL):           193 coletas (2.23%)
5. MEZANINO AUDITORIO(IFMT - PDL):  185 coletas (2.14%)
```

### 3.4 Análise da Secretaria (Caso Crítico)
```
Total de Coletas: 1.195
Estado BOM: 83 (6.95%)
Estado IRRECUPERÁVEL: 1.110 (92.89%) ⚠️ CRÍTICO
Divergências: 439 (36.74%)
Tempo Médio: 16.68 segundos (2.5x a média geral)
```

### 3.5 Patrimônios Cadastrados vs. Coletados
```
Total Cadastrado: 11.570
Total Coletado: 8.533 (73.75%)
Não Localizado: 3.037 (26.25%)
```

---

## 4. Metodologias de Análise

### 4.1 Normalização de Dados
**Problema:** Inconsistências ortográficas (IRRECUPERAVEL vs IRRECUPERÁVEL)

**Solução:**
```sql
-- Normalizar estados de conservação
CASE 
    WHEN estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
    WHEN estado_encontrado IN ('RECUPERAVEL', 'RECUPERÁVEL') THEN 'RECUPERÁVEL'
    ELSE estado_encontrado
END as estado_normalizado
```

### 4.2 Cálculo de Métricas de Eficiência
```sql
-- Tempo médio por método de coleta
SELECT 
    metodo_coleta,
    COUNT(*) as total,
    ROUND(AVG(tempo_coleta_segundos), 2) as tempo_medio,
    ROUND(STDDEV(tempo_coleta_segundos), 2) as desvio_padrao,
    MIN(tempo_coleta_segundos) as tempo_min,
    MAX(tempo_coleta_segundos) as tempo_max
FROM tabela_coleta
WHERE tempo_coleta_segundos IS NOT NULL
GROUP BY metodo_coleta
```

### 4.3 Análise de Divergências
```sql
-- Taxa de divergência por sala
SELECT 
    s.descricao,
    COUNT(c.id) as total_coletas,
    COUNT(CASE WHEN c.divergencia = true THEN 1 END) as divergencias,
    ROUND(COUNT(CASE WHEN c.divergencia = true THEN 1 END) * 100.0 / COUNT(c.id), 2) as taxa_divergencia
FROM tabela_sala s
INNER JOIN tabela_patrimonio p ON s.id_sala = p.id_sala
INNER JOIN tabela_coleta c ON p.id = c.id_patrimonio
GROUP BY s.id_sala, s.descricao
HAVING COUNT(c.id) > 10
ORDER BY taxa_divergencia DESC
```

### 4.4 Análise Financeira
```sql
-- Valor total por estado de conservação
SELECT 
    CASE 
        WHEN c.estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
        WHEN c.estado_encontrado IN ('RECUPERAVEL', 'RECUPERÁVEL') THEN 'RECUPERÁVEL'
        ELSE c.estado_encontrado
    END as estado,
    COUNT(*) as quantidade,
    SUM(p.valor_aquisicao) as valor_aquisicao_total,
    SUM(p.valor_depreciado) as valor_depreciado_total,
    ROUND(AVG(p.valor_aquisicao), 2) as valor_medio
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
WHERE c.estado_encontrado IS NOT NULL
GROUP BY estado
ORDER BY quantidade DESC
```

### 4.5 Análise Temporal
```sql
-- Distribuição de coletas por período do dia
SELECT 
    periodo_coleta,
    COUNT(*) as total_coletas,
    ROUND(AVG(tempo_coleta_segundos), 2) as tempo_medio,
    COUNT(CASE WHEN divergencia = true THEN 1 END) as divergencias
FROM tabela_coleta
WHERE periodo_coleta IS NOT NULL
GROUP BY periodo_coleta
ORDER BY 
    CASE periodo_coleta
        WHEN 'MANHA' THEN 1
        WHEN 'TARDE' THEN 2
        WHEN 'NOITE' THEN 3
    END
```

---

## 5. Requisitos Técnicos

### 5.1 Ferramentas de Análise
- PostgreSQL para queries analíticas
- Python/Pandas para processamento de dados
- R para análises estatísticas avançadas
- Matplotlib/Seaborn para visualizações
- LaTeX para formatação de tabelas científicas

### 5.2 Outputs Esperados
1. **Relatório Executivo** (PDF)
   - Resumo executivo
   - Principais achados
   - Recomendações

2. **Relatório Técnico** (PDF)
   - Metodologia detalhada
   - Análises estatísticas
   - Tabelas e gráficos

3. **Dataset Processado** (CSV)
   - Dados normalizados
   - Métricas calculadas
   - Pronto para análise em R/Python

4. **Artigo Científico** (LaTeX)
   - Tabelas formatadas
   - Gráficos em alta resolução
   - Referências bibliográficas

5. **Apresentação** (PowerPoint/PDF)
   - Slides com principais resultados
   - Gráficos e infográficos

---

## 6. Prioridades

### Alta Prioridade
- AC 2.1.1 a 2.1.5: Análise de estado de conservação
- AC 2.4.1 a 2.4.5: Análise de divergências
- AC 2.7.1 a 2.7.5: Relatórios para artigo científico

### Média Prioridade
- AC 2.2.1 a 2.2.5: Análise de eficiência
- AC 2.3.1 a 2.3.5: Análise espacial
- AC 2.6.1 a 2.6.5: Análise financeira

### Baixa Prioridade
- AC 2.5.1 a 2.5.5: Análise de itens sem etiqueta

---

## 7. Restrições e Considerações

### 7.1 Qualidade dos Dados
- Estado de conservação cadastrado está vazio em 89.6% dos registros
- Necessário usar `estado_encontrado` da coleta como fonte primária
- Inconsistências ortográficas precisam ser normalizadas

### 7.2 Dados Sensíveis
- Não incluir nomes de responsáveis em publicações
- Anonimizar dados de coletores
- Generalizar localizações específicas se necessário

### 7.3 Limitações Temporais
- Dados referem-se ao inventário 2025 (realizado entre nov/2024 e jan/2026)
- Análise longitudinal limitada (sistema digital recente)

---

## 8. Glossário

- **Divergência**: Diferença entre localização cadastrada e localização encontrada
- **Estado de Conservação**: Condição física do patrimônio (BOM, RECUPERÁVEL, IRRECUPERÁVEL, OCIOSO)
- **Tempo de Coleta**: Tempo total desde scan até finalização do registro
- **Taxa de Localização**: Percentual de patrimônios encontrados vs. cadastrados
- **Densidade Patrimonial**: Número de itens por metro quadrado

---

**Versão:** 1.0.0  
**Data:** 07/02/2026  
**Status:** ✅ Requirements Completos
